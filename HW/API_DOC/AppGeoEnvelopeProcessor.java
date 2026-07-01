/*
 * The overall classification of this file is: UNCLASSIFIED
 */
package gov.ic.silkwave.common.messages.processors;

import gov.ic.silkwave.common.messages.processors.msgdata.DOMMsgData;
import gov.ic.silkwave.common.messages.processors.msgdata.EnvelopeMsgData;
import gov.ic.silkwave.common.messages.processors.msgdata.MsgData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated included for compatibility with 2.4.1.4
 */
@Deprecated
public class AppGeoEnvelopeProcessor extends EnvelopeProcessor {
    private static Logger log = LoggerFactory.getLogger(AppGeoEnvelopeProcessor.class);

    public AppGeoEnvelopeProcessor(String param) throws PluginException {
        super(param);
    }

    @Override
    public MsgData process(MsgData msgData) {
        EnvelopeMsgData envData = null;
        if (msgData instanceof EnvelopeMsgData) {
            envData = (EnvelopeMsgData) msgData;
        } else {
            try {
                envData = new EnvelopeMsgData(msgData);
            } catch (Exception e) {
                log.warn("Could not parse message data", e);
                MsgData failureMsgData = envData;
                if (envData == null) {
                    failureMsgData = new DOMMsgData();
                }
                failureMsgData.setFailureReason("Could not parse message data");
                failureMsgData.setSuccess(false);
                failureMsgData.setCont(false);
                return failureMsgData;
            }
        }

        // Process message
        envData.getEnvelope();


        return envData;
    }

}
/*
 * The overall classification of this file is: UNCLASSIFIED
 */
