package com.yangyi.graduationproject.entities;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Delete;
import com.yangyi.graduationproject.controller.validation.TC_Find;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息表，对应数据库（tab_user_info）表
 */
@Data
@EqualsAndHashCode
@Accessors
public class UserInfo implements Serializable {

    /**
     * id，对应数据库（id）字段
     */
    @NotBlank(message = "id不能为空",groups = {TC_Update.class,TC_Delete.class,TC_Find.class})
    private String id;

    /**
     * 用户名，对应数据库（username）字段
     */
    @NotBlank(message = "账号不能为空",groups = {TC_Add.class, TC_Update.class})
    private String username;

    /**
     * 用户昵称，对应数据库（nickname）字段
     */
    private String nickname;

    /**
     * 用户真实姓名，对应数据库（real_name）字段
     */
    private String realName;

    /**
     * 用户头像URL，对应数据库（portrait_img）字段
     */
    private String portraitImg;

    /**
     * 性别，对应数据库（gender）字段
     */
    @Range(min = 0,max = 1,message = "性别只能是0或1",groups = {TC_Add.class,TC_Update.class})
    private Integer gender;

    /**
     * 生日，对应数据库（birthday）字段
     */
    @Past(message = "生日必须是一个过去的日期",groups = {TC_Add.class,TC_Update.class})
    private Date birthday;

    /**
     * 地址，对应数据库（address）字段
     */
    private String address;

    /**
     * 邮政编码，对应数据库（postal_code）字段
     */
    private String postalCode;

    /**
     * 邮箱，对应数据库（email）字段
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号，对应数据库（phone）字段
     */
    @Pattern(regexp = " ^1[0-9]{10}$",message = "手机号码格式不正确",groups = {TC_Add.class,TC_Update.class})
    private String phone;

    /**
     * 用户签名，对应数据库（motto）字段
     */
    private String motto;

    /**
     * 个人简介，对应数据库（describe）字段
     */
    private String describe;

    /**
     * 用户相关图片，对应数据库（imgs）字段
     */
    private String imgs;

    /**
     * 工作单位（公司），对应数据库（company）字段
     */
    private String company;

    /**
     * 职位，对应数据库（position）字段
     */
    private String position;

    /**
     * 职业，对应数据库（career）字段
     */
    private String career;
}