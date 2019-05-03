package com.yangyi.graduationproject.service;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.entities.NewsContent;
import org.springframework.validation.annotation.Validated;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/4/24
 * Time: 16:22
 */
public interface NewsContentService {
    /**
     * 根据 id 获取新闻内容
     * @param id：新闻内容 id
     * @return 新闻内容对象
     */
    public NewsContent getNewsContentById(Integer id);

    /**
     * 新闻内容添加方法
     * @param newsContent 新闻内容
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer add(@Validated(value = {TC_Add.class}) NewsContent newsContent);

    /**
     * 新闻内容更新方法
     * @param newsContent：要更新的新闻内容
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer update(@Validated(value = {TC_Update.class}) NewsContent newsContent);

    /**
     * 新闻内容删除方法
     * @param id：新闻内容id
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer delete(Integer id);
}
