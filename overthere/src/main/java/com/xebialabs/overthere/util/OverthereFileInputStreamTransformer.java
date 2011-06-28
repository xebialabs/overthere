/*
 * Copyright (c) 2008-2011 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.overthere.util;

import java.io.FilterInputStream;
import java.io.InputStream;

import com.xebialabs.overthere.OverthereFile;

/**
 * FIXME: Move to its proper place.
 */
public interface OverthereFileInputStreamTransformer {
	
	/**
	 * Transforms a source {@link OverthereFile}
	 * 
	 * Can transform paths or contents of the file.
	 * 
	 * @param input OverthereFile
	 * @return the transformed InputStream 
	 */
	TransformedInputStream transform(OverthereFile input);


    public static final class TransformedInputStream extends FilterInputStream {

        private long length;

        /**
         * Creates a <code>TransformedInputStream</code> with associated length of
         * the wrapped stream.
         *
         * @param in the underlying input stream, or <code>null</code> if
         *           this instance is to be created without an underlying stream.
         * @param length the underlying input stream
         */
        public TransformedInputStream(InputStream in, long length) {
            super(in);
            this.length = length;
        }

        /**
         * @return length the underlying input stream
         */
        public long length() {
            return length;
        }
    }

}
