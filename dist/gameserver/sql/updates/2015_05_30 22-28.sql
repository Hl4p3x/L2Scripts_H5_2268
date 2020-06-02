-- ----------------------------
-- Table structure for p2p_accounts
-- ----------------------------
DROP TABLE IF EXISTS `p2p_accounts`;
CREATE TABLE `p2p_accounts` (
	`account` varchar(255) NOT NULL,
	`period` int NOT NULL,
	PRIMARY KEY  (`account`)
) ENGINE=MyISAM;