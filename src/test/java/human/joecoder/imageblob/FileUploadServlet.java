package human.joecoder.imageblob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * File Upload Servlet for unit testing.
 * @author joe
 */
@MultipartConfig
@SuppressWarnings("serial")
public class FileUploadServlet extends HttpServlet {

	// thread-safe objects
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
	
	private File uploadDirectory;
	
	/**
	 * Constructor.
	 * @param uploadDir
	 */
	public FileUploadServlet(File uploadDir) {
		this.uploadDirectory = uploadDir;
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Collection<Part> parts = req.getParts();
		if (!parts.isEmpty()) {
			List<FileResponse> files = new ArrayList<>(parts.size());
			for (Part part : parts) {
				if (part != null && part.getContentType() != null) {
					String fileName = getFilename(part);
					File file = writeFile(fileName, part.getInputStream());
					FileResponse fileResponse = new FileResponse();
					fileResponse.setFileName(fileName);
					fileResponse.setFileType(part.getContentType());
					fileResponse.setLength(file.length());
					try (FileInputStream fis = new FileInputStream(file)) {
						fileResponse.setBase64(Base64Utils.encodeBase64(fis));
					}
					files.add(fileResponse);
				}
			}
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			Map<String, List<FileResponse>> filesMap = new HashMap<>(1);
			filesMap.put("files", files);
			writer.writeValue(resp.getOutputStream(), filesMap);
		}
	}

	private File writeFile(String filename, InputStream inputStream) throws IOException {
		File file = File.createTempFile(filename, null, uploadDirectory);
		file.deleteOnExit();
		try (FileOutputStream outputStream = new FileOutputStream(file)) { 
			IOUtils.copy(inputStream, outputStream);
		}
		return file;
	}
	
    private static String getFilename(Part part) {
    	String retval = null;
        for (String contentDisposition : part.getHeader("content-disposition").split(";")) {
            if (contentDisposition.trim().startsWith("filename")) {
                retval= contentDisposition.substring(contentDisposition.indexOf('=') + 1).trim().replace("\"", "");
                // IE fix
                retval = retval.substring(retval.lastIndexOf('/') + 1).substring(retval.lastIndexOf('\\') + 1);
            }
        }
        return retval;
    }
}