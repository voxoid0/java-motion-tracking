/******************************************************************************
* Copyright (c) 2008-2010 Joel Becker. All Rights Reserved.
* http://tech.joelbecker.net
*
*    This is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License
*    version 3, as published by the Free Software Foundation.
*
*    This is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public
*    License along with this source file; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
******************************************************************************/
package net.joelbecker.vision.blob.processor;

import java.util.List;

import net.joelbecker.vision.blob.Blob;

/**
 * Interface for an object that processes a list of blobs.
 * @author Joel R. Becker
 * <br>8/10/09
 */
public interface BlobProcessor {
	/**
	 * Processes a list of blobs, possibly adding to and removing from the list.
	 * @param input The input list of {@link Blob}s.
	 * @return The processed list; may be the same list given as input, but modified.
	 */
	List<Blob> process(List<Blob> input);
}
