package se.cag.morotz.client;

/**
 * @author Daniel Marell
 *         Created 2012-01-11 13:16
 */
public interface KarotzApi {
    void setLedColor(LedColor color) throws KarotzException;

    void setLedOff() throws KarotzException;
}
