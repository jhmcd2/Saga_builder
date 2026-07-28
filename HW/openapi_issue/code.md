# Webserver.java

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
                //OpenAPI


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

                    // Default constraints
                    Constraint constraint = new Constraint();
                    constraint.setName(Constraint.__FORM_AUTH);
                    constraint.setRoles(new String[]{"user", "admin", "moderator"});
                    constraint.setAuthenticate(true);

                    ConstraintMapping constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(constraint);
                    constraintMapping.setPathSpec("/*");
                    securityHandler.addConstraintMapping(constraintMapping);


                    // Public constraints
                    Constraint publicConstraint = new Constraint();
                    publicConstraint.setName(Constraint.NONE);
                    publicConstraint.setAuthenticate(false);

                    // OpenAPI documentation
                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(publicConstraint);
                    constraintMapping.setPathSpec("/docs");
                    securityHandler.addConstraintMapping(constraintMapping);

                    constraintMapping = new ConstraintMapping();
                    constraintMapping.setConstraint(publicConstraint);
                    constraintMapping.setPathSpec("/docs/*");
                    securityHandler.addConstraintMapping(constraintMapping);


                    for (ConstraintMapping cm : securityHandler.getConstraintMappings()) {
                        log.info("JHM Security mapping: {} authenticate={}",
                                cm.getPathSpec(),
                                cm.getConstraint().getAuthenticate());
                    }

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


# resourcesExportServelet.java

public class ResourcesExportServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ResourcesExportServlet.class);
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("*** ResourcesExportServlet.doGet() invoked ***");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo(); // null or "/resources", "/all", etc.
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/", "" -> handleDomainList(resp);
                case "/routing"             -> handleRouting(req, resp);
                case "/resources"           -> handleResources(req, resp);
                case "/netResources"        -> handleNetResources(req, resp);
                case "/nameServerResources" -> handleNameServerResources(req, resp);
                case "/all"                 -> handleAll(req, resp);
                default -> {
                    resp.setStatus(404);
                    writeError(resp, "Unknown endpoint: " + path, 404);
                }
            }
        } catch (Exception e) {
            log.error("Error handling resources-export request", e);
            resp.setStatus(500);
            writeError(resp, "Internal server error: " + e.getMessage(), 500);
        }
    }

    // GET /resources-export/
    private void handleDomainList(HttpServletResponse resp) throws IOException {
        List<String> domains = NameServerLocator.getNameServerDomains();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("domains", domains);
        result.put("endpoints", List.of("/resources", "/routing","/netResources", "/nameServerResources", "/all"));
        writeJson(resp, result);
    }