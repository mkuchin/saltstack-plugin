package com.waytta;

import com.waytta.model.State;
import hudson.model.BuildListener;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Copyright (c) 2016 Maxim Kuchin. All rights reserved.
 */

public class HtmlDumper {
    private HtmlLogger html;
    private static final Logger log = Logger.getLogger(HtmlDumper.class);
    private boolean changes;
    public HtmlDumper(BuildListener listener, boolean changes) {
        html = new HtmlLogger(listener);
        this.changes = changes;
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
                    //todo: fix div style
                   html.print("<div class='red'>").print(StringUtils.join(messages.toArray(), ", ")).print("</div>");
                }
                 else {
                    html.print("<table class='border'>");
                    html.print("<tr><th>state</th><th>name</th><th>status</th><th>order</th><th>changes</th></tr>");
                    JSONObject minion = data.getJSONObject((String) key);
                    LinkedList<State> states = new LinkedList<>();
                    for (Object entryObject : minion.entrySet()) {
                        Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) entryObject;
                        State state = new State(entry);

                        if(!changes || state.isChanged())
                            states.add(state);
                    }
                    Collections.sort(states);
                    for (State state: states) {
                        html.print("<tr class='" + state.getStatus().getColor() + "'>");
                        html.print("<td>").print(state.getState()).print("</td>");
                        printCell(state.getName());
                        html.print("<td title='").print(state.getComment().replaceAll("'", "&apos;")).print("'>").print(state.getStatus().name()).print("</td>");
                        printCell(String.valueOf(state.getOrder()));
                        printCell(state.getChanges());
                        html.print("</tr>");
                    }
                    html.print("</table>");
                }
            }
        }
    }

    private void printCell(String value) {
        html.print("<td>").print(value).print("</td>");
    }
}
