package human.joecoder.imageblob.testng;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import human.joecoder.imageblob.Browser;
import human.joecoder.imageblob.FileResponse;
import human.joecoder.imageblob.JettyUploadServer;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for the Image Blob jQuery plugin.
 *
 * @author joe
 */
public class TestImageBlob {

    private static final int PORT = 8080;
    private static final String URL = "http://localhost:" + PORT;
    private static final long WEBDRIVER_TIMEOUT_SECONDS = 120L;
    private static final File RESOURCE_BASE = new File("src/test/webapp");
    private static final File IMAGE_SOURCE_DIR = new File(RESOURCE_BASE, "/images");
    private static final String UPLOAD_SERVLET_PATH = "/upload";
    private static final String AJAX_JS =
            "webdriver(arguments[arguments.length - 1]);" + "\n" +
                    "$(arguments[0]).imageBlob().ajax('" + UPLOAD_SERVLET_PATH + "');";
    private static final String AJAX_WITH_DATA_JS =
            "webdriver(arguments[arguments.length - 1]);" + "\n" +
                    "$(arguments[0]).imageBlob().formData(arguments[1]).ajax('" + UPLOAD_SERVLET_PATH + "');";

    private JettyUploadServer server = null;
    private WebDriver driver = null;

    ////////////////////
    // TESTNG LIFECYCLE
    ////////////////////

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception {
        server = JettyUploadServer.Builder.newInstance()
                .withPort(PORT)
                .withResourceBase(RESOURCE_BASE)
                .withServletPath(UPLOAD_SERVLET_PATH)
                .build()
                .start();
        driver = Browser.CHROME.asHeadless(true).initialize()
                .getDriver();
        driver.manage().timeouts().pageLoadTimeout(WEBDRIVER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(WEBDRIVER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
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

    @DataProvider(name = "red_dot")
    public Object[][] red_dot() {
        browseToForm();
        return findElementAndSourcePairs("div#red_dot img");
    }

    @DataProvider(name = "withSpaces")
    public Object[][] withSpaces() {
        browseToForm();
        return findElementAndSourcePairs("div.testcase.withSpaces img");
    }

    @DataProvider(name = "urlEncoded")
    public Object[][] urlEncoded() {
        browseToForm();
        return findElementAndSourcePairs("div.testcase.urlEncoded img");
    }

    //////////////
    // TEST CASES
    //////////////

    @Test(description = "Test image upload content.",
            dataProvider = "all")
    public void testUploadContent(WebElement img, File sourceImage) throws IOException {
        FileResponse fileResponse = ajax(img);
        Assert.assertTrue(fileResponse.getLength() > 0,
                "Image upload was empty.\n");
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
        FileResponse fileResponse = ajax(img);
        String actual = fileResponse.getFileType();
        Assert.assertEquals(expected, actual,
                "Wrong MIME type.\n");
    }

    @Test(description = "Test image uploads with name attributes.",
            dataProvider = "withNames")
    public void testImagesWithFileNames(WebElement img, File sourceImage) throws IOException {
        FileResponse fileResponse = ajax(img);
        String expected = sourceImage.getName();
        String actual = fileResponse.getFileName();
        Assert.assertEquals(expected, actual,
                "Uploaded image had wrong filename.\n");
    }

    @Test(description = "Test image uploads without name attributes.",
            dataProvider = "withoutNames")
    public void testImagesWithoutFileNames(WebElement img, File sourceImage) throws IOException {
        FileResponse fileResponse = ajax(img);
        // img element does not have a "name" attribute; it should use default name
        String expected = ((JavascriptExecutor) driver).executeScript(
                "return jQuery.fn.imageBlob.defaultImageName;", ArrayUtils.EMPTY_OBJECT_ARRAY).toString();
        String actual = fileResponse.getFileName();
        Assert.assertEquals(expected, actual,
                "Uploaded image had wrong filename.\n");
    }

    @Test(description = "Test image uploads without name attributes, overriding the default name.",
            dataProvider = "withoutNames")
    public void testImagesWithoutFileNamesCustom(WebElement img, File sourceImage) throws IOException {
        // use a custom default name
        String expected = "0xDEADBEEF";
        ((JavascriptExecutor) driver).executeScript(
                "jQuery.fn.imageBlob.defaultImageName = '" + expected + "';", ArrayUtils.EMPTY_OBJECT_ARRAY);
        FileResponse fileResponse = ajax(img);
        String actual = fileResponse.getFileName();
        Assert.assertEquals(expected, actual,
                "Uploaded image had wrong filename.\n");
    }

    @Test(description = "Test image uploads with additional form data.",
            dataProvider = "all")
    public void testWithFormData(WebElement img, File sourceImage) throws IOException {
        Map<String, String> formData = new HashMap<>(1);
        String param = "FOO_PARAM";
        String expected = "FOO_VAL";
        formData.put(param, expected);
        FileResponse fileResponse = ajaxWithData(img, formData);
        Assert.assertEquals(
                expected,
                fileResponse.getParams().get(param)[0],
                "Additional form data not found.");
    }

    //////////////////
    // HELPER METHODS
    //////////////////

    private void browseToForm() {
        driver.get(URL);
    }

    private FileResponse ajax(WebElement img)
            throws IOException {
        Object obj = ((JavascriptExecutor) driver).executeAsyncScript(
                AJAX_JS, img);
        return getResponse(obj);
    }

    private FileResponse ajaxWithData(WebElement img, Map<String, String> formData)
            throws IOException {
        Object obj = ((JavascriptExecutor) driver).executeAsyncScript(
                AJAX_WITH_DATA_JS, img, formData);
        return getResponse(obj);
    }

    private FileResponse getResponse(Object obj) throws IOException {
        // a String indicates success, a List indicates failure
        if (obj instanceof List) {
            Assert.fail(((List<?>) obj).iterator().next().toString());
        }
        ObjectMapper mapper = new ObjectMapper();
        List<FileResponse> responseFiles = mapper.readValue(obj.toString(),
                new TypeReference<List<FileResponse>>() {
                });
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
            elmArray[i] = new Object[]{elm, sourceImage};
        }
        return elmArray;
    }
}
