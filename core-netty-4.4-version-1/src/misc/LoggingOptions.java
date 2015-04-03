package misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LoggingOptions {
	protected static Logger logger = LoggerFactory.getLogger("options");

	public LoggingOptions() {
		
	}
	public void sample() {
		logger.info("I am an info message");
		logger.warn("I can also warn you");

		MDC.put("one", "a-mdc-value");
		logger.info("I have a mdc value");
		MDC.remove("one");

		Marker marker = MarkerFactory.getMarker("marko");
		logger.error(marker, "an error message with a marker");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
