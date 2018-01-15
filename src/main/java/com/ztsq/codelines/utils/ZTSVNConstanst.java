package com.ztsq.codelines.utils;

public class ZTSVNConstanst {
	public static String account = "zhangyue";
	public static String password = "123456";
	public static String path = "svn://192.168.2.190/LHZT3000";//最后不能有斜杠，因为拼接时要比较

	// svn://192.168.2.190/LHZT3000/zt3000/02开发/02代码/android/branches/zerodd_branch_1.0.08
	// svn://192.168.2.190/LHZT3000/zt3000/02%E5%BC%80%E5%8F%91/02%E4%BB%A3%E7%A0%81/ios/branches/ZhiTong_new_branch_malaysia_1.0.01

	public static String TMP_LOG_FILE = "./tmplog.txt";
	
//	public static String DEV_SVN_NAME = "liqiantu";
//	public static String DEV_SVN_START_DATE = "2018-01-03 09:16:30";
//	public static String DEV_SVN_END_DATE = "2018-01-03 09:17:00";



	public static String DEV_SVN_NAME = "liqiantu";
	public static String DEV_SVN_START_DATE = "2018-01-01 00:00:00";
	public static String DEV_SVN_END_DATE = "2018-01-05 00:00:00";

	//一个阈值 当修改的文件数量，超过这个值时，说明是 打分支的 ，不纳入统计
	public static int DEV_BRANCH_CHANGE_FILE_COUNT = 200;
}
