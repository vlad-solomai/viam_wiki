# Work with partition

### LVM resize partition
By default, CentOS 7 uses XFS for the file system and Logical Volume Manager (LVM), creating 3 partitions:
```
/
/home
swap
```
1. Copy `/home` Contents
To backup the contents of /home, do the following:
```
df -h
mkdir /temp
cp -a /home /temp/
```
Once that is finished at your back at the prompt, you can proceed to step 2.
2. Unmount the /home directory
```
umount -vfl /home
```
3. Note the size of the home LVM volume
We run the lvs command to display the attributes of the LVM volumes:
```
lvs
Sample output:
 LV   VG Attr       LSize   Pool Origin Data%  Meta%  Move Log Cpy%Sync Convert
   home cl -wi-a----- 400.00g
   root cl -wi-ao----  50.00g
   swap cl -wi-ao----   7.81g
```
4. Remove the home LVM volume
```
dmsetup info -c | grep [lvname]
lsof | grep "major,minor"
kill -9 [PID]
lvremove /dev/cl/home
```
5. Resize the root LVM volume
Based on the output of lvs above, I can safely extend the root LVM by 406GiB.
```
lvextend -L+406G /dev/cl/root
```
6. Resize the root partition
```
xfs_growfs /dev/mapper/cl-root
df -h
```
7. Copy the `/home` contents back into the /home directory
```
cp -a /temp/home /
```
8. Remove the temporary location
```
rm -rf /temp
```
9. Remove the entry from `/etc/fstab`
Using your preferred text editor, ensure you open /etc/fstab and remove the line for /dev/mapper/cl-home.
10. Don't miss this!
Run the following command to sync systemd up with the changes.
```
dracut --regenerate-all --force
```

### LVM reduce partition
Sometimes when we are running out of disk space in our Linux box and if partition is created on LVM, then we can make some free space in the volume group by reducing the LVM using lvreduce command.
Below steps are eligible when the LVM partition is formatted either as ext. To reduce `/home` by 2GB which is on LVM partition & formatted as ext4.
```
df -h /home/
 Filesystem         Size  Used Avail Use% Mounted on
  /dev/mapper/vg_cloud-LogVol00
                    12G   9.2G  1.9G  84%  /home
```
1. Unmount the file system
Use the beneath umount command
```
umount /home/
```
2. Check the file system for Errors using e2fsck command.
```
e2fsck -f /dev/mapper/vg_cloud-LogVol00
 e2fsck 1.41.12 (17-May-2010)
 Pass 1: Checking inodes, blocks, and sizes
 Pass 2: Checking directory structure
 Pass 3: Checking directory connectivity
 Pass 4: Checking reference counts
 Pass 5: Checking group summary information
 /dev/mapper/vg_cloud-LogVol00: 12/770640 files (0.0% non-contiguous), 2446686/3084288 blocks
```
**Note**: In the above command e2fsck , we use the option ‘-f’ to forcefully check the file system, even if the file system is clean.
3. Reduce or Shrink the size of /home to desire size.
As shown in the above scenario, the size of /home is 12 GB , so by reducing it by 2GB , then the size will become 10GB.
```
resize2fs /dev/mapper/vg_cloud-LogVol00 10G
resize2fs 1.41.12 (17-May-2010)
```
Resizing the filesystem on /dev/mapper/vg_cloud-LogVol00 to 2621440 (4k) blocks.
The filesystem on /dev/mapper/vg_cloud-LogVol00 is now 2621440 blocks long.
4. Now reduce the size using lvreduce command.
```
lvreduce -L 10G /dev/mapper/vg_cloud-LogVol00
 WARNING: Reducing active logical volume to 10.00 GiB
 THIS MAY DESTROY YOUR DATA (filesystem etc.)
 Do you really want to reduce LogVol00? [y/n]: y
 Reducing logical volume LogVol00 to 10.00 GiB
 Logical volume LogVol00 successfully resized
```
5. (Optional) For the safer side, now check the reduced file system for errors
```
e2fsck -f /dev/mapper/vg_cloud-LogVol00
 e2fsck 1.41.12 (17-May-2010)
 Pass 1: Checking inodes, blocks, and sizes
 Pass 2: Checking directory structure
 Pass 3: Checking directory connectivity
 Pass 4: Checking reference counts
 Pass 5: Checking group summary information
 /dev/mapper/vg_cloud-LogVol00: 12/648960 files (0.0% non-contiguous), 2438425/2621440 blocks
```
6. Mount the file system and verify its size.
```
mount /home/
df -h /home/
 Filesystem           Size  Used Avail Use% Mounted on
 /dev/mapper/vg_cloud-LogVol00
                      9.9G  9.2G  208M  98% /home
```

