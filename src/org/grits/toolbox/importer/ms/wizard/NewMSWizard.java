package org.grits.toolbox.importer.ms.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.preference.MassSpecPreference;
import org.grits.toolbox.entry.ms.preference.MassSpecPreferenceLoader;
import org.grits.toolbox.entry.ms.property.FileLockManager;
import org.grits.toolbox.entry.ms.property.FileLockingUtils;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecMetaData;
import org.grits.toolbox.importer.ms.Activator;
import org.grits.toolbox.importer.ms.handler.NewMSHandler;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.file.MSFileInfo;

public class NewMSWizard extends Wizard {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(NewMSWizard.class);
	protected PageOne one = null;
	private Entry sampleEntry = null;

	public static MassSpecPreference preferences = null;

	boolean fileTypeCorrect = true;
	private Entry newMSEntry;
	private List<MSPropertyDataFile> allFiles = new ArrayList<>();

	public NewMSWizard() {
		super();
		setNeedsProgressMonitor(true);
		preferences = MassSpecPreferenceLoader.getMassSpecPreferences();
	}

	@Override
	public void addPages() {
		one = getNewPageOne();
		if(sampleEntry != null)
		{
			one.setSampleEntry(sampleEntry);
		}
		addPage(one);
		this.setForcePreviousAndNextButtons(true);  // this is required since we will add next page dynamically
	}

	protected MassSpecProperty getNewMSProperty() {
		MassSpecProperty property = new MassSpecProperty();
		property.setVersion(MassSpecProperty.CURRENT_VERSION);
		return property;
	}
	
	protected MassSpecMetaData getNewMSMetaData() {
		MassSpecMetaData model = new MassSpecMetaData();
		model.setVersion(MassSpecMetaData.CURRENT_VERSION);
		return model;
	}
	
	public void setSample(Entry entry)
	{
		this.sampleEntry = entry;
	}
	
	@Override
	public boolean performFinish() {
		getOne().save();
		NewMSWizard.preferences.saveValues();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					fileTypeCorrect = doFinish (monitor, allFiles);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			logger.error(realException.getMessage(), realException);
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return fileTypeCorrect;
	}
	
	protected PageOne getNewPageOne() {
		return new PageOne();
	}
	
	private boolean doFinish(IProgressMonitor monitor, List<MSPropertyDataFile> allFiles) throws CoreException{
		try {

			monitor.beginTask("Finishing...", allFiles != null ? allFiles.size() + 2 : 2);
			// Check all XML Files for validity
			
			monitor.subTask("Checking files for validity");
			boolean bPass = true;
			
			for (MSPropertyDataFile msPropertyDataFile : allFiles) {
				// validate the file
				if (msPropertyDataFile.getMSFileType().equals(MSFileInfo.MS_FILE_TYPE_DATAFILE) || msPropertyDataFile.getMSFileType().equals(MSFileInfo.MS_FILE_TYPE_PROCESSED)) {  // do not check for Instrument files
					MSFile msFile = msPropertyDataFile.getMSFileWithReader("", getOne().getMsExperimentType());
					if (msFile.getReader() != null)
						bPass = msFile.getReader().isValid(msFile);
					if (! bPass) {
						throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The specified MS File (" + msPropertyDataFile.getName() + ") is not valid.\n\nPlease correct this and continue."));
					}
				}
				if (msPropertyDataFile.getChildren() != null) {
					for (MSPropertyDataFile child : msPropertyDataFile.getChildren()) {
						if (child.getMSFileType().equals(MSFileInfo.MS_FILE_TYPE_DATAFILE) 
								|| child.getMSFileType().equals(MSFileInfo.MS_FILE_TYPE_PROCESSED)) {  // do not check for Instrument files
							MSFile msFile = child.getMSFileWithReader("", getOne().getMsExperimentType());
							if (msFile.getReader() != null)
								bPass = msFile.getReader().isValid(msFile);
							if (! bPass) {
								throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The specified MS File (" + child.getName() + ") is not valid.\n\nPlease correct this and continue."));
							}
						}
					}
				}
				monitor.worked(1);
			}
			
			String sMsPath = NewMSHandler.createMassSpecPath(getShell(), this);
			if ( sMsPath == null ) { // failed!
				throw new Exception( "Unable to create mass spec path.");
			}

			monitor.subTask("Creating entry");
			newMSEntry = createNewEntry(allFiles);
			monitor.worked(1);
			if( newMSEntry == null ) {
				return false;
			}
			
			monitor.subTask("Copying files into workspace");
			MassSpecProperty.copyMSFilesIntoWorkspace(allFiles,
					sMsPath, newMSEntry);
			
			monitor.subTask("Updating File Lock");
			try {
				// add all the files to the .lockFile, if not added before
				String metadataPath = ((MassSpecProperty) newMSEntry.getProperty()).getMSSettingsFile().getName();
				String lockFileName = sMsPath + File.separator;
				if (metadataPath.lastIndexOf(File.separator) != -1)
					lockFileName += metadataPath.substring(0, metadataPath.lastIndexOf(File.separator)) + File.separator + FileLockManager.LOCKFILE_NAME;
				else
					lockFileName += FileLockManager.LOCKFILE_NAME;
				FileLockManager fileLockManager = FileLockingUtils.readLockFile(lockFileName);
				// add all the files to fileLockManager
				MassSpecMetaData metaData = ((MassSpecProperty) newMSEntry.getProperty()).getMassSpecMetaData();
				for (MSPropertyDataFile file: metaData.getFileList()) {
					fileLockManager.addFile(file.getName());
					if (file.getChildren() != null) {
						for (MSPropertyDataFile child: file.getChildren()) {
							fileLockManager.addFile(child.getName());
						}
					}
				}
				FileLockingUtils.writeLockFile(fileLockManager, lockFileName);
			} catch (Exception e) {
				logger.error("Error adding file to the .lockfile", e);
			}
			
			monitor.worked(1);
			monitor.done();
			return true;

		} catch (CoreException ex) {
			throw ex;
		} catch( Exception ex ) {
			logger.error("Error in performFinish.", ex);
		}

		return false;
	}	

	public PageOne getOne() {
		return one;
	}
	
	public Entry createNewEntry(List<MSPropertyDataFile> allFiles )
	{
		Entry entry = new Entry();
		entry.setDisplayName(one.getName());
		MassSpecProperty property = getNewMSProperty();
		MassSpecMetaData model = getNewMSMetaData();
		property.setMassSpecMetaData(model);
		model.setCreationDate(new Date());
		model.setUpdateDate(model.getCreationDate());
		property.getMassSpecMetaData().setDescription(one.getMSDescription());
		property.getMassSpecMetaData().setMsExperimentType(one.getMsExperimentType());
		property.getMassSpecMetaData().setInstrument(one.getSettingsPreference().getInstrument());
		property.getMassSpecMetaData().setFileList(allFiles);
		entry.setProperty(property);
		return entry;
	}

	public Entry getEntry () {
		return newMSEntry;
	}
	
	public List<MSPropertyDataFile> getAllFiles() {
		return allFiles;
	}
}
