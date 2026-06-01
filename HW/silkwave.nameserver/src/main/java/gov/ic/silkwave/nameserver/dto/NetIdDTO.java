/*
 * The overall classification of this file is: UNCLASSIFIED//FOUO
 */
package gov.ic.silkwave.nameserver.dto;

public class NetIdDTO {
    private String id;
    private String ownerId;
    private long creationTime;

    public NetIdDTO() {
    }

    public NetIdDTO(String id, String ownerId, long creationTime) {
        this.id = id;
        this.ownerId = ownerId;
        this.creationTime = creationTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
