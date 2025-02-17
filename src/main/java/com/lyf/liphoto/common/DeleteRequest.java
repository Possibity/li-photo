package com.lyf.liphoto.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除id数据请求类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
