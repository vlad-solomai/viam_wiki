- Install Java 11

- PostgreSQL 9.3 or never is installed (how to install postgresql 12 on centos-7)

1. Configure SELinux as Permissive
```
sudo setenforce 0
sudo sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
```

2. Tweak max_map_count and fs.file-max
```
sudo vi /etc/sysctl.conf
vm.max_map_count=262144
fs.file-max=65536
```

3. Create a user for sonar
```
sudo useradd sonar
sudo passwd sonar
```

4. Create a SonarQube PostgreSQL user and database 
```
postgres=# createuser sonar;
postgres=# createdb sonar_db owner sonar;
postgres=# grant all privileges on database sonar_db to sonar;
postgres=# ALTER USER sonar WITH ENCRYPTED password 'Password';
```

5. Fetch and install SonarQube from site https://www.sonarqube.org/downloads/
```
cd /opt/
sudo wget https://binaries.sonarsource.com/Distribution/sonarqube/sonarqube-8.8.5.zip
sudo unzip sonarqube-8.8.5.zip
sudo mv sonarqube-8.8.5.zip sonarqube
```

6. Configure SonarQube
```
$ sudo vi /opt/sonarqube/conf/sonar.properties

sonar.jdbc.username=sonar
sonar.jdbc.password=Password
sonar.jdbc.url=jdbc:postgresql://localhost/sonar_db
sonar.web.javaOpts=-Xms512m -Xms512m -XX:+HeapDumpOnOutOfMemoryError
sonar.search.javaOpts=-Xmx512m -Xms512m -XX:MaxDirectMemorySize=256m -XX:+HeapDumpOnOutOfMemoryError
sonar.path.data=/var/sonarqube/data
sonar.path.temp=/var/sonarqube/temp
```
```
sudo chown -R sonar:sonar /opt/sonarqube
```

7. Add SonarQube SystemD service file
```
$ sudo vim /etc/systemd/system/sonarqube.service
[Unit]
Description=SonarQube service
After=syslog.target network.target

[Service]
Type=forking
ExecStart=/opt/sonarqube/bin/linux-x86-64/sonar.sh start
ExecStop=/opt/sonarqube/bin/linux-x86-64/sonar.sh stop
LimitNOFILE=65536
LimitNPROC=4096
User=sonar
Group=sonar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```
```
sudo systemctl daemon-reload
sudo systemctl start sonarqube.service
sudo systemctl enable sonarqube.service
sudo systemctl status sonarqube.service
```

8. Access the Web User Interface
```
http://sonar01.gameiom.int:9000
```

To log in, simply click on the “Log In” button as shared above and you should be ushered in a page similar to the one shared below. Use username as “admin” and password as “admin“.

9. Configure proxy01-test for access to SonarQube throw https://sonar.com

10. Nginx configuration
```
server {
    listen       80;
    server_name  sonar.com;
    access_log  off;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name sonar.com;
    include ssl.conf;

    access_log /usr/local/openresty/nginx/logs/sonar.access.log;
    error_log /usr/local/openresty/nginx/logs/sonar.error.log;

    allow 18.18.18.18/32;  # Allow VPN
    allow 34.34.34.34/32;  # Allow jenkins

    deny all;

    location / {
        #try_files $uri $uri/ =404;
        proxy_pass                  http://172.31.1.10:9000;
        proxy_read_timeout          90s;
        proxy_redirect              http://172.31.1.10:9000 https://sonar.com;
        proxy_set_header            X-Real-IP       $remote_addr;
        proxy_set_header            X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_headers_hash_max_size     512;
        proxy_headers_hash_bucket_size  64;
        proxy_cookie_path /sonar /;
    }
}   
```

