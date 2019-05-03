package com.yangyi.graduationproject.service;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.entities.Comment;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

public interface CommentService {
    /**
     * 通过 id 获取留言信息
     * @param id ：留言ID
     * @return 返回留言对象
     */
    public Comment getCommentById(String id);

    /**
     * 获取所有留言对象，不分页
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回留言对象集合
     */
    public List<Comment> getComments(Map<String, Object> condition);

    /**
     * 获取所有留言对象，支持分页
     * @param currentPage：当前页
     * @param rows：每页要显示的数据条数
     * @param condition：自定义查询条件，模糊查询的 key 固定为 searchContent
     * @return 返回留言对象集合
     */
    public List<Comment> getComments(Integer currentPage, Integer rows, Map<String, Object> condition);

    /**
     * 添加方法
     */
    public Integer add(@Validated(value = {TC_Add.class}) Comment comment);

    /**
     * 更新方法
     */
    public Integer update(@Validated(value = {TC_Update.class}) Comment comment);

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
