package eu.legnica.iilo.numerki;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout refreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshLayout = findViewById(R.id.swipe_refresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshing(true);

        TooltipCompat.setTooltipText(findViewById(R.id.settings_button), getString(R.string.app_settings));
        TooltipCompat.setTooltipText(findViewById(R.id.help_button), getString(R.string.help));

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleWork(this);
    }

    public static void scheduleWork(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notify = preferences.getBoolean(context.getString(R.string.notify_key), false);
        int seconds = preferences.getInt(context.getString(R.string.check_time_key), -1);

        Log.d("omg", Boolean.toString(notify));

        if(notify && seconds >= 0) {
            Calendar currentDate = Calendar.getInstance();
            Calendar dueDate = Calendar.getInstance();
            dueDate.set(Calendar.HOUR_OF_DAY, seconds / 60);
            dueDate.set(Calendar.MINUTE, seconds % 60);
            dueDate.set(Calendar.SECOND, 0);

            // Muszę być co najmniej 30 sekund przed, aby powiadomienie zostało wysłane
            currentDate.add(Calendar.SECOND, 30);
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24);
            }
            currentDate.add(Calendar.SECOND, -30);

            long delay = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
            Log.d("omg", Long.toString(delay));

            // Wymagane jest połączenie z Internetem
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(FetchApiWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .build();
            WorkManager.getInstance(context).enqueueUniqueWork("number_notification", ExistingWorkPolicy.REPLACE, workRequest);
        } else {
            // Wyłączenie powiadomień
            WorkManager.getInstance(context).cancelUniqueWork("number_notification");
        }
    }

    void loadData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://2lo.legnica.eu")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);

        Call<ApiResponse> call = api.getNumbers();
        call.enqueue(new Callback<ApiResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call,@NonNull Response<ApiResponse> response) {
                if(response.body() != null && response.body().days.size() > 0) {
                    ApiResponse.Day day = response.body().days.get(0);

                    TextView date = findViewById(R.id.date);
                    TextView number1 = findViewById(R.id.number1);
                    TextView number2 = findViewById(R.id.number2);

                    date.setText(day.date);
                    number1.setText(day.numbers.get(0).toString());
                    number2.setText(day.numbers.get(1).toString());
                } else if(response.body().days.size() == 0) { // weekend
                    TextView date = findViewById(R.id.date);
                    date.setText("(weekend)");
                } else {
                    onFailure(call, null);
                }

                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call,@Nullable Throwable t) {
                refreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, getString(R.string.cannot_fetch_numbers), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    public void onSettingsClick(View button)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onHelpClick(View button)
    {
        @SuppressLint("InflateParams")
        View aboutView = getLayoutInflater().inflate(R.layout.help, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_help_24px);
        builder.setTitle("Pomoc i wskazówki");
        builder.setView(aboutView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create();
        builder.show();
    }
}
