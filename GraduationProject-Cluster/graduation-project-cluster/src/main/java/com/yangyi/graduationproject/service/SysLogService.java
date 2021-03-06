package com.yangyi.graduationproject.service;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.entities.SysLog;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

public interface SysLogService {

    /**
     * 通过 Id 获取系统日志
     * @param id：日志id
     * @return 返回日志对象
     */
    public SysLog getSysLogById(String id);

    /**
     * 分页获取数据
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回所有数据
     */
    public List<SysLog> getSysLogs(Map<String, Object> condition);
    /**
     * 分页获取数据
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回当前页的数据集合
     */
    public List<SysLog> getSysLogs(Integer currentPage, Integer rows, Map<String, Object> condition);

    /**
     * 添加日志
     * @param sysLog：日志对象
     * @return 返回操作成功的条数，0表示操作失败
     */
    public Integer add(@Validated(value = {TC_Add.class}) SysLog sysLog);

    /**
     * 通过 id 删除日志
     * @param id ：日志对象ID
     * @return 返回操作成功的条数，0表示操作失败
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
