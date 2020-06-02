CREATE TABLE IF NOT EXISTS `mercenaries_kills` (
  `target` int(11) NOT NULL,
  `killer` int(11) NOT NULL,
  `count` int(11) NOT NULL,
  PRIMARY KEY (`target`,`killer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
