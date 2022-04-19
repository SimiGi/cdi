package com.vaadin.cdi.internal;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;

import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.impl.BeanManagerProvider;
import io.quarkus.arc.impl.LazyValue;
import org.apache.deltaspike.core.util.context.ContextualInstanceInfo;
import org.apache.deltaspike.core.util.context.ContextualStorage;

public abstract class AbstractVaadinContext implements InjectableContext {

    private final LazyValue<BeanManager> beanManagerLazyValue = new LazyValue<>(() -> new BeanManagerProvider<>().get(null));

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (creationalContext == null)
        {
            return get(contextual);
        }

        if (getBeanManager().isPassivatingScope(getScope()))
        {
            if (!(contextual instanceof PassivationCapable))
            {
                throw new IllegalStateException(contextual.toString() +
                        " doesn't implement " + PassivationCapable.class.getName());
            }
        }

        ContextualStorage storage = getContextualStorage(contextual, true);

        Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
        ContextualInstanceInfo<?> contextualInstanceInfo = contextMap.get(storage.getBeanKey(contextual));

        if (contextualInstanceInfo != null)
        {
            @SuppressWarnings("unchecked")
            final T instance =  (T) contextualInstanceInfo.getContextualInstance();

            if (instance != null)
            {
                return instance;
            }
        }

        return storage.createContextualInstance(contextual, creationalContext);
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        ContextualStorage storage = getContextualStorage(contextual, false);
        if (storage == null)
        {
            return null;
        }

        Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
        ContextualInstanceInfo<?> contextualInstanceInfo = contextMap.get(storage.getBeanKey(contextual));
        if (contextualInstanceInfo == null)
        {
            return null;
        }

        return (T) contextualInstanceInfo.getContextualInstance();
    }

    @Override
    public ContextState getState() {
        return new ContextState() {
            @Override
            public Map<InjectableBean<?>, Object> getContextualInstances() {
                return getBeans().stream().collect(Collectors.toMap(bean -> (InjectableBean<?>) bean, bean2 -> getBeanManager().getBeans(bean2.getBeanClass().getTypeName()))) ;
            }
        };
    }

    @Override
    public ContextState getStateIfActive() {
        return InjectableContext.super.getStateIfActive();
    }

    @Override
    public <T> T getIfActive(Contextual<T> contextual, Function<Contextual<T>, CreationalContext<T>> contextualCreationalContextFunction) {
        return InjectableContext.super.getIfActive(contextual, contextualCreationalContextFunction);
    }

    @Override
    public void destroy(ContextState state) {
        InjectableContext.super.destroy(state);
    }

    @Override
    public boolean isNormal() {
        return InjectableContext.super.isNormal();
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        getBeanManager().createCreationalContext(contextual).release();
    }

    protected BeanManager getBeanManager() {
        return beanManagerLazyValue.get();
    }

    protected abstract ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist);

    protected abstract Class<?> getBeanType();

    protected Set<Bean<?>> getBeans() {
        return getBeanManager().getBeans(getBeanType());
    }

}
