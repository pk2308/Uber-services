package Uber.manager;

import Uber.model.Member;
import java.lang.annotation.AnnotationFormatError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

/**
 *
 * @author Kent Yeh
 */
@WebAppConfiguration
@ContextConfiguration(classes = Uber.context.TestContext.class)
public class TestModel extends AbstractTestNGSpringContextTests {

    private static final Logger logger = LogManager.getLogger(TestModel.class);

    @Autowired
    private TestMemberManager memberManager;

    @Test(expectedExceptions = {RuntimeException.class, AnnotationFormatError.class})
    public void testDuplicateMember() throws Exception {
        Member member = new Member("admin", "admin");
        member.setPassword("xxxx");
        memberManager.newMember(member);
    }

    @Test(expectedExceptions = {RuntimeException.class, AnnotationFormatError.class})
    public void testWrongMember() throws Exception {
        Member member = new Member();
        member.setAccount("someone");
        memberManager.newMember(member);
    }

    @Test
    public void testNewMember() throws Exception {
        Member member = new Member("newbie", "newbie");
        member.setPassword("newbie");
        member.setBirthday(new java.util.Date());
        memberManager.newMember(member);
        member = memberManager.findByPrimaryKey("newbie");
        assertThat("new Member failed", member, is(notNullValue()));

    }

    @Test
    public void testUpdateMember() throws Exception {
        Member member = memberManager.findByPrimaryKey("newbie");
        assertThat("new Member failed", member, is(notNullValue()));
        member.setName("Junior");
        member.setPassword("HelloWorld!");
        assertThat("Update member failed", memberManager.updateMember(member), is(true));
        member = memberManager.findByPrimaryKey("newbie");
        assertThat("Update member failed", member.getName(), is("Junior"));
        assertThat("Update member failed", member.getPassword(), is("HelloWorld!"));
    }

    @Test
    public void testRollback() throws Exception {
        Member member = memberManager.findByPrimaryKey("admin");
        String orignPass = member.getPassword();
        try {
            memberManager.raiseRollback(member);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
        member = memberManager.findByPrimaryKey("admin");
        assertThat("DB not rollback!", member.getPassword(), is(equalTo(orignPass)));
    }
}
