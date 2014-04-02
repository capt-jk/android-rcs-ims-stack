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
package com.orangelabs.rcs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.orangelabs.rcs.utils.logger.Logger;

import javax2.sip.InvalidArgumentException;

/**
 * File utilities
 * 
 * @author YPLO6403
 * 
 */
public class FileUtils {

	private static final Logger logger = Logger.getLogger(FileUtils.class.getSimpleName());

	/**
	 * Copy a file to a directory
	 * 
	 * @param srcFile
	 *            the source file (may not be null)
	 * @param destDir
	 *            the destination directory (may not be null)
	 * @param preserveFileDate
	 *            whether to preserve the file date
	 * @throws IOException
	 * @throws InvalidArgumentException
	 */
	public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws IOException,
			InvalidArgumentException {
		if (srcFile == null) {
			throw new InvalidArgumentException("Source is null");
		}
		if (srcFile.exists() == false) {
			throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new IOException("Source '" + srcFile + "' is a directory");
		}
		if (destDir == null) {
			throw new InvalidArgumentException("Destination is null");
		}
		if (destDir.exists() == false) {
			// Create directory if it does not exist
			if (destDir.mkdir() == false) {
				throw new IOException("Destination '" + destDir + "' directory cannot be created");
			}
		} else {
			if (destDir.isDirectory() == false) {
				throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
			}
		}
		File destFile = new File(destDir, srcFile.getName());
		if (destFile.exists() && destFile.canWrite() == false) {
			throw new IOException("Destination '" + destFile + "' file exists but is read-only");
		}
		FileInputStream input = new FileInputStream(srcFile);
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				if (input != null)
					input.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		// check if full content is copied
		if (srcFile.length() != destFile.length()) {
			throw new IOException("Failed to copy from '" + srcFile + "' to '" + destFile + "'");
		}
		// preserve the file date
		if (preserveFileDate)
			destFile.setLastModified(srcFile.lastModified());
	}

}
