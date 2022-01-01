package Uber.context;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.StringUtils;

/**
 *
 * @author Kent Yeh
 */
public class AjaxAwareLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private static final Logger logger = LogManager.getLogger(AjaxAwareLoginUrlAuthenticationEntryPoint.class);
    @Autowired(required = false)
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;
    private String accessDenied = "Access denied! 人員未登錄，禁止存取 !";

    public AjaxAwareLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (messageAccessor != null) {
            accessDenied = messageAccessor.getMessage("AbstractAccessDecisionManager.accessDenied", accessDenied);
        }
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            logger.debug("Ajax fail owing forbidden!");
            //jetty sendError only support ISO-8859-1
            response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDenied);
        } else {
            String pathInfo = request.getServletPath();
            if (StringUtils.hasText(pathInfo) && pathInfo.contains("/json")) {
                logger.debug("Ajax fail owing forbidden!");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDenied);
            } else {
                super.commence(request, response, authException);
            }
        }
    }
}
