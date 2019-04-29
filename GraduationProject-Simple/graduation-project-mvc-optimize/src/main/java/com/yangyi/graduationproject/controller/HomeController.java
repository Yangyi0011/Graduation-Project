package com.yangyi.graduationproject.controller;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.yangyi.graduationproject.utils.AjaxJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/4/3
 * Time: 20:16
 */
@Controller
public class HomeController {
    @Autowired
    private DefaultKaptcha captchaProducer;

//    @GetMapping("/")
//    public String home() {
//        return "index";
//    }
//
//    @GetMapping("/goHome")
//    public String goHome() {
//        return "index";
//    }
//
//    @GetMapping("/index.html")
//    public String backHome() {
//        return "index";
//    }
//
//    @GetMapping("/body")
//    public String body() {
//        return "pages/static/body";
//    }
//
//    @GetMapping("/footer")
//    public String footer() {
//        return "pages/static/footer";
//    }

    /**
     * 获取验证码 的 请求路径
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws Exception
     */
    @GetMapping("/defaultKaptcha")
    public void defaultKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        byte[] captchaChallengeAsJpeg = null;
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        try { //生产验证码字符串并保存到session中
            String createText = captchaProducer.createText();
            httpServletRequest.getSession().setAttribute(Constants.KAPTCHA_SESSION_KEY, createText); //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
            BufferedImage challenge = captchaProducer.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
        captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
    }

    /**
     * 获取服务器根路径
     * @param request：HttpServletRequest
     * @return
     */
    @ResponseBody
    @GetMapping("/context")
    public AjaxJson getContext(HttpServletRequest request){
        AjaxJson ajaxJson = new AjaxJson();
        ajaxJson.setCode(1);
        ajaxJson.setMsg("请求成功");

        String path = request.getContextPath();
        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;

        Map<String,Object> map = new ConcurrentHashMap<>();
        map.put("context",basePath);
        ajaxJson.setData(map);
        return ajaxJson;
    }
}