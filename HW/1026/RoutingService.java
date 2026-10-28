package gov.ic.silkwave.routing;

/*
 * SOME NOTES ON ROUTING:
 *
 * TABLES AND THE BASIC APPROACH
 * The basic scheme is based on ideas from the BGP protocol.
 *
 * Each hub maintains a primary and an alternate route table.  In addition,
 * each hub maintains a hash table constructed from the primary and alternate
 * route tables that is specifically constructed to service getLinksTo and
 * getOrderedLinks calls; this is the routeResponse table.
 *
 * The primary table is a map where the keys are the names of destination hubs,
 * and the values are Routes to that destination.  A Route has a list of hubNames,
 * starting with the name of the hostHub, and ending with the destination hub.  The
 * second item in the list (index 1) is the name of the neighbor hub you need to
 * send the message to in order to reach the destination.
 *
 * The alternate table is a map where the keys are the names of destination hubs,
 * and the values are lists of Routes to that destination; the lists are sorted
 * in cost order.  The alternate table allows a hub's routing machinery to replace
 * withdrawn routes (see below for a discussion of advertised and withdrawn) immediately,
 * without waiting for update messages from neighbors.  It also allows the getOrderedLinks
 * calls to have multiple returns.
 *
 * UPDATE MESSAGES
 * Whenever a hub routing system sees a change in connectivity, it determines what routes
 * it wishes to withdraw, and what routes it wishes to advertise.  It creates a message
 * with an advertise list, and a withdraw list (either list may be empty), and sends
 * that message to all of its neighbors.  Each neighbor will process the message, and create
 * its own update message, and send that message to its neighbors.  This process does terminate,
 * because if a hub has no new routes to advertise, and no routes to withdraw in response to
 * an update message it receives, it does not send out an update message.
 *
 * THREADS, TASKS, SHARED DATA STRUCTURES
 * The RouteTables class contains the primary, alternate, and routeResponse tables.  The
 * RoutingService has an AtomicReference to this class.  When routing messages come in, or
 * calls to updateLink/removeLink are made, the current RouteTables class is used along with
 * the message/call information to create a new RouteTables instance.  The AtomicReference
 * scheme is then used to swap the new instance for the old one.
 *
 * In order to prevent two different threads from making changes based on the same
 * instance of RouteTables, but with different message/call information, each action
 * on the RouteTables is turned into a task to be performed by a single threaded
 * task Executor.  This guarantees that only one change is done at a time (order is not
 * guaranteed).  In the case of the updateLink call, Future is used to block the return
 * of the function until the task completes.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import gov.ic.silkwave.NetworkManager;
import gov.ic.silkwave.Service;
import gov.ic.silkwave.ServiceLocator;
import gov.ic.silkwave.common.RoutingScheme;
import gov.ic.silkwave.common.RoutingURI;
import gov.ic.silkwave.common.RoutingURISpec;
import gov.ic.silkwave.common.messages.builders.RtemsgManipulator;
import gov.ic.silkwave.common.messages.builders.SILKWAVEMessageManipulator;
import gov.ic.silkwave.common.messages.builders.SecurityManipulator;
import gov.ic.silkwave.common.messaging.Envelope;
import gov.ic.silkwave.common.transport.TransportException;
import gov.ic.silkwave.common.transport.jms.IRealTimeMessageHandler;
import gov.ic.silkwave.common.utils.Constants;
import gov.ic.silkwave.common.utils.FactoryUtil;
import gov.ic.silkwave.common.xml.XMLUtil;
import gov.ic.silkwave.registration.LocalRegistry;
import gov.ic.silkwave.registration.LocalRegistryCallback;
import gov.ic.silkwave.registration.LocalRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import silkwave.MessagePayloadType;
import silkwave.SILKWAVE;
import silkwave.internal.RoutingInfo;
import silkwave.internal.RoutingInfoResponse;
import silkwave.internal.Rtemsg;
import silkwave.net.OutcomeEnum;

/**
 * Implementation of the remote routing service
 * <p>
 * LOCKING Consider locking. We process the messages one at a time; we set up the Executor to do that. In order to
 * ensure that all table mods are done one at a time, we make the removeLink and updateLink execute tasks on the same
 * exec that the incoming messages use.
 * <p>
 * All the things that can change the table are now in one-at-a-time processing. The only thing that needs to be
 * protected is the table swap.
 */
