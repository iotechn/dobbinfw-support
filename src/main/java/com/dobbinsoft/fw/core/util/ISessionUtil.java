package com.dobbinsoft.fw.core.util;

import com.dobbinsoft.fw.core.entiy.inter.CustomAccountOwner;
import com.dobbinsoft.fw.core.entiy.inter.IdentityOwner;
import com.dobbinsoft.fw.core.entiy.inter.PermissionOwner;
import com.dobbinsoft.fw.core.exception.ServiceException;

/**
 * An interface for session-related operations, providing methods to set and get user and admin information,
 * manage custom account owners, and handle permissions.
 *
 * @param <U> the type of the user, extending {@link IdentityOwner}
 * @param <A> the type of the admin, extending {@link PermissionOwner}
 */
public interface ISessionUtil<U extends IdentityOwner, A extends PermissionOwner> {

    /**
     * Sets the user information in the session.
     *
     * @param userDTO the user object to be set in the session
     */
    void setUser(U userDTO);

    /**
     * Retrieves the user information from the session.
     *
     * @return the user object from the session
     */
    U getUser();

    /**
     * Sets the admin information in the session.
     *
     * @param adminDTO the admin object to be set in the session
     */
    void setAdmin(A adminDTO);

    /**
     * Retrieves the admin information from the session.
     *
     * @return the admin object from the session
     */
    A getAdmin();

    /**
     * Sets a custom account owner object in the session.
     *
     * @param obj the custom account owner object to be set in the session
     */
    void setCustom(CustomAccountOwner obj);

    /**
     * Retrieves a custom account owner object from the session.
     *
     * @param <T> the type of the custom account owner, extending {@link CustomAccountOwner}
     * @param clazz the class type of the custom account owner
     * @return the custom account owner object from the session
     */
    <T extends CustomAccountOwner> T getCustom(Class<T> clazz);

    /**
     * Gets the class type of the user.
     *
     * @return the class type of the user
     */
    Class<U> getUserClass();

    /**
     * Gets the class type of the admin.
     *
     * @return the class type of the admin
     */
    Class<A> getAdminClass();

    /**
     * Checks if the current session has the specified permission.
     *
     * @param permission the permission to check
     * @return true if the session has the specified permission, false otherwise
     * @throws ServiceException if an error occurs while checking the permission
     */
    boolean hasPerm(String permission) throws ServiceException;

    /**
     * Clears the session, removing all stored information.
     */
    void clear();
}
