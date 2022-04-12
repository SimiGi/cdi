/*
 * Copyright 2000-2013 Vaadin Ltd.
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
package com.vaadin.cdi.internal;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import com.vaadin.cdi.NormalUIScoped;
import com.vaadin.cdi.UIScoped;
import com.vaadin.server.VaadinSession;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * UIScopedContext is the context for @UIScoped beans.
 */
public class UIScopedContext extends AbstractVaadinContext {

    private UIContextualStorageManager contextualStorageManager;

    public UIScopedContext() {
    }

    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        return getUIContextualStorageManager().getContextualStorage(createIfNotExist);
    }

    public UIContextualStorageManager getUIContextualStorageManager() {
        if(contextualStorageManager == null) {

        Set<Bean<?>> beans = getBeans();
        Bean<?> bean = getBeanManager().resolve(beans);
        CreationalContext<?> creationalContext = getBeanManager().createCreationalContext(bean);

        contextualStorageManager = (UIContextualStorageManager) getBeanManager().getReference(bean, UIContextualStorageManager.class, creationalContext);
        }
        return contextualStorageManager;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return NormalUIScoped.class;
    }

    @Override
    public boolean isActive() {
        return VaadinSession.getCurrent() != null
                && getUIContextualStorageManager() != null
                && getUIContextualStorageManager().isActive();
    }

    @Override
    protected Annotation[] getAnnotations() {
        Annotation[] annotations = {new AnnotationLiteral<UIScoped>() {}, new AnnotationLiteral<NormalUIScoped>() {}};
        return annotations;
    }

    @Override
    protected Class<?> getBeanType() {
        return UIContextualStorageManager.class;
    }

    @Override
    public void destroy() {
        ContextualStorage storage = getUIContextualStorageManager().getContextualStorage(false);
        if (storage != null) {
            AbstractContext.destroyAllActive(storage);
        }
    }
}
