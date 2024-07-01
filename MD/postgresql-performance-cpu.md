# PostgreSQL对CPU影响的命令

在PostgreSQL中，有些SQL命令和函数由于其复杂性或资源密集性，往往对CPU影响较大

## 影响较大的SQL命令
- JOIN操作：特别是复杂的多表JOIN或涉及大表的JOIN，可能需要大量计算资源来匹配和合并记录。
~~~postgresql
SELECT * FROM table1 t1
JOIN table2 t2 ON t1.id = t2.id;
~~~
- 聚合函数（如SUM、AVG、COUNT、MAX、MIN等）：这些函数需要扫描大量数据，尤其是在没有索引的情况下，会消耗大量CPU资源。
~~~postgresql
SELECT COUNT(*) FROM large_table;
SELECT AVG(column) FROM large_table;
~~~
- 排序（ORDER BY）：对大量数据进行排序需要大量的计算资源。
- GROUP BY：需要对数据进行分组并进行计算，尤其是涉及大量数据时，CPU消耗较大。
- DISTINCT：去重操作需要对数据进行比较和排序，消耗大量CPU资源。
- 复杂子查询：特别是嵌套深度较大的子查询，可能会导致CPU使用率飙高。
~~~postgresql
SELECT * FROM table1 WHERE column1 IN (SELECT column2 FROM table2);
~~~
- 窗口函数（Window Functions）：这些函数通常涉及排序和分组操作
~~~postgresql
SELECT column, ROW_NUMBER() OVER (PARTITION BY column ORDER BY another_column) FROM table;
~~~
- 全文搜索：使用tsvector和tsquery进行全文搜索
~~~postgresql
SELECT * FROM table WHERE to_tsvector('english', column) @@ to_tsquery('english', 'search_query');
~~~

## 影响较大的SQL函数
- 正则表达式函数：例如regexp_matches、regexp_replace等函数需要进行复杂的字符串匹配和替换操作
~~~postgresql
SELECT regexp_matches(column, 'pattern') FROM table;
~~~
- 数学函数和复杂计算：
~~~postgresql
SELECT power(column, 2) FROM table;
~~~
- 自定义函数：特别是使用PL/pgSQL、PL/Python等编写的复杂自定义函数
~~~postgresql
CREATE OR REPLACE FUNCTION custom_function() RETURNS VOID AS $$
BEGIN
    -- 复杂计算逻辑
END;
$$ LANGUAGE plpgsql;
~~~

## 优化建议
- 创建适当的索引：为经常使用的查询创建索引，减少全表扫描
~~~postgresql
CREATE INDEX idx_column ON table(column);
~~~

- 使用EXPLAIN分析查询计划：使用EXPLAIN和EXPLAIN ANALYZE查看查询的执行计划，识别性能瓶颈
~~~postgresql
EXPLAIN ANALYZE SELECT * FROM table WHERE column = 'value';
~~~
- 优化查询和数据库设计：简化复杂查询，避免不必要的嵌套查询和子查询。正常化数据库设计，减少冗余数据。
- 分区表：对大表进行分区，减少每次查询扫描的数据量。
~~~postgresql
CREATE TABLE partitioned_table (column INTEGER) PARTITION BY RANGE (column);
~~~
- 定期维护：执行定期的VACUUM和ANALYZE操作，保持表和索引的性能
~~~postgresql
VACUUM ANALYZE;
~~~

## SQL语句或条件可能导致索引失效
在PostgreSQL中，某些SQL语句或条件可能导致索引失效，即查询优化器不会使用可用的索引。这会导致性能下降，因为数据库会进行全表扫描而不是利用索引
- 函数和表达式
~~~postgresql
-- 在索引列上使用函数或表达式：
SELECT * FROM table WHERE LOWER(column) = 'value'; -- 索引可能失效
-- 在这种情况下，索引无法使用。解决方案是创建表达式索引：
CREATE INDEX idx_lower_column ON table (LOWER(column));
~~~
- 类型转换
~~~postgresql
-- 隐式或显式类型转换,确保查询中的类型与索引列的类型一致。
SELECT * FROM table WHERE column::text = 'value'; -- 索引可能失效
~~~
- 使用不等于操作符
~~~postgresql
-- 使用!=或<>：不等于操作符通常会导致索引失效，因为数据库无法通过索引快速排除结果。
SELECT * FROM table WHERE column != 'value'; -- 索引可能失效
~~~
- 使用IS NULL或IS NOT NULL
~~~postgresql
SELECT * FROM table WHERE column IS NULL; -- 索引可能失效
SELECT * FROM table WHERE column IS NOT NULL; -- 索引可能失效
-- 可以通过创建部分索引来优化这类查询：
CREATE INDEX idx_column_not_null ON table (column) WHERE column IS NOT NULL;
~~~
- LIKE操作
~~~postgresql
-- 前缀通配符：
SELECT * FROM table WHERE column LIKE '%value'; -- 索引可能失效
-- 使用前缀通配符时，索引无法使用。可以使用后缀通配符优化查询：
SELECT * FROM table WHERE column LIKE 'value%'; -- 索引生效
~~~
- 布尔运算
~~~postgresql
-- 在布尔列上使用非索引值
SELECT * FROM table WHERE NOT (column = 'value'); -- 索引可能失效
-- 重写查询以使用索引
SELECT * FROM table WHERE column != 'value'; -- 更可能使用索引
~~~
- 范围条件
~~~postgresql
-- 使用复杂的范围条件
SELECT * FROM table WHERE column > 'value1' AND column < 'value2'; -- 索引可能失效
~~~
- 联合查询: 使用UNION而不是UNION ALL
~~~postgresql
SELECT * FROM table1 WHERE column = 'value'
UNION
SELECT * FROM table2 WHERE column = 'value'; -- 索引可能失效
-- UNION会去重，可能导致索引失效。UNION ALL不会去重，更可能使用索引
~~~
- 查询优化器设置 : 禁用索引扫描<br>
  查询优化器设置可能导致索引未使用。可以通过SET enable_seqscan = OFF;强制使用索引，但不建议长期使用。
