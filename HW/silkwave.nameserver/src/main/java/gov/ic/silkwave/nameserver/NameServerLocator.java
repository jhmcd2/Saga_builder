package gov.ic.silkwave.nameserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NameServerLocator {

    private static final ConcurrentMap<String, NameServer> nameservers = new ConcurrentHashMap<>();

    private NameServerLocator() {

    }

    public static synchronized List<String> getNameServerDomains() {
        return new ArrayList<>(nameservers.keySet());
    }

    public static synchronized NameServer getNameServer(String domain) {
        return nameservers.get(domain);
    }

    public static synchronized void putNameServer(String domain, NameServer nameServer) {
        nameservers.put(domain, nameServer);
    }
}
