#!/bin/bash

# 定义jar文件名数组
jar_files=("pigx-upms-biz-sso.jar" "hs-ward.jar" "pigx-gateway.jar" "pigx-auth.jar")

#jar包存放路径
jar_dir="/nfdata/hs"

# Java可执行文件的绝对路径
java_exec="/nfdata/engine/jdk1.8.0_301/bin/java"

# 定义日志文件路径
log_file="/app/monitor/logs/monitor.log"

# 遍历每个jar文件
for jar_file in "${jar_files[@]}"; do
    # 检查jar进程是否存在
    if pgrep -f "$jar_file" >/dev/null; then
        echo "$(date): Jar进程 '$jar_file' 已经在运行." >> "$log_file"
    else
        echo "$(date): Jar进程 '$jar_file' 不存在，启动中..." >> "$log_file"
        
        # 启动jar进程的命令，你可能需要根据实际情况修改
        #java -jar "$jar_file" &
				nohup "$java_exec" -Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom -jar "$jar_dir"/"$jar_file" > "$jar_dir"/logs/"$jar_file".log 2>&1 &
       
        echo "$(date): Jar进程 '$jar_file' 已启动." >> "$log_file"
    fi
done
