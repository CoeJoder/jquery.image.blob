package human.joecoder.imageblob;

import java.io.File;

import javax.servlet.MultipartConfigElement;
import javax.servlet.annotation.MultipartConfig;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * Simple Jetty server for testing jQuery Image Blob.
 * @author joe
 */
public class JettyUploadServer {
	
	/**
	 * Test Driver.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		File uploadDir = new File("tmp_uploads");
		new JettyUploadServer(new File("src/test/webapp"), uploadDir, "/upload").start().join();
	}
	
	private static final int DEFAULT_PORT = 8080;
	private static final Logger LOG = Log.getLog();
	
	private Server server;
	private String servletPath;
	private File resourceBase;
	private File uploadDirectory;

	/**
	 * Constructor.  Initializes and starts the Jetty server.
	 * @param uploadDirectory
	 * @throws Exception
	 */
	public JettyUploadServer(File resourceBase, File uploadDirectory, String servletPath) throws Exception {
		this(DEFAULT_PORT, resourceBase, uploadDirectory, servletPath);
	}

	/**
	 * Constructor.  Initializes and starts the Jetty server with the given
	 * file upload path.
	 * @param port
	 * @throws Exception
	 */
	public JettyUploadServer(int port, File resourceBase, File uploadDirectory, String servletPath) throws Exception {
		this.server = new Server(port);
		this.resourceBase = resourceBase;
		this.servletPath = servletPath;
		this.uploadDirectory = uploadDirectory;
	}

	/**
	 * Starts the embedded Jetty server.
	 * @throws Exception
	 */
	public JettyUploadServer start() throws Exception {
		initServlets();
		server.start();
		return this;
	}
	
	/**
	 * Joins the current thread to the Jetty server thread.
	 * @throws Exception
	 */
	public JettyUploadServer join() throws Exception {
		server.join();
		return this;
	}
	
	/**
	 * Stops the Jetty server.
	 */
	public void stop() throws Exception {
		server.stop();
	}
	
	private void initServlets() {
		// init servlet tree
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		
        context.setContextPath("/");
        context.setWelcomeFiles(new String[] { "form.html" });
        
        // dynamic file upload servlet
        ServletHolder uploadServletHolder = new ServletHolder("upload", new FileUploadServlet(uploadDirectory));
        // setup multipart config (servlet spec 3.0)
		MultipartConfig multipartConfig = FileUploadServlet.class.getAnnotation(MultipartConfig.class);
		MultipartConfigElement multipartConfigElm = new MultipartConfigElement(multipartConfig);
		uploadServletHolder.getRegistration().setMultipartConfig(multipartConfigElm);
		context.addServlet(uploadServletHolder, servletPath);
		
		LOG.info("Serving file upload servlet at \"/upload\"");
        
        // static default servlet for root content (per servlet spec; must be last)
        ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
        defaultServlet.setInitParameter("resourceBase", resourceBase.getAbsolutePath());
        defaultServlet.setInitParameter("dirAllowed", "true");
        context.addServlet(defaultServlet, "/");
        
        LOG.info("Serving static content at root \"/\"");
        
        server.setHandler(context);
	}
}
