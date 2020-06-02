CREATE TABLE IF NOT EXISTS `raidboss_history` (
	`killer_object_id` INT NOT NULL,
	`killer_name` varchar(35) NOT NULL DEFAULT '',
	`raid_id` INT NOT NULL,
	`raid_name` varchar(35) NOT NULL DEFAULT '',
	`kill_time` INT NOT NULL DEFAULT '0'
) ENGINE=MyISAM;

INSERT INTO installed_updates (`file_name`) VALUES ("2018_07_19 23-41");