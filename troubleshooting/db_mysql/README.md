# MySQL db

### Tuning MySQL on server
- DB slow response - increase innodb_buffer_pool_size value to 50% of RAM memory;
- The smallest recommended integer value in Gbs for innodb_buffer_pool_size option with an additional 60% reserve included:
```
-- Check innodb_buffer_pool_size value for NOW:
show variables where Variable_name like 'innodb_buffer_pool_size';

SELECT CEILING(Total_InnoDB_Bytes*1.6/POWER(1024,3)) RIBPS FROM (SELECT SUM(data_length+index_length) Total_InnoDB_Bytes FROM information_schema.tables WHERE engine='InnoDB') AS A;
```
- MySQL service issue with the swap memory - decrease innodb_buffer_pool_size value.;
- resources (RAM, disk, raid-controller, etc) – try to check free memory, add more RAM, increase free space;
- some system jobs which finished with failed status - optimize system task with data;
- flapping as result of the big load in MySQL service, issues with dns/sudo, Dos attack, etc - check hosts.allow or hosts.deny files;
- check files in the temp directory ls -la /tmp;
- Some hosts have a mysql.sock file, another – mysql-slave.sock, it depends on the server's role. You should fix path in /root/.my.cnf;
```
[client] 
socket = /tmp/mysql-slave.sock
```

### Logs
```
/var/log/
/var/log/mysql
/var/log/mysql/error.log
```

### Heavy database query
- big amount of select queries or heavy requests - kill appropriate query;
```
> mysql -uroot -e "show full processlist;"
> mtop
> mytop
> mysql -uroot -e "KILL QUERY processlist_id"
mysql> show innodb status \G
```

### Restart mysqld service
```
> mysql -uroot -e "stop slave;"
> mysql -uroot -e "show slave status\G"
#restart service mysqld
########## RHEL 5,6 ##########
> sudo service mysqld restart
> sudo /etc/init.d/mysqld restart
#check status
> sudo service mysqld status
> sudo /etc/init.d/mysqld status
########## RHEL 7 ##########
> sudo systemctl restart mysqld
> sudo systemctl status mysqld
#start replication:
> mysql -uroot -e "start slave;"
> mysql -uroot -e "show slave status\G"
```

### Check the password
```
> grep -i pass ~/.mysql_history
```

### Create readable MySQL output
```
> mysql -uroot -e "use <db>; select * <info>;" | sed "s/'/\'/;s/\t/\",\"/g;s/^/\"/;s/$/\"/;s/\n//g" | tee ~/cust.csv
```

### Create Users
```
-- Check iptables rules
sudo iptables -nvL --line-numbers

-- Check if everything is OK
use mysql
select user, host from user;

create user 'username'@'%' identified by 'password';
-- Give needed grants for user
GRANT SELECT ON `database`.* TO 'USERNAME'@'%';
GRANT ALL ON `database.*` TO 'username'@'%';
FLUSH PRIVILEGES;
SHOW GRANTS FOR 'username'@'%';

-- repl user
CREATE USER 'repl'@'10.0.1.7' IDENTIFIED BY 'password';
GRANT REPLICATION SLAVE ON . TO 'repl'@'%';
FLUSH PRIVILEGES;
FLUSH TABLES WITH READ LOCK;

-- delete user
DROP USER 'USERNAME'@'hostname';


-- add hosts IP to the /etc/hosts, /etc/hosts.allow, after check iptables table
-- make dump of grants:
> mysql -uroot -sse 'SELECT user, host FROM mysql.user' | awk -F '\t' '{print "SHOW GRANTS FOR `"$1"`@`"$2"`;"}' | mysql -uroot -ss | sed 's/$/\;/g' > grantsdump.sql
```

### Change max connection to DB
While using MySQL service you get an error “too many connections”, you can change max_connections value. It will help to increase the number of connections. By default this parameter is equal to 100. 
However, you can change it:
```
mysql> show variables like "max_connections";
mysql> set global max_connections = 200;
```

### Check amount of DATA GB
The amount of actual pages of InnoDB data located in the InnoDB buffer pool:
```
SELECT ROUND((PagesData*PageSize)/POWER(1024,3),6) DataGB FROM (SELECT variable_value PagesData FROM information_schema.global_status WHERE variable_name='Innodb_buffer_pool_pages_data') AS A, (SELECT variable_value PageSize FROM information_schema.global_status WHERE variable_name='Innodb_page_size') AS B;


SELECT 
    table_name AS `Table`, 
        round(((data_length + index_length) / 1024 / 1024), 2) `Size in MB` 
        FROM information_schema.TABLES 
        WHERE table_schema = "$DB_NAME"
            AND table_name = "$TABLE_NAME";

or this query to list the size of every table in every database, largest first:


SELECT 
    table_schema as `Database`, 
    table_name AS `Table`, 
    round(((data_length + index_length) / 1024 / 1024), 2) `Size in MB` 
FROM information_schema.TABLES 
ORDER BY (data_length + index_length) DESC;
```

### Create SQL dump
```
mysqldump --all-databases --master-data >mysql_dump.sql

-- dump without data, only structure
mysqldump --no-data - u USER -pPASSWORD DATABASE > /path/to/file/schema.sql

-- dump of database
mysqldump -u USER -pPASSWORD DATABASE > /path/to/file/dump.sql

-- creating mysql dump table
mysqldump --defaults-file=~/.my.cnf -h host --single-transaction db_name TABLE1 TABLE2 TABLE3 > table_name.sql

-- creating mysql dump with archive
mysqldump --defaults-file=~/.my.cnf -h host --single-transaction db_name table_name | gzip -9 > table_name.sql

-- restoring mysql dump
mysqladmin -u USER -pPASSWORD create NEWDATABASE
gunzip < /path/to/outputfile.sql.gz | mysql -u USER -pPASSWORD DATABASE
mysql -u root -p db_name < table_name.sql
mysql -u  -p < mysql_dump.sql
```

