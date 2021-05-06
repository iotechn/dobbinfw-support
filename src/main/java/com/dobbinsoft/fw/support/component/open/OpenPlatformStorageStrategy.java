package com.dobbinsoft.fw.support.component.open;

import com.dobbinsoft.fw.support.component.open.model.OPClient;
import com.dobbinsoft.fw.support.component.open.model.OPClientPermission;
import com.dobbinsoft.fw.support.component.open.model.OPNotify;

import java.util.List;

/**
 * ClassName: OpenPlatformStorageStrategy
 * Description: 持久化策略
 *
 * @author: e-weichaozheng
 * @date: 2021-04-23
 */
public interface OpenPlatformStorageStrategy {

    public boolean checkClientExists(String clientCode);

    /**
     * 初始化客户，并产生 服务器公私钥，并将公钥返回
     * @param clientCode
     * @param notifyUrl
     * @param publicKey
     * @return
     */
    public OPClient initClient(String clientCode, String notifyUrl, String publicKey);

    public boolean setClientNotifyUrl(String clientCode, String notifyUrl);

    public boolean setClientPublicKey(String clientCode, String publicKey);

    public OPClient getClient(String clientCode);

    public boolean setClientPermissionList(String clientCode, List<OPClientPermission> permissionList);

    public boolean checkApiPermission(String clientCode, String group, String method);

    /**
     * 带服务器公私钥获取客户
     * @param clientCode
     * @return
     */
    public OPClient getClientWithPK(String clientCode);

    /**
     * 将通知实体缓存起来
     * @param notify
     * @return
     */
    public String storeNotify(OPNotify notify);

    /**
     * 获取需要通知的实体
     * @return
     */
    public List<OPNotify> getNeedNotify();

    /**
     *
     * @param updateNotify
     * @return
     */
    public boolean updateNotify(OPNotify updateNotify);

}
