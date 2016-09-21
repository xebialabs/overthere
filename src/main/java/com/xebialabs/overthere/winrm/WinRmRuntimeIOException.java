/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.winrm;

import java.io.IOException;
import java.io.StringWriter;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.xebialabs.overthere.RuntimeIOException;

@SuppressWarnings("serial")
public class WinRmRuntimeIOException extends RuntimeIOException {

    final Document in;
    final Document out;

    public WinRmRuntimeIOException(String message, Document in, Document out, Throwable cause) {
        super(message, cause);
        this.in = in;
        this.out = out;
    }

    public WinRmRuntimeIOException(String message) {
        this(message, null, null, null);

    }

    public WinRmRuntimeIOException(String message, Throwable throwable) {
        this(message, null, null, throwable);
    }

    @Override
    public String getMessage() {
        if (in == null && out == null) {
            return super.getMessage();
        }
        return String.format("%s\nRequest:\n%s\nResponse:\n%s", super.getMessage(), toString(in), toString(out));
    }

    private static String toString(Document doc) {
        if (doc == null) {
            return "[EMPTY]";
        }

        StringWriter stringWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("error ", e);
        }
        return stringWriter.toString();
    }

}
