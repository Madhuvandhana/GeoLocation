package com.example.geolocationdemo;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtility {

    /*----------Returns the day of the week elapsed from today------------- */
    public static double getDayFromDateString(String stringDate, String dateTimeFormat) {

        long dayOfWeek = 0;
        long time = 0;
        SimpleDateFormat formatter = new SimpleDateFormat(dateTimeFormat);
        Date date;
        try {
            date = formatter.parse(stringDate);
            Calendar c = Calendar.getInstance();
            Calendar c1 = Calendar.getInstance();
            c1.setTime(date);
            time = c.getTime().getTime() - c1.getTime().getTime();
            dayOfWeek = time / 3600000;
            Log.d("TAG", "dayof:" + dayOfWeek);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dayOfWeek;
    }

    /*----------Returns time(in hours) elapsed from today------------- */
    public static double getTwentyFourHrsTime(String stringDate, String dateTimeFormat) {

        double hourOftheDay = 0;
        long time = 0;
        SimpleDateFormat formatter = new SimpleDateFormat(dateTimeFormat, Locale.US);
        Date date;
        try {
            date = formatter.parse(stringDate);
            Calendar c = Calendar.getInstance();
            Calendar c1 = Calendar.getInstance();
            c1.setTime(date);
            time = c.getTime().getTime() - c1.getTime().getTime();
            hourOftheDay = time / 3600000;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hourOftheDay;
    }
}
