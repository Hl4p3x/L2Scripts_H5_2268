ALTER TABLE character_variables CHANGE `value` `value` VARCHAR(300) CHARACTER SET UTF8 NOT NULL DEFAULT '0';
ALTER TABLE character_variables DROP COLUMN `type`;
REPLACE INTO installed_updates (`file_name`) VALUES ("2018_10_26 22-51");