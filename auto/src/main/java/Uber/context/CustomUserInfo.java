package Uber.context;

import Uber.model.Member;
import java.util.Collections;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

/**
 *
 * @author Kent Yeh
 */
public class CustomUserInfo extends User {

    private static final long serialVersionUID = -2209416924912982094L;

    private final Member member;

    public Member getMember() {
        return member;
    }

    public CustomUserInfo(Member member, String roles) {
        super(member.getAccount(), member.getPassword(), true, true, true, true, roles == null || roles.isEmpty()
                ? Collections.<GrantedAuthority>emptyList() : AuthorityUtils.commaSeparatedStringToAuthorityList(roles));
        this.member = member;
    }

    @Override
    public boolean isEnabled() {
        return "Y".equals(member.getEnabled());
    }

    @Override
    public String getUsername() {
        return member == null ? super.getUsername() : member.getAccount();
    }

    @Override
    public String getPassword() {
        return "{noop}" + (member == null ? super.getPassword() : member.getPassword());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.member);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CustomUserInfo other = (CustomUserInfo) obj;
        return Objects.equals(this.member, other.member);
    }

    @Override
    public String toString() {
        return member.toString();
    }

}
