package org.grits.toolbox.importer.ms.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.grits.toolbox.entry.ms.preference.MassSpecSettingPreferenceUI;

public class PageTwo extends WizardPage implements IPropertyChangeListener {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(PageTwo.class);

	// setup bold font
	protected final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT); 

	private Composite container;
	private MassSpecSettingPreferenceUI settingsPreference = null;

	public PageTwo() {
		super("New MS Experiment");
		setTitle("New MS Experiment");
		setDescription("Create a new MS Experiment.");
	}
	
	public MassSpecSettingPreferenceUI getSettingsPreference() {
		return settingsPreference;
	}
	public void setSettingsPreference(MassSpecSettingPreferenceUI settingsPreference) {
		this.settingsPreference = settingsPreference;
	}	
	
	@Override
	public void createControl(Composite parent) {
		//This is important. DO NOT USE parent directly!
		container = new Composite(parent, SWT.NONE);
		//has to be gridLayout, since it extends TitleAreaDialog
		FillLayout gridLayout = new FillLayout();
		container.setLayout(gridLayout);
		settingsPreference = new MassSpecSettingPreferenceUI(container, SWT.NONE, this);
		settingsPreference.setPreferences(NewMSWizard.preferences);
		settingsPreference.initComponents();
		setControl(container);
		setPageComplete(true);
	}
	
	/**
	 * Check if ready to finish
	 * @return
	 */
	private boolean isReadyToFinish() {
    	if( ! settingsPreference.isPageCompete() ) {
    		setErrorMessage( settingsPreference.getErrorMessage() );
    		return false;
    	}
        setErrorMessage(null);
        return true;
	}

	public IWizardPage getNextPage() {
		save();
		return super.getNextPage();
	}

	public void save() {
		settingsPreference.updatePreferences();
//		NewMSWizard.preferences.saveValues();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if( isReadyToFinish() ) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
		
	}


}