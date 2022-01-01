package Uber.context;

import Uber.model.Dao;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import javax.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

/**
 *
 * @author Kent Yeh
 */
@Configuration
@ImportResource("classpath:applicationContext.xml")
    @EnableHazelcastHttpSession(maxInactiveIntervalInSeconds = 86400)
public class ApplicationContext {

    private static final Logger logger = LogManager.getLogger(ApplicationContext.class);
    private Jdbi jdbi;
    private ServletContext servletContext;

    @Autowired
    public void setJdbi(Jdbi jdbi) {
        this.jdbi = jdbi;
        this.jdbi.setSqlLogger(jdbiLog());
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public JdbiLog jdbiLog() {
        return new JdbiLog();
    }

    @Bean(destroyMethod = "close")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Dao dao() {
        return jdbi.open().attach(Dao.class);
    }

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance() {
        HazelcastInstance hazelcastInstance = (HazelcastInstance) servletContext.getAttribute("hazelcastInstance");
        if (hazelcastInstance == null) {
            logger.debug("Create hazelcastInstance");
            hazelcastInstance = Hazelcast.newHazelcastInstance(clientConfig());
            servletContext.setAttribute("hazelcastInstance", hazelcastInstance);
            return hazelcastInstance;
        } else {
            return hazelcastInstance;
        }
    }

    @Bean
    public Config clientConfig() {
        return SpringSessionListener.createHazelcastConfig(servletContext);
    }
}
