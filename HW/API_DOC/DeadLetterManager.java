package gov.ic.silkwave;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import gov.ic.silkwave.common.RoutingURI;
import gov.ic.silkwave.common.logging.TrackingLogger;
import gov.ic.silkwave.common.messages.builders.SILKWAVEMessageManipulator;
import gov.ic.silkwave.common.messages.builders.SecurityManipulator;
import gov.ic.silkwave.common.messaging.Envelope;
import gov.ic.silkwave.common.transport.TransportException;
import gov.ic.silkwave.common.utils.Constants;
import gov.ic.silkwave.common.xml.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import silkwave.MessagePayloadType;
import silkwave.SILKWAVE;
import silkwave.net.FailureNotification;

public class DeadLetterManager {
    private final Logger log;
    private final Logger failedXMLLogger;
    private final NetworkManager networkManager;

    private final LoadingCache<String, Boolean> cache;
    private static final long MAX_SIZE = Long.parseLong(
            Constants.props.getProperty("service.deadletter.maxCacheSize", "10000"));
    private static final long MAX_AGE = Long.parseLong(
            Constants.props.getProperty("service.deadletter.maxCacheAge", "300"));

    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong badEnvelopeCount = new AtomicLong(0);
    private final AtomicLong invalidSourceCount = new AtomicLong(0);
    private final AtomicLong notDelivarableCount = new AtomicLong(0);
    private final AtomicLong notResolvableCount = new AtomicLong(0);
    private final AtomicLong notRoutableCount = new AtomicLong(0);
    private final AtomicLong unknownCount = new AtomicLong(0);
    private final AtomicLong invalidNetPayloadCount = new AtomicLong(0);
    private final AtomicLong improperClassificationCount = new AtomicLong(0);
    private final AtomicLong notAuthorizedCount = new AtomicLong(0);

