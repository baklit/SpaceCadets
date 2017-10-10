package Project1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
	public static void main(String[] args) throws IOException{
		
		//Enable cookies in HTTP requests
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

		//Retrieve login details for logging in
		BufferedReader file = new BufferedReader(new FileReader("user.txt"));
		String username = file.readLine();
		String password = file.readLine();
		file.close();
		
		//Login to ECS system
		URL url = new URL("https://secure.ecs.soton.ac.uk/login/now/index.php");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		OutputStream output = connection.getOutputStream();
		output.write(("ecslogin_username="+username+"&ecslogin_password="+password).getBytes());
		output.flush();
		output.close();
		
		System.out.println("ECS Login response: " + connection.getResponseCode() + " " + connection.getResponseMessage());
		
		//Ask for ID to lookup
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter an ID");
		
		//Construct url with given ID
		url = new URL("https://secure.ecs.soton.ac.uk/people/" + in.readLine());
		
		//Create an HTTP connection
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		
		//Send the HTTP GET request
		DataOutputStream returnData = new DataOutputStream(connection.getOutputStream());
		returnData.close();
		
		//Fetch the response
		BufferedReader response = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String info = "Info not found!";
		
		String line;
		while((line = response.readLine()) != null){
			if (line.contains("<span itemprop='name'>") || line.contains("<span itemprop=\"name\"")){
				int beginIndex = line.indexOf("<span itemprop") + 22;
				int endIndex = line.indexOf("</span>", beginIndex);
				info = line.substring(beginIndex, endIndex);
				
				beginIndex = line.indexOf("class='role'>") + 13;
				endIndex = line.indexOf("</span>", beginIndex);
				info += "\n" + line.substring(beginIndex, endIndex);
				
				beginIndex = endIndex + 7;
				endIndex = line.indexOf("<br/>", beginIndex);
				info += line.substring(beginIndex, endIndex);
				
				break;
			}
		}
		
		System.out.println(info);
	}
}
