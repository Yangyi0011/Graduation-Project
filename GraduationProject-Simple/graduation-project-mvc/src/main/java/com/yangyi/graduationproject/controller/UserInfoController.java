package com.yangyi.graduationproject.controller;

import com.yangyi.graduationproject.entities.UserInfo;
import com.yangyi.graduationproject.service.UserInfoService;
import com.yangyi.graduationproject.utils.AjaxJson;
import com.yangyi.graduationproject.utils.DateUtil;
import com.yangyi.graduationproject.utils.FindConditionUtils;
import com.yangyi.graduationproject.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequestMapping("/userInfo")
@Controller
public class UserInfoController {
    @Qualifier("userInfoService")
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 获取用户昵称
     * @param id
     * @return
     */
    @ResponseBody
    @GetMapping("/nickname/userInfoId/{id}")
    public AjaxJson getNicknameByUserInfoId(@PathVariable("id") String id){
        AjaxJson ajaxJson = new AjaxJson();
        if (StringUtil.isEmpty(id)){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("查询错误，请先选择要查询的对象");
            return ajaxJson;
        }

        UserInfo userInfo = userInfoService.getUserInfoById(id);
        if (userInfo == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("查询错误，对象不存在或已被删除");
            return ajaxJson;
        }

        ajaxJson.setCode(1);
        ajaxJson.setMsg("查询成功");
        Map<String,Object> data = new ConcurrentHashMap<>();
        data.put("nickname",userInfo.getNickname());
        ajaxJson.setData(data);
        return ajaxJson;
    }

    /**
     * 通过 userId 获取用户昵称
     * @param id
     * @return
     */
    @ResponseBody
    @GetMapping("/nickname/userId/{id}")
    public AjaxJson getNicknameByUserId(@PathVariable("id") String id){
        AjaxJson ajaxJson = new AjaxJson();
        if (StringUtil.isEmpty(id)){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("查询错误，请先选择要查询的对象");
            return ajaxJson;
        }

        UserInfo userInfo = userInfoService.getUserInfoByUserId(id);
        if (userInfo == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("查询错误，对象不存在或已被删除");
            return ajaxJson;
        }

        ajaxJson.setCode(1);
        ajaxJson.setMsg("查询成功");
        Map<String,Object> data = new ConcurrentHashMap<>();
        data.put("nickname",userInfo.getNickname());
        ajaxJson.setData(data);
        return ajaxJson;
    }

    /*@PreAuthorize("hasPermission('/webpages/user/userInfo.html','READ')")*/
    @ResponseBody
    @RequestMapping("/getUserInfoById")
    public AjaxJson getUserInfoById(@RequestParam("id") String id){
        AjaxJson ajaxJson = new AjaxJson();
        if (StringUtil.isEmpty(id)){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("查询错误，请先选择要查询的对象");
            return ajaxJson;
        }

        UserInfo userInfo = userInfoService.getUserInfoById(id);
        if (userInfo == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("查询错误，对象不存在或已被删除");
            return ajaxJson;
        }else {
            ajaxJson.setCode(1);
            ajaxJson.setMsg("查询成功");
            Map<String,Object> data = new ConcurrentHashMap<>();
            data.put("userInfo",userInfo);
            ajaxJson.setData(data);
        }
        return ajaxJson;
    }


    /*@PreAuthorize("hasPermission('/webpages/user/userInfo.html','READ')")*/
    @ResponseBody
    @RequestMapping("/getUserInfoByUsername")
    public AjaxJson getUserInfoByUsername(@RequestParam("username") String username){
        AjaxJson ajaxJson = new AjaxJson();
        if (StringUtil.isEmpty(username)){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("查询错误，请先选择要查询的对象");
            return ajaxJson;
        }

        UserInfo userInfo = userInfoService.getUserInfoByUsername(username);
        if (userInfo == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("查询错误，对象不存在或已被删除");
            return ajaxJson;
        }else {
            ajaxJson.setCode(1);
            ajaxJson.setMsg("查询成功");
            Map<String,Object> data = new ConcurrentHashMap<>();
            data.put("userInfo",userInfo);
            ajaxJson.setData(data);
        }
        return ajaxJson;
    }

