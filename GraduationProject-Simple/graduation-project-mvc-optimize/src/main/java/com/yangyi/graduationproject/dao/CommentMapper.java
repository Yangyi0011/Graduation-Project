package com.yangyi.graduationproject.dao;

import com.yangyi.graduationproject.entities.Comment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Repository("commentMapper")
public interface CommentMapper {
    /**
     *
     * @param id 留言的ID
     * @return 留言
     */
    public Comment getCommentById(@Param("id") String id);

    /**
     * 分页获取所有留言
     * @param start：从第几条数据开始
     * @param number：要获取多少条数据
     * @param condition：模糊查询内容
     * @return 返回留言对象集合
     */
    public List<Comment> getComments(@Param("start") Integer start, @Param("number") Integer number,
                                     @Param("condition") Map<String, Object> condition);


    /**
     * 留言添加方法
     * @param comment 留言
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer add(@Valid @Param("comment") Comment comment);


    /**
     * 留言更新方法
     * @param comment：要更新的留言
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer update(@Valid @Param("comment") Comment comment);

    /**
     * 留言删除方法
     * @param id：留言id
     * @return 返回操作成功的个数，0表示操作失败
     */
    public Integer delete(@Param("id") String id);

}
