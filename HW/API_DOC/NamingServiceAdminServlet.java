package gov.ic.silkwave.web;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import gov.ic.silkwave.common.RoutingURI;
import gov.ic.silkwave.nameserver.NameServer;
import gov.ic.silkwave.nameserver.NameServerLocator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nameserver.internal.NameServerPullResponse;
import nameserver.internal.NetId;
import nameserver.internal.RouteEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingServiceAdminServlet extends HttpServlet {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 2725678617535448471L;
    private static final Logger log = LoggerFactory.getLogger(NamingServiceAdminServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        try {
            String action = request.getParameter("action");
            if ("nameservers".equals(action)) {
                StringBuilder builder = new StringBuilder();
                response.setContentType("application/json");
                builder.append("{\"nameservers\":[");
                List<String> domains = NameServerLocator.getNameServerDomains();
                String prefix = "";
                for (String domain : domains) {
                    builder.append(prefix);
                    prefix = ",";
                    builder.append("\"");
                    builder.append(domain);
                    builder.append("\"");
                }
                builder.append("]}\n");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(builder);
            } else if ("query".equals(action)) {
                String rowsString = request.getParameter("rows");
                String pageString = request.getParameter("page");

                String groupOp = null;
                JsonArray rules = null;
                String filters = request.getParameter("filters");
                if (filters != null && !filters.isEmpty()) {
                    JsonElement jelement = new JsonParser().parse(filters);
                    JsonObject jobject = jelement.getAsJsonObject();
                    JsonPrimitive prim = jobject.getAsJsonPrimitive("groupOp");
                    groupOp = prim.getAsString();
                    rules = jobject.getAsJsonArray("rules");
                }

                List<RouteEntry> routes = new ArrayList<>();
                List<String> domains = NameServerLocator.getNameServerDomains();
                for (String domain : domains) {
                    NameServer server = NameServerLocator.getNameServer(domain);
                    NameServerPullResponse pullResponse = server.buildNameServerPullResponse();
                    List<RouteEntry> tempDomains = pullResponse.getRoute();
                    if (groupOp != null && rules != null) {
                        for (RouteEntry entry : tempDomains) {
                            boolean add = false;
                            boolean done = false;
                            for (int I = 0; I < rules.size() && !done; ++I) {
                                JsonObject rule = rules.get(I).getAsJsonObject();
                                JsonPrimitive prim = rule.getAsJsonPrimitive("field");
                                String field = prim.getAsString();
                                prim = rule.getAsJsonPrimitive("op");
                                String op = prim.getAsString();
                                prim = rule.getAsJsonPrimitive("data");
                                String dataString = prim.getAsString();

                                boolean isNumber = "count".equals(field);
                                String value = switch (field) {
                                    case "name" -> entry.getName();
                                    case "type" -> entry.getType().toString();
                                    case "count" -> String.valueOf(entry.getNetId().size());
                                    default -> null;
                                };

                                switch (op) {
                                    case "eq":
                                        add = value.equals(dataString);
                                        break;
                                    case "ne":
                                        add = !value.equals(dataString);
                                        break;
                                    case "bw":
                                        add = value.startsWith(dataString);
                                        break;
                                    case "ew":
                                        add = value.endsWith(dataString);
                                        break;
                                    case "cn":
                                        add = value.contains(dataString);
                                        break;
                                    case "lt":
                                        if (isNumber) {
                                            int intValue = Integer.parseInt(value);
                                            int intData = Integer.parseInt(dataString);
                                            add = intValue < intData;
                                        } else {
                                            add = 0 > value.compareTo(dataString);
                                        }
                                        break;
                                    case "gt":
                                        if (isNumber) {
                                            int intValue = Integer.parseInt(value);
                                            int intData = Integer.parseInt(dataString);
                                            add = intValue > intData;
                                        } else {
                                            add = 0 < value.compareTo(dataString);
                                        }
                                        break;
                                }
                                if (add && "OR".equals(groupOp)) {
                                    done = true;
                                } else if (!add && "AND".equals(groupOp)) {
                                    done = true;
                                }
                            }
                            if (add) {
                                routes.add(entry);
                            }
                        }
                    } else {
                        routes.addAll(tempDomains);
                    }
                }

                String sidx = request.getParameter("sidx");
                String sord = request.getParameter("sord");

                if (sidx != null && !sidx.isEmpty()) {
                    Comparator<RouteEntry> comparator_rows = (o1, o2) -> {
                        int result = 0;
                        switch (sidx) {
                            case "name":
                                result = o1.getName().compareTo(o2.getName());
                                break;
                            case "type":
                                result = o1.getType().compareTo(o2.getType());
                                break;
                            case "count":
                                int i = o2.getNetId().size();
                                int j = o1.getNetId().size();
                                result = Integer.compare(j, i);
                                break;
                        }

                        return result;
                    };

                    if ("desc".equals(sord)) {
                        routes.sort(Collections.reverseOrder(comparator_rows));
                    } else {
                        routes.sort(comparator_rows);
                    }
                }

                int page = 1;
                int rows = 10;
                if (pageString != null) {
                    page = Integer.parseInt(pageString);
                }
                if (rowsString != null) {
                    rows = Integer.parseInt(rowsString);
                }

                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append("{\n");
                resultBuilder.append("\"total\":");
                resultBuilder.append("\"").append((routes.size() + rows - 1) / rows).append("\",\n");
                resultBuilder.append("\"page\":");
                resultBuilder.append("\"").append(page).append("\",\n");
                resultBuilder.append("\"records\":");
                resultBuilder.append("\"").append(routes.size()).append("\",\n");
                resultBuilder.append("\"rows\" : [");
                String prefix = "\n";
                for (int I = (page - 1) * rows; I < routes.size() && I < page * rows; ++I) {
                    RouteEntry entry = routes.get(I);
                    resultBuilder.append(prefix);
                    prefix = ",\n";
                    resultBuilder.append("{");
                    resultBuilder.append("\"id\":\"");
                    resultBuilder.append(I + 1);
                    resultBuilder.append("\", \"cell\":[");
                    resultBuilder.append("\"").append(entry.getName()).append("\",");
                    resultBuilder.append("\"").append(entry.getType()).append("\",");
                    resultBuilder.append("\"").append(entry.getNetId().size()).append("\"]");
                    resultBuilder.append("}");
                }
                resultBuilder.append("\n]\n");
                resultBuilder.append("}\n");
                response.getWriter().println(resultBuilder);

            } else if ("subquery".equals(action)) {
                String name = request.getParameter("name");
                String rowsString = request.getParameter("rows");
                String pageString = request.getParameter("page");

                int page = 1;
                int rows = 10;
                if (pageString != null) {
                    page = Integer.parseInt(pageString);
                }
                if (rowsString != null) {
                    rows = rows;
                }

                List<RouteEntry> routes = new ArrayList<>();
                RoutingURI routeUri = null;
                if (name != null) {
                    routeUri = RoutingURI.build(name, log);
                    if (routeUri != null) {
                        NameServer server = NameServerLocator.getNameServer(routeUri.getDomain());
                        if (server != null) {
                            NameServerPullResponse pullResponse = server.buildNameServerPullResponse();
                            routes.addAll(pullResponse.getRoute());
                        }
                    }
                }

                RouteEntry entry = null;
                for (RouteEntry route : routes) {
                    if (route.getName().equals(name)) {
                        entry = route;
                        break;
                    }
                }

                int size = 0;
                if (entry != null) {
                    size = entry.getNetId().size();
                }

                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append("{\n");
                resultBuilder.append("\"total\":");
                resultBuilder.append("\"").append((size + rows - 1) / rows).append("\",\n");
                resultBuilder.append("\"page\":");
                resultBuilder.append("\"").append(page).append("\",\n");
                resultBuilder.append("\"records\":");
                resultBuilder.append("\"").append(size).append("\",\n");
                resultBuilder.append("\"rows\" : [");
                String prefix = "\n";
                if (entry != null) {
                    List<NetId> netIds = entry.getNetId();
                    for (int I = (page - 1) * rows; I < size && I < page * rows; ++I) {
                        NetId netId = netIds.get(I);
                        resultBuilder.append(prefix);
                        prefix = ",\n";
                        resultBuilder.append("{");
                        resultBuilder.append("\"id\":\"");
                        resultBuilder.append(I + 1);
                        resultBuilder.append("\", \"cell\":[");
                        resultBuilder.append("\"").append(netId.getId()).append("\",");
                        resultBuilder.append("\"").append(netId.getOwner()).append("\",");
                        resultBuilder.append("\"").append(netId.getTimestamp().toXMLFormat()).append("\"]");
                        resultBuilder.append("}");
                    }
                }
                resultBuilder.append("\n]\n");
                resultBuilder.append("}\n");
                response.getWriter().println(resultBuilder);

            }

        } catch (Throwable ex) {
            log.warn("Exception thrown while handling request", ex);
        }

    }
}
