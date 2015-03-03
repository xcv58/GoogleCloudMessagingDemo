package org.phone_lab.maybe.googlecloudmessaging.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by xcv58 on 3/3/15.
 */
public class GcmIntentService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
