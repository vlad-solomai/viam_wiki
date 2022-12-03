# Console actions audit

### Skills summary:
- **#bash**
- **#aws**

### Description:
1. Audit all console actions was configured by addind script into /etc/profile globally for all users.
```
# OS to audit all console actions
# Determine remote user IP
IP=$(who am i | awk '{ print $5 }' | sed 's/(//g' | sed 's/)//g')
# Put to log logged user notiify
logger -p local7.notice -t "bash $LOGNAME $$" User $LOGNAME logged from $IP
# Prefix for messages filtering (if messages logged to /var/log/messages)
PREF="usrlog"

# Logger
function h2log
{
  declare CMD
  declare _PWD
  CMD=$(history 1)
  CMD=$(echo $CMD |awk '{print substr($0,length($1)+2)}')
  _PWD=$(pwd)
  if [ "$CMD" != "$pCMD" ]; then
   logger -p local7.notice -t bash -i -- "${PREF} : SESSION=$$ : ${IP} : ${USER} : ${_PWD} : ${CMD}"
  fi
  pCMD=$CMD
}
trap h2log DEBUG || EXIT
```
2. Rsync configuration was changed.
```
- name: Modify /etc/profile file
  shell: "cat '{{ dest_dir }}'/global_profile_audit.sh >> /etc/profile"

- name: Create console audit log
  shell: sed -i '/log\/boot.log/a local7.notice '{{ dest_dir }}'/console_audit.log' /etc/rsyslog.conf
  
  - name: Disable logging from messages
  shell: sed -i 's/cron.none\b/&;local7.!notice/' /etc/rsyslog.conf
```
3. Added restriction for wheel group in /etc/sudoers.
```
## Allows people in group wheel to run all commands
%wheel ALL=(ALL) NOPASSWD: ALL, !/bin/su
```
4. Log rotate to AWS S3:
- ENVIRONMENT - string parameter
- DATE - yyyyMMdd date parameter - LocalDate.now()
- DATE_previous - yyyyMMdd date parameter - LocalDate.now().minusDays(1)
- SERVERS - multi-line string parameter
```
folder=${DATE%??}

for server in  $SERVERS; do
    ssh jenkins@${server} "sudo chown -R jenkins:jenkins /var/log/project/console_audit/console_audit.log-${DATE}.gz"
    ssh jenkins@${server} "sudo cp /root/.bash_history /var/log/project/console_audit/root_history-${DATE}"
    ssh jenkins@${server} "sudo chown -R jenkins:jenkins /var/log/project/console_audit/root_history-${DATE}"
    scp jenkins@${server}:/var/log/project/console_audit/console_audit.log-${DATE}.gz /opt/project/logs/${ENVIRONMENT}/console_audit/${server}_console_audit.log-${DATE_previous}.gz
    scp jenkins@${server}:/var/log/project/console_audit/root_history-${DATE} /opt/project/logs/${ENVIRONMENT}/console_audit/${server}_root_history-${DATE_previous}

    cd /opt/project/logs/${ENVIRONMENT}/console_audit/
    aws s3 cp ${server}_console_audit.log-${DATE_previous}.gz s3://project.console-actions-logs/$ENVIRONMENT/$folder/${server}/ || true
    aws s3 cp ${server}_root_history-${DATE_previous} s3://project.console-actions-logs/$ENVIRONMENT/$folder/${server}/ || true
    sudo rm -rf ${server}_console_audit.log-${DATE_previous}.gz || true   
    sudo rm -rf ${server}_root_history-${DATE_previous} || true
done
```

### Playbook Execution
```
configure_console_audit.yaml -i inventories/TEST/hosts --private-key /var/lib/jenkins/.ssh/id_rsa -u jenkins -e "hosts=ps01-test"
```

