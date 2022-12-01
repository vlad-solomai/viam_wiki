# WEBSITE availability
```
> ping
> mtr
> traceroute
> dig
> host
> nslookup
> netstat -tulpn | grep :80 - check if 80 is used
> /sbin/iptables -L - check firewall
> curl -w "%{http_code} %{time_total} %{size_download} %{content_type}\n" website_name
> telnet website 80 - check website via 80 port
```
Information about CURL:
Сurl — консольная утилита для передачи данных используя URL-синтаксис, поддерживаются протоколы DICT, FILE, FTP, FTPS, Gopher, HTTP, HTTPS, IMAP, IMAPS, LDAP, LDAPS, POP3, POP3S, RTMP, RTSP, SCP, SFTP, SMTP, SMTPS, Telnet и TFTP.
```
curl -Ik http://stfl-prod.astoundcommerce.com
curl -4 wttr.in/Kiev
curl -s
```
### Apache
Check status of service:
```
> sudo /etc/init.d/httpd status
```
The main logs are stored in:
/var/log/httpd/
Main problems:
Apache not starting - occurs after problems with rotation, configuration applications, HUP signals, etc. A common cause in such cases is incorrect configuration or modified certificates. Don't forget to do a configuration check before restart/graceful, even if you changed very little:
```
> sudo apachectl configtest
```
Certificates have expired or will expire soon. - In such cases just look for the problematic certificate and ask the client if it is self-signed or if the client will sign a new one (standard template).
Do not forget to write passphrase to the ticket if you are generating a certificate for the client to sign.
And don't put new .crt and .key files instead of the old ones beforehand - in this case Apache will just stop during a planned reload.
```
> sudo apachectl configtest
```
You need to enter passphrase - if it's not spelled correctly.
