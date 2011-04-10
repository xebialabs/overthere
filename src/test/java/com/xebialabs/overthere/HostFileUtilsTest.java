/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

public class HostFileUtilsTest {

	// private String type1;
	// private ConnectionOptions host1;
	//
	// private String type2;
	// private ConnectionOptions host2;
	//
	// @Before
	// public void setUpHosts() {
	// type1 = "ssh_scp";
	// host1 = new ConnectionOptions();
	// host1.set("address", "overthere");
	// host1.set("username", "root");
	// host1.set("password", "centos");
	// host1.set("os", OperatingSystemFamily.UNIX);
	//
	// type2 = "ssh_sftp";
	// host2 = new ConnectionOptions();
	// host2.set("address", "overthere");
	// host2.set("username", "trusted");
	// host2.set("password", "trustme");
	// host2.set("os", OperatingSystemFamily.UNIX);
	// }

	@Test
	public void copyOfFileToDirectoryFails() {

		OverthereFile regularFile = Mockito.mock(OverthereFile.class);
		Mockito.when(regularFile.exists()).thenReturn(true);
		Mockito.when(regularFile.isDirectory()).thenReturn(false);

		OverthereFile directory = Mockito.mock(OverthereFile.class);
		Mockito.when(directory.exists()).thenReturn(true);
		Mockito.when(directory.isDirectory()).thenReturn(true);

		try {
			HostFileUtils.copy(regularFile, directory);
			fail();
		} catch (RuntimeIOException expected) {
		}

		try {
			HostFileUtils.copyFile(regularFile, directory);
			fail();
		} catch (RuntimeIOException expected) {

		}
	}

	@Test
	public void testCopyDirIsRecursive() {
		ConnectionOptions localOptions = new ConnectionOptions();
		localOptions.set("os", UNIX);
		HostConnection s = Overthere.getConnection("local", localOptions);
		try {
			File srcDirFile = File.createTempFile("srcdir", null);
			OverthereFile srcDir = s.getFile(srcDirFile.getPath());
			String javaTempDirName = srcDir.getParent();

			String mySrcTempDirName = javaTempDirName + File.separator + "srcTempDir";
			srcDirFile = new File(mySrcTempDirName);
			srcDir = s.getFile(srcDirFile.getPath());
			srcDir.mkdirs();

			String fileAtTopLevel = mySrcTempDirName + File.separator + "fileAtTopLevel.txt";
			File fileAtTopLevelFile = new File(fileAtTopLevel);
			PrintWriter pw = new PrintWriter(new FileWriter(fileAtTopLevelFile));
			pw.println("I am the content of the top level file");
			pw.close();

			String mySrcSubTempDirName = mySrcTempDirName + File.separator + "srcSubTempDir";
			File srcSubDirFile = new File(mySrcSubTempDirName);
			OverthereFile srcSubDir = s.getFile(srcSubDirFile.getPath());
			srcSubDir.mkdirs();

			String fileAtFirstSubLevel = mySrcSubTempDirName + File.separator + "fileAtFirstSubLevel.txt";
			File fileAtFirstSubLevelFile = new File(fileAtFirstSubLevel);
			pw = new PrintWriter(new FileWriter(fileAtFirstSubLevelFile));
			pw.println("I am the content of the first sub level file");
			pw.close();

			String mySecondSrcSubTempDirName = mySrcTempDirName + File.separator + "srcSecondSubTempDir";
			File srcSecondSubDirFile = new File(mySecondSrcSubTempDirName);
			OverthereFile srcSecondSubDir = s.getFile(srcSecondSubDirFile.getPath());
			srcSecondSubDir.mkdirs();

			String fileAtSecondSubLevel = mySecondSrcSubTempDirName + File.separator + "fileAtSecondSubLevel.txt";
			File fileAtSecondSubLevelFile = new File(fileAtSecondSubLevel);
			pw = new PrintWriter(new FileWriter(fileAtSecondSubLevelFile));
			pw.println("I am the content of the second sub level file");
			pw.close();

			OverthereFile destDir = s.getFile(new File(javaTempDirName + File.separator + "destdir").getPath());

			assertFalse(destDir.exists());

			HostFileUtils.copyDirectory(srcDir, destDir);

			// now inspect destDir
			assertTrue(destDir.exists());
			assertTrue(destDir.isDirectory());

			List<OverthereFile> filesInDestDir = destDir.listFiles();
			int nFiles = countFiles(filesInDestDir);
			assertEquals(1, nFiles);
			int nDirs = countDirs(filesInDestDir);
			assertEquals(2, nDirs);

			List<OverthereFile> subDirsInDestDir = getDirs(filesInDestDir);

			for (OverthereFile subDir : subDirsInDestDir) {
				assertTrue(subDir.exists());
				assertTrue(subDir.isDirectory());
				filesInDestDir = subDir.listFiles();
				nFiles = countFiles(filesInDestDir);
				assertEquals(1, nFiles);
				nDirs = countDirs(filesInDestDir);
				assertEquals(0, nDirs);
			}

			// cleanup
			if (destDir.exists()) {
				destDir.deleteRecursively();
			}

			assertFalse(destDir.exists());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			s.disconnect();
		}
	}

	private int countFiles(List<OverthereFile> files) {
		return files.size() - countDirs(files);
	}

	private int countDirs(List<OverthereFile> files) {
		int cnt = 0;
		for (OverthereFile f : files) {
			if (f.isDirectory()) {
				cnt++;
			}
		}
		return cnt;
	}

	private List<OverthereFile> getDirs(List<OverthereFile> files) {
		List<OverthereFile> dirs = new ArrayList<OverthereFile>();
		for (OverthereFile f : files) {
			if (f.isDirectory()) {
				dirs.add(f);
			}
		}
		return dirs;
	}

	// FIXME: Find a better way to write these test
	// @Test
	// @Ignore
	// public void testCopyToSameMachine() {
	// HostConnection s = Overthere.getConnection(type1, host1);
	// try {
	// HostFile h1 = s.getFile("/tmp/input.txt");
	// HostFile h2 = s.getFile("/root/loopback/mnt/output.txt");
	// HostFileUtils.copy(h1, h2);
	// } finally {
	// s.disconnect();
	// }
	// }
	//
	// @Test
	// @Ignore
	// public void testCopyToFullDisk() {
	// Host h = new Host();
	// // h.setAddress("was-51");
	// h.setAddress("apache-22");
	// h.setUsername("root");
	// h.setPassword("centos");
	// // h.setTemporaryDirectoryLocation("/root/loopback/mnt");
	// h.setAccessMethod(HostAccessMethod.SSH_SCP);
	// h.setOperatingSystemFamily(OperatingSystemFamily.UNIX);
	//
	// HostConnection s = HostSessionFactory.getHostSession(h);
	// try {
	// try {
	// s.copyToTemporaryFile(new ClassPathResource("web/help/settingUpAnEnvironment.mp4"));
	// fail("No exception thrown when writing to full disk");
	// } catch (Exception exc) {
	// exc.printStackTrace();
	// }
	// } finally {
	// s.disconnect();
	// }
	// }
	//
	// @Test
	// @Ignore
	// public void testTemporaryDirectory() {
	// HostConnection s = HostSessionFactory.getHostSession(host1);
	// try {
	// HostFile tmp1 = s.getTempFile("bla.txt");
	// HostFileUtils.putStringToHostFile("blargh1", tmp1);
	// HostFile tmp2 = s.getTempFile("bla", ".tmp");
	// HostFileUtils.putStringToHostFile("blargh2", tmp2);
	// HostFile tmp3 = s.getTempFile("bla", ".tmp");
	// HostFileUtils.putStringToHostFile("blargh2", tmp3);
	// } finally {
	// s.disconnect();
	// }
	// }
}

