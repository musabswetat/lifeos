package com.mosaab.lifeos;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

public class MainActivity extends BridgeActivity {

    public static final String ALARM_CHANNEL_ID =
            "lifeos_ultra_alarm_v5";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AlarmOptimizerPlugin.class);

        super.onCreate(savedInstanceState);

        createAlarmNotificationChannel();

        applyStatusBar("#0B1120", false);
    }

    /**
     * إنشاء قناة تنبيه عالية الأهمية.
     * تُنشأ مرة واحدة وتبقى محفوظة في النظام.
     */
    private void createAlarmNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) getSystemService(
                            Context.NOTIFICATION_SERVICE
                    );

            if (manager == null) return;

            NotificationChannel channel =
                    new NotificationChannel(
                            ALARM_CHANNEL_ID,
                            "تنبيهات LifeOS المهمة",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "تنبيهات المواعيد والمنبهات المهمة"
            );

            channel.enableVibration(true);
            channel.setVibrationPattern(
                    new long[]{0, 500, 250, 500, 250, 800}
            );

            channel.setBypassDnd(true);
            channel.setShowBadge(true);

            manager.createNotificationChannel(channel);
        }
    }

    /**
     * تطبيق لون شريط الإشعارات ولون الأيقونات.
     *
     * darkIcons = true  : أيقونات سوداء للوضع الفاتح
     * darkIcons = false : أيقونات بيضاء للوضع الداكن
     */
    public void applyStatusBar(
            String colorHex,
            boolean darkIcons
    ) {
        Window window = getWindow();

        try {
            window.setStatusBarColor(
                    Color.parseColor(colorHex)
            );
        } catch (Exception e) {
            window.setStatusBarColor(
                    Color.parseColor("#0B1120")
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController controller =
                        window.getInsetsController();

                if (controller != null) {
                    if (darkIcons) {
                        controller.setSystemBarsAppearance(
                                WindowInsetsController
                                        .APPEARANCE_LIGHT_STATUS_BARS,
                                WindowInsetsController
                                        .APPEARANCE_LIGHT_STATUS_BARS
                        );
                    } else {
                        controller.setSystemBarsAppearance(
                                0,
                                WindowInsetsController
                                        .APPEARANCE_LIGHT_STATUS_BARS
                        );
                    }
                }
            } else {
                View decorView = window.getDecorView();

                int flags =
                        decorView.getSystemUiVisibility();

                if (darkIcons) {
                    flags |=
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    flags &=
                            ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                decorView.setSystemUiVisibility(flags);
            }
        }
    }

    @CapacitorPlugin(name = "AlarmOptimizer")
    public static class AlarmOptimizerPlugin
            extends Plugin {

        @PluginMethod
        public void setStatusBar(PluginCall call) {
            String colorHex =
                    call.getString("color", "#0B1120");

            boolean darkIcons =
                    call.getBoolean("darkIcons", false);

            Activity activity = getActivity();

            if (activity == null) {
                call.reject("النشاط غير متاح");
                return;
            }

            activity.runOnUiThread(() -> {
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).applyStatusBar(
                            colorHex,
                            darkIcons
                    );
                }
            });

            JSObject result = new JSObject();
            result.put("success", true);
            call.resolve(result);
        }

        @PluginMethod
        public void createAlarmChannel(PluginCall call) {
            Activity activity = getActivity();

            if (activity instanceof MainActivity) {
                ((MainActivity) activity)
                        .createAlarmNotificationChannel();
            }

            JSObject result = new JSObject();
            result.put("success", true);
            result.put("channelId", ALARM_CHANNEL_ID);
            call.resolve(result);
        }

        @PluginMethod
        public void checkStatus(PluginCall call) {
            JSObject result = new JSObject();

            result.put("api", Build.VERSION.SDK_INT);
            result.put("success", true);

            call.resolve(result);
        }

        @PluginMethod
        public void requestIgnoreBattery(PluginCall call) {
            try {
                Intent intent = new Intent(
                        Settings
                                .ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                );

                intent.setData(
                        Uri.parse(
                                "package:" +
                                getContext().getPackageName()
                        )
                );

                getContext().startActivity(intent);
                call.resolve();

            } catch (Exception e) {
                call.reject(
                        "تعذر فتح إعدادات تحسين البطارية",
                        e
                );
            }
        }

        @PluginMethod
        public void requestExactAlarmPermission(
                PluginCall call
        ) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent intent = new Intent(
                            Settings
                                    .ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    );

                    intent.setData(
                            Uri.parse(
                                    "package:" +
                                    getContext().getPackageName()
                            )
                    );

                    getContext().startActivity(intent);
                }

                call.resolve();

            } catch (Exception e) {
                call.reject(
                        "تعذر فتح إعدادات المنبهات الدقيقة",
                        e
                );
            }
        }

        @PluginMethod
        public void openAutostartSettings(
                PluginCall call
        ) {
            try {
                Intent intent = new Intent(
                        Settings
                                .ACTION_APPLICATION_DETAILS_SETTINGS
                );

                intent.setData(
                        Uri.parse(
                                "package:" +
                                getContext().getPackageName()
                        )
                );

                getContext().startActivity(intent);
                call.resolve();

            } catch (Exception e) {
                call.reject(
                        "تعذر فتح إعدادات التشغيل التلقائي",
                        e
                );
            }
        }

        @PluginMethod
        public void openAppSettings(
                PluginCall call
        ) {
            try {
                Intent intent = new Intent(
                        Settings
                                .ACTION_APPLICATION_DETAILS_SETTINGS
                );

                intent.setData(
                        Uri.parse(
                                "package:" +
                                getContext().getPackageName()
                        )
                );

                getContext().startActivity(intent);
                call.resolve();

            } catch (Exception e) {
                call.reject(
                        "تعذر فتح إعدادات التطبيق",
                        e
                );
            }
        }
    }
}