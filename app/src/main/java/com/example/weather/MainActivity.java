package com.example.weather;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

   // 4de6edbfa536f8f00e69a12c81c09380
    FusedLocationProviderClient fusedLocationProviderClient;
    String cityofuser;
    List<Address> addresses;
    ImageView conditionimage;
    LottieAnimationView lottieAnimationView;
    TextView city,temperature,min_temp,max_temp,humidity,sun_rise,sun_set,wind_speed,conditionmain,sea_level;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        city=findViewById(R.id.city);
        conditionimage=findViewById(R.id.conidtion_image);
        temperature=findViewById(R.id.temperature);
        min_temp=findViewById(R.id.temperature_min);
        max_temp=findViewById(R.id.temperature_max);
        humidity=findViewById(R.id.humidity);
        sun_rise=findViewById(R.id.sun_rise);
        sun_set=findViewById(R.id.sun_set);
        conditionmain=findViewById(R.id.conition);
        wind_speed=findViewById(R.id.wind_speed);
        sea_level=findViewById(R.id.sea_level);
        lottieAnimationView=findViewById(R.id.lottie);
        if (checkper()) {
            getuserlocation();
            searchcity();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 100);
        }

    }

    private void searchcity() {
        SearchView searchView=findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                city.setText(query);
                showweather(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    private void showweather(String cityofuser) {

        Class<Retrofit> apiinterface = null;
        com.example.weather.ApiInterface retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).
                baseUrl("https://api.openweathermap.org/data/2.5/").build().create(com.example.weather.ApiInterface.class);
        Call<WeatherApp>  call= retrofit.getWeatherData(cityofuser, "4de6edbfa536f8f00e69a12c81c09380", "metric");
        // Response<Weatherapp> response=call.execute();
       call.enqueue(new Callback<WeatherApp>() {
           @Override
           public void onResponse(Call<WeatherApp> call, Response<WeatherApp> response) {
               WeatherApp responsebody = response.body();
               if(response.isSuccessful() && responsebody!=null) {
                   double temp = responsebody.getMain().getTemp();
                   double temp_min = responsebody.getMain().getTemp_min();
                   double temp_max = responsebody.getMain().getTemp_max();
                  double humidit = responsebody.getMain().getHumidity();
                  double wind = responsebody.getWind().getSpeed();
                   double sunrise = responsebody.getSys().getSunrise()*1000L;
                   double sunset=responsebody.getSys().getSunset()*1000L;
                   int sealevel = responsebody.getMain().getSea_level();
                   Date sunrisedate=new Date((long) sunrise);
                   Date sunsetdate=new Date((long) sunset);
                   SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
                   String formattedsunrisetime=sdf.format(sunrisedate);
                   String formattedsunsettime=sdf.format(sunsetdate);
                   String conditi=responsebody.getWeather().get(0).getMain();

                   String tem = String.format(Locale.getDefault(),"%.2f", temp) + "°C";
                   String min_tem = String.format(Locale.getDefault(),"%.2f", temp_min) + "°C";
                   String max_tem = String.format(Locale.getDefault(),"%.2f", temp_max) + "°C";
                  String humi = String.format(Locale.getDefault(),"%.2f", humidit)+"%";
                 String wind_spe = String.format(Locale.getDefault(),"%.2f", wind)+"m/s";
                 String sea=String.format(Locale.getDefault(),"%d",sealevel)+"hPa";



                   temperature.setText(tem);
                   min_temp.setText(min_tem);
                   max_temp.setText(max_tem);
                  humidity.setText(humi);
                   wind_speed.setText(wind_spe);
                    sun_rise.setText(formattedsunrisetime);
                    sun_set.setText(formattedsunsettime);
                    sea_level.setText(sea);
                    conditionmain.setText(conditi);
                    changebackgroundtoweathercondition(conditi);
               }
               else {
                   String citywhenunknown=addresses.get(0).getPostalCode();
                  // Toast.makeText(MainActivity.this, citywhenunknown, Toast.LENGTH_SHORT).show();
                  showweather(citywhenunknown);
               }

           }

           @Override
           public void onFailure(Call<WeatherApp> call, Throwable t) {
               Toast.makeText(MainActivity.this, "Cannot find the location", Toast.LENGTH_LONG).show();
           }
       });

    }

    private void changebackgroundtoweathercondition(String condition) {
        if(Objects.equals(condition, "Clear Sky") || Objects.equals(condition, "Sunny") || Objects.equals(condition, "Clear"))
        {

            lottieAnimationView.setAnimation(R.raw.sunny);
        } else if (Objects.equals(condition, "Partly Clouds") || Objects.equals(condition, "Clouds") || Objects.equals(condition, "Overcast")
           || Objects.equals(condition, "Mist") || Objects.equals(condition, "Foggy")
        ) {
         lottieAnimationView.setAnimation(R.raw.cloudy);
        }
        else if(Objects.equals(condition, "Light Rain") || Objects.equals(condition, "Drizzle") || Objects.equals(condition, "Moderate Rain")
                || Objects.equals(condition, "Heavy Rain") || Objects.equals(condition, "Showers"))
        {
         lottieAnimationView.setAnimation(R.raw.rainy);
        }
        else if(Objects.equals(condition, "Light Snow") || Objects.equals(condition, "Moderate Snow") || Objects.equals(condition, "Heavy Snow")
                || Objects.equals(condition, "Blizzard"))
        {
            lottieAnimationView.setAnimation(R.raw.snow);
        }
        conditionimage.setImageResource(R.drawable.condition);
        lottieAnimationView.playAnimation();
    }

    private void getuserlocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
               if(location!=null)
               {
                   Geocoder geocoder=new Geocoder(MainActivity.this, Locale.getDefault());
                   try {
                       addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
                   cityofuser=addresses.get(0).getLocality();
                  // cityofuser=cityofuser.toLowerCase();
                   city.setText(cityofuser);
                   showweather(cityofuser);
               }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100 && grantResults.length>0)
        {
            int re1=grantResults[0];
            int re2=grantResults[1];
            boolean res1=re1==PackageManager.PERMISSION_GRANTED;
            boolean res2=re2==PackageManager.PERMISSION_GRANTED;
            if(res1 && res2)
            {
               getuserlocation();
            }
        }
    }

    private boolean checkper() {
       int result1=ActivityCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION);
       int result2=ActivityCompat.checkSelfPermission(this,ACCESS_COARSE_LOCATION);
       return result1== PackageManager.PERMISSION_GRANTED && result2==PackageManager.PERMISSION_GRANTED;
    }
}