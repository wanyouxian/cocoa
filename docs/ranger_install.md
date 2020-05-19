## Ranger-Admin下载解压编译

```
wget http://mirrors.tuna.tsinghua.edu.cn/apache/ranger/1.2.0/apache-ranger-1.2.0.tar.gz

tar xvf apache-ranger-1.2.0.tar.gz

mvn clean compile package assembly:assembly install -DskipTests -Drat.skip=true
```



## Ranger-Admin配置

```
1）数据库配置
DB_FLAVOR=MYSQL #指明使用数据库类型
SQL_CONNECTOR_JAR=/root/src/mysql-connector-java-5.1.27-bin.jar #数据库连接驱动
db_root_user=root        #数据库root用户名
db_root_password=123456 #数据库主机
db_host=192.168.8.100 #数据库密码

# 以下三个属性是用于设置ranger数据库的
db_name=ranger          #数据库名
db_user=root        #管理该数据库用户
db_password=123456    #密码

# 不需要保存，为空，否则生成的数据库密码为'_'
cred_keystore_filename=

2) 审计日志， 如果没有安装solr，对应的属性值为空即可
audit_store=db

audit_db_user=root
audit_db_name=ranger
audit_db_password=123456

3）策略管理配置，配置ip和端口，默认即可
policymgr_external_url=http://localhost:6080

4) 配置hadoop集群的core-site.xml文件，把core-site.xml文件拷贝到该目录
hadoop_conf=/etc/hadoop/conf

5) rangerAdmin、rangerTagSync、rangerUsersync、keyadmin密码配置。默认为空，可以不配，对应的内部组件该属性也要为空
rangerAdmin_password=cocoa@123
rangerTagsync_password=
rangerUsersync_password=
keyadmin_password=
```



## Ranger-Admin安装启动验证



