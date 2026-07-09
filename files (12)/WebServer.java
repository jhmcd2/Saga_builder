package gov.ic.silkwave.web;

import java.util.List;
import java.util.UUID;

import gov.ic.silkwave.common.client.ConnectionManager;
import gov.ic.silkwave.common.messaging.MessageValidationException;
import gov.ic.silkwave.common.transport.TransportException;
import gov.ic.silkwave.common.utils.Constants;
import gov.ic.silkwave.common.utils.SslUtils;
import gov.ic.silkwave.common.xml.DateFromXMLAdapter;
import gov.ic.silkwave.data.files.FileAdminServlet;
import gov.ic.silkwave.data.streams.StreamAdminServlet;
import gov.ic.silkwave.discovery.DiscoveryServiceAdminServlet;
import gov.ic.silkwave.naming.NamingAdminServlet;
import gov.ic.silkwave.networkstatus.NetworkStatusAdminServlet;
import gov.ic.silkwave.registration.RegistrationAdminServlet;
import gov.ic.silkwave.routing.RoutingAdminServlet;
import gov.ic.silkwave.security.SecurityAdminServlet;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import silkwave.net.OutcomeEnum;
import silkwave.net.PingResponse;
import gov.ic.silkwave.web.ResourcesExportServlet;

public class WebServer {
    private static WebServer instance;
    private Server server;
    private static Logger log = LoggerFactory.getLogger(WebServer.class);
    private ConnectionManager cm;
    private String userName;
    private String password;
    private String url;


