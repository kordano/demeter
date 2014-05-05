package demeter.worker;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class Crawler {
	private static Twitter twitter;
	
	/**
	 * Initialize twitter4j
	 */
	public Crawler() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("RfwfMlqMXqWnIofQ8QjU5TpSX")
		  .setOAuthConsumerSecret("tXF0cJM0ltTyMw1363cNWAkflbgzg0LBeFrutFer7E9ksSZaJz")
		  .setOAuthAccessToken("108654757-1jR2QjJj3gZINhT7aTdQGKX0pKf3yIKyQGSu322w")
		  .setOAuthAccessTokenSecret("nbEkOMtOM6Ped8xozXHo6j2sI82k1uH1yOyZPMzuoOcng");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();		
	}
	
	/**
	 * Fetches timeline for a given user
	 * @param user twitter screen name
	 * @return
	 */
	public List<Status> fetchUserTimeline(String user) {
		try {
			Paging page = new Paging(1,300);
			return twitter.getUserTimeline(user,page);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return new ArrayList<Status>();
	}
	
	/**
	 * Search twitter for a specific string
	 * @param searchString
	 * @return
	 */
	public List<Status> search(String searchString){
		Query query = new Query(searchString);
	    QueryResult result;
		try {
			result = twitter.search(query);
			return result.getTweets();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return new ArrayList<Status>();
	    	
	}
}