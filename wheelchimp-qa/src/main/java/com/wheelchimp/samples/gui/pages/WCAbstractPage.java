package com.wheelchimp.samples.gui.pages;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractPage;

public class WCAbstractPage extends AbstractPage {
	protected static final int UP_TO_FIVE_MINUTES = Configuration.get(Parameter.ENV).toLowerCase().contains("prod") ? 150 : 300;
	protected static final int UP_TO_TWO_MINUTES = Configuration.get(Parameter.ENV).toLowerCase().contains("prod") ? 60 : 120;
	protected static final int SHORT_TIMEOUT = 10;

	private static final String SERVER_DOWN_MESSAGE_1 = "The connection was reset";
	private static final String SERVER_DOWN_MESSAGE_2 = "This webpage is not available";
	
	//private static final Logger logger = Logger.getLogger(LCAbstractPage.class);

	private By elementToClick;

	@FindBy(css = ".module-ajax-mid")
	public ExtendedWebElement busyIndicator;

	public WCAbstractPage(WebDriver webdriver) {
		super(webdriver);
	}

	/**
	 * TODO needs to move in DriverHelper The root findElement method
	 * 
	 */
	public WebElement findElement(int timeToWait, ExpectedCondition<WebElement> conditiontoCheck) {
		FluentWait<WebDriver> fluentWaiter = new FluentWait<WebDriver>(driver).ignoring(UnreachableBrowserException.class);
		return fluentWaiter.withTimeout(timeToWait, TimeUnit.SECONDS).pollingEvery(500, TimeUnit.MILLISECONDS).until(conditiontoCheck);
	}

	/**
	 * TODO needs to move in DriverHelper Finds an element which is visible
	 * 
	 * @param elementSelector
	 * @return The element, or null if was not visible
	 */
	public WebElement findVisibleElement(ExtendedWebElement elementSelector) {
		return findVisibleElement(UP_TO_TWO_MINUTES, elementSelector.getBy());
	}

	/**
	 * TODO needs to move in DriverHelper Finds an element which is visible
	 * 
	 * @param elementSelector
	 * @return The element, or null if was not visible
	 */
	public WebElement findVisibleElement(By elementSelector) {
		return findVisibleElement(UP_TO_TWO_MINUTES, elementSelector);
	}

	/**
	 * TODO needs to move in DriverHelper Finds an element which is visible
	 * 
	 * @param timeToWait
	 *            .Time in seconds
	 * @param by
	 *            The By, like By.cssSelector("div#much.wow")
	 * @return The element, or null if it was not visible
	 */
	public WebElement findVisibleElement(int timeToWait, By by) {
		try {
			return findElement(timeToWait, ExpectedConditions.visibilityOfElementLocated(by));
		} catch (Exception e) {
			return null;
		}
	}

	// Protected methods
	protected void open(int durationfactor) {
		long startTime = System.nanoTime();
		setExplicitTime(durationfactor);
		try {
			open();
		} catch (Throwable thr) {
			printErrorMessageAndExit("Application too slow: Cannot open the page '" + getPageURL() + "' after " + getElapsedTime(startTime) + " seconds. ");
		} finally {
			setExplicitTime(1);
		}
	}

	protected boolean isServerDown() {
		String htmlSource = driver.getPageSource();
		if (htmlSource.contains(SERVER_DOWN_MESSAGE_1) || htmlSource.contains(SERVER_DOWN_MESSAGE_2)) {
			return true;
		}
		return false;
	}

	protected String onErrorPage() {
		if (driver.getTitle().contains("Error report")) {
			return driver.findElement(By.tagName("h1")).getText();
		}
		return "";
	}

