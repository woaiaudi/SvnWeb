package com.ztsq.codelines.controller;

import com.jfinal.core.Controller;
import com.ztsq.codelines.db.SVNAuth;
import com.ztsq.codelines.db.SVNChangePathsLog;
import com.ztsq.codelines.db.SVNCommitLog;
import com.ztsq.codelines.db.SVNProject;
import com.ztsq.codelines.services.SvnServices;
import com.ztsq.codelines.utils.RespBaseBean;
import com.ztsq.codelines.utils.ZTSVNConstanst;
import javafx.scene.input.DataFormat;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.SimpleFormatter;

import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNLogEntryPath;

/**
 * Created by zyuework on 2018/1/5.
 */
public class SvnController extends Controller {
    private static Logger logger = Logger.getLogger(SvnController.class);

    private SvnServices svnServices = null;

    private List<SVNProject> allProject;


    public void index(){
        renderText("<<>><<<>>>i am SvnController<<>?<><>??");
    }


    /**
     * 获取 svn 作者列表 支持模糊搜索
     */
    public void auths(){
        RespBaseBean resp = null;
        try {
            String searchText = (null == getPara("searchText","")?"":getPara("searchText",""));
            List<SVNAuth> userList = SVNAuth.dao.find("SELECT * FROM svnt_auth WHERE auth like '%"+searchText+"%' ;");
            resp = RespBaseBean.createSuccessResp(userList);
        }catch (Exception e){
            resp = RespBaseBean.createErrorResp(400,e.getLocalizedMessage());
        }

        renderJson(resp);
    }





