import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * @author pablo
 *
 */

public class Main {

	private static String dFolder;
	private static String wFile;

	public static void main(String[] args) throws IOException,
			InterruptedException {
		

	    Scanner scanner = new Scanner(System.in);
	    System.out.print("Introduce el número máximo de threads que puede lanzar la aplicación: ");
	    while (!scanner.hasNextInt()) {
	    	scanner.next();
	    }
	    int maxTh = scanner.nextInt();

	    System.out.print("Introduce el número máximo de thread en descarga simultaneamente: ");
	    while (!scanner.hasNextInt()) {
	    	scanner.next();
	    }
	    int  maxSimultaneousTh= scanner.nextInt();

		FileSelector fileSelector = new FileSelector();
		dFolder = fileSelector.selectDirectory();
		
		System.out.println("Carpeta asignada para guardar -> \t"+dFolder);
		WebProcessor webProcessor = new WebProcessor(dFolder,maxTh,maxSimultaneousTh);
		wFile = fileSelector.selectFile(); 

		System.out.println("Fichero elegido con direcciones web -> \t" + wFile);
		System.out.println("\nEl programa procedera a descargar los ficheros html en la carpeta asiganda.");
		System.out.println("\nPuede parar las descargas en cualquier momento pulsando INTRO");
		System.out.println("\n------------------------------------------------");
		
		webProcessor.process(wFile); // procesa fichero de texto con las direcciones

		System.out.println("------------------------------------------------");
		System.out.println("Programa Finalizado");
	}

}
