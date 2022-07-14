package org.grits.toolbox.importer.ms.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.utilShare.EntrySelectionAdapter;
import org.grits.toolbox.core.utilShare.FileSelectionAdapter;
import org.grits.toolbox.core.utilShare.ListenerFactory;
import org.grits.toolbox.core.utilShare.SelectionInterface;
import org.grits.toolbox.entry.ms.preference.MassSpecPreference;
import org.grits.toolbox.entry.ms.preference.MassSpecPreferencePage;
import org.grits.toolbox.entry.ms.preference.MassSpecSettingPreferenceUI;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.sample.property.SampleProperty;
import org.grits.toolbox.ms.file.FileCategory;
import org.grits.toolbox.ms.file.MSFileInfo;
import org.grits.toolbox.ms.om.data.Method;

public class PageOne extends WizardPage implements SelectionInterface, IPropertyChangeListener {
    // setup bold font
    protected final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT); 

    private Text nameText;
    private String name;
    private Label nameLabel;

	//private Combo msExperimentCombo;
	private String msExperimentType=null;
	private Label msExperimentLabel;
    
    private Text descriptionText;
    private String description;
    private Label descriptionLabel;

    private Text rawFileNameText;
    private String rawFileName;
    private Label rawFileNameLabel;

    private Text mzxmlFileNameText;
    private String mzxmlFileName;
    private Label mzxmlFileNameLabel;

    private Text sampleText;
    private Label sampleNameLabel;

    protected Entry sampleEntry = null;

    protected Composite container;

    protected MassSpecSettingPreferenceUI settingsPreference;
	protected boolean nextPage = false;

	protected FileUploadPage fileUploadPage;

	protected List<Button> experimentTypeButtons = new ArrayList<>();

	protected Composite firstGroup = null;
	protected Composite secondGroup = null;
	
    public PageOne() {
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
        gridLayout.numColumns = 3;
        gridLayout.verticalSpacing = 10;
        container.setLayout(gridLayout);

        /*
         * First row starts: create project textfield with a browse button
         */
        GridData sampleNameData = new GridData();
        sampleNameLabel = new Label(container, SWT.NONE);
        sampleNameLabel.setText("Sample");
        sampleNameLabel = setMandatoryLabel(sampleNameLabel);
        sampleNameLabel.setLayoutData(sampleNameData);

        GridData samplenameTextData = new GridData();
        samplenameTextData.grabExcessHorizontalSpace = true;
        samplenameTextData.horizontalAlignment = GridData.FILL;
        sampleText = new Text(container, SWT.BORDER);
        //for the first time if an entry was chosen by a user
        if(sampleEntry != null)
        {
            sampleText.setText(sampleEntry.getDisplayName());
        }
        sampleText.setEditable(false);
        sampleText.setLayoutData(samplenameTextData);

        //sampleProjectBrowserButtonData
        GridData sampleProjectBrowserButtonData = new GridData();
        Button button = new Button(container, SWT.PUSH);
        button.setText("Browse");
        button.setLayoutData(sampleProjectBrowserButtonData);
        EntrySelectionAdapter sampleProjectSelectionAdapter = new EntrySelectionAdapter(SampleProperty.TYPE,"Sample Selection","Choose a sample");
        sampleProjectSelectionAdapter.setParent(container);
        sampleProjectSelectionAdapter.setEntry(sampleEntry);
        sampleProjectSelectionAdapter.setParentWindow(this);
        //		sampleProjectSelectionAdapter.setText(sampleText);
        button.addSelectionListener(sampleProjectSelectionAdapter);

        //then add separator
        createSeparator(container,3);

        /*
         * Second row starts: Display Name
         */
        GridData nameData = new GridData();
        nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("Display Name");
        nameLabel = setMandatoryLabel(nameLabel);
        nameLabel.setLayoutData(nameData);

        GridData nameTextData = new GridData();
        nameTextData.grabExcessHorizontalSpace = true;
        nameTextData.horizontalAlignment = GridData.FILL;
        nameTextData.horizontalSpan = 2;
        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(nameTextData);
        nameText.addModifyListener( new ModifyListener() 
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                setPageComplete( isReadyToFinish() );
            }
        });

		String sDefaultExpType = NewMSWizard.preferences.getExperimentType() != null && ! NewMSWizard.preferences.getExperimentType().equals("") ? 
				NewMSWizard.preferences.getExperimentType() : Method.MS_TYPE_INFUSION_LABEL;
		msExperimentLabel = MassSpecPreferencePage.createLabel(container, "Experiment Type");
		createExperimentGroup (container, NewMSWizard.preferences.getAllExperimentTypes(), sDefaultExpType);
		//msExperimentCombo = MassSpecPreferencePage.createCombo(container, msExperimentLabel, NewMSWizard.preferences.getAllExperimentTypes(), sDefaultExpType, false, false);
		MassSpecPreferencePage.setMandatoryLabel(msExperimentLabel);
        
        /*
         * Third row starts:Description
         */
        GridData descriptionData = new GridData();
        descriptionLabel = new Label(container, SWT.LEFT);
        descriptionLabel.setText("Description");
        descriptionLabel.setLayoutData(descriptionData);

        GridData descriptionTextData = new GridData();
        descriptionTextData.minimumHeight = 80;
        descriptionTextData.grabExcessHorizontalSpace = true;
        descriptionTextData.grabExcessVerticalSpace = true;
        descriptionTextData.horizontalAlignment = GridData.FILL;
        descriptionTextData.horizontalSpan = 2;
        descriptionText = new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        descriptionText.setLayoutData(descriptionTextData);
        descriptionText.addTraverseListener(ListenerFactory.getTabTraverseListener());
        descriptionText.addModifyListener( new ModifyListener() {		
            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete( isReadyToFinish() );				
            }
        });
        descriptionText.addKeyListener(ListenerFactory.getCTRLAListener());
        
        addInstrumentOptions (container);

        /*
         * Fourth row starts:RawFileName
         */
        GridData rawFileNameData = new GridData();
        rawFileNameLabel = new Label(container, SWT.NONE);
        rawFileNameLabel.setText("MS Instrument File");
