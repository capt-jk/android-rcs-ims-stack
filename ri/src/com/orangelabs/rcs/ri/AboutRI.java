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

import com.orangelabs.rcs.service.api.client.Build;
import com.orangelabs.rcs.utils.AppUtils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

/**
 * About RI
 * 
 * @author jexa7410
 */
public class AboutRI extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.app_about);

        // Set title
        setTitle(R.string.menu_about);
        
        // Display release number
        String relNumber = AppUtils.getApplicationVersion(this);
        TextView releaseView = (TextView)findViewById(R.id.release);
        releaseView.setText(getString(R.string.label_about_release, relNumber));

        String apiRelease = Build.GSMA_SUPPORTED_RELEASE + "_" + Build.API_CODENAME + "_" + Build.API_RELEASE;
        TextView apiView = (TextView)findViewById(R.id.api);
        apiView.setText(getString(R.string.label_about_api, apiRelease));
    }
}
