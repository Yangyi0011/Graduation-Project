package com.yangyi.graduationproject.service;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.entities.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ResourceService {
    /**
     * 通过 id 获取资源信息
     * @param id ：资源ID
     * @return 返回资源对象
     */
    public Resource getResourceById(String id);

    /**
     * 获取所有资源对象，不分页，只获取当前用户有权限访问的资源对象
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回资源对象集合
     */
    public List<Resource> getResources(Map<String, Object> condition);

    /**
     * 获取当前用户有权访问的资源集合，用来构建树形菜单
     * @return
     */
    public Set<Resource> getResourcesForUser();

    /**
     * 获取所有资源对象，支持分页
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回资源对象集合
     */
    public List<Resource> getResources(Integer currentPage, Integer rows, Map<String, Object> condition);

    /**
     * 添加方法
     */
    public Integer add(@Validated(value = {TC_Add.class}) Resource resource);

    /**
     * 更新方法
     */
    public Integer update(@Validated(value = {TC_Update.class}) Resource resource);

    /**
     * 删除方法
     */
    public Integer delete(String id);

    /**
     * 批量删除
     *
     * @param ids：需要删除的对象的id集
     * @return 返回操作结果（1：删除成功，0：删除失败）
     * 添加事务，保证中间删除失败时可以回滚
     */
    public Integer batchDelete(String ids);
}
