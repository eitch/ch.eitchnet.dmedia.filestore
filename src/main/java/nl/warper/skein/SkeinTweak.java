/*
 * Copyright (c) 2008 - $Date: 2008/05/29 13:50:21 $, Sdu Identification BV
 * Classificatie: Commercieel vertrouwelijk
 *
 * File:     $rcsfile$
 * Date:     $Date: 2008/05/29 13:50:21 $
 * Version:  $Revision: 1.1 $
 */
package nl.warper.skein;

import java.math.BigInteger;

/**
 * Calculates the tweak values according to the various tweak fields. This class is for initial calculations only, it's
 * too slow for general calculation of tweak values. Warning: due to lsb mode you can easily confuse tweakHigh and
 * tweakLow. T[0] actually contains the position (0..2^64 at least) and T[1] the type etc.
 * 
 * @author maartenb
 * @since 5 nov 2008
 */
public class SkeinTweak {

	private final long t0;
	private final long t1;

	/**
	 * Convenience constructor that works for values in the range [0..2^64-1], encoded in the long parameter position as
	 * an unsigned value.
	 * 
	 * @param isFinal
	 *            indicates this is the tweak value of the final block to be processed
	 * @param isFirst
	 *            indicates this is the tweak value of the fist block to be processed
	 * @param type
	 *            the type contained in the block
	 * @param bitPadded
	 *            the last byte in the block uses the indicated bit padding (see class description)
	 * @param treeLevel
	 *            the level in the tree of this block
	 * @param position
	 *            the number of bytes processed so far including the unpadded bytes in this block
	 */
	public SkeinTweak(final boolean isFinal, final boolean isFirst, final int type, final boolean bitPadded,
			final int treeLevel, final long position) {

		if (position < 0) {
			throw new IllegalArgumentException("Position may not be negative");
		}

		long highTweak = 0;

		if (isFinal) {
			highTweak |= 1L << SkeinConstants.FINAL_LOCATION_IN_T1;
		}
		
		if (isFirst) {
			highTweak |= 1L << SkeinConstants.FIRST_LOCATION_IN_T1;
		}

		highTweak |= ((long) type) << SkeinConstants.TYPE_LOCATION_IN_T1;

		if (bitPadded) {
			highTweak |= 1L << SkeinConstants.BIT_PAD_LOCATION_IN_T1;
		}

		this.t1 = highTweak;
		this.t0 = position;
	}

	/**
	 * Convenience constructor that works for values in the range [0..2^96-1], encoded in the {@link BigInteger}
	 * parameter position as an unsigned value.
	 * 
	 * @param isFinal
	 *            indicates this is the tweak value of the final block to be processed
	 * @param isFirst
	 *            indicates this is the tweak value of the fist block to be processed
	 * @param type
	 *            the type contained in the block
	 * @param bitPadded
	 *            the last byte in the block uses the indicated bit padding (see class description)
	 * @param position
	 *            the number of bytes processed so far including the unpadded bytes in this block
	 */
	public SkeinTweak(final boolean isFinal, final boolean isFirst, final int type, final boolean bitPadded,
			final BigInteger position) {

		if (position.signum() == -1 || position.compareTo(SkeinConstants.MAXIMUM_POSITION_VALUE_PLUS_ONE) >= 0) {
			throw new IllegalArgumentException("Position must be in the range [0..2^96-1]");
		}

		long highTweak = 0;
		if (isFinal) {
			highTweak |= 1L << SkeinConstants.FINAL_LOCATION_IN_T1;
		}
		
		if (isFirst) {
			highTweak |= 1L << SkeinConstants.FIRST_LOCATION_IN_T1;
		}

		highTweak |= ((long) type) << SkeinConstants.TYPE_LOCATION_IN_T1;

		if (bitPadded) {
			highTweak |= 1L << SkeinConstants.BIT_PAD_LOCATION_IN_T1;
		}

		highTweak |= position.shiftRight(64).longValue();

		this.t1 = highTweak;
		this.t0 = position.longValue();
	}

	/**
	 * @param t0
	 * @param t1
	 */
	public SkeinTweak(final long t0, final long t1) {
		this.t0 = t0;
		this.t1 = t1;
	}

	/**
	 * Note that because of the LSB configuration of the hash method, the low tweak value must come to the left of the
	 * high tweak value (visualize the lowest byte containing the lowest part of the position completely to the left).
	 * 
	 * @return the lowest part of the tweak value
	 */
	public long getT0() {
		return this.t0;
	}

	/**
	 * Note that because of the LSB configuration of the hash method, the low tweak value must come to the left of the
	 * high tweak value (visualize the lowest byte containing the lowest part of the position completely to the left).
	 * 
	 * @return the highest part of the tweak value
	 */
	public long getT1() {
		return this.t1;
	}

	@Override
	public String toString() {
		return String.format("T0=%016X T1=%016X", this.t0, this.t1);
	}
}