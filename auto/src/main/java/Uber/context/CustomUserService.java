package Uber.context;

import Uber.manager.MemberManager;
import Uber.model.Authority;
import Uber.model.Member;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kent Yeh
 */
@Service("customUserService")
public class CustomUserService implements UserDetailsService, AuthenticationUserDetailsService<UsernamePasswordAuthenticationToken> {

    private static final Logger logger = LogManager.getLogger(CustomUserService.class);

    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;

    @Autowired
    private MemberManager memberManager;

    /**
     * @param account
     * @return
     */
    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        //Find user data,找到用戶資料
        try {
            String[] vals;
            if (account.startsWith("[") && account.endsWith("]") && account.contains(",")) {
                try (JsonReader jsonReader = Json.createReader(new StringReader(account))) {
                    JsonArray jsonary = jsonReader.readArray();
                    vals = new String[jsonary.size()];
                    for (int i = 0; i < jsonary.size(); i++) {
                        vals[i] = jsonary.getString(i);
                    }
                }
            } else {
                vals = account.split(",");
            }
            logger.debug("loadUserByUsername({})", account);
            Member member = memberManager.findByPrimaryKey(vals[0]);
            //Decide user's roles,自行決定如何給角色
            if (member == null) {
                throw new UsernameNotFoundException(messageAccessor.getMessage("Uber.context.CustomUserService.userNotEnabled"));
            } else if (!"Y".equals(member.getEnabled())) {
                throw new UsernameNotFoundException(messageAccessor.getMessage("Uber.context.CustomUserService.userNotEnabled"));
            } else if (vals.length > 1 && !vals[1].equals(member.getPassword())) {//it could be load by rememberMe service
                throw new UsernameNotFoundException(messageAccessor.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials"));
            }
            StringBuilder roles = null;
            for (Authority authority : member.getAuthorities()) {
                if (roles == null) {
                    roles = new StringBuilder(authority.getAuthority());
                } else {
                    roles.append(",").append(authority.getAuthority());
                }
            }
            logger.debug("{}'s roles is {}", member.getAccount(), roles);
            if (roles == null) {
                return new CustomUserInfo(member, "");
            } else {
                return new CustomUserInfo(member, roles.toString());
            }
        } catch (UsernameNotFoundException ex) {
            throw ex;
        } catch (NoSuchMessageException ex) {
            logger.error(ex.getMessage(), ex);
            throw new UsernameNotFoundException(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param token
     * @return
     */
    @Override
    public UserDetails loadUserDetails(UsernamePasswordAuthenticationToken token) throws UsernameNotFoundException {
        return loadUserByUsername(token.getName());
    }
}
