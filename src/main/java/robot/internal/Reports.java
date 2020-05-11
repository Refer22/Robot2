package robot.internal;

import java.util.ArrayList;

import robot.robot.Measure;

/**
 * @author 0.2
 *
 */
public class Reports {
	private static ArrayList<Measure> measures = new ArrayList<Measure>();
	private static ArrayList<Measure> hMeasures = new ArrayList<Measure>();
	
	/**
	 * Report the current status in console
	 */
	public static void reportInLog() {
		String aux = generateCurrentReport();
		if (aux != "")	{
			System.out.println(aux);			
		}
	}

	/**
	 * Collect all the information of related measures and clear the measure list
	 * 
	 * @return last measures report
	 */
	public static String generateCurrentReport() {
		String aux = generateReport(measures);
		measures.clear();
		
		return aux;
	}

	/**
	 * Collect all the information of related measures and clear the measure list
	 * 
	 * @return full report
	 */
	public static String generateHistoricReport() {
		return generateReport(hMeasures);
	}
	
	/**
	 * @param report
	 * @return report
	 */
	private static String generateReport(ArrayList<Measure> report)	{
		String aux = "";

		if (report.size() != 0) {
			for (Measure element : report) {
				aux += report(element);
			}
		}
		
		return aux;
	}
	
	/**
	 * @param status -- measure to be thanslated
	 * @return translation as json
	 */
	private static String report(Measure status) {
		String report = "";

		report += "{\"timestamp\": " + status.getTime() + ", ";
		report += "\"location\": {";
		report += "\"lat\": " + status.getLocation().getX() + ", \"lng\": " + status.getLocation().getY() + "}, ";
		report += "\"level\": " + status.getQualityRange() + ", ";
		report += "\"source\": " + status.getStation_name() + "}\n";

		return report;
	}
	
	/**
	 * @param newOne -- new value added for report propouses
	 */
	public static void add_Report(Measure newOne)	{
		measures.add(newOne);
		hMeasures.add(newOne);
	}
	
	public static void resetHistoricMeasures()	{
		hMeasures.clear(); 
	}
}
