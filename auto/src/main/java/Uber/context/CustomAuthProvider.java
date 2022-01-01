package Uber.context;

import javax.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 *
 * @author kent
 */
public class CustomAuthProvider implements AuthenticationProvider {
    
    private static final Logger logger = LogManager.getLogger(CustomAuthProvider.class);
    
    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;
    
    private final UserDetailsService userDetailsService;
    
    public CustomAuthProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        String account = auth.getName();
        String passwd = auth.getCredentials().toString();
        logger.debug("{}: {}/{}", messageAccessor.getMessage("Uber.context.CustomAuthProvider.validateAccount"), account, passwd);
        UserDetails userDetails = account == null || account.trim().isEmpty() ? null
                : userDetailsService.loadUserByUsername(Json.createArrayBuilder().add(account).add(passwd).build().toString());
        if (userDetails == null) {
            throw new UsernameNotFoundException(messageAccessor.getMessage("BindAuthenticator.badCredentials"));
        } else if (!userDetails.isEnabled()) {
            throw new DisabledException(messageAccessor.getMessage("Uber.context.CustomUserService.userNotEnabled"));
        } else {
            logger.debug("{}'s roles is {}", account, userDetails.getAuthorities());
        }
        //Authenticate
        return new UsernamePasswordAuthenticationToken(userDetails, auth.getCredentials().toString(), userDetails.getAuthorities());
        
    }
    
    @Override
    public boolean supports(Class<?> type) {
        return UsernamePasswordAuthenticationToken.class.equals(type);
    }
    
}
