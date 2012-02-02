/*
 * @author Daniel Marell
 *         Created 2012-01-11 13:16
 */
package se.cag.morotz.client;

public interface KarotzApi {
    void setLedColor(LedColor color) throws KarotzException;

    void setLedOff() throws KarotzException;

    void takePicture(String url) throws KarotzException;
}