    private DeadLetterManager() {
        log = LoggerFactory.getLogger(getClass());
        failedXMLLogger = LoggerFactory.getLogger("FailedXMLLogger");
        log.info("Constructed DeadLetterManager");

        cache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).expireAfterWrite(MAX_AGE, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public Boolean load(String key) {
                        return Boolean.TRUE;
                    }
                });

        networkManager = ServiceLocator.getNetworkManager();
    }

    private static class SingletonHolder {
        private static final DeadLetterManager INSTANCE = new DeadLetterManager();
    }

    public static DeadLetterManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void process(Envelope envelope, RoutingURI dest, String reason, DeadLetterTypeEnum type) {
        TrackingLogger.messageFailed(envelope, (dest != null ? dest.toString() : ""), reason);
        log.warn("{}{} from source ({}) and connectionId ({})", reason, dest != null ? " : " + dest : "",
                envelope.getSource(), envelope.getTransportProps().get("connectionId"));
        totalFailed.getAndIncrement();
        if (type == null) {
            unknownCount.getAndIncrement();
        } else {
            switch (type) {
                case BAD_ENVELOPE:
                    badEnvelopeCount.getAndIncrement();
                    break;
                case INVALID_SOURCE:
                    invalidSourceCount.getAndIncrement();
                    break;
                case NOT_DELIVARABLE:
                    notDelivarableCount.getAndIncrement();
                    break;
                case NOT_RESOLVABLE:
                    notResolvableCount.getAndIncrement();
                    break;
                case NOT_ROUTABLE:
                    notRoutableCount.getAndIncrement();
                    break;
                case INVALID_NET_PAYLOAD:
                    invalidNetPayloadCount.getAndIncrement();
                    break;
                case IMPROPER_CLASSIFICATION:
                    improperClassificationCount.getAndIncrement();
                    break;
                case NOT_AUTHORIZED:
                    notAuthorizedCount.getAndIncrement();
                    break;
                case UNKNOWN:
                default:
                    unknownCount.getAndIncrement();
                    break;
            }
        }

        if (envelope != null && type != null) {
            if (failedXMLLogger.isTraceEnabled()) {
                failedXMLLogger.trace("{} | {}| {} | {} | {}", envelope.getInternalMessageId(),
                        envelope.getTrackingId(), type, reason, envelope.getOrigXml());
            }
            if (envelope.getSource() != null && !"UNKNOWN".equals(envelope.getSource()) && ServiceLocator.getService(
                    RoutingURI.build(envelope.getSource(), log)) == null) {
                String key = null;
                if (DeadLetterTypeEnum.BAD_ENVELOPE == type || DeadLetterTypeEnum.INVALID_SOURCE == type || DeadLetterTypeEnum.UNKNOWN == type || dest == null) {
                    key = envelope.getSource() + "_" + type;
                } else {
                    key = envelope.getSource() + "_" + (dest != null ? dest.toString() : "") + "_" + type;
                }
                if (cache.getIfPresent(key) == null) {
                    cache.put(key, Boolean.TRUE);
                    sendFailureNotification(envelope, reason);
                }
            }
        }
    }

    public long getTotalFailed() {
        return totalFailed.get();
    }

    public long getBadEnvelopeCount() {
        return badEnvelopeCount.get();
    }

    public long getInvalidSourceCount() {
        return invalidSourceCount.get();
    }

    public long getNotDelivarableCount() {
        return notDelivarableCount.get();
    }

    public long getNotResolvableCount() {
        return notResolvableCount.get();
    }

    public long getNotRoutableCount() {
        return notRoutableCount.get();
    }

    public long getUnknownFailedCount() {
        return unknownCount.get();
    }

    public long getInvalidNetPayloadCount() {
        return invalidNetPayloadCount.get();
    }

    public long getImproperClassificationCount() {
        return improperClassificationCount.get();
    }

    public long getNotAuthorizedCount() {
        return notAuthorizedCount.get();
    }

    private void sendFailureNotification(Envelope envelope, String reason) {
        FailureNotification notification = new FailureNotification();

        notification.getError().add(reason);
        notification.setMessageId(envelope.getInternalMessageId());
        if (envelope.getTrackingId() != null && !envelope.getTrackingId().isEmpty()) {
            notification.setTrackingId(envelope.getTrackingId());
        }
        SILKWAVE jicd = new SILKWAVE();
        // set security to the level of the message that caused the failure
        SILKWAVEMessageManipulator.buildMessage(jicd,
                SecurityManipulator.mergeSecuritySettings(envelope.getSecurity(), NetworkManager.NETWORK_MIN_CLASS),
                ServiceLocator.NETWORK_MANAGER.toString(), log);

        jicd.setPayload(new MessagePayloadType());
        jicd.getDestination().add(envelope.getSource());
        if (envelope.getMessageCorrelationId() != null && !envelope.getMessageCorrelationId().isEmpty()) {
            jicd.setMessageCorrelationId(envelope.getMessageCorrelationId());
        }

        jicd.setPayload(XMLUtil.buildMessagePayload(notification, "net", log));

        Envelope responseEnv = null;
        try {
            responseEnv = new Envelope(jicd);
            if (responseEnv.isValid()) {
                networkManager.sendOutgoing(responseEnv);
            } else {
                log.warn("Unable to send FailureNotification to {}", envelope.getSource(),
                        responseEnv.getInvalidReason());
            }
        } catch (TransportException e) {
            log.warn("Unable to send FailureNotification to {}", envelope.getSource(), e);
        }

    }

    public void resetCounts() {
        totalFailed.set(0);
        badEnvelopeCount.set(0);
        invalidSourceCount.set(0);
        notDelivarableCount.set(0);
        notResolvableCount.set(0);
        notRoutableCount.set(0);
        unknownCount.set(0);
        invalidNetPayloadCount.set(0);
        improperClassificationCount.set(0);
        notAuthorizedCount.set(0);
    }
}
