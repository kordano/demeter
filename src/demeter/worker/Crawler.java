package demeter.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Crawler {
	
	/** Encodes the consumer key and secret to create the basic authorization key
	 * 
	 * @param consumerKey
	 * @param consumerSecret
	 * @return
	 */
	private static String encodeKeys(String consumerKey, String consumerSecret) {
		try {
			String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
			String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");
			
			String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
			byte[] encodedBytes = Base64.encodeBase64(fullKey.getBytes());
			return new String(encodedBytes);  
			}
		catch (UnsupportedEncodingException e) {
			return new String();
		}
	}
	
	/** Writes a request to a connection
	 * 
	 * @param connection
	 * @param textBody
	 * @return
	 */
	private static boolean writeRequest(HttpsURLConnection connection, String textBody) {
		try {
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			wr.write(textBody);
			wr.flush();
			wr.close();
				
			return true;
		}
		catch (IOException e) { return false; }
	}
		
	/** Reads a response for a given connection and returns it as a string.
	 * 
	 * @param connection
	 * @return
	 */
	private static String readResponse(HttpsURLConnection connection) {
		try {
			StringBuilder str = new StringBuilder();
				
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			while((line = br.readLine()) != null) {
				str.append(line + System.getProperty("line.separator"));
			}
			return str.toString();
		}
		catch (IOException e) { return new String(); }
	}

	
	/** Constructs the request for requesting a bearer token and returns that token as a string
	 * 
	 * @param endPointUrl
	 * @return
	 * @throws IOException
	 */
	private static String requestBearerToken(String endPointUrl) throws IOException {
		HttpsURLConnection connection = null;
		String encodedCredentials = encodeKeys("RfwfMlqMXqWnIofQ8QjU5TpSX","tXF0cJM0ltTyMw1363cNWAkflbgzg0LBeFrutFer7E9ksSZaJz");
			
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "News-Crawler");
			connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"); 
			connection.setRequestProperty("Content-Length", "29");
			connection.setUseCaches(false);
				
			writeRequest(connection, "grant_type=client_credentials");
				
			// Parse the JSON response into a JSON mapped object to fetch fields from.
			JSONObject obj = (JSONObject)JSONValue.parse(readResponse(connection));
				
			if (obj != null) {
				String tokenType = (String)obj.get("token_type");
				String token = (String)obj.get("access_token");
				
			
				return ((tokenType.equals("bearer")) && (token != null)) ? token : "";
			}
			return new String();
		}
		catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * Search twitter with given query
	 * @param query simple String
	 * @return
	 * @throws IOException
	 */
	public static JSONObject searchTwitter(String query) throws IOException {
		HttpsURLConnection connection = null;
		String bearerToken = requestBearerToken("https://api.twitter.com/oauth2/token");
		String endPointUrl = "https://api.twitter.com/1.1/search/tweets.json?q=" + query + "&result_type=mixed&count=100";
					
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "News-Crawler");
			connection.setRequestProperty("Authorization", "bearer " + bearerToken);
			connection.setUseCaches(false);
				
			JSONObject obj = (JSONObject)JSONValue.parse(readResponse(connection));
			// Parse the JSON response into a JSON mapped object to fetch fields from.
			
			return obj;
		}
		catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * Fetches the timeline for a given user
	 * @param username twitter screen_name
	 * @return list of tweets
	 * @throws IOException
	 */
	public static JSONArray fetchTimelineTweet(String username) throws IOException {
		HttpsURLConnection connection = null;
		String bearerToken = requestBearerToken("https://api.twitter.com/oauth2/token");
		String endPointUrl = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + username + "&count=200";
					
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "News-Crawler");
			connection.setRequestProperty("Authorization", "bearer " + bearerToken);
			connection.setUseCaches(false);
				
			// Parse the JSON response into a JSON mapped object to fetch fields from.
			JSONArray obj = (JSONArray)JSONValue.parse(readResponse(connection));
			return obj;
		}
		catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * Get reteets for a given tweet
	 * @param id tweet-id
	 * @return list of tweets
	 * @throws IOException
	 */
	private static JSONArray fetchRetweets(Long id) throws IOException {
		HttpsURLConnection connection = null;
		String bearerToken = requestBearerToken("https://api.twitter.com/oauth2/token");
		String endPointUrl = "https://api.twitter.com/1.1/statuses/retweets/"+ id +".json";
					
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "News-Crawler");
			connection.setRequestProperty("Authorization", "bearer " + bearerToken);
			connection.setUseCaches(false);
			
				
			JSONArray tweets = (JSONArray)JSONValue.parse(readResponse(connection));
			return tweets;
		}
		catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * Find all tweets in timeline which contains a given string
	 * @param timeline Array of tweets from a user
	 * @param query String to be searched
	 * @return first tweet which contains the string
	 */
	public static JSONObject findTweet(JSONArray timeline, String query) {
		for(Object o: timeline) {
			JSONObject tweet = (JSONObject)o;
			if(((String)tweet.get("text")).contains(query)) {
				return tweet;
			} 
		}
		return new JSONObject();
	}

	
	// Just playing around a little bit
//	public static void main(String[] args) {
//		String twitterAccount = "FAZ_Topnews";
//		String query = "Netzneutralität:%20Was%20eine%20Überholspur%20im%20Internet%20für%20uns%20bedeutet";
//		String query2 = "Macht%20der%20Maschinen";
//		String query3 = "Deutsche%20Bank%20Elephant";
//
//		
//		try {
//			JSONArray fazTimeline = fetchTimelineTweet(twitterAccount);
//			
//			JSONObject result = findTweet(fazTimeline, "Netzneutralität");
//			System.out.println(result.get("retweet_count"));
//			
//			
//			JSONObject results = searchTwitter(query3);
//			
//			JSONArray tweets = (JSONArray)results.get("statuses");
//			for(Object t: tweets) {
//				JSONObject tweet = (JSONObject)t;
//				System.out.println(((JSONObject)tweet.get("user")).get("screen_name") + ": " + tweet.get("text") +  " -> " + tweet.get("retweet_count"));
//			}
//			System.out.println("Tweets found: " + tweets.size());
//			
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}