package nl.warper.threefish;

import static nl.warper.skein.SkeinUtil.lsbBytesToArrayOfLong;

/**
 * @since 4 nov 2008
 * @author maartenb
 */
public class ThreefishSecretKey {

	private final long[] keyWords;

	/**
	 * @param keyWords
	 */
	public ThreefishSecretKey(final long[] keyWords) {
		this.keyWords = keyWords.clone();
	}

	/**
	 * Temporary public constructor that builds a TreeFish compatible secret key. To be replaced by a factory.
	 * 
	 * @param keyBytes
	 */
	public ThreefishSecretKey(final byte[] keyBytes) {
		if (keyBytes == null) {
			throw new IllegalArgumentException("Please supply key data to this constructor (null provided)");
		}

		final int keySizeBits = keyBytes.length * Byte.SIZE;
		switch (keySizeBits) {
		case ThreefishImpl.BLOCK_SIZE_BITS_256:
		case ThreefishImpl.BLOCK_SIZE_BITS_512:
		case ThreefishImpl.BLOCK_SIZE_BITS_1024:
			this.keyWords = lsbBytesToArrayOfLong(keyBytes);
			break;
		default:
			throw new IllegalArgumentException("Key data size is invalid, it should be"
					+ " either 256, 512 or 1024 bits long");
		}
	}

	/**
	 * @param keyWordsBuffer
	 */
	public void getKeyWords(final long[] keyWordsBuffer) {
		for (int i = 0; i < this.keyWords.length; i++) {
			keyWordsBuffer[i] = this.keyWords[i];
		}
	}

	/**
	 * @return
	 */
	public int getKeySizeInWords() {
		return this.keyWords.length;
	}
}
