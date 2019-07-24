# 一、优化目的

## 1、优化背景

由测试报告可知，项目 graduation-project-mvc 的新闻列表接口（http://120.78.59.162:8081/newsInfo/list?currentPage=1&rows=10&condition=%7B%22searchContent%22%3A%22%22%7D）在并发访问测试时**龟速响应**，接口的平均响应时间高达 **465811 毫秒，即7.76分钟**！这种响应速度令人难以接受，点进去半天页面内容加载不出来，不知道的还以为是静态页面，不可忍，必须优化！

## 2、优化目的

对测试的新闻列表接口（http://120.78.59.162:8081/newsInfo/list?currentPage=1&rows=10&condition=%7B%22searchContent%22%3A%22%22%7D）进行优化，让其平均响应时间降低到可以接受的范围内。

# 二、优化方案

## 1、优化前提

由测试报告得知，降低接口响应速度主要有以下原因：

1.  **SQL语句**，占URL响应时间的**99.51%**。
2. **JVM 频繁GC**导致的性能损耗。

## 2、优化方案制定

### 1、服务器优化

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
	<exclusions>
		<!--排除 tomcat 容器-->
		<exclusion>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <groupId>org.springframework.boot</groupId>
        </exclusion>
	</exclusions>
</dependency>

<!--引入其他 Servlet 容器
     	Jetty：擅长 长连接
        undertow：擅长 并发，是非阻塞 Servlet 容器
       -->
<dependency>
	<artifactId>spring-boot-starter-undertow</artifactId>
	<groupId>org.springframework.boot</groupId>
