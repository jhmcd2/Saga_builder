package gov.ic.silkwave;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import gov.ic.silkwave.activemq.interceptor.SilkwavePlugin;
import gov.ic.silkwave.authentication.AuthenticationService;
import gov.ic.silkwave.authentication.ExtendedAuthenticationService;
import gov.ic.silkwave.authorization.AuthorizationService;
import gov.ic.silkwave.common.RoutingURI;
import gov.ic.silkwave.common.ServiceException;
import gov.ic.silkwave.common.logging.LoggerHelper;
import gov.ic.silkwave.common.logging.TrackingLogger;
import gov.ic.silkwave.common.messages.builders.RestrictionManipulator;
import gov.ic.silkwave.common.messages.builders.SecurityManipulator;
import gov.ic.silkwave.common.messaging.Envelope;
import gov.ic.silkwave.common.messaging.ScratchPad;
import gov.ic.silkwave.common.thread.PausableThreadPoolExecutor;
import gov.ic.silkwave.common.transport.TransportException;
import gov.ic.silkwave.common.transport.jms.IRealTimeMessageHandler;
import gov.ic.silkwave.common.transport.jms.JMSConnection;
import gov.ic.silkwave.common.transport.jms.JMSMessageListener;
import gov.ic.silkwave.common.transport.jms.JmsDestination;
import gov.ic.silkwave.common.transport.jms.MessageSender;
import gov.ic.silkwave.common.transport.jms.PayloadDestination;
import gov.ic.silkwave.common.utils.Constants;
import gov.ic.silkwave.data.SimpleFileServer;
import gov.ic.silkwave.data.files.FileService;
import gov.ic.silkwave.data.streams.StreamService;
import gov.ic.silkwave.discovery.DiscoveryService;
import gov.ic.silkwave.nameserver.NameServer;
import gov.ic.silkwave.nameserver.NameServerLocator;
import gov.ic.silkwave.naming.NameResolution;
import gov.ic.silkwave.naming.NamingRegistry;
import gov.ic.silkwave.networkinfo.NetworkInfoService;
import gov.ic.silkwave.networkstatus.NetworkStatusService;
import gov.ic.silkwave.registration.LocalRegistry;
import gov.ic.silkwave.registration.LocalRoute;
import gov.ic.silkwave.registration.Registrar;
import gov.ic.silkwave.routing.RouteOption;
import gov.ic.silkwave.routing.RoutingService;
import gov.ic.silkwave.security.RestrictionManager;
import gov.ic.silkwave.security.SecurityService;
import gov.ic.silkwave.transport.destination.DestinationManager;
import gov.ic.silkwave.transport.destination.JMSDestinationManager;
import gov.ic.silkwave.web.WebServer;
import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.xml.bind.JAXBException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import silkwave.markings.Restriction;
import silkwave.markings.Security;

/**
 * Responsible for:
 * <p>
 * - subscribing to the input queue (front door) - validating the envelope - enforcing any front door policy - passing
 * registration message to registrar - passing routing rule modification to route manager - enforcing ntk policy for all
 * intended recipients - delivery of messages to all validated recipients
 *
 */

public class NetworkManager implements Service {

    private static final boolean COMPOSITE_SENDS = false;
    private static final int INPUT_THREADS = Integer.parseInt(
            Constants.props.getProperty("service.networkmanager.inputThreads", "10"));
    private static final int HEARTBEAT_THREADS = Integer.parseInt(
            Constants.props.getProperty("service.networkmanager.heartbeatThreads", "4"));
    private static final int CORE_THREADS = Integer.parseInt(
            Constants.props.getProperty("service.networkmanager.coreThreads", "4"));
    private static final int AUTHENTICATION_THREADS = Integer.parseInt(
            Constants.props.getProperty("service.networkmanager.authenticationThreads", "2"));
    private static final String USER_NAME = Constants.props.getProperty("service.networkmanager.username", "core");
    private static final String PASSWORD = Constants.props.getProperty("service.networkmanager.password", "manager");
    private static final String SECURITY_STATE = Constants.props.getProperty("service.security.state", "off");
    private static final String MY_PARTY_NAME = "network-manager";
    private boolean standalone;

    private String messageBrokerUrl;
    private BrokerService embeddedBroker;

    private Connection inputConnection;
    private Session inputSession;
    private Destination inputDestination;
    private MessageConsumer inputConsumer;

    private Connection heartbeatConnection;
    private Session heartbeatSession;
    private Destination heartbeatDestination;
    private MessageConsumer heartbeatConsumer;

    private Connection coreConnection;
    private Session coreSession;
    private Destination coreDestination;
    private MessageConsumer coreConsumer;

    private Connection authenticationConnection;
    private Session authenticationSession;
    private Destination authenticationDestination;
    private MessageConsumer authenticationConsumer;

    private PausableThreadPoolExecutor inputProcessingPool;
    private BlockingQueue<Runnable> inputProcessingQue;

    private PausableThreadPoolExecutor heartbeatProcessingPool;
    private BlockingQueue<Runnable> heartbeatProcessingQue;

    private PausableThreadPoolExecutor coreProcessingPool;
    private BlockingQueue<Runnable> coreProcessingQue;

    private PausableThreadPoolExecutor authenticationProcessingPool;
    private BlockingQueue<Runnable> authenticationProcessingQue;

    public static final Security NETWORK_HIGH_CLASS;
    public static final Security NETWORK_MIN_CLASS;

    private Registrar registrar;
    private NamingRegistry naming;
    private DiscoveryService discovery;
    private RoutingService routing;
    private NetworkStatusService networkStatus;
    private LocalRegistry registry;
    private FileService files;
    private DestinationManager destMgr;
    private SecurityService securityService;
    private AuthenticationService authentication;
    private ExtendedAuthenticationService extendedAuthentication;
    private AuthorizationService authorization;
    private RestrictionManager restrictionManager;
    private SimpleFileServer fileServer;

    // This service should never be removed from NetworkManager.
    private NetworkInfoService networkInfo;

    private final AtomicLong count = new AtomicLong(0);
    private ConcurrentMap<String, IRealTimeMessageHandler> internalListeners;

    // The ExecutorService for the Scheduled reloads of RestrictionManager.
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // The xml file that contains the restrictions for the RestrictionManager.
    private static final String RESTRICTIONS_FILE = Constants.props.getProperty("service.restrictions.restrictionFile",
            "/opt/silkwave/config/restrictions.xml");

    // The last time the restriction file was changed.
    private long lastTime;

    private static final Logger log;

