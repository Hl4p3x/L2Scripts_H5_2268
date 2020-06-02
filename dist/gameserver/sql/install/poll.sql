/*
Navicat MySQL Data Transfer

Source Server         : home
Source Server Version : 50173
Source Host           : localhost:3306
Source Database       : l2tales

Target Server Type    : MYSQL
Target Server Version : 50173
File Encoding         : 65001

Date: 2014-07-30 03:16:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for poll
-- ----------------------------
DROP TABLE IF EXISTS `poll`;
CREATE TABLE `poll` (
  `question` text NOT NULL,
  `answer_id` int(5) NOT NULL,
  `answer_text` text NOT NULL,
  `answer_votes` int(10) NOT NULL DEFAULT '0',
  `end_time` int(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`answer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
