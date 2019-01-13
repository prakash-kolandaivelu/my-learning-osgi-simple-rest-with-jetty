package my.learnings.osgi.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("jsonstatus")
public class JsonResource {

    private static Logger logger = LoggerFactory.getLogger(JsonResource.class);

    @GET
    @Produces("application/json")
    public String listConferences() {
        logger.info("Returning the status");
        return "{\"status\": \"success\"}";
    }
}
