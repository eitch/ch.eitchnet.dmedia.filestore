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

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class DmediaFileSlice {

	private DmediaFile dmediaFile;
	private int index;
	private long bytesOffset;
	private String hash;

	/**
	 * @param dmediaFile
	 * @param index
	 * @param bytesOffset
	 * @param hash
	 */
	public DmediaFileSlice(int index, long bytesOffset, String hash) {
		this.index = index;
		this.bytesOffset = bytesOffset;
		this.hash = hash;
	}

	/**
	 * @return the dmediaFile
	 */
	public DmediaFile getDmediaFile() {
		return this.dmediaFile;
	}

	/**
	 * @param dmediaFile
	 *            the dmediaFile to set
	 */
	public void setDmediaFile(DmediaFile dmediaFile) {
		this.dmediaFile = dmediaFile;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * @return the bytesOffset
	 */
	public long getBytesOffset() {
		return this.bytesOffset;
	}

	/**
	 * @return the hash
	 */
	public String getHash() {
		return this.hash;
	}
}
