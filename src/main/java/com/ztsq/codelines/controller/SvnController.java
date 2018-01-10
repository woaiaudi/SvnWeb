package com.ztsq.codelines.controller;

import com.jfinal.core.Controller;
import com.ztsq.codelines.db.SVNAuth;
import com.ztsq.codelines.db.SVNCommitLog;
import com.ztsq.codelines.services.SvnServices;
import com.ztsq.codelines.utils.RespBaseBean;
import javafx.scene.input.DataFormat;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

import org.apache.log4j.Logger;
/**
 * Created by zyuework on 2018/1/5.
 */
public class SvnController extends Controller {
    private static Logger logger = Logger.getLogger(SvnController.class);

    private SvnServices svnServices = null;


    public void index(){
        renderText("<<>><<<>>>i am SvnController<<>?<><>??");
    }


    /**
     * 获取 svn 作者列表 支持模糊搜索
     */
    public void auths(){
        RespBaseBean resp = null;
        try {
            String searchText = getPara("searchText","");
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
        String svnName = getPara("auth");
        if (null == svnName || svnName.length()<=0){
            renderJson(RespBaseBean.createErrorResp(40,"请指定要查询的开发人员svn账号"));
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

        SimpleDateFormat svnkitFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time1 = svnkitFormatter.format(startTimeDate);
        String time2 = svnkitFormatter.format(endTimeDate);

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
            renderJson(RespBaseBean.createErrorResp(4,"没有查询到svnName的提交记录！"));
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
                        .set("code_lines", 0)
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
                int tmpCode = commitLog.getInt("code_lines");
                if (tmpCode <= 0){
                    //不》0 时 才更新
                    int codeLine = svnServices.calcVersionDiffCodeLine(item);

                    //更新库里面的记录
                    commitLog.set("code_lines",codeLine).update();
                }
            }

            retLogList.add(commitLog);
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
}
