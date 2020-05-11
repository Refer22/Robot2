package robot.robot;

import java.util.ArrayList;

import robot.internal.Reports;
import robot.start.StartTask;
import robot.start.StartTask.TSync;

/**
 * @author 0.2
 *
 */
public class Robot implements Runnable {
	private final double R_EARTH = 6371;


	public double distance = 0;
	public double currentDistance = 0;
	
	private double _lastDistanceReport = 0;
	private int _minute = 0;
	private Place _plzBPalace = null;
	private Place _plzTEstation = null;

	private ArrayList<Point> _init;
	private int _currentSegment = 0;
	private Point _offset;
	private TSync _pc2;

	public Robot(ArrayList<Point> init, TSync pc) {
		_pc2 = pc;
		init(init);
	}

	/**
	 * @return current position of the robot
	 */
	private Point getOffset() {
		return _offset;
	}

	/**
	 * @return get seed init array
	 */
	private ArrayList<Point> getInit() {
		return _init;
	}

	/**
	 * @return current position of the robot
	 */
	private Point getCurrentPoint() {
		return _init.get(_currentSegment);
	}

	/**
	 * @return current position of the robot
	 */
	private Point getNextPoint() {
		return _init.get(_currentSegment + 1);
	}
	
	/**
	 * set new segment of the travel
	 * Prev. one should be reached
	 */
	private void nextSegment() {
		_currentSegment++;
	}

	/**
	 * 1st position and assign stations
	 * 
	 * @param init -- 1st position
	 */
	public void init(ArrayList<Point> init) {
		_init = init;
		try {
			_offset = _init.get(0);// 1st point offset is 0
		} catch (Exception e) {
			System.err.println("Error! No point has been added");
			System.exit(0);
		}
		if (init.size() > 1) {
			currentDistance = distanceBetweenCurrentAndNextPoint();
		}
		_plzBPalace = new Place("Buckingham Palace", new Point(51.50129, -0.14193));
		_plzTEstation = new Place("Temple Station", new Point(51.51085, -0.11416));
	}

	/**
	 * Main logic
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(60000);
				_minute++;
				distance += updateOffset();
				if (getInit().size() > 1 && distance > currentDistance) {
						currentDistance += distanceBetweenCurrentAndNextPoint();
						nextSegment();
				}
				isNearToPOI();
				isAppropiateTakeMeasure();
				isTimeReport();
			} catch (InterruptedException ex) {
				System.err.println("Error! Shutdown...");
				System.exit(0);
			} catch (ArrayIndexOutOfBoundsException ex2) {
				System.out.println(ex2.getMessage());
				StartTask.stop = true;
			}
			isStop();
		}
	}

	/**
	 * Check if stop of the robot has been requested. The robot will wait until next
	 * order
	 */
	private void isStop() {
		try {
			while (StartTask.stop) {
				System.out.println("Robot Waiting");
				_pc2.pause();
				System.out.println("Robot Restarted");
			}
		} catch (InterruptedException e) {
			System.err.println("Error! Robot Thread sundely stopped" + e.getMessage());
		}

	}

	/**
	 * Check if the robot is near to certain places
	 */
	private void isNearToPOI() {
		if (currentDistanceToKnownPoint(_plzBPalace.getLocation())) {
			Reports.add_Report((new Measure(getOffset(), Math.random() * 200, _plzBPalace.getName())));
		} else if (currentDistanceToKnownPoint(_plzTEstation.getLocation())) {
			Reports.add_Report((new Measure(getOffset(), Math.random() * 200, _plzTEstation.getName())));
		}
	}

	/**
	 * Check if the robot has advanced the defined distance between measures
	 */
	private void isAppropiateTakeMeasure() {
		if (distance - _lastDistanceReport > StartTask.distanceBetweenMeasures) {
			_lastDistanceReport = distance;
			Reports.add_Report((new Measure(getOffset(), Math.random() * 200)));
		}
	}

