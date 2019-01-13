package my.learnings.osgi.rs.activator;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Activator implements BundleActivator {
    private static Logger logger = LoggerFactory.getLogger("my.learnings.osgi.rs.activator");

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting the bundle");
        ServletContextHandler ch = new ServletContextHandler();
        ch.setContextPath("/");
        ServletHolder holder = new ServletHolder(new ServletContainer());
        holder.setInitParameter("javax.ws.rs.Application", JerseyApplication.class.getName());
        ch.addServlet(holder, "/*");
        context.registerService(ContextHandler.class.getName(), ch, null);
    }


    @Override
    public void stop(BundleContext context) throws Exception {

    }
}