package org.grits.toolbox.importer.ms.process;

import java.io.File;

import org.apache.log4j.Logger;

public class RawToMzXMLConverterProcess extends Thread {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(RawToMzXMLConverterProcess.class);
	public final static long MAX_UPLOAD_SIZE = 20; // 20MB?
//	private WebServiceClient t_client = null;
//	protected String sRawFile = null;
//	protected String sMzXMLFile = null;
//	private String sDestPath = null;
//	private RawToMzXMLConverterDialog m_progressDialog = null;
	private volatile boolean isCanceled = false;
//
//	public RawToMzXMLConverterProcess( String _sRawFile, String _sDestPath, RawToMzXMLConverterDialog _dialog ) {
//		t_client = new WebServiceClient("http://glycomics.ccrc.uga.edu/rawtomzxml/");
//		this.sRawFile = _sRawFile;
//		this.sDestPath = _sDestPath;
//		setDialog(_dialog);
//	}
//
//	public void setDialog(RawToMzXMLConverterDialog a_dialog)
//	{
//		this.m_progressDialog = a_dialog;
//	}
//
//	public String getResult() {
//		return this.sMzXMLFile;
//	}
//
	public static boolean checkFileSize( File f ) {
		double fSizeInMB = (double) f.length() / 1024.0d / 1024.0d;
		return fSizeInMB <= MAX_UPLOAD_SIZE;		
	}

	@Override
	public void run() {
//		try {
//			boolean successful = this.threadStart();
//			this.m_progressDialog.threadFinished(this, successful);
//		} catch (Exception e) {
//			this.m_progressDialog.endWithException(e);
//			//			e.printStackTrace();
//		}
	}

//	public boolean threadStart() throws ClientProtocolException, IOException, WsClientException, InterruptedException {
//		if ( this.sRawFile != null ) {
//			String sMzXML = processFile(this.sRawFile);
//			if( sMzXML == null )
//				return false;
//			this.sMzXMLFile = sMzXML;
//		}
//		return true;
//	}
//
//
//	private String processFile( String _sFileName ) throws ClientProtocolException, IOException, WsClientException, InterruptedException
//	{
//		String sDestFile = null;
//		try {
//			File fRawFile = new File(_sFileName);
//			if ( ! fRawFile.exists() ) {
//				throw new IOException("Raw file '" + _sFileName + "' does not exit.");
//			}
//			Integer t_jobID = t_client.submitJob(fRawFile, "idawg", "idawg", true, false, true);
//			JobStatus t_status = new JobStatus();
//			t_status.setStatus(JobStatus.PENDING);
//			while ( ! isCanceled && t_status.getStatus().equals(JobStatus.PENDING) )
//			{
//				// request job status based on the created job ID
//				t_status = t_client.requestJobStatus(t_jobID);
//				this.m_progressDialog.updateProgressBar(t_status.getStatus(), this);
//				Thread.sleep(1000);
//			}
//			if ( this.isCanceled ) {
//				this.m_progressDialog.updateProgressBar("Canceled...", this);
//				return null;
//			}
//
//			// download file
//			String sMzXML = fRawFile.getName();
//			int iInx = sMzXML.lastIndexOf("."); // assuming this is file extension
//			if( iInx > 0 ) {
//				sMzXML = sMzXML.substring(0, iInx);
//			} 
//			sMzXML += ".mzXML_" + t_jobID;
//			if ( this.isCanceled ) {
//				this.m_progressDialog.updateProgressBar("Canceled...", this);
//				return null;
//			}
//			this.m_progressDialog.updateProgressBar("Downloading file...", this);
//			sDestFile = this.sDestPath + File.separator + sMzXML;
//			t_client.downloadFile(new File(sDestFile), t_status.getUrl());
//			this.m_progressDialog.updateProgressBar("Success!", this);
//		} catch( Exception ex ) {
// 			logger.error(ex.getMessage(), ex);
//		}
//		return sDestFile;
//	}
//
	public void cancelWork() {
		this.isCanceled = true;
	}

}
