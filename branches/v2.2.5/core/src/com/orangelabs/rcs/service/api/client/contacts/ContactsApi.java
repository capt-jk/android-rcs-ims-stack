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

package com.orangelabs.rcs.service.api.client.contacts;

import java.util.List;

import android.content.Context;

import com.orangelabs.rcs.provider.eab.ContactsManager;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.server.ServerApiException;

/**
 * Contacts API
 */
public class ContactsApi {
    /**
     * Constructor
     * 
     * @param ctx Application context
     */
    public ContactsApi(Context ctx) {
    	// Initialize contacts provider
    	ContactsManager.createInstance(ctx);
    }

    /**
     * Get contact info
     * 
     * @param contact Contact
     * @return ContactInfo
     */
    public ContactInfo getContactInfo(String contact) {
    	return ContactsManager.getInstance().getContactInfo(contact);
    }

    /**
     * Get a list of all RCS contacts with social presence
     * 
     * @return list of all RCS contacts
     */
    public List<String> getRcsContactsWithSocialPresence(){
    	return ContactsManager.getInstance().getRcsContactsWithSocialPresence();
    }
    
    /**
     * Get a list of all RCS contacts
     * 
     * @return list of all RCS contacts
     */
    public List<String> getRcsContacts(){
    	return ContactsManager.getInstance().getRcsContacts();
    }
    
    /**
     * Get a list of all RCS blocked contacts
     * 
     * @return list of all RCS contacts
     */
    public List<String> getRcsBlockedContacts(){
    	return ContactsManager.getInstance().getRcsBlockedContacts();
    }
    
    /**
     * Get a list of all RCS invited contacts
     * 
     * @return list of all RCS contacts
     */
    public List<String> getRcsInvitedContacts(){
    	return ContactsManager.getInstance().getRcsInvitedContacts();
    }
    
    /**
     * Get a list of all RCS willing contacts
     * 
     * @return list of all RCS contacts
     */
    public List<String> getRcsWillingContacts(){
    	return ContactsManager.getInstance().getRcsWillingContacts();
    }
    
    /**
     * Get a list of all RCS cancelled contacts
     * 
     * @return list of all RCS contacts
     */
    public List<String> getRcsCancelledContacts(){
    	return ContactsManager.getInstance().getRcsCancelledContacts();
    }
    
    /**
     * Get the IM-blocked status of a contact
     * 
     * @param contact
     */
    public boolean isContactImBlocked(String contact){
    	return ContactsManager.getInstance().isImBlockedForContact(contact);
    }
    
	/**
	 * Is the number in the RCS blocked list
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberBlocked(String number) {
		return ContactsManager.getInstance().isNumberBlocked(number);
	}
	
	/**
	 * Is the number in the RCS buddy list
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberShared(String number) {
		return ContactsManager.getInstance().isNumberShared(number);
	}

	/**
	 * Has the number been invited to RCS
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberInvited(String number) {
		return ContactsManager.getInstance().isNumberInvited(number);
	}

	/**
	 * Has the number invited us to RCS
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberWilling(String number) {
		return ContactsManager.getInstance().isNumberWilling(number);
	}
	
	/**
	 * Has the number invited us to RCS then be cancelled
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberCancelled(String number) {
		return ContactsManager.getInstance().isNumberCancelled(number);
	}
    
    /**
     * Set the IM-blocked status of a contact
     * 
     * @param contact
     * @param status of the IM-blocked
     */
    public void setImBlockedForContact(String contact, boolean status){
    	ContactsManager.getInstance().setImBlockedForContact(contact, status);
    }
    
	/**
	 * Get list of blocked contacts for IM sessions
	 *  
	 * @return List of contacts
	 * @throws ClientApiException
	 */
	public List<String> getBlockedContactsForIm(){
		return ContactsManager.getInstance().getIMBlockedContacts();
	}
	
	/**
	 * Get list of contacts that can use IM sessions
	 *  
	 * @return List of contacts
	 * @throws ServerApiException
	 */
	public List<String> getImSessionCapableContacts(){
		return ContactsManager.getInstance().getImSessionCapableContacts();
	}
	
	/**
	 * Get list of contacts that can do use rich call features
	 *  
	 * @return List of contacts
	 * @throws ServerApiException
	 */
	public List<String> getRichcallCapableContacts(){
		return ContactsManager.getInstance().getRichcallCapableContacts();
	}
	
	/**
	 * Set the weblink visited flag to true for given contact
	 * 
	 * @param contact
	 */
	public void setWeblinkVisitedForContact(String contact){
		ContactsManager.getInstance().setWeblinkUpdatedFlag(contact, false);
	}
	
	/**
	 * Get the weblink visited flag
	 * 
	 * @param contact
	 * @return true if the weblink has been updated since last visit
	 */
	public boolean hasWeblinkBeenUpdatedForContact(String contact){
		return ContactsManager.getInstance().getWeblinkUpdatedFlag(contact);
	}
	
	/**
	 * Remove a cancelled presence invitation
	 * 
	 * @param contact
	 */
	public void removeCancelledPresenceInvitation(String contact){
		ContactsManager.getInstance().removeCancelledPresenceInvitation(contact);
	}	
}