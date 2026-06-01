/*
 * The overall classification of this file is: UNCLASSIFIED//FOUO
 */
package gov.ic.silkwave.nameserver.dto;

import java.util.Map;

public class ResourcesExportDTO {
    private Map<String, ResourceRouteDTO> resources;
    private Map<String, NetResourcesDTO> netToResources;
    private Map<String, NetResourcesDTO> nameServersToResources;
    private long exportTimestamp;

    public ResourcesExportDTO() {
    }

    public ResourcesExportDTO(Map<String, ResourceRouteDTO> resources,
            Map<String, NetResourcesDTO> netToResources,
            Map<String, NetResourcesDTO> nameServersToResources) {
        this.resources = resources;
        this.netToResources = netToResources;
        this.nameServersToResources = nameServersToResources;
        this.exportTimestamp = System.currentTimeMillis();
    }

    public Map<String, ResourceRouteDTO> getResources() {
        return resources;
    }

    public void setResources(Map<String, ResourceRouteDTO> resources) {
        this.resources = resources;
    }

    public Map<String, NetResourcesDTO> getNetToResources() {
        return netToResources;
    }

    public void setNetToResources(Map<String, NetResourcesDTO> netToResources) {
        this.netToResources = netToResources;
    }

    public Map<String, NetResourcesDTO> getNameServersToResources() {
        return nameServersToResources;
    }

    public void setNameServersToResources(Map<String, NetResourcesDTO> nameServersToResources) {
        this.nameServersToResources = nameServersToResources;
    }

    public long getExportTimestamp() {
        return exportTimestamp;
    }

    public void setExportTimestamp(long exportTimestamp) {
        this.exportTimestamp = exportTimestamp;
    }
}
