package com.ztsq.codelines.utils.svn.factory;


import com.ztsq.codelines.utils.svn.conf.ErrorVar;
import com.ztsq.codelines.utils.svn.conf.SvnConfig;
import com.ztsq.codelines.utils.svn.impl.SvnBaseImpl;
import com.ztsq.codelines.utils.svn.inf.ISvn;
import com.ztsq.codelines.utils.svn.model.SvnLinkPojo;

/**
 * TroilaSvn主体
 * 
 * @author Allen
 * @date 2016年8月8日
 */
public final class TroilaSvn {

	SvnLinkPojo svnLink;

	public SvnLinkPojo getSvnLink() {
		return svnLink;
	}

	/**
	 * 私有构造
	 */
	public TroilaSvn() {
	}

	public TroilaSvn(String svnAccount, String svnPassword, String repoPath) {
		this.svnLink = new SvnLinkPojo(repoPath, svnAccount, svnPassword);
	}

	/**
	 * 获取SVN操作
	 * 
	 * @param val
	 *            default 不设置日志状态 log 开启console日志状态
	 * @throws 没有操作匹配
	 * @return {@link ISvn}
	 */
	public ISvn execute(SvnConfig val) throws Exception {
		ISvn is = null;
		if (val == null)
			throw new Exception(ErrorVar.SvnConfig_is_null);

		if ("normal".equals(val.getVal())){
			is = new SvnBaseImpl(svnLink.getSvnAccount(), svnLink.getSvnPassword(), false, svnLink.getRepoPath());
		}else if ("log".equals(val.getVal())){
			is = new SvnBaseImpl(svnLink.getSvnAccount(), svnLink.getSvnPassword(), true, svnLink.getRepoPath());
		}else {
			throw new Exception(ErrorVar.SvnConfig_is_null);
		}

		return is;
	}
}