### Increase/expand disk partition fdisk
1. Add additional disk space
Increase disk space in vCenter console
```
ls /sys/class/scsi_host/
echo "- - -" > /sys/class/scsi_host/host0/scan
echo "- - -" > /sys/class/scsi_host/host1/scan
echo "- - -" > /sys/class/scsi_host/host2/scan
ls /sys/class/scsi_device/
echo 1 > /sys/class/scsi_device/1\:0\:0\:0/device/rescan
echo 1 > /sys/class/scsi_device/2\:0\:0\:0/device/rescan
fdisk /dev/sda
  n
  p
  3
  ENTER
  ENTER
  t
  3
  8e
  w
partprobe
pvcreate /dev/sda3
vgdisplay (check VG name)
vgextend centos /dev/sda3
pvscan
ls /dev/centos
lvextend /dev/centos/root /dev/sda3
xfs_growfs /dev/mapper/centos-root
```
2. Can't expand current disk you need to add another disk and expand LVM to this disk
Add additional disk to VM
```
ls /sys/class/scsi_host/
echo "- - -" > /sys/class/scsi_host/host0/scan
echo "- - -" > /sys/class/scsi_host/host1/scan
echo "- - -" > /sys/class/scsi_host/host2/scan
ls /sys/class/scsi_device/
echo 1 > /sys/class/scsi_device/2\:0\:1\:0/device/rescan
fdisk -l
fdisk /dev/sdb
  n
  p
  1
  enter
  enter
  t
  8e
  w
cat /proc/partitions
  8 16 203423744 sdb
mknod /dev/sdb1 b 8 16
partprobe /dev/sdb1
pvcreate /dev/sdb1
vgdisplay
vgextend centos /dev/sdb1
vgdisplay
lvdisplay /dev/centos/root
lvextend -L +200GB /dev/centos/root
xfs_growfs /dev/centos/root
df -h
```

### AWS Resize EBS partition
1. Modify volume in AWS EC2 UI
After login to AWS console, navigate to
`EC2 -> Elastic Block Store -> Volumes`
Click on the volume that you wish to resize, then select
`Actions -> Modify Volume`
It will open a popup.
- Enter the new size in the size field. Lets says we are resizing from 150 GB to 500 GB.
- Click Modify button
- Click Yes button in the confirm popup.
Now the volume has been resized, but it won't reflect in the system. We need to do some more steps to make it work.
2. Resize the partition
Let's ssh into the machine.
List block devices attached to the machine.
```
> df -h
 Filesystem Size Used Avail Use% Mounted on
 /dev/nvme0n1p1 150G 145G 5.1G 97% /
 devtmpfs 31G 0 31G 0% /dev
 tmpfs 31G 0 31G 0% /dev/shm
 tmpfs 31G 377M 31G 2% /run
 tmpfs 31G 0 31G 0% /sys/fs/cgroup
 tmpfs 6.2G 0 6.2G 0% /run/user/1001
 tmpfs 6.2G 0 6.2G 0% /run/user/1008

> lsblk
  NAME MAJ:MIN RM SIZE RO TYPE MOUNTPOINT
  nvme0n1 259:0 0 500G 0 disk
  └─nvme0n1p1 259:1 0 150G 0 part /
```
You can see that xvda1 is still 8 GB. Lets increase the partition to disk size.
Install cloud-guest-utils
```
> yum install cloud-guest-utils
```
Grow the partition
```
> growpart /dev/nvme0n1 1
  CHANGED: partition=1 start=2048 old: size=314570719 end=314572767 new: size=1048573919,end=1048575967
```
Let's check the partition size:
```
> lsblk
  NAME MAJ:MIN RM SIZE RO TYPE MOUNTPOINT
  nvme0n1 259:0 0 500G 0 disk
  └─nvme0n1p1 259:1 0 500G 0 part /
```
3. Resize the file system
Check the file system size.
```
> df -h
 Filesystem Size Used Avail Use% Mounted on
 /dev/nvme0n1p1 150G 145G 5.1G 97% /
 devtmpfs 31G 0 31G 0% /dev
 tmpfs 31G 0 31G 0% /dev/shm
 tmpfs 31G 377M 31G 2% /run
 tmpfs 31G 0 31G 0% /sys/fs/cgroup
 tmpfs 6.2G 0 6.2G 0% /run/user/1001
 tmpfs 6.2G 0 6.2G 0% /run/user/1008
```
Resize the filesystem
```
> resize2fs /dev/nvme0n1p1

OR

> xfs_growfs /dev/nvme0n1p1
 meta-data=/dev/nvme0n1p1 isize=512 agcount=76, agsize=524224 blks
 = sectsz=512 attr=2, projid32bit=1
 = crc=1 finobt=0 spinodes=0
 data = bsize=4096 blocks=39321339, imaxpct=25
 = sunit=0 swidth=0 blks
 naming =version 2 bsize=4096 ascii-ci=0 ftype=1
 log =internal bsize=4096 blocks=2560, version=2
 = sectsz=512 sunit=0 blks, lazy-count=1
 realtime =none extsz=4096 blocks=0, rtextents=0
 data blocks changed from 39321339 to 131071739
```
Check after resizing
```
> lsblk
 NAME MAJ:MIN RM SIZE RO TYPE MOUNTPOINT
 nvme0n1 259:0 0 500G 0 disk
 └─nvme0n1p1 259:1 0 500G 0 part /

> df -h
 Filesystem Size Used Avail Use% Mounted on
 /dev/nvme0n1p1 500G 145G 356G 29% /
 devtmpfs 31G 0 31G 0% /dev
 tmpfs 31G 0 31G 0% /dev/shm
 tmpfs 31G 377M 31G 2% /run
 tmpfs 31G 0 31G 0% /sys/fs/cgroup
 tmpfs 6.2G 0 6.2G 0% /run/user/1001
 tmpfs 6.2G 0 6.2G 0% /run/user/1008
```
So we have increased the EBS volume without rebooting and zero downtime.

