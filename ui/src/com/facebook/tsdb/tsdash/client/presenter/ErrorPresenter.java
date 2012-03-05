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

import com.facebook.tsdb.tsdash.client.event.ErrorEvent;
import com.facebook.tsdb.tsdash.client.event.ErrorEventHandler;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.facebook.tsdb.tsdash.client.service.ServiceException;
import com.facebook.tsdb.tsdash.client.service.json.JSONParseException;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class ErrorPresenter implements Presenter {

    public interface ErrorWidget {
        void setVisible(boolean visible);

        HasText title();

        HasHTML details();

        void showDetails();

        HasClickHandlers detailsButton();
    }

    private final HandlerManager eventBus;
    private final ErrorWidget widget;

    public ErrorPresenter(HandlerManager eventBus, ErrorWidget widget) {
        this.eventBus = eventBus;
        this.widget = widget;
        listenErrorEvent();
        bindWidget();
    }

    private void bindWidget() {
        widget.detailsButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                widget.showDetails();
            }
        });
    }

    private void listenErrorEvent() {
        eventBus.addHandler(ErrorEvent.TYPE, new ErrorEventHandler() {
            @Override
            public void onError(ErrorEvent event) {
                widget.setVisible(true);
                Throwable error = event.getCause();
                widget.title().setText(error.toString());
                String details = null;
                // figure out the details
                try {
                    throw error;
                } catch (JSONParseException e) {
                    details = e.getIncorrectJSONText();
                } catch (ServiceException e) {
                    details = e.getServerStackTrace();
                } catch (Throwable e) {
                    for (StackTraceElement stack : e.getStackTrace()) {
                        details += stack.toString() + '\n';
                    }
                }
                widget.details().setHTML("<pre>" + details + "</pre>");
            }
        });
    }

    @Override
    public void go(HasWidgets container, ApplicationState appState) {
        container.add((Widget) widget);
    }

}
