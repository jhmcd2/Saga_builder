package gov.ic.silkwave;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import gov.ic.silkwave.common.RoutingScheme;
import gov.ic.silkwave.common.RoutingURI;
import gov.ic.silkwave.common.utils.Constants;
import gov.ic.silkwave.data.files.FileService;
import gov.ic.silkwave.data.streams.StreamService;
import gov.ic.silkwave.discovery.DiscoveryService;
import gov.ic.silkwave.naming.NamingRegistry;
import gov.ic.silkwave.networkinfo.NetworkInfoService;
import gov.ic.silkwave.networkstatus.NetworkStatusService;
import gov.ic.silkwave.registration.LocalRegistry;
import gov.ic.silkwave.registration.Registrar;
import gov.ic.silkwave.routing.RoutingService;
import gov.ic.silkwave.security.RestrictionManager;
import gov.ic.silkwave.security.SecurityService;
import gov.ic.silkwave.transport.destination.DestinationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLocator {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceLocator.class);
    private static final ConcurrentMap<RoutingURI, Service> services = new ConcurrentHashMap<>();
    private static DestinationManager destMgr;
    private static final String NET = RoutingScheme.net.toString();

    public static final RoutingURI NETWORK_MANAGER = RoutingURI.build(NET, Constants.MYDOMAIN, "manager", LOG);
    public static final RoutingURI LOCAL_REGISTRY = RoutingURI.build(NET, Constants.MYDOMAIN, "localregistry", LOG);
    public static final RoutingURI REGISTRAR = RoutingURI.build(NET, Constants.MYDOMAIN, "registrar", LOG);
    public static final RoutingURI DISCOVERY = RoutingURI.build(NET, Constants.MYDOMAIN, "discovery", LOG);
    public static final RoutingURI NAMING = RoutingURI.build(NET, Constants.MYDOMAIN, "naming", LOG);
    public static final RoutingURI NETWORK_STATUS = RoutingURI.build(NET, Constants.MYDOMAIN, "networkstatus", LOG);
    public static final RoutingURI ROUTING = RoutingURI.build(NET, Constants.MYDOMAIN, "routing", LOG);
    public static final RoutingURI FILES = RoutingURI.build(NET, Constants.MYDOMAIN, "files", LOG);
    public static final RoutingURI FILE_SERVER = RoutingURI.build(NET, Constants.MYDOMAIN, "fileserver", LOG);
    public static final RoutingURI STREAMS = RoutingURI.build(NET, Constants.MYDOMAIN, "streams", LOG);
    public static final RoutingURI SECURITY = RoutingURI.build(NET, Constants.MYDOMAIN, "security", LOG);
    public static final RoutingURI AUTHENTICATION = RoutingURI.build(NET, Constants.MYDOMAIN, "authentication", LOG);
    public static final RoutingURI EXTENDED_AUTHENTICATION = RoutingURI.build(NET, Constants.MYDOMAIN,
            "extended.authentication", LOG);
    public static final RoutingURI AUTHORIZATION = RoutingURI.build(NET, Constants.MYDOMAIN, "authorization", LOG);
    public static final RoutingURI NETWORK_INFO = RoutingURI.build(NET, Constants.MYDOMAIN, "networkinfo", LOG);

    private static RestrictionManager restrictionManager;

    protected static void addService(RoutingURI uri, Service service) {
        services.put(uri, service);
    }

    protected static void removeService(RoutingURI uri, Service service) {
        services.remove(uri);
    }

    public static Service getService(RoutingURI uri) {
        if (uri != null) {
            return services.get(uri);

        } else {
            return null;
        }
    }

    public static NetworkManager getNetworkManager() {
        return (NetworkManager) services.get(NETWORK_MANAGER);
    }

    public static LocalRegistry getLocalRegistry() {
        return (LocalRegistry) services.get(LOCAL_REGISTRY);
    }

    public static Registrar getRegistrar() {
        return (Registrar) services.get(REGISTRAR);
    }

    public static DiscoveryService getDiscoveryService() {
        return (DiscoveryService) services.get(DISCOVERY);
    }

    public static NamingRegistry getNamingRegistry() {
        return (NamingRegistry) services.get(NAMING);
    }

    public static NetworkStatusService getNetworkStatusService() {
        return (NetworkStatusService) services.get(NETWORK_STATUS);
    }

    public static RoutingService getRoutingService() {
        return (RoutingService) services.get(ROUTING);
    }

    public static SecurityService getSecurityService() {
        return (SecurityService) services.get(SECURITY);
    }

    public static NetworkInfoService getNetworkInfoService() {
        return (NetworkInfoService) services.get(NETWORK_INFO);
    }

    public static FileService getFiles() {
        return (FileService) services.get(FILES);
    }

    public static StreamService getStreams() {
        return (StreamService) services.get(STREAMS);
    }

    public static DestinationManager getDestinationManager() {
        return destMgr;
    }

    public static void setDestinationManager(DestinationManager dstMgr) {
        destMgr = dstMgr;
    }

    protected static void setRestrictionManager(RestrictionManager manager) {
        restrictionManager = manager;
    }

    public static RestrictionManager getRestrictionManager() {
        return restrictionManager;
    }
}
