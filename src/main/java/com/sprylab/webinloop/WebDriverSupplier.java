package com.sprylab.webinloop;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;

import com.google.common.base.Supplier;

/**
 * Supplier for WebDriver. Supplies a WebDriver for the configured browser. 
 * @author jb
 *
 */
public class WebDriverSupplier implements Supplier<WebDriver> {

	@Override
	public WebDriver get() {
        final String browser = WiLConfiguration.getInstance().getString(WiLConfiguration.BROWSER_PROPERTY_KEY);

        if (browser.equals("*googlechrome")) {
	        final DesiredCapabilities capability = DesiredCapabilities.chrome();
	        capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
	        
	        final String executable = WiLConfiguration.getInstance().getString(WiLConfiguration.CHROME_DRIVER_EXE_PROPERTY_KEY);
	        if (executable != null) {
	        	System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, "c:\\devel\\tools\\chromedriver.exe");
	        }
	        
	    	return new ChromeDriver(capability);
        } else if (browser.equals("*firefox")) {
        	return new FirefoxDriver();
        } else if (browser.equals("*iexplore")) {
        	return new InternetExplorerDriver();
        } else if (browser.equals("*safari")) {
        	return new SafariDriver();
        }

        throw new IllegalArgumentException("No webdriver for browser " + browser);
	}

}
