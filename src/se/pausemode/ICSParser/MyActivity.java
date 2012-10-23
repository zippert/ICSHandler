package se.pausemode.ICSParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;
import se.pausemode.ICSParser.DataTypes.AttendeeData;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static android.provider.CalendarContract.Events;

public class MyActivity extends Activity {
    Calendar calendar;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d("ics", "onCreate");
        super.onCreate(savedInstanceState);
        Intent i = super.getIntent();

        calendar = null;
        try {
            calendar = new CalendarHandler(new File(new URI(i.getDataString()))).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Intent launchIntent = createIntent(calendar);
        if(launchIntent != null){
            startActivity(launchIntent);
            finish();
        }

    }

    private Intent createIntent(se.pausemode.ICSParser.Calendar calendar){
        Log.d("ics", "createIntent: " + calendar.toString());

        Intent intent = null;
        if (calendar.getSTATUS() == Calendar.EventStatus.CONFIRMED){
            if(calendar.getSEQUENCE() == 0){
                intent = createNewEventIntent(calendar);
            } else {
                intent = createChangeEventIntent(calendar);
            }
        } else if(calendar.getSTATUS() == Calendar.EventStatus.CANCELLED){
            intent = createRemoveEventIntent(calendar);
        }

        return intent;
    }

    private Intent createChangeEventIntent(final Calendar calendar) {
        new AlertDialog.Builder(MyActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.not_supported_header)
                .setMessage(R.string.not_supported_changed)
                .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = createNewEventIntent(calendar);
                        startActivity(intent);
                        finish();
                    }

                })
                .setNegativeButton(R.string.confirm_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which){
                        finish();
                    }
                })
                .show();
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private Intent createRemoveEventIntent(Calendar calendar) {
        //Intent intent =  new Intent(Intent.ACTION_EDIT);

        new AlertDialog.Builder(MyActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.not_supported_header)
                .setMessage(R.string.not_supported_delete)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .show();
        return null;
        //return intent;
    }

    private Intent createNewEventIntent(Calendar calendar){
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(Events.CONTENT_URI);
        if(calendar.getUID() != null){
            intent.putExtra(Intent.EXTRA_UID, calendar.getUID());
        }
        if(calendar.getDTSTART() != null && calendar.getDTSTART().getValue() != null){
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, parseCalendarString(calendar.getDTSTART().getValue()).getTimeInMillis());
        }
        if(calendar.getDTEND() != null && calendar.getDTEND().getValue() != null){
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, parseCalendarString(calendar.getDTEND().getValue()).getTimeInMillis());
        }
        if(calendar.getSUMMARY() != null && calendar.getSUMMARY().getString() != null){
            intent.putExtra(Events.TITLE, calendar.getSUMMARY().getString());
        }
        if(calendar.getDESCRIPTION() != null && calendar.getDESCRIPTION().getString() != null){
            String descString =  calendar.getDESCRIPTION().getString().replaceAll("\\n", "\\\n");
            Toast.makeText(this, descString, Toast.LENGTH_LONG).show();
            intent.putExtra(Events.DESCRIPTION, descString);
        }
        if(calendar.getLOCATION() != null && calendar.getLOCATION().getString() != null){
            intent.putExtra(Events.EVENT_LOCATION, calendar.getLOCATION().getString());
        }
        if(calendar.getATTENDEES() != null && calendar.getATTENDEES().length > 0){
            //intent.putExtra(Intent.EXTRA_EMAIL, createCommaSeparatedStringOfAddresses(calendar.getATTENDEES()));
        }
        if(calendar.getRECURRENCERULE() != null && calendar.getRECURRENCERULE().getCompleteString() != null){
            intent.putExtra(Events.RRULE, calendar.getRECURRENCERULE().getCompleteString());
        }
        if(calendar.getDTSTART() != null && calendar.getDTEND() != null){
            boolean allDay = isAllDayEvent(calendar);
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay);
        }


        return intent;
    }

    private boolean isAllDayEvent(Calendar calendar) {
        String startString = calendar.getDTSTART().getValue();
        String endString = calendar.getDTEND().getValue();
         return startString.substring(9).equals("000000") && endString.substring(9).equals("000000");
    }

    private java.util.Calendar parseCalendarString(String calendarString) {
       //20120828T150000
        java.util.Calendar c = null;
        if(calendarString != null && calendarString.length() == 15){
            c = java.util.Calendar.getInstance();
            int year, month, day, hour, minute, second;
            year = Integer.parseInt(calendarString.substring(0, 4));
            //Month is 0-based in java.util.Calendar
            month = Integer.parseInt(calendarString.substring(4,6)) - 1;
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
