# Coredumps
The **abrt-cli** utility is used to get information about existing coredumps and to manipulate them.
To get a list of possible commands you just need to call this utility without any keys:
```
> abrt-cli
```

To check the list of coredumps (can be located in **/var/tmp/<name>.core**):
```
> sudo abrt-cli list
```

To delete the coredumps:
```
> sudo abrt-cli remove /var/tmp/abrt/ccpp*
```

Go to the directory with the coredumps and analyze it (under the root user "sudo su").
If you suspect that the process is "stuck" - remove the coredump and restart the process.
```
> cd /var/tmp; sudo gcore
> sudo strace -fTp -o /var/tmp/strace.out
> sudo pidstat -t -p 1 20
> sudo kill -11 $pid
```
