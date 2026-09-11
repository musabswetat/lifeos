package com.mosaab.lifeos;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
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

    /*
     * لا نغير الثابت الموجود عندك.
     */
    public static final String ALARM_CHANNEL_ID =
            "lifeos_ultra_alarm_v5";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        /*
         * تسجيل Plugin قبل super.onCreate
         */
        registerPlugin(AlarmOptimizerPlugin.class);

        super.onCreate(savedInstanceState);

        /*
         * إنشاء قناة التنبيهات Native
         */
        createAlarmNotificationChannel();

        /*
         * لون شريط الحالة الافتراضي
         */
        applyStatusBar("#0B1120", false);
    }


    /**
     * إنشاء قناة الإشعارات المهمة.
     *
     * القناة Native واحدة وثابتة.
     */
    public void createAlarmNotificationChannel() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        /*
         * ملف النغمة:
         * android/app/src/main/res/raw/marimba.mp3
         */
        Uri soundUri =
                Uri.parse(
                        ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" +
                        getPackageName() +
                        "/" +
                        R.raw.marimba
                );

        AudioAttributes audioAttributes =
                new AudioAttributes.Builder()
                        .setUsage(
                                AudioAttributes.USAGE_NOTIFICATION
                        )
                        .setContentType(
                                AudioAttributes.CONTENT_TYPE_SONIFICATION
                        )
                        .build();

        /*
         * فحص القناة الموجودة.
         */
        NotificationChannel existing =
                manager.getNotificationChannel(
                        ALARM_CHANNEL_ID
                );

        /*
         * إذا كانت القناة موجودة وبها نغمة marimba،
         * لا نلمسها.
         */
        if (existing != null) {

            Uri existingSound =
                    existing.getSound();

            if (soundUri.equals(existingSound)) {
                return;
            }

            /*
             * نحفظ إعدادات القناة الحالية حتى لا نغيّر
             * قوة التنبيه أو الاهتزاز أو الشارة أو
             * إعدادات شاشة القفل.
             */
            int importance =
                    existing.getImportance();

            boolean vibrationEnabled =
                    existing.shouldVibrate();

            long[] vibrationPattern =
                    existing.getVibrationPattern();

            boolean showBadge =
                    existing.canShowBadge();

            int lockscreenVisibility =
                    existing.getLockscreenVisibility();

            boolean showLights =
                    existing.shouldShowLights();

            int lightColor =
                    existing.getLightColor();

            String description =
                    existing.getDescription();

            /*
             * Android لا يسمح بتغيير صوت قناة موجودة.
             * لذلك نحذف القناة القديمة ونعيد إنشاءها
             * بنفس المعرّف وإعداداتها السابقة.
             */
            manager.deleteNotificationChannel(
                    ALARM_CHANNEL_ID
            );

            NotificationChannel channel =
                    new NotificationChannel(
                            ALARM_CHANNEL_ID,
                            "تنبيهات LifeOS المهمة",
                            importance
                    );

            channel.setDescription(
                    description != null
                            ? description
                            : "تنبيهات المواعيد والمنبهات المهمة"
            );

            /*
             * التغيير المطلوب فقط:
             * إضافة marimba كنغمة للإشعار.
             */
            channel.setSound(
                    soundUri,
                    audioAttributes
            );

            /*
             * إعادة إعدادات الاهتزاز السابقة.
             */
            channel.enableVibration(
                    vibrationEnabled
            );

            if (vibrationPattern != null) {
                channel.setVibrationPattern(
                        vibrationPattern
                );
            }

            /*
             * إعادة إعدادات الشارة.
             */
            channel.setShowBadge(
                    showBadge
            );

            /*
             * إعادة إعدادات شاشة القفل.
             */
            channel.setLockscreenVisibility(
                    lockscreenVisibility
            );

            /*
             * إعادة إعدادات الإضاءة.
             */
            if (showLights) {

                channel.enableLights(true);

                channel.setLightColor(
                        lightColor
                );

            } else {

                channel.enableLights(false);
            }

            manager.createNotificationChannel(
                    channel
            );

            return;
        }

        /*
         * إنشاء القناة لأول مرة.
         */
        NotificationChannel channel =
                new NotificationChannel(
                        ALARM_CHANNEL_ID,
                        "تنبيهات LifeOS المهمة",
                        NotificationManager.IMPORTANCE_HIGH
                );

        channel.setDescription(
                "تنبيهات المواعيد والمنبهات المهمة"
        );

        /*
         * نغمة marimba.
         */
        channel.setSound(
                soundUri,
                audioAttributes
        );

        channel.enableVibration(true);

        channel.setVibrationPattern(
                new long[]{
                        0,
                        500,
                        250,
                        500,
                        250,
                        800
                }
        );

        channel.setShowBadge(true);

        manager.createNotificationChannel(
                channel
        );
    }


    /**
     * تغيير لون شريط الحالة وأيقوناته.
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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            WindowInsetsController controller =
                    window.getInsetsController();

            if (controller != null) {

                int appearance =
                        darkIcons
                                ? WindowInsetsController
                                        .APPEARANCE_LIGHT_STATUS_BARS
                                : 0;

                controller.setSystemBarsAppearance(
                        appearance,
                        WindowInsetsController
                                .APPEARANCE_LIGHT_STATUS_BARS
                );
            }

        } else if (
                Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.M
        ) {

            View decorView =
                    window.getDecorView();

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


    /**
     * Plugin الخاص بالتطبيق.
     */
    @CapacitorPlugin(name = "AlarmOptimizer")
    public static class AlarmOptimizerPlugin
            extends Plugin {


        /**
         * تغيير Status Bar.
         */
        @PluginMethod
        public void setStatusBar(
                PluginCall call
        ) {

            String colorHex =
                    call.getString(
                            "color",
                            "#0B1120"
                    );

            boolean darkIcons =
                    call.getBoolean(
                            "darkIcons",
                            false
                    );


            if (getActivity() == null) {

                call.reject(
                        "النشاط غير متاح"
                );

                return;
            }


            getActivity().runOnUiThread(() -> {

                if (
                        getActivity()
                                instanceof MainActivity
                ) {

                    ((MainActivity)
                            getActivity())
                            .applyStatusBar(
                                    colorHex,
                                    darkIcons
                            );
                }
            });


            JSObject result =
                    new JSObject();

            result.put(
                    "success",
                    true
            );

            call.resolve(result);
        }


        /**
         * إنشاء قناة التنبيهات.
         */
        @PluginMethod
        public void createAlarmChannel(
                PluginCall call
        ) {

            if (
                    getActivity()
                            instanceof MainActivity
            ) {

                ((MainActivity)
                        getActivity())
                        .createAlarmNotificationChannel();
            }


            JSObject result =
                    new JSObject();

            result.put(
                    "success",
                    true
            );

            result.put(
                    "channelId",
                    ALARM_CHANNEL_ID
            );

            call.resolve(result);
        }


        /**
         * فحص حالة Android.
         */
        @PluginMethod
        public void checkStatus(
                PluginCall call
        ) {

            JSObject result =
                    new JSObject();

            result.put(
                    "api",
                    Build.VERSION.SDK_INT
            );

            result.put(
                    "success",
                    true
            );

            /*
             * فحص exact alarm
             */
            if (
                    Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.S
            ) {

                try {

                    android.app.AlarmManager alarmManager =
                            (android.app.AlarmManager)
                                    getContext()
                                            .getSystemService(
                                                    Context.ALARM_SERVICE
                                            );

                    result.put(
                            "exactAlarmAllowed",
                            alarmManager != null &&
                                    alarmManager.canScheduleExactAlarms()
                    );

                } catch (Exception e) {

                    result.put(
                            "exactAlarmAllowed",
                            false
                    );
                }

            } else {

                result.put(
                        "exactAlarmAllowed",
                        true
                );
            }


            /*
             * فحص Battery Optimization
             */
            try {

                android.os.PowerManager powerManager =
                        (android.os.PowerManager)
                                getContext()
                                        .getSystemService(
                                                Context.POWER_SERVICE
                                        );

                boolean ignoring =
                        Build.VERSION.SDK_INT < 23 ||
                        (
                                powerManager != null &&
                                powerManager.isIgnoringBatteryOptimizations(
                                        getContext().getPackageName()
                                )
                        );

                result.put(
                        "batteryOptimizationIgnored",
                        ignoring
                );

            } catch (Exception e) {

                result.put(
                        "batteryOptimizationIgnored",
                        false
                );
            }


            call.resolve(result);
        }


        /**
         * فتح إعدادات Battery Optimization.
         */
        @PluginMethod
        public void requestIgnoreBattery(
                PluginCall call
        ) {

            try {

                if (
                        Build.VERSION.SDK_INT <
                                Build.VERSION_CODES.M
                ) {

                    call.resolve();
                    return;
                }


                Intent intent =
                        new Intent(
                                Settings
                                        .ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        );

                intent.setData(
                        Uri.parse(
                                "package:" +
                                getContext()
                                        .getPackageName()
                        )
                );

                getContext()
                        .startActivity(intent);

                call.resolve();

            } catch (Exception e) {

                call.reject(
                        "تعذر فتح إعدادات تحسين البطارية",
                        e
                );
            }
        }


        /**
         * فتح إعدادات Exact Alarm.
         */
        @PluginMethod
        public void requestExactAlarmPermission(
                PluginCall call
        ) {

            try {

                if (
                        Build.VERSION.SDK_INT >=
                                Build.VERSION_CODES.S
                ) {

                    Intent intent =
                            new Intent(
                                    Settings
                                            .ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            );

                    intent.setData(
                            Uri.parse(
                                    "package:" +
                                    getContext()
                                            .getPackageName()
                            )
                    );

                    getContext()
                            .startActivity(intent);
                }

                call.resolve();

            } catch (Exception e) {

                call.reject(
                        "تعذر فتح إعدادات المنبهات الدقيقة",
                        e
                );
            }
        }


        /**
         * فتح إعدادات التطبيق.
         *
         * Android لا يملك صفحة Autostart موحدة
         * لجميع الشركات، لذلك نفتح صفحة التطبيق.
         */
        @PluginMethod
        public void openAutostartSettings(
                PluginCall call
        ) {

            openAppDetails(call);
        }


        /**
         * فتح إعدادات التطبيق.
         */
        @PluginMethod
        public void openAppSettings(
                PluginCall call
        ) {

            openAppDetails(call);
        }


        private void openAppDetails(
                PluginCall call
        ) {

            try {

                Intent intent =
                        new Intent(
                                Settings
                                        .ACTION_APPLICATION_DETAILS_SETTINGS
                        );

                intent.setData(
                        Uri.parse(
                                "package:" +
                                getContext()
                                        .getPackageName()
                        )
                );

                getContext()
                        .startActivity(intent);

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