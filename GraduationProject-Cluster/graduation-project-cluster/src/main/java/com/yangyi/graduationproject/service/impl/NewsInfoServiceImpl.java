package com.yangyi.graduationproject.service.impl;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.dao.NewsContentMapper;
import com.yangyi.graduationproject.dao.NewsInfoMapper;
import com.yangyi.graduationproject.entities.NewsContent;
import com.yangyi.graduationproject.entities.NewsInfo;
import com.yangyi.graduationproject.exception.DeleteException;
import com.yangyi.graduationproject.exception.FindException;
import com.yangyi.graduationproject.exception.InsertException;
import com.yangyi.graduationproject.exception.UpdateException;
import com.yangyi.graduationproject.service.NewsInfoService;
import com.yangyi.graduationproject.utils.LogUtil;
import com.yangyi.graduationproject.utils.StringUtil;
import com.yangyi.graduationproject.utils.SysResourcesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Service("newsInfoService")
public class NewsInfoServiceImpl implements NewsInfoService {
    @Qualifier("newsInfoMapper")
    @Autowired
    private NewsInfoMapper newsInfoMapper;
    @Qualifier("newsContentMapper")
    @Autowired
    private NewsContentMapper newsContentMapper;

    private static final String NEWS_INFO_CACHE = "newsInfo";
    private static final String NEWS_INFOS_CACHE = "newsInfos";

    /**
     *通过id获取新闻信息
     * @param id：新闻信息id
     * @return 返回对应的新闻信息对象
     */
    @Cacheable(value = NEWS_INFO_CACHE, key = "#id") //当key不存在时，缓存在 newsInfo 中，key=#id
    @Override
    public NewsInfo getNewsInfoById(Integer id) {
        return id==null?null:newsInfoMapper.getNewsInfoById(id);
    }

    /**
     * 获取当前查询条件下的数据条数
     * @param condition：自定义查询条件
     * @return ：数据条数
     */
    @Cacheable(value = NEWS_INFO_CACHE) //当key不存在时，缓存在 newsInfo 中，key由SpringCache自动生成
    public Long count(Map<String, Object> condition){
        return newsInfoMapper.count(condition);
    }

    @Override
    public List<NewsInfo> getNewsInfos(Map<String,Object> condition) {
        return getNewsInfos(null, null, condition);
    }

    /**
     *分页获取所以新闻信息
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：模糊查询内容
     */
    @Cacheable(value = NEWS_INFOS_CACHE) //当key不存在时，缓存在 newsInfos 中，key由SpringCache自动生成
    @Override
    public List<NewsInfo> getNewsInfos(Integer currentPage, Integer rows, Map<String,Object> condition) {
            if(currentPage != null && rows != null) {
                if (currentPage < 0 || rows < 0) {
                    return null;
                }
                Integer start = (currentPage - 1) * rows;
                return newsInfoMapper.getNewsInfos(start, rows, condition);
            }
            else {
                return newsInfoMapper.getNewsInfos(null,null,condition);
            }
    }

    /**
     * 新闻信息添加方法
     * @param newsInfo：要添加的新闻信息
     * @param newsContent：要添加的新闻内容
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    //allEntries = true：方法执行时，清空 value 指定的缓存区中的所有缓存
    @CacheEvict(value= {NEWS_INFOS_CACHE},allEntries = true)
    @Transactional  //事务处理
    @Override
    public Integer add(@Validated(value = {TC_Add.class}) NewsInfo newsInfo, @Validated(value = {TC_Add.class})NewsContent newsContent) {
        int res = 0;
        InsertException insertException;
        if (newsContent == null || newsInfo == null){
            return res;
        }
        //添加后获取新添加的对象（包括自主id）
        res = newsContentMapper.add(newsContent);
        if (res != 0 && newsContent.getId() != null){

            //注入 新闻内容id
            newsInfo.setNewsContentId(newsContent.getId());
            res = newsInfoMapper.add(newsInfo);
        }else {
            insertException = new InsertException("添加失败，请稍后重试！");
            LogUtil.warn(this, "新闻信息添加失败", "添加失败，原因：新闻内容添加已成功，但获取对象为空。");
            throw insertException;
        }
        return res;
    }

    /**
     * 新闻信息更新方法
     * @param newsInfo:要更新的新闻信息
     * @param newsContent:要更新的新闻内容
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    //清除 newsInfo 中，key=#newsInfo.id的缓存
    @CacheEvict(value= NEWS_INFO_CACHE,key="#newsInfo.id")
    @Override
    @Transactional //事务处理
    public Integer update(@Validated(value = {TC_Update.class}) NewsInfo newsInfo, @Validated(value = {TC_Update.class}) NewsContent newsContent) {
        Integer res = 0;
        if (newsContent == null || newsInfo == null){
            return res;
        }

        res = newsContentMapper.update(newsContent);
        if (res > 0 ){
            res = newsInfoMapper.update(newsInfo);
        }else {
            LogUtil.warn(this, "新闻信息更新失败", "更新失败，原因：新闻内容更新失败。");
            throw new UpdateException("更新失败，请稍后重试！");
        }
        return res;
    }

    /**
     * 新闻信息删除方法
     * @param id：新闻信息id
     * @return 返回值大于0表示操作成功，否则操作失败
     * @throws DeleteException ：删除异常
     */
    //allEntries = true：方法执行时，清空 value 指定的缓存区的所有缓存
    @CacheEvict(value={NEWS_INFO_CACHE, NEWS_INFOS_CACHE},allEntries = true)
    @Override
    @Transactional  //事务处理
    public Integer delete(Integer id) throws DeleteException {
        Integer result = 0;
        if (id != null){
            NewsInfo newsInfo = this.getNewsInfoById(id);
            if (newsInfo != null){//确保要删除对象存在

                //先删除新闻内容
                result = newsContentMapper.delete(newsInfo.getNewsContentId());

                if (result > 0){
                    //再删除新闻信息
                    result = newsInfoMapper.delete(id);
                }else {
                    LogUtil.warn(this, "新闻信息删除失败", "删除失败，原因：新闻内容删除失败。");
                    throw new DeleteException("删除失败，请稍后重试！");
                }
            }else {
                throw new DeleteException("删除失败，对象不存在或已被删除！");
            }
        }else {
            throw new DeleteException("删除失败，删除对象id不能为空！");
        }
        return result;
    }

    /**
     * 批量删除
     *
     * @param ids：需要删除的对象的id集
     * @return 返回操作结果（true：删除成功，false：删除失败）
     * 添加事务，保证中间删除失败时可以回滚
     */
    @Override
    @Transactional
    public boolean batchDelete(String ids) throws FindException, DeleteException {
        if (StringUtil.isEmpty(ids))
            return false;

        //获取当前登录用户
        String currentUsername = SysResourcesUtils.getCurrentUsername();
        LogUtil.info(this, "新闻批量删除", "用户：【" + currentUsername + "】正在批量删除IDS为：【" + ids + "】的新闻");

        String[] arr = ids.split(",");  //分割成数组
        for (String id : arr) {
            Integer res = this.delete(Integer.valueOf(id));
            if (res == 0) {
                return false;
            }
        }
        return true;
    }
}
