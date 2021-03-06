package com.yangyi.graduationproject.service.impl;

import com.yangyi.graduationproject.dao.SysLogMapper;
import com.yangyi.graduationproject.entities.SysLog;
import com.yangyi.graduationproject.exception.DeleteException;
import com.yangyi.graduationproject.exception.InsertException;
import com.yangyi.graduationproject.service.SysLogService;
import com.yangyi.graduationproject.utils.LogUtil;
import com.yangyi.graduationproject.utils.StringUtil;
import com.yangyi.graduationproject.utils.SysResourcesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service("sysLogService")
public class SysLogServiceImpl implements SysLogService {

    @Qualifier("sysLogMapper")
    @Autowired
    private SysLogMapper sysLogMapper;
    /**
     * 通过 Id 获取系统日志
     * @param id：日志id
     * @return 返回日志对象
     */
    @Override
    public SysLog getSysLogById(String id) {
        return StringUtil.isEmpty(id)?null:sysLogMapper.getSysLogById(id);
    }

    /**
     * 获取所有数据
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回所有数据
     */
    @Override
    public List<SysLog> getSysLogs( Map<String,Object> condition) {
        return this.getSysLogs(null,null,condition);
    }

    /**
     * 分页获取数据
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回当前页的数据集合
     */
    @Override
    public List<SysLog> getSysLogs(Integer currentPage, Integer rows, Map<String,Object> condition) {
        if (currentPage != null && rows != null) {
            if (currentPage < 0 || rows < 0) {
                return null;
            } else {
                Integer start = (currentPage - 1) * rows;   //计算当前页的数据是从第几条开始查询
                return sysLogMapper.getSysLogs(start, rows, condition);
            }
        } else {
            return sysLogMapper.getSysLogs(null, null, condition);
        }
    }

    /**
     * 添加日志
     * @param sysLog：日志对象
     * @return 返回操作成功的条数，0表示操作失败
     */
    @Override
    public Integer add(SysLog sysLog) {
        InsertException insertException;
        if (sysLog == null){
            insertException = new InsertException("添加失败，添加对象不能为空");
            throw insertException;
        }
        return sysLogMapper.add(sysLog);
    }

    /**
     * 根据 ID 删除日志
     * @param id ：日志对象ID
     * @return
     */
    @Override
    public Integer delete(String id) {
        String currentUsername = SysResourcesUtils.getCurrentUsername();
        DeleteException deleteException;
        if (StringUtil.isEmpty(id)){
            deleteException = new DeleteException("删除失败，请先选择要删除的对象");
            throw deleteException;
        }

        SysLog sysLog = this.getSysLogById(id);
        if (sysLog == null){
            deleteException = new DeleteException("删除失败，对象不存在或已被删除");
            throw deleteException;
        }
        LogUtil.info(this,"资源删除","用户：【"+currentUsername+"删除了【"+sysLog.getOpContent()+"】日志内容");

        Integer delete = sysLogMapper.delete(sysLog.getId());

        if (delete == 0){
            deleteException = new DeleteException("删除失败，请稍后重试");
            LogUtil.warn(this,"资源删除","未知错误：对象存在但删除失败！");
            throw deleteException;
        }
        return delete;
    }

    /**
     * 批量删除
     *
     * @param ids：需要删除的对象的id集
     * @return 返回操作结果（true：删除成功，false：删除失败）
     * 添加事务，保证中间删除失败时可以回滚
     */
    @Transactional
    @Override
    public Integer batchDelete(String ids) {
        if (StringUtil.isEmpty(ids))
            return 0;

        //获取当前登录用户
        String currentUsername = SysResourcesUtils.getCurrentUsername();
        LogUtil.info(this, "日志批量删除", "用户：【" + currentUsername + "】正在批量删除IDS为：【" + ids + "】的日志");

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