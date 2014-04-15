package at.renbrand.rap.workbench.detach;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSashContainer;

/**
 * Moves the tabs/parts of one stack to a completely new one.
 */
@SuppressWarnings("restriction") // NOPMD some eclipse internals are needed to make parts movable
final class MovePartsBetweenStacks implements Runnable {
    
    /** The source which currently holds the parts. */
    private final LayoutPart sourceStack;
    
    /** The target which should hold the parts, after the move has been done. */
    private final LayoutPart targetStack;
    
    /** The parts to move. */
    private final PartPane[] toDrop;
    
    /** 
     * The insert location of the new parts.
     * 
     * <p>
     * Useful only if moving to a new part is requested.
     * Possible values mentioned by the sash container:
     * </p>
     * 
     * <ul>
     *  <li>{@link SWT#LEFT}</li>
     *  <li>{@link SWT#RIGHT}</li>
     *  <li>{@link SWT#TOP}</li>
     *  <li>{@link SWT#BOTTOM}</li>
     *  <li>{@link SWT#CENTER}</li>
     * </ul>
     * 
     */
    private final int side;

    /**
     * Sole constructor.
     * @param sourceStack source which currently holds the parts
     * @param toDrop parts to move
     * @param targetStack target to move the parts to (can be <code>null</code>)
     * @param side the side to insert the parts in the container (possible values: <code>SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM, SWT.CENTER</code>)
     * @throws NullPointerException if either the given <code>sourceStack</code> or the <code>toDrop</code> is <code>null</code>
     */
    private MovePartsBetweenStacks(LayoutPart sourceStack, PartPane[] toDrop, LayoutPart targetStack, int side) {
        this.sourceStack = Objects.requireNonNull(sourceStack, "No source stack given!");
        this.toDrop = Arrays.copyOf(toDrop, toDrop.length);
        this.targetStack = targetStack;
        this.side = side;
    }

    @Override
    public void run() {
        Object partSashContainer = sourceStack.getContainer();
        
        // move the tabs from one stack to another via the container, or to a new one if no targetStack is given
        Method dropObjectMethod = getDeclaredMethod(PartSashContainer.class, "dropObject", true, PartPane[].class, LayoutPart.class, LayoutPart.class, int.class); //$NON-NLS-1$
        invoke(dropObjectMethod, partSashContainer, null, toDrop, sourceStack, targetStack, side);
    }

    /**
     * Creates a new instance of the {@link MovePartsBetweenStacks} which will move the parts from the source stack to another already existing stack.
     * @param sourceStack source which currently holds the parts
     * @param toDrop parts to move to the <code>targetStack</code>
     * @param targetStack the stack to which the parts should be moved
     * @return a new {@link MovePartsBetweenStacks} instance which will move the parts to another already existing stack
     * @throws NullPointerException if either the given <code>sourceStack</code> or the <code>toDrop</code> is <code>null</code>
     */
    static MovePartsBetweenStacks createMoveToOtherStack(LayoutPart sourceStack, PartPane[] toDrop, LayoutPart targetStack){
        return new MovePartsBetweenStacks(sourceStack, toDrop, targetStack, SWT.CENTER);
    }
    
    static Method getDeclaredMethod(Class<?> c, String methodName, boolean setAccessible, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = c.getDeclaredMethod(methodName, parameterTypes);
        } catch (SecurityException e) {
            System.err.println("SecurityException while getting method \"" + methodName + "\""); //$NON-NLS-1$
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            System.err.println("no method \"" + methodName + "\" anymore (RCP/RAP version changed?)"); //$NON-NLS-1$
            e.printStackTrace();
        }
        
        if (method != null && setAccessible) {
            try {
                method.setAccessible(true);
            } catch (SecurityException e) {
                System.err.println("SecurityException while setting method \"" + methodName + "\" accessible"); //$NON-NLS-1$
                e.printStackTrace();
                return null;
            }
        }
        
        return method;
    }
    
    static <T> T invoke(Method method, Object objectToInvokeFrom, Class<T> returnType, Object... args) {
        if (method == null) {
            System.out.println("no method to invoke");
            return null;
        }
        
        Object retVal = null;
        try {
            retVal = method.invoke(objectToInvokeFrom, args);
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException while calling method \"" + method.getName() + "\" (RCP/RAP version changed?)"); //$NON-NLS-1$
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("IllegalAccessException while calling method \"" + method.getName() + "\""); //$NON-NLS-1$
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.err.println("method \"" + method.getName() + "\" has thrown an exception"); //$NON-NLS-1$
            e.printStackTrace();
        }
        
        if (retVal == null) {
            return null;
        } else {
            if (returnType == null) {
                if (void.class != method.getReturnType()) {
                    System.err.println("Expected a method without return type!");
                }
                return null;
            } else if (returnType.isAssignableFrom(retVal.getClass())) {
                @SuppressWarnings("unchecked") // checked above
                T cast = (T) retVal;
                return cast;
            } else {
                System.err.println("Invalid return type: " + retVal.getClass());
                return null;
            }
        }
    }
}
