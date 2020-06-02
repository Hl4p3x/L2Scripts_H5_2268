/*
Navicat MySQL Data Transfer

Source Server         : home
Source Server Version : 50173
Source Host           : localhost:3306
Source Database       : l2ovdb

Target Server Type    : MYSQL
Target Server Version : 50173
File Encoding         : 65001

Date: 2014-10-10 01:28:40
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for character_offline_buffers
-- ----------------------------
DROP TABLE IF EXISTS `character_offline_buffers`;
CREATE TABLE `character_offline_buffers` (
  `charId` int(10) NOT NULL,
  `price` int(10) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