    /**
     * 获取 日志提交记录
     * param：
     * auth： 作者
     * startDate: yyyy-MM-dd hh:mm:ss
     * endDate: yyyy-MM-dd hh:mm:ss
     */
    public void commitLogs(){
        String svnName = (null == getPara("auth")?"":getPara("auth"));
        if (null == svnName || svnName.length()<=0){
            renderJson(RespBaseBean.createErrorResp(40,"请指定要查询的开发人员svn账号"));
            return;
        }

        int projectId = (null == getParaToInt("projectId")?0:getParaToInt("projectId"));
        if (projectId <= 0){
            renderJson(RespBaseBean.createErrorResp(44,"请指定要查询的项目"));
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startTimeDate = null;
        Date endTimeDate = null;

        try {
            startTimeDate = formatter.parse(getPara("startDate"));
        }catch (Exception e){
            renderJson(RespBaseBean.createErrorResp(41,"开始日期设置错误，请设置 yyyy-MM-dd！"));
            return;
        }

        try {
            endTimeDate = formatter.parse(getPara("endDate"));
        }catch (Exception e){
            renderJson(RespBaseBean.createErrorResp(41,"结束日期设置错误，请设置 yyyy-MM-dd！"));
            return;
        }

        if (startTimeDate.getTime() >= endTimeDate.getTime()){
            renderJson(RespBaseBean.createErrorResp(41,"开始时间不能在结束时间之后。"));
            return;
        }
        long timeRangeLong = endTimeDate.getTime()-startTimeDate.getTime();
        if (timeRangeLong > ZTSVNConstanst.DEV_MAX_DAY_4_SEARCH*24*60*60*1000){
            renderJson(RespBaseBean.createErrorResp(41,"抱歉，时间范围最长为"+ZTSVNConstanst.DEV_MAX_DAY_4_SEARCH+"天"));
            return;
        }


        SimpleDateFormat svnkitFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time1 = svnkitFormatter.format(startTimeDate);
        String time2 = svnkitFormatter.format(endTimeDate);

        //要查询的 项目
        SVNProject findProject = SVNProject.dao.findFirst("SELECT id FROM svnt_project where id = '"+projectId+"' ;");
        if (null == findProject){
            renderJson(RespBaseBean.createErrorResp(41,"未能找到相应的项目，请先添加项目！"));
            return;
        }

        allProject = SVNProject.dao.find("SELECT * FROM svnt_project ;");


        //调用 svnkit 获取 这个 作者 在 时间范围内的 提交记录
        List<SVNLogEntry> retRersionList = new ArrayList<SVNLogEntry>();
        try {
            svnServices = new SvnServices();
            retRersionList = svnServices.getLogCommitLogs(svnName,time1,time2);
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }

        if (null == retRersionList || retRersionList.isEmpty()){
            renderJson(RespBaseBean.createErrorResp(4,"在指定时间范围内，没有查询到"+svnName+"的提交记录！"));
            svnServices.closeSvn();
            return;
        }

        SVNAuth authModel = SVNAuth.dao.findFirst("SELECT * FROM svnt_auth WHERE auth = '"+svnName+"' ;");
        if (null == authModel || authModel.getLong("id") == 0){
            //往 数据库中添加用户（因为 用svnkit 能查询到这个人的 提交记录）
           boolean insertFlag = new SVNAuth().set("auth", svnName).set("mark", "").save();
           logger.info("向 数据库中 新增用户【"+svnName+"】结果："+(insertFlag?"成功":"失败"));

            //添加完成后，再查询一遍
            authModel = SVNAuth.dao.findFirst("SELECT * FROM svnt_auth WHERE auth = '"+svnName+"' ;");
        }


        //讲提交记录入库
        for (SVNLogEntry item :retRersionList) {
            try {
                new SVNCommitLog()
                        .set("revision_id", item.getRevision())
                        .set("auth_id", authModel.getLong("id"))
                        .set("commit_log", item.getMessage())
                        .set("commit_time", item.getDate())
                        .set("is_branch", 0)
                        .set("code_lines", 0)
                        .set("project_id", 0)
                        .save();
            }catch (Exception e){
                logger.error(e);
                //添加 失败 可能因为 库中已经存在了提交记录
            }
        }


        List<SVNCommitLog> retLogList = new ArrayList<SVNCommitLog>();

        for (SVNLogEntry item :retRersionList) {
            //先查询库里面 这条记录 的 codeline 是否 》0
            SVNCommitLog commitLog = SVNCommitLog.dao.findFirst("SELECT * FROM svnt_commit_log WHERE revision_id = '"+item.getRevision()+"' ;");
            if (null != commitLog){
                //更新 是否是分支
                Map<String,SVNLogEntryPath> changePaths = item.getChangedPaths();
                Iterator iter = changePaths.entrySet().iterator();
                if (changePaths.size() == 1 && iter.hasNext()){
                    Map.Entry entry = (Map.Entry) iter.next();
                    SVNLogEntryPath tmpValue = (SVNLogEntryPath) entry.getValue();
                    if (changePaths.size() == 1
                            && tmpValue.getCopyRevision() > 0
                            && null != tmpValue.getCopyPath()
                            && tmpValue.getCopyPath().length() > 0){
                        //这次提交 是从 其他地方复制来的 ，可以理解为是拉分支。
                        //此时修改 对应记录
                        commitLog.set("is_branch",1).update();
                    }
                }


                //更新 所属项目
                int tmpProjectId = 0;
                try {
                    tmpProjectId = commitLog.getInt("project_id");
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (tmpProjectId <= 0 ){

                    //之前没有 所属项目 是 才执行更新操作
                    SVNProject project = queryProjectIdByChange(item);
                    if (null != project && project.getInt("id") > 0){
                        //更新库里面的记录
                        commitLog.set("project_id",project.getInt("id")).update();
                    }
                }

                if (commitLog.getInt("is_branch") != 1){
                    //不是分支的时候，才开始计算代码行数

                    //更新 代码行数
                    int tmpCode = 0;
                    if (null != commitLog.getInt("code_lines")){
                        tmpCode = commitLog.getInt("code_lines");
                    }

                    if (tmpCode <= 0){
                        //不》0 时 才更新
                        int codeLine = svnServices.calcVersionDiffCodeLine(item);

                        //更新库里面的记录
                        commitLog.set("code_lines",codeLine).update();
                    }
                }
            }


            //只有 是当前要 查询的项目，才返回结果
            if (commitLog.getInt("project_id") == findProject.getInt("id")){
                retLogList.add(commitLog);
            }

        }

        svnServices.closeSvn();

        RespBaseBean resp = RespBaseBean.createSuccessResp(retLogList);

        renderJson(resp);

    }


    private SVNCommitLog lastLog(String authName){
        SVNCommitLog retCommitLog = null;

        SVNAuth authModel = SVNAuth.dao.findFirst("SELECT * FROM svnt_auth WHERE auth = '"+authName+"' ;");
        if (null != authModel){
            //先查 数据库中 这个人维护的 最新 commit 版本是多少
            SVNCommitLog lastCommit =  SVNCommitLog.dao.findFirst("SELECT * FROM svnt_commit_log WHERE auth_id = "+authModel.get("auth_id","")+" ;");
            if (null != lastCommit){
                retCommitLog = lastCommit;
            }
        }

        return retCommitLog;
    }


    /**
     * 从提交记录中  判断，这次提交属于哪一个项目
     * @param logEntry
     * @return
     */
    private SVNProject queryProjectIdByChange(SVNLogEntry logEntry){
        SVNProject retProj = null;
        Map<String,SVNLogEntryPath> changePaths = logEntry.getChangedPaths();


        Iterator iter = changePaths.entrySet().iterator();		//获取key和value的set
        if (iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();		//把hashmap转成Iterator再迭代到entry
            String tmpKey = (String) entry.getKey();
            String fullPath = ZTSVNConstanst.path + tmpKey;

            for (SVNProject itemProj:allProject) {
                String itemPath = itemProj.get("path","");
                if (fullPath.indexOf(itemPath) >= 0){
                    //找到这个proj
                    retProj = itemProj;
                }
            }
        }

        logger.debug(retProj);
        return retProj;

    }
}