//        rawFileNameLabel = setMandatoryLabel(rawFileNameLabel);
        rawFileNameLabel.setLayoutData(rawFileNameData);

        GridData rawFileNameTextData = new GridData();
        rawFileNameTextData.grabExcessHorizontalSpace = true;
        rawFileNameTextData.horizontalAlignment = GridData.FILL;
        rawFileNameTextData.horizontalSpan=1;
        rawFileNameText = new Text(container, SWT.BORDER);
        //		rawFileNameText.setEditable(false);
        rawFileNameText.setLayoutData(rawFileNameTextData);
        rawFileNameText.addModifyListener( new ModifyListener() {		
            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete( isReadyToFinish() );				
            }
        });
        rawFileNameText.addListener(SWT.Verify, new Listener(){
            @Override
            public void handleEvent(Event e) {
            	// Get the source widget
		        Text source = (Text) e.widget;
		        // Get the text
		        final String oldS = source.getText();
		        final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end); 
		        if (newS.isEmpty()) // clear the saved data
		        	rawFileName = null;
	        	// remove the file from file list
	        	MSPropertyDataFile toBeRemoved = null;
	        	for (MSPropertyDataFile msPropertyDataFile : ((NewMSWizard) getWizard()).getAllFiles()) {
	        		if (msPropertyDataFile.getName().equals(oldS)) {
	        			toBeRemoved = msPropertyDataFile;
	        		}
	        	}
	        	if (toBeRemoved != null)
	        		((NewMSWizard) getWizard()).getAllFiles().remove(toBeRemoved);
            }
        });


        //browse button
        GridData rawFilebrowseButtonData = new GridData();
        Button button2 = new Button(container, SWT.PUSH);
        button2.setText("Browse");
        button2.setLayoutData(rawFilebrowseButtonData);
        FileSelectionAdapter rawFileBrowserSelectionAdapter = new FileSelectionAdapter();
        rawFileBrowserSelectionAdapter.setShell(container.getShell());
        rawFileBrowserSelectionAdapter.setText(rawFileNameText);
        button2.addSelectionListener(rawFileBrowserSelectionAdapter);
        rawFileBrowserSelectionAdapter.setFilterExtensions( new String[] {MSFileInfo.MSFILES_FILTER_EXTENSIONS, "*.*"});
		rawFileBrowserSelectionAdapter.setFilterNames( new String[] { MSFileInfo.MSFILES_FILTER_NAMES, "All files"});

        /*
         * Fifth row starts:MzxmlFileName
         */
        GridData mzxmlFileNameData = new GridData();
        mzxmlFileNameLabel = new Label(container, SWT.NONE);
        mzxmlFileNameLabel.setText("MS Annotation File (mzML, mzXML etc.)");
        mzxmlFileNameLabel.setLayoutData(mzxmlFileNameData);
      //  mzxmlFileNameLabel = setMandatoryLabel(mzxmlFileNameLabel);
        GridData mzxmlFileTextData = new GridData();
        mzxmlFileTextData.grabExcessHorizontalSpace = true;
        mzxmlFileTextData.horizontalAlignment = GridData.FILL;
        mzxmlFileTextData.horizontalSpan=1;
        mzxmlFileNameText = new Text(container, SWT.BORDER);
        //		mzxmlFileNameText.setEditable(false);
        mzxmlFileNameText.setLayoutData(mzxmlFileTextData);
        mzxmlFileNameText.addModifyListener( new ModifyListener() {		
            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete( isReadyToFinish() );				
            }
        });
        
        mzxmlFileNameText.addListener(SWT.Verify, new Listener(){
            @Override
            public void handleEvent(Event e) {
            	// Get the source widget
		        Text source = (Text) e.widget;
		        // Get the text
		        final String oldS = source.getText();
		        final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
		        if (newS.isEmpty()) // clear the saved data
		        	mzxmlFileName = null;
	        	// remove the file from file list
		        MSPropertyDataFile parentToBeRemoved = null;
	        	for (MSPropertyDataFile msPropertyDataFile : ((NewMSWizard) getWizard()).getAllFiles()) {
	        		if (msPropertyDataFile.getChildren() != null) {
	        			MSPropertyDataFile toBeRemoved = null;
	        			for (MSPropertyDataFile child : msPropertyDataFile.getChildren()) {
							if (child.getName().equals(oldS)) {
								toBeRemoved = child;
							}
						}
	        			if (toBeRemoved != null) {
	        				msPropertyDataFile.getChildren().remove(toBeRemoved);
	        				if (msPropertyDataFile.getChildren().isEmpty() && msPropertyDataFile.getName().isEmpty()) // artificial empty parent, no need to keep it anymore
	        					parentToBeRemoved = msPropertyDataFile;
	        					
	        			}
	        		}
	        	}
	        	if (parentToBeRemoved != null)
	        		((NewMSWizard) getWizard()).getAllFiles().remove(parentToBeRemoved);
		        
            }
        });

        //browse button
        GridData mzxmlbrowseButtonData = new GridData();
        Button button3 = new Button(container, SWT.PUSH);
        button3.setText("Browse");
        button3.setLayoutData(mzxmlbrowseButtonData);
        FileSelectionAdapter mzxmlFileBrowserSelectionAdapter = new FileSelectionAdapter();
        mzxmlFileBrowserSelectionAdapter.setShell(container.getShell());
        mzxmlFileBrowserSelectionAdapter.setText(mzxmlFileNameText);
        String sMzXML = "*." + MSFileInfo.MSFORMAT_MZXML_EXTENSION;
        String sMzML = "*." + MSFileInfo.MSFORMAT_MZML_EXTENSION;
        mzxmlFileBrowserSelectionAdapter.setFilterExtensions( new String[] {sMzXML + ";" + sMzML, "*.*"});
        mzxmlFileBrowserSelectionAdapter.setFilterNames( new String[] { "MS files (" + sMzML + "," + sMzML + ")",
        																"All files"});
        button3.addSelectionListener(mzxmlFileBrowserSelectionAdapter);

        //then add separator
        createSeparator(container,3);
        
        createQuantificationFileOption(container);
        
        // Required to avoid an error in the system
        setControl(container);
        setPageComplete(false);
    }
    
    private boolean matchesGroupType(String experimentType, String typeToMatch, boolean bYesOrNO) {
    	if( bYesOrNO ) {
    		return experimentType.equals(typeToMatch);
    	} else {
    		return ! experimentType.equals(typeToMatch);
    	}
    }
    
	protected int setMSGroups(Group experimentComposite, Set<String> allExperimentTypes, String sDefaultExpType, 
			Composite msGroup, String typeToMatch, boolean bYesOrNo) {
        int iNumGroups = 0;
        for (String experimentType: allExperimentTypes) {
        	Button experimentTypeButton;
        	if( matchesGroupType(experimentType, typeToMatch, bYesOrNo) ) {
        		experimentTypeButton = new Button(msGroup, SWT.RADIO);
        		iNumGroups++;
         	} else {
         		continue;
         	}
        	experimentTypeButton.setText(experimentType);
        	if (sDefaultExpType.equals(experimentType))
    			experimentTypeButton.setSelection(true);
    		experimentTypeButtons.add(experimentTypeButton);
    		experimentTypeButton.addSelectionListener(new SelectionListener() {
    			
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				if (((Button)e.getSource()).getSelection())
    					disableOtherButtons((Button) e.getSource());
    			}
    			
    			@Override
    			public void widgetDefaultSelected(SelectionEvent e) {
    			}
    		});
        }
        return iNumGroups;
	}
		
	protected int setLeftLCGroup(Group experimentComposite, Set<String> allExperimentTypes, String sDefaultExpType) {
        firstGroup = new Composite (experimentComposite, SWT.NONE);
        firstGroup.setLayout(new GridLayout(1, true));
        return setMSGroups(experimentComposite, allExperimentTypes, sDefaultExpType, firstGroup, Method.MS_TYPE_LC_LABEL, true);
	}	
	
	protected int setRightDIGroup(Group experimentComposite, Set<String> allExperimentTypes, String sDefaultExpType) {
		secondGroup = new Composite (experimentComposite, SWT.NONE);
		secondGroup.setLayout(new GridLayout(1, true));
        return setMSGroups(experimentComposite, allExperimentTypes, sDefaultExpType, secondGroup, Method.MS_TYPE_LC_LABEL, false);
	}
	
	protected List<Button> getExperimentTypeButtons() {
		return experimentTypeButtons;
	}
	
	protected MSPropertyDataFile getAnnotationDataFile( String sAnnotationFile ) {
		String sExtension = ".mzXML"; // assuming mzXML
		int iLastDot = mzxmlFileName.lastIndexOf(".");
		if( iLastDot >= 0 ) { // just in case there is a dot, then take what we see
			sExtension = mzxmlFileName.substring(iLastDot+1);
		} 
		String sMSFormat = "";
		if( sExtension.equalsIgnoreCase(MSFileInfo.MSFORMAT_MZML_EXTENSION) ) {
			sMSFormat = MSFileInfo.MSFORMAT_MZML_TYPE;
		} else {
			sMSFormat = MSFileInfo.MSFORMAT_MZXML_TYPE;				
		}
		MSPropertyDataFile mzXMLFile = new MSPropertyDataFile(mzxmlFileName, MSFileInfo.MSFORMAT_MZXML_CURRENT_VERSION, sMSFormat, 
				FileCategory.ANNOTATION_CATEGORY, MSFileInfo.MS_FILE_TYPE_DATAFILE, mzxmlFileName, 
				Arrays.asList(new String[]{FileCategory.ANNOTATION_CATEGORY.getLabel()}), false);
		return mzXMLFile;
	}

    /**
     * see which radio button is selected for the experiment type
     * @return the experiment type selected
     */
    protected String getExperimentSelection() {
		for (Button button : getExperimentTypeButtons()) {
			if (button.getSelection()) {
				String selectedLabel = button.getText();
				String gritsType = Method.getMsTypeByLabel(selectedLabel);
				return gritsType;
			}
		}
		return null;
	}
	
	protected void addBlankLabels(Composite group, int iNumLabels) {
   		for (int i=0; i < iNumLabels; i++) {
			new Label(group, SWT.NONE);
		}		
	}
    
    private void createExperimentGroup(Composite parent, Set<String> allExperimentTypes, String sDefaultExpType) {
		Group experimentComposite = new Group(parent, SWT.NONE);
		GridData expComboData = new GridData();
        expComboData.grabExcessHorizontalSpace = true;
        expComboData.horizontalAlignment = GridData.FILL;
        expComboData.horizontalSpan = 2;
        experimentComposite.setLayoutData(expComboData);
        
        GridLayout experimentLayout = new GridLayout(3, false);
        experimentComposite.setLayout(experimentLayout);
        int iNumLC = setLeftLCGroup( experimentComposite, allExperimentTypes, sDefaultExpType );
        int iNumDI = allExperimentTypes.size() - iNumLC;
        int iNumBlanks = iNumDI - iNumLC;
        addBlankLabels(firstGroup, iNumBlanks);
        new Label(experimentComposite, SWT.SEPARATOR | SWT.VERTICAL);
        iNumDI = setRightDIGroup( experimentComposite, allExperimentTypes, sDefaultExpType );
        iNumLC = allExperimentTypes.size() - iNumDI;
        iNumBlanks = iNumLC - iNumDI;
        addBlankLabels(secondGroup, iNumBlanks);        
 	}

    /**
     * de-select all the radio buttons but the selected
     * 
     * @param selected the button to be selected
     */
	protected void disableOtherButtons(Button selected) {
		for (Button button: experimentTypeButtons) {
			if (!button.equals(selected)) 
				button.setSelection(false);
		}
		
	}

	private void addInstrumentOptions(Composite container) {
    	/* moved from second page 
         * 
         */
    	Composite container2 = new Composite(container, SWT.NONE);
    	GridData gridData = new GridData();
    	gridData.horizontalSpan = 3;
    	gridData.grabExcessHorizontalSpace = true;
    	gridData.horizontalAlignment = GridData.FILL;
    	container2.setLayoutData(gridData);
    	container2.setLayout(new FillLayout());
        settingsPreference = new MassSpecSettingPreferenceUI(container2, SWT.NONE, this);
		settingsPreference.setPreferences(NewMSWizard.preferences);
		settingsPreference.initComponents();
		
	}

	private void createQuantificationFileOption(Composite container) {
    	GridData msDIOptionBtnGridData = new GridData();
    	msDIOptionBtnGridData.horizontalSpan = 3;
		msDIOptionBtnGridData.grabExcessHorizontalSpace = true;
		msDIOptionBtnGridData.horizontalAlignment = GridData.FILL;
		Group group1 = new Group(container, SWT.SHADOW_IN);
	    group1.setText("Do you have additional files to upload (eg. for quantification)?");
	    group1.setLayoutData(msDIOptionBtnGridData);
	    group1.setLayout(new RowLayout(SWT.HORIZONTAL));
	    Button yesOption = new Button(group1, SWT.RADIO);
	    yesOption.setText("Yes");
	    yesOption.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (yesOption.getSelection()) {
					nextPage = true;
					canFlipToNextPage();
					setPageComplete(isReadyToFinish());	
					// explicit call
					getWizard().getContainer().updateButtons();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    Button noOption = new Button(group1, SWT.RADIO);
	    noOption.setText("No");
	    noOption.setSelection(true);
	    noOption.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (noOption.getSelection()) {
					nextPage = false;
					canFlipToNextPage();
					setPageComplete(isReadyToFinish());	
					// if you went to the next page and come back, need to make sure the next page is marked complete
					if (fileUploadPage != null) fileUploadPage.setPageComplete(true);
					// explicit call
					getWizard().getContainer().updateButtons();
				}		
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
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

    protected boolean checkSameNameEntry() {
        //need to check if ms entry in the sample container has the same name
        //if found then show error msg
        if(sampleEntry != null)
        {
            for(Entry child : this.sampleEntry.getChildren())
            {
                if(child.getDisplayName().equals(nameText.getText()))
                {
                    setErrorMessage("Please choose a different name for ms.");
                    return true;
                }
            }
            setErrorMessage(null);
            return false;
        }
        else
        {
            //no parent is chosen, so it is not ready for next page
            setErrorMessage(null);
            return false;
        }
    }

    protected Label setMandatoryLabel(Label lable) {
        lable.setText(lable.getText()+"*");
        lable.setFont(boldFont);
        return lable;
    }
    
    @Override
    public boolean canFlipToNextPage() {
    	return nextPage && isReadyToFinish();
    }
    
    public IWizardPage getNextPage() {
        save();
        if (nextPage) {
        	if (fileUploadPage == null) {
	        	// create page for file upload
	        	fileUploadPage = new FileUploadPage();
	        	((Wizard) getWizard()).addPage(fileUploadPage);
	        	// explicit call
				getWizard().getContainer().updateButtons();
        	} else {
        		fileUploadPage.refresh();
        	}
        	return fileUploadPage;
        }
        return super.getNextPage();
    }

    /**
     * Check if ready to finish
     * @return
     */
    private boolean isReadyToFinish()
    {
        // test the 3 required variables first
        if( sampleText.getText().isEmpty() || sampleEntry == null ) {
            //			setErrorMessage("Sample cannot be blank.");
            return false;
        }

        if ( nameText.getText().isEmpty() ) {
            setErrorMessage("Display Name name name cannot be blank.");
            return false;			
        }

        if ( checkSameNameEntry() ) { 
            return false; 
        }
        
        //name should be at least 1 character
        if( ! nameText.getText().isEmpty() && nameText.getText().length() >= 128)
        {
            setErrorMessage("Name must be less than 128 characters.");
            return false;
        }		

        if( ! descriptionText.getText().isEmpty() && 
                descriptionText.getText().length() >= Integer.parseInt(PropertyHandler.getVariable("descriptionLength")))
        {
            setErrorMessage("Description must be less than 1024 characters.");
            return false;
        }

        // now check existence of any specified files
        if ( ! rawFileNameText.getText().isEmpty() ) {
            if ( ! fileExists(rawFileNameText.getText()) ) {
                setErrorMessage("Specified file for 'Raw File' does not exist.");
                return false;
            }
        }
        if ( ! mzxmlFileNameText.getText().isEmpty() ) {
            if ( ! fileExists(mzxmlFileNameText.getText()) ) {
                setErrorMessage("Specified file for 'mzML/mzXML File' does not exist.");
                return false;
            }
        }
        
        if( ! settingsPreference.isPageCompete() ) {
    		setErrorMessage( settingsPreference.getErrorMessage() );
    		return false;
    	}
        setErrorMessage(null);
        return true;
    }

    private boolean fileExists(String _sFileName) {
        File file = new File(_sFileName);
        return file.exists();

    }

    public void setMsMsMzXMLText( String _sText ) {
        this.mzxmlFileNameText.setText(_sText);
    }
    
    public void setRawFileNameText(String _sText) {
		this.rawFileNameText.setText(_sText);
	}
    
    public void save()
    {
    	//need to save variables
        this.name = nameText.getText();
        this.description = descriptionText.getText();
		//msExperimentType = msExperimentCombo.getText();
        msExperimentType = getExperimentSelection();
		if( MassSpecPreferencePage.hasChanged(msExperimentType, NewMSWizard.preferences.getExperimentType(), NewMSWizard.preferences.getAllExperimentTypes()) ) {
			NewMSWizard.preferences.setExperimentType(msExperimentType);
		}
        if( ! rawFileNameText.getText().equals("") ) {
        	this.rawFileName = rawFileNameText.getText();
        }
        if( (this.mzxmlFileName != null && ! this.mzxmlFileName.equals(MassSpecProperty.CONVERT_RAW) ) || ! mzxmlFileNameText.getText().equals("")) {
        	this.mzxmlFileName = mzxmlFileNameText.getText();
        }
        MSPropertyDataFile parent = null;
		if (rawFileName != null) {
			String sExtension = ".raw"; // assuming raw
			int iLastDot = rawFileName.lastIndexOf(".");
			if( iLastDot >= 0 ) { // just in case there is a dot, then take what we see
				sExtension = rawFileName.substring(iLastDot+1);
			} 
			parent = new MSPropertyDataFile(rawFileName, MSFileInfo.MSFORMAT_RAW_CURRENT_VERSION, MSFileInfo.MSFORMAT_RAW_TYPE, 
					FileCategory.ANNOTATION_CATEGORY, MSFileInfo.MS_FILE_TYPE_INSTRUMENT, rawFileName, 
					Arrays.asList(new String[]{FileCategory.ANNOTATION_CATEGORY.getLabel()}), true);
			addToFileList(parent);
		}
		if (mzxmlFileName != null) {
			MSPropertyDataFile mzXMLFile = getAnnotationDataFile(mzxmlFileName);
			if (parent == null) {
				// create an empty parent
				parent = new MSPropertyDataFile("", MSFileInfo.MSFORMAT_RAW_CURRENT_VERSION, MSFileInfo.MSFORMAT_RAW_TYPE, 
						FileCategory.ANNOTATION_CATEGORY, MSFileInfo.MS_FILE_TYPE_INSTRUMENT, "", 
						Arrays.asList(new String[]{FileCategory.ANNOTATION_CATEGORY.getLabel()}), true);
				addToFileList(parent);
			} 
			parent.addChild(mzXMLFile);
		}
        settingsPreference.updatePreferences();
    }

	/**
     * add the given file to wizard's fileList if is not already added
     * 
     * @param parent file to add
     */
    private void addToFileList(MSPropertyDataFile parent) {
    	boolean exists = false;
    	for (MSPropertyDataFile f: ((NewMSWizard) getWizard()).getAllFiles()) {
    		if (f.getName().equals(parent.getName()) && f.getCategory().equals(parent.getCategory()) &&
    				f.getMSFileType().equals(parent.getMSFileType())) {
    			exists = true;
    			break;
    		}		
    	}
    	if (!exists) ((NewMSWizard) getWizard()).getAllFiles().add(parent);
		
	}

	public Entry getSampleEntry() {
        return sampleEntry;
    }

    public void setSampleEntry(Entry sampleEntry) {
        this.sampleEntry = sampleEntry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMSDescription() {
        return description;
    }

    public void setMSDescription(String description) {
        this.description = description;
    }

    public String getRawFileName() {
        return rawFileName;
    }

    public void setRawFileName(String rawFileName) {
        this.rawFileName = rawFileName;
    }

    public String getMzxmlFileName() {
        return mzxmlFileName;
    }

    public void setMzxmlFileName(String mzxmlFileName) {
        this.mzxmlFileName = mzxmlFileName;
    }

	public String getMsExperimentType() {
		return msExperimentType;
	}

	public void setMsExperimentType(String msExperimentType) {
		this.msExperimentType = msExperimentType;
	}
	
    @Override
    public void updateComponent( SelectionAdapter adapter ) {
        if ( adapter instanceof EntrySelectionAdapter ) { // update the Sample Data
            this.sampleEntry = ( (EntrySelectionAdapter) adapter).getEntry();
            this.sampleText.setText( ( (EntrySelectionAdapter) adapter).getEntry().getDisplayName() );
            setPageComplete( isReadyToFinish() );
        }	
    }

    @Override
	public void propertyChange(PropertyChangeEvent event) {
		if( isReadyToFinish() ) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
		
	}

	public MassSpecPreference getSettingsPreference() {
		return settingsPreference.getPreferences();
	}
	
	public boolean isNextPage() {
		return nextPage;
	}
	
	public FileUploadPage getFileUploadPage() {
		return fileUploadPage;
	}
}