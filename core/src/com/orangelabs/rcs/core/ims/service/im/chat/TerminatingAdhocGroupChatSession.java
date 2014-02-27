/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.core.ims.service.im.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpEventListener;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpSession;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.SessionTimerManager;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terminating ad-hoc group chat session
 * 
 * @author jexa7410
 */
public class TerminatingAdhocGroupChatSession extends GroupChatSession implements MsrpEventListener {

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Constructor
     * 
	 * @param parent IMS service
	 * @param invite Initial INVITE request
	 */
	public TerminatingAdhocGroupChatSession(ImsService parent, SipRequest invite) {
		super(parent, ChatUtils.getReferredIdentity(invite), ChatUtils.getListOfParticipants(invite));

		// Detect if it's a rejoin
		if (getParticipants().getList().size() == 0) {
			if (logger.isActivated()) {
	    		logger.info("Invite to join a group chat");
	    	}
		}
		
		// Set subject
		String subject = ChatUtils.getSubject(invite);
		setSubject(subject);
		
		// Create dialog path
		createTerminatingDialogPath(invite);
		
		// Set contribution ID
		String id = ChatUtils.getContributionId(invite);
		setContributionID(id);
		
		// Check if chatID already exists in provider
		if (RichMessaging.getInstance().getGroupChatStatus(id) != -1) {
			if (logger.isActivated()) {
	    		logger.info("Invite to rejoin or restart a group chat");
	    	}
		}
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new ad-hoc group chat session as terminating");
	    	}

            if (RcsSettings.getInstance().isGroupChatAutoAccepted() || ChatUtils.getHttpFTInfo(getDialogPath().getInvite()) != null) {
                if (logger.isActivated()) {
                    logger.debug("Auto accept group chat invitation");
                }
            } else {
                if (logger.isActivated()) {
                    logger.debug("Accept manually group chat invitation");
                }
    	    	// Send a 180 Ringing response
    			send180Ringing(getDialogPath().getInvite(), getDialogPath().getLocalTag());
    			
    			// Wait invitation answer
    	    	int answer = waitInvitationAnswer();
    			if (answer == ImsServiceSession.INVITATION_REJECTED) {
    				if (logger.isActivated()) {
    					logger.debug("Session has been rejected by user");
    				}
    				
    		    	// Remove the current session
    		    	getImsService().removeSession(this);
    
    		    	// Notify listeners
    		    	for(int i=0; i < getListeners().size(); i++) {
    		    		getListeners().get(i).handleSessionAborted(ImsServiceSession.TERMINATION_BY_USER);
    		        }
    				return;
    			} else
    			if (answer == ImsServiceSession.INVITATION_NOT_ANSWERED) {
    				if (logger.isActivated()) {
    					logger.debug("Session has been rejected on timeout");
    				}
    
    				// Ringing period timeout
    				send486Busy(getDialogPath().getInvite(), getDialogPath().getLocalTag());
    				
    		    	// Remove the current session
    		    	getImsService().removeSession(this);
    
    		    	// Notify listeners
        	    	for(int i=0; i < getListeners().size(); i++) {
        	    		getListeners().get(i).handleSessionAborted(ImsServiceSession.TERMINATION_BY_TIMEOUT);
    		        }
    				return;
    			} else
                if (answer == ImsServiceSession.INVITATION_CANCELED) {
                    if (logger.isActivated()) {
                        logger.debug("Session has been canceled");
                    }
                    return;
                }
            }

