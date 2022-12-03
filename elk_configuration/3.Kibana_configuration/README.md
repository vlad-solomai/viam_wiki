1. Set hostname and disable SELINUX
```
sudo hostnamectl set-hostname elasticshearch02
sudo vi /etc/selinux/config
```
2. Import PGP key
```
sudo rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
```
3. Create elasticsearch repo file /etc/yum.repos.d/kibana.repo
```
[kibana-7.x]
name=Kibana repository for 7.x packages
baseurl=https://artifacts.elastic.co/packages/7.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=1
autorefresh=1
type=rpm-md
```
4. Install kibana
```
sudo yum install kibana
```
5. Configure kibana (/etc/kibana/kibana.yml)
```
# Address and port
server.host: 172.31.32.111
server.port: 5601

# Elasticsearch nodes
elasticsearch.hosts:
  - http://172.31.32.101:9200
  - http://172.31.32.102:9200
  - http://172.31.32.103:9200

elasticsearch.sniffInterval: 60000
elasticsearch.sniffOnConnectionFault: true
logging.dest: /var/log/kibana/kibana.log
```
6. Run kibana service when all nodes was configured
```
sudo systemctl start kibana.service
sudo systemctl enable kibana.service
```
7. Check kibana status http://172.31.32.111:5601

8. Configure load balancing between Elasticsearch and Kibana.
Install Elasticsearch service with Coordinating role on Kibana host according to instruction Configure ELasticsearch VMs  with configuration file /etc/elasticsearch/elasticsearch.yml
```
# ------------------------------------ Node ------------------------------------
node.name: es-nlb01

# Setup role Coordinating only
node.master: false
node.data: false
node.ingest: false
#
# ---------------------------------- Cluster -----------------------------------
#
cluster.name: es_cluster  
# --------------------------------- Discovery ----------------------------------
discovery.seed_providers: file                       
# ---------------------------------- Network -----------------------------------
network.host: 172.31.32.111           
http.port: 9200                   
transport.host: 172.31.32.111         
transport.tcp.port: 9300-9400     
#
# ----------------------------------- Paths ------------------------------------
#
path.data: /var/lib/elasticsearch 
path.logs: /var/log/elasticsearch 
```
Run elasticsearch and check that host connected to cluster
```
curl -X GET "http://172.31.32.111:9200/_cluster/health?pretty"
#Result example
{
  "cluster_name" : "es_cluster",
  "status" : "green",
  "timed_out" : false,
  "number_of_nodes" : 4,
  "number_of_data_nodes" : 3,
  "active_primary_shards" : 15,
  "active_shards" : 30,
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
Edit Kibana configuration file /etc/kibana/kibana.yml
```
# Address and port
server.host: 172.31.32.111
server.port: 5601

# Elasticsearch nodes
elasticsearch.hosts:
  - http://172.31.32.111:9200
#  - http://172.31.32.101:9200
#  - http://172.31.32.102:9200
#  - http://172.31.32.103:9200

elasticsearch.sniffInterval: 60000
elasticsearch.sniffOnConnectionFault: true
logging.dest: /var/log/kibana/kibana.log
```
Restart Kibana
