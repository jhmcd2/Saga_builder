package gov.ic.silkwave.web;

import java.io.IOException;
import java.io.Serial;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasportV3SimServlet extends HttpServlet {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -7669511779229331113L;
    private static final Logger log = LoggerFactory.getLogger(CasportV3SimServlet.class);

    private static final String simValidGroup = "testgroup";
    private static final String simValidMember = "CN=Test Client,OU=SILKWAVE,O=JICD 4.2,L=Warrenton,ST=Virginia,C=US";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String context = request.getContextPath();
        String reqUri = request.getRequestURI();

        log.debug("in doGet - context:{} reqUri:{}", context, reqUri);
        // sample debug output:
        //    context:/rest/v3 reqUri:/rest/v3/groups/mygroupa/members/jdoe

        response.setContentType("application/json");

        if (context == null || reqUri == null) {
            String message = "empty context or request uri";
            log.warn(message);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(message);
        } else {
            String[] parts = reqUri.split("/");

            // look for expected length
            if (7 != parts.length) {
                String message = "expected 7 parts to request uri";
                log.warn(message);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println(message);
                // look for expected keywords
            } else if (!"groups".equalsIgnoreCase(parts[3]) || !"members".equalsIgnoreCase(parts[5])) {
                String message = "missing expected keywords";
                log.warn(message);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println(message);
                // test for special group that results in 400 response
            } else if ("invalidgroup".equalsIgnoreCase(parts[4])) {
                String message = "Invalid group name"; // from API doc
                log.warn(message);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println(message);
                // test for special group that results in 404 response
            } else if ("groupnotfound".equalsIgnoreCase(parts[4])) {
                String message = "Group not found"; // from API doc
                log.warn(message);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println(message);
            } else {
                // canned response
                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append("{\n");

                // remove any encoding
                String group = URLDecoder.decode(parts[4], StandardCharsets.UTF_8);
                String member = URLDecoder.decode(parts[6], StandardCharsets.UTF_8);

                log.debug("in doGet - group:{}, member:{}", group, member);

                // not sure if should do case insensitive compare here - for now is case sensitive
                if (group.equals(simValidGroup) && member.equals(simValidMember)) {
                    log.debug("in doGet - returning 'isMember'");
                    resultBuilder.append("\"isMember\": true\n");
                } else {
                    log.debug("in doGet - returning NOT 'isMember'");
                    resultBuilder.append("\"isMember\": false\n");
                }

                resultBuilder.append("}\n");

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(resultBuilder);
            }
        }
    }
}
