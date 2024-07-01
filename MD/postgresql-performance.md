# PostgreSQL性能分析

## PostgreSQL 执行计划

PostgreSQL 中的EXPLAIN支持 SELECT、INSERT、UPDATE、DELETE、VALUES、EXECUTE、DECLARE、CREATE TABLE AS 以及 CREATE MATERIALIZED VIEW AS 语句。

~~~postgresql
EXPLAIN
SELECT e.first_name,e.last_name,e.salary,d.department_name
  FROM employees e
  JOIN departments d ON (e.department_id = d.department_id)
 WHERE e.salary > 15000;

QUERY PLAN                                                            |
----------------------------------------------------------------------|
Hash Join  (cost=3.38..4.84 rows=3 width=29)                          |
  Hash Cond: (d.department_id = e.department_id)                      |
  ->  Seq Scan on departments d  (cost=0.00..1.27 rows=27 width=15)   |
  ->  Hash  (cost=3.34..3.34 rows=3 width=22)                         |
        ->  Seq Scan on employees e  (cost=0.00..3.34 rows=3 width=22)|
              Filter: (salary > '15000'::numeric)                     |
~~~
PostgreSQL 执行计划的顺序按照缩进来判断，缩进越多的越先执行，同样缩进的从上至下执行。对于以上示例，首先对 employees 表执行全表扫描（Seq Scan），使用 salary > 15000 作为过滤条件；cost 分别显示了预估的返回第一行的成本（0.00）和返回所有行的成本（3.34）；rows 表示预估返回的行数；width 表示预估返回行的大小（单位 Byte）。然后将扫描结果放入到内存哈希表中，两个 cost 都等于 3.34，因为是在扫描完所有数据后一次性计算并存入哈希表。接下来扫描 departments 并且根据 department_id 计算哈希值，然后和前面的哈希表进行匹配（d.department_id = e.department_id）。最上面的一行表明数据库采用的是 Hash Join 实现连接操作。

## EXPLAIN ANALYZE
PostgreSQL 中的EXPLAIN也可以使用 ANALYZE 选项显示语句的实际运行时间和更多信息：
~~~postgresql
EXPLAIN ANALYZE
SELECT e.first_name,e.last_name,e.salary,d.department_name
  FROM employees e
  JOIN departments d ON (e.department_id = d.department_id)
 WHERE e.salary > 15000;

QUERY PLAN                                                                                                      |
----------------------------------------------------------------------------------------------------------------|
Hash Join  (cost=3.38..4.84 rows=3 width=29) (actual time=0.347..0.382 rows=3 loops=1)                          |
  Hash Cond: (d.department_id = e.department_id)                                                                |
  ->  Seq Scan on departments d  (cost=0.00..1.27 rows=27 width=15) (actual time=0.020..0.037 rows=27 loops=1)  |
  ->  Hash  (cost=3.34..3.34 rows=3 width=22) (actual time=0.291..0.292 rows=3 loops=1)                         |
        Buckets: 1024  Batches: 1  Memory Usage: 9kB                                                            |
        ->  Seq Scan on employees e  (cost=0.00..3.34 rows=3 width=22) (actual time=0.034..0.280 rows=3 loops=1)|
              Filter: (salary > '15000'::numeric)                                                               |
              Rows Removed by Filter: 104                                                                       |
Planning Time: 1.053 ms                                                                                         |
Execution Time: 0.553 ms                                                                                        |
~~~

EXPLAIN ANALYZE通过执行语句获得了更多的信息。其中，actual time 是每次迭代实际花费的平均时间（ms），也分为启动时间和完成时间；loops 表示迭代次数；Hash 操作还会显示桶数（Buckets）、分批数量（Batches）以及占用的内存（Memory Usage），Batches 大于 1 意味着需要使用到磁盘的临时存储；Planning Time 是生成执行计划的时间；Execution Time 是执行语句的实际时间，不包括 Planning Time。

## 分析PostgreSQL服务的CPU使用率持续飙高的问题的方法
### 可能的原因
- 慢查询：长时间运行的查询或复杂的查询可能会占用大量CPU资源。
- 高并发：许多并发的数据库连接和查询会导致CPU负载增加。
- 索引问题：缺少必要的索引或不适当的索引会导致查询效率低下。
- 表锁：频繁的写操作可能会导致表锁，从而影响查询性能。
- 配置问题：不适当的PostgreSQL配置（例如，work_mem，shared_buffers等）可能会影响性能。
- 硬件资源不足：服务器硬件资源（如CPU，内存等）不足也会导致性能问题。
### 分析手段
- 检查慢查询日志：启用并查看慢查询日志，识别哪些查询耗时最长。
~~~postgresql
SET log_min_duration_statement = '1000'; -- 记录超过1000毫秒的查询
~~~
- 使用pg_stat_statements扩展：安装并启用pg_stat_statements扩展，查看消耗最多资源的查询。
~~~postgresql
CREATE EXTENSION pg_stat_statements;
SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;
~~~
- 监控工具：使用监控工具如pgAdmin、Datadog、Prometheus等来监控数据库性能。这些工具可以提供实时的CPU使用率、查询性能、锁信息等。
  <br>pgAdmin 4下载地址： https://www.pgadmin.org/download/
