package twitter.query;

import com.intuit.karate.cucumber.CucumberRunner;

import twitter4j.TwitterException;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.junit.Test;
import org.apache.commons.csv.*;

/**
 * Executes Twitter Query Test
 * 
 * @author agebhardtsbauer
 *
 */
public class QueryRunner{ 
	
	/**
	 * Path to Credentials CSV
	 */
	private static final String CREDENTIALS_CSV_PATH = "/src/test/java/twitter/csvs/OauthCred.csv";
	
	/**
	 * Path to Query Values CSV
	 */
	private static final String QUERY_VALUES_CSV_PATH = "/src/test/java/twitter/csvs/QueryInputs.csv";
	
	
	/**
	 * Test Execution of Twitter Auth and Query
	 * 
	 * @throws TwitterException
	 */
	@Test
	public void loadTest() throws IOException{
		System.out.println("Twitter Load Test Running\n------------------------");
        
		//map the credentials from CSV
		Map<String, String> appCreds = getCredentialsMap();
		final String CONSUMER_KEY = appCreds.get("clientID");
		final String CONSUMER_SECRET = appCreds.get("clientSecret");
		
		//generate base64 Authorization Header value
		String stringBytes = generateTwitterBase64FromClientCredentials(CONSUMER_KEY, CONSUMER_SECRET);
	    
		//generate the bearer token from base64 credential hash
	    String bearerToken = generateBearerToken(stringBytes);
	        
	    //execute a query with bearer token for each record in CSV
	    executeAllQueries(bearerToken);
		
		System.out.println("----------------------------\nTwitter Query Execution Finished");
	}
	
	/**
	 * Generate a Twitter Bearer Token using the hashed credentials required in OAuth2
	 * 
	 * @param base64EncodedCredentials the base64 hashed client_id and client_secret to twitter specifications
	 * @return the base64 hash value to be used as a bearer token for OAuth2
	 */
	private String generateBearerToken(String base64EncodedCredentials) {
		//create argument hash map for base64 auth
	    Map<String, Object> args = new HashMap<String, Object>();
	    args.put("baseKey", base64EncodedCredentials);
	    
	    //run get bearer token
	    Map<String, Object> result = CucumberRunner.runFeature(getClass(), "GenerateBearerToken.feature", args, true);
	    
	    //parse the bearer token from the response
	    String theResponse = result.get("response").toString();	    
	    return parseAccessTokenFromResponse(theResponse);
	}
	
	/**
	 * Execute Twitter Query for each row in CSV
	 * 
	 * @param bearerToken the bearer token to authorize twitter search
	 * @throws IOException
	 */
	private void executeAllQueries(String bearerToken) throws IOException{
		//generate path to query csv
        String path = getCSVAbsolutePath(QUERY_VALUES_CSV_PATH);

		//create a reader for queries csv
        Reader reader = Files.newBufferedReader(Paths.get(path));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .withHeader("at", "count", "until")
                .withIgnoreHeaderCase()
                .withTrim());
        
        //get the first record in the file
        for(CSVRecord record : csvParser.getRecords()){
            //store the values in a map
    	    		Map<String, Object> args = new HashMap<String, Object>();
	        args.put("aT", record.get("at"));
	        args.put("counT", record.get("count"));
	        args.put("untiL", record.get("until"));
	    	    	args.put("accessToken", bearerToken);
	    	    	
    		    //cucumber run tweet query
    		    CucumberRunner.runFeature(getClass(), "SimpleTweetSearch.feature", args, false);
        }
     
        //close streaming resource users
        csvParser.close();
        reader.close();
	   
	}
	
	/**
	 * Get a Hash Map of the OauthCred.csv. Map Keys are 
	 * 
	 * @return map of twitter oauth2 credentials
	 * @throws IOException
	 */
	private Map<String, String> getCredentialsMap() throws IOException{
		Map<String, String> creds = new HashMap<String, String>();
		
        String path = getCSVAbsolutePath(CREDENTIALS_CSV_PATH);
		
		//create a reader for creds csv
        Reader reader = Files.newBufferedReader(Paths.get(path));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .withHeader("clientID", "clientSecret")
                .withIgnoreHeaderCase()
                .withTrim());
        
        //get the first record in the file
        CSVRecord record = csvParser.getRecords().get(1);

        //store the values in a map
        creds.put("clientID", record.get("clientID"));
        creds.put("clientSecret", record.get("clientSecret"));
        
        //close streaming resource users
        reader.close();
        csvParser.close();
        
        return creds;   
	}
	
	/**
	 * Generate an absolute file path using a relative path
	 * 
	 * @param relativePath the relative path to the file
	 */
	private String getCSVAbsolutePath(String relativePath) {
        String filePath = new File("").getAbsolutePath();
        return filePath.concat(relativePath);
	}
	
	/**
	 * Generate a Base64 Hash suitable for submitting a twitter Oauth2 Authorization Base header<br>
	 * Example Usage: header 'Authorization Base <the return from this function>'
	 * 
	 * @param publicKey the public key to encode - Twitter calls this the 'Consumer Key'
	 * @param privateKey the private key to encode - Twitter calls this the 'Consumer Secret'
	 * @return the Base64 hash of the key parameters suitable for the 'Authorization Base' header
	 */
	private String generateTwitterBase64FromClientCredentials(String publicKey, String privateKey){
		return Base64.getEncoder().encodeToString((publicKey+":"+privateKey).getBytes());
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