#### Example of successful execution:
```
PLAY [ps01-test] *****************************************************************************************************************************************************************************************************************************************

TASK [Gathering Facts] ***********************************************************************************************************************************************************************************************************************************
ok: [ps01-test]

TASK [configure_console_audit : Create a working directory for console audit] ****************************************************************************************************************************************************************************
ok: [ps01-test] => (item=/var/log/project/console_audit)

TASK [configure_console_audit : Copy console audit script] ***********************************************************************************************************************************************************************************************
ok: [ps01-test]

TASK [configure_console_audit : Create backup of root profile] *******************************************************************************************************************************************************************************************
changed: [ps01-test] => (item=/etc/profile)
changed: [ps01-test] => (item=/etc/rsyslog.conf)

TASK [configure_console_audit : Modify /etc/profile file] ************************************************************************************************************************************************************************************************
changed: [ps01-test]

TASK [configure_console_audit : Create console audit log] ************************************************************************************************************************************************************************************************

changed: [ps01-test]

TASK [configure_console_audit : Disable logging from messages] *******************************************************************************************************************************************************************************************
changed: [ps01-test]

TASK [configure_console_audit : Restart rsyslog service] *************************************************************************************************************************************************************************************************
changed: [ps01-test]

TASK [configure_console_audit : Reset bash profile] ******************************************************************************************************************************************************************************************************
changed: [ps01-test]

TASK [configure_console_audit : Copy nginx rotation config] **********************************************************************************************************************************************************************************************
ok: [ps01-test]

PLAY RECAP ***********************************************************************************************************************************************************************************************************************************************
ps01-test                  : ok=10   changed=6    unreachable=0    failed=0
```
#### Example of console_audit.log
```
Aug 13 11:15:00 ps01-test journal: bash root 32682: User root logged from
Aug 13 11:16:57 ps01-test journal: bash vlad.solomai 5291: User vlad.solomai logged from eu-west-1.compute.internal
Aug 13 11:16:57 ps01-test bash[5345]: usrlog : SESSION=5291 : eu-west-1.compute.internal : vlad.solomai : /home/vlad.solomai : sudo su
Aug 13 11:19:43 ps01-test bash[5406]: usrlog : SESSION=5291 : eu-west-1.compute.internal : vlad.solomai : /home/vlad.solomai : cat /var/log/project/console_audit/console_audit.log
Aug 13 11:19:47 ps01-test bash[5418]: usrlog : SESSION=5291 : eu-west-1.compute.internal : vlad.solomai : /home/vlad.solomai : sudo cat /var/log/project/console_audit/console_audit.log
Aug 13 11:20:00 ps01-test bash[5432]: usrlog : SESSION=5291 : eu-west-1.compute.internal : vlad.solomai : /home/vlad.solomai : ls
Aug 13 11:20:02 ps01-test bash[5457]: usrlog : SESSION=5291 : eu-west-1.compute.internal : vlad.solomai : /home/vlad.solomai : sudo cat /var/log/project/console_audit/console_audit.log
Aug 13 11:25:38 ps01-test bash[5483]: usrlog : SESSION=5291 : eu-west-1.compute.internal : vlad.solomai : /home/vlad.solomai : cat /var/log/project/console_audit/console_audit.log
Aug 13 11:29:34 ps01-test bash[5525]: usrlog : SESSION=5291 : eu-west-1.compute.internal : vlad.solomai : /home/vlad.solomai : sudo cat /var/log/project/console_audit/console_audit.log
Aug 13 11:29:38 ps01-test bash[5538]: usrlog : SESSION=5291 : eu-west-1.compute.internal : vlad.solomai : /home/vlad.solomai : sudo su
Aug 13 11:30:41 ps01-test journal: bash root 5541: User root logged from ip-172-31-42-215.eu-west-1.compute.internal
Aug 13 11:30:41 ps01-test bash[5603]: usrlog : SESSION=5541 : eu-west-1.compute.internal : root : /home/vlad.solomai : source /etc/profile
Aug 13 11:30:44 ps01-test bash[5609]: usrlog : SESSION=5541 : eu-west-1.compute.internal : root : /home/vlad.solomai : cat /var/log/project/console_audit/console_audit.log
```
