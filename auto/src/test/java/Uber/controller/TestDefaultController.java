package Uber.controller;

import Uber.manager.TestMemberManager;
import Uber.model.Member;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author kent
 */
@WebAppConfiguration
@ContextConfiguration(classes = Uber.context.TestContext.class)
public class TestDefaultController extends AbstractTestNGSpringContextTests {

    private static final Logger logger = LogManager.getLogger(TestDefaultController.class);
    @Autowired
    WebApplicationContext wac;
    private MockMvc mockMvc;
    @Autowired
    private TestMemberManager memberManager;

    @BeforeClass
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void testDevice() throws Exception {
        mockMvc.perform(get("/").param("device", "mobile")).andExpect(view().name("index")).andExpect(model().attribute("device", is(equalTo("mobile"))));
    }

    @Test
    public void testListuser() throws Exception {
        mockMvc.perform(post("/admin/users").with(user("admin").roles("ADMIN"))).andDo(print())
                .andExpect(jsonPath("$.total", is(equalTo(memberManager.countUsers()))));
    }

    @Test
    public void testListAdminOrUser() throws Exception {
        mockMvc.perform(post("/admin/adminOrUsers").with(user("admin").roles("ADMIN"))).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(equalTo(memberManager.countAdminOrUser(Arrays.asList(new String[]{"ROLE_ADMIN", "ROLE_USER"}))))));
    }

    @Test
    public void testMyinfo() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/user/myinfo").principal(new TestingAuthenticationToken("admin", "admin", "ROLE_ADMIN"))).andReturn();
        Member member = (Member) mvcResult.getRequest().getAttribute("member");
        if (member != null) {
            assertThat("Test UserInfo error ", member.getAccount(), is(equalTo("admin")));
        } else {
            throw new RuntimeException("member not found!");
        }
    }

    @Test
    public void testUserInfo() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/admin/user/{account}", "admin").with(user("admin").roles("ADMIN"))).andReturn();
        Member member = (Member) mvcResult.getRequest().getAttribute("member");
        logger.debug("account \"{}\" name is {}", member.getAccount(), member.getName());
        assertThat("Test UserInfo error ", "admin", is(equalTo(member.getAccount())));
    }

    @Test
    public void testUserLike() throws Exception {
        mockMvc.perform(post("/user/like")).andDo(print())
                .andExpect(jsonPath("$.count", is(equalTo(1))));
    }
    
    @Test
    public void testUserDislike() throws Exception {
        mockMvc.perform(post("/user/dislike")).andDo(print())
                .andExpect(jsonPath("$.count", is(equalTo(1))));
    }
}
