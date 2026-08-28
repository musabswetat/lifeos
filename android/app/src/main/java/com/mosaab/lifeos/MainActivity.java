package com.mosaab.lifeos;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {}

package com.yourcompany.lifeos; // تأكد من اسم الحزمة الخاص بك

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LifeOS Alarms";
            String description = "قناة إشعارات وتنبيهات Life OS المباشرة";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("lifeos_alarms_channel", name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
