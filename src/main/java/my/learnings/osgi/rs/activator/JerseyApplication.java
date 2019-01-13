package my.learnings.osgi.rs.activator;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import my.learnings.osgi.rs.JsonResource;
import my.learnings.osgi.rs.StatusResource;

public class JerseyApplication extends Application {
    
    private static Logger logger = LoggerFactory.getLogger(JerseyApplication.class.getName());
    @Override
    public Set<Class<?>> getClasses() {
        logger.info("--Returning all the claess-");
        Set<Class<?>> result = new HashSet<Class<?>>();
        result.add(JsonResource.class);
        result.add(StatusResource.class);
        return result;
    }
}
