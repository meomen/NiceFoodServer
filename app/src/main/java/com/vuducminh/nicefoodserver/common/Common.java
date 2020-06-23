package com.vuducminh.nicefoodserver.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;
import com.vuducminh.nicefoodserver.model.BestDealsModel;
import com.vuducminh.nicefoodserver.model.CategoryModel;
import com.vuducminh.nicefoodserver.model.FoodModel;
import com.vuducminh.nicefoodserver.model.MostPopularModel;
import com.vuducminh.nicefoodserver.model.OrderModel;
import com.vuducminh.nicefoodserver.model.ServerUserModel;
import com.vuducminh.nicefoodserver.model.TokenModel;
import com.vuducminh.nicefoodserver.R;

import java.util.ArrayList;
import java.util.List;

public class Common {
    public static ServerUserModel currentServerUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
    public static OrderModel currentOrdeSelected;
    public static BestDealsModel bestDealsSelected;
    public static MostPopularModel mostPopularSelected;

    public static void setSpanString(String welcome, String name, TextView tv_user) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        tv_user.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static void setSpanStringColor(String welcome, String name, TextView textView, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0: {
                return "Placed";
            }
            case 1: {
                return "Shipping";
            }
            case 2: {
                return "Shipped";
            }
            case -1: {
                return "Cancelled";
            }
            default:
                return "Unknown";
        }
    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        String NOTIFICATION_CHANNEL_ID = "minh_vu_nice_food_java";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Nice Food Java", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Nice Food Java");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_restaurant_menu_black_24dp));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }
    public static void updateToken(Context context, String newToken,boolean isServer,boolean isShipper) {
        if(Common.currentServerUser != null) {
            FirebaseDatabase.getInstance()
                    .getReference(CommonAgr.TOKEN_REF)
                    .child(Common.currentServerUser.getUid())
                    .setValue(new TokenModel(Common.currentServerUser.getPhone(), newToken, isServer,isShipper))
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    public static List<LatLng> decodePoly(String encode) {
        List poly = new ArrayList();
        int index = 0,len = encode.length();
        int lat = 0, lng = 0;
        while(index < len) {
            int b,shift=0,result=0;
            do{
                b = encode.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lat +=dlat;
            shift = 0;
            result = 0;
            do{
                b = encode.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while(b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double)lat/1E5)),
                    (((double)lng/1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);


        if(begin.latitude < end.latitude && begin.longitude < end.longitude) {
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        }
        else if(begin.latitude >= end.latitude && begin.longitude < end.longitude) {
            return (float)((90 - Math.toDegrees(Math.atan(lng/lat)))+90);
        }
        else if(begin.latitude >= end.latitude && begin.longitude >= end.longitude) {
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        }
        else if(begin.latitude < end.latitude && begin.longitude >= end.longitude) {
            return (float)((90 - Math.toDegrees(Math.atan(lng/lat)))+270);
        }
        return -1;

    }

    public static String createTopicOrder() {
        return new StringBuilder("/topics/")
                .append(Common.currentServerUser.getRestaurant())
                .append("_")
                .append("new_order")
                .toString();
    }

    public static String getNewsTopic() {
        return new StringBuilder("/topics/")
                .append(Common.currentServerUser.getRestaurant())
                .append("_")
                .append("news")
                .toString();
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String result = null;
        if(fileUri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(fileUri,null,null,null,null);
            try {
                if(cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

            }
            finally {
                cursor.close();
            }
        }
        if(result == null) {
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1)
                result = result.substring(cut+1);
        }
        return result;
    }

}
