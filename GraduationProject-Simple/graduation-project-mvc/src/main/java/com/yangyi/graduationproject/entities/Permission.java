package com.yangyi.graduationproject.entities;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Delete;
import com.yangyi.graduationproject.controller.validation.TC_Find;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 权限表，对应数据库（tab_permission）
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Accessors
public class Permission extends BaseEntity implements Serializable {
    /**
     * 操作常量
     */
    public static final String CREATE = "CREATE";
    public static final String READ = "READ";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";

    /**
     * id,对应数据库（id）字段
     */
    @NotBlank(message = "id不能为空",groups = {TC_Update.class,TC_Delete.class,TC_Find.class})
    private String id;

    /**
     * 权限名称,对应数据库（name）字段
     */
    @NotBlank(message = "权限名称不能为空",groups = {TC_Add.class, TC_Update.class})
    private String name;

    /**
     * 权限对应资源ID，对应数据库（resource_id）字段
     */
    @NotBlank(message = "权限对应资源不能为空",groups = {TC_Add.class, TC_Update.class})
    private String resourceId;

    /**
     * 对某资源含有的操作权限（CREATE、READ、UPDATE、DELETE），各个操作间用英文逗号分分隔
     * 对应数据库字段（permission）
     */
    private String operations;

    /**
     * 使用状态，对应数据库（state）字段
     * （1：已启用，0：已禁用，-1：已删除）
     */
    @NotNull(message = "使用状态不能为空",groups = {TC_Add.class, TC_Update.class})
    @Range(min = -1,max = 1,message = "用户使用状态范围只能在-1~1",groups = {TC_Update.class})
    private Integer state;

    /**
     * 权限描述,对应数据库（describe）字段
     */
    private String describe;
}