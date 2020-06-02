/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2ovdb
Target Host: localhost
Target Database: l2ovdb
Date: 29.10.2013 20:21:03
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for online_names
-- ----------------------------
DROP TABLE IF EXISTS `online_names`;
CREATE TABLE `online_names` (
  `name` varchar(255) NOT NULL,
  `status` int(2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
