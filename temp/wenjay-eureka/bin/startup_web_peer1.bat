@echo off
rem ---------------------------------------------------------------------------
rem Start script for the eureka Server
rem ---------------------------------------------------------------------------

set "JAVA_HOME=D:/JDev/JDK1.8.0_152"
set path=%path%;%JAVA_HOME%\bin;
set "CLASSPATH=%CLASSPATH%;%JAVA_HOME%/lib;"

rem START "wenjay-eureka-01" "java" -Xms1024m -Xmx1024m -jar wenjay-eureka.jar --spring.profiles.active=eureka-peer --server.port=7001 --eureka.instance.hostname=wenjay-eureka-01 --wenjay-eureka-peer1.hostname=wenjay-eureka-02 --wenjay-eureka-peer1.port=7002 --wenjay-eureka-peer2.hostname=wenjay-eureka-03 --wenjay-eureka-peer2.port=7003
REM 注册中心集群服务配置，需要指定eureka-peer.zone的配置
START "wenjay-eureka-01" "java" -Xms1024m -Xmx1024m -jar wenjay-eureka.jar --spring.profiles.active=eureka-peer --spring.security.user.name=wenjay --spring.security.user.password=wenjay --server.port=7001 --eureka.instance.hostname=wenjay-eureka-01 --eureka-peer.zone=http://wenjay:wenjay@wenjay-eureka-02:7002/eureka,http://wenjay:wenjay@wenjay-eureka-03:7003/eureka

pause