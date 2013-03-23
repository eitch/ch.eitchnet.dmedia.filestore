/*
 * Copyright (c) 2012, Robert von Burg
 *
 * All rights reserved.
 *
 * This file is part of the XXX.
 *
 *  XXX is free software: you can redistribute 
 *  it and/or modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation, either version 3 of the License, 
 *  or (at your option) any later version.
 *
 *  XXX is distributed in the hope that it will 
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XXX.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package ch.eitchnet.dmedia.filestore.api;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class DmediaFile {

	private String filePath;
	private String rootHash;
	private long fileSize;
	private List<DmediaFileSlice> fileSlices;

	/**
	 * @param file
	 * @param fileSize
	 * @param leafHashes
	 * @param rootHash
	 */
	public DmediaFile(String filePath, long fileSize, String rootHash, List<DmediaFileSlice> fileSlices) {
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.fileSlices = fileSlices;
		this.rootHash = rootHash;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return this.filePath;
	}

	/**
	 * @return the fileSlices
	 */
	public List<DmediaFileSlice> getFileSlices() {
		return new ArrayList<DmediaFileSlice>(this.fileSlices);
	}

	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return this.fileSize;
	}

	/**
	 * @return the rootHash
	 */
	public String getRootHash() {
		return this.rootHash;
	}
}
