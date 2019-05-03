package com.yangyi.graduationproject.service.impl;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.dao.NewsContentMapper;
import com.yangyi.graduationproject.entities.NewsContent;
import com.yangyi.graduationproject.service.NewsContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/4/24
 * Time: 16:24
 */
@Service("newsContentService")
public class NewsContentServiceImpl implements NewsContentService {

    @Qualifier("newsContentMapper")
    @Autowired
    private NewsContentMapper newsContentMapper;

    private static final String NEWS_CONTENT = "newsContent";
    private static final String NEWS_CONTENTS = "newsContents";

    /**
     * 通过id获取新闻内容信息
     * @param id：新闻内容id
     * @return
     */
    @Cacheable(value = NEWS_CONTENT,key = "#id")
    @Override
    public NewsContent getNewsContentById(Integer id) {
        return id==null?null:newsContentMapper.getNewsContentById(id);
    }

    /**
     * 新闻内容添加方法
     *
     * @param newsContent：新闻内容对象
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    @CacheEvict(value = {NEWS_CONTENTS},allEntries = true)
    @Override
    public Integer add(@Validated(value = {TC_Add.class}) NewsContent newsContent) {
        return newsContent==null?0:newsContentMapper.add(newsContent)==null?0:1;
    }

    /**
     * 新闻内容添加方法
     *
     * @param newsContent：新闻内容对象
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    @CacheEvict(value = {NEWS_CONTENT},key = "#newsContent.id")
    @Override
    public Integer update(@Validated(value = {TC_Update.class}) NewsContent newsContent) {
        return newsContent==null?0:newsContentMapper.update(newsContent);
    }

    /**
     * 新闻内容删除方法
     *
     * @param id：新闻内容id
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    @CacheEvict(value = {NEWS_CONTENT,NEWS_CONTENTS},allEntries = true)
    @Override
    public Integer delete(Integer id) {
        return id==null?0:newsContentMapper.delete(id);
    }
}