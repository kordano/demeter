package demeter.worker;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;
import twitter4j.URLEntity;

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
	
	public void storeTweet(Status status){
		String tweet = TwitterObjectFactory.getRawJSON(status);
		DBObject doc = (DBObject)JSON.parse(tweet);
		table.insert(doc);
	}
}
