package com.example.bookapp1;

import android.app.Application;
import android.text.format.DateFormat;


import java.util.Calendar;
import java.util.Locale;

// application class will run before launching activity
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // create a static method to convert timestamp to proper date format,
    // no need to rewrite again
    public static final String timestampFormat(long timeStamp)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timeStamp);

        // formatting timestamp to dd/mm/yyyy
        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }
}
