/*
 * Copyright (c) 2008 - $Date: 2008/05/29 13:50:21 $, Sdu Identification BV
 * Classificatie: Commercieel vertrouwelijk
 *
 * File:     $rcsfile$
 * Date:     $Date: 2008/05/29 13:50:21 $
 * Version:  $Revision: 1.1 $
 */
package nl.warper.skein;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Skein configuration creator
 * 
 * @author maartenb
 * @since 5 nov 2008
 */
public class SkeinConfiguration {

	private final long outputLength;
	private final int treeLeafSize;
	private final int treeFanOut;
	private final int treeMaxHeight;

	/**
	 * @param outputLength
	 * @param treeLeafSize
	 * @param treeFanOut
	 * @param treeMaxHight
	 */
	public SkeinConfiguration(final long outputLength, final int treeLeafSize, final int treeFanOut,
			final int treeMaxHeight) {
		this.outputLength = outputLength;
		this.treeLeafSize = treeLeafSize;
		this.treeFanOut = treeFanOut;
		this.treeMaxHeight = treeMaxHeight;
	}

	/**
	 * @return
	 */
	public byte[] getEncoded() {
		return SkeinConfiguration.build(this.outputLength, this.treeLeafSize, this.treeFanOut, this.treeMaxHeight);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("Schema Identifier: SHA3 (static)%n"));
		sb.append(String.format("Schema version: 1.0 (static)%n"));
		sb.append(String.format("Output length (in bits): %d%n", this.outputLength));

		if (this.treeLeafSize == 0 && this.treeFanOut == 0 && this.treeMaxHeight == 0) {
			sb.append("Tree mode not used%n");
		} else {
			sb.append(String.format(
					"Tree mode used (tree leaf size = %d, tree fan out = %d and tree max height = %d%n",
					this.treeLeafSize, this.treeFanOut, this.treeMaxHeight));
		}

		return sb.toString();
	}

	/**
	 * Convenience method to quickly create a SkeinConfiguration
	 * 
	 * @param outputLength
	 * @param treeLeafSize
	 * @param treeFanOut
	 * @param treeMaxHeight
	 * @return
	 */
	public static byte[] build(final long outputLength, final int treeLeafSize, final int treeFanOut,
			final int treeMaxHeight) {

		// TODO rewrite by using simple byte array - length should always be the same

		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// schema identifier
			baos.write(new byte[] { 'S', 'H', 'A', '3' });

			// version number (in LSB!!!)
			baos.write(0x01);
			baos.write(0x00);

			// reserved
			baos.write(0x00);
			baos.write(0x00);

			// output length
			baos.write(SkeinUtil.lsbLongToBytes(outputLength));

			// output tree parts
			baos.write(treeLeafSize);
			baos.write(treeFanOut);
			baos.write(treeMaxHeight);

			// write additional 12 reserved bytes
			for (int i = 0; i < 13; i++) {
				baos.write(0x00);
			}

			return baos.toByteArray();

		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}