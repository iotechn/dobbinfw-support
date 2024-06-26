package com.dobbinsoft.fw.core.util;

import com.dobbinsoft.fw.core.entiy.inter.CustomAccountOwner;
import com.dobbinsoft.fw.core.entiy.inter.IdentityOwner;
import com.dobbinsoft.fw.core.entiy.inter.PermissionOwner;
import com.dobbinsoft.fw.core.exception.ServiceException;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SessionUtil is a utility class that manages user and admin sessions, and provides
 * functionality to handle custom account owners. It supports setting and getting
 * session information in a thread-safe manner using ThreadLocal variables.
 *
 * @param <U> the type of user which extends IdentityOwner
 * @param <A> the type of admin which extends PermissionOwner
 *
 * Created by rize on 2019/2/27.
 * Edited by rize on 2021/3/16.
 */
public class SessionUtil<U extends IdentityOwner, A extends PermissionOwner> implements ISessionUtil<U, A> {

    private ThreadLocal<U> userLocal = new ThreadLocal<>();

    private ThreadLocal<A> adminLocal = new ThreadLocal<>();

    /**
     * -- GETTER --
     *  Gets the user class type.
     *
     * @return the user class type
     */
    @Getter
    private Class<U> userClass;

    /**
     * -- GETTER --
     *  Gets the admin class type.
     *
     * @return the admin class type
     */
    @Getter
    private Class<A> adminClass;

    private final Map<Class<?>, ThreadLocal<Object>> customLocalMap = new HashMap<>();

    /**
     * Constructs a SessionUtil instance with specified user and admin classes,
     * and optionally custom classes for custom account owners.
     *
     * @param userClass the class type of the user
     * @param adminClass the class type of the admin
     * @param customClass additional custom classes for custom account owners
     */
    public SessionUtil(Class<U> userClass, Class<A> adminClass, Class<?>... customClass) {
        this.userClass = userClass;
        this.adminClass = adminClass;
        for (Class<?> clazz : customClass) {
            customLocalMap.put(clazz, new ThreadLocal<>());
        }
    }

    /**
     * Sets the current user session.
     *
     * @param userDTO the user session object
     */
    public void setUser(U userDTO) {
        userLocal.set(userDTO);
    }

    /**
     * Gets the current user session.
     *
     * @return the current user session object
     */
    public U getUser() {
        return userLocal.get();
    }

    /**
     * Sets the current admin session.
     *
     * @param adminDTO the admin session object
     */
    public void setAdmin(A adminDTO) {
        adminLocal.set(adminDTO);
    }

    /**
     * Gets the current admin session.
     *
     * @return the current admin session object
     */
    public A getAdmin() {
        return adminLocal.get();
    }

    /**
     * Sets a custom account owner session.
     *
     * @param obj the custom account owner session object
     */
    @Override
    public void setCustom(CustomAccountOwner obj) {
        Class<?> clazz = obj.getClass();
        this.customLocalMap.get(clazz).set(obj);
    }

    /**
     * Gets the custom account owner session.
     *
     * @param clazz the class type of the custom account owner
     * @param <T>   the type of the custom account owner
     * @return the custom account owner session object
     */
    @Override
    public <T extends CustomAccountOwner> T getCustom(Class<T> clazz) {
        return (T) this.customLocalMap.get(clazz).get();
    }

    /**
     * Checks if the admin has a specific permission.
     *
     * @param permission the permission string to check
     * @return true if the admin has the permission, false otherwise
     * @throws ServiceException if an error occurs while checking the permission
     */
    public boolean hasPerm(String permission) throws ServiceException {
        List<String> perms = getAdmin().getPerms();
        boolean hasPerm = false;
        String[] permissions = permission.split(":");
        outer:
        for (String item : perms) {
            String[] hasPer = item.split(":");
            inner:
            for (int i = 0; i < permissions.length; i++) {
                if ("*".equals(hasPer[i])) {
                    hasPerm = true;
                    break outer;
                } else if (hasPer[i].equals(permissions[i])) {
                    if (i == permissions.length - 1) {
                        hasPerm = true;
                    }
                } else {
                    break inner;
                }
            }
        }
        return hasPerm;
    }

    /**
     * Clears all session information for the current thread.
     */
    @Override
    public void clear() {
        this.userLocal.remove();
        this.adminLocal.remove();
        this.customLocalMap.forEach((k, v) -> v.remove());
    }
}