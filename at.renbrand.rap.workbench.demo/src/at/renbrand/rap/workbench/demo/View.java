package at.renbrand.rap.workbench.demo;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import at.renbrand.rap.detach.DetachedShellFactory;
import at.renbrand.rap.detach.UnleashedShell;

/**
 * This view shows a &quot;mail message&quot;. This class is contributed through
 * the plugin.xml.
 */
public class View extends ViewPart {

	public static final String ID = "at.renbrand.rap.workbench.demo.view";
	
	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);
		// top banner
		Composite banner = new Composite(top, SWT.NONE);
		banner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false));
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		banner.setLayout(layout);
		
		// setup bold font
		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);    
		
		Label l = new Label(banner, SWT.WRAP);
		l.setText("Subject:");
		l.setFont(boldFont);
		l = new Label(banner, SWT.WRAP);
		l.setText("This is a message about the cool Eclipse RCP!");
		
		l = new Label(banner, SWT.WRAP);
		l.setText("From:");
		l.setFont(boldFont);
		
		final Link link = new Link(banner, SWT.NONE);
		link.setText("<a>nicole@mail.org</a>");
		link.addSelectionListener(new SelectionAdapter() {    
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(getSite().getShell(), "Not Implemented", "Imagine the address book or a new message being created now.");
			}    
		});
		
		l = new Label(banner, SWT.WRAP);
		l.setText("Date:");
		l.setFont(boldFont);
		l = new Label(banner, SWT.WRAP);
		l.setText("10:34 am");
		
		JFaceResources.getColorRegistry().put("red", new RGB(255, 0, 0));
		l = new Label( top, SWT.WRAP );
		l.setFont(boldFont);
		l.setForeground(JFaceResources.getColorRegistry().get("red"));
		l.setText("To detach a part, just drag a Part-Tab out of the browser window as you would do in a normal RCP application.");
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		
		// message contents
		Text text = new Text(top, SWT.MULTI | SWT.WRAP);
		text.setText("This RAP Application was generated from the PDE Plug-in Project wizard. This sample shows how to:\n"+
						"- add a top-level menu and toolbar with actions\n"+
						"- create views that can't be closed and\n"+
						"  multiple instances of the same view\n"+
						"- perspectives with placeholders for new views\n"+
						"- use the default about dialog\n");
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Button b = new Button(top, SWT.PUSH);
		b.setText("Open a detached window!");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UnleashedShell unleashed = DetachedShellFactory.create(getViewSite().getShell(), SWT.NONE);
				unleashed.setTitle("A new detached window!");
				unleashed.getShell().setLayout(new FillLayout());
				
				final Label l = new Label( unleashed.getShell(), SWT.NONE );
				l.setText("Hello World!");
				
				Button b = new Button( unleashed.getShell(), SWT.PUSH );
				b.setText("change");
				
				b.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						l.setText("Changed: " + l.getText());
					}
				});
				
				unleashed.open();
			}
		});
	}

	public void setFocus() {
	}
}
