package org.icatproject.core.manager.search.queue;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RotatingFileQueue {

	private static final long MAX_SIZE = 10_000_000L;
	private static final Logger logger = LoggerFactory.getLogger(RotatingFileQueue.class);

	private final Path directory;
	private final String filename;
	private final Path writePath;

	private final Object writeLock = new Object();
	private final Object readLock = new Object();

	public RotatingFileQueue(Path directory, String filename) {
		this.directory = directory;
		this.filename = filename;
		this.writePath = directory.resolve(filename);
	}

	public void synchronizedWrite(String line) throws IcatException {

		logger.trace("Writing {} to {}", line, writePath);

		synchronized (writeLock) {
			try (BufferedWriter writer = Files.newBufferedWriter(writePath, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
				writer.write(line);
				writer.newLine();
			} catch (IOException e) {
				logger.error("Error writing to queue file: {}", writePath, e);
				throw new IcatException(IcatExceptionType.INTERNAL, "Error writing to queue file");
			}
		}
	}

	private boolean checkSize() {
		long size = 0L;
		try {
			size = Files.size(writePath);
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			logger.warn("Error checking queue file size: {}", writePath, e);
			// Ignore
		}
		if (size > MAX_SIZE) {
			logger.debug("Rotating queue file due to size {}: {}", size, writePath);
			return true;
		}

		return false;
	}

	private void rotate() throws IcatException {
		logger.debug("Rotating queue file: {}", writePath);

		synchronized (readLock) {
			// get current list of numbered files
			NavigableMap<Integer, Path> filemap = getNumberedFiles();

			while (!filemap.isEmpty()) {
				Map.Entry<Integer, Path> entry = filemap.pollLastEntry();

				Integer existingNumber = entry.getKey();
				Path existingPath = entry.getValue();

				Integer newNumber = existingNumber + 1;
				String newFilename = filename + "." + newNumber.toString();
				Path newPath = directory.resolve(newFilename);

				checkedMove(existingPath, newPath);
			}

			synchronized (writeLock) {
				Path existingPath = writePath;
				Path newPath = directory.resolve(filename + ".0");

				if (Files.exists(existingPath)) {
					checkedMove(existingPath, newPath);
				}
			}
		}
	}

	public Path getReadPath() throws IcatException {
		synchronized (readLock) {
			if (checkSize()) {
				rotate();
			}

			NavigableMap<Integer, Path> filemap = getNumberedFiles();

			while (!filemap.isEmpty()) {
				Map.Entry<Integer, Path> entry = filemap.pollLastEntry();

				Long size = null;
				try {
					size = Files.size(entry.getValue());
				} catch (IOException e) {
					logger.error("Error checking queue file size: {}", entry.getValue(), e);
					throw new IcatException(IcatExceptionType.INTERNAL, "Error checking queue file size");
				}

				if (size == 0L) {
					logger.debug("Deleting empty queue file: {}", entry.getValue());
					try {
						Files.delete(entry.getValue());
					} catch (IOException e) {
						logger.error("Error deleting queue file: {}", entry.getValue(), e);
						throw new IcatException(IcatExceptionType.INTERNAL, "Error deleting queue file");
					}
					continue;
				}

				return entry.getValue();
			}

			logger.debug("Rotating queue file due to exhaustion: {}", writePath);
			rotate();

			Map.Entry<Integer, Path> entry = getNumberedFiles().lastEntry();
			if (entry != null) {
				return entry.getValue();
			}
		}

		return null;
	}

	private void checkedMove(Path existingPath, Path newPath) throws IcatException {
		logger.debug("Moving {} -> {}", existingPath, newPath);

		try {
			Files.move(existingPath, newPath);
		} catch (IOException e) {
			logger.error("Error rotating queue file: {} -> {}", existingPath, newPath, e);
			throw new IcatException(IcatExceptionType.INTERNAL, "Error rotating queue file");
		}
	}

	private NavigableMap<Integer, Path> getNumberedFiles() throws IcatException {
		synchronized (readLock) {
			List<Path> files;
			try {
				files = Files.list(directory).collect(Collectors.toList());
			} catch (IOException e) {
				logger.error("Error reading directory: {}", directory, e);
				throw new IcatException(IcatExceptionType.INTERNAL, "Error reading queue file directory");
			}

			NavigableMap<Integer, Path> filemap = new TreeMap<>();

			for (Path file : files) {
				if (file.getFileName().toString().startsWith(filename + ".")) {
					String end = file.getFileName().toString().substring(filename.length() + 1);

					try {
						Integer n = Integer.valueOf(end);
						filemap.put(n, file);
					} catch (NumberFormatException e) {
						logger.warn("Ignoring file: {}", directory);
						// Ignore
					}
				}
			}

			return filemap;
		}
	}

	public Object getReadLock() {
		return readLock;
	}
}
