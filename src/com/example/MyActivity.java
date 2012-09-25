package com.example;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import se.pausemode.ICSHandler.CalendarHandler;
import se.pausemode.ICSHandler.DataTypes.AttendeeData;


import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static android.provider.CalendarContract.Events;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AssetManager am = getApplicationContext().getAssets();
        InputStream is = null;
        try {
            is = am.open("invite.ics");
        } catch (IOException e) {
            e.printStackTrace();
        }
         se.pausemode.ICSHandler.Calendar c = new CalendarHandler(is).build();

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, parseCalendarString(c.getDTSTART().getValue()))
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, parseCalendarString(c.getDTEND().getValue()))
                .putExtra(Events.TITLE, c.getSUMMARY().getString())
                .putExtra(Events.DESCRIPTION, c.getDESCRIPTION().getString())
                .putExtra(Events.EVENT_LOCATION, c.getLOCATION().getString())
                .putExtra(Intent.EXTRA_EMAIL, createCommaSeparatedStringOfAddresses(c.getATTENDEES()))
                .putExtra(Events.RRULE, "FREQ=WEEKLY;COUNT=11;WKST=SU;BYDAY=TU,TH");
        startActivity(intent);
        finish();
    }

    private Calendar parseCalendarString(String calendarString) {
       //20120828T150000
        Calendar c = null;
        if(calendarString != null && calendarString.length() == 15){
            c = Calendar.getInstance();
            int year, month, day, hour, minute, second;
            year = Integer.parseInt(calendarString.substring(0, 4));
            month = Integer.parseInt(calendarString.substring(4,6));
            day = Integer.parseInt(calendarString.substring(6,8));
            hour = Integer.parseInt(calendarString.substring(9,11));
            minute = Integer.parseInt(calendarString.substring(11, 13));
            second = Integer.parseInt(calendarString.substring(13));
            c.set(year,month,day,hour,minute,second);
        }
        return c;

    }

    private String createCommaSeparatedStringOfAddresses(AttendeeData[] attendeeData){
        String retVal = null;
        if(attendeeData != null){
            StringBuilder sb = new StringBuilder();
            for(AttendeeData ad : attendeeData){
                sb.append(ad.getURI().substring(ad.getURI().indexOf(":")+1) + ", ");
            }
            sb.delete(sb.length()-2,sb.length());
            retVal = sb.toString();
        }
        return retVal;
    }

}
