![](https://doc-1324075299.cos.ap-guangzhou.myqcloud.com/dobbinfw/banner.jpg)

[![Release Version](https://img.shields.io/badge/release-2.0.2-brightgreen.svg)](https://gitee.com/iotechn/dobbinfw-support) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://gitee.com/iotechn/unimall/pulls)

#### 一、项目背景 

> 为了快速落地项目、快速搭建脚手架，dobbinsoft开发一套基于SpringBoot3 MyBatis的框架，并手搓了如参数校验、文档生成、限流、鉴权等等常用功能。core包中包括工具类、注解、模型等。


#### 二、快速开始

##### 2.1. 下载代码

您可以在国内开源社区Gitee下载（推荐）：https://gitee.com/iotechn/dobbinfw-support

您可以在国际开源社区Github下载：https://github.com/iotechn/dobbinfw-support

##### 2.2. maven引入

引入maven坐标到工程pom.xml文件中。

```xml
<dependency>
    <groupId>com.dobbinsoft</groupId>
    <artifactId>fw-support</artifactId>
    <version>2.x.x</version>
</dependency>
```

版本可以去maven库里面查询：

https://central.sonatype.com/artifact/com.dobbinsoft/fw-support/versions

#### 三、功能列表 & 文档索引

[3.1. 用户&管理员会话(Session)](./doc/01.session.md) 

[3.2. 业务日志(Log)](./doc/02.log.md)

[3.3. 延迟队列(DelayedMQ)](./doc/03.delayed.md)

[3.4. 远程调用(RPC)](./doc/04.rpc.md)

[3.5. 数据库扩展(DB)](./doc/05.db.md)

[3.6. 缓存(Cache)](./doc/06.cache.md)

[3.7. 分布式锁(Lock)](./doc/07.lock.md)

[3.8. 限流(Rate)](./doc/08.rate.md)

[3.9. 滑动验证码(Captcha)](./doc/09.captcha.md)

[3.10. 短信(Sms)](./doc/10.sms.md)

[3.11. 对象存储(Storage)](./doc/11.storage.md)

[3.12. 工具类(Utils)](./doc/12.utils.md)


#### 四、贡献 & 社区
若Support包不能满足您的业务需求，您可以直接在仓库中发布Pull Request。本项目欢迎所有开发者一起维护，并永久开源。