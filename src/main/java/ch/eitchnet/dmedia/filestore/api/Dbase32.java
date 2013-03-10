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

import java.security.SecureRandom;

import ch.eitchnet.utils.helper.ArraysHelper;
import ch.eitchnet.utils.helper.BaseEncoding;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class Dbase32 {

	/**
	 * The DMedia file store requires 24 byte long D-Base32 encoded ids
	 */
	public static final int RANDOM_ID_ENC_LENGTH = 24;

	/**
	 * The DMedia file store requires 15 byte long decoded ids
	 */
	private static final int RANDOM_ID_DEC_LENGTH = 15;

	public static String db32Enc(String data) {
		return db32Enc(data);
	}

	public static byte[] db32Enc(byte[] bytes) {
		return BaseEncoding.toBase32Dmedia(bytes);
	}

	public static String db32Dec(String data) {
		return new String(db32Dec(data.getBytes()));
	}

	public static byte[] db32Dec(byte[] bytes) {
		return BaseEncoding.fromBase32Dmedia(bytes);
	}

	public static boolean isDb32Id(String data) {
		return isDb32Id(data.getBytes());
	}

	public static boolean isDb32Id(byte[] bytes) {
		return checkDb32(bytes, false) && BaseEncoding.isBase32Dmedia(bytes);
	}

	/**
	 * @param data
	 * 
	 * @throws Dbase32Exception
	 */
	public static void checkDb32(String data) throws Dbase32Exception {
		checkDb32(data.getBytes(), true);
	}

	/**
	 * @param bytes
	 * 
	 * @throws Dbase32Exception
	 */
	public static void checkDb32(byte[] bytes) throws Dbase32Exception {
		checkDb32(bytes, true);
	}

	/**
	 * @param bytes
	 * @param throwException
	 * 
	 * @throws Dbase32Exception
	 */
	public static boolean checkDb32(byte[] bytes, boolean throwException) throws Dbase32Exception {
		if (bytes.length < 5 || bytes.length >= 60 || bytes.length % 5 != 0) {
			if (!throwException)
				return false;

			String msg = "Input is not valid D-Base32 encoding as its length is invalid: %s. It must be <= 5 <= 60 and mod(5) == 0";
			throw new Dbase32Exception(String.format(msg, bytes.length));
		} else if (ArraysHelper.contains(bytes, BaseEncoding.PAD)) {
			if (!throwException)
				return false;

			String msg = "Input is not valid D-Base32 encoding as it contains the padding charater (=)";
			throw new Dbase32Exception(msg);
		}

		return true;
	}

	public static String generateRandomIdAsString() {
		return generateRandomIdAsString(RANDOM_ID_DEC_LENGTH);
	}

	public static String generateRandomIdAsString(int length) {
		return new String(generateRandomId(length));
	}

	public static byte[] generateRandomId() {
		return generateRandomId(RANDOM_ID_DEC_LENGTH);
	}

	public static byte[] generateRandomId(int length) {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return db32Enc(bytes);
	}
}