### RESET SLAVE
```
CHANGE MASTER TO MASTER_HOST='10.0.1.8', MASTER_USER='repl', MASTER_PASSWORD='dfgdfg', MASTER_LOG_FILE='mysql.000001', MASTER_LOG_POS=184262196;

-- check slave status and collect info about binlog
SHOW SLAVE STATUS;
stop slave;reset slave;change master to master_log_file='mysql.000950', master_log_pos=621669464;start slave;
```

### Restore one table from master
```
-- Switch the system to work with MasterDB
-- On SlaveDB:
mysql> stop slave;

-- On MasterDB:
sudo mysqldump -uroot table_name --master-data=2 --single-transaction > /var/tmp/RT_Users.sql
sudo scp -i /var/tmp/table_name.sql slave@db-slave:/var/tmp

-- On SlaveDB:
--Log position and binlog should be taken from RT_Users.sql:
mysql> START SLAVE UNTIL MASTER_LOG_FILE='mysql-bin.***', MASTER_LOG_POS=***;
~~~~~~~~~~~~
wait until replication is stopped
slave status should be the following:
~~~~~~~~~~~~
Slave_IO_Running: Yes
Slave_SQL_Running: No
~~~~~~~~~~~~
mysql -u root billing < ./var/tmp/RT_Users.sql
~~~~~~~~~~~~
mysql> start slave;
```

### Remote connection
```
mysql --defaults-file=~/.my.cnf -h 10.0.3.14 -N -B -e "use database; SELECT user_agent, count(user_agent) AS user_agent_count from tx_player_device where login_date>'2021-02-01' and login_date<'2021-03-01' GROUP BY user_agent;" > user_agent.csv
```

### SELECT
```
select * from core_stake where game_id=2521;
SELECT concat('curl -v -X POST -H "Authorization: Basic Wo=" \'http://id=',c.operator_token,'&amountInCents=0&roundId=',a.game_cycle_id,'&paymentId=',a.transaction_id,'&finishRound=true\'') as d FROM tmp_rush a
left join tx_payment_journal b on a.game_cycle_id=b.game_cycle_id
left join tx_player_session c on b.session_id=c.session_id;

SELECT DISTINCT core_operator.operator_id, core_operator.operator_name
FROM core_operator
INNER JOIN core_operator_params  
ON core_operator.operator_id = core_operator_params.operator_id
WHERE core_operator.operator_id NOT IN (SELECT operator_id
FROM core_operator_params  
WHERE param_key like 'wrapper%')
ORDER BY core_operator.operator_id;
```
### INSERT
```
INSERT INTO core_stake (game_id, operator_id, currency_id, jurisdiction_id, config) VALUES ('2521', '0', '0', '16', '{"stakes":[88],"defaultStake":88}');
INSERT INTO core_denomination (game_id, operator_id, currency_id, jurisdiction_id, config) VALUES ('2521', '0', '0', '16', '{"denominations":[1],"defaultDenomination":1}');

SET @OPERATOR_ID=86;
SET @PROVIDER_ID=48;
insert into core_live_game select @OPERATOR_ID as operator_id,game_id,1 as active,null as game_rtp_id,1 as has_demo_mode from core_game where provider_id=@PROVIDER_ID and game_id not in (select game_id from core_live_game where operator_id=@OPERATOR_ID) and game_name not like '%Social';

insert into tx_player_game (SELECT $PLAYER_DESTINATION_ID as player_id,game_id FROM tx_player_game where player_id=$PLAYER_SOURCE_ID);
```

### UPDATE
```
UPDATE core_stake set config='{"stakes":[88],"defaultStake":88}' where id=53 and game_id=2521;
update core_operator_params set param_value ='595' where operator_id=68 and param_key='opId';
update core_live_game set active=0 where operator_id in (26,23,24,39,10,29,27,30)
```

### MODIFY
```
ALTER TABLE tx_interrupted_game MODIFY game_cycle_id VARCHAR(256);
```

### CREATE
```
CREATE TABLE `core_provider_language` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `language_id` bigint(20) NOT NULL,
  `operator_id` bigint(20) DEFAULT NULL,
  `provider_id` bigint(20) DEFAULT NULL,
  `language_code` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB   DEFAULT CHARSET=latin1;
```

### DELETE
```
DELETE FROM core_game_stake WHERE game_id=2208 and operator_id=28;
```

### LOAD DATA
```
LOAD DATA LOCAL INFILE '/tmp/rank.csv' INTO TABLE tmp_gr_rank fields terminated BY ',';
```

###  Big size of binary logs
```
mysql> show slave status\G
mysql> SHOW BINARY LOGS;
mysql> PURGE BINARY LOGS BEFORE '2021-06-21';
mysql> PURGE BINARY LOGS TO 'mysql.000063';
mysql> PURGE BINARY LOGS BEFORE '2013-04-22 09:55:22';
```

### CHECK DB TABLE INFO
```
mysqlshow -u USER -pPASSWORD
mysqlshow -u USER -pPASSWORD DATABASE
```
