package ch.sympany.quarkus.vaadin.extension.test;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import com.vaadin.cdi.UIScoped;
import com.vaadin.cdi.ViewScoped;
import com.vaadin.cdi.internal.UIScopedContext;
import com.vaadin.cdi.internal.ViewScopedContext;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext;
import io.quarkus.test.QuarkusUnitTest;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class QuarkusVaadinExtensionTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(TestUiScopedClass.class));

    // We have to inject this bean, otherwise quarkus would remove the bean again.
    @Inject
    @UIScoped
    TestUiScopedClass testUiScopedClass;

    @Inject
    BeanManager beanManager;

    @Test
    public void confirmCustomAndDefaultContextsAreRegistered() {
        Set<Class<? extends Annotation>> availableScopes = Arc.container().getScopes();

        List<InjectableContext> contexts = new ArrayList<>();
        for (Class<? extends Annotation> scope : availableScopes) {
            InjectableContext context = Arc.container().getContexts(scope).stream().findFirst().orElseThrow();
            contexts.add(context);
        }

        Assertions.assertEquals(7, availableScopes.size());
        Assertions.assertTrue(availableScopes.stream().anyMatch(scope -> scope == UIScoped.class));
        Assertions.assertTrue(availableScopes.stream().anyMatch(scope -> scope == ViewScoped.class));

        Assertions.assertEquals(7, contexts.size());
        Assertions.assertTrue(contexts.stream().anyMatch(context -> context instanceof UIScopedContext));
        Assertions.assertTrue(contexts.stream().anyMatch(context -> context instanceof ViewScopedContext));
    }

    @Test
    void givenTestClassWithCustomAnnotation_whenActiveBeansScanned_thenWeFindExampleBean() {
        Bean<?> testUiScopedBean = beanManager.getBeans(TestUiScopedClass.class, new AnyLiteral())
                .stream()
                .filter(bean -> bean.getBeanClass() == TestUiScopedClass.class).findFirst()
                .get();

        Assertions.assertNotNull(testUiScopedBean);
    }

    // TestClass
    @UIScoped
    public static class TestUiScopedClass implements Serializable {

    }
}