### How to increase the swap size from 4GB to 16GB (example)
1. Before the maintenance please check the next information regarding the system
```
> free -m
total used free shared buffers cached
Mem: 96333 92067 4265 0 354 1343
-/+ buffers/cache: 90370 5962
>>>Swap: 4095 0 4095
> df -h Filesystem
Size Used Avail Use% Mounted on
/dev/mapper/vg_p1sdb01-lv_update_root 20G 11G 8.6G 55% /
tmpfs 48G 0 48G 0% /dev/shm
/dev/sda1 485M 119M 341M 26% /boot
>>>/dev/mapper/vg_p1sdb01-lv_porta_var 816G 239G 537G 31% /porta_var
/dev/mapper/vg_p1sdb01-lv_root 20G 11G 7.8G 59% /update_root
> lsblk
NAME MAJ:MIN RM SIZE RO TYPE MOUNTPOINT
sda 8:0 0 893.3G 0 disk
|-sda1 8:1 0 500M 0 part /boot
`-sda2 8:2 0 892.8G 0 part
|-vg_p1sdb01-lv_root (dm-0) 253:0 0 20G 0 lvm /update_root
|-vg_p1sdb01-lv_update_root (dm-1) 253:1 0 20G 0 lvm /
>>>|-vg_p1sdb01-lv_porta_var (dm-2) 253:2 0 828.8G 0 lvm /porta_var
`-vg_p1sdb01-lv_swap (dm-3) 253:3 0 4G 0 lvm [SWAP]
```
2. Starting the maintenance. The command below is used in order to found out those processes
```
> sudo lsof /dev/mapper/vg_p1sdb01-lv_porta_var | awk '{ print $1,$2 }' | sort | uniq
COMMAND PID
mysqld 450
apache 27391
```
3. Stop all tasks
```
> mysql -u root
mysql> slave stop
sudo mysqld stop
sudo service apache stop
```
4. When all processes will be stopped
```
> sudo umount /dev/mapper/vg_p1sdb01-lv_porta_var
> lsblk (vg_p1sdb01-lv_porta_var) need to check
> sudo e2fsck -f /dev/mapper/vg_p1sdb01-lv_porta_var
> sudo resize2fs /dev/mapper/vg_p1sdb01-lv_porta_var 12G
> sudo lvreduce -L-12G /dev/mapper/vg_p1sdb01-lv_porta_var
> sudo resize2fs /dev/mapper/vg_p1sdb01-lv_porta_var
> sudo lvextend -L+12G /dev/mapper/vg_p1sdb01-lv_swap
> sudo swapoff -v /dev/mapper/vg_p1sdb01-lv_swap
> sudo mkswap /dev/mapper/vg_p1sdb01-lv_swap
> sudo swapon -v /dev/mapper/vg_p1sdb01-lv_swap
> sudo mount /dev/mapper/vg_p1sdb01-lv_porta_var /porta_var
```
5. Start all previous services
```
> sudo porta-mysqld start
> sudo service apache start
> mysql -u root
mysql> slave start
```
6. Сheck the results of the maintenance
```
> df -h
> free -m
> lsblk
mysql> slave status

Results: the swap size should be changed from 4GB to 16GB on SlaveDB.
 
 > lsblk
 ......
 vg_p1sdb01-lv_swap (dm-3) 253:3 0 16G 0 lvm [SWAP]
```