    @PreAuthorize("hasPermission('/webpages/admin/user/userInfo_list.html','READ')")
    @ResponseBody
    @RequestMapping("/list")
    public AjaxJson getUserInfos(@RequestParam("currentPage") Integer currentPage, @RequestParam("rows") Integer rows, HttpServletRequest request){
        AjaxJson ajaxJson = new AjaxJson();
        if (currentPage == null || rows == null) {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("数据获取失败，页数不能为空");
            return ajaxJson;
        } else {

            //自定义查询条件，以 key-value 的形式进行条件查询，模糊查询的 key 固定为 searchContent
            Map<String, Object> condition = new ConcurrentHashMap<>();
            String conditionStr = request.getParameter("condition");
            if (StringUtil.isNotEmpty(conditionStr)) {
                condition = FindConditionUtils.findConditionBuild(UserInfo.class, conditionStr);
            }
            //获取当前查询条件下的所有数据条数，分页用
            Integer total = userInfoService.getUserInfos(condition).size();
            //获取当前页的数据
            List<UserInfo> userInfos = userInfoService.getUserInfos(currentPage, rows, condition);
            ajaxJson.setCode(1);
            if (userInfos.size() == 0) {
                ajaxJson.setMsg("暂无数据Ծ‸Ծ");
            } else {
                ajaxJson.setMsg("数据获取成功");
            }
            Map<String, Object> data = new ConcurrentHashMap<>();
            data.put("total", total);
            data.put("items", userInfos);
            ajaxJson.setData(data);
            return ajaxJson;
        }
    }

    @PreAuthorize("hasPermission('/webpages/user/userInfo.html','UPDATE')")
    @ResponseBody
    @RequestMapping("/update")
    public AjaxJson update(HttpServletRequest request){
        AjaxJson ajaxJson = new AjaxJson();
        UserInfo userInfo;

        String id = request.getParameter("id");
        String username = request.getParameter("username");
        //从数据库读取旧的对象进行更新
        if (StringUtil.isNotEmpty(id)){
            //按id查找
            userInfo = userInfoService.getUserInfoById(id);
        }else if (StringUtil.isNotEmpty(username)){
            //按 username 查找
            userInfo = userInfoService.getUserInfoByUsername(username);
        }else {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("更新失败，请先选择要更新的对象");
            return ajaxJson;
        }

        if (userInfo == null) {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("更新失败，对象不存在或已被删除");
            return ajaxJson;
        }

        String nickname = request.getParameter("nickname");
        if (StringUtil.isNotEmpty(nickname)){
            userInfo.setNickname(nickname);
        }
        String realName = request.getParameter("realName");
        if (realName != null){
            userInfo.setRealName(realName);
        }
        String genderStr = request.getParameter("gender");
        Integer gender;
        if (StringUtil.isNotEmpty(genderStr)){
            gender = Integer.valueOf(genderStr);
            userInfo.setGender(gender);
        }
        String birthdayStr = request.getParameter("birthday");
        Date birthday;
        if (StringUtil.isNotEmpty(birthdayStr)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            birthday = DateUtil.strToDate(birthdayStr,sdf);
            if (birthday != null){
                userInfo.setBirthday(birthday);
            }
        }
        String address = request.getParameter("address");
        if (address != null){
            userInfo.setAddress(address);
        }
        String postalCode = request.getParameter("postalCode");
        if (postalCode != null ){
            userInfo.setPostalCode(postalCode);
        }
        String email = request.getParameter("email");
        if (StringUtil.isNotEmpty(email)){
            userInfo.setEmail(email);
        }
        String phone = request.getParameter("phone");
        if (StringUtil.isNotEmpty(phone)){
            userInfo.setPhone(phone);
        }
        String motto = request.getParameter("motto");
        if (motto != null){
            userInfo.setMotto(motto);
        }
        String describe = request.getParameter("describe");
        if (describe != null){
            userInfo.setDescribe(describe);
        }
        String company = request.getParameter("company");
        if (company != null){
            userInfo.setCompany(company);
        }
        String position = request.getParameter("position");
        if (position != null){
            userInfo.setPosition(position);
        }
        String career = request.getParameter("career");
        if (career != null){
            userInfo.setCareer(career);
        }

        //更新结果
        Integer res = userInfoService.update(userInfo);
        if (res > 0) {
            ajaxJson.setCode(1);
            ajaxJson.setMsg("更新成功");
        } else {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("更新失败，请重试");
        }
        return ajaxJson;
    }

