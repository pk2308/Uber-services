package Uber.context;

import Uber.model.TestDao;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import javax.servlet.ServletContext;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

/**
 *
 * @author kent Yeh
 */
@Configuration
@ImportResource({"classpath:testContext.xml", "classpath:applicationContext-security.xml"})
@ComponentScan("Uber.manager")
@EnableCaching
@EnableHazelcastHttpSession()
public class TestContext {

    private Jdbi jdbi;
    private ServletContext servletContext;

    @Autowired
    public void setJdbi(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Bean(destroyMethod = "close")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TestDao testDao() {
        return jdbi.open().attach(TestDao.class);
    }

    @Bean
    public CustomUserService customUserService() {
        return new CustomUserService();
    }

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance() {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(clientConfig());
        servletContext.setAttribute("hazelcastInstance", hazelcastInstance);
        return hazelcastInstance;
    }

    @Bean
    public Config clientConfig() {
        return SpringSessionListener.createHazelcastConfig(servletContext);
    }
}
