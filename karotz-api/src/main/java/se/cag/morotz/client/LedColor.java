/*
 * @author Daniel Marell
 *         Created 2012-01-12 17:44
 */
package se.cag.morotz.client;

public enum LedColor {
    RED("FF0000"),
    GREEN("00FF00"),
    BLUE("0000FF"),
    YELLOW("FFFF00");

    private String code;

    /**
     * Constructor.
     *
     * @param code RGB color string like "00FF00"
     */
    private LedColor(String code) {
        this.code = code;
    }

    /**
     * Gets color code.
     *
     * @return color code
     */
    public String getCode() {
        return code;
    }
}
