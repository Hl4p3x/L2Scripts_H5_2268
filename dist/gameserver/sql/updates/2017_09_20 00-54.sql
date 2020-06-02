CREATE TABLE IF NOT EXISTS `clan_autoacademies` (
	`clan_id` INT NOT NULL DEFAULT '0',
	`reward_count` bigint(20) NOT NULL,
	PRIMARY KEY (`clan_id`)
) ENGINE=MyISAM;
