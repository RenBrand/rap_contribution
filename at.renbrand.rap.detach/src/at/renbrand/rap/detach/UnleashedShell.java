package at.renbrand.rap.detach;

import static at.renbrand.rap.detach.ScriptUtils.loadScriptResource;
import static at.renbrand.rap.detach.ScriptUtils.registerResource;

import java.util.Objects;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * Represents a detached new browser window.
 * 
 * @author brandstetter
 *
 */
public final class UnleashedShell {
	
	/** The handler script to load. */
	private static final String HANDLER_SCRIPT = "DetachShellHandler.js";
	
	/** The type handler used to create the JavaScript instance object (singleton). */
	private static final String REMOTE_TYPE = "at.renbrand.rap.detach.unleashedShell";
	
	/** {@link Shell#setData(String, Object)} key which will be used to mark real {@link Shell} instances. */
	// this could later be used similar to "RWT.TOOLTIP_MARKUP_ENABLED"
	public static final String OPEN_DETACHED = "at.renbrand.rap.detach.openDetached";
	
	/**
	 * Enumeration of JavaScript {@code window.open()} specs parameter.
	 * (@see: http://www.w3schools.com/jsref/met_win_open.asp)
	 * 
	 * @author brandstetter
	 *
	 */
	public static enum BrowserOption {
		/** Whether or not to display the address field. (Opera only) */
		LOCATION("location"),
		
		/** Whether or not to display the menu bar. */
		MENUBAR("menubar"),
		
		/** Whether or not to add a status bar. */
		STATUSBAR("status"),
		
		/** Whether or not to display the browser toolbar. (IE and Firefox only) */
		TOOLBAR("toolbar");
		
		/** Spec-Option name used in the JavaScript call. */
		private final String specName;
		
		private BrowserOption(String specName){
			this.specName = Objects.requireNonNull(specName);
		}
		
		/**
		 * Activate the option.
		 * @param shell the shell on which the option should be activated.
		 */
		public void show(Shell shell){
			if( shell == null ) return;
			shell.setData(this.name(), Boolean.TRUE);
		}
		
		/**
		 * Deactivate the option.
		 * @param shell the shell on which the option should be deactivated.
		 */
		public void hide(Shell shell){
			if( shell == null ) return;
			shell.setData(this.name(), Boolean.FALSE);
		}
		
		/**
		 * Check if the open is activate on the given shell.
		 * @param shell the shell on which the option should be checked.
		 * @return true if the option is active; false otherwise
		 */
		public boolean isShown(Shell shell){
			if( shell == null ) return false;
			return Boolean.TRUE.equals(shell.getData(this.name()));
		}
	}
	
	/** The wrapped shell which will be displayed in a detached browser window. */
	private final Shell shell;
	
	/** A reference to the remote object, to make JavaScript calls. */
	private final RemoteObject remoteObject;
	
	/**
	 * Creates a new instance and registers the required JavaScript as needed.
	 * @param shell the shell to unleash
	 * @throws NullPointerException if the given shell is <code>null</code>
	 */
	UnleashedShell(Shell shell) {
		registerResource(HANDLER_SCRIPT, UnleashedShell.class);
		loadScriptResource(HANDLER_SCRIPT);
		
		this.shell = Objects.requireNonNull(shell, "No Shell given!");
		shell.setData(OPEN_DETACHED, Boolean.TRUE);
		
		Connection connection = RWT.getUISession().getConnection();
		remoteObject = connection.createRemoteObject(REMOTE_TYPE);
	}
	
	/**
	 * @return the Shell which will be displayed in the detached Browser window
	 */
	public Shell getShell(){
		return shell;
	}
	
	/**
	 * Specify the bounds of the shell, this will effect the open location of the new browser window
	 * if it's not already open.
	 * 
	 * @param bounds the bounds of the new window
	 * @return the this reference, to chain calls
	 */
	public UnleashedShell setBounds(Rectangle bounds){
		shell.setBounds(bounds);
		return this;
	}
	
	/**
	 * Specify the bounds of the shell, this will effect the open location of the new browser window
	 * if it's not already open.
	 * 
	 * @param x the x-coordinate of the new window
	 * @param y the y-coordinate of the new window
	 * @param width the width of the new window
	 * @param height the height of the new window
	 * @return the this reference, to chain calls
	 */
	public UnleashedShell setBounds(int x, int y, int width, int height){
		shell.setBounds(x, y, width, height);
		return this;
	}
	
	/**
	 * Specify the title to display.
	 * If a new browser window wasn't opened so far it will also be displayed a the browser title. 
	 * @param title the title to display
	 * @return the this reference, to chain calls
	 */
	public UnleashedShell setTitle(String title){
		shell.setText(title);
		return this;
	}
	
	/**
	 * Creates a new browser window and opens the wrapped shell in the new browser window.
	 */
	public void open(){
		callDetachScript();
		shell.open();
	}
	
	/**
	 * Call the appropriate JavaScript to open a new Browser-Window and move the content
	 * of the shell to the new Browser-Window.
	 */
	private void callDetachScript(){
		JsonObject parameter = new JsonObject();
		
		parameter.add("newShell", WidgetUtil.getId(shell));
		String title = shell.getText();
		if( !title.isEmpty() ){
			parameter.add("title", title);
		}
		parameter.add("specs", createOpenSpecs());
		
		shell.setLocation(0,0);  // if not set to 0,0 the shell inside the browser would be displayed anywhere (TODO: find better solution, maybe change setBounds())
		
		remoteObject.call("detachWindow", parameter);
	}
	
	/**
	 * Creates the {@code "specs"} parameter of the {@code window.open()}-JavaScript method.
	 * @return a JsonObject containing the {@code "specs"} settings
	 */
	private JsonObject createOpenSpecs(){
		Rectangle bounds = shell.getBounds();
		
		JsonObject openSpecs = new JsonObject();
		// position and size of the new browser window
		openSpecs.add("left", bounds.x)
				 .add("top", bounds.y)
				 .add("width", bounds.width)
				 .add("height", bounds.height);
		
		// display options for the new browser window (toolbar, menubar, ...)
		for( BrowserOption browserOpt : BrowserOption.values() ){
			openSpecs.add(browserOpt.specName, browserOpt.isShown(shell) ? "yes" : "no" );
		}
		
		return openSpecs;
	}
}
