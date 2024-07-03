# 排查实例 CPU使用率高的原因和解决方法

在使用分析型数据库PostgreSQL时，如果实例的CPU使用率长时间偏高，将会导致数据库读写处理性能下降，影响业务正常运行，需要重点分析原因并解决
参考：https://www.ctyun.cn/document/10015123/10181394

## 排查实例 CPU使用率高的原因和解决方法
- 确定高 CPU 使用率的原因：
  - 使用系统监控工具（如top、htop等）检查占用 CPU 资源最多的进程或查询。
  - 使用系统视图（如pg_stat_activity、pg_stat_bgwriter等）检查正在执行的查询、锁定情况和系统活动。
- 识别导致高 CPU 使用率的问题：
  - 检查是否存在并发冲突、死锁或长时间等待的情况。
  - 检查是否有大量的并行查询或并行工作者导致 CPU 资源竞争。
- 解决高 CPU 使用率的问题：
  - 优化查询：通过优化查询语句、索引设计、表分区等方式来改善查询性能，减少 CPU 资源的消耗。
  - 调整配置参数：根据实际情况调整配置参数，如shared_buffers、work_mem、max_connections等，以平衡系统资源的使用。
  - 调整并行设置：如果高CPU使用率与并行查询相关，可以调整max_parallel_workers_per_gather参数来限制并行工作者的数量，以减少资源竞争。
  - 升级硬件：如果高CPU使用率是由于硬件资源不足导致的，可以考虑升级服务器或增加节点来提供更多的计算资源。

## 实例参数调优建议
- 把vm.overcommit_memory设置为2。<br>
  Linux为了保证在有限的内存中尽可能多的运行更多的进程，在内存分配策略中提出了overcommit的策略，即允许内存溢出。之所以可以这么做是因为我们进程在申请内存时并不会立刻分配内存页，而是到使用时才进行分配。所以Linux才允许进程overcommit，因为即使申请了这么多实际可能也用不到，所以便允许进程申请了。
  <br>参考：https://baijiahao.baidu.com/s?id=1706961998236734349&wfr=spider&for=pc
- 不要配置OS使用大页。
- 使用gp_vmem_protect_limit设置实例可以为每个Segment数据库中执行的所有工作分配的最大内存。
- 通过下面的计算为gp_vmem_protect_limit设置值：
  - gp_vmem –数据库可用的总内存。
  ~~~
  gp_vmem = ((SWAP + RAM) – (7.5GB + 0.05 * RAM)) / 1.7
  ~~~
  其中 SWAP是该主机的交换空间（以GB为单位），RAM是该主机的RAM（以GB为单位）。

  - max_acting_primary_segments – 当镜像Segment由于主机或者Segment失效而被激活时，能在一台主机上运行的最主Segment的最大数量。

  - gp_vmem_protect_limit，转换成MB来设置配置参数的值。
  ~~~
  gp_vmem_protect_limit = gp_vmem / acting_primary_segments
  ~~~
