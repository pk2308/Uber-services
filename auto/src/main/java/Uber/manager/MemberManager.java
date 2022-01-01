package Uber.manager;

import Uber.context.ValidationUtils;
import Uber.model.Authority;
import Uber.model.Dao;
import Uber.model.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Kent Yeh
 */
@Repository("memberManager")
public class MemberManager extends AbstractDaoManager<String, Member> {

    private static final Logger logger = LogManager.getLogger(MemberManager.class);
    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;

    public MessageSourceAccessor getMessageAccessor() {
        return messageAccessor;
    }

    @Autowired
    ValidationUtils vu;

    @Override
    public String text2Key(String text) {
        return text;
    }

    protected Exception extractSQLException(Exception ex) {
        Throwable result = ex;
        boolean found = false;
        while (result != null) {
            if (result instanceof java.sql.SQLException) {
                found = true;
                break;
            } else if (result.getCause() == null) {
                break;
            } else {
                result = result.getCause();
            }
        }

        return found ? (java.sql.SQLException) result : ex;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public Member findByPrimaryKey(String account) {
        Dao dao = getContext().getBean(Dao.class);
        Member member = dao.findMemberByPrimaryKey(account);
        if (member != null) {
            try {
                List<Authority> auths = dao.findAuthorityByAccount(account);
                member.setAuthorities(auths);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return member;
    }

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public List<Member> findAvailableUsers() throws Exception {
        return getContext().getBean(Dao.class).findAvailableUsers();
    }

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public List<Member> findAllUsers() throws Exception {
        return getContext().getBean(Dao.class).findAllUsers();
    }

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public List<Member> findAdminUser() throws Exception {
        try {
            return getContext().getBean(Dao.class).findUsersByAuthoritues(Arrays.asList(new String[]{"ROLE_ADMIN", "ROLE_USER"}));
        } catch (BeansException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void newMember(Member member) throws Exception {
        try {
            Dao dao = getContext().getBean(Dao.class);
            vu.validateMessage(member, RuntimeException.class);
            dao.newMember(member);
            List<Authority> authories = member.getAuthorities();
            if (authories != null) {
                for (Authority authority : authories) {
                    vu.validateMessage(authority, RuntimeException.class);
                    dao.newAuthority(authority);
                }
            }
        } catch (BeansException ex) {
            logger.debug("{}{}", messageAccessor.getMessage("exception.newMember"), ex.getMessage());
            throw new RuntimeException(ex.getMessage(), extractSQLException(ex));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean updateMember(Member member) throws Exception {
        Dao dao = getContext().getBean(Dao.class);
        vu.validateMessage(member, RuntimeException.class);
        if (dao.updateMember(member) == 1) {
            List<Authority> oriauthories = dao.findAuthorityByAccount(member.getAccount());
            List<Authority> authories = member.getAuthorities();
            List<Authority> newauthories = new ArrayList<>();
            if (authories != null && !authories.isEmpty()) {
                for (Authority authority : authories) {
                    if (!oriauthories.contains(authority)) {
                        vu.validateMessage(authority, RuntimeException.class);
                        authority.setAid(dao.newAuthority(authority));
                        newauthories.add(authority);
                        oriauthories.remove(authority);
                    } else {
                        newauthories.add(authority);
                        oriauthories.remove(authority);
                    }
                }
                for (Authority authority : oriauthories) {
                    dao.removeAuthority(authority.getAid());
                }
                member.setAuthorities(newauthories);
            } else {
                dao.removeAuthories(member.getAccount());
            }
            return true;
        } else {
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int updatePass(String account, String oldPass, String newPass) throws Exception {
        return getContext().getBean(Dao.class).changePasswd(account, oldPass, newPass);
    }

}
