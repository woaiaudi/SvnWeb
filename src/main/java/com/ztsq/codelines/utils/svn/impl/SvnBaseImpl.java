package com.ztsq.codelines.utils.svn.impl;

import com.ztsq.codelines.utils.ZTSVNConstanst;
import com.ztsq.codelines.utils.svn.conf.ErrorVar;
import com.ztsq.codelines.utils.svn.conf.SvnConfig;
import com.ztsq.codelines.utils.svn.impl.service.SvnServiceImpl;
import com.ztsq.codelines.utils.svn.inf.ISvn;
import com.ztsq.codelines.utils.svn.inf.service.ISvnDbLog;
import com.ztsq.codelines.utils.svn.inf.service.ISvnService;
import com.ztsq.codelines.utils.svn.model.SvnRepoPojo;
import com.ztsq.codelines.utils.svn.tools.StrOutputStrean;
import com.ztsq.codelines.utils.svn.tools.StringOutputSteam;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * {@link ISvnService}
 * 
 * @author Allen
 * @date 2016年8月8日
 *
 */
public class SvnBaseImpl extends SvnServiceImpl implements ISvn {
	private static Logger logger = Logger.getLogger(SvnBaseImpl.class);

	public SvnBaseImpl(String account, String password, boolean logStatus, String repoPath) {
		super(account, password, logStatus, repoPath);
	}

