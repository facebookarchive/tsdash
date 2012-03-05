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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;

import com.facebook.tsdb.tsdash.server.TsdbServlet;
import com.facebook.tsdb.tsdash.server.model.Metric;

public abstract class GnuplotProcess {

    public static final String OUTPUT_DIR_ENV = "TSDASH_PLOT_DIR";
    public static final String PIPES_DIR = "/tmp/plotter";
    public static final String BASH = "/bin/bash";
    public static final String GNUPLOT = "/usr/local/bin/gnuplot";
    private static Random rand = new Random();

    protected Process gnuplot;
    protected BufferedWriter gnuplotStdin;
    protected BufferedReader gnuplotStderr;
    protected ArrayList<String> dataPipes = new ArrayList<String>();
    protected int id;
    protected int plotNo = 0;

    public GnuplotProcess() throws Exception {
        id = rand.nextInt();
        if (id < 0) {
            id = -id;
        }
        ProcessBuilder processBuilder = new ProcessBuilder(GNUPLOT);
        gnuplot = processBuilder.start();
        gnuplotStdin = new BufferedWriter(new OutputStreamWriter(
                gnuplot.getOutputStream()));
        gnuplotStderr = new BufferedReader(new InputStreamReader(
                gnuplot.getErrorStream()));
    }

    protected String getPipeFilename(int pipeNo) {
        return String.format("%s/%d-%d", PIPES_DIR, id, pipeNo);
    }

    protected static String noDataFilename() {
        return TsdbServlet.plotsDir + "/no_data.jpg";
    }

    protected String getOutputFilename(GnuplotOptions options) {
        return String.format("%s/%d-%d.%s", TsdbServlet.plotsDir, id, plotNo,
                options.getTerminal());
    }

    protected void createPipes(int pipesCount) throws IOException,
            InterruptedException {
        String bashCommand = String.format("mkdir -p %s;", PIPES_DIR);
        dataPipes.clear();
        for (int i = 0; i < pipesCount; i++) {
            String pipeFilename = getPipeFilename(i);
            File pipe = new File(pipeFilename);
            if (!pipe.exists()) {
                bashCommand += String.format("mkfifo %s;", pipeFilename);
            }
            dataPipes.add(pipeFilename);
        }
        ProcessBuilder bashBuilder = new ProcessBuilder(BASH);
        Process bash = bashBuilder.start();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                bash.getOutputStream()));
        writer.write(bashCommand);
        writer.newLine();
        writer.close();
        bash.waitFor();
    }

    private String getGnuplotError() throws IOException {
        if (!gnuplotStderr.ready()) {
            return "";
        }
        System.err.println("Reading error: ");
        String error = "Gnuplot Error: ";
        String line;
        while ((line = gnuplotStderr.readLine()) != null) {
            error += line;
        }
        System.err.println(error);
        return error;
    }

    public String plot(Metric metric, GnuplotOptions options) throws Exception {
        Metric[] metrics = new Metric[1];
        metrics[0] = metric;
        return plot(metrics, options);
    }

    public abstract String plot(Metric[] metrics, GnuplotOptions options)
            throws Exception;

    public void close() throws InterruptedException {
        // delete all pipes
        for (String pipeFilename : dataPipes) {
            File pipe = new File(pipeFilename);
            if (pipe.exists()) {
                pipe.delete();
            }
        }
        gnuplot.waitFor();
        gnuplot.destroy();
    }

    public static GnuplotProcess create(boolean surface) throws Exception {
        if (surface) {
            return new Gnuplot3D();
        }
        return new Gnuplot2D();
    }
}
