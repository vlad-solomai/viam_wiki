1. Set hostname and disable SELINUX
```
sudo hostnamectl set-hostname elasticshearch02
sudo vi /etc/selinux/config
```
2. Import PGP key
```
sudo rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
```
3. Create logstash repo file /etc/yum.repos.d/logstash.repo
```
[logstash-7.x]
name=Elastic repository for 7.x packages
baseurl=https://artifacts.elastic.co/packages/7.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=1
autorefresh=1
type=rpm-md
```
4. Install Logstash
```
sudo yum install logstash
```
5. Configure Logstash avtostart
```
sudo /bin/systemctl daemon-reload && sudo /bin/systemctl enable logstash.service
```
