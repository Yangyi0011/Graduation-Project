# 一、前提

## 1、未优化信息

通过测试报告可知，未优化的接口在并发访问时的数据如下：

1. 接口URL平均响应时间：465811毫秒。
2. SQL平均查询时间：463543.5 毫秒。
3. 系统GC次数：113 次，GC总耗时 1.32 秒。

## 2、优化信息

依据优化报告，我对项目已做了以下优化：

1. 服务器优化

   使用更擅长并发的 undertow 容器。

2. 数据库优化

   1. 表结构优化

      拆分原有的新闻信息表，将新闻内容分离出来单独成一张表。

   2. 字段优化

      将UUID的主键改为自增id主键，其他字段使用了能存下内容的更小类型。

   3. 索引优化

      给经常查询到的字段和可能经常要用到的字段添加了B+树索引。

   4. SQL语句优化

      通过自增id的性质来替换原有的按时间逆序查询，效果保持一致。

3. JVM优化

   优化了JVM种的内存分配，并改用G1垃圾收集器。

4. 加入缓存

   使用Ehcache作为本地缓存。

5. 业务代码调整和优化

   为了符合优化逻辑，调整了部分接口业务代码，并清除了一些冗余。

## 3、服务器信息

![](/images/优化后测试/服务器配置信息.png)

- 服务器：阿里云ECS云服务器
- 运行系统：CentOS 7.3 64位
- CPU：1 vCPU
- 内存：2GB(IO优化)
- 带宽：1MBbps

## 4、被测试项目信息

### 1、项目名：

​	graduation-project-mvc-optimize

### 2、项目介绍：

- 用途：

  ​	**MVC+缓存架构**的内容管理系统项目，采用前后端分离技术，利用Ehcache进行本地缓存，可通过后台管理系统的操作对前台显示的相关信息进行管理（增删改查），提供权限动态分配、用户管理、内容模糊查询等功能。

- 特点：**权限动态分配、前后端分离、本地缓存**。

- 说明：**MVC+权限认证项目，Ehcache本地缓存，主键索引、常用字段普通索引**。

- 相关技术及工具：

  - 技术：
    - 主体架构：SpringBoot、SpringMvc、Mybatis
    - 权限控制：SpringSecurity
    - 数据库：Docker-MySql
    - 前端：JavaScript、JQuery、BootStrop、Layui、Html/CSS等。
    - 前后端交互：Ajax、Json
    - 缓存：Ehcahe
    - 监控：SpringBootAdmin、Alibaba-Druid、阿里云ECS监控
  - 工具：
    - 结构控制：Maven
    - 版本控制：Git/Github
    - 设计与画图：processon、PowerDesigner、Navicat。

## 5、项目结构

![](/images/优化后测试/毕设-单体MVC优化版.png)

## 6、主要模块的数据库设计

- 用户与权限模块

  ![用户-权限模块](/images/优化后测试/用户-权限模块.png)

- 留言模块

  ![用户留言模块](/images/优化后测试/用户留言模块.png)

- 内容发布模块

  ![](/images/优化后测试/内容发布模块.png)

# 二、测试前信息

### 1、待测试接口

- 新闻列表：http://120.78.59.162:8082/newsInfo/list?currentPage=1&rows=10&condition=%7B%22searchContent%22%3A%22%22%7D
  - 说明：查询首页数据，从第 1 条查到第 10 条，condition后面表示模糊查询条件，默认为 null。

### 2、待测试接口所涉及到的数据库表结构

- 新闻信息表

  ![](/images/优化后测试/新闻信息表.png)

- 新闻内容表

  ![](/images/优化后测试/新闻内容表.png)

### 3、待测试接口所涉及的SQL语句

- 新闻列表（http://120.78.59.162:8082/newsInfo/list?currentPage=1&rows=10&condition=%7B%22searchContent%22%3A%22%22%7D）：

  - 分页查新闻信息

  ```mysql
  SELECT 
  id,title,imgs,news_content_id,remarks,create_user_id,create_date,update_user_id,update_date
  FROM tab_news_info
  ORDER BY id desc 
  LIMIT 0,10
  ```

### 4、测试接口的对应的表的数据量

​	![](/images/优化后测试/新闻信息表-数据量.png)

![](/images/优化后测试/新闻内容表-数据量.png)

- 查询可知，新闻列表的单表数据量为 **1601** 条。

### 5、环境配置信息

