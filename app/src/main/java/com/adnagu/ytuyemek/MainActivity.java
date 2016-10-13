package com.adnagu.ytuyemek;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    Database db;
    SharedPreferences pref;
    RecyclerView mealList;
    ArrayList<Meal> meals = new ArrayList<Meal>();
    MealListAdapter mealListAdapter;
    LinearLayoutManager layoutManager;
    LinearSnapHelper snapHelper;
    DatePickerDialog pickerDialog;
    Calendar today;
    RelativeLayout layout;
    RelativeLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        today = Calendar.getInstance();
        pref = getSharedPreferences(Fetch.SHARED_PREFERENCES, 0);

        layout = (RelativeLayout) findViewById(R.id.main_layout);
        mealList = (RecyclerView) findViewById(R.id.meal_list);

        db = new Database(this);

        //sendBroadcast(new Intent(this, LunchNotificationReceiver.class));
        //fetchMeals();

        if(today.get(Calendar.MONTH) != pref.getInt("meal_month", -1) || today.get(Calendar.DAY_OF_MONTH) > db.getMealCount()) fetchMeals();
        else initCards();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            Log.d("ADN","resultCode:"+resultCode);
            switch (resultCode) {
                case 0:
                    if(data.getBooleanExtra("setAlarms", false)) Fetch.setAlarms(this);
                    updateMeals();
                    break;
                default:
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void fetchMeals() {
        Fetch.request(new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                progressLayout = (RelativeLayout) findViewById(R.id.main_progress);
                progressLayout.setVisibility(View.VISIBLE);
                mealList.setVisibility(View.GONE);
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(Fetch.save(MainActivity.this, new String(responseBody))) {
                    mealList.setVisibility(View.VISIBLE);
                    initCards();
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mealList.smoothScrollToPosition(today.get(Calendar.DAY_OF_MONTH) - 1);
                        }
                    }, 100);*/
                } else {
                    Snackbar.make(layout, R.string.somethingWentWrong, Snackbar.LENGTH_LONG).show();
                    findViewById(R.id.button_tryAgain).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if(internetConnection()) Snackbar.make(layout, R.string.somethingWentWrong, Snackbar.LENGTH_LONG).show();
                else Snackbar.make(layout, R.string.noInternet, Snackbar.LENGTH_LONG).show();
                findViewById(R.id.button_tryAgain).setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                progressLayout.setVisibility(View.GONE);
                super.onFinish();
            }
        });
    }

    public void initCards() {
        Log.d("ADN", "initCards()");
        meals = db.getItems();
        if(meals.size() == 0) {
            fetchMeals();
            return;
        } else Fetch.setAlarms(this);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mealList.setLayoutManager(layoutManager);
        mealList.setHasFixedSize(true);
        mealListAdapter = new MealListAdapter(this, meals);
        mealList.setAdapter(mealListAdapter);

        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mealList);

        pickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if(dayOfMonth > meals.size()) mealList.smoothScrollToPosition(meals.size());
                else mealList.smoothScrollToPosition(dayOfMonth - 1);
            }

        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        today.set(Calendar.DAY_OF_MONTH, today.getActualMinimum(Calendar.DAY_OF_MONTH));
        pickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
        today.set(Calendar.DAY_OF_MONTH, meals.size());
        pickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        today = Calendar.getInstance();

        mealList.scrollToPosition(today.get(Calendar.DAY_OF_MONTH) - 1);
    }

    public void updateMeals() {
        meals = db.getItems();
        mealListAdapter.notifyDataSetChanged();
    }

    public void tryAgain(View v) {
        v.setVisibility(View.GONE);
        fetchMeals();
    }

    public void pickDate(View v) {
        if(null != mealListAdapter) pickerDialog.show();
    }

    public void goToToday(View v) {

        if(null != mealListAdapter) {
            mealList.smoothScrollToPosition(today.get(Calendar.DAY_OF_MONTH) - 1);
        }
    }

    public void goToPreferences(View v) {
        startActivityForResult(new Intent(this, PreferencesActivity.class), 0);
    }

    public boolean internetConnection() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] inf = connectivity.getAllNetworkInfo();
            if (inf != null)
                for (int i = 0; i < inf.length; i++)
                    if (inf[i].getState() == NetworkInfo.State.CONNECTED) return true;
        }
        return false;
    }

}
