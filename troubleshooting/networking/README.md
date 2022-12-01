# Networking
Interface bce0 has input errors
Usually indicates physical errors on the network. On our side we need to check the state and configuration of the network interfaces, recent changes that may have affected network operation, and ask the client to check everything on his end if the problem cannot be fixed by us.
Typical commands used to check the network:
- ifconfig
- netstat
- mii-tool
- ethtool
- route (ping, traceroute, mtr, …)

We can only reset the % of errors on the interface by restarting the server or unloading/loading the kernel module (=loss of connection to the server), so usually this error is ignored for a certain time after fixing the problem with regular checks that the error % decreases.
Collect tcpdump of 100 packages:
```
> sudo tcpdump -c 100 -i eth0 -tttt -s0 -w /var/tmp/eth0_100packets.pcap -C 100 -W 50 -Z user_name
```
UDP packet analysis:
```
> sudo netstat -anp| egrep "Recv-Q|udp" | grep -v "0 0"
```
Starting with kernel versions higher than 2.6.37, packets of unregistered OSI network layer protocols (not to be confused with transport layer protocols) get into errors.
On our servers routing protocols are disabled by default, and they are among the dropped packets. You can check their frequency:
```
> while true; do RD1=`cat /sys/class/net/eth0/statistics/rx_dropped`; sleep 1;\
RD2=`cat /sys/class/net/eth0/statistics/rx_dropped`; RDPS=`expr $RD2 - $RD1`; echo "Receive drops /s: $RDPS"; done
```
If the number of drops per second is regular (e.g. 1 in 2 seconds) - most likely it is one of the routing protocols or STP/VTP/CDP, and you can enable support for these protocols on the server. Load modules into the kernel:
```
> sudo modprobe bridge
```
The bridge module will also load 2 dependent modules: stp and llc. The server now "understands" the routing protocols, and discards them not at kernel level, but above at bridge module level, thus not increasing the drop counter.
If there are still errors, we have to build a new network traffic dump and examine it. If there are no more errors, let's add bridge.ko to the autoloader:
```
> sudo -s
> echo -e '#Load "bridge" kernel module.\nbridge' > /etc/modules-load.d/bridge.conf
```
To identify the specific protocol which causes drops on an interface, you need to build tcpdump and filter it by registered protocols using a filter like: eth.type != 0x800 and eth.type != 0x806. If you use such a filter, the packets which were dropped and caught in the errors will remain. The list of registered protocols can be found with the command (example):
```
> sudo cat /proc/net/ptype
Type Device Function
0800 ip_rcv
0806 arp_rcv
```
The most common unregistered protocols: STP, VTP, CDP, MOP, RARP, VMware BP
### List of network files:
- /etc/hosts - binds hostname to IP
- /etc/networks - binds names to network name addresses
- /etc/rc.d/init.d/inet - script that configures the network interface at boot
- /etc/host.conf - list of address resolution options
- /etc/resolv.conf - a list of service names, addresses to find the remote systems
- /etc/protocols - list of protocols available in the system
- /etc/services - list of available network services
- /etc/hostname - contains the name of your system
- /etc/nsswitch.conf - configuration of the system without data and DNS
- /etc/sysconfig/network - used by rc scripts when starting the system
- /etc/sysconfig/network-scripts/ifcfg-. network interfaces (eth0:1 alias)
- /etc/sysconfig/static-routes - routing
- /etc/sysconfig/network-scripts/route - routing
- /etc/passwd - user accounts
- /etc/shadow - shadow passwords file
- /etc/aliases - aliases

### Networking programs
- netcfg - network interface configuration
- ifconfig - network interface routing (ifup/ifdown)
- route - network interface routing
- ping - check if the host is available
- netstat - network interfaces status report
- hostname - host name
- dip is a script for dialup which sets up SLIP CONNECTION
- pppd - a dialup script to connect to a modem, makes a PPP connection
- dmesg - get the list of equipment
- modprobe is a module management program (/etc/modprobe.conf)
- netstat -r - get the routing table
- whois domain - get information about the domain
- dig -x host - search for host