- 德鲁伊（Druid）数据源配置信息

  ```yaml
  spring: 
    #数据源配置
    datasource:
      username: root
      password: 你的密码
      url: jdbc:mysql://120.78.59.162:3308/graduation?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=GMT%2B8&allowMultiQueries=true
      type: com.alibaba.druid.pool.DruidDataSource
      #driver-class-name:springBoot2.x 后驱动程序通过SPI自动注册，并且通常不需要手动加载驱动程序类。
      druid:
        # 下面为连接池的补充设置，应用到上面所有数据源中
        initial-size: 5   #线程池初大小
        min-idle: 5       #线程池最小空闲连接
        max-active: 2000  #线程池最大连接数
        # 配置获取连接等待超时的时间
        max-wait: 60000
       # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        time-between-eviction-runs-millis: 60000
        # 配置一个连接在池中最小生存的时间，单位是毫秒
        min-evictable-idle-time-millis: 300000
        validation-query: SELECT 1 FROM DUAL
        test-while-idle: true
        test-on-borrow: false
        test-on-return: false
        # 打开PSCache，并且指定每个连接上PSCache的大小
        pool-prepared-statements: true
        #   配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
        max-pool-prepared-statement-per-connection-size: 20
        # SpringBoot 2.x 此处记得去掉 log4j
        filters: stat,wall
        use-global-data-source-stat: true
        # 通过connectProperties属性来打开mergeSql功能；慢SQL记录
        connect-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
        # 配置监控服务器
        stat-view-servlet:
          login-username: admin
          login-password: 你的密码
          reset-enable: false
          url-pattern: /druid/*
          # 添加IP白名单
          #allow:
          # 添加IP黑名单，当白名单和黑名单重复时，黑名单优先级更高
          #deny:
        web-stat-filter:
          # 添加过滤规则
          url-pattern: /*
          # 忽略过滤格式，一定要加上 ""，否则会报错
          exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
  
    cache:
        type: ehcache   #使用 ehcache
        ehcache:
            config: classpath:ehcache.xml
  
  ```

- JVM配置信息

  ```sh
  nohup java -jar -Xms768m -Xmx768m -Xmn256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -Xss256k -XX:+UseG1GC graduation-project-mvc-optimize-1.0-SNAPSHOT.jar >graduation-project-mvc-optimize.log &
  
  #其他使用默认配置
  ```

