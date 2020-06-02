DROP TABLE IF EXISTS `bbs_buff_sets`;
CREATE TABLE `bbs_buff_sets` (
	`owner_id` int(11) NOT NULL,
	`id` int(11) NOT NULL,
	`name` varchar(256) CHARACTER SET UTF8 NOT NULL,
	PRIMARY KEY (`owner_id`, `id`)
) ENGINE=MyISAM;