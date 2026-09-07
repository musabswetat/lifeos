package com.mosaab.lifeos;

import android.app.Activity;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AlarmOptimizerPlugin.class);

        super.onCreate(savedInstanceState);

        // اللون الافتراضي لشريط الإشعارات في الوضع الداكن
        applyStatusBar("#0B1120", false);
    }

    /**
     * تطبيق لون شريط الإشعارات وتحديد لون الأيقونات.
     *
     * darkIcons = true  : أيقونات سوداء للوضع الفاتح
     * darkIcons = false : أيقونات بيضاء للوضع الداكن
     */
    public void applyStatusBar(String colorHex, boolean darkIcons) {
        Window window = getWindow();

        try {
            window.setStatusBarColor(Color.parseColor(colorHex));
        } catch (Exception e) {
            window.setStatusBarColor(Color.parseColor("#0B1120"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController controller =
                        window.getInsetsController();

                if (controller != null) {
                    if (darkIcons) {
                        controller.setSystemBarsAppearance(
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        );
                    } else {
                        controller.setSystemBarsAppearance(
                                0,
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        );
                    }
                }
            } else {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();

                if (darkIcons) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                decorView.setSystemUiVisibility(flags);
            }
        }
    }

    @CapacitorPlugin(name = "AlarmOptimizer")
    public static class AlarmOptimizerPlugin extends Plugin {

        @PluginMethod
        public void setStatusBar(PluginCall call) {
            String colorHex =
                    call.getString("color", "#0B1120");

            boolean darkIcons =
                    call.getBoolean("darkIcons", false);

            Activity activity = getActivity();

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
        public void checkStatus(PluginCall call) {
            JSObject result = new JSObject();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result.put("api", Build.VERSION.SDK_INT);
            } else {
                result.put("api", 0);
            }

            result.put("success", true);
            call.resolve(result);
        }

        @PluginMethod
        public void requestIgnoreBattery(PluginCall call) {
            try {
                Intent intent = new Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                );

                intent.setData(
                        Uri.parse("package:" + getContext().getPackageName())
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
        public void requestExactAlarmPermission(PluginCall call) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent intent = new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    );

                    intent.setData(
                            Uri.parse("package:" + getContext().getPackageName())
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
        public void openAutostartSettings(PluginCall call) {
            try {
                Intent intent = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                );

                intent.setData(
                        Uri.parse("package:" + getContext().getPackageName())
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
        public void openAppSettings(PluginCall call) {
            try {
                Intent intent = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                );

                intent.setData(
                        Uri.parse("package:" + getContext().getPackageName())
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