    static {
        String home = System.getProperty("LOG_HOME");
        if (home == null) {
            home = "./logs/";
            System.setProperty("LOG_HOME", home);

        }
        log = LoggerFactory.getLogger(NetworkManager.class);
        Security high = null;
        Security low = null;
        try {
            high = SecurityManipulator.buildHubHigh(log);
            low = SecurityManipulator.buildHubMinimum(log);
        } catch (Exception ex) {
            log.error(
                    "Problem setting up Network Classifications. Verify you have classification set correctly. " +
                            "reason:{}",
                    ex.getLocalizedMessage());
            System.exit(1);
        }

        NETWORK_HIGH_CLASS = high;
        NETWORK_MIN_CLASS = low;


    }

    private static final Logger classificationAuditLog = LoggerFactory.getLogger("audit.classification");
    private StreamService streams;

    public NetworkManager() {
        embeddedBroker = null;
    }

    public long getMessageCount() {
        return count.get();
    }

    @Override
    public void startup() throws ServiceException {

        if (!SecurityManipulator.isValid(NETWORK_HIGH_CLASS) || !SecurityManipulator.isValid(NETWORK_MIN_CLASS)) {
            throw new IllegalArgumentException("Security Classification is invalid");
        }
        log.info("System High Classification = {}", SecurityManipulator.toString(NETWORK_HIGH_CLASS));
        log.info("System Min Classification = {}", SecurityManipulator.toString(NETWORK_MIN_CLASS));

        if (Boolean.parseBoolean(Constants.props.getProperty("core.envelope.useValidation", "false"))) {
            log.info("Envelope Validation is enabled");
        } else {
            log.info("Envelope Validation is disabled");
        }
        standalone = Boolean.parseBoolean(Constants.props.getProperty("jms.standalone", "true"));
        messageBrokerUrl = Constants.props.getProperty("jms.url", "vm://localhost?jms.alwaysSyncSend=false");

        startupInputProcess();
        startupHeartbeatProcess();
        startCoreProcess();
        startupAuthenticationProcess();

        internalListeners = new ConcurrentHashMap<>();

        if (standalone) {
            setupEmbeddedBroker();
        }

        destMgr = new JMSDestinationManager();
        ServiceLocator.setDestinationManager(destMgr);

        setupInputQueueConsumer();
        setupHeartbeatQueueConsumer();
        setupCoreQueueConsumer();
        setupAuthenticationQueueConsumer();
        registerServices();

        inputProcessingPool.resume();
        heartbeatProcessingPool.resume();
        coreProcessingPool.resume();
        authenticationProcessingPool.resume();
        WebServer.getInstance();

        startupSecurity();
        startupNameServers();
        startupFileServer();

        log.info("Network Manager Started with INPUT_THREADS[{}], CORE_THREADS[{}], and AUTHENTICATION_THREADS[{}]",
                INPUT_THREADS, CORE_THREADS, AUTHENTICATION_THREADS);
        log.info("Silkwave Server has started successfully");
    }

    private void startupFileServer() throws ServiceException {
        String fileServerStart = Constants.props.getProperty("service.fileserver.start", "true");
        if (Boolean.parseBoolean(fileServerStart)) {
            fileServer = new SimpleFileServer();
            fileServer.startup();
            ServiceLocator.addService(ServiceLocator.FILE_SERVER, fileServer);
        }
    }


    private void startupSecurity() {
        if (!"off".equalsIgnoreCase(SECURITY_STATE)) {
            securityService.startupSecurityNameServer();
            authentication.registerWithSecurity();
            extendedAuthentication.registerWithSecurity();
            authorization.registerWithSecurity();
        }
    }

    private void registerServices() throws ServiceException {

        ServiceLocator.addService(ServiceLocator.NETWORK_MANAGER, this);

        registry = new LocalRegistry();
        ServiceLocator.addService(ServiceLocator.LOCAL_REGISTRY, registry);

        routing = new RoutingService();
        ServiceLocator.addService(ServiceLocator.ROUTING, routing);

        discovery = new DiscoveryService();
        ServiceLocator.addService(ServiceLocator.DISCOVERY, discovery);

        naming = new NamingRegistry();
        ServiceLocator.addService(ServiceLocator.NAMING, naming);

        registrar = new Registrar();
        ServiceLocator.addService(ServiceLocator.REGISTRAR, registrar);

        networkStatus = new NetworkStatusService();
        ServiceLocator.addService(ServiceLocator.NETWORK_STATUS, networkStatus);

        networkInfo = new NetworkInfoService();
        ServiceLocator.addService(ServiceLocator.NETWORK_INFO, networkInfo);

        if (!"off".equalsIgnoreCase(SECURITY_STATE)) {
            startupRestrictionManager();

            // create the security only name server
            securityService = new SecurityService();
            ServiceLocator.addService(ServiceLocator.SECURITY, securityService);

            authentication = new AuthenticationService();
            ServiceLocator.addService(ServiceLocator.AUTHENTICATION, authentication);

            extendedAuthentication = new ExtendedAuthenticationService();
            ServiceLocator.addService(ServiceLocator.EXTENDED_AUTHENTICATION, extendedAuthentication);

            authorization = new AuthorizationService();
            ServiceLocator.addService(ServiceLocator.AUTHORIZATION, authorization);

            securityService.startup();
            authentication.startup();
            extendedAuthentication.startup();
            authorization.startup();
        }

        files = new FileService();
        ServiceLocator.addService(ServiceLocator.FILES, files);

        streams = new StreamService();
        ServiceLocator.addService(ServiceLocator.STREAMS, streams);

        registry.startup();
        discovery.startup();
        naming.startup();
        registrar.startup();
        networkStatus.startup();
        networkInfo.startup();
        routing.startup();
        files.startup();
        streams.startup();

    }

