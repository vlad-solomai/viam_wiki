# CPU Usage
Usually the cause is some process that loads one processor/core for a long time. Utilities are usually used to find such processes:
- top
- htop
- atop
- mpstat -P ALL 2
- ps
- vmstat 1
- sar -q

Typical examples of such processes:
**bzip**, **gzip**, **etc** – logs are archived. Usually the process is periodic and is solved by ignoring or by fine-tuning. If the logs increased suddenly - you need to find out the reason for the sudden increase in log size, assess the graphs, etc.
If you suspect that the process is "stuck" - check the logs and restart the process:
```
> cd /var/tmp; sudo gcore
> sudo strace -fTp -o /var/tmp/strace.out
> sudo pidstat -t -p 1 20
> sudo kill -11 $pid
```
Diagnose High I/O Wait:
```
> iostat (can use iostat -n will give you statistics about the NFS shares)
> iotop
> top
```
View CPU statistics:
Need to configure sysstat: go to the /etc/default/sysstat (or similar) and change ENABLED="false" -> "true".
Statistics will be captured every 10 minutes and daily summary will be logged. Check them in the /var/log/sysstat or /var/log/sa. Script /etc/cron.d/sysstat is used.
To view statistics use:
```
> sar - to view statistics for a day
> sar -r - view RAM statistics
> sar -b - view DISK statistics
> sar -A - view ALL statistics
> sar -s 20:00 -e 20:30 - view CPU data from 20:00 to 20:30
```
How to collect info about process:
```
> PID=495
> some_actions $PID > action_"$PID"_`date+'%d-%m-%Y-%H%M%S'`.log 2>&1
```
