package org.grits.toolbox.importer.ms.wizard;

import java.io.File;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.utilShare.FileSelectionAdapter;
import org.grits.toolbox.ms.file.PeakListInfo;

public class PageThree extends WizardPage {
	// setup bold font
	protected final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT); 

	private Button msPeakListOptionBtn;

	
	private Label peakListLabel;
	private Combo peakListTypeList;
	private Label peakListFileLabel;
	private Text peakListFileText;
	private Button peakListFileBtn;

	private String peakListFileName = null;
	private String peakListFileFormat = null;

	private Composite container;

	public PageThree() {
		super("New MS Experiment");
		setTitle("New MS Experiment");
		setDescription("Create a new MS Experiment.");
	}

	@Override
	public void createControl(Composite parent) {
		//This is important. DO NOT USE parent directly!
		container = new Composite(parent, SWT.NONE);
		//has to be gridLayout, since it extends TitleAreaDialog
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.verticalSpacing = 10;
		container.setLayout(gridLayout);

		createPeakListOption(container);
		createPeakListComponents(container);
		setPeakListEnabled(false);        

		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(true);
	}

	private void createPeakListOption( Composite container ) {
		GridData msDIOptionBtnGridData = new GridData();
		msPeakListOptionBtn = new Button(container, SWT.CHECK);
		msPeakListOptionBtn.setText("Specify Peak List File");
		msPeakListOptionBtn.setLayoutData(msDIOptionBtnGridData);
		msDIOptionBtnGridData.horizontalSpan = 4;
		msDIOptionBtnGridData.grabExcessHorizontalSpace = true;
		msPeakListOptionBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setPeakListEnabled(msPeakListOptionBtn.getSelection());			
				setPageComplete( isReadyToFinish() );
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});    	
	}

	private void createPeakListComponents(Composite container) {
		//Label
		Label label0 = new Label(container, SWT.NONE);
		label0.setText("    ");

		peakListLabel = new Label(container, SWT.NONE);
		peakListLabel.setText("Peak List File Format: ");
		GridData gdLabel = new GridData(GridData.FILL_HORIZONTAL);
		peakListLabel.setLayoutData(gdLabel);
		peakListLabel.setEnabled(false);
		peakListTypeList = new Combo(container, SWT.READ_ONLY);
		GridData gdType = new GridData(GridData.FILL_HORIZONTAL);
		peakListTypeList.setLayoutData(gdType);
		peakListTypeList.setItems(new String[] {
				PeakListInfo.PEAKLISTFORMAT_EXTRACT_XML_DESC, 
				PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_MGF_DESC});
		peakListTypeList.select(0);
		peakListTypeList.addModifyListener( new ModifyListener() {		
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete( isReadyToFinish() );				
			}
		});
		gdType.horizontalSpan = 2;
		peakListTypeList.setEnabled(false);
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("    ");

		peakListFileLabel = new Label(container, SWT.NONE);
		peakListFileLabel.setText("Peak List File");
		peakListFileLabel = setMandatoryLabel(peakListFileLabel);

		//text
		peakListFileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		peakListFileText.setText("");
		GridData gdFileText = new GridData(GridData.FILL_HORIZONTAL);
		peakListFileText.setLayoutData(gdFileText);
		//		mzXMLFullFileText.setEditable(false);

		//browse button
		GridData peakListBrowseButtonData = new GridData();
		peakListFileBtn = new Button(container, SWT.PUSH);
		peakListFileBtn.setText("Browse");
		peakListFileBtn.setLayoutData(peakListBrowseButtonData);
		final FileSelectionAdapter peakListFileBrowserSelectionAdapter = new FileSelectionAdapter();
		peakListFileBrowserSelectionAdapter.setShell(container.getShell());
		peakListFileBrowserSelectionAdapter.setText(peakListFileText);
		String sXML = "*." + PeakListInfo.PEAKLISTFORMAT_EXTRACT_XML_EXTENSION;
		String sMGF = "*." + PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_MGF_EXTENSION;
		peakListFileBrowserSelectionAdapter.setFilterExtensions( new String[] {sXML + ";" + sMGF, "*.*"});
		peakListFileBrowserSelectionAdapter.setFilterNames( new String[] { "Peak list files (" + sXML + ";" + sMGF + ")",	"All files"});
		peakListFileBtn.addSelectionListener(peakListFileBrowserSelectionAdapter);
		peakListFileText.addModifyListener( new ModifyListener() {		
			@Override
			public void modifyText(ModifyEvent e) {
				peakListFileName = null;
				peakListFileFormat = null;
				if( msPeakListOptionBtn.getSelection()) {
					peakListFileName = peakListFileText.getText();
					peakListFileFormat = null;
					if( peakListFileName.endsWith( PeakListInfo.PEAKLISTFORMAT_EXTRACT_XML_EXTENSION) ) {
						peakListFileFormat = PeakListInfo.PEAKLISTFORMAT_EXTRACT_TYPE;				
					} else if ( peakListFileName.endsWith( PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_MGF_EXTENSION) ) {
						peakListFileFormat = PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_TYPE;
					} 
					if( peakListTypeList.getText().equals( PeakListInfo.PEAKLISTFORMAT_EXTRACT_XML_DESC ) ) {
						peakListFileFormat = PeakListInfo.PEAKLISTFORMAT_EXTRACT_TYPE;
					} else if ( peakListTypeList.getText().equals( PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_MGF_DESC ) ) {
						peakListFileFormat = PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_TYPE;
					}    		
				}
				setPageComplete( isReadyToFinish() );				
			}
		});
	}

	

	public void setPeakListEnabled( boolean _bVal ) {
		//		peakListLabel.setEnabled(_bVal);
		//		peakListTypeList.setEnabled(_bVal);
		peakListFileLabel.setEnabled(_bVal);
		peakListFileText.setEnabled(_bVal);
		peakListFileBtn.setEnabled(_bVal);  	
		peakListLabel.setEnabled(_bVal);
		peakListTypeList.setEnabled(_bVal);
	}

	

	protected Label createSeparator(Composite container, int span) {
		GridData separatorData = new GridData();
		separatorData.grabExcessHorizontalSpace = true;
		separatorData.horizontalAlignment = GridData.FILL;
		separatorData.horizontalSpan = span;
		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(separatorData);
		return separator;
	}

	protected Label setMandatoryLabel(Label lable) {
		lable.setText(lable.getText()+"*");
		lable.setFont(boldFont);
		return lable;
	}

	/**
	 * Check if ready to finish
	 * @return
	 */
	private boolean isReadyToFinish() {
		//        if( msDIOptionBtn.getSelection() && rawFullMSFileText.getText().isEmpty() ) {
		//            setErrorMessage("Full File option selected, but no files specified.");
		//        	return false;
		//        }
		if( msPeakListOptionBtn.getSelection() ) {
			//			if( msPeakListOptionBtn.getSelection() && peakListTypeList.getText().isEmpty() ) {
			//				setErrorMessage("Peak List option selected, but peak list type not specified.");
			//				return false;
			//			}
			if( msPeakListOptionBtn.getSelection() && peakListFileText.getText().isEmpty() ) {
				setErrorMessage("Peak List option selected, but peak list file not specified.");
				return false;
			}
			if ( ! peakListFileText.getText().isEmpty() ) {
				if ( ! fileExists(peakListFileText.getText()) ) {
					setErrorMessage("Specified file for 'Peak List MS File' does not exist.");
					return false;
				}
			}
//			if ( peakListFileFormat == null ) {
//				setErrorMessage("Unrecognized peak list file format.");
//				return false;
//			}
		}
		setErrorMessage(null);
		return true;
	}

	private boolean fileExists(String _sFileName) {
		File file = new File(_sFileName);
		return file.exists();
	}


	public void save()
	{
		
		this.peakListFileName = null;
		this.peakListFileFormat = null;
		if( this.msPeakListOptionBtn.getSelection()) {
			this.peakListFileName = peakListFileText.getText();
			this.peakListFileFormat = null;
			if( peakListFileName.endsWith( PeakListInfo.PEAKLISTFORMAT_EXTRACT_XML_EXTENSION) ) {
				this.peakListFileFormat = PeakListInfo.PEAKLISTFORMAT_EXTRACT_TYPE;				
			} else if ( peakListFileName.endsWith( PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_MGF_EXTENSION) ) {
				this.peakListFileFormat = PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_TYPE;
			} 
			if( peakListTypeList.getText().equals( PeakListInfo.PEAKLISTFORMAT_EXTRACT_XML_DESC ) ) {
				this.peakListFileFormat = PeakListInfo.PEAKLISTFORMAT_EXTRACT_TYPE;
			} else if ( peakListTypeList.getText().equals( PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_MGF_DESC ) ) {
				this.peakListFileFormat = PeakListInfo.PEAKLISTFORMAT_MASCOTGENERIC_TYPE;
			}    		
		}
		 
	}

	public IWizardPage getNextPage() {
		save();
		return super.getNextPage();
	}

	public String getPeakListFileName() {
		return peakListFileName;
	}

	public void setPeakListFileName(String peakListFileName) {
		this.peakListFileName = peakListFileName;
	}

	public String getPeakListFileFormat() {
		return peakListFileFormat;
	}

	public void setPeakListFileFormat(String peakListFileFormat) {
		this.peakListFileFormat = peakListFileFormat;
	}

}