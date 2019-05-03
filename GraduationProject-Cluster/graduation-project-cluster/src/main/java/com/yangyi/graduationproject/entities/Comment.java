package com.yangyi.graduationproject.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=true)
@Accessors
public class Comment extends BaseEntity implements Serializable {
    private String id;
    private String title;
    private Integer state;
    private String content;

    private long replyCount;
    private long praiseCount;
}

