package microsim.gui.shell;

import org.apache.commons.io.output.WriterOutputStream;

import java.io.OutputStream;
import java.io.Writer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * JAS custom logging appender to catch logs and write them into the JAS Console window.<br/><br/>
 * // todo expand the docs
 */

class JasConsoleAppender extends ConsoleHandler {

    public JasConsoleAppender(Formatter formatter, OutputStream os) {
		super.setFormatter(formatter);
		super.setOutputStream(os);
    }

    public JasConsoleAppender(Formatter formatter, Writer writer) {
		super.setFormatter(formatter);
		super.setOutputStream(new WriterOutputStream(writer, "UTF-8"));
    }

	@Override
    public void publish(LogRecord record) {
		super.publish(record);
		if (MicrosimShell.currentShell != null) MicrosimShell.currentShell.log(record.getMessage());
		else System.out.println(record.getMessage());
    }
}
