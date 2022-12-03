1. Set hostname and disable SELINUX
```
sudo hostnamectl set-hostname elasticshearch02
sudo vi /etc/selinux/config
```

2. Import PGP key
```
sudo rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
```

3 Create elasticsearch repo file /etc/yum.repos.d/elasticsearch.repo
```
[elasticsearch]
name=Elasticsearch repository for 7.x packages
baseurl=https://artifacts.elastic.co/packages/7.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=0
autorefresh=1
type=rpm-md
```

4. Install elasticshearch
```
sudo yum install --enablerepo=elasticsearch elasticsearch
```
5. Configure nodes (/etc/elasticsearch/elasticsearch.yml)
```
# ------------------------------------ Node ------------------------------------
node.name: es-node02          
node.roles: [ master, data ]  
#
# ---------------------------------- Network -----------------------------------
network.host: 172.31.32.102 # Node address
http.port: 9200             
#
# ---------------------------------- Cluster -----------------------------------
cluster.name: es_cluster                                            
cluster.initial_master_nodes: ["es-node01","es-node02","es-node03"]  
#
# --------------------------------- Discovery ----------------------------------
discovery.seed_hosts: ["172.31.32.101", "172.31.32.102", "172.31.32.103"] # Claster nodes
#
# ----------------------------------- Paths ------------------------------------
path.data: /var/lib/elasticsearch
path.logs: /var/log/elasticsearch 
```

6. Run elasticsearch service when all nodes was configured
```
sudo systemctl start elasticsearch.service
sudo systemctl enable elasticsearch.service
```

7. Check cluster status
```
curl -X GET "http://172.31.32.103:9200/_cluster/health?pretty"
# Result example
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

curl -X GET "http://172.31.32.103:9200/_cat/master?pretty"
# Result example
Pk2KiMAORZeSs3b7GcRu1A 172.31.32.102 172.31.32.102 es-node02
```
8. Create file /etc/elasticsearch/jvm.options.d/jvm.options with next parameters (50% of total RAM)
```
-Xms8g
-Xmx8g
```
9. Disable swap
```
sudo swapoff -a
```
10. Configure virtual memory, create file  /etc/sysctl.d/mmapfs.conf with parameter
```
vm.max_map_count=262144
```
11. Restart all nodes and check cluster status again
