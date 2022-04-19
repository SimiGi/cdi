package ch.sympany.quarkus.vaadin.extension.deployment;

import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.UIScoped;
import com.vaadin.cdi.VaadinSessionScoped;
import com.vaadin.cdi.ViewScoped;
import com.vaadin.cdi.internal.UIScopedContext;
import com.vaadin.cdi.internal.VaadinSessionScopedContext;
import com.vaadin.cdi.internal.ViewScopedContext;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.ContextRegistrationPhaseBuildItem;
import io.quarkus.arc.deployment.CustomScopeBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import org.jboss.jandex.DotName;

class QuarkusVaadinExtensionProcessor {

    private static final String FEATURE = "quarkus-vaadin-extension";

    // Register custom vaadin-scopes
    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerUIScopedContext(ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
                phase.getContext().configure(UIScoped.class).normal().contextClass(UIScopedContext.class));
    }

    @BuildStep
    CustomScopeBuildItem customUIScope() {
        return new CustomScopeBuildItem(DotName.createSimple(UIScoped.class.getName()));
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerViewScopedContext(ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
                phase.getContext().configure(ViewScoped.class).normal().contextClass(ViewScopedContext.class));
    }

    @BuildStep
    CustomScopeBuildItem customViewScope() {
        return new CustomScopeBuildItem(DotName.createSimple(ViewScoped.class.getName()));
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerVaadinSessionScopedContext(ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
                phase.getContext().configure(VaadinSessionScoped.class).normal().contextClass(VaadinSessionScopedContext.class));
    }

    @BuildStep
    CustomScopeBuildItem customVaadinSessionScope() {
        return new CustomScopeBuildItem(DotName.createSimple(VaadinSessionScoped.class.getName()));
    }

    // Add CDIUI as a BeanDefiningAnnotation, so quarkus picks it up
    @BuildStep
    BeanDefiningAnnotationBuildItem additionalBeanDefiningAnnotationCDIUIScoped() {
        return new BeanDefiningAnnotationBuildItem(DotName.createSimple(CDIUI.class.getName()));
    }
}