    private void startupRestrictionManager() {
        restrictionManager = new RestrictionManager();
        ServiceLocator.setRestrictionManager(restrictionManager);
        try {
            restrictionManager.loadRestrictions(RESTRICTIONS_FILE);
        } catch (ParserConfigurationException | SAXException | IOException | JAXBException e1) {
            log.error("Problem loading restrictions", e1);
        }
        scheduler.scheduleAtFixedRate(() -> {
            try {
                File file = new File(RESTRICTIONS_FILE);
                if (file.exists() && file.lastModified() > lastTime) {
                    lastTime = file.lastModified();
                    log.info("Loading new restrictions file");
                    restrictionManager.loadRestrictions(RESTRICTIONS_FILE);
                }
            } catch (Throwable e) {
                log.error("Problem loading restrictions", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void startupNameServers() {
        String domainsProp = Constants.props.getProperty("service.nameserver.domains", "ww,oio.appgeo");
        if (domainsProp != null && !domainsProp.isEmpty()) {
            String[] domains = domainsProp.split(",");
            for (String domain : domains) {

                int statusInterval = Integer.parseInt(
                        Constants.props.getProperty("service.nameserver." + domain + ".statusInterval",
                                Constants.props.getProperty("service.nameserver.statusInterval", "5")));
                int defaultExpiration = Integer.parseInt(
                        Constants.props.getProperty("service.nameserver." + domain + ".defaultExpiration",
                                Constants.props.getProperty("service.nameserver.defaultExpiration", "15000")));
                int maxAge = Integer.parseInt(
                        Constants.props.getProperty("service.nameserver." + domain + ".maxSubscriberInactivity",
                                Constants.props.getProperty("service.nameserver.maxSubscriberInactivity", "630000")));
                int maxNSAge = Integer.parseInt(
                        Constants.props.getProperty("service.nameserver." + domain + ".maxNSInactivity",
                                Constants.props.getProperty("service.nameserver.maxNSInactivity", "630000")));
                int timeout = Integer.parseInt(
                        Constants.props.getProperty("service.nameserver." + domain + ".requestTimeout",
                                Constants.props.getProperty("service.nameserver.requestTimeout", "5000")));

                String userName = Constants.props.getProperty("service.nameserver." + domain + ".user",
                        Constants.props.getProperty("service.nameserver.user",
                                Constants.props.getProperty("client.user", "client")));
                String password = Constants.props.getProperty("service.nameserver." + domain + ".password",
                        Constants.props.getProperty("service.nameserver.pass",
                                Constants.props.getProperty("client.pass", "manager")));
                String url = Constants.props.getProperty("service.nameserver." + domain + ".url",
                        Constants.props.getProperty("service.nameserver.url",
                                Constants.props.getProperty("client.url", "vm://localhost?jms.alwaysSyncSend=false")));

                NameServer nameServer = new NameServer("ns:" + domain, userName, password, url, statusInterval,
                        defaultExpiration, maxAge, maxNSAge, timeout);
                try {
                    nameServer.connect();
                    NameServerLocator.putNameServer(domain, nameServer);
                } catch (TransportException e) {
                    log.error("Unable to start nameserver with domain [{}]", domain, e);
                    //TODO: should this try more than once?
                }
            }
        }
    }

    private void startupInputProcess() {
        inputProcessingQue = new LinkedBlockingDeque<>(INPUT_THREADS * 4);
        inputProcessingPool = new PausableThreadPoolExecutor(INPUT_THREADS, INPUT_THREADS, 365, TimeUnit.DAYS,
                inputProcessingQue, new ThreadPoolExecutor.AbortPolicy());
        inputProcessingPool.pause();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("InputProcessingQue-%d").build();
        inputProcessingPool.setThreadFactory(namedThreadFactory);
        inputProcessingPool.prestartAllCoreThreads();
    }

    private void startupHeartbeatProcess() {
        heartbeatProcessingQue = new LinkedBlockingDeque<>(HEARTBEAT_THREADS * 4);
        heartbeatProcessingPool = new PausableThreadPoolExecutor(HEARTBEAT_THREADS, HEARTBEAT_THREADS, 365,
                TimeUnit.DAYS, heartbeatProcessingQue, new ThreadPoolExecutor.AbortPolicy());
        heartbeatProcessingPool.pause();

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("HeartbeatProcessingQue-%d")
                .build();
        heartbeatProcessingPool.setThreadFactory(namedThreadFactory);
        heartbeatProcessingPool.prestartAllCoreThreads();
    }

    private void startCoreProcess() {
        coreProcessingQue = new LinkedBlockingDeque<>(CORE_THREADS * 4);
        coreProcessingPool = new PausableThreadPoolExecutor(CORE_THREADS, CORE_THREADS, 365, TimeUnit.DAYS,
                coreProcessingQue, new ThreadPoolExecutor.AbortPolicy());
        coreProcessingPool.pause();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("CoreProcessingQue-%d").build();
        coreProcessingPool.setThreadFactory(namedThreadFactory);
        coreProcessingPool.prestartAllCoreThreads();
    }

    private void startupAuthenticationProcess() {
        authenticationProcessingQue = new LinkedBlockingDeque<>(AUTHENTICATION_THREADS * 4);
        authenticationProcessingPool = new PausableThreadPoolExecutor(AUTHENTICATION_THREADS, AUTHENTICATION_THREADS,
                365, TimeUnit.DAYS, authenticationProcessingQue, new ThreadPoolExecutor.AbortPolicy());
        authenticationProcessingPool.pause();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("AuthenticationProcessingQue-%d")
                .build();
        authenticationProcessingPool.setThreadFactory(namedThreadFactory);
        authenticationProcessingPool.prestartAllCoreThreads();
    }

    public static List<InetAddress> getAddressesForIface(String ifaceName) throws SocketException {
        List<InetAddress> addrs = new ArrayList<>();
        if (ifaceName == null) {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();

                List<InterfaceAddress> addresses = iface.getInterfaceAddresses();

                for (InterfaceAddress addr : addresses) {
                    addrs.add(addr.getAddress());
                }
                break;
            }
        } else {
            NetworkInterface iface = NetworkInterface.getByName(ifaceName);

            List<InterfaceAddress> addresses = iface.getInterfaceAddresses();

            for (InterfaceAddress addr : addresses) {
                addrs.add(addr.getAddress());
            }

        }
        return addrs;
    }

    private void setupEmbeddedBroker() throws TransportException {
        log.info("Establishing embedded AMQ Broker");
        try {
            boolean file = Boolean.parseBoolean(Constants.props.getProperty("jms.standalone.useFile", "true"));
            if (file) {
                String url = Constants.props.getProperty("jms.standalone.configFile");
                if (url == null) {
                    url = "xbean:activemq.xml";
                } else {
                    url = "xbean:file:" + url;
                }
                embeddedBroker = BrokerFactory.createBroker(url);
            } else {
                // TODO: This should be removed so we only have one
                // configuration to setup.
                // This message broker is embedded
                embeddedBroker = new BrokerService();
                embeddedBroker.setPersistent(false);
                embeddedBroker.setUseJmx(true);

                int connectorPort = Integer.parseInt(
                        Constants.props.getProperty("activemq.managercontext.jmxport", "1098"));
                embeddedBroker.getManagementContext().setConnectorPort(connectorPort);

                embeddedBroker.setPersistent(false);
                // String ssl =
                // Constants.props.getProperty("jms.standalone.ssl",
                // "ssl://0.0.0.0:61617?needClientAuth=true");
                // embeddedBroker.addConnector(ssl);

                String tcp = Constants.props.getProperty("jms.standalone.tcp", "tcp://0.0.0.0:61616");
                log.info("Adding connector on {}.", tcp);

                embeddedBroker.addConnector(tcp);

                embeddedBroker.addConnector("vm://localhost");

                ActiveMQDestination queue = new ActiveMQQueue();
                queue.setPhysicalName("SILKWAVE.Incoming");

                ActiveMQDestination authenticationQueue = new ActiveMQQueue();
                authenticationQueue.setPhysicalName("SILKWAVE.Authentication");

                ActiveMQDestination coreQueue = new ActiveMQQueue();
                coreQueue.setPhysicalName("SILKWAVE.Core");

                // embeddedBroker.setBrokerName("abc");

                embeddedBroker.setDestinations(new ActiveMQDestination[]{queue, authenticationQueue, coreQueue});

                embeddedBroker.setPlugins(new BrokerPlugin[]{new SilkwavePlugin()});
            }
            embeddedBroker.start();
        } catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private void setupInputQueueConsumer() throws TransportException {
        try {
            String queueName = "queue://SILKWAVE.Incoming";
            inputConnection = JMSConnection.getConnection(USER_NAME, PASSWORD, messageBrokerUrl, true, MY_PARTY_NAME);
            ActiveMQConnection conn = (ActiveMQConnection) inputConnection;
            destMgr.createDestination(queueName);
            destMgr.addUserPermission(conn.getConnectionInfo().getConnectionId().getValue(), queueName, "READ");

            inputSession = inputConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ActiveMQDestination queue = new ActiveMQQueue();
            queue.setPhysicalName("SILKWAVE.Incoming");
            inputDestination = queue;

            inputConsumer = inputSession.createConsumer(inputDestination);
            inputConsumer.setMessageListener(
                    new JMSMessageListener(new MyHandler(inputConnection, inputProcessingQue, "Input")));

        } catch (JMSException e) {
            throw new TransportException(e);
        }
    }

    private void setupHeartbeatQueueConsumer() throws TransportException {
        try {
            String queueName = "queue://SILKWAVE.Heartbeat";
            heartbeatConnection = JMSConnection.getConnection(USER_NAME, PASSWORD, messageBrokerUrl, true,
                    MY_PARTY_NAME);
            ActiveMQConnection conn = (ActiveMQConnection) heartbeatConnection;
            destMgr.createDestination(queueName);
            destMgr.addUserPermission(conn.getConnectionInfo().getConnectionId().getValue(), queueName, "READ");

            heartbeatSession = heartbeatConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ActiveMQDestination queue = new ActiveMQQueue();
            queue.setPhysicalName("SILKWAVE.Heartbeat");
            heartbeatDestination = queue;

            heartbeatConsumer = heartbeatSession.createConsumer(heartbeatDestination);
            heartbeatConsumer.setMessageListener(
                    new JMSMessageListener(new MyHandler(heartbeatConnection, heartbeatProcessingQue, "Heartbeat")));

        } catch (JMSException e) {
            throw new TransportException(e);
        }
    }

    private void setupCoreQueueConsumer() throws TransportException {
        try {
            String queueName = "queue://SILKWAVE.Core";
            coreConnection = JMSConnection.getConnection(USER_NAME, PASSWORD, messageBrokerUrl, true, MY_PARTY_NAME);
            ActiveMQConnection conn = (ActiveMQConnection) coreConnection;
            destMgr.createDestination(queueName);
            destMgr.addUserPermission(conn.getConnectionInfo().getConnectionId().getValue(), queueName, "READ");
            coreSession = coreConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ActiveMQDestination queue = new ActiveMQQueue();
            queue.setPhysicalName("SILKWAVE.Core");
            coreDestination = queue;

            coreConsumer = coreSession.createConsumer(coreDestination);
            coreConsumer.setMessageListener(
                    new JMSMessageListener(new MyHandler(coreConnection, coreProcessingQue, "Core")));
        } catch (JMSException e) {
            throw new TransportException(e);
        }
    }

    private void setupAuthenticationQueueConsumer() throws TransportException {
        try {
            String queueName = "queue://SILKWAVE.Authentication";
            authenticationConnection = JMSConnection.getConnection(USER_NAME, PASSWORD, messageBrokerUrl, true,
                    MY_PARTY_NAME);
            ActiveMQConnection conn = (ActiveMQConnection) authenticationConnection;
            destMgr.createDestination(queueName);
            destMgr.addUserPermission(conn.getConnectionInfo().getConnectionId().getValue(), queueName, "READ");
            authenticationSession = authenticationConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ActiveMQDestination queue = new ActiveMQQueue();
            queue.setPhysicalName("SILKWAVE.Authentication");
            authenticationDestination = queue;

            authenticationConsumer = authenticationSession.createConsumer(authenticationDestination);
            authenticationConsumer.setMessageListener(new JMSMessageListener(
                    new MyHandler(authenticationConnection, authenticationProcessingQue, "Authentication")));
        } catch (JMSException e) {
            throw new TransportException(e);
        }
    }

    @Override
    public void shutdown() {
        // Shutdown the webserver.
        WebServer.shutdown();

        // Shutdown all of the NameServers
        for (String domain : NameServerLocator.getNameServerDomains()) {
            NameServer ns = NameServerLocator.getNameServer(domain);
            if (ns != null) {
                ns.disconnect();
            }
        }

        registrar.shutdown();

        // this.policy.shutdown();
        // this.destMgr.shutdown();

        naming.shutdown();
        discovery.shutdown();
        routing.shutdown();
        networkStatus.shutdown();
        registry.shutdown();
        files.shutdown();

        streams.shutdown();

        if (fileServer != null) {
            fileServer.shutdown();
        }


        inputProcessingPool.shutdownNow();
        heartbeatProcessingPool.shutdownNow();
        coreProcessingPool.shutdownNow();
        authenticationProcessingPool.shutdownNow();
        try {
            inputConnection.close();
        } catch (JMSException e1) {
            // ignore
        }

        try {
            heartbeatConnection.close();
        } catch (JMSException e1) {
            // ignore
        }

        try {
            coreConnection.close();
        } catch (JMSException e1) {
            // ignore
        }

        try {
            authenticationConnection.close();
        } catch (JMSException e1) {
            // ignore
        }

        if (embeddedBroker != null) {
            try {
                log.info("Shutting down broker.");
                embeddedBroker.stop();
            } catch (Exception e) {
                log.error("shutdown", e);
            }
        }
    }

    class MyHandler implements IRealTimeMessageHandler {

        private final Connection connection;
        private final BlockingQueue<Runnable> processingQue;
        private final String name;

        protected MyHandler(Connection connection, BlockingQueue<Runnable> processingQue, String name) {
            this.connection = connection;
            this.processingQue = processingQue;
            this.name = name;
        }

        @Override
        public void onMessage(Envelope envelope) {
            try {
                count.incrementAndGet();
                envelope.getProps().put("msg.handling", name);
                if (log.isTraceEnabled()) {
                    log.trace("Received new message ({}) from {} and the processing queue can handle {} messages.",
                            envelope.getInternalMessageId(), name, processingQue.remainingCapacity());
                }
                processingQue.put(new IncomingMessageHandler(envelope, connection));
            } catch (InterruptedException e1) {
                log.warn(LoggerHelper.generateStandardMessage(envelope, "Interrupt moving message to processingQue"));
            } catch (Throwable ex) {
                log.error("Unhandled Exception", ex);
            }
        }

        @Override
        public void onError(Throwable error, Envelope envelope) {
            count.incrementAndGet();
            sendDeadLetter(envelope, null, envelope.getInvalidReason(), DeadLetterTypeEnum.BAD_ENVELOPE);
        }

        @Override
        public void onTransportInterrupted() {
            log.info("Transport Interupted");

        }

        @Override
        public void onTransportResumed(String netId, boolean reconnected) {
            log.info("Transport Resumed");

        }
    }

    /**
     * Send core initiated requests to the transport (name lookups, need-to-know, etc)
     *
     * @param envelope message to send
     *
     * @throws TransportException
     */
    public void sendOutgoing(Envelope envelope) throws TransportException {
        MessageSender sender = JMSConnection.getMessageSender(inputConnection);
        deliver(envelope, sender);
    }

    /**
     * Create a virtual destination so that components of core can receive directed messages
     *
     * @param identity the virtual routing identity (e.g. net:hub:naming)
     * @param callback the callback for this destination
     *
     * @return true if successful
     */
    public boolean registerInternalListener(RoutingURI identity, IRealTimeMessageHandler callback) {
        // allow internal component to be a virtual routing endpoint
        log.info("Registered virtual destination {}", identity.toString());
        return internalListeners.putIfAbsent(identity.toString(), callback) == null;
    }

    /**
     * Create a virtual destination so that components of core can receive directed messages
     *
     * @param endpoint the virtual endpoint (e.g. "naming", will create virtual destinations for net:hubname:naming and
     *                 net:local:naming)
     * @param callback the callback for this destination
     *
     * @return true if successful
     */
    public boolean registerInternalListener(String endpoint, IRealTimeMessageHandler callback) {
        // allow internal component to be a virtual routing endpoint
        log.info("Registered virtual destination {}", endpoint);
        return internalListeners.putIfAbsent(
                RoutingURI.build("net:" + Constants.MYDOMAIN + ":" + endpoint, log).toString(),
                callback) == null && internalListeners.putIfAbsent(
                RoutingURI.build("net:local:" + endpoint, log).toString(), callback) == null;
    }

    /*
     * private RoutingURI selectAnyEndpoint(NameResolution nr, RoutingURI
     * sender) { // TODO: the thing selected could be cached somewhere for
     * repeated use RoutingURI selected = null; List<RoutingURI> remote = new
     * ArrayList<RoutingURI>(); for (RoutingURI ep : nr.getEndpoints()) { //
     * prevent an ANY from resolving to the sender and bouncing back to them if
     * (ep.isLocalHub()) { if (!ep.equals(sender)) { selected = ep; break; } }
     * else { remote.add(ep); } } if (selected == null) { // TODO: ask routing
     * service which remote is nearest if (!remote.isEmpty()) { selected =
     * remote.get(0); } } return selected; }
     */

    private void deliver(Envelope envelope, MessageSender sender) {
        // route lookup and delivery

        PayloadDestination destination = (PayloadDestination) envelope.getProps().get("dest");
        if (destination != null) {
            // message from hub to client using temporary destination
            try {
                sender.send(envelope.getClientXml(), envelope.getProps(), envelope.getMapContents(), destination);
            } catch (Exception ex) {
                if (!envelope.getDestinations().isEmpty()) {
                    sendDeadLetterAndLogException(envelope, RoutingURI.build(envelope.getDestinations().get(0), log),
                            "Delivery Failure", DeadLetterTypeEnum.NOT_DELIVARABLE, ex);
                }
            }
        } else {
            // gather the list of destinations
            Set<String> dests;
            if (envelope.getScratchPad() != null) {
                if (envelope.getScratchPad().getHubs().contains(Constants.MYDOMAIN)) {
                    // JC-641 Stack Trace requested for this condition
                    log.warn("Stack trace for Loop Detected in deliver attempt", new Exception("Domain Loop"));
                    // potential loop detected
                    sendDeadLetter(envelope, null,
                            "Loop Detected - Message has already passed through " + Constants.MYDOMAIN,
                            DeadLetterTypeEnum.NOT_ROUTABLE);
                    return;
                }
                dests = new HashSet<>(envelope.getScratchPad().getDestinations());
            } else {
                dests = new HashSet<>(envelope.getDestinations());
                dests.addAll(envelope.getBlindDestinations());
            }

            if (log.isDebugEnabled()) {
                log.debug(LoggerHelper.generateStandardMessage(envelope, "Destinations = " + dests));
            }

            RoutingURI src = RoutingURI.build(envelope.getSource(), log);

            Set<LocalRoute> local = new HashSet<>();
            Map<LocalRoute, Set<String>> routed = new HashMap<>();

            // 1. resolve all resource names
            // 2. validate that local nets exist, remote nets are routeable
            // 3. remove duplicate nets
            // 4. check all local nets for NTK
            // 5. deliver all local nets
            // 6. if remote nets exist
            // write scratchpad info
            // resolve all remote nets to next hop, remove duplicates
            // deliver all next hops

            // resolve logical names / validate physical endpoints
            for (String dest : dests) {
                RoutingURI uri = RoutingURI.build(dest, log);
                if (uri != null) {
                    switch (uri.getScheme()) {

                        case ns:
                        case localns:
                            NameResolution nr = naming.resolve(uri, envelope.getSecurity());
                            if (nr != null && nr.isResolved()) {
                                if (nr.isAll()) {
                                    // resolves to 1+ endpoints
                                    int cnt = 0;
                                    for (RoutingURI endpoint : nr.getEndpoints()) {
                                        // don't send an ALL back to the originator
                                        if (!src.equals(endpoint)) {
                                            processResolvedEndpoint(envelope, uri, endpoint, local, routed);
                                            cnt++;
                                        }
                                    }
                                    if (nr.getEndpoints().isEmpty()) {
                                        sendDeadLetter(envelope, uri,
                                                "Unable to resolve ALL to routeable net(s): " + uri,
                                                DeadLetterTypeEnum.NOT_RESOLVABLE);
                                    }
                                } else if (nr.isAny()) {
                                    // resolves to 1 of many
                                    RoutingURI endpoint = null;
                                    if (nr.getEndpoints() != null && !nr.getEndpoints().isEmpty()) {
                                        endpoint = nr.getEndpoints().get(0);
                                        // If the cheapest endpoint is the sender we
                                        // need to go to the next endpoint.
                                        if (endpoint.equals(src.getEndpoint())) {
                                            if (1 < nr.getEndpoints().size()) {
                                                endpoint = nr.getEndpoints().get(1);
                                            } else {
                                                endpoint = null;
                                            }
                                        }
                                    }
                                    if (endpoint != null) {
                                        IRealTimeMessageHandler virtual = internalListeners.get(
                                                endpoint.toStringNormalized());
                                        if (virtual != null) {
                                            if (log.isTraceEnabled()) {
                                                log.trace(LoggerHelper.generateStandardMessage(envelope,
                                                        "Provide message to internal listener: " + uri.getEndpoint()));
                                            }
                                            virtual.onMessage(envelope);
                                        } else {
                                            processResolvedEndpoint(envelope, uri, endpoint, local, routed);
                                        }
                                    } else {
                                        sendDeadLetter(envelope, uri, "Unable to resolve ANY to routeable net: " + uri,
                                                DeadLetterTypeEnum.NOT_RESOLVABLE);
                                    }
                                } else {
                                    // resolves to single endpoint
                                    if (!nr.getEndpoints().isEmpty()) {
                                        RoutingURI endpoint = nr.getEndpoints().iterator().next();
                                        processResolvedEndpoint(envelope, uri, endpoint, local, routed);
                                    } else {
                                        sendDeadLetter(envelope, uri,
                                                "Unable to resolve EXCLUSIVE to routeable net: " + uri,
                                                DeadLetterTypeEnum.NOT_RESOLVABLE);
                                    }
                                }
                            } else {
                                sendDeadLetter(envelope, uri, "Unresolvable Resource Identity: " + uri,
                                        DeadLetterTypeEnum.NOT_RESOLVABLE);
                            }
                            break;

                        case net:
                        case localnet:
                            if (uri.isLocalHub()) {
                                LocalRoute rr = registrar.find(uri);
                                if (rr != null) {
                                    local.add(rr);
                                } else {
                                    IRealTimeMessageHandler virtual = internalListeners.get(uri.toStringNormalized());
                                    if (virtual != null) {
                                        if (log.isTraceEnabled()) {
                                            log.trace(LoggerHelper.generateStandardMessage(envelope,
                                                    "Provide message to internal listener: " + uri.getEndpoint()));
                                        }
                                        virtual.onMessage(envelope);
                                    } else {
                                        sendDeadLetter(envelope, uri, "Unknown Destination Identity: " + uri,
                                                DeadLetterTypeEnum.NOT_DELIVARABLE);
                                    }
                                }
                            } else if (!uri.isLocalNet()) {
                                RouteOption link = routing.getPrimaryLinkTo(uri.getDomain());
                                if (link != null) {
                                    // found a matching neighbor
                                    LocalRoute rr = registrar.find(link.getNeighborURI());
                                    if (rr != null) {
                                        Set<String> list = routed.computeIfAbsent(rr, k -> new HashSet<>());
                                        list.add(uri.toString());
                                    } else {
                                        sendDeadLetter(envelope, uri, "Unknown Destination Identity: " + uri,
                                                DeadLetterTypeEnum.NOT_DELIVARABLE);
                                    }
                                } else {
                                    sendDeadLetter(envelope, uri, "No link found to domain: " + uri.getDomain(),
                                            DeadLetterTypeEnum.NOT_ROUTABLE);
                                }
                            } else {
                                sendDeadLetter(envelope, uri,
                                        uri + " is not locally routeable on hub [" + Constants.MYDOMAIN + "]",
                                        DeadLetterTypeEnum.NOT_ROUTABLE);
                            }
                            break;
                        default:
                            sendDeadLetter(envelope, uri, "Scheme " + uri.getScheme() + " not routeable (" + uri + ")",
                                    DeadLetterTypeEnum.NOT_ROUTABLE);
                            break;
                    }
                }
            }

            // stop now if nothing to route to
            if (local.isEmpty() && routed.isEmpty()) {
                return;
            }

            // now check NTK on all local endpoints
            Restriction ntk = envelope.getRestrictions();
            if (ntk != null) {
                String ntkStr = RestrictionManipulator.toString(ntk);
                List<LocalRoute> failed = new ArrayList<>();
                for (LocalRoute route : local) {
                    if (securityService == null || !(securityService.checkAuthorization(envelope.getSource(),
                            restrictionManager.getRestriction("BypassRestrictionPermission"),
                            envelope.getSecurity()) || securityService.checkAuthorization(route.toString(), ntk,
                            envelope.getSecurity()))) {
                        failed.add(route);
                        sendDeadLetter(envelope, route.getRoutingURI(), "Failed Restriction Check: " + ntkStr,
                                DeadLetterTypeEnum.NOT_DELIVARABLE);
                        classificationAuditLog.error("NetId:{}Failed Restriction Check: {} for message delivery",
                                route.getRoutingURI(), ntkStr);
                    } else if (log.isTraceEnabled()) {
                        log.trace(LoggerHelper.generateStandardMessage(envelope,
                                route.getRoutingURI() + " passed restriction check: " + ntkStr));
                    }
                }
                failed.forEach(local::remove);
            }

            // send all locals
            if (!local.isEmpty()) {
                if (log.isTraceEnabled()) {
                    log.trace(LoggerHelper.generateStandardMessage(envelope, "Deliver Local: " + local));
                }
                send(envelope, sender, local, true);
            }

            // send all remotes one neighbor at a time
            if (!routed.isEmpty()) {
                ScratchPad pad = envelope.getScratchPad();
                if (pad == null) {
                    pad = new ScratchPad();
                    String handling = (String) envelope.getProps().get("msg.handling");
                    if (handling != null) {
                        pad.setHandling(handling);
                    }
                    envelope.setScratchPad(pad);
                }
                // add this hub to the list for loop prevention
                pad.getHubs().add(Constants.MYDOMAIN);
                for (Entry<LocalRoute, Set<String>> ent : routed.entrySet()) {
                    pad.getDestinations().clear();
                    // add the list of destinations to be delivered via this
                    // link
                    pad.getDestinations().addAll(ent.getValue());
                    if (log.isTraceEnabled()) {
                        log.trace(LoggerHelper.generateStandardMessage(envelope,
                                "Deliver to " + ent.getValue() + " via " + ent.getKey()));
                    }
                    send(envelope, sender, Collections.singletonList(ent.getKey()), false);
                }
            }
        }
    }

    private void processResolvedEndpoint(Envelope envelope, RoutingURI uri, RoutingURI endpoint,
            Collection<LocalRoute> local, Map<LocalRoute, Set<String>> routed) {
        if (endpoint.isLocalHub()) {
            LocalRoute rr = registrar.find(endpoint);
            if (rr != null) {
                local.add(rr);
            } else {
                sendDeadLetter(envelope, uri, "Unknown Destination Identity: " + endpoint,
                        DeadLetterTypeEnum.NOT_DELIVARABLE);
            }
        } else if (!uri.isLocalNs()) {
            // resolve to remote endpoints if !localns
            RouteOption link = routing.getPrimaryLinkTo(endpoint.getDomain());
            if (link != null) {
                // found a matching neighbor
                LocalRoute rr = registrar.find(link.getNeighborURI());
                if (rr != null) {
                    Set<String> dests = routed.computeIfAbsent(rr, k -> new HashSet<>());
                    dests.add(endpoint.toString());
                } else {
                    sendDeadLetter(envelope, uri, "Unknown Destination Identity: " + endpoint,
                            DeadLetterTypeEnum.NOT_DELIVARABLE);
                }
            } else {
                sendDeadLetter(envelope, uri, "No link found to domain: " + endpoint.getDomain(),
                        DeadLetterTypeEnum.NOT_ROUTABLE);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(LoggerHelper.generateStandardMessage(envelope,
                        "Suppressing endpoint " + endpoint + " resolved from " + uri));
            }
        }
    }

    private void send(Envelope envelope, MessageSender sender, Collection<LocalRoute> dests, boolean local) {
        // deliver message to all validated recipients
        // obj.setSessionToken(null); // clear private session token?
        String message;
        if (local) {
            message = envelope.getClientXml();
        } else {
            message = envelope.getHubXml();
        }
        if (1 < dests.size()) {
            if (log.isTraceEnabled()) {
                log.trace(LoggerHelper.generateStandardMessage(envelope,
                        "Route message from " + envelope.getSource() + " to " + dests.size() + " recipients"));
            }
            if (COMPOSITE_SENDS) {
                // use composite destination for better performance when
                // multiple recipients
                // TODO abstract composite destinations so any destination
                // type can produce one
                List<JmsDestination> dlist = new ArrayList<>();
                StringBuilder distBuilder = new StringBuilder();
                String prefix = "";
                for (LocalRoute rte : dests) {
                    distBuilder.append(prefix);
                    distBuilder.append(rte.getRoutingURI().toString());
                    prefix = ",";
                    dlist.add((JmsDestination) rte.getDestination());
                }
                JmsDestination comp = JmsDestination.buildCompositeDestination(dlist);

                try {
                    sender.send(message, envelope.getProps(), envelope.getMapContents(), comp);
                    TrackingLogger.messageDelivered(envelope, distBuilder.toString());
                } catch (JMSException ex) {
                    sendDeadLetterAndLogException(envelope, null, "Composite Delivery Failure",
                            DeadLetterTypeEnum.NOT_DELIVARABLE, ex);
                }

            } else {
                // individual send per recipient
                for (LocalRoute rte : dests) {
                    PayloadDestination dest = rte.getDestination();
                    try {
                        if (dest != null) {
                            sender.send(message, envelope.getProps(), envelope.getMapContents(), dest);
                            TrackingLogger.messageDelivered(envelope, rte.getRoutingURI().toString());
                        } else {
                            sendDeadLetter(envelope, rte.getRoutingURI(),
                                    "Individual Delivery Failure: " + rte.getRoutingURI()
                                            .toString() + " - NULL DESTINATION", DeadLetterTypeEnum.UNKNOWN);
                        }
                    } catch (Exception ex) {
                        sendDeadLetterAndLogException(envelope, rte.getRoutingURI(),
                                "Individual Delivery Failure: " + rte.getRoutingURI().toString(),
                                DeadLetterTypeEnum.NOT_DELIVARABLE, ex);
                    }
                }
            }
        } else if (!dests.isEmpty()) {
            // single recipient
            LocalRoute route = dests.iterator().next();
            PayloadDestination dest = route.getDestination();
            // TODO: what if dest is null for some reason
            if (log.isTraceEnabled()) {
                log.trace(LoggerHelper.generateStandardMessage(envelope,
                        "Route message from " + envelope.getSource() + " to " + route.getRoutingURI()
                                .toString() + "( " + route.getDestination() + ")"));
            }
            try {
                if (dest != null) {
                    sender.send(message, envelope.getProps(), envelope.getMapContents(), dest);
                    TrackingLogger.messageDelivered(envelope, route.getRoutingURI().toString());
                } else {
                    sendDeadLetter(envelope, route.getRoutingURI(),
                            "Delivery Failure: " + route.getRoutingURI().toString() + " - NULL DESTINATION",
                            DeadLetterTypeEnum.UNKNOWN);
                }
            } catch (Exception ex) {
                sendDeadLetterAndLogException(envelope, route.getRoutingURI(),
                        "Delivery Failure: " + route.getRoutingURI().toString(), DeadLetterTypeEnum.NOT_DELIVARABLE,
                        ex);
            }
        }
    }

    // JC-641 Don't generate stack trace for NOT_RESOLVABLE Dead Letters, these
    // never have exceptions
    private void sendDeadLetter(Envelope envelope, RoutingURI dest, String reason, DeadLetterTypeEnum errorType) {
        DeadLetterManager.getInstance().process(envelope, dest, reason, errorType);
    }

    // JC-641 Renamed error metod to be more explicit about what the method does
    private void sendDeadLetterAndLogException(Envelope envelope, RoutingURI dest, String reason,
            DeadLetterTypeEnum errorType, Exception ex) {
        if (ex != null) {
            log.warn("Exception thrown while handling message", ex);
        } else {
            // JC-641 Request was for stack trace, so generate one when there is no
            // exception.
            log.warn("Non-Exception error handling message", new Exception("handlingmessagestack"));
        }
        DeadLetterManager.getInstance().process(envelope, dest, reason, errorType);
    }

    /**
     * Runnable task to pass to thread pool
     */
    private class IncomingMessageHandler implements Runnable {
        /** Message Payload Input */
        private final Envelope envelope;
        private final Connection connection;

        /**
         * Constructor
         *
         * @param envelope   received message to process
         * @param connection received message to process
         */
        IncomingMessageHandler(Envelope envelope, Connection connection) {
            this.envelope = envelope;
            this.connection = connection;
        }

        // local help function for determining if dest is allowable for the source
        private boolean authorizedInternalService() {
            // not allowable if multiple destinations (clearly not single internal service)
            if (1 != envelope.getDestinations().size()) {
                return false;
            }

            // if the destination is not even in our internal list, no need to continue here - not internal service
            if (internalListeners.get(envelope.getDestinations().get(0).toLowerCase()) == null) {
                return false;
            }

            // if security is not ON (i.e., 'optional' or 'off') then it is ok to send to any internal service
            if (!"on".equalsIgnoreCase(SECURITY_STATE)) {
                return true;
            }

            // else, security is on, so only allow certain internal services

            // going to compare against ServiceLocator URIs, however those all
            // have domain name in them, and source may have sent to either the domain
            // or 'local' (correct?), so we will compare the endpoints (we can
            // do this because we have already confirmed the dest as an internal
            // listener above (there it is stored as both 'local' and with domain)

            // convert dest to uri (so we can get end point)
            RoutingURI uri = RoutingURI.build(envelope.getDestinations().get(0), log);
            if (compareEndpoints(uri, ServiceLocator.REGISTRAR)) {
                return true;
            }
            return compareEndpoints(uri, ServiceLocator.SECURITY);

            // individual tests failed
            // return 'not allowed'
        }

        // local helper function
        private boolean compareEndpoints(RoutingURI one, RoutingURI two) {
            return one.getEndpoint().equalsIgnoreCase(two.getEndpoint());
        }

        @Override
        public void run() {
            try {
                TrackingLogger.messageReceived(envelope);
                if (log.isTraceEnabled()) {
                    log.trace("Run Task ({})", envelope.getInternalMessageId());
                }
                if (envelope != null) {

                    if (envelope.getDestinations().isEmpty()) {
                        sendDeadLetter(envelope, null, "Envelope must have a destination",
                                DeadLetterTypeEnum.BAD_ENVELOPE);
                        return;
                    }

                    Security sec = envelope.getSecurity();
                    if (!SecurityManipulator.isValid(sec)) {
                        if (SecurityManipulator.isClassificationValidatorLoggingEnabled()) {
                            classificationAuditLog.error("NetId:{} SessionToken:{} invalid classification: {}",
                                    envelope.getSource(), envelope.getSessionToken(),
                                    SecurityManipulator.toString(sec));
                        }
                        if (SecurityManipulator.isClassificationValidatorEnabled()) {
                            sendDeadLetter(envelope, null,
                                    "Envelope failed valid classification check! Envelope Classification: " + SecurityManipulator.toString(
                                            sec), DeadLetterTypeEnum.BAD_ENVELOPE);
                            return;
                        }

                    } else if (!SecurityManipulator.isAcceptable(NETWORK_HIGH_CLASS, sec)) {
                        if (SecurityManipulator.isClassificationValidatorLoggingEnabled()) {
                            classificationAuditLog.error("NetId:{} SessionToken:{} SystemHighClass:{} MessageClass:{}",
                                    envelope.getSource(), envelope.getSessionToken(),
                                    SecurityManipulator.toString(NETWORK_HIGH_CLASS),
                                    SecurityManipulator.toString(sec));
                        }
                        if (SecurityManipulator.isClassificationValidatorEnabled()) {
                            sendDeadLetter(envelope, null,
                                    "Envelope failed classification check! System High Class: " + SecurityManipulator.toString(
                                            NETWORK_HIGH_CLASS) + " Message Class: " + SecurityManipulator.toString(
                                            sec), DeadLetterTypeEnum.BAD_ENVELOPE);
                            return;
                        }
                    }

                    RoutingURI dest1 = RoutingURI.build(envelope.getDestinations().get(0), log);

                    if (dest1 != null && dest1.isLocalHub() && Registrar.ID.equals(dest1.getEndpoint())) {
                        // process messages directed at the local registrar
                        registrar.process(envelope);
                    } else if (registrar.validateSource(envelope)) {
                        RoutingURI source = RoutingURI.build(envelope.getSource(), log);
                        // source has been validated against registry

                        // delivery tests:
                        if (!"on".equalsIgnoreCase(SECURITY_STATE) // if security is not ON (i.e., 'optional' or 'off')
                                // source domain is not this domain (i.e., message is from another domain?)
                                || !source.getDomain()
                                .equals(ServiceLocator.NETWORK_MANAGER.getDomain()) || authorizedInternalService() //
                                // message is being sent to allowable internal source
                                || securityService.checkAuthorization(envelope.getSource(),
                                restrictionManager.getRestriction("Messaging"), envelope.getSecurity())) {
                            // source
                            // is
                            // authorized
                            // to
                            // send
                            // messages
                            delivery(envelope);
                        } else {
                            // failed source authorization
                            sendDeadLetter(envelope, null, "Not Authorized to send messages: " + envelope.getSource(),
                                    DeadLetterTypeEnum.NOT_AUTHORIZED);
                            classificationAuditLog.error("NetId:{} Not Authorized to send messages",
                                    envelope.getSource());
                        }
                    } else {
                        // failed source validation
                        sendDeadLetter(envelope, null, "Unable to Authenticate: " + envelope.getSource(),
                                DeadLetterTypeEnum.INVALID_SOURCE);
                    }
                }
            } catch (Throwable ex) {
                log.error("Caught an unexpected exception {}", ex.getMessage(), ex);
            }

            if (log.isTraceEnabled()) {
                log.trace("Completed Task ({})", envelope.getInternalMessageId());
            }
        }

        private void delivery(Envelope envelope) throws TransportException {
            MessageSender sender = JMSConnection.getMessageSender(connection);
            deliver(envelope, sender);
        }
    }

    public static void main(String[] args) {
        NetworkManager network = new NetworkManager();
        try {

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down SILKWAVE.");
                network.shutdown();
                log.info("SILKWAVE shutdown complete.");
            }));

            network.startup();
        } catch (TransportException e) {
            log.warn("TransportException thrown while starting NetworkManager", e);
        } catch (ServiceException e) {
            log.error("Received a ServiceException", e);
            System.exit(1);
        } catch (Throwable ex) {
            log.error("Unhandled Exception", ex);
        }
    }

}
