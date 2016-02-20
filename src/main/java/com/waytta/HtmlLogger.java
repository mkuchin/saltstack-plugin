package com.waytta;

import hudson.model.BuildListener;
import hudson.plugins.ansicolor.SimpleHtmlNote;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright (c) 2016 Maxim Kuchin. All rights reserved.
 */

public class HtmlLogger {
    private PrintStream logger;
    private static final Logger LOG = Logger.getLogger(SaltAPIBuilder.class.getName());

    public HtmlLogger(BuildListener listener) {
        logger = listener.getLogger();
    }

    public HtmlLogger print(String html) {
        emitHtml(logger, html);
        return this;
    }

    private void emitHtml(PrintStream logger, String html) {
        try {
            new SimpleHtmlNote(html).encodeTo(logger);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to add HTML markup '" + html + "'", e);
        }
    }
}
