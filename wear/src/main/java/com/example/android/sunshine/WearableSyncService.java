package com.example.android.sunshine;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by tiagooliveira95 on 09/02/18.
 */

public class WearableSyncService extends WearableListenerService {
    private static final String TAG = WearableListenerService.class.getSimpleName();
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "create");

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.d(TAG, "onDataChanged: ");

        // Loop through the events and send a message
        // to the node that created the data item.
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            if(uri.getPath().equals("/weather")) {


                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                if (dataMap.containsKey("high")) {
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("high", (int) dataMap.getDouble("high")).apply();
                    Log.d(TAG, "DATA: " + dataMap.getDouble("high"));
                }
                if (dataMap.containsKey("low")) {
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("low", (int) dataMap.getDouble("low")).apply();
                    Log.d(TAG, "DATA: " + dataMap.getDouble("low"));
                }

                if (dataMap.containsKey("icon")) {
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("icon", dataMap.getString("icon")).apply();
                    Log.d(TAG, "DATA: " + dataMap.getString("icon"));
                }
            }

            Intent intent = new Intent("weather_changed");
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

            // Get the node id from the host value of the URI
            String nodeId = uri.getHost();
            // Set the data of the message to be the bytes of the URI
            byte[] payload = uri.toString().getBytes();

            // Send the RPC
            Wearable.getMessageClient(getApplicationContext()).sendMessage(
                    nodeId,  DATA_ITEM_RECEIVED_PATH, payload);
        }

    }
}
