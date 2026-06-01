/*
 * The overall classification of this file is: UNCLASSIFIED//FOUO
 */
package gov.ic.silkwave.nameserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import gov.ic.silkwave.common.client.ConnectionManager;
import gov.ic.silkwave.common.client.security.KeySpec;
import gov.ic.silkwave.common.error.GeneralErrorCode;
import gov.ic.silkwave.common.error.SystemException;
import gov.ic.silkwave.common.messages.builders.SILKWAVEMessageManipulator;
import gov.ic.silkwave.common.messages.builders.SecurityManipulator;
import gov.ic.silkwave.common.messaging.Envelope;
import gov.ic.silkwave.common.messaging.MessageValidationException;
import gov.ic.silkwave.common.transport.TransportException;
import gov.ic.silkwave.common.transport.jms.IRealTimeMessageHandler;
import gov.ic.silkwave.common.transport.jms.IRealTimeNetworkStatusHandler;
import gov.ic.silkwave.common.utils.Constants;
import gov.ic.silkwave.common.utils.SslUtils;
import gov.ic.silkwave.common.xml.DateFromXMLAdapter;
import gov.ic.silkwave.common.xml.XMLUtil;
import jakarta.xml.bind.JAXBException;
import nameserver.internal.ActionEnum;
import nameserver.internal.NameServerPullRequest;
import nameserver.internal.NameServerPullResponse;
import nameserver.internal.NameServerUpdate;
import nameserver.internal.RouteEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import silkwave.MessagePayloadType;
import silkwave.SILKWAVE;
import silkwave.markings.Security;
import silkwave.net.FailureNotification;
import silkwave.net.NamingServiceRole;
import silkwave.net.NetworkStatusActionType;
import silkwave.net.NetworkStatusResponse;
import silkwave.net.NetworkStatusResultType;
import silkwave.net.OutcomeEnum;
import silkwave.net.PingRequest;
import silkwave.net.PingResponse;
import silkwave.net.RegisterPrivilegedRole;
import silkwave.net.RegisterPrivilegedRoleResponse;
import silkwave.net.Resource;
import silkwave.net.ResourceDeregistration;
import silkwave.net.ResourceDeregistrationResponse;
import silkwave.net.ResourceLookup;
import silkwave.net.ResourceLookupResponse;
import silkwave.net.ResourceRegistration;
import silkwave.net.ResourceRegistrationResponse;
import silkwave.net.ResourceResolution;
import silkwave.net.ResourceType;
import silkwave.net.ScheduleNetworkStatusType;
import silkwave.security.CredentialAuthenticated;
import silkwave.security.SecurityAuthenticatedResponse;

/**
 * Base implementation of a nameserver that can be tailored for a particular domain
 * <p>
 * - connects to core - registers as a privileged nameserver with core (TBD) - supports resource add/remove/lookup -
 * allows specific implementation to customize permissions (allow X to register resource but not Y)
 * <p>
 * *Note - Currently does not handle the owning Nameserver missing a deregister message coming from another nameserver
 * in an update message. If this happens The owning Nameserver will broadcast forever the net address in the resource
 * route or until someone else tells him to remove it.
 *
 */
public class NameServer implements IRealTimeMessageHandler {
    protected static final String LOCAL_HUB = "net:local:registrar";
    protected static final String SECURITY_AUTHENTICATOR = "ns:silkwave.security:x509.authenticator";

