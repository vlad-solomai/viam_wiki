# Current Load

Load average — these are the three numbers displayed when running the **top** and **uptime** commands. They look like this: **load average: 0.35, 0.32, 0.41**.
These numbers represent the number of blocking processes in the execution queue within a certain time interval, 1 minute, 5 minutes and 15 minutes, respectively. In this case a blocking process is a process waiting for resources to continue running. Typically, it is waiting for resources such as the CPU, the disk I/O subsystem or the network I/O subsystem. High load average values indicate that the system is not coping with the load. If we are talking about a target server running under high load it is usually useful to fine tune the operating system (network subsystem, limit the number of simultaneously opened files, etc.) High load can also be caused by hardware problems, such as a failed disk drive.
To make a diagnosis, let's turn to other useful data, provided by the top output. The Cpu(s) line contains information about the CPU time allocation. The first two values directly reflect the CPU's work in processing processes:
```
top - 10:37:34 up 28 days, 7:26, 2 users, load average: 0.03, 0.10, 0.13
Tasks: 344 total, 1 running, 343 sleeping, 0 stopped, 0 zombie
%Cpu(s): 0.5 us, 0.1 sy, 0.0 ni, 99.5 id, 0.0 wa, 0.0 hi, 0.0 si, 0.0 st
```

If the hardware is fine and the CPU is fast, the problem is most likely in the software. The problematic application can be detected with **ps axfu**. 
The output will give a list of the processes as well as information such as CPU consumption, memory consumption, state and information which identifies the process (PID and command). To quickly diagnose such processes it is convenient to put the sorting mode in htop by column **'S'** (state).
Further debugging can be performed armed with **top, dstat, vmstat, iostat, iotop, strace, iperf**.

The main reasons: the high load of the processor, the disk or the network.

### Database server
Execution of some heavy or stuck custom query in the database. View which query is causing the load:
```
> mysql -uroot -e "show full processlist;"
> mtop
> mysql -uroot -e "KILL QUERY processlist_id"
```

In critical situations - by restarting the mysqld service (do not forget to stop replication before restarting):
```
> mysql -uroot -e "stop slave;"
> mysql -uroot -e "show slave status\G"
#restart service mysqld
########## RHEL 5,6 ##########
> sudo service mysqld restart
> sudo /etc/init.d/mysqld restart
#check status
> sudo service mysqld status
> sudo /etc/init.d/mysqld status
########## RHEL 7 ##########
> sudo systemctl restart mysqld
> sudo systemctl status mysqld
#start replication:
> mysql -uroot -e "start slave;"
> mysql -uroot -e "show slave status\G"
```

Because the memory is actively using SWAP partition. Solved by mysql tuning (setting optimal value for **innodb_buffer_pool_size**, **changing flushing policy** - **innodb_flush_log_at_trx_commit**, setting NUMA)

### Changing the value of the swappiness parameter
- For any server: Large log compression. May be caused by enabled debugging.
- SWAP usage. Can be fixed by adding extra memory or by changing SWAP partition policy with swappiness parameter, also caching policy can be changed.
- **WriteThrough** mode on battery or disk degradation which reduces system performance. If battery is charging wait until it is charged, in case it does not switch automatically - switch to WriteBack mode manually, in case of disk - replace loaded disk.
- Using **less, grep, rm, vim, vi** to work with large files. This can be solved by closing or clearing the process.
- Executing client's script. Inform the client that the script causes a heavy load. If the LA value is too high then the process is killed.
- Network problems. Capturing a dump and analyzing a graph.
- In case of stuck process - collect strace, crust, logs and analyze.
