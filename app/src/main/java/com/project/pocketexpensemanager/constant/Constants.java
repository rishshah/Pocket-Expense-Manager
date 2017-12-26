package com.project.pocketexpensemanager.constant;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
    public static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("dd : MM : yyyy", Locale.ENGLISH);
    public static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
    public static final int CREATED = 0;
    public static final int UPDATED = 1;
    public static final int DELETED = 2;
    public static final int ACTIVATED = 0;
    public static final int DEACTIVATED = 1;
}


