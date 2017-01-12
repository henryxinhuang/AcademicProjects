package com.trello.api.trelloapitest;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Main class to obtain arguments(API key and token) from command line
 * by Xin Huang
 *
 */
public class TrelloAPI {

	public static String key = "";
	public static String token = "";
	/**
	 * Passing arguments from command line
	 */
	public static void main(String[] args) {
		key = args[0];
		token = args[1];
		
		Result result = JUnitCore.runClasses(TrelloAPITest.class);
		System.out.println("Trello API Test Passed? " + (result.wasSuccessful() ? "YES" : "NO"));
	}

}
