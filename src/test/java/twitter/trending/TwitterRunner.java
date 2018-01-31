package twitter.trending;

import com.intuit.karate.cucumber.CucumberRunner;
import twitter4j.TwitterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import org.junit.Test;

/**
 * Class Runner for Twitter Query
 * 
 * @author agebhardtsbauer
 *
 */
public class TwitterRunner{ 
    // refer to https://github.com/intuit/karate#naming-conventions
    // for folder-structure recommendations and naming conventions

	private static final String CONSUMER_KEY = "FyaxzrjSeANKblTaBL7h8lTdL";
	private static final String CONSUMER_SECRET = "XTzVNqexqPpq5jgcx6HDV8A7anCR3NTLXv1MNj38eJnXmblmxq";
	
	/**
	 * Test Execution of Twitter Auth and Query
	 * 
	 * @throws TwitterException
	 */
	@Test
	public void loadTest(){
		System.out.println("Twitter Load Test Running\n---------------");
		
		//base64 encode the consumer key and consumer secret
		byte[] encodedBytes = Base64.getEncoder().encode((CONSUMER_KEY+":"+CONSUMER_SECRET).getBytes());
		String stringBytes = Base64.getEncoder().encodeToString((CONSUMER_KEY+":"+CONSUMER_SECRET).getBytes());
	
		//debug code
//	    System.out.println("\n------Base64 Encode Debug---------");
//		System.out.println("Encoded Bytes:__"+encodedBytes+"__");
//		System.out.println("String Bytes__"+stringBytes+"__");
//		byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes);
//		System.out.println("Decoded Bytes:__" + new String(decodedBytes)+"__");
//		System.out.println("------End Encode Debug----------\n");
	
		//create argument hash map for base64 auth
	    Map<String, Object> args = new HashMap();
	    args.put("baseKey", stringBytes);
	    
	    //run get bearer token
	    Map<String, Object> result = CucumberRunner.runFeature(getClass(), "GenerateBearerToken.feature", args, true);
	    
	    //Debug Stuff - Verify the response in console
	    System.out.println("\n------Result Access Token---------");
	    String theResponse = result.get("response").toString();
	    System.out.println(theResponse);
	    System.out.println(parseAccessTokenFromResponse(theResponse));
	    System.out.println("------End Result Token ----------\n");
	    
	    //add the access token to the args hash map
	    args.put("accessToken", parseAccessTokenFromResponse(theResponse));
	    
	    //cucumber run tweet query
	    Map<String, Object> query = CucumberRunner.runFeature(getClass(), "SimpleTweetSearch.feature", args, true);
	
	    //debug the result of query
	    System.out.println("------Result Entire Map---------");
	    //System.out.println(query.toString());
	    System.out.println("------End Entire Map------------");
		
		System.out.println("-------------\nTwitter Load Test Finished");
	}
	
	/**
	 * Parse the Response of a bearer token request and get just the access token
	 * 
	 * @param theResponse the response to parse
	 * @return the access token in String form
	 */
	private String parseAccessTokenFromResponse(String theResponse) {
		return theResponse.substring(theResponse.indexOf("access_token=") + 13 , theResponse.length()-1);
	}
}