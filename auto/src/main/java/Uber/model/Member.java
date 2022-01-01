package Uber.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 *
 * @author Kent Yeh
 */
@Entity
public class Member implements Serializable {

    private static final long serialVersionUID = 395368712192880218L;

    @NotNull(message = "{Uber.model.Member.account.notNull.message}")
    @Size(min = 1, message = "{Uber.model.Member.account.notEmpty.message}")
    @Column
    @Id
    private String account;

    @NotNull(message = "{Uber.model.Member.passwd.notNull.message}")
    @Size(min = 1, message = "{Uber.model.Member.passwd.notEmpty.message}")
    @Column(name = "passwd")
    private String password;

    @NotNull(message = "{Uber.model.Member.name.notNull.message}")
    @Size(min = 1, message = "{Uber.model.Member.name.notEmpty.message}")
    @Column
    private String name;

    @Column
    private String enabled = "Y";

    @Column
    @Temporal(TemporalType.DATE)
    private Date birthday;

    private List<Authority> authorities;

    public Member() {
    }

    public Member(String account, String name) {
        this.account = account;
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnabled() {
        return "Y".equals(enabled) ? "Y" : "N";
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public Date getBirthday() {
        return birthday == null ? null : new Date(birthday.getTime());
    }

    public void setBirthday(Date birthday) {
        if (birthday == null) {
            this.birthday = null;
        } else if (this.birthday == null) {
            this.birthday = new Date(birthday.getTime());
        } else {
            this.birthday.setTime(birthday.getTime());
        }
    }

    public List<Authority> getAuthorities() {
        if (authorities == null) {
            authorities = new ArrayList<>();
        }
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            if (this.authorities != null) {
                this.authorities.clear();
            }
        } else {
            if (this.authorities == null) {
                this.authorities = new ArrayList<>(authorities.size());
            } else if (!this.authorities.isEmpty()) {
                this.authorities.clear();
            }
            this.authorities.addAll(authorities);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.account);
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
        final Member other = (Member) obj;
        return Objects.equals(this.account, other.account);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]:%s", name, account, password);
    }

    public static class MemberMapper implements RowMapper<Member> {

        @Override
        public Member map(ResultSet rs, StatementContext ctx) throws SQLException {
            Member res = new Member(rs.getString("account"), rs.getString("name"));
            res.setPassword(rs.getString("passwd"));
            res.setEnabled(rs.getString("enabled"));
            java.sql.Date dv = rs.getDate("birthday");
            if (!rs.wasNull()) {
                res.setBirthday(new Date(dv.getTime()));
            }
            return res;
        }

    }
}