</dependency>
```

​	说明：

​		更换 Servlet 容器，使用更擅长并发的 undertow，抛弃原来的 tomcat。

### 2、Mysql数据库优化

#### 1、数据库优化

##### 1、优化理论

- 数据库表设计

  1. 表字段避免null值出现，null值很难进行查询优化且占用额外的索引空间，推荐默认数字0代替null。
  2. 尽量使用INT而非BIGINT，如果非负则加上UNSIGNED（这样数值容量会扩大一倍），当然能使用TINYINT、SMALLINT、MEDIUM_INT更好。
  3. 使用枚举或整数代替字符串类型。
  4. 尽量使用TIMESTAMP而非DATETIME。
  5. 单表不要有太多字段，建议在20以内。
  6. 用整型来存IP。
  7. 等等。

- 索引设计

  1. 索引要占用物理内存，并不是越多越好，要根据查询有针对性的创建，考虑在WHERE和ORDER BY命令上涉及的列建立索引，可根据EXPLAIN来查看是否用了索引还是全表扫描。
  2. 应尽量避免在WHERE子句中对字段进行NULL值判断，**否则将导致引擎放弃使用索引而进行全表扫描。**
  3. 值分布很稀少的字段不适合建索引，例如"性别"这种只有两三个值的字段。
  4. 字符字段只建前缀索引。
  5. 字符字段最好不要做主键。
  6. 不用外键，由程序保证约束。
  7. 尽量不用UNIQUE，由程序保证约束。
  8. 使用多列索引时主意顺序和查询条件保持一致，同时删除不必要的单列索引。
  9. 等等。

- 总结

  **使用合适的数据类型，选择合适的索引**

  - 使用合适的数据类型
    - 使用可存下数据的最小的数据类型，整型 < date，time < char，varchar < blob
    - 使用简单的数据类型，整型比字符处理开销更小，因为字符串的比较更复杂。如，**int类型存储时间类型，bigint类型转ip函数。**
    - 使用合理的字段属性长度，固定长度的表会更快。使用enum、char而不是varchar。
    - 尽可能使用not null定义字段。
    - 尽量少用text，非用不可最好分表。

  - 选择合适的索引列（即哪些列适合添加索引）
    - 查询频繁的列，在where，group by，order by，on从句中出现的列。
    - where条件中<，<=，=，>，>=，between，in，以及like 字符串+通配符（%）出现的列。
    - 长度小的列，索引字段越小越好，因为数据库的存储单位是页，一页中能存下的数据越多越好。
    - 离散度大（不同的值多）的列，放在联合索引前面。查看离散度，通过统计不同的列值来实现，count越大，离散程度越高。

##### 2、优化操作

1. 表拆分

   背景：因为之前的新闻信息表有个新闻内容字段，用来存储新闻内容，字段类型为text，所以我将新闻内容从新闻信息表里分离出来，单独做成一个新闻内容表，以新闻内容id做关联。

   ![](/images/优化方案/新闻信息表-拆分.png)

2. 字段优化

- 优化前

  ![](/images/优化方案/新闻信息表-优化前.png)

  ```mysql
  DROP TABLE IF EXISTS `tab_news_info`;
  CREATE TABLE `tab_news_info` (
    `id` varchar(36) NOT NULL DEFAULT '' COMMENT 'ID',
    `title` varchar(100) DEFAULT NULL COMMENT '新闻备注',
    `content` longtext COMMENT '新闻内容',
    `imgs` text COMMENT '新闻图片',
    `remarks` varchar(36) DEFAULT NULL COMMENT '新闻备注',
    `create_user_id` varchar(36) DEFAULT NULL COMMENT '创始人ID',
    `create_date` datetime DEFAULT NULL COMMENT '创建时间',
    `update_user_id` varchar(36) DEFAULT NULL COMMENT '更新人ID',
    `update_date` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  ```

  

- 优化操作

  1. 将 varchar 类型的UUID主键改为 int 类型的自增主键。
  2. 将 varchar 类型的变长字段改为 char 类型的固定长度字段。
  3. 将新闻内容分离出去，单独成一张新表 tab_news_content，通过 news_content_id 关联。
  4. 为create_user_id、update_user_id、news_content_id等可能是经常使用到的字段添加索引。
  5. 将 datetime 类型的字段改为 timestamp 类型。

- 优化后

  - 新闻信息表

  ![](/images/优化方案/新闻信息表-优化后.png)

  ```mysql
  DROP TABLE IF EXISTS `tab_news_info`;
  CREATE TABLE `tab_news_info` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `title` char(200) NOT NULL DEFAULT '0' COMMENT '新闻备注',
    `news_content_id` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '新闻内容id',
    `imgs` char(200) NOT NULL DEFAULT '0' COMMENT '新闻图片预览',
    `remarks` char(200) NOT NULL DEFAULT '0' COMMENT '新闻备注',
    `create_user_id` varchar(36) NOT NULL DEFAULT '0' COMMENT '创始人ID',
    `create_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` varchar(36) NOT NULL COMMENT '更新人ID',
    `update_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_newInfoOpt_c_uid` (`create_user_id`) USING BTREE COMMENT '索引-新闻信息表【优化】-发布人id',
    KEY `idx_newInfoOpt_u_uid` (`update_user_id`) USING BTREE COMMENT '索引-新闻信息表【优化】-更新人id',
    KEY `idx_newInfoOpt_contentId` (`news_content_id`) USING BTREE COMMENT '索引-新闻信息表【优化】-内容id'
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  ```

  ​	说明：

  ​		此处只是针对 **新闻列表接口** 做优化，所以关于用户的字段我就暂且不修改，后期再改影响也不大。

  - 新闻内容表

  ![](/images/优化方案/新闻内容表.png)

  ```mysql
  DROP TABLE IF EXISTS `tab_news_content`;
  CREATE TABLE `tab_news_content` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `content` text NOT NULL COMMENT '新闻内容',
    PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  
  ```

#### 2、SQL优化

##### 1、优化理论

1. 使用limit对查询结果的记录进行限定。
2. 避免select *，将需要查找的字段列出来。
3. 使用连接（join）来代替子查询。
4. 拆分大的delete或insert语句。
5. 可通过开启慢查询日志来找出较慢的SQL。
6. 不做列运算：SELECT id WHERE age + 1 = 10，任何对列的操作都将导致表扫描，它包括数据库教程函数、计算表达式等等，查询时要尽可能将操作移至等号右边。
7. sql语句尽可能简单：一条sql只能在一个cpu运算；大语句拆小语句，减少锁时间；一条大sql可以堵死整个库。
8. OR改写成IN：OR的效率是O(n)级别，IN的效率是O(logn)级别，in的个数建议控制在200以内。
9. 不用函数和触发器，在应用程序实现。
10. 避免%xxx式查询。
11. 少用JOIN。
12. 使用同类型进行比较，比如用'123'和'123'比，123和123比。
13. 尽量避免在WHERE子句中使用 != 或 <> 操作符，否则导致引擎放弃使用索引而进行全表扫描。
14. 对于连续数值，使用BETWEEN不用IN：SELECT id FROM t WHERE num BETWEEN 1 AND 5。
15. 列表数据不要拿全表，要使用LIMIT来分页，每页数量也不要太大。
16. 等等。

##### 2、优化操作

1. 优化前

   ```mysql
   -- 作用：按时间逆序对新闻列表进行分页查询，此处查询的是第一页的数据。
   
   SELECT id,title,imgs,content,remarks,create_user_id,create_date,update_user_id,update_date
   FROM tab_news_info
   ORDER BY create_date DESC
   LIMIT 1,10
   ```

   MySql Explain查询计划：

   ![](/images/优化方案/Explain信息-优化前.png)

   SQL查询计划分析：

   - select_type（SQL复杂度）：SIMPLE，简单SQL语句，不包含子查询或UNION联合查询。
   - type（查询类型）：ALL，全表查询。【**非常严重，需要优化**】
   - possible_keys（可能用到的索引）：null。
   - key（实际用到的索引）：null。【**非常严重，需要优化**】
   - rows（大概读取的行数）：931.【**非常严重，读取的数据接近全部数据的2/3，需要优化**】
   - Extra（附加信息）：Using filesort，表示MySQL中无法利用索引完成排序，只能依赖于外部的文件进行排序。【**非常严重，需要优化**】

2. 优化思路

   ​	优化后的新闻信息表使用的是自增id，所以最新插入的新闻信息id肯定是最大的，又因为id是主键，自带主键索引，所以可以利用自增id来完成高效率的时间逆序分页查询。

3. 优化后

   ```mysql
   -- 作用：按时间逆序对新闻列表进行分页查询，此处查询的是第一页的数据。
   
   SELECT id,title,imgs,news_content_id,remarks,create_user_id,create_date,update_user_id,update_date
   FROM tab_news_info
   ORDER BY id desc 
   LIMIT 1,10
   ```

   MySql Explain查询计划：

   ![](/images/优化方案/Explain信息-优化后.png)

   SQL查询计划分析：

   - select_type（SQL复杂度）：SIMPLE，简单SQL语句，不包含子查询或UNION联合查询。
   - type（查询类型）：index，用到了索引，还不错。
   - possible_keys（可能用到的索引）：null。
   - key（实际用到的索引）：PRIMARY，实际使用了主键索引。
   - rows（大概读取的行数）：10，只读取了我们实际需要的行数，不错。
   - Extra（附加信息）：无

4. SQL优化总结

   优化后的sql语句总体还不错，但还存在缺陷，即数据量很大时，直接使用LIMIT进行分页，它也需要扫描前面的所有数据，然后再提取符合当前页数的10条数据，扫描的数据量还是很大，还有优化的空间，不过本项目的数据量不是很大，就不做那么细了。

### 3、JVM优化

#### 1、优化理论

​	JVM调优的内容过于庞大，需要大量的实战经验才来处理各种情况，本人还达不到那种火候，这里就只是稍微优化一下：

- 分配合理的堆内存大小，尽可能减少堆内存自动扩容带来的性能损耗，并防止内存溢出等异常。
- 设置合理的堆内存比列【新生代：老年代】，防止GC过于频繁导致性能下降。
- 选用合理的垃圾收集算法【垃圾收集器】。
- 等。

#### 2、优化前提

​	![](/images/优化方案/java信息.png)

![](/images/优化方案/jinfo信息.png)

![](/images/优化方案/jmap信息.png)

​	通过查看服务器的信息可知：

- **JDK版本**：JDK1.8.0_151

- **JVM版本**：Java HotSpot(TM) 64-Bit Server VM （25.151-b12）

- **垃圾收集器**：CMS

-  **MaxHeapFreeRatio**:  40%

  ​	GC后如果发现空闲堆内存占到整个预估堆内存的N%(百分比)，则收缩堆内存的预估最大值, 预估堆内存是堆大小动态调控的重要选项之一.  堆内存预估最大值一定小于或等于固定最大值(-Xmx指定的数值). 前者会根据使用情况动态调大或缩小, 以提高GC回收的效率

- **MinHeapFreeRatio**: 70%

  ​	GC后如果发现空闲堆内存占到整个预估堆内存的N%(百分比), 则放大堆内存的预估最大值。

- **MaxHeapSize**: 460.0 MB

  ​	即-Xmx, 堆内存大小的上限。

- **InitialHeapSize**: 30 MB

  即-Xms, 堆内存大小的初始值

- **NewSize**: 10.0 MB

  ​	新生代预估堆内存占用的默认值

- **MaxNewSize**: 153.3 MB

  ​	新生代占整个堆内存的最大值

- **OldSize**: 20.0 MB

  ​	老年代的默认大小

- **NewRatio**: 2

  ​	老年代对比新生代的空间大小, 比如2代表老年代空间是新生代的两倍大小. 

- **SurvivorRatio**: 8

  ​	 Eden/Survivor的比值，8表示Survivor:Eden=1:8,  因为survivor区有2个, 所以Eden的占比为8/10. 

- **MetaspaceSize**: 20.8 MB 

  ​	分配给类元数据空间的初始大小(Oracle逻辑存储上的初始高水位，the initial high-water-mark ). 此值为估计值.  MetaspaceSize设置得过大会延长垃圾回收时间. 垃圾回收过后, 引起下一次垃圾回收的类元数据空间的大小可能会变大

- **MaxMetaspaceSize**: 17592186044415 MB

  ​	是分配给类元数据空间的最大值, 超过此值就会触发Full GC. 此值仅受限于系统内存的大小, JVM会动态地改变此值

- **CompressedClassSpaceSize**: 1024 MB

  ​	类指针压缩空间大小, 默认为1G

- **G1HeapRegionSize**: 0 MB

  ​	G1区块的大小, 取值为1M至32M. 其取值是要根据最小Heap大小划分出2048个区块.   

#### 3、优化操作

1. 内存分配

   参考：[JVM内存配置详解](https://www.cnblogs.com/qmfsun/p/5396710.html)、[JVM内存参数详解以及配置调优](https://www.cnblogs.com/milton/p/6134251.html)、[JVM内存设置多大合适？Xmx和Xmn如何设置？](https://www.jianshu.com/p/d23e7197d3fa)

   基于ECS服务器的2GB内存，除去系统占用的，还剩1.8GB左右的内存考虑，加上跑Docker和其他应用等，实际能用的内存大概1.2G左右，依据网上查阅的经验，做了以下内存分配规则：

   - 初始堆内存：768MB，设置参数：-Xms768m
   - 最大堆内存：768MB，设置参数：-Xmx768m
   - 年轻代内存：256MB，设置参数：-Xmn256m
   - 元空间内存：128MB，设置参数：-XX:MetaspaceSize=128m
   - 最大元空间内存：128BM，设置参数：-XX:MaxMetaspaceSize=128m
   - 每个线程的大小：256k，设置参数：-Xss256k
   - 根据计算公式：堆大小=年轻代大小+老年代大小，得出老年代内存是：768-256 = 512MB（元空间不影响堆内存）

   说明：**将最大内存与最小内存设为一样，是为了避免内存扩容时带来的性能损耗**

   ```shell
   nohup java -jar -Xms768m -Xmx768m -Xmn256m -XX:MetaspaceSize=128m \
   -XX:MaxMetaspaceSize=128m -Xss256k -XX:+UseG1GC \
   graduation-project-mvc-optimize-1.0-SNAPSHOT.jar \
   >graduation-project-mvcoptimize.log & 
   ```

   

2. 垃圾收集器

   改用G1收集器，减少可能由CMS带来的内存碎片导致频繁GC的问题，设置参数：-XX:+UseG1GC

3. 其他

   如新生代与老年代的比列、Eden区与Survivor区的比列等，都使用默认的。

### 4、加入缓存

​	加入 Ehcahe 作为本地缓存，使用 Mybatis 对其进行整合。

### 5、代码优化

1. 调整部分业务逻辑代码，以符合上述优化操作后的使用。
2. 清除没有用到的外部引入和垃圾代码，重构冗余过大的部分代码。
3. 开启 Mybatis 懒加载、开启二级缓存（使用 Ehcahe）。

# 三、参考资料

服务器优化：

​	[**Tomcat 、Jetty 和 Undertow 对比测试**](https://www.jianshu.com/p/f7cb40a8ce22)

​	[**Undertow,Tomcat和Jetty服务器配置详解与性能测试**](https://www.cnblogs.com/maybo/p/7784687.html)

​	[**Spring Boot 容器选择 Undertow 而不是 Tomcat**](https://www.cnblogs.com/duanxz/p/9337022.html)

Mysql优化：

​	[**Mysql优化技巧**](https://blog.csdn.net/u013087513/article/details/77899412#commentBox)

​	[**MySQL 对于千万级的大表要怎么优化？**](https://www.zhihu.com/question/19719997)

​	[**MySQL索引优化分析**](http://www.cnblogs.com/itdragon/p/8146439.html)

​	[**MySQL大数据量分页查询方法及其优化**](https://www.cnblogs.com/geningchao/p/6649907.html)

JVM优化：

​	《深入理解Java虚拟机》 第2版 周志明 著

​	[**JDK8堆默认比例**](https://blog.csdn.net/liuchaoxuan/article/details/80958128)

​	[**Java8 jvm参数**](https://www.cnblogs.com/milton/p/6134251.html)

​	[**jvm参数设置大全**](https://www.cnblogs.com/marcotan/p/4256885.html)

​	[**JVM（Java虚拟机）优化大全和案例实战**](https://blog.csdn.net/kthq/article/details/8618052)

​	[**JVM性能调优指南**](https://www.jianshu.com/p/aaee11115f37)

​	[**JVM调优总结**](https://www.cnblogs.com/andy-zhou/p/5327288.html)

​	[**JVM内存参数详解以及配置调优**](https://www.cnblogs.com/milton/p/6134251.html)

​	[**JVM内存设置多大合适？Xmx和Xmn如何设置？**](https://www.jianshu.com/p/d23e7197d3fa)

缓存优化：

​	[**Mybatis整合第三方缓存ehcache**](https://blog.csdn.net/jinhaijing/article/details/84255107)

