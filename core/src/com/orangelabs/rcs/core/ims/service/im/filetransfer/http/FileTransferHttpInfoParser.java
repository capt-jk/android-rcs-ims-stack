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

package com.orangelabs.rcs.core.ims.service.im.filetransfer.http;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.orangelabs.rcs.utils.DateUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * File transfer over HTTP info parser
 
 * @author vfml3370
 */
public class FileTransferHttpInfoParser extends DefaultHandler {
	
	/* File-Transfer HTTP SAMPLE:
	<?xml version="1.0" encoding=�UTF-8�?>
	<file>
	<file-info type="thumbnail">
	<file-size>[thumbnail size in bytes]</file-size>
	<content-type>[MIME-type for thumbnail]</content-type>
	<data url = "[HTTP URL for the thumbnail]" until = "[validity of the thumbnail]"/>
	</file-info>
	<file-info type="file">
	<file-size>[file size in bytes]</file-size>
	<content-type>[MIME-type for file]</content-type>
	<data url = "[HTTP URL for the file]" until = "[validity of the file]"/>
	</file-info>
	<file>
   */

    /**
     * Accumulator buffer
     */
	private StringBuffer accumulator;

    /**
     * File transfer over HTTP info document
     */
	private FileTransferHttpInfoDocument ftInfo = null;

    /**
     * File transfer over HTTP thumbnail info
     */
	private FileTransferHttpThumbnail thumbnailInfo = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param inputSource Input source
     * @throws Exception
     */
    public FileTransferHttpInfoParser(InputSource inputSource) throws Exception {
    	SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(inputSource, this);
	}

    /**
     * Get file transfer info
     *
     * @return Info document
     */
	public FileTransferHttpInfoDocument getFtInfo() {
		return ftInfo;
	}
	
	/**
	 * Receive notification of the beginning of the document.
	 */
	public void startDocument() {
		if (logger.isActivated()) {
			logger.debug("Start document");
		}
		accumulator = new StringBuffer();
	}

	/**
	 * Receive notification of character data inside an element.
	 */
	public void characters(char buffer[], int start, int length) {
		accumulator.append(buffer, start, length);
	}

	/**
	 * Receive notification of the start of an element.
	 */
	public void startElement(String namespaceURL, String localName,	String qname, Attributes attr) {
		accumulator.setLength(0);
		if (localName.equals("file")) {		
			ftInfo = new FileTransferHttpInfoDocument();
		} else
		if (localName.equals("file-info")) {
			if (ftInfo != null) {
				String type = attr.getValue("type").trim();	
				if (type.equals("thumbnail")) {
					thumbnailInfo = new FileTransferHttpThumbnail();
				}
			}
		} else		
		if (localName.equals("data")) {
			if (ftInfo != null) {
				String url = attr.getValue("url").trim();
				String validity = attr.getValue("until").trim();
				if (ftInfo.getFileThumbnail() != null){
					ftInfo.setFileUrl(url);
					ftInfo.setTransferValidity(DateUtils.decodeDate(validity));
				} else
				if (thumbnailInfo != null) {
					thumbnailInfo.setThumbnailUrl(url);		
					thumbnailInfo.setThumbnailValidity(DateUtils.decodeDate(validity));
					ftInfo.setFileThumbnail(thumbnailInfo);
				}
			}
		}
	}

	/**
	 * Receive notification of the end of an element.
	 */
	public void endElement(String namespaceURL, String localName, String qname) {
		if (localName.equals("file-size")) {
			if (ftInfo != null && ftInfo.getFileThumbnail() != null) {
				ftInfo.setFileSize(Integer.parseInt(accumulator.toString().trim()));
			} else
			if (thumbnailInfo != null) {
				thumbnailInfo.setThumbnailSize(Integer.parseInt(accumulator.toString().trim()));
			}
		} else
		if (localName.equals("content-type")) {			                  
			if (ftInfo != null && ftInfo.getFileThumbnail() != null) {
				ftInfo.setFileType(accumulator.toString().trim());
			} else
			if (thumbnailInfo != null) {
				thumbnailInfo.setThumbnailType(accumulator.toString().trim());
			}
		} 
	}

	/**
	 * Receive notification of the end of the document.
	 */
	public void endDocument() {
		if (logger.isActivated()) {
			logger.debug("End document");
		}
	}
}
