# 一、背景

## 1、优化前分析

由《5、带宽升级后压力测试报告》可知，单体MVC应用所处的服务器在线程组的线程数为20时，服务器的CPU使用率就已达到了极限的100%，之后由于受服务器CPU的影响，系统的响应时间变得越来越长，只有吞吐量63左右，且系统的URL实际并发量只有16。

​	由上可知，现在系统并发能力的瓶颈在服务器的CPU上，所以我能从系统代码进行处理的方案不多，且效果也不明显，既然是服务器的问题就要从服务器上下手，此处有两个解决方案，可以选其一：

- 方案一：提高服务器配置。

  比如将服务器配置从1个CPU提升到2个CPU或者4个CPU。

- 方案二：使用多个相同配置的服务器进行集群。

  既然一台服务器无法解决问题，那就提升到3台、5台等等，让多台服务器一起工作。

## 2、优化方案分析

​	简单来说，方案一无疑是最省事的，只需要将项目部署到配置更好、性能更高的服务器上即可，方案二还要涉及到系统集群的各种问题，不过方案一在实际应用中也存在很多问题，并没有想象的那么简单，主要问题有以下两点：

1. 高配服务器架构昂贵。

   配置越高的服务器往往价格非常越贵，往往一台顶级服务器的价格就能买好几台稍微逊色一点的服务器，且对于提升并发能力来说，一台顶配服务器的性能并没有几台高配服务器集群的好。

2. 受硬件极限的制约。

   排除价格原因，单服务器的性能始终是受硬件制约的，服务器的性能不可能想提升就提升，当服务器的配置已经达到了目前硬件所能支持的极限时，就无法再使用方案一这种办法了，最终还是要走集群的路线。

## 3、方案选择

经过上面分析之后，出于成本与实用考虑，此处我就模拟一下硬件配置到了极限时的场景，**即单体服务器的性能无法再提高，只能通过集群进行处理**。

# 二、集群信息

## 1、服务器信息

本次集群采用3台配置相同的阿里云ECS服务器进行处理，具体如下：

| 服务器/数据 |     IP地址     |  服务器地址   |
| :---------: | :------------: | :-----------: |
|   服务器1   | 120.78.59.162  | 华南1（深圳） |
|   服务器2   | 47.101.189.171 | 华东2（上海） |
|   服务器3   | 47.101.214.48  | 华东2（上海） |

## 2、服务器配置信息

- 服务器1

![](/images/集群方案/服务器1.png)

- 服务器2

![](/images/集群方案/服务器2.png)

- 服务器3

![](/images/集群方案/服务器2.png)



本次所用服务器的配置都是相同的，配置信息如下：

- 服务器：阿里云ECS云服务器
- 运行系统：CentOS 7.3 64位
- CPU：1 vCPU
- 内存：2GB(IO优化)
- 带宽：15 MBbps	

## 3、项目信息

### 1、前提说明

​	由于采用多台服务器进行集群，考虑到Session共享、数据一致性等原因，我在之前优化后的MVC项目的基础上做了一定的修改，项目名由 GraduationProject-Simple 变成了 GraduationProject-Cluster，新增了Nginx做反向代理和负载均衡，增加 Redis 哨兵集群做分布式缓存和Session管理。

### 2、项目信息

- 项目名：graduation-project-cluster

- 用途：

  ​	**MVC集群+分布式缓存+负载均衡架构**的内容管理系统项目，采用前后端分离技术，使用Redis集群进行分布式缓存和分布式Session管理，利用 Nginx 进行负载均衡，可通过后台管理系统的操作对前台显示的相关信息进行管理（增删改查），提供权限动态分配、用户管理、内容模糊查询等功能。

- 特点：**权限动态分配、前后端分离、分布式Session、分布式缓存、负载均衡**。

- 说明：**MVC+权限认证集群项目，Nginx负债均衡、Redis分布式缓存，主键索引、常用字段普通索引**。

- 相关技术及工具：

  - 技术：
    - 主体架构：SpringBoot、SpringMvc、Mybatis
    - 权限控制：SpringSecurity
    - Servlet容器：Undertow
    - Session管理：SpringSession
    - 缓存管理：SpringCahce、Lettuce
    - 数据库：Docker-MySql
    - 前端：Html/CSS、JavaScript、JQuery、Bootstrap、Layui、WangEditor等。
    - 前后端交互：Ajax、Json
    - 缓存：Redis哨兵模式
    - 负载均衡：Nginx
    - 监控：SpringBootAdmin、Alibaba-Druid、阿里云ECS监控
  - 工具：
    - 结构控制：Maven
    - 版本控制：Git/Github
    - 设计与画图：processon、PowerDesigner、Navicat。

### 3、项目结构

![](/images/集群方案/MVC集群.png)

### 4、集群节点说明

