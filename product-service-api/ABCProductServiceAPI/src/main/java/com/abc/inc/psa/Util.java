package com.abc.inc.psa;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {

    public static String getFullStackTrace(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
