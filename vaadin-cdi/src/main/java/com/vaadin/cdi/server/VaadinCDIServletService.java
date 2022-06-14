/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.cdi.server;

import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import com.vaadin.cdi.CDIUIProvider;
import com.vaadin.cdi.VaadinSessionScoped;
import com.vaadin.cdi.internal.VaadinContextUtils;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import io.quarkus.arc.impl.BeanManagerProvider;

/**
 * Servlet service implementation for Vaadin CDI.
 * 
 * This class automatically initializes CDIUIProvider and provides the CDI
 * add-on events about session and request processing. For overriding this
 * class, see VaadinCDIServlet.
 */
public class VaadinCDIServletService extends VaadinServletService {

    private final CDIUIProvider cdiuiProvider;

    BeanManager beanManager;

    VaadinContextUtils vaadinContextUtils = CDI.current().select(VaadinContextUtils.class).get();

    protected final class SessionListenerImpl implements SessionInitListener,
            SessionDestroyListener {
        @Override
        public void sessionInit(SessionInitEvent event) {
            getLogger().fine("Session init");
            event.getSession().addUIProvider(cdiuiProvider);
        }

        @Override
        public void sessionDestroy(SessionDestroyEvent event) {
            if (guessContextIsUndeployed()) {
                // Happens on tomcat when it expires sessions upon undeploy.
                // beanManager.getPassivationCapableBean returns null for passivation id,
                // so we would get an NPE from AbstractContext.destroyAllActive
                getLogger().warning("VaadinSessionScoped context does not exist. " +
                                "Maybe application is undeployed." +
                                " Can't destroy VaadinSessionScopedContext.");
                return;
            }
            getLogger().fine("VaadinSessionScopedContext destroy");
            vaadinContextUtils.destroySessionScopedContext(event.getSession());
        }

    }

    public VaadinCDIServletService(VaadinServlet servlet,
            DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        super(servlet, deploymentConfiguration);

        beanManager = new BeanManagerProvider<>().get(null);
        Set<Bean<?>> beans = beanManager.getBeans(CDIUIProvider.class);
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        cdiuiProvider = (CDIUIProvider) beanManager.getReference(bean, CDIUIProvider.class, creationalContext);
        SessionListenerImpl sessionListener = new SessionListenerImpl();
        addSessionInitListener(sessionListener);
        addSessionDestroyListener(sessionListener);
    }

    private boolean guessContextIsUndeployed() {
        // Given there is a current VaadinSession, we should have an active context,
        // except we get here after the application is undeployed.
        return (VaadinSession.getCurrent() != null
                && !vaadinContextUtils.isContextActive(VaadinSessionScoped.class));
    }

    private static Logger getLogger() {
        return Logger.getLogger(VaadinCDIServletService.class
                .getCanonicalName());
    }

}
