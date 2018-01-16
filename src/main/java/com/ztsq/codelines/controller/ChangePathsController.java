package com.ztsq.codelines.controller;

import com.jfinal.core.Controller;
import com.ztsq.codelines.db.SVNChangePathsLog;
import com.ztsq.codelines.db.SVNCommitLog;
import com.ztsq.codelines.services.SvnServices;
import com.ztsq.codelines.utils.RespBaseBean;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zyuework on 2018/1/16.
 */
public class ChangePathsController  extends Controller {

    private static Logger logger = Logger.getLogger(ChangePathsController.class);

    private SvnServices svnServices = null;

    public void query(){
        long revision_id = (null == getParaToLong("revisionId")?0l:getParaToLong("revisionId"));

        if (revision_id <= 0){
            renderJson(RespBaseBean.createErrorResp(43,"没有查询到对应ID的提交记录！"));
            return;
        }

        List<SVNChangePathsLog> reqList = SVNChangePathsLog.dao.find("SELECT * FROM svnt_changed_paths_log WHERE revision_id = "+revision_id+" ;");

        if (null != reqList){
            renderJson(RespBaseBean.createSuccessResp(reqList));
            return;
        }else {
            renderJson(RespBaseBean.createErrorResp(43,"查询记录失败！"));
            return;
        }
    }


    public void refresh(){
        long revision_id = (null == getParaToLong("revisionId")?0l:getParaToLong("revisionId"));

        if (revision_id <= 0){
            renderJson(RespBaseBean.createErrorResp(43,"没有查询到对应ID的提交记录！"));
            return;
        }

        SVNCommitLog svnlog = SVNCommitLog.dao.findFirst("SELECT * FROM svnt_commit_log WHERE revision_id = "+revision_id+" ;");
        if (null == svnlog){
            renderJson(RespBaseBean.createErrorResp(43,"没有查询到对应ID的提交记录！"));
            return;
        }

        //删除表里 现有的记录
        List<SVNChangePathsLog> reqList = SVNChangePathsLog.dao.find("SELECT * FROM svnt_changed_paths_log WHERE revision_id = "+revision_id+" ;");

        if (null != reqList && !reqList.isEmpty()){
            for (SVNChangePathsLog item:reqList) {
                item.delete();
            }
        }

        if (null == svnlog.getDate("commit_time")){
            renderJson(RespBaseBean.createErrorResp(43,"没有查到该ID对应的提交日期！"));
            return;
        }
        Date commit = svnlog.getDate("commit_time");

        Date timeDate1 = new Date(commit.getTime()- 60*1000);//开始时间 为 提交时间 前一分钟
        Date timeDate2 = new Date(commit.getTime()+ 60*1000);//结束时间 为 提交时间 后一分钟

        SimpleDateFormat svnkitFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time1 = svnkitFormatter.format(timeDate1);
        String time2 = svnkitFormatter.format(timeDate2);

        //svnkit执行 查询commit
        List<SVNLogEntry> retRersionList = new ArrayList<SVNLogEntry>();
        try {
            svnServices = new SvnServices();

            retRersionList = svnServices.getALLLogCommitLogs(time1,time2);

        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            for (SVNLogEntry item:retRersionList) {
                if (item.getRevision() == svnlog.getLong("revision_id")){
                    //筛选对应的提交记录
                    Map<String,SVNLogEntryPath> changePaths = item.getChangedPaths();
                    Iterator iter = changePaths.entrySet().iterator();
                    while (iter.hasNext()){
                        Map.Entry entry = (Map.Entry) iter.next();
                        SVNLogEntryPath tmpValue = (SVNLogEntryPath) entry.getValue();
                        new SVNChangePathsLog()
                                .set("revision_id",revision_id)
                                .set("type",String.valueOf(tmpValue.getType()))
                                .set("path",tmpValue.getPath())
                                .set("copy_revision",tmpValue.getCopyRevision())
                                .set("copy_path",tmpValue.getCopyPath())
                                .save();
                    }
                }
            }

            renderJson(RespBaseBean.createSuccessResp("更新成功，请重新获取数据！"));
            return;
        }catch (Exception e){
            renderJson(RespBaseBean.createErrorResp(402,e.getLocalizedMessage()));
            return;
        }

    }
}
