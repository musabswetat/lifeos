package com.mosaab.lifeos;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AlarmOptimizerPlugin.class);
        super.onCreate(savedInstanceState);

        // إعداد شريط الإشعارات عند فتح التطبيق
        applyStatusBar("#0B1120", false);
    }

    /**
     * تغيير لون شريط الإشعارات ولون أيقوناته.
     *
     * darkIcons = true  → أيقونات داكنة (لخلفية فاتحة)
     * darkIcons = false → أيقونات فاتحة (لخلفية داكنة)
     */
    public void applyStatusBar(String colorHex, boolean darkIcons) {
        try {
            Window window = getWindow();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                );

                window.clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                );

                window.setStatusBarColor(Color.parseColor(colorHex));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = window.getDecorView();
                int flags = decor.getSystemUiVisibility();

                if (darkIcons) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                decor.setSystemUiVisibility(flags);
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        if (this.bridge != null && this.bridge.getWebView() != null) {
            this.bridge.getWebView().evaluateJavascript(
                    "window.handleSystemBackButton();",
                    null
            );
        } else {
            super.onBackPressed();
        }
    }
}

@CapacitorPlugin(name = "AlarmOptimizer")
class AlarmOptimizerPlugin extends Plugin {

    @PluginMethod
    public void setStatusBar(PluginCall call) {
        String colorHex = call.getString("color", "#0B1120");
        boolean darkIcons = Boolean.TRUE.equals(
                call.getBoolean("darkIcons", false)
        );

        getActivity().runOnUiThread(() -> {
            try {
                Window window = getActivity().getWindow();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(
                            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    );

                    window.clearFlags(
                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    );

                    window.setStatusBarColor(Color.parseColor(colorHex));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View decor = window.getDecorView();
                    int flags = decor.getSystemUiVisibility();

                    if (darkIcons) {
                        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    } else {
                        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    }

                    decor.setSystemUiVisibility(flags);
                }

                call.resolve();

            } catch (Exception e) {
                call.reject(e.getMessage());
            }
        });
    }

    @PluginMethod
    public void checkStatus(PluginCall call) {
        Context context = getContext();
        JSObject ret = new JSObject();

        boolean isIgnoringBattery = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager)
                    context.getSystemService(Context.POWER_SERVICE);

            if (pm != null) {
                isIgnoringBattery = pm.isIgnoringBatteryOptimizations(
                        context.getPackageName()
                );
            }
        }

        ret.put("batteryIgnored", isIgnoringBattery);

        boolean canExactAlarm = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager am =
                    (android.app.AlarmManager)
                            context.getSystemService(Context.ALARM_SERVICE);

            if (am != null) {
                canExactAlarm = am.canScheduleExactAlarms();
            }
        }

        ret.put("canExactAlarm", canExactAlarm);
        call.resolve(ret);
    }

    @PluginMethod
    public void requestIgnoreBattery(PluginCall call) {
        Context context = getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent();
                PowerManager pm = (PowerManager)
                        context.getSystemService(Context.POWER_SERVICE);

                if (pm != null && !pm.isIgnoringBatteryOptimizations(
                        context.getPackageName()
                )) {
                    intent.setAction(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    );
                    intent.setData(Uri.parse(
                            "package:" + context.getPackageName()
                    ));
                } else {
                    intent.setAction(
                            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    );
                }

                getActivity().startActivity(intent);

            } catch (Exception e) {
                Intent fallback = new Intent(
                        Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                );
                getActivity().startActivity(fallback);
            }
        }

        call.resolve();
    }

    @PluginMethod
    public void requestExactAlarmPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Intent intent = new Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                );
                intent.setData(Uri.parse(
                        "package:" + getContext().getPackageName()
                ));
                getActivity().startActivity(intent);

            } catch (Exception e) {
                Intent intent = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                );
                intent.setData(Uri.parse(
                        "package:" + getContext().getPackageName()
                ));
                getActivity().startActivity(intent);
            }
        }

        call.resolve();
    }

    @PluginMethod
    public void openAutostartSettings(PluginCall call) {
        Context context = getContext();
        Intent intent = new Intent();

        String brand = Build.BRAND.toLowerCase();
        String manufacturer = Build.MANUFACTURER.toLowerCase();

        try {
            if (brand.contains("xiaomi")
                    || manufacturer.contains("xiaomi")
                    || brand.contains("redmi")
                    || brand.contains("poco")) {

                intent.setComponent(new ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                ));

            } else if (brand.contains("samsung")
                    || manufacturer.contains("samsung")) {

                intent.setComponent(new ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                ));

            } else if (brand.contains("huawei")
                    || brand.contains("honor")) {

                intent.setComponent(new ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"
                ));

            } else {
                intent.setAction(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                );
                intent.setData(Uri.parse(
                        "package:" + context.getPackageName()
                ));
            }

            getActivity().startActivity(intent);

        } catch (Exception e) {
            Intent fallback = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            );
            fallback.setData(Uri.parse(
                    "package:" + context.getPackageName()
            ));
            getActivity().startActivity(fallback);
        }

        call.resolve();
    }

    @PluginMethod
    public void openAppSettings(PluginCall call) {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        );
        intent.setData(Uri.parse(
                "package:" + getContext().getPackageName()
        ));

        getActivity().startActivity(intent);
        call.resolve();
    }
}