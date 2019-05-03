package com.yangyi.graduationproject.controller;

import com.yangyi.graduationproject.controller.validation.TC_Add;
import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.entities.NewsContent;
import com.yangyi.graduationproject.entities.NewsInfo;
import com.yangyi.graduationproject.entities.User;
import com.yangyi.graduationproject.exception.UpdateException;
import com.yangyi.graduationproject.service.NewsContentService;
import com.yangyi.graduationproject.service.NewsInfoService;
import com.yangyi.graduationproject.service.UserService;
import com.yangyi.graduationproject.utils.AjaxJson;
import com.yangyi.graduationproject.utils.FindConditionUtils;
import com.yangyi.graduationproject.utils.StringUtil;
import com.yangyi.graduationproject.utils.SysResourcesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequestMapping("/newsInfo")
@Controller
public class NewsInfoController {

    @Qualifier("newsInfoService")
    @Autowired
    private NewsInfoService newsInfoService;

    @Qualifier("newsContentService")
    @Autowired
    private NewsContentService newsContentService;

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @GetMapping(value = "/list")
    @ResponseBody
    public AjaxJson listPage(@RequestParam("currentPage") Integer currentPage, @RequestParam("rows") Integer rows, HttpServletRequest request) {
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
                condition = FindConditionUtils.findConditionBuild(NewsInfo.class, conditionStr);
            }

            //获取当前查询条件下的所有数据条数，分页用
            Long total = newsInfoService.count(condition);
            //获取当前页的数据
            List<NewsInfo> resources = newsInfoService.getNewsInfos(currentPage, rows, condition);

            ajaxJson.setCode(1);
            if (resources.size() == 0) {
                ajaxJson.setMsg("暂无数据Ծ‸Ծ");
            } else {
                ajaxJson.setMsg("数据获取成功");
            }

