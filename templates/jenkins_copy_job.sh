#!/bin/bash

# get config and edit it
curl -s -X GET "http://vsolo:e50785b001b@192.168.240.176:50085/job/RFCE-DEVM-US-working/config.xml" | sed '/^\ *<.*>$/! s/$/\&#xA\;/' > config.xml
# post edited config back
curl -s -X POST "http://vsolo:e50785b001b@192.168.240.176:50085/job/RF
