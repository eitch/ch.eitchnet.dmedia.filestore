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
	public void shouldValidateLeafTestVectors() {

		byte[] _zero_index = "0".getBytes();
		byte[] _one_index = "1".getBytes();
		//byte[] personalization = PERS_LEAF.getBytes();

		Skein skein;

		byte[] data;
		byte[] digest;
		String db32Hash;

		// A
		data = generateTestVectorA();
		skein = new Skein(BLOCK_BITS, DIGEST_BITS);
		skein.setKey(_zero_index);
		//skein.setPersonalization(personalization);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("A0: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("A0: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("A0: Skein D-Media Hash: " + db32Hash);
		// 0, pers : 3M3IHYG9TV3UWTIFI37LN5OVYFEB3PDIAUAQ7OKOQF84MIQT
		// 0, key db32 : WGLFVANTYOSPGHPDQJIRXUKRODJQGPXIKYSWAPA5ANSQV84Q
		// 0, key HEX  : eb64ce1e9afd7366bacabc1f8f6e38aaa176dbcf8ff3d3d8e23d337e1437

		skein = new Skein(BLOCK_BITS, DIGEST_BITS);
		skein.setKey(_one_index);
		//skein.setPersonalization(personalization);
		//digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("A1: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("A1: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("A1: Skein D-Media Hash: " + db32Hash);
		// 1, pers : VACOMMVCMJP9QBIUIEMMYAPXOWRYDFW3JQUJB4NGNMF48IIW
		// 1, key db32 : 47LY35TSXBTBXYDSRBX3TU3SXLAF69YV6YQHBGAPUCXNT9JB
		// 1, key HEX  : 0925f00b59f2348f7d59c23c0d6c19f48ec19bfc1feee434f6da7d4d1a08

		// db32    : WCKGWXCOBA5DEEC7N5ARHL6YNI3E6HF7RNCWE5WETK7NED8W
		// HEX     : ea62def93541c4a5ad24a08f87487fa3c0b1b984c513d58babd44945a8bd
		// pers db32 : ACDQE3OMRMYAKDOGB39VD8LW6TNTH8VYKA4WLHBL4IQOTYSL
		// pers HEX  : 3a557582b3c4fe78aaad400dc5165d1ea9a7179f89c3d939120bef5d7f32

		// B
		data = generateTestVectorB();
		skein = new Skein(BLOCK_BITS, DIGEST_BITS);
		skein.setKey(_zero_index);
		//skein.setPersonalization(PERS_LEAF.getBytes());
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("B0: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("B0: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("B0: Skein D-Media Hash: " + db32Hash);
		// 0, pers : YSRG5KWP5IKWCXTHNN58PY7KSEUJ6JYKDA4RT3F7T8UATQC7
		// 0, key db32 : 7FRLU9QFO7N6PQPUONKODUREWWEPPI3IYKI39AOXEL7GN7LX
		// 0, key HEX  : 23312d9aeca9283b5edbad23556f0bef576b3c0ffc5e031ebe5c88da125e

		skein = new Skein(BLOCK_BITS, DIGEST_BITS);
		skein.setKey(_one_index);
		//skein.setPersonalization(personalization);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("B1: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("B1: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("B1: Skein D-Media Hash: " + db32Hash);
		// 1, pers : V6QX3QUDUNE9PVJVF8HBUP4RBKY7AXRJ7MBPWASIKTVWYVUD
		// 1, key db32 : 87FJ4U6KHJBCYQ8AEIE8JEGBDWTEEXGCGA9OY5LJWH9CRQ87
		// 1, key HEX  : 291900ec7174109fdca75bd6582da85774b5f9a969cd5f8a50eb8c9c5ca4

		// db32    : EQGFHGHBFXQM48O8ENRGBU7CAVUIWQPWSEIBE4REXPHWDSED
		// HEX     : 5ddac735c867af3096a55d30d46c893f36fededdcade85870bf59dd5656a
		// pers db32 : YTD9DADG4YFID6MNSBUGO6QUMSKSGCPM8ERGCIDXCWSJ6N5Y
		// pers HEX  : fe94651d4d0fd8f50e74ca36da8efb9e6396a6d32af0d4bd5e4f7301d05f

		// C
		data = generateTestVectorC();
		skein = new Skein(BLOCK_BITS, DIGEST_BITS);
		skein.setKey(_zero_index);
		//skein.setPersonalization(personalization);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("C0: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("C0: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("C0: Skein D-Media Hash: " + db32Hash);
		// 0, pers : C8BNKNHHPCSONGE785HTVTIXCHFBCT5SY9GYRDELHQ3BVEHV
		// 0, key db32 : 9CWUX8TDLVV8L7XFNLTTWBOUFDMWLTTOIC95PPM6X4WFQRA8
		// 0, key HEX  : 327bbf174a97385913cca4b5aea2bb62a7d96b557a4c2b5a63f07acbe0e5

		skein = new Skein(BLOCK_BITS, DIGEST_BITS);
		skein.setKey(_one_index);
		//skein.setPersonalization(personalization);
		digest = skein.doSkein(data);
		db32Hash = Dbase32.db32EncAsString(digest);
		logger.info("C1: MD5 Hash: " + StringHelper.getHexString(StringHelper.hashMd5(digest)));
		logger.info("C1: Skein HEX: " + StringHelper.getHexString(digest));
		logger.info("C1: Skein D-Media Hash: " + db32Hash);
		// 1, pers : EEOJR9LHS46469EYDUWYWOK7BXW5Y486SNEG9KTSHSSPNIMQ
		// 1, key db32 : VE7ODF8SKSK7HOGXLGBDOJGAQH5IEIE6X6YGAX3SDOTD533T
		// 1, key HEX  : e2c95530b98e624755be9350aac1a7bb84f5bd63f0fed3f8195574a1001a

		// db32    : J8NWAN7YPEB8HIU8M4OMNO5K87R6NCT5VWD9LMRMMPHNK9SE
		// HEX     : 8169d3d09fb2d0573f65986b3a545129303a2742e754694f139d9d489b2b
		// pers db32 : 74N8TLFK89JE9W8G3QVDDIED6DTDP3SW9T9DC78X4IA7YUCE
		// pers HEX  : 20685d499129a0b374ad05f8a53d6a1ab4ab033d368ca490be0bce4fed2b
	}

	@Test
	public void shouldValidateRootTestVectors() {

		// no key, no pers
		// - DB32
		// "A": "RM9NUE56US7RNKMTDPQBUWVY7H5PFQ8PDOAFJCPKJ7JN88D5",
		// "B": "PCBTYGIOWL6K43BR3UNMNAET8WJUE8S6YY8H49TARGOJLR9M",
		// "C": "REUOEK5A5PN54KS8BMFIPFXPMSFMNIEH34J4SJ3L8MGFVQRK",
		// "CA": "JBL3BKWL53IIYPQOE8SUWWBQN8QMUR7I7PUCEW3XISC4N5SP",
		// "CB": "CUJMOGGM8GYU6EMYWCQODX4ABGN4FYRWRLO7TTGTAGP39X88",
		// "CC": "H8IPLFFRBMOY886WD43CJRN7D9ESSVAQTOWFBV3D5G3CEWU4"
		// - HEX
		//  "A": "c4cd4dac43de498a467a55ae8df79f2385665cb6554ec826d18121429542",
		// "B": "b251afb5f5ec8710811806e93a1d7a2f61b59723ffcae09b47c36b0960d3",
		// "C": "c2f755c44715a820c72544d8fb33d69e593a3d6e00601cc0122cdace5f11",
		// "CA": "82240447b2101effdaf55973bef517a16f3de08f25b695f41e7e521a0b36",
		// "CB": "4ee13ab5b32b7fb1ae7fea6f5578274368167f1dc4aa4d69ba3b6c0378a5",
		// "CC": "715f69319844ebf2947d504098628451979cf0f7d57ac4700a134095f761"

		// only pers
		// - DB32
		// "A": "C4DA9LV6WH5LQ8FHM59ISPPYDRHPA9OVRUXJ68JCMFYV7SVO",
		// "B": "JOOT3QTHI55GWIG9WGTEUR4T48833QJ6REDAKOYJUTEX5FYL",
		// "C": "L7U7BQI4TNRTJB6IGWAIFPW6BC9JUOXT5QI43HYVP5LSQONV",
		// "CA": "Q55EQ9D6G7G36ONN4DYQXEN8JWHM6LXX6LK737JRM3GQRX39",
		// "CB": "SEP6IMVGNY3L5JO3GW7UMK73HPTMK84XIUHHUJ3DXE4IJXWN",
		// "CC": "NDEFGGGQBGFM3ARPCG6FJP57FSR833PON66LJKVJYH6YEPCO"
		// - HEX
		// "A": "4854734b83eb852b958e988cfcdadf561d639abcc6fd0196099b3fc26795",
		// "B": "856ba05f4e7884debda6eb74bde03a094a005e03c2d478d7f0de97e133f2",
		// "C": "9136445de1d531a8206f6f4ef65ba3424d0dd7da15de103bfcb0a59bd69c",
		// "CA": "b884bb9943691a01d6940abf7f2e85875d31cbde1ca2401218981b7c7806",
		// "CB": "caec37cf8da7c12142a06f49b9c48075b538943e7edcedc00af2c2f87bb4",
		// "CC": "a296c6b5b74359301f164b46c8584466705002d5a0c7284790fb87f5d935"

		// only key
		// - DB32
		// "A": "CTR6CL4ASAHJCQ5UKUFNKPBIQB49OMVQ8HNBUHV7VG5U8U7U",
		// "B": "JY5C8MLCLKMSQ67M7HJL7R93XL7QXM9OQ84N967ULUPA4TBL",
		// "C": "JVSUKOVKWN97C77XNNJL4FKQFUCS5PCOG7UUXLISPIUO9XWL",
		// "CA": "V5EQDF7QRTEUPLFDIYHA36HL3TPOFX3UF84OEVOI5AXTQ7C7",
		// "CB": "MQDSA4JFGPKEMCEO5F76K63NPJWPF3CK9CEQRPQVVTQNV8VI",
		// "CC": "FTKHDSURT5U8T45WQUTB8OL7PASLYVAXQ5PO4N7XJ7C7TRJD"
		// - HEX
		// "A": "4eb034c827c9dd04dc5b8ed948d90fba026acf972ba88dbb84e345b2ec9b",
		// "B": "87c492ce4994679b8c9323a12260c0f4897f4cd5b943430c9b96ec70e912",
		// "C": "8733b8d791ed0c44909ea52120b23766d39159356937bf49f9b3f7537bb2",
		// "CA": "e097753097c697bb498a7fdc700dd206ad56781b614355f2af11fdab9124",
		// "CB": "9dd593860c6da2b9a5751308388c14b43b66013132577c5afce6af4e178f",
		// "CC": "66a2e56778d0b65d045dbef482d644b1f32ff0feb8ad50d09e81124d620a"
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
