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

package com.orangelabs.rcs.ri.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * File transfer invitation receiver
 * 
 * @author jexa7410
 */
public class FileTransferInvitationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean autoAccept = intent.getBooleanExtra("autoAccept", false);
		if (autoAccept) {
			// Display progress
			Intent progress = new Intent(intent);
			progress.setClass(context, ReceiveFileTransfer.class);
			progress.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(progress);
		} else {
			// Display invitation notification
			ReceiveFileTransfer.addFileTransferInvitationNotification(context, intent);
		}
    }
}
