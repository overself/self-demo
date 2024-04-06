#配置ENV环境
NUNBUFFERED=0
PYTHONUNBUFFERED=0
REDASH_LOG_LEVEL=INFO
#REDASH_REDIS_URL=redis://Ncmxbcx5@192.168.6.146:16379
REDASH_REDIS_URL=redis://default:Ncmxbcx5@172.16.232.157:16379
REDASH_DATABASE_URL=postgresql://redash:redash@172.16.232.157:15432/pg_redash_db
REDASH_SECRET_KEY=D9cZYUlcXQ568RUog05S0QNegLga1Oo7
REDASH_COOKIE_SECRET=t3sBcBWsR0dOczmCuzQDY0Zhk2QFhg69
REDASH_BACKEND=http://127.0.0.1:5000



#安装Redash
docker compose -f docker-compose.yml up -d

#初始化数据库
docker-compose -f docker-compose.yml run --rm server create_db



用户：
admin@163.com/12345678