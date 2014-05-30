package at.renbrand.rap.detach;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.service.ResourceManager;

/**
 * Utility class for script handling in RAP.
 * 
 * @author brandstetter
 */
public final class ScriptUtils {
	
	/**
     * @throws IllegalStateException always, because instantiation not allowed
     */
    private ScriptUtils(){
		throw new IllegalStateException("Instantiation not allowed!");
	}
	
    /**
     * Registers the given resource in the {@link ResourceManager}.
     * @param resourceFileName the name of the resource file
     * @param resourceLoader a class reference which is used to load the resource
     * @throws NullPointerException if the either the given resource filename or the resource loader class is <code>null</code>
     * @throws IllegalArgumentException if the resource couldn't be loaded
     */
    public static void registerResource(String resourceFileName, Class<?> resourceLoader){
        ResourceManager rm = RWT.getResourceManager();
        
        if( !rm.isRegistered(Objects.requireNonNull(resourceFileName, "No resource filename given!")) ){
            try( InputStream resourceStream = resourceLoader.getResourceAsStream(resourceFileName) ){
                rm.register(resourceFileName, resourceStream);
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't load resource '" + resourceFileName + "'! (reason: " + e.getMessage() + ')', e);
            }
        }
    }
    
    /**
     * Loads a JavaScript resource if it was already registered in the {@link ResourceManager}. 
     * @param javaScriptResourceFileName the JavaScript-Resource name registered at the {@link ResourceManager}
     * @throws NullPointerException if the given javaScriptResourceFileName is <code>null</code>
     * @throws IllegalArgumentException if the given javaScriptResourceFileName wasn't registered so far (see: {@link #registerResource(String, Class)})
     */
    public static void loadScriptResource(String javaScriptResourceFileName){
        ResourceManager rm = RWT.getResourceManager();
        
        if( rm.isRegistered(Objects.requireNonNull(javaScriptResourceFileName, "No JavaScript resource filename given!")) ){
            JavaScriptLoader loader = RWT.getClient().getService(JavaScriptLoader.class);
            if( loader != null ){
                loader.require(rm.getLocation(javaScriptResourceFileName));
            } else {
            	// TODO: replace with logger!
                System.err.println("No JavaScriptLoader found for current client! (" + RWT.getClient() + ')');
            }
        } else {
            throw new IllegalArgumentException('\'' + javaScriptResourceFileName + "' resource is not registered!");
        }
    }
}
