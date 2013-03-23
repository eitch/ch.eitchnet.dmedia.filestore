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
public class FileStoreConstants {

	public static final long MAX_FILE_SIZE = 9007199254740992L;
	public static final int MAX_LEAF_COUNT = 1073741824;
	public static final int LEAF_SIZE = 8388608;

	public static final int BLOCK_BITS = 512;
	public static final int DIGEST_BITS = 240;
	public static final int DIGEST_BYTES = 30;
	public static final int DIGEST_B32LEN = 48;
	public static final String PERS_LEAF = "20110430 jderose@novacut.com dmedia/leaf";
	public static final String PERS_ROOT = "20110430 jderose@novacut.com dmedia/root";
}
