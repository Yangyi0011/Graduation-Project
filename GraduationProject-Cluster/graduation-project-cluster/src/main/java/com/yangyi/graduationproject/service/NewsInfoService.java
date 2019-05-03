package com.yangyi.graduationproject.service;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.entities.NewsContent;
import com.yangyi.graduationproject.entities.NewsInfo;
import com.yangyi.graduationproject.exception.DeleteException;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

public interface NewsInfoService {
    /**
     * 通过id获取新闻信息
     * @param id：新闻信息id
     * @return 返回对应的新闻信息对象
     */
    public NewsInfo getNewsInfoById(Integer id);


    /**
     * 获取当前查询条件下的数据条数
     * @param condition：自定义查询条件
     * @return ：数据条数
     */
    public Long count(Map<String, Object> condition);

    /**
     * 获取所有资源对象
     * @return 返回资源对象集合
     */
    public List<NewsInfo> getNewsInfos(Map<String, Object> condition);

    /**
     * 分页获取所以新闻信息
     *
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：模糊查询内容
     * @return 返回当前页的数据集合
     */
    public List<NewsInfo> getNewsInfos(Integer currentPage, Integer rows, Map<String, Object> condition);

    /**
     * 新闻信息添加方法
     * @param newsInfo：要添加的新闻信息
     * @param newsContent：要添加的新闻内容
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    public Integer add(@Validated(value = {TC_Add.class}) NewsInfo newsInfo, @Validated(value = {TC_Add.class}) NewsContent newsContent);

    /**
     * 新闻信息更新方法
     * @param newsInfo:要更新的新闻信息
     * @param newsContent:要更新的新闻内容
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    public Integer update(@Validated(value = {TC_Update.class}) NewsInfo newsInfo, @Validated(value = {TC_Update.class}) NewsContent newsContent);

    /**
     * 动态培训删除方法
     *
     * @param id：动态培训id
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer delete(Integer id) throws DeleteException;

    /**
     * 批量删除
     *
     * @param ids：需要删除的对象的id集
     * @return 返回操作结果（true：删除成功，false：删除失败）
     * 添加事务，保证中间删除失败时可以回滚
     */
    public boolean batchDelete(String ids);
    /**
     * 通过多个动态培训ID去查询符合条件的动态培训集
     * @param newsInfoIds：多个动态培训ID
     * @return 返回动态培训集合
     */
}
