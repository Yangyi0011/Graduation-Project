package com.yangyi.graduationproject.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=true)
@Accessors
public class NewsInfo extends BaseEntity implements Serializable {
    /**
     * id，对应数据库（id）字段
     */
    @NotBlank(message = "新闻id不能为空")
    private String id;

    /**
     * title，对应数据库（id）字段
     */
    @NotBlank(message = "新闻标题不能为空")
    private String title;

    /**
     *  content 新闻内容
     */
    @NotBlank(message = "新闻内容不能为空")
    private String content;

    /**
     *  imgs 新闻图片
     */
    private String imgs;
}
