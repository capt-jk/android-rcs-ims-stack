/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
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

package com.orangelabs.rcs.service.api.server.sip;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.os.IBinder;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.sip.GenericSipSession;
import com.orangelabs.rcs.service.api.client.sip.ISipApi;
import com.orangelabs.rcs.service.api.client.sip.ISipSession;
import com.orangelabs.rcs.service.api.server.ServerApiException;
import com.orangelabs.rcs.service.api.server.ServerApiUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * SIP API service
 * 
 * @author jexa7410
 */
public class SipApiService extends ISipApi.Stub {
    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public SipApiService() {
		if (logger.isActivated()) {
			logger.info("SIP API is loaded");
		}
	}

	/**
	 * Close API
	 */
	public void close() {
	}
	
	/**
	 * Initiate a SIP session
	 * 
	 * @param contact Contact
	 * @param featureTag Feature tag of the service
	 * @param offer SDP offer
	 * @throws ServerApiException
	 */
	public ISipSession initiateSession(String contact, String featureTag, String offer) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate a SIP session with " + contact);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			// Initiate a new session
			GenericSipSession session = Core.getInstance().getSipService().initiateSession(contact, featureTag, offer);
			
			return new SipSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Get a SIP session from its session ID
	 *
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public ISipSession getSession(String id) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get SIP session " + id);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getSipService().getSession(id);
			if ((session != null) && (session instanceof GenericSipSession)) {
				return new SipSession((GenericSipSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get list of SIP sessions with a contact
	 * 
	 * @param contact Contact
	 * @return List of sessions
	 * @throws ServerApiException
	 */
	public List<IBinder> getSessionsWith(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get SIP sessions with " + contact);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			Vector<GenericSipSession> list = Core.getInstance().getSipService().getSipSessionsWith(contact);
			ArrayList<IBinder> result = new ArrayList<IBinder>(list.size());
			for(int i=0; i < list.size(); i++) {
				GenericSipSession session = list.elementAt(i);
				SipSession apiSession = new SipSession(session);
				result.add(apiSession.asBinder());
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}		
	}

	/**
	 * Get list of current established SIP sessions
	 * 
	 * @return List of sessions
	 * @throws ServerApiException
	 */
	public List<IBinder> getSessions() throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get SIP sessions");
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			Vector<GenericSipSession> list = Core.getInstance().getSipService().getSipSessions();
			ArrayList<IBinder> result = new ArrayList<IBinder>(list.size());
			for(int i=0; i < list.size(); i++) {
				GenericSipSession session = list.elementAt(i);
				SipDialogPath dialog = session.getDialogPath();
				if ((dialog != null) && (dialog.isSigEstablished())) {
					// Returns only sessions which are established
					SipSession apiSession = new SipSession(session);
					result.add(apiSession.asBinder());
				}
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
}
