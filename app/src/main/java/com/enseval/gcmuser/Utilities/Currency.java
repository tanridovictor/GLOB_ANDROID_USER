package com.enseval.gcmuser.Utilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Currency {
    private static Locale localeID = new Locale("in", "ID");
    private static DecimalFormat kursIdr = (DecimalFormat) DecimalFormat.getCurrencyInstance(localeID);
    private static DecimalFormatSymbols formatIDR = new DecimalFormatSymbols();

    public static DecimalFormat getCurrencyFormat(){
        formatIDR.setCurrencySymbol("IDR ");
        formatIDR.setMonetaryDecimalSeparator(',');
        formatIDR.setGroupingSeparator('.');
        kursIdr.setDecimalFormatSymbols(formatIDR);
        return kursIdr;
    }
}
