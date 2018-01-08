package com.ztsq.codelines.services;

import com.ztsq.codelines.db.SVNCommitLog;
import com.ztsq.codelines.utils.ZTSVNConstanst;
import com.ztsq.codelines.utils.svn.conf.SvnConfig;
import com.ztsq.codelines.utils.svn.factory.TroilaSvn;
import com.ztsq.codelines.utils.svn.inf.ISvn;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyuework on 2018/1/8.
 */
public class SvnServices {

    private static Logger logger = Logger.getLogger(SvnServices.class);

    private TroilaSvn ts;
    ISvn svn;

    public SvnServices() {
        // 初始化实例
        ts = new TroilaSvn(ZTSVNConstanst.account, ZTSVNConstanst.password, ZTSVNConstanst.path);
        // 获得操作对象
        try {
            svn = ts.execute(SvnConfig.log);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 得到版本库信息
        svn.createSVNRepository();
        // 得到基础操作对象
        svn.createSVNClientManager();
    }


    public List<SVNLogEntry> getLogCommitLogs(final String authName, String startDate, String endDate){
        final List<SVNLogEntry> retRersionList = new ArrayList<SVNLogEntry>();
        svn.getCommitHistory(ZTSVNConstanst.path, startDate, endDate, authName, new ISVNLogEntryHandler() {
            public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
                if (authName.equals(logEntry.getAuthor())) {
                    // 这次提交 是这个作者 提交的
                    logger.debug("提交记录："+logEntry);
                    retRersionList.add(logEntry);
                }
            }
        });

        return retRersionList;
    }


    public void closeSvn(){
        if (null != svn){
            svn.closeRepo();
        }
    }

    public void calcAndUpdateCodeLine(SVNLogEntry version) {
        List<String> changeDetailList = svn.diffPath(ZTSVNConstanst.path, version);

        //更新库里面的记录
        SVNCommitLog commitLog = SVNCommitLog.dao.findFirst("SELECT * FROM svnt_commit_log WHERE revision_id = '"+version.getRevision()+"' ;");
        commitLog.set("code_lines",changeDetailList.size()).update();

    }
}
