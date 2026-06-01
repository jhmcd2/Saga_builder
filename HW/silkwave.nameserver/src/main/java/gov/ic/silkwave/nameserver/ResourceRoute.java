package gov.ic.silkwave.nameserver;

import java.util.HashMap;
import java.util.Map;

import silkwave.net.ResourceType;

public class ResourceRoute {
    private final String resId;
    private final Map<String, NetId> netIds;
    private final ResourceType type;

    public ResourceRoute(String resId, ResourceType type) {
        netIds = new HashMap<>();
        this.resId = resId;
        this.type = type;
    }

    public String getResId() {
        return resId;
    }

    public Map<String, NetId> getNetIds() {
        return netIds;
    }

    public ResourceType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceRoute resId=").append(resId).append(" type=").append(type).append("\n");
        for (String key : netIds.keySet()) {
            NetId netId = netIds.get(key);
            builder.append("\tNetId  id=").append(netId.getId()).append(" owner=").append(netId.getOwnerId())
                    .append(" time=").append(netId.getCreationTime()).append("\n");
        }
        return builder.toString();
    }

}
