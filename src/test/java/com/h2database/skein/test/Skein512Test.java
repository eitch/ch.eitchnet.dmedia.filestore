package com.h2database.skein.test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;

import com.h2database.skein.Skein512;
import com.h2database.skein.Skein512Small;


/**
 * Skein 1.3 512-512 self test and performance benchmark.
 * <p>
 * Author: Thomas Mueller, 2008-2010. Released to the public domain.
 */
public class Skein512Test {

	/**
	 * 
	 */
	private static final String TEST_VECTORS_TXT = "/h2database/testVectors.txt";

	/**
	 * Run all tests.
	 * 
	 * @param args
	 *            the command line arguments (ignored)
	 */
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 5; i++) {
			testPerformance();
		}
		testSelf();
	}

	/**
	 * Read the test file, and for each entry check if the computed digest matches the expected result.
	 */
	static void testSelf() throws Exception {
		InputStream inputStream = Skein512Test.class.getResourceAsStream(TEST_VECTORS_TXT);
		if (inputStream == null)
			throw new RuntimeException("Failed to find resource " + TEST_VECTORS_TXT);
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(inputStream));
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			} else if (line.startsWith("Len")) {
				int len = Integer.parseInt(line.split("=")[1].trim());
				byte[] msg = toBytes(reader.readLine().split("=")[1].trim());
				byte[] digest = toBytes(reader.readLine().split("=")[1].trim());
				test(msg, len, digest);
			}
		}
		reader.close();
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
	static void test(byte[] msg, int bitCount, byte[] expected) {
		byte[] result = new byte[expected.length];
		Skein512.hash(msg, bitCount, result);
		if (!Arrays.equals(expected, result)) {
			throw new AssertionError("bitCount=" + bitCount);
		}
		if (msg.length == bitCount / 8) {
			Skein512Small.hash(msg, result);
			if (!Arrays.equals(expected, result)) {
				throw new AssertionError("bitCount=" + bitCount);
			}
		}
	}

	/**
	 * Test the performance in processed megabytes per second.
	 */
	static void testPerformance() throws Exception {
		byte[] digest = new byte[512];
		byte[] msg = new byte[1024 * 1024];
		long start = System.currentTimeMillis();
		int mb = 200;
		for (int count = 0; count < mb; count++) {
			Skein512.hash(msg, 1024 * 1024 * 8, digest);
		}
		long time = System.currentTimeMillis() - start;
		int mbPerSecond = (int) (mb * 1000L / time);
		System.out.println(mbPerSecond + " MB/s (" + time + " ms for " + mb + " MB)");
	}

	/**
	 * Convert a hex encoded string to a byte array.
	 * 
	 * @param s
	 *            the hex encoded string
	 * @return the byte array
	 */
	static byte[] toBytes(String s) {
		int len = s.length();
		if (len % 2 != 0) {
			throw new IllegalArgumentException(s);
		}
		len /= 2;
		byte[] buff = new byte[len];
		for (int i = 0; i < len; i++) {
			buff[i] = (byte) Integer.parseInt(s.substring(i + i, i + i + 2), 16);
		}
		return buff;
	}

}
