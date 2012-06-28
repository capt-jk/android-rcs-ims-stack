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

package com.orangelabs.rcs.ri;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.eventlog.EventLog;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiIntents;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * RCS RI application
 * 
 * @author jexa7410
 */
public class RcsRI extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		// Set application context
		AndroidFactory.setApplicationContext(getApplicationContext());

		// Set title
        setTitle(getString(R.string.app_name));

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       
        // Instanciate the settings manager
        RcsSettings.createInstance(getApplicationContext());		
		
        // Initialize the country code
		PhoneUtils.initialize(getApplicationContext());
		
		// Set items
        String[] items = {
    		getString(R.string.menu_address_book),
    		getString(R.string.menu_capabilities),
    		getString(R.string.menu_presence),
    		getString(R.string.menu_messaging),
    		getString(R.string.menu_richcall),
    		getString(R.string.menu_eventlog),
    		getString(R.string.menu_settings),
    		getString(R.string.menu_about)
        };
    	setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	switch(position) {
        	case 0:
        		try {
        			startActivity(new Intent(Intent.ACTION_VIEW).setType(ContactsContract.Contacts.CONTENT_TYPE));
        		} catch(ActivityNotFoundException e1) {
        			try {
        				startActivity(new Intent("com.android.contacts.action.LIST_DEFAULT"));
        			} catch(ActivityNotFoundException e2) {
        				Utils.showMessage(this, getString(R.string.label_ab_not_found));
        			}
        		}
        		break;
        		
        	case 1:
        		startActivity(new Intent(this, CapabilitiesRI.class));
        		break;
        		
        	case 2:
                if (RcsSettings.getInstance().isSocialPresenceSupported()){
            		startActivity(new Intent(this, PresenceRI.class));
                } else {
    				Utils.showMessage(this, getString(R.string.label_presence_service_not_activated));
                }
        		break;
        		
        	case 3:
        		startActivity(new Intent(this, MessagingRI.class));
        		break;
        		
        	case 4:
        		startActivity(new Intent(this, RichCallRI.class));
        		break;
        		
        	case 5:
        		startActivity(new Intent(this, EventLog.class));
        		break;
        		
        	case 6:
        		startActivity(new Intent(ClientApiIntents.RCS_SETTINGS));
        		break;
        		
        	case 7:
        		startActivity(new Intent(this, AboutRI.class));
        		break;
    	}
    }
}
