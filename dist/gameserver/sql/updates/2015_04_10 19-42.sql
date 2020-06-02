DROP TABLE IF EXISTS `ip_table`;
DROP TABLE IF EXISTS `bbs_pointsave`;
DROP TABLE IF EXISTS `bbs_skillsave`;
CREATE TABLE IF NOT EXISTS  `vote_system` (
	`value_type` TINYINT NOT NULL,
	`value` VARCHAR(255) NOT NULL,
	`penalty_time` BIGINT NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;