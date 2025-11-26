package com.example.tinythanks;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Bildirime tıklayınca Ekleme Sayfasına gitsin
        Intent i = new Intent(context, AddGratitudeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

        // Bildirimin Tasarımı
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "tiny_thanks_channel")
                .setSmallIcon(R.drawable.ic_nav_ideas) // Ampul ikonu (veya ic_launcher)
                .setContentTitle("TinyThanks Hatırlatıcı ✨")
                .setContentText("Bugün seni gülümseten bir şey oldu mu? Kaydetmeyi unutma!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Bildirimi Göster
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // İzin kontrolü (Android zorunlu kılıyor)
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(200, builder.build());
        }
    }
}