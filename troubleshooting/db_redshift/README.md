# Redshift DB

### View refreshing
Preparation on host:
1. Install PostgreSQL:
```
> sudo yum install postgresql-server postgresql-contrib
```
2. Create ~/.pgpass file. The file itself is in the format:
```
YOUR_HOST:YOUR_PORT:DB_NAME:USER_NAME:PASSWORD - example format

redshift-cluster.amazonaws.com:5439:production:awsuser:password - our production cluster
```
3. Importantly, you need to give the password file the 600 permission, or psql will just ignore it:
```
> chmod 600 ~/.pgpass
```
4. Perform action REFRESH MATERIALIZED VIEW vacuum analyze
```
PGPASSFILE=~/.pgpass psql -h redshift-cluster.amazonaws.com -U awsuser -d production -p 5439 -c "REFRESH MATERIALIZED VIEW mv_analyst_view;"

PGPASSFILE=~/.pgpass psql -h redshift-cluster.amazonaws.com -U awsuser -d production -p 5439 -c "vacuum;"

PGPASSFILE=~/.pgpass psql -h redshift-cluster.amazonaws.com -U awsuser -d production -p 5439 -c "analyze;"
```

### User Creation
Create user to read one table:
```
create user USERNAME password 'PASSWORD';
GRANT USAGE ON SCHEMA schemaname TO username;
GRANT SELECT ON schema.table TO username;
```
Create user to read all table from schema:
```
create user USERNAME password 'PASSWORD';
GRANT USAGE ON SCHEMA schemaname TO username;
GRANT SELECT ON ALL TABLES IN SCHEMA schemaname TO username;
```

### Load data from file
1. Dump all needed information into csv file (use separate screen to avoid connection lost):
```
mysql --defaults-file=.my.cnf -h localhost -P 3306 -D database_name -A -udw -e "$SQL_QUERY" > file.csv
```
2. Copy csv file into S3 storage:
```
aws s3 cp file.csv s3://redshift/import/ --recursive
```
3. Check information about data in AWS Redshift:
```
select count(*) from schema.table where data=1;
```
4. Delete information from database:
```
delete from schema.table where data=1;
```
5. Run the COPY command:
```
copy schema.table from 's3://redshift/import/file.csv' credentials 'aws_access_key_id=sdfsdfsdf; aws_secret_access_key=sdfsdfsdfsdfq' delimiter '\t' csv IGNOREHEADER 1;
```
6. Vacuum and analyze the database:
```
vacuum schema.table;
analyze schema.table;
```

### Create materialized view
```
PGPASSFILE=~/.pgpass psql -h redshift-cluster.amazonaws.com -U awsuser -d production -p 5439 -c "create materialized view mv_daily_report_view as
> select data_center_name, operator_name, provider_name, game_name, number_of_plays, unique_players,
> turnover, return, turnover - return as margin, date
> from (
> select data_center_name, operator_name, provider_name, game_name,
> count(distinct number_of_plays) as number_of_plays,
> count(distinct unique_players) as unique_players,
> sum(decode(transaction_type, 'debit', amount, 0)) as turnover,
> sum(decode(transaction_type, 'credit', amount, 0)) as return,
> date
> from (
> select data_center_name, operator_name, provider_name, game_name,
> round(amount_base, 4) AS amount, transaction_type,
> decode(transaction_type, 'debit', game_cycle_id) as number_of_plays,
> decode(transaction_type, 'debit', username) as unique_players,
> trunc(transaction_date) as date
> from dw.transaction_journal
> where operator_id != 49
> ) as R1
> group by data_center_name, operator_name, provider_name, game_name, date
> ) as R2;"
22WARNING:  An incrementally maintained materialized view could not be created, reason: Column aliases are not supported. The materialized view created, mv_daily_report_view, will be recomputed from scratch for every REFRESH.
23CREATE MATERIALIZED VIEW
```
