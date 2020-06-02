/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2ovdb
Target Host: localhost
Target Database: l2ovdb
Date: 12.01.2012 13:05:57
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for refferal_system
-- ----------------------------
DROP TABLE IF EXISTS `refferal_system`;
CREATE TABLE `refferal_system` (
  `reffered_id` int(13) NOT NULL,
  `reffered_name` varchar(255) NOT NULL,
  `refferer_id` int(13) NOT NULL,
  `refferer_name` varchar(255) NOT NULL,
  PRIMARY KEY (`refferer_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
