package apk.tool.patcher.util;


public class PathF {
    @SuppressWarnings("WeakerAccess")
    public static final char CPATHSEPARATOR = '/';
    public static final String SPATHSEPARATOR = "/";

    static String removeEndSlash(String path) {
        return (path.endsWith(SPATHSEPARATOR) && !path.equals(SPATHSEPARATOR)) ? path.substring(0, path.length() - 1) : path;
    }

    static boolean isFullPath(String path) {
        return path.startsWith(SPATHSEPARATOR);
    }

    static boolean isWildcard(String str) {
        return (str.indexOf('*') != -1 || str.indexOf('?') != -1);
    }

    /**
     * @param path Path to file
     * @return Name of file with extension
     */
    public static String pointToName(String path) {
        int lastIndexOf = path.lastIndexOf(CPATHSEPARATOR);
        return lastIndexOf == -1 ? path : path.substring(lastIndexOf + 1);
    }


    public static String getExt(String path) {
        String pointToName = pointToName(path);
        int lastIndexOf = pointToName.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return pointToName.substring(lastIndexOf + 1);
    }

    public static String addEndSlash(String path) {
        if (path.length() == 0 || path.endsWith(SPATHSEPARATOR)) {
            return path;
        }
        return path + SPATHSEPARATOR;
    }

    public static String removeNameFromPath(String path) {
        int lastIndexOf = path.lastIndexOf(CPATHSEPARATOR);
        if (lastIndexOf < 0) {
            return "";
        }
        return path.substring(0, lastIndexOf);
    }

}
