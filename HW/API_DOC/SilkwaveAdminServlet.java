package gov.ic.silkwave.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serial;
import java.lang.management.ManagementFactory;
import java.security.cert.X509Certificate;
import javax.naming.InvalidNameException;

import gov.ic.silkwave.DeadLetterManager;
import gov.ic.silkwave.NetworkManager;
import gov.ic.silkwave.ServiceLocator;
import gov.ic.silkwave.common.certs.CertUtils;
import gov.ic.silkwave.common.messages.builders.SecurityManipulator;
import gov.ic.silkwave.common.messaging.Envelope;
import gov.ic.silkwave.common.messaging.MessageValidationException;
import gov.ic.silkwave.common.utils.Constants;
import gov.ic.silkwave.common.xml.DateFromXMLAdapter;
import gov.ic.silkwave.registration.LocalRegistry;
import gov.ic.silkwave.routing.RoutingService;
import gov.ic.silkwave.transport.destination.DestinationInfo;
import gov.ic.silkwave.transport.destination.DestinationManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import silkwave.markings.Security;

public class SilkwaveAdminServlet extends HttpServlet {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -7669511779229331113L;

    private static final Logger log = LoggerFactory.getLogger(SilkwaveAdminServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        response.setContentType("application/json");
        if (action == null || "load".equals(action)) {

            Security maxSecurity = SecurityManipulator.buildHubHigh(log);
            Security minSecurity = SecurityManipulator.buildHubMinimum(log);

            long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
            long s = (System.currentTimeMillis() - jvmStartTime) / 1000;
            String uptime = String.format("%d day(s) %02d:%02d:%02d", s / 86400, (s % 86400) / 3600, (s % 3600) / 60,
                    (s % 60));
            DestinationManager destMgr = ServiceLocator.getDestinationManager();

            NetworkManager nm = ServiceLocator.getNetworkManager();
            DeadLetterManager dl = DeadLetterManager.getInstance();
            LocalRegistry registry = ServiceLocator.getLocalRegistry();
            RoutingService routing = ServiceLocator.getRoutingService();
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("{\n");
            Object nameObj = null;
            if (request.getUserPrincipal() != null) {
                nameObj = request.getUserPrincipal().getName();
            } else if (request.getAttribute("jakarta.servlet.request.X509Certificate") != null) {
                X509Certificate[] certs = (X509Certificate[]) request.getAttribute(
                        "jakarta.servlet.request.X509Certificate");
                try {
                    nameObj = CertUtils.getCN(certs[0]);
                } catch (InvalidNameException e) {
                    log.warn("Unable to get cn from Certificate.", e);
                }
            }
            if (nameObj != null) {
                resultBuilder.append("\"loggedName\": \"").append(nameObj).append("\",\n");
            }
            resultBuilder.append("\"currenttime\": \"")
                    .append(DateFromXMLAdapter.convert(System.currentTimeMillis()).toXMLFormat()).append("\",\n");
            resultBuilder.append("\"starttime\": \"").append(DateFromXMLAdapter.convert(jvmStartTime).toXMLFormat())
                    .append("\",\n");
            resultBuilder.append("\"uptime\": \"").append(uptime).append("\",\n");
            resultBuilder.append("\"messages\": \"").append(nm.getMessageCount()).append("\",\n");
            DestinationInfo destInfo = destMgr.getDestinationInfo("queue://SILKWAVE.Incoming");
            long total = destInfo.getSize();
            resultBuilder.append("\"incomingtotal\": \"").append(destInfo.getDequeued()).append("\",\n");
            resultBuilder.append("\"incomingsize\": \"").append(destInfo.getSize()).append("\",\n");
            resultBuilder.append("\"incomingmin\": \"").append(destInfo.getMinMessageSize()).append("\",\n");
            resultBuilder.append("\"incomingmax\": \"").append(destInfo.getMaxMessageSize()).append("\",\n");
            resultBuilder.append("\"incomingavg\": \"").append(destInfo.getAverageMessageSize()).append("\",\n");
            destInfo = destMgr.getDestinationInfo("queue://SILKWAVE.Heartbeat");
            total += destInfo.getSize();
            resultBuilder.append("\"heartbeattotal\": \"").append(destInfo.getDequeued()).append("\",\n");
            resultBuilder.append("\"heartbeatsize\": \"").append(destInfo.getSize()).append("\",\n");
            resultBuilder.append("\"heartbeatmin\": \"").append(destInfo.getMinMessageSize()).append("\",\n");
            resultBuilder.append("\"heartbeatmax\": \"").append(destInfo.getMaxMessageSize()).append("\",\n");
            resultBuilder.append("\"heartbeatavg\": \"").append(destInfo.getAverageMessageSize()).append("\",\n");
            destInfo = destMgr.getDestinationInfo("queue://SILKWAVE.Core");
            total += destInfo.getSize();
            resultBuilder.append("\"coretotal\": \"").append(destInfo.getDequeued()).append("\",\n");
            resultBuilder.append("\"coresize\": \"").append(destInfo.getSize()).append("\",\n");
            resultBuilder.append("\"coremin\": \"").append(destInfo.getMinMessageSize()).append("\",\n");
            resultBuilder.append("\"coremax\": \"").append(destInfo.getMaxMessageSize()).append("\",\n");
            resultBuilder.append("\"coreavg\": \"").append(destInfo.getAverageMessageSize()).append("\",\n");
            destInfo = destMgr.getDestinationInfo("queue://SILKWAVE.Authentication");
            total += destInfo.getSize();
            resultBuilder.append("\"authenticationtotal\": \"").append(destInfo.getDequeued()).append("\",\n");
            resultBuilder.append("\"authenticationsize\": \"").append(destInfo.getSize()).append("\",\n");
            resultBuilder.append("\"authenticationmin\": \"").append(destInfo.getMinMessageSize()).append("\",\n");
            resultBuilder.append("\"authenticationmax\": \"").append(destInfo.getMaxMessageSize()).append("\",\n");
            resultBuilder.append("\"authenticationavg\": \"").append(destInfo.getAverageMessageSize()).append("\",\n");
            resultBuilder.append("\"summaryqueued\": \"").append(total).append("\",\n");
            resultBuilder.append("\"failedmessages\": \"").append(dl.getTotalFailed()).append("\",\n");
            resultBuilder.append("\"badenvelopemessages\": \"").append(dl.getBadEnvelopeCount()).append("\",\n");
            resultBuilder.append("\"invalidsourcemessages\": \"").append(dl.getInvalidSourceCount()).append("\",\n");
            resultBuilder.append("\"notdelivarablemessages\": \"").append(dl.getNotDelivarableCount()).append("\",\n");
            resultBuilder.append("\"notresolvablemessages\": \"").append(dl.getNotResolvableCount()).append("\",\n");
            resultBuilder.append("\"notroutablemessages\": \"").append(dl.getNotRoutableCount()).append("\",\n");
            resultBuilder.append("\"unknownfailedmessages\": \"").append(dl.getUnknownFailedCount()).append("\",\n");
            resultBuilder.append("\"invalidnetpayloadmessages\": \"").append(dl.getInvalidNetPayloadCount())
                    .append("\",\n");
            resultBuilder.append("\"notauthorizedmessages\": \"").append(dl.getNotAuthorizedCount()).append("\",\n");
            resultBuilder.append("\"improperclassificationmessages\": \"").append(dl.getImproperClassificationCount())
                    .append("\",\n");
            resultBuilder.append("\"connections\": \"").append(registry.getRegistryCount()).append("\",\n");
            resultBuilder.append("\"privconnections\": \"").append(registry.getPrivilegedConnectionCount())
                    .append("\",\n");
            resultBuilder.append("\"neighbors\": \"").append(routing.getNeighborCount()).append("\",\n");
            resultBuilder.append("\"totalhubs\": \"").append(routing.getRouteCount() + 1).append("\",\n");
            resultBuilder.append("\"hubminsecurity\": \"").append(SecurityManipulator.toStringWeb(minSecurity))
                    .append("\",\n");
            resultBuilder.append("\"hubmaxsecurity\": \"").append(SecurityManipulator.toStringWeb(maxSecurity))
                    .append("\"\n");
            resultBuilder.append("}\n");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(resultBuilder);

        } else if ("resetFailed".equals(action)) {
            DeadLetterManager dl = DeadLetterManager.getInstance();
            dl.resetCounts();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{}");
        } else if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("{}");
            }
        } else if ("domain".equals(action)) {
            String resultBuilder = "{\n" + "\"domain\": \"" + Constants.MYDOMAIN + "\"\n" + "}\n";
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(resultBuilder);
        } else if ("validate".equals(action)) {
            response.setContentType("text/plain");
            StringBuilder resultBuilder = new StringBuilder();

            try {

                StringBuffer jb = new StringBuffer();
                String line = null;
                try {
                    BufferedReader reader = request.getReader();
                    while ((line = reader.readLine()) != null)
                        jb.append(line);
                } catch (Exception e) { /* report an error */
                }

                Envelope env = new Envelope(jb.toString());
                env.validate();
                resultBuilder.append("Valid Envelope\n");
                String payload = request.getParameter("payload");
                if ("true".equals(payload)) {
                    try {
                        env.getPayloadObjects();
                        resultBuilder.append("Valid Payload\n");
                    } catch (MessageValidationException e) {
                        resultBuilder.append(e.getMessage().replaceAll("\n", "n").replaceAll("\r", "r")).append("\"\n");
                    }
                }
            } catch (Exception e) {
                resultBuilder.append(e.getMessage().replaceAll("\n", "n").replaceAll("\r", "r")).append("\"\n");
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(resultBuilder);
        }
    }

    /*
     * private HttpSession login(HttpServletRequest request) { HttpSession
     * session = null; boolean ok = false; String name = null; if
     * (request.isSecure()) { Object object =
     * request.getAttribute("javax.servlet.request.X509Certificate"); if (object
     * != null) { X509Certificate[] certs = (X509Certificate[]) object; String
     * dn = certs[0].getSubjectDN().getName(); LdapName ln; try { ln = new
     * LdapName(dn); for (Rdn rdn : ln.getRdns()) { if
     * (rdn.getType().equalsIgnoreCase("CN")) { name = (String) rdn.getValue();
     * break; } } } catch (InvalidNameException e) { // TODO Auto-generated
     * catch block e.printStackTrace(); } ok = true; } } else { String validUser
     * = Constants.props.getProperty("service.webserver.user", "admin"); String
     * validPass = Constants.props.getProperty("service.webserver.pass",
     * "silkwave"); String user = request.getParameter("user"); String pass =
     * request.getParameter("pass"); name = user;
     *
     * if (validUser.equals(user) && validPass.equals(pass)) { ok = true; } }
     *
     * if (ok) { if (session == null) { session = request.getSession(); }
     * session.setAttribute("loggedIn", true); session.setAttribute("name",
     * name); } return session; }
     */
}
