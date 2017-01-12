/**
 * Trello API Test, Maven dependencies added: Rest Assured, Javax JSON, JUnit
 * by Xin Huang
 */
package com.trello.api.trelloapitest;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.rules.TestName;

import static com.jayway.restassured.RestAssured.given; 
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.*; //Rest Assured Library

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;


public class TrelloAPITest 
{
	public static String cardId;
	public static String key = TrelloAPI.key;   //Obtain key from main class
	public static String token = TrelloAPI.token; //Obtain token from main class 
	public static String encode = "UTF-8";
	public static String baseUrl = "https://api.trello.com/1/cards/";
	/*
	 * Trello API Documentation: POST/1/cards
	 * https://developers.trello.com/advanced-reference/card#post-1-cards
	*/
	@Test 
	public void addCard() throws IOException, ClientProtocolException 
	{
		//See if Trello server is up
		given().when().get("https://trello.com").then().statusCode(200);

		String expire = "null";
		String name = "New card";
		String content = "Hello, World!";
		/*
		 * Encoding request string into UTF-8 Apache Codec
		 * Request format is "cardID + expire + name + content of card + API key + API token"
		 * https://commons.apache.org/proper/commons-codec/apidocs/org/apache/commons/codec/net/URLCodec.html
		 */
		String requestBody = String.format("idList=%s&due=%s&name=%s&desc=%s&key=%s&token=%s", 
				URLEncoder.encode(cardId, encode), 
				URLEncoder.encode(expire, encode),
				URLEncoder.encode(name, encode), 
				URLEncoder.encode(content, encode), 
				URLEncoder.encode(key, encode), 
			    URLEncoder.encode(token, encode));
		
		//Starts HTTP session
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(baseUrl + "?" + requestBody); //POST
		HttpResponse response = httpClient.execute(postRequest); 
		if (response.getStatusLine().getStatusCode() == 200) {
			//Check Response Status 200
			//given(contentType).when().get(response).then().statusCode(200);
			assertEquals(response.getStatusLine().getStatusCode(), 200);
		}
		else {
			throw new ClientProtocolException();
		}
		//Closing HTTP session
		httpClient.close();
	}
	
	/*
	 * Trello API Documentation: DELETE/1/cards/card id
	 * https://developers.trello.com/advanced-reference/card#delete-1-cards-card-id-or-shortlink
	 * */
	@Test 
	public void deleteCard() throws IOException, ClientProtocolException 
	{		
		String expire = "null";
		String name = "Delete card";
		String content = "Goodbye, World!";
		String requestBody = String.format("idList=%s&due=%s&name=%s&desc=%s&key=%s&token=%s",
				URLEncoder.encode(cardId, encode), 
				URLEncoder.encode(expire, encode),
				URLEncoder.encode(name, encode), 
				URLEncoder.encode(content, encode), 
				URLEncoder.encode(key, encode), 
				URLEncoder.encode(token, encode));
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(baseUrl + "?" + requestBody);
		HttpResponse response = httpClient.execute(postRequest);
		
		if (response.getStatusLine().getStatusCode() == 200) {
			
			//String Auth is API key + token
			String Auth = String.format("key=%s&token=%s", URLEncoder.encode(key, encode), URLEncoder.encode(token, encode));
			HttpDelete delete = new HttpDelete(baseUrl + cardId + "?" + Auth);
			HttpResponse delResponse = httpClient.execute(delete);
			assertEquals(delResponse.getStatusLine().getStatusCode(), 200);
		}
		else {
			throw new ClientProtocolException();
		}
		httpClient.close();
	}
	
	/*
	 * Editing a card's content
	 * */
	@Test 
	public void editCard() throws IOException, ClientProtocolException
	{
		String expire = "null";
		String name = "Content Change";
		String content = "Giberish giberish";
		
		String requestBody = String.format("idList=%s&due=%s&name=%s&desc=%s&key=%s&token=%s", 
				URLEncoder.encode(cardId, encode), 
				URLEncoder.encode(expire, encode),
				URLEncoder.encode(name, encode), 
				URLEncoder.encode(content, encode), 
				URLEncoder.encode(key, encode), 
				URLEncoder.encode(token, encode));

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(baseUrl + "?" + requestBody);
		HttpResponse response = httpClient.execute(postRequest);
		
		if(response.getStatusLine().getStatusCode() == 200) {
			
			//Use JSON reader library to find "id" in response body
			JsonReader getContent = Json.createReader(response.getEntity().getContent());
			JsonObject contentOld = getContent.readObject();
			String cardId = contentOld.getString("id");
			getContent.close();
			
			//Send the new content with key and token to the card
			String contentNew = "New Content Blah Blah Blah!";
			String changeAuth = String.format("value=%s&key=%s&token=%s",
				URLEncoder.encode(contentNew, encode), 
				URLEncoder.encode(key,encode), 
				URLEncoder.encode(token, encode));

			//Path providede by API Doc
			HttpPut putRequest = new HttpPut(baseUrl + cardId + "/desc" + "?" + changeAuth);
			httpClient.execute(putRequest);
			
			//Retrieve the new content of the card
			String Auth = String.format("key=%s&token=%s",URLEncoder.encode(key, encode), URLEncoder.encode(token, encode));
			HttpGet newRequest = new HttpGet(baseUrl + cardId + "/desc" + "?" + Auth);
			HttpResponse newResponse = httpClient.execute(newRequest);
			
			if(newResponse.getStatusLine().getStatusCode() == 200) {
				//Use JSON reader to find field "_value", which is the new content
				JsonReader getNewContent = Json.createReader(newResponse.getEntity().getContent());
				JsonObject newJson = getNewContent.readObject();
				getNewContent.close();
				String currentContent = newJson.getString("_value");
				//See if the content has been changed
				assertEquals(currentContent, contentNew);
			}
			else {
				throw new ClientProtocolException();
				}
		}
		httpClient.close();
	}
}
	