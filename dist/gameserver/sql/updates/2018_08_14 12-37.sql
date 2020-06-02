ALTER TABLE character_subclasses CHANGE `curHp` `curHp` DECIMAL(11,4) UNSIGNED NOT NULL DEFAULT '0';
ALTER TABLE character_subclasses CHANGE `curMp` `curMp` DECIMAL(11,4) UNSIGNED NOT NULL DEFAULT '0';
REPLACE INTO installed_updates (`file_name`) VALUES ("2018_08_14 12-37");