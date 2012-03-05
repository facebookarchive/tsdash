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
package com.facebook.tsdb.tsdash.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;

import com.facebook.tsdb.tsdash.server.data.hbase.HBaseConnection;

public class TsdbServlet extends HttpServlet {

    protected static Logger logger = Logger
            .getLogger("com.facebook.tsdb.services");

    private static final long serialVersionUID = 1L;
    public static final String PROPERTIES_FILE = "conf/tsdash.properties";
    public static final String LOG4J_PROPERTIES_FILE = "conf/log4j.properties";

    public static final String URL_PATTERN_PARAM = "plot.tsdash.urlpattern";
    public static final String DEFAULT_URL_PATTERN = "http://%h:%p/plots/%f";
    public static final int DEFAULT_PLOT_PORT = 8090;
    public static final String PLOTS_DIR_PARAM = "plot.tsdash.dir";
    public static final String DEFAULT_PLOTS_DIR = "/tmp";
    public static String plotsDir = DEFAULT_PLOTS_DIR;
    private static String URLPattern = DEFAULT_URL_PATTERN;
    private String hostname = null;

    private static void loadConfiguration() {
        Properties tsdbConf = new Properties();
        try {
            PropertyConfigurator.configure(LOG4J_PROPERTIES_FILE);
            tsdbConf.load(new FileInputStream(PROPERTIES_FILE));
            HBaseConnection.configure(tsdbConf);
            URLPattern = tsdbConf.getProperty(URL_PATTERN_PARAM,
                    DEFAULT_URL_PATTERN);
            logger.info("URL pattern: " + URLPattern);
            plotsDir = tsdbConf.getProperty(PLOTS_DIR_PARAM, DEFAULT_PLOTS_DIR);
            logger.info("Plots are being written to: " + plotsDir);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find " + PROPERTIES_FILE);
        } catch (IOException e) {
            System.err.println("Cannot read " + PROPERTIES_FILE);
        }
    }

    static {
        loadConfiguration();
    }

    protected String generatePlotURL(String filenamePath)
            throws UnknownHostException {
        File plot = new File(filenamePath);
        if (hostname == null) {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        String URL = URLPattern.replace("%h", hostname);
        URL = URL.replace("%p", "" + DEFAULT_PLOT_PORT);
        URL = URL.replace("%f", plot.getName());
        return URL;
    }

    @SuppressWarnings("unchecked")
    protected String getErrorResponse(Throwable e) {
        JSONObject errObj = new JSONObject();
        errObj.put("error", e.getMessage());
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        errObj.put("stacktrace", stackTrace.toString());
        return errObj.toJSONString();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}
