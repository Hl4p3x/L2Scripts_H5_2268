/*
MySQL Data Transfer
Source Host: 77.82.124.11
Source Database: l2jdb
Target Host: 77.82.124.11
Target Database: l2jdb
Date: 24.01.2012 18:53:08
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for mmotop_10_now_month
-- ----------------------------
CREATE TABLE `mmotop_10_now_month` (
  `Name` varchar(255) NOT NULL,
  `Counts` int(255) default NULL,
  PRIMARY KEY  (`Name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for mmotop_prizi
-- ----------------------------
CREATE TABLE `mmotop_prizi` (
  `prizid` int(255) NOT NULL default '0',
  `itemid` int(10) NOT NULL default '0',
  `kolvo` int(10) default NULL,
  `kolvo-mes` varchar(255) default NULL,
  `rozdano` int(10) default NULL,
  `chance` int(3) default NULL,
  PRIMARY KEY  (`prizid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for mmotop_voted_ip
-- ----------------------------
CREATE TABLE `mmotop_voted_ip` (
  `id` int(10) NOT NULL default '0',
  `charid` int(255) default NULL,
  `charname` varchar(255) default NULL,
  `ip` varchar(25) default NULL,
  `date_vote` date default '0000-00-00',
  `time_vote` time default NULL,
  `date_deliver` date default NULL,
  `time_deliver` time default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for mmotop_winners
-- ----------------------------
CREATE TABLE `mmotop_winners` (
  `id` int(255) NOT NULL default '0',
  `name` varchar(255) NOT NULL default '',
  `prizid` int(11) default NULL,
  `data` date default NULL,
  `time` time default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for nextpay_l2_order
-- ----------------------------
CREATE TABLE `nextpay_l2_order` (
  `order_id` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `product_id` int(11) NOT NULL default '0',
  `volute` int(11) NOT NULL default '0',
  `product_count` int(11) NOT NULL default '0',
  `server` int(11) NOT NULL default '0',
  `char_name` varchar(255) collate utf8_bin NOT NULL default '',
  `profit` float NOT NULL default '0',
  `comment` varchar(255) collate utf8_bin default NULL,
  `status` int(11) NOT NULL default '0',
  PRIMARY KEY  (`order_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for nextpay_partner
-- ----------------------------
CREATE TABLE `nextpay_partner` (
  `user_id` varchar(255) NOT NULL default '',
  `partner_id` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for nextpay_sms
-- ----------------------------
CREATE TABLE `nextpay_sms` (
  `id` varchar(255) collate utf8_bin NOT NULL default '',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `amount_usd` float NOT NULL default '0',
  `number` varchar(255) collate utf8_bin NOT NULL default '',
  `text` text collate utf8_bin,
  `prefix` varchar(255) collate utf8_bin NOT NULL default '',
  `hash` varchar(255) collate utf8_bin NOT NULL default '',
  `country` varchar(255) collate utf8_bin NOT NULL default '',
  `op` varchar(255) collate utf8_bin NOT NULL default '',
  `sms_date` varchar(255) collate utf8_bin NOT NULL default '',
  `amount` float NOT NULL default '0',
  `phone` varchar(255) collate utf8_bin NOT NULL default '',
  `last_modified` datetime NOT NULL default '0000-00-00 00:00:00',
  `status` int(11) NOT NULL default '0',
  `eup` float default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `mmotop_10_now_month` VALUES ('123123', '1');
INSERT INTO `mmotop_prizi` VALUES ('0', '6673', '5', 'unlimit', '428', '100');
INSERT INTO `mmotop_voted_ip` VALUES ('0', '123123', '123123', '123.123.123.123', '2009-07-08', '00:00:00', '2009-07-19', '00:00:00');
INSERT INTO `mmotop_winners` VALUES ('0', 'Admin', '15', '2009-12-23', '00:00:00');