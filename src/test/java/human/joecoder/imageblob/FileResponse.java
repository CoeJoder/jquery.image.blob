package human.joecoder.imageblob;

import java.util.Map;

/**
 * Model for AJAX file data response.
 * @author joe
 */
public class FileResponse {
	
    private String fileName;
    private String fileType;
    private Map<String, String[]> params;
    private byte[] base64;
    private long length;
    
    /**
     * Default Constructor.
     */
    public FileResponse() {
    	// for bean construction
    }

	public Map<String, String[]> getParams() {
		return params;
	}

	public void setParams(Map<String, String[]> params) {
		this.params = params;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getBase64() {
		return base64;
	}

	public void setBase64(byte[] base64) {
		this.base64 = base64;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}
}