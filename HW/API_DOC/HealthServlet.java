package gov.ic.silkwave.web;

import java.io.IOException;
import java.io.Serial;
import java.util.Properties;

import com.google.common.base.Stopwatch;
import gov.ic.silkwave.common.client.ConnectionManager;
import gov.ic.silkwave.common.utils.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intended for k8s liveness probe
 */
public class HealthServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 3473769827556213840L;
    private static final Properties props = Constants.props;
    private static final Logger log = LoggerFactory.getLogger(HealthServlet.class);
    private static final String url = Constants.props.getProperty("service.webserver.client.url",
            Constants.props.getProperty("client.url", "vm://localhost?jms.alwaysSyncSend=false"));
    private static final ConnectionManager cm = new ConnectionManager(
            Constants.props.getProperty("client.user", "client"), Constants.props.getProperty("client.pass", "manager"),
            url, log);
    private static final boolean securityEnabled = !"off"
            .equals(Constants.props.getProperty("service.security.state", "off"));
    private static final int tmoMs = Integer.parseInt(
            props.getProperty("service.webserver.health.conn.timeoutMs", "3000"));
    private static final boolean testAppgeo = Boolean.parseBoolean(
            props.getProperty("service.webserver.health.appgeo.enabled", "true"));
    private static final boolean testFabric = Boolean.parseBoolean(
            props.getProperty("service.webserver.health.fabric.enabled", "false"));

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        boolean healthy = isHealthy();
        if (healthy) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("good");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("bad");
        }
    }

    private static synchronized boolean isHealthy() {
        Stopwatch sw = Stopwatch.createStarted();
        boolean healthy = false;
        try {
            log.debug("Begin health check on {}", url);
            if (cm.connect("HealthTask", null, false)) {
                healthy = cm.ping("localns:ww", false, tmoMs);
                log.debug("WW healthy = {}", healthy);
                if (securityEnabled && healthy) {
                    healthy = cm.ping("localns:silkwave.security", false, tmoMs);
                    log.debug("SECURITY healthy = {}", healthy);
                }
                if (testAppgeo && healthy) {
                    healthy = cm.ping("localns:oio.appgeo:" + cm.getHubId() + ".status", false, tmoMs) && cm.ping(
                            "localns:oio.appgeo:" + cm.getHubId() + ".topic.status", false, tmoMs) && cm.ping(
                            "localns:oio.appgeo:" + cm.getHubId() + ".command", false, tmoMs) && cm.ping(
                            "localns:oio.appgeo:" + cm.getHubId() + ".registration", false, tmoMs);
                    log.debug("APPGEO healthy = {}", healthy);
                }
                if (testFabric && healthy) {
                    healthy = cm.ping("localns:isrfabric:status", false, tmoMs);
                    log.debug("FABRIC healthy = {}", healthy);
                }
            } else {
                log.error("Unable to connect for health check");
            }
        } catch (Throwable e) {
            log.error("Exception", e);
            healthy = false;
        } finally {
            if (cm != null && cm.isConnected()) {
                try {
                    cm.disconnect();
                } catch (Throwable e) {
                    log.error("Exception", e);
                }
            }
            log.debug("HUB healthy = {}", healthy);
            log.debug("Completed health check in {}", sw.stop());
        }
        return healthy;
    }

}
