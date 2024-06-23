## nginx请求转发配置

### 一、背景

公司中某位大佬运用nginx配置，通过cookie的配置，将测试环境中前端请求转发到各个后端同学本地的程序中，方便了测试环境出现问题时的调试，也解决了后端频繁要求前端联调的问题。

### 二、说明

- 测试环境地址：192.168.9.134
- 本地局域网地址：192.168.190.xxx（xxx:主机地址）

### 三、具体实现

#### 3.1、测试环境nginx配置文件

```text
# 配置指定了nginx启动时的工作进程数量
# auto参数告诉nginx根据可用的CPU核心数量来自动确定工作进程的数量
# nginx会检测系统中的可用CPU核心数量，并根据这个数量动态地创建相应数量的工作进程
# 工作进程是指用于处理客户端请求的进程。每个工作进程都是独立运行的，并负责监听、接收和处理来自客户端的连接和请求
worker_processes auto;

# 指定错误日志的路径
error_log /var/log/nginx/error.log;

# 配置指示nginx在启动时包含指定目录下的所有以.conf为后缀的文件，这些文件通常包含了额外的nginx模块配置
include /usr/share/nginx/modules/*.conf;

# 定义nginx服务器处理连接的事件模块
events {
	# 指定了每个worker进程能够同时处理的最大连接数为1024
    # 工作进程（worker process）是指处理客户端请求的主要进程。当 Nginx 启动时，根据配置文件中的指令创建一个或多个工作进程。这些工作进程独立运行，每个都负责监听、接受和处理来自客户端的连接和请求
    worker_connections 1024;
}

http {
	# 配置日志格式main
	# $remote_addr: 客户端的IP地址
	# $remote_user：使用HTTP基本身份验证时的用户
	# $time_local：记录日志的本地时间
	# $request：客户端请求的内容
	# $status：服务器响应的状态码
	# $body_bytes_sent：响应发送给客户端的字节数
	# $http_referer：客户端请求的来源页面
	# $http_user_agent：发起请求的客户端用户代理（浏览器等）
	# $remote_addr：客户端的IP地址
	# $http_x_forwarded_for：如果有反向代理，此字段将包含原始客户端的IP地址
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$remote_addr" "$http_x_forwarded_for"';

	# 指定Nginx访问日志文件的路径和日志格式
    access_log  /var/log/nginx/access.log  main;

	# sendfile 是一种优化文件传输的技术，在传输文件时可以减少 CPU 的负载，提高传输效率
    sendfile            on;
    # tcp_nopush 是一种优化 TCP 传输的功能，它可以减少网络传输中的延迟和带宽消耗
    tcp_nopush          on;
    # 一种 TCP 协议选项，它可以控制数据包的延迟和传输方式
    tcp_nodelay         on;
    # 指定了 Nginx 服务器与客户端之间的 keep-alive 连接超时时间（单位秒）
    # 客户端与服务器之间没有活动的情况下保持 65 秒，如果在此时间内没有新的请求到达，连接将被关闭。
    keepalive_timeout   65;
    # 指定了 Nginx 在处理 MIME 类型时使用的哈希表的最大大小为 4096
    # MIME (Multipurpose Internet Mail Extensions) 类型是一种在互联网上用于标识文件类型的标准
    # 当 Nginx 收到一个请求时，它需要确定请求的文件类型以及如何处理该文件。为了提高性能，Nginx 使用哈希表来快速映射文件扩展名到相应的 MIME 类型。types_hash_max_size 指令允许您控制这个哈希表的大小，以便在内存占用和性能之间取得平衡
    types_hash_max_size 4096;

	# 配置mime类型文件，该文件通常包含了 MIME 类型与文件扩展名之间的映射关系 
    include             /etc/nginx/mime.types;
    
    # default_type 指令用于设置在未指定 MIME 类型的情况下，Nginx 服务器将使用的默认 MIME 类型
    default_type        application/octet-stream;
    
	# 使用 Nginx 的 map 指令来创建一个变量 $is_mobile，用于判断请求的用户代理是否来自移动设备
	# map 指令用于创建一个映射表，它将根据给定的条件将一个值映射到另一个值。在这里，我们使用 $http_user_agent（即客户端发送的 User-Agent 头部信息）作为键来查找对应的值
	# 在这个映射表中，$http_user_agent 是键，$is_mobile 是值，它表示用户是否来自移动设备
	map $http_user_agent $is_mobile {
		# 默认情况下，如果用户代理不匹配任何其他条件，则将 $is_mobile 设置为 0（即不是移动设备
        default         0;
        # 接下来的条目 ~*mobile 是一个正则表达式，它使用了 Nginx 的正则匹配模式。它的意思是如果用户代理中包含 "mobile"（不区分大小写），则将 $is_mobile 设置为 1（表示是移动设备）
        ~*mobile        1;
    }
    
    # 使用 Nginx 的 map 指令来创建一个变量 $stage，用于根据请求中的 Cookie 中的 stage 值来映射到对应的路径
	map $cookie_stage $stage {
		1 "/stage1";
		2 "/stage2";
		3 "/stage3";
		4 "/stage4";
		5 "/stage5";
		6 "/stage6";
		default "/";
    }
    
    # 使用 Nginx 的 map 指令来创建一个变量 $change_host，用于根据请求中的 Cookie 中的 local_ip_flag 值来映射到不同的主机地址
	map $cookie_local_ip_flag $change_host {
            1 "192.168.10.149:7030";
            default "192.168.9.134:9999";
     }
    
    # 包含 /etc/nginx/conf.d/ 目录下的所有 .conf 结尾的配置文件
    include /etc/nginx/conf.d/*.conf;
	
	# 基本的 Nginx 服务器块
    server {
    	# Nginx 监听在 80 端口上，用于处理 HTTP 请求
        listen       80;
        # 表示 Nginx 监听在 IPv6 的 80 端口上，也用于处理 HTTP 请求
        listen       [::]:80;
        # 设置服务器名为通配符 _，表示匹配任意域名
        server_name  _;
        
        # 设置 Nginx 服务器的根目录，即指定了网站的根目录路径
        # 例如，如果有一个请求 /index.html，Nginx 将会查找 /usr/share/nginx/html/index.html 文件，并将其返回给客户端
        root         /usr/share/nginx/html;

        # Load configuration files for the default server block.
        include /etc/nginx/default.d/*.conf;

		# 配置了当发生 404 错误时，Nginx 将会将请求重定向到 /404.html 页面
        error_page 404 /404.html;
        # 定义了对 /404.html 页面的具体配置
        # { } 内部没有配置，表示对 /404.html 页面使用默认的处理方式
        location = /404.html {
        }
		
		# 配置了当发生 500、502、503 或 504 错误时，Nginx 将会将请求重定向到 /50x.html 页面
        error_page 500 502 503 504 /50x.html;
        location = /50x.html {
        }
    }

 	server {
 		# 指定 Nginx 监听的端口为 8888
		listen       8888;
		# 指定 Nginx 监听的 IPv6 地址和端口
        listen       [::]:8888;
        # 匹配任意的域名或主机名
        server_name  _;
        
        # 匹配任何以/hssoft开头的URL
        location /hssoft {
            # 改变请求到指定location时的文件系统路径
            alias /front/$stage/hssoft;
            # 尝试按顺序查找文件或目录，并返回找到的第一个
			# 它首先尝试返回与$uri（请求的URI）匹配的文件或目录。如果找不到，它会尝试添加斜杠并查找目录。如果仍然找不到，它会返回/index.html
            try_files $uri $uri/ /index.html;
        # 这是一个if条件语句，它检查请求的文件名是否匹配指定的正则表达式。这里，它查找所有以.js、.css、.woff、.png、.jpg或.jpeg结尾的文件
        if ($request_filename ~* .*\.(js|css|woff|png|jpg|jpeg)$) {
            # 使浏览器缓存这些文件长达1800秒
            # 有助于减少对这些静态资源的重复请求，从而提高网站的性能
            expires 1800s;  # js、css、图片缓存30分钟
        }
		# 请求的文件名 是否与正则表达式.*\.(htm|html)$匹配，用于匹配以".htm"或".html"结尾的文件名
        if ($request_filename ~* .*\.(htm|html)$) {
            # 添加一个名为"Cache-Control"的HTTP响应头，并将其值设置为"no-store"，表示禁止缓存该HTML文件
            add_header Cache-Control "no-store";  # html不缓存
        }
	}
	
	# 向HTTP响应头中添加一个名为"X-Is-Mobile"的自定义标头，并将其值设置为$is_mobile
	add_header X-Is-Mobile $is_mobile;
	# 向HTTP响应头中添加一个名为"X-stage"的自定义标头，并将其值设置为$stage
	add_header X-stage $stage;
	
	# 匹配任何以/mobile开头的URL
	location /mobile/ {
 	 # 使用 $stage 变量来匹配请求
	#access_log /usr/local/nginx/logs/matching_location.log custom;

		alias /front/$stage/mobile/;
		#alias /front/mobile/;
		#index  index.html index.htm;
		try_files $uri $uri/ /mobile/index.html;
 	if ($request_filename ~* .*\.(js|css|woff|png|jpg|jpeg)$) {
        	expires 1800s;  # js、css、图片缓存30分钟
    	}

    	if ($request_filename ~* .*\.(htm|html)$) {
        	add_header Cache-Control "no-store";  # html不缓存
    	}
	#	expires -1;
    	#	add_header Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0";

	}
	location /platform {
                alias /front/platform;
                try_files $uri $uri/ /index.html;
        }
	location /test {
                alias /front/test;
                try_files $uri $uri/ /index.html;
        }
	location /hsapi/ynwapi {
                rewrite  ^.+hsapi/?(.*)$ /$1 break;
                proxy_read_timeout 300s;
                 proxy_connect_timeout 300s;
                proxy_pass http://114.55.65.63:14084/ynwapi/;
        }
	location /hsapi/aiapi {
                rewrite  ^.+hsapi/?(.*)$ /$1 break;
                proxy_read_timeout 30s;
                proxy_connect_timeout 300s;
                proxy_pass http://192.168.9.137:9110/;
        }
	location /hsapi/febiapi {
       		proxy_pass https://influenza.nbfeyy.com:7031;
    		proxy_set_header Host $host;
    		proxy_set_header X-Real-IP $remote_addr;
    		proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    		proxy_set_header X-Forwarded-Proto $scheme;
	 }
       	#location /hsapi {
	#	proxy_read_timeout 300s;
   	#	proxy_connect_timeout 300s;
	#	rewrite  ^.+hsapi/?(.*)$ /$1 break;
	#	proxy_pass http://192.168.9.134:9999/;
	#	#proxy_pass http://$change_host/;
	#}
	
	# 匹配以"/hsapi"开头的URL路径
	location /hsapi {
        # 设置代理读取超时时间为300秒	
        proxy_read_timeout 300s;
		# 重写URL，保持不变
		rewrite ^ $request_uri;
		# # 重写URL，去除"/hsapi"前缀
		rewrite ^.+hsapi/?(.*)$ /$1 break;
		# 设置代理连接超时时间为300秒
        proxy_connect_timeout 300s;
        #  设置代理请求的目标主机为当前主机
   		proxy_set_header Host $host;
                #proxy_set_header X-Real-IP $remote_addr;
                #proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        # 设置代理请求的X-Forwarded-Proto头为当前协议（HTTP或HTTPS）
        proxy_set_header X-Forwarded-Proto $scheme;
		# 将请求转发到指定的代理服务器
		proxy_pass http://$change_host/$1;
		# 返回200状态码和重定向URL
		return 200 http://$change_host/$1;
	}
	
	location /platformapi {
		rewrite  ^.+/platformapi/?(.*)$ /$1 break;
                proxy_read_timeout 300s;
                proxy_connect_timeout 300s;
                proxy_pass http://192.168.9.134:8089/;
	}
	# yangdanyan-development
	location /ydy/admin {
		alias /front/ydy/admin;
		try_files $uri $uri/ /index.html;
	}
	location /ydy/mobile/ {
		alias /front/ydy/mobile/;
		try_files $uri $uri/ /ydy/mobile/index.html;
	}
	# caoyu-development
        location /cy/admin {
                alias /front/cy/admin;
                try_files $uri $uri/ /index.html;
        }
        location /cy/mobile/ {
                alias /front/cy/mobile/;
                try_files $uri $uri/ /cy/mobile/index.html;
        }
	# shixiaopan-development
        location /sxp/admin {
                alias /front/sxp/admin;
                try_files $uri $uri/ /index.html;
        }
        location /sxp/mobile/ {
                alias /front/sxp/mobile/;
                try_files $uri $uri/ /sxp/mobile/index.html;
        }
	# chenyefei-development
        location /cyf/admin {
                alias /front/cyf/admin;
                try_files $uri $uri/ /index.html;
        }
        location /cyf/mobile/ {
                alias /front/cyf/mobile/;
                try_files $uri $uri/ /cyf/mobile/index.html;
        }
	# zhangyizhen-development
        location /zyz/admin {
                alias /front/zyz/admin;
                try_files $uri $uri/ /index.html;
        }
        location /zyz/mobile/ {
                alias /front/zyz/mobile/;
                try_files $uri $uri/ /zyz/mobile/index.html;
        }
    }

}


```



