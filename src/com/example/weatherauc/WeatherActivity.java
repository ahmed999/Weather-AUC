package com.example.weatherauc;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends ActionBarActivity {

	private class WeatherFragment extends Fragment {
	    Typeface weatherFont;
	     
	    TextView cityField;
	    TextView updatedField;
	    TextView detailsField;
	    TextView currentTemperatureField;
	    TextView weatherIcon;
	    TextView developerName; 
	    Handler handler;
	 
	    public WeatherFragment(){   
	        handler = new Handler();
	    }
	 
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
	        cityField = (TextView)rootView.findViewById(R.id.city_field);
	        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
	        detailsField = (TextView)rootView.findViewById(R.id.details_field);
	        currentTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);
	        weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);
	        developerName = (TextView)rootView.findViewById(R.id.developed_by_field);
	        weatherIcon.setTypeface(weatherFont);
	        return rootView; 
	    }
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);  
	        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");     
	        updateWeatherData(new CityPreference(getActivity()).getCity());
	    }

		private void updateWeatherData(final String city) {
			new Thread(){
		        public void run(){
		            final JSONObject json = WeatherRemoteFetch.getWeatherUpdates(getActivity(), city);
		            if(json == null){
		                handler.post(new Runnable(){
		                    public void run(){
		                        Toast.makeText(getActivity(), 
		                                getActivity().getString(R.string.place_not_found), 
		                                Toast.LENGTH_LONG).show(); 
		                    }
		                });
		            } else {
		                handler.post(new Runnable(){
		                    public void run(){
		                        renderWeather(json);
		                    }
		                });
		            }               
		        }
		    }.start();
			
		}
		private void renderWeather(JSONObject json) {
			try {
		        cityField.setText(json.getString("name").toUpperCase(Locale.US) + 
		                ", " + 
		                json.getJSONObject("sys").getString("country"));
		         
		        JSONObject details = json.getJSONArray("weather").getJSONObject(0);
		        JSONObject main = json.getJSONObject("main");
		        detailsField.setText(
		                details.getString("description").toUpperCase(Locale.US) +
		                "\n" + "Humidity: " + main.getString("humidity") + "%" +
		                "\n" + "Pressure: " + main.getString("pressure") + " hPa");
		         
		        currentTemperatureField.setText(
		                    String.format("%.2f", main.getDouble("temp"))+ " ℃");
		        
		        DateFormat df = DateFormat.getDateTimeInstance();
		        String updatedOn = df.format(new Date(json.getLong("dt")*1000));
		        updatedField.setText("Last update: " + updatedOn);
		 
		        setWeatherIcon(details.getInt("id"),
		                json.getJSONObject("sys").getLong("sunrise") * 1000,
		                json.getJSONObject("sys").getLong("sunset") * 1000);
		         
		        developerName.setText("Developed By: " + this.getString(R.string.developer_name) + "\n");
			}catch(Exception e){
		        Log.e("SimpleWeather", "One or more fields not found in the JSON data");
		    }
		}
		private void setWeatherIcon(int actualId, long sunrise, long sunset){
		    int id = actualId / 100;
		    String icon = "";
		    if(actualId == 800){
		        long currentTime = new Date().getTime();
		        if(currentTime>=sunrise && currentTime<sunset) {
		            icon = getActivity().getString(R.string.weather_sunny);
		        } else {
		            icon = getActivity().getString(R.string.weather_clear_night);
		        }
		    } else {
		        switch(id) {
		        case 2 : icon = getActivity().getString(R.string.weather_thunder);
		                 break;         
		        case 3 : icon = getActivity().getString(R.string.weather_drizzle);
		                 break;     
		        case 7 : icon = getActivity().getString(R.string.weather_foggy);
		                 break;
		        case 8 : icon = getActivity().getString(R.string.weather_cloudy);
		                 break;
		        case 6 : icon = getActivity().getString(R.string.weather_snowy);
		                 break;
		        case 5 : icon = getActivity().getString(R.string.weather_rainy);
		                 break;
		        }
		    }
		    weatherIcon.setText(icon);
		}
		public void changeCity(String city){
		    updateWeatherData(city);
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_weather);
	 
	    if (savedInstanceState == null) {
	        getSupportFragmentManager().beginTransaction()
	                .add(R.id.container, new WeatherFragment())
	                .commit();
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if(item.getItemId() == R.id.change_city){
	        showInputDialog();
	    }
	    else
	    	if(item.getItemId() == R.id.tutorial_link){
	    		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.tutorial_link)));
	    		startActivity(browserIntent);
	    	}
	    return false;
	}
	private void showInputDialog(){
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Change city");
	    final EditText input = new EditText(this);
	    input.setInputType(InputType.TYPE_CLASS_TEXT);
	    builder.setView(input);
	    builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            changeCity(input.getText().toString());
	        }
	    });
	    builder.show();
	}
	 
	public void changeCity(String city){
	    WeatherFragment wf = (WeatherFragment)getSupportFragmentManager()
	                            .findFragmentById(R.id.container);
	    wf.changeCity(city);
	    new CityPreference(this).setCity(city);
	}

}
