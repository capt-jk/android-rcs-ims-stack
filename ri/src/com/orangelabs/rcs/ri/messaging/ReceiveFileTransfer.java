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

package com.orangelabs.rcs.ri.messaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.ImsEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Received file transfer
 * 
 * @author jexa7410
 */
public class ReceiveFileTransfer extends Activity implements ClientApiListener, ImsEventListener {
    /**
     * UI handler
     */
    private final Handler handler = new Handler();
    
    /**
	 * Messaging API
	 */
	private MessagingApi messagingApi;
    
	/**
	 * Session ID
	 */
    private String sessionId;
    
    /**
     * Remote Contact
     */
    private String remoteContact;
   
    /**
     * File size
     */
    private long fileSize;
    
    /**
     * File transfer session
     */
    private IFileTransferSession transferSession = null;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get invitation info
        sessionId = getIntent().getStringExtra("sessionId");
		remoteContact = getIntent().getStringExtra("contact");
		fileSize = getIntent().getLongExtra("size", -1);
		
        // Remove the notification
        ReceiveFileTransfer.removeFileTransferNotification(this, sessionId);
        
        // Instanciate messaging API
		messagingApi = new MessagingApi(getApplicationContext());
		messagingApi.addApiEventListener(this);
        messagingApi.addImsEventListener(this);
		messagingApi.connectApi();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		messagingApi.removeApiEventListener(this);
		messagingApi.disconnectApi();
	}
	
	/**
     * API disabled
     */
    public void handleApiDisabled() {
		handler.post(new Runnable() { 
			public void run() {
				Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_api_disabled));
			}
		});
    }
    
    /**
     * IMS connected
     */
	public void handleImsConnected() {
	}

    /**
     * IMS disconnected
     */
	public void handleImsDisconnected() {
    	// IMS has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_ims_disconnected));
			}
		});
	}     
    
    /**
     * API connected
     */
    public void handleApiConnected() {
		try {
			// Get the file transfer session
    		transferSession = messagingApi.getFileTransferSession(sessionId);
			if (transferSession == null) {
				// Session not found or expired
				Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_session_has_expired));
				return;
			}
			transferSession.addSessionListener(fileTransferSessionListener);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_recv_file_transfer);
			
			String size;
	    	if (fileSize != -1) {
	    		size = getString(R.string.label_file_size, " "+ (fileSize/1024), " Kb");
	    	} else {
	    		size = getString(R.string.label_file_size_unknown);
	    	}
			
			builder.setMessage(getString(R.string.label_from) +	remoteContact +	"\n" + size);
			builder.setCancelable(false);
			builder.setIcon(R.drawable.ri_notif_file_transfer_icon);
			builder.setPositiveButton(getString(R.string.label_accept), acceptBtnListener);
			builder.setNegativeButton(getString(R.string.label_decline), declineBtnListener);
			builder.show();			
		} catch(Exception e) {
			Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_api_failed));
		}
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
    	// Service has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_api_disconnected));
			}
		});
	}
	
    /**
     * Accept button listener
     */
    private OnClickListener acceptBtnListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        	// Set layout
            setContentView(R.layout.messaging_receive_filetransfer);
            
            // Set title
            setTitle(R.string.title_recv_file_transfer);
            
    		TextView from = (TextView)findViewById(R.id.from);
	        from.setText(getString(R.string.label_from) + " " + remoteContact);
	        
	    	TextView size = (TextView)findViewById(R.id.image_size);
	    	if (fileSize != -1) {
	    		size.setText(getString(R.string.label_file_size, " "+ (fileSize/1024), " Kb"));
	    	} else {
	    		size.setText(getString(R.string.label_file_size_unknown));
	    	}
 
	    	Thread thread = new Thread() {
            	public void run() {
                	try {
                		// Accept the invitation
            			transferSession.acceptSession();
	            	} catch(Exception e) {
	        			handler.post(new Runnable() { 
	        				public void run() {
	        					Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_invitation_failed));
							}
		    			});
	            	}
            	}
            };
            thread.start();
        }
    };

    /**
     * Reject button listener
     */    
    private OnClickListener declineBtnListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Thread thread = new Thread() {
            	public void run() {
                	try {
                		// Reject the invitation
                		transferSession.removeSessionListener(fileTransferSessionListener);
            			transferSession.rejectSession();
	            	} catch(Exception e) {
	            	}
            	}
            };
            thread.start();

            // Exit activity
			finish();
        }
    };
    
    /**
     * File transfer session event listener
     */
    private IFileTransferEventListener fileTransferSessionListener = new IFileTransferEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("started");
				}
			});
		}
	
		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_sharing_aborted));
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
	
		// File transfer progress
		public void handleTransferProgress(final long currentSize, final long totalSize) {
			handler.post(new Runnable() { 
    			public void run() {
    				updateProgressBar(currentSize, totalSize);
    			}
    		});
		}
		
		// File transfer error
		public void handleTransferError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showMessageAndExit(ReceiveFileTransfer.this, getString(R.string.label_transfer_failed, error));
				}
			});
		}
		
		// File has been transfered
		public void handleFileTransfered(final String filename) {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("transfered");
					
					// Make sure progress bar is at the end
			        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
			        progressBar.setProgress(progressBar.getMax());
			        
			        // Show the transfered image
			        Utils.showPictureAndExit(ReceiveFileTransfer.this, filename);	
				}
			});
		}
    };    

    /**
     * Show the transfer progress
     * 
     * @param currentSize Current size transfered
     * @param totalSize Total size to be transfered
     */
    private void updateProgressBar(long currentSize, long totalSize) {
    	TextView statusView = (TextView)findViewById(R.id.progress_status);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
    	
		String value = "" + (currentSize/1024);
		if (totalSize != 0) {
			value += "/" + (totalSize/1024);
		}
		value += " Kb";
		statusView.setText(value);
	    
	    if (currentSize != 0) {
	    	double position = ((double)currentSize / (double)totalSize)*100.0;
	    	progressBar.setProgress((int)position);
	    } else {
	    	progressBar.setProgress(0);
	    }
    }
    
    
    /**
     * Add file transfer notification
     * 
     * @param context Context
     * @param contact Contact
     * @param sessionId Session ID
     * @param size File size
     */
    public static void addFileTransferInvitationNotification(Context context, String contact, String sessionId, long size) {
    	// Create notification
		Intent intent = new Intent(context, ReceiveFileTransfer.class);
		intent.putExtra("contact", contact);
		intent.putExtra("sessionId", sessionId);
		intent.putExtra("size", size);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notif = new Notification(R.drawable.ri_notif_file_transfer_icon,
        		context.getString(R.string.title_recv_file_transfer),
        		System.currentTimeMillis());
        notif.flags = Notification.FLAG_NO_CLEAR;
        notif.setLatestEventInfo(context,
        		context.getString(R.string.title_recv_file_transfer),
        		context.getString(R.string.label_from) + " " + contact,
        		contentIntent);
        
        // Set ringtone
        String ringtone = RcsSettings.getInstance().getFileTransferInvitationRingtone();
        if (!TextUtils.isEmpty(ringtone)) {
			notif.sound = Uri.parse(ringtone);
        }
        
        // Set vibration
        if (RcsSettings.getInstance().isPhoneVibrateForFileTransferInvitation()) {
        	notif.defaults |= Notification.DEFAULT_VIBRATE;
        }
        
        // Send notification
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)Long.parseLong(sessionId), notif);
    }
    
	/**
     * Remove file transfer notification
     * 
     * @param context Context
     * @param sessionId Session ID
     */
    public static void removeFileTransferNotification(Context context, String sessionId) {
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel((int)Long.parseLong(sessionId));
    }

    /**
     * Quit the session
     */
    private void quitSession() {
		// Stop session
        Thread thread = new Thread() {
        	public void run() {
            	try {
                    if (transferSession != null) {
                    	try {
                    		transferSession.removeSessionListener(fileTransferSessionListener);
                    		transferSession.cancelSession();
                    	} catch(Exception e) {
                    	}
                    	transferSession = null;
                    }
            	} catch(Exception e) {
            	}
        	}
        };
        thread.start();
    	
        // Exit activity
		finish();
    }      
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // Quit the session
            	quitSession();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.menu_ft, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_close_session:
				// Quit the session
				quitSession();
				break;
		}
		return true;
	}            
}
