/*
 * Copyright (c) 2008 - $Date: 2008/05/29 13:50:21 $, Sdu Identification BV
 * Classificatie: Commercieel vertrouwelijk
 *
 * File:     $rcsfile$
 * Date:     $Date: 2008/05/29 13:50:21 $
 * Version:  $Revision: 1.1 $
 */
package nl.warper.skein.test;

import static nl.warper.skein.SkeinUtil.lsbArrayOfLongToBytes;
import static nl.warper.skein.SkeinUtil.lsbBytesToArrayOfLong;
import static nl.warper.skein.SkeinUtil.toFormattedHex;
import static nl.warper.skein.SkeinUtil.tohex;
import static nl.warper.skein.SkeinUtil.zeroPad;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import nl.warper.skein.Skein;
import nl.warper.skein.SkeinConfiguration;
import nl.warper.skein.SkeinConstants;
import nl.warper.skein.SkeinTweak;
import nl.warper.skein.Ubi64;
import nl.warper.threefish.ThreefishImpl;
import nl.warper.threefish.ThreefishSecretKey;

import org.junit.Assert;
import org.junit.Test;

/**
 * SkeinTest is used to test the SimpleSkein, Ubi64 and Threefish implementations and to see that the test vectors,
 * including initial chaining vectors are correct. The output of the main() method has been used to successfully confirm
 * the old and new chaining values as created by the Skein team.
 * <P>
 * The initial implementation incorrectly used the blocksize as size of the configuration encoding instead of the real
 * size of the configuration encoding. You can simulate this incorrect behaviour by simply setting the private
 * USE_BLOCK_SIZE constant to true.
 * <P>
 * The Threefish part of the protocol is of course the most difficult part of the algorithm to implement. To test the
 * intermediate values of your own implementation, just change the logger settings below. For full logging to the
 * console, just set the logging level to Level.FINEST instead of Level.OFF.
 * 
 * @since 5 nov 2008
 * @author maartenb
 * @author Robert von Burg - refactored code for a nicer API and converted to JUnit 4 using asserts for actual
 *         verification
 */
public class SkeinTest {

	private static final boolean USE_BLOCK_SIZE = false;
	private static final boolean INITIAL_CHAINING_VALUES_IN_JAVA = false;
	private static final String LOGFORMAT = "%s Skein-%d-%d T(%d) %n%nMessage data:%n%s%nResult:%n%s%n";

