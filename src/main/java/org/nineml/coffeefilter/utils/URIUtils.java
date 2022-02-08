package org.nineml.coffeefilter.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtils {
    private static Boolean isWindows = null;

    private static boolean checkWindows() {
        if (isWindows == null) {
            String os = System.getProperty("os.name", "unknown").toLowerCase();
            isWindows = os.contains("win");
        }
        return isWindows;
    }

    private static String windowsPathURI(String uri) {
        if (!checkWindows()) {
            return uri;
        }
        String fixSlashes = uri.replaceAll("\\\\", "/");
        if (fixSlashes.length() >= 2 && fixSlashes.charAt(1) == ':') {
            return "file:///" + fixSlashes;
        }
        return fixSlashes;
    }

    /**
     * Creates a URI for the users current working directory.
     *
     * In order that this method should neither raise an exception nor return <code>null</code>,
     * if the <code>user.dir</code> system property cannot be converted into a URI, the
     * file URI "<code>file:///</code>" is returned.
     *
     * @return a file: URI for the current working directory
     */
    public static URI cwd() {
        String dir = windowsPathURI(System.getProperty("user.dir"));
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        try {
            return URIUtils.newURI(dir);
        } catch (URISyntaxException ex) {
            return URI.create("file:///"); // ¯\_(ツ)_/¯
        }
    }

    /**
     * Create a new URI, attempting to deal with the vagaries of file: URIs.
     *
     * Given something that looks like a file: URI or a path, return
     * a file: URI with a consistent number of leading "/". characters.
     * Any number of leading slashes are interpreted the same way.
     * (This is slightly at odds with specs, as file:///////path should
     * probably be an error. But this method "fixes it" to file:///path.)
     *
     * Strings that don't begin file: or /, are constructed with new URI() without
     * preprocessing. This will construct URIs for other schemes if the href
     * parameter is valid.
     *
     * This method will encode query strings, but that's ok for this application.
     *
     * @param href The string to be interpreted as a URI.
     * @return A URI constructed from the href parameter.
     * @throws URISyntaxException if the string cannot be converted into a URI.
     */
    public static URI newURI(String href) throws URISyntaxException {
        // This is a tiny bit complicated because I'm trying to avoid creating additional
        // string objects if I don't have to. I wonder if the effort is worth it.
        // Since I haven't measured it, "no".
        if (href.startsWith("file:")) {
            int pos = 5;
            while (pos <= href.length() && href.charAt(pos) == '/') {
                pos++;
            }
            if (pos > 5) {
                pos--;
            } else {
                pos = 0;
                href = "/" + href.substring(5);
            }
            if (href.contains("#")) {
                int hashpos = href.indexOf('#');
                String base = href.substring(pos, hashpos);
                String fragid = href.substring(hashpos + 1);
                return new URI("file", "", base, fragid);
            } else {
                return new URI("file", "", href.substring(pos), null);
            }
        } else if (href.startsWith("/")) {
            if (href.contains("#")) {
                int hashpos = href.indexOf('#');
                String base = href.substring(0, hashpos);
                String fragid = href.substring(hashpos + 1);
                return new URI("file", "", base, fragid);
            } else {
                return new URI("file", "", href, null);
            }
        } else {
            return new URI(href);
        }
    }
}
