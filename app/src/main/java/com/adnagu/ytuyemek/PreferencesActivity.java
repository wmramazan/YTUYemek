package com.adnagu.ytuyemek;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Calendar;

public class PreferencesActivity extends AppCompatPreferenceActivity {

    SharedPreferences pref;
    boolean pref_notification, updateMeals;
    int pref_hours;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref_notification = pref.getBoolean("pref_notification", true);
        pref_hours = Integer.parseInt(pref.getString("pref_hours", "0"));
        updateMeals = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        intent = new Intent();
        if(pref_notification != pref.getBoolean("pref_notification", true) || pref_hours != Integer.parseInt(pref.getString("pref_hours", "0"))) intent.putExtra("setAlarms", true);
        setResult(0, intent);
        super.finish();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        Preference update, about;
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            update = findPreference("pref_update");
            update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());

                    Fetch.request(new AsyncHttpResponseHandler() {
                        @Override
                        public void onStart() {
                            progressDialog.setMessage(getString(R.string.loadingMeals));
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                        }

                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                            if(Fetch.save(getActivity(), new String(responseBody))) Toast.makeText(getActivity(), R.string.savedMeals, Toast.LENGTH_LONG).show();
                            else Toast.makeText(getActivity(), R.string.alreadyUpdated, Toast.LENGTH_LONG).show();

                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                            if(internetConnection()) Toast.makeText(getActivity(), R.string.somethingWentWrong, Toast.LENGTH_LONG).show();
                            else Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFinish() {
                            progressDialog.dismiss();
                        }
                    });
                    return false;
                }
            });
            about = findPreference("pref_about");
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(R.string.aboutApp)
                            .setMessage(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME + "\n\n" + getString(R.string.usedLibraries) + getString(R.string.libraries))
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return false;
                }
            });
        }

        public boolean internetConnection() {
            ConnectivityManager connectivity = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] inf = connectivity.getAllNetworkInfo();
                if (inf != null)
                    for (int i = 0; i < inf.length; i++)
                        if (inf[i].getState() == NetworkInfo.State.CONNECTED) return true;
            }
            return false;
        }
    }
}
