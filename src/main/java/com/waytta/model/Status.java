package com.waytta.model;

/**
 * Copyright (c) 2016 Maxim Kuchin. All rights reserved.
 */

public enum Status {
    OK(""), FAIL("red"), CHANGED("green"), WARN("yellow");
    String color;

    Status(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
