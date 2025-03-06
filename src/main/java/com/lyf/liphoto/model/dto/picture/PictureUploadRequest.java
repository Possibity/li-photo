package com.lyf.liphoto.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName:PictureUploadRequest
 * Package: com.lyf.liphoto.model.dto.picture
 * Description:
 *  用于接收请求参数的类，由于图片需要支持重复上传（基础信息不变，只改变图片文件）
 * @Author 黎云锋
 * @Create 2025/2/20 23:55
 */
@Data
public class PictureUploadRequest implements Serializable {
    /**
     * 图片id（用于修改）
     */
    private Long id;
    /**
     * 文件地址
     */
    private String fileUrl;
    private static final long serialVersionUID=1L;
}