    private WebServer() {
        try {
            Boolean httpEnabled = Boolean.valueOf(
                    Constants.props.getProperty("service.webserver.http.enabled", "false"));
            Boolean httpsEnabled = Boolean.valueOf(
                    Constants.props.getProperty("service.webserver.https.enabled", "false"));
            boolean casportSimEnabled = Boolean.parseBoolean(
                    Constants.props.getProperty("service.webserver.casportsim.enabled", "false"));

            if (httpEnabled || httpsEnabled) {

                server = new Server();
                String webcontent = Constants.props.getProperty("service.webserver.content.location", "WebContent");
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

                DefaultSessionIdManager idmanager = new DefaultSessionIdManager(server);

                SessionHandler sessionHandler = new SessionHandler();
                sessionHandler.setSessionCookie("JSESSIONID_" + UUID.randomUUID());
                sessionHandler.setSessionIdPathParameterName(sessionHandler.getSessionCookie().toLowerCase());
                sessionHandler.setSessionIdManager(idmanager);
                context.setSessionHandler(sessionHandler);
                context.setContextPath("/");


                context.setResourceBase(webcontent);
                context.addServlet(new ServletHolder(new SilkwaveAdminServlet()), "/SilkwaveAdminServlet");
                context.addServlet(new ServletHolder(new DiscoveryServiceAdminServlet()),
                        "/DiscoveryServiceAdminServlet");
                context.addServlet(new ServletHolder(new NetworkStatusAdminServlet()), "/NetworkStatusAdminServlet");
                context.addServlet(new ServletHolder(new RegistrationAdminServlet()), "/RegistrationAdminServlet");
                context.addServlet(new ServletHolder(new RoutingAdminServlet()), "/RoutingAdminServlet");
                context.addServlet(new ServletHolder(new SecurityAdminServlet()), "/SecurityAdminServlet");
                context.addServlet(new ServletHolder(new NamingAdminServlet()), "/NamingAdminServlet");
                context.addServlet(new ServletHolder(new NamingServiceAdminServlet()), "/NamingServiceAdminServlet");
                context.addServlet(new ServletHolder(new StreamAdminServlet()), "/StreamAdminServlet/*");
                context.addServlet(new ServletHolder(new FileAdminServlet()), "/FileAdminServlet/*");
                context.addServlet(new ServletHolder(new HealthServlet()), "/health");
                context.addServlet(DefaultServlet.class, "/");
                ServletHolder exportHolder = new ServletHolder(new ResourcesExportServlet());
                context.addServlet(exportHolder, "/resources-export/*");
                context.addServlet(exportHolder, "/resources-export");
                //context.addServlet(new ServletHolder(new ResourcesExportServlet()), "/resources-export/*");

                HandlerList handlers = new HandlerList();

                if (!casportSimEnabled) {
                    // if not going to sim casport interface, just register normal handlers
                    handlers.setHandlers(new Handler[]{context, new DefaultHandler()});
                } else {
                    // going to sim casport interface, create new handler and add with others
                    log.info("CasportSim support enabled");
                    ServletContextHandler casportSimContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
                    casportSimContext.setContextPath("/rest/v3");
                    server.setHandler(casportSimContext);
                    casportSimContext.addServlet(new ServletHolder(new CasportV3SimServlet()), "/*");

                    handlers.setHandlers(new Handler[]{context, casportSimContext, new DefaultHandler()});
                }

                server.setHandler(handlers);

                if (httpsEnabled) {
                    log.info("Starting websever with https support");
                    HttpConfiguration https_config = new HttpConfiguration();
                    https_config.setSecureScheme("https");
                    https_config.setSecurePort(
                            Integer.parseInt(Constants.props.getProperty("service.webserver.http.port", "8601")));
                    https_config.setOutputBufferSize(Integer.parseInt(
                            Constants.props.getProperty("service.webserver.outputbuffersize", "32768")));
                    https_config.addCustomizer(new SecureRequestCustomizer());

                    SslContextFactory.Server sslContextFactory = SslUtils.buildSSLContextFactoryServer(Constants.props,
                            "service.webserver.https");
                    ServerConnector https = new ServerConnector(server,
                            new SslConnectionFactory(sslContextFactory, "http/1.1"),
                            new HttpConnectionFactory(https_config));
                    https.setPort(
                            Integer.parseInt(Constants.props.getProperty("service.webserver.https.port", "8601")));
                    https.setIdleTimeout(Integer.parseInt(
                            Constants.props.getProperty("service.webserver.https.idletimeout", "30000")));
                    if ("true".equals(Constants.props.getProperty("service.webserver.localonly", "false"))) {
                        https.setHost("127.0.0.1");
                    }
                    server.setConnectors(new Connector[]{https});
                } else if (httpEnabled) {
                    log.info("Starting websever with http support");
                    HttpConfiguration http_config = new HttpConfiguration();
                    http_config.setOutputBufferSize(32768);
                    ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
                    http.setPort(Integer.parseInt(Constants.props.getProperty("service.webserver.http.port", "8501")));
                    http.setIdleTimeout(Integer.parseInt(
                            Constants.props.getProperty("service.webserver.http.idletimeout", "30000")));
                    if ("true".equals(Constants.props.getProperty("service.webserver.localonly", "false"))) {
                        http.setHost("127.0.0.1");
                    }

                    ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();

                    //Default constraints
                    Constraint constraint = new Constraint();
                    constraint.setName(Constraint.__FORM_AUTH);
                    constraint.setRoles(new String[]{"user", "admin", "moderator"});
                    constraint.setAuthenticate(true);

                    ConstraintMapping constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/*");

                    securityHandler.addConstraintMapping(constraintMapping);

                    constraint = new Constraint();
                    constraint.setName(Constraint.NONE);
                    constraint.setAuthenticate(false);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/jquery-ui/*");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/css/login.css");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/css/login-error.css");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/jquery/*");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/jquery-ui/*");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/js/login.js");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/js/login-error.js");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/health");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/resources-export/*");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/resources-export");
                    securityHandler.addConstraintMapping(constraintMapping);

                    // Method to disable track/trace to allow clients to pass security scans
                    disableTrackTrace(securityHandler);

                    HashLoginService loginService = new HashLoginService();
                    String realm = Constants.props.getProperty("service.webserver.http.realm.file");
                    if (realm != null) {
                        loginService.setConfig(realm);
                    } else {
                        UserStore userStore = new UserStore();
                        userStore.addUser(Constants.props.getProperty("service.webserver.user", "admin"),
                                new Password(Constants.props.getProperty("service.webserver.pass", "silkwave")),
                                new String[]{"admin"});

                        loginService.setUserStore(userStore);

                    }
                    securityHandler.setLoginService(loginService);
                    //securityHandler.setLoginService(new RestrictionsLoginService());

                    FormAuthenticator authenticator = new FormAuthenticator("/login.html", "/loginerror.html", false);
                    securityHandler.setAuthenticator(authenticator);
                    context.setSecurityHandler(securityHandler);

                    server.setConnectors(new Connector[]{http});

                }

                userName = Constants.props.getProperty("service.webserver.client.user", "webserver");
                password = Constants.props.getProperty("service.webserver.client.password", "manager");
                url = Constants.props.getProperty("service.webserver.client.url",
                        Constants.props.getProperty("client.url", "vm://localhost?jms.alwaysSyncSend=false"));

                cm = new ConnectionManager(userName, password, url, log);
                cm.connect("WebServer", null, false);

                server.start();
            } else {
                log.warn("Must enable HTTP or HTTPS for webserver to start");
            }
        } catch (Exception ex) {
            log.error("Unable to start WebServer", ex);
        }
    }

    public synchronized String trace(String resource) throws TransportException {
        StringBuilder builder = new StringBuilder();
        if (cm.traceRoute(resource, false, 10000, envelope -> {
            try {
                List<Object> objects = envelope.getPayloadObjects();
                for (Object object : objects) {
                    if (object instanceof PingResponse response) {
                        builder.append(DateFromXMLAdapter.convert(System.currentTimeMillis())).append(" ")
                                .append(envelope.getSource()).append(" ").append(response.getOutcome()).append("\n");
                        if (OutcomeEnum.FAILURE == response.getOutcome()) {
                            String error = DateFromXMLAdapter.convert(
                                    System.currentTimeMillis()) + " TraceRoute failed";
                            if (!response.getError().isEmpty()) {
                                error += " - " + response.getError().get(0);
                            }
                            builder.append(error).append("\n");
                        }
                    }
                }
            } catch (MessageValidationException e) {
                builder.append(DateFromXMLAdapter.convert(System.currentTimeMillis())).append(" Bad response from ")
                        .append(envelope.getSource()).append("\n");
            }
        })) {
            builder.append(DateFromXMLAdapter.convert(System.currentTimeMillis())).append(" Trace Route Success.\n");
        } else {
            builder.append(DateFromXMLAdapter.convert(System.currentTimeMillis())).append(" Trace Route Failed.\n");
        }
        return builder.toString();
    }

    public static synchronized WebServer getInstance() {
        if (instance == null) {
            instance = new WebServer();
        }
        return instance;
    }

    public static synchronized void shutdown() {
        log.info("Shutting down webserver");
        if (instance != null) {
            try {
                instance.server.stop();
                instance.cm.disconnect();
            } catch (Exception e) {
                log.warn("Unable to shut webserver down");
            }
        }

    }

    public static void main(String[] args) {
        getInstance();
    }

    /**
     * Method to disable track/trace on our embedded Jetty server
     *
     * @param securityHandler - the security handler set for our ServletContextHandler which contains our constraint
     *                        mappings
     */
    private void disableTrackTrace(ConstraintSecurityHandler securityHandler) {
        ConstraintMapping disableTraceMapping = setConstraintMapping("Disable TRACE", "TRACE");
        securityHandler.addConstraintMapping(disableTraceMapping);

        ConstraintMapping disableTrackMapping = setConstraintMapping("Disable TRACK", "TRACK");
        securityHandler.addConstraintMapping(disableTrackMapping);

        ConstraintMapping enableEverythingButTraceTrackMapping = new ConstraintMapping();
        Constraint enableEverythingButTraceTrackConstraint = new Constraint();
        enableEverythingButTraceTrackConstraint.setName("Enable everything but TRACE/TRACK");
        enableEverythingButTraceTrackMapping.setConstraint(enableEverythingButTraceTrackConstraint);
        enableEverythingButTraceTrackMapping.setMethodOmissions(new String[]{"TRACE", "TRACK"});
        enableEverythingButTraceTrackMapping.setPathSpec("/");
        securityHandler.addConstraintMapping(enableEverythingButTraceTrackMapping);
    }

    /**
     * Method to create the constraint mapping to use with our security handler
     *
     * @param constraintName - name of the constraint as a string
     * @param methodName     - name of the method we want we to constrain
     *
     * @return - a ConstraintMapping object to use with our security handler
     */
    private ConstraintMapping setConstraintMapping(String constraintName, String methodName) {
        ConstraintMapping constraintMapping = new ConstraintMapping();
        Constraint constraint = new Constraint();
        constraint.setName(constraintName);
        constraint.setAuthenticate(true);
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/");
        constraintMapping.setMethod(methodName);

        return constraintMapping;
    }

}
