package org.grits.toolbox.importer.ms.handler;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.sample.property.SampleProperty;
import org.grits.toolbox.importer.ms.wizard.NewMSWizard;

/**
 * Create a new MS dialog
 * @author dbrentw
 *
 */
public class NewMSHandler {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(NewMSHandler.class);
	
	@Inject private static IGritsDataModelService gritsDataModelService = null;
	@Inject static IGritsUIService gritsUIService = null;

	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IEventBroker eventBroker, @Named (IServiceConstants.ACTIVE_SHELL) Shell shell, EPartService partService) {
		
		Entry selectedEntry = null;
		if(object instanceof Entry)
		{
			selectedEntry = (Entry) object;
		}
		else if (object instanceof StructuredSelection)
		{
			if(((StructuredSelection) object).getFirstElement() instanceof Entry)
			{
				selectedEntry = (Entry) ((StructuredSelection) object).getFirstElement();
			}
		}
		if(selectedEntry != null)
		{
			if(selectedEntry.getProperty() == null 
					|| !SampleProperty.TYPE.equals(selectedEntry.getProperty().getType()))
			{
				selectedEntry = null;
			}
		}
		// try getting the last selection from the data model
		if(selectedEntry == null
				&& gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
		{
			selectedEntry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
		}
		if(selectedEntry != null)
		{
			if(selectedEntry.getProperty() == null 
					|| !SampleProperty.TYPE.equals(selectedEntry.getProperty().getType()))
			{
				selectedEntry = null;
			}
		}
		
		createNewMSDialog(shell, eventBroker, selectedEntry, partService);
		return null;
	}

	private void createNewMSDialog(Shell shell, IEventBroker eventBroker, Entry sample, EPartService partService) {
		NewMSWizard wizard = new NewMSWizard();
		//set the Sample entry if there is one chosen
		wizard.setSample(sample);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		try {
			int iRes = dialog.open();
			if (iRes == Window.OK) {
				try {
					Entry newMSEntry = wizard.getEntry();
					if (newMSEntry == null) {
						ErrorUtils.createErrorMessageBox(shell, "Unable to create the new MS entry.");	
						return;
					}
						
					gritsDataModelService.addEntry(wizard.getOne().getSampleEntry(), newMSEntry);
					try
					{
						ProjectFileHandler.saveProject(wizard.getOne().getSampleEntry().getParent());
					} catch (IOException e)
					{
						logger.error("Something went wrong while saving project entry \n" + e.getMessage(),e);
						logger.fatal("Closing project entry \""
								+ wizard.getOne().getSampleEntry().getParent().getDisplayName() + "\"");
						gritsDataModelService.closeProject(wizard.getOne().getSampleEntry());
						throw e;
					}
					// post will not work because of synchronization 
					// the selection needs to change before we try to open the part!
					eventBroker.send(IGritsDataModelService.EVENT_SELECT_ENTRY, newMSEntry);
					// need to set the partService to refresh gritsUIServices' stale partService, see ticket #799
					gritsUIService.setPartService(partService);
					gritsUIService.openEntryInPart(newMSEntry);
				} catch( Exception ex ) {
					logger.error(ex.getMessage(),ex);
					ErrorUtils.createErrorMessageBox(shell, "Unable to create the new MS entry.",ex);					
				}
			}						
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			ErrorUtils.createErrorMessageBox(shell, "Exception", e);
		}
	}

	public static String createMassSpecPath( Shell _shell, NewMSWizard _wizard ) throws IOException {
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
		Entry projectEntry = DataModelSearch.findParentByType(_wizard.getOne().getSampleEntry(), ProjectProperty.TYPE);
		String projectName = projectEntry.getDisplayName();

		//create ms folder
		File msFolder = new File(workspaceLocation + projectName + File.separator + MassSpecProperty.getFoldername() );
		if(!msFolder.exists())
		{
			if(!msFolder.mkdir())
			{
				IOException e = new IOException("Unable to create MS folder: (" + msFolder + ")");
				logger.error(e.getMessage(), e);
				ErrorUtils.createErrorMessageBox(_shell, "Unable to create MS folder",e);
				//need to close() the shell;
				throw e;
			}
		}
		return msFolder.getAbsolutePath();
	}

}
