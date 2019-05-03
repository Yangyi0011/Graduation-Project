package com.yangyi.graduationproject.entities;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据库公共字段
 */
@Data
@Accessors
public class BaseEntity implements Serializable {

    /**
     * 备注，对应数据库各表的（remarks）字段
     */
    protected String remarks;

    /**
     * 创建人ID，对应数据库各表的（create_user_id）字段
     */
    protected String createUserId;

    /**
     * 创建时间，对应数据库各表的（create_date）字段
     */
    protected Date createDate;

    /**
     * 更新人ID，对应数据库各表的（update_user_id）字段
     */
    protected String updateUserId;

    /**
     * 更新时间，对应数据库各表的（update_date）字段
     */
    protected Date updateDate;
}