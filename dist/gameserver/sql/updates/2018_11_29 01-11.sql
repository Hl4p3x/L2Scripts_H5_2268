ALTER TABLE raidboss_history CONVERT TO CHARACTER SET utf8;
ALTER TABLE bbs_buffs CHANGE `skills` `skills` varchar(1024) NOT NULL DEFAULT '';
REPLACE INTO installed_updates (`file_name`) VALUES ("2018_11_29 01-11");