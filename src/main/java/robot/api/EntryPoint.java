package robot.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import robot.internal.Decode;
import robot.internal.Reports;
import robot.start.StartTask;

/**
 * @author 0.2
 *
 */
@Path("/robot")
public class EntryPoint {

	/**
	 * @return
	 */
	@GET
	@Path("start")
	@Produces(MediaType.TEXT_PLAIN)
	public String start() {
		StartTask.stop = false;
		StartTask.callRestart();

		return "204 No Content";
	}

	/**
	 * @return
	 */
	@GET
	@Path("stop")
	@Produces(MediaType.TEXT_PLAIN)
	public String stop() {
		StartTask.stop = true;

		return "204 No Content";
	}

	/**
	 * @return
	 */
	@GET
	@Path("report")
	@Produces(MediaType.TEXT_PLAIN)
	public String report() {
		return Reports.generateHistoricReport();
	}

	/**
	 * @param value // json input has start param
	 */
	@POST
	@Path("re-route")
	@Produces(MediaType.TEXT_PLAIN)
	public void route(String value) {
		StartTask.robot.reRoute(Decode.decodePolyLineString(value));
	}
}