	@SuppressWarnings("unchecked")
	public List<SvnRepoPojo> getRepoCatalog(String openPath) {
		try {
			if (repository == null)
				throw new Exception(ErrorVar.SVNRepository_is_null);
			Collection<SVNDirEntry> entries = repository.getDir(openPath, -1, null, (Collection<SVNDirEntry>) null);
			List<SvnRepoPojo> svns = new ArrayList<SvnRepoPojo>();
			Iterator<SVNDirEntry> it = entries.iterator();
			while (it.hasNext()) {
				SVNDirEntry entry = it.next();
				SvnRepoPojo svn = new SvnRepoPojo();
				svn.setCommitMessage(entry.getCommitMessage());
				svn.setDate(entry.getDate());
				svn.setKind(entry.getKind().toString());
				svn.setName(entry.getName());
				svn.setRepositoryRoot(entry.getRepositoryRoot().toString());
				svn.setRevision(entry.getRevision());
				svn.setSize(entry.getSize() / 1024);
				svn.setUrl(openPath.equals("") ? new StringBuffer("/").append(entry.getName()).toString() : new StringBuffer(openPath).append("/").append(entry.getName()).toString());
				svn.setAuthor(entry.getAuthor());
				svn.setState(svn.getKind() == "file" ? null : "closed");
				svns.add(svn);
			}
			super.log("获得版本库文件信息");
			return svns;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			super.log(e);
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean checkOut(String checkUrl, String savePath) {
		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		try {
			if (savePath == null || savePath.trim().equals(""))
				throw new Exception(ErrorVar.Path_no_having);
			else if (checkUrl == null || checkUrl.trim().equals(""))
				throw new Exception(ErrorVar.Url_no_having);
			File save = new File(savePath);
			if (!save.isDirectory())
				save.mkdir();
			updateClient.doCheckout(SVNURL.parseURIEncoded(checkUrl), save, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
			super.log("检出版本库信息");
			return true;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 此实现自动对Add后的数据进行提交
	 */
	public <T> boolean add(String[] paths, String message, boolean uLocks, ISvnDbLog<? extends T> isvnLog) {
		try {
			File[] files = checkFilePaths(paths);
			files = sortF_S(files);
			SVNStatus status;
			List<File> targetList = new ArrayList<File>();
			List<List<File>> fileList = bindFile(files);
			for (int i = 0; i < fileList.size(); i++) {
				for (File f : fileList.get(i)) {
					if ((status = clientManager.getStatusClient().doStatus(f, true, true)) != null && status.getContentsStatus() != SVNStatusType.STATUS_UNVERSIONED
							&& status.getContentsStatus() != (SVNStatusType.STATUS_NONE))
						continue;
					else if (f.isFile()) {
						clientManager.getWCClient().doAdd(f, true, false, true, SVNDepth.fromRecurse(true), false, false, true);
						targetList.add(f);
						super.log("添加文件到提交队列");
					} else if (f.isDirectory()) {
						// SVNDepth.empty 保证不递归文件夹下文件
						clientManager.getWCClient().doAdd(f, false, false, false, SVNDepth.EMPTY, false, false, false);
						targetList.add(f);
						super.log("添加文件夹到提交队列");
					}
				}
			}
			long versionId = commit(targetList.toArray(new File[targetList.size()]), message, uLocks);
			if (versionId == -1)
				throw new Exception(ErrorVar.Commit_error);
			if (!isvnLog.addLog(this.svnAccount, SvnConfig.add, versionId, files))
				throw new Exception(ErrorVar.AddDbLog_error);
			return true;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Long commit(File[] files, String message, boolean uLocks) {
		try {
			if (files.length == 0) {
				super.log("无效的提交信息");
				return -1l;
			}
			long versionId = clientManager.getCommitClient().doCommit(files, uLocks, message, null, null, false, false, SVNDepth.INFINITY).getNewRevision();
			super.log("提交队列中预处理的操作操作  => 版本号: " + versionId);
			return versionId;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1l;
	}

	public <T> boolean delete(String[] paths, boolean localDelete, String message, boolean uLock, ISvnDbLog<? extends T> isvnLog) {
		try {
			File[] files = checkFilePaths(paths);
			files = sortS_F(files);
			SVNStatus status = null;
			{
				List<File> targetList = new ArrayList<File>();
				List<List<File>> fileList = bindFile(files);
				for (int i = fileList.size() - 1; i >= 0; i--) {
					for (File f : fileList.get(i)) {
						if ((status = clientManager.getStatusClient().doStatus(f, true, true)) == null)
							throw new Exception(ErrorVar.File_Repo_no_having);
						else if (status.getContentsStatus() != SVNStatusType.STATUS_NORMAL)
							throw new Exception(ErrorVar.Repo_Status_error + status.getContentsStatus().toString());
						else {
							clientManager.getWCClient().doDelete(f, false, localDelete, false);
							if (f.isFile())
								super.log("添加文件到删除队列");
							else
								super.log("添加文件夹到删除队列");
							targetList.add(f);
						}
					}
				}
				long versionId = commit(targetList.toArray(new File[targetList.size()]), message, uLock);
				if (versionId == -1)
					throw new Exception(ErrorVar.Commit_error);
				if (!isvnLog.addLog(this.svnAccount, SvnConfig.delete, versionId, files))
					throw new Exception(ErrorVar.AddDbLog_error);
			}
			return true;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public <T> boolean update(String path, String message, boolean uLocks, ISvnDbLog<? super T> isvnLog) {
		try {
			File[] files = checkFilePaths(new String[] { path });
			// diffPath(files);
			long[] l = clientManager.getUpdateClient().doUpdate(files, SVNRevision.HEAD, SVNDepth.INFINITY, true, false);
			super.log("更新文件到操作队列");
			long versionId = l[0];// commit(files, message, uLocks);
			if (versionId == -1)
				throw new Exception(ErrorVar.Update_no_change);
			if (!isvnLog.addLog(this.svnAccount, SvnConfig.update, versionId, files))
				throw new Exception(ErrorVar.AddDbLog_error);
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	
	public List<String> diffPath(File file) {
		try {
			if (file == null || !file.exists())
				throw new Exception(ErrorVar.Path_no_having);
			// 获取SVNDiffClient
			SVNDiffClient diffClient = clientManager.getDiffClient();
			diffClient.setIgnoreExternals(false);
			StringOutputSteam os = new StringOutputSteam(new ArrayList<String>());
			diffClient.doDiff(new File[] { file }, SVNRevision.HEAD, SVNRevision.BASE, null, SVNDepth.INFINITY, true, os, null);
			super.log("比对库路径");
			return os.s;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	
	/**
	 * @param svnUrl
	 * @param ver
	 */
	public List<String> diffPath(String svnUrl, SVNLogEntry ver) {
		//比较 这个版本 与 head 的 差异

		//用于返回的 拆散的 代码数组
		List<String> retList = new ArrayList<String>();
		StrOutputStrean os = new StrOutputStrean(new ArrayList<String>());
		try {
			if (svnUrl == null || svnUrl.length() < 3)
				throw new Exception(ErrorVar.Path_no_having);
			// 获取SVNDiffClient
			SVNDiffClient diffClient  = clientManager.getDiffClient();
			diffClient.setIgnoreExternals(false);
			
			//SVNRevision.create(ver.getRevision() - 1 )
			//SVNRevision.PREVIOUS
			SVNRevision startV = SVNRevision.create(ver.getRevision() -1);
			SVNRevision endV = SVNRevision.create(ver.getRevision());
			
			SVNURL tmpSVNURL =  SVNURL.parseURIEncoded(ZTSVNConstanst.path);

			// FIXME: 2018/1/15 这一句耗时最长，考虑如何优化
			diffClient.doDiff(tmpSVNURL, endV,startV, endV, SVNDepth.INFINITY, true, os);

			for (String tmpStr:os.lineList) {
				String[] tmpArr = tmpStr.split("\n");
				retList.addAll(Arrays.asList(tmpArr));
			}

			os.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return retList;
	}
	
	
	public List<String> diffPath(File paths, String startDate, String endDate) {
		List<String> retBuffer = new ArrayList<String>();
		
		try {
			if (paths == null || !paths.exists())
				throw new Exception(ErrorVar.Path_no_having);
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			
			// 获取SVNDiffClient
			SVNDiffClient diffClient = clientManager.getDiffClient();
			
			SVNRevision startRevision = SVNRevision.create(formatter.parse(startDate));
			
			SVNRevision endRevision = SVNRevision.create(formatter.parse(endDate));
			
			diffClient.setIgnoreExternals(false);
			StringOutputSteam os = new StringOutputSteam(new ArrayList<String>());
			diffClient.doDiff(new File[] { paths }, startRevision, endRevision, null, SVNDepth.INFINITY, true, os, null);
			for (String itemFileDiff : os.s) {
				String[] xx = itemFileDiff.split("\n");
				if(null != xx) {
					for (String lineStr : xx) {
						retBuffer.add(new String(lineStr.toCharArray()));
					}
				}
				xx = null;
			}
			return retBuffer;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void getCommitHistory(String svnUrl, String startDate, String endDate, String authName,
			ISVNLogEntryHandler logEntryHandler) {
		try {
			
			if (svnUrl == null  || svnUrl.length() < 3)
				throw new Exception(ErrorVar.Path_no_having);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			// 获取SVNLogClient
			SVNLogClient logClient = clientManager.getLogClient();
			
			SVNRevision startRevision = SVNRevision.create(formatter.parse(startDate));
			
			SVNRevision endRevision = SVNRevision.create(formatter.parse(endDate));
			logClient.doLog(SVNURL.parseURIEncoded(svnUrl), new String[] {  },null, startRevision, endRevision, true, true, 1000000l, logEntryHandler);
			
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean doLock() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean unLock() {
		// TODO Auto-generated method stub
		return false;
	}


}
