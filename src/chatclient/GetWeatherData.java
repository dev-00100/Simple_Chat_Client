package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
/*
 * This class handles weather data collection.
 * It uses the weather API from OpenWeatherMap.
 */
public class GetWeatherData {
	
	private static GetWeatherData data;
	String API_KEY = "YOUR_API_KEY";
	String LOCATION = "Port of Spain, Trinidad and Tobago";
	String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + LOCATION + "&appid=" + API_KEY + "&units=metric"; 
	String weatherData; 
	
	private GetWeatherData() {}
	
	public static GetWeatherData getInstance() { //BotClient.getInstance()
		if(data == null) {
			data = new GetWeatherData();
		}
		return data; 
	}
	
	public Map<String, Object> jsonToMap(String str) {
		Map<String, Object> map = new Gson().fromJson(str, new TypeToken<HashMap<String, Object>>() {}.getType());
		return map;
	}
	
	public String getWeatherData() {
		try {
			StringBuilder result = new StringBuilder(); 
			URL url = new URL(urlString); 
			URLConnection conn = url.openConnection(); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line; 
			while((line = reader.readLine()) != null) {
				result.append(line);
			}
			reader.close();
			
			Map<String, Object> resMap = jsonToMap(result.toString());
			Map<String, Object> mainMap = jsonToMap(resMap.get("main").toString()); 
			Map<String, Object> windMap = jsonToMap(resMap.get("wind").toString());
			
			weatherData = "Temperature: " + mainMap.get("temp") + " C"
					 		+ "\nHumidity: " + mainMap.get("humidity") + " %" 
							+ "\nWind speed: " + windMap.get("speed") + " m/s";
			
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
		return weatherData;
	}
} 