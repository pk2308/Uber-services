package Uber.context;

/**
 *
 * @author Kent Yeh
 */
import com.hazelcast.config.AttributeConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystemManagementService;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository;
import org.springframework.session.hazelcast.Hazelcast4PrincipalNameExtractor;
import org.springframework.session.hazelcast.HazelcastSessionSerializer;
import org.springframework.session.web.http.SessionRepositoryFilter;

public class SpringSessionListener implements ServletContextListener {

    private static final Logger logger = LogManager.getLogger(SpringSessionListener.class);
    private HazelcastInstance instance;
    private List<String> hzmembers;

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("hazelcastInstance");
        if (hzmembers.size() > 2) {
            this.instance.getCPSubsystem().getCPSubsystemManagementService()
                    .removeCPMember(this.instance.getCPSubsystem().getLocalCPMember().getUuid());
        }
        this.instance.shutdown();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String members = sce.getServletContext().getInitParameter("hazelcastMembers");
        hzmembers = Arrays.asList(members == null || members.trim().isEmpty()
                ? new String[]{"127.0.0.1"} : members.split(","));
        instance = (HazelcastInstance) sce.getServletContext().getAttribute("hazelcastInstance");
        if (instance == null) {
            logger.debug("Create SpringSessionListener with {} CPMember", hzmembers.size());
            this.instance = Hazelcast.newHazelcastInstance(createHazelcastConfig(sce.getServletContext()));
        }
        if (hzmembers.size() > 2) {
            logger.debug("Hz Member size is {}", hzmembers.size());
            try {
                CPSubsystemManagementService cpsm = this.instance.getCPSubsystem().getCPSubsystemManagementService();
                cpsm.awaitUntilDiscoveryCompleted(1, TimeUnit.MINUTES);
                cpsm.promoteToCPMember();
            } catch (InterruptedException ex) {
                logger.fatal("Failed to create CP SubSystem:" + ex.getMessage(), ex);
            }
        }
        Map<String, Session> sessions = this.instance.getMap(Hazelcast4IndexedSessionRepository.DEFAULT_SESSION_MAP_NAME);
        MapSessionRepository sessionRepository = new MapSessionRepository(sessions);
        SessionRepositoryFilter<? extends Session> filter = new SessionRepositoryFilter<>(sessionRepository);

        Dynamic fr = sce.getServletContext().addFilter("springSessionFilter", filter);
        fr.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
        sce.getServletContext().setAttribute("hazelcastInstance", instance);
    }

    public static Config createHazelcastConfig(ServletContext servletContext) {
        Config config = new Config().setProperty("hazelcast.logging.type", "log4j2").setClusterName("springJndi");
        String members = servletContext.getInitParameter("hazelcastMembers");
        List<String> hzmembers = Arrays.asList(members == null || members.trim().isEmpty()
                ? new String[]{} : members.split(","));
        NetworkConfig networkConfig = config.getNetworkConfig();
        if (hzmembers.size() > 2) {
            config.getCPSubsystemConfig().setCPMemberCount(3)
                    .setSessionTimeToLiveSeconds(300).setSessionHeartbeatIntervalSeconds(5)
                    .setMissingCPMemberAutoRemovalSeconds(14400).setFailOnIndeterminateOperationState(false);
            networkConfig.setPortAutoIncrement(false).setPort(5701);
        }
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setMembers(hzmembers).setEnabled(true);
        AttributeConfig attributeConfig = new AttributeConfig()
                .setName(Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
                .setExtractorClassName(Hazelcast4PrincipalNameExtractor.class.getName());
        config.getMapConfig(Hazelcast4IndexedSessionRepository.DEFAULT_SESSION_MAP_NAME)
                .setTimeToLiveSeconds(86400)
                .addAttributeConfig(attributeConfig).addIndexConfig(
                        new IndexConfig(IndexType.HASH, Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE));
        SerializerConfig serializerConfig = new SerializerConfig();
        serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
        config.getSerializationConfig().addSerializerConfig(serializerConfig);
        return config;
    }

}
