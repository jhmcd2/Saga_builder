package gov.ic.silkwave;

import gov.ic.silkwave.common.ServiceException;

public interface Service {
    void startup() throws ServiceException;

    void shutdown();
}
