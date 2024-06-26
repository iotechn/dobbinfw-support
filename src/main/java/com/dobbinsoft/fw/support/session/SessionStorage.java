package com.dobbinsoft.fw.support.session;

import com.dobbinsoft.fw.core.entiy.inter.IdentityOwner;

/**
 * 存储一个Session
 */
public interface SessionStorage {

    /**
     * 保存Session
     * @param prefix 即是token的前缀，又是hash桶的名字
     * @param token
     * @param identityOwner
     * @param expire
     */
    public void save(String prefix, String token, IdentityOwner identityOwner, Integer expire);


    /**
     * 刷新session值
     * @param identityOwner
     */
    public void refresh(String prefix, IdentityOwner identityOwner);

    /**
     * 获取session对象（通过Token）
     * @param token
     * @param clazz
     * @return
     * @param <T>
     */
    public <T extends IdentityOwner> T get(String prefix, String token, Class<T> clazz);

    /**
     * 获取session对象（通过唯一ID）
     * @param prefix
     * @param id
     * @param clazz
     * @return
     * @param <T>
     */
    public <T extends IdentityOwner> T get(String prefix, Long id, Class<T> clazz);

    /**
     * 续约时间
     * @param prefix
     * @param token
     * @param expire
     */
    public void renew(String prefix, String token, Integer expire);

    /**
     * 登出
     * @param prefix
     * @param token
     */
    public void logout(String prefix, String token);


    /**
     * 登出所有
     * @param prefix
     * @param id
     */
    public void logoutAll(String prefix, Long id);

}
