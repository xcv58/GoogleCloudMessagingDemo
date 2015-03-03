package org.phone_lab.maybe.googlecloudmessaging.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import org.phone_lab.maybe.googlecloudmessaging.MainActivity;

/**
 * Created by xcv58 on 3/3/15.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Get message", Toast.LENGTH_SHORT).show();
        Bundle bundle = intent.getExtras();
        Log.d(MainActivity.TAG, "Get message: " + intent.getAction());
        for (String key : bundle.keySet()) {
            Log.d(MainActivity.TAG, key + ": " + bundle.get(key));
        }
    }
}
