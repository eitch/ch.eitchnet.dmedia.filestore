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
package ch.eitchnet.utils.helper;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class BytesHelper {

	public static void main(String[] args) {

//		byte i = -128;
//		System.out.println((i >>> 7) & 1);
//		System.out.println((i >>> 6) & 1);
//		System.out.println((i >>> 5) & 1);
//		System.out.println((i >>> 4) & 1);
//		System.out.println((i >>> 3) & 1);
//		System.out.println((i >>> 2) & 1);
//		System.out.println((i >>> 1) & 1);
//		System.out.println((i >>> 0) & 1);
		
		byte c = 0;
		byte i = -128;
		byte test = -128;
		c &= ((i & test));
		System.out.println(c);
		test = (byte) (test>>> 1);
		System.out.println(((i & test) == test));
		test = (byte) (test>>> 1);
		System.out.println(((i & test) == test));
		test = (byte) (test>>> 1);
		System.out.println(((i & test) == test));
		test = (byte) (test>>> 1);
		System.out.println(((i & test) == test));
	}
}
