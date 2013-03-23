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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.warper.skein.Skein;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eitchnet.utils.helper.FileHelper;
import ch.eitchnet.utils.helper.StringHelper;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class DmediaFilestoreTest {

	private static final Logger logger = LoggerFactory.getLogger(DmediaFilestoreTest.class);

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
		Assert.assertEquals("7fc56270e7a70fa81a5935b72eacbe29", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// B
		bytes = generateTestVectorB();
		Assert.assertEquals("d2bad3eedb424dd352d65eafbf6c79ba", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// C
		bytes = generateTestVectorC();
		Assert.assertEquals("5dd3531303dd6764acb93e5f171a4ab8", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// CA
		bytes = generateTestVectorCA();
		Assert.assertEquals("0722f8dc36d75acb602dcee8d0427ce0", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// CB
		bytes = generateTestVectorCB();
		Assert.assertEquals("77264eb6eed7777a1ee03e2601fc9f64", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// CC
		bytes = generateTestVectorCC();
		Assert.assertEquals("1fbfabdaafff31967f9a95f3a3d3c642", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// integers
		Assert.assertEquals("cfcd208495d565ef66e7dff9f98764da", StringHelper.hashMd5AsHex("0").toLowerCase());
		Assert.assertEquals("c4ca4238a0b923820dcc509a6f75849b", StringHelper.hashMd5AsHex("1").toLowerCase());
		Assert.assertEquals("48ac8929ffdc78a66090d179ff1237d5", StringHelper.hashMd5AsHex("16777215").toLowerCase());
		Assert.assertEquals("e3e330499348f791337e9da6b534a386", StringHelper.hashMd5AsHex("16777216").toLowerCase());
		Assert.assertEquals("32433904a755e2b9eb82cf167723b34f", StringHelper.hashMd5AsHex("8388607").toLowerCase());
		Assert.assertEquals("03926fda4e223707d290ac06bb996653", StringHelper.hashMd5AsHex("8388608").toLowerCase());
		Assert.assertEquals("e9b74719ce6b80c5337148d12725db03", StringHelper.hashMd5AsHex("8388609").toLowerCase());

		// personalization
		Assert.assertEquals("8aee35e23a5a74147b230f12123ca82e", StringHelper.hashMd5AsHex(FileStoreConstants.PERS_LEAF)
				.toLowerCase());
		Assert.assertEquals("0445d91d37383f5384023d49e71cc629", StringHelper.hashMd5AsHex(FileStoreConstants.PERS_ROOT)
				.toLowerCase());
	}

	@Test
	public void testMd5TestVectors() {

		byte[] bytes;

		// A
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_A));
		Assert.assertEquals("7fc56270e7a70fa81a5935b72eacbe29", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// B
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_B));
		Assert.assertEquals("d2bad3eedb424dd352d65eafbf6c79ba", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// C
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_C));
		Assert.assertEquals("5dd3531303dd6764acb93e5f171a4ab8", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// CA
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CA));
		Assert.assertEquals("0722f8dc36d75acb602dcee8d0427ce0", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// CB
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CB));
		Assert.assertEquals("77264eb6eed7777a1ee03e2601fc9f64", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// CC
		bytes = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CC));
		Assert.assertEquals("1fbfabdaafff31967f9a95f3a3d3c642", StringHelper.getHexString(StringHelper.hashMd5(bytes))
				.toLowerCase());

		// integers
		Assert.assertEquals("cfcd208495d565ef66e7dff9f98764da", StringHelper.hashMd5AsHex("0").toLowerCase());
		Assert.assertEquals("c4ca4238a0b923820dcc509a6f75849b", StringHelper.hashMd5AsHex("1").toLowerCase());
		Assert.assertEquals("48ac8929ffdc78a66090d179ff1237d5", StringHelper.hashMd5AsHex("16777215").toLowerCase());
		Assert.assertEquals("e3e330499348f791337e9da6b534a386", StringHelper.hashMd5AsHex("16777216").toLowerCase());
		Assert.assertEquals("32433904a755e2b9eb82cf167723b34f", StringHelper.hashMd5AsHex("8388607").toLowerCase());
		Assert.assertEquals("03926fda4e223707d290ac06bb996653", StringHelper.hashMd5AsHex("8388608").toLowerCase());
		Assert.assertEquals("e9b74719ce6b80c5337148d12725db03", StringHelper.hashMd5AsHex("8388609").toLowerCase());

		// personalization
		Assert.assertEquals("8aee35e23a5a74147b230f12123ca82e", StringHelper.hashMd5AsHex(FileStoreConstants.PERS_LEAF)
				.toLowerCase());
		Assert.assertEquals("0445d91d37383f5384023d49e71cc629", StringHelper.hashMd5AsHex(FileStoreConstants.PERS_ROOT)
				.toLowerCase());
	}

	@Test
	public void shouldValidateLeafTestVectorsNoKeyNoPers() {

		Skein skein;

		byte[] data;
		byte[] digest;
		String db32Hash;

		// A
		data = generateTestVectorA();
		skein = new Skein(FileStoreConstants.BLOCK_BITS, FileStoreConstants.DIGEST_BITS);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("A0: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("A0: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("A0: Skein D-Media Hash: " + db32Hash);

		skein = new Skein(FileStoreConstants.BLOCK_BITS, FileStoreConstants.DIGEST_BITS);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("A1: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("A1: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("A1: Skein D-Media Hash: " + db32Hash);

		// B
		data = generateTestVectorB();
		skein = new Skein(FileStoreConstants.BLOCK_BITS, FileStoreConstants.DIGEST_BITS);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("B0: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("B0: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("B0: Skein D-Media Hash: " + db32Hash);

		skein = new Skein(FileStoreConstants.BLOCK_BITS, FileStoreConstants.DIGEST_BITS);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("B1: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("B1: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("B1: Skein D-Media Hash: " + db32Hash);

		// C
		data = generateTestVectorC();
		skein = new Skein(FileStoreConstants.BLOCK_BITS, FileStoreConstants.DIGEST_BITS);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("C0: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("C0: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("C0: Skein D-Media Hash: " + db32Hash);

		skein = new Skein(FileStoreConstants.BLOCK_BITS, FileStoreConstants.DIGEST_BITS);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("C1: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("C1: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("C1: Skein D-Media Hash: " + db32Hash);
	}

	private void assertLeafHash(DmediaFile dmediaFile, int expectedNrOfLeaves, int leafIndex, String hash) {
		List<DmediaFileSlice> fileSlices = dmediaFile.getFileSlices();
		assertLeafHash(fileSlices, expectedNrOfLeaves, leafIndex, hash);
	}

	private void assertLeafHash(List<DmediaFileSlice> fileSlices, int expectedNrOfLeaves, int leafIndex, String hash) {
		Assert.assertTrue(expectedNrOfLeaves == fileSlices.size());
		DmediaFileSlice fileSlice = fileSlices.get(leafIndex);
		Assert.assertEquals(hash, fileSlice.getHash());
	}

	@Test
	public void shouldStoreTestVectorsNoKeyNoPers() throws IOException {

		FileStore fileStore = new FileStore(FileStoreConstants.BLOCK_BITS, FileStoreConstants.DIGEST_BITS, false, false);
		byte[] data;

		// A
		data = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_A));
		String a0 = fileStore.hashLeafToString(0, data);
		Assert.assertEquals("WCKGWXCOBA5DEEC7N5ARHL6YNI3E6HF7RNCWE5WETK7NED8W", a0);
		String a1 = fileStore.hashLeafToString(1, data);
		Assert.assertEquals("WCKGWXCOBA5DEEC7N5ARHL6YNI3E6HF7RNCWE5WETK7NED8W", a1);
		String hA = fileStore.hashRootToString(data.length, a0.getBytes());
		Assert.assertEquals("", hA);

		// B
		data = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_B));
		String b0 = fileStore.hashLeafToString(0, data);
		Assert.assertEquals("", b0);
		String b1 = fileStore.hashLeafToString(1, data);
		Assert.assertEquals("", b1);
		String hB = fileStore.hashRootToString(data.length, b0.getBytes());
		Assert.assertEquals("", hB);

		// C
		data = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_C));
		String c0 = fileStore.hashLeafToString(0, data);
		Assert.assertEquals("", c0);
		String c1 = fileStore.hashLeafToString(1, data);
		Assert.assertEquals("", c1);
		String hC = fileStore.hashRootToString(data.length, c0.getBytes());
		Assert.assertEquals("", hC);

		ByteArrayOutputStream bout;

		// CA
		data = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CA));
		bout = new ByteArrayOutputStream();
		bout.write(c0.getBytes());
		bout.write(a1.getBytes());

		// CB
		data = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CB));

		// CC
		data = FileHelper.readFile(new File(TEST_VECTOR_PATH + TEST_VECTOR_CC));
	}

	private static byte[] generateTestVectorCC() {
		byte[] bytes;
		bytes = new byte[FileStoreConstants.LEAF_SIZE + FileStoreConstants.LEAF_SIZE];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 'C';
		}
		return bytes;
	}

	private static byte[] generateTestVectorCB() {
		byte[] bytes;
		bytes = new byte[FileStoreConstants.LEAF_SIZE + FileStoreConstants.LEAF_SIZE - 1];
		for (int i = 0; i < bytes.length; i++) {
			if (i >= FileStoreConstants.LEAF_SIZE)
				bytes[i] = 'B';
			else
				bytes[i] = 'C';
		}
		return bytes;
	}

	private static byte[] generateTestVectorCA() {
		byte[] bytes;
		bytes = new byte[FileStoreConstants.LEAF_SIZE + 1];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 'C';
		}
		bytes[bytes.length - 1] = 'A';
		return bytes;
	}

	private static byte[] generateTestVectorC() {
		byte[] bytes;
		bytes = new byte[FileStoreConstants.LEAF_SIZE];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 'C';
		}
		return bytes;
	}

	private static byte[] generateTestVectorB() {
		byte[] bytes;
		bytes = new byte[FileStoreConstants.LEAF_SIZE - 1];
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
