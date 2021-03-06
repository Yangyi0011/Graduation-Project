package com.yangyi.graduationproject.service;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.entities.Permission;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

public interface PermissionService {
    /**
     * 通过id获取权限
     * @param id：权限id
     * @return 返回对应的权限对象
     */
    public Permission getPermissionById(String id);

    /**
     * 获取所有权限对象，不分页
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回权限对象集合
     */
    public List<Permission> getPermissions(Map<String, Object> condition);

    /**
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回权限对象集合
     */
    public List<Permission> getPermissions(Integer currentPage, Integer rows, Map<String, Object> condition);

    /**
     * 通过角色id 获取指定 角色 所含有的全部权限
     * @param roleId 角色id
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     */
    public List<Permission> getPermissionsByRoleId(String roleId, Map<String, Object> condition);

    /**
     * 通过id 获取指定 角色 所含有的全部权限
     *
     * @param roleId：角色id
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回该角色所拥有的所有权限，支持分页、模糊查询
     */
    public List<Permission> getPermissionsByRoleId(String roleId, Integer currentPage, Integer rows, Map<String, Object> condition);

    /**
     * 通过 角色名称 获取指定 角色 所含有的全部权限
     * @param roleName 角色名称
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return
     */
    public List<Permission> getPermissionsByRoleName(String roleName, Map<String, Object> condition);

    /**
     * 通过 角色名称 获取指定 角色 所含有的全部权限
     *
     * @param roleName：角色名称
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回该角色所拥有的所有权限，支持分页、模糊查询
     */
    public List<Permission> getPermissionsByRoleName(String roleName, Integer currentPage, Integer rows, Map<String, Object> condition);

    /**
     * 权限添加方法
     * @param permission：要添加的权限
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer add(@Validated(value = {TC_Add.class}) Permission permission);

    /**
     * 权限更新方法
     * @param permission: 要更新的权限
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer update(@Validated(value = {TC_Update.class}) Permission permission);

    /**
     * 权限删除方法
     * @param id：权限id
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer delete(String id);

    /**
     * 批量删除
     *
     * @param ids：需要删除的对象的id集
     * @return 返回操作结果（true：删除成功，false：删除失败）
     * 添加事务，保证中间删除失败时可以回滚
     */
    public Integer batchDelete(String ids);
}
