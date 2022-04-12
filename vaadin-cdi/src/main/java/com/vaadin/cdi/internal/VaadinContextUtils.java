package com.vaadin.cdi.internal;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import com.vaadin.server.VaadinSession;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * Class is duplicated and modified by injecting the BeanManager,
 * since we cannot override the abstract ContextUtils of deltaspikes as it only contains a private constructor.
 */
@ApplicationScoped
public class VaadinContextUtils {

    @Inject
    BeanManager beanManager;

    public boolean isContextActive(Class<? extends Annotation> scopeAnnotationClass)
    {
        return isContextActive(scopeAnnotationClass, beanManager);
    }

    public boolean isContextActive(Class<? extends Annotation> scopeAnnotationClass, BeanManager beanManager)
    {
        try
        {
            if (beanManager.getContext(scopeAnnotationClass) == null
                    || !beanManager.getContext(scopeAnnotationClass).isActive())
            {
                return false;
            }
        }
        catch (ContextNotActiveException e)
        {
            return false;
        }

        return true;
    }

    public void destroySessionScopedContext(VaadinSession vaadinSession) {
        ContextualStorage storage = (ContextualStorage) vaadinSession.getAttribute(VaadinSessionScopedContext.getAttributeName());
        if (storage != null) {
            AbstractContext.destroyAllActive(storage);
        }
    }
}
