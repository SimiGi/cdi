package com.vaadin.cdi.internal;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import com.vaadin.cdi.viewcontextstrategy.ViewContextByNameAndParameters;
import com.vaadin.cdi.viewcontextstrategy.ViewContextStrategy;
import com.vaadin.cdi.viewcontextstrategy.ViewContextStrategyQualifier;
import com.vaadin.navigator.View;
import org.apache.deltaspike.core.api.literal.AnyLiteral;

/**
 * Looks up ViewContextStrategy for view classes.
 */
@ApplicationScoped
public class ViewContextStrategyProvider {
    private static AnyLiteral ANY_LITERAL = new AnyLiteral();
    @Inject
    private BeanManager beanManager;

    public ViewContextStrategy lookupStrategy(Class<? extends View> viewClass) {
        Class<? extends Annotation> annotationClass = findStrategyAnnotation(viewClass);
        if (annotationClass == null) {
            annotationClass = ViewContextByNameAndParameters.class;
        }
        final Bean strategyBean = findStrategyBean(annotationClass);
        if (strategyBean == null) {
            throw new IllegalStateException(
                    "No ViewContextStrategy found for " + annotationClass.getCanonicalName());
        }
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(strategyBean);
        return (ViewContextStrategy) beanManager.getReference(strategyBean, ViewContextStrategy.class, creationalContext);
    }

    private Class<? extends Annotation> findStrategyAnnotation(Class<? extends View> viewClass) {
        final Annotation[] annotations = viewClass.getAnnotations();
        for (Annotation annotation : annotations) {
            final Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.getAnnotation(ViewContextStrategyQualifier.class) != null) {
                return annotationType;
            }
        }
        return null;
    }

    private Bean findStrategyBean(Class<? extends Annotation> annotationClass) {
        final Set<Bean<?>> strategyBeans =
                beanManager.getBeans(ViewContextStrategy.class, ANY_LITERAL);
        for (Bean<?> strategyBean : strategyBeans) {
            final Class<?> strategyBeanClass = strategyBean.getBeanClass();
            if (ViewContextStrategy.class.isAssignableFrom(strategyBeanClass)
                    && strategyBeanClass.getAnnotation(annotationClass) != null) {
                return strategyBean;
            }
        }
        return null;
    }
}
