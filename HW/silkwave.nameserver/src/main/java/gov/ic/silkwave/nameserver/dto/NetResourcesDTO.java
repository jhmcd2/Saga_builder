/*
 * The overall classification of this file is: UNCLASSIFIED//FOUO
 */
package gov.ic.silkwave.nameserver.dto;

import java.util.Map;

public class NetResourcesDTO {
    private String netId;
    private long lastSeen;
    private int resourceCount;
    private Map<String, ResourceRouteDTO> resourceMap;

    public NetResourcesDTO() {
    }

    public NetResourcesDTO(String netId, long lastSeen, int resourceCount, Map<String, ResourceRouteDTO> resourceMap) {
        this.netId = netId;
        this.lastSeen = lastSeen;
        this.resourceCount = resourceCount;
        this.resourceMap = resourceMap;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public Map<String, ResourceRouteDTO> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<String, ResourceRouteDTO> resourceMap) {
        this.resourceMap = resourceMap;
    }
}
