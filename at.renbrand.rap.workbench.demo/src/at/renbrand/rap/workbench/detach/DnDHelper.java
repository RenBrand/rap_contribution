package at.renbrand.rap.workbench.detach;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * A helper class which contains some helper methods to perform the Drag&Drop operations.
 */
@SuppressWarnings("restriction") // NOPMD PartPane has an access restriction but it is needed for our purpose
public final class DnDHelper {

    /**
     * @throws IllegalStateException always, because instantiation not allowed
     */
    private DnDHelper() {
        throw new IllegalStateException("Instantiation not allowed!");
    }

    /**
     * Very specific retrieve method which tries to call the <code>"getData"</code> method on the given object and
     * requires it to return an {@link IPresentablePart}.
     * 
     * <p>
     * Required in the special case of extracting the {@link IPresentablePart} from a CTabFolder, hence it is NOT public! 
     * </p>
     * 
     * @param tabItem
     * @return
     */
    private static IPresentablePart retrievePresentablePart(Object tabItem){
        return invokeSimplePublicMethod(IPresentablePart.class, tabItem, "getData");
    }
    
    /**
     * Very specific retrieve method which tries to call the <code>"getPane"</code> method on the given {@link IPresentablePart} and
     * requires it to return a {@link PartPane}.
     * 
     * <p>
     * Required in the special case of extracting the real PartPane from an {@link IPresentablePart}, hence it is NOT public! 
     * </p>
     * 
     * @param tabItem
     * @return
     */
    private static PartPane getPartPane(IPresentablePart presentablePart){
        return invokeSimplePublicMethod(PartPane.class, presentablePart, "getPane");
    }
    
    /**
     * Retrieves the {@link PartPane}s of the given List of {@link CTabItem}s. 
     * @param tabItems the list of {@link CTabItem}s from which the {@link PartPane}'s should be retrieved
     * @return the {@link PartPane}'s of the given {@link CTabItem}s
     */
    static PartPane[] getPartPane(List<CTabItem> tabItems){
        if( tabItems == null || tabItems.isEmpty() ) return new PartPane[0];
        
        List<PartPane> partPanes = new ArrayList<PartPane>(tabItems.size());
        
        for( CTabItem tabItem : tabItems ){
            PartPane pane = DnDHelper.getPartPane(DnDHelper.retrievePresentablePart(tabItem.getData()));
            if( pane != null ){
                partPanes.add(pane);
            }
        }
        
        return partPanes.toArray(new PartPane[partPanes.size()]);
    }
    
    /**
     * Helper to call a public method on an object which requires a special return type. 
     * @param returnType the type of the object which should be returned
     * @param obj the object on which the method should be searched and called
     * @param methodToCall the name of the public no argument method which should be called
     * @return the value returned from the method-call if it matches the given <code>returnType</code>,
     *         or <code>null</code> if
     *         <ul>
     *          <li>the object returned from the method-call can't be cast to the given return type</li>
     *          <li>the given object doesn't have the requested no argument method</li>
     *          <li>the method couldn't be invoked because of security exceptions or any other reflective method call exceptions</li>
     *         </ul>
     * @throws IllegalArgumentException if the given return type is <code>null</code> or the name of the method to call is <code>null</code> or empty
     */
    private static <T> T invokeSimplePublicMethod(Class<T> returnType, Object obj, String methodToCall){
        if( returnType == null ){
            throw new IllegalArgumentException("No return type given!");
        }
        
        if( methodToCall == null || methodToCall.isEmpty() ){
            throw new IllegalArgumentException("No method to call given!");
        }
        
        if( obj != null ){
            try{
                Method publicMethod = obj.getClass().getMethod(methodToCall);  // no check on public, but should be because I will not change the accessibility flag here!!!
                Object data = publicMethod.invoke(obj);
                if( !returnType.equals(Void.class) && returnType.isInstance(data) ){
                   return returnType.cast(data);
                }
            }catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                System.err.println("Couldn't retrieve " + returnType + " from object: " + obj.getClass() + "!");
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    /**
     * Helper method to retrieve the {@link CTabFolder} which is used by the given {@link StackPresentation}
     * @param stack the {@link StackPresentation} from which the {@link CTabFolder} should be retrieved
     * @return the {@link CTabFolder} used by the given {@link StackPresentation} or <code>null</code> if it can't be retrieved
     */
    static CTabFolder getTabFolder(StackPresentation stack){
        if( stack!= null ){
            Control tabFolder = stack.getControl();
            if( tabFolder instanceof CTabFolder ){
                return (CTabFolder) tabFolder;
            }
        }
        
        return null;
    }
    
    /**
     * Registers the given resource in the {@link ResourceManager}.
     * @param resourceFileName the name of the resource file (must be at the same package as this class)
     */
    static void registerResource(String resourceFileName){
        ResourceManager rm = RWT.getResourceManager();
        
        if( !rm.isRegistered(Objects.requireNonNull(resourceFileName, "No resource filename given!")) ){
            try( InputStream resourceStream = DnDHelper.class.getResourceAsStream(resourceFileName) ){
                rm.register(resourceFileName, resourceStream);
            } catch (IOException e) {
                System.err.println("Can't load resource '" + resourceFileName + "'! (reason: " + e.getMessage() + ')');
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Loads a JavaScript resource if it was already registered in the {@link ResourceManager}. 
     * @param javaScriptResourceFileName the JavaScript-Resource name registered at the {@link ResourceManager}
     */
    static void loadScriptResource(String javaScriptResourceFileName){
        ResourceManager rm = RWT.getResourceManager();
        
        if( rm.isRegistered(Objects.requireNonNull(javaScriptResourceFileName, "No JavaScript resource filename given!")) ){
            JavaScriptLoader loader = RWT.getClient().getService(JavaScriptLoader.class);
            if( loader != null ){
                loader.require(rm.getLocation(javaScriptResourceFileName));
            } else {
                System.err.println("No JavaScriptLoader found for current client! (" + RWT.getClient() + ')');
            }
        } else {
            throw new IllegalArgumentException('\'' + javaScriptResourceFileName + "' resource is not registered!");
        }
    }
    
    /**
     * Method to install the listeners for Drag&Drop on the given {@link StackPresentation}.
     * @param stack the {@link StackPresentation} on which the Drag&Drop should be installed
     * @param site {@link IStackPresentationSite} which hosts the stack
     * @return the given {@link StackPresentation} (just for convenience usage)
     * @throws NullPointerException if the given {@link StackPresentation} or {@link IStackPresentationSite} is <code>null</code>
     * @throws IllegalStateException if the given {@link StackPresentation} doesn't use {@link CTabFolder}'s
     */
    public static StackPresentation installTabDnD(StackPresentation stack, IStackPresentationSite site){
        TabMoveDragSource.install(stack);
        return stack;
    }

}