public class RoutingService implements Service, IRealTimeMessageHandler, LocalRegistryCallback {
    private Logger log;
    private RoutingURI identity;
    private NetworkManager manager;
    private LocalRegistry registry;
    // TODO: is AtomicReference the best way to do this? many gets and few sets
    private AtomicReference<RouteTables> tables = new AtomicReference<>();
    private String name;
    private static final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentHashMap<String, Long> lastHeard = new ConcurrentHashMap<>();
    private final int LIVENESS_MULTIPLIER = Integer.parseInt(
            Constants.props.getProperty("routing.neighborLivenessMulitplier", "2"));

    private int HELLO_INTERVAL = Integer.parseInt(Constants.props.getProperty("routing.requestInterval", "600"));

    private boolean sendOldFormat = Boolean.parseBoolean(
            Constants.props.getProperty("backwards.compatible.rtemsg", "false"));

    public RoutingService() {
        log = LoggerFactory.getLogger(getClass());
    }

    public Set<String> getNeighborNames() {
        return tables.get().getNeighborNames();
    }

    public long getRouteCount() {
        return tables.get().getRouteCount();
    }

    public long getNeighborCount() {
        return tables.get().getNeighborCount();
    }

    public List<Route> getPrimaryRoutes() {
        return tables.get().getPrimaryRoutes();
    }

    
    public RouteTables getRouteTables() {
        return tables.get();
    }
    /**
     * Get list of routing options ordered by cost that may provide a path to given hub
     *
     * @param destination the destination hub name
     *
     * @return ordered list of RouteOption that satisfy the request
     */
    public List<RouteOption> getLinksTo(String destination) {

        List<RouteOption> result = tables.get().getDestinationHops(destination);
        if (log.isDebugEnabled()) {
            log.debug("RoutingService::getLinksTo:destination {}", destination);
            log.debug("result:{}", result);
        }
        return result;
    }

