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
package ch.eitchnet.dmedia.filestore.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.warper.skein.Skein;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eitchnet.utils.helper.FileHelper;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class FileStore {

	private static final Logger logger = LoggerFactory.getLogger(FileStore.class);

	private final int blockSize;
	private final int digestSize;
	private final boolean withKey;
	private final boolean withPers;

	/**
	 * 
	 */
	public FileStore() {
		this(FileStoreConstants.BLOCK_BITS, FileStoreConstants.DIGEST_BITS, true, true);
	}

	/**
	 * @param blockSize
	 * @param digestSize
	 * @param withKey
	 * @param withPers
	 */
	public FileStore(int blockSize, int digestSize, boolean withKey, boolean withPers) {
		this.blockSize = blockSize;
		this.digestSize = digestSize;
		this.withKey = withKey;
		this.withPers = withPers;
	}

	public DmediaFile hashAndStoreFile(File file) {

		validateFile(file);

		List<DmediaFileSlice> fileSlices = hashLeaves(file);
		DmediaFile dmediaFile = hashRoot(file, fileSlices);

		return dmediaFile;
	}

	/**
	 * @param file
	 * @param fileSlices
	 * @return
	 */
	public DmediaFile hashRoot(File file, List<DmediaFileSlice> fileSlices) {

		StringBuilder sb = new StringBuilder();
		for (DmediaFileSlice dmediaFileSlice : fileSlices) {
			sb.append(dmediaFileSlice.getHash());
		}

		String leafHashes = sb.toString();
		long fileSize = file.length();

		String rootHash = new String(hashRoot(fileSize, leafHashes.getBytes()));

		DmediaFile dmediaFile = new DmediaFile(file.getAbsolutePath(), fileSize, rootHash, fileSlices);
		return dmediaFile;
	}

	/**
	 * @param file
	 */
	public void validateFile(File file) {

		if (!file.exists())
			throw new FileStoreException("The file does not exist at " + file.getAbsolutePath());

		long fileSize = file.length();
		if (fileSize == FileStoreConstants.MAX_FILE_SIZE) {
			String msg = "The file size %s is larger than the allowed size %s for file %s";
			msg = String.format(msg, FileHelper.humanizeFileSize(fileSize),
					FileHelper.humanizeFileSize(FileStoreConstants.MAX_FILE_SIZE), file.getAbsolutePath());
			throw new FileStoreException(msg);
		}
	}

	public int getNrOfSlices(File file) {

		long fileSize = file.length();

		// get number of slices. We can truncate to int as we won't have more than
		// FileStoreConstants.MAX_LEAF_COUNT leaves
		long nrOfSlices = fileSize <= FileStoreConstants.LEAF_SIZE ? 1 : (fileSize / FileStoreConstants.LEAF_SIZE + 1);
		if (nrOfSlices > FileStoreConstants.MAX_LEAF_COUNT)
			throw new FileStoreException("There are too many leaves!"); // shouldn't happen

		return (int) nrOfSlices;
	}

	/**
	 * @param file
	 * @return
	 */
	public List<DmediaFileSlice> hashLeaves(File file) {

		long fileSize = file.length();
		int nrOfSlices = getNrOfSlices(file);
		logger.info(String.format("Number of slices: %s, size %s", FileHelper.humanizeFileSize(nrOfSlices),
				FileHelper.humanizeFileSize(file.length())));

		List<DmediaFileSlice> fileSlices = new ArrayList<>(nrOfSlices);

		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {

			int leafIndex = 0;
			int read;
			int offset = 0;
			byte[] bytes = new byte[FileStoreConstants.LEAF_SIZE];
			int length = fileSize < FileStoreConstants.LEAF_SIZE ? (int) fileSize : FileStoreConstants.LEAF_SIZE;

			while ((read = in.read(bytes, 0, length)) != -1) {

				logger.info("Read " + read);
				byte[] leafData = new byte[read];
				System.arraycopy(bytes, 0, leafData, 0, read);
				String leafHash = hashLeafToString(leafIndex, leafData);

				DmediaFileSlice fileSlice = new DmediaFileSlice(leafIndex, offset, leafHash);
				fileSlices.add(fileSlice);

				leafIndex++;
				offset += read;
			}

		} catch (FileNotFoundException e) {
			throw new FileStoreException("The file does not exist at " + file.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new FileStoreException("Failed to read file at " + file.getAbsolutePath(), e);
		} catch (Exception e) {
			String msg = String.format("Failed to hash file %s due to internal error", file.getAbsoluteFile());
			throw new FileStoreException(msg, e);
		}

		return fileSlices;
	}

	/**
	 * @param leafIndex
	 * @param leafData
	 * @return
	 */
	public String hashLeafToString(int leafIndex, byte[] leafData) {
		return new String(hashLeaf(leafIndex, leafData));
	}

	/**
	 * @param leafIndex
	 * @param leafData
	 * @return
	 */
	public byte[] hashLeaf(int leafIndex, byte[] leafData) {

		if (leafIndex < 0 || leafIndex >= FileStoreConstants.MAX_LEAF_COUNT) {
			String msg = String.format("The leafIndex %d is not in the allowed range 0 - %d", leafIndex,
					FileStoreConstants.MAX_LEAF_COUNT);
			throw new FileStoreException(msg);
		}
		if (leafData.length > FileStoreConstants.LEAF_SIZE) {
			String msg = String.format("The leafData length %d is larger than the allowed %s", leafData.length,
					FileStoreConstants.LEAF_SIZE);
			throw new FileStoreException(msg);
		}

		Skein skein = new Skein(this.blockSize, this.digestSize);
		if (this.withKey)
			skein.setKey(Integer.valueOf(leafIndex).toString().getBytes());
		if (this.withPers)
			skein.setPersonalization(FileStoreConstants.PERS_LEAF.getBytes());
		byte[] digest = skein.doSkein(leafData);

		return Dbase32.db32Enc(digest);
	}

	/**
	 * @param fileSize
	 * @param leafHashes
	 * @return
	 */
	public String hashRootToString(long fileSize, byte[] leafHashes) {
		return new String(hashRoot(fileSize, leafHashes));
	}

	/**
	 * @param fileSize
	 * @param leafHashes
	 * @return
	 */
	public byte[] hashRoot(long fileSize, byte[] leafHashes) {

		if (fileSize < 1 || fileSize > FileStoreConstants.MAX_FILE_SIZE) {
			String msg = String.format("The fileSize %d is not in the allowed range 1 - %d", fileSize,
					FileStoreConstants.MAX_FILE_SIZE);
			throw new FileStoreException(msg);
		}
		if (leafHashes.length < FileStoreConstants.DIGEST_B32LEN) {
			String msg = String.format("The leafHashes length %d is not at least one hash long (%d)",
					leafHashes.length, FileStoreConstants.DIGEST_B32LEN);
			throw new FileStoreException(msg);
		}
		if (leafHashes.length % FileStoreConstants.DIGEST_B32LEN != 0) {
			String msg = String.format("The leafHashes length %d is not a multiple of %d", leafHashes.length,
					FileStoreConstants.DIGEST_B32LEN);
			throw new FileStoreException(msg);
		}

		int count = leafHashes.length / FileStoreConstants.DIGEST_B32LEN;
		int low = (count - 1) * FileStoreConstants.LEAF_SIZE + 1;
		int high = count * FileStoreConstants.LEAF_SIZE;
		if (fileSize < low || fileSize > high) {
			String msg = "The fileSize %d and the leafHash size %d do not fit together as calculated low is %d and high is %d";
			msg = String.format(msg, fileSize, leafHashes.length, low, high);
			throw new FileStoreException(msg);
		}

		Skein skein = new Skein(this.blockSize, this.digestSize);
		if (this.withKey)
			skein.setKey(Long.valueOf(fileSize).toString().getBytes());
		if (this.withPers)
			skein.setPersonalization(FileStoreConstants.PERS_ROOT.getBytes());
		byte[] digest = skein.doSkein(leafHashes);

		return Dbase32.db32Enc(digest);
	}
}
