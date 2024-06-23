#!/bin/bash

# 声明备份目录
BACKUP_ROOT = /app/mysqlbak
BACKUP_FILEDIR=$BACKUP_ROOT/data

# 备份当前日期
DATE = $（date +%Y%m%d）

# 备份命令
docker exec  mysql57 /usr/bin/mysqldump -uroot -pHtyw@2020 --default-character-set=utf8 --hex-blob pmc > $BACKUP_FILEDIR/pmc/pmc_$DATE.sql

# 打印结束
echo $DATE" done"