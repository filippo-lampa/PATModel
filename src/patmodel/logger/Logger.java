package patmodel.logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import repast.simphony.engine.environment.RunEnvironment;

/**
 * This class provides a project wide logger, please use this instead of normal prints, following an example of how to use the class
 * Logger.getLogger().<Method>(messageString, printCurrentTimeTick, classname);
 * <Method> can be changed with either:
 * 	- Debug
 *  - Info
 *  - Warn
 *  - Error
 * Use the method wisely in order to allow for a simple use and to avoid having the console full of non meaningful data
 * 
 * Note: printCurrentTimeTick and/or classname can be omitted if not important, is really encouraged to avoid omitting them.
 * command to write to get the classname: this.getClass().getName()
 * Note: the classname command might change to <ClassName>.class.getName() if the class is static 
 */
public class Logger {
	
	private static Logger Instance = null;
	private Logger() {}
	
	public static Logger getLogger() {
		if (Instance == null) {
			Instance = new Logger();
		}
		return Instance;
	}
	
	public void Log(LogLevel logLevel, String msg, boolean printCurrentTimeTick, String classname) {
		LogLevel minSeverity = (LogLevel) RunEnvironment.getInstance().getParameters().getValue("minLogSeverity");
		if(logLevel.getSeverity()<minSeverity.getSeverity()) {
			return;
		}
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		DecimalFormat df = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.ITALIAN));
		
		StringBuilder sb = new StringBuilder();
		LocalDateTime now = LocalDateTime.now();
		sb.append(dtf.format(now));
		if (printCurrentTimeTick) {
			sb.append(' ').append("Time Tick: ").append(df.format(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()));
		}
		sb.append(' ').append(logLevel.toString());
		if (!classname.isBlank()) {
			sb.append(' ').append(classname);
		}
			
		sb.append(" - ").append(msg);
		
		System.out.println(sb.toString());
	}
	
	public void Log(LogLevel logLevel, String msg, String classname) {
		Log(logLevel, msg, false, classname);
	}
	
	public void Log(LogLevel logLevel, String msg, boolean printCurrentTimeTick) {
		Log(logLevel, msg, printCurrentTimeTick, "");
	}
	
	public void Log(LogLevel logLevel, String msg) {
		Log(logLevel, msg, false, "");
	}
	
	public void Info(String msg, boolean printCurrentTimeTick, String classname) {
		Log(LogLevel.INFO, msg, printCurrentTimeTick, classname);
	}
	
	public void Info(String msg, String classname) {
		Info(msg, false, classname);
	}
	
	public void Info(String msg, boolean printCurrentTimeTick) {
		Info(msg, printCurrentTimeTick, "");
	}
	
	public void Info(String msg) {
		Info(msg, false, "");
	}
	
	public void Error(String msg, boolean printCurrentTimeTick, String classname) {
		Log(LogLevel.ERROR, msg, printCurrentTimeTick, classname);
	}
	
	public void Error(String msg, String classname) {
		Error(msg, false, classname);
	}
	
	public void Error(String msg, boolean printCurrentTimeTick) {
		Error(msg, printCurrentTimeTick, "");
	}
	

	public void Error(String msg) {
		Error(msg, false, "");
	}
	
	public void Warn(String msg, boolean printCurrentTimeTick, String classname) {
		Log(LogLevel.WARNING, msg, printCurrentTimeTick, classname);
	}
	
	public void Warn(String msg, String classname) {
		Warn(msg, false, classname);
	}
	
	public void Warn(String msg, boolean printCurrentTimeTick) {
		Warn(msg, printCurrentTimeTick, "");
	}
	
	public void Warn(String msg) {
		Warn(msg, false, "");
	}
	
	public void Debug(String msg, boolean printCurrentTimeTick, String classname) {
		Log(LogLevel.DEBUG, msg, printCurrentTimeTick, classname);
	}
	
	public void Debug(String msg, String classname) {
		Debug(msg, false, classname);
	}
	
	public void Debug(String msg, boolean printCurrentTimeTick) {
		Debug(msg, printCurrentTimeTick, "");
	}
	
	public void Debug(String msg) {
		Debug(msg, false, "");
	}
}
