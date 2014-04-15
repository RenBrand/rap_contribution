package at.renbrand.rap.workbench.detach;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.ViewStack;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.presentations.StackPresentation;

@SuppressWarnings("restriction")  // NOMPD: can't bypass this
public class DetachedWindow {

    private final WorkbenchPage page;
    private final CustomViewStack folder;
    private final DropOutsideWindow dropOutsideWindow;
    
    private Shell windowShell;
    
    public DetachedWindow(IWorkbenchPage workbenchPage, DropOutsideWindow dropOutsideWindow) {
        this.page = (WorkbenchPage) Objects.requireNonNull(workbenchPage, "No WorkbenchPage given!");
        this.dropOutsideWindow = Objects.requireNonNull(dropOutsideWindow);
        this.folder = new CustomViewStack(page);
    }
    
    public StackPresentation create(){
        Shell workbenchWindowShell = page.getWorkbenchWindow().getShell();
        windowShell = new Shell(workbenchWindowShell.getDisplay(), SWT.NO_TRIM | workbenchWindowShell.getOrientation());
        windowShell.setFullScreen(true);
        windowShell.setData(this);
        
        // create the tab folder
        folder.createControl(windowShell);
        
        windowShell.layout(true);
        folder.setBounds(windowShell.getClientArea());
        
        return folder.getPresentation();
    }
    
    public Shell getShell(){
        return windowShell;
    }
    
    public void open(){
        if( windowShell == null ) return;
        
//        windowShell.setVisible(true);
        windowShell.open();
    }

    private class CustomViewStack extends ViewStack {
        public CustomViewStack(WorkbenchPage page){
            super(page, false);
        }
        
        @Override
        public StackPresentation getPresentation() {
            return super.getPresentation();
        }
        
        @Override
        protected void add(LayoutPart newChild, Object cookie) {
            super.add(newChild, cookie);
            
            // move it to the new shell
            dropOutsideWindow.ensureHTMLLocation(windowShell, newChild.getControl());
        }
    }
}