	protected boolean waitForTheWidget(int time, By widget) {
		try {
			(new WebDriverWait(driver, time)).until(ExpectedConditions.elementToBeClickable(widget));
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	protected void waitForPageToFullyLoad(int numberOfSeconds, String errorMessage) {
		pause(3); // wait for 'LC Wait Icon' to appear.
		try {
			while (!hasLoadingIconDisappeared(numberOfSeconds))
				;
		} catch (TimeoutException ex) {
			printErrorMessageAndExit(errorMessage);
		}
	}

	public void waitForLCWaitIconToAppear(int numberOfSeconds) {
		new WebDriverWait(driver, numberOfSeconds).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver d) {
				return isElementVisible(By.id("wait-image"));
			}
		});
	}

	public boolean hasLoadingIconDisappeared(int numberOfSeconds) {
		return new WebDriverWait(driver, numberOfSeconds).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver d) {
				return !isElementVisible(By.id("wait-image"));
			}
		});
	}

	protected void waitForElementAndExitIfTimedout(By locator, int waitPeriod, String errorMessage) {
		try {
			(new WebDriverWait(driver, waitPeriod)).until(ExpectedConditions.elementToBeClickable(locator));
		} catch (TimeoutException ex) {
			printErrorMessageAndExit(errorMessage);
		}
	}

	protected void waitForElementAndExitIfTimedout(String label, By locator) {
		try {
			(new WebDriverWait(driver, UP_TO_FIVE_MINUTES)).until(ExpectedConditions.elementToBeClickable(locator));
		} catch (TimeoutException ex) {
			printErrorMessageAndExit("Application too slow: The test terminates after clicking on the '" + label + "' and waited for 5 minutes.");
		}
	}

	protected void clickAndExitIfTimedOut(By elementToClick, String widgetName) {
		clickAndExitIfTimedOut(elementToClick, widgetName, 1);
	}

	protected void clickAndExitIfTimedOut(By elementToClick, By elementToAppear, String widgetName) {
		clickAndExitIfTimedOut(elementToClick, elementToAppear, widgetName, 1);
	}

	protected void clickAndExitIfTimedOut(By elementToClick, By elementToAppear, String widgetName, int n) {
		this.elementToClick = elementToClick;
		long startTime = System.nanoTime();
		try {
			System.out.println("click on the '" + widgetName + "'.");
			driver.findElement(elementToClick).click();
		} catch (TimeoutException ex1) {
			try {
				new WebDriverWait(driver, n * UP_TO_FIVE_MINUTES).until(ExpectedConditions.elementToBeClickable(elementToAppear)); // waitForPageToLoad(elementToAppear,
																																	// n);//sub-page
																																	// to
																																	// load
			} catch (TimeoutException ex2) {
				printErrorMessageAndExit("Application too slow: The test terminates after clicking on the " + widgetName + ",  and waited " + getElapsedTime(startTime) + " seconds.");
			}
		}
	}

	protected void clickAndExitIfTimedOut(By elementToClick, String widgetName, int n) {
		this.elementToClick = elementToClick;
		long startTime = System.nanoTime();
		try {
			System.out.println("click on the '" + widgetName + "'.");
			driver.findElement(elementToClick).click();
		} catch (TimeoutException ex) {
			waitForPageToLoad(n); // sub-page to load
			printErrorMessageAndExit("Application too slow: The test terminates after clicking on the " + widgetName + ",  and waited " + getElapsedTime(startTime) + " seconds.");
		}
	}

	protected void waitForPageToLoad(int n) {
		new WebDriverWait(driver, n * UP_TO_FIVE_MINUTES).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver d) {
				System.out.println("Check for the element to disappears.");
				return !isElementVisible(elementToClick);
			}
		});
	}

	public boolean isElementVisible(By by) {
		if (isElementPresent(by)) {
			return driver.findElement(by).isDisplayed();
		}
		return false;
	}

	public boolean isElementPresent(By by) {
		List<WebElement> elements = driver.findElements(by);
		return 0 != elements.size();
	}

	public Boolean isTextPresentOnPage(String headerText) {
		Boolean isTextPresent = false;
		try {
			WebElement pageTitle = driver.findElement(By.xpath("//*[contains(.,'" + headerText + "')]"));
			if (pageTitle != null)
				isTextPresent = true;
			return isTextPresent;
		} catch (Exception e) {
			isTextPresent = false;
			return isTextPresent;
		}
	}

	protected void printErrorMessageAndExit(String errorMessage) {
		String message = highlightErrorMessage(errorMessage);
		Reporter.log(message, true);
		LOGGER.error(message);
		Assert.fail(message);
	}

	protected boolean click(ExtendedWebElement elementToClick, int number, By elementToLatch) {
		if (!click(elementToClick, number)) {
			return false;
		}
		if (!latch(elementToLatch, number)) {
			return false;
		}
		return true;
	}

	protected boolean click(ExtendedWebElement elementToClick, By elementToLatch, By expectedElement, int number) {
		if (!click(elementToClick, number)) {
			return false;
		}
		if (!latch(elementToLatch, number)) {
			return false;
		}
		// Latch has disappeared, check whether the expectedElement is available
		try {
			(new WebDriverWait(driver, UP_TO_FIVE_MINUTES)).until(ExpectedConditions.elementToBeClickable(expectedElement));
			return true;
		} catch (Throwable thr) {
			return false;
		}
	}

	private boolean latch(By elementToLatch, int number) {
		if (searchLatch(elementToLatch)) {
			// Latch is found, wait until it disappears
			try {
				new WebDriverWait(driver, UP_TO_FIVE_MINUTES).until(ExpectedConditions.invisibilityOfElementLocated(elementToLatch));
				return true;
			} catch (TimeoutException ex) {
				Reporter.log("\n Application too slow: Waited 5 minutes for the '" + elementToLatch.toString() + "' element to disapper, \n", true);
				return false;
			}
		}
		return true;
	}

	private boolean searchLatch(By elementToLatch) {
		try {
			(new WebDriverWait(driver, 10)).until(ExpectedConditions.elementToBeClickable(elementToLatch));
			return true;
		} catch (Throwable thr) {
			return false;
		}
	}

	protected boolean click(ExtendedWebElement elementToClick, By expectedElement, int number) {
		if (!click(elementToClick, number)) {
			return false;
		}
		try {
			(new WebDriverWait(driver, UP_TO_FIVE_MINUTES)).until(ExpectedConditions.elementToBeClickable(expectedElement));
			return true;
		} catch (Throwable thr) {
			return false;
		}
	}

	protected boolean click(ExtendedWebElement element, int number) {
		// setImplicitTime(number);
		try {
			findVisibleElement(element).click();
			return true;
		} catch (NoSuchElementException ex) {
			printErrorMessageAndExit("Cannot find '" + element.getName() + "' widget.");
			return false; // never execute
		} catch (Throwable thr) {
			printErrorMessageAndExit("Error during click:"+thr);
			return false; // timeout
		} finally {
			// setImplicitTime(1); // Revert back to original
		}
	}

	protected void setImplicitTime(int number) {
		// there is no way to change timeout in such way for mobile web tests
		if (Configuration.get(Parameter.BROWSER).toLowerCase().contains("mobile") || Configuration.get(Parameter.BROWSER).toLowerCase().contains("safari"))
			return;

		long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);
		driver.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT * number, TimeUnit.SECONDS);
	}

	protected void setExplicitTime(int number) {
		// there is no way to change timeout in such way for mobile web tests
		if (Configuration.get(Parameter.BROWSER).toLowerCase().contains("mobile") || Configuration.get(Parameter.BROWSER).toLowerCase().contains("safari"))
			return;

		long explicitTimeout = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);
		driver.manage().timeouts().pageLoadTimeout(explicitTimeout * number, TimeUnit.SECONDS);
	}

	protected String highlightErrorMessage(String message) {
		return "\n---------------------------------------------- The test has found the following error(s): --------------------------------------------\n" + message
				+ "\n--------------------------------------------------------------------------------------------------------------------------------------\n";
	}

	protected void isOnTheRightPage(String PAGE_TITLE_LOCATOR, final String TITLE, final ExtendedWebElement pageTitle) {
		// by default if String is transfered verification is performed using
		// By.tagName()
		isOnTheRightPage(By.tagName(PAGE_TITLE_LOCATOR), TITLE, pageTitle);
	}

	protected void isOnTheRightPage(By by, final String TITLE, final ExtendedWebElement pageTitle) {
		waitForElementAndExitIfTimedout(by, UP_TO_FIVE_MINUTES, "Could not navigate to the '" + TITLE + "' page after waiting for 5 minutes. The current page title is '" + driver.getTitle() + "'.");
		try {
			new WebDriverWait(driver, UP_TO_FIVE_MINUTES).until(new ExpectedCondition<Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					return pageTitle.getText().equals(TITLE);
				}
			});
		} catch (Throwable thr) {
			if (isServerDown()) {
				printErrorMessageAndExit("Could not navigate to the '" + TITLE + "' page. Either the server is down or page is not available.");
			}
			printErrorMessageAndExit("Could not navigate to the '" + TITLE + "' page. The current page title is '" + driver.getTitle() + "'.");
		}
	}

	// --------------------------------------------- Private
	// -------------------------------------------
	private String getElapsedTime(long startTime) {
		long elapsedTime = System.nanoTime() - startTime;
		return "" + Math.round(elapsedTime / 1000000000);
	}
	
	protected boolean isEnabled(WebElement wb){
		return wb.isEnabled();
	}

	/**
	 * Waits alert appearance during timeToWait and trying to accept it. TODO
	 * move to DriverHelper class
	 */
	public void acceptAlert(long timeToWait) {
		WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, timeToWait, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return isAlertPresent();
				}
			});
			drv.switchTo().alert().accept();
			Messager.ALERT_ACCEPTED.info("");
		} catch (Exception e) {
			Messager.ALERT_NOT_ACCEPTED.error("");
		}
	}

	/**
	 * Waits for fully page load.
	 */
	protected void waitForPageLoad() {
		WebDriver driver = getDriver();
		try {
			ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
				}
			};
			WebDriverWait wait = new WebDriverWait(driver, UP_TO_TWO_MINUTES);
			wait.until(pageLoadCondition);
		} catch (Exception ex) {
			Messager.TEST_FAILED.error("Time is over and page was not fully loaded");
		}
	}

	/**
	 * Waits for file exists
	 * 
	 * @param filePath
	 *            . Full path to the file including file name
	 * @throws Exception
	 */
	public void waitUntilFileExists(String filePath) throws Exception {
		File file = new File(filePath);
		int counter = 0;
		while (true) {
			if (file.exists())
				return;
			else {
				pause(1);
				counter++;
			}
			if (counter == UP_TO_TWO_MINUTES)
				break;
		}
		Messager.TEST_FAILED.error("There is no downloaded file");
	}
}
