package com.yangyi.graduationproject.entities;


import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * IP锁定类，对应数据库（tab_lock_ip）表，
 * 只要出现在该表内的IP都会被锁住
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Accessors
public class LockedIP extends BaseEntity implements Serializable{
    /**
     * 主键id，对应数据库（id）字段
     */
    private String id;

    /**
     * 被锁定IP地址，对应数据库（ip）
     */
    @NotBlank(message = "ip不能为空",groups = {TC_Add.class,TC_Update.class})
    private String ip;
}