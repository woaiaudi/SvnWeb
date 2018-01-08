package com.ztsq.codelines.utils.fun;

import com.ztsq.codelines.utils.ZTSVNConstanst;
import com.ztsq.codelines.utils.svn.conf.SvnConfig;
import com.ztsq.codelines.utils.svn.factory.TroilaSvn;
import com.ztsq.codelines.utils.svn.inf.ISvn;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ZTMain {
	private static Logger logger = Logger.getLogger(ZTMain.class);
	
	ISvn svn;
	private void testLog() throws Exception {
		// 初始化实例
		TroilaSvn ts = new TroilaSvn(ZTSVNConstanst.account, ZTSVNConstanst.password, ZTSVNConstanst.path);
		// 获得操作对象
		this.svn = ts.execute(SvnConfig.log);
		// 得到版本库信息
		svn.createSVNRepository();
		// 得到基础操作对象
		svn.createSVNClientManager();


		final List<SVNLogEntry> retRersionList = new ArrayList<SVNLogEntry>();
		svn.getCommitHistory(ZTSVNConstanst.path, ZTSVNConstanst.DEV_SVN_START_DATE, ZTSVNConstanst.DEV_SVN_END_DATE, ZTSVNConstanst.DEV_SVN_NAME, new ISVNLogEntryHandler() {

			public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
				if (ZTSVNConstanst.DEV_SVN_NAME.equals(logEntry.getAuthor())) {
					// 这次提交 是这个作者 提交的
					System.out.print("out>提交记录："+logEntry);
			        logger.debug("提交记录："+logEntry);  
					retRersionList.add(logEntry);
				}

			}
		});


		int xxxLine = 0;
		
		for (SVNLogEntry object : retRersionList) {
			List<String> xxx = svn.diffPath(ZTSVNConstanst.path, object);
			xxxLine += (xxx.size());
		}
		

		logger.warn("统计结果：["+ZTSVNConstanst.DEV_SVN_NAME+"] 在 ["+ZTSVNConstanst.DEV_SVN_START_DATE+"] 到 ["+ZTSVNConstanst.DEV_SVN_END_DATE+"] 期间 ，共计代码行数："+xxxLine);  
		// 关闭库容器
		svn.closeRepo();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			new ZTMain().testLog();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
