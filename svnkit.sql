

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for svnt_auth
-- ----------------------------
DROP TABLE IF EXISTS `svnt_auth`;

CREATE TABLE `svnt_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `auth` varchar(20) DEFAULT NULL COMMENT '开发人员 svn 账号',
  `mark` text COMMENT '备用字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='开发人员列表';


INSERT INTO `svnt_auth` VALUES ('2', 'zhangyue', '张跃');
INSERT INTO `svnt_auth` VALUES ('3', 'liqiantu', '李前途');
INSERT INTO `svnt_auth` VALUES ('4', 'ouyang', '欧阳');
INSERT INTO `svnt_auth` VALUES ('5', 'zhaoyunxiao', '赵云霄');
INSERT INTO `svnt_auth` VALUES ('6', 'qianjianmei', '钱建梅');
INSERT INTO `svnt_auth` VALUES ('7', 'lulu', '露露');
INSERT INTO `svnt_auth` VALUES ('8', 'wangtianyuan', '王天元');
INSERT INTO `svnt_auth` VALUES ('9', 'guguoxi', '顾国喜');
INSERT INTO `svnt_auth` VALUES ('10', 'chenjin', '陈进');
INSERT INTO `svnt_auth` VALUES ('11', 'huangzhenxiao', '黄振骁');
INSERT INTO `svnt_auth` VALUES ('12', 'wangjun', '王军');
INSERT INTO `svnt_auth` VALUES ('13', 'luchao', '陆超');
INSERT INTO `svnt_auth` VALUES ('14', 'wanghuimin', '王慧敏');
INSERT INTO `svnt_auth` VALUES ('15', 'zhaochengqing', '赵成庆');
INSERT INTO `svnt_auth` VALUES ('16', 'dingpanpan', '丁盼盼');
INSERT INTO `svnt_auth` VALUES ('17', 'wangkangkang', '王康康');
INSERT INTO `svnt_auth` VALUES ('18', 'panjiawei', '潘家伟');

-- ----------------------------
-- Table structure for svnt_commit_log
-- ----------------------------
DROP TABLE IF EXISTS `svnt_commit_log`;

CREATE TABLE `svnt_commit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `revision_id` bigint(20) DEFAULT NULL UNIQUE COMMENT '本次修改的 id',
  `auth_id` bigint(20) DEFAULT NULL COMMENT '作者 的 id',
  `commit_log` text COMMENT '提交日志',
  `commit_time` datetime DEFAULT NULL COMMENT '修改时间',
  `is_branch` int DEFAULT NULL COMMENT '是否 是 打分支的版本  1:是  0:不是',
  `code_lines` bigint(20) DEFAULT NULL COMMENT '本次修改 代码 行数',
  `project_id` bigint(20) DEFAULT NULL COMMENT '所属项目ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='提交日志记录表';


-- ----------------------------
-- Table structure for svnt_changed_paths_log
-- ----------------------------
DROP TABLE IF EXISTS `svnt_changed_paths_log`;

CREATE TABLE `svnt_changed_paths_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `revision_id` bigint(20) DEFAULT NULL COMMENT '修改所属 的 修改id',
  `type` varchar(10) DEFAULT NULL COMMENT '修改类型：A(added) D(delete) M(modify) R(relpace)',
  `path` text COMMENT '变化文件的路径',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='已变化文件的列表';


-- ----------------------------
-- Table structure for svnt_project
-- ----------------------------
DROP TABLE IF EXISTS `svnt_project`;

CREATE TABLE `svnt_project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) DEFAULT NULL COMMENT '项目名称',
  `path` text COMMENT '项目路径',
  `mark` varchar(100) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='项目列表';

INSERT INTO `svnt_project` VALUES ('2', '马来西亚项目安卓1.0.01', 'svn://192.168.2.190/LHZT3000/zt3000/02开发/02代码/android/branches/zero_branch_malaysia_1.0.01','');
INSERT INTO `svnt_project` VALUES ('3', '马来西亚项目ios', 'svn://192.168.2.190/LHZT3000/zt3000/02开发/02代码/ios/branches/ZhiTong_new_branch_malaysia_1.0.01','');
INSERT INTO `svnt_project` VALUES ('4', '智能调用App五期', 'svn://192.168.2.190/LHZT3000/zt3000/02开发/02代码/android/branches/zerodd_branch_1.0.08','');



SELECT * FROM svnt_commit_log;


SELECT * FROM svnt_auth;


SELECT * FROM svnt_auth WHERE auth like '%%';


SELECT tsa.id FROM svnt_auth tsa WHERE tsa.auth = 'zhaochengqing';

SELECT * FROM svnt_commit_log WHERE auth_id = 15;


SELECT * FROM svnt_changed_paths_log;

