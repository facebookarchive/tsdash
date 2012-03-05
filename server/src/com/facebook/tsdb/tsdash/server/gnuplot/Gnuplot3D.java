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

import com.facebook.tsdb.tsdash.server.model.DataPoint;
import com.facebook.tsdb.tsdash.server.model.Metric;
import com.facebook.tsdb.tsdash.server.model.TagsArray;

public class Gnuplot3D extends GnuplotProcess {

    public Gnuplot3D() throws Exception {
        super();
    }

    private void writeGridDataPoints(Metric metric, String dataPipe)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dataPipe)));
        int lineCount = 0;
        // is important to add to walk the time-series by using the keys because
        // the are ordered
        for (TagsArray header : metric.timeSeries.keySet()) {
            for (DataPoint p : metric.timeSeries.get(header)) {
                writer.write("" + lineCount + '\t' + p.ts + '\t' + p.value);
                writer.newLine();
            }
            writer.newLine();
            lineCount++;
        }
        writer.close();
    }

    @Override
    public String plot(Metric[] metrics, GnuplotOptions options)
            throws Exception {
        createPipes(metrics.length);
        options.clearDataSources();
        int i = 0;
        for (Metric metric : metrics) {
            metric.alignAllTimeSeries();
            if (metric.hasData()) {
                options.addDataSource(new DataSource(getPipeFilename(i), metric
                        .getName()));
                i++;
            }
        }
        String outputFilename = getOutputFilename(options);
        options.setOutput(outputFilename);
        gnuplotStdin.write(options.toScript());
        gnuplotStdin.close();
        i = 0;
        for (Metric metric : metrics) {
            if (metric.hasData()) {
                writeGridDataPoints(metric, getPipeFilename(i));
                i++;
            }
        }
        return outputFilename;
    }

}
