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

package com.orangelabs.rcs.provisioning.local;

import static com.orangelabs.rcs.provisioning.local.Provisioning.saveCheckBoxParameter;
import static com.orangelabs.rcs.provisioning.local.Provisioning.saveEditTextParameter;
import static com.orangelabs.rcs.provisioning.local.Provisioning.setCheckBoxParameter;
import static com.orangelabs.rcs.provisioning.local.Provisioning.setEditTextParameter;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Stack parameters provisioning File
 * 
 * @author jexa7410
 */
public class StackProvisioning extends Activity {
	/**
	 * Folder path for certificate
	 */
	public static final String CERTIFICATE_FOLDER_PATH = Environment.getExternalStorageDirectory().getPath();

	/**
	 * Auto config mode
	 */
	private static final String[] AUTO_CONFIG = { "None", "HTTPS" };

	/**
	 * SIP protocol
	 */
	private static final String[] SIP_PROTOCOL = { "UDP", "TCP", "TLS" };

	/**
	 * Network access
	 */
	private static final String[] NETWORK_ACCESS = { "All networks", "Mobile only", "Wi-Fi only" };

	/**
	 * FT protocol
	 */
	private static final String[] FT_PROTOCOL = { RcsSettingsData.FT_PROTOCOL_HTTP, RcsSettingsData.FT_PROTOCOL_MSRP };

