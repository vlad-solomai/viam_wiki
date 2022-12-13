#!/bin/bash

#System rotation:
count=`find /var/backups/new/ -maxdepth 1 -type f | grep slave* | wc -l | awk '{print $1}'`
oldest=`ls -1rt /var/backups/new/ | head -1`
if [ ${count} -gt 7 ]; then
    rm -f /var/backups/new/${oldest}
    logging "Oldest backup has been removed: ${oldest}"
    exit 1
fi
