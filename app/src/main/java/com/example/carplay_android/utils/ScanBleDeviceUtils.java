package com.example.carplay_android.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.util.Iterator;
import java.util.List;

public class ScanBleDeviceUtils {
    private static List<BleDevice> resultList;

    public static void scanLeDevice(Context context) {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                CharSequence text = "Scan start";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                resultList = scanResultList;
                //Log.d("aaaaaaaaa", resultList.get(0).getName());
                for (int i = 0; i < resultList.size(); i++) {
                    String element = resultList.get(i).getName();
                    if (element == null) {
                        resultList.remove(i);
                        i--; // Adjust the index after removal
                    } else {
                        if (!element.contains("mpy")) { // Only show devices starting with mpy (Will later be changed to product name)
                            resultList.remove(i);
                            i--; // Adjust the index after removal
                        }
                    }
                }
                BroadcastUtils.sendBleDevices(resultList, "DeviceList", context);
            }
        });
    }

    public static List<BleDevice> getResultList() {
        return resultList;
    }
}
