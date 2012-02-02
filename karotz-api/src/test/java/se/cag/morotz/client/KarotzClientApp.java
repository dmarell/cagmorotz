package se.cag.morotz.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Marell
 *         Created 2012-01-12 11:48
 */
public class KarotzClientApp {
    private static final String API_KEY= "6b1ad6c6-e8c2-4947-8397-ce199321c5af";
    private static final String	SECRET_KEY= "163b57bd-4d13-4c94-ad43-bf54a9eb17e2";
    private static final String	INSTALLID = "519affa9-d28f-4e40-a907-2b942dde5e4f";
    private static Logger log = LoggerFactory.getLogger(KarotzClient.class);

    public static void main(String[] args) throws InterruptedException {
        log.trace("KarotzClientApp started/trace");
        log.info("KarotzClientApp started/info");

        KarotzClient c = new KarotzClient(API_KEY, SECRET_KEY, INSTALLID);

        try {
            c.startInteractiveMode();
            log.info("startInteractiveMode ok");
            c.setLedColor(LedColor.BLUE);
            log.info("setLedColor ok");
            //c.stopInteractiveMode();
            //log.info("stopInteractiveMode ok");
        } catch (KarotzException e) {
            log.error("client operation failed:" + e.getMessage());
            return;
        }

        //Thread.sleep(1000);

        log.info("exit");
    }
}
