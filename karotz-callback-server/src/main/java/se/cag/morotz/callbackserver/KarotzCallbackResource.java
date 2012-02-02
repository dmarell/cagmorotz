/*
 * @author Daniel Marell
 *         Created 2012-01-26 14:01
 */
package se.cag.morotz.callbackserver;

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    @Consumes("multipart/mixed")
    @Path("image")
    public Response post(MultiPart multiPart) {
        log.info("Received post multipart/mixed,parts=" + multiPart.getBodyParts().size());
        for (BodyPart p : multiPart.getBodyParts()) {
            log.info("part: MediaType=" + p.getMediaType());
        }
        return Response.ok().build();
    }

    @POST
    @Consumes("image/jpeg")
    @Path("image")
    public Response uploadImage(File tmpFile) {
        log.info("Received uploadImage,tmpFile=" + tmpFile);
        File destFile = new File("/mnt/raid/public/public-downloads/karotzlab/karotz-" + dateAndTimeNow() + ".jpg");
        try {
            FileUtils.copyFile(tmpFile, destFile);
        } catch (IOException e) {
            String msg = "Failed to write image file:" + e.getMessage();
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        log.info("wrote image file " + destFile.getAbsolutePath());
        return Response.ok().build();
    }

    public static String dateAndTimeNow() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        return sdf.format(cal.getTime());
    }
}

