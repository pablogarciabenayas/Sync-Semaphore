import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;

public class FileSelector {

	public String selectFile() throws IOException {
		String result = null;
		try {
			JFileChooser loadEmp = new JFileChooser();// new dialog
			loadEmp.setDialogTitle("Seleccionar fichero de texto");
			File selectedFile = null;
			FileReader reader = null;

			if (loadEmp.showOpenDialog(loadEmp) == JFileChooser.APPROVE_OPTION) {
				selectedFile = loadEmp.getSelectedFile();
				if (selectedFile.canRead() && selectedFile.exists()) {
					reader = new FileReader(selectedFile);
				}
				result = selectedFile.getPath();
			}

		} catch (NullPointerException ex) {
			System.out.println("Open File Cancelled:\n" + ex.getMessage()
					+ "\n");
		}
		return result;

	}

	public String selectDirectory() {
		String result = null;
		try{
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Seleccionar directorio");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			  result = chooser.getSelectedFile().getPath();
			} else {
			  System.out.println("No Selection ");
			}
			
		}catch(Exception e){
			
		}
		
		return result;
	}
}
