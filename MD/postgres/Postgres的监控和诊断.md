# Postgres的监控和诊断

参考文档： PostgreSQL 9 Administration Cookbook （第二版）中文版 第八章 监控和诊断

~~~
select * from pg_stat_user_tables --和pg_stat_all_tables一样，但只显示用户表,当前数据库中每个表一行，显示有关访问指定表的统计信息
select * from pg_stat_user_indexes --和pg_stat_all_indexes一样，但只显示系统表上的索引.
~~~

-- 可以使用pgadmin快速查看数据库的当前状态，需要安装adminpack扩展
~~~
create extension adminpack;
~~~

--检查postgres用户是否已经连接上
~~~
select * from pg_stat_activity where usename='postgres';  -- 返回值，表示已经连接
pg_stat_activity，每个服务器进程一行，显示与那个进程的当前活动相关的信息，例如状态和当前查询
select datname,usename,client_addr,client_port,application_name from pg_stat_activity;  -- 查看连接信息，比如用户、ip、端口等等
~~~

-- 在psql中，反复执行某一个查询语句，比如，先查询时间，然后每隔4秒执行一次该查询语句
~~~
mydb=# select now();
              now              
-------------------------------
 2021-02-03 09:28:55.455629+08
(1 row)
 
mydb=# \watch 4
Wed 03 Feb 2021 09:29:00 AM CST (every 4s)
 
              now              
-------------------------------
 2021-02-03 09:29:00.447043+08
(1 row)
 
Wed 03 Feb 2021 09:29:04 AM CST (every 4s)
 
              now              
-------------------------------
 2021-02-03 09:29:04.452628+08
(1 row)
 
Wed 03 Feb 2021 09:29:08 AM CST (every 4s)
 
             now              
------------------------------
 2021-02-03 09:29:08.45936+08
(1 row)
 
Wed 03 Feb 2021 09:29:12 AM CST (every 4s)
 
             now              
------------------------------
 2021-02-03 09:29:12.46499+08
(1 row)
~~~

--检查那个查询在运行（设置了track_activities=on后，pg会自动收集所有当前运行的查询）
~~~
select datname,usename,state,query from pg_stat_activity; --检查已连接的用户正在运行什么 
select datname,usename,state,query from pg_stat_activity where state='active'; --检查已连接的用户正在运行什么,只查询活跃的用户
~~~

-- 捕获运行时间数毫秒的查询（OLTP查询，一般都很短，在pg中用视图，可能会查询不到）
-- 获得执行时间最长的查询
~~~
select current_timestamp - query_start as runtime,datname,usename,query from pg_stat_activity where state='active' order by 1 desc;
select current_timestamp - query_start as runtime,datname,usename,query
from pg_stat_activity where state='active' and current_timestamp - query_start>'1 min' order by 1 desc;   -- 获取运行时间超过1分钟的查询
也可以使用ps命令查询，但是不一定每次都能查询的到。需要设置参数update_process_title = on（默认已设置）
~~~

-- 检查那个查询正在运行或被阻塞
~~~
SELECT datname,usename,current_query FROM pg_stat_activity WHERE waiting = true;  -- 10.0中没有这个字段 
mydb=# \timing on
Timing is on.
mydb=# select pg_sleep(10);  -- 可以使用pg_sleep来制造一个查询，然后捕获该查询，该语句意思是等待10秒再执行
 pg_sleep 
----------
 
(1 row)
 
Time: 10008.825 ms (00:10.009)
mydb=# 
~~~

-- 确定谁阻塞了一个查询 （因为where条件，没有执行成功）
~~~
SELECT
w.query as waiting_query,
w.pid as w_pid,
w.usename as w_user,
l.query as locking_query,
l.pid as l_pid,
l.usename as l_user,
t.schemaname || '.' || t.relname as tablename
from pg_stat_activity w
join pg_locks l1 on w.pid = l1.pid and not l1.granted
join pg_locks l2 on l1.relation = l2.relation and l2.granted
join pg_stat_activity l on l2.pid = l.pid
join pg_stat_user_tables t on l1.relation = t.relid
where w.waiting; 
~~~

--kill掉指定的会话，可以通过pg_stat_activity查询到pid，再根据pg_terminate_backend(pid)来kill 。
~~~
select pg_terminate_backend(pid) from pg_stat_activity;  -- 会断开当前的链接 
select pg_terminate_backend(21044);   -- kill掉会话21044 ，这个是直接kill掉会话了
select pg_cancel_backend();   -- 取消当前查询 。这个只是取消查询，不kill掉会话
 
select pg_sleep(100);  -- 先进行一个查询，100秒后再查询
select * from pg_stat_activity; select pid from pg_stat_activity where query like 'select pg%';  -- 查询到具体的pid
select pg_cancel_backend(36465);  -- 取消查询
 
mydb=# select pg_sleep(100);  
ERROR:  canceling statement due to user request    --取消的结果 
Time: 46710.351 ms (00:46.710)
mydb=# 
~~~

-- 使用语句超时设置来清除超长时间运行的查询，statement_timeout，默认是0，是不设置的。设置为10s，做个测试
~~~
mydb=# show statement_timeout;
 statement_timeout 
-------------------
 0
(1 row)
 
mydb=# 
 
