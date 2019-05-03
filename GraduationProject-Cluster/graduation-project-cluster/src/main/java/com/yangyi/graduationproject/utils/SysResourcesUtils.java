package com.yangyi.graduationproject.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.Serializable;

/**
 * 系统资源获取工具类
 */
public class SysResourcesUtils implements Serializable {

    /**
     * 获取当前登录用户的用户名
     * @return username
     */
    public static String getCurrentUsername(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null){
            return null;
        }else {
            return authentication.getName();
        }
    }

    /**
     * 获取认证对象
     * @return Authentication
     */
    public static Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取文件上传路径，默认是操作系统下用户目录
    * @return
     */
    public static String getWebUploadPath(){
        String userHome = System.getProperty("user.home");
        String parentDir = "uploadFiles";
        File file = new File(userHome + File.separator + parentDir);
        return file.toString();
    }
}