package at.renbrand.rap.workbench.detach;

import static at.renbrand.rap.workbench.detach.DnDHelper.installTabDnD;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

/**
 * Presentation Factory which extends the default {@link WorkbenchPresentationFactory} and enables the Drag&Drop of parts.
 */
public class DnDPresentationFactory extends WorkbenchPresentationFactory {

    public DnDPresentationFactory() {
    }

    @Override
    public StackPresentation createEditorPresentation(Composite parent, IStackPresentationSite site) {
        return installTabDnD(super.createEditorPresentation(parent, site), site);
    }

    @Override
    public StackPresentation createViewPresentation(Composite parent, IStackPresentationSite site) {
        return installTabDnD(super.createViewPresentation(parent, site), site);
    }
    
}
