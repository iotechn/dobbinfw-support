## Dobbin Framework Support Logo

#### 一、项目背景 

> 为了快速落地项目、快速搭建脚手架，dobbinsoft开发一套基于SpringBoot MyBatis的框架，并手搓了如参数校验、文档生成、限流、鉴权等等常用功能。core包中包括工具类、注解、模型等。


#### 二、快速开始

##### 2.1. 下载代码

您可以在国内开源社区Gitee下载（推荐）：https://gitee.com/iotechn/dobbinfw-support

您可以在国际开源社区Github下载：https://github.com/iotechn/dobbinfw-support

##### 2.2. maven引入

请确定您已经将 JAVA_HOME 配置，并将mvn命令配置到PATH中，若出现找不到命令，或找不到JAVA_HOME，[请参考此文档](https://blog.csdn.net/weixin_44548718/article/details/108635409)

在项目根目录，打开命令行。并执行 ：

```shell
mvn install -Dmaven.test.skip=true
```

引入maven坐标到工程pom.xml文件中。

```xml
<groupId>com.dobbinsoft</groupId>
<artifactId>fw-support</artifactId>
<version>1.0-SNAPSHOT</version>
```

##### PS. 请注意

请确认已经引入fw-core，[请参照项目](../dobbinfw-core) 

#### 三、功能列表 & 文档索引

3.1. 用户&管理员会话(Session) 

3.2. 动态配置(Dynamic)

3.3. 延迟队列(DelayedMQ)

[3.4. 开放平台(OpenPlatform)](./doc/04.open.md)

3.5. 数据库扩展(DB)

3.6. 缓存(Cache)

3.7. 分布式锁(Lock)

3.8. 限流(Rate)

3.9. 滑动验证码(Captcha)

3.10. 短信(Sms)

3.11. 对象存储(Storage)


#### 四、贡献 & 社区
若Support包不能满足您的业务需求，您可以直接在仓库中发布Pull Request。本项目欢迎所有开发者一起维护，并永久开源。