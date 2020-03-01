package eu.legnica.iilo.numerki;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static eu.legnica.iilo.numerki.App.NUMBER_CHANNEL_PUSH_ID;

public class FetchApiWorker extends Worker {

    private NotificationManagerCompat notificationManager;

    public FetchApiWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        notificationManager = NotificationManagerCompat.from(context);
    }

    @NonNull
    @Override
    public Result doWork() {

        int number1;
        int number2;
        String date;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://2lo.legnica.eu")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);
        Call<ApiResponse> call = api.getNumbers();
        try {
            Response<ApiResponse> response = call.execute();
            if(response.body() != null && response.body().days.size() > 0) {
                ApiResponse.Day day = response.body().days.get(0);
                number1 = day.numbers.get(0);
                number2 = day.numbers.get(1);
                date = day.date;
            } else if (response.body().days.size() == 0) { // Prawdopodobnie jest weekend
                Log.d("omg", "Weekend");
                MainActivity.scheduleWork(getApplicationContext());
                return ListenableWorker.Result.success();
            } else {
                throw new RuntimeException("Incorrect response from the server");
            }
        } catch (Exception e) {
            return ListenableWorker.Result.retry();
        }

        sendNotificationIfExpected(number1, number2, date);
        // Ustaw od nowa worker na przyszły dzień
        MainActivity.scheduleWork(getApplicationContext());
        return ListenableWorker.Result.success();
    }

    private void sendNotificationIfExpected(int num1, int num2, String date) {
        Context context = getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userType = preferences.getString(context.getString(R.string.user_type_key), "unknown");
        Set<String> numbers = preferences.getStringSet(context.getString(R.string.notify_numbers_key), new HashSet<String>());

        if(userType.equals("teacher")) { // Nauczyciel dostaje powiadomienie zawsze
            sendNotification(num1, num2, date,false);
        } else if(numbers.contains(Integer.toString(num1)) || numbers.contains(Integer.toString(num2))) {
            sendNotification(num1, num2, date,true);
        }
    }

    private void sendNotification(int num1, int num2, String date, boolean userStudent) {
        String title = userStudent ? "Wylosowano Twój numerek!" : "Wylosowano numerek";
        String message = "" + num1 + " i " + num2 + " to numerki na dzień " + date;
        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorLogo);
        PendingIntent openAppIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), NUMBER_CHANNEL_PUSH_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(color)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(openAppIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }
}
