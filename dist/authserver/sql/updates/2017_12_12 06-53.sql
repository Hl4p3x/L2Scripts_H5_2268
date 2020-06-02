ALTER TABLE gameservers CHANGE `host` `key` varchar(255);
ALTER TABLE gameservers CHANGE `server_id` `id` int(11) NOT NULL;
