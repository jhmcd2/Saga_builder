package gov.ic.silkwave.web;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletRequest;

public class RestrictionsLoginService extends AbstractLifeCycle implements LoginService {

    private static Logger log = LoggerFactory.getLogger(RestrictionsLoginService.class);

    protected IdentityService identityService = new DefaultIdentityService();
    protected String name;

    @Override
    public IdentityService getIdentityService() {
        return identityService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void logout(UserIdentity arg0) {
        log.info("Logout");

    }

    @Override
    public void setIdentityService(IdentityService service) {
        identityService = service;

    }

    @Override
    public boolean validate(UserIdentity arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UserIdentity login(String username, Object credentials, ServletRequest servletRequest) {
        UserIdentity user = null;

        if (username != null) {
            log.info("Got a username: {} with credentials of type:{}", username,
                    credentials.getClass().getSimpleName());
        }

        return user;
    }

}
