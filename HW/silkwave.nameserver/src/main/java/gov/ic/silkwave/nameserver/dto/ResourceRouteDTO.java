/*
 * The overall classification of this file is: UNCLASSIFIED//FOUO
 */
package gov.ic.silkwave.nameserver.dto;

import java.util.Map;

public class ResourceRouteDTO {
    private String resId;
    private String type;
    private Map<String, NetIdDTO> netIds;

    public ResourceRouteDTO() {
    }

    public ResourceRouteDTO(String resId, String type, Map<String, NetIdDTO> netIds) {
        this.resId = resId;
        this.type = type;
        this.netIds = netIds;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, NetIdDTO> getNetIds() {
        return netIds;
    }

    public void setNetIds(Map<String, NetIdDTO> netIds) {
        this.netIds = netIds;
    }
}
