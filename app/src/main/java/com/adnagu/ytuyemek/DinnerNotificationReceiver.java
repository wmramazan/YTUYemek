package com.adnagu.ytuyemek;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

public class DinnerNotificationReceiver extends BroadcastReceiver {

	Intent in;
	PendingIntent pendingIntent;
	NotificationCompat.Builder builder;
	NotificationManager manager;
	Database db;
	Calendar today;
	Cursor cursor;
	String[] strArr;
    String text;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("ADN","started DinnerNotificationReceiver.");

		today = Calendar.getInstance();
		db = new Database(context);

        cursor = db.getDinnerMeal(today.get(Calendar.DAY_OF_MONTH));

        if(cursor.moveToFirst() && !cursor.isNull(0)) {
            in = new Intent(context, MainActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);

            strArr = context.getResources().getStringArray(R.array.notification_texts);
            text = strArr[(int)(Math.random() * strArr.length)].replace("%s", cursor.getString(0));

            builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_restaurant_white_24dp)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setLights(ContextCompat.getColor(context, R.color.colorPrimary), 3000, 3000)
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(text)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);

            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }

	}
}