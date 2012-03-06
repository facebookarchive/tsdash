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
package com.facebook.tsdb.tsdash.server.gnuplot;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.facebook.tsdb.tsdash.server.model.DataPoint;
import com.facebook.tsdb.tsdash.server.model.Metric;
import com.facebook.tsdb.tsdash.server.model.TagsArray;

public class Gnuplot2D extends GnuplotProcess {

    public Gnuplot2D() throws Exception {
        super();
    }

    private void write2DTimeSeries(ArrayList<DataPoint> dataPoints,
            String dataPipe) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dataPipe)));
        for (DataPoint point : dataPoints) {
            writer.write(String.format("%d %.2f\n", point.ts, point.value));
        }
        writer.close();
    }

    private String renderLineTitle(Metric metric, TagsArray rowTags) {
        String suffix = metric.isRate() ? " /s" : "";
        return metric.getName() + "{" + rowTags.getTitle() + "}" + suffix;
    }

    @Override
    public String plot(Metric[] metrics, GnuplotOptions options)
            throws Exception {
        int count = 0;
        int rates = 0;
        for (Metric metric : metrics) {
            for (ArrayList<DataPoint> dataPoints : metric.timeSeries.values()) {
                if (dataPoints.size() > 0) {
                    count++;
                }
            }
            if (metric.isRate()) {
                rates++;
            }
        }
        if (count == 0) {
            return noDataFilename();
        }
        // we enable rate display (/s) on the y axis
        if (count == rates) {
            options.setDisplayRate(true);
        }
        createPipes(count);
        options.clearDataSources();
        int i = 0;
        for (Metric metric : metrics) {
            for (TagsArray rowTags : metric.timeSeries.keySet()) {
                if (metric.timeSeries.get(rowTags).size() > 0) {
                    options.addDataSource(new DataSource(getPipeFilename(i),
                           renderLineTitle(metric, rowTags)));
                    i++;
                }
            }
        }
        String outputFilename = getOutputFilename(options);
        options.setOutput(outputFilename);
        gnuplotStdin.write(options.toScript());
        gnuplotStdin.close();
        i = 0;
        for (Metric metric : metrics) {
            for (TagsArray rowTags : metric.timeSeries.keySet()) {
                ArrayList<DataPoint> dataPoints = metric.timeSeries
                        .get(rowTags);
                if (dataPoints.size() > 0) {
                    write2DTimeSeries(dataPoints, getPipeFilename(i));
                    i++;
                }
            }
        }
        return outputFilename;
    }
}
