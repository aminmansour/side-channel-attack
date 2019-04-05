package com.example.side_channel_attack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.WindowManager;

public class ChatHeadService extends Service
        implements BackButtonAwareLinearLayout.BackButtonListener {

    private WindowManager mWindowManager;
    private BackButtonAwareLinearLayout mRootContainer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootContainer = (BackButtonAwareLinearLayout) inflater.inflate(
                R.layout.chat_head_container, null, false);
        mRootContainer.setBackButtonListener(this);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSPARENT);

        mWindowManager.addView(mRootContainer, layoutParams);
    }

    @Override
    public void onBackButtonPressed() {
        System.out.println("hio");
        mRootContainer.setBackButtonListener(null);
        mWindowManager.removeView(mRootContainer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRootContainer != null) mWindowManager.removeView(mRootContainer);
    }
}