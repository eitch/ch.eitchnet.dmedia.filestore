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

import nl.warper.skein.Skein;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eitchnet.utils.helper.BaseEncoding;
import ch.eitchnet.utils.helper.StringHelper;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class DmediaFilestoreTest {

	private static final Logger logger = LoggerFactory.getLogger(DmediaFilestoreTest.class);

	private static final int LEAF_SIZE = 8388608;
	private static final int BLOCK_BITS = 512;
	private static final int DIGEST_BITS = 240;
	private static final int DIGEST_BYTES = 35;
	private static final int DIGEST_B32LEN = 56;
	private static final String PERS_LEAF = "20110430 jderose@novacut.com dmedia/leaf";
	private static final String PERS_ROOT = "20110430 jderose@novacut.com dmedia/root";

	@Test
	public void shouldValidateTestVectors() {

		Skein skein = new Skein(BLOCK_BITS, DIGEST_BITS);
		// skein.setPersonalization(PERS_LEAF.getBytes());

		String message = "A";
		byte[] data = message.getBytes();
		byte[] digest = skein.doSkein(data);
		logger.info("Digest length: " + digest.length);
		byte[] trimmed = new byte[DIGEST_BITS/8];
		System.arraycopy(digest, 0, trimmed, 0, trimmed.length);
		digest = trimmed;
		logger.info("Trimmed Digest length: " + digest.length);
		

		String hash = new String(BaseEncoding.toBase32Dmedia(digest));
		logger.info("MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(message)));
		logger.info("Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("Skein D-Media Hash: " + hash);
	}
}
