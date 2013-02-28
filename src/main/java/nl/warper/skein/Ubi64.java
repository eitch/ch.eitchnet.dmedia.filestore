/*
 * Copyright (c) 2008 - $Date: 2008/05/29 13:50:21 $, Sdu Identification BV
 * Classificatie: Commercieel vertrouwelijk
 *
 * File:     $rcsfile$
 * Date:     $Date: 2008/05/29 13:50:21 $
 * Version:  $Revision: 1.1 $
 */
package nl.warper.skein;

import nl.warper.threefish.ThreefishImpl;
import nl.warper.threefish.ThreefishSecretKey;

/**
 * Implementation of the UBI function that can be used to chain the three parts of the Skein hash algorithm together.
 * This is a strictly 64 bit version (long in Java) for performance reasons.
 * 
 * @since 5 nov 2008
 * @author maartenb
 */
public class Ubi64 {

	private final ThreefishImpl blockCipher;
	private long[] hi; // the intermediate result in bytes (maybe long)

	/**
	 * Constructs a Ubi64 instance, currently only with the Threefish block cipher. The idea is that any tweakable block
	 * cipher may be used, but this has not been implemented at the time of writing.
	 * 
	 * @param blockCipher
	 */
	public Ubi64(final ThreefishImpl blockCipher) {
		if (blockCipher == null) {
			throw new IllegalArgumentException("You need to supply an underlying blockcipher");
		}
		this.blockCipher = blockCipher;
	}

	/**
	 * Initialize the Ubi64 chaining method using the initial value. The value should be set to all zeros to initialize
	 * this instance for use with the Simple Skein hash algorithm. Warning: for performance reasons, the values of g
	 * will be directly used with the underlying block cipher, which may replace these values with other values.
	 * 
	 * @param g
	 *            the uncloned block to ubi
	 */
	public void init(final long[] g) {
		final int nb = this.blockCipher.getBlockSize() / Long.SIZE;
		if (g == null || g.length != nb) {
			throw new IllegalArgumentException("G input parameter must be an array of " + nb + " bytes");
		}
		this.hi = g; // set H{0, i}
	}

	/**
	 * Initializes Ubi64 with a newly created block with 0h values, as used for the simple Skein hash method
	 */
	public void init() {
		final int nb = this.blockCipher.getBlockSize() / Long.SIZE;
		this.hi = new long[nb]; // set H{0} to all zero's
	}

	public void update(long[] mi, long[] ti) {
		this.blockCipher.init(new ThreefishSecretKey(this.hi), ti);
		this.blockCipher.blockEncrypt(mi, this.hi);
		for (int i = 0; i < this.hi.length; i++) {
			this.hi[i] ^= mi[i];
		}
	}

	public long[] getOutput() {
		return this.hi.clone();
	}

	@Override
	public String toString() {
		return String.format(" --- UBI state ---%n") + SkeinUtil.tohex(this.hi);
	}
}
