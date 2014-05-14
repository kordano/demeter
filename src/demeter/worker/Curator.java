package demeter.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class Curator {
	private static MongoClient mongo; 
	private static DBCollection table;
	
	/**
	 * Initialize MongoDB Connection
	 */
	public Curator(){
		try {
			mongo = new MongoClient( "localhost" , 27017 );
			DB db = mongo.getDB("athena");
			table = db.getCollection("tweets");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Store given status to mongodb tweets collection
	 * @param status twitter status
	 */
	public void storeTweet(Status status){
		String tweet = TwitterObjectFactory.getRawJSON(status);
		DBObject doc = (DBObject)JSON.parse(tweet);
		table.insert(doc);
	}
	
	/**
	 * Find all tweets of given user
	 * @param username twitter account name
	 * @return
	 * @throws TwitterException 
	 */
	public void retrieveUserTweets(String username) throws TwitterException {
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("user.screen_name", username);
		
		System.out.println("tweets by @" + username);
		
		DBCursor cursor = table.find(whereQuery);
		while(cursor.hasNext()) {
			String entry = JSON.serialize(cursor.next());
			Status status = TwitterObjectFactory.createStatus(entry);
			System.out.println(status.getCreatedAt());
		}
		System.out.println("\n");
	}
	
	/**
	 * Find all tweets mentioning the given user
	 * @param username
	 * @throws TwitterException 
	 */
	public void retrieveUserMentions(String username) throws TwitterException {
		BasicDBObject query = new BasicDBObject("entities.user_mentions.screen_name", username);
		DBCursor cursor = table.find(query);
		while(cursor.hasNext()) {
			String jsonString = JSON.serialize(cursor.next());
			Status status = TwitterObjectFactory.createStatus(jsonString);
			Status retweetedStatus = status.getRetweetedStatus();
			if(retweetedStatus != null) {
				BasicDBObject accQuery = new BasicDBObject("id_str",Long.toString(retweetedStatus.getId()));
				DBCursor accCursor = table.find(accQuery);
				while(accCursor.hasNext()) {
					String accJsonString = JSON.serialize(accCursor.next());
					Status accStatus = TwitterObjectFactory.createStatus(accJsonString);
					System.out.println(accCursor.next());
				}
			}
		}
	}
	
	
	
	/** 
	 * Get all tweets in the database
	 * @return
	 * @throws TwitterException
	 */
	public List<Status> getAllTweets() throws TwitterException {
		DBCursor cursor = table.find();
		ArrayList<Status> statuses = new ArrayList<Status>();
		while(cursor.hasNext()) {
			String jsonString = JSON.serialize(cursor.next());
			statuses.add(TwitterObjectFactory.createStatus(jsonString));
		}
		return statuses;
		
	}
		
	/** 
	 * Get all tweets of a specific user in the database
	 * @return
	 * @throws TwitterException
	 */
	public List<Status> getAllTweets(String username) throws TwitterException {
		BasicDBObject query = new BasicDBObject("user.screen_name", username);
		DBCursor cursor = table.find(query);
		ArrayList<Status> statuses = new ArrayList<Status>();
		while(cursor.hasNext()) {
			String jsonString = JSON.serialize(cursor.next());
			Status status = TwitterObjectFactory.createStatus(jsonString);
			System.out.println(status.getText());
			statuses.add(status);
		}
		return statuses;
		
	}
	
	/**
	 * Calculate the hours distribution of the tweets
	 * @throws TwitterException
	 */
	public void getTimeDistribution() throws TwitterException{
		DBCursor cursor = table.find();
		HashMap<Integer,Integer> timeFrequencies = new HashMap<Integer,Integer>();
		Set<String> newsAccounts = new HashSet<String>() ;
		newsAccounts.add("FAZ_NET");
		newsAccounts.add("dpa");
		newsAccounts.add("tagesschau");
		newsAccounts.add("SPIEGELONLINE");
		newsAccounts.add("SZ");
		
		for(int i=0;i<24;i++) {
			timeFrequencies.put(i,0);
		}
		
		while(cursor.hasNext()) {
			String entry = JSON.serialize(cursor.next());
			Status status = TwitterObjectFactory.createStatus(entry);
			int hours = status.getCreatedAt().getHours();
			timeFrequencies.put(hours, timeFrequencies.get(hours) + 1);
		}
		
		for(int i=0;i<24;i++) {
			System.out.println(i + ":" + timeFrequencies.get(i));
		}
	}
	
	/**
	 * Write mongodb data to json file
	 * @param folder
	 * @throws TwitterException
	 */
	public void serializeDatabase(String folder) throws TwitterException {
		DBCursor cursor = table.find();
 		Writer writer = null;

 		try {
 			String filePath = folder +"/"+ table.getName() + ".json";
 			System.out.println("Writing mongodb table <" + table.getName() + "> to " + filePath);
 			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "utf-8"));
		      
 			while(cursor.hasNext()) {
 				DBObject entry = cursor.next();
 				entry.removeField("_id");
 				entry.removeField("filter_level");
 				writer.write(JSON.serialize(entry) + "\n");
 			}
		 	} catch (IOException ex) {
		 		// report
		 	} finally {
		 		try {writer.close();} catch (Exception ex) {}
		 	}	
	}
	
	/**
	 * Read data from json file
	 * @param filePath
	 * @throws IOException
	 */
	public void parseDatebase(String filePath) throws IOException {
		File file = new File(filePath);
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			while (in.ready()) {
				table.insert((DBObject)JSON.parse(in.readLine()));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {in.close();} catch(Exception ex) {}
		}
	}
}