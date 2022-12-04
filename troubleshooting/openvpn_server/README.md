# OpenVPN server

### Instal OpenVPN server on Ubuntu 18.04
https://github.com/Nyr/openvpn-install

Download and install packages
```
$ sudo apt update
$ sudo apt upgrade
wget https://git.io/vpn -O openvpn-install.sh
chmod +x openvpn-install.sh
sudo ./openvpn-install.sh
```

Follow the steps below according to the instructions:
```
Welcome to this OpenVPN road warrior installer!

This server is behind NAT. What is the public IPv4 address or hostname?
Public IPv4 address / hostname [18.134.56.244]:

Which protocol should OpenVPN use?
1) UDP (recommended)
2) TCP
Protocol [1]:

What port should OpenVPN listen to?
Port [1194]:

Select a DNS server for the clients:
1) Current system resolvers
2) Google
3) 1.1.1.1
4) OpenDNS
5) Quad9
6) AdGuard
DNS server [1]: 2

Enter a name for the first client:
Name [client]: test_client
```

To change default subnet (10.0.8.0/24) edit the files /etc/systemd/system/openvpn-iptables.service and /etc/openvpn/server/server.conf.

Restart OpenVPN service:
```
sudo systemctl stop openvpn-server@server.service
sudo systemctl start openvpn-server@server.service
sudo systemctl status openvpn-server@server.service
```

### Create OpenVPN user
Run the script again:
```
sudo ./openvpn-install.sh
OpenVPN is already installed.

Select an option:
   1) Add a new client
   2) Revoke an existing client
   3) Remove OpenVPN
   4) Exit
Option: 1

Provide a name for the client:
Name: VPNClient3
Using SSL: openssl OpenSSL 1.1.1  11 Sep 2018
Generating a RSA private key
.......................+++++
...................................+++++
writing new private key to '/etc/openvpn/server/easy-rsa/pki/easy-rsa-2966.45ArT8/tmp.dJlDy2'
-----
Using configuration from /etc/openvpn/server/easy-rsa/pki/easy-rsa-2966.45ArT8/tmp.OzgbF7
Check that the request matches the signature
Signature ok
The Subject's Distinguished Name is as follows
commonName            :ASN.1 12:'VPNClient3'
Certificate is to be certified until Mar 15 15:14:29 2031 GMT (3650 days)

Write out database with 1 new entries
Data Base Updated

VPNClient3 added. Configuration available in: /home/jenkins/VPNClient.ovpn
```
After that download the .ovpn file from the server and import it on the OpenVPN client.

### Delete OpenVPN user
Run the script again:
```
OpenVPN is already installed.

Select an option:
    1) Add a new client
    2) Revoke an existing client
    3) Remove OpenVPN
    4) Exit
Option: 2

Select the client to revoke:
    1) VPNClient1
    2) VPNClient2
    3) VPNClient3
Client: 2

Confirm VPNClient2 revocation? [y/N]: y
Using SSL: openssl OpenSSL 1.1.1  11 Sep 2018
Using configuration from /etc/openvpn/server/easy-rsa/pki/easy-rsa-3062.9Qitmv/tmp.eUFqia
Revoking Certificate 842B9E1F0BAB8518D74450487A943CF6.
Data Base Updated

Using SSL: openssl OpenSSL 1.1.1  11 Sep 2018
Using configuration from /etc/openvpn/server/easy-rsa/pki/easy-rsa-3099.06bzik/tmp.NPXBTR

An updated CRL has been created.
CRL file: /etc/openvpn/server/easy-rsa/pki/crl.pem

VPNClient2 revoked!
```
