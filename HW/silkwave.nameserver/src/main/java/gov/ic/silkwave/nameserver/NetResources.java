package gov.ic.silkwave.nameserver;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NetResources {

    private final String netId;
    private long lastSeen;
    private final ConcurrentMap<String, ResourceRoute> resourceMap;

    public NetResources(String netId) {
        this.netId = netId;
        resourceMap = new ConcurrentHashMap<>();
        lastSeen = System.currentTimeMillis();
    }

    public ConcurrentMap<String, ResourceRoute> getResourceMap() {
        return resourceMap;
    }

    public String getNetId() {
        return netId;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public Set<String> getResourceRouteIds() {
        return resourceMap.keySet();
    }

    public void addResourceRoute(ResourceRoute rr) {
        resourceMap.put(rr.getResId(), rr);
    }

    public int getResourceRouteCount() {
        return resourceMap.size();
    }

    public void removeResourceRoute(String resId) {
        resourceMap.remove(resId);
    }

    public ResourceRoute getResourceRoute(String resId) {
        return resourceMap.get(resId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NetResources netId=").append(netId).append(", lastSeen=").append(lastSeen).append("\n");
        for (String key : resourceMap.keySet()) {
            ResourceRoute rr = resourceMap.get(key);
            builder.append("\t").append(rr.toString()).append("\n");
        }
        return builder.toString();
    }

}
