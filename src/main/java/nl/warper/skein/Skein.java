package nl.warper.skein;

import nl.warper.threefish.ThreefishImpl;

/**
 * The main class for the Skein hash algorithm by Niels Ferguson, Stefan Lucks, Bruce Schneier, Doug Whiting, Mihir
 * Bellare, Tadayoshi Kohno, Jon Callas and Jesse Walker.
 * 
 * @since 4 nov 2008
 * @author Maarten Bodewes
 * @author Robert von Burg - simplified API so that using the full skein implementation is easier and there is only one
 *         method to perform the Skein Hash
 */
public class Skein {

	// input data
	private final int blockSize;
	private final int blockSizeBytes;
	private final int outputSize;

	// extended input configuration
	private byte[] key;
	private byte[] pers;
	private byte[] pk;
	private byte[] kdf;
	private byte[] nonce;

	private Ubi64 ubi;

	/**
	 * @param blockSize
	 * @param outputSize
	 */
	public Skein(final int blockSize, final int outputSize) {
		if (outputSize <= 0 || outputSize % Byte.SIZE != 0) {
			throw new IllegalArgumentException(
					"The output size N must fullfil N MOD 8 = 0 (a complete number of bytes)");
		}

		this.blockSize = blockSize;
		this.blockSizeBytes = blockSize / Byte.SIZE;
		this.outputSize = outputSize;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(byte[] key) {
		this.key = key;
	}

	/**
	 * @param pers
	 *            the pers to set
	 */
	public void setPersonalization(byte[] personalization) {
		this.pers = personalization;
	}

	/**
	 * @param pk
	 *            the pk to set
	 */
	public void setPk(byte[] pk) {
		this.pk = pk;
	}

	/**
	 * @param kdf
	 *            the kdf to set
	 */
	public void setKdf(byte[] kdf) {
		this.kdf = kdf;
	}

	/**
	 * @param nonce
	 *            the nonce to set
	 */
	public void setNonce(byte[] nonce) {
		this.nonce = nonce;
	}

	/**
	 * Updates the internal state of the UBI
	 * 
	 * @param ubi
	 * @param blockBuffer
	 * @param last
	 * @param first
	 * @param type
	 * @param data
	 * @param offset
	 * @param count
	 */
	private void update(byte[] blockBuffer, boolean last, boolean first, int type, byte[] data, int offset, int count) {

		final int available = data.length - offset;
		final int toblock = Math.min(blockBuffer.length, available);
		System.arraycopy(data, offset, blockBuffer, 0, toblock);

		// pad block itself (not the bits)
		if (toblock != blockBuffer.length) {
			for (int i = toblock; i < blockBuffer.length; i++) {
				blockBuffer[i] = 0;
			}
		}

		long[] blockWords = SkeinUtil.lsbBytesToArrayOfLong(blockBuffer);

		SkeinTweak tweak = new SkeinTweak(last, first, type, false, 0, count);

		// update UBI with configuration
		//System.out.println("full update: "+blockWords+" "+tweak.getT0()+" "+tweak.getT1());
		this.ubi.update(blockWords, new long[] { tweak.getT0(), tweak.getT1() });
	}

	/**
	 * Performs the actual Skein hash with the given input message
	 * 
	 * @param message
	 *            the data to hash
	 * 
	 * @return the digest result
	 */
	public byte[] doSkein(final byte[] message) {
		if (message == null) {
			throw new IllegalArgumentException("Please provide a message, even one of 0 bytes to process");
		}

		// create buffer
		byte[] blockBuffer = new byte[this.blockSizeBytes];

		// create cipher with UBI
		ThreefishImpl threefish = new ThreefishImpl(this.blockSize);
		this.ubi = new Ubi64(threefish);
		this.ubi.init();

		if (this.key != null) {
			update(blockBuffer, true, true, SkeinConstants.T_KEY, this.key, 0, this.key.length);
		}

		if (this.pers != null) {
			update(blockBuffer, true, true, SkeinConstants.T_PRS, this.pers, 0, this.pers.length);
		}

		if (this.pk != null) {
			update(blockBuffer, true, true, SkeinConstants.T_PK, this.pk, 0, this.pk.length);
		}

		if (this.kdf != null) {
			update(blockBuffer, true, true, SkeinConstants.T_KDF, this.kdf, 0, this.kdf.length);
		}

		if (this.nonce != null) {
			update(blockBuffer, true, true, SkeinConstants.T_NON, this.nonce, 0, this.nonce.length);
		}

		// create tweak for configuration
		// used configEncoding.length, but it seems the entire block should 
		// be in the tweak value (???) -> see question on site
		SkeinConfiguration config = new SkeinConfiguration(this.outputSize, 0, 0, 0);
		byte[] configEncoding = config.getEncoded();
		update(blockBuffer, true, true, SkeinConstants.T_CFG, configEncoding, 0, configEncoding.length);

		// padded automatically, block is still filled with 00h values
		// System.arraycopy(configEncoding, 0, blockBuffer, 0, configEncoding.length);

		// process message in blocks
		int bytesProcessed = 0;
		while (bytesProcessed < message.length) {
			int available = message.length - bytesProcessed;
			int toblock = Math.min(blockBuffer.length, available);
			//System.out.println("full tweak: "+(bytesProcessed+toblock)+" "+message.length);
			update(blockBuffer, bytesProcessed + toblock == message.length,
					bytesProcessed + toblock <= this.blockSizeBytes, SkeinConstants.T_MSG, message, bytesProcessed,
					bytesProcessed + toblock);
			bytesProcessed += toblock;
		}

		final int outputBlocks = (this.outputSize - 1) / this.blockSize + 1;

		// create a new set of longs of the same size (terrible hack, but whatever)
		long[] blockWords = SkeinUtil.lsbBytesToArrayOfLong(blockBuffer);
		long[] inputForOutput = new long[blockWords.length];
		for (int i = 0; i < outputBlocks; i++) {
			// create input for the OUTPUT function
			inputForOutput[0] = i;

			SkeinTweak tweak = new SkeinTweak(i == outputBlocks - 1, i == 0, SkeinConstants.T_OUT, false, 0, 8);
			this.ubi.update(inputForOutput, new long[] { tweak.getT0(), tweak.getT1() });
		}

		final long[] outputWords = this.ubi.getOutput();

		final byte[] output = SkeinUtil.lsbArrayOfLongToBytes(outputWords);

		return output;
	}
}