	private boolean isInFront;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		// Set layout
		setContentView(R.layout.rcs_provisioning_stack);
		// Set buttons callback
		Button btn = (Button) findViewById(R.id.save_btn);
		btn.setOnClickListener(saveBtnListener);
		updateView(bundle);
		isInFront = true;
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		saveInstanceState(bundle);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isInFront == false) {
			isInFront = true;
			// Update UI (from DB)
			updateView(null);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isInFront = false;
	}

	/**
	 * Save parameters either in bundle or in RCS settings
	 */
	private void saveInstanceState(Bundle bundle) {
		Spinner spinner = (Spinner) findViewById(R.id.Autoconfig);
		if (bundle != null) {
			bundle.putInt(RcsSettingsData.AUTO_CONFIG_MODE, spinner.getSelectedItemPosition());
		} else {
			RcsSettings.getInstance().writeParameter(RcsSettingsData.AUTO_CONFIG_MODE, "" + spinner.getSelectedItemPosition());
		}

		spinner = (Spinner) findViewById(R.id.SipDefaultProtocolForMobile);
		if (bundle != null) {
			bundle.putString(RcsSettingsData.SIP_DEFAULT_PROTOCOL_FOR_MOBILE, (String) spinner.getSelectedItem());
		} else {
			RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_DEFAULT_PROTOCOL_FOR_MOBILE,
					(String) spinner.getSelectedItem());
		}

		spinner = (Spinner) findViewById(R.id.SipDefaultProtocolForWifi);
		if (bundle != null) {
			bundle.putString(RcsSettingsData.SIP_DEFAULT_PROTOCOL_FOR_WIFI, (String) spinner.getSelectedItem());
		} else {
			RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_DEFAULT_PROTOCOL_FOR_WIFI,
					(String) spinner.getSelectedItem());
		}

		saveCheckBoxParameter(this, R.id.TcpFallback, RcsSettingsData.KEY_TCP_FALLBACK, bundle);

		spinner = (Spinner) findViewById(R.id.TlsCertificateRoot);
		if (spinner.getSelectedItemPosition() == 0) {
			if (bundle != null) {
				bundle.putString(RcsSettingsData.TLS_CERTIFICATE_ROOT, "");
			} else {
				RcsSettings.getInstance().writeParameter(RcsSettingsData.TLS_CERTIFICATE_ROOT, "");
			}
		} else {
			String path = CERTIFICATE_FOLDER_PATH + File.separator + (String) spinner.getSelectedItem();
			if (bundle != null) {
				bundle.putString(RcsSettingsData.TLS_CERTIFICATE_ROOT, path);
			} else {
				RcsSettings.getInstance().writeParameter(RcsSettingsData.TLS_CERTIFICATE_ROOT, path);
			}
		}

		spinner = (Spinner) findViewById(R.id.TlsCertificateIntermediate);
		if (spinner.getSelectedItemPosition() == 0) {
			if (bundle != null) {
				bundle.putString(RcsSettingsData.TLS_CERTIFICATE_INTERMEDIATE, "");
			} else {
				RcsSettings.getInstance().writeParameter(RcsSettingsData.TLS_CERTIFICATE_INTERMEDIATE, "");
			}
		} else {
			String path = CERTIFICATE_FOLDER_PATH + File.separator + (String) spinner.getSelectedItem();
			if (bundle != null) {
				bundle.putString(RcsSettingsData.TLS_CERTIFICATE_INTERMEDIATE, path);
			} else {
				RcsSettings.getInstance().writeParameter(RcsSettingsData.TLS_CERTIFICATE_INTERMEDIATE, path);
			}
		}

		spinner = (Spinner) findViewById(R.id.NetworkAccess);
		int index = spinner.getSelectedItemPosition();
		switch (index) {
		case 0:
			if (bundle != null) {
				bundle.putInt(RcsSettingsData.NETWORK_ACCESS, RcsSettingsData.ANY_ACCESS);
			} else {
				RcsSettings.getInstance().writeParameter(RcsSettingsData.NETWORK_ACCESS, "" + RcsSettingsData.ANY_ACCESS);
			}
			break;
		case 1:
			if (bundle != null) {
				bundle.putInt(RcsSettingsData.NETWORK_ACCESS, RcsSettingsData.MOBILE_ACCESS);
			} else {
				RcsSettings.getInstance().writeParameter(RcsSettingsData.NETWORK_ACCESS, "" + RcsSettingsData.MOBILE_ACCESS);
			}
			break;
		case 2:
			if (bundle != null) {
				bundle.putInt(RcsSettingsData.NETWORK_ACCESS, RcsSettingsData.WIFI_ACCESS);
			} else {
				RcsSettings.getInstance().writeParameter(RcsSettingsData.NETWORK_ACCESS, "" + RcsSettingsData.WIFI_ACCESS);
			}
			break;
		}

		spinner = (Spinner) findViewById(R.id.FtProtocol);
		if (bundle != null) {
			bundle.putString(RcsSettingsData.FT_PROTOCOL, (String) spinner.getSelectedItem());
		} else {
			RcsSettings.getInstance().writeParameter(RcsSettingsData.FT_PROTOCOL, (String) spinner.getSelectedItem());
		}

		saveEditTextParameter(this, R.id.SecondaryProvisioningAddress, RcsSettingsData.SECONDARY_PROVISIONING_ADDRESS, bundle);
		saveCheckBoxParameter(this, R.id.SecondaryProvisioningAddressOnly, RcsSettingsData.SECONDARY_PROVISIONING_ADDRESS_ONLY,
				bundle);
		saveEditTextParameter(this, R.id.SipListeningPort, RcsSettingsData.SIP_DEFAULT_PORT, bundle);
		saveEditTextParameter(this, R.id.SipTimerT1, RcsSettingsData.SIP_TIMER_T1, bundle);
		saveEditTextParameter(this, R.id.SipTimerT2, RcsSettingsData.SIP_TIMER_T2, bundle);
		saveEditTextParameter(this, R.id.SipTimerT4, RcsSettingsData.SIP_TIMER_T4, bundle);
		saveEditTextParameter(this, R.id.SipTransactionTimeout, RcsSettingsData.SIP_TRANSACTION_TIMEOUT, bundle);
		saveEditTextParameter(this, R.id.SipKeepAlivePeriod, RcsSettingsData.SIP_KEEP_ALIVE_PERIOD, bundle);
		saveEditTextParameter(this, R.id.DefaultMsrpPort, RcsSettingsData.MSRP_DEFAULT_PORT, bundle);
		saveEditTextParameter(this, R.id.DefaultRtpPort, RcsSettingsData.RTP_DEFAULT_PORT, bundle);
		saveEditTextParameter(this, R.id.MsrpTransactionTimeout, RcsSettingsData.MSRP_TRANSACTION_TIMEOUT, bundle);
		saveEditTextParameter(this, R.id.RegisterExpirePeriod, RcsSettingsData.REGISTER_EXPIRE_PERIOD, bundle);
		saveEditTextParameter(this, R.id.RegisterRetryBaseTime, RcsSettingsData.REGISTER_RETRY_BASE_TIME, bundle);
		saveEditTextParameter(this, R.id.RegisterRetryMaxTime, RcsSettingsData.REGISTER_RETRY_MAX_TIME, bundle);
		saveEditTextParameter(this, R.id.PublishExpirePeriod, RcsSettingsData.PUBLISH_EXPIRE_PERIOD, bundle);
		saveEditTextParameter(this, R.id.RevokeTimeout, RcsSettingsData.REVOKE_TIMEOUT, bundle);
		saveEditTextParameter(this, R.id.RingingPeriod, RcsSettingsData.RINGING_SESSION_PERIOD, bundle);
		saveEditTextParameter(this, R.id.SubscribeExpirePeriod, RcsSettingsData.SUBSCRIBE_EXPIRE_PERIOD, bundle);
		saveEditTextParameter(this, R.id.IsComposingTimeout, RcsSettingsData.IS_COMPOSING_TIMEOUT, bundle);
		saveEditTextParameter(this, R.id.SessionRefreshExpirePeriod, RcsSettingsData.SESSION_REFRESH_EXPIRE_PERIOD, bundle);
		saveEditTextParameter(this, R.id.CapabilityRefreshTimeout, RcsSettingsData.CAPABILITY_REFRESH_TIMEOUT, bundle);
		saveEditTextParameter(this, R.id.CapabilityExpiryTimeout, RcsSettingsData.CAPABILITY_EXPIRY_TIMEOUT, bundle);
		saveEditTextParameter(this, R.id.CapabilityPollingPeriod, RcsSettingsData.CAPABILITY_POLLING_PERIOD, bundle);
		saveCheckBoxParameter(this, R.id.SipKeepAlive, RcsSettingsData.SIP_KEEP_ALIVE, bundle);
		saveCheckBoxParameter(this, R.id.PermanentState, RcsSettingsData.PERMANENT_STATE_MODE, bundle);
		saveCheckBoxParameter(this, R.id.TelUriFormat, RcsSettingsData.TEL_URI_FORMAT, bundle);
		saveCheckBoxParameter(this, R.id.ImAlwaysOn, RcsSettingsData.IM_CAPABILITY_ALWAYS_ON, bundle);
		saveCheckBoxParameter(this, R.id.FtAlwaysOn, RcsSettingsData.FT_CAPABILITY_ALWAYS_ON, bundle);
		saveCheckBoxParameter(this, R.id.ImUseReports, RcsSettingsData.IM_USE_REPORTS, bundle);
		saveCheckBoxParameter(this, R.id.Gruu, RcsSettingsData.GRUU, bundle);
		saveCheckBoxParameter(this, R.id.CpuAlwaysOn, RcsSettingsData.CPU_ALWAYS_ON, bundle);
		saveCheckBoxParameter(this, R.id.SecureMsrpOverWifi, RcsSettingsData.SECURE_MSRP_OVER_WIFI, bundle);
		saveCheckBoxParameter(this, R.id.SecureRtpOverWifi, RcsSettingsData.SECURE_RTP_OVER_WIFI, bundle);
		saveCheckBoxParameter(this, R.id.ImeiAsDeviceId, RcsSettingsData.USE_IMEI_AS_DEVICE_ID, bundle);
	}

	/**
	 * Update UI (upon creation, rotation, tab switch...)
	 * 
	 * @param bundle
	 */
	private void updateView(Bundle bundle) {
		// Display stack parameters
		Spinner spinner = (Spinner) findViewById(R.id.Autoconfig);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, AUTO_CONFIG);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		int mode = 0;
		if (bundle != null && bundle.containsKey(RcsSettingsData.AUTO_CONFIG_MODE)) {
			mode = bundle.getInt(RcsSettingsData.AUTO_CONFIG_MODE);
		} else {
			mode = RcsSettings.getInstance().getAutoConfigMode();
		}
		spinner.setSelection((mode == RcsSettingsData.HTTPS_AUTO_CONFIG) ? 1 : 0);

		setEditTextParameter(this, R.id.SecondaryProvisioningAddress, RcsSettingsData.SECONDARY_PROVISIONING_ADDRESS, bundle);
		setCheckBoxParameter(this, R.id.SecondaryProvisioningAddressOnly, RcsSettingsData.SECONDARY_PROVISIONING_ADDRESS_ONLY,
				bundle);

		spinner = (Spinner) findViewById(R.id.NetworkAccess);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, NETWORK_ACCESS);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		int access = 0;
		if (bundle != null && bundle.containsKey(RcsSettingsData.NETWORK_ACCESS)) {
			access = bundle.getInt(RcsSettingsData.NETWORK_ACCESS);
		} else {
			access = RcsSettings.getInstance().getNetworkAccess();
		}
		if (access == RcsSettingsData.WIFI_ACCESS) {
			spinner.setSelection(2);
		} else if (access == RcsSettingsData.MOBILE_ACCESS) {
			spinner.setSelection(1);
		} else {
			spinner.setSelection(0);
		}

		spinner = (Spinner) findViewById(R.id.SipDefaultProtocolForMobile);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SIP_PROTOCOL);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		String sipMobile = null;
		if (bundle != null && bundle.containsKey(RcsSettingsData.SIP_DEFAULT_PROTOCOL_FOR_MOBILE)) {
			sipMobile = bundle.getString(RcsSettingsData.SIP_DEFAULT_PROTOCOL_FOR_MOBILE);
		} else {
			sipMobile = RcsSettings.getInstance().getSipDefaultProtocolForMobile();
		}
		if (sipMobile.equalsIgnoreCase(SIP_PROTOCOL[0])) {
			spinner.setSelection(0);
		} else if (sipMobile.equalsIgnoreCase(SIP_PROTOCOL[1])) {
			spinner.setSelection(1);
		} else {
			spinner.setSelection(2);
		}

		spinner = (Spinner) findViewById(R.id.SipDefaultProtocolForWifi);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SIP_PROTOCOL);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		String sipWifi = null;
		if (bundle != null && bundle.containsKey(RcsSettingsData.SIP_DEFAULT_PROTOCOL_FOR_WIFI)) {
			sipWifi = bundle.getString(RcsSettingsData.SIP_DEFAULT_PROTOCOL_FOR_WIFI);
		} else {
			sipWifi = RcsSettings.getInstance().getSipDefaultProtocolForWifi();
		}
		if (sipWifi.equalsIgnoreCase(SIP_PROTOCOL[0])) {
			spinner.setSelection(0);
		} else if (sipWifi.equalsIgnoreCase(SIP_PROTOCOL[1])) {
			spinner.setSelection(1);
		} else {
			spinner.setSelection(2);
		}

		setCheckBoxParameter(this, R.id.TcpFallback, RcsSettingsData.KEY_TCP_FALLBACK, bundle);

		String[] certificates = loadCertificatesList();
		spinner = (Spinner) findViewById(R.id.TlsCertificateRoot);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, certificates);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		boolean found = false;
		String certRoot = null;
		if (bundle != null && bundle.containsKey(RcsSettingsData.TLS_CERTIFICATE_ROOT)) {
			certRoot = bundle.getString(RcsSettingsData.TLS_CERTIFICATE_ROOT);
		} else {
			certRoot = RcsSettings.getInstance().getTlsCertificateRoot();
		}
		for (int i = 0; i < certificates.length; i++) {
			if (certRoot.contains(certificates[i])) {
				spinner.setSelection(i);
				found = true;
			}
		}
		if (!found) {
			spinner.setSelection(0);
			RcsSettings.getInstance().writeParameter(RcsSettingsData.TLS_CERTIFICATE_ROOT, "");
		}

		spinner = (Spinner) findViewById(R.id.TlsCertificateIntermediate);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, certificates);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(0);
		found = false;
		String certInt = null;
		if (bundle != null && bundle.containsKey(RcsSettingsData.TLS_CERTIFICATE_INTERMEDIATE)) {
			certInt = bundle.getString(RcsSettingsData.TLS_CERTIFICATE_INTERMEDIATE);
		} else {
			certInt = RcsSettings.getInstance().getTlsCertificateIntermediate();
		}
		for (int i = 0; i < certificates.length; i++) {
			if (certInt.contains(certificates[i])) {
				spinner.setSelection(i);
				found = true;
			}
		}
		if (!found) {
			spinner.setSelection(0);
			RcsSettings.getInstance().writeParameter(RcsSettingsData.TLS_CERTIFICATE_INTERMEDIATE, "");
		}

		spinner = (Spinner) findViewById(R.id.FtProtocol);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, FT_PROTOCOL);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		String ftProtocol = null;
		if (bundle != null && bundle.containsKey(RcsSettingsData.FT_PROTOCOL)) {
			ftProtocol = bundle.getString(RcsSettingsData.FT_PROTOCOL);
		} else {
			ftProtocol = RcsSettings.getInstance().getFtProtocol();
		}
		if ((ftProtocol != null) && ftProtocol.equalsIgnoreCase(FT_PROTOCOL[0])) {
			spinner.setSelection(0);
		} else {
			spinner.setSelection(1);
		}

		setEditTextParameter(this, R.id.ImsServicePollingPeriod, RcsSettingsData.IMS_SERVICE_POLLING_PERIOD, bundle);
		setEditTextParameter(this, R.id.SipListeningPort, RcsSettingsData.SIP_DEFAULT_PORT, bundle);
		setEditTextParameter(this, R.id.SipTimerT1, RcsSettingsData.SIP_TIMER_T1, bundle);
		setEditTextParameter(this, R.id.SipTimerT2, RcsSettingsData.SIP_TIMER_T2, bundle);
		setEditTextParameter(this, R.id.SipTimerT4, RcsSettingsData.SIP_TIMER_T4, bundle);
		setEditTextParameter(this, R.id.SipTransactionTimeout, RcsSettingsData.SIP_TRANSACTION_TIMEOUT, bundle);
		setEditTextParameter(this, R.id.SipKeepAlivePeriod, RcsSettingsData.SIP_KEEP_ALIVE_PERIOD, bundle);
		setEditTextParameter(this, R.id.DefaultMsrpPort, RcsSettingsData.MSRP_DEFAULT_PORT, bundle);
		setEditTextParameter(this, R.id.DefaultRtpPort, RcsSettingsData.RTP_DEFAULT_PORT, bundle);
		setEditTextParameter(this, R.id.MsrpTransactionTimeout, RcsSettingsData.MSRP_TRANSACTION_TIMEOUT, bundle);
		setEditTextParameter(this, R.id.RegisterExpirePeriod, RcsSettingsData.REGISTER_EXPIRE_PERIOD, bundle);
		setEditTextParameter(this, R.id.RegisterRetryBaseTime, RcsSettingsData.REGISTER_RETRY_BASE_TIME, bundle);
		setEditTextParameter(this, R.id.RegisterRetryMaxTime, RcsSettingsData.REGISTER_RETRY_MAX_TIME, bundle);
		setEditTextParameter(this, R.id.PublishExpirePeriod, RcsSettingsData.PUBLISH_EXPIRE_PERIOD, bundle);
		setEditTextParameter(this, R.id.RevokeTimeout, RcsSettingsData.REVOKE_TIMEOUT, bundle);
		setEditTextParameter(this, R.id.RingingPeriod, RcsSettingsData.RINGING_SESSION_PERIOD, bundle);
		setEditTextParameter(this, R.id.SubscribeExpirePeriod, RcsSettingsData.SUBSCRIBE_EXPIRE_PERIOD, bundle);
		setEditTextParameter(this, R.id.IsComposingTimeout, RcsSettingsData.IS_COMPOSING_TIMEOUT, bundle);
		setEditTextParameter(this, R.id.SessionRefreshExpirePeriod, RcsSettingsData.SESSION_REFRESH_EXPIRE_PERIOD, bundle);
		setEditTextParameter(this, R.id.CapabilityRefreshTimeout, RcsSettingsData.CAPABILITY_REFRESH_TIMEOUT, bundle);
		setEditTextParameter(this, R.id.CapabilityExpiryTimeout, RcsSettingsData.CAPABILITY_EXPIRY_TIMEOUT, bundle);
		setEditTextParameter(this, R.id.CapabilityPollingPeriod, RcsSettingsData.CAPABILITY_POLLING_PERIOD, bundle);
		setCheckBoxParameter(this, R.id.SipKeepAlive, RcsSettingsData.SIP_KEEP_ALIVE, bundle);
		setCheckBoxParameter(this, R.id.PermanentState, RcsSettingsData.PERMANENT_STATE_MODE, bundle);
		setCheckBoxParameter(this, R.id.TelUriFormat, RcsSettingsData.TEL_URI_FORMAT, bundle);
		setCheckBoxParameter(this, R.id.ImAlwaysOn, RcsSettingsData.IM_CAPABILITY_ALWAYS_ON, bundle);
		setCheckBoxParameter(this, R.id.FtAlwaysOn, RcsSettingsData.FT_CAPABILITY_ALWAYS_ON, bundle);
		setCheckBoxParameter(this, R.id.ImUseReports, RcsSettingsData.IM_USE_REPORTS, bundle);
		setCheckBoxParameter(this, R.id.Gruu, RcsSettingsData.GRUU, bundle);
		setCheckBoxParameter(this, R.id.CpuAlwaysOn, RcsSettingsData.CPU_ALWAYS_ON, bundle);
		setCheckBoxParameter(this, R.id.SecureMsrpOverWifi, RcsSettingsData.SECURE_MSRP_OVER_WIFI, bundle);
		setCheckBoxParameter(this, R.id.SecureRtpOverWifi, RcsSettingsData.SECURE_RTP_OVER_WIFI, bundle);
		setCheckBoxParameter(this, R.id.ImeiAsDeviceId, RcsSettingsData.USE_IMEI_AS_DEVICE_ID, bundle);
	}

	/**
	 * Save button listener
	 */
	private OnClickListener saveBtnListener = new OnClickListener() {
		public void onClick(View v) {
			// Save parameters
			saveInstanceState(null);
			Toast.makeText(StackProvisioning.this, getString(R.string.label_reboot_service), Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * Load a list of certificates from the SDCARD
	 * 
	 * @return List of certificates
	 */
	private String[] loadCertificatesList() {
		String[] files = null;
		File folder = new File(CERTIFICATE_FOLDER_PATH);
		try {
			folder.mkdirs();
			if (folder.exists()) {
				// filter
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						return filename.contains(RcsSettingsData.CERTIFICATE_FILE_TYPE);
					}
				};
				files = folder.list(filter);
			}
		} catch (SecurityException e) {
			// intentionally blank
		}
		if (files == null) {
			// No certificate
			return new String[] { getString(R.string.label_no_certificate) };
		} else {
			// Add certificates in the list
			String[] temp = new String[files.length + 1];
			temp[0] = getString(R.string.label_no_certificate);
			if (files.length > 0) {
				System.arraycopy(files, 0, temp, 1, files.length);
			}
			return temp;
		}
	}
}