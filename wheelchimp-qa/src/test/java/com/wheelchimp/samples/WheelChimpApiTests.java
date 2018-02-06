package com.wheelchimp.samples;

import org.apache.log4j.Logger;
import org.json.JSONException;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.jayway.restassured.RestAssured;
import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class WheelChimpApiTests  extends AbstractTest {
	
	private static final Logger LOGGER = Logger.getLogger(WheelChimpApiTests.class);
	
	@Test
	public void testCookieForAuth() throws JSONException{
		JSONObject obj = postLoginRequest();
		String cookie = RestAssured.given().relaxedHTTPSValidation().contentType("application/json").when()
				.body(obj.toString()).log().all().post(Configuration.getEnvArg("app_url")+"api/login").getCookie("connect.sid");
	
		LOGGER.info("cookie value.."+cookie);
		
	}
	
	public static JSONObject postLoginRequest() throws JSONException{
		JSONObject obj = new JSONObject();
		obj.put("username", Configuration.getEnvArg("userName"));
		obj.put("password", Configuration.getEnvArg("password"));
		return obj;
	}
	
	

}
