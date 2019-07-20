package human.joecoder.imageblob;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Base64 utility methods
 *
 * @author joe
 */
public class Base64Utils {

    /**
     * Dump the InputStream contents into a Base64 encoded String.
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String encodeBase64String(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, buffer);
        return Base64.encodeBase64String(buffer.toByteArray());
    }

    /**
     * Dump the InputStream contents into a Base64 encoded byte buffer.
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] encodeBase64(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, buffer);
        return Base64.encodeBase64(buffer.toByteArray());
    }
}
