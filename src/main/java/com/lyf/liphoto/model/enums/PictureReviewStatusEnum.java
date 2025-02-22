package com.lyf.liphoto.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * ClassName:PictureReviewStatusEnum
 * Package: com.lyf.liphoto.model.enums
 * Description:
 *
 * @Author 黎云锋
 * @Create 2025/2/22 21:22
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核",0),
    PASS("通过",1),
    REJECT("拒绝",2);

    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }
    /**
     * 根据value获取枚举
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value){
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        for(PictureReviewStatusEnum pictureReviewStatusEnum:PictureReviewStatusEnum.values()){
            if(pictureReviewStatusEnum.value==value){
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
