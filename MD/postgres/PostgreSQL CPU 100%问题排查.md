# PostgreSQL CPU 100%问题排查

## 前 言
一般来说, CPU 问题是由低效SQL引发，而造成低效SQL 的原因有：
- SQL写法不够优化
- 表的数据量发生了变化， 但统计信息未及时更新
- 数据库本身的bug

## 分析解决
为了进一步调查这个问题， 请参考如下建议：
本机 PostgreSQL 视图和目录（例如 pg_stat_statements、pg_stat_activity 和 pg_stat_user_tables）查看数据库级详细信息。
有关更多信息，请参阅有关监控数据库活动和 pg_stat_statements 的 PostgreSQL 文档。

PostgreSQL 提供各种日志记录参数来记录长时间运行的查询、autovacuum、锁定等待以及连接和断开连接请求。有关更多信息，请参阅如何使用 Amazon RDS for PostgreSQL 启用查询日志记录？


确定原因后，可以使用以下方法进一步降低 CPU 使用：

如果有机会进行调整，请使用 EXPLAIN 和 EXPLAIN ANALYZE 来识别警告。有关更多信息，请参阅有关 EXPLAIN 的 PostgreSQL 文档。

## 有用的查询
以下是一些有用的查询， 请在您的环境中运行， 看是否有慢查询等

- 1. 查看当前活跃的DB session 正在运行的SQL语句（运行时间超过10秒）
~~~
SELECT now() - query_start as "runtime", usename,application_name, client_hostname, datname,  state, query
        FROM  pg_stat_activity
        WHERE now() - query_start > '10 seconds'::interval
           and state!='idle'
       ORDER BY runtime DESC;
~~~
- 2. 按 total_time 列出查询，并查看哪个查询在数据库中花费的时间最多
~~~
SELECT round(total_time*1000)/1000 AS total_time,query
    FROM pg_stat_statements
    ORDER BY total_time DESC limit 5;
~~~
- 3. 查看哪些table未及时做vacuum，以及未及时收集统计信息
~~~
SELECT relname, n_live_tup, n_dead_tup, trunc(100*n_dead_tup/(n_live_tup+1))::float "ratio%",
    to_char(last_autovacuum, 'YYYY-MM-DD HH24:MI:SS') as autovacuum_date,
    to_char(last_autoanalyze, 'YYYY-MM-DD HH24:MI:SS') as autoanalyze_date
    FROM pg_stat_all_tables
    ORDER BY last_autovacuum;
~~~

- 4. 查看有没有被锁的session
~~~
select pid,
    usename,
    pg_blocking_pids(pid) as blocked_by,
    query as blocked_query
    from pg_stat_activity
    where cardinality(pg_blocking_pids(pid)) > 0;
