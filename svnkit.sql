

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
  `code_lines` bigint(20) DEFAULT NULL COMMENT '本次修改 代码 行数',
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




INSERT INTO `svnt_auth` VALUES ('2', 'zhangyue', '');
INSERT INTO `svnt_auth` VALUES ('3', 'ouyang', '');
INSERT INTO `svnt_auth` VALUES ('4', 'liqiantu', '');
INSERT INTO `svnt_auth` VALUES ('5', 'zhaoyunxiao', '');
INSERT INTO `svnt_auth` VALUES ('6', 'qianjianmei', '');
INSERT INTO `svnt_auth` VALUES ('7', 'lulu', '');
INSERT INTO `svnt_auth` VALUES ('8', 'wangtianyuan', '');
INSERT INTO `svnt_auth` VALUES ('9', 'guguoxi', '');
INSERT INTO `svnt_auth` VALUES ('10', 'chenjin', '');
INSERT INTO `svnt_auth` VALUES ('11', 'huangzhenxiao', '');
INSERT INTO `svnt_auth` VALUES ('12', 'wangjun', '');


SELECT * FROM svnt_commit_log;


SELECT * FROM svnt_auth;


SELECT * FROM svnt_auth WHERE auth like '%%';

