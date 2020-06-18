package com.vuducminh.nicefoodserver.services;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vuducminh.nicefoodserver.MainActivity;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.common.CommonAgr;

import java.util.Map;
import java.util.Random;

public class MyFCMServices extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> dataRecv = remoteMessage.getData();
        if(dataRecv != null) {
            if(dataRecv.get(CommonAgr.NOTI_TITLE).equals("New Order")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(CommonAgr.IS_OPEN_ACTIVITY_NEW_ORDER,true);
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(CommonAgr.NOTI_TITLE),
                        dataRecv.get(CommonAgr.NOTI_CONTENT),
                        intent);
            }
            else {
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(CommonAgr.NOTI_TITLE),
                        dataRecv.get(CommonAgr.NOTI_CONTENT),
                        null);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this,s,true,false);
    }
}
