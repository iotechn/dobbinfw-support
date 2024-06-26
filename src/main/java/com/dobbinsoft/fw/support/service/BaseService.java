package com.dobbinsoft.fw.support.service;

import com.dobbinsoft.fw.core.entiy.inter.IdentityOwner;
import com.dobbinsoft.fw.core.entiy.inter.PermissionOwner;
import com.dobbinsoft.fw.core.util.ISessionUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ClassName: BaseService
 * Description: 基础服务类，用于统一扩展
 * @param <U> 前台用户类型
 * @param <A> 后台用户类型
 */
public class BaseService<U extends IdentityOwner, A extends PermissionOwner> {

    @Autowired(required = false)
    protected ISessionUtil<U, A> sessionUtil;

}
