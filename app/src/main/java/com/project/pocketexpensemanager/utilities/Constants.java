package com.project.pocketexpensemanager.utilities;

import com.project.pocketexpensemanager.R;

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
    public static final int[] COLORS = new int[]{
            R.color.color1,
            R.color.color2,
            R.color.color3,
            R.color.color4,
            R.color.color5,
            R.color.color6,
            R.color.color7,
            R.color.color8,
            R.color.color9
    };
    public static final int ACTIVATED = 0;
    public static final int DEACTIVATED = 1;
}


