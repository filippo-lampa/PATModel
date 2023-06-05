package patmodel.logger;

public enum LogLevel {
	DEBUG("DEBUG", 0),
	INFO("INFO", 1),
	WARNING("WARN", 2),
	ERROR("ERROR", 3);
	private final String value;
	private final int severity;
	private LogLevel(String value, int severity) {
		this.value = value;
		this.severity = severity;
	}
	public int getSeverity() {
		return severity;
	}
	@Override
	public String toString() {
		return this.value;
	}
	public static LogLevel fromString(String logLevel) {
		for(LogLevel ll: LogLevel.values()) {
			if(logLevel.equals(ll.toString())) {
				return ll;
			}
		}
		return null;
	}
}