~~~
- 5. 推荐创建如下session
    - pg_stat_tables
      ~~~
      CREATE OR REPLACE VIEW pg_stat_tables
          AS
          WITH s AS (
          SELECT *, cast((n_tup_ins + n_tup_upd + n_tup_del) AS numeric) AS total
                 FROM pg_stat_user_tables
          )
          SELECT s.schemaname,       s.relname,       s.relid,
                 s.seq_scan,         s.idx_scan,
                 CASE WHEN s.seq_scan + s.idx_scan = 0 THEN 'NaN'::double precision
                      ELSE round(100 * s.idx_scan/(s.seq_scan+s.idx_scan),2)  END AS idx_scan_ratio,

                 s.seq_tup_read,       s.idx_tup_fetch,

                 sio.heap_blks_read,       sio.heap_blks_hit,
                 CASE WHEN sio.heap_blks_read = 0 THEN 0.00
                      ELSE round(100*sio.heap_blks_hit/(sio.heap_blks_read+sio.heap_blks_hit),2)  END AS hit_ratio,

                 n_tup_ins,       n_tup_upd,       n_tup_del,
                 CASE WHEN s.total = 0 THEN 0.00
                      ELSE round((100*cast(s.n_tup_ins AS numeric)/s.total) ,2) END AS ins_ratio,
                 CASE WHEN s.total = 0 THEN 0.00
                      ELSE round((100*cast(s.n_tup_upd AS numeric)/s.total) ,2) END AS upd_ratio,
                 CASE WHEN s.total = 0 THEN 0.00
                      ELSE round((100*cast(s.n_tup_del AS numeric)/s.total) ,2) END AS del_ratio,

                 s.n_tup_hot_upd,
                 CASE WHEN s.n_tup_upd = 0 THEN 'NaN'::double precision
                      ELSE round(100*cast(cast(n_tup_hot_upd as numeric)/n_tup_upd as numeric), 2) END AS hot_upd_ratio,

                 pg_size_pretty(pg_relation_size(sio.relid)) AS "table_size",
                 pg_size_pretty(pg_total_relation_size(sio.relid)) AS "total_size",

                 s.last_vacuum,       s.last_autovacuum,
                 s.vacuum_count,      s.autovacuum_count,
                 s.last_analyze,      s.last_autoanalyze,
                 s.analyze_count,     s.autoanalyze_count
          FROM s, pg_statio_user_tables AS sio WHERE s.relid = sio.relid ORDER BY relname;
      ~~~

    - pg_stat_indexes
      ~~~
      AS
          SELECT s.schemaname,       s.relname,       s.indexrelname,       s.relid,
                 s.idx_scan,       s.idx_tup_read,       s.idx_tup_fetch,
                 sio.idx_blks_read,       sio.idx_blks_hit,
                 CASE WHEN sio.idx_blks_read  + sio.idx_blks_hit = 0 THEN 'NaN'::double precision
                 ELSE round(100 * sio.idx_blks_hit/(sio.idx_blks_read + sio.idx_blks_hit), 2) END AS idx_hit_ratio,
                 pg_size_pretty(pg_relation_size(s.indexrelid)) AS "index_size"
          FROM pg_stat_user_indexes AS s, pg_statio_user_indexes AS sio
          WHERE s.relid = sio.relid ORDER BY relname;
      ~~~

    - pg_stat_users
      ~~~
      CREATE OR REPLACE VIEW pg_stat_users
          AS
          SELECT datname,       usename,       pid,       backend_start, 
                 (current_timestamp - backend_start)::interval(3) AS "login_time"
          FROM pg_stat_activity;
      ~~~
    - pg_stat_queries
      ~~~
          CREATE OR REPLACE VIEW pg_stat_queries 
          AS
          SELECT datname,       usename,       pid,
                 (current_timestamp - xact_start)::interval(3) AS duration, 
                 waiting,       query
          FROM pg_stat_activity WHERE pid != pg_backend_pid();
      ~~~
    - pg_stat_long_trx
      ~~~
      CREATE OR REPLACE VIEW pg_stat_long_trx 
          AS
          SELECT pid,        waiting,
              (current_timestamp - xact_start)::interval(3) AS duration, query
          FROM pg_stat_activity
          WHERE pid <> pg_backend_pid();
      ~~~
    - pg_stat_waiting_locks
      ~~~
      CREATE OR REPLACE VIEW pg_stat_waiting_locks
          AS
          SELECT l.locktype,       c.relname,       l.pid,       l.mode,
                 substring(a.query, 1, 6) AS query,
                 (current_timestamp - xact_start)::interval(3) AS duration
          FROM pg_locks AS l
            LEFT OUTER JOIN pg_stat_activity AS a ON l.pid = a.pid
            LEFT OUTER JOIN pg_class AS c ON l.relation = c.oid 
          WHERE  NOT l.granted ORDER BY l.pid;
      ~~~

- 4. 请启用慢日志查询
     请依据文档【2】 启用如下两个参数， 以将慢查询记录到日志中， 方便进行分析：

可以修改以下参数，使日志记录的更详细，并将日志保存至 cloudwatch 中，这样将有利于您更好的发现问题。
~~~
log_checkpoints = on
log_connections = on
log_disconnections = on
log_lock_waits = on
log_temp_files = 0
log_autovacuum_min_duration = 0
log_statement = 1
log_min_duration_statement = 200
~~~

原文链接：https://blog.csdn.net/Tech_Sharing/article/details/108670599