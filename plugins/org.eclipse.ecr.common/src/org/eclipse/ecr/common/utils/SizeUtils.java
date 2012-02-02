/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.eclipse.ecr.common.utils;

/**
 * Class containing helpers related to the expression of sizes in bytes,
 * kilobytes, etc.
 */
public class SizeUtils {

    private SizeUtils() {
        // utility class
    }

    public static final long KB = 1024;

    public static final long MB = KB * KB;

    public static final long GB = MB * KB;

    public static final long TB = GB * KB;

    /**
     * Returns the number of bytes corresponding to a string expressing a size.
     * <p>
     * The suffixes KB, MB, GB, TB are recognized (in any case).
     *
     * @param string the size as a string
     * @return the size
     * @throws NumberFormatException if the size cannot be parsed
     */
    public static long parseSizeInBytes(String string)
            throws NumberFormatException {
        String digits = string;
        if (digits.length() == 0) {
            throw new NumberFormatException("Invalid empty string");
        }
        char unit = digits.charAt(digits.length() - 1);
        if (unit == 'b' || unit == 'B') {
            digits = digits.substring(0, digits.length() - 1);
            if (digits.length() == 0) {
                throw new NumberFormatException(string);
            }
            unit = digits.charAt(digits.length() - 1);
        }
        long mul;
        switch (unit) {
        case 'k':
        case 'K':
            mul = KB;
            break;
        case 'm':
        case 'M':
            mul = MB;
            break;
        case 'g':
        case 'G':
            mul = GB;
            break;
        case 't':
        case 'T':
            mul = TB;
            break;
        default:
            if (!Character.isDigit(unit)) {
                throw new NumberFormatException(string);
            }
            mul = 1;
        }
        if (mul != 1) {
            digits = digits.substring(0, digits.length() - 1);
            if (digits.length() == 0) {
                throw new NumberFormatException(string);
            }
        }
        try {
            return Long.parseLong(digits.trim()) * mul;
        } catch (NumberFormatException e) {
            throw new NumberFormatException(string);
        }
    }

}
