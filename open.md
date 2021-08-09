### 1.  基本

完成开放平台交互总共需要两对公私钥匙，开放平台与APP各自保留3把钥匙。

App保留： app公钥、平台公钥、app私钥

平台保留：app公钥、平台公钥、平台私钥

app钥匙对由贵方自己生成并保留，平台不承担私钥泄露造成的损失。



### 2. 请求接口

##### 公共参数（1个）：

optimestamp:时间戳 意为open platform timestamp ，若时间戳(毫秒)与标准时间相差超过一分钟，认为请求失效。

_gp: api分组

_mt: 具体API，后两个参数，详见API文档

##### 应用参数：

将参数以表单的形式排列

a=1&b=2&c=3&obj={"a":"b"}

##### 合并参数

a=1&b=2&c=3&obj={"a":"b"}&optimestamp=1628046510450&_gp=admin.erpstockin&_mt=list

##### 使用app私钥签名

例如我的私钥为：

MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALwcyvYIGmhk+be320JWWsq1OYjiM0lzv8eHGMgSIOMLxzM/g9X7jguNe8thxJXR/CLqcTgsfZzk8E8Sc9+qnSDxNl5f5tga93vRxd5713zAeAGqLiTQnRffdzRmdbsmu5+0/K8mj056VhKh8FdBNzAj7e4iX9i+uBBG/oDmZbTVAgMBAAECgYEAmgNU5NTDkj9B+Pnt6UU8doSjw3+3j+bV2K2yS3QUOvAUus/Ax7x6ktjWxzCXvDY9IfUil2RNv9vtKEAqYLCWjc+lf8PV/yH1b7NEgyeAPBXtAJRoOnmYL2bdPW92kP9KgxJruF6Dz/C5AmMOncsvq8ABD+9Darn4p8dwj2ZC4O0CQQDf/AHmZsQokEItfCy4mHS9UbxbfIhEUv1ApPh/+Sr7NkJkHWYCtBQo+8jKO6zurAZQgWBPD1XX2UE4R+VIiZazAkEA1wAqtMvGhccyRZr+6kpkpDIa8+9jOE+nGUzqTDvgCID6as8AzOONFVVK6m/UUqkhcJ8Qu1pF36BGojy5BX2KVwJBAJSFpbji0hXXupowqfLp3RcgmNbNWAp+QUJZYhJx5cdYbmO2fssyH+AhPT6knYJR/YnqkDM8hv6vKCkqu2YDHjMCQAOA8TE5EOclM+CGghj3VWSHnIDVKdzFD4gOBNNxNlltIKeU8AJmwunSFgJ0CBXAw9a+ANvMwM7AIeaK7sj0HskCQAvxfDCq7gaNx+pfu0FHG8Gix08A/A6foggBl1fVu+L9sr9ZuOQ3HbXnl28F9ewuB9xdjnLUDjp7W7U0pB+vKoQ=

对以上参数进行加密，得到以下数据：

o1/kTe4B04e5mUhpVxIbAtTRxhOn9eCPVN2Dltvq2qm4Y71qgugDoRCC7mLPe+mmFvxxJNk3XJCyJyN5IuMYIlWgQdABppfzySwt+fne4kOSuTS/sNE7Be6cd3kX9c6f4Ramcj4uTqlQ8BvyrXqVzHVJXyjmmSRevg2rrwFgD4c=

##### 构建请求实体

```json
{
    "clientCode":"您的APP编号，申请开放平台时产生",
    "ciphertext":"o1/kTe4B04e5mUhpVxIbAtTRxhOn9eCPVN2Dltvq2qm4Y71qgugDoRCC7mLPe+mmFvxxJNk3XJCyJyN5IuMYIlWgQdABppfzySwt+fne4kOSuTS/sNE7Be6cd3kX9c6f4Ramcj4uTqlQ8BvyrXqVzHVJXyjmmSRevg2rrwFgD4c="
}
```

使用POST请求网关地址，并且在Header里面加上 Content-Type    application/json  就可以了，您将看到响应。

### 3. 接受回调

当服务器有通知消息时，会通过之前设置的回调接口进行通知。

加密方式与请求相同，只是是反过来的。由平台构建请求，用平台私钥进行加密报文。您将得到与 “上文json”类似的请求。

使用平台公钥进行解密，即可得到原文。

得到原文后进行相应的处理业务即可。