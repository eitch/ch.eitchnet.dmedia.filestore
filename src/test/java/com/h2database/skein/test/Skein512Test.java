package com.h2database.skein.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eitchnet.utils.helper.StringHelper;

import com.h2database.skein.Skein512;
import com.h2database.skein.Skein512Small;

/**
 * Skein 1.3 512-512 self test and performance benchmark.
 * <p>
 * Author: Thomas Mueller, 2008-2010. Released to the public domain.
 * Author: Robert von Burg, 2013 - Modified to use JUnit4 and cleaned up the code and JavaDoc a bit
 */
public class Skein512Test {

	/**
	 * Test vectors file expected to contain 3 consecutive lines:
	 * <ul>
	 * <li>Len = <code>n</code></li>
	 * <li>Msg = <code>Message to hash</code></li>
	 * <li>MD = <code>Expected digest</code></li>
	 * </ul>
	 */
	private static final String TEST_VECTORS_TXT = "/h2database/testVectors.txt";

	private static final Logger logger = LoggerFactory.getLogger(Skein512Test.class);

	private static int nrOfBits;
	private static byte[] message;
	private static byte[] digest;

	/**
	 * Parses the {@link #TEST_VECTORS_TXT} file and sets up the test
	 * 
	 * @throws IOException
	 */
	@BeforeClass
	public static void setup() throws IOException {

		InputStream inputStream = Skein512Test.class.getResourceAsStream(TEST_VECTORS_TXT);
		Assert.assertNotNull("Failed to find resource " + TEST_VECTORS_TXT, inputStream);

		LineNumberReader reader = new LineNumberReader(new InputStreamReader(inputStream));
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;

				if (line.startsWith("Len")) {
					nrOfBits = Integer.parseInt(line.split("=")[1].trim());
					message = StringHelper.fromHexString(reader.readLine().split("=")[1].trim());
					digest = StringHelper.fromHexString(reader.readLine().split("=")[1].trim());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse the input file " + TEST_VECTORS_TXT
					+ ". Does it contain the 3 consecutive lines as described in the JavaDoc?", e);
		} finally {
			reader.close();
		}

		Assert.assertFalse("No 'Len' parameter found", nrOfBits == 0);
		Assert.assertNotNull("Parameter 'Msg' not found!", message);
		Assert.assertNotNull("Parameter 'MD' not found", digest);
	}

	/**
	 * Read the test file, and for each entry check if the computed digest matches the expected result.
	 */
	@Test
	public void selfTest() {
		hashAndCheck(message, nrOfBits, digest);
	}

	/**
	 * Test the hash value.
	 * 
	 * @param msg
	 *            the message
	 * @param bitCount
	 *            the number of bits
	 * @param expected
	 *            the expected digest
	 */
	private void hashAndCheck(byte[] msg, int bitCount, byte[] expected) {
		byte[] result = new byte[expected.length];

		Skein512.hash(msg, bitCount, result);
		Assert.assertTrue("bitCount=" + bitCount, Arrays.equals(expected, result));

		if (msg.length == bitCount / 8) {
			Skein512Small.hash(msg, result);
			Assert.assertTrue("bitCount=" + bitCount, Arrays.equals(expected, result));
		}
	}

	/**
	 * Test the performance in processed megabytes per second.
	 */
	@Test
	public void performanceTest() throws Exception {

		// one MB
		int msgSize = 1024 * 1024;
		// how many bits to process
		int msgSizeInBits = msgSize * 8;
		// 200 runs by 1 MByte = 200MBytes
		int runCount = 200;

		for (int i = 0; i < 5; i++) {
			long start = System.currentTimeMillis();

			byte[] digest = new byte[512];
			byte[] msg = new byte[msgSize];

			for (int count = 0; count < runCount; count++) {
				Skein512.hash(msg, msgSizeInBits, digest);
			}

			long time = System.currentTimeMillis() - start;
			int mbPerSecond = (int) (runCount * 1000L / time);
			logger.info(mbPerSecond + " MB/s (" + time + " ms for " + runCount + " MB)");
		}
	}
}
