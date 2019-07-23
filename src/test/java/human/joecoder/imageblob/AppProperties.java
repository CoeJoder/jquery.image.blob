package human.joecoder.imageblob;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Wrapper for the app.properties file.
 *
 * @author Joe Nasca
 */
public class AppProperties {

    private static final Logger LOG = Log.getLog();
    public static final String PROPERTIES_FILE = "app.properties";
    private static final String KEY_FIREFOX_DRIVER = "firefox_driver";
    private static final String KEY_CHROME_DRIVER = "chrome_driver";
    private static final String KEY_BROWSER = "browser";

    private static AppProperties INSTANCE = null;

    /**
     * Factory method.
     *
     * @return the #AppProperties singleton
     * @throws IOException Thrown when file not found or invalid properties file.
     */
    public static AppProperties getInstance() throws IOException {
        if (INSTANCE == null) {
            Path propFile = Paths.get(PROPERTIES_FILE);
            if (!Files.exists(propFile)) {
                throw new FileNotFoundException(PROPERTIES_FILE);
            }
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(propFile)) {
                props.load(in);
            }
            INSTANCE = new AppProperties(props);
        }
        return INSTANCE;
    }

    private final Properties props;

    private AppProperties(Properties props) throws IOException {
        this.props = props;
    }

    public String getFirefoxDriver() {
        return prop(KEY_FIREFOX_DRIVER);
    }

    public String getChromeDriver() {
        return prop(KEY_CHROME_DRIVER);
    }

    public Browser getBrowser() {
        return Browser.fromString(prop(KEY_BROWSER));
    }

    private String prop(String key) {
        return props.getProperty(key);
    }
}