	/**
	 * Check if is time for new report and report in console
	 */
	private void isTimeReport() {
		if (_minute % 15 == 0) {
			Reports.reportInLog();
		}
	}

	/**
	 * reset current status of robot parameter to the new origin and restart robot
	 * thread
	 * 
	 * @param newInit -- array of point for the new routes
	 * @throws InterruptedException
	 */
	public void reRoute(ArrayList<Point> newInit) {
		_minute = 0;
		distance = 0;
		_currentSegment = 0;
		Reports.resetHistoricMeasures();
		if (StartTask.stop == true) {
			StartTask.callRestart();
		}
		init(newInit);
		System.out.println("Robot New route selected");
	}

	/**
	 * New position calculated by the average robot step per minute
	 * 
	 * number of km per degree = ~111km (111.32 in google maps, but range varies
	 * between 110.567km at the equator and 111.699km at the poles) 1km in degree =
	 * 1 / 111.32km = 0.0089 1m in degree = 0.0089 / 1000 = 0.0000089
	 * 
	 * @return distance traveled by the robot this last minute
	 * @throws ArrayIndexOutOfBoundsException
	 */
	private double updateOffset() throws ArrayIndexOutOfBoundsException {
		Triangle triangle = getAppropriateTriangle();
		double offsetX = triangle.get_catA() * 0.0000089;
		double offsetY = triangle.get_catO() * 0.0000089;
		double new_lat = getOffset().getX() + offsetX;
		double new_long = getOffset().getY() + offsetY / Math.cos(getOffset().getX() * 0.018);
		
		_offset = new Point(new_lat, new_long);

		return triangle.get_Hypotenuse();
	}

	/**
	 * Return the appropriate triangle based on the segment and know angle
	 * 
	 * @return
	 */
	private Triangle getAppropriateTriangle() {
		if (getInit().size() > 1) {
			try {
				return new Coordinates(getCurrentPoint(), getNextPoint()).calculateNextCord();
			} catch (IndexOutOfBoundsException e) {
				throw new ArrayIndexOutOfBoundsException("End point reached.");
			}
		} else {
			return new Coordinates(getOffset()).calculateNextCord();
		}
	}

	/**
	 * Check current position position distance
	 * 
	 * @return distance
	 */
	public double distanceBetweenCurrentAndNextPoint() {
		Point aux = new Point();

		aux.setX(Math.toRadians(getCurrentPoint().getX() - getNextPoint().getX()));
		aux.setY(Math.toRadians(getCurrentPoint().getY() - getNextPoint().getY()));

		return getSegment(aux, Math.cos(Math.toRadians(getNextPoint().getX())));
	}

	/**
	 * Check if position is the same of measure the distance
	 * 
	 * @param station
	 * @return
	 */
	public boolean currentDistanceToKnownPoint(Point station) {
		Point aux = new Point();

		if (station.getX() == getOffset().getX() && station.getY() == getOffset().getY()) {
			return true;
		}
		aux.setX(Math.toRadians(getOffset().getX() - station.getX()));
		aux.setY(Math.toRadians(getOffset().getY() - station.getY()));

		if (getSegment(aux, Math.cos(Math.toRadians(station.getX()))) < 100) {
			return true;
		}
		return false;
	}

	/**
	 * get the distance 
	 * @param point      -- point to be checked agais the robot position
	 * @param cosXTarget -- cos angle in radians of target to be compared
	 * @return
	 */
	private double getSegment(Point point, double cosXTarget) {
		double a = Math.sin(point.getX() / 2) * Math.sin(point.getX() / 2)
				+ Math.cos(Math.toRadians(getOffset().getX())) * cosXTarget * Math.sin(point.getY() / 2)
						* Math.sin(point.getY() / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = (R_EARTH * c) * 1000;
		distance = Math.pow(distance, 2) + 1;

		return Math.sqrt(distance);
	}
}