|  服务端口/服务器  | 服务器1：120.78.59.162 | 服务器2：120.78.59.162 | 服务器3：47.101.189.171 |
| :---------------: | :--------------------: | :--------------------: | :---------------------: |
| BootAdmin监控端口 |          9999          |           \            |            \            |
|     Nginx端口     |          8080          |           \            |            \            |
|     Mysql端口     |          3308          |           \            |            \            |
|   Redis节点端口   |     6379（master）     |          6379          |          6379           |
|   Redis哨兵端口   |         26379          |         26379          |          26379          |
|    MVC应用端口    |          8888          |          8888          |          8888           |

由上面的节点分布可知，服务器1又要跑监控，又要跑数据库，还要跑Nginx，压力比较大，所以我对 Nginx 的权重配置如下，希望测试期间由服务器2和服务器3来承受大部分请求，以此来减轻服务1的测试压力：

```shell
#服务器的集群        
upstream  gtaduation.com {  #服务器集群名字     
	#服务器weight是权重的意思，权重越大，分配的概率越大。  
	server    120.78.59.162:8888  weight=20; #服务器1
    server    47.101.189.171:8888  weight=45;#服务器2
    server    47.101.214.48:8888  weight=45;#服务器3
}
```

## 4、数据源配置

```yaml
spring:
  #数据源配置
  datasource:
    username: root
    password: toor
    url: jdbc:mysql://120.78.59.162:3308/graduation?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=GMT%2B8&allowMultiQueries=true
    type: com.alibaba.druid.pool.DruidDataSource
    #driver-class-name:springBoot2.x 后驱动程序通过SPI自动注册，并且通常不需要手动加载驱动程序类。
    druid:
      # 下面为连接池的补充设置，应用到上面所有数据源中
      initial-size: 5   #线程池初大小
      min-idle: 5       #线程池最小空闲连接
      max-active: 2000  #线程池最大连接数

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
        login-password: toor
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
```



## 5、缓存配置

```yaml
spring:
  #使用Spring Cache
  cache:
      type: redis #指定使用redis
      redis:
        time-to-live: 600 #缓存持续时间 600 秒
        
  # Redis配置，SpringBoot2.x默认使用 Lettuce 作为客户端      
  redis:
    # Redis服务器连接密码（默认为空）
    password: 你的密码
   # 连接超时时间（毫秒）
    timeout: 10000

    #哨兵模式
    sentinel:
      #哨兵集群名称
      master: mymaster
      #哨兵节点
      nodes:
        - 120.78.59.162:26379
        - 47.101.189.171:26379
        - 47.101.214.48:26379

	#这里使用 Lettuce，Jedis的连接池在 SpringBoot2.x 已不推荐使用
    lettuce:
      pool:
        # 连接池最大连接数（使用负值表示没有限制）
        max-active: 5000
        # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-wait: 60000 #毫秒
        # 连接池中的最大空闲连接
        max-idle: 8
        # 连接池中的最小空闲连接
        min-idle: 0
      #关闭超时时间，毫秒
      shutdown-timeout: 1000
```

## 6、相关代码修改

因为使用了SpringSession来管理Session，使用SpringCache来管理缓存，使用Redis作为分布式缓存等，所以与之相关的代码也都做了响应的调整，[具体源码看这里](https://github.com/Yangyi0011/Graduation-Project/tree/master/GraduationProject-Cluster/graduation-project-cluster)。

# 三、其他信息

因为是基于 graduation-project-mvc-optimize 优化后的MVC项目做的集群，所以除了集群和缓存相关的内容之外，其他如项目模块、数据库设计等信息都与优化后的MVC项目一样。

# 四、参考资料

Redis：

​	[**Redis中文网**](http://redis.cn/commands.html)

​	[**分布式知识-缓存**](https://doocs.github.io/advanced-java/#/README?id=%e7%bc%93%e5%ad%98)

​	[**docker 安装部署 redis（配置文件启动）**](https://segmentfault.com/a/1190000014091287)

​	[**springboot中各个版本的redis配置问题**](https://blog.csdn.net/qq_33326449/article/details/80457571)

​	[**Lettuce相较于Jedis有哪些优缺点？**](https://www.zhihu.com/question/53124685)

​	[**Spring Boot Lettuce Redis**](https://www.cnblogs.com/baidawei/p/9156410.html)

Nginx：

​	[**Nginx配置详解**](https://www.cnblogs.com/knowledgesea/p/5175711.html)

​	[**Nginx安装及配置详解**](https://www.cnblogs.com/fengff/p/8892590.html)

​	[**Nginx实现SpringBoot项目的负载均衡**](https://blog.csdn.net/pilihaotian/article/details/88016748)

​	[**Nginx开发从入门到精通**](http://tengine.taobao.org/book/index.html)

​	[**CentOS7 下 Nginx 安装部署和配置**](https://www.linuxidc.com/Linux/2018-07/153183.htm)

​	[**docker部署nginx**](https://www.cnblogs.com/wwzyy/p/8337965.html)

​	[**Nginx-DockerHub**](https://hub.docker.com/_/nginx)