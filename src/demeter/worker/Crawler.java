package demeter.worker;

import java.util.ArrayList;
import java.util.List;


import twitter4j.FilterQuery;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.StatusDeletionNotice;

public class Crawler {
	private static Twitter twitter;
	private static TwitterStream twitterStream;
	
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
		ConfigurationBuilder scb = new ConfigurationBuilder();
		scb.setDebugEnabled(true)
		  .setOAuthConsumerKey("RfwfMlqMXqWnIofQ8QjU5TpSX")
		  .setOAuthConsumerSecret("tXF0cJM0ltTyMw1363cNWAkflbgzg0LBeFrutFer7E9ksSZaJz")
		  .setOAuthAccessToken("108654757-1jR2QjJj3gZINhT7aTdQGKX0pKf3yIKyQGSu322w")
		  .setOAuthAccessTokenSecret("nbEkOMtOM6Ped8xozXHo6j2sI82k1uH1yOyZPMzuoOcng");
		twitterStream = new TwitterStreamFactory(scb.build()).getInstance();
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
	
	public void stream(String[] keywords, long[] ids) {
	    StatusListener listener = new StatusListener() {

	        public void onStatus(Status status) {
	            System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
	        }

	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	            System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
	        }

	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	            System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
	        }

	        public void onScrubGeo(long userId, long upToStatusId) {
	            System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
	        }

	        public void onException(Exception ex) {
	            ex.printStackTrace();
	        }

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				System.out.println(arg0.getMessage());
				
			}
	    };

	    FilterQuery fq = new FilterQuery();

	    fq.track(keywords);
	    fq.follow(ids);

	    twitterStream.addListener(listener);
	    twitterStream.filter(fq);
	}
}