    /**
     * Select the route we believe to be the best choice and return no others
     *
     * @param destination the destination to reach
     *
     * @return the selected RouteOption or null if no options exist
     */
    public RouteOption getPrimaryLinkTo(String destination) {
        List<RouteOption> result = getLinksTo(destination);
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * Given a list of destinations provide a list of routing options; each item on the list is the cheapest route to
     * one of the destinations. The list is then ordered by cost. This could be used for selecting the nearest hub that
     * can provide a particular service.
     *
     * @param destinations list of destinations
     *
     * @return list of RouteOptions, in cost order.
     */
    public List<EndpointCost> getOrderedLinks(Collection<RoutingURISpec> destinations) {
        List<EndpointCost> result = new ArrayList<>();
        for (RoutingURISpec destination : destinations) {
            List<RouteOption> dh = tables.get().getDestinationHops(destination.getDomain());
            if (!dh.isEmpty()) {
                result.add(new EndpointCost(destination, dh.get(0).getCost()));
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public void onAddLocalRoute(LocalRoute route) {
        // a local connect - not relevant to us
    }

    @Override
    public void onError(Throwable error, Envelope envelope) {
        // error occurred on incoming message destined for routing service
        log.error("Cannot process:{}", envelope.getInvalidReason(), error);
    }

    /**
     * All onMessage does is dump a task onto the executor. The executor maintains a queue to work off the messages
     *
     * @param envelope message
     */
    @Override
    public void onMessage(Envelope envelope) {
        Runnable task = () -> handleMessage(envelope);
        exec.execute(task);
    }

    @Override
    public void onRemoveLocalRoute(LocalRoute route) {
        // a local disconnect - is it one of our neighbor links?
        RoutingURI uri = route.getRoutingURI();
        String neighborName = tables.get().getNeighborName(uri);
        if (neighborName != null) {
            removeLink(neighborName, uri);
        }
    }

    /**
     * Puts a task to remove the link onto our exec. See LOCKING note for more information on why this is done.
     *
     * @param neighbor the name of the neighbor to remove
     * @param link     the RoutingURI corresponding to the neighbor
     */
    public void removeLink(String neighbor, RoutingURISpec link) {
        Runnable task = () -> doRemoveLink(neighbor, link);
        exec.execute(task);
    }


    @VisibleForTesting
    void doRemoveLink(String neighbor, RoutingURISpec link) {
        try {
            log.info("doRemoveLink::neighbor: {}/{}", neighbor, link);
            Neighbor toRemove = new Neighbor(name, neighbor, link, Integer.MAX_VALUE);
            TableUpdate result = tables.get().removeLink(toRemove);
            tables.set(result.getTables());
            log.info("doRemoveLink::sending:{}", result.getLetter());
            doSend(result.getLetter());
        } catch (Exception e) {
            log.error("doRemoveLink::{},{} fails", neighbor, link, e);
        }
    }

    @Override
    public void startup() {
        identity = ServiceLocator.ROUTING;
        name = identity.getDomain();
        registry = ServiceLocator.getLocalRegistry();
        manager = ServiceLocator.getNetworkManager();

        // change order so tables are not null before registering the callback listener
        tables.set(RouteTables.build());

        manager.registerInternalListener(identity.getEndpoint(), this);
        registry.addCallback(this);

        schedule.scheduleAtFixedRate(this::helloAndSweep, 2L * HELLO_INTERVAL, HELLO_INTERVAL, TimeUnit.SECONDS);
        /*
         * NOTES ON HELLO The routing scheme depends on generating, sending,
         * receiving, and processing messages. The routing scheme can fail to
         * operate correctly if messages are garbled, or if routing messages are
         * not delivered for some reason.
         *
         * In order to fix problems caused by delivery failure of routing
         * messages, each hub will send a hello message to its neighbors
         * periodically. This hello message contains all the routes the hub has,
         * both alternate and primary.
         *
         * The receiving hub looks at all the routes it has that start with a
         * hop to the neighbor that sent the routes. If the receiving hub is
         * using a route that the neighbor no longer claims to have, it
         * withdraws that route. If there are routes the neighbor has that the
         * receiving hub is not aware of, that route is added either to the
         * alternate routes, or, if it is the best route to a destination, it is
         * added to the primary routes, and is advertised.
         *
         * This process is intended to allow the network routing to fix itself
         * in the (unlikely) event that some routing messages failed delivery.
         */
        log.info("Started HubService:{}", name);
    }

    /**
     * method to trigger the helloAndSweep action
     * <p>
     * separated out for better testing visibility into actual functionality
     * <p>
     * calculates the maxSilenceMs by LIVENESS_MULTIPLIER * HELLO_INTERVAL * 1000L default values: LIVENESS_MULTIPLIER =
     * 2 HELLO_INTERVAL = 600  [seconds]
     */
    private void helloAndSweep() {
        long threshold = (long) LIVENESS_MULTIPLIER * HELLO_INTERVAL * 1000L;
        helloAndSweep(System.currentTimeMillis(), threshold);
    }

    /**
     * task to be run at scheduled *interval* (as defined in Constants, default 600 seconds) Has 2 main goals:
     * <p>
     * 1) Send a hello message (rtemsg:request) to all neighbors to ask for routes
     * <p>
     * 2) check for the last heard time from each neighbor, and remove any that have gone silent for longer than
     * maxSilenceMs
     * <p>
     * Returns a List of neighbor names that were removed from our internal neighbors structure
     *
     * @param nowMs        current timestamp (or time to check against) in milliseconds
     * @param maxSilenceMs longest allowable 'not heard from' duration in milliseconds
     *
     * @return list of all neighbor names that were evicted
     */
    @VisibleForTesting
    List<String> helloAndSweep(long nowMs, long maxSilenceMs) {
        List<String> evicted = new ArrayList<>();
        try {
            // 1) hello
            String message = RoutingMessage.makeMessage("request", Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList());
            log.info("Sending hello request:{}", message);
            Letter letter = Letter.build(message, getNeighborNames());
            doSend(letter);

            // 2 sweep: force removal of neighbors that have sent no messages in the last (LIVENESS_MULTIPLIER *
            // interval)
            for (String neighbor : getNeighborNames()) {
                Long ts = lastHeard.get(neighbor);
                if (ts == null) {
                    lastHeard.putIfAbsent(neighbor, nowMs); // seed a timestamp the first time
                } else if (nowMs - ts > maxSilenceMs) { // has not been heard from in configured interval
                    RoutingURISpec uri = tables.get().getNeighborsURI(neighbor);
                    log.warn("routing sweep: neighbor {} silent > {}s, removing link", neighbor, maxSilenceMs / 1000);
                    removeLink(neighbor, uri);
                    lastHeard.remove(neighbor);
                    evicted.add(neighbor);
                }
            }
            // stop tracking neighbors that were removed
            lastHeard.keySet().retainAll(getNeighborNames());

        } catch (Throwable e) {
            log.error("helloAndSweep failed", e);
        }
        return evicted;
    }

    @Override
    public void shutdown() {
        log.info("RoutingService received shutdown command; attempting to clear routes and notify neighbors");
        try {
            schedule.shutdownNow(); // stop hello/request messages

            // Best effort to send goodbye,  run through exec so it will happen after any
            // in -progress table changes
            Future<?> bye = exec.submit(() -> {
                RouteTables rt = tables.get();
                Set<String> neighbors = rt.getNeighborNames();
                if (neighbors.isEmpty()) {
                    return;
                }
                String message = RoutingMessage.makeMessage("update", Collections.emptyList(), // advertise nothing
                        rt.getPrimaryRoutes()); // withdraw all routes
                doSend(Letter.build(message, neighbors));
                tables.set(RouteTables.build()); //reset to empty
            });
            bye.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("shutdown goodbye failed (neighbors should time us out): {}", e.toString());
        }
        // NOTE: exec is static final, do NOT call exec.shutdown() here or a future RoutingService instance
        // in the same JVM would be dead on arrival.   Should change exec to an instance field
    }

    /**
     * Add/Update a link to a neighboring hub. Sets up a task to do this, and adds it to exec to execute. See LOCKING
     * note for more information on why this is done.
     *
     * @param neighborName the neighbor hub id
     * @param link         the net address of the connection
     * @param cost         the relative cost of this link
     */
    public void updateLink(String neighborName, RoutingURISpec link, int cost) {
        Runnable task = () -> doUpdateLink(neighborName, link, cost);
        // For this function, we want to block until the task is done
        // We still want to use the exec, in order to ensure that tasks
        // are done one at a time.
        Future<?> f = exec.submit(task);
        try {
            f.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("updateLink fails waiting for result", e);
        } catch (TimeoutException e) {
            log.error("updateLink fails waiting for result:", e);
        }
    }

    private void doUpdateLink(String neighborName, RoutingURISpec link, int cost) {
        try {
            Neighbor rte = new Neighbor(name, neighborName, link, cost);
            TableUpdate result = tables.get().updateLink(rte);
            tables.set(result.getTables());
            log.info("doUpdateLink::sending message:{}", result.getLetter());
            doSend(result.getLetter());
            log.info("doUpdateLink::tables:\n{}", tables);
        } catch (Exception e) {
            log.error("doUpdateLink({},{},{}) fails", neighborName, link, cost, e);
        }
    }

    @VisibleForTesting
    void doSend(Letter toSend) {
        if (toSend.getAddressees().isEmpty()) {
            return;
        }
        try {
            sendMessage(toSend.getMessage(), toSend.getAddressees());
        } catch (TransportException e) {
            log.error("doSend::cannot send:{}", toSend, e);
        }
    }

    protected List<List<String>> getDump() {
        List<List<String>> dump = new ArrayList<>();
        RouteTables table = tables.get();
        List<Route> routes = table.getPrimaryRoutes();
        List<Route> alts = table.getAltRoutes();
        for (Route route : routes) {
            List<String> row = new ArrayList<>();
            dump.add(row);
            row.add(route.getDestination());
            row.add(route.getNeighbor());
            row.add(String.valueOf(route.getCost()));
            int cnt = 0;
            for (Route alt : alts) {
                if (alt.getDestination().equals(route.getDestination())) {
                    cnt++;
                }
            }
            row.add(String.valueOf(cnt));
        }

        return dump;
    }

    public RoutingInfoResponse buildRoutingInfoResponse() {
        RoutingInfoResponse response = new RoutingInfoResponse();

        RouteTables table = tables.get();
        List<Route> routes = table.getPrimaryRoutes();
        List<Route> alts = table.getAltRoutes();

        Document xmlDoc = FactoryUtil.getDocumentBuilder().newDocument();

        for (Route route : routes) {
            RoutingInfo info = new RoutingInfo();
            info.setDestination(route.getDestination());
            info.setNeighbor(route.getNeighbor());
            info.setCost(route.getCost());

            Node primaryRoute = xmlDoc.createElement("PrimaryRoute");
            primaryRoute.appendChild(xmlDoc.createTextNode(route.toString()));
            info.getAny().add(primaryRoute);

            int cnt = 0;
            for (Route alt : alts) {
                if (alt.getDestination().equals(route.getDestination())) {
                    cnt++;
                    Node alternateRoute = xmlDoc.createElement("AlternateRoute");
                    alternateRoute.appendChild(xmlDoc.createTextNode(alt.toString()));
                    info.getAny().add(alternateRoute);
                }
            }
            info.setAlternates(cnt);

            // add in the client id of the connector that is representing the neighbor
            RoutingURISpec neighborsURI = table.getNeighborsURI(route.getNeighbor());

            Node neighborElement = xmlDoc.createElement("NeighborURI");
            neighborElement.appendChild(xmlDoc.createTextNode(neighborsURI.toString()));

            info.getAny().add(neighborElement);

            response.getRoutingInfo().add(info);
        }

        response.setOutcome(OutcomeEnum.SUCCESS);

        return response;
    }

    /**
     * helper function to record last time a neighbor was heard, will help with testing
     *
     * @param neighborName name of the neighbor to record
     * @param whenMs       timestamp to store as last heard for this neighbor, in milliseconds
     */
    void recordHeard(String neighborName, long whenMs) {
        lastHeard.put(neighborName, whenMs);
    }

    /**
     * Actually handle the message. Called by the exec. This will create new tables, an advertised list, a withdrawn
     * list, and a message.
     *
     * @param envelope message
     */
    @VisibleForTesting
    void handleMessage(Envelope envelope) {
        try {
            String source = envelope.getSource();
            // update 'heartbeat' time from neighbor for tracking
            recordHeard(RoutingURI.build(source, log).getDomain(), System.currentTimeMillis());
            String payloadText = envelope.getPayloadText();
            List<Object> payloads = envelope.getPayloadObjects();

            // to be backwards-compatible with 4.2.0, look for JSON
            // in the payload text
            int start = payloadText.indexOf('{');
            int end = payloadText.lastIndexOf('}');
            if (-1 != start && end > start) {
                // this is the 4.2.0 way of doing it
                String json = payloadText.substring(start, end + 1);
                Rtemsg rm = RtemsgManipulator.convertJSONToXML(json);
                // we'll assume that there was just one payload object, and it
                // will be in the old format. replace it with this new one.
                payloads.clear();
                payloads.add(rm);
                log.info("handling a 4.2.0-style rtemsg");
            }

            for (Object payload : payloads) {

                if (payload instanceof Rtemsg rtemsg) {
                    log.info("handleMessage::from:{}\n{}", source, payloadText);
                    if (SecurityManipulator.isAcceptable(NetworkManager.NETWORK_MIN_CLASS, envelope.getSecurity())) {
                        // convert the xml rtemsg into the json rtemsg
                        String json = RtemsgManipulator.convertXMLToJSON(rtemsg);
                        RoutingMessage message = RoutingMessage.build(json);
                        TableUpdate update = TableUpdate.build();
                        if ("update".equals(message.getMessageType())) {
                            handleUpdateMessage(message, tables);
                        } else if ("request".equals(message.getMessageType())) {
                            log.debug("handleMessage::request:got message {}\n{}", source, payloadText);
                            List<Route> currentTables = tables.get().getPrimaryRoutes();
                            String newMessage = RoutingMessage.makeMessage("response", currentTables,
                                    Collections.emptyList(), Collections.emptyList());
                            Letter letter = Letter.build(newMessage,
                                    ImmutableSet.of(RoutingURI.build(source, log).getDomain()));
                            log.debug("handleMessage::request:sending message\n{}", letter);
                            doSend(letter);
                        } else if ("response".equals(message.getMessageType())) {
                            log.debug("handleMessage::response:got message {}\n{}", source, payloadText);
                            update = tables.get().routingUpdate(name, message.getTableRoutes());
                            tables.set(update.getTables());
                            log.info("handleMessage::response:calculated update{}", update);
                            log.info("handleMessage::response:sending message\n{}", update.getLetter());
                            doSend(update.getLetter());
                        } else {
                            log.warn("handleMessage::got unknown message {}\n{}", source, payloadText);
                            return;
                        }
                    } else {
                        log.info(
                                "handleMessage::update:received a message that was higher classification then we " +
                                        "except from \n{}",
                                envelope.getSource());
                        // TODO:Failed letter or just log it at least provide better logging.
                    }
                } else {
                    log.warn("Received unexpected message: \n{}", payloadText);
                }
            }
        } catch (Throwable e) {
            log.error("handleMessage::Cannot handle message:\n{}", envelope.getOrigXml(), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("handleMessage::COMPLETE:tables:\n{}", tables.get());
        }
    }

    /**
     * handle a routing message that is an 'update' type. Build and set a new route tables to the AtomicReference, also
     * sends an update message with changes from the update.
     * <p>
     * the CALLER must make sure the message is an 'update' message
     *
     * @param message RouteMessage that MUST be an UPDATE type
     * @param tables  AtomicReference to a RouteTable, that will be updated by this method
     */
    @VisibleForTesting
    void handleUpdateMessage(RoutingMessage message, AtomicReference<RouteTables> tables) {
        TableUpdate update = TableUpdate.build();
        update = tables.get().update(name, message.getAdvertised(), message.getWithdrawn());
        tables.set(update.getTables());
        log.info("handleMessage::update:sending message\n{}", update.getLetter());
        doSend(update.getLetter());
    }


    @VisibleForTesting
    void sendMessage(String text, Collection<String> neighbors) throws TransportException {
        SILKWAVE jicd = new SILKWAVE();
        SILKWAVEMessageManipulator.buildMessage(jicd, NetworkManager.NETWORK_MIN_CLASS, identity.toString());
        for (String hub : neighbors) {
            RoutingURI uri = RoutingURI.build(RoutingScheme.net.toString(), hub, "routing", log);
            jicd.getDestination().add(uri.toString());
        }

        // convert the json text here into the xml rtemsg
        Rtemsg rtemsg = RtemsgManipulator.convertJSONToXML(text);
        MessagePayloadType payload = XMLUtil.buildMessagePayload(rtemsg, "internal");

        // here we're going to take advantage of the fact that 4.2.1 allows multiple
        // payload objects to send both the "new" XML Rtemsg AND the "old" JSON Rtemsg
        // at the same time. 4.2.0 hubs will find the { and } anywhere in the payload
        // text and work with the JSON. 4.2.1 hubs will process the XML version and then
        // just log a warning about not being able to handle the JSON version. So we
        // achieve backwards compatability.
        //
        // NOTE - turned off by default. left here just in case...
        if (sendOldFormat) {
            Element oldElement = XMLUtil.buildDocumentElement("rtemsg", text);
            payload.getAny().add(oldElement);
        }

        jicd.setPayload(payload);

        Envelope envelope = new Envelope(jicd);
        if (envelope.isValid()) {
            manager.sendOutgoing(envelope);
        } else {
            log.warn("Unable to send routing message: {}", envelope.getInvalidReason());
        }
    }

    @Override
    public void onTransportInterrupted() {
        log.info("TransportInterrupted");

    }

    @Override
    public void onTransportResumed(String netId, boolean reconnected) {
        log.info("TransportResumed");
    }

    @Override
    public void onPrivelegedLocalRoute(LocalRoute route) {
        // Do nothing
    }
}


