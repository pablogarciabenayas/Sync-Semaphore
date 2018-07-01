import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

/**
 * 
 * @author Pablo
 *
 */

public class WebProcessor {

	private Semaphore writeErrorLogResource;
	private Semaphore maxSimulDownloads;
	private Semaphore downloadsVarResource;
	private Semaphore maxThreads;
	private int downloads;
	private String path; // Ruta directorio donde se almacenan las webs.
	private int nDown;
	private boolean terminate;

	/**
	 * 
	 * @param path
	 * @param nDown
	 *            : número de procesos de descarga que lanzara la aplicación
	 * @param maxDown
	 *            : número maximo de procesos simultaneos
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public WebProcessor(String dirPath, int nDown, int maxDown)
			throws IOException, InterruptedException {
		this.downloads = 0;
		this.path = dirPath;
		this.nDown = nDown;
		this.terminate = false;

		// Iniciación de los semaforos:

		// Semáforo número de threds
		maxThreads = new Semaphore(nDown);
		// Semáforo número máximo de descargas simultaneas permisos = maxDown
		maxSimulDownloads = new Semaphore(maxDown);
		// Semáforo para escribir log de error
		writeErrorLogResource = new Semaphore(1);
		// Semáforo para controlar acceso al recurso downloads
		downloadsVarResource = new Semaphore(1);
	}

	public void process(String fileName) throws IOException,
			InterruptedException {

		// Inicia thread que informa de descargas ok
		StatsThread thInfo = new StatsThread();
		thInfo.setName("thInfo");
		thInfo.start();

		// Inicia thread que permite parar descargas en cualquier momento
		// pulsando INTRO
		StopThread thStop = new StopThread();
		thStop.setName("thStop");
		thStop.start();

		String line = "";
		BufferedReader reader = new BufferedReader(new FileReader(fileName));

		while ((line = reader.readLine()) != null && !terminate) {
			maxThreads.acquire();
			Thread th = new Thread(new WebThread(line));
			th.start();
		}
		reader.close();
		thInfo.terminate();
		thStop.interrupt();
	}

	public void downloadUrl(String url) throws InterruptedException,
			IOException {
		maxSimulDownloads.acquire();
		// Creates a connection to given url
		Connection conn = Jsoup.connect(url);

		try {
			Response resp = conn.execute();

			if (resp.statusCode() != 200) {
				// Suelto permiso para controlar el máximo de descargas
				// simultaneas en caso de statusCode erróneo
				maxSimulDownloads.release();
				processErrorLog(url);
			} else {
				String html = conn.get().html();
				// Suelto permiso para controlar el máximo de descargas
				// simultaneas cuando termina de descargar OK
				maxSimulDownloads.release();
				// Name
				String[] splitName = url.split("\\W");
				String name = splitName[4] + "_" + splitName[5];

				writeHtmlFile(name, html);

			}
		} catch (IOException e) {
			maxSimulDownloads.release();
			processErrorLog(url);
		} finally {
			maxThreads.release();
		}
	}

	public void writeHtmlFile(String name, String content) throws IOException,
			InterruptedException {

		File newFile = new File(path + "/" + name + ".html");
		if (!newFile.exists()) {
			newFile.createNewFile();
		}

		FileWriter fw = new FileWriter(newFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();
		downloadsVarResource.acquire();
		downloads++;
		downloadsVarResource.release();
	}

	public void processErrorLog(String url) throws IOException,
			InterruptedException {

		writeErrorLogResource.acquire(); // Pido permiso para escribir en
											// fichero error_log.txt

		File logFile = new File(path + "/" + "error_log.txt");
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
		FileWriter fileWritter = new FileWriter(logFile.getAbsoluteFile(), true);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		bufferWritter.newLine();
		bufferWritter.write(url);
		bufferWritter.close();

		writeErrorLogResource.release(); // Suelto permiso para escribir en
											// fichero error_log.txt

	}

	private class WebThread implements Runnable {

		String line;

		public WebThread(String line) {
			this.line = line;

		}

		@Override
		public void run() {
			try {
				downloadUrl(this.line);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}

		}
	}

	private class StopThread extends Thread {

		public void run() {
				try {
					while(System.in.available() == 0){
						Thread.sleep(500);
					}
					terminate = true;
					System.out.println("Finalizando descargas en curso...");
				} catch (InterruptedException e) {
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private class StatsThread extends Thread {

		private boolean running = true;

		public void terminate() {
			this.running = false;
		}

		@Override
		public void run() {
			while (running) {
				try {
					Thread.sleep(3000);
					downloadsVarResource.acquire();
					System.out.println("Descargas correctas: " + downloads);
					downloadsVarResource.release();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
