package com.project.pocketexpensemanager.utilities;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
    public static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("dd : MM : yyyy", Locale.ENGLISH);
    public static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
    public static final String[] MONTHS = new String[]{
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
    };
    public static final int ACTIVATED = 0;
    public static final int DEACTIVATED = 1;
    public static final String SEPARATOR = " : ";
}


