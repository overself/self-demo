# 基于CentOS镜像定制开发Centos系统镜像

## 1、 构建Centos环境
### 1.1  获取CentOS镜像，配置基本的OS环境：
~~~
docker pull centos   
~~~
安裝Centos系統(使用特权模式--privileged=true进入，注意后面的命令必须是/sbin/init)
~~~ shell
docker run -itd -p 10022:22 --privileged=true --name=centos-sshd centos_sshd:1.0.0 /sbin/init
docker exec -it centos-sshd bash
~~~
- **进入容器后，安装必要的软件**
  https://developer.aliyun.com/mirror/
~~~
# 配置yum源
mkdir /etc/yum.repos.d.bak
mv /etc/yum.repos.d/* /etc/yum.repos.d.bak
curl -o /etc/yum.repos.d/CentOS-Base.repo https://mirrors.aliyun.com/repo/Centos-vault-8.5.2111.repo

# 更新Centos的应用版本
yum clean all
yum makecache
yum update -y

# 安装常用工具
yum -y install net-tools
yum -y install htop
yum -y install bind-utils
yum -y install lrzsz
yum -y install lnav
yum -y install nc
yum -y install lsof
yum -y install tree
yum -y install psmisc
yum -y install ncdu
yum -y install dstat

# 安装iptables
#先检查是否安装了iptables
service iptables status
#安装iptables
yum install -y iptables
yum -y install nftables
#安装iptables-services
yum install iptables-services

#注册iptables服务
#相当于以前的chkconfig iptables on
systemctl enable iptables.service
#开启服务
systemctl start iptables.service
#查看状态
systemctl status iptables.service

# 安装语言库
locale -a
echo $LANG
locale
# yum install -y langpacks-*
yum install -y langpacks-zh_CN
vi /etc/locale.conf LANG="zh_CN.UTF-8"
~~~

- **开启SSHD功能**
~~~
# 安装SSHD
sudo yum install openssh-server
cat /etc/ssh/sshd_config

# 在SSH配置过程中为root用户设置密码：root
yum -y install passwd
passwd root

#首次使用SSH登录本机（验证输入密码为root用户密码）
[root@ef69bca31ddb ~]# ssh localhost
The authenticity of host 'localhost (::1)' can't be established.
ECDSA key fingerprint is SHA256:qGJ+ADraO0oINgrJ2qQZy+uJh2Oc2K4/OGSNU+FHE9o.
Are you sure you want to continue connecting (yes/no/[fingerprint])? y
Please type 'yes', 'no' or the fingerprint: yes
Warning: Permanently added 'localhost' (ECDSA) to the list of known hosts.
root@localhost's password:
Activate the web console with: systemctl enable --now cockpit.socket
 
Last login: Sun Mar 13 12:28:17 2022 from 192.168.0.101
[root@ef69bca31ddb ~]#
 
#退出SSH，再次验证登录本机
[root@ef69bca31ddb ~]# ssh localhost
root@localhost's password:
Activate the web console with: systemctl enable --now cockpit.socket
 
Last login: Sun Mar 13 12:41:25 2022 from ::1
[root@ef69bca31ddb ~]# ls
anaconda-ks.cfg
[root@ef69bca31ddb ~]# exit
注销
Connection to localhost closed.
[root@ef69bca31ddb ~]#
~~~

如果使用ssh localhost命令，无法在没有密码的情况下ssh到localhost，请配置SSH服务及协议秘钥登录
~~~ shell
#在客户端生成密钥和公钥
[root@ef69bca31ddb ~]# ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
 cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
 chmod 0600 ~/.ssh/authorized_keys

Generating public/private rsa key pair.
Your identification has been saved in /root/.ssh/id_rsa.
Your public key has been saved in /root/.ssh/id_rsa.pub.
The key fingerprint is:
SHA256:hv8F7WXb7GfxqHCiwgeIWq9TmPL93dWvv8dIcUTsiiE root@ef69bca31ddb
The key's randomart image is:
+---[RSA 3072]----+
|               o.|
|                o|
|               o |
|       .  E.. . o|
|   + .. S ...oo+ |
|. = o .o   o.+o= |
| = + . .. o =.o+=|
|. o o o..+ *  oo*|
|  .o ..oo o ...=B|
+----[SHA256]-----+
[root@ef69bca31ddb ~]#
#重新启动SSH服务
[root@ef69bca31ddb ~]# systemctl stop sshd.service
[root@ef69bca31ddb ~]# systemctl start sshd.service
#设置开机自动启动ssh服务
[root@ef69bca31ddb ~]# systemctl enable sshd.service

#最后，再次验证一下SSH登录是否免密登录
[root@ef69bca31ddb ~]# ssh localhost
Activate the web console with: systemctl enable --now cockpit.socket
 
Last login: Sun Mar 13 13:08:07 2022 from 192.168.0.101
[root@ef69bca31ddb ~]#
~~~

- **导出容器为镜像**
~~~shell
# 导入既有的容器为镜像文件
docker export -o D:\docker\centos\centos-sshd.tar centos-sshd
docker import D:\docker\centos\centos-sshd.tar centos_sshd:1.0.0
# 保存既有的镜像
# docker save -o D:\docker\hadoop\centos-hadoop.1.0.0.tar centos_hadoop:1.0.0
# docker load -i D:\docker\hadoop\centos-hadoop.1.0.0.tar
~~~

### 1.2  配置CentOS环境中的错误及解决方法：
- 1.2.1 启动iptables时报错：
~~~
[root@ef69bca31ddb ~]# systemctl status iptables
● iptables.service - IPv4 firewall with iptables
   Loaded: loaded (/usr/lib/systemd/system/iptables.service; enabled; vendor preset: disabled)
   Active: failed (Result: exit-code) since Tue 2023-09-26 05:05:46 UTC; 1min 6s ago
 Main PID: 24479 (code=exited, status=1/FAILURE)
Sep 26 05:05:46 ef69bca31ddb systemd[1]: Starting IPv4 firewall with iptables...
Sep 26 05:05:46 ef69bca31ddb iptables.init[24479]: iptables: Applying firewall rules: iptables-restore v1.8.4 (nf_tables): Couldn't load match `state':No such file or directory
Sep 26 05:05:46 ef69bca31ddb iptables.init[24479]: Error occurred at line: 8
Sep 26 05:05:46 ef69bca31ddb iptables.init[24479]: Try `iptables-restore -h' or 'iptables-restore --help' for more information.
Sep 26 05:05:46 ef69bca31ddb iptables.init[24479]: [FAILED]
Sep 26 05:05:46 ef69bca31ddb systemd[1]: iptables.service: Main process exited, code=exited, status=1/FAILURE
Sep 26 05:05:46 ef69bca31ddb systemd[1]: iptables.service: Failed with result 'exit-code'.
Sep 26 05:05:46 ef69bca31ddb systemd[1]: Failed to start IPv4 firewall with iptables.
~~~
```
该错误通常是由于在使用新版本的iptables时，尝试使用旧版本的match条件所导致的。state匹配条件已经被弃用并从新版本的iptables中移除。
**编辑** vi /etc/sysconfig/iptables
更新iptables规则：如果您的防火墙规则中使用了旧的state匹配条件，您可以将其替换为新的条件。例如，将-m state --state替换为-m conntrack --ctstate。
```
