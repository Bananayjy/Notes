#!/usr/bin/bash

#备份目录
BACKUP_ROOT=/app/mysqlbak/data

find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d -ctime +15 -exec rm -r {} \;
