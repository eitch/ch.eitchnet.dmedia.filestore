/*
 * Copyright (c) 2012, Robert von Burg
 *
 * All rights reserved.
 *
 * This file is part of the nl.warper.skein.
 *
 *  nl.warper.skein is free software: you can redistribute 
 *  it and/or modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation, either version 3 of the License, 
 *  or (at your option) any later version.
 *
 *  nl.warper.skein is distributed in the hope that it will 
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nl.warper.skein.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package nl.warper.skein;

/**
 * Exception classed used when problems are encountered while performing a Skein hash
 * 
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public class SkeinException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 * @param cause
	 */
	public SkeinException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public SkeinException(String message) {
		super(message);
	}
}
