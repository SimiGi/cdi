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
 *
 */

package com.vaadin.cdi.internal;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.util.AnnotationLiteral;

import com.vaadin.cdi.VaadinSessionScoped;
import com.vaadin.server.VaadinSession;
import org.apache.commons.lang.NotImplementedException;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * Context for {@link VaadinSessionScoped}.
 *
 * Stores contextuals in {@link VaadinSession}.
 * Other Vaadin CDI contexts are stored in the corresponding VaadinSessionScoped context.
 *
 * @since 3.0
 */
public class VaadinSessionScopedContext extends AbstractVaadinContext implements Serializable {
    private static final String ATTRIBUTE_NAME = VaadinSessionScopedContext.class.getName();
    public VaadinSessionScopedContext() {

    }

    protected static String getAttributeName() {
        return ATTRIBUTE_NAME;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return VaadinSessionScoped.class;
    }

    @Override
    public boolean isActive() {
        return VaadinSession.getCurrent() != null;
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        VaadinSession session = VaadinSession.getCurrent();
        ContextualStorage storage = findContextualStorage(session);
        if (storage == null && createIfNotExist) {
            storage = new VaadinContextualStorage(getBeanManager());
            session.setAttribute(getAttributeName(), storage);
        }
        return storage;
    }

    @Override
    protected Annotation[] getAnnotations() {
        Annotation[] annotations = {new AnnotationLiteral<VaadinSessionScoped>() {}};
        return annotations;
    }

    @Override
    protected Class<?> getBeanType() {
        throw new NotImplementedException("getBeanType has not been implemented for VaadinSessionScopedContext");
    }

    private ContextualStorage findContextualStorage(VaadinSession session) {
        return (ContextualStorage) session.getAttribute(getAttributeName());
    }

    @Override
    public void destroy() {
        ContextualStorage storage = findContextualStorage(VaadinSession.getCurrent());
        if (storage != null) {
            AbstractContext.destroyAllActive(storage);
        }
    }
}
