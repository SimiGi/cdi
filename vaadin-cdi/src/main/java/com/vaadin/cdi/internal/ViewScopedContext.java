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

import com.vaadin.cdi.ViewScoped;
import com.vaadin.ui.UI;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * ViewScopedContext is the context for @ViewScoped beans.
 */
public class ViewScopedContext extends AbstractVaadinContext{

    private ViewContextualStorageManager contextualStorageManager;

    public ViewScopedContext() {

    }

    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        return getViewContextualStorageManager().getContextualStorage(createIfNotExist);
    }

    public ViewContextualStorageManager getViewContextualStorageManager() {
        if(contextualStorageManager == null) {
            Set<Bean<?>> beans = getBeans();
            Bean<?> bean = getBeanManager().resolve(beans);
            CreationalContext<?> creationalContext = getBeanManager().createCreationalContext(bean);

            contextualStorageManager = (ViewContextualStorageManager) getBeanManager().getReference(bean, ViewContextualStorageManager.class, creationalContext);
        }
        return contextualStorageManager;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ViewScoped.class;
    }

    @Override
    public void destroy() {
        ContextualStorage storage = getViewContextualStorageManager().getContextualStorage(false);
        if (storage != null) {
            AbstractContext.destroyAllActive(storage);
        }
    }

    @Override
    public boolean isActive() {
        return UI.getCurrent() != null
                && getViewContextualStorageManager() != null
                && getViewContextualStorageManager().isActive();
    }

    @Override
    protected Class<?> getBeanType() {
        return ViewContextualStorageManager.class;
    }
}
