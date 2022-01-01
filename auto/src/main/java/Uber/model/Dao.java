package Uber.model;

import java.util.Collection;
import java.util.List;

import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.DefineList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

/**
 *
 * @author Kent Yeh
 */
public interface Dao extends SqlObject, AutoCloseable {

    @SqlQuery("SELECT * FROM appmember WHERE account = :account")
    @RegisterRowMapper(Member.MemberMapper.class)
    Member findMemberByPrimaryKey(@Bind("account") String account);

    @SqlQuery("SELECT * FROM appmember WHERE enabled = 'Y' ORDER BY account")
    @RegisterRowMapper(Member.MemberMapper.class)
    List<Member> findAvailableUsers();

    @SqlQuery("SELECT * FROM appmember ORDER BY account")
    @RegisterRowMapper(Member.MemberMapper.class)
    List<Member> findAllUsers();

    @SqlQuery("SELECT * FROM appmember WHERE EXISTS(SELECT 1 FROM authorities"
            + " WHERE authorities.account=appmember.account AND ARRAY_CONTAINS(ARRAY[ <auths> ],authority) )"
            + " ORDER BY account")
    @UseStringTemplateEngine
    @RegisterRowMapper(Member.MemberMapper.class)
    List<Member> findUsersByAuthoritues(@BindList("auths") List<String> auths);

    @SqlQuery("SELECT * FROM authorities WHERE account = :account")
    @RegisterRowMapper(Authority.AuthorityMapper.class)
    List<Authority> findAuthorityByAccount(@Bind("account") String account);

    @SqlUpdate("UPDATE appmember SET passwd = :newPass WHERE account = :account AND passwd= :oldPass")
    int changePasswd(@Bind("account") String account, @Bind("oldPass") String oldPass, @Bind("newPass") String newPass);

    @SqlUpdate("INSERT INTO appmember(account,name,passwd,enabled,birthday)"
            + "values( :account , :name , :password , :enabled , :birthday )")
    void newMember(@BindBean Member member);

    @SqlUpdate("UPDATE appmember SET name= :name ,passwd= :password,enabled= :enabled ,birthday= :birthday WHERE account= :account")
    int updateMember(@BindBean Member member);

    @SqlQuery("SELECT passwd FROM appmember WHERE account= :account")
    String getPasswd(@Bind("account") String account);

    @SqlUpdate("DELETE FROM appmember WHERE account= :account")
    int removeMember(@Bind("account") String account);

    @SqlQuery("SELECT * FROM authorities WHERE account = :account AND authority= :authority")
    @RegisterRowMapper(Authority.AuthorityMapper.class)
    Authority findAuthorityByBean(@BindBean Authority authority);

    @SqlUpdate("INSERT INTO authorities(account,authority) values( :account, :authority)")
    @GetGeneratedKeys("aid")
    long newAuthority(@BindBean Authority authority);

    @SqlUpdate("DELETE FROM authorities WHERE account = :account")
    int removeAuthories(@Bind("account") String account);

    @SqlUpdate("DELETE FROM authorities WHERE account = :account AND authority not in ( <authorities> )")
    int removeAuthories(@Bind("account") String account, @DefineList("authorities") Collection<String> authorities);

    @SqlUpdate("DELETE FROM authorities WHERE aid= :aid")
    int removeAuthority(@Bind("aid") long aid);

    @SqlQuery("select count(8) from information_schema.sessions")
    int countSessions();
    
    @SqlQuery("SELECT member FROM hzmembers")
    List<String> queryHzMembers();

    @Override
    default void close() {
        getHandle().close();
    }
}
