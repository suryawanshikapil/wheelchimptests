package com.wheelchimp.samples;

import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.AbstractTest;

public class WheelChimpTests extends AbstractTest {
	
	@Test
	public void testHomePage() {
		WheelChimpHomePage homePage = new WheelChimpHomePage(getDriver());
	}

}
