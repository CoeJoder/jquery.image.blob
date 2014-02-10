package human.joecoder.imageblob.testng;

import human.joecoder.imageblob.FileResponse;
import human.joecoder.imageblob.JettyUploadServer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for the Image Blob jQuery plugin.
 * @author joe
 */
public class TestImageBlob {

	private static final int PORT = 8080;
	private static final String URL = "http://localhost:"+PORT;
	private static final long WEBDRIVER_TIMEOUT_SECONDS = 120L;
	private static final File WEBDRIVER_INJECTION_JS = new File("src/webapp/js/webdriver_injection.js");
	private static final File FIREFOX_PROFILE_DIR = new File("firefox_profile");
	private static final File IMAGE_SOURCE_DIR = new File("src/webapp/images");
	private static final File IMAGE_UPLOAD_DIR = new File("tmp_uploads");
	
	private JettyUploadServer server = null;
	private WebDriver driver = null;
	private String js_webdriverInjection = null;
	
	////////////////////
	// TESTNG LIFECYCLE
	////////////////////
	
	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws Exception {
		server = new JettyUploadServer(PORT, IMAGE_UPLOAD_DIR).start();
		driver = new FirefoxDriver(new FirefoxProfile(FIREFOX_PROFILE_DIR));
		driver.manage().timeouts().pageLoadTimeout(WEBDRIVER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(WEBDRIVER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		js_webdriverInjection = FileUtils.readFileToString(WEBDRIVER_INJECTION_JS);
	}
	
	@AfterClass(alwaysRun = true)
	public void afterClass() throws Exception {
		driver.close();
		server.stop();
	}
	
	//////////////////
	// DATA PROVIDERS
	//////////////////

	@DataProvider(name = "all")
	public Object[][] all() {
		browseToForm();
		return findElementAndSourcePairs("div.testcase img");
	}
	
	@DataProvider(name = "allPng")
	public Object[][] allPng() {
		browseToForm();
		return findElementAndSourcePairs("div.testcase.png img");
	}

	@DataProvider(name = "allJpg")
	public Object[][] allJpg() {
		browseToForm();
		return findElementAndSourcePairs("div.testcase.jpg img");
	}
	
	@DataProvider(name = "withNames")
	public Object[][] withNames() {
		browseToForm();
		return findElementAndSourcePairs("div.testcase:not(.withoutName) img");
	}

	@DataProvider(name = "withoutNames")
	public Object[][] withoutNames() {
		browseToForm();
		return findElementAndSourcePairs("div.testcase.withoutName img");
	}

	@DataProvider(name = "dataUri")
	public Object[][] dataUri() {
		browseToForm();
		return findElementAndSourcePairs("div.testcase.dataUri img");
	}

	//////////////
	// TEST CASES
	//////////////
	
	@Test(description = "Test image upload content.",
			dataProvider = "all")
	public void testUploadContent(WebElement img, File sourceImage) throws IOException {
		FileResponse fileResponse = uploadImageAndGetResponse(img);
		Assert.assertTrue("Image upload was empty.\n",
				fileResponse.getLength() > 0);
	}

	@Test(description = "Test PNG upload MIME type.",
			dataProvider = "allPng")
	public void testPngMimeType(WebElement img, File sourceImage) throws IOException {
		testMimeType(img, sourceImage, "image/png");
	}

	@Test(description = "Test JPG upload MIME type.",
			dataProvider = "allJpg")
	public void testJpgMimeType(WebElement img, File sourceImage) throws IOException {
		testMimeType(img, sourceImage, "image/jpeg");
	}
	
	// factor
	private void testMimeType(WebElement img, File sourceImage, String expected) throws IOException {
		FileResponse fileResponse = uploadImageAndGetResponse(img);
		String actual = fileResponse.getFileType();
		Assert.assertEquals("Wrong MIME type.\n",
				expected, actual);
	}

	@Test(description = "Test image uploads with name attributes.",
			dataProvider = "withNames")
	public void testImagesWithFileNames(WebElement img, File sourceImage) throws IOException {
		FileResponse fileResponse = uploadImageAndGetResponse(img);
		String expected = sourceImage.getName();
		String actual = fileResponse.getFileName();
		Assert.assertEquals("Uploaded image had wrong filename.\n", 
				expected, actual);
	}
	
	@Test(description = "Test image uploads without name attributes.",
			dataProvider = "withoutNames")
	public void testImagesWithoutFileNames(WebElement img, File sourceImage) throws IOException {
		FileResponse fileResponse = uploadImageAndGetResponse(img);
		// img element does not have a "name" attribute; it should use default name
		String expected = ((JavascriptExecutor) driver).executeScript(
				"return jQuery.fn.imageBlob.defaultImageName;", ArrayUtils.EMPTY_OBJECT_ARRAY).toString();
		String actual = fileResponse.getFileName();
		Assert.assertEquals("Uploaded image had wrong filename.\n", 
				expected, actual);
	}
	
	@Test(description = "Test image uploads without name attributes, overriding the default name.",
			dataProvider = "withoutNames")
	public void testImagesWithoutFileNamesCustom(WebElement img, File sourceImage) throws IOException {
		// use a custom default name
		String expected = "0xDEADBEEF";
		((JavascriptExecutor) driver).executeScript(
				"jQuery.fn.imageBlob.defaultImageName = '"+expected+"';", ArrayUtils.EMPTY_OBJECT_ARRAY);
		FileResponse fileResponse = uploadImageAndGetResponse(img);
		String actual = fileResponse.getFileName();
		Assert.assertEquals("Uploaded image had wrong filename.\n", 
				expected, actual);
	}
	
	//////////////////
	// HELPER METHODS
	//////////////////
	
	private void browseToForm() {
		driver.get(URL);
	}

	private FileResponse uploadImageAndGetResponse(WebElement img) 
			throws IOException {
		Object obj = ((JavascriptExecutor) driver).executeAsyncScript(
				js_webdriverInjection, img);
		// a String indicates success, a List indicates failure
		if (obj instanceof List) {
			Assert.fail(((List<?>)obj).iterator().next().toString());
		}
		ObjectMapper mapper = new ObjectMapper();
		List<FileResponse> responseFiles = mapper.readValue(obj.toString(), 
				new TypeReference<List<FileResponse>>() {});
		if (responseFiles.isEmpty()) {
			Assert.fail("Server response was empty.");
		}
		FileResponse fileResponse = responseFiles.iterator().next();
		return fileResponse;
	}
	
	private Object[][] findElementAndSourcePairs(String css) {
		List<WebElement> elmList = driver.findElements(By.cssSelector(css));
		Object[][] elmArray = new Object[elmList.size()][];
		for (int i = 0; i < elmList.size(); i++) {
			WebElement elm = elmList.get(i);
			File sourceImage = new File(IMAGE_SOURCE_DIR, elm.getAttribute("alt"));
			elmArray[i] = new Object[] { elm, sourceImage };
		}
		return elmArray;
	}
}
