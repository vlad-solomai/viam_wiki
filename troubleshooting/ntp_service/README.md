# NTP CRITICAL
Typical reasons:
You use your own DNS server, which does not resolve (or takes a long time to do so) NTP server names (you can check it by ping or nslookup/dig) - we write to the client to check his dns servers, as a temporary solution we can add a public dns (8.8.8.8.8) to resolv.conf
Client uses his internal NTP servers (from ntp.conf/chrony.conf), they don't respond properly - as a temporary solution we can manually synchronize from public ntp servers.
Client has configured firewall, which blocks ports NTP (123/UDP) - usually manifested in the fact that we can not update via just ntpdate, but via ntpdate -u - can. After finding out about Network Time Protocol (NTP) Amplification attack, many providers, through which servers are connected to custome, closed incoming traffic on port 123/udp
Changed network configuration, etc. (manually updated time without problems, but automatically does not want to) - just restart ntpd daemon.

Responsible services for synchronization: RHEL5,6 - ntpd, RHEL7 - chronyd
### NTPD
Basic analysis tools: ntpdate, ntpdc, nslookup, nmap:
```
> sudo ntpdate -u pool.ntp.org - update time from node with process progress output.
> sntp -P no -r host - update time from host with process progress output.
> ntpdc -p [-n] - view information about the current state ("*" indicates the server with which you synchronized)
```
### CHRONYC
The following commands are used to check: chronyc tracking, chronyc sources, chronyc sourcestats
```
> chronyc tracking
> chronyc sources
> chronyc sourcestats
```
