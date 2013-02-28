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
package nl.warper.skein;

import java.math.BigInteger;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class SkeinConstants {

	public static final byte[] SCHEMA = { 'S', 'H', 'A', '3' };

	/**
	 * Largest possible value + 1 for the position: Set to 2^96
	 */
	public static final BigInteger MAXIMUM_POSITION_VALUE_PLUS_ONE = BigInteger.valueOf(2).pow(96);

	public static final int FINAL_LOCATION_IN_T1 = 127 - Long.SIZE;
	public static final int FIRST_LOCATION_IN_T1 = 126 - Long.SIZE;
	public static final int TYPE_LOCATION_IN_T1 = 120 - Long.SIZE;
	public static final int BIT_PAD_LOCATION_IN_T1 = 119 - Long.SIZE;

	public static final int T_KEY = 0;
	public static final int T_CFG = 4;
	public static final int T_PRS = 8;
	public static final int T_PK = 12;
	public static final int T_KDF = 16;
	public static final int T_NON = 20;
	public static final int T_MSG = 48;
	public static final int T_OUT = 63;

}
