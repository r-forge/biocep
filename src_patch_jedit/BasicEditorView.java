import java.io.File;

import javax.swing.SwingUtilities;
import org.gjt.sp.jedit.jEdit;
import org.kchine.r.workbench.RGui;

public class BasicEditorView {
	private RGui _rgui;	
	private static boolean firstCall = true;
	public BasicEditorView(RGui rgui) {
		_rgui=rgui;		
		try {				
			if (firstCall) {
				firstCall=false;			
				new Thread(new Runnable(){
					public void run() {						
						try {
							File jEditDir = new File(_rgui.getInstallDir()+  "/basiceditor/jEdit");
							if (!jEditDir.exists()) {
								new File(_rgui.getInstallDir() + "/basiceditor/jEdit").mkdirs();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						System.setProperty("jedit.home", _rgui.getInstallDir() +  "/basiceditor/jEdit");
						jEdit.main( new String[] {	"-noserver", "-norestore", "-nosettings" , "-nogui" } , _rgui) ;
						
						while(!jEdit.isStartUpDone()) {
							try {Thread.sleep(100);} catch (Exception e) {}
						}
						
						SwingUtilities.invokeLater(new Runnable(){
							public void run() {
								jEdit.newView(null);								
							}							
						});
					}
				}).start();
			} else {
				jEdit.newView(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}