- EXPLAIN ANALYZE：使用EXPLAIN ANALYZE来分析具体查询的执行计划，识别潜在的性能瓶颈。
~~~postgresql
EXPLAIN ANALYZE SELECT * FROM your_table WHERE your_condition;
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM your_table WHERE your_condition;
~~~
- 系统资源监控：使用系统工具如top、htop、vmstat等监控系统资源使用情况。
- PostgreSQL配置检查：
- 检查并优化PostgreSQL的配置参数，确保它们适合当前的负载和硬件环境。
- 索引和架构优化：确保适当的索引存在，并考虑对表进行分区或其他架构优化。
- 硬件升级：在确认数据库优化无效后，考虑升级硬件资源，如增加CPU和内存。

## 使用pgAdmin监控和分析性能
- 使用服务器监控面板：<br>
在pgAdmin中，选择你连接的服务器，右键点击并选择“Dashboard”。在Dashboard中，可以看到基本的性能指标，如CPU使用率、内存使用率、磁盘IO等。
- 启用和查看慢查询日志：<br>
在pgAdmin中，导航到“Configuration”选项卡，找到并设置参数log_min_duration_statement为合适的值（例如1000毫秒）。这样，任何超过这个时间的查询都会被记录下来。
- 使用查询工具查看pg_stat_activity:<br>
- 使用pgAdmin的查询工具，运行以下SQL语句，查看当前正在运行的查询：
~~~postgresql
SELECT pid, usename, application_name, client_addr, backend_start, query_start, state, query
FROM pg_stat_activity
WHERE state <> 'idle';

~~~
- 安装和使用pg_stat_statements扩展：<br>
  确保pg_stat_statements扩展已安装，并在pgAdmin中启用它。
~~~postgresql
CREATE EXTENSION pg_stat_statements;
~~~
  然后运行查询以获取消耗最多资源的查询：
~~~postgresql
SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;
~~~
- 使用pgAdmin的性能分析工具：<br>
  在pgAdmin中，有一个名为“Server Activity”的工具，可以查看当前活动的会话、锁信息、以及等待的事件。这些信息有助于识别导致CPU使用率飙高的原因。
- 使用pgAdmin的“Query Tool”进行查询分析:<br>
  使用“Query Tool”运行具体查询，并使用EXPLAIN ANALYZE分析查询的执行计划，识别查询的性能瓶颈。
~~~postgresql
EXPLAIN ANALYZE SELECT * FROM your_table WHERE your_condition;
~~~

## 内存使用与CPU使用关系
CPU飙高确实可能与内存使用率有关系。在数据库系统中，CPU和内存使用率之间存在紧密的关系
- 缓存命中率：<br>
  高缓存命中率通常可以减少I/O操作，从而降低CPU负载。如果内存不足导致缓存命中率低，则数据库需要频繁访问磁盘，增加CPU负载。
- 内存分配和管理：<br>
  PostgreSQL会为查询分配内存用于排序、哈希表等操作。如果内存不足，这些操作可能会变得低效，导致更多的CPU使用。
- 交换（Swapping）：<br>
  如果系统内存不足，操作系统可能会将部分内存内容换出到磁盘（交换），这会显著增加CPU负载
- 并发连接:<br>
  高并发连接会消耗大量内存资源，如果内存不足，系统会频繁进行上下文切换，增加CPU使用率。
## 分析内存与CPU使用的关系
- 检查系统内存使用情况:<br>
  使用系统工具（如free、vmstat、top等）监控内存使用情况。
~~~shell
free -m
vmstat 1
top
~~~
- PostgreSQL内存配置:<br>
  检查并调整PostgreSQL的内存配置参数，如shared_buffers、work_mem、maintenance_work_mem等。
~~~postgresql
SHOW shared_buffers;
SHOW work_mem;
SHOW maintenance_work_mem;
~~~
- 监控内存相关的系统指标：<br>
  使用pgAdmin或其他监控工具，监控内存相关的系统指标，如缓存命中率、交换活动等。
- 检查查询内存使用情况：<br>
  使用EXPLAIN ANALYZE查看查询的内存使用情况，识别需要大量内存的查询。
~~~postgresql
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM your_table WHERE your_condition;
~~~
- pg_stat_activity和pg_stat_statements：使用pgAdmin的查询工具查看当前活动的查询和其资源使用情况。<br>
~~~postgresql
SELECT pid, usename, application_name, client_addr, backend_start, query_start, state, query
FROM pg_stat_activity
WHERE state <> 'idle';
~~~
## PostgreSQL官方文档性能提示
- PostgreSQL Manuals
https://www.postgresql.org/docs/14/index.html

- PostgreSQL 性能
https://www.postgresql.org/docs/14/performance-tips.html

- PostgreSQL 14.1 手册
http://www.postgres.cn/docs/14/index.html

其他：
- 阿里云RDS PostgreSQL CPU利用率高问题
https://help.aliyun.com/zh/rds/apsaradb-rds-for-postgresql/troubleshoot-high-cpu-utilization
- RDS PostgreSQL慢SQL问题
  https://help.aliyun.com/zh/rds/apsaradb-rds-for-postgresql/slow-sql-queries#concept-2038580