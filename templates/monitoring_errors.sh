#!/bin/bash

LOG_FILE=/usr/local/openresty/nginx/logs/access.log

function failed_502_count() {
    COUNT=0
    REQUESTS=$(tail -n 10000 $LOG_FILE|awk 'BEGIN {FS="-"; RS="\n"} {if ($2==502){print }}')
    for i in $REQUESTS; do
        let "COUNT+=1"
    done
    echo $COUNT
}

function failed_499_count() {
    COUNT=0
    REQUESTS=$(tail -n 10000 $LOG_FILE|awk 'BEGIN {FS="-"; RS="\n"} {if ($2==499){print }}')
    for i in $REQUESTS; do
        let "COUNT+=1"
    done
    echo $COUNT
}

function failed_500_count() {
    COUNT=0
    REQUESTS=$(tail -n 10000  $LOG_FILE|awk 'BEGIN {FS="-"; RS="\n"} {if ($2==500){print }}')
    for i in $REQUESTS; do
        let "COUNT+=1"
    done
    echo $COUNT
}

function long_request_count() {
    COUNT=0
    REQUESTS=$(tail -n 10000  $LOG_FILE|awk 'BEGIN {FS="-"; RS="\n"} {if ($3>10){print }}')
    for i in $REQUESTS; do
        let "COUNT+=1"
    done
    echo $COUNT
}

function long_request_count_rush() {
    STRINGS=40000
    COUNT=0
    COLLECT_DATA="/tmp/{$1}_sec_response.log"
    LOCAL_MINS_NOW=$(date +'%Y:%H:%M:%S')
    LOCAL_MINS_PREV=$(date -d "1 min ago" +'%Y:%H:%M:%S')
    DATA_COLLECTION=$(tail -n $STRINGS $LOG_FILE | grep "rush" | awk -F"-" '{if ($3>10) {print $0} }' > $COLLECT_DATA)
    COUNT=$(sed -n "/$LOCAL_MINS_PREV/,/$LOCAL_MINS_NOW/p" $COLLECT_DATA | wc -l)
    echo $COUNT
}

case "$1" in
        502)
                failed_502_count $1
                ;;
        499)
                failed_499_count $1
                ;;
        500)
                failed_500_count $1
                ;;
        10)
                long_request_count $1
                ;;
        rush_10)
                long_request_count_rush $1
                ;;
        *)
                echo $"Usage $0 {502|499|500|10|rush_10}"
                exit
esac
