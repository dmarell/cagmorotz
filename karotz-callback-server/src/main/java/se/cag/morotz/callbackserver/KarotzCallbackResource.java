package se.cag.morotz.callbackserver;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;

/**
 * @author Daniel Marell
 *         Created 2012-01-26 14:01
 */
@Path("/karotz-callback")
public class KarotzCallbackResource {
    @Produces("text/plain")
    @Path("/image")
    public void putImage() {
        System.out.println("putImage");
    }

    public static void run() throws IOException {
        HttpServer server = HttpServerFactory.create("http://localhost:9998/");
        server.start();
    }
}