    /**
     *更换头像
     * @param request:HttpServletRequest
     * @return
     */
    /*@ResponseBody
    @RequestMapping("/updateLogo")
    public AjaxJson updateHead(HttpServletRequest request){
        AjaxJson ajaxJson = new AjaxJson();
        *//**
         * 执行上传操作
         * 1、判断表单是否支持上传，即判断表单的提交类型是否为enctype=multipart/form-data
         * 2、创建文件上传工厂对象：DiskFileItemFactory
         * 3、创建表单解析器对象：ServletFileUpload（核心对象）
         * 4、解析request对象，并得到一个表单项的List集合
         * 5、迭代（遍历）表单项List集合，获取到上传的数据
         *//*
        // 1、判断表单是否支持上传，表单不支持上传则抛异常
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            throw new UploadException("Form 表单不是 multipart/form-data 类型，不支持上传操作！");
        }

        // 2、创建文件上传工厂对象
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        String webUploadPath = SysResourcesUtils.getWebUploadPath();
        diskFileItemFactory.setRepository(new File(webUploadPath + File.separator +"temp")); // 指定临时文件的存储路径

        // 3、创建表单解析器对象
        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);

        servletFileUpload.setSizeMax(1024 * 1024 * 2); // 限制总上传文件的大小不能超过2M

        //存放普通表单项
        List<FileItem> ordinary = Collections.synchronizedList(new ArrayList<>());
        String username = null;  //要更新头像的用户的用户名

        try {
            // 4、解析request对象，并得到一个表单项的List集合
            List<FileItem> fileItemList = servletFileUpload.parseRequest(request);

            // 5、迭代（遍历）表单项List集合，获取到上传的数据
            for (FileItem fileItem : fileItemList) {
                if (fileItem.isFormField()) {
                    //提取普通表单项
                    ordinary.add(fileItem);
                }
            }
            for (FileItem item:ordinary) {
                if (item.getName().equals("username")){
                    username = item.getString("UTF-8"); // 获取字段value，并设置编码
                    break;
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
            throw new UploadException("头像上传只支持2M以内的图片");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ajaxJson.setCode(0);
            ajaxJson.setMsg("保存失败，找不到用户信息（username）");
            return ajaxJson;
        }

        if (StringUtil.isEmpty(username)){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("更新失败，请先选择更新对象");
            return ajaxJson;
        }

        UserInfo userInfo = userInfoService.getUserInfoByUsername(username);
        if (userInfo == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("更新失败，对象不存在或已被删除");
            return ajaxJson;
        }

        List<String> uploadFiles = FileUploadUtils.upload(request);

        if (uploadFiles == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("更新失败，请重试");
            return ajaxJson;
        }
        String url = uploadFiles.get(0);//头像上传只有一个图片
        userInfo.setPortraitImg(url);
        Integer res = userInfoService.update(userInfo);//更新用户信息
        if (res > 0) {
            ajaxJson.setCode(1);
            ajaxJson.setMsg("保存成功");
        } else {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("保存成功，请重试");
        }
        return ajaxJson;
    }*/
} 