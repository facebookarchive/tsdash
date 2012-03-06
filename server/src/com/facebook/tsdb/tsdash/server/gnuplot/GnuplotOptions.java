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

import java.util.ArrayList;

public class GnuplotOptions {

    public enum Terminal {
        SVG, PNG, JPEG;
    }

    private Terminal terminal = Terminal.PNG;
    private int width = 800; // px
    private int height = 600; // px
    public String outputFilename = null;
    private final ArrayList<DataSource> dataSources =
        new ArrayList<DataSource>();
    private long tsFrom = 0;
    private long tsTo = 0;
    private boolean surface;
    private boolean palette = false;
    private boolean displayRate = false;

    public GnuplotOptions(boolean surface) {
        this.surface = surface;
    }

    public void addDataSource(DataSource dataSource) {
        dataSources.add(dataSource);
    }

    public void clearDataSources() {
        dataSources.clear();
    }

    public GnuplotOptions setDisplayRate(boolean rate) {
        displayRate = rate;
        return this;
    }

    public GnuplotOptions setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public GnuplotOptions enablePalette(boolean enabled) {
        palette = enabled;
        return this;
    }

    public GnuplotOptions enableSurface(boolean enabled) {
        surface = enabled;
        return this;
    }

    public boolean isSurfaceEnabled() {
        return surface;
    }

    public GnuplotOptions setTimeRange(long tsFrom, long tsTo) {
        this.tsFrom = tsFrom;
        this.tsTo = tsTo;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GnuplotOptions setTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }

    public String getTerminal() {
        return terminal.toString().toLowerCase();
    }

    public GnuplotOptions setOutput(String outputFilename) {
        if (!outputFilename.endsWith(getTerminal())) {
            outputFilename += "." + getTerminal();
        }
        this.outputFilename = outputFilename;
        return this;
    }

    private String generatePlotCommand() {
        String cmd = "";
        for (DataSource dataSource : dataSources) {
            if (!cmd.equals("")) {
                cmd += ", ";
            }
            cmd += '"' + dataSource.dataFilename + '"' + " using 1:2 "
                    + "title " + '"' + dataSource.title + '"';
        }
        return "plot " + cmd;
    }

    private String generateSurfacePlotCommand() {
        String cmd = "";
        for (DataSource dataSource : dataSources) {
            if (!cmd.equals("")) {
                cmd += ", ";
            }
            cmd += '"' + dataSource.dataFilename + '"' + " using 1:2:3 "
                    + "title " + '"' + dataSource.title + '"' + " with lines";
            if (palette) {
                cmd += " palette";
            }
        }
        return "splot " + cmd;
    }

    private String getValueFormat() {
        final String Y_FORMAT = "%.1s %c";
        if (displayRate) {
            return '"' + Y_FORMAT + "/s" + '"';
        }
        return '"' + Y_FORMAT + '"';
    }

    private String generateLinePointsScript() {
        String script = "reset\n";
        script += "set terminal " + terminal.toString().toLowerCase()
                + " size " + width + "," + height + "\n";
        script += "set xdata time\n";
        script += "set timefmt \"%s\"\n";
        script += "set format y " + getValueFormat() + "\n";
        script += "set xtic rotate\n";
        if (tsFrom > 0 && tsTo > 0) {
            script += "set xrange [\"" + tsFrom + "\":\"" + tsTo + "\"]\n";
        }
        script += "set grid\n";
        script += "set style data linespoints\n";
        script += "set output \"" + outputFilename + "\"\n";
        script += "set key right box\n";
        script += generatePlotCommand() + "\n";
        return script;
    }

    private String generateSurfaceScript() {
        String script = "reset\n";
        script += "set terminal " + terminal.toString().toLowerCase()
                + " size " + width + "," + height + "\n";
        script += "set ydata time\n";
        script += "set timefmt \"%s\"\n";
        script += "set format z " + getValueFormat() + "\n";
        script += "set format cb " + getValueFormat() + "\n";
        script += "set grid\n";
        script += "set xtics 1\n";
        script += "set hidden3d trianglepattern 7\n";
        script += "set key right box\n";
        script += "set yrange [] reverse\n";
        script += "set output \"" + outputFilename + "\"\n";
        script += generateSurfacePlotCommand() + "\n";
        return script;
    }

    public String toScript() {
        if (surface) {
            return generateSurfaceScript();
        } else {
            return generateLinePointsScript();
        }
    }

}