        	// Parse the remote SDP part
			String remoteSdp = getDialogPath().getInvite().getSdpContent();
        	SdpParser parser = new SdpParser(remoteSdp.getBytes());
    		Vector<MediaDescription> media = parser.getMediaDescriptions();
			MediaDescription mediaDesc = media.elementAt(0);
			MediaAttribute attr1 = mediaDesc.getMediaAttribute("path");
            String remotePath = attr1.getValue();
            String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription, mediaDesc);
    		int remotePort = mediaDesc.port;
			
    		// Changed by Deutsche Telekom
    		String fingerprint = SdpUtils.extractFingerprint(parser, mediaDesc);

            // Extract the "setup" parameter
            String remoteSetup = "passive";
			MediaAttribute attr2 = mediaDesc.getMediaAttribute("setup");
			if (attr2 != null) {
				remoteSetup = attr2.getValue();
			}
            if (logger.isActivated()){
				logger.debug("Remote setup attribute is " + remoteSetup);
			}
            
    		// Set setup mode
            String localSetup = createSetupAnswer(remoteSetup);
            if (logger.isActivated()){
				logger.debug("Local setup attribute is " + localSetup);
			}

    		// Set local port
	    	int localMsrpPort;
	    	if (localSetup.equals("active")) {
		    	localMsrpPort = 9; // See RFC4145, Page 4
	    	} else {
	    		localMsrpPort = getMsrpMgr().getLocalMsrpPort();
	    	}            
	    	
			// Build SDP part
	    	String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();
	    	String sdp = SdpUtils.buildGroupChatSDP(ipAddress, localMsrpPort, getMsrpMgr().getLocalSocketProtocol(),
                    getAcceptTypes(), getWrappedTypes(), localSetup, getMsrpMgr().getLocalMsrpPath(),
                    SdpUtils.DIRECTION_SENDRECV);

	    	// Set the local SDP part in the dialog path
	        getDialogPath().setLocalContent(sdp);

	        // Test if the session should be interrupted
	        if (isSessionInterrupted() || isInterrupted()) {
				if (logger.isActivated()) {
					logger.debug("Session has been interrupted: end of processing");
				}
				return;
			}
	        
    		// Create the MSRP server session
            if (localSetup.equals("passive")) {
            	// Passive mode: client wait a connection
            	MsrpSession session = getMsrpMgr().createMsrpServerSession(remotePath, this);
    			session.setFailureReportOption(false);
    			session.setSuccessReportOption(false);
    			
    			// Open the connection
    			Thread thread = new Thread(){
    				public void run(){
    					try {
    						// Open the MSRP session
    						getMsrpMgr().openMsrpSession();
    						
    						// Even if local setup is passive, an empty packet must be sent to open the NAT
							// and so enable the active endpoint to initiate a MSRP connection.
    		            	sendEmptyDataChunk();    						
						} catch (IOException e) {
							if (logger.isActivated()) {
				        		logger.error("Can't create the MSRP server session", e);
				        	}
						}		
    				}
    			};
    			thread.start();
            }
            
            // Create a 200 OK response
        	if (logger.isActivated()) {
        		logger.info("Send 200 OK");
        	}
            SipResponse resp = SipMessageFactory.create200OkInviteResponse(getDialogPath(),
            		getFeatureTags(), getAcceptContactTags(), sdp);

            // The signalisation is established
            getDialogPath().sigEstablished();

	        // Send response
            SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(resp);

            // Analyze the received response 
            if (ctx.isSipAck()) {
    	        // ACK received
    			if (logger.isActivated()) {
    				logger.info("ACK request received");
    			}

                // The session is established
    	        getDialogPath().sessionEstablished();

        		// Create the MSRP client session
                if (localSetup.equals("active")) {
                	// Active mode: client should connect
                	MsrpSession session = getMsrpMgr().createMsrpClientSession(remoteHost, remotePort, remotePath, this, fingerprint);
        			session.setFailureReportOption(false);
        			session.setSuccessReportOption(false);
        			
        			// Open the MSRP session
        			getMsrpMgr().openMsrpSession();
        			
        	        // Send an empty packet
                	sendEmptyDataChunk();
                }

            	// Notify listeners
    	    	for(int i=0; i < getListeners().size(); i++) {
    	    		getListeners().get(i).handleSessionStarted();
    	        }

				// Invite missing participant
				inviteMissingParticipants(getParticipants());
				
    	    	// Subscribe to event package
            	getConferenceEventSubscriber().subscribe();

            	// Start session timer
            	if (getSessionTimerManager().isSessionTimerActivated(resp)) {        	
            		getSessionTimerManager().start(SessionTimerManager.UAS_ROLE, getDialogPath().getSessionExpireTime());
            	}
            } else {
        		if (logger.isActivated()) {
            		logger.debug("No ACK received for INVITE");
            	}

        		// No response received: timeout
            	handleError(new ChatError(ChatError.SESSION_INITIATION_FAILED));
            }
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ChatError(ChatError.UNEXPECTED_EXCEPTION, e.getMessage()));
		}		
	}
	
	/**
	 * Invite missing participants.
	 * <p>
	 * This method is executed once upon reception of the first conference event notification after invite.
	 * 
	 * @param invitedParticipants
	 *            the list of invited participants contained in the notify with full state
	 */
	private void inviteMissingParticipants(final ListOfParticipant invitedParticipants) {
		new Thread() {
			public void run() {
				try {
					if (logger.isActivated()) {
						logger.debug("Check if participants are missing in the conference");
					}
					Set<String> connectedProvider = new HashSet<String>(RichMessaging.getInstance()
							.getGroupChatConnectedParticipants(getContributionID()));
					Set<String> invitedSet = new HashSet<String>(invitedParticipants.getList());
					// Only keep participants who are not invited by the AS
					connectedProvider.removeAll(invitedSet);
					if (!connectedProvider.isEmpty()) {
						if (logger.isActivated())
							logger.debug("Add " + connectedProvider.size() + " missing participants in the conference");
						addParticipants(new ArrayList<String>(connectedProvider));
					}
				} catch (Exception e) {
					if (logger.isActivated()) {
						logger.error("Session initiation has failed", e);
					}
				}
			}
		}.start();
	}
}
