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

import java.io.File;

import nl.warper.skein.Skein;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eitchnet.utils.helper.BaseEncoding;
import ch.eitchnet.utils.helper.FileHelper;
import ch.eitchnet.utils.helper.StringHelper;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class DmediaFilestoreTest {

	private static final Logger logger = LoggerFactory.getLogger(DmediaFilestoreTest.class);

	private static final long MAX_FILE_SIZE = 9007199254740992L;
	private static final long MAX_LEAF_COUNT = 1073741824L;

	private static final int LEAF_SIZE = 8388608;
	private static final int BLOCK_BITS = 512;
	private static final int DIGEST_BITS = 240;
	private static final int DIGEST_BYTES = 30;
	private static final int DIGEST_B32LEN = 48;
	private static final String PERS_LEAF = "20110430 jderose@novacut.com dmedia/leaf";
	private static final String PERS_ROOT = "20110430 jderose@novacut.com dmedia/root";

	private static final String TEST_VECTOR_PATH = "target/testvectors/";
	private static final String TEST_VECTOR_A = "A";
	private static final String TEST_VECTOR_B = "B";
	private static final String TEST_VECTOR_C = "C";
	private static final String TEST_VECTOR_CA = "CA";
	private static final String TEST_VECTOR_CB = "CB";
	private static final String TEST_VECTOR_CC = "CC";

	@BeforeClass
	public static void beforeClass() {

		File dirF = new File(TEST_VECTOR_PATH);
		if (dirF.exists() && !FileHelper.deleteFile(dirF, false))
			throw new RuntimeException("Could not remove existing path " + dirF.getAbsolutePath());
		if (!dirF.mkdirs())
			throw new RuntimeException("Could not create path " + dirF.getAbsolutePath());

		byte[] bytes;
		File dstFile;

		bytes = generateTestVectorA();
		dstFile = new File(TEST_VECTOR_PATH + TEST_VECTOR_A);
		FileHelper.writeToFile(bytes, dstFile);

		bytes = generateTestVectorB();
		dstFile = new File(TEST_VECTOR_PATH + TEST_VECTOR_B);
		FileHelper.writeToFile(bytes, dstFile);

		bytes = generateTestVectorC();
		dstFile = new File(TEST_VECTOR_PATH + TEST_VECTOR_C);
		FileHelper.writeToFile(bytes, dstFile);

		bytes = generateTestVectorCA();
		dstFile = new File(TEST_VECTOR_PATH + TEST_VECTOR_CA);
		FileHelper.writeToFile(bytes, dstFile);

		bytes = generateTestVectorCB();
		dstFile = new File(TEST_VECTOR_PATH + TEST_VECTOR_CB);
		FileHelper.writeToFile(bytes, dstFile);

		bytes = generateTestVectorCC();
		dstFile = new File(TEST_VECTOR_PATH + TEST_VECTOR_CC);
		FileHelper.writeToFile(bytes, dstFile);
	}

	@AfterClass
	public static void afterClass() {
		File dirF = new File(TEST_VECTOR_PATH);
		if (dirF.exists() && !FileHelper.deleteFile(dirF, false))
			throw new RuntimeException("Could not remove existing path " + dirF.getAbsolutePath());
	}

	@Test
	public void testDbase32Enc() {
		Assert.assertArrayEquals("Wrong D-Base32 encoding!", "FCNPVRELI7J9FUUI".getBytes(),
				Dbase32.db32Enc("binary foo".getBytes()));
	}

	@Test
	public void testDbase32Dec() {
		Assert.assertArrayEquals("Wrong D-Base32 Decoding!", "binary foo".getBytes(),
				Dbase32.db32Dec("FCNPVRELI7J9FUUI".getBytes()));
	}

	@Test
	public void testIsDb32() {
		Assert.assertEquals(true, Dbase32.isDb32Id("AAAAAAAA"));
		Assert.assertEquals(false, Dbase32.isDb32Id("AAAAAAAZ"));
		Assert.assertEquals(false, Dbase32.isDb32Id("AAAAAAA"));
	}

	@Test
	public void testDb32Id() {

		String randomId = Dbase32.generateRandomIdAsString();
		Assert.assertEquals("A Dbas32 ID must be " + Dbase32.RANDOM_ID_ENC_LENGTH + " long",
				Dbase32.RANDOM_ID_ENC_LENGTH, randomId.length());

		Dbase32.checkDb32Id(randomId);
	}

	@Test
	public void testCheckDb32() {

		try {
			Dbase32.checkDb32Id("AAAAAAAA".getBytes());
		} catch (Dbase32Exception e) {
			Assert.fail("This value is valid and should not throw an exception!");
		}

		try {
			Dbase32.checkDb32Id("AAAAAAAZ".getBytes());
			Assert.fail("This value is invalid and should throw an exception");
		} catch (Dbase32Exception e) {
			// good
		}

		try {
			Dbase32.checkDb32Id("AAAAAAA".getBytes());
			Assert.fail("This value is invalid and should throw an exception");
		} catch (Dbase32Exception e) {
			// good
		}
	}

	@Test
	public void testMd5DebugValues() {

		byte[] bytes;

		// A
		bytes = generateTestVectorA();
		Assert.assertEquals("7fc56270e7a70fa81a5935b72eacbe29", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// B
		bytes = generateTestVectorB();
		Assert.assertEquals("d2bad3eedb424dd352d65eafbf6c79ba", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// C
		bytes = generateTestVectorC();
		Assert.assertEquals("5dd3531303dd6764acb93e5f171a4ab8", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// CA
		bytes = generateTestVectorCA();
		Assert.assertEquals("0722f8dc36d75acb602dcee8d0427ce0", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// CB
		bytes = generateTestVectorCB();
		Assert.assertEquals("77264eb6eed7777a1ee03e2601fc9f64", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// CC
		bytes = generateTestVectorCC();
		Assert.assertEquals("1fbfabdaafff31967f9a95f3a3d3c642", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// integers
		Assert.assertEquals("cfcd208495d565ef66e7dff9f98764da", StringHelper.hashMd5AsHex("0"));
		Assert.assertEquals("c4ca4238a0b923820dcc509a6f75849b", StringHelper.hashMd5AsHex("1"));
		Assert.assertEquals("48ac8929ffdc78a66090d179ff1237d5", StringHelper.hashMd5AsHex("16777215"));
		Assert.assertEquals("e3e330499348f791337e9da6b534a386", StringHelper.hashMd5AsHex("16777216"));
		Assert.assertEquals("32433904a755e2b9eb82cf167723b34f", StringHelper.hashMd5AsHex("8388607"));
		Assert.assertEquals("03926fda4e223707d290ac06bb996653", StringHelper.hashMd5AsHex("8388608"));
		Assert.assertEquals("e9b74719ce6b80c5337148d12725db03", StringHelper.hashMd5AsHex("8388609"));

		// personalization
		Assert.assertEquals("8aee35e23a5a74147b230f12123ca82e", StringHelper.hashMd5AsHex(PERS_LEAF));
		Assert.assertEquals("0445d91d37383f5384023d49e71cc629", StringHelper.hashMd5AsHex(PERS_ROOT));
	}

	@Test
	public void testMd5TestVectors() {

		byte[] bytes;

		// A
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_A));
		Assert.assertEquals("7fc56270e7a70fa81a5935b72eacbe29", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// B
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_B));
		Assert.assertEquals("d2bad3eedb424dd352d65eafbf6c79ba", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// C
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_C));
		Assert.assertEquals("5dd3531303dd6764acb93e5f171a4ab8", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// CA
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CA));
		Assert.assertEquals("0722f8dc36d75acb602dcee8d0427ce0", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// CB
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CB));
		Assert.assertEquals("77264eb6eed7777a1ee03e2601fc9f64", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// CC
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CC));
		Assert.assertEquals("1fbfabdaafff31967f9a95f3a3d3c642", StringHelper.getHexString(StringHelper.hashMd5(bytes)));

		// integers
		Assert.assertEquals("cfcd208495d565ef66e7dff9f98764da", StringHelper.hashMd5AsHex("0"));
		Assert.assertEquals("c4ca4238a0b923820dcc509a6f75849b", StringHelper.hashMd5AsHex("1"));
		Assert.assertEquals("48ac8929ffdc78a66090d179ff1237d5", StringHelper.hashMd5AsHex("16777215"));
		Assert.assertEquals("e3e330499348f791337e9da6b534a386", StringHelper.hashMd5AsHex("16777216"));
		Assert.assertEquals("32433904a755e2b9eb82cf167723b34f", StringHelper.hashMd5AsHex("8388607"));
		Assert.assertEquals("03926fda4e223707d290ac06bb996653", StringHelper.hashMd5AsHex("8388608"));
		Assert.assertEquals("e9b74719ce6b80c5337148d12725db03", StringHelper.hashMd5AsHex("8388609"));

		// personalization
		Assert.assertEquals("8aee35e23a5a74147b230f12123ca82e", StringHelper.hashMd5AsHex(PERS_LEAF));
		Assert.assertEquals("0445d91d37383f5384023d49e71cc629", StringHelper.hashMd5AsHex(PERS_ROOT));
	}

	@Test
	public void shouldValidateTestVectors() {

		Skein skein = new Skein(BLOCK_BITS, DIGEST_BITS);
		// skein.setPersonalization(PERS_LEAF.getBytes());

		String message = "A";
		byte[] data = message.getBytes();
		byte[] digest = skein.doSkein(data);
		logger.info("Digest length: " + digest.length);

		String hash = new String(BaseEncoding.toBase32Dmedia(digest));
		logger.info("MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(message)));
		logger.info("Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("Skein D-Media Hash: " + hash);
	}

	private static byte[] generateTestVectorCC() {
		byte[] bytes;
		bytes = new byte[LEAF_SIZE + LEAF_SIZE];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 'C';
		}
		return bytes;
	}

	private static byte[] generateTestVectorCB() {
		byte[] bytes;
		bytes = new byte[LEAF_SIZE + LEAF_SIZE - 1];
		for (int i = 0; i < bytes.length; i++) {
			if (i >= LEAF_SIZE)
				bytes[i] = 'B';
			else
				bytes[i] = 'C';
		}
		return bytes;
	}

	private static byte[] generateTestVectorCA() {
		byte[] bytes;
		bytes = new byte[LEAF_SIZE + 1];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 'C';
		}
		bytes[bytes.length - 1] = 'A';
		return bytes;
	}

	private static byte[] generateTestVectorC() {
		byte[] bytes;
		bytes = new byte[LEAF_SIZE];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 'C';
		}
		return bytes;
	}

	private static byte[] generateTestVectorB() {
		byte[] bytes;
		bytes = new byte[LEAF_SIZE - 1];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 'B';
		}
		return bytes;
	}

	private static byte[] generateTestVectorA() {
		byte[] bytes;
		bytes = new byte[] { 'A' };
		return bytes;
	}
}
