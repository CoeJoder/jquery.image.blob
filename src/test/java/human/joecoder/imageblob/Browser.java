package human.joecoder.imageblob;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Browser enum.  Handles the initialization of the underlying {@link WebDriver} object and provides some utility
 * methods.  Operates headless by default, but can be configured with {@link #asHeadless(boolean)}.
 *
 * @author Joe Nasca
 */
public enum Browser {
    CHROME, FIREFOX;

    private static final String DRIVER_PATH_NOT_SET = "WebDriver binary path not set in " + AppProperties.PROPERTIES_FILE;
    private static final String PLATFORM_NOT_SUPPORTED = "This O/S or architecture has no WebDriver distribution available.";

    private WebDriver driver = null;
    private boolean headless = false;

    /**
     * Launches the browser driver.
     *
     * @throws IOException
     */
    public Browser initialize() throws IOException {
        if (driver == null) {
            switch (this) {
                case CHROME:
                    driver = getChromeDriver(headless);
                    break;
                case FIREFOX:
                    driver = getFirefoxDriver(headless);
                    break;
                default:
                    throw new RuntimeException("Unrecognized browser: " + this);
            }
        }
        return this;
    }

    /**
     * Get the {@link WebDriver} instance.  Throws an exception if {@link #initialize()} isn't called first.
     *
     * @return
     */
    public WebDriver getDriver() {
        if (driver == null) {
            throw new RuntimeException("Driver not initialized.");
        }
        return driver;
    }

    /**
     * Gets a {@link WebDriverWait waiter} with the specified timeout.
     *
     * @param timeoutInSeconds
     * @return
     */
    public WebDriverWait waitFor(long timeoutInSeconds) {
        return new WebDriverWait(getDriver(), timeoutInSeconds);
    }

    /**
     * Must be called prior to {@link #initialize()}.
     *
     * @param headless
     * @return
     */
    public Browser asHeadless(boolean headless) {
        this.headless = headless;
        return this;
    }

    /**
     * Shuts down the browser driver.
     */
    public void close() {
        if (driver != null) {
            driver.close();
        }
    }

    private static WebDriver getChromeDriver(boolean headless) throws IOException {
        File driverBinary = new File(AppProperties.getInstance().getChromeDriver());
        assertDriverBinaryExists(driverBinary);
        System.setProperty("webdriver.chrome.driver", driverBinary.getAbsolutePath());
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(headless);
        return new ChromeDriver(options);
    }

    private static WebDriver getFirefoxDriver(boolean headless) throws IOException {
        File driverBinary = new File(AppProperties.getInstance().getFirefoxDriver());
        assertDriverBinaryExists(driverBinary);
        System.setProperty("webdriver.gecko.driver", driverBinary.getAbsolutePath());
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(headless);
        return new FirefoxDriver(options);
    }

    private static void assertDriverBinaryExists(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException("Driver binary missing: " + file.getAbsolutePath());
        }
    }
}
