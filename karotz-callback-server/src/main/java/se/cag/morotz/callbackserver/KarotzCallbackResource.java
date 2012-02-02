/*
 * @author Daniel Marell
 *         Created 2012-01-26 14:01
 */
package se.cag.morotz.callbackserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.*;

@Path("/karotz-callback")
public class KarotzCallbackResource {
    private Logger log = LoggerFactory.getLogger(getClass());

    @GET
    @Produces("text/plain")
    @Path("ping")
    public String ping() {
        String message = "karotz-callback app is alive";
        log.info(message);
        return message;
    }

    @POST
    @Consumes("image/png")
    @Path("image")
    public Response putImage(InputStream stream) {
        log.trace("putImage");
        try {
            writeToFile(stream, new File("image.png"));
        } catch (IOException e) {
            String msg = "Failed to write image file:" + e.getMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } finally {
            try {
                stream.close();
            } catch (IOException ignore) {
            }
        }
        log.info("wrote file image.png");
        return Response.ok().build();
    }

    public void writeToFile(InputStream is, File file) throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)));
            int c;
            while ((c = is.read()) != -1) {
                out.writeByte(c);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}

