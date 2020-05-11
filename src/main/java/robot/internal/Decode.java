package robot.internal;

import java.util.ArrayList;

import robot.robot.Point;

/**
 * @author 0.2
 *
 */
public class Decode {
    private static final double DEFAULT_PRECISION = 1E5;

    public static ArrayList<Point> decodePolyLineString(String encoded) {
        return decode(encoded, DEFAULT_PRECISION);
    }

    /**
     * Precision should be something like 1E5 or 1E6. For OSRM routes found precision was 1E6, not the original default
     * 1E5.
     *
     * @param encoded
     * @param precision
     * @return
     */
    public static ArrayList<Point> decode(String encoded, double precision) {
    	ArrayList<Point> track = new ArrayList<Point>();
        int index = 0;
        int lat = 0, lng = 0;

        while (index < encoded.length()) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            Point p = new Point((double) lat / precision, (double) lng / precision);
            track.add(p);
        }
        return track;
    }
    
    /**
	 * 
	 * Read all the json input to find the place of each element
	 * 
	 * @param value -- json input with lat and lng values
	 * @return
	 */
	public static ArrayList<Point> getPointFromInput(String[] value) {
		ArrayList<Point> seed = new ArrayList<Point>();
		for (String s : value) {
			for (String split : s.split(",“")) {
				if (split.indexOf("location") != -1) {
					seed.add(addPoint(split));
				}
			}
		}
		return seed;
	}

	/**
	 * @param info -- each the json input with lat and lng values
	 * @return
	 */
	private static Point addPoint(String info) {
		try {
			String x = info.substring(info.indexOf("lat") + 5, info.indexOf(",", info.indexOf("lat")));
			String y = info.substring(info.indexOf("lng") + 5, info.indexOf("}", info.indexOf("lng")));

			return new Point(Double.parseDouble(x), Double.parseDouble(y));
		} catch (Exception e) {
			System.err.println("Error in coordinate transformation: " + e.getMessage());
		}
		return null;
	}
}