### TCPDUMP:
```
sudo tcpdump -i any -s0 -tttt -w /var/call.cap -C 50 -W 50 -Z root
sudo tcpdump -i em1 -tttt -s0 -w /var/tmp/321294/test.pcap -C 10 -W 50 -Z root
sudo tcpdump -i any -vvvv -tttt -s0 -w /var/tmp/tcpdump01
sudo tcpdump -i em1:0 -tttt -s0 -w -tttt -s0 -X -C 105 -W 100 -Z root -w /var/tmp/392857.pcap
```
http://wiki.media5corp.com/wiki/index.php?title=Debug_%26_Troubleshooting_-_How_to_decode_and_playback_G729_audio_streams
http://myartblog.wordpress.com/2008/06/20/how-to-playback-g729-audio-streams/

Before starting the dump collection, check the free space on the customer`s file system:
```
> df /var/
```
Check active call`s traffic on the installation (or server). In case there are lots of active calls on the server (> 300), you need to restrict the traffic collecting by caller`s/calle`s unique IP address or subnet IP mask they belong to.
If the dump is assumed to show some problems with user`s RTP data (DTMF, codecs, etc), and the call is going through the UM, don`t be lazy and execute the dump collection both on the SIP and UM server.
Please make certain that the tcpdump has been started before a Customer placed a call. Otherwise, the collected information may be not enough for you to make out the issue while analyzing the dump. If you feel that the call was initiated earlier – just ask to re-make a call once more.
Example command to collect a tcpdump for a sip call (with RPT):
```
> sudo tcpdump -i eth0 -tttt -s0 -Z root -C 500 -W 10 -w /var/tmp/tt363401.cap
Example command to collect a tcpdump for a sip call (without RPT, only sip signalization):
> sudo tcpdump -i eth0 port 5060 or port 5061 -tttt -s0 -Z root -C 500 -W 10 -w /var/tmp/tt363401.cap
Example command to collect a tcpdump for a radius session:
> sudo tcpdump -i eth0 port 1813 or port 1812 -tttt -s0 -Z root -C 500 -W 10 -w /var/tmp/tt363401.cap
Example command to make a real-time check whether the remote UA sends any sip signalization to our sip server or not:
> sudo tcpdump -i eth1 -n src net 192.168.64.0/24 and port 5060 or port 5061 -tttt -vv -s0 -Z root -C 500 -W 10 | egrep -v '[0-9]x(\d)*'
> sudo tcpdump -i eth1 -n src host 192.168.64.70 and port 5060 or port 5061 -tttt -vv -s0 -Z root -C 500 -W 10 | egrep -v '[0-9]x(\d)*'
Example command to make a real-time check whether the remote UA sends any packages to our RTP proxy or not:
> sudo tcpdump -p -i eth1 -n src net 192.168.64.0/24 and udp portrange 35000-65000 -tttt -vv -s0 -Z root -C 500 -W 10 | egrep -v '[0-9]x(\d)*'
> sudo tcpdump -p -i eth1 -n src host 192.168.64.70 and udp portrange 35000-65000 -tttt -vv -s0 -Z root -C 500 -W 10 | egrep -v '[0-9]x(\d)*'
```
Description of the most popular OPTIONS:
-i - Listen on interface. If unspecified, tcpdump searches the system interface list for the lowest numbered, configured up interface (excluding loopback), which may turn out to be, for example, eth0. On Linux systems with 2.2 or later kernels, an interface argument of any can be used to capture packets from all interfaces. Note that captures on the any device will not be done in promiscuous mode.
-tttt - Print a timestamp in default format proceeded by date on each dump line.
-s - Snarf snaplen bytes of data from each packet rather than the default of 65535 bytes. Be aware! Without the key, tcpdump will gather only first 65kb from each packet, so it would be not possible for you to examine the media session and so on.
-Z root - If tcpdump is running as root, after opening the capture device or input savefile, but before opening any savefiles for output, change the user ID to user and the group ID to the primary group of user. This behavior can also be enabled by default at compile time.
-C - Before writing a raw packet to a savefile, check whether the file is currently larger than file_size and, if so, close the current savefile and open a new one.
-W - Used in conjunction with the -C option, this will limit the number of files created to the specified number, and begin overwriting files from the beginning, thus creating a 'rotating' buffer.
-w - Write the raw packets to file rather than parsing and printing them out. They can later be printed with the -r option. Standard output is used if file is ``-''.
host - To print all packets arriving at or departing from the host.
net - To print all packets arriving at or departing from the subnet.
port - To print all packets arriving at or departing from the port.
