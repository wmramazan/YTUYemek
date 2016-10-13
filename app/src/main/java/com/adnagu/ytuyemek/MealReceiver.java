package com.adnagu.ytuyemek;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class MealReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(final Context context, Intent intent) {

        Log.d("ADN","started MealReceiver.");

		Fetch.request(new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				if(!Fetch.save(context, new String(responseBody))) {
                    Log.d("ADN","Error: Fetch.save");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, MealReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Calendar date  = Calendar.getInstance();
                    date.set(Calendar.SECOND, 0);
                    date.set(Calendar.MINUTE, 0);
                    date.add(Calendar.HOUR_OF_DAY, 1);
                    alarm.set(AlarmManager.RTC, date.getTimeInMillis(), pendingIntent);
                }
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("ADN","Error: MealReceiver");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, MealReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Calendar date  = Calendar.getInstance();
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MINUTE, 0);
                date.add(Calendar.HOUR_OF_DAY, 1);
                alarm.set(AlarmManager.RTC, date.getTimeInMillis(), pendingIntent);
			}
		});

	}


}