package at.renbrand.rap.workbench.detach;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Listener which handles the drag source of parts.
 */
@SuppressWarnings("restriction")
final class TabMoveDragSource implements DragSourceListener {

    /** The serial version ID of this listener. */
    private static final long serialVersionUID = -4908233051850650759L;
    
    /** A reference to the stack on which this listener is registered. */
    protected final StackPresentation stack;

    /** A reference to the CTabFolder used by the stack. */
    protected final CTabFolder tabFolder;

    /** The list of tabs which should be moved/dragged whenever a drop is performed. */
    private CTabItem[] toDrag; // synchronized via the Display (UI thread confined)
    
    private final DropOutsideWindow dropOutsideWindow;
    
    /**
     * Sole private constructor.
     * <p>
     * (Use: {@link #install(StackPresentation)} for creation and installation)
     * </p>
     * @param stack the {@link StackPresentation} which should react on drag start.
     * @throws NullPointerException if the given {@link StackPresentation} is <code>null</code>
     * @throws IllegalStateException if the given {@link StackPresentation} doesn't use {@link CTabFolder}'s
     */
    private TabMoveDragSource(StackPresentation stack) {
        this.stack = Objects.requireNonNull(stack, "No presentation stack given!");

        this.tabFolder = DnDHelper.getTabFolder(stack);
        if (tabFolder == null) {
            // fail early
            throw new IllegalStateException("The given StackPresentation doesn't use a CTabFolder! (>> use another implementation)");
        }
        dropOutsideWindow = DropOutsideWindow.getInstance(); // session singleton and the first call of this method will install the required JavaScript
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        System.out.println("\tstart: " + event);
        dropOutsideWindow.addDropListener(l);
        DragSource dragSource = (DragSource) event.getSource();

        if (tabFolder.equals(dragSource.getControl())) { // just for safety
            CTabItem item = tabFolder.getItem(new Point(event.x, event.y)); // the tab under the drag start location
            if (item == null) {
                // if no tab is located under the drag start location move all tabs (happens if you click the blank area after the last tab)
                CTabItem[] items = tabFolder.getItems();
                if (items.length > 0) {
                    toDrag = items; // tabFolder.getItems() already made a fresh array for us, so no new array copy!
                }
            } else {
                toDrag = new CTabItem[] { item };
            }
            
            if (toDrag != null) {
                return;
            }
        }

        System.out.println("\tstart --> doit: false");
        event.doit = false;
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        System.out.println("\tsetData: " + event);
        event.data = new CTabDnDHolder(stack, toDrag); // create the object which should be transfered via CTabTransfer (only called if the DropTarget changes the event.detail state to something other than DND.DROP_NONE)
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        System.out.println("\tfinished: " + event + " --> Register Listener on drop event ;-)" );
        toDrag = null; // clear the instance field, because the drop has been performed
        dropOutsideWindow.removeDropListener(l);
    }

    /**
     * Method to install this {@link DragSourceListener} to the correct control of the given {@link StackPresentation}.
     * @param stack the {@link StackPresentation} on which this {@link DragSourceListener} should be
     *            activated/installed.
     * @throws NullPointerException if the given {@link StackPresentation} is <code>null</code>
     * @throws IllegalStateException if the given {@link StackPresentation} doesn't use {@link CTabFolder}'s
     */
    public static void install(StackPresentation stack) {
        TabMoveDragSource listener = new TabMoveDragSource(stack);

        DragSource dragSource = new DragSource(listener.tabFolder, DND.DROP_MOVE); // we only support move, because that's what we want when moving a tab
        dragSource.setTransfer(new Transfer[] { CTabTransfer.getInstance() }); // install our special transfer object
        dragSource.addDragListener(listener);
    }
    
    private final DropOutsideWindow.Listener l = new DropOutsideWindow.Listener(){

        @Override
        public void dropOut(int x, int y) {
            System.out.println("drop at: " + x +  "/" + y);
            StackPresentation targetStack = dropOutsideWindow.detachWindow(x, y, Workbench.getInstance().getActiveWorkbenchWindow().getActivePage(), toDrag);
            Object targetLayoutPart = DnDHelper.getTabFolder(targetStack).getData();
            
            tabFolder.getDisplay().asyncExec( MovePartsBetweenStacks.createMoveToOtherStack((LayoutPart)tabFolder.getData(), DnDHelper.getPartPane(Arrays.asList(toDrag)), (LayoutPart)targetLayoutPart) );
        }
        
    };

}
