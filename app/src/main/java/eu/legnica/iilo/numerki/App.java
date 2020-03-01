package eu.legnica.iilo.numerki;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;


public class App extends Application {

    public static final String NUMBER_CHANNEL_PUSH_ID = "channel1";

    @Override
    public void onCreate()
    {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NUMBER_CHANNEL_PUSH_ID,
                    "Szczęśliwy numerek",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Powiadomienie o szczęśliwym numerku");
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
