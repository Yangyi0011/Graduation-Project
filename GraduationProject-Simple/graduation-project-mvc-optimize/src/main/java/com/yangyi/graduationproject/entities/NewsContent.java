package com.yangyi.graduationproject.entities;

import com.yangyi.graduationproject.controller.validation.TC_Delete;
import com.yangyi.graduationproject.controller.validation.TC_Find;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/4/24
 * Time: 16:09
 */
@Data
@Accessors
public class NewsContent implements Serializable {
    /**
     * id，对应数据库（id）字段
     */
    @NotBlank(message = "新闻内容id不能为空",groups = {TC_Update.class,TC_Delete.class,TC_Find.class})
    private Integer id;

    /**
     *  content 新闻内容
     */
    private String content;
}
