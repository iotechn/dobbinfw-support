package com.dobbinsoft.fw.core.exception;

/**
 * Created by rize on 2019/7/1.
 */
public class CoreExceptionDefinition {

    public static final ServiceExceptionDefinition THIRD_PART_SERVICE_EXCEPTION =
            new ServiceExceptionDefinition(0, "第三方服务异常");

    public static final ServiceExceptionDefinition THIRD_PART_IO_EXCEPTION =
            new ServiceExceptionDefinition(0, "第三方服务网络异常");

    public static final ServiceExceptionDefinition LAUNCHER_API_REGISTER_FAILED =
            new ServiceExceptionDefinition(9999, "api注册失败");

    public static final ServiceExceptionDefinition LAUNCHER_UNKNOWN_EXCEPTION =
            new ServiceExceptionDefinition(10000, "系统未知异常");

    public static final ServiceExceptionDefinition LAUNCHER_USER_NOT_LOGIN =
            new ServiceExceptionDefinition(10001, "用户尚未登录");

    public static final ServiceExceptionDefinition LAUNCHER_PARAM_CHECK_FAILED =
            new ServiceExceptionDefinition(10002, "参数校验失败");

    public static final ServiceExceptionDefinition PARAM_CHECK_FAILED =
            new ServiceExceptionDefinition(10002, "参数校验失败");

    public static final ServiceExceptionDefinition LAUNCHER_NUMBER_PARSE_ERROR =
            new ServiceExceptionDefinition(10002, "数字类型解析失败");

    public static final ServiceExceptionDefinition LAUNCHER_DATE_PARSE_ERROR =
            new ServiceExceptionDefinition(10002, "日期解析失败");

    public static final ServiceExceptionDefinition LAUNCHER_CONTENT_TYPE_NOT_SUPPORT =
            new ServiceExceptionDefinition(10002, "Http ContentType不支持");

    public static final ServiceExceptionDefinition LAUNCHER_READ_FILE_JUST_SUPPORT_MULTIPART =
            new ServiceExceptionDefinition(10002, "请使用文件上传格式报文上传文件");

    public static final ServiceExceptionDefinition LAUNCHER_API_NOT_EXISTS =
            new ServiceExceptionDefinition(10003, "API不存在");

    public static final ServiceExceptionDefinition LAUNCHER_IO_EXCEPTION =
            new ServiceExceptionDefinition(10004, "Http IO异常");

    public static final ServiceExceptionDefinition LAUNCHER_SYSTEM_BUSY =
            new ServiceExceptionDefinition(10005, "系统繁忙～");

    public static final ServiceExceptionDefinition LAUNCHER_ADMIN_NOT_LOGIN =
            new ServiceExceptionDefinition(10006, " 管理员尚未登录");

    public static final ServiceExceptionDefinition LAUNCHER_ADMIN_PERMISSION_DENY =
            new ServiceExceptionDefinition(10007, "管理员权限不足");

    public static final ServiceExceptionDefinition LAUNCHER_GET_IP_FAILED =
            new ServiceExceptionDefinition(10008, "获取用户IP失败，请联系管理员");

    public static final ServiceExceptionDefinition LAUNCHER_RPC_SIGN_INCORRECT =
            new ServiceExceptionDefinition(10009, "远程调用签名错误");


    public static ServiceExceptionDefinition buildVariableException(ServiceExceptionDefinition definition, String ...args) {
        String msg = definition.getMsg();
        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("${" + i + "}", args[i]);
        }
        return new ServiceExceptionDefinition(definition.getCode(), msg);
    }




}
