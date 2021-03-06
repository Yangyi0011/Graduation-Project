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

@Data
@EqualsAndHashCode(callSuper=true)
@Accessors
public class Resource extends BaseEntity implements Serializable {
    /**
     * 主键ID，对应数据库（id）字段
     */
    @NotBlank(message = "id不能为空",groups = {TC_Update.class,TC_Delete.class,TC_Find.class})
    private String id;

    /**
     * 资源名称，对应数据库（name）字段
     */
    @NotBlank(message = "资源名称不能为空",groups = {TC_Add.class, TC_Update.class})
    private String name;

    /**
     * 资源对应URL，对应数据库（url）字段
     */
    private String url;

    /**
     * 资源使用状态，对应数据库（state）字段
     * （1：已启用，0：已禁用，-1：已删除）
     */
    @NotNull(message = "使用状态不能为空",groups = {TC_Add.class, TC_Update.class})
    @Range(min = -1,max = 1,message = "用户使用状态范围只能在-1~1",groups = {TC_Update.class})
    private Integer state;

    /**
     * 该资源的父级菜单ID，对应数据库（parent_id）字段
     * 若父级菜单ID为自己，则该资源为顶级菜单,默认为自己
     */
    @NotNull(message = "父页面id不能为空",groups = {TC_Add.class, TC_Update.class})
    private String parentId;

    /**
     * 资源菜单排序序号，对应数据库（order_number）字段
     */
    @NotNull(message = "菜单顺序不能为空",groups = {TC_Add.class, TC_Update.class})
    private Integer orderNumber;

    /**
     * 资源菜单所处层级，对应数据库（level）字段
     */
    private Integer level;

    /**
     * 菜单图标样式，对应数据库（icon_style）字段
     */
    private String iconStyle;

    /**
     * 资源描述，对应数据库（describe）字段
     */
    private String describe;
}