package at.renbrand.rap.workbench.demo;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * This workbench advisor creates the window advisor, and specifies
 * the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "at.renbrand.rap.workbench.demo.perspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}
	
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
	    configurer.setSaveAndRestore(true);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}
}
