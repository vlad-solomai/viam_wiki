1. Logstash
- Configure a Filebeat input in the configuration file  /etc/logstash/conf.d/proxy.conf 
```
input {
  beats {
    port => 5044
  }
}

filter {
  if [type] == "syslog" {
    grok {
      match => { "message" => "%{SYSLOGTIMESTAMP:syslog_timestamp} %{SYSLOGHOST:syslog_hostname} %{DATA:syslog_program}(?:\[%{POSINT:syslog_pid}\])?: %{GREEDYDATA:syslog_message}" }
      add_field => [ "received_at", "%{@timestamp}" ]
      add_field => [ "received_from", "%{host}" ]
    }
    syslog_pri { }
    date {
      match => [ "syslog_timestamp", "MMM  d HH:mm:ss", "MMM dd HH:mm:ss" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["172.31.32.101:9200", "172.31.32.102:9200","172.31.32.103:9200"]
    manage_template => false
    index => "%{[@metadata][beat]}-%{[@metadata][version]}-%{+YYYY.MM.dd}"
    document_type => "%{[@metadata][type]}"
  }
}
```
Restart Logstash service

2. Filebeat

- Install Filebeat on all hosts from which logs will be collected

```
curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-7.12.0-x86_64.rpm
sudo rpm -vi filebeat-7.12.0-x86_64.rpm
```
Edit file /etc/filebeat/filebeat.yml.  
Index name must end with an integer. This is necessary for the Index Lifecycle Policies rollover to work properly - setup.ilm.pattern: "{now/d}-000001"
```
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /usr/local/openresty/nginx/logs/access.log
filebeat.config.modules:
  path: ${path.config}/modules.d/*.yml
  reload.enabled: false
setup.ilm.overwrite: true
setup.ilm.enabled: true
setup.ilm.rollover_alias: "%{[agent.name]}-nj"   # Edit index name here
setup.ilm.pattern: "{now/d}-000001"
setup.template.settings:
  index.number_of_shards: 1

setup.kibana.host: "172.31.32.111:5601"
setup.kibana.protocol: "http"

output.elasticsearch:
  # Array of hosts to connect to.
  hosts: ["http://172.31.32.101:9200","http://172.31.32.102:9200","http://172.31.32.103:9200"]
```
Filebeat uses different modules to parse different log files. Enable the system plugin to handle generic system log files with Filebeat. Enable the plugin:

Skip this step
```
sudo filebeat modules enable system
```
Start Filebeat
```
sudo systemctl start filebeat
sudo systemctl enable filebeat
```
3.  Allow inbound connection from filebeat host
```
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["10.31.5.172/32"]
    description = "proxy"
  }
``` 
4. Check Index Lifecycle Policies after enabling the new Filebeat
