package com.yangyi.graduationproject.security.config;

import com.yangyi.graduationproject.security.authentication.CustomAuthenticationEntryPoint;
import com.yangyi.graduationproject.security.authentication.CustomAuthenticationProvider;
import com.yangyi.graduationproject.security.authentication.CustomPermissionEvaluator;
import com.yangyi.graduationproject.security.filter.KaptchaAuthenticationFilter;
import com.yangyi.graduationproject.security.handler.CustomAccessDeniedHandler;
import com.yangyi.graduationproject.security.handler.CustomAuthenticationSuccessHandler;
import com.yangyi.graduationproject.security.handler.CustomUrlAuthenticationFailureHandler;
import com.yangyi.graduationproject.service.UserInfoService;
import com.yangyi.graduationproject.service.UserService;
import com.yangyi.graduationproject.service.impl.UserInfoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/3/26
 * Time: 15:19
 */

/**
 * web安全配置
 */
//开启基于注解表达式的权限控制
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true, securedEnabled = true, proxyTargetClass = true)
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    private static final String ICO = "/**/favicon.ico"; //图标
    private static final String HOME = "/";   //网站首页
    private static final String STATIC_RESOURCE = "/**/static/**";   //静态资源路径
    private static final String ERROR_PAGE = "/**/errorpage/**"; //错误页面
    private static final String CAPTCHA_URL = "/**/defaultKaptcha";   //验证码路径

    private static final String LOGIN_PAGE = "/webpages/static/login.html"; //指定登录页面
    private static final String REGISTER_PAGE = "/webpages/static/login.html"; //指定注册页面

    private static final String CHECK_USERNAME_URL = "/user/usernameCheck";  //验证用户名是否可用
    private static final String LOGIN_URL = "/user/login";  //指定登录数据请求的URL
    private static final String REGISTER_URL = "/user/register";  //指定注册数据请求的URL
    private static final String LOGOUT_URL = "/user/logout";   //指定退出登录请求的URL

    private static final boolean INVALIDATE_SESSION = true; //指定退出登录时，是否让 session 失效
    private static final String[] DELETE_COOKIES = {   //指定退出登录时要删除的 cookies
            "JSESSIONID", "USERNAME","REMEMBER"
    };

    private static final String USERNAME_PARAMETER = "username"; //指定登录时，username的name属性
    private static final String PASSWORD_PARAMETER = "password"; //指定登录时，password的name属性

    private static final String ADMIN_PATH = "/**/admin/**"; //管理员路径，需要管理员权限才能访问
    private static final String USER_PATH = "/**/user/**";  //用户路径，需要用户权限才能访问
    private static final String[] RESOURCE_PATH = {   //对任何用户都放行的资源请求
            "/","/**/user/getUser**", ICO, ERROR_PAGE, CAPTCHA_URL, HOME, STATIC_RESOURCE,CHECK_USERNAME_URL, LOGIN_PAGE, REGISTER_PAGE, LOGIN_URL, REGISTER_URL
    };

    /**
     * 放行任意静态资源
     *
     * @param web：WebSecurity
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/static/**","favicon.ico","/**/**image**","/**/**.jpg","/**/**.png");
    }

    /**
     * 自定义认证处理
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new CustomAuthenticationProvider(userDetailsService()));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //基本设置
        http
                .cors()
                .and()   //开启跨域请求
                .csrf().disable()   //关闭跨站请求，防止CSRF恶意攻击与利用
                .anonymous() //允许匿名访问
                .and()
                .headers().frameOptions().sameOrigin() //支持同域名下 iframe 加载
        ;
        //静态资源放行
        http
                .authorizeRequests()
                //处理跨域请求中的Preflight请求
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                //放行所有静态资源
                .antMatchers(RESOURCE_PATH).permitAll()
        ;
        //控制权限
        http
                //验证码过滤
                .addFilterBefore(new KaptchaAuthenticationFilter(CAPTCHA_URL, LOGIN_PAGE), UsernamePasswordAuthenticationFilter.class)
                //权限控制
                .authorizeRequests()
                //静态资源
                .antMatchers(RESOURCE_PATH).permitAll()
                //admin路径需要 ADMIN 权限
                .antMatchers(ADMIN_PATH).access("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
                //user路径需要 User 权限
                .antMatchers(USER_PATH).access("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
                .anyRequest().permitAll()   //其他可以直接访问
        ;
        //开启记住我功能，并设定提交参数
        http.rememberMe()
                .rememberMeParameter("remember");

        //登录表单
        http
                .formLogin()
                    .loginPage(LOGIN_PAGE).permitAll()  //指定自定义登录页面
                    .loginProcessingUrl(LOGIN_URL).permitAll()  //指定登录数据请求的url，
                    .usernameParameter(USERNAME_PARAMETER)  //指定用户名输入框的name属性
                    .passwordParameter(PASSWORD_PARAMETER)  //指定密码输入框的name属性
                    .defaultSuccessUrl(HOME).permitAll()    //指定直接请求登录页面的用户登录成功后返回的页面
                    //指定自定义的登录成功后处理器，使用后之前设置的登录成功后跳转都会失效
                    .successHandler(new CustomAuthenticationSuccessHandler(userService,userInfoService()))
                    //指定自定义的认证失败后处理器，使用后默认的认证失败后跳转都会失效
                    .failureHandler(new CustomUrlAuthenticationFailureHandler(LOGIN_PAGE))
        ;
        //认证异常处理
        http
                //异常处理器，引导未登录用户登录
                .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint(LOGIN_PAGE))
                .accessDeniedHandler(new CustomAccessDeniedHandler())   //认证失败统一处理器
        ;
        //退出登录
        http
                .logout()
                    .logoutUrl(LOGOUT_URL).permitAll()  //指定注销请求的url
                    .logoutSuccessUrl(HOME) //指定注销成功后返回的页面
                    .deleteCookies(DELETE_COOKIES)  //指定退出时删除的cookies
                    .invalidateHttpSession(INVALIDATE_SESSION) //退出时是否让 session 失效
                ;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return (UserDetailsService) userService;
    }

    @Bean
    public UserInfoService userInfoService(){
        return new UserInfoServiceImpl();
    }

    /**
     * 注入自定义PermissionEvaluator，基于注解的权限认证
     */
    @Bean
    public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setPermissionEvaluator(new CustomPermissionEvaluator());
        return handler;
    }
}
