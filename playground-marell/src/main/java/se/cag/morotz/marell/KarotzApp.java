/*
 * @author Daniel Marell
 *         Created 2012-01-12 11:48
 */
package se.cag.morotz.marell;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cag.morotz.client.KarotzClient;
import se.cag.morotz.client.KarotzException;
import se.cag.morotz.client.LedColor;

import java.io.IOException;
import java.net.InetAddress;

public class KarotzApp {
    private static final String API_KEY = "6b1ad6c6-e8c2-4947-8397-ce199321c5af";
    private static final String SECRET_KEY = "163b57bd-4d13-4c94-ad43-bf54a9eb17e2";
    private static final String INSTALLID = "519affa9-d28f-4e40-a907-2b942dde5e4f";
    private static Logger log = LoggerFactory.getLogger(KarotzClient.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        log.info("KarotzClientApp started");

        String ip = InetAddress.getLocalHost().getHostAddress();
        int port = 9998;
        String baseUri = "http://" + ip + ":" + port + "/";
        
        String marellBaseUri = "http://marell.se/lab/";

        HttpServer server = HttpServerFactory.create(baseUri,
                new PackagesResourceConfig("se.cag.morotz.callbackserver"));
        server.start();

        KarotzClient c = new KarotzClient(API_KEY, SECRET_KEY, INSTALLID);

        try {
            c.startInteractiveMode();
            log.info("startInteractiveMode ok");

            c.setLedColor(LedColor.BLUE);
            log.info("setLedColor ok");

            
            c.takePicture(marellBaseUri + "karotz-callback/image");
            log.info("takePicture ok");

            Thread.sleep(3000);

            c.stopInteractiveMode();
            log.info("stopInteractiveMode ok");

            server.stop(0);
        } catch (KarotzException e) {
            log.error("client operation failed:" + e.getMessage());
            return;
        }

        log.info("exit");
    }
}
