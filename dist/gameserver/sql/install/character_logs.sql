/*
Navicat MySQL Data Transfer

Source Server         : home
Source Server Version : 50173
Source Host           : localhost:3306
Source Database       : l2tales

Target Server Type    : MYSQL
Target Server Version : 50173
File Encoding         : 65001

Date: 2014-07-30 03:13:28
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for character_logs
-- ----------------------------
DROP TABLE IF EXISTS `character_logs`;
CREATE TABLE `character_logs` (
  `obj_Id` int(11) NOT NULL,
  `HWID` varchar(40) DEFAULT NULL,
  `action` text,
  `time` bigint(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
