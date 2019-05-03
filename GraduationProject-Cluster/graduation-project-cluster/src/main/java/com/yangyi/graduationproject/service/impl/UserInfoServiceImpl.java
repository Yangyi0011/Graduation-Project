package com.yangyi.graduationproject.service.impl;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.dao.UserInfoMapper;
import com.yangyi.graduationproject.dao.UserMapper;
import com.yangyi.graduationproject.entities.User;
import com.yangyi.graduationproject.entities.UserInfo;
import com.yangyi.graduationproject.exception.DeleteException;
import com.yangyi.graduationproject.exception.FindException;
import com.yangyi.graduationproject.service.UserInfoService;
import com.yangyi.graduationproject.utils.LogUtil;
import com.yangyi.graduationproject.utils.StringUtil;
import com.yangyi.graduationproject.utils.SysResourcesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {
    @Qualifier(value = "userInfoMapper")
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Qualifier(value = "userMapper")
    @Autowired
    private UserMapper userMapper;

    private static final String USER_INFO_CACHE="userInfo";
    private static final String USER_INFOS_CACHE="userInfos";

    /**
     * 通过id获取用户信息
     *
     * @param id
     * @return
     */
    @Cacheable(value = USER_INFO_CACHE, key = "#id")
    @Override
    public UserInfo getUserInfoById(String id) {
        return StringUtil.isNotEmpty(id) ? userInfoMapper.getUserInfoById(id) : null;
    }

    /**
     * 通过userId获取用户信息
     * @param userId
     * @return
     */
    @Cacheable(value = USER_INFO_CACHE)
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        if (StringUtil.isEmpty(userId)) return null;
        User user = userMapper.getUserById(userId);
        return user != null?userInfoMapper.getUserInfoByUsername(user.getUsername()) : null;
    }

    /**
     * 通过username获取用户信息
     * @param username
     * @return
     */
    @Cacheable(value = USER_INFO_CACHE, key = "#username")
    @Override
    public UserInfo getUserInfoByUsername(String username) {
        return StringUtil.isNotEmpty(username) ? userInfoMapper.getUserInfoByUsername(username) : null;
    }

    /**
     * 获取所有用户数据，不分页
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回所有数据
     */
    @Override
    public List<UserInfo> getUserInfos(Map<String, Object> condition) {
        return this.getUserInfos(null,null,condition);
    }

    /**
     * 分页获取所有用户
     *
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回当前页的数据集合
     */
    @Cacheable(value = USER_INFOS_CACHE)
    @Override
    public List<UserInfo> getUserInfos(Integer currentPage, Integer rows, Map<String,Object> condition) {
        FindException findException;

        if (currentPage != null && rows != null) {
            if (currentPage < 0 || rows < 0) {
                findException = new FindException("分页查询不能的页数或查询数量小于0");
                LogUtil.info(this, "查询用户登录信息", "查询失败，分页查询不能的页数或查询数量小于0");
                throw findException;
            } else {
                Integer start = (currentPage - 1) * rows;   //计算当前页的数据是从第几条开始查询
                return userInfoMapper.getUserInfos(start, rows, condition);
            }
        } else {
            return userInfoMapper.getUserInfos(null, null, condition);
        }
    }

    /**
     * 用户添加方法
     *
     * @param user：用户对象
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    @CacheEvict(value = USER_INFOS_CACHE, allEntries = true)
    @Override
    public Integer add(@Validated(value = {TC_Add.class}) UserInfo user) {
        return user == null ? 0 : userInfoMapper.add(user);
    }

    /**
     * 用户更新方法
     *
     * @param userInfo：用户信息对象
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    @CacheEvict(value = USER_INFO_CACHE, key = "#userInfo.id")
    @Override
    public Integer update(@Validated(value = {TC_Update.class}) UserInfo userInfo) {
        return userInfo == null ? 0 : userInfoMapper.update(userInfo);
    }

    /**
     * 用户删除方法
     *
     * @param id：用户id
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    @CacheEvict(value = {USER_INFO_CACHE,USER_INFOS_CACHE},allEntries = true)
    @Override
    public Integer delete(String id) throws DeleteException {
        return StringUtil.isEmpty(id)?0:userInfoMapper.delete(id);
    }

    /**
     * 批量删除
     *
     * @param ids：需要删除的对象的id集
     * @return 返回操作结果状态（1：成功，0：失败）
     */
    @Transactional
    @Override
    public Integer batchDelete(String ids){
        if (StringUtil.isEmpty(ids))
            return 0;

        //获取当前登录用户
        String currentUsername = SysResourcesUtils.getCurrentUsername();
        LogUtil.info(this, "用户信息批量删除", "用户：【" + currentUsername + "】正在批量删除IDS为：【" + ids + "】的用户信息");

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