/*
 * The overall classification of this file is: UNCLASSIFIED
 */
package gov.ic.silkwave.common.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import gov.ic.silkwave.common.client.security.KeySpec;
import gov.ic.silkwave.common.messaging.Envelope;
import gov.ic.silkwave.common.messaging.request.ResourceRequest;
import gov.ic.silkwave.common.transport.jms.IRealTimeMessageHandler;
import gov.ic.silkwave.common.transport.jms.IRealTimeNetworkStatusHandler;
import silkwave.SILKWAVE;
import silkwave.markings.Restriction;
import silkwave.markings.Security;
import silkwave.net.ResourceType;
import silkwave.net.ScheduleNetworkStatusType;
import silkwave.security.SecurityAuthenticatedResponse;

/**
 * @deprecated included for compatibility with 2.4.1.4
 */
@Deprecated
public class MockConnection implements ConnectionInterface {

    private static final Map<String, MockConnection> CONNECTIONS = new HashMap<>();

    protected boolean connected;
    protected String netId = UUID.randomUUID().toString();

    protected IRealTimeMessageHandler messageHandler;

    private boolean calledReinitialize;
    protected List<Envelope> messages = new ArrayList<>();
    protected List<ResourceRequest> registeredResources = new ArrayList<>();
    protected Map<ScheduleNetworkStatusType, List<String>> networkStatusToNetIDMapping = new HashMap<>();

    public MockConnection() {
        CONNECTIONS.put(netId, this);
    }

    public boolean didReinitializeGetCalled() {
        return calledReinitialize;
    }

    public void reset() {
        calledReinitialize = false;
    }

    public List<Envelope> getMessages() {
        return messages;
    }

    @Override
    public boolean connect(String connectionName, IRealTimeMessageHandler mhandler, boolean attemptReconnect) {
        messageHandler = mhandler;
        connected = true;
        return connected;
    }

    /**
     * How to send a message to a MockAppGeoConnection. This method allows us to push a silkwave message to a client for
     * testing
     *
     * @param message message to pass to connections
     */
    public static void injectMessage(SILKWAVE message) {
        if (message != null) {
            for (String destination : message.getDestination()) {
                MockConnection connection = CONNECTIONS.get(destination);
                if (connection != null && connection.messageHandler != null) {
                    connection.messageHandler.onMessage(new Envelope(message));
                }
            }
        }
    }

    /**
     * How to send a message to all current mock connections
     *
     * @param message message to pass to connections
     */
    public static void injectAll(SILKWAVE message) {
        if (message != null) {
            for (MockConnection connection : CONNECTIONS.values()) {
                if (connection.messageHandler != null) {
                    connection.messageHandler.onMessage(new Envelope(message));
                }
            }
        }

    }

    @Override
    public Security getHubMinimumSecurity() {
        return null;
    }

    @Override
    public String getNetId() {
        return netId;
    }

    @Override
    public List<String> resolveResourceName(String resource, Security security) {
        return null;
    }

    @Override
    public void send(Envelope message) {
        messages.add(message);
    }

    @Override
    public void requestNetworkStatus(String net, ScheduleNetworkStatusType schedule) {
        List<String> netIDs = new ArrayList<>();
        netIDs.add(net);
        requestNetworkStatus(netIDs, schedule);
    }

    @Override
    public boolean registerResourceName(String resource, String domain, ResourceType type, Security security) {
        registeredResources.add(new ResourceRequest(resource, domain, type, netId, security));
        return true;
    }

    @Override
    public void setNetworkStatusHandler(IRealTimeNetworkStatusHandler networkHandler) {
    }

    @Override
    public void reinitializeConnection(boolean b) {
        calledReinitialize = true;
    }

    @Override
    public String getSessionToken() {
        return null;
    }

    @Override
    public void waitForResource(String resource, boolean hubOnly, Security security) {
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public String getConnectionName() {
        return null;
    }

    @Override
    public long getCurrentTimeoutMillis() {
        return 0;
    }

    @Override
    public void setCurrentTimeoutMillis(long currentTimeoutMillis) {
    }

    @Override
    public void setKeystore(String keystorePath, String keystorePass, String keystoreType) {
    }

    @Override
    public void setKeystore(KeySpec keyspec) {
    }

    @Override
    public void setTruststore(String truststorePath, String truststorePass) {
    }

    @Override
    public String getHubId() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean ping(String resource, boolean hubOnly, long timeout, Security security) {
        return false;
    }

    @Override
    public SecurityAuthenticatedResponse updateSecurityCredentialsFromKeyList(List<KeySpec> keySpecList) {
        return null;
    }

    @Override
    public void requestNetworkStatus(List<String> nets, ScheduleNetworkStatusType schedule) {
        if (networkStatusToNetIDMapping.containsKey(schedule)) {
            networkStatusToNetIDMapping.get(schedule).addAll(nets);
        } else {
            networkStatusToNetIDMapping.put(schedule, nets);
        }
    }

    @Override
    public Envelope getResponse(Envelope request) {
        return null;
    }

    @Override
    public Envelope getResponse(Envelope request, long timeout) {
        return null;
    }

    @Override
    public boolean authorizationTest(String silkwaveId, Restriction restriction) {
        return false;
    }

    public boolean isRegisteredResource(String resource, String domain, ResourceType type) {
        boolean found = false;

        for (ResourceRequest resourceRequest : registeredResources) {
            if (resourceRequest.getResource().equals(resource) && resourceRequest.getDomain()
                    .equals(domain) && resourceRequest.getType() == type) {
                found = true;
                break;
            }
        }

        return found;
    }

    public boolean doesNetworkStatusRequestExist(String net, ScheduleNetworkStatusType schedule) {
        if (!networkStatusToNetIDMapping.containsKey(schedule)) {
            return false;
        }

        return networkStatusToNetIDMapping.get(schedule).contains(net);
    }

    public boolean isThereASentMessageWithCorrelationID(String correlation) {
        for (Envelope envelope : messages) {
            if (envelope.getMessageCorrelationId().equals(correlation)) {
                return true;
            }
        }

        return false;
    }

    public boolean isThereASentMessageToDestination(String destination) {
        for (Envelope envelope : messages) {
            if (envelope.getDestinations().contains(destination)) {
                return true;
            }
        }

        return false;
    }
}
/*
 * The overall classification of this file is: UNCLASSIFIED
 */
