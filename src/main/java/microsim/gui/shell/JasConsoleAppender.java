package microsim.gui.shell;

import java.io.OutputStream;
import java.io.Writer;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * JAS custom log4j appender to catch logs and write them into the
 * JAS Console window.<br/><br/>
 * 
 * If you want to enable logging on the JAS console simply add this appender to 
 * the log4j.properties file, like that:<br/>
 * <br/>
 * <i>log4j.appender.stdout=microsim.gui.shell.JasConsoleAppender</i><br/>
 * <i>log4j.appender.stdout.layout=org.apache.log4j.PatternLayout</i><br/>
 * <i>log4j.appender.stdout.layout.ConversionPattern=%d %p [%c] - %m%n</i><br/>
 * 
 * <p>
 * Title: JAS
 * </p>
 * <p>
 * Description: Java Agent-based Simulation library
 * </p>
 * <p>
 * Copyright (C) 2002-13 Michele Sonnessa
 * </p>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 * 
 * @author Michele Sonnessa
 * 
 */
public class JasConsoleAppender extends WriterAppender {

	public JasConsoleAppender() {		
	}

	public JasConsoleAppender(Layout layout, OutputStream os) {
		super(layout, os);		
	}

	public JasConsoleAppender(Layout layout, Writer writer) {
		super(layout, writer);
	}

	@Override
	public void append(LoggingEvent event) {
		super.append(event);
		if (MicrosimShell.currentShell != null)
			MicrosimShell.currentShell.log(event.getMessage().toString());
		else
			System.out.println(event.getMessage().toString());
	}

}
