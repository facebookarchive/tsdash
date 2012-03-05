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
import java.util.HashMap;

import com.facebook.tsdb.tsdash.client.event.ErrorEvent;
import com.facebook.tsdb.tsdash.client.event.MetricEvent;
import com.facebook.tsdb.tsdash.client.event.MetricHeaderEvent;
import com.facebook.tsdb.tsdash.client.event.MetricHeaderEventHandler;
import com.facebook.tsdb.tsdash.client.event.TagEvent;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.facebook.tsdb.tsdash.client.model.Metric;
import com.facebook.tsdb.tsdash.client.model.MetricHeader;
import com.facebook.tsdb.tsdash.client.model.TimeRange;
import com.facebook.tsdb.tsdash.client.service.HTTPService;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class MetricPresenter implements Presenter {

    public interface MetricsFormWidget {
        void focusSuggest(boolean focus);

        HasValue<String> typedMetric();

        HasClickHandlers addMetricButton();

        HasWidgets metricsContainer();

        void setMetricSuggestions(ArrayList<String> options);

        int metricsCount();

        void setLoadingVisible(boolean visible);

        HasClickHandlers viewAllButton();

        HasText viewAllButtonText();
    }

    public interface MetricWidget {
        HasClickHandlers deleteButton();

        HasClickHandlers cloneButton();

        HasClickHandlers rightAxisButton();

        HasClickHandlers rateButton();

        HasClickHandlers commitButton();

        boolean isPressed(Object toggleButton);

        String selectedAggregator();

        void selectedAggregator(String aggregator);

        HasChangeHandlers aggregator();

        HasValue<Boolean> aggregatorSwitch();

        void pressToggleButton(Object toggleButton, boolean pressed);

        String getName();

        HasWidgets tagsContainer();

        void setEnabled(boolean enabled);

        void markPlottable(boolean plottable);

        void aggregatorEnabled(boolean enabled);

        void commitEnabled(boolean enabled);

        void commitVisible(boolean visible);
    }

    public interface TagWidget {
        HasClickHandlers deleteButton();

        HasClickHandlers setValueButton();

        HasClickHandlers removeValueButton();

        String getSelectedValue();

        void optionsVisible(boolean visible);

        void setValue(String value);
    }

    public interface MetricOptionWidget {
        HasText link();

        HasClickHandlers linkButton();
    }

    private final HandlerManager eventBus;
    private final HTTPService service;
    private final MetricsFormWidget widget;
    private final HashMap<String, MetricHeader> headers =
        new HashMap<String, MetricHeader>();
    private ApplicationState appState;
    // this is used only for metric header update
    private final HashMap<Metric, MetricWidget> metricsWidgets =
        new HashMap<Metric, MetricWidget>();

    private final PopupPanel allMetricsPopup = new PopupPanel(true);
    private final HTMLPanel allMetricsContainer = new HTMLPanel("");

    public MetricPresenter(HandlerManager eventBus, HTTPService service,
            MetricsFormWidget widget) {
        this.eventBus = eventBus;
        this.service = service;
        this.widget = widget;
        bindWidget();
        listenHeaderUpdates();
    }

    private void setupAllMetricsPopup() {
        allMetricsPopup.setWidget(allMetricsContainer);
        Widget w = (Widget) widget;
        int top = w.getAbsoluteTop() + 10;
        int left = w.getAbsoluteLeft() + 350;
        allMetricsPopup.setPopupPosition(left, top);
        allMetricsContainer.addStyleName("allMetricsPopup");
    }

    private void listenHeaderUpdates() {
        eventBus.addHandler(MetricHeaderEvent.TYPE,
                new MetricHeaderEventHandler() {
                    @Override
                    public void onHeadersLoaded(MetricHeaderEvent event) {
                        int i = 0;
                        for (MetricHeader newHeader : event.getHeaders()) {
                            Metric metric = appState.metrics.get(i);
                            if (newHeader.compareTo(metric.header) != 0) {
                                // update the header re-render
                                metric.header = newHeader;
                                MetricWidget metricWidget = metricsWidgets
                                        .get(metric);
                                metricWidget.setEnabled(true);
                                loadTagsWidgets(metricWidget, metric);
                            }
                            i++;
                        }
                    }
                });
    }

    private void loadMetricHeader(final Metric metric, final Command cmd) {
        TimeRange timeRange = appState.getAndUpdateTimeRange();
        widget.setLoadingVisible(true);
        service.loadMetricHeader(metric, timeRange,
                new AsyncCallback<MetricHeader>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.fireEvent(new ErrorEvent(caught));
                    }

                    @Override
                    public void onSuccess(MetricHeader result) {
                        metric.header = result;
                        headers.put(metric.getSignature(), result);
                        widget.setLoadingVisible(false);
                        cmd.execute();
                    }
                });
    }

    private void getCachedHeader(Metric metric, final Command cmd) {
        if (headers.containsKey(metric.getSignature())) {
            metric.header = headers.get(metric.getSignature());
            cmd.execute();
        } else {
            loadMetricHeader(metric, cmd);
        }
    }

    private void bindWidget() {
        // ADD METRIC
        widget.addMetricButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final String metricName = widget.typedMetric().getValue();
                if (metricName.equals("")) {
                    widget.focusSuggest(true);
                    return;
                }
                widget.typedMetric().setValue("");
                final Metric newMetric = new Metric(metricName);
                getCachedHeader(newMetric, new Command() {
                    @Override
                    public void execute() {
                        addMetric(newMetric);
                    }
                });
            }
        });
        // ALL METRICS
        widget.viewAllButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                allMetricsPopup.show();
            }
        });
    }

    private TagWidget createTagWidget(Metric metric, String tag) {
        // if this is a tag set by the user
        if (metric.tags.containsKey(tag)) {
            return new com.facebook.tsdb.tsdash.client.ui.TagWidget(tag,
                    metric.tags.get(tag));
        }
        // we join the aggregated and multiple options in one case
        ArrayList<String> options;
        if (metric.isAggregated() &&
                metric.header.tagsSet.get(tag).size() == 0) {
            options = new ArrayList<String>();
            options.add(metric.aggregator);
        } else {
            options = metric.header.tagsSet.get(tag);
        }
        return new com.facebook.tsdb.tsdash.client.ui.TagWidget(tag, options);
    }

    private TagWidget addTag(final MetricWidget metricWidget,
            final Metric metric, final String tagName) {
        final TagWidget tagWidget = createTagWidget(metric, tagName);
        // TAG SET
        tagWidget.setValueButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String tagValue = tagWidget.getSelectedValue();
                metric.tags.put(tagName, tagValue);
                if (!metric.plottable) {
                    getCachedHeader(metric, new Command() {
                        @Override
                        public void execute() {
                            loadTagsWidgets(metricWidget, metric);
                            metricWidget.commitEnabled(true);
                        }
                    });
                    return;
                }
                eventBus.fireEvent(new TagEvent(TagEvent.Operation.SET, metric,
                        tagName, tagValue));
            }
        });
        // TAG REMOVE VALUE
        tagWidget.removeValueButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                metric.tags.remove(tagName);
                if (!metric.plottable) {
                    getCachedHeader(metric, new Command() {
                        @Override
                        public void execute() {
                            loadTagsWidgets(metricWidget, metric);
                            metricWidget.commitEnabled(true);
                        }
                    });
                }
                eventBus.fireEvent(new TagEvent(TagEvent.Operation.REMOVE,
                        metric, tagName, null));
            }
        });
        metricWidget.tagsContainer().add((Widget) tagWidget);
        return tagWidget;
    }

    private void loadTagsWidgets(MetricWidget metricWidget, Metric metric) {
        metricWidget.tagsContainer().clear();
        for (String tagName : metric.header.tagsSet.keySet()) {
            addTag(metricWidget, metric, tagName);
        }
    }

    private MetricWidget addMetric(final Metric metric) {
        final MetricWidget metricWidget =
            new com.facebook.tsdb.tsdash.client.ui.MetricWidget(metric.name);
        // store the mapping for header update
        metricsWidgets.put(metric, metricWidget);
        // DELETE
        metricWidget.deleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                widget.metricsContainer().remove((Widget) metricWidget);
                if (!metric.isPlottable()) {
                    return;
                }
                eventBus.fireEvent(new MetricEvent(
                        MetricEvent.Operation.DELETE, metric));
            }
        });
        // CLONE
        metricWidget.cloneButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addMetric(metric.dup());
            }
        });
        // COMMIT
        metricWidget.commitButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                metric.plottable = true;
                metricWidget.markPlottable(true);
                metricWidget.commitEnabled(false);
                metricWidget.commitVisible(false);
                eventBus.fireEvent(new MetricEvent(MetricEvent.Operation.ADD,
                        metric));
            }
        });
        // AGGREGATOR SWITCH
        metricWidget.aggregatorSwitch().addValueChangeHandler(
                new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        if (!metric.plottable) {
                            metricWidget.commitEnabled(true);
                            return;
                        }
                        if (event.getValue() == true) {
                            // the aggregator has just been enabled
                            metric.aggregator = metricWidget
                                    .selectedAggregator();
                        } else {
                            metric.aggregator = null;
                        }
                        eventBus.fireEvent(new MetricEvent(
                                MetricEvent.Operation.AGGREGATE, metric));
                    }
                });
        // AGGREGATOR CHANGE
        metricWidget.aggregator().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (!metric.plottable) {
                    metricWidget.commitEnabled(true);
                    return;
                }
                if (metric.aggregator != null) {
                    metric.aggregator = metricWidget.selectedAggregator();
                    eventBus.fireEvent(new MetricEvent(
                            MetricEvent.Operation.AGGREGATE, metric));
                }
            }
        });
        // PARAM TOGGLE
        ClickHandler toggleButtonHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                metricWidget.pressToggleButton(event.getSource(),
                        !metricWidget.isPressed(event.getSource()));
                metric.rightAxis = metricWidget.isPressed(metricWidget
                        .rightAxisButton());
                metric.rate = metricWidget.isPressed(metricWidget.rateButton());
                eventBus.fireEvent(new MetricEvent(
                        MetricEvent.Operation.PARAM_TOGGLE, metric));
            }
        };
        metricWidget.rightAxisButton().addClickHandler(toggleButtonHandler);
        metricWidget.rateButton().addClickHandler(toggleButtonHandler);

        if (metric.header.tagsSet.size() == 0) {
            metricWidget.setEnabled(false);
        } else {
            metricWidget.pressToggleButton(metricWidget.rightAxisButton(),
                    metric.rightAxis);
            metricWidget.pressToggleButton(metricWidget.rateButton(),
                    metric.rate);
            if (!metric.isPlottable()) {
                metricWidget.markPlottable(false);
            }
            if (metric.aggregator != null) {
                metricWidget.aggregatorSwitch().setValue(true, false);
                metricWidget.selectedAggregator(metric.aggregator);
            }
            metricWidget.aggregatorEnabled(metric.allowsAggregation());
            loadTagsWidgets(metricWidget, metric);
        }
        widget.metricsContainer().add((Widget) metricWidget);
        return metricWidget;
    }

    private void loadMetrics(ArrayList<Metric> metrics) {
        widget.metricsContainer().clear();
        for (Metric metric : metrics) {
            addMetric(metric);
        }
    }

    private void addMetricOption(final String metricName) {
        MetricOptionWidget optionWidget =
            new com.facebook.tsdb.tsdash.client.ui.MetricOptionWidget();
        optionWidget.link().setText(metricName);
        optionWidget.linkButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final Metric newMetric = new Metric(metricName);
                allMetricsPopup.hide();
                getCachedHeader(newMetric, new Command() {
                    @Override
                    public void execute() {
                        addMetric(newMetric);
                    }
                });
            }
        });
        allMetricsContainer.add((Widget) optionWidget);
    }

    @Override
    public void go(final HasWidgets container,
            final ApplicationState appState) {
        this.appState = appState;
        container.add((com.google.gwt.user.client.ui.Widget) widget);
        widget.focusSuggest(true);
        loadMetrics(appState.metrics);
        service.loadMetricsName(new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                // fire error event
                eventBus.fireEvent(new ErrorEvent(caught));
            }

            @Override
            public void onSuccess(ArrayList<String> metricNames) {
                widget.setMetricSuggestions(metricNames);
                widget.viewAllButtonText().setText(
                        "view all (" + metricNames.size() + ")");
                setupAllMetricsPopup();
                allMetricsContainer.clear();
                for (String metricName : metricNames) {
                    addMetricOption(metricName);
                }
            }
        });
    }
}
