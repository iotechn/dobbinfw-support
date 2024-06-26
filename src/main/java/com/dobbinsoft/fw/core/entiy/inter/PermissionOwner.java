package com.dobbinsoft.fw.core.entiy.inter;

import java.util.List;

/**
 * ClassName: PermissionOwner
 * Description: 表示一个有权限的身份
 */
public interface PermissionOwner extends IdentityOwner {

    public List<String> getPerms();

}
