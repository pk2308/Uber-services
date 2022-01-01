package Uber.manager;

import Uber.model.Member;
import Uber.model.TestDao;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Kent Yeh
 */
@Repository("testMemberManager")
public class TestMemberManager extends MemberManager {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public int countUsers() throws Exception {
        return getContext().getBean(TestDao.class).countUsers();
    }

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public int countAdminOrUser(List<String> authoritues) {
        return getContext().getBean(TestDao.class).countAdminOrUser(authoritues);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void raiseRollback(Member member) throws Exception {
        TestDao dao = getContext().getBean(TestDao.class);
        dao.changePasswd(member.getAccount(), member.getPassword(), "guesspass");
        member.setName(null);
        dao.updateMember(member);
    }
}
