package com.ztsq.codelines.controller;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.jfinal.core.Controller;
import com.ztsq.codelines.db.SVNAuth;
import com.ztsq.codelines.db.SVNCommitLog;
import com.ztsq.codelines.db.SVNProject;
import com.ztsq.codelines.services.SvnServices;
import com.ztsq.codelines.utils.RespBaseBean;
import com.ztsq.codelines.utils.ZTSVNConstanst;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.text.SimpleDateFormat;
import java.util.*;

public class HelloController extends Controller {

    private static Logger logger = Logger.getLogger(ProjectController.class);

    public void index(){
        renderText("<<>><<<>>>sdsdsd<<>?<><>??");
    }

    public void save(){
        renderJson(new ArrayList<String>());
    }

    public void segword(){

        String orgStr = getPara("str");

        JiebaSegmenter segmenter = new JiebaSegmenter();

        List<SegToken> regList = segmenter.process(orgStr, JiebaSegmenter.SegMode.SEARCH);

        if(null != regList && regList.size() > 0){

            List<String> wordList = new ArrayList<String>();

            for (SegToken item:regList) {
                wordList.add(item.word);
            }
            renderJson(wordList);
        }else {
            renderJson("ERROR");
        }

    }




    private SvnServices svnServices = null;

    /**
     * 获取 日志提交记录
     * param：
     * auth： 作者
     * startDate: yyyy-MM-dd hh:mm:ss
     * endDate: yyyy-MM-dd hh:mm:ss
     */
    public void createData(){



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

        //调用 svnkit 获取 这个 作者 在 时间范围内的 提交记录
        List<SVNLogEntry> retRersionList = new ArrayList<SVNLogEntry>();
        try {
            svnServices = new SvnServices();
            retRersionList = svnServices.getALLLogCommitLogs(time1,time2);
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }






        //讲提交记录入库
        for (SVNLogEntry item :retRersionList) {
            try {

                SVNAuth authModel = SVNAuth.dao.findFirst("SELECT * FROM svnt_auth WHERE auth = '"+item.getAuthor()+"' ;");
                if (null == authModel || authModel.getLong("id") == 0){
                    //不在 默认脚本中的 人员 可能是 产品人员，不需要统计代码
                    continue;
                }

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

            SVNAuth authModel = SVNAuth.dao.findFirst("SELECT * FROM svnt_auth WHERE auth = '"+item.getAuthor()+"' ;");
            if (null == authModel || authModel.getLong("id") == 0){
                //不在 默认脚本中的 人员 可能是 产品人员，不需要统计代码
                continue;
            }


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

            retLogList.add(commitLog);

        }

        svnServices.closeSvn();

        RespBaseBean resp = RespBaseBean.createSuccessResp(retLogList);

        renderJson(resp);

    }

}
