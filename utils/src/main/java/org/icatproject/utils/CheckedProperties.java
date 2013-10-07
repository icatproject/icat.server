package org.icatproject.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

@SuppressWarnings("serial")
public class CheckedProperties extends Properties {

	public class CheckedPropertyException extends Exception {

		public CheckedPropertyException(String msg) {
			super(msg);
		}
	}

	private String fileName;
	
	private static FileSystem fileSystem = FileSystems.getDefault();

	public void loadFromFile(String fileName) throws CheckedPropertyException {
		InputStream fis = null;

		try {
			fis = new FileInputStream(fileName);
			load(fis);
			this.fileName = fileName;
		} catch (IOException e) {
			throw new CheckedPropertyException("Unable to load properties from " + fileName);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}
	}

	public void loadFromResource(String name) throws CheckedPropertyException {
		URL url = this.getClass().getClassLoader().getResource(name);
		if (url == null) {
			throw new CheckedPropertyException("Unable to locate resource " + name);
		}
		loadFromFile(url.getFile());
	}

	public String getString(String name) throws CheckedPropertyException {
		String value = getProperty(name);
		if (value == null) {
			throw new CheckedPropertyException(name + " is not defined in " + this.fileName);
		}
		return value;
	}

	public int getPositiveInt(String name) throws CheckedPropertyException {
		String value = getProperty(name);
		if (value == null) {
			throw new CheckedPropertyException(name + " is not defined in " + this.fileName);
		}
		try {
			int iValue = Integer.parseInt(value);
			if (iValue <= 0) {
				throw new CheckedPropertyException(name + " as defined in " + this.fileName
						+ " is not a representation of a positive integer");
			}
			return iValue;
		} catch (NumberFormatException e) {
			throw new CheckedPropertyException(name + " as defined in " + this.fileName
					+ " is not a representation of a positive integer");
		}
	}
	
	public int getNonNegativeInt(String name) throws CheckedPropertyException {
		String value = getProperty(name);
		if (value == null) {
			throw new CheckedPropertyException(name + " is not defined in " + this.fileName);
		}
		try {
			int iValue = Integer.parseInt(value);
			if (iValue < 0) {
				throw new CheckedPropertyException(name + " as defined in " + this.fileName
						+ " is not a representation of a non-negative integer");
			}
			return iValue;
		} catch (NumberFormatException e) {
			throw new CheckedPropertyException(name + " as defined in " + this.fileName
					+ " is not a representation of a non-negative integer");
		}
	}
	
	public double getDouble(String name) throws CheckedPropertyException {
		String value = getProperty(name);
		if (value == null) {
			throw new CheckedPropertyException(name + " is not defined in " + this.fileName);
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new CheckedPropertyException(name + " as defined in " + this.fileName
					+ " is not a representation of a floating point number");
		}
	}

	public URL getURL(String name) throws CheckedPropertyException {
		String value = getProperty(name);
		if (value == null) {
			throw new CheckedPropertyException(name + " is not defined in " + this.fileName);
		}
		try {
			return new URL(value);
		} catch (MalformedURLException e) {
			throw new CheckedPropertyException(name + " as defined in " + this.fileName
					+ " is not a representation of a URL");
		}
	}

	public boolean has(String name) {
		return getProperty(name) != null;
	}

	public File getFile(String name) throws CheckedPropertyException {
		String value = getProperty(name);
		if (value == null) {
			throw new CheckedPropertyException(name + " is not defined in " + this.fileName);
		}
		return new File(value);
	}
	
	public Path getPath(String name) throws CheckedPropertyException {
		String value = getProperty(name);
		if (value == null) {
			throw new CheckedPropertyException(name + " is not defined in " + this.fileName);
		}
		return fileSystem.getPath(value);
	}

	public long getPositiveLong(String name) throws CheckedPropertyException {
		String value = getProperty(name);
		if (value == null) {
			throw new CheckedPropertyException(name + " is not defined in " + this.fileName);
		}
		try {
			long lValue = Long.parseLong(value);
			if (lValue <= 0L) {
				throw new CheckedPropertyException(name + " as defined in " + this.fileName
						+ " is not a representation of a positive long");
			}
			return lValue;
		} catch (NumberFormatException e) {
			throw new CheckedPropertyException(name + " as defined in " + this.fileName
					+ " is not a representation of a positive long");
		}

	}

}
