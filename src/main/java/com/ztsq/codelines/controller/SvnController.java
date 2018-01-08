package com.ztsq.codelines.controller;

import com.jfinal.core.Controller;
import com.ztsq.codelines.db.SVNAuth;
import com.ztsq.codelines.db.SVNCommitLog;
import com.ztsq.codelines.services.SvnServices;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.util.ArrayList;
import java.util.List;

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
     * 获取 日志提交记录
     * param：
     * auth： 作者
     * startDate: yyyy-MM-dd hh:mm:ss
     * endDate: yyyy-MM-dd hh:mm:ss
     */
    public void commitLogs(){
        String auth = getPara("auth");
        String startDate = getPara("startDate");
        String endDate = getPara("endDate");
    }

    public void showLog(){
        String svnName = getPara("auth");
        String sTime = getPara("startDate");
        String eTime = getPara("endDate");

        List<SVNLogEntry> retRersionList = new ArrayList<SVNLogEntry>();
        try {
            svnServices = new SvnServices();
            retRersionList = svnServices.getLogCommitLogs(svnName,sTime,eTime);
        }catch (Exception e){
            e.printStackTrace();
        }




        SVNAuth authModel = SVNAuth.dao.findFirst("SELECT * FROM svnt_auth WHERE auth = '"+svnName+"' ;");

        if (null != authModel){
            //讲提交记录入库
            for (SVNLogEntry item :retRersionList) {
                new SVNCommitLog()
                        .set("revision_id", item.getRevision())
                        .set("auth_id", authModel.getLong("id"))
                        .set("commit_log", item.getMessage())
                        .set("commit_time", item.getDate())
                        .set("code_lines", 0)
                        .save();


                svnServices.calcAndUpdateCodeLine(item);
            }
        }






        svnServices.closeSvn();
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