            Map<String, Object> data = new ConcurrentHashMap<>();
            data.put("total", total);
            data.put("items", resources);
            ajaxJson.setData(data);
            return ajaxJson;
        }
    }


    /**
     * 详细页信息
     *
     * @return loginPage
     */
    @GetMapping(value = "/detail/{id}")
    @ResponseBody
    public AjaxJson detail(@PathVariable("id") Integer id) {
        AjaxJson ajaxJson = new AjaxJson();
        if(id != null){
            NewsInfo newsInfo = newsInfoService.getNewsInfoById(id);
            if (newsInfo == null){
                ajaxJson.setCode(1);
                ajaxJson.setMsg("暂无数据");
                return ajaxJson;
            }else{
                ajaxJson.setCode(1);
                ajaxJson.setMsg("请求成功");
                Map<String, Object> map = new ConcurrentHashMap<>(); //返回携带的数据
                map.put("items",newsInfo);
                ajaxJson.setData(map);
                return ajaxJson;
            }
        }else {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("id异常请检查");
        }
        return ajaxJson;
    }

    /**
     * 通过 id 获取资源对象
     */
    @ResponseBody
    @RequestMapping("/getNewsInfoById")
    public AjaxJson getNewsInfoById(@RequestParam("id") Integer id) {
        AjaxJson ajaxJson = new AjaxJson();
        if (id == null) {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("请先选择要查询的对象");
            return ajaxJson;
        }

        NewsInfo resource = newsInfoService.getNewsInfoById(id);
        if (resource == null) {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("对象不存在或已被删除");
            return ajaxJson;
        } else {
            ajaxJson.setCode(1);
            ajaxJson.setMsg("获取成功");
            Map<String, Object> map = new ConcurrentHashMap<>();
            map.put("newsInfo", resource);
            ajaxJson.setData(map);
            return ajaxJson;
        }
    }


    @RequestMapping(value = "/update")
    @ResponseBody
    public AjaxJson update(@Validated(value = {TC_Update.class}) NewsInfo newsInfo, @Validated(value = {TC_Update.class})NewsContent newsContent){
        AjaxJson ajaxJson = new AjaxJson();
        Integer res;    //操作结果 flag

        User user = null;
        //获取当前用户
        String currentUsername = SysResourcesUtils.getCurrentUsername(); //当前登录人账号
        if (!"anonymousUser".equals(currentUsername)){
            user = userService.getUserByUsername(currentUsername); //当前登录对象
        }
        if (user == null){
            throw new CredentialsExpiredException("登录凭证已过期");
        }

        if (newsInfo == null || newsContent == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("操作失败，对象不能为空");
            return ajaxJson;
        }

        //更新操作
        NewsInfo newsInfoById = newsInfoService.getNewsInfoById(newsInfo.getId());
        NewsContent newsContentById = newsContentService.getNewsContentById(newsContent.getId());
        if (newsInfoById == null || newsContentById == null){
            throw new UpdateException("更新失败，对象不存在或已被删除");
        }else {
            //新闻信息
            if (newsInfo.getImgs() != null) {
                newsInfoById.setImgs(newsInfo.getImgs());
            }
            if (newsInfo.getRemarks() != null) {
                newsInfoById.setRemarks(newsInfo.getRemarks());
            }
            if(newsInfo.getTitle() != null){
                newsInfoById.setTitle(newsInfo.getTitle());
            }

            //新闻内容
            if (newsContent.getContent() != null){
                newsContentById.setContent(newsContent.getContent());
            }

            newsInfoById.setUpdateUserId(user.getId());
            newsInfoById.setUpdateDate(new Date());

            res = newsInfoService.update(newsInfoById,newsContentById);
        }
        if (res == 0){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("操作失败，请重试");
            return ajaxJson;
        }else {
            ajaxJson.setCode(1);
            ajaxJson.setMsg("操作成功");
            return ajaxJson;
        }
    }

    @RequestMapping(value = "/add")
    @ResponseBody
    public AjaxJson add(@Validated(value = {TC_Add.class}) NewsInfo newsInfo, @Validated(value = {TC_Add.class}) NewsContent newsContent){
        AjaxJson ajaxJson = new AjaxJson();

        User user = null;
        //获取当前用户
        String currentUsername = SysResourcesUtils.getCurrentUsername(); //当前登录人账号
        if (!"anonymousUser".equals(currentUsername)){
            user = userService.getUserByUsername(currentUsername); //当前登录对象
        }
        if(user==null){
            throw new CredentialsExpiredException("登录凭证已过期");
        }

        //非空验证
        if(newsInfo==null || newsContent == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("操作对象为空，操作失败");
            return ajaxJson;
        }
        //资料添加
        //id 自增，无需添加

        newsInfo.setCreateUserId(user.getId());
        newsInfo.setCreateDate(new Date());
        newsInfo.setUpdateUserId(user.getId());
        newsInfo.setUpdateDate(new Date());
        Integer add = newsInfoService.add(newsInfo,newsContent);

        if (add==1){
            ajaxJson.setCode(1);
            ajaxJson.setMsg("添加成功");
        }else {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("添加失败，请稍后重试");
        }

        return ajaxJson;
    }

    @RequestMapping(value = "/delete")
    @ResponseBody
    public AjaxJson delete(@RequestParam("ids") String ids) {
        AjaxJson ajaxJson = new AjaxJson();
        User user = null;
        //获取当前用户
        String currentUsername = SysResourcesUtils.getCurrentUsername(); //当前登录人账号
        if (!"anonymousUser".equals(currentUsername)){
            user = userService.getUserByUsername(currentUsername); //当前登录对象
        }
        if(user==null){
            throw new CredentialsExpiredException("登录凭证已过期");
        }

        if (StringUtil.isEmpty(ids)){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("删除失败，请先选择要删除的对象");
            return ajaxJson;
        }

        boolean delete = newsInfoService.batchDelete(ids);
        if (delete){
            ajaxJson.setCode(1);
            ajaxJson.setMsg("删除成功");
        }else {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("删除失败，请稍后重试");
        }

        return ajaxJson;
    }
}
