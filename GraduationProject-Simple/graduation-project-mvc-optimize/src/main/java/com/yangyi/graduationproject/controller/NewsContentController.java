package com.yangyi.graduationproject.controller;

import com.yangyi.graduationproject.controller.validation.TC_Update;
import com.yangyi.graduationproject.entities.NewsContent;
import com.yangyi.graduationproject.entities.User;
import com.yangyi.graduationproject.exception.UpdateException;
import com.yangyi.graduationproject.service.NewsContentService;
import com.yangyi.graduationproject.service.UserService;
import com.yangyi.graduationproject.utils.AjaxJson;
import com.yangyi.graduationproject.utils.SysResourcesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/4/24
 * Time: 17:47
 */
@RequestMapping("/newsContent")
@RestController
public class NewsContentController {
    @Qualifier("newsContentService")
    @Autowired
    private NewsContentService newsContentService;

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    /**
     * 根据 id 获取新闻内容
     * @param id：新闻内容 id
     * @return 新闻内容对象
     */
    @GetMapping("/{id}")
    public AjaxJson getNewsContentById(@PathVariable("id") Integer id){
        AjaxJson ajaxJson = new AjaxJson();
        if (id == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("请求失败，请求对象id不能为空");
            return ajaxJson;
        }

        NewsContent newsContent = newsContentService.getNewsContentById(id);
        if (newsContent == null) {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("对象不存在或已被删除");
            return ajaxJson;
        } else {
            ajaxJson.setCode(1);
            ajaxJson.setMsg("获取成功");
            Map<String, Object> map = new ConcurrentHashMap<>();
            map.put("newsContent", newsContent);
            ajaxJson.setData(map);
            return ajaxJson;
        }
    }

    /**
     * 新闻内容添加方法
     * @param newsContent 新闻内容
     * @return 返回操作成功的个数，0表示操作失败
     */
    @PostMapping
    public AjaxJson add(NewsContent newsContent){
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
        if(newsContent==null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("操作对象为空，操作失败");
            return ajaxJson;
        }
        //资料添加

        //id 自增，无需添加
        //newsContent.setId(UUID.randomUUID().toString());
        Integer add = newsContentService.add(newsContent);
        if (add==1){
            ajaxJson.setCode(1);
            ajaxJson.setMsg("添加成功");
        }else {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("添加失败，请稍后重试");
        }
        return ajaxJson;
    }

    /**
     * 新闻内容更新方法
     * @param newsContent：要更新的新闻内容
     * @return 返回操作成功的个数，0表示操作失败
     */
    @PutMapping
    public AjaxJson update(@Validated(value = {TC_Update.class}) NewsContent newsContent){
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
        if (newsContent == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("操作失败，对象不能为空");
            return ajaxJson;
        }
        //更新操作
        NewsContent content = newsContentService.getNewsContentById(newsContent.getId());
        if (content == null){
            throw new UpdateException("更新失败，对象不存在或已被删除");
        }else {
            if (newsContent.getContent() != null){
                content.setContent(newsContent.getContent());
            }
            res = newsContentService.update(content);
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

    /**
     * 新闻内容删除方法
     * @param id：新闻内容id
     * @return 返回操作成功的个数，0表示操作失败
     */
    @DeleteMapping("/{id}")
    public AjaxJson delete(@PathVariable("id") Integer id){
        AjaxJson ajaxJson = new AjaxJson();

        if (id == null){
            ajaxJson.setCode(0);
            ajaxJson.setMsg("删除失败，请先选择要删除的对象");
            return ajaxJson;
        }

        Integer delete = newsContentService.delete(id);
        if (delete > 0){
            ajaxJson.setCode(1);
            ajaxJson.setMsg("删除成功");
        }else {
            ajaxJson.setCode(0);
            ajaxJson.setMsg("删除失败，请稍后重试");
        }
        return ajaxJson;
    }
}