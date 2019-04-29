package com.yangyi.graduationproject.service.impl;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.dao.ResourceMapper;
import com.yangyi.graduationproject.entities.Permission;
import com.yangyi.graduationproject.entities.Resource;
import com.yangyi.graduationproject.entities.Role;
import com.yangyi.graduationproject.exception.DeleteException;
import com.yangyi.graduationproject.exception.InsertException;
import com.yangyi.graduationproject.exception.UpdateException;
import com.yangyi.graduationproject.service.ResourceService;
import com.yangyi.graduationproject.utils.StringUtil;
import com.yangyi.graduationproject.utils.SysResourcesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("resourceService")
public class ResourceServiceImpl implements ResourceService {
    @Qualifier("resourceMapper")
    @Autowired
    ResourceMapper resourceMapper;

    /**
     * 通过 id 获取资源信息
     * @param id ：资源ID
     * @return 返回资源对象
     */
    @Override
    public Resource getResourceById(String id) {
        return StringUtil.isEmpty(id) ? null : resourceMapper.getResourceById(id);
    }

    /**
     * 获取所有资源对象，只获取当前用户有权限访问的资源对象
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回资源对象集合
     */
    @Override
    public List<Resource> getResources(Map<String,Object> condition) {
        return getResources(null, null, condition);
    }

    /**
     * 获取当前用户有权访问的资源集合，用来构建树形菜单
     * @return
     */
    @Override
    public Set<Resource> getResourcesForUser(){
        // 1、获取认证对象的信息
        Authentication authentication = SysResourcesUtils.getAuthentication();
        // 2、获得loadUserByUsername()中注入的角色
        Collection<Role> roles = (Collection<Role>) authentication.getAuthorities();

        boolean isAdmin = false;
        for (Role r: roles) {
            if (r.getName().contains("ADMIN")){
                isAdmin = true;
                break;
            }
        }

        //3、遍历所有角色，获取每个角色所含有的所有权限
        Set<Resource> resourceSet = Collections.synchronizedSet(new HashSet<>());
        if (isAdmin){
            //为超级管理员查询所有数据
            Map<String, Object> condition = new ConcurrentHashMap<>();
            condition.put("state",1);
            resourceSet.addAll(resourceMapper.getResources(null,null,condition));
        }else {
            for (Role role:roles) {
                //获取到角色中的注入的所有权限
                Collection<Permission> permissions = Collections.synchronizedCollection(role.getPermissions());

                //4、遍历所有权限，提取每个权限对应的资源url
                for (Permission permission:permissions) {
                    //提取权限对应的资源
                    String resourceId = permission.getResourceId();
                    if (StringUtil.isNotEmpty(resourceId)){
                        //获取权限对应的资源
                        Resource resource = resourceMapper.getResourceById(resourceId);
                        if (resource != null && resource.getState() == 1){ //只获取已启用的资源对象
                            resourceSet.add(resource);
                        }
                    }
                }
            }
        }
        return resourceSet;
    }

    /**
     * 获取所有资源对象，支持分页
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回资源对象集合
     */
    @Override
    public List<Resource> getResources(Integer currentPage, Integer rows, Map<String,Object> condition) {
        if (currentPage != null && rows != null) {
            if (currentPage < 0 || rows < 0) {
                return null;
            } else {
                Integer start = (currentPage - 1) * rows;   //计算当前页的数据是从第几条开始查询
                return resourceMapper.getResources(start, rows, condition);
            }
        } else {
            return resourceMapper.getResources(null, null, condition);
        }
    }

    @Override
    public Integer add(@Validated(value = {TC_Add.class}) Resource resource) {
        InsertException insertException;
        if (resource == null){
            insertException = new InsertException("添加失败，添加对象不能为空");
            throw insertException;
        }
        Integer add = resourceMapper.add(resource);
        if (add == 0) {
            insertException = new InsertException("添加失败，请重试");
            throw insertException;
        }
        return add;
    }

    @Override
    public Integer update(@Validated(value = {TC_Update.class}) Resource resource) {
        UpdateException updateException;
        if (resource == null){
            updateException = new UpdateException("更新失败，请先选择更新对象");
            throw updateException;
        }
        Integer update = resourceMapper.update(resource);
        if (update == 0){
            updateException = new UpdateException("更新失败，请重试");
            throw updateException;
        }
        return update;
    }

    @Override
    public Integer delete(String id) {
        DeleteException deleteException;
        if (StringUtil.isEmpty(id)){
            deleteException = new DeleteException("删除失败，请先选择要删除的对象");
            throw deleteException;
        }

        //保证删除对象存在
        Resource resource = getResourceById(id);
        if (resource == null){
            deleteException = new DeleteException("删除失败，对象不存在或已被删除");
            throw deleteException;
        }

        //实行软删除
        resource.setState(-1);
        Integer delete = resourceMapper.update(resource);

        if (delete == 0){
            deleteException = new DeleteException("删除失败，请稍后重试");
            throw deleteException;
        }

        return delete;
    }

    /**
     * 批量删除
     *
     * @param ids：需要删除的对象的id集
     * @return 返回操作结果（1：删除成功，0：删除失败）
     * 添加事务，保证中间删除失败时可以回滚
     */
    @Transactional
    @Override
    public Integer batchDelete(String ids) {
        if (StringUtil.isEmpty(ids))
            return 0;
        String[] arr = ids.split(",");  //分割成数组
        for (String id : arr) {
            Integer res = this.delete(id);
            if (res == 0) {
                return 0;
            }
        }
        return 1;
    }
}