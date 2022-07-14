package org.grits.toolbox.importer.ms.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.utilShare.ErrorUtils;

import org.grits.toolbox.importer.ms.process.RawToMzXMLConverterProcess;

/**
 * Progress Dialog to show progress indicator
 * @author dbrentw
 *
 */
public class RawToMzXMLConverterDialog extends Dialog 
{
	protected RawToMzXMLConverterProcess msmsFile_worker = null;
	protected Boolean msmsIsDone = false;
	
	protected RawToMzXMLConverterProcess msFile_worker = null;
	protected Boolean msIsDone = false;

	protected Label msmsLabel = null;
	protected Label msmsStatus = null;
	protected Label msLabel = null;
	protected Label msStatus = null;
	protected Button cancelButton; 
	protected Composite cancelComposite;
	protected Shell shell; //
	protected Display display = null; 
	protected boolean isCanceled = false;
	protected boolean errorPrinted = false;
	
	protected ProgressBar msmsFile_ProgressBar = null;
	protected ProgressBar msFile_ProgressBar = null;
	
	public RawToMzXMLConverterDialog(Shell parentShell) {
		super(parentShell);
	}
		
	public Thread getMSMSWorker() {
		return msmsFile_worker;
	}
	
	public void setMSMSWorker(RawToMzXMLConverterProcess a_worker)
	{
		this.msmsFile_worker = a_worker;
	}
	
	public ProgressBar getMSMSProgressBar() {
		return msmsFile_ProgressBar;
	}

	public Thread getMSWorker() {
		return msFile_worker;
	}
	
	public void setMSWorker(RawToMzXMLConverterProcess a_worker)
	{
		this.msFile_worker = a_worker;
	}
	
	public ProgressBar getMSProgressBar() {
		return msFile_ProgressBar;
	}
	
	public void setDisplay(Display display) {
		this.display = display;
	}
		
	private boolean checkAllDone() {
		boolean bDone = true;
		if ( this.msmsFile_worker != null ) {
			bDone &= this.msmsIsDone;
		}
		if ( this.msFile_worker != null ) {
			bDone &= this.msIsDone;
		}
		return bDone;
	}
	
	public void setDone( final RawToMzXMLConverterProcess _worker ) {
		// need final variable
		// create sync thread that allows to change the display
		this.display.syncExec(new Runnable() 
		{
			public void run() 
			{
				if( _worker.equals(msmsFile_worker) ) {
					msmsIsDone = Boolean.TRUE;
					msmsFile_ProgressBar.setState(SWT.PAUSED);
				} else {
					msIsDone = Boolean.TRUE;
					msFile_ProgressBar.setState(SWT.PAUSED);
				}
			}
		});
	}
	
	
	public int open() 
	{
		createContents();

		//find the center of a main monitor
		Monitor primary = shell.getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);

		shell.open();
		shell.layout();

		if ( this.msmsFile_worker != null ) {
			this.msmsFile_ProgressBar.setState(SWT.NORMAL);
			updateProgressBar("Starting...", this.msmsFile_worker);
			this.msmsFile_worker.start();
						
		}
		if ( this.msFile_worker != null ) {
			this.msFile_ProgressBar.setState(SWT.NORMAL);
			updateProgressBar("Starting...", this.msFile_worker);
			this.msFile_worker.start();			
		}
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch()) 
			{
				display.sleep();
			}
		}
		
		if(isCanceled)
		{
			return SWT.CANCEL;
		}
		return SWT.OK;
	}
	
	public void updateProgressBar( String _sText, RawToMzXMLConverterProcess _worker ) {
		// need final variable
		final String t_message = new String(_sText);
		final Label processMessageLabel = _worker.equals(this.msmsFile_worker) ? this.msmsStatus :  this.msStatus;
		// create sync thread that allows to change the display
		this.display.syncExec(new Runnable() 
		{
			public void run() 
			{
				processMessageLabel.setText(t_message);
			}
		});
	}

	protected void createContents() 
	{
		//shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL);
		shell = PropertyHandler.getModalDialog(getParent());
		display = shell.getDisplay();
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.verticalSpacing = 10;

		shell.setLayout(gridLayout);
		shell.setSize(483, 210);
		shell.setText("Raw-to-MzXML Converter Dialog");

		
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		msmsLabel = new Label(shell, SWT.NONE);
		msmsLabel.setText("MS/MS File Progress: ");
		msmsLabel.setLayoutData(gd1);
		
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		msmsStatus = new Label(shell, SWT.NONE);
		msmsStatus.setText("Nothing to do");
		msmsStatus.setLayoutData(gd2);

		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		gd3.horizontalSpan = 2;
		msmsFile_ProgressBar = new ProgressBar(shell, SWT.INDETERMINATE);
		msmsFile_ProgressBar.setLayoutData(gd3);
		msmsFile_ProgressBar.setState(SWT.PAUSED);
		
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		msLabel = new Label(shell, SWT.NONE);
		msLabel.setText("MS File Progress: ");
		msLabel.setLayoutData(gd4);
		
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		msStatus = new Label(shell, SWT.NONE);
		msStatus.setText("Nothing to do");
		msStatus.setLayoutData(gd5);

		GridData gd6 = new GridData(GridData.FILL_HORIZONTAL);
		gd6.horizontalSpan = 2;
		msFile_ProgressBar = new ProgressBar(shell, SWT.INDETERMINATE);
		msFile_ProgressBar.setLayoutData(gd6);
		msFile_ProgressBar.setState(SWT.PAUSED);

		//new composite
		cancelComposite = new Composite(shell, SWT.NONE);
		cancelComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		cancelComposite.setLayout(gridLayout_1);

		cancelButton = new Button(cancelComposite, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isCanceled = true;
				//close the shell
				clickCancel();
			}
		});
		cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
		cancelButton.setText("cancel");
	}
	
	public Display getDisplay() {
		return display;
	}

	public void threadFinished(RawToMzXMLConverterProcess worker, boolean successful)
	{
		setDone( worker );
		if (!isCanceled && successful) // user didn't cancel and this thread was successful
		{
			this.display.syncExec(new Runnable() 
			{
				public void run() 
				{
					isCanceled |= false; // ORing isCanceled..if this thread is successfule but previous wasn't, must stay true
				}
			});
		}
		else
		{
			this.display.syncExec(new Runnable() 
			{
				public void run() 
				{
					isCanceled &= true;
				}
			});
		}
		
		// close shell if necessary
		if ( checkAllDone() ) {
			this.display.syncExec(new Runnable() 
			{
				public void run() 
				{
					shell.close();
				}
			});			
		}
	}
	
	public void endWithException(final Exception e) {
		if ( this.errorPrinted )  // multi-threaded, so if first thread errors and prints message, do nothing on subsequent errors
			return;
		//need to close
		if ( shell.isDisposed() ) {
			return;
		}
		this.display.syncExec(new Runnable() 
		{
			public void run() 
			{
				errorPrinted = true;
				clickCancel();
				if (ErrorUtils.createErrorMessageBoxReturn(shell, "An error has occurred.",e) == 1)
				{
					//then close
					shell.close();
				}
			}
		});
	}

	
	protected void clickCancel() {
		//notify worker
		this.isCanceled = true;

		if ( this.msmsFile_worker != null )  {
			this.msmsFile_worker.cancelWork();
		}
		if ( this.msFile_worker != null ) {
			this.msFile_worker.cancelWork();
		}
//		shell.close();
	}
}
