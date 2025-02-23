package com.abc.inc.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {

    public static String getFullStackTrace(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