	@Test
	public void testSkeinNew() {

		int blockSize;
		int outputSize;
		byte[] output;
		byte[] data;
		String name;
		Skein skein;

		name = "E.1";
		blockSize = 256;
		outputSize = 256;
		data = new byte[1];
		data[0] = -1;
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, data.length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "E.1";
		blockSize = 512;
		outputSize = 512;
		data = new byte[1];
		data[0] = -1;
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, data.length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));
	}

	@Test
	public void threeFishTest() {
		System.out.println();
		System.out.println(" === Threefish encrypt and decyrpt === ");
		testThreefish(256, 72);
		System.out.println();
		testThreefish(512, 80);
		System.out.println();
		testThreefish(1024, 80);
	}

	/**
	 * @param blockSize
	 * @param rounds
	 */
	private void testThreefish(final int blockSize, final int rounds) {

		final ThreefishImpl impl;
		if (rounds <= 0) {
			impl = new ThreefishImpl(blockSize);
		} else {
			impl = new ThreefishImpl(blockSize, rounds);
		}

		byte[] keyData = new byte[blockSize / Byte.SIZE]; // initialized to 00h values, used later on
		final long[] tweak;
		try {
			SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
			rnd.nextBytes(keyData);
			tweak = new long[] { rnd.nextLong(), rnd.nextLong() };
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		ThreefishSecretKey sk = new ThreefishSecretKey(keyData);

		impl.init(sk, tweak);
		final byte[] plain = "Maarten".getBytes();
		final byte[] plainPadded = zeroPad(plain, impl.getBlockSize());

		final long[] encryptedBlock = new long[impl.getBlockSize() / Long.SIZE];

		final long[] plainBlock = lsbBytesToArrayOfLong(plainPadded);
		System.out.printf("Threefish plainbl: %s%n", tohex(lsbArrayOfLongToBytes(plainBlock)));

		// not needed: impl.init(sk, tweak);
		impl.blockEncrypt(plainBlock, encryptedBlock);

		System.out.printf("Threefish encrypt: %s%n", tohex(lsbArrayOfLongToBytes(encryptedBlock)));

		long[] decryptedBlock = new long[encryptedBlock.length];
		impl.blockDecrypt(encryptedBlock, decryptedBlock);

		System.out.printf("Threefish decrypt: %s%n", tohex(lsbArrayOfLongToBytes(decryptedBlock)));

		Assert.assertTrue("Encrypt/Decrypt does not have the same result!", Arrays.equals(plainBlock, decryptedBlock));
	}

	@Test
	public void basicSkeinTest() {

		System.out.println();
		System.out.println(" === initial chaining values === ");
		System.out.printf("B.1 Skein-256-128%n%n");
		showConfigurationInit(256, 128);
		System.out.println();

		System.out.printf("B.2 Skein-256-128%n%n");
		showConfigurationInit(256, 160);
		System.out.println();

		System.out.printf("B.3 Skein-256-224%n%n");
		showConfigurationInit(256, 224);
		System.out.println();

		System.out.printf("B.4 Skein-256-256%n%n");
		showConfigurationInit(256, 256);
		System.out.println();

		System.out.printf("B.5 Skein-512-128%n%n");
		showConfigurationInit(512, 128);
		System.out.println();

		System.out.printf("B.6 Skein-512-160%n%n");
		showConfigurationInit(512, 160);
		System.out.println();

		System.out.printf("B.7 Skein-512-224%n%n");
		showConfigurationInit(512, 224);
		System.out.println();

		System.out.printf("B.8 Skein-512-256%n%n");
		showConfigurationInit(512, 256);
		System.out.println();

		System.out.printf("B.9 Skein-512-384%n%n");
		showConfigurationInit(512, 384);
		System.out.println();

		System.out.printf("B.10 Skein-512-512%n%n");
		showConfigurationInit(512, 512);
		System.out.println();

		System.out.printf("B.11 Skein-1024-384%n%n");
		showConfigurationInit(1024, 384);
		System.out.println();

		System.out.printf("B.12 Skein-1024-512%n%n");
		showConfigurationInit(1024, 512);
		System.out.println();

		System.out.printf("B.13 Skein-1024-1024%n%n");
		showConfigurationInit(1024, 1024);
		System.out.println();

		// initialization
		byte[] data;
		int blockSize;
		int outputSize;
		int length;
		byte[] output;
		String name;

		Skein skein;

		System.out.println();
		System.out.println(" === test values === ");

		// C.1
		blockSize = 256;
		outputSize = 256;

		name = "C.1 a.";
		length = 1;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "C.1 b.";
		length = 32;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "C.1 c.";
		length = 64;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		// C.2
		blockSize = 512;
		outputSize = 512;

		name = "C.2 a.";
		length = 1;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "C.2 b.";
		length = 64;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "C.2 c.";
		length = 128;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		// C.3
		blockSize = 1024;
		outputSize = 1024;

		name = "C.3 a.";
		length = 1;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "C.3 b.";
		length = 128;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "C.3 c.";
		length = 256;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		System.out.println("");
		System.out.println("End Basic Skein Test");
		System.out.println("=========================");
		System.out.println("");
	}

	@Test
	public void fullSkeinTest() {

		// initialization
		byte[] key;
		byte[] pers;
		byte[] nonce;
		byte[] data;
		int blockSize;
		int outputSize;
		int length;
		byte[] output;
		String name;
		Skein skein;

		System.out.println();
		System.out.println(" === test values === ");

		// D.1
		blockSize = 512;
		outputSize = 256;

		name = "D.1 a.";
		length = 1;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));
		skein = new Skein(blockSize, outputSize);

		name = "D.1 b.";
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "D.2 a.";
		length = 32;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));
		name = "D.2 b.";
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "D.3 a.";
		length = 64;
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));
		name = "D.3 b.";
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "D.4 a.";
		length = 32;
		key = createTestArray(length);
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		skein.setKey(key);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "D.5";
		length = 32;
		key = createTestArray(length);
		pers = "Personalized!".getBytes();
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		skein.setKey(key);
		skein.setPersonalization(pers);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		name = "D.6";
		length = 32;
		key = createTestArray(length);
		pers = "Personalized!".getBytes();
		nonce = createTestArray(length);
		data = createTestArray(length);
		skein = new Skein(blockSize, outputSize);
		skein.setKey(key);
		skein.setPersonalization(pers);
		skein.setNonce(nonce);
		output = skein.doSkein(data);
		System.out.printf(LOGFORMAT, name, blockSize, outputSize, length, toFormattedHex(data, 1),
				toFormattedHex(output, 1));

		System.out.println("");
		System.out.println("End Extended Skein Test");
		System.out.println("=========================");
		System.out.println("");
	}

	private void showConfigurationInit(final int blockSize, final int outputSize) {

		final int blockSizeBytes = blockSize / Byte.SIZE;

		if (outputSize <= 0 || outputSize % Byte.SIZE != 0) {
			throw new IllegalArgumentException(
					"The output size N must fullfil N MOD 8 = 0 (a complete number of bytes)");
		}

		// create buffer
		byte[] blockBuffer = new byte[blockSizeBytes];

		// create cipher
		ThreefishImpl threefish = new ThreefishImpl(blockSize);

		// create and init UBI
		Ubi64 ubi = new Ubi64(threefish);
		ubi.init();

		// create configuration
		SkeinConfiguration config = new SkeinConfiguration(outputSize, 0, 0, 0);
		byte[] configEncoding = config.getEncoded();
		// padded automatically, block is still filled with 00h values
		System.arraycopy(configEncoding, 0, blockBuffer, 0, configEncoding.length);
		long[] blockWords = lsbBytesToArrayOfLong(blockBuffer);

		int configSize = configEncoding.length;
		if (USE_BLOCK_SIZE) { // wrong, but whatever
			configSize = blockSizeBytes;
		}

		// create tweak for configuration
		// used configEncoding.length, but it seems the entire block should be in the tweak value (???) -> see question on site
		SkeinTweak tweak = new SkeinTweak(true, true, SkeinConstants.T_CFG, false, 0, configSize);

		final long configTweak[] = { tweak.getT0(), tweak.getT1() };

		// update UBI with configuration
		ubi.update(blockWords, configTweak);
		long[] initialChainingValue = ubi.getOutput();

		if (INITIAL_CHAINING_VALUES_IN_JAVA) {
			System.out.printf("\tpublic static final long[] INITIAL_CHAINING_VALUE_SKEIN_%d_%d = {%n", blockSize,
					outputSize);

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < initialChainingValue.length; i++) {
				sb.append(String.format("0x%016XL", initialChainingValue[i]));
				if (i != initialChainingValue.length - 1) {
					sb.append(", ");
					if (i % 4 == 3) {
						sb.append(String.format("%n"));
					}
				}
			}
			sb.append(String.format("%n"));
			System.out.println(sb.toString());
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < initialChainingValue.length; i++) {
				sb.append(String.format("0x%016X", initialChainingValue[i]));
				if (i != initialChainingValue.length - 1) {
					sb.append(", ");
					if (i % 4 == 3) {
						sb.append(String.format("%n"));
					}
				}
			}
			System.out.println(sb.toString());

		}
	}

	private static byte[] createTestArray(int bytes) {
		byte[] testArray = new byte[bytes];
		for (int i = 0; i < bytes; i++) {
			testArray[i] = (byte) (-i - 1);
		}
		return testArray;
	}
}