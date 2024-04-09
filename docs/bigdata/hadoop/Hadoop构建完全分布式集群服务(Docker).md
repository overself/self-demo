# Apache Hadoop完全分布式集群环境构建（Docker）

<!-- TOC -->
* [Apache Hadoop完全分布式集群环境构建（Docker）](#apache-hadoop完全分布式集群环境构建docker)
  * [1 构建Hadoop服务](#1-构建hadoop服务)
    * [1.1 启动一个新容器（单机模式）](#11-启动一个新容器单机模式)
    * [1.2 安装JAVA](#12-安装java)
    * [1.3 安装Hadoop](#13-安装hadoop)
  * [2、配置构建Hadoop集群(本环境为windows Docker Desktop)](#2配置构建hadoop集群--本环境为windows-docker-desktop-)
      * [2.1 完全分布式集群构建（Fully-Distributed Operation）](#21-完全分布式集群构建fully-distributed-operation)
      * [2.1.1  先决条件](#211--先决条件)
      * [2.1.2  在Non-Secure Mode（非安全模式）下的配置](#212--在non-secure-mode非安全模式下的配置)
      * [2.1.3 配置Hadoop后台进程的配置环境 hadoop-env.sh](#213-配置hadoop后台进程的配置环境-hadoop-envsh)
      * [2.1.4 配置Hadoop守护进程的参数](#214-配置hadoop守护进程的参数)
      * [2.1.5 配置workers文件(Slaves File)](#215-配置workers文件--slaves-file-)
      * [2.1.6 监控NodeManager的健康状态](#216-监控nodemanager的健康状态)
      * [2.1.7 Hadoop Rack Awareness（机架感知）](#217-hadoop-rack-awareness机架感知)
      * [2.1.8 日志](#218-日志)
  * [3 构建Centos Hadoop集群容器](#3-构建centos-hadoop集群容器)
    * [3.1 独立docker容器，通过共享网络方式](#31-独立docker容器通过共享网络方式)
  * [4、操作Hadoop集群](#4操作hadoop集群)
    * [Hadoop环境问题](#hadoop环境问题)
  * [参考：](#参考)
<!-- TOC -->

参考：https://hadoop.apache.org/docs/r3.3.6/hadoop-project-dist/hadoop-common/ClusterSetup.html

## 1 构建Hadoop服务
### 1.1 启动一个新容器（单机模式）
~~~
docker run -itd -p 10022:22 --privileged=true --name=centos_hadoop centos_sshd:1.0.0 /sbin/init
docker exec -it centos_hadoop bash
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
## 
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

## 2、配置构建Hadoop集群(本环境为windows Docker Desktop)
Hadoop支持一下三种方式的集群，
- Local (Standalone) Mode：单机模式
- Pseudo-Distributed Mode：伪军分布模式
- Fully-Distributed Mode：完全分布模式

#### 2.1 完全分布式集群构建（Fully-Distributed Operation）
#### 2.1.1  先决条件
从单机模式的Hadoop环境复制，上面的单机Hadoop节点已经满足了所有必要的软件条件。
在三个服务环境之间SSH无密码可登录互访。

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
#### 2.1.6 监控NodeManager的健康状态

略

#### 2.1.7 Hadoop Rack Awareness（机架感知）

略

#### 2.1.8 日志

略

#### 2.1.9 容器之间同步文件
~~~
# 如修改了hdfs-site.xml文件
rsync -r $HADOOP_HOME/etc/hadoop/hdfs-site.xml data-worker01:$HADOOP_HOME/etc/hadoop/hdfs-site.xml
rsync -r $HADOOP_HOME/etc/hadoop/hdfs-site.xml data-worker02:$HADOOP_HOME/etc/hadoop/hdfs-site.xml
rsync -r $HADOOP_HOME/etc/hadoop/core-site.xml data-worker01:$HADOOP_HOME/etc/hadoop/core-site.xml
rsync -r $HADOOP_HOME/etc/hadoop/core-site.xml data-worker02:$HADOOP_HOME/etc/hadoop/core-site.xml
rsync -r $HADOOP_HOME/etc/hadoop/yarn-site.xml data-worker01:$HADOOP_HOME/etc/hadoop/yarn-site.xml
rsync -r $HADOOP_HOME/etc/hadoop/yarn-site.xml data-worker02:$HADOOP_HOME/etc/hadoop/yarn-site.xml

# 也可以这更目录同步
scp -r $HADOOP_HOME/etc/hadoop data-worker01:$HADOOP_HOME/etc

~~~

## 3 构建Centos Hadoop集群容器
- 提交已构建好的Hadoop容器为新的容器
~~~
#docker commit -m "hadoop install" {CONTAINER ID} ubuntu:hadoop
PS C:\Users\Admin> docker commit -m "centos hadoop" ef69bca31ddbe8eae5e79dae4819017fcf27e0014d7b40ecb294fec89a8e7f84 centos_hadoop:1.0.0
sha256:923bafa87f39a37600a962deb70330fb1d3de5a35089d729b5a3a8c616c9dae2
PS C:\Users\Admin>
PS C:\Users\Admin> docker images
REPOSITORY                                         TAG             IMAGE ID       CREATED         SIZE
centos_hadoop                                      1.0.0           923bafa87f39   4 minutes ago   4.48GB
~~~

### 3.1 独立docker容器，通过共享网络方式
- 构建docker共享网络
  因为要求三个容器可以互访，因此构建一个共享的bridge网络
~~~ shell
# 构建共享网络
docker network create --driver=bridge hadoop_net
docker network ls
docker network inspect hadoop_net
~~~~~~ shell
PS C:\Users\Admin> docker network ls
NETWORK ID     NAME              DRIVER    SCOPE
d11f02fd2ef0   bridge            bridge    local
ba0e826a74cd   host              host      local
bb185d047101   none              null      local

PS C:\Users\Admin> docker network create --driver=bridge hadoop_net
d924bf46a7a2cf664720c2b88727769afbca5d415af304d4c95dad1fa3303acf
PS C:\Users\Admin> docker network inspect hadoop_net
[
    {
        "Name": "hadoop_net",
        "Id": "d924bf46a7a2cf664720c2b88727769afbca5d415af304d4c95dad1fa3303acf",
        "Created": "2023-09-27T08:35:42.6405711Z",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.18.0.0/16",
                    "Gateway": "172.18.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {},
        "Options": {},
        "Labels": {}
    }
]
PS C:\Users\Admin>
~~~ 

- 新建容器集群（data-master01，data-worker01，data-worker02）

**Hadoop集群配置说明**<br>

|     主机名     | 简写  | IP/hostname | 用户  |  安装软件   | 提供的服务                                                             |
|---------------|------|-------------|------|-----------|----------------------------------------------------------------------|
| data-master01 | dm01 |             | root |  Hadoop   | HDFS：NameNode、SecondaryNameNode <br>YARN：NodeManager、ResourceManager |
| data-worker01 | dw01 |             | root |  Hadoop   | NodeManager、DataNode                                                   |
| data-worker02 | dw02 |             | root |  Hadoop   | NodeManager、DataNode                                                   |

创建三个容器，指定容器的名称与上面workers配置的服务器名称一致
~~~ shell
# 构建共享网络下的不同节点容器
# docker run -itd -p 12222:22 -p 19870:9870 -p 18080:8080 -p 10020:10020 -p 10021:10021 -p 18088:8088 -p 19888:19888 -p 18042:8042 -p 18031:8031 -p 18032:8032 -p 19000:9000 -p 19077:9077 -h data-master01 --network hadoop_net --privileged=true --name=hadoop-master01 centos_hadoop:1.1.0 /sbin/init
# docker run -itd -p 14422:22 -p 19874:9864 -p 18142:8042 -h data-worker01 --network hadoop_net --privileged=true --name=hadoop-worker01 centos_hadoop:1.1.0 /sbin/init
# docker run -itd -p 15522:22 -p 19884:9864 -p 18242:8042 -h data-worker02 --network hadoop_net --privileged=true --name=hadoop-worker02 centos_hadoop:1.1.0 /sbin/init
PS C:\Users\Admin> docker run -itd -p 12222:22 -p 10020:10020 -p 10021:10021 -p 19888:19888 -p 9870:9870 -p 8080:8080 -p 8088:8088 -p 8042:8042 -p 8030:8030 -p 8031:8031 -p 8032:8032 -p 8033:8033 -p 9077:9077 -p 9000:9000 -p 9864:9864 -h data-master01 --network hadoop_net --privileged=true --name=hadoop-master01 centos_hadoop:1.1.0 /sbin/init
0fe2bc15b60387961eaa1f9e0e9be680153dc1fd349845d517e80e11ae9515e3
PS C:\Users\Admin> docker run -itd -p 14422:22 -p 9874:9864 -p 18042:8042 -p 8142:8142 -h data-worker01 --network hadoop_net --privileged=true --name=hadoop-worker01 centos_hadoop:1.1.0 /sbin/init
fb2112db97b36730a5ad54bc774599c107cfccd2763910c397a8d93fe6016db1
PS C:\Users\Admin> docker run -itd -p 15522:22 -p 9884:9864 -p 28042:8042 -p 8242:8242 -h data-worker02 --network hadoop_net --privileged=true --name=hadoop-worker02 centos_hadoop:1.1.0 /sbin/init
bc5f0f719fab9521409049ce6122552d49b7490639113cfa5f98ba9dfa9c68fb
PS C:\Users\Admin>
~~~
- 验证三个容器之间是否可以免密SSH登录
~~~ shell
[root@data-master01 ~]# ssh data-worker01
The authenticity of host 'data-worker01 (172.18.0.3)' can't be established.
ECDSA key fingerprint is SHA256:expb2qlgeLug580R5EoY+Tkr3U7iXa6ObRhfmi6X4Ko.
Are you sure you want to continue connecting (yes/no/[fingerprint])? yes
Warning: Permanently added 'data-worker01,172.18.0.3' (ECDSA) to the list of known hosts.
Last login: Wed Sep 27 05:53:22 2023 from 172.17.0.1
[root@data-worker01 ~]#
~~~

## 4、操作Hadoop集群
启动Hadoop集群需要启动HDFS集群和Map/Reduce集群；首次打开HDFS时，必须对其进行格式化。将新的分布式文件系统格式化为hdfs：
- 格式化一个分布式文件系统：（每个几点都执行）
~~~ shell
[root@data-master01 ~]# hadoop namenode -format
~~~
- 修改启动文件脚本
~~~shell
export HDFS_NAMENODE_USER=root
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
http://localhost:19870/
http://localhost:18088/


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

### Hadoop环境问题

## 参考：
https://blog.csdn.net/weixin_42258633/article/details/131361086
https://downloads.apache.org/
https://www.xjx100.cn/news/684426.html?action=onClick

