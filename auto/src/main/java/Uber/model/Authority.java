package Uber.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 *
 * @author Kent Yeh
 */
public class Authority implements Serializable {

    private static final long serialVersionUID = -7454760999684175357L;
    private long aid = -1;
    @NotNull
    private String authority;
    private String account;

    public Authority() {
    }

    public Authority(long aid, String authority, String account) {
        this.aid = aid;
        this.authority = authority;
        this.account = account;
    }

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.aid ^ (this.aid >>> 32));
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
        final Authority other = (Authority) obj;
        if (!Objects.equals(this.authority, other.authority)) {
            return false;
        }
        return Objects.equals(this.account, other.account);
    }

    @Override
    public String toString() {
        return String.format("[%d]%s:%s", aid, account, authority);
    }

    public static class AuthorityMapper implements RowMapper<Authority> {

        @Override
        public Authority map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new Authority(rs.getLong("aid"), rs.getString("authority"), rs.getString("account"));
        }
    }
}
