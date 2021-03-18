package com.dobbinsoft.fw.support.service;

import com.dobbinsoft.fw.core.entiy.inter.IdentityOwner;
import com.dobbinsoft.fw.core.entiy.inter.PermissionOwner;
import com.dobbinsoft.fw.core.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ClassName: BaseService
 * Description: 基础服务类，用于统一扩展
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
public class BaseService<U extends IdentityOwner, A extends PermissionOwner> {

    @Autowired
    protected SessionUtil<U, A> sessionUtil;

}
