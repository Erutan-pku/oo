import java.io.*;

public class Log {
	BufferedWriter bw;
	public Log(String fileName) {
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), "utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void writeLine(String line) {
		try {
			bw.write(line + "\n");
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
