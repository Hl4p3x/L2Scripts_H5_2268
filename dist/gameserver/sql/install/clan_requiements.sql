/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2ovdb
Target Host: localhost
Target Database: l2ovdb
Date: 24.12.2013 14:09:36
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for clan_requiements
-- ----------------------------
DROP TABLE IF EXISTS `clan_requiements`;
CREATE TABLE `clan_requiements` (
  `clan_id` int(11) NOT NULL,
  `recruting` int(11) NOT NULL,
  `classes` varchar(1024) NOT NULL,
  `question1` varchar(1024) NOT NULL,
  `question2` varchar(1024) NOT NULL,
  `question3` varchar(1024) NOT NULL,
  `question4` varchar(1024) NOT NULL,
  `question5` varchar(1024) NOT NULL,
  `question6` varchar(1024) NOT NULL,
  `question7` varchar(1024) NOT NULL,
  `question8` varchar(1024) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
