package com.waytta;

import hudson.model.BuildListener;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Map;

/**
 * Copyright (c) 2016 Maxim Kuchin. All rights reserved.
 */

public class HtmlDumper {
    private HtmlLogger html;
    public HtmlDumper(BuildListener listener) {
        html = new HtmlLogger(listener);
    }
    @SuppressWarnings("unchecked")
    public void dump(JSONArray array) {
        JSONObject highState =  array.getJSONObject(0);
        if(highState.getString("outputter").equals("highstate")) {
            html.print("</pre></pre>");
            html.print("<style>table.border\n" +
                    "{\n" +
                    "    border-color: #600;\n" +
                    "    border-width: 0 0 1px 1px;\n" +
                    "    border-style: solid;\n" +
                    "    border-collapse: collapse;\n" +
                    "}\n" +
                    "\n" +
                    "table.border td, table.border th\n" +
                    "{\n" +
                    "    border-color: #600;\n" +
                    "    border-width: 1px 1px 0 0;\n" +
                    "    border-style: solid;\n" +
                    "    margin: 0;\n" +
                    "    padding: 4px;\n" +
                    "}\n" +
                    ".red{background-color: #FCC;}\n" +
                    ".green{background-color: #CFC;}\n" +
                    ".yellow{background-color: #FFC;}\n" +
                    "</style>");
            html.print("<h1>state.highstate status</h1>");
            JSONObject data = highState.getJSONObject("data");
            for (Object key: data.keySet()) {
                html.print("<h2>" + key + "</h2>");
                html.print("<table class='border'>");
                html.print("<tr><th>state</th><th>name</th><th>result</th><th>order</th><th>changes</th></tr>");
                JSONObject minion = data.getJSONObject((String) key);
                for (Object entryObject: minion.entrySet()) {
                    Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) entryObject;
                    JSONObject values = entry.getValue();
                    boolean result = values.getBoolean("result");
                    boolean warnings = values.containsKey("warnings");
                    String color = "red";
                    String resultName = "Fail";
                    if(result) {
                        color = "green";
                        resultName = "OK";
                        if(warnings) {
                            color = "yellow";
                            resultName = "Warn";
                        }
                    }
                    html.print("<tr class='" + color + "'>");
                    String id = entry.getKey();
                    String[] splitted = id.split("\\|");
                    html.print("<td>").print(splitted[0].replaceFirst("_", "")  + splitted[3]).print("</td>");
                    printCell(values, "name");
                    html.print("<td>").print(resultName).print("</td>");
                    printCell(values, "__run_num__");
                    printCell(values, "changes");
                    html.print("</tr>");
                }
                html.print("</table>");
                html.print("<pre>");
            }
        }
    }

    private void printCell(JSONObject object, String key) {
        html.print("<td>").print(object.getString(key)).print("</td>");
    }
}
