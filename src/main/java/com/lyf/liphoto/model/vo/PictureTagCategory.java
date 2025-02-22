package com.lyf.liphoto.model.vo;

import lombok.Data;

import java.util.List;

/**
 * ClassName:PictureTagCategory
 * Package: com.lyf.liphoto.model.dto.picture
 * Description:
 *
 * @Author 黎云锋
 * @Create 2025/2/22 15:55
 */
@Data
public class PictureTagCategory {
    /**
     * 标签类
     */
    List<String> tagList;
    /**
     * 分类
     */
    List<String> categoryList;
}
