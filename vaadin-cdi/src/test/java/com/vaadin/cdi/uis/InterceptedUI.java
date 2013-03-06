/*
 * Copyright 2012 Vaadin Ltd.
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

package com.vaadin.cdi.uis;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.vaadin.cdi.CDIUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


@CDIUI
public class InterceptedUI extends UI {

    private final static AtomicInteger COUNTER = new AtomicInteger(0);
    private final static AtomicInteger EVENT_COUNTER = new AtomicInteger(0);

    @Inject
    InterceptedBean interceptedBean;

    @PostConstruct
    public void initialize() {
        COUNTER.incrementAndGet();
    }

    @Override
    protected void init(VaadinRequest request) {
        setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        final Label label = new Label("+InterceptedUI");
        label.setId("label");
        Button changeLabel = new Button("button", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                label.setValue(interceptedBean.fromInterceptorBean());
            }
        });
        changeLabel.setId("button");
        layout.addComponent(label);
        layout.addComponent(changeLabel);
        setContent(layout);
    }

    public void onEventArrival(@Observes String message) {
        this.EVENT_COUNTER.incrementAndGet();
        System.out.println("Message arrived!");
    }

    public static int getNumberOfInstances() {
        return COUNTER.get();
    }

    public static void resetCounter() {
        COUNTER.set(0);
    }

}
