package com.yangyi.graduationproject.service.impl;

import com.yangyi.graduationproject.dao.NewsInfoMapper;
import com.yangyi.graduationproject.entities.NewsInfo;
import com.yangyi.graduationproject.exception.DeleteException;
import com.yangyi.graduationproject.exception.FindException;
import com.yangyi.graduationproject.service.NewsInfoService;
import com.yangyi.graduationproject.utils.LogUtil;
import com.yangyi.graduationproject.utils.StringUtil;
import com.yangyi.graduationproject.utils.SysResourcesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Service("newsInfoService")
public class NewsInfoServiceImpl implements NewsInfoService {
    @Qualifier("newsInfoMapper")
    @Autowired
    private NewsInfoMapper newsInfoMapper;

    /**
     *通过id获取培训动态
     * @param id：培训动态id
     * @return 返回对应的培训动态对象
     */
    @Override
    public NewsInfo getNewsInfoById(String id) {
        return StringUtil.isEmpty(id)?null:newsInfoMapper.getNewsInfoById(id);
    }

    @Override
    public List<NewsInfo> getNewsInfos(Map<String,Object> condition) {
        return getNewsInfos(null, null, condition);
    }
    /**
     *分页获取所以培训动态
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：模糊查询内容
     * @return
     */
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
     * 培训动态添加方法
     * @param newsInfo：要添加的培训动态
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    @Override
    public Integer add(@Valid NewsInfo newsInfo) {
        return newsInfo == null ? 0:newsInfoMapper.add(newsInfo);
    }

    /**
     * 培训动态更新方法
     * @param newsInfo:要更新的培训动态
     * @return 返回值大于0表示操作成功，否则操作失败
     */
    @Override
    public Integer update(@Valid NewsInfo newsInfo) {
        return newsInfo == null ? 0:newsInfoMapper.update(newsInfo);
    }

    /**
     * 培训动态删除方法
     * @param id：培训动态id
     * @return 返回值大于0表示操作成功，否则操作失败
     * @throws DeleteException
     */
    @Override
    public Integer delete(String id) throws DeleteException {
        Integer result = 0;
        if (StringUtil.isNotEmpty(id)){
            if (this.getNewsInfoById(id) != null){//确保要删除对象存在
                result = newsInfoMapper.delete(id);
                if (result == 0){
                    throw new DeleteException("删除失败，请稍后重试！");
                }
            }else {
                throw new DeleteException("删除失败，动态培训不存在或已被删除！");
            }
        }else {
            throw new DeleteException("删除失败，删除动态培训id不能为空！");
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
        LogUtil.info(this, "角色批量删除", "用户：【" + currentUsername + "】正在批量删除IDS为：【" + ids + "】的角色");

        String[] arr = ids.split(",");  //分割成数组
        for (String id : arr) {
            Integer res = this.delete(id);
            if (res == 0) {
                return false;
            }
        }
        return true;
    }

}
