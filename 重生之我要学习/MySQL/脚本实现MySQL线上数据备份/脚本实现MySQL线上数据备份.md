# 脚本实现MySQL线上数据备份

## 一、前言

### 1.1 说明

文章是对使用Shell脚本实现MySQL线上数据备份的方式进行探究和学习。

Shell脚本用于线上或本地MySQL数据库中对数据进行备份处理的shell脚本。

相关脚本可以通过如下方式获取：

--github--

### 1.2 参考文章

> [MySQL——使用mysqldump备份与恢复数据](https://blog.csdn.net/DreamEhome/article/details/133580992)

### 1.3 前置知识点

#### 1. mysqldump

>  mysqldump命令可以将数据库中指定或所有的库、表导出为SQL脚本。表的结构和表中的数据将存储在生成的SQL脚本中。
>
> mysqldump备份恢复原理：通过先查出需要备份的库及表的结构，在SQL脚本中生成CREATE语句。然后将表中的所有记录转换成INSERT语句并写入SQL脚本中。这些CREATE语句和INSERT语句都是还原时使用的：还原数据时可使用其中的CREATE语句来创建表，使用INSERT语句还原数据。
>

**基本的备份用法：**

- 备份整个数据库

```shell
mysqldump -u username -p database_name > backup_file.sql
```

这将备份名为 `database_name` 的整个数据库，并将结果输出到名为 `backup_file.sql` 的文件中。`-u` 选项用于指定用户名，`-p` 选项提示用户输入密码

- 备份特定表

```shell
mysqldump -u username -p database_name table_name > backup_file.sql
```

这将备份名为 `database_name` 的数据库中名为 `table_name` 的表，并将结果输出到 `backup_file.sql` 文件中。

- 仅备份数据，不包括表结构

```shell
mysqldump -u username -p --no-create-info database_name > backup_file.sql
```

这将仅备份数据库中的数据，不包括表的结构信息

- 仅备份表结构，不包括数据

```shell
mysqldump -u username -p --no-data database_name > backup_file.sql
```

这将仅备份数据库中的表结构，不包括数据



**恢复的基本用法：**

- 恢复数据库

语法：`mysql -u[用户名] -p[密码] < /备份文件路径/备份文件名.sql`

示例：

```shell
#还原数据库
mysql -uroot -p123456 < backup_file.sql
```

- 恢复数据表

语法：`mysqldump -u[用户名] -p[密码] [database] < /备份文件路径/备份文件名.sql`

注意：恢复表的前提是表所在的库必须存在，且可任意指定库进行恢复操作

示例：

```shell
mysql -u root -p123456 database_name < backup_file.sql
```



#### 2. 关于`#!/usr/bin/env bash`和`#!/usr/bin/bash`说明







## 二、备份数据库shell脚本

### 2.1 使用说明



### 2.2 脚本详情

#### 1. 初始化

配置文件config.txt （默认名称）

```
# MYSQL用户名
MYSQL_USERNAME=yjy
# MYSQL密码
MYSQL_PASSWORD=123456
# MYSQL备份脚本名称
MYSQL_BACKUP_SCRIPT_NAME=MySQL_BASE_BACKUP
# MYSQLDUMP目标位置
MYSQLDUMP_DIRECTORY=/nfadata/engine/mysql/bbin/mysqldump
# MYSQL容器名称
MYSQL_CONTAINER_NAME=mysql
# 是否启用docker
IF_DOCKER=1
# 数据库名称
DATABASES_NAME=(hsx hsx2)
# 备份目标地址
BACKUP_ROOT=/app/mysqlbak

```



```shell
#!/usr/bin/env bash

# 变量声明
# 1.配置文件
CONFIG_FILE="config.txt"

# 函数声明
# 1.解析配置文件函数
parse_config_file() {
	local file="$1"

	echo "开始解析配置文件【$CONFIG_FILE】……"

	# 检查配置文件是否存在
	if [[ ! -f $CONFIG_FILE ]]; then
        	echo "需要的配置文件【$CONFIG_FILE】不存在, 请先完成配置文件的配置!"
        	exit 1
	fi

	# 解析配置文件
	while IFS='=' read -r line;
	do
        	# 跳过注释和空行
        	if [[ ! $line =~ ^#.*$ ]] && [[ -n $line ]]; then
                	# 使用 eval 解析数组
                	eval "$line"
        	fi
	done < "$CONFIG_FILE"
	
	echo "解析配置文件【$CONFIG_FILE】成功！"
}

# 2.检查必要参数配置函数
check_required_params() {
	# 检查必要的参数变量是否进行配置
	if [ -z "$MYSQL_USERNAME" ]; then
  		echo "配置文件中缺少必要的配置项！"
  		exit 1
	fi

}

# 3.MySQL备份脚本创建
create_MySQL_backup_script() {
	touch "$MYSQL_BACKUP_SCRIPT_NAME.sh"
	#向文件中写入内容
	cat << EOF > "$MYSQL_BACKUP_SCRIPT_NAME.sh"
#!/bin/usr/env bash

DATABASES=(${DATABASES_NAME[@]})
MYSQL_USERNAME=$MYSQL_USERNAME
MYSQL_PASSWORD=$MYSQL_PASSWORD
MYSQL_CONTAINER_NAME=$MYSQL_CONTAINER_NAME
BACKUP_ROOT=$BACKUP_ROOT
MYSQLDUMP_DIRECTORY=$MYSQLDUMP_DIRECTORY

EOF

	cat << 'EOF' >> "$MYSQL_BACKUP_SCRIPT_NAME.sh"

#获取当前日期
DATE=$(date +%Y%m%d)

#备份命令
mkdir -p /app/mysqlbak/data/$DATE

#需要备份的数据表配置
#DATABASES=(${DATABASES_NAME[@]})

#备份数据表
for DB in "${DATABASES[@]}"
do
        # 执行备份命令
        docker exec "$MYSQL_CONTAINER_NAME" $MYSQLDUMP_DIRECTORY -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --default-character-set=utf8 --hex-blob "$DB" > "$BACKUP_FILEDIR/$DATE/${DB}_${DATE}.sql"
        # 检查是否成功
        if [ $? -eq 0 ]; then
                echo "备份数据库 【$DB】 成功"
        else
                echo "备份数据库【$DB】 失败"
        fi

done


#打印任务结束
echo $DATE" done"
EOF

	#授予文件执行权限
	chmod +x "$MYSQL_BACKUP_SCRIPT_NAME.sh"

}


# 调用解析配置文件函数
parse_config_file "$CONFIG_FILE"

# 调用检查必要参数配置函数
check_required_params

# MySQL备份脚本创建
create_MySQL_backup_script


for v in "${DATABASES_NAME[@]}";
do
	echo "123:$v"
done


exit 0

# 执行MySQL备份脚本文件
bash $MYSQL_BACKUP_SCRIPT_NAME.sh

# 打印成功消息
echo "初始化完成"


```



```shell
#!/usr/bin/bash

# 声明一个关联数组
declare -A paramMap
declare bash

# 声明函数
check_paramMap_exist() {
        local param="$1"
        # 判断关联数组中是否存在指定的键
        if [ -n "${paramMap[$param]}" ]; then
                echo "0"
        else
                echo "1"
        fi

}


while IFS='=' read -r key value;
do
        #echo "Key: $key, Value: $value"
        paramMap["$key"]="$value"
done < file.txt


# 打印关联数组内容
for key in "${!paramMap[@]}";
do
        echo "Key: $key, Value: ${paramMap[$key]}"
done



if [ -n "${paramMap["bashHead"]}" ]; then
        echo "nb111"
        bash=${paramMap["bashHead"]}
else
        echo "获取bash头异常"
        exit 1

fi

echo "bashHead:$bash"

```



```
#!/usr/bin/bash

# 声明一个关联数组
declare -A paramMap
declare bash

# 声明函数
check_paramMap_exist() {
        local param="$1"
        # 判断关联数组中是否存在指定的键
        if [ -n "${paramMap[$param]}" ]; then
                echo "0"
        else
                echo "1"
        fi

}


while IFS='=' read -r line;
do
        # 跳过注释和空行
        if [[ ! $line =~ ^#.*$ ]] && [[ -n $line ]]; then
                # 使用 eval 解析数组
                eval "$line"
        fi
done < file.txt


echo "$key1"
echo "$key2"
echo "$bashHead"
echo "$shuzhu"
for sz in "${shuzhu[@]}";do
        echo "wakaka: $sz"
done


# 打印关联数组内容
for key in "${!paramMap[@]}";
do
        echo "Key: $key, Value: ${paramMap[$key]}"
done



if [ -n "${paramMap["bashHead"]}" ]; then
        echo "nb111"
        bash=${paramMap["bashHead"]}
else
        echo "获取bash头异常"
        exit 1

fi

echo "bashHead:$bash"

```

![image-20240610124021107](%E8%84%9A%E6%9C%AC%E5%AE%9E%E7%8E%B0MySQL%E7%BA%BF%E4%B8%8A%E6%95%B0%E6%8D%AE%E5%A4%87%E4%BB%BD.assets/image-20240610124021107.png)

![image-20240610130057056](%E8%84%9A%E6%9C%AC%E5%AE%9E%E7%8E%B0MySQL%E7%BA%BF%E4%B8%8A%E6%95%B0%E6%8D%AE%E5%A4%87%E4%BB%BD.assets/image-20240610130057056.png)

```
# 检查必须的变量是否已设置
if [ -z "$USERNAME" ] || [ -z "$PASSWORD" ] || [ -z "$SERVER" ] || [ -z "$PORT" ]; then
  echo "配置文件中缺少必要的配置项！"
  exit 1
fi
```



#### 2. 正常部署的MySQL【非Docker容器部署】实现数据库的备份

```shell
#!/bin/bash

#备份目录路径
BACKUP_ROOT=/app/mysqlbak
BACKUP_FILEDIR=$BACKUP_ROOT/data

#容器名称
CONTAINER_NAME="mysql"

#MYSQL配置
MYSQL_USER="root"
MYSQL_PASSWORD="Cc123@leo"

#获取当前日期
DATE=$(date +%Y%m%d)

#备份命令
mkdir -p /app/mysqlbak/data/$DATE

#需要备份的数据表配置
DATABASES=("hsx_config" "hsx")

#备份数据表
for DB in "${DATABASES[@]}"
do
        # 执行备份命令
        /nfdata/engine/mysql/bin/mysqldump -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --default-character-set=utf8 --hex-blob "$DB" > "$BACKUP_FILEDIR/$DATE/${DB}_${DATE}.sql"
        # 检查是否成功
        if [ $? -eq 0 ]; then
                echo "备份数据库 【$DB】 成功"
        else
                echo "备份数据库【$DB】 失败"
        fi

done


#打印任务结束
echo $DATE" done"

```



#### 3. 通过Docker部署的MySQL实现数据库的备份

```shell
#!/usr/bin/bash

#备份目录路径
BACKUP_ROOT=/app/mysqlbak
BACKUP_FILEDIR=$BACKUP_ROOT/data

#容器名称
CONTAINER_NAME="mysql"

#MYSQL配置
MYSQL_USER="root"
MYSQL_PASSWORD="Cc123@leo"

#获取当前日期
DATE=$(date +%Y%m%d)

#备份命令
mkdir -p /app/mysqlbak/data/$DATE

#需要备份的数据表配置
DATABASES=("hsx_config" "hsx")

#备份数据表
for DB in "${DATABASES[@]}"
do
        # 执行备份命令
        docker exec "$CONTAINER_NAME" /usr/bin/mysqldump -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --default-character-set=utf8 --hex-blob "$DB" > "$BACKUP_FILEDIR/$DATE/${DB}_${DATE}.sql"
        # 检查是否成功
        if [ $? -eq 0 ]; then
                echo "备份数据库 【$DB】 成功"
        else
                echo "备份数据库【$DB】 失败"
        fi

done


#打印任务结束
echo $DATE" done"

```



