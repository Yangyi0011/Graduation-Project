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
    private Integer id;

    /**
     * title，对应数据库（id）字段
     */
    @NotBlank(message = "新闻标题不能为空")
    private String title;

    /**
     *  newsContentId 新闻内容id
     */
    private Integer newsContentId;

    /**
     *  imgs 新闻预览图片
     */
    private String imgs;
}
