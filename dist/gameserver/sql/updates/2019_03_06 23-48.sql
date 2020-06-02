DROP TABLE IF EXISTS `bbs_buffs`;

DROP TABLE IF EXISTS `bbs_buff_sets`;
CREATE TABLE `bbs_buff_sets` (
	`owner_id` int(11) NOT NULL,
	`id` int(11) NOT NULL,
	`name` varchar(256) CHARACTER SET UTF8 NOT NULL,
	PRIMARY KEY (`owner_id`, `id`)
) ENGINE=MyISAM;

DROP TABLE IF EXISTS `bbs_buff_set_skills`;
CREATE TABLE `bbs_buff_set_skills` (
	`owner_id` int(11) NOT NULL,
	`set_id` int(11) NOT NULL,
	`skill_id` int(11) NOT NULL,
	PRIMARY KEY (`owner_id`, `set_id`, `skill_id`)
) ENGINE=MyISAM;

REPLACE INTO installed_updates (`file_name`) VALUES ("2019_03_06 23-48");