1、rewrite ^.+hsapi/?(.*)$ /$1 break;

- `rewrite`：这是Nginx的rewrite指令，用于对URL进行重写。

- ```
  ^.+hsapi/?(.*)$
  ```

  ：这是一个正则表达式，用于匹配URL中以"/hsapi"开头的部分。解释如下：

  - `^`：表示匹配字符串的开始。
  - `.+`：表示匹配一个或多个任意字符。
  - `hsapi/`：表示匹配字面值"/hsapi"。
  - `?`：表示前面的字符是可选的，即"/hsapi"后面的斜杠是可选的。
  - `(.*)`：表示匹配零个或多个任意字符，并将其捕获为一个分组。
  - `$`：表示匹配字符串的结束。

- `/$1`：这是重写后的新路径。`$1`表示之前正则表达式中捕获的分组中的内容，即除去"/hsapi"的剩余部分。

- `break`：这是rewrite指令的标志，表示停止执行其他的rewrite指令。



2、proxy_set_header Host $host;

HTTP协议中，客户端发送请求时会包含一个Host头，用于指定目标服务器的主机名或IP地址。当使用Nginx作为代理服务器时，它会将客户端请求中的Host头信息传递给代理后端服务器。通过使用`proxy_set_header`指令，可以设置代理请求的Host头为当前主机的主机名。`Host $host`：这是要设置的头名称和值。`$host`是一个内置变量，表示客户端请求中的Host头信息。通过将`$host`赋值给Host头，可以将客户端请求的Host信息传递给代理后端服务器。



