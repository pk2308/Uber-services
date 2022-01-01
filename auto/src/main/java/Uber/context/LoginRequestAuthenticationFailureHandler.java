package Uber.context;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 *
 * @author kent
 */
public class LoginRequestAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger logger = LogManager.getLogger(CustomAuthSuccessHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String val = request.getParameter("username");
        if (val != null && !val.trim().isEmpty()) {
            request.getSession().setAttribute("username", val);
        }
        val = request.getParameter("password");
        if (val != null && !val.trim().isEmpty()) {
            request.getSession().setAttribute("password", val);
        }
        logger.debug("Login failed:" + exception.getMessage(), exception);
        super.onAuthenticationFailure(request, response, exception);
    }

}
