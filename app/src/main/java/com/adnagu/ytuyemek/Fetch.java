package com.adnagu.ytuyemek;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fetch {

    public static final String URL = "http://www.sks.yildiz.edu.tr/yemekmenu/";
    public static final String SHARED_PREFERENCES = "YTUYemek";
    public static AsyncHttpClient client;
    public static Calendar today;

    public static void request(AsyncHttpResponseHandler responseHandler) {
        if(null == client) {
            client = new AsyncHttpClient();
            today = Calendar.getInstance();
        }
        //client.get(URL + "2/" + today.get(Calendar.YEAR), responseHandler);
        client.get(URL + (today.get(Calendar.MONTH) + 1) + "/" + today.get(Calendar.YEAR), responseHandler);
    }

    public static boolean save(Context context, String data) {
        Database db = new Database(context);
        db.resetTables();

        Matcher matcher_menu = Pattern.compile("(?is)<div id=\"menu_background(.*?)one_menu_date").matcher(data);
        Pattern pattern_date = Pattern.compile("[0-9]{2}.[0-9]{2}.[0-9]{4}");
        Pattern pattern_meal = Pattern.compile("(?is)MainMenu\">(.*?)</div>");
        Matcher matcher_date;
        Matcher matcher_meal;
        int day = 1;
        String lunch, dinner;

        while(matcher_menu.find()) {
            matcher_date = pattern_date.matcher(matcher_menu.group());
            if(matcher_date.find()) {
                //Log.d("ADN","Day:"+Integer.parseInt(matcher_date.group().substring(0,2)));
                while(Integer.parseInt(matcher_date.group().substring(0,2)) != day) db.insertMeal(day++, null, null);
                matcher_meal = pattern_meal.matcher(matcher_menu.group());
                lunch = null; dinner = null;
                if(matcher_meal.find()) {
                    lunch = matcher_meal.group().substring(10, matcher_meal.group().length() - 6);
                    if(lunch.charAt(lunch.length() - 1) == '>') lunch = lunch.substring(0, lunch.length() - 5);
                    if(matcher_meal.find()) {
                        dinner = matcher_meal.group().substring(10, matcher_meal.group().length() - 6);
                        if(dinner.length() == 0) dinner = null;
                        else if(dinner.charAt(dinner.length() - 1) == '>') dinner = dinner.substring(0, dinner.length() - 5);
                    }
                }
                db.insertMeal(day++, lunch, dinner);
            }
        }
        //Log.d("ADN","Database getMealCount(): "+db.getMealCount());

        if(db.getMealCount() == 0) return false;
        else {
            context.getSharedPreferences(SHARED_PREFERENCES, 0).edit().putInt("meal_month", today.get(Calendar.MONTH)).apply();
            return true;
        }
    }

    public static void setAlarms(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarm.cancel(PendingIntent.getBroadcast(context, 0, new Intent(context, MealReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
        alarm.cancel(PendingIntent.getBroadcast(context, 11, new Intent(context, LunchNotificationReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
        alarm.cancel(PendingIntent.getBroadcast(context, 17, new Intent(context, DinnerNotificationReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));

        Calendar alarmDate = Calendar.getInstance();

        Database db = new Database(context);
        int day = db.getMealCount();

        alarmDate.set(Calendar.SECOND, 0);
        alarmDate.set(Calendar.MINUTE, 0);

        if(alarmDate.getActualMaximum(Calendar.DAY_OF_MONTH) - day > 3) {
            alarmDate.set(Calendar.HOUR_OF_DAY, 0);
            alarmDate.set(Calendar.DAY_OF_MONTH, day + 1);
        } else {
            alarmDate.set(Calendar.HOUR_OF_DAY, 0);
            alarmDate.set(Calendar.DAY_OF_MONTH, 1);
            alarmDate.add(Calendar.MONTH, 1);
        }

        alarm.set(AlarmManager.RTC, alarmDate.getTimeInMillis(), PendingIntent.getBroadcast(context, 0, new Intent(context, MealReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT));

        alarmDate = Calendar.getInstance();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("ADN","pref_notification:" + pref.getBoolean("pref_notification", true));
			
        if(pref.getBoolean("pref_notification", true)) {
            Calendar now = Calendar.getInstance();
            alarmDate.set(Calendar.SECOND, 0);
            alarmDate.set(Calendar.MINUTE, 0);

            Log.d("ADN", "pref_hours:"+pref.getString("pref_hours", "0"));

            switch (pref.getString("pref_hours", "0")) {
                case "0":
					Log.d("ADN","set alarms 11, 17");
					alarmDate.set(Calendar.HOUR_OF_DAY, 17);
					if(now.getTimeInMillis() > alarmDate.getTimeInMillis()) {
						alarmDate.add(Calendar.DAY_OF_MONTH, 1);
						alarm.setInexactRepeating(AlarmManager.RTC, alarmDate.getTimeInMillis(), 86400000, PendingIntent.getBroadcast(context, 17, new Intent(context, DinnerNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT));
						alarmDate.set(Calendar.HOUR_OF_DAY, 11);
						alarm.setInexactRepeating(AlarmManager.RTC, alarmDate.getTimeInMillis(), 86400000, PendingIntent.getBroadcast(context, 11, new Intent(context, LunchNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT));
					} else {
						alarm.setInexactRepeating(AlarmManager.RTC, alarmDate.getTimeInMillis(), 86400000, PendingIntent.getBroadcast(context, 17, new Intent(context, DinnerNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT));
						alarmDate.set(Calendar.HOUR_OF_DAY, 11);
						if(now.getTimeInMillis() > alarmDate.getTimeInMillis()) alarmDate.add(Calendar.DAY_OF_MONTH, 1);
						alarm.setInexactRepeating(AlarmManager.RTC, alarmDate.getTimeInMillis(), 86400000, PendingIntent.getBroadcast(context, 11, new Intent(context, LunchNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT));
					}
                    break;
                case "1":
					alarmDate.set(Calendar.HOUR_OF_DAY, 11);
					if(now.getTimeInMillis() > alarmDate.getTimeInMillis()) alarmDate.add(Calendar.DAY_OF_MONTH, 1);
					alarm.setInexactRepeating(AlarmManager.RTC, alarmDate.getTimeInMillis(), 86400000, PendingIntent.getBroadcast(context, 11, new Intent(context, LunchNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT));
                    break;
                case "2":                    
					alarmDate.set(Calendar.HOUR_OF_DAY, 17);
					if(now.getTimeInMillis() > alarmDate.getTimeInMillis()) alarmDate.add(Calendar.DAY_OF_MONTH, 1);
					alarm.setInexactRepeating(AlarmManager.RTC, alarmDate.getTimeInMillis(), 86400000, PendingIntent.getBroadcast(context, 17, new Intent(context, DinnerNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT));
                    break;
                default:
            }
        }
    }

}