mydb=# set statement_timeout to '10 s';  -- 在当前会话中设置参数为10秒，超过10秒取消查询
SET
mydb=# \timing on
Timing is on.
mydb=# select pg_sleep(100);   -- 进行一个查询，100秒，10秒后取消查询
ERROR:  canceling statement due to statement timeout
Time: 10002.691 ms (00:10.003)
mydb=# 
~~~

--kill掉"在事务中空闲（idle in transaction）的查询"
~~~
select pg_terminate_backend(pid)
from pg_stat_activity
where current_query = 'idle in transaction'  --  idle 
and current_timestamp - query_start > '10 min';
~~~

-- 确定是否某人在使用某张表（原理是，查询所有与查询数据相关的使用计数器发生变化的表）
~~~
create temp table tmp_stat_user_tables as select * from pg_stat_user_tables;
 
select * from pg_stat_user_tables n
join tmp_stat_user_tables t
on n.relid=t.relid
and (n.seq_scan,n.idx_scan,n.n_tup_ins,n.n_tup_upd,n.n_tup_del) <>
(t.seq_scan,t.idx_scan,t.n_tup_ins,t.n_tup_upd,t.n_tup_del);
 
 
select pg_stat_reset(); -- 或重置所有表的统计信息。重置后为0，可以通过查询使用次数不为0的记录来检测表的使用情况。
~~~

--确定一张表的最后被使用的时间

该方法使用文件系统作为信息源，获取PG修改和读取文件的时间来初步判断。该方法不是太可靠，因为autovacuum可能也会访问表

~~~
create or replace function table_file_access_info (
in schemaname text,in tablename text,
out last_access timestamp with time zone,
out last_change timestamp with time zone ) language plpgsql as $func$
declare 
tabledir text;
filenode text;
begin 
select regexp_replace(
current_setting('data_directory') || '/' || pg_relation_filepath(c.oid),
pg_relation_filenode(c.oid) || '$',''),
pg_relation_filenode(c.oid)
into tabledir,filenode
from pg_class c
join pg_namespace ns 
on c.relnamespace = ns.oid 
and c.relname = tablename 
and ns.nspname = schemaname ;
raise notice 'tabledir :% -filenode :%',tabledir,filenode;
-- find lastest access and modification times over all segments 
select max((pg_stat_file(tabledir || filename)).access),
max((pg_stat_file(tabledir || filename)).modification)
into last_access,last_change from pg_ls_dir(tabledir) as filename 
-- only use files matching <basefilename>[.segmentnumber]
where filename ~('^' || filenode || '([.]?[0-9]+)?$');
end;
$func$;
 
select table_file_access_info('public','eater')
mydb=# select table_file_access_info('public','eater');
NOTICE:  tabledir :/postgres/10/data/base/16393/ -filenode :17063
               table_file_access_info                
-----------------------------------------------------
 ("2021-02-03 12:44:57+08","2021-01-26 16:31:10+08")
(1 row)
 
Time: 4.889 ms
mydb=# 
~~~

-- 临时数据使用的磁盘空间
检查数据库是否使用了自定义的表以存放临时文件，空则表示没有使用临时表空间，而临时对象则被放置到每个数据库的默认表空间中
~~~
select current_setting('temp_tablespaces');
~~~

-- 如果存在临时表，则使用以下的语句查看临时表的大小
~~~
with temporary_tablespaces as (
select unnest(string_to_array(
current_setting('temp_tablespaces'),',')
) as temp_tablespace 
)
select tt.temp_tablespace,pg_tablespace_location(t.oid) as location,
-- t.spclocation as location , -- for 9.0 and 9.1 
pg_tablespace_size(t.oid) as size     -- 可以通过pg_tablespace_size(oid) ,pg_tablespace(name) 获取到表空间的大小
from temporary_tablespaces tt 
join pg_tablespace t on t.spcname = tt.temp_tablespace 
order by 1 ;
~~~

-- 如果temp_tablespace设置为空，则临时表放在和普通表相同的位置 ，通常存放在主数据库目录的pgsql_tmp目录下
-- 查询实例的主目录的临时目录
~~~
mydb=# select current_setting('data_directory') || '/base/pgsql_tmp';
             ?column?             
----------------------------------
 /postgres/10/data/base/pgsql_tmp
(1 row)
 
mydb=# 
~~~

-- 数据库使用的临时文件的总量，可以在pg_stat_database视图中查看到
~~~
select datname,temp_files,temp_bytes,stats_reset from pg_stat_database;
 
select sum(pg_total_relation_size(relid))  -- 旧版本的书上的查询方法 
from pg_stat_all_tables
where schemaname like 'pg_%temp%';
 
mydb=# select sum(pg_total_relation_size(relid))
mydb-# from pg_stat_all_tables
mydb-# where schemaname like 'pg_%temp%';
  sum  
-------
 16384
(1 row)
 
mydb=# 
~~~

-- 查看表或者索引是否有膨胀（膨胀的原因是，大量的增删改，PG的运行机制，并没有彻底删除掉这些数据）
~~~
select pg_relation_size(relid) as tablesize,schemaname,relname,n_live_tup    -- n_live_tup活着的行的估计数量，还记录增删改的行数
from pg_stat_user_tables
where relname = <tablename>;
~~~

