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
import android.view.WindowManager;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

public class MainActivity extends BridgeActivity {

    /*
     * حالة شريط الحالة الحالية.
     * تُستخدم لإعادة تطبيقها إذا أعاد Capacitor أو Android ضبط الشريط.
     */
    private String currentStatusBarColor = "#0B1120";
    private boolean currentStatusBarDarkIcons = false;

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
         * ملف نغمة الإشعارات الموجود داخل:
         * res/raw/marimba.mp3
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
         *
         * إذا كانت موجودة وبها نغمة marimba بالفعل،
         * لا نلمسها إطلاقاً.
         */
        NotificationChannel existing =
                manager.getNotificationChannel(
                        ALARM_CHANNEL_ID
                );

        if (existing != null) {

            Uri existingSound = existing.getSound();

            if (soundUri.equals(existingSound)) {
                return;
            }

            /*
             * القناة القديمة لا تحتوي على marimba.
             *
             * نحفظ إعداداتها الحالية حتى لا نغيّر
             * قوة التنبيه أو الاهتزاز أو الشارة أو
             * ظهور التنبيه على شاشة القفل.
             */
            int importance = existing.getImportance();

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
             * Android لا يسمح بتغيير صوت القناة بعد إنشائها،
             * لذلك نحذف القناة القديمة ونعيد إنشاءها بنفس
             * المعرّف وبنفس مستوى الأهمية.
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
             * التغيير المطلوب فقط: صوت marimba.
             */
            channel.setSound(
                    soundUri,
                    audioAttributes
            );

            /*
             * إعادة إعدادات الاهتزاز كما كانت.
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
             * إعادة إعدادات الشارة والإضاءة وشاشة القفل.
             */
            channel.setShowBadge(
                    showBadge
            );

            channel.setLockscreenVisibility(
                    lockscreenVisibility
            );

            if (showLights) {
                channel.enableLights(true);
                channel.setLightColor(lightColor);
            } else {
                channel.enableLights(false);
            }

            manager.createNotificationChannel(
                    channel
            );

            return;
        }

        /*
         * إنشاء القناة لأول مرة مع النغمة.
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

        manager.createNotificationChannel(channel);
    }


    /**
     * تغيير لون شريط الحالة وأيقوناته.
     */
    public void applyStatusBar(
            String colorHex,
            boolean darkIcons
    ) {

        Window window = getWindow();

        /*
         * نجعل Android يرسم خلفية شريط الحالة بنفسه
         * بدل تركها شفافة أو تحت تأثير إعدادات الثيم.
         *
         * هذا خاص بشريط الحالة فقط ولا علاقة له بالتنبيهات.
         */
        window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        );

        window.addFlags(
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        );

        int parsedColor;

        try {

            parsedColor = Color.parseColor(colorHex);

        } catch (Exception e) {

            parsedColor = Color.parseColor("#0B1120");
            colorHex = "#0B1120";
        }

        currentStatusBarColor = colorHex;
        currentStatusBarDarkIcons = darkIcons;

        window.setStatusBarColor(parsedColor);

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


    /*
     * إعادة تثبيت مظهر شريط الحالة عند عودة النافذة إلى الواجهة.
     *
     * لا نلمس أي إعداد متعلق بقناة الإشعارات.
     */
    @Override
    public void onResume() {

        super.onResume();

        applyStatusBar(
                currentStatusBarColor,
                currentStatusBarDarkIcons
        );
   