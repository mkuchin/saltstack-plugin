package com.waytta.model;

import net.sf.json.JSONObject;

import java.util.Map;

/**
 * Copyright (c) 2016 Maxim Kuchin. All rights reserved.
 */

//state model from highstate output
public class State implements Comparable<State> {
    private String state;
    private String name;
    private int order;
    private String changes;
    private String comment;
    private Status status;
    private boolean changed;
    public State(Map.Entry<String, JSONObject> entry) {
        JSONObject values = entry.getValue();
        boolean result = values.getBoolean("result");
        boolean warnings = values.containsKey("warnings");
        boolean changes = values.containsKey("changes") && !values.getJSONObject("changes").isEmpty();
        this.changed = !result || warnings || changes;
        if (result) {
            if(changes)
                this.status = Status.CHANGED;
            else
                this.status = Status.OK;
            if (warnings) {
                this.status = Status.WARN;
            }
        } else
            this.status =  Status.FAIL;

        String id = entry.getKey();
        String[] splitted = id.split("\\|");
        this.state = splitted[0].replaceFirst("_", "") + splitted[3];
        this.name = values.has("name") ? values.getString("name") : "";
        this.order = values.getInt("__run_num__");
        this.changes = values.getString("changes");
        this.comment = values.getString("comment");
    }

    public String getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public String getChanges() {
        return changes;
    }

    public Status getStatus() {
        return status;
    }

    public String getComment() {
        return comment;
    }

    public boolean isChanged() {
        return changed;
    }

    @Override
    public int compareTo(State o) {
        return this.order - o.order;
    }
}