- Ehcache配置

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:noNamespaceSchemaLocation="../config/ehcache.xsd">
   <!-- 磁盘保存路径 -->
   <diskStore path="/opt/apps/ehcache/optimize/" />
   <defaultCache
     maxElementsInMemory="10000" 
     maxElementsOnDisk="10000000"
     eternal="false" 
     overflowToDisk="true" 
     timeToIdleSeconds="120"
     timeToLiveSeconds="120" 
     diskExpiryThreadIntervalSeconds="120"
     diskPersistent="true"
     memoryStoreEvictionPolicy="LRU"
   >
   </defaultCache>
  </ehcache>
   
  ```

# 三、并发测试

## 1、测试工具

Apche-JMeter(5.1.1 r1855137)

## 2、获取平均响应时间

### 1、线程组配置

![](/images/优化后测试/获取平均响应时间/线程组配置.png)

说明：

​	1、线程数：5 个

​	2、总启动时间：10 s

​	3、由于优化后响应时间大幅缩短，为了保证数据的真实性，根据测试理论，我需要增大循环次数

​		为了获取准确的平均响应时间，我将循环次数设为：**1000 次**

### 2、HTTP配置

![](/images/优化后测试/获取平均响应时间/HTTP配置.png)

说明：

​	1、请求协议：HTTP

​	2、请求服务器IP：120.78.59.162

​	3、请求端口号：8082

​	4、请求方法：GET

​	5、请求URL：/newsInfo/list?currentPage=1&rows=10&condition=%7B%22searchContent%22%3A%22%22%7D

​	6、响应内容编码：UTF-8

### 3、超时配置

![](/images/优化后测试/获取平均响应时间/超时配置.png)

说明：

​	获取平均响应时间不配置超时时间，使用 JMeter 默认配置的120秒。



### 4、聚合结果

- 聚合报告：

![](/images/优化后测试/获取平均响应时间/聚合报告.png)

​	说明：

​		平均响应时间：166 ms，即 0.166 s

​		吞吐量：27.7/s



## 3、JMeter配置

### 1、测试理论

根据[JMeter之Ramp-up Period（in seconds）说明](https://www.cnblogs.com/hjhsysu/p/9189897.html)可得以下参数：

T：线程组启动时间

n：线程组的线程数

S：最后一个线程的启动时刻

a：理论上达到并发访问效果所需的循环次数

t：测试接口的平均响应时间

R：单个线程运行时间



由 聚合报告 可知，t=0.166 s，因此处n=10，则设T=10，则有：

​	S = (T- T/n) = (10-10/10) = 9

​	a*t > S => a > S/t，即  a > 9/0.166 ≈ 55

​	即循环次数至少为 55 次才能保证这10个线程并发访问测试接口，为了方便观测，我就将循环次数设为1000次，由R = a*t=166，得：

​	**并发时间段为：第9秒~第166秒。**



### 2、线程组配置

![](/images/优化后测试/线程组配置.png)

说明：

​	1、线程数：10个

​	2、启动时间：10秒

​	3、循环次数：1000 次



### 3、HTTP配置

- URL配置

  ![](/images/优化后测试/HTTP配置.png)

- URL数据

  ![](/images/优化后测试/测试数据.png)

- 超时配置

  ![](/images/优化后测试/超时配置.png)

  说明：

  ​	由于测试接口的响应速度已经够快了，为了防止并发测试下网络拥塞导致 JMeter 卡死，这里设置了超时时间：

  ​	请求超时：60秒

  ​	响应超时：60秒

  

## 4、测试前信息

### 1、德鲁伊监控

- URL信息

![](/images/优化后测试/测试前/URL信息.png)

说明：

1. 请求URL：/newsInfo/list
2. 请求次数：5010（以往的数据）
3. 请求时间和：89158 ms
4. 最慢一次请求耗时：438 ms
5. 执行中的线程：0
6. 最大并发数：5
7. jdbc总次数：15
8. jdbc总耗时：291 ms



- SQL信息

![](/images/优化后测试/测试前/SQL信息.png)

1. 查询sql：

   ```mysql
   SELECT 
   id,title,imgs,news_content_id,remarks,create_user_id,create_date,update_user_id,update_date
   FROM tab_news_info
   ORDER BY id desc 
   LIMIT 0,10
   ```

2. 执行数：13（以往数据）

3. 总执行时间：210 ms

4. 最慢一次查询耗时：76 ms

5. 执行中的线程：0

6. 最大并发数：2【因为有缓存】



### 2、BootAdmin监控

- 堆内存信息

  ![](/images/优化后测试/测试前/堆内存信息.png)

  - Java堆
    1. 当前堆内存：805 MB
    2. 当前堆内存使用：256 MB
    3. 最大堆内存：805 MB
    4. 堆内存使用线：锯齿型，跨度很大，变化密集处是获取平均时间时的测试。
  - 非堆
    1. 元空间（METASPACE）内存：71.4 MB
    2. 当前非堆内存：121 MB
    3. 当前非堆内存使用：116 MB
    4. 最大非堆内存：存1.46 GB
    5. 堆内存使用线：在16：28~16:29稍微上升，上升阶段是获取平均响应时间的测试，其他时间段基本是直线型。

- GC信息

  ![](/images/优化后测试/测试前/GC信息.png)

  说明：

  1. GC总次数：27
  2. GC总耗时：1.3450 s
  3. 最慢一次GC耗时：0.0090 s 

  

  结合GC日志：

  ![](/images/优化后测试/测试前/GC日志.png)

  可知道，从项目运行到日志采集为止：

  - 年轻代的GC次数（YGC）：31 次
  - 年轻代中GC总耗时（YGCT）：1.470 秒
  - 全GC的次数（FGC）：0次
  - 全GC总耗时（FGCT）：0秒
  - 所有GC的总耗时（GCT）：1.470 秒

  

- 运行线程信息

  ![](/images/优化后测试/测试前/线程信息.png)

  ​	说明：

  ​		1、当前运行线程：29

  ​		2、守护线程：7

  ​		3、顶峰运行线程：31

  

- CPU信息

  ![](/images/优化后测试/测试前/CPU信息.png)

  说明：

  ​		1、进程ID：12726

  ​		2、CPU使用率：8%

### 3、ECS服务器监控

​	![](/images/优化后测试/测试前/ECS信息.png)

说明：

​	1、CPU使用率：25%以下。

​	2、网络带宽（入）：200 kb/s以下

​	3、网络带宽（出）：500 kb/s以下



## 5、测试时信息

### 1、时间

- 并发时间段：**第9秒~第166秒**
- 开始时间：2019年4月26日16:35:21
- 记录时间：2019年4月26日16:37:14
- 结束时间：2019年4月26日16:42:07
- 总测试时间：6分46秒

### 2、德鲁伊监控

- URL信息

![](/images/优化后测试/测试时/URL信息.png)

说明：

1. 请求URL：/newsInfo/list
2. 请求次数：7627（包含以往的数据）
3. 请求时间和：122768 ms （包含以往的数据）
4. 最慢一次请求耗时：438 ms
5. 执行中的线程：0
6. 最大并发数：8
7. jdbc总次数：21
8. jdbc总耗时：458 ms



- SQL信息

![](/images/优化后测试/测试时/SQL信息.png)

1. 查询sql：

   ```mysql
   SELECT 
   id,title,imgs,news_content_id,remarks,create_user_id,create_date,update_user_id,update_date
   FROM tab_news_info
   ORDER BY id desc 
   LIMIT 0,10
   ```

2. 执行数：24（包含以往数据）

3. 总执行时间：1370 ms（包含以往数据）

4. 最慢一次查询耗时：722 ms

5. 执行中的线程：0

6. 最大并发数：2 【因为有缓存】



### 3、BootAdmin监控

- 堆内存信息

  ![](/images/优化后测试/测试时/堆内存信息.png)

  - Java堆
    1. 当前堆内存：805 MB
    2. 当前堆内存使用：191 MB
    3. 最大堆内存：805 MB
    4. 堆内存使用线：其他时间段平稳变化，且跨度大，测试时间段内变化密集。
  - 非堆
    1. 元空间（METASPACE）内存：71.6 MB
    2. 当前非堆内存：122 MB
    3. 当前非堆内存使用：117 MB
    4. 最大非堆内存：存1.46 GB
    5. 堆内存使用线：测试期间基本是直线型

- GC信息

  ![](/images/优化后测试/测试时/GC信息.png)

  说明：

  1. GC总次数：45
  2. GC总耗时：1.5110 s
  3. 最慢一次GC耗时：0.0190s

  

- 运行线程信息

  ![](/images/优化后测试/测试时/线程信息.png)

  ​	说明：

  ​		1、当前运行线程：30

  ​		2、守护线程：8

  ​		3、顶峰运行线程：31

  

- CPU信息

  ![](/images/优化后测试/测试时/CPU信息.png)

  说明：

  ​		1、进程ID：12726

  ​		2、CPU使用率：46%

### 4、ECS服务器监控

​	![](/images/优化后测试/测试时/ECS信息.png)

说明：

​	1、CPU使用率：25%以下。

​	2、网络带宽（入）：200 kb/s以下

​	3、网络带宽（出）：500 kb/s以下，呈上升趋势



## 6、测试后信息

### 1、德鲁伊监控

- URL信息

![](/images/优化后测试/测试后/URL信息.png)

说明：

1. 请求URL：/newsInfo/list
2. 请求次数：15010（包含以往的数据）
3. 请求时间和：204327 ms
4. 最慢一次请求耗时：895 ms
5. 执行中的线程：0
6. 最大并发数：8
7. jdbc总次数：44
8. jdbc总耗时：3408 ms



- SQL信息

![](/images/优化后测试/测试后/SQL信息.png)

1. 查询sql：

   ```mysql
   SELECT 
   id,title,imgs,news_content_id,remarks,create_user_id,create_date,update_user_id,update_date
   FROM tab_news_info
   ORDER BY id desc 
   LIMIT 0,10
   ```

2. 执行数：37（包含以往数据）

3. 总执行时间：3303 ms

4. 最慢一次查询耗时：886 ms

5. 执行中的线程：0

6. 最大并发数：2 【因为有缓存】



### 2、BootAdmin监控

- 堆内存信息

  ![](/images/优化后测试/测试后/堆内存信息.png)

  - Java堆
    1. 当前堆内存：805 MB
    2. 当前堆内存使用：87.4 MB
    3. 最大堆内存：805 MB
    4. 堆内存使用线：测试时，图形变化密集，其他时间段平稳变化，且跨度大
  - 非堆
    1. 元空间（METASPACE）内存：71.8 MB
    2. 当前非堆内存：122 MB
    3. 当前非堆内存使用：118 MB
    4. 最大非堆内存：存1.46 GB
    5. 堆内存使用线：除去获取平均响应时间的测试时间段内有所上升外，剩下的基本是直线型。

- GC信息

  ![](/images/优化后测试/测试后/GC信息.png)

  说明：

  1. GC总次数：69
  2. GC总耗时：1.6870 s
  3. 最慢一次GC耗时：0.0090s

  

  结合GC日志：

  ![](/images/优化后测试/测试后/GC日志1.png)

  ![](/images/优化后测试/测试后/GC日志2.png)

  可知道，从项目运行到日志采集为止：

  - 年轻代的GC次数（YGC）：72次
  - 年轻代中GC总耗时（YGCT）：1.804 秒
  - 全GC的次数（FGC）：0次
  - 全GC总耗时（FGCT）：0秒
  - 所有GC的总耗时（GCT）：1.804 秒

  

- 运行线程信息

  ![](/images/优化后测试/测试后/线程信息.png)

  ​	说明：

  ​		1、当前运行线程：31

  ​		2、守护线程：8

  ​		3、顶峰运行线程：31

- CPU信息

  ![](/images/优化后测试/测试后/CPU信息.png)

  说明：

  ​		1、进程ID：12726

  ​		2、CPU使用率：6%

### 3、ECS服务器监控

​	![](/images/优化后测试/测试后/ECS信息.png)

说明：

​	1、CPU使用率：测试期间在50%左右。

​	2、网络带宽（入）：测试期间在200 kb/s左右

​	3、网络带宽（出）：测试期间，已达极限的 1MB/S

### 4、聚合报告

- 聚合报告

  ![](/images/优化后测试/测试后/聚合报告.png)

  说明：

  ​	1、平均响应时间：367 毫秒

  ​	2、吞吐量：24.6次/秒

  ​	3、请求异常：0


# 四、测试数据汇总分析

## 1、测试时间分析

分析：

- 开始时间：2019年4月26日16:35:21
- 测试时记录时间：2019年4月26日16:37:14
- 结束时间：2019年4月26日16:42:07
- 测试时记录开始时间：第113 秒，符合测试理论中第9秒到第166秒之间为所有线程并发访问的时间段。
- 总测试时间（结束时间-开始时间）：6分46秒 = 406秒 = 406000 毫秒

**小结：手动记录的时间虽然有些误差，但数据记录基本都在理论的并发时间段内，10个线程的 1000次循环并发访问耗时 7分钟左右，响应速度非常不错！**

## 2、URL数据分析

|     数据/测试时间段      |       测试前       |         测试时         |         测试后          |
| :----------------------: | :----------------: | :--------------------: | :---------------------: |
|           URL            |   /newsInfo/list   |     /newsInfo/list     |     /newsInfo/list      |
|      请求次数（次）      | 5010（以往的数据） | 7627（包含以往的数据） | 15010（包含以往的数据） |
|     请求时间和（ms）     |       89158        |         122768         |         204327          |
| 最慢的一次请求耗时（ms） |        438         |          438           |           895           |
|    执行中的线程（个）    |         0          |           0            |            0            |
|     最大并发数（个）     |         5          |           8            |            8            |
|      jdbc次数（次）      |         15         |           21           |           44            |
|     jdbc总耗时（ms）     |        291         |          458           |          3408           |

分析：

- 整个测试过程的URL请求次数（测试后-测试前）：10000 次

  ​		成功请求率：100%。

- 请求时间和（测试后-测试前）：115169 毫秒

- 平均请求时间（请求时间总和/请求次数和）：11.5169 毫秒

- 测试过程中最慢的一次URL请求耗时：895 毫秒

- 测试过程中观测到的线程执行数：0

- 测试过程中的最大线程并发数：8

- 测试过程中JDBC总次数（测试后-测试前）：29

- 测试过程中JDBC总耗时（测试后-测试前）：3117 毫秒

- 测试过程中平均jdbc耗时（总耗时/总次数）：107.48 毫秒

- 测试请求失败率：0%

**小结：**

- **优化后URL请求响应时间大幅减少，平均响应时间由优化前的4658110 毫秒减低至11.5169 毫秒，说明系统性能得到大幅提升！**
- **优化后的平均DJBC耗时大幅减少，由优化前的 232504.45毫秒 降低至 107.48毫秒，说明SQL语句的查询效率得到大幅提升！**

## 3、SQL数据分析

|          数据/测试时间段          |                            测试前                            |       测试时       |       测试后       |
| :-------------------------------: | :----------------------------------------------------------: | :----------------: | :----------------: |
|              查询SQL              | SELECT id,title,imgs,news_content_id,remarks,create_user_id,create_date,update_user_id,update_date FROM tab_news_info ORDER BY id DESC LIMIT 0,10 |         同         |         同         |
|          SQL执行数（条）          |                        13（以往数据）                        | 24（包含以往数据） | 37（包含以往数据） |
|         总执行时间（ms）          |                             210                              |        1370        |        3303        |
| 测试过程中最慢一次SQL查询耗时(ms) |                              76                              |        722         |        886         |
|        执行中的线程（个）         |                              0                               |         0          |         0          |
|         最大并发数（个）          |                              2                               |         2          |         2          |

分析：

- SQL执行条数（测试后-测试前）：24
- SQL总执行时间（测试后-测试前）：3093 ms
- 平均SQL执行时间（总时间/执行数）：128.875 ms
- 测试过程中最慢一次SQL查询耗时：886 ms
- 测试过程中的线程执行数：0
- 测试过程中的最大线程并发数：2

**小结：**

- **SQL执行率大幅降低，由优化前的100%SQL执行降低至24/10000x100%≈0.0024%，说明加入的缓存抵挡了99.998%的查询，大幅降低了数据库的查询压力！**

- **平均SQL查询耗时大幅降低，由优化前的 463543.5毫秒降低至 128.875毫秒，说明优化后的SQL查询效率得到大幅提高！**

  

Mysql Explian命令分析Sql语句：

![](/images/优化后测试/Explain信息.png)		

SQL查询计划分析：

- select_type（SQL复杂度）：SIMPLE，简单SQL语句，不包含子查询或UNION联合查询。
- type（查询类型）：index，用到了索引，还不错。
- possible_keys（可能用到的索引）：null。
- key（实际用到的索引）：PRIMARY，实际使用了主键索引。
- rows（大概读取的行数）：10，只读取了我们实际需要的行数，不错。
- Extra（附加信息）：无

**SQL分析总结：**

​	**根据SQL分析得知，优化后的SQL语句，由全表查询转为索引查询，由文件排序转为索引排序，使得SQL查询变得简单高效，大幅提高了查询效率。**



## 4、堆内存数据分析

JAVA堆：

|     数据/测试时间段      |        测试前        |    测试时    |        测试后        |
| :----------------------: | :------------------: | :----------: | :------------------: |
|   当前堆内存大小（MB）   |         805          |     805      |         805          |
| 当前堆内存使用大小（MB） |         256          |     191      |         87.4         |
|   最大堆内存大小（MB）   |         805          |     805      |         805          |
|    堆内存使用变化曲线    | 变化平稳，且跨度很大 | 变化有些密集 | 恢复平稳，且跨度很大 |

分析：

- 测试过程中当前堆内存变化：一直是805，不变
- 测试过程中堆内存使用变化：256-> 191-> 87.4，堆内使用呈降低趋势，可能是测试时的记录时刻刚好是一次GC后带来的。
- 测试过程中最大堆内存变化：一直是 805，不变。
- 测试过程中堆内存使用曲线：测试时变化密集，其他时间段平稳，且变化时间跨度很大。

**小结**：

​	1、测试过程中Java堆内存都保存不变，这避免扩容带来的性能损失。

​	2、测试过程中堆内存的使用一直小于最大堆内存，没有发生内存溢出。

​	3、测试过程中堆内存使用稍有变化，但没有优化前那么密集，说明 JVM 存在GC，但不是很频繁，具体需要结合GC日志方可知晓。

非堆（方法区）：

|      数据/测试时间段       | 测试前 |  测试时  | 测试后 |
| :------------------------: | :----: | :------: | :----: |
|      元空间大小（MB）      |  71.4  |   71.6   |  71.8  |
|   当前非堆内存大小（MB）   |  121   |   122    |  122   |
| 当前非堆内存使用大小（MB） |  116   |   117    |  118   |
|   最大非堆内存大小（GB）   |  1.46  |   1.46   |  1.46  |
|     堆内存使用变化曲线     |  平稳  | 稍有上升 |  平稳  |

分析：

- 测试过程中元空间内存变化：71.4-> 71.8，变化不大
- 测试过程中非堆内存变化：121->122，扩容了1MB
- 测试过程中非堆内存使用变化：116-> 118，稍微增加了一点
- 测试过程中最大非堆内存变化：一直是 1.46 GB，不变
- 测试过程中非堆内存使用线：只在测试时稍微上升。

**小结：**

​	测试过程中，非堆内存的使用变化不大，但扩容了1MB，在最大非堆内存之内，总体还不错。



## 5、GC数据分析

|   数据/测试时间段   | 测试前 | 测试时 | 测试后 |
| :-----------------: | :----: | :----: | :----: |
|   GC总次数（次）    |   27   |   45   |   69   |
|    GC总耗时（s）    | 1.3450 | 1.5110 | 1.6870 |
| 最慢一次GC耗时（s） | 0.0090 | 0.0190 | 0.0090 |

分析：

- 测试过程中GC发生的次数：69-27 = 42次

- 测试过程中GC总耗时：1.6870 - 1.3450= 0.3420 秒

- 测试过程中最慢的一次GC耗时：0.0190 秒

- 测试过程中的GC频率（总GC次数/总测试时间）：42 / 406 ≈ 0.1034 次/秒

  即10秒左右发生一次GC。

  

GC日志记录：

|        数据/测试阶段         | 测试前 | 测试后 |
| :--------------------------: | :----: | :----: |
|  年轻代的GC次数（YGC，次）   |   31   |   72   |
| 年轻代中GC总耗时（YGCT，秒） | 1.470  | 1.804  |
|    全GC的次数（FGC，次）     |   0    |   0    |
|    全GC总耗时（FGCT，秒）    |   0    |   0    |
|  所有GC的总耗时（GCT，秒）   | 1.470  | 1.804  |

- 从系统运行到日志采集为止的GC信息：
  - 年轻代的GC次数（YGC）：72次
  - 年轻代中GC总耗时（YGCT）：1.804秒
  - 全GC的次数（FGC）：0 次
  - 全GC总耗时（FGCT）：0 秒
  - 所有GC的总耗时（GCT）：1.804 秒
- 测试过程中的GC信息（测试后-测试前）：
  - 年轻代的GC次数（YGC）：41次
  - 年轻代中GC总耗时（YGCT）：1.804 - 1.470 = 0.334秒
  - 全GC的次数（FGC）：0 次
  - 全GC的次数（FGC）：0 次
  - 所有GC的总耗时（GCT）：0.334秒

**小结：**

​	1、系统的整个运行的整个过程中没有发生一次全GC（FGC）。

​	2、年轻代的GC次数大幅减少。

​	3、测试过程中的GC频率大幅降低，由优化前的 0.233秒/次降低至0.1034次/秒，间接提升了系统的整体性能。

## 6、运行线程分析

|  数据/测试时间段   | 测试前 | 测试时 | 测试后 |
| :----------------: | :----: | :----: | :----: |
| 当前运行线程（个） |   29   |   30   |   31   |
|   守护线程（个）   |   7    |   8    |   8    |
| 顶峰运行线程（个） |   31   |   31   |   31   |

分析：

- 当前运行线程数变化：29-> 31
- 守护线程数变化：7-> 8
- 顶峰运行线程数变化：都是31，不变

**小结：**

​	测试过程中运行线程数有增长，但因并发数不高，所以增长得不明显。

## 7、CPU数据分析

| 数据/测试时间段 | 测试前 | 测试时 | 测试后 |
| :-------------: | :----: | :----: | :----: |
|     进程ID      | 12726  | 12726  | 12726  |
|    CPU使用率    |   8%   |  46%   |   6%   |

分析：

- 进程ID：都是1844
- CPU使用率变化：6% -> 40% -> 6%

**小结：**

​	1、测试过程中进程ID都是 12726，保证了测试的合法性。

​	2、结合 ECS 服务器监控数据可知，CPU使用率在测试过程中上升比较快，但整体都在服务器的承受范围内，对测试的效果影响不大。

## 8、JMeter聚合报告分析

|        字段        | 数据  |
| :----------------: | :---: |
|  测试线程数（个）  |  10   |
|  测试样本数（个）  | 10000 |
| 平均响应时间（ms） |  367  |
|  请求异常率（%）   |   0   |
|   吞吐量（次/s）   | 24.6  |

分析：

​	由测试报告可知：

​	1、测试正常，没有异常。

​	2、平均响应时间为 367 毫秒，还算比较快。

​	3、吞吐量为24.6次/s，即服务器每秒大概可以处理24.6个请求。

**小结：**

​	服务器整体响应速度还算不错，就是并发承载量有点小，理论上能承受的最大并发大概为25左右。



## 9、优化测试分析总结

|           数据\版本           |  优化前  | 优化后  |  提升   |
| :---------------------------: | :------: | :-----: | :-----: |
|     URL平均响应时间（ms）     |  465811  | 11.5169 | 99.998% |
|     SQL平均执行时间（ms）     | 463543.5 | 128.875 | 99.972% |
|  测试过程中的GC频率（次/秒）  |  0.233   | 0.1034  | 55.622% |
|   测试过程中GC总次数（次）    |   113    |   42    | 62.832% |
| 系统运行过程中FGC总次数（次） |    15    |    0    |  100%   |
|        吞吐量（次/秒）        |   0.02   |  24.6   | 1229倍  |

**总结：**

​	**此次系统接口的响应速度优化非常成功，将接口的平均响应速度优化到了毫秒级以内，接口的整体性能不错，==但理论上服务器的并发承载量不大，具体需要对接口进行并发压力测试==。**

# 五、错误处理

- JMeter报错如下：

  ```java
  java.net.SocketException: Permission denied: connect
  	at java.net.DualStackPlainSocketImpl.connect0(Native Method)
  	at java.net.DualStackPlainSocketImpl.socketConnect(DualStackPlainSocketImpl.java:83)
  	at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)
  	at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:206)
  	at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:188)
  	at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:172)
  	at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
  ```

  原因分析：

  ​	网上查阅资料得知，这大概是JMeter使用IPV6访问服务器，但服务器却不支持IPV6导致。

  解决方法：

  ​	在 JMeter 的 *system.properties* 配置文件中添加如下代码：

  ```properties
  java.net.preferIPv4Stack=true
  ```

  参考：[SocketException: Permission denied: connect in Jmeter](https://stackoverflow.com/questions/47403691/socketexception-permission-denied-connect-in-jmeter)

- JMeter报错如下：

  ```java
  java.net.SocketTimeoutException: connect timed out，Read timed out
  ```

  可能原因分析：

  ​	1、根据错误信息可知，这是JMeter在测试时连接超时与读取超时导致。

  ​	2、服务器那边未处理该线程的请求，或者为保证服务能力，断掉了连接。

  

  解决办法：

  ​	1、提高JMeter的HTTP配置中的超时时间。

  ​	2、排查服务器错误日志。

  

  参考：

  [jmeter性能测试java.net.SocketTimeoutException: connect timed out，Read timed out](https://www.cnblogs.com/huamei2008/p/8677016.html)

  [JMeter测试问题：java.net.SocketTimeoutException: connect timed out，Read timed out](https://blog.csdn.net/ceo158/article/details/9251825)

  

- JMeter报错如下：

  ```java
  Connection reset by peer: socket write error
  ```

  可能原因分析：

  ​	查阅相关资料可知，这是因为JMeter请求连接被重置导致的问题。

  解决办法：

  ```
  1.修改HTTP请求下面的Impementation选项，改成HttpClient4
  
  2.在user.properties文件内修改：
  
  hc.parameters.file=hc.parameters
  
  3.在hc.parameters文件内修改：
  
  http.connection.stalecheck$Boolean=true
  
  重启Jmeter再尝试一下
  ```

  参考：[Jmeter遇到线程链接被重置（Connection reset by peer: socket write error）的解决方法](https://www.cnblogs.com/performancetest/p/5535883.html)



# 六、参考资料

- 测试：

​	[**JMeter网站并发性测试**](http://www.cnblogs.com/qianzf/p/6923412.html)

​	[**JMeter之Ramp-up Period（in seconds）说明**](https://www.cnblogs.com/hjhsysu/p/9189897.html)

​	[**MySql Explian命令详解**](https://www.cnblogs.com/gomysql/p/3720123.html)

​	[**SocketException: Permission denied: connect in Jmeter**](https://stackoverflow.com/questions/47403691/socketexception-permission-denied-connect-in-jmeter)

​	[**jmeter性能测试java.net.SocketTimeoutException: connect timed out，Read timed out**](https://www.cnblogs.com/huamei2008/p/8677016.html)

​	[**JMeter测试问题：java.net.SocketTimeoutException: connect timed out，Read timed out**](https://blog.csdn.net/ceo158/article/details/9251825)

​	[**Jmeter遇到线程链接被重置（Connection reset by peer: socket write error）的解决方法**](https://www.cnblogs.com/performancetest/p/5535883.html)

​	[**Jmeter性能结果分析**](https://www.cnblogs.com/xiaoxiaoxuepiao/p/9057211.html)

​	[**TPS、QPS和系统吞吐量的区别和理解**](https://blog.csdn.net/u010889616/article/details/83245695)

​	[**[JM_03]JMeter性能测试基础实战之QPS检测过程解析**](https://www.jianshu.com/p/b22c57ceb52b)

​	《深入理解Java虚拟机》第2版 周志明著

- 优化

  - 服务器优化：

  ​	[**Tomcat 、Jetty 和 Undertow 对比测试**](https://www.jianshu.com/p/f7cb40a8ce22)

  ​	[**Undertow,Tomcat和Jetty服务器配置详解与性能测试**](https://www.cnblogs.com/maybo/p/7784687.html)

  ​	[**Spring Boot 容器选择 Undertow 而不是 Tomcat**](https://www.cnblogs.com/duanxz/p/9337022.html)
  - Mysql优化：

  ​	[**Mysql优化技巧**](https://blog.csdn.net/u013087513/article/details/77899412#commentBox)

  ​	[**MySQL 对于千万级的大表要怎么优化？**](https://www.zhihu.com/question/19719997)

  ​	[**MySQL索引优化分析**](http://www.cnblogs.com/itdragon/p/8146439.html)

  ​	[**MySQL大数据量分页查询方法及其优化**](https://www.cnblogs.com/geningchao/p/6649907.html)
  - JVM优化：

  ​	《深入理解Java虚拟机》 第2版 周志明 著

  ​	[**JDK8堆默认比例**](https://blog.csdn.net/liuchaoxuan/article/details/80958128)

  ​	[**Java8 jvm参数**](https://www.cnblogs.com/milton/p/6134251.html)

  ​	[**jvm参数设置大全**](https://www.cnblogs.com/marcotan/p/4256885.html)

  ​	[**JVM（Java虚拟机）优化大全和案例实战**](https://blog.csdn.net/kthq/article/details/8618052)

  ​	[**JVM性能调优指南**](https://www.jianshu.com/p/aaee11115f37)

  ​	[**JVM调优总结**](https://www.cnblogs.com/andy-zhou/p/5327288.html)

  ​	[**JVM内存参数详解以及配置调优**](https://www.cnblogs.com/milton/p/6134251.html)

  ​	[**JVM内存设置多大合适？Xmx和Xmn如何设置？**](https://www.jianshu.com/p/d23e7197d3fa)
  - 缓存优化：

  ​	[**Mybatis整合第三方缓存ehcache**](https://blog.csdn.net/jinhaijing/article/details/84255107)