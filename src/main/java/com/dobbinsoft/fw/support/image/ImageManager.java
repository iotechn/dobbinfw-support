package com.dobbinsoft.fw.support.image;

import com.dobbinsoft.fw.core.enums.BaseEnums;
import com.dobbinsoft.fw.support.model.Page;
import com.dobbinsoft.fw.support.storage.StorageClient;

/**
 * 图片管理器
 * 管理图片对象存储中的图片
 *
 * 依赖：
 * 1. StorageClient 为其提供 物理删除，转储上传 功能
 * 2. 依赖一个 com.dobbinsoft.fw.core.enums.BaseEnums 其实现为为图片的业务类型枚举。用于搜索分组。
 * 3. 依赖图片基础路径BaseUrl，为智能图片转储提供是否转储的依据
 *
 * ImageManager 需要自己去管理图片已经存储的图片， 包括图片的标题、路径、类型、关联类型等。
 *
 * 提供：
 * 1. 对外提供，查询图片，搜索图片的功能
 * 2. 对外提供，智能 图片转存功能，（不支持防盗链）。若目标图片存在防盗链，则直接返回503。
 * 3.
 */
public interface ImageManager {

    /**
     * 获取图片类型分组
     * @return
     */
    public Class<? extends BaseEnums> getImageBizTypes();

    /**
     * 获取图片列表
     * @param imageName 搜索图片名
     * @param bizType 图片类型
     * @param page 页码
     * @param pageSize 页码长度
     * @return
     */
    public Page<ImageModel> getImageList(String imageName, Integer bizType, Integer page, Integer pageSize);

    /**
     * 删除某张图片
     * @param url
     * @return
     */
    public boolean delete(String url);

    /**
     * 以URL作为Key。
     * 若图片存在，则更新，不存在，则以匿名的方式（bizId = 0) 插入
     * @param model
     * @return
     */
    public boolean upsertImg(ImageModel model);

}
