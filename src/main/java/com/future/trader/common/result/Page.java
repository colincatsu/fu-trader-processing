package com.future.trader.common.result;


import lombok.Data;

import java.io.Serializable;

/**
 * 自定义分页bean
 *
 * @author Admin
 * @version: 1.0
 */
@Data
public class Page implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 总记录数
     */
    private long total;
    /**
     * 总页数
     */
    private long totalPages;
    /**
     * 当前页数量
     */
    private int pageNo;
    /**
     * 当前页数量
     */
    private int pageSize;
    /**
     * 每页数量
     */
    private int size;

}