package org.phone_lab.maybe.googlecloudmessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends ActionBarActivity {
    public final static String TAG = "Maybe.GCM";

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "1068479230660";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;
    String deviceID;
    boolean createNewRecord = false;
    String remoteRecord = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        deviceID = tm.getDeviceId();

        // Check device for Play Services APK.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            Toast.makeText(this, "Regid: " + regid, Toast.LENGTH_SHORT).show();
            if (regid.isEmpty()) {
                registerInBackground();
            }
            tellServer();
        } else {
            Toast.makeText(this, "No Play Services!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            Toast.makeText(this, "App version changed.", Toast.LENGTH_SHORT).show();
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String msg = "";
                try {
                    Toast.makeText(context, "Start AsyncTask!", Toast.LENGTH_SHORT).show();
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid);
                    updateToServer();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void updateToServer() {
        Log.d(TAG, "update to server run");
        if (regid == null || regid.isEmpty()) {
            Log.d(TAG, "regid is not ready!");
            return;
        }
        if (remoteRecord == null) {
            Log.d(TAG, "remoteRecord is not ready!");
            return;
        }
        Log.d(TAG, "actual update to server");
        SyncWithMaybe syncWithMaybe = new SyncWithMaybe();
        syncWithMaybe.execute();
    }

    private class SyncWithMaybe extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            HttpClient httpclient = new DefaultHttpClient();
            try {
                if (createNewRecord) {
                    Log.d(TAG, "Create new Record");
                    String postUrl = "https://maybe.xcv58.me/maybe-api-v1/devices";
                    HttpPost post = new HttpPost(postUrl);
                    post.setHeader("Content-type", "application/json");

                    JSONObject data = new JSONObject();
                    data.put("deviceid", deviceID);
                    data.put("gcmid", regid);

                    StringEntity se = new StringEntity(data.toString());
                    post.setEntity(se);

                    HttpResponse response = httpclient.execute(post);
                    StatusLine statusLine = response.getStatusLine();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    String responseString = out.toString();
                    Log.d(TAG, responseString);
                    out.close();
                } else {
                    // examine whether is difference
                    String GCMID = "gcmid";
                    JSONArray jsonArray = new JSONArray(remoteRecord);
                    JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                    Log.d(TAG, "Compare local regid and remote regid ");
                    if (jsonObject.has(GCMID)) {
                        if (regid.equals(jsonObject.getString(GCMID))) {
                            Log.d(TAG, "has gcmid and equal");
                            return null;
                        } else {
                            Log.d(TAG, "has gcmid not equal");
                        }
                    } else {
                        Log.d(TAG, "no gcmid");
                    }
                    // update gcmid:
                    String putUrl = "https://maybe.xcv58.me/maybe-api-v1/devices/" + deviceID;
                    HttpPut put = new HttpPut(putUrl);
                    put.setHeader("Content-type", "application/json");

                    JSONObject data = new JSONObject();
                    JSONObject gcmidJson = new JSONObject();
                    gcmidJson.put(GCMID, regid);
                    data.put("$set", gcmidJson);

                    StringEntity se = new StringEntity(data.toString());
                    put.setEntity(se);

                    HttpResponse response = httpclient.execute(put);
                    StatusLine statusLine = response.getStatusLine();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    String responseString = out.toString();
                    Log.d(TAG, responseString);
                    out.close();
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //    private String URL = "https://maybe.xcv58.me/maybe-api-v1/devices/001";
    private class MaybeBackendTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            HttpClient httpclient = new DefaultHttpClient();
            try {
                String URL = "https://maybe.xcv58.me/maybe-api-v1/devices/" + deviceID;
                Log.d(TAG, URL);
                HttpGet get = new HttpGet(URL);
                get.setHeader("Content-type", "application/json");

                HttpResponse response = httpclient.execute(get);
                StatusLine statusLine = response.getStatusLine();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                remoteRecord = responseString;
                Log.d(TAG, responseString);
                if (responseString.contains("No Record(s) Found")) {
                    Log.d(TAG, "prepare new");
                    createNewRecord = true;
                } else {
                    Log.d(TAG, "update exist");
                    createNewRecord = false;
                }
                out.close();
                updateToServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void tellServer() {
        MaybeBackendTask task = new MaybeBackendTask();
        task.execute();
    }
}
