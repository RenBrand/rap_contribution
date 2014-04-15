package at.renbrand.rap.workbench.detach;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Simple holder object which is used as the content of a Transfer object.
 * <p>
 * It contains the {@link StackPresentation} from which the elements should be dragged
 * away and the list of {@link CTabItem}'s that should be dragged. 
 * </p>
 */
class CTabDnDHolder {

    /** The stack from which the tabs should be moved/dragged away. */
    private final StackPresentation originStack;
    
    /** The tabs to move/drag away. */
    private final List<CTabItem> itemsToMove;
    
    /**
     * Constructs an instance of the object.
     * @param originStack the stack from which to move the tabs a way
     * @param itemsToMove the tabs to move
     * @throws NullPointerException if either the origin stack or the list of tabs to move are <code>null</code>
     */
    public CTabDnDHolder(StackPresentation originStack, CTabItem... itemsToMove) {
        this.originStack = Objects.requireNonNull(originStack, "No origin StackPresentation given!");
        this.itemsToMove = Collections.unmodifiableList(Arrays.asList(itemsToMove));
    }

    /**
     * The stack which holds the tab that should be moved/dragged.
     * @return the stack which holds the tab that should be moved/dragged (never <code>null</code>)
     */
    public StackPresentation getOriginStack() {
        return originStack;
    }

    /**
     * The list of tabs contained in the origin stack which should be moved/dragged.
     * @return the tabs to move/drag (never <code>null</code>)
     */
    public List<CTabItem> getItemsToMove() {
        return itemsToMove;
    }

}
