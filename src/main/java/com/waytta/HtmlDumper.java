package com.waytta;

import hudson.model.BuildListener;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Copyright (c) 2016 Maxim Kuchin. All rights reserved.
 */

public class HtmlDumper {
    private HtmlLogger html;
    private static final Logger log = Logger.getLogger(HtmlDumper.class);

    public HtmlDumper(BuildListener listener) {
        html = new HtmlLogger(listener);
    }
    @SuppressWarnings("unchecked")
    public void dump(JSONArray array) {
        JSONObject highState =  array.getJSONObject(0);
        if(highState.getString("outputter").equals("highstate")) {
            String style = "";
            try {
                InputStream styleStream = this.getClass().getClassLoader().getResourceAsStream("saltstack/css/style.css");
                style = IOUtils.toString(styleStream, "UTF-8");
                styleStream.close();
            } catch(IOException e) {
                log.error("Error loading style:", e);
            }
            html.print("<style>").print(style).print("</style>");
            html.print("<h1>state.highstate status</h1>");
            JSONObject data = highState.getJSONObject("data");
            for (Object key: data.keySet()) {
                html.print("<h2>" + key + "</h2>");

                JSON json = (JSON)data.get(key);
                if(json.isArray()) {
                    JSONArray messages = (JSONArray) json;
                   html.print("<div class='red'>").print(StringUtils.join(messages.toArray(), ", ")).print("</div>");
                }
                 else {
                    html.print("<table class='border'>");
                    html.print("<tr><th>state</th><th>name</th><th>result</th><th>order</th><th>changes</th></tr>");
                    JSONObject minion = data.getJSONObject((String) key);
                    for (Object entryObject : minion.entrySet()) {
                        Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) entryObject;
                        JSONObject values = entry.getValue();
                        boolean result = values.getBoolean("result");
                        boolean warnings = values.containsKey("warnings");
                        boolean changes = values.containsKey("changes") && !values.getJSONObject("changes").isEmpty();
                        String color = "red";
                        String resultName = "Fail";
                        if (result) {
                            if(changes)
                              color = "green";
                            else
                              color = "";
                            resultName = "OK";
                            if (warnings) {
                                color = "yellow";
                                resultName = "Warn";
                            }
                        }
                        html.print("<tr class='" + color + "'>");
                        String id = entry.getKey();
                        String[] splitted = id.split("\\|");
                        html.print("<td>").print(splitted[0].replaceFirst("_", "") + splitted[3]).print("</td>");
                        printCell(values, "name");
                        html.print("<td title='").print(values.getString("comment").replaceAll("'", "&apos;")).print("'>").print(resultName).print("</td>");
                        printCell(values, "__run_num__");
                        printCell(values, "changes");
                        html.print("</tr>");
                    }
                    html.print("</table>");
                }
            }
        }
    }

    private void printCell(JSONObject object, String key) {
        String value = "&nbsp;";
        if(object.has(key))
            value = object.getString(key).replaceAll("\\\"", "\"");
        html.print("<td>").print(value).print("</td>");
    }
}
