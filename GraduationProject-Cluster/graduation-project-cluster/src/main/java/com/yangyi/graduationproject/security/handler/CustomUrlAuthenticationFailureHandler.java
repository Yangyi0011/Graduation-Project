package com.yangyi.graduationproject.security.handler;

import com.yangyi.graduationproject.utils.HTTPUtils;
import com.yangyi.graduationproject.utils.LogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义 Security 异常响应处理器
 * 目的：添加处理 Ajax 请求
 */
public class CustomUrlAuthenticationFailureHandler implements AuthenticationFailureHandler {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private String defaultFailureUrl;               //默认登录异常后要跳转到的页面url
    private boolean forwardToDestination = false;   //使用forward
    private boolean allowSessionCreation = true;    //允许创建 session
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();  //重定向策略

    public CustomUrlAuthenticationFailureHandler() {
    }

    public CustomUrlAuthenticationFailureHandler(String defaultFailureUrl) {
        this.setDefaultFailureUrl(defaultFailureUrl);
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        //如果是 ajax 请求
        if (HTTPUtils.isAjaxRequest(request)) {
            if (this.defaultFailureUrl == null) {
                LogUtil.info(this, "SecurityAjax异常处理", "401错误，没有配置相应的failure URL");
                this.logger.debug("No failure URL set, sending 401 Unauthorized error");
                response.sendError(401);
            } else {

                //是ajax请求时，不再将异常信息保存到 session 中，而是直接响应
                //将响应数据封装成 json 格式

                //要符合 AjaxJson 类的数据格式
                Map<String,Object> responseMap = new ConcurrentHashMap<>();
                responseMap.put("code",0);
                responseMap.put("msg",exception.getMessage());

                this.logger.debug("Forwarding to " + this.defaultFailureUrl);
                LogUtil.info(this, "SecurityAjax异常处理", "请求错误，异常响应至【" + this.defaultFailureUrl + "】页面");

                //使用工具类向前端响应数据
                HTTPUtils.responseByJacson(request,response,responseMap);
            }
        } else {
            if (this.defaultFailureUrl == null) {
                this.logger.debug("No failure URL set, sending 401 Unauthorized error");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
            } else {
                this.saveException(request, exception);
                if (this.forwardToDestination) {
                    this.logger.debug("Forwarding to " + this.defaultFailureUrl);
                    request.getRequestDispatcher(this.defaultFailureUrl).forward(request, response);
                } else {
                    this.logger.debug("Redirecting to " + this.defaultFailureUrl);
                    this.redirectStrategy.sendRedirect(request, response, this.defaultFailureUrl);
                }
            }
        }
    }

    protected final void saveException(HttpServletRequest request, AuthenticationException exception) {
        if (this.forwardToDestination) {
            request.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null || this.allowSessionCreation) {
                request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
            }
        }

    }

    public void setDefaultFailureUrl(String defaultFailureUrl) {
        Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), "'" + defaultFailureUrl + "' is not a valid redirect URL");
        this.defaultFailureUrl = defaultFailureUrl;
    }

    protected boolean isUseForward() {
        return this.forwardToDestination;
    }

    public void setUseForward(boolean forwardToDestination) {
        this.forwardToDestination = forwardToDestination;
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    protected RedirectStrategy getRedirectStrategy() {
        return this.redirectStrategy;
    }

    protected boolean isAllowSessionCreation() {
        return this.allowSessionCreation;
    }

    public void setAllowSessionCreation(boolean allowSessionCreation) {
        this.allowSessionCreation = allowSessionCreation;
    }
} 