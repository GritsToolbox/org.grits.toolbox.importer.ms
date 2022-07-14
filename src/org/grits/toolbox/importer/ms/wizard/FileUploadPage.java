package org.grits.toolbox.importer.ms.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.entry.ms.dialog.MassSpecFileAddDialog;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecOverviewPage;
import org.grits.toolbox.entry.ms.views.tabbed.content.MassSpecFileListTableComposite;
import org.grits.toolbox.ms.file.FileCategory;
import org.grits.toolbox.ms.file.MSFileInfo;

public class FileUploadPage extends WizardPage {
	private static final Logger logger = Logger.getLogger(FileUploadPage.class);
	
	List<MSPropertyDataFile> fileList;

	private Tree fileTree;
	private TreeViewer fileTreeViewer;

	protected FileUploadPage() {
		super("New MS Experiment");
		setTitle("Add files to MS Experiment");
		setDescription("Add files to MS Experiment");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.verticalSpacing = 10;
        container.setLayout(gridLayout);
        
        MassSpecFileListTableComposite comp = new MassSpecFileListTableComposite(container, SWT.WRAP, false);  // we don't need inUse column
        // display the files uploaded so far
        fileList = ((NewMSWizard) getWizard()).getAllFiles();
        if (fileList == null) fileList = new ArrayList<>();
        comp.setFileList(fileList);
        comp.initComponents();
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 3));
        fileTreeViewer = comp.getFileTableViewer();
        fileTree = fileTreeViewer.getTree();
        
        new Label(container, SWT.NONE);
        // add and delete buttons
        Button addButton = new Button(container, SWT.PUSH);
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// find selected file
        		TreeItem[] items = fileTree.getSelection();
        		// since we allow single selection only, get the first one
        		if (items.length > 0) {
        			TreeItem selected = items[0];
        			if (selected.getParentItem() == null) { // parent node
        				MSPropertyDataFile parentFile = (MSPropertyDataFile) selected.getData();
        				addFile (Display.getCurrent().getActiveShell(), parentFile);
        			} else {
        				addFile (Display.getCurrent().getActiveShell(), null);
        			}
        		}
        		else {
        			addFile (Display.getCurrent().getActiveShell(), null);
        		}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
        Button deleteButton = new Button(container, SWT.PUSH);
        deleteButton.setText("Remove");
        deleteButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = fileTree.getSelection();
        		// since we allow single selection only, get the first one
        		if (items.length > 0) {
        			TreeItem selected = items[0];
        			MSPropertyDataFile dataFile = (MSPropertyDataFile) selected.getData();
        			deleteFile(Display.getCurrent().getActiveShell(), dataFile);
        		} else {
        			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "No selection", "Please select a file entry below to delete");
        		}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
        
        setControl(container);
        setPageComplete(false);
	}

	/**
	 * remove the given file from the file list
	 * 
	 * @param activeShell shell to use for displaying messages
	 * @param dataFile file to be removed
	 */
	private void deleteFile(Shell activeShell, MSPropertyDataFile dataFile) {
		if (dataFile == null)  // nothing to delete
			return;
		// check if the selection is a parent selection 
		if (dataFile.getIsParent()) {
			// check if it has children
			if (dataFile.getChildren() != null && !dataFile.getChildren().isEmpty()) {
				logger.debug(dataFile + " cannot be deleted since it has children. Delete them first then try to delete this entry again!");
				MessageDialog.openInformation(activeShell, "Parent", dataFile.getOriginalFileName() + " cannot be deleted since it has children. Delete them first then try to delete this entry again!");
				return;
			} 
		}
		// remove from the first page if necessary
		if (dataFile.getName().equals(((NewMSWizard) getWizard()).getOne().getRawFileName()))
			((NewMSWizard) getWizard()).getOne().setRawFileNameText("");
		if (dataFile.getName().equals(((NewMSWizard) getWizard()).getOne().getMzxmlFileName()))
			((NewMSWizard) getWizard()).getOne().setMsMsMzXMLText("");
		if (dataFile.getIsParent()) {
			// remove the dataFile from the list
			fileList.remove(dataFile);
			fileTreeViewer.refresh();
			fileTreeViewer.expandAll();
		} else if (!dataFile.getIsParent()){
			// remove the dataFile from the parent
			for (MSPropertyDataFile msPropertyDataFile : fileList) {
				if (msPropertyDataFile.getChildren() != null && msPropertyDataFile.getChildren().contains(dataFile))
					msPropertyDataFile.getChildren().remove(dataFile);
			}
			fileTreeViewer.refresh();
			fileTreeViewer.expandAll();
		}	
	}

	/**
	 * add a new file to the file list
	 * @param shell shell to use for displaying messages
	 * @param parentFile parent file to display in the add dialog as corresponding "instrument file" if any. Maybe null.
	 */
	private void addFile(Shell shell, MSPropertyDataFile parentFile) {
		MassSpecFileAddDialog dialog = new MassSpecFileAddDialog(shell, fileList, parentFile);
		if (dialog.open() == Window.OK) {
			String file = dialog.getFileName();
			String fileFormat = "";
			if (file != null && !file.isEmpty()) {
				fileFormat = MassSpecOverviewPage.findFileFormatType(dialog.getFileCategory(), file);
			}
			String originalFileName = file == null || file.isEmpty() ? "" : file.substring(file.lastIndexOf(File.separator)+1);
			MSPropertyDataFile newDataFile = new MSPropertyDataFile(file, MassSpecProperty.CURRENT_VERSION, 
					fileFormat, dialog.getFileCategory(), MassSpecOverviewPage.findMSFileType (fileFormat), originalFileName, Arrays.asList(new String[] {dialog.getFileCategory().getLabel()}));
			MSPropertyDataFile selectedParentFile = dialog.getParentFile();
			if (selectedParentFile != null) { // child node
				if (!file.isEmpty())  {// do not allow empty children nodes
					selectedParentFile.addChild(newDataFile);
				} else {  // should not happen since dialog should prevent it
					MessageDialog.openError(shell, "Error", "File name cannot be empty. Not adding the new entry!");
				}
			}
			else {
				if (dialog.isInstrumentFile()) {
					// adding a new parent
					newDataFile.setIsParent(true);
					fileList.add(newDataFile);
				} else { // not an instrument file but parent is not available, create a new empty parent
					List<String> purpose = dialog.getFileCategory().equals(FileCategory.ANNOTATION_CATEGORY) ? 
							Arrays.asList(new String[] {FileCategory.ANNOTATION_CATEGORY.getLabel()}) : Arrays.asList(new String[] {FileCategory.EXTERNAL_QUANTIFICATION_CATEGORY.getLabel()});
					MSPropertyDataFile newParent = new MSPropertyDataFile("", MassSpecProperty.CURRENT_VERSION, 
							MSFileInfo.MSFORMAT_RAW_EXTENSION, dialog.getFileCategory(), MSFileInfo.MS_FILE_TYPE_INSTRUMENT, "", purpose );
					newParent.setIsParent(true);
					newParent.addChild(newDataFile);
					fileList.add(newParent);
				}
				
			}
			
			fileTreeViewer.refresh();
			fileTreeViewer.expandAll();
			if (!fileList.isEmpty()) setPageComplete(true);
		}
		
	}

	public List<MSPropertyDataFile> getFileList() {
		return fileList;
	}
	
	public void setFileList(List<MSPropertyDataFile> fileList) {
		this.fileList = fileList;
	}
	
	public void refresh() {
		fileTreeViewer.refresh();
		fileTreeViewer.expandAll();
	}
}
