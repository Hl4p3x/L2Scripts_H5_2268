DROP TABLE IF EXISTS `bbs_buff_set_skills`;
CREATE TABLE `bbs_buff_set_skills` (
	`owner_id` int(11) NOT NULL,
	`set_id` int(11) NOT NULL,
	`skill_id` int(11) NOT NULL,
	PRIMARY KEY (`owner_id`, `set_id`, `skill_id`)
) ENGINE=MyISAM;