# Apache Hadoop 完全分布式集群安装

<!-- TOC -->
* [Apache Hadoop 完全分布式集群安装](#apache-hadoop-完全分布式集群安装)
  * [1、先决条件](#1先决条件)
    * [1.1、 支持平台](#11-支持平台)
    * [1.2 安装JAVA](#12-安装java)
    * [1.3 安装Hadoop](#13-安装hadoop)
  * [2、配置构建Hadoop集群](#2配置构建hadoop集群)
      * [2.1 完全分布式集群构建（Fully-Distributed Operation）](#21-完全分布式集群构建fully-distributed-operation)
      * [2.1.2  在Non-Secure Mode（非安全模式）下的配置](#212--在non-secure-mode非安全模式下的配置)
      * [2.1.3 配置Hadoop后台进程的配置环境 hadoop-env.sh](#213-配置hadoop后台进程的配置环境-hadoop-envsh)
      * [2.1.4 配置Hadoop守护进程的参数](#214-配置hadoop守护进程的参数)
      * [2.1.5 配置workers文件(Slaves File)](#215-配置workers文件--slaves-file-)
      * [2.1.5 修改启动shell脚本](#215-修改启动shell脚本)
  * [3，复制hadoop服务构建机器集群](#3复制hadoop服务构建机器集群)
    * [4、操作Hadoop集群](#4操作hadoop集群)
<!-- TOC -->

##  1、先决条件
### 1.1、 支持平台
GNU/Linux是产品开发和运行的平台。 Hadoop已在有2000个节点的GNU/Linux主机组成的集群系统上得到验证。

- 关闭防火墙
~~~
1. 防火墙状态：sudo systemctl status firewalld.service
2. 停止防火墙：sudo systemctl stop firewalld.service
3. 关闭开机启动：sudo systemctl disable firewalld.service
 
或者
4. iptables的状态：service iptables status
5. 关闭iptables：service iptables stop
6. 关闭开机启动：sudo systemctl disable iptables.service
~~~
- 安装SSH服务
  ssh 必须安装并且保证 sshd一直运行，以便用Hadoop脚本管理远端Hadoop守护进程。
~~~
#检查SSH的安装情况
[root@data-master01 /]# yum list installed | grep openssh-server
Repository extras is listed more than once in the configuration
openssh-server.x86_64                        8.0p1-10.el8                            @anaconda
[root@data-master01 /]#
 
#安装SSH
[root@data-master01 /]# yum install -y openssl openssh-server
Repository extras is listed more than once in the configuration
上次元数据过期检查：0:29:39 前，执行于 2022年03月13日 星期日 08时15分52秒。
软件包 openssl-1:1.1.1k-4.el8.x86_64 已安装。
软件包 openssh-server-8.0p1-10.el8.x86_64 已安装。
依赖关系解决。
====================================================================================================================================================================================
 软件包                                         架构                                  版本                                              仓库                                   大小
====================================================================================================================================================================================
升级:
 openssh                                        x86_64                                8.0p1-12.el8                                      baseos                                522 k
 openssh-clients                                x86_64                                8.0p1-12.el8                                      baseos                                668 k
 openssh-server                                 x86_64                                8.0p1-12.el8                                      baseos                                491 k
 openssl                                        x86_64                                1:1.1.1k-5.el8_5                                  base                                  709 k
 openssl-libs                                   x86_64                                1:1.1.1k-5.el8_5                                  base                                  1.5 M
 
#检查rsync的安装情况（yum list installed | grep rsync）
[root@data-master01]# rpm -qa | grep rsync
rsync-3.1.3-12.el8.x86_64
[root@data-master01]# dnf install rsync
已升级:
  rsync-3.1.3-14.el8.x86_64
  
~~~
- 验证SSH是否OK

SSH首次登录会有提示，输入yes即可，然后按照提示输入本机密码即可。但是这样每次登录都要输入密码，现在设置SSH无密码登录。首先退出SSH，利用ssh-keygen生成密钥，并将密钥加入到授权中。

~~~
#首次使用SSH登录本机（验证输入密码为root用户密码）
[root@data-master01 ~]# ssh localhost
The authenticity of host 'localhost (::1)' can't be established.
ECDSA key fingerprint is SHA256:qGJ+ADraO0oINgrJ2qQZy+uJh2Oc2K4/OGSNU+FHE9o.
Are you sure you want to continue connecting (yes/no/[fingerprint])? y
Please type 'yes', 'no' or the fingerprint: yes
Warning: Permanently added 'localhost' (ECDSA) to the list of known hosts.
root@localhost's password:
Activate the web console with: systemctl enable --now cockpit.socket
 
Last login: Sun Mar 13 12:28:17 2022 from 192.168.0.101
[root@data-master01 ~]#
 
#退出SSH，再次验证登录本机
[root@data-master01 ~]# ssh localhost
root@localhost's password:
Activate the web console with: systemctl enable --now cockpit.socket
 
Last login: Sun Mar 13 12:41:25 2022 from ::1
[root@data-master01 ~]# ls
anaconda-ks.cfg
[root@data-master01 ~]# exit
注销
Connection to localhost closed.
[root@data-master01 ~]#

~~~
配置SSH服务及协议秘钥登录
~~~
#在客户端生成密钥和公钥
[root@data-master01 ~]# ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
 cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
Generating public/private rsa key pair.
Your identification has been saved in /root/.ssh/id_rsa.
Your public key has been saved in /root/.ssh/id_rsa.pub.
The key fingerprint is:
SHA256:hv8F7WXb7GfxqHCiwgeIWq9TmPL93dWvv8dIcUTsiiE root@data-master01
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
[root@data-master01 ~]#
#重新启动SSH服务
[root@data-master01 ~]# systemctl stop sshd.service
[root@data-master01 ~]# systemctl start sshd.service
#设置开机自动启动ssh服务
[root@data-master01 ~]# systemctl enable sshd.service
~~~
最后验证SSH登录是否免密登录
~~~
[root@data-master01 ~]# ssh localhost
Activate the web console with: systemctl enable --now cockpit.socket
 
Last login: Sun Mar 13 13:08:07 2022 from 192.168.0.101
[root@data-master01 ~]#
~~~

### 1.2 安装JAVA
Hadoop3.x目前支持的JDK版本是1.8版本
~~~ shell
# 检查系统自带java，如果版并不符合则卸载
yum -y list java*
yum list installed | grep java
yum -y remove java-1.*
yum -y remove tzdata-java.noarch
yum -y remove javapackages-tools.noarch

# 安装1.8版本的JDK
## hwj2004dhc@163.com/Hanwj@2004
## https://www.oracle.com/cn/java/technologies/downloads/
## https://download.oracle.com/otn/java/jdk/8u381-b09/8c876547113c4e4aab3c868e9e0ec572/jdk-8u381-linux-x64.tar.gz?AuthParam=1695712558_bb656d182f8a493a775f95a7e077121e
tar -zvxf /soft/jdk-8u381-linux-x64.tar.gz -C /usr/local/
mv /usr/local/jdk1.8.0_381 /usr/local/java

#配置环境变量
[root@ef69bca31ddb ~]cat /etc/profile
# 编辑vi /etc/profile，在最后增加
export JAVA_HOME=/usr/local/java
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar
export PATH=$PATH:$JAVA_HOME/bin
---
[root@ef69bca31ddb ~]source /etc/profile
[root@ef69bca31ddb ~]# java -version
java version "1.8.0_381"
Java(TM) SE Runtime Environment (build 1.8.0_381-b09)
Java HotSpot(TM) 64-Bit Server VM (build 25.381-b09, mixed mode)
[root@ef69bca31ddb ~]#
[root@ef69bca31ddb ~]# whereis java
java: /usr/local/java /usr/local/java/bin/java
[root@ef69bca31ddb ~]
~~~

### 1.3 安装Hadoop
- **下载Hadoop包**
~~~ html
Apache DownloadsHome page of The Apache Software Foundation
https://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz
~~~
- **安装Hadoop包**
~~~ shell
# 解压缩hadoop
[root@ef69bca31ddb ~]# tar -zvxf /soft/hadoop-3.3.6.tar.gz -C /usr/local
[root@ef69bca31ddb ~]# mv /usr/local/hadoop-3.3.6 /usr/local/hadoop
 
#修改环境参数
[root@ef69bca31ddb ~]# vi /etc/profile
export HADOOP_HOME=/usr/local/hadoop
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
[root@wenjay ~]# source /etc/profile
~~~

- **单机模式下验证Hadoop**

  默认情况下，Hadoop被配置成以非分布式模式运行的一个独立Java进程。这对调试非常有帮助。
~~~ shell
[root@ef69bca31ddb hadoop]# cd /usr/local/hadoop/
[root@ef69bca31ddb hadoop]# mkdir /tmp/input
[root@ef69bca31ddb hadoop]# cp etc/hadoop/*.xml /tmp/input
[root@ef69bca31ddb hadoop]#  bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.2.jar grep /tmp/input /tmp/output 'dfs[a-z.]+'
[root@ef69bca31ddb hadoop]#  cat /tmp/output/*
1       dfsadmin
[root@ef69bca31ddb hadoop]# cd /tmp/output/
[root@ef69bca31ddb output]# ls
part-r-00000  _SUCCESS
[root@ef69bca31ddb output]# 
~~~

## 2、配置构建Hadoop集群
Hadoop支持一下三种方式的集群，
- Local (Standalone) Mode：单机模式
- Pseudo-Distributed Mode：伪军分布模式
- Fully-Distributed Mode：完全分布模式

#### 2.1 完全分布式集群构建（Fully-Distributed Operation）
**Hadoop集群配置说明**<br>

|     主机名     | 简写  | IP/hostname | 用户  |  安装软件   | 提供的服务                                                             |
|---------------|------|-------------|------|-----------|----------------------------------------------------------------------|
| data-master01 | dm01 |             | root |  Hadoop   | HDFS：NameNode、SecondaryNameNode <br>YARN：NodeManager、ResourceManager |
| data-worker01 | dw01 |             | root |  Hadoop   | NodeManager、DataNode                                                   |
| data-worker02 | dw02 |             | root |  Hadoop   | NodeManager、DataNode                                                   |

**现在Master01机器上配置相关环境**

#### 2.1.2  在Non-Secure Mode（非安全模式）下的配置
Hadoop的环境配置，由config/目录下的两种类型重要配置文件构成：<br>
- 只读默认配置：core-default.xml、hdfs-default.xml、yarn-default.xml和mapred-default.xml。
- 集群特有配置：etc/hadoop/core-site.xml、etc/hadoop/hdfs-site.xml、etc/hadoop/yarn-site.xml和etc/hadoop/mapred-site.xml。

此外，可以通过配置bin目录下的etc/hadoop/hadoop-env.sh和etc/hadoop/yarn-env.sh脚本文件的值来控制hadoop。
为了配置Hadoop集群，你需要配置Hadoop守护进程的执行环境和Hadoop守护进程的配置参数。
HDFS的守护进程是NameNode、SecondaryNameNode和DataNode；YARN的守护进程是 ResourceManager、NodeManager和WebAppProxy。
如果要使用MapReduce，那么MapReduce Job History Server也是在运行的。在大型的集群中，这些一般在不同的主机上运行。

#### 2.1.3 配置Hadoop后台进程的配置环境 hadoop-env.sh
~~~
export JAVA_HOME=/usr/local/java
export HDFS_NAMENODE_OPTS="-XX:+UseParallelGC -Xmx4g ${HDFS_NAMENODE_OPTS}"
export HADOOP_OPTS="${HADOOP_OPTS} -Djava.library.path=${HADOOP_HOME}/lib/native"
export HADOOP_PID_DIR=/tmp/hadoop
export HADOOP_LOG_DIR=/tmp/hadoop/logs
~~~
#### 2.1.4 配置Hadoop守护进程的参数
- **编辑core-site.xml文件：**vi etc/hadoop/core-site.xml
~~~ xml
    <!--指定hdfs的NameNode的URI-->
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://data-master01:9000</value>
    </property>
    <!--SequenceFiles中使用的读/写缓冲区的大小-->
    <property>
        <name>io.file.buffer.size</name>
        <value>4096</value>
    </property>
    <!--指定hadoop运行时产生文件的存放目录-->
    <property>
        <name>hadoop.tmp.dir</name>
        <value>file:/usr/local/hadoop/data</value>
    </property>
    <!-- 配置HDFS网页登录使用的静态用户为root -->
    <property>
        <name>hadoop.http.staticuser.user</name>
        <value>root</value>
    </property>
~~~
- **hdfs-site.xml文件：**vi etc/hadoop/hdfs-site.xml
- - NameNode的配置
~~~ xml
    <!--Configurations for NameNode-->
    <property>
        <name>dfs.namenode.name.dir</name>
        <!--value>file:///data/hadoop/hdfs/namenode</value-->
        <value>file://${hadoop.tmp.dir}/hdfs/namenode</value>
        <description>NameNode directory for namespace and transaction logs storage.</description>
    </property>
    <!-- nn web端访问地址-->
		<property>
			<name>dfs.namenode.http-address</name>
			<value>data-master01:9870</value>
		</property>
		<!-- 2nn web端访问地址-->
		<property>
			<name>dfs.namenode.secondary.http-address</name>
			<value>data-worker01:9868</value>
		</property>
		<!-- 测试环境指定HDFS副本的数量为2 -->
		<property>
			<name>dfs.replication</name>
			<value>2</value>
		</property>
~~~
- - DataNode的配置
~~~ xml
	<!--Configurations for DataNode-->
    <property>
        <name>dfs.datanode.data.dir</name>
        <!--value>file:///data/hadoop/hdfs/datanode</value-->
        <value>file://${hadoop.tmp.dir}/hdfs/datanode</value>
        <description>DataNode directory</description>
    </property>
~~~
- **yarn-site.xml文件**：vi etc/hadoop/yarn-site.xml
- - ResourceManager和NodeManager的配置：
~~~ xml
    <!-- Configurations for ResourceManager and NodeManager -->
    <property>
        <name>yarn.acl.enable</name>
        <value>false</value>  <!--Enable ACLs? Defaults to false-->
        <description></description>
    </property>
    <!-- Configurations for ResourceManager-->
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>data-master01</value>
    </property>
    <property>
        <name>yarn.resourcemanager.address</name>
        <value>${yarn.resourcemanager.hostname}:8030</value>
        <description>ResourceManager host:port for clients to submit jobs.</description>
    </property>
    <property>
        <name>yarn.resourcemanager.scheduler.address</name>
        <value>${yarn.resourcemanager.hostname}:8033</value>
        <description>ResourceManager host:port for ApplicationMasters to talk to Scheduler to obtain resources.</description>
    </property>
    <property>
        <name>yarn.resourcemanager.resource-tracker.address</name>
        <value>${yarn.resourcemanager.hostname}:8035</value>
        <description>ResourceManager host:port for NodeManagers.</description>
    </property>
    <property>
        <name>yarn.resourcemanager.webapp.address</name>
        <value>${yarn.resourcemanager.hostname}:8038</value>
        <description>ResourceManager web-ui host:port.</description>
    </property>
    <!--<property>
        <name>yarn.resourcemanager.webapp.https.address</name>
        <value>${yarn.resourcemanager.hostname}:8090</value>
    </property>-->
    <property>
        <name>yarn.resourcemanager.admin.address</name>
        <value>${yarn.resourcemanager.hostname}:8088</value>
        <description>ResourceManager host:port for administrative commands.</description>
    </property>
    <property>
        <name>yarn.resourcemanager.scheduler.class</name>
        <value>org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler</value>
        <description>	ResourceManager Scheduler class.</description>
    </property>
    <!-- 指定reducer获取数据的方式：shuffle-->
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    <property>
        <name>yarn.nodemanager.local-dirs</name>
        <value>${hadoop.tmp.dir}/yarn/local</value>
        <description>Comma-separated list of paths on the local filesystem where intermediate data is written.</description>      
    </property>
    <property>
        <name>yarn.nodemanager.log-dirs</name>
        <value>${hadoop.tmp.dir}/yarn/logs</value>
        <description>Comma-separated list of paths on the local filesystem where intermediate data is written.</description>      
    </property>
    <!-- yarn容器允许分配的最大最小内存-->
    <property>
    	<name>yarn.scheduler.minimum-allocation-mb</name>
    	<value>512</value>
    </property>
    <property>
    	<name>yarn.scheduler.maximum-allocation-mb</name>
    	<value>2048</value>
    </property>
    <!-- yarn容器允许管理的物理内存大小-->
    <property>
    	<name>yarn.nodemanager.resource.memory-mb</name>
    	<value>4096</value>
    </property>
    <!-- 关闭yarn对虚拟内存的限制检查-->
    <property>
    	<name>yarn.nodemanager.vmem-check-enabled</name>
    	<value>false</value>
    </property>
    <!-- 设置日志聚集服务器地址 -->
    <property>
    	<name>yarn.log.server.url</name>
    	<value>http://data-worker01:9999/jobhistory/logs</value>
    </property>
    <!-- Configurations for History Server (Needs to be moved elsewhere):-->
    <!-- 设置日志保留时间为7天(7*24*60*60) -->
    <property>
    	<name>yarn.log-aggregation.retain-seconds</name>
    	<value>604800</value>
    </property>
~~~

- **mapred-site.xml文件：**vi etc/hadoop/mapred-site.xml
- - MapReduce应用程序的配置：
~~~ xml
    <!-- Configurations for MapReduce Applications -->
    <!--指定mapreduce运行在yarn上-->
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
        <description>Execution framework set to Hadoop YARN.</description>
    </property>
    <!-- Configurations for MapReduce JobHistory Server -->
    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>data-worker01:10020</value>
        <description>MapReduce JobHistory Server host:port</description>
    </property>
    <property>
    	  <name>mapreduce.jobhistory.webapp.address</name>
    	  <value>data-worker01:9999</value>>
    	  <description>MapReduce JobHistory Server Web UI host:port</description>
    </property>
~~~
#### 2.1.5 配置workers文件(Slaves File)
打开${HADOOP_HOME}/etc/hadoop/workers,把localost删掉，然后配置上所有从节点的主机名，注意：这样配置的前提是主节点要能免密登录到从节点中
通常，你选择集群中的一台机器作为NameNode，另外一台不同的机器作为JobTracker。余下的机器即作为DataNode又作为TaskTracker，这些被称之为slaves。
在/workers文件中列出所有slave的主机名或者IP地址，一行一个。
~~~ text
data-master01
data-worker01
data-worker02
~~~

#### 2.1.5 修改启动shell脚本
~~~shell
# sbin/start-dfs.sh，stop-dfs.sh
#HWJ ADD
HDFS_DATANODE_USER=root
HDFS_DATANODE_SECURE_USER=root
HDFS_NAMENODE_USER=root
HDFS_SECONDARYNAMENODE_USER=root
#HWJ ADD

# sbin/start-yarn.sh，stop-yarn.sh
#HWJ ADD
YARN_RESOURCEMANAGER_USER=root
HADOOP_SECURE_DN_USER=root
YARN_NODEMANAGER_USER=root
#HWJ ADD
~~~

**注意：修改脚步后可以通过命令同步，如**
~~~shell
rsync -r $HADOOP_HOME/etc/hadoop/hdfs-site.xml slave1:$HADOOP_HOME/etc/hadoop/hdfs-site.xml
~~~
## 3，复制hadoop服务构建机器集群
把/usr/local/hadoop复制到其他节点上。复制之前先修改master服务的hosts文件
~~~
修改后的Master节点
[root@data-master01 ~]# cat /etc/hosts
#127.0.0.1   localhost localhost.localdomain wenjay  data-master01
#::1         localhost localhost.localdomain wenjay  data-master01
192.168.0.40 data-master01 localhost
192.168.0.43 data-worker01
192.168.0.44 data-worker02
~~~
复制已配置好的服务hadoop目录到，worker01和worker02
~~~
cd /usr/local
tar -zxcf hadoop.tar.gz ./hadoop   # 先压缩再复制
scp ./hadoop.tar.gz data-worker01:/usr/local
scp ./hadoop.tar.gz data-worker02:/usr/local
~~~
登录worker01和worker02系统，解压已复制过来的文件
~~~
cd /usr/local
rm -r ./hadoop    # 删掉旧的（如果存在）
tar -zxvf hadoop.tar.gz
~~~
修改worker01和worker02的hosts文件
~~~
#修改后的worker节点
[root@data-worker01 ~]# cat /etc/hosts
#127.0.0.1   localhost localhost.localdomain wenjay  data-worker01
#::1         localhost localhost.localdomain wenjay  data-worker01
192.168.0.40 data-master01
192.168.0.43 data-worker01 localhost
192.168.0.44 data-worker02
#修改后的worker节点
[root@data-worker02 ~]# cat /etc/hosts
#127.0.0.1   localhost localhost.localdomain wenjay  data-worker02
#::1         localhost localhost.localdomain wenjay  data-worker02
192.168.0.40 data-master01
192.168.0.43 data-worker01 
192.168.0.44 data-worker02 localhost
~~~

### 4、操作Hadoop集群
启动Hadoop集群需要启动HDFS集群和Map/Reduce集群；首次打开HDFS时，必须对其进行格式化。将新的分布式文件系统格式化为hdfs：
- 格式化一个分布式文件系统：（登录master01机器）
~~~ shell
[root@data-master01 ~]# hadoop namenode -format
~~~

- Hadoop启动
~~~ shell
[root@data-master01 ~]# start-all.sh
Starting namenodes on [data-master01]
data-master01: Warning: Permanently added 'data-master01' (ED25519) to the list of known hosts.
Starting datanodes
data-worker02: Warning: Permanently added 'data-worker02' (ED25519) to the list of known hosts.
data-worker01: WARNING: /tmp/hadoop does not exist. Creating.
data-worker01: WARNING: /tmp/hadoop/logs does not exist. Creating.
data-worker02: WARNING: /tmp/hadoop does not exist. Creating.
data-worker02: WARNING: /tmp/hadoop/logs does not exist. Creating.
Starting secondary namenodes [data-master01]
Starting resourcemanager
Starting nodemanagers
[root@data-master01 ~]#
~~~
- 登录各个节点服务，通过jps命令查看各自启动的进程
~~~ shell
[root@data-master01 ~]# jps
2647 ResourceManager
2170 DataNode
3163 Jps
2413 SecondaryNameNode
2782 NodeManager
2015 NameNode
[root@data-master01 ~]#
[root@data-worker01 ~]# jps
1877 DataNode
2184 Jps
2011 NodeManager
[root@data-worker01 ~]#
[root@data-worker02 ~]# jps
1881 DataNode
2189 Jps
2015 NodeManager
[root@data-worker02 ~]#
~~~
从浏览器登录，验证服务：
http://192.168.0.40:9870/
http://192.168.0.40:8088/

- Hadoop关闭
~~~ shell
[root@data-master01 ~]# stop-all.sh
Stopping namenodes on [data-master01]
Stopping datanodes
Stopping secondary namenodes [data-master01]
Stopping nodemanagers
data-worker01: WARNING: nodemanager did not stop gracefully after 5 seconds: Trying to kill with kill -9
data-worker02: WARNING: nodemanager did not stop gracefully after 5 seconds: Trying to kill with kill -9
Stopping resourcemanager
[root@data-master01 ~]#
~~~
