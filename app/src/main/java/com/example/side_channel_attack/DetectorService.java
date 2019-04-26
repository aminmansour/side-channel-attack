package com.example.side_channel_attack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DetectorService extends Service {
    public DetectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SideChannelDetector cm = new SideChannelDetector(this);

        return super.onStartCommand(intent, flags, startId);
    }
}

