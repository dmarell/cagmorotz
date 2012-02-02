/*
 * @author Daniel Marell
 *         Created 2012-01-11 14:38
 */
package se.cag.morotz.client;

public class KarotzException extends Exception {
    public KarotzException(String s) {
        super(s);
    }

    public KarotzException(Throwable throwable) {
        super(throwable);
    }
}