    protected final int defaultExpiration;// =
    // Integer.valueOf(Constants.props.getProperty("service.nameserver.defaultExpiration",
    // "15000"));
    protected Logger log;
    protected String domain;
    protected ConnectionManager connection;
    // Mapping of all resources. Mine and others.
    protected ConcurrentMap<String, ResourceRoute> resources;
    // Mapping of my net address to my resources.
    protected ConcurrentMap<String, NetResources> netToResources;
    // Mapping of other Nameservers resources.
    protected ConcurrentMap<String, NetResources> nameServersToResources;
    protected final ScheduleNetworkStatusType addInterval;
    protected final ScheduleNetworkStatusType removeInterval;
    // protected static final int STATUS_INTERVAL =
    // Integer.valueOf(Constants.props.getProperty("service.nameserver.statusInterval",
    // "1"));
    protected final int statusInterval;
    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("NameServer-%d").build();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, namedThreadFactory);
    private final ScheduledExecutorService broadcastScheduler = Executors.newScheduledThreadPool(1, namedThreadFactory);
    // protected static JAXBContext context;
    protected final long maxAge;// =
    // Long.valueOf(Constants.props.getProperty("service.nameserver.maxSubscriberInactivity",
    // "120000"));
    protected final long maxNSAge;// =
    // Long.valueOf(Constants.props.getProperty("service.nameserver.maxNSInactivity",
    // "120000"));
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock read = readWriteLock.readLock();
    private final Lock write = readWriteLock.writeLock();
    private final int pullRequestTimeout;
    protected final String userName;
    protected final String password;
    protected final String url;

    protected final Security maxRegistrationClassification;
    protected final Security hubMinClassification;
    protected final Security hubMaxClassification;

    /**
     *
     * @param domain             The domain the nameserver should use.
     * @param userName           The username the nameserver should use to connect to silkwave. Not used if SSL
     *                           connection.
     * @param password           The password the nameserver should use to connect to silkwave. Not used if SSL
     *                           connection.
     * @param url                The transport url used to connect to silkwave.
     * @param statusInterval     The interval to ask NetworkStatus to send you updates on a address that has registered
     *                           a name with the NameServer.
     * @param defaultExpiration  The default amount of time in milliseconds for a name expiration.
     * @param maxAge             The max amount of time in milliseconds before a registered name will be kept before
     *                           being removed from the nameserver if it has not received network status.
     * @param maxNSAge           The max amount of time in milliseconds before a neighbor nameserver will be kept before
     *                           being removed if it has not heard from it.
     * @param pullRequestTimeout
     */

    public NameServer(String domain, String userName, String password, String url, int statusInterval,
            int defaultExpiration, int maxAge, int maxNSAge, int pullRequestTimeout) {

        log = LoggerFactory.getLogger(getClass());

        String tempDomain = domain;
        if (domain.startsWith("ns:")) {
            tempDomain = tempDomain.substring(3); // move past the ns:
        }

        if (tempDomain.contains(":") || tempDomain.contains("#")) {
            throw new SystemException("Invalid domain name, name contains reserved characters that are not allowed",
                    GeneralErrorCode.MESSAGE_SERVICE_ERROR);
        }

        hubMinClassification = SecurityManipulator.buildHubMinimum(log);
        hubMaxClassification = SecurityManipulator.buildHubHigh(log);

        boolean nameserverMinClassEnabled = Boolean.parseBoolean(
                Constants.props.getProperty("service.nameserver." + tempDomain + ".minClassificationEnabled",
                        Constants.props.getProperty("service.nameserver.minClassificationEnabled", "true")));
        if (nameserverMinClassEnabled) {
            // nameserver classification should be pulled downward
            // towards the hub minimum classification
            //
            // What does this mean?
            //     Whatever parts are specified
            //     will be merged with the hub minimum to produce a max classification that
            //     may be higher than hub minimum. If totally unspecified, the nameserver
            //     max classification will end up equal to the hub minimum.
            if (Constants.props.getProperty(tempDomain + ".max.registration") != null) {
                Security specifiedSecurity = SecurityManipulator.buildSecurityFromProperties(Constants.props,
                        tempDomain + ".max.registration", log);
                maxRegistrationClassification = SecurityManipulator.mergeSecuritySettings(specifiedSecurity,
                        hubMinClassification);
            } else {
                maxRegistrationClassification = hubMinClassification;
            }
        } else {
            // nameserver classification should be pulled upward
            // towards the hub maximum classification
            if (Constants.props.getProperty(tempDomain + ".max.registration") != null) {
                Security specifiedSecurity = SecurityManipulator.buildSecurityFromProperties(Constants.props,
                        tempDomain + ".max.registration", log);
                maxRegistrationClassification = SecurityManipulator.mergeSecuritySettings(specifiedSecurity,
                        hubMaxClassification);
            } else {
                maxRegistrationClassification = hubMaxClassification;
            }
        }

        if (!SecurityManipulator.isAcceptable(hubMaxClassification, maxRegistrationClassification)) {
            throw new SystemException(domain + " nameserver classification higher than hub maximum!",
                    GeneralErrorCode.MESSAGE_SERVICE_ERROR);
        }

        log.info("Max Registration Classification for domain {} is: {}", domain,
                SecurityManipulator.toString(maxRegistrationClassification));

        this.userName = userName;
        this.password = password;
        this.url = url;
        this.statusInterval = statusInterval;
        this.defaultExpiration = defaultExpiration;
        this.maxAge = maxAge;
        this.maxNSAge = maxNSAge;
        this.pullRequestTimeout = pullRequestTimeout;
        addInterval = new ScheduleNetworkStatusType();
        addInterval.setAction(NetworkStatusActionType.ADD);
        addInterval.setInterval(this.statusInterval);

        removeInterval = new ScheduleNetworkStatusType();
        removeInterval.setAction(NetworkStatusActionType.REMOVE);
        removeInterval.setInterval(this.statusInterval);

        try {
            XMLUtil.addClassesToContext(nameserver.internal.ObjectFactory.class);
            XMLUtil.addSchema("/xsd/NSInternal.xsd");
        } catch (JAXBException e) {
            log.error("Unable to add NameServer classes to XMLUtil", e);
        }

        this.domain = domain;
        resources = new ConcurrentHashMap<>();
        netToResources = new ConcurrentHashMap<>();
        nameServersToResources = new ConcurrentHashMap<>();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                write.lock();
                log.info("Running {} NameServer cleanup", domain);
                long currentTime = System.currentTimeMillis();
                for (String netId : netToResources.keySet()) {
                    log.trace("Checking {} to see if it needs removed", netId);
                    NetResources netResources = netToResources.get(netId);
                    if (netResources != null) {
                        long lastSeen = netResources.getLastSeen();
                        if ((currentTime - lastSeen) > maxAge) {
                            log.info("Removing {} because it has not been seen in awhile.", netId);
                            resourceDeregistration(netId);
                            connection.requestNetworkStatus(netId, removeInterval);
                        }
                    }
                }

                currentTime = System.currentTimeMillis();
                for (String nsId : nameServersToResources.keySet()) {
                    NetResources netResources = nameServersToResources.get(nsId);
                    if ((currentTime - netResources.getLastSeen()) > maxNSAge) {
                        for (String resourceId : netResources.getResourceRouteIds()) {
                            ResourceRoute rr = netResources.getResourceRoute(resourceId);

                            Iterator<String> it = rr.getNetIds().keySet().iterator();
                            while (it.hasNext()) {
                                String key = it.next();
                                NetId netId = rr.getNetIds().get(key);
                                if (netId.getOwnerId().equals(nsId)) {
                                    it.remove();
                                    if (rr.getNetIds().isEmpty()) {
                                        resources.remove(rr.getResId());
                                    }
                                }
                            }
                        }
                        nameServersToResources.remove(nsId);
                    }
                }
            } catch (Throwable ex) {
                log.error("Uncaught exception", ex);
            } finally {
                write.unlock();
            }
        }, 1, 1, TimeUnit.MINUTES);

        // Schedule sending our full status.
        broadcastScheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("Running {} NameServer broadcast", domain);
                NameServerUpdate update = new NameServerUpdate();
                update.setFull(false);
                List<RouteEntry> routes = update.getRoute();
                try {
                    read.lock();

                    // TODO:This needs to be better so we do not have to loop
                    // over all of the routes.
                    for (String resourceId : resources.keySet()) {
                        log.trace("Checking {} to see if it needs to be sent in broadcast", resourceId);
                        ResourceRoute route = resources.get(resourceId);
                        RouteEntry routeEntry = new RouteEntry();
                        routeEntry.setName(resourceId);
                        routeEntry.setType(nameserver.internal.ResourceType.valueOf(route.getType().toString()));
                        List<nameserver.internal.NetId> netIds = routeEntry.getNetId();
                        for (String key : route.getNetIds().keySet()) {
                            NetId netId = route.getNetIds().get(key);
                            if (connection.getNetId().equals(netId.getOwnerId())) {
                                nameserver.internal.NetId interNetId = new nameserver.internal.NetId();
                                interNetId.setId(netId.getId());
                                // interNetId.setOwner(connection.getNetId());
                                interNetId.setTimestamp(DateFromXMLAdapter.convert(netId.getCreationTime()));
                                interNetId.setAction(ActionEnum.ADD);
                                netIds.add(interNetId);
                            }
                        }
                        if (!netIds.isEmpty()) {
                            routes.add(routeEntry);
                        }
                    }
                } finally {
                    read.unlock();
                }

                // Need to send even if no routes so we clear.
                sendNameServerUpdate(routes, true);
            } catch (Throwable ex) {
                log.error("Uncaught exception", ex);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     *
     * @param routes
     * @param full
     */
    public void sendNameServerUpdate(List<RouteEntry> routes, boolean full) {
        SILKWAVE msg = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(msg, maxRegistrationClassification, connection.getNetId(), log);
        msg.setMessageCorrelationId(UUID.randomUUID().toString());
        msg.setSessionToken(connection.getSessionToken());
        msg.setPayload(new MessagePayloadType());
        msg.getDestination().add(domain + "#all");
        NameServerUpdate update = new NameServerUpdate();
        update.setFull(full);
        update.getRoute().addAll(routes);
        Element elem = XMLUtil.buildDocumentElement(update, "nsin", log);
        msg.getPayload().getAny().add(elem);

        Envelope env = new Envelope(msg);
        try {
            connection.send(env);
        } catch (TransportException e) {
            log.warn("Unable to send Name Server Update", e);
        }
    }

    public void sendNameServerPullRequest() {
        SILKWAVE find = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(find, hubMinClassification, connection.getNetId(), log);
        find.setSessionToken(connection.getSessionToken());
        find.getDestination().add(domain);
        find.setMessageCorrelationId("LoadNameServer");
        find.setPayload(new MessagePayloadType());
        NameServerPullRequest pullRequest = new NameServerPullRequest();
        Element elem = XMLUtil.buildDocumentElement(pullRequest, "nsin", log);
        find.getPayload().getAny().add(elem);

        log.info("Pulling state from a neighboring name service...");
        Envelope env = new Envelope(find);

        Envelope response;
        try {
            response = connection.getResponse(env, pullRequestTimeout);
            if (response != null) {
                handleNameServerPullResponse(response);
            } else {
                log.info("No neighboring name service identified");
            }
        } catch (TransportException e) {
            log.warn("Unable to do a pull request.", e);
        }
    }

    /**
     *
     * @return
     *
     * @throws TransportException
     */
    public boolean connect() throws TransportException {
        boolean conn = false;
        if (connection == null) {
            connection = new ConnectionManager(userName, password, url, log);
            conn = connection.connect("NameServer." + domain, this, false);

            IRealTimeNetworkStatusHandler networkHandler = new IRealTimeNetworkStatusHandler() {

                @Override
                public void onError(NetworkStatusResponse response) {
                    if (response.getError() != null) {
                        StringBuilder builder = new StringBuilder();
                        for (String string : response.getError()) {
                            builder.append(string);
                            builder.append("\n");
                        }
                        log.warn("Got an Error: {}", builder);
                    } else {
                        log.warn("Got an unknown error.");
                    }
                }

                @Override
                public void onDisconnect(NetworkStatusResultType result) {
                    log.info("Received a network status disconnect message for {}", result.getNet());
                    resourceDeregistration(result.getNet());
                }

                @Override
                public void onChange(NetworkStatusResultType result) {
                    log.debug("networkstatus onChange({})", result.getNet());
                    NetResources netResources = netToResources.get(result.getNet());
                    if (netResources != null) {
                        netResources.setLastSeen(System.currentTimeMillis());
                    }
                }
            };

            connection.setNetworkStatusHandler(networkHandler);

            // send the update security credential message
            checkSecurityCredentialUpdate();

            sendNameServerPullRequest();
            RegisterPrivilegedRoleResponse resp = registerAsNamingService();
            if (resp != null && OutcomeEnum.SUCCESS == resp.getOutcome()) {
                log.info("Successfully registered as naming service for [{}]", domain);
            } else if (resp == null) {
                log.warn("No Response when trying to register as a naming service for [{}]", domain);
            } else {
                // TODO: We need to do more then just log this.
                log.error("Unable to register as naming service for domain [{}].{}", domain, resp.getError());
            }
        } else {
            conn = connection.isConnected();
        }
        return conn;
    }

    private void checkSecurityCredentialUpdate() throws TransportException {

        // create key specification list object
        List<KeySpec> keySpecList = new ArrayList<>();

        Properties cprops = Constants.props;

        //
        // look for deprecated name server specific values first
        //

        KeySpec ks = SslUtils.getLegacyKeySpec(cprops, "service.nameserver.connection.p12", "PKCS12");
        if (ks.isValid()) {
            log.warn(
                    "service.nameserver.connection.p12.* is deprecated - use client|system.{keystore,keystorePass," + "keystoreType,keystoreKeyAlias} instead");
            keySpecList.add(ks);
        } else {
            ks = SslUtils.getLegacyKeySpec(cprops, "service.nameserver.connection.jks", "JKS");
            if (ks.isValid()) {
                log.warn(
                        "service.nameserver.connection.jks.* is deprecated - use client|system.{keystore," +
                                "keystorePass,keystoreType,keystoreKeyAlias} instead");
                keySpecList.add(ks);
            }
        }

        // if here, and one or more set above
        if (!keySpecList.isEmpty()) {
            // use what we have so far (don't look for the generic values below)
            sendSecurityCredentialUpdate(keySpecList);
            return; // done here
        }

        //
        // look for generic values next
        //

        ks = SslUtils.getLegacyKeySpec(cprops, "client.connection.p12", "PKCS12");
        if (ks.isValid()) {
            log.warn(
                    "client.connection.p12.* is deprecated - use client|system.{keystore,keystorePass,keystoreType," + "keystoreKeyAlias} instead");
        } else {
            ks = SslUtils.getLegacyKeySpec(cprops, "client.connection.jks", "JKS");
            if (ks.isValid()) {
                log.warn(
                        "client.connection.jks.* is deprecated - use client|system.{keystore,keystorePass," +
                                "keystoreType,keystoreKeyAlias} instead");
            } else {
                // this is the preferred usage of either client.* or system.*
                ks = SslUtils.getOrDefaultKeySpec(cprops, "client");
            }
        }
        log.debug(ks.toString());
        if (ks.isValid()) {
            List<KeySpec> fileSpecList = new ArrayList<>();
            fileSpecList.add(ks);
            SecurityAuthenticatedResponse response = connection.updateSecurityCredentialsFromKeyList(fileSpecList);
            for (CredentialAuthenticated credAuthenticated : response.getAuthenticatedCredential()) {
                if (!credAuthenticated.isAuthenticated()) {
                    log.warn("Credential: {} failed to authenticate: {}", credAuthenticated.getId(),
                            credAuthenticated.getMessage());
                } else {
                    log.info("Credential: {} successfully authenticated", credAuthenticated.getId());
                }
            }
        }
    }

    private void sendSecurityCredentialUpdate(List<KeySpec> keySpecList) throws TransportException {

        // send the credentials if we have them but don't block forever if security appears to be turned off
        boolean securityEnabled = !"off".equalsIgnoreCase(Constants.props.getProperty("service.security.state", "off"));
        if (securityEnabled) {
            connection.waitForResource(SECURITY_AUTHENTICATOR, true, hubMinClassification);
        } else {
            log.info("Silkwave security appears to be OFF - NOT waiting for resource {}", SECURITY_AUTHENTICATOR);
        }

        // perform security credential update using given list
        SecurityAuthenticatedResponse response = connection.updateSecurityCredentialsFromKeyList(keySpecList);

        // log result
        for (CredentialAuthenticated credAuthenticated : response.getAuthenticatedCredential()) {
            if (!credAuthenticated.isAuthenticated()) {
                log.error("Transport Credential:{} failed to authenticate", credAuthenticated.getId());
                log.error("  Message:{}", credAuthenticated.getMessage());
            } else {
                log.info("Transport Credential:{} authenticated", credAuthenticated.getId());
            }
        }
    }

    /**
     *
     */
    public void disconnect() {
        log.info("Shutting down NameServer[{}]", domain);
        // cleanup...
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (TransportException e) {
                log.warn("Problems disconnecting.", e);
            }
        }
    }

    /**
     *
     */
    protected RegisterPrivilegedRoleResponse registerAsNamingService() {
        // privileged message to be a nameserver
        SILKWAVE reg = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(reg, hubMinClassification, connection.getNetId(), log);
        reg.setMessageCorrelationId(UUID.randomUUID().toString());
        reg.setSessionToken(connection.getSessionToken());
        reg.getDestination().add(LOCAL_HUB);
        reg.setPayload(new MessagePayloadType());
        RegisterPrivilegedRole rpr = new RegisterPrivilegedRole();
        NamingServiceRole role = new NamingServiceRole();
        role.setDomain(domain);
        rpr.setRole(role);

        Element elem = XMLUtil.buildDocumentElement(rpr, "net", log);
        reg.getPayload().getAny().add(elem);

        log.info("Registering as Naming Service role");
        Envelope request = new Envelope(reg);
        Envelope response;
        RegisterPrivilegedRoleResponse resp = null;
        try {
            response = connection.getResponse(request);
            if (response != null) {
                List<Object> objs = response.getPayloadObjects();
                for (Object obj : objs) {
                    if (obj instanceof RegisterPrivilegedRoleResponse) {
                        resp = (RegisterPrivilegedRoleResponse) obj;
                    } else {
                        // We better never get into this.
                        log.error(
                                "Received the wrong response to our RegisterPrivilegedRole for domain [{}] Received " + "a" + " {}",
                                domain, obj.getClass().getName());
                    }
                }
            } else {
                log.error("Did not receive a response to our RegisterPrivilegedRole for domain [{}]", domain);
                // TODO:What do we do now?
            }
        } catch (TransportException | MessageValidationException e) {
            log.error("Unable to register as naming service", e);
        }

        return resp;
    }

    /**
     *
     * @param env
     */
    protected void handleNameServerPullResponse(Envelope env) {
        // check for classification here to make sure that the message
        // classification is not higher than allowed
        if (SecurityManipulator.isAcceptable(maxRegistrationClassification, env.getSecurity())) {
            try {
                write.lock();
                List<Object> objs = env.getPayloadObjects();
                for (Object obj : objs) {
                    if (obj instanceof NameServerPullResponse response) {
                        log.info("Processing response from neighbor {}", env.getSource());

                        if (nameserver.internal.OutcomeEnum.SUCCESS == response.getOutcome()) {
                            for (RouteEntry routeEntry : response.getRoute()) {
                                ResourceRoute rr = new ResourceRoute(routeEntry.getName(),
                                        ResourceType.valueOf(routeEntry.getType().toString()));
                                resources.put(rr.getResId(), rr);

                                for (nameserver.internal.NetId interNetId : routeEntry.getNetId()) {
                                    NetId netId = new NetId();
                                    netId.setCreationTime(
                                            DateFromXMLAdapter.convert(interNetId.getTimestamp()).getTime());
                                    netId.setId(interNetId.getId());
                                    netId.setOwnerId(interNetId.getOwner());
                                    rr.getNetIds().put(netId.getId(), netId);

                                    NetResources netResources = nameServersToResources.get(netId.getOwnerId());

                                    if (netResources == null) {
                                        netResources = new NetResources(netId.getOwnerId());
                                        nameServersToResources.put(netResources.getNetId(), netResources);
                                    }
                                    netResources.addResourceRoute(rr);
                                }
                            }
                            if (log.isTraceEnabled()) {
                                printResources();
                                printNameServersToResources();
                                printNetToResources();
                            }
                        }
                    }
                }
            } catch (MessageValidationException e) {
                log.error("Invalid NameServerPullResponse from {}", env.getSource());
            } finally {
                write.unlock();
            }
        } else {
            log.warn("Received a nameserver pull response that was higher classification then we can handle from: {}",
                    env.getSource());
        }

    }

    public NameServerPullResponse buildNameServerPullResponse() {
        NameServerPullResponse response = new NameServerPullResponse();
        try {
            read.lock();
            List<RouteEntry> routes = response.getRoute();

            for (String key : resources.keySet()) {
                ResourceRoute rr = resources.get(key);
                RouteEntry route = new RouteEntry();
                routes.add(route);
                route.setName(key);
                route.setType(nameserver.internal.ResourceType.valueOf(rr.getType().toString()));
                List<nameserver.internal.NetId> interNetIds = route.getNetId();
                for (String netKey : rr.getNetIds().keySet()) {
                    NetId netId = rr.getNetIds().get(netKey);
                    nameserver.internal.NetId interNetId = new nameserver.internal.NetId();
                    interNetId.setAction(ActionEnum.ADD);
                    interNetId.setId(netKey);
                    interNetId.setOwner(netId.getOwnerId());
                    interNetId.setTimestamp(DateFromXMLAdapter.convert(netId.getCreationTime()));
                    interNetIds.add(interNetId);
                }
            }
            response.setOutcome(nameserver.internal.OutcomeEnum.SUCCESS);
        } finally {
            read.unlock();
        }
        return response;
    }

    /**
     * Handles Generating a snapshot of our current resources. Used to prime a new nameserver.
     *
     * @param obj
     * @param msg
     */
    protected void handleNameServerPullRequest(Envelope obj, NameServerPullRequest msg) {
        SILKWAVE silkwave = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(silkwave,
                SecurityManipulator.mergeSecuritySettings(maxRegistrationClassification, obj.getSecurity()),
                connection.getNetId(), log);
        silkwave.setMessageCorrelationId(obj.getMessageCorrelationId());
        silkwave.setSessionToken(connection.getSessionToken());
        silkwave.getDestination().add(obj.getSource());
        silkwave.setPayload(new MessagePayloadType());
        NameServerPullResponse response = new NameServerPullResponse();

        try {
            read.lock();
            response = buildNameServerPullResponse();
            Element elem = XMLUtil.buildDocumentElement(response, "nsin", log);
            silkwave.getPayload().getAny().add(elem);

        } catch (Throwable ex) {
            response.setOutcome(nameserver.internal.OutcomeEnum.FAILURE);
            Element elem = XMLUtil.buildDocumentElement(response, "nsin", log);
            silkwave.getPayload().getAny().clear();
            silkwave.getPayload().getAny().add(elem);
        } finally {
            read.unlock();
        }
        Envelope env = new Envelope(silkwave);
        try {
            connection.send(env);
        } catch (TransportException e) {
            log.error("Unable to send Name Server Pull Response.", e);
        }
    }

    /**
     * Method to handle the name server update
     *
     * @param envelope the {@link Envelope} being sent
     * @param update   the {@link NameServerUpdate} being updated
     */
    protected void handleNameServerUpdate(Envelope envelope, NameServerUpdate update) {
        if (SecurityManipulator.isAcceptable(maxRegistrationClassification, envelope.getSecurity())) {
            try {
                write.lock();

                String owner = envelope.getSource();
                NetResources netResources = nameServersToResources.get(owner);
                // Set up a new owner NetResources
                if (netResources == null) {
                    netResources = new NetResources(owner);
                    NetResources temp = nameServersToResources.putIfAbsent(owner, netResources);
                    if (temp != null) {
                        netResources = temp;
                    }
                }
                // Done setting up new Owner Resources.

                // Update the last time we have seen this NameServer
                long envelopeTime = envelope.getTimestamp().getTimeInMillis();
                if (netResources.getLastSeen() < envelopeTime) {
                    netResources.setLastSeen(envelopeTime);
                }

                Map<String, ResourceRoute> resourceMap = netResources.getResourceMap();
                // Handle a full update.
                if (update.isFull()) {

                    Map<String, ResourceRoute> tempMap = new HashMap<>(resourceMap);
                    // Loop over all the RouteEntries.
                    for (RouteEntry route : update.getRoute()) {
                        // Handle updating existing routes
                        tempMap.remove(route.getName());
                        findRouteAndUpdate(envelope, resourceMap, route);
                    }
                    // Look at the leftovers.
                    // This should only happen if a transaction beat the State push
                    // or we missed a transaction. So we need to check to see if we
                    // need to remove netIds.
                    for (String key : tempMap.keySet()) {
                        ResourceRoute rr = resourceMap.get(key);
                        // Use the keep flag because there could be another netId in
                        // the Route that we still have interest in.
                        boolean keep = false;
                        // Loop over every netId to see if the owner is the same as
                        // the update message.
                        for (String netKey : rr.getNetIds().keySet()) {
                            NetId netId = rr.getNetIds().get(netKey);
                            if (envelope.getSource().equals(netId.getOwnerId())) {
                                if (envelopeTime > netId.getCreationTime()) {
                                    rr.getNetIds().remove(netKey);
                                    if (rr.getNetIds().isEmpty()) {
                                        resources.remove(rr.getResId());
                                    }
                                } else {
                                    keep = true;
                                }
                            }
                        }
                        if (!keep) {
                            resourceMap.remove(key);
                        }
                    }
                    // Handle a partial update.
                } else {
                    for (RouteEntry route : update.getRoute()) {
                        findRouteAndUpdate(envelope, resourceMap, route);
                    }
                }

                if (log.isTraceEnabled()) {
                    printResources();
                    printNameServersToResources();
                    printNetToResources();
                }
            } finally {
                write.unlock();
            }
        } else {
            log.warn("Received a nameserver update that was higher classification then we can handle from: {}",
                    envelope.getSource());
        }
    }

    /**
     *
     * @param envelope    the {@link Envelope} to be routed
     * @param resourceMap the map of resourceRoutes
     * @param route       the {@link RouteEntry}
     */
    protected void findRouteAndUpdate(Envelope envelope, Map<String, ResourceRoute> resourceMap, RouteEntry route) {
        ResourceRoute rr = resourceMap.get(route.getName());
        // if ResourceRoute exists for resourceMap update it
        if (rr != null) {
            updateRoute(resourceMap, envelope.getSource(), rr, route);
        } else {
            rr = resources.get(route.getName());
            // If resourceRoute exits for someone else update it.
            if (rr != null) {
                updateRoute(resourceMap, envelope.getSource(), rr, route);
            } else {
                ResourceRoute nrr = new ResourceRoute(route.getName(),
                        ResourceType.valueOf(route.getType().toString()));
                updateRoute(resourceMap, envelope.getSource(), nrr, route);
                ResourceRoute orr = resources.putIfAbsent(route.getName(), nrr);

                if (orr != null) {
                    updateRoute(resourceMap, envelope.getSource(), orr, route);
                    rr = orr;
                } else {
                    rr = nrr;
                }
            }
        }

        if (rr.getNetIds().isEmpty()) {
            resourceMap.remove(rr.getResId());
            resources.remove(rr.getResId());
        }
    }

    /**
     *
     */
    protected void updateRoute(Map<String, ResourceRoute> resourceMap, String source, ResourceRoute rr, RouteEntry re) {
        switch (rr.getType()) {
            case EXCLUSIVE:
                nameserver.internal.NetId newInterNetId = re.getNetId().get(0);
                long updateTime = DateFromXMLAdapter.convert(newInterNetId.getTimestamp()).getTime();
                NetId oldNetId = null;
                // Find the first and only NetId
                for (String key : rr.getNetIds().keySet()) {
                    oldNetId = rr.getNetIds().get(key);
                }
                if (oldNetId == null || oldNetId.getCreationTime() < updateTime) {
                    if (ActionEnum.ADD == newInterNetId.getAction()) {
                        // Need to clean up old netId if it's not the same.
                        resourceMap.put(rr.getResId(), rr);

                        if (oldNetId == null) {
                            oldNetId = new NetId();
                        } else if (!newInterNetId.getId().equals(oldNetId.getId())) {
                            NetResources resources = netToResources.get(oldNetId.getId());
                            // If resources is not null then it was one of mine.
                            if (resources != null) {
                                resources.removeResourceRoute(rr.getResId());
                                // If there are no resources left that mean we have no interest in this net anymore
                                if (0 == resources.getResourceRouteCount()) {
                                    netToResources.remove(oldNetId.getId());
                                    try {
                                        connection.requestNetworkStatus(oldNetId.getId(), removeInterval);
                                    } catch (TransportException e) {
                                        log.warn("Unable to request network status for {}", oldNetId.getId(), e);
                                    }
                                }
                            }
                        }

                        // If there is a new owner remove this net from the previous owner.
                        // Not in the above if/else because could be just changing owner not netIds.
                        if (oldNetId.getOwnerId() != null && !source.equals(oldNetId.getOwnerId())) {
                            NetResources otherNetResources = nameServersToResources.get(oldNetId.getOwnerId());
                            if (otherNetResources != null) {
                                otherNetResources.removeResourceRoute(rr.getResId());
                            }
                        }

                        rr.getNetIds().clear();

                        oldNetId.setCreationTime(updateTime);
                        oldNetId.setOwnerId(source);
                        oldNetId.setId(newInterNetId.getId());
                        rr.getNetIds().put(oldNetId.getId(), oldNetId);
                    }
                    if (ActionEnum.REMOVE == newInterNetId.getAction()) {

                        // If oldNetId is null means the remove was for the wrong
                        // net Id.
                        if (oldNetId != null && oldNetId.getId().equals(newInterNetId.getId())) {
                            NetResources myNetResources = netToResources.get(oldNetId.getId());
                            if (myNetResources != null) {
                                myNetResources.removeResourceRoute(rr.getResId());
                                // If there are not resources left that mean we
                                // have no interest in this net anymore
                                if (0 == myNetResources.getResourceRouteCount()) {
                                    netToResources.remove(oldNetId.getId());
                                    try {
                                        connection.requestNetworkStatus(oldNetId.getId(), removeInterval);
                                    } catch (TransportException e) {
                                        log.warn("Unable to remove the request network status for {}", oldNetId.getId(),
                                                e);
                                    }
                                }
                            } else {
                                NetResources otherNetResources = nameServersToResources.get(oldNetId.getOwnerId());
                                if (otherNetResources != null) {
                                    otherNetResources.removeResourceRoute(rr.getResId());
                                }
                            }
                            rr.getNetIds().clear();
                        }
                    }
                }
                break;
            case ANY:
            case ALL:
            default:
                Map<String, NetId> rrNetIds = rr.getNetIds();
                for (nameserver.internal.NetId updateNetId : re.getNetId()) {
                    oldNetId = rrNetIds.get(updateNetId.getId());
                    updateTime = DateFromXMLAdapter.convert(updateNetId.getTimestamp()).getTime();
                    // HANDLE ADD
                    if (ActionEnum.ADD == updateNetId.getAction()) {
                        if (oldNetId == null) {
                            rrNetIds.put(updateNetId.getId(), new NetId(updateNetId.getId(), source, updateTime));
                            resourceMap.put(rr.getResId(), rr);
                        } else {
                            if (updateTime > oldNetId.getCreationTime()) {
                                if (!oldNetId.getOwnerId().equals(source)) {
                                    resourceMap.put(rr.getResId(), rr);

                                    NetResources myNetResources = netToResources.get(oldNetId.getId());
                                    // Need to see if we had any interest in this
                                    // old NetId
                                    if (myNetResources != null) {
                                        myNetResources.removeResourceRoute(rr.getResId());
                                        // If we have no more interest in this netid
                                        // then clean it up
                                        if (0 == myNetResources.getResourceRouteCount()) {
                                            netToResources.remove(oldNetId.getId());
                                            try {
                                                connection.requestNetworkStatus(oldNetId.getId(), removeInterval);
                                            } catch (TransportException e) {
                                                log.warn("Unable to remove the request network status for {}",
                                                        oldNetId.getId(), e);
                                            }
                                        }
                                    } else {

                                        // Need to see if the old owner has any
                                        // interest in this route now.
                                        boolean interest = false;
                                        String oldOwner = oldNetId.getOwnerId();
                                        for (String key : rr.getNetIds().keySet()) {
                                            NetId temp = rr.getNetIds().get(key);
                                            if (temp.getOwnerId().equals(oldOwner)) {
                                                interest = true;
                                            }
                                        }
                                        if (!interest) {
                                            NetResources otherNetResources = nameServersToResources.get(oldOwner);
                                            if (otherNetResources != null) {
                                                otherNetResources.removeResourceRoute(rr.getResId());
                                            }
                                        }
                                    }
                                }
                                // Update the NetId with the new information.
                                oldNetId.setCreationTime(updateTime);
                                oldNetId.setOwnerId(source);
                            }
                        }
                    } else if (ActionEnum.REMOVE == updateNetId.getAction()) {
                        if (oldNetId != null) {
                            if (updateTime > oldNetId.getCreationTime()) {
                                rrNetIds.remove(updateNetId.getId());

                                NetResources myNetResources = netToResources.get(oldNetId.getId());
                                // If its one of mine then remove it.
                                if (myNetResources != null) {
                                    myNetResources.removeResourceRoute(rr.getResId());
                                    // If we have no more interest in this netid
                                    // then clean it up
                                    if (0 == myNetResources.getResourceRouteCount()) {
                                        netToResources.remove(oldNetId.getId());
                                        try {
                                            connection.requestNetworkStatus(oldNetId.getId(), removeInterval);
                                        } catch (TransportException e) {
                                            log.warn("Unable to remove the request network status for {}",
                                                    oldNetId.getId(), e);
                                        }
                                    }
                                } else {
                                    // Must be another nameservers.
                                    String oldOwner = oldNetId.getOwnerId();
                                    boolean interest = false;
                                    for (String key : rrNetIds.keySet()) {
                                        NetId temp = rrNetIds.get(key);
                                        if (temp.getOwnerId().equals(oldOwner)) {
                                            interest = true;
                                        }
                                    }
                                    if (!interest) {
                                        NetResources otherNetResources = nameServersToResources.get(oldOwner);
                                        if (otherNetResources != null) {
                                            otherNetResources.removeResourceRoute(rr.getResId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     *
     */
    protected void resourceRegistration(Envelope envelope, ResourceRegistration payload) {
        log.debug("registration request from {}", envelope.getSource());

        // check for classification here to make sure that the message
        // classification is not higher than allowed
        boolean allowed = SecurityManipulator.isAcceptable(maxRegistrationClassification, envelope.getSecurity());

        SILKWAVE resp = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(resp,
                SecurityManipulator.mergeSecuritySettings(envelope.getSecurity(), hubMinClassification),
                connection.getNetId(), log);
        resp.setMessageCorrelationId(envelope.getMessageCorrelationId());
        resp.setSessionToken(connection.getSessionToken());
        resp.getDestination().add(envelope.getSource());
        resp.setPayload(new MessagePayloadType());
        resp.setTrackingId(String.valueOf(System.currentTimeMillis()));
        ResourceRegistrationResponse rrr = new ResourceRegistrationResponse();
        List<RouteEntry> updateList = new ArrayList<>();
        try {
            write.lock();
            if (allowed) {
                boolean validResources = checkValidResources(payload.getResource());
                if (validResources) {
                    for (Resource res : payload.getResource()) {
                        String resId = res.getName().toLowerCase();
                        if (!res.getNet().isEmpty()) {
                            ResourceRoute nrr = new ResourceRoute(resId, res.getType());
                            RouteEntry entry = updateResourceRoute(nrr, res, connection.getNetId(),
                                    envelope.getTimestamp().getTimeInMillis());
                            ResourceRoute orr = resources.putIfAbsent(resId, nrr);

                            if (orr != null) {
                                entry = updateResourceRoute(orr, res, connection.getNetId(),
                                        envelope.getTimestamp().getTimeInMillis());
                            }
                            if (entry != null) {
                                updateList.add(entry);
                            }
                            try {
                                connection.requestNetworkStatus(res.getNet(), addInterval);
                            } catch (TransportException e) {
                                log.warn("Unable to request network status for {}", res.getNet(), e);
                            }
                            rrr.getResource().add(res);
                        }
                    }
                    rrr.setOutcome(OutcomeEnum.SUCCESS);
                } else {
                    rrr.setOutcome(OutcomeEnum.FAILURE);
                    rrr.getResource().addAll(payload.getResource());
                    rrr.getError()
                            .add("Request contained resource with reserved character. Reserved Characters are: #");
                    log.info("************************* Bad Resource in nameserver registration request");
                }
            } else {
                rrr.setOutcome(OutcomeEnum.FAILURE);
                rrr.getResource().addAll(payload.getResource());
                rrr.getError().add("Request Classification above allowed max value: " + SecurityManipulator.toString(
                        maxRegistrationClassification));
                log.info("************************* Bad Classification in nameserver registration request");
                log.debug(envelope.getOrigXml());
            }
        } finally {
            write.unlock();
        }

        if (!updateList.isEmpty()) {
            sendNameServerUpdate(updateList, false);
        }

        Element elem = XMLUtil.buildDocumentElement(rrr, "net", log);
        resp.getPayload().getAny().add(elem);
        Envelope env = new Envelope(resp);
        try {
            connection.send(env);

            if (log.isTraceEnabled()) {
                printResources();
                printNameServersToResources();
                printNetToResources();
            }
        } catch (TransportException e) {
            log.error("Unable to send Register Resource Response.", e);
        }
    }

    private boolean checkValidResources(List<Resource> resources) {
        for (Resource res : resources) {
            String resId = res.getName().toLowerCase();
            if (resId.startsWith(domain + ":")) {
                resId = resId.substring(domain.length() + 1);
            }
            if (resId.contains("#") || resId.contains(":")) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param route
     * @param res
     * @param ownerId
     * @param creationTime
     *
     * @return
     */
    protected RouteEntry updateResourceRoute(ResourceRoute route, Resource res, String ownerId, long creationTime) {
        RouteEntry routeEntry = null;
        if (route.getType() == res.getType()) {
            routeEntry = new RouteEntry();
            routeEntry.setName(res.getName().toLowerCase());
            routeEntry.setType(nameserver.internal.ResourceType.valueOf(res.getType().toString()));
            List<nameserver.internal.NetId> interNetIds = routeEntry.getNetId();
            Map<String, NetId> netIds = route.getNetIds();
            NetId newNetId = null;
            switch (route.getType()) {
                case EXCLUSIVE:
                    boolean updated = false;
                    newNetId = new NetId(res.getNet().get(0), ownerId, creationTime);
                    // Need to cleanup old netId if its not the same.
                    NetId oldNetId = null;
                    for (String key : netIds.keySet()) {
                        oldNetId = netIds.get(key);
                        if (!newNetId.getId().equals(oldNetId.getId())) {
                            updated = true;
                            NetResources resources = netToResources.get(oldNetId.getId());
                            if (resources != null) {
                                resources.removeResourceRoute(route.getResId());
                                if (0 == resources.getResourceRouteCount()) {
                                    netToResources.remove(oldNetId.getId());
                                    try {
                                        connection.requestNetworkStatus(oldNetId.getId(), removeInterval);
                                    } catch (TransportException e) {
                                        log.warn("Unable to remove the request network status for {}", oldNetId.getId(),
                                                e);
                                    }
                                }
                            } else {
                                log.info("Overwriting an exlusive route that we did not own.");
                            }
                        }
                    }
                    netIds.clear();
                    netIds.put(newNetId.getId(), newNetId);
                    nameserver.internal.NetId interNetId = new nameserver.internal.NetId();
                    interNetId.setId(newNetId.getId());
                    interNetId.setAction(ActionEnum.ADD);
                    interNetId.setTimestamp(DateFromXMLAdapter.convert(creationTime));
                    interNetIds.add(interNetId);

                    if (updated || oldNetId == null) {
                        NetResources resources = netToResources.get(newNetId.getId());
                        if (resources == null) {
                            NetResources newResources = new NetResources(newNetId.getId());
                            resources = netToResources.putIfAbsent(newNetId.getId(), newResources);

                            if (resources == null) {
                                resources = newResources;
                            }
                        }
                        resources.addResourceRoute(route);
                    }

                    break;
                case ANY:
                case ALL:
                default:
                    for (String net : res.getNet()) {
                        newNetId = new NetId(net, ownerId, creationTime);
                        netIds.put(newNetId.getId(), newNetId);

                        NetResources resources = netToResources.get(net);
                        if (resources == null) {
                            NetResources newResources = new NetResources(net);
                            resources = netToResources.putIfAbsent(newNetId.getId(), newResources);
                            if (resources == null) {
                                resources = newResources;
                                // this.connection.requestNetworkStatus(netId.getId(),
                                // ADD_INTERVAL);
                            }
                        }
                        resources.addResourceRoute(route);

                        interNetId = new nameserver.internal.NetId();
                        interNetId.setId(newNetId.getId());
                        interNetId.setAction(ActionEnum.ADD);
                        interNetId.setTimestamp(DateFromXMLAdapter.convert(creationTime));
                        interNetIds.add(interNetId);
                    }
                    break;
            }
            log.debug("{} : {} => {}", res.getName().toLowerCase(), route.getType().toString(), netIds.keySet());
        } else {
            // type conflict between what we have and what is being requested
            log.warn("Unable to update resource: {} due to type mismatch [{} != {}]", res.getName().toLowerCase(),
                    route.getType().toString(), res.getType().toString());
        }

        return routeEntry;
    }

    /**
     * NetId has not been seen for a while or Network Status told us he left so expire his resources that I own.
     *
     */
    protected void resourceDeregistration(String netId) {
        NetResources netResources = netToResources.remove(netId);
        if (netResources != null) {
            // NameServerUpdate update = new NameServerUpdate();
            // update.setFull(false);
            List<RouteEntry> routes = new ArrayList<>();
            for (String resId : netResources.getResourceRouteIds()) {
                ResourceRoute rr = netResources.getResourceRoute(resId);
                RouteEntry routeEntry = new RouteEntry();
                routeEntry.setName(resId);
                routeEntry.setType(nameserver.internal.ResourceType.valueOf(rr.getType().toString()));
                routes.add(routeEntry);
                List<nameserver.internal.NetId> netIds = routeEntry.getNetId();
                nameserver.internal.NetId interNetId = new nameserver.internal.NetId();
                interNetId.setId(netId);
                interNetId.setTimestamp(DateFromXMLAdapter.convert(new Date()));
                interNetId.setAction(ActionEnum.REMOVE);
                netIds.add(interNetId);

                rr.getNetIds().remove(netId);
                if (rr.getNetIds().isEmpty()) {
                    resources.remove(resId);
                }

            }
            log.debug("Sending deregister for {}", netId);
            sendNameServerUpdate(routes, false);
        }
    }

    /**
     *
     * @param envelope
     * @param payload
     */
    protected void resourceDeregistration(Envelope envelope, ResourceDeregistration payload) {
        log.debug("deregistration request from {}", envelope.getSource());
        SILKWAVE resp = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(resp,
                SecurityManipulator.mergeSecuritySettings(envelope.getSecurity(), hubMinClassification),
                connection.getNetId(), log);
        resp.setMessageCorrelationId(envelope.getMessageCorrelationId());
        resp.setSessionToken(connection.getSessionToken());
        resp.getDestination().add(envelope.getSource());
        resp.setPayload(new MessagePayloadType());
        resp.setTrackingId(String.valueOf(System.currentTimeMillis()));
        ResourceDeregistrationResponse rdr = new ResourceDeregistrationResponse();
        List<RouteEntry> updateList = new ArrayList<>();
        try {
            write.lock();

            for (Resource res : payload.getResource()) {
                String resId = res.getName().toLowerCase();
                ResourceRoute rr = resources.get(resId);
                // TODO:What if we do not know about it yet??
                if (rr != null) {

                    RouteEntry routeEntry = new RouteEntry();
                    List<nameserver.internal.NetId> netIds = routeEntry.getNetId();
                    routeEntry.setName(rr.getResId());
                    routeEntry.setType(nameserver.internal.ResourceType.valueOf(res.getType().toString()));
                    updateList.add(routeEntry);
                    for (String netId : res.getNet()) {
                        log.debug("deregister {} => {}", resId, netId);
                        nameserver.internal.NetId interNetId = new nameserver.internal.NetId();
                        interNetId.setId(netId);
                        interNetId.setAction(ActionEnum.REMOVE);
                        interNetId.setTimestamp(DateFromXMLAdapter.convert(envelope.getTimestamp().getTimeInMillis()));
                        netIds.add(interNetId);

                        NetId oldNetId = rr.getNetIds().remove(netId);
                        if (oldNetId != null) {
                            NetResources netResources = netToResources.get(oldNetId.getId());
                            // Do we have interest
                            if (netResources != null) {
                                netResources.removeResourceRoute(resId);
                                if (0 == netResources.getResourceRouteCount()) {
                                    netToResources.remove(netId);
                                    try {
                                        connection.requestNetworkStatus(netId, removeInterval);
                                    } catch (TransportException e) {
                                        log.warn("Unable to remove the request network status for {}", oldNetId.getId(),
                                                e);
                                    }
                                }
                            } else {
                                // Need to see if the old owner has any
                                // interest in this route now.
                                boolean interest = false;
                                String oldOwner = oldNetId.getOwnerId();
                                for (String key : rr.getNetIds().keySet()) {
                                    NetId temp = rr.getNetIds().get(key);
                                    if (temp.getOwnerId().equals(oldOwner)) {
                                        interest = true;
                                    }
                                }
                                if (!interest) {
                                    NetResources otherNetResources = nameServersToResources.get(oldOwner);
                                    if (otherNetResources != null) {
                                        otherNetResources.removeResourceRoute(rr.getResId());
                                    }
                                }
                            }
                        }
                    }
                    if (rr.getNetIds().isEmpty()) {
                        log.debug("resource {} has been removed", resId);
                        resources.remove(resId);
                    }

                }
                rdr.getResource().add(res);
            }
            rdr.setOutcome(OutcomeEnum.SUCCESS);
        } finally {
            write.unlock();
        }

        if (!updateList.isEmpty()) {
            log.info("Sending a deregister msg");
            sendNameServerUpdate(updateList, false);
        }

        Element elem = XMLUtil.buildDocumentElement(rdr, "net", log);
        resp.getPayload().getAny().add(elem);
        Envelope env = new Envelope(resp);
        try {
            connection.send(env);

            if (log.isTraceEnabled()) {
                printResources();
                printNameServersToResources();
                printNetToResources();
            }
        } catch (TransportException e) {
            log.error("Unable to send Resource Deregistration response.", e);
        }
    }

    /**
     *
     * @param envelope
     * @param payload
     */
    protected void resourceLookup(Envelope envelope, ResourceLookup payload) {
        log.debug("lookup request from {}", envelope.getSource());
        SILKWAVE resp = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(resp,
                SecurityManipulator.mergeSecuritySettings(envelope.getSecurity(), maxRegistrationClassification),
                connection.getNetId(), log);
        resp.setMessageCorrelationId(envelope.getMessageCorrelationId());
        resp.setSessionToken(connection.getSessionToken());
        resp.getDestination().add(envelope.getSource());
        resp.setTrackingId(String.valueOf(System.currentTimeMillis()));
        resp.setPayload(new MessagePayloadType());

        ResourceLookupResponse rlr = resourceLookup(payload);
        Element elem = XMLUtil.buildDocumentElement(rlr, "net", log);
        resp.getPayload().getAny().add(elem);
        Envelope env = new Envelope(resp);
        try {
            connection.send(env);
        } catch (TransportException e) {
            log.error("Unable to send Resource Lookup Response.", e);
        }
    }

    /**
     *
     * @param payload
     *
     * @return
     */
    public ResourceLookupResponse resourceLookup(ResourceLookup payload) {
        ResourceLookupResponse rlr = new ResourceLookupResponse();
        for (String res : payload.getResource()) {
            ResourceResolution resolution = new ResourceResolution();

            if (-1 != res.indexOf('#')) {
                res = res.substring(0, res.indexOf('#'));
            }
            res = res.toLowerCase();

            resolution.setName(res);
            ResourceRoute rr = resources.get(res);
            if (rr != null) {
                // TODO: temporarily order ANY results by last updated so as to
                // get the most "current" for better routing
                if (ResourceType.ANY == rr.getType()) {
                    TreeMap<Long, String> sortedByTime = new TreeMap<>();
                    for (NetId net : rr.getNetIds().values()) {
                        sortedByTime.put(net.getCreationTime(), net.getId());
                    }
                    for (String id : sortedByTime.descendingMap().values()) {
                        resolution.getNet().add(id);
                    }
                } else {
                    for (NetId net : rr.getNetIds().values()) {
                        resolution.getNet().add(net.getId());
                    }
                }
                resolution.setResolved(true);
                resolution.setType(rr.getType());

            } else {
                resolution.setResolved(false);
                resolution.setType(ResourceType.EXCLUSIVE);
            }
            resolution.setExpiration(defaultExpiration);
            rlr.getResolution().add(resolution);
            log.debug("lookup {} => {}", res, resolution.getNet());
        }
        rlr.setOutcome(OutcomeEnum.SUCCESS);
        return rlr;
    }

    protected void printResources() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n**************Resources******************\n");
        for (String key : resources.keySet()) {
            ResourceRoute rr = resources.get(key);
            builder.append(rr.toString());
        }
        builder.append("********************************\n");
        log.trace(builder.toString());
    }

    protected void printNameServersToResources() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n************NameServerResources********************\n");
        for (String key : nameServersToResources.keySet()) {
            NetResources netResources = nameServersToResources.get(key);
            builder.append(netResources.toString());
        }
        builder.append("********************************\n");
        log.trace(builder.toString());
    }

    protected void printNetToResources() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n************NetToResources********************\n");
        for (String key : netToResources.keySet()) {
            NetResources netResources = netToResources.get(key);
            builder.append(netResources.toString());
        }
        builder.append("********************************\n");
        log.trace(builder.toString());
    }

    @Override
    public void onMessage(Envelope envelope) {
        try {
            List<Object> msgs = envelope.getPayloadObjects();

            for (Object msg : msgs) {

                if (msg instanceof ResourceRegistration) {
                    resourceRegistration(envelope, (ResourceRegistration) msg);
                } else if (msg instanceof ResourceDeregistration) {
                    resourceDeregistration(envelope, (ResourceDeregistration) msg);
                } else if (msg instanceof ResourceLookup) {
                    resourceLookup(envelope, (ResourceLookup) msg);
                } else if (msg instanceof NameServerUpdate) {
                    handleNameServerUpdate(envelope, (NameServerUpdate) msg);
                } else if (msg instanceof NameServerPullRequest) {
                    handleNameServerPullRequest(envelope, (NameServerPullRequest) msg);
                } else if (msg instanceof NameServerPullResponse) { //JC-530
                    handleNameServerPullResponse(envelope);
                } else if (msg instanceof FailureNotification notif) {
                    log.debug("Received a FailureNotification for messageId: {} trackingId: {} reason: {}",
                            notif.getMessageId(), notif.getTrackingId(), notif.getError());
                } else if (msg instanceof PingRequest) {
                    sendPingResponse(envelope);
                } else {
                    if (msg != null) {
                        log.warn("message received is not of supported type: {}", msg.getClass().getSimpleName());
                    } else {
                        log.warn("message received could not be unmarshalled");
                    }
                }

            }
        } catch (MessageValidationException ex) {
            log.warn("Unable to process payload \n{}", envelope.getOrigXml(), ex);
        } catch (Throwable e) {
            log.error("Unhandled Exception", e);
        }
    }

    private void sendPingResponse(Envelope envelope) {
        SILKWAVE jicd = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(jicd, connection.getNetId(), log);
        jicd.getDestination().add(envelope.getSource());
        jicd.setPayload(new MessagePayloadType());
        jicd.setSessionToken(connection.getSessionToken());
        jicd.setMessageCorrelationId(envelope.getMessageCorrelationId());
        PingResponse response = new PingResponse();
        response.setOutcome(OutcomeEnum.SUCCESS);
        response.setResource(connection.getNetId());
        response.setFinal(true);
        Element elem = XMLUtil.buildDocumentElement(response, "net", log);
        jicd.getPayload().getAny().add(elem);

        try {
            connection.send(new Envelope(jicd));
        } catch (TransportException e) {
            log.error("Unable to send message", e);
        }
    }

    @Override
    public void onError(Throwable error, Envelope envelope) {
        log.error("error", error);
    }

    @Override
    public void onTransportInterrupted() {
        log.warn("transport interrupted");
    }

    @Override
    public void onTransportResumed(String netId, boolean reconnected) {
        // check to see if we need to send the update security credential message
        try {
            checkSecurityCredentialUpdate();
            sendNameServerPullRequest();
            RegisterPrivilegedRoleResponse resp = registerAsNamingService();
            if (resp != null && OutcomeEnum.SUCCESS == resp.getOutcome()) {
                log.info("Successfully registered as naming service for [{}]", domain);
            } else if (resp == null) {
                log.warn("No Response when trying to register as a naming service for [{}]", domain);
            } else {
                // TODO: We need to do more then just log this.
                log.error("Unable to register as naming service for domain [{}].{}", domain, resp.getError());
            }
        } catch (TransportException e) {
            log.error("could not send security credential update", e);
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        String domain = "ns:" + Constants.props.getProperty("service.nameserver.domain", "ww");
        if (1 <= args.length) {
            domain = args[0];
        }

        int statusInterval = Integer.parseInt(Constants.props.getProperty("service.nameserver.statusInterval", "1"));
        int defaultExpiration = Integer.parseInt(
                Constants.props.getProperty("service.nameserver.defaultExpiration", "15000"));
        int maxAge = Integer.parseInt(
                Constants.props.getProperty("service.nameserver.maxSubscriberInactivity", "120000"));
        int maxNSAge = Integer.parseInt(Constants.props.getProperty("service.nameserver.maxNSInactivity", "120000"));
        int timeout = Integer.parseInt(Constants.props.getProperty("service.nameserver.requestTimeout", "5000"));
        String userName = Constants.props.getProperty("service.nameserver.user",
                Constants.props.getProperty("client.user", "nameserver"));
        String password = Constants.props.getProperty("service.nameserver.password",
                Constants.props.getProperty("client.pass", "manager"));
        String url = Constants.props.getProperty("service.nameserver.url", Constants.props.getProperty("client.url"));
        NameServer ns = new NameServer(domain, userName, password, url, statusInterval, defaultExpiration, maxAge,
                maxNSAge, timeout);
        try {
            ns.connect();

            Thread shutdownHook = new Thread(ns::disconnect);
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            CountDownLatch latch = new CountDownLatch(1);
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ns.disconnect();
        } catch (TransportException e1) {
            ns.log.error("error", e1);
        }
    }

    public ConcurrentMap<String, ResourceRoute> getResources() {
        read.lock();
        try {
            return new ConcurrentHashMap<>(resources);
        } finally {
            read.unlock();
        }
    }

    public ConcurrentMap<String, NetResources> getNetToResources() {
        read.lock();
        try {
            return new ConcurrentHashMap<>(netToResources);
        } finally {
            read.unlock();
        }
    }

    public ConcurrentMap<String, NetResources> getNameServersToResources() {
        read.lock();
        try {
            return new ConcurrentHashMap<>(nameServersToResources);
        } finally {
            read.unlock();
        }
    }
}
/*
 * The overall classification of this file is: UNCLASSIFIED//FOUO
 */