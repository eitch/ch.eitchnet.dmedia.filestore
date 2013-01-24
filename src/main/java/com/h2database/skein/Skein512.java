package com.h2database.skein;

import java.util.Arrays;

/**
 * <p>
 * This is a fast implementation of the Skein-512-512 hash function. All other modes are not tested and supported. It is
 * compatible with the revised reference implementation (1.3).
 * </p>
 * <ul>
 * <li>Author: Thomas Mueller, 2008-2010 based on the C reference implementation written by Doug Whiting, 2008.</li>
 * <li>Author: Robert von Burg, 2013 - Formatted code, added JavaDoc and fixed some compiler warnings</li>
 * </ul>
 * 
 * <p>
 * This algorithm and source code is released to the public domain.
 * </p>
 */
public class Skein512 {

	// block function constants
	private static final int R00 = 46, R01 = 36, R02 = 19, R03 = 37;
	private static final int R10 = 33, R11 = 27, R12 = 14, R13 = 42;
	private static final int R20 = 17, R21 = 49, R22 = 36, R23 = 39;
	private static final int R30 = 44, R31 = 9, R32 = 54, R33 = 56;
	private static final int R40 = 39, R41 = 30, R42 = 34, R43 = 24;
	private static final int R50 = 13, R51 = 50, R52 = 10, R53 = 17;
	private static final int R60 = 25, R61 = 29, R62 = 39, R63 = 43;
	private static final int R70 = 8, R71 = 35, R72 = 56, R73 = 22;

	// version 1, id-string "SHA3"
	private static final long SCHEMA_VERSION = 0x133414853L;
	private static final long T1_FLAG_FINAL = 1L << 63;
	private static final long T1_FLAG_FIRST = 1L << 62;
	private static final long T1_FLAG_BIT_PAD = 1L << 55;
	private static final long T1_POS_TYPE = 56;
	private static final long TYPE_CONFIG = 4L << Skein512.T1_POS_TYPE;
	private static final long TYPE_MESSAGE = 48L << Skein512.T1_POS_TYPE;
	private static final long TYPE_OUT = 63L << Skein512.T1_POS_TYPE;
	private static final int WORDS = 8, BYTES = 8 * Skein512.WORDS;
	private static final long KS_PARITY = 0x1BD11BDAA9FC1A22L;

	private static final int ROUNDS = 72;
	private static final int[] MOD3 = new int[Skein512.ROUNDS];
	private static final int[] MOD9 = new int[Skein512.ROUNDS];
	static {
		for (int i = 0; i < Skein512.MOD3.length; i++) {
			Skein512.MOD3[i] = i % 3;
			Skein512.MOD9[i] = i % 9;
		}
	}

	private static Skein512 INITIALIZED = new Skein512(512);

	// size of hash result, in bits
	private int hashBitCount;

	// current byte count in the buffer
	private int byteCount;

	// tweak words: tweak0=byte count, tweak1=flags
	private long tweak0, tweak1;

	// chaining variables
	private long[] x = new long[Skein512.WORDS];

	// partial block buffer (8-byte aligned)
	private byte[] buffer = new byte[Skein512.BYTES];

	// key schedule: tweak
	private long[] tweakSchedule = new long[5];

	// key schedule: chaining variables
	private long[] keySchedule = new long[17];

	/**
	 * Build/Process the configuration block (only done once)
	 * 
	 * @param hashBitCount
	 */
	private Skein512(int hashBitCount) {
		this.hashBitCount = hashBitCount;
		startNewType(Skein512.TYPE_CONFIG | Skein512.T1_FLAG_FINAL);

		// set the schema, version
		long[] w = new long[] { Skein512.SCHEMA_VERSION, hashBitCount };

		// compute the initial chaining values from the configuration block
		setBytes(this.buffer, w, 2 * 8);
		processBlock(this.buffer, 0, 1, 4 * Skein512.WORDS);

		// the chaining vars (x) are now initialized for the given hashBitLen.
		// set up to process the data message portion of the hash (default)
		// buffer starts out empty
		startNewType(Skein512.TYPE_MESSAGE);
	}

	/**
	 * Private constructor
	 */
	private Skein512() {
		this.hashBitCount = Skein512.INITIALIZED.hashBitCount;
		this.tweak0 = Skein512.INITIALIZED.tweak0;
		this.tweak1 = Skein512.INITIALIZED.tweak1;
		System.arraycopy(Skein512.INITIALIZED.x, 0, this.x, 0, this.x.length);
	}

	/**
	 * <p>
	 * Calculates the hash code of the given message. Each bit in the message is processed.
	 * </p>
	 * 
	 * <p>
	 * This is the same as calling <code>Skein512.hash(msg, msg.length * 8, digest)</code>
	 * </p>
	 * 
	 * @param msg
	 *            the message to hash
	 * @param digest
	 *            the resulting hash code
	 */
	public static void hash(byte[] msg, byte[] digest) {
		Skein512.hash(msg, msg.length * 8, digest);
	}

	/**
	 * Calculates the hash code of the given message.
	 * 
	 * @param msg
	 *            the message to hash
	 * @param bitCount
	 *            the number of bits to process. Mostly the length of the message * 8
	 * @param digest
	 *            the resulting hash code
	 */
	public static void hash(byte[] msg, int bitCount, byte[] digest) {

		int byteCount = bitCount >>> 3;
		if ((bitCount & 7) != 0) {
			int mask = 1 << (7 - (bitCount & 7));
			msg[byteCount] = (byte) ((msg[byteCount] & (-mask)) | mask);
			byteCount++;
		}

		Skein512 instance = new Skein512();
		instance.update(msg, byteCount);

		if ((bitCount & 7) != 0) {
			instance.tweak1 |= Skein512.T1_FLAG_BIT_PAD;
		}

		instance.finalize(digest);
	}

	/**
	 * Process the input bytes
	 * 
	 * @param msg
	 * @param len
	 */
	private void update(byte[] msg, int length) {

		int len = length;
		int pos = 0;

		// process full blocks, if any
		if (len + this.byteCount > Skein512.BYTES) {

			// finish up any buffered message data
			if (this.byteCount != 0) {

				// # bytes free in buffer
				int n = Skein512.BYTES - this.byteCount;
				if (n != 0) {
					System.arraycopy(msg, 0, this.buffer, this.byteCount, n);
					len -= n;
					pos += n;
					this.byteCount += n;
				}

				processBlock(this.buffer, 0, 1, Skein512.BYTES);
				this.byteCount = 0;
			}

			// now process any remaining full blocks,
			// directly from input message data
			if (len > Skein512.BYTES) {

				// number of full blocks to process
				int n = (len - 1) / Skein512.BYTES;
				processBlock(msg, pos, n, Skein512.BYTES);
				len -= n * Skein512.BYTES;
				pos += n * Skein512.BYTES;
			}
		}

		// copy any remaining source message data bytes into the buffer
		if (len != 0) {
			System.arraycopy(msg, pos, this.buffer, this.byteCount, len);
			this.byteCount += len;
		}
	}

	/**
	 * Finalizes the hash computation and output the result
	 * 
	 * @param hash
	 */
	private void finalize(byte[] hash) {

		// tag as the final block
		this.tweak1 |= Skein512.T1_FLAG_FINAL;

		// zero pad if necessary
		if (this.byteCount < Skein512.BYTES) {
			Arrays.fill(this.buffer, this.byteCount, Skein512.BYTES, (byte) 0);
		}

		// process the final block
		processBlock(this.buffer, 0, 1, this.byteCount);

		// now output the result
		// zero out the buffer, so it can hold the counter
		Arrays.fill(this.buffer, (byte) 0);

		// up to 512 bits are supported
		// build the counter block
		startNewType(Skein512.TYPE_OUT | Skein512.T1_FLAG_FINAL);

		// run "counter mode"
		processBlock(this.buffer, 0, 1, 8);

		// "output" the counter mode bytes
		setBytes(hash, this.x, (this.hashBitCount + 7) >> 3);
	}

	/**
	 * Set up for starting with a new type
	 * 
	 * @param type
	 */
	private void startNewType(long type) {
		this.tweak0 = 0;
		this.tweak1 = Skein512.T1_FLAG_FIRST | type;
	}

	private void processBlock(byte[] block, int offset, int nrOfBlocks, int bytes) {

		int off = offset;
		int blocks = nrOfBlocks;

		while (blocks-- > 0) {

			// this implementation supports 2**64 input bytes (no carry out here)
			// update processed length
			long[] ts = this.tweakSchedule;
			this.tweak0 += bytes;
			int[] mod3 = Skein512.MOD3;
			int[] mod9 = Skein512.MOD9;
			ts[3] = ts[0] = this.tweak0;
			ts[4] = ts[1] = this.tweak1;
			ts[2] = this.tweak0 ^ this.tweak1;
			long[] c = this.x;
			long[] ks = this.keySchedule;

			// pre-compute the key schedule for this block
			System.arraycopy(c, 0, ks, 0, 8);
			System.arraycopy(c, 0, ks, 9, 8);
			ks[8] = Skein512.KS_PARITY ^ c[7] ^ c[0] ^ c[1] ^ c[2] ^ c[3] ^ c[4] ^ c[5] ^ c[6];

			// do the first full key injection
			long x0 = (c[0] = getLong(block, off)) + ks[0];
			long x1 = (c[1] = getLong(block, off + 8)) + ks[1];
			long x2 = (c[2] = getLong(block, off + 16)) + ks[2];
			long x3 = (c[3] = getLong(block, off + 24)) + ks[3];
			long x4 = (c[4] = getLong(block, off + 32)) + ks[4];
			long x5 = (c[5] = getLong(block, off + 40)) + ks[5] + this.tweak0;
			long x6 = (c[6] = getLong(block, off + 48)) + ks[6] + this.tweak1;
			long x7 = (c[7] = getLong(block, off + 56)) + ks[7];

			// unroll 8 rounds
			for (int r = 1; r <= Skein512.ROUNDS / 4; r += 2) {
				int rm9 = mod9[r], rm3 = mod3[r];
				x1 = rotlXor(x1, Skein512.R00, x0 += x1);
				x3 = rotlXor(x3, Skein512.R01, x2 += x3);
				x5 = rotlXor(x5, Skein512.R02, x4 += x5);
				x7 = rotlXor(x7, Skein512.R03, x6 += x7);
				x1 = rotlXor(x1, Skein512.R10, x2 += x1);
				x7 = rotlXor(x7, Skein512.R11, x4 += x7);
				x5 = rotlXor(x5, Skein512.R12, x6 += x5);
				x3 = rotlXor(x3, Skein512.R13, x0 += x3);
				x1 = rotlXor(x1, Skein512.R20, x4 += x1);
				x3 = rotlXor(x3, Skein512.R21, x6 += x3);
				x5 = rotlXor(x5, Skein512.R22, x0 += x5);
				x7 = rotlXor(x7, Skein512.R23, x2 += x7);
				x1 = rotlXor(x1, Skein512.R30, x6 += x1) + ks[rm9 + 1];
				x7 = rotlXor(x7, Skein512.R31, x0 += x7) + ks[rm9 + 7] + r;
				x5 = rotlXor(x5, Skein512.R32, x2 += x5) + ks[rm9 + 5] + ts[rm3];
				x3 = rotlXor(x3, Skein512.R33, x4 += x3) + ks[rm9 + 3];
				x1 = rotlXor(x1, Skein512.R40, x0 += x1 + ks[rm9]);
				x3 = rotlXor(x3, Skein512.R41, x2 += x3 + ks[rm9 + 2]);
				x5 = rotlXor(x5, Skein512.R42, x4 += x5 + ks[rm9 + 4]);
				x7 = rotlXor(x7, Skein512.R43, x6 += x7 + ks[rm9 + 6] + ts[rm3 + 1]);
				x1 = rotlXor(x1, Skein512.R50, x2 += x1);
				x7 = rotlXor(x7, Skein512.R51, x4 += x7);
				x5 = rotlXor(x5, Skein512.R52, x6 += x5);
				x3 = rotlXor(x3, Skein512.R53, x0 += x3);
				x1 = rotlXor(x1, Skein512.R60, x4 += x1);
				x3 = rotlXor(x3, Skein512.R61, x6 += x3);
				x5 = rotlXor(x5, Skein512.R62, x0 += x5);
				x7 = rotlXor(x7, Skein512.R63, x2 += x7);
				x1 = rotlXor(x1, Skein512.R70, x6 += x1) + ks[rm9 + 2];
				x7 = rotlXor(x7, Skein512.R71, x0 += x7) + ks[rm9 + 8] + r + 1;
				x5 = rotlXor(x5, Skein512.R72, x2 += x5) + ks[rm9 + 6] + ts[rm3 + 1];
				x3 = rotlXor(x3, Skein512.R73, x4 += x3) + ks[rm9 + 4];
				x0 += ks[rm9 + 1];
				x2 += ks[rm9 + 3];
				x4 += ks[rm9 + 5];
				x6 += ks[rm9 + 7] + ts[rm3 + 2];
			}

			// do the final "feed forward" xor, update context chaining vars
			c[6] ^= x6;
			c[4] ^= x4;
			c[0] ^= x0;
			c[1] ^= x1;
			c[2] ^= x2;
			c[3] ^= x3;
			c[5] ^= x5;
			c[7] ^= x7;

			// clear the start bit
			this.tweak1 &= ~Skein512.T1_FLAG_FIRST;
			off += Skein512.BYTES;
		}
	}

	private long rotlXor(long x, int n, long xor) {
		return ((x << n) | (x >>> (64 - n))) ^ xor;
	}

	private void setBytes(byte[] dst, long[] src, int byteCount) {
		for (int n = 0, i = 0; n < byteCount; n += 8, i++) {
			long x = src[i];
			dst[n] = (byte) x;
			dst[n + 1] = (byte) (x >> 8);
			dst[n + 2] = (byte) (x >> 16);
			dst[n + 3] = (byte) (x >> 24);
			dst[n + 4] = (byte) (x >> 32);
			dst[n + 5] = (byte) (x >> 40);
			dst[n + 6] = (byte) (x >> 48);
			dst[n + 7] = (byte) (x >> 56);
		}
	}

	private long getLong(byte[] b, int i) {
		if (i >= b.length + 8) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return (((b[i] & 255) + ((b[i + 1] & 255) << 8) + ((b[i + 2] & 255) << 16) + ((b[i + 3] & 255) << 24)) & 0xffffffffL)
				+ (((b[i + 4] & 255) + ((b[i + 5] & 255) << 8) + ((b[i + 6] & 255) << 16) + ((b[i + 7] & 255L) << 24)) << 32);
	}
}