3、proxy_set_header X-Forwarded-Proto $scheme;

在HTTP协议中，X-Forwarded-Proto头用于指示客户端与代理服务器之间所使用的协议（HTTP或HTTPS）。当使用Nginx作为代理服务器时，它可以通过设置`X-Forwarded-Proto`头来告知后端服务器实际的协议类型。

具体解释如下：

- `proxy_set_header`：这是Nginx的`proxy_set_header`指令，用于设置代理请求的头信息。
- `X-Forwarded-Proto $scheme`：这是要设置的头名称和值。`$scheme`是一个内置变量，表示当前请求的协议类型（例如，http或https）。通过将`$scheme`赋值给X-Forwarded-Proto头，可以将实际的协议类型传递给后端服务器。





#### 3.2、中转nginx配置

在本地局域网中，专门拿出一台机子作为请求的中转。

```java
 map $cookie_local_ip $pass_ip {
100 "100";101 "101";102 "102";103 "103";104 "104";105 "105";106 "106";107 "107";108 "108";109 "109";110 "110";111 "111";112 "112";113 "113";114 "114";115 "115";116 "116";117 "117";118 "118";119 "119";120 "120";121 "121";122 "122";123 "123";124 "124";125 "125";126 "126";127 "127";128 "128";129 "129";130 "130";220 "220";
           default "99";
           }
	server {
	        listen       7030;
	        server_name  -;
		location / {
			#return http://192.168.190.$pass_ip:9999;
			        proxy_set_header Host $host;
			        proxy_set_header X-Real-IP $remote_addr;
			        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
			        proxy_set_header X-Forwarded-Proto $scheme;
			proxy_pass  http://192.168.190.$pass_ip:9999;
		    }
	    }
```

