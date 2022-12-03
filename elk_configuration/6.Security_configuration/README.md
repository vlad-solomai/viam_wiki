1. Elasticshearch.

- Stop all nodes.
- Run  on one of them
```
cd /usr/share/elasticsearch
sudo ./bin/elasticsearch-certutil ca
```
- Copy generated CA certificate elastic-stack-ca.p12  to all nodes in directory /etc/elasticsearch
- Generate certificate on all nodes
```
sudo ./bin/elasticsearch-certutil cert --ca /etc/elasticsearch/elastic-stack-ca.p12 --ip 172.31.32.102 --dns elasticsearch02
```
- Create keystore for elasticsearch on all nodes
```
sudo ./bin/elasticsearch-keystore create -p
```
- Add credentials to keystore
```
sudo ./bin/elasticsearch-keystore add xpack.security.transport.ssl.keystore.secure_password
sudo ./bin/elasticsearch-keystore add xpack.security.transport.ssl.truststore.secure_password
```
- Add key for keystore into Variable
```
echo "password" > /etc/elasticsearch/ks_secret.tmp
chmod 600 /etc/elasticsearch/ks_secret.tmp
sudo systemctl set-environment ES_KEYSTORE_PASSPHRASE_FILE=/etc/elasticsearch/ks_secret.tmp
```
- Add this block to /etc/elasticsearch/elasticsearch.yml
```
xpack.security.enabled: true
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: none
xpack.security.transport.ssl.keystore.path: elastic-certificates.p12
xpack.security.transport.ssl.truststore.path: elastic-stack-ca.p12
```
- Generate passwords for cluster users 
```
sudo ./bin/elasticsearch-setup-passwords auto

# Result eample

Changed password for user apm_system
PASSWORD apm_system = ###################

Changed password for user kibana_system
PASSWORD kibana_system = ###################

Changed password for user kibana
PASSWORD kibana = ###################

Changed password for user logstash_system
PASSWORD logstash_system = ###################

Changed password for user beats_system
PASSWORD beats_system = ###################

Changed password for user remote_monitoring_user
PASSWORD remote_monitoring_user = ###################

Changed password for user elastic
PASSWORD elastic = ###################
```
- Check connection
```
curl -u 'elastic' -X GET "http://172.31.32.101:9200/_cluster/health?pretty"
Enter host password for user 'elastic':

# Result example
{
  "cluster_name" : "es_cluster",
  "status" : "green",
  "timed_out" : false,
  "number_of_nodes" : 4,
  "number_of_data_nodes" : 3,
  "active_primary_shards" : 9,
  "active_shards" : 18,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 0,
  "delayed_unassigned_shards" : 0,
  "number_of_pending_tasks" : 0,
  "number_of_in_flight_fetch" : 0,
  "task_max_waiting_in_queue_millis" : 0,
  "active_shards_percent_as_number" : 100.0
}
```
2. Kibana
- Stop Kibana.
- Connect Kibana to elasticsearch (edit /etc/kibana/kibana.yml)
```
elasticsearch.username: "kibana_system"
elasticsearch.password: ######################
xpack.security.session.idleTimeout: "30m"
```
Create users.
- login into https://kibana.gameiom.com with credentials for user kibana_system
- open Menu > Management > Stack Management > Users
- create Users

3. Logstash

Create role for logstash.
- login into https://kibana.gameiom.com
- open Menu > Management > Stack Management > Roles
- create Role with next privilegies

Create user for logstash
- open Menu > Management > Stack Management > Users
- create User with role created before.

Add logstah user credentials to files /etc/logstash/conf.d/*.yml
```
output {
  elasticsearch {
    hosts => ["172.31.32.101:9200", "172.31.32.102:9200","172.31.32.103:9200"]
    user => "logstash_user"
    password => ###############
```
