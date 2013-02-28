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
 * Skein implementation constants
 * 
 * @author maartenb - original author
 * @author Robert von Burg <eitch@eitchnet.ch> - extracted to its own classes
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
