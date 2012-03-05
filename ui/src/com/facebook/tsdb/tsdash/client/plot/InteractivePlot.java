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
package com.facebook.tsdb.tsdash.client.plot;

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.client.event.LogEvent;
import com.facebook.tsdb.tsdash.client.model.Metric;
import com.facebook.tsdb.tsdash.client.model.MetricHeader;
import com.facebook.tsdb.tsdash.client.model.TimeRange;
import com.facebook.tsdb.tsdash.client.model.TimeSeriesResponse;
import com.facebook.tsdb.tsdash.client.service.HTTPService;
import com.facebook.tsdb.tsdash.client.service.json.TimeSeriesException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.ReadyHandler;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;

public class InteractivePlot extends Plot {

    private JSONObject timeSeriesJSON = null;
    private static boolean APILoaded = false;

    public InteractivePlot(HandlerManager eventBus, HasWidgets container) {
        super(eventBus, container);
    }

    @Override
    public void render(final Command onRendered) {
        if (!APILoaded) {
            GWT.log("Loading the API...");
            VisualizationUtils.loadVisualizationApi(new Runnable() {
                @Override
                public void run() {
                    APILoaded = true;
                    GWT.log("API loaded");
                    render(onRendered);
                }
            }, AnnotatedTimeLine.PACKAGE);
            return;
        }
        if (timeSeriesJSON == null) {
            GWT.log("cannot render: null data");
            return;
        }
        DataTable dataTable = DataTable.create(timeSeriesJSON
                .getJavaScriptObject());
        Widget w = (Widget) container;
        final AnnotatedTimeLine newChart = new AnnotatedTimeLine(dataTable,
                getChartOptions(), (w.getOffsetWidth() - 10) + "px",
                (w.getOffsetHeight() - 10) + "px");
        newChart.addReadyHandler(new ReadyHandler() {
            @Override
            public void onReady(ReadyEvent event) {
                endRender(newChart);
                onRendered.execute();
            }
        });
        startRender(newChart);
    }

    private static AnnotatedTimeLine.Options getChartOptions() {
        AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
        options.setDateFormat("H:mm:ss");
        options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
        return options;
    }

    @Override
    public void loadAndRender(TimeRange timeRange, ArrayList<Metric> metrics,
            HTTPService service,
            final AsyncCallback<ArrayList<MetricHeader>> callback,
            final Command onRender) {
        service.loadTimeSeries(timeRange, metrics,
                new AsyncCallback<TimeSeriesResponse>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(final TimeSeriesResponse result) {
                        eventBus.fireEvent(new LogEvent("Data Load", ""
                                + result));
                        if (result.rows == 0) {
                            callback.onFailure(new TimeSeriesException(
                                    "no data"));
                            return;
                        }
                        timeSeriesJSON = result.timeSeriesJSON;
                        onRender.execute();
                        render(new Command() {
                            @Override
                            public void execute() {
                                callback.onSuccess(result.metrics);
                            }
                        });
                    }
                });
    }

}
