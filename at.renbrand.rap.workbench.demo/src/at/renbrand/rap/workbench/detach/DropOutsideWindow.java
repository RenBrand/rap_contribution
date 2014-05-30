package at.renbrand.rap.workbench.detach;

import static at.renbrand.rap.detach.ScriptUtils.loadScriptResource;
import static at.renbrand.rap.detach.ScriptUtils.registerResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * A helper class which creates a RemoteObject to transfer Drag&Drop related data.
 */
class DropOutsideWindow {

    private static final String HANDLER_SCRIPT = "DropOutsideWindowHandler.js";
    
    private static final String REMOTE_TYPE = "at.renbrand.dnd.dropOutsideWindow";
    
    private final RemoteObject remoteObject;
    
    private static enum TAttribute {
        DROP_AT_X("dropAtX"),
        DROP_AT_Y("dropAtY");
        
        private final String propertyName;
        
        private TAttribute(String propertyName){
            this.propertyName = Objects.requireNonNull(propertyName);
        }
        
        public int getInt(JsonObject properties, int defaultValue){
            JsonValue value = properties.get(propertyName);
            
            if( value != null && value.isNumber() ){
                return value.asInt(); 
            }
            
            return defaultValue;
        }
    }
    
    interface Listener {
        void dropOut(int x, int y);
    }
    
    private final List<Listener> listener = new ArrayList<>();
    
    private DropOutsideWindow() {
        registerResource(HANDLER_SCRIPT, DropOutsideWindow.class);
        loadScriptResource(HANDLER_SCRIPT);
        
        Connection connection = RWT.getUISession().getConnection();
        remoteObject = connection.createRemoteObject(REMOTE_TYPE);
        remoteObject.setHandler(new AbstractOperationHandler() {
            private static final long serialVersionUID = -2253812399897873857L;
            
            @Override
            public void handleCall(String method, JsonObject parameters) {
                switch (method){
                    case "dropOutsideWindow":
                        int dropAtX = TAttribute.DROP_AT_X.getInt(parameters, 0);
                        int dropAtY = TAttribute.DROP_AT_Y.getInt(parameters, 0);
                        
                        System.out.println("time to open a window at: " + dropAtX + "x" + dropAtY);
                        for( Listener l : listener ){
                            l.dropOut(dropAtX, dropAtY);
                        }
                        break;
                        
                    default: 
                        System.out.println("unknown method: " + method + " params: " + parameters);
                        break;
                        
                }
            }
        });
    }
    
    void addDropListener( Listener l ){
        listener.add(l);
    }
    
    void removeDropListener( Listener l ){
        listener.remove(l);
    }
    
    /**
     * Creates a new browser window at the requested coordinates 
     * @param x the client X location
     * @param y the client Y location
     * @param page the WorkbenchPage held by the detached window
     * @param toDetach the tabs/parts which should be held there (more for debugging)
     * @return
     */
    public StackPresentation detachWindow( int x, int y, IWorkbenchPage page, CTabItem[] toDetach ){
        DetachedWindow detachedWindow = new DetachedWindow(page, this);
        StackPresentation newStack = detachedWindow.create();
        
        JsonObject styleParameter = new JsonObject();
        styleParameter.add("x", x);
        styleParameter.add("y", y);
        
        Point p = Display.getCurrent().getActiveShell().getSize();
        styleParameter.add("width", p.x);
        styleParameter.add("heigth", p.y);
        
        JsonArray partsParameter = new JsonArray();
        for( CTabItem tab : toDetach ){
            partsParameter.add(tab.getText());
        }
        
        JsonObject parameter = new JsonObject();
        parameter.add("style", styleParameter );
        parameter.add("parts", partsParameter );
        parameter.add("newShell", WidgetUtil.getId(detachedWindow.getShell()));
        
        remoteObject.call("detachWindow", parameter);
        
        detachedWindow.open();
        
        return newStack;
    }
    
    /**
     * Moves the DOM element the given control to the given shell which should be already
     * displayed in the new Browser-Window.
     * 
     * @param window the new shell which should be displayed already in the new browser window
     * @param control the control which should also be moved to the new browser window
     */
    void ensureHTMLLocation(Shell window, Control control){
        if( window == null || control == null ) return;
        
        JsonObject controlParameters = new JsonObject();
        controlParameters.add("shellID", WidgetUtil.getId(window));
        controlParameters.add("controlID", WidgetUtil.getId(control));
        
        remoteObject.call("ensureHTMLLocation", controlParameters);
    }
    
    public static DropOutsideWindow getInstance(){
        return SingletonUtil.getSessionInstance(DropOutsideWindow.class);
    }
}
