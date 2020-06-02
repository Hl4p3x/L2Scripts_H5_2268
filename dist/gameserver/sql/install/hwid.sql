/*
Navicat MySQL Data Transfer

Source Server         : home
Source Server Version : 50173
Source Host           : localhost:3306
Source Database       : l2tales

Target Server Type    : MYSQL
Target Server Version : 50173
File Encoding         : 65001

Date: 2014-07-30 03:16:36
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for hwid
-- ----------------------------
DROP TABLE IF EXISTS `hwid`;
CREATE TABLE `hwid` (
  `HWID` varchar(40) NOT NULL,
  `first_time_played` bigint(15) DEFAULT NULL,
  `total_time_played` bigint(11) DEFAULT NULL,
  `poll_answer` int(2) DEFAULT NULL,
  `warnings` int(5) DEFAULT '0',
  `seenChangeLog` int(5) DEFAULT '-1',
  `threat` varchar(32) DEFAULT NULL,
  `banned` bigint(20) DEFAULT '0',
  PRIMARY KEY (`HWID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