- 在有大量工作文件被生成的场景下用下面的公式计算将工作文件考虑在内的gp_vmem因子。
  ~~~
  gp_vmem = ((SWAP + RAM) – (7.5GB + 0.05 * RAM - (300KB * total_#_workfiles))) / 1.7
  ~~~
- 绝不将gp_vmem_protect_limit设置得过高或者比系统上的物理RAM大。
- 使用计算出的gp_vmem值来计算操作系统参数vm.overcommit_ratio的设置。
  ~~~
  vm.overcommit_ratio = (RAM - 0.026 * gp_vmem) / RAM
  ~~~
- 使用statement_mem来分配每个Segment数据库中用于一个查询的内存。
- 使用资源队列设置活动查询的数目（ACTIVE_STATEMENTS）以及队列中查询所能利用的内存量（MEMORY_LIMIT）。
- 把所有的用户都与一个资源队列关联。不要使用默认的队列。
- 设置PRIORITY以匹配用于负载以及实际情况的队列的实际需要。
- 确保资源队列的内存分配不会超过gp_vmem_protect_limit的设置。
- 动态更新资源队列设置以匹配日常操作流。

## 实例CPU使用率高的排查及解决办法
### 问题描述
  阿里云云数据库RDS PostgreSQL使用过程中，可能会遇到CPU使用率过高甚至达到100%的情况。本文将介绍造成该状况的常见原因以及解决方法，并通过CPU使用率为100%的典型场景，来分析引起该状况的排查及其相应的解决方案。
  
  https://www.alibabacloud.com/help/zh/rds/support/how-to-troubleshoot-high-cpu-utilization-of-apsaradb-rds-for-postgresql

### 解决方案
  CPU使用率到达100%，首先检查是不是业务高峰活跃连接陡增，而数据库预留的资源不足。需要查看问题发生时，活跃的连接数是否比平时多很多。对于RDS PostgreSQL，数据库上的连接数变化，可以从控制台的监控信息中看到。而当前活跃的连接数，可以直接连接数据库，使用下列查询语句得到。
  ~~~
  select count( * ) from pg_stat_activity where state not like '%idle';
  ~~~

### 追踪慢SQL
如果活跃连接数的变化处于正常范围，则可能是当时有性能很差的SQL被大量执行。由于RDS有慢SQL日志，可以通过这个日志，定位到当时比较耗时的SQL来进一步做分析。但通常问题发生时，整个系统都处于停滞状态，所有SQL都慢下来，当时记录的慢SQL可能非常多，并不容易找到目标。这里介绍几种追查慢SQL的方法。

- 1、第一种方法是使用pg_stat_statements插件定位慢SQL，仅适用于PostgreSQL，步骤如下。
  - a. 如果没有pg_stat_statements插件，需要先手动创建。要利用插件和数据库系统里面的计数信息（如SQL执行时间累积等），而这些信息是不断累积的，包含了历史信息。为了更方便的排查当前的CPU过高问题，要先使用以下命令重置计数器。
    ~~~
    create extension pg_stat_statements;
    select pg_stat_reset();
    select pg_stat_statements_reset();
    ~~~
  - b. 等待一段时间（例如1分钟），使计数器积累足够的信息。
  - c. 参考以下命令查询最耗时的SQL，一般就是导致问题的直接原因。
    ~~~
    select * from pg_stat_statements order by total_time desc limit 5;
    ~~~
  - d. 参考以下SQL语句，查询读取Buffer次数最多的SQL，这些SQL可能由于所查询的数据没有索引，而导致了过多的Buffer读，也同时大量消耗了CPU。
  ~~~
  select * from pg_stat_statements order by shared_blks_hit+shared_blks_read desc limit 5;
  ~~~
- 2、第二种方法是直接通过pg_stat_activity视图，参考以下查询SQL语句，查看当前长时间执行，一直不结束的SQL。这些SQL也可能造成CPU过高。
  ~~~
  select datname,
         usename,
         client_addr,
         application_name,
         state,
         backend_start,
         xact_start,
         xact_stay,
         query_start,
         query_stay,
         replace(query, chr(10), ' ') as query
  from
    (select pgsa.datname as datname,
            pgsa.usename as usename,
            pgsa.client_addr client_addr,
            pgsa.application_name as application_name,
            pgsa.state as state,
            pgsa.backend_start as backend_start,
            pgsa.xact_start as xact_start,
            extract(epoch
                    from (now() - pgsa.xact_start)) as xact_stay,
            pgsa.query_start as query_start,
            extract(epoch
                    from (now() - pgsa.query_start)) as query_stay,
            pgsa.query as query
     from pg_stat_activity as pgsa
     where pgsa.state != 'idle'
       and pgsa.state != 'idle in transaction'
       and pgsa.state != 'idle in transaction (aborted)') idleconnections
  order by query_stay desc
  limit 5;
  ~~~

- 3、第3种方法是从数据表上表扫描（Table Scan）的信息开始查起，查找缺失索引的表。数据表如果缺失索引，大部分热数据又都在内存时（例如内存8G，热数据6G），此时数据库只能使用表扫描，并需要处理已在内存中的大量无关记录，导致耗费大量CPU。特别是对于表记录数超过100的表，一次表扫描占用大量CPU（基本把一个CPU占满）和多个连接并发（例如上百连接）。
  - a. 参考以下SQL语句，查出使用表扫描最多的表。
    ~~~
    select * from pg_stat_user_tables where n_live_tup > 100000 and seq_scan > 0 order by seq_tup_read desc limit 10;
    ~~~
  - c. 参考以下SQL语句，查询当前正在运行的访问到上述表的慢查询。
    ~~~
    select * from pg_stat_activity where query ilike '%<table name>%' and query_start - now() > interval '10 seconds';
    ~~~
    **说明：也可以通过pg_stat_statements插件定位涉及到这些表的查询，如下所示。 select * from pg_stat_statements where query ilike '%<table>%'order by shared_blks_hit+shared_blks_read desc limit 3;**

### 处理慢SQL

对于上面的方法查出来的慢SQL，如下所示，首先需要做的是结束掉它们，使业务先恢复。
~~~
select pg_cancel_backend(pid) from pg_stat_activity where  query like '%<query text>%' and pid != pg_backend_pid();
select pg_terminate_backend(pid) from pg_stat_activity where  query like '%<query text>%' and pid != pg_backend_pid();
~~~

如果这些SQL确实是业务上必需的，则需要对他们做如下优化。
- 1、对查询涉及的表，执行ANALYZE [$Table]或VACUUM ANZLYZE [$Table]语句，更新表的统计信息，使查询计划更准确。为避免对业务影响，最好在业务低峰执行。
  **说明：[$Table]为查询涉及的表。**
- 2、选择一条如下SQL语句执行，查看SQL的执行计划，第一条SQL语句不会实际执行SQL语句，第二条SQL语句会实际执行而且能得到详细的执行信息，对其中的Table Scan涉及的表，建立索引。
  ~~~
  explain [$Query_Text]
  explain (buffers true, analyze true, verbose true) [$Query_Text]
  ~~~
  **说明：[$Query_Text]为SQL文件或语句。**
- 3、重新编写SQL语句，去除掉不必要的子查询、改写UNION ALL、使用JOIN CLAUSE固定连接顺序等，都是进一步深度优化SQL语句的手段。

## 其他调查实例
[如何排查 Amazon RDS 或 Amazon Aurora PostgreSQL 的高 CPU 利用率问题？](https://repost.aws/zh-Hans/knowledge-center/rds-aurora-postgresql-high-cpu)
[如何使用 Amazon RDS 为 PostgreSQL 启用查询日志记录？](https://repost.aws/zh-Hans/knowledge-center/rds-postgresql-query-logging)