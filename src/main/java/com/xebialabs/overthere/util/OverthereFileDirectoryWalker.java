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
package com.xebialabs.overthere.util;

import java.io.IOException;
import java.util.List;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Abstract class that walks through a directory hierarchy and provides subclasses with convenient hooks to add specific
 * behaviour.
 * <p/>
 * This class operates with a maximum depth to limit the files and direcories visited.
 * <p/>
 * The following sections describe:
 * <ul>
 * <li><a href="#example">1. Example Implementation</a> - example <code>FileCleaner</code> implementation.</li>
 * </ul>
 * <p/>
 * <a name="example"></a>
 * <h3>1. Example Implementation</h3>
 * <p/>
 * There are many possible extensions, for example, to delete all files and '.svn' directories
 * <p/>
 * <pre>
 * public class FileCleaner extends OverthereFileDirectoryWalker {
 *     private List&lt;OverthereFile&gt; results = new ArrayList&lt;OverthereFile&gt;();
 *
 *     public FileCleaner() {
 *         super();
 *     }
 *
 *     public List clean(OverthereFile startDirectory) {
 *         results.clear();
 *         walk(startDirectory, results);
 *         return results;
 *     }
 *
 *     protected boolean handleDirectory(OverthereFile directory, int depth) {
 *         // delete svn directories and then skip
 *         if (&quot;.svn&quot;.equals(directory.getName())) {
 *             directory.delete();
 *             return false;
 *         } else {
 *             return true;
 *         }
 *
 *     }
 *
 *     protected void handleFile(OverthereFile file, int depth) {
 *         // delete file and add to list of deleted
 *         file.delete();
 *         results.add(file);
 *     }
 * }
 * </pre>
 * <p/>
 * Adapted from the DirectoryWalker class in Common IO.
 * <p/>
 * FIXME: Move to its proper place
 */
public abstract class OverthereFileDirectoryWalker {

    /**
     * The directory level representing the starting directory = 0
     */
    public static final int ROOT = 0;

    /**
     * The limit on the directory depth to walk.
     */
    private final int depthLimit;

    /**
     * Construct an instance with unlimited <i>depth</i>.
     */
    protected OverthereFileDirectoryWalker() {
        this(-1);
    }

    /**
     * Construct an instance with limit the <i>depth</i> navigated to.
     * <p/>
     *
     * @param depthLimit controls how <i>deep</i> the hierarchy is navigated to (less than 0 means unlimited)
     */
    protected OverthereFileDirectoryWalker(int depthLimit) {
        this.depthLimit = depthLimit;
    }

    /**
     * Internal method that walks the directory hierarchy in a depth-first manner.
     * <p/>
     * Users of this class do not need to call this method. This method will be called automatically by another (public)
     * method on the specific subclass.
     * <p/>
     * Writers of subclasses should call this method to start the directory walk. Once called, this method will emit
     * events as it walks the hierarchy. The event methods have the prefix <code>handle</code>.
     *
     * @param startDirectory the directory to start from, not null
     * @throws NullPointerException if the start directory is null
     * @throws RuntimeIOException   if an I/O Error occurs
     */
    protected final void walk(OverthereFile startDirectory) throws RuntimeIOException {
        try {
            if (startDirectory == null) {
                throw new NullPointerException("Start Directory is null");
            }
            handleStart(startDirectory);
            walk(startDirectory, 0);
            handleEnd();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }

    }

    /**
     * Main recursive method to examine the directory hierarchy.
     *
     * @param directory the directory to examine, not null
     * @param depth     the directory level (starting directory = 0)
     * @throws IOException if an I/O Error occurs
     */
    private void walk(OverthereFile directory, int depth) throws IOException {
        if (handleDirectory(directory, depth)) {
            handleDirectoryStart(directory, depth);
            int childDepth = depth + 1;
            if (depthLimit < 0 || childDepth <= depthLimit) {
                List<OverthereFile> childFiles = listFiles(directory);
                if (childFiles == null) {
                    handleRestricted(directory, childDepth);
                } else {
                    for (OverthereFile childFile : childFiles) {
                        if (childFile.isDirectory()) {
                            walk(childFile, childDepth);
                        } else {
                            handleFile(childFile, childDepth);
                        }
                    }
                }
            }
            handleDirectoryEnd(directory, depth);
        }

    }

    /**
     * Lists the files in the directory.
     *
     * @param directory in which to list files.
     * @return all the files in the directory as filtering.
     */
    protected List<OverthereFile> listFiles(OverthereFile directory) {
        return directory.listFiles();
    }

    /**
     * Overridable callback method invoked at the start of processing.
     * <p/>
     * This implementation does nothing.
     *
     * @param startDirectory the directory to start from
     * @throws IOException if an I/O Error occurs
     */
    protected void handleStart(OverthereFile startDirectory) throws IOException {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked to determine if a directory should be processed.
     * <p/>
     * This method returns a boolean to indicate if the directory should be examined or not. If you return false, the
     * entire directory and any subdirectories will be skipped.
     * <p/>
     * This implementation does nothing and returns true.
     *
     * @param directory the current directory being processed
     * @param depth     the current directory level (starting directory = 0)
     * @return true to process this directory, false to skip this directory
     * @throws IOException if an I/O Error occurs
     */
    protected boolean handleDirectory(OverthereFile directory, int depth) throws IOException {
        // do nothing - overridable by subclass
        return true; // process directory
    }

    /**
     * Overridable callback method invoked at the start of processing each directory.
     * <p/>
     * This implementation does nothing.
     *
     * @param directory the current directory being processed
     * @param depth     the current directory level (starting directory = 0)
     * @throws IOException if an I/O Error occurs
     */
    protected void handleDirectoryStart(OverthereFile directory, int depth) throws IOException {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked for each (non-directory) file.
     * <p/>
     * This implementation does nothing.
     *
     * @param file  the current file being processed
     * @param depth the current directory level (starting directory = 0)
     * @throws IOException if an I/O Error occurs
     */
    protected void handleFile(OverthereFile file, int depth) throws IOException {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked for each restricted directory.
     * <p/>
     * This implementation does nothing.
     *
     * @param directory the restricted directory
     * @param depth     the current directory level (starting directory = 0)
     * @throws IOException if an I/O Error occurs
     */
    protected void handleRestricted(OverthereFile directory, int depth) throws IOException {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked at the end of processing each directory.
     * <p/>
     * This implementation does nothing.
     *
     * @param directory the directory being processed
     * @param depth     the current directory level (starting directory = 0)
     * @throws IOException if an I/O Error occurs
     */
    protected void handleDirectoryEnd(OverthereFile directory, int depth) throws IOException {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked at the end of processing.
     * <p/>
     * This implementation does nothing.
     *
     * @throws IOException if an I/O Error occurs
     */
    protected void handleEnd() throws IOException {
        // do nothing - overridable by subclass
    }

}
