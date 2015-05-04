package com.vmware.vim25.mo.samples;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WriteToLog {
	public static WriteToLog obj;
	BufferedWriter writer = null;

	private WriteToLog() throws IOException {
		File logFile = new File("vm.log");
		System.out.println(logFile.getCanonicalPath());
		writer = new BufferedWriter(new FileWriter(logFile, true));
	}

	public static WriteToLog getInstance() throws IOException {
		if (obj == null) {
			obj = new WriteToLog();
		}
		return obj;
	}

	public void write(String s) {

		try {
			writer.write(s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.flush();
				// Close the writer regardless of what happens...
				// writer.close();
			} catch (Exception e) {
			}
		}
	}
}

