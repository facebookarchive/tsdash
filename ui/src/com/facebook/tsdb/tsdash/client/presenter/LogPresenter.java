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

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.client.event.LogEvent;
import com.facebook.tsdb.tsdash.client.event.LogEventHandler;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class LogPresenter implements Presenter {

    private static final int LIMIT = 20;

    public interface LogWidget {
        HasWidgets container();
    }

    public interface LogEntryWidget {
    }

    private final HandlerManager eventBus;
    private final LogWidget widget;

    private final ArrayList<LogEntryWidget> entries =
        new ArrayList<LogEntryWidget>();

    public LogPresenter(HandlerManager eventBus, LogWidget widget) {
        this.eventBus = eventBus;
        this.widget = widget;
        listenLogEvents();
    }

    private void listenLogEvents() {
        eventBus.addHandler(LogEvent.TYPE, new LogEventHandler() {
            @Override
            public void onLog(LogEvent event) {
                if (entries.size() == LIMIT) {
                    widget.container().remove((Widget) entries.get(0));
                    entries.remove(0);
                }
                LogEntryWidget logEntryWidget =
                    new com.facebook.tsdb.tsdash.client.ui.LogEntryWidget(
                        event.getTitle(), event.getMessage());
                widget.container().add((Widget) logEntryWidget);
                entries.add(logEntryWidget);
            }
        });
    }

    @Override
    public void go(final HasWidgets container,
            final ApplicationState appState) {
        container.clear();
        container.add((Widget) widget);
    }

}
