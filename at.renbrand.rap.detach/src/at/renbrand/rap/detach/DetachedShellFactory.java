package at.renbrand.rap.detach;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Factory class to create an unleashed shell which opens a new browser window.
 * 
 * @author brandstetter
 */
public final class DetachedShellFactory {
	
	/**
     * @throws IllegalStateException always, because instantiation not allowed
     */
    private DetachedShellFactory() {
		throw new IllegalStateException("Instantiation not allowed!");
	}

    /**
     * Creates a new shell with the given style.
     * @param style the Shell-styles to apply
     * @return a new unleashed shell
     */
	public static UnleashedShell create(int style){
		return new UnleashedShell(new Shell(style));
	}
	
	/**
	 * Creates a new shell with the given parent and style.
	 * @param parent the parent of the new shell instance
	 * @param style the Shell-styles to apply
	 * @return a new unleashed shell
	 */
	public static UnleashedShell create(Shell parent, int style){
		return new UnleashedShell(new Shell(parent, style));
	}
	
	/**
	 * Creates a new shell with the given parent and style.
	 * @param parent the parent of the new shell instance
	 * @param style the Shell-styles to apply
	 * @return a new unleashed shell
	 */
	public static UnleashedShell create(Display display, int style){
		return new UnleashedShell(new Shell(display, style));
	}
}
