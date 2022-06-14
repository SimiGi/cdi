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

package com.vaadin.cdi;

import javax.enterprise.inject.spi.Extension;

import com.vaadin.cdi.access.AccessControl;
import com.vaadin.cdi.access.JaasAccessControl;
import com.vaadin.cdi.internal.AnnotationUtil;
import com.vaadin.cdi.internal.ContextDeployer;
import com.vaadin.cdi.internal.ContextWrapper;
import com.vaadin.cdi.internal.Conventions;
import com.vaadin.cdi.internal.Counter;
import com.vaadin.cdi.internal.CounterFilter;
import com.vaadin.cdi.internal.InconsistentDeploymentException;
import com.vaadin.cdi.internal.UIContextualStorageManager;
import com.vaadin.cdi.internal.UIScopedContext;
import com.vaadin.cdi.internal.VaadinContextualStorage;
import com.vaadin.cdi.internal.VaadinSessionScopedContext;
import com.vaadin.cdi.internal.ViewContextStrategies;
import com.vaadin.cdi.internal.ViewContextStrategyProvider;
import com.vaadin.cdi.internal.ViewContextualStorageManager;
import com.vaadin.cdi.internal.ViewScopedContext;
import com.vaadin.cdi.server.VaadinCDIServlet;
import com.vaadin.cdi.server.VaadinCDIServletService;
import com.vaadin.cdi.viewcontextstrategy.ViewContextByName;
import com.vaadin.cdi.viewcontextstrategy.ViewContextByNameAndParameters;
import com.vaadin.cdi.viewcontextstrategy.ViewContextByNavigation;
import com.vaadin.cdi.viewcontextstrategy.ViewContextStrategy;
import com.vaadin.cdi.viewcontextstrategy.ViewContextStrategyQualifier;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 */
public class ArchiveProvider {

    public final static Class FRAMEWORK_CLASSES[] = new Class[] {
            AccessControl.class, CDIUIProvider.class, CDIViewProvider.class, CDINavigator.class,
            ContextDeployer.class, JaasAccessControl.class,
            UIScopedContext.class, UIContextualStorageManager.class,
            ViewScopedContext.class, ViewContextualStorageManager.class,
            ViewContextStrategy.class, AfterViewChange.class, ViewContextStrategyProvider.class,
            ViewContextStrategyQualifier.class, ViewContextByNavigation.class, ViewContextByName.class,
            ViewContextByNameAndParameters.class, ViewContextStrategies.class,
            CDIView.class, CDIUI.class,
            VaadinCDIServlet.class,
            VaadinCDIServletService.class,
            CDIUIProvider.DetachListenerImpl.class,
            Conventions.class,
            InconsistentDeploymentException.class, AnnotationUtil.class,
            VaadinContextualStorage.class, ContextWrapper.class,
            URLMapping.class,
            UIScoped.class, ViewScoped.class, NormalUIScoped.class, NormalViewScoped.class,
            VaadinSessionScoped.class, VaadinSessionScopedContext.class,
            CounterFilter.class, Counter.class};
    public static WebArchive createWebArchive(String warName, Class... classes) {
        return createWebArchive(warName, true, classes);
    }

    public static WebArchive createWebArchive(String warName,
            boolean emptyBeansXml, Class... classes) {
        WebArchive archive = base(warName, emptyBeansXml);
        archive.addClasses(classes);
        System.out.println(archive.toString(true));
        return archive;
    }

    static WebArchive base(String warName, boolean emptyBeansXml) {
        PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile(
                "pom.xml");
        // these version numbers should match the POM files
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, warName + ".war")
                .addClasses(FRAMEWORK_CLASSES)
                .addAsLibraries(
                        pom.resolve("com.vaadin:vaadin-server")
                                .withTransitivity().asFile())
                .addAsLibraries(
                        pom.resolve("com.vaadin:vaadin-client-compiled")
                                .withTransitivity().asFile())
                .addAsLibraries(
                        pom.resolve("com.vaadin:vaadin-themes")
                                .withTransitivity().asFile())
                .addAsLibraries(
                        pom.resolve(
                                "org.apache.deltaspike.core:deltaspike-core-impl")
                                .withTransitivity().asFile())
                .addAsServiceProvider(Extension.class);
        if (emptyBeansXml) {
            archive = archive.addAsWebInfResource(EmptyAsset.INSTANCE,
                    ArchivePaths.create("beans.xml"));
        }
        return archive;
    }

}
