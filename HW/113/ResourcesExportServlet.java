package gov.ic.silkwave.web;

import com.google.gson.Gson;
import gov.ic.silkwave.nameserver.NameServer;
import gov.ic.silkwave.nameserver.NameServerLocator;
import gov.ic.silkwave.nameserver.NetResources;
import gov.ic.silkwave.nameserver.ResourceRoute;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


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
        result.put("endpoints", List.of("/resources", "/netResources", "/nameServerResources", "/all"));
        writeJson(resp, result);
    }

    // GET /resources-export/resources?domain=...
    private void handleResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, ResourceRoute> data = ns.getResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/netResources?domain=...
    private void handleNetResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, NetResources> data = ns.getNetToResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("netToResources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/nameServerResources?domain=...
    private void handleNameServerResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, NetResources> data = ns.getNameServersToResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nameServersToResources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/all?domain=...
    private void handleAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resources", ns.getResources());
        result.put("netToResources", ns.getNetToResources());
        result.put("nameServersToResources", ns.getNameServersToResources());
        result.put("exportTimestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    /**
     * Resolves the NameServer from the optional ?domain= param.
     * Falls back to the first available domain if not specified.
     * Writes a 404 and returns null if nothing is found.
     */
    private NameServer resolveNameServer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String domain = req.getParameter("domain");

        if (domain != null && !domain.isBlank()) {
            NameServer ns = NameServerLocator.getNameServer(domain);
            if (ns == null) {
                resp.setStatus(404);
                writeError(resp, "NameServer not found for domain: " + domain, 404);
                return null;
            }
            return ns;
        }

        // No domain specified — use first available
        List<String> domains = NameServerLocator.getNameServerDomains();
        if (domains.isEmpty()) {
            resp.setStatus(404);
            writeError(resp, "No nameservers available", 404);
            return null;
        }

        return NameServerLocator.getNameServer(domains.get(0));
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        resp.getWriter().write(gson.toJson(obj));
    }

    private void writeError(HttpServletResponse resp, String message, int status) throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        error.put("status", status);
        error.put("timestamp", System.currentTimeMillis());
        resp.getWriter().write(gson.toJson(error));
    }
}
