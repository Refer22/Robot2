package robot.start;

import java.util.ArrayList;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import robot.api.EntryPoint;
import robot.internal.Decode;
import robot.robot.Point;
import robot.robot.Robot;

/**
 * @author 0.2
 *
 */
public class StartTask {
	public static final TSync sync = new TSync();
	public static boolean stop = false;

	private static final int PORT_NR = 7070;
	private static final String ENDPOINT_1 = "/*";

	public static Robot robot = null;
	public static int distanceBetweenMeasures = 100;

	/**
	 * @param args -- input
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ArrayList<Point> init = new ArrayList<Point>();
		
		init = Decode.getPointFromInput(args);
		if (init.size() == 0) {
			throw new Exception("Are needed at least is needed one point to guide");
		} else {
			robot = new Robot(init, sync);
			startApp();
		}
	}

	/**
	 * Main logic
	 * 
	 * @throws Exception
	 */
	public static void startApp() throws Exception {
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		Server jettyServer = new Server(PORT_NR);
		jettyServer.setHandler(context);
		ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, ENDPOINT_1);
		jerseyServlet.setInitOrder(0);
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", getAvailableApi());

		Thread robotThread = new Thread(robot);

		try {
			jettyServer.start();
			robotThread.start();
			jettyServer.join();
			robotThread.join();
		} finally {
			jettyServer.destroy();
		}
	}

	/**
	 * @return the available api for the robot
	 */
	private static String getAvailableApi() {
		return EntryPoint.class.getCanonicalName();
	}

	public static class TSync {
		public void pause() throws InterruptedException {
			synchronized (this) {
				wait();
			}
		}

		/**
		 * @throws InterruptedException
		 */
		public void restart() throws InterruptedException {
			synchronized (this) {
				StartTask.stop = false;
				notify();
			}
		}
	}

	/**
	 * 
	 */
	public static void callRestart() {
		try {
			sync.restart();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
