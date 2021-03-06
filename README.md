# 本科毕设项目：

# 	    面向高并发访问优化的内容管理系统

- 立项背景：

  大学四年间，本人学习了各种各样的开发技术，也做过了一些相关的项目实战，并发和优化的相关知识也学过不少，但却因条件原因没有在实际中真正触及到项目的并发和优化。众所周知，在开发当中系统的并发优化是个重点，也是个难点，所以本人想在离校之际挑战一下自己，顺便总结一下自己的四年所学。

- 优化目的：

  让部署在阿里云ECS学生云服务器（默认1CPU+2GB内存+1MB带宽）上的系统能满足高并发、高性能、高可用的需求。（即能满足100以上用户并发，且并发期间系统的平均响应时间能保持在2S以内，且系统具备一定的容灾能力。）

- 操作简介

  项目以一个简单MVC三层结构的系统项目为基础，重复以下步骤的操作，直到系统能满足相关的性能需求。

  - 制定测试方案对系统进行并发访问测试
  - 收集相关测试数据
  - 分析测试数据
  - 找出系统性能瓶颈
  - 为系统性能瓶颈制定相应的优化方案
  - 执行优化方案对系统进行优化
  - 对优化后的系统进行并发访问测试
  - 收集相关测试数据并对比优化前后的相关性能
  - ……

- 涉及到的优化

  - 数据库优化
    - 表结构优化
    - 添加索引
    - SQL语句优化
  - JVM优化
  - 添加缓存
    - Ehcache本地缓存
    - Redis分布式缓存
  - Sevlet容器优化
  - 带宽升级
  - 系统集群

- 测试手记

  - [简单MVC项目测试报告](test/1、简单MVC项目测试报告.md)
  - [简单MVC项目优化方案](test/2、简单MVC项目优化方案.md)
  - [简单MVC优化后测试报告](test/3、简单MVC优化后测试报告.md)
  - [MVC优化后压力测试报告](test/4、MVC优化后压力测试报告.md)
  - [带宽升级后压力测试报告](test/5、带宽升级后压力测试报告.md)
  - [MVC应用集群优化方案](test/6、MVC应用集群优化方案.md)
  - [MVC应用集群压力测试](test/7、MVC应用集群压力测试.md)

  注：这些只是本人在做毕设时所做的一些测试手记，结构和内容不够严谨，且是本人在毕业一个月后才来整理的，整理时对过去做测试的一些相关测试数据已经记忆模糊，**所以手记可能会存在一些纰漏和问题，仅做测试和优化的操作参考**，具体可参看本人的本科毕业论文：《面向高并发访问优化的内容管理系统》。

- 毕业论文

  [《面向高并发访问的内容管理系统》](毕业论文-面向高并发访问优化的内容管理系统.docx)

