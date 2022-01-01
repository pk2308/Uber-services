package Uber.context;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *
 * @author Kent Yeh
 */
public class CaptchaUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger logger = LogManager.getLogger(CaptchaUsernamePasswordAuthenticationFilter.class);
    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
            HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        String captcha = request.getParameter("captcha");
        HttpSession session = request.getSession();
        if (captcha == null || captcha.trim().isEmpty()) {
            throw new AuthenticationServiceException(messageAccessor.getMessage("Uber.context.CaptchaUsernamePasswordAuthenticationFilter.notEmptyCaptcha"));
        } else if (!captcha.trim().equalsIgnoreCase("" + session.getAttribute("captcha"))) {
            logger.debug("captcha:{} not equal session's captcha:{}", captcha.trim(), session.getAttribute("captcha"));
            throw new AuthenticationServiceException(messageAccessor.getMessage("Uber.context.CaptchaUsernamePasswordAuthenticationFilter.captchaNotValid"));
        }
        return super.attemptAuthentication(request, response);
    }
}
