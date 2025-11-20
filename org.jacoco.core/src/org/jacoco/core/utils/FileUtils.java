/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Stark X - initial API and implementation
 *******************************************************************************/
package org.jacoco.core.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class FileUtils {
	private FileUtils() {
	}

	public static List<File> getFiles(final File directory,
			final Collection<String> includes,
			final Collection<String> excludes, final boolean includeBaseDir)
			throws IOException {
		final List<String> includePatterns = new ArrayList<String>();
		if (includes != null) {
			for (String include : includes) {
				if (include != null) {
					includePatterns.add(convertToRegex(include));
				}
			}
		}
		final List<String> excludePatterns = new ArrayList<String>();
		if (excludes != null) {
			for (String exclude : excludes) {
				if (exclude != null) {
					excludePatterns.add(convertToRegex(exclude));
				}
			}
		}

		final List<File> files = new ArrayList<File>();
		collectFiles(directory, directory, files, includePatterns, excludePatterns, includeBaseDir);
		return files;
	}

	private static void collectFiles(final File basedir, final File currentDir,
			final List<File> result, final List<String> includes,
			final List<String> excludes, final boolean includeBaseDir)
			throws IOException {
		File[] children = currentDir.listFiles();
		if (children == null) {
			// directory is not readable or does not exist
			return;
		}
		
		for (File child : children) {
			if (child.isDirectory()) {
				collectFiles(basedir, child, result, includes, excludes, includeBaseDir);
			} else {
				String relativePath = getRelativePath(basedir, child);
				// Normalize path separators to forward slashes for pattern matching
				String normalizedPath = relativePath.replace('\\', '/');
				
				boolean isIncluded = false;
				for (String include : includes) {
					if (normalizedPath.matches(include)) {
						isIncluded = true;
						break;
					}
				}
				if (includes.isEmpty()) {
					isIncluded = true; // Default behavior: include all if no includes specified
				}
				
				if (isIncluded) {
					boolean isExcluded = false;
					for (String exclude : excludes) {
						if (normalizedPath.matches(exclude)) {
							isExcluded = true;
							break;
						}
					}
					
					if (!isExcluded) {
						if (includeBaseDir) {
							result.add(child);
						} else {
							result.add(child);
						}
					}
				}
			}
		}
	}

	/**
	 * Gets the relative path from one file to another.
	 */
	private static String getRelativePath(File baseDir, File file) {
		String basePath = normalizePath(baseDir);
		String filePath = normalizePath(file);
		
		if (filePath.startsWith(basePath)) {
			String relative = filePath.substring(basePath.length());
			if (relative.startsWith("/")) {
				relative = relative.substring(1);
			}
			return relative;
		}
		return file.getPath();
	}

	/**
	 * Normalizes file path by converting backslashes to forward slashes.
	 */
	private static String normalizePath(File file) {
		return file.getPath().replace('\\', '/');
	}

	/**
	 * Converts a glob pattern to a regex pattern.
	 * Supports simple glob patterns like **, *, ?
	 */
	private static String convertToRegex(String pattern) {
		// Normalize the pattern to use forward slashes
		pattern = pattern.replace('\\', '/');
		
		StringBuffer regex = new StringBuffer();
		regex.append('^');
		
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			switch (c) {
				case '*':
					if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
						// Handle ** - matches any number of directories
						if (i + 2 < pattern.length() && pattern.charAt(i + 2) == '/') {
							// Pattern like **/ matches any number of directories followed by /
							regex.append("(.*/)?");
							i += 2; // Skip the next two characters (**/)
						} else if (i == 0 && i + 1 == pattern.length()) {
							// Pattern is just ** - matches anything
							regex.append(".*");
							i++; // Skip the next *
						} else {
							// Pattern like **something - matches any number of directories followed by something
							regex.append(".*");
							i++; // Skip the next *
						}
					} else {
						// Handle * - matches any number of characters except /
						regex.append("[^/]*");
					}
					break;
				case '?':
					// Handle ? - matches single character except /
					regex.append("[^/]");
					break;
				case '.':
					// Escape . in regex
					regex.append("\\.");
					break;
				case '+':
				case '(':
				case ')':
				case '[':
				case ']':
				case '$':
				case '^':
				case '|':
				case '\\':
					// Escape regex special characters
					regex.append('\\').append(c);
					break;
				default:
					// Regular character
					regex.append(c);
					break;
			}
		}
		
		regex.append('$');
		return regex.toString();
	}

	public static List<File> getFiles(final File directory,
			final Collection<String> includes,
			final Collection<String> excludes) throws IOException {
		return getFiles(directory, includes, excludes, true);
	}

	public static List<String> getFileNames(File directory,
			final Collection<String> includes,
			final Collection<String> excludes, boolean includeBaseDir)
			throws IOException {
		final List<File> files = FileUtils.getFiles(directory, includes,
				excludes, includeBaseDir);
		final List<String> names = new ArrayList<String>();
		for (File file : files) {
			// Use normalized path for consistency
			String path = file.getPath().replace('\\', '/');
			if (!includeBaseDir) {
				// Get relative path from directory to file
				String dirPath = normalizePath(directory);
				if (path.startsWith(dirPath)) {
					path = path.substring(dirPath.length());
					if (path.startsWith("/")) {
						path = path.substring(1);
					}
				}
			}
			names.add(path);
		}
		return names;
	}

	public static List<String> getFileNames(File directory,
			final Collection<String> includes,
			final Collection<String> excludes) throws IOException {
		return getFileNames(directory, includes, excludes, true);
	}
}
