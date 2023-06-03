package patmodel.logger;

import repast.simphony.parameter.StringConverter;

public class LogLevelConverter implements StringConverter<LogLevel>{
	@Override
	public String toString(LogLevel obj) {
		return obj.toString();
	}

	@Override
	public LogLevel fromString(String strRep) {
		return LogLevel.fromString(strRep);
	}
}
