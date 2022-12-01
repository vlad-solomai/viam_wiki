# Disk Health
Load the kernel module mptctl:
```
> sudo mknod /dev/mptctl c 10 220
> sudo modprobe mptctl
> sudo echo modprobe mptctl >> /etc/rc.modules
> sudo chmod +x /etc/rc.modules
```
 Working with RAID controllers. Define RAID controller manufacturer and drive types (0-productivity, 1-security):
```
> lspci |grep RAID; for f in /sys/block/sd?/queue/rotational; do printf "Drive type is "; cat $f| sed -e "s/1/HDD/" -e "s/0/SSD/"; done | uniq
```
Check status of LSI 1078/2108/2208 controllers. LSI 1078/2108/2208 controllers may be OEM parts of Intel, Dell, HP, IBM, Supermicro and are identified for example as "Dell PowerEdge Expandable RAID controller 5".
```
> lspci |grep RAID
```
To determine RAID levels, current cache policy and default policy, and advance read status, do the following:
```
> sudo megacli -CfgDsply -aAll |egrep "Product Name|Spans|RAID Level|Write"
```
**WriteThrough** - Pass-through recording is performed directly to the main memory (and duplicated in the cache), i.e. recording is not cached.
**WriteBack** - Postponed data recording is performed into cache (optimal mode). WriteThrough mode can be set temporarily (~2-4 hours) for the time of recharging the battery (learning cycle) or there are problems with the battery (long life, loss of capacity).
XX relocated sectors at megaraid PortX, state Degraded on VD0 is not optimal
Use megacli and smartctl to detect disk errors:
```
> sudo megacli -CfgDsply -aALL | grep "Virtual Drive:"|uniq; array1=(`sudo megacli -CfgDsply -aALL | egrep "Device Id"|\
awk '{print $3}'`); for f in ${array1[@]}; do printf "\n====megacli====\n"; sudo megacli -CfgDsply -aAll |\
egrep "Disk:|Device Id|Device ID|Slot|Error|Predictive Failure Count:"|\
grep -A 3 -B 3 "Device Id: $f"; echo "====smartctl====";sudo smartctl -a -d megaraid,$f /dev/sda|\
egrep -i "vendor:|product|device model|serial|health status|elements|error count:|reallocated_sector"; done
```
Problems with a small number of reallocated sectors and other non-critical situations are usually solved by notifying the client that this problem exists and asking him to pay attention and, if possible, to correct the situation. If the client thinks it's normal, we increase the critical threshold and ask the client to be more persistent when it's reached.
The temperature is 41C at megaraid Port8
Similarly solve the problem by notifying the client about the problem and ask to pay attention and if possible fix it.
You can also find documentation on the Internet for a certain model of hard drive and use it to raise the threshold:
```
> sudo megacli -CfgDsply -aALL | grep "Virtual Drive:"|uniq; array1=(`sudo megacli -CfgDsply -aALL | egrep "Device Id"|\
awk '{print $3}'`); for f in ${array1[@]}; do printf " \n=======================\n"; sudo megacli -CfgDsply -aAll |\
egrep "Disk:|Device Id|Device ID|Slot|Inquiry Data|Temp"|grep -A 2 -B 3 "Device Id: $f"; done
```
Check the temperature:
```
> sudo megacli -CfgDsply -aAll | egrep 'C\s'
> cat /proc/acpi/thermal_zone/THRM/temperature
> sensors-detect # lm-sensors
> sensors # lm-sensors
> mbmon
> ipmitool sdr
```
Check the status of Hewlett-Packard Company Smart Array controllers:
```
> sudo hpacucli ctrl all show config detail
```
HP Smart Array Disk Array Controller Management:
```
> hpacucli> ctrl all show config
> hpacucli> ctrl all show config detail
```
Controller Status:
```
> hpacucli> ctrl all show status
```
Cache management:
```
> hpacucli> ctrl slot=0 modify dwc=disable
> hpacucli> ctrl slot=0 modify dwc=enable
```
Rescan devices added since the last scan:
```
> hpacucli> rescandetects newly added devices since the last rescan
```
Managing the physical disks in the array.
```
> hpacucli> ctrl slot=0 pd all show
> hpacucli> ctrl slot=0 pd 2:3 show detail
```
You can specify a specific slot to display information on a specific disk only. Disk Status:
```
> hpacucli> ctrl slot=0 pd all show status
> hpacucli> ctrl slot=0 pd 2:3 show status
```
Сlean up:
```
> hpacucli> ctrl slot=0 pd 2:3 modify erase
```
Controlling the diodes on the disks:
```
hpacucli> ctrl slot=0 pd 2:3 modify led=on
hpacucli> ctrl slot=0 pd 2:3 modify led=off
```
Managing logical disks
```
> hpacucli> ctrl slot=0 ld all show [detail]
> hpacucli> ctrl slot=0 ld 4 show [detail]
```
Disc Status:
```
> hpacucli> ctrl slot=0 ld all show status
> hpacucli> ctrl slot=0 ld 4 show status
```
Control the diodes on the disks:
```
> hpacucli> ctrl slot=0 ld 4 modify led=on
> hpacucli> ctrl slot=0 ld 4 modify led=off
```
Restart the "failed" disks:
```
> hpacucli> ctrl slot=0 ld 4 modify reenable forced
```
Creating disks:
```
# logical drive - hpacucli> ctrl slot=0 create type=ld drives=1:12 raid=0
# logical drive - striping > hpacucli> ctrl slot=0 create type=ld drives=1:13,1:14 size=300 raid=1
# logical drive - raid 5 > hpacucli> ctrl slot=0 create type=ld drives=1:13,1:14,1:15,1:16,1:17 raid=5drives - specific drives, all drives or unassigned drives
size - The size of the logical drive in MB
raid - raid type 0, 1 , 1+0 and 5
Delete - hpacucli> ctrl slot=0 ld 4 delete
Add drives - hpacucli> ctrl slot=0 ld 4 add drives=2:3
Add disk space (forced) - hpacucli> ctrl slot=0 ld 4 modify size=500 forced
Add spare disk - hpacucli> ctrl slot=0 array all add spares=1:5,1:7
```
S.M.A.R.T.: Check HDD
```
> smartctl -a /dev/ad0
> sas2ircu 0 DISPLAY
> smartctl -a /dev/sg0
> mpt-status
> smartctl -a /dev/sg0> sudo smartctl -d megaraid,0 --all -i  (/dev/sda - вычитать SMART для дисков с LSI рейдом)
```
MEGACLI:
```
> sudo megacli -ShowSummary -a0 | grep "RAID Level" - тип рейда
> sudo megacli -AdpAllInfo -aALL - информация о рейде и устройстве
> sudo megacli -CfgDsply -aAll
> sudo megacli -AdpBbuCmd -aALL
> sudo megacli LDPDInfo -aall - инфо о дисках и рейде
> sudo megacli -AdpBbuCmd -BbuLearn -a0 - запустить цикл обучения
> sudo ./MegaCli -LDSetProp -Cached -LAll -aAll - Read Cache
> sudo ./MegaCli -LDSetProp EnDskCache -LAll -aAll - Read Cache
> sudo ./MegaCli -LDSetProp ADRA -LALL -aALL - Read Ahead mode
> sudo ./MegaCli -LDSetProp WB -LALL -aALL - Write-Back Cache
> sudo ./MegaCli -LDSetProp NoCachedBadBBU -LALL -aALL - but disable it if the battery went discharged
> sudo ./MegaCli -LDSetProp CachedBadBBU -LALL -aALL - Enable WriteCache even when BBU is bad
> sudo ./MegaCli -AdpBbuCmd -GetBbuStatus -aALL - To check a BBU state
```
