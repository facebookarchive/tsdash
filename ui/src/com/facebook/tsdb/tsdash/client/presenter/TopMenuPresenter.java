/*
 * Copyright 2011 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.tsdb.tsdash.client.presenter;

import java.util.Map;
import java.util.HashMap;

import com.facebook.tsdb.tsdash.client.event.ViewChangeEvent;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;

public class TopMenuPresenter implements Presenter {

    public interface Widget {
        HasClickHandlers getGraphButton();

        HasClickHandlers getLogButton();

        void setSelected(Object object);

        Object getSelected();
    }

    private final HandlerManager eventBus;
    private Widget widget;
    private final Map<String, HasClickHandlers> viewToButton =
        new HashMap<String, HasClickHandlers>();
    private final Map<HasClickHandlers, String> buttonToView =
        new HashMap<HasClickHandlers, String>();

    public TopMenuPresenter(HandlerManager eventBus, Widget widget) {
        this.eventBus = eventBus;
        bindWidget(widget);
    }

    private void bindWidget(final Widget widget) {
        // set mappings between buttons and view strings
        viewToButton.put(ViewChangeEvent.View.GRAPH.toString(),
                widget.getGraphButton());
        buttonToView.put(widget.getGraphButton(),
                ViewChangeEvent.View.GRAPH.toString());
        viewToButton.put(ViewChangeEvent.View.LOG.toString(),
                widget.getLogButton());
        buttonToView.put(widget.getLogButton(),
                ViewChangeEvent.View.LOG.toString());

        this.widget = widget;
        ClickHandler buttonHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (widget.getSelected() == event.getSource()) {
                    // ignore the event
                    return;
                }
                HasClickHandlers source = (HasClickHandlers) event.getSource();
                ViewChangeEvent viewEvent = new ViewChangeEvent(
                        buttonToView.get(source));
                eventBus.fireEvent(viewEvent);
            }
        };
        widget.getGraphButton().addClickHandler(buttonHandler);
        widget.getLogButton().addClickHandler(buttonHandler);
    }

    public void setSelected(ViewChangeEvent.View view) {
        widget.setSelected(viewToButton.get(view.toString()));
    }

    public void setSelected() {
        widget.setSelected(widget.getGraphButton());
    }

    @Override
    public void go(final HasWidgets container,
            final ApplicationState appState) {
        container.clear();
        container.add((com.google.gwt.user.client.ui.Widget) widget);
        // initialize
        setSelected(appState.view);
    }

}
