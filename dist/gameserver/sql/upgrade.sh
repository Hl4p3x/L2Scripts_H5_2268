#!/bin/sh

source mysql_settings.sh


for sqlfile in upgrade/*.sql
do
        echo Loading $sqlfile ...
        mysql -h $DBHOST -u $USER --password=$PASS -D $DBNAME < $sqlfile
done
