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

package com.orangelabs.rcs.core.ims.service.toip;

import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;

/**
 * ToIP session
 * 
 * @author jexa7410
 */
public abstract class ToIpSession extends ImsServiceSession {
    /**
	 * Media player
	 */
	private IMediaPlayer player = null;
	
    /**
	 * Media renderer
	 */
	private IMediaRenderer renderer = null;
	
	/**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 */
	public ToIpSession(ImsService parent, String contact) {
		super(parent, contact);
	}

	/**
	 * Get the media player
	 * 
	 * @return Player
	 */
	public IMediaPlayer getMediaPlayer() {
		return player;
	}
	
	/**
	 * Set the media player
	 * 
	 * @param player Player
	 */
	public void setMediaPlayer(IMediaPlayer player) {
		this.player = player;
	}
	
	/**
	 * Get the media renderer
	 * 
	 * @return Renderer
	 */
	public IMediaRenderer getMediaRenderer() {
		return renderer;
	}
	
	/**
	 * Set the media renderer
	 * 
	 * @param renderer Renderer
	 */
	public void setMediaRenderer(IMediaRenderer renderer) {
		this.renderer = renderer;
	}	
	
	/**
	 * Returns the event listener
	 * 
	 * @return Listener
	 */
	public ToIpSessionListener getListener() {
		return (ToIpSessionListener)super.getListener();
	}
}
