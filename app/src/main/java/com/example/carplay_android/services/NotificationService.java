package com.example.carplay_android.services;

import static com.example.carplay_android.javabeans.JavaBeanFilters.*;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;
import android.text.Spanned;

import java.io.IOException;
import java.util.Set;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.carplay_android.utils.BroadcastUtils;
import com.example.carplay_android.utils.DirectionUtils;
import com.example.carplay_android.utils.DiscordWebhook;
import com.example.carplay_android.utils.WebhookTest;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class NotificationService extends NotificationListenerService {
    private static final String TAG = NotificationService.class.getSimpleName();
    private BleService.BleBinder controlBle;
    private ServiceConnToBle serviceConnToBle;
    private Boolean deviceStatus = false;
    private Timer timerSendNotification;
    private Boolean ifSendNotification = false;
    private static String[] informationMessageSentLastTime = new String[7];

    public NotificationService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        init();

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        //Bundle extras = sbn.getNotification().extras;
        //for (String key: extras.keySet())
        //{
        //    Log.d ("myApplication", key + " is a key in the bundle");
        //}
        // Get all keys from the extras bundle
        //Set<String> allKeys = extras.keySet();

        // Iterate through the keys and do something with each key
        //for (String key : allKeys) {
            //Object value = extras.get(key);
            // Do something with the key and value
            // For example, you can log them or process them as needed
            //Log.d("NotificationKeys", "Key: " + key + ", Value: " + value);

        //}
        //Log.d("NotificationKeys", "  ");
        //Log.d("NotificationKeys", "  ");
        if (sbn != null && isGMapNotification(sbn)) {
            handleGMapNotification(sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.d("Notification", "removed");
    }

    public static void cleanLastTimeSent() {
        Arrays.fill(informationMessageSentLastTime, "");
    }


    private boolean isGMapNotification(StatusBarNotification sbn) {
        if (!sbn.isOngoing() || !sbn.getPackageName().contains("com.google.android.apps.maps")) {
            return false;
        }
        return (sbn.getId() == 1);
    }


    private void handleGMapNotification(StatusBarNotification sbn) {
        //Bundle bundle = sbn.getNotification().extras;
        //Notification bundle = sbn.getNotification();
        //showToast("Notification: " + notification);
        String[] informationMessage = new String[7];
        //String pack = sbn.getPackageName();
        //String ticker = "";
        //if (sbn.getNotification().tickerText != null) {
        //    ticker = (String) sbn.getNotification().tickerText;
        //}

        // Access the notification content
        Bundle extras = sbn.getNotification().extras;
        //for (String key: extras.keySet())
        //{
        //    Log.d ("myApplication", key + " is a key in the bundle");
        //}
        // Get all keys from the extras bundle
        Set<String> allKeys = extras.keySet();

        // Iterate through the keys and do something with each key
        //for (String key : allKeys) {
        //    Object value = extras.get(key);
        //    // Do something with the key and value
        //    // For example, you can log them or process them as needed
        //    //Log.d("NotificationKeys", "Key: " + key + ", Value: " + value);
        //}
        CharSequence titleCharSeq = extras.getCharSequence("android.title");
        String title = (titleCharSeq instanceof Spanned) ?
                ((Spanned) titleCharSeq).toString() : (titleCharSeq != null ? titleCharSeq.toString() : null);
        //
        CharSequence textCharSeq = extras.getCharSequence("android.text");
        String text = (textCharSeq instanceof Spanned) ?
                ((Spanned) textCharSeq).toString() : (textCharSeq != null ? textCharSeq.toString() : null);
        CharSequence subtextCharSeq = extras.getCharSequence("android.subText");
        String subtext = (subtextCharSeq instanceof Spanned) ?
                ((Spanned) subtextCharSeq).toString() : (subtextCharSeq != null ? subtextCharSeq.toString() : null);
        //CharSequence infotextCharSeq = extras.getCharSequence("android.infoText");
        //String infotext = (infotextCharSeq instanceof Spanned) ?
        //        ((Spanned) infotextCharSeq).toString() : (infotextCharSeq != null ? infotextCharSeq.toString() : null);
        //
        //Log.i("Notification", "Package: " + pack + "\nTicker: " + ticker + "\nTitle: " + title + "\nText: " + text + "\nsubText: " + subtext + "\ninfoText: " + infotext);
        //String string = bundle.getString(Notification.EXTRA_TEXT);
        Log.i("Notification", "\nTitle: " + title + "\nText: " + text + "\nsubText: " + subtext);
        //Log.i("Notification", "Text: " + string);
        //showToast(bundle.getString(Notification.EXTRA_TEXT));
        if (subtext != null) {
            try {
                String string = subtext;
                //String[] strings = string.split("-");//destination
                String[] strings = string.split(" · ");//destination
                //Log.d("Notification","String 0" + strings[0] + "String 1" + strings[1] + "String 2" + strings[2]);
                //string = subtext;
                //strings = string.split(" · ");

                //informationMessage[0] = strings[0].trim();



                //strings = strings[1].trim().split(" ");
                if (strings.length == 3) {
                    //strings[0] = strings[0] + " ";//concat a " "
                    //strings[0] = strings[0] + strings[1];//if use 12 hour type, then concat the time and AM/PM
                    informationMessage[1] = strings[2];// get the ETA
                    informationMessage[4] = strings[0];//ETA in Minutes .trim()
                    informationMessage[5] = strings[1];//Distance .trim()
                }
                DateTimeFormatter dtf;// Current time  string = title;
                if (informationMessage[1].contains("AM") || informationMessage[1].contains("PM")) {
                    //dtf = DateTimeFormatter.ofPattern("HH:mm a");
                    dtf = DateTimeFormatter.ofPattern("HH:mm");
                } else {
                    dtf = DateTimeFormatter.ofPattern("HH:mm");
                }
                LocalDateTime CurrentTime= LocalDateTime.now();
                informationMessage[0] = dtf.format(CurrentTime); // Current time  string = title;

                if (string != null) {
                    strings = string.split("-");
                    if (strings.length == 2) {
                        informationMessage[2] = strings[1].trim();//Distance to next direction
                        informationMessage[3] = strings[0].trim();//Direction to somewhere
                    } else if (strings.length == 1) {
                        informationMessage[2] = strings[0].trim();//Direction to somewhere
                        informationMessage[3] = "N/A";
                        //informationMessage[3] = text; // Street to go to
                    }
                } else {
                    informationMessage[2] = "N/A";//Distance to next direction
                    informationMessage[3] = "N/A";//Direction to somewhere
                }
                //Log.d("Device Name", android.os.Build.MODEL);
                //DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/1176465533487546398/S3xpoNTpLowaLuRy5UldcEvKC6tV9iUva1cWWtwxMiY3FfGgOLm3tGnojFyv7tOrBROm");
                //webhook.setContent("Package: " + pack + "\nTicker: " + ticker + "\nTitle: " + title + "\nText: " + text + "\nsubText: " + subtext + "\ninfoText: " + infotext);
                //webhook.setAvatarUrl("https://your.awesome/image.png");
                //webhook.setUsername("Maps Data " + android.os.Build.MODEL);
                //webhook.setTts(true);
                //webhook.addEmbed(new DiscordWebhook.EmbedObject()
                //        .setTitle("Title")
                //        .setDescription("This is a description")
                //        .addField("1st Field", "Inline", true)
                //        .addField("2nd Field", "Inline", true)
                //        .addField("3rd Field", "No-Inline", false)
                //        .setThumbnail("https://kryptongta.com/images/kryptonlogo.png")
                //        .setFooter("Footer text", "https://kryptongta.com/images/kryptonlogodark.png")
                //        .setImage("https://kryptongta.com/images/kryptontitle2.png")
                //        .setAuthor("Author Name", "https://kryptongta.com", "https://kryptongta.com/images/kryptonlogowide.png")
                //        .setUrl("https://kryptongta.com"));
                //webhook.addEmbed(new DiscordWebhook.EmbedObject()
                //        .setDescription("Just another added embed object!"));
                //webhook.execute(); //Handle exception
                //JSONObject WebhookData = new JSONObject();
                //WebhookData.put("username", "Maps Data " + android.os.Build.MODEL);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) sbn.getNotification().getLargeIcon().loadDrawable(getApplicationContext());

                informationMessage[6] = String.valueOf(DirectionUtils.getDirectionNumber(DirectionUtils.getDirectionByComparing(bitmapDrawable.getBitmap())));
                Log.d("Debug cuz stupid issue", Arrays.toString(informationMessage));
            } catch (NullPointerException e) {
                showToast("Fuckkkkkkkk kan destination niet lezen :(");
                //showToast(string);
                //Log.d(TAG, "Something went wrong i guess", e);
                //Log.d(TAG, string, e);
            }


            if (deviceStatus) {
                if (informationMessage[0] != null && !informationMessage[0].equals(informationMessageSentLastTime[0])) {//destination
                    controlBle.sendDestination(informationMessage[0]);
                    informationMessageSentLastTime[0] = informationMessage[0];
                }
                if (informationMessage[1] != null && !Objects.equals(informationMessage[1], informationMessageSentLastTime[1])) {//ETA
                    controlBle.sendEta(informationMessage[1]);
                    informationMessageSentLastTime[1] = informationMessage[1];
                }
                if (informationMessage[2] != null && !Objects.equals(informationMessage[2], informationMessageSentLastTime[2])) {//direction

                    if (informationMessage[2].length() > 20) {
                        controlBle.sendDirection(informationMessage[2].substring(0, 20) + "..");
                    } else {
                        controlBle.sendDirection(informationMessage[2]);
                    }

                    informationMessageSentLastTime[2] = informationMessage[2];
                }
                if (informationMessage[3] != null && !Objects.equals(informationMessage[3], informationMessageSentLastTime[3])) {

                    controlBle.sendDirectionDistances(informationMessage[3]);

                    informationMessageSentLastTime[3] = informationMessage[3];
                }
                if (informationMessage[4] != null && !Objects.equals(informationMessage[4], informationMessageSentLastTime[4])) {

                    controlBle.sendEtaInMinutes(informationMessage[4]);

                    informationMessageSentLastTime[4] = informationMessage[4];
                }
                if (informationMessage[5] != null && !Objects.equals(informationMessage[5], informationMessageSentLastTime[5])) {

                    controlBle.sendDistance(informationMessage[5]);

                    informationMessageSentLastTime[5] = informationMessage[5];
                }
                if (informationMessage[6] != null && !Objects.equals(informationMessage[6], informationMessageSentLastTime[6])) {

                    controlBle.sendDirectionPrecise(informationMessage[6]);

                    informationMessageSentLastTime[6] = informationMessage[6];
                }
                Log.d("d", "done");
                informationMessageSentLastTime = informationMessage;
                ifSendNotification = false;//reduce the frequency of sending messages
                //why not just check if two messages are the same,  why still need to send same message every half second:
                //because if the device lost connection before, we have to keep send message to it to keep it does not
                //receive any wrong message.
            }
        }
    }


    private void init() {
        Arrays.fill(informationMessageSentLastTime, "");

        initService();
        initBroadcastReceiver();
        setSendNotificationTimer();
        BroadcastUtils.sendStatus(true, getFILTER_NOTIFICATION_STATUS(), getApplicationContext());
        DirectionUtils.loadSamplesFromAsserts(getApplicationContext());
    }

    private void initService() {
        serviceConnToBle = new ServiceConnToBle();
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, serviceConnToBle, BIND_AUTO_CREATE);
        startService(intent);//bind the service
    }

    private void initBroadcastReceiver() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        ReceiverForDeviceStatus receiverForDeviceStatus = new ReceiverForDeviceStatus();
        IntentFilter intentFilterForDeviceStatus = new IntentFilter(getFILTER_DEVICE_STATUS());
        localBroadcastManager.registerReceiver(receiverForDeviceStatus, intentFilterForDeviceStatus);
    }

    private class ServiceConnToBle implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            controlBle = (BleService.BleBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class ReceiverForDeviceStatus extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            deviceStatus = intent.getBooleanExtra(getFILTER_DEVICE_STATUS(), false);
        }
    }

    public void setSendNotificationTimer() {
        if (timerSendNotification == null) {
            timerSendNotification = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    ifSendNotification = true;
                }
            };
            timerSendNotification.schedule(timerTask, 10, 2000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BroadcastUtils.sendStatus(false, getFILTER_NOTIFICATION_STATUS(), getApplicationContext());
        unbindService(serviceConnToBle);
    }
    private void showToast(String message) {
        // Display a toast message on the screen
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public static String getStringBetweenTwoCharacters(String input, String to, String from)
    {
        return input.substring(input.indexOf(to)+1, input.lastIndexOf(from));
    }
}