package at.renbrand.rap.workbench.demo;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * This class controls all aspects of the application's execution
 * and is contributed through the plugin.xml.
 */
public class Application implements IApplication {

	public Object start(IApplicationContext context) throws Exception {
		Display display = PlatformUI.createDisplay();
		
		if( MessageDialog.openConfirm(display.getActiveShell(), "Simulated Login", "This should simulate a user login.\nClick 'OK' to go on.") ){
		    WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();
		    return PlatformUI.createAndRunWorkbench(display, advisor);		    
		}
		
		return Integer.valueOf(2);
	}

	public void stop() {
		// Do nothing
	}
}
