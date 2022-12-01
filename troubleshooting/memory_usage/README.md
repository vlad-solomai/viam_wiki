# Memory usage
Checking the Memory Usage
To determine the size and usage of memory, you can enter the following command:
```
grep MemTotal /proc/meminfo
```
You can find a detailed description of the entries in /proc/meminfo. Alternatively, you can use the free(1) command to check the memory:
```
$ free
              total       used        free    shared    buffers    cached
Mem:        4040360    4012200       28160         0     176628   3571348
-/+ buffers/cache:      264224     3776136
Swap:       4200956      12184     4188772
```
In this example the total amount of available memory is 4040360 KB. 264224 KB are used by processes and 3776136 KB are free for other applications. Do not get confused by the first line which shows that 28160KB are free! If you look at the usage figures you can see that most of the memory use is for buffers and cache. Linux always tries to use RAM to speed up disk operations by using available memory for buffers (file system metadata) and cache (pages with actual contents of files or block devices). This helps the system to run faster because disk information is already in memory which saves I/O operations. If space is needed by programs or applications like Oracle, then Linux will free up the buffers and cache to yield memory for the applications. If your system runs for a while you will usually see a small number under the field "free" on the first line.

### SWAP usage
This warning occurs when the swap partition or memory usage by the specified application exceeds the specified limits.
Here is a sample action: Estimate free memory and swap usage, it is convenient to use **htop, free**.
 See which processes are using memory (general statistics):
```
> ps axo rss,comm,pid \
| awk '{ proc_list[$2]++; proc_list[$2 "," 1] += $1; } \
END { for (proc in proc_list) { printf("%d\t%s\n", \
proc_list[proc "," 1],proc); }}' | sort -n | tail -n 10 | sort -rn \
| awk '{$1/=1024;printf "%.0fMB\t",$1}{print $2}'
```
You need to know specific processes and how much memory they occupy in swap. You can use **top, htop, ps, etc.** to view processes by memory consumption.
```
> sudo cat /proc/PID/smaps |grep Swap|awk '{sum+=$2}END{print sum/1024 " MB"}'

> SUM=0; for PID in `ps auxww |sort -g -k6 |tail -20 | awk {'print $2'}`; do PROGNAME=`ps -p $PID -o comm --no-headers`; for SWAP in `grep Swap /proc/$PID/smaps 2>/dev/null| awk '{ print $2 }'`; do let SUM=$SUM+$SWAP; done; let SUM=$SUM/1024; if [ $SUM -ne 0 ]; then echo "PID= $PID - SwapUsed= $SUM - PROCESS= $PROGNAME"; fi; done | sort -g -k5
```
Look at the parameter **"sysctl vm.swappines"** (default is 60), if the size of memory in swap is within this value **(100-N_of_vm.swappines)**, reduce this parameter:
```
> cat /proc/sys/vm/swappiness
> free -m
> sudo sysctl vm.swappiness=20
vm.swappiness = 20
> sudo vim /etc/sysctl.conf
+vm.swappiness=20
```
Try to improve application configuration. The most typical processes that are out of memory norms: Mysql, Apache (often helps for Apache, normal for Mysql).
If there is enough free RAM and the system is not loaded, unload swap. To unload swap use construction (on kernels 3.10.0-123.20.1 command often causes system to reboot, in this case offer to update kernel):
```
> sudo ionice -c3 swapoff -a && sudo swapon -a
```
The easiest way to unload memory is to reload the application (for some applications it is possible to do this with the graceful option (e.g. for Apache), but you still need to count on the probability of a complete restart - so it is better to do it off-peak and with the client confirmation).
How to collect info about process:
```
> PID=495
> some_actions $PID > action_"$PID"_`date +'%d-%m-%Y-%H%M%S'`.log 2>&1
```
