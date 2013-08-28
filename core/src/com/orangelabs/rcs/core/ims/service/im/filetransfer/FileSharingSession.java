package com.orangelabs.rcs.core.ims.service.im.filetransfer;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.chat.ListOfParticipant;
import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * Abstract file sharing session 
 * 
 * @author jexa7410
 */
public abstract class FileSharingSession extends ImsServiceSession {
    /**
     * Contribution ID
     */
    private String contributionId = null;	
    
    /**
	 * Content to be shared
	 */
	private MmContent content;
	
	/**
	 * File transfered
	 */
	private boolean fileTransfered = false;

    /**
     * List of participants
     */
    protected ListOfParticipant participants = new ListOfParticipant();

    /**
	 * Thumbnail
	 */
	private byte[] thumbnail = null;
	
    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param content Content to be shared
	 * @param contact Remote contact
	 * @param thumbnail Thumbnail
	 */
	public FileSharingSession(ImsService parent, MmContent content, String contact, byte[] thumbnail) {
		super(parent, contact);
		
		this.content = content;
		this.thumbnail = thumbnail;
	}
	
	/**
	 * Return the contribution ID
	 * 
	 * @return Contribution ID
	 */
	public String getContributionID() {
		return contributionId;
	}	
	
	/**
	 * Set the contribution ID
	 * 
	 * @param id Contribution ID
	 */
	public void setContributionID(String id) {
		this.contributionId = id;
	}
	
	/**
	 * Returns the content
	 * 
	 * @return Content 
	 */
	public MmContent getContent() {
		return content;
	}
	
	/**
	 * Returns the list of participants involved in the transfer
	 * 
	 * @return List of participants 
	 */
	public ListOfParticipant getParticipants() {
		return participants;
	}
	
	/**
	 * Set the content
	 * 
	 * @param content Content  
	 */
	public void setContent(MmContent content) {
		this.content = content;
	}	
	
	/**
	 * Returns the "file-transfer-id" attribute
	 * 
	 * @return String
	 */
	public String getFileTransferId() {
		return "" + System.currentTimeMillis();
	}	
	
	/**
	 * File has been transfered
	 */
	public void fileTransfered() {
		this.fileTransfered = true;
	}
	
	/**
	 * Is file transfered
	 * 
	 * @return Boolean
	 */
	public boolean isFileTransfered() {
		return fileTransfered; 
	}
	
	/**
	 * Returns max file sharing size
	 * 
	 * @return Size in bytes
	 */
	public static int getMaxFileSharingSize() {
		return RcsSettings.getInstance().getMaxFileTransferSize()*1024;
	}

    /**
     * Returns the thumbnail
     * 
     * @return Thumbnail
     */
    public byte[] getThumbnail() {
    	return thumbnail;
    }

    /**
     * Set the thumbnail
     *
     * @param Thumbnail
     */
    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }
}
