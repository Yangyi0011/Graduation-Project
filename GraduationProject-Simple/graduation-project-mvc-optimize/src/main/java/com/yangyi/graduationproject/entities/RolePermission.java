package com.yangyi.graduationproject.entities;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Delete;
import com.yangyi.graduationproject.controller.validation.TC_Find;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 角色-权限对应信息维护类，对应数据（tab_role_permission）表
 */
@Data
@EqualsAndHashCode
@Accessors
public class RolePermission implements Serializable{
    /**
     * 表id，对应数据库（id）字段
     */
    @NotBlank(message = "id不能为空",groups = {TC_Delete.class,TC_Find.class})
    private String id;

    /**
     * 角色id，对应数据库（role_id）字段
     */
    @NotBlank(message = "授权角色id不能为空",groups = {TC_Add.class, TC_Update.class})
    private String roleId;

    /**
     * 权限id，对应数据库（permission_id）字段
     */
    @NotBlank(message = "被授权限id不能为空",groups = {TC_Add.class, TC_Update.class})
    private String permissionId;
}