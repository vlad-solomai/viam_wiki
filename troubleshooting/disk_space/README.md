# Disk Space
Some of the partitions run out of space or the number of free inodes (by default the critical threshold is 5%). Basic analysis tools:
- df
- du
- lsof

Looking for the cause (file(s) and the process that generates them), disk write activity can be tracked with the **iotop** utility:
```
> df -h ; df -i -k ; sudo du -sh /var/*
> du -sch /* | grep [0-9][M,G]
> du -shc --exclude=archive * | grep [0-9]G | sort -n
> sudo find / -type f -mtime +1 -size +1G -exec ls -lh {} \; | awk '{ print $9 ": " $5 }'
> du -ckx | sort -n - to check disk space
> tune2fs -l /dev/sda1 | grep -i "block count" - to check system blocks
> df -i  - out of Inodes
```
Add **-xdev** parameter to 'find', to prevent searching on NFS like this:
```
> find / -xdev -type f -size +100M -exec ls -lh {} \; | awk '{ print $9 ": " $5 }'
```
If you are running out of space quickly, move some obviously unused data to a free partition (e.g. using scp utility).
Alternatively, delete/click corresponding files (preferably with nice, ionice). Do not delete a file which is used by some applications, it should be cleaned:
```
> sudo -s ; cat /dev/null > some.file
```
Most likely some files have been deleted, but the corresponding service still "holds" them (maybe even continues to write to them). To find such files use the command:
```
> sudo lsof /directory | grep deleted
```
To check status of the disk space outage use the next scripts:
```
> sudo find /var/ -type f -size +100M -exec ls -al {} \; | sort -n -k 5 | grep -v ibd
> S1=`df -h -B M | grep "var" | awk '{print $4}' | sed s/M$//`; sleep 1; S2=`df -h -B M | grep "var" | awk '{print $4}' | sed s/M$//`; echo "Free space left: "$S2"M"; echo "Disk space decreasing speed: "$(($S1-$S2))"M/s"; echo "No free disk space after: "$(($S1/($S1-$S2)/60))"min";
-------
OUTPUT:
Free space left: 8309M
Disk space decreasing speed: 1M/s
No free disk space after: 138min
------
```
The space may be run out by a process hanging in the database (e.g. SELECT), consequently, all information will continue to be written to the log (file). In this case, deleting the file will help only temporarily, so you need to look at the processes in the database:
```
> mysql> show processlist;
> mysql> kill PID;
```
