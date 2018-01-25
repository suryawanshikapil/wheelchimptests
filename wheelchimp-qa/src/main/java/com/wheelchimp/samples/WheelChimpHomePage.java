package com.wheelchimp.samples;

import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.wheelchimp.samples.gui.pages.WCAbstractPage;

public class WheelChimpHomePage extends WCAbstractPage{
	
	public WheelChimpHomePage(WebDriver webdriver) {
		super(webdriver);
		setPageAbsoluteURL("https://www.wheelchimp.com");
		open();
	}

}
