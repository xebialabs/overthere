/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
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

package com.xebialabs.overthere;

import com.google.common.collect.Maps;
import com.xebialabs.deployit.ci.Host;
import com.xebialabs.deployit.ci.HostAccessMethod;
import com.xebialabs.deployit.ci.OperatingSystemFamily;
import com.xebialabs.deployit.ci.artifact.EjbJar;
import com.xebialabs.deployit.ci.artifact.Libraries;
import com.xebialabs.deployit.ci.artifact.War;
import com.xebialabs.deployit.ci.artifact.mapping.PlaceholderFormat;
import com.xebialabs.deployit.exception.RuntimeIOException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.*;

public class HostFileUtilsTest {

	private Host host1;

	private Host host2;

	@Before
	public void setUpHost1() {
		host1 = new Host();
		host1.setAddress("apache-22");
		host1.setUsername("root");
		host1.setPassword("centos");
		host1.setAccessMethod(HostAccessMethod.SSH_SCP);
		host1.setOperatingSystemFamily(OperatingSystemFamily.UNIX);
	}

	@Before
	public void setUpHost2() {
		host2 = new Host();
		host2.setAddress("was-51");
		host2.setUsername("root");
		host2.setPassword("centos");
		host2.setAccessMethod(HostAccessMethod.SSH_SFTP);
		host2.setOperatingSystemFamily(OperatingSystemFamily.UNIX);
	}

	@Test
	public void copyOfFileToDirectoryFails() {

		HostFile regularFile = createNiceMock(HostFile.class);
		expect(regularFile.exists()).andReturn(true);
		expect(regularFile.isDirectory()).andReturn(false);

		HostFile directory = createNiceMock(HostFile.class);
		expect(directory.exists()).andReturn(true);
		expect(directory.isDirectory()).andReturn(true);

		replay(regularFile, directory);

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
		HostSession s = Host.getLocalHost().getHostSession();
		try {
			File srcDirFile = File.createTempFile("srcdir", null);
			HostFile srcDir = s.getFile(srcDirFile.getPath());
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
			HostFile srcSubDir = s.getFile(srcSubDirFile.getPath());
			srcSubDir.mkdirs();

			String fileAtFirstSubLevel = mySrcSubTempDirName + File.separator + "fileAtFirstSubLevel.txt";
			File fileAtFirstSubLevelFile = new File(fileAtFirstSubLevel);
			pw = new PrintWriter(new FileWriter(fileAtFirstSubLevelFile));
			pw.println("I am the content of the first sub level file");
			pw.close();

			String mySecondSrcSubTempDirName = mySrcTempDirName + File.separator + "srcSecondSubTempDir";
			File srcSecondSubDirFile = new File(mySecondSrcSubTempDirName);
			HostFile srcSecondSubDir = s.getFile(srcSecondSubDirFile.getPath());
			srcSecondSubDir.mkdirs();

			String fileAtSecondSubLevel = mySecondSrcSubTempDirName + File.separator + "fileAtSecondSubLevel.txt";
			File fileAtSecondSubLevelFile = new File(fileAtSecondSubLevel);
			pw = new PrintWriter(new FileWriter(fileAtSecondSubLevelFile));
			pw.println("I am the content of the second sub level file");
			pw.close();

			HostFile destDir = s.getFile(new File(javaTempDirName + File.separator + "destdir").getPath());

			assertFalse(destDir.exists());

			HostFileUtils.copyDirectory(srcDir, destDir);

			// now inspect destDir
			assertTrue(destDir.exists());
			assertTrue(destDir.isDirectory());

			List<HostFile> filesInDestDir = destDir.listFiles();
			int nFiles = countFiles(filesInDestDir);
			assertEquals(1, nFiles);
			int nDirs = countDirs(filesInDestDir);
			assertEquals(2, nDirs);

			List<HostFile> subDirsInDestDir = getDirs(filesInDestDir);

			for (HostFile subDir : subDirsInDestDir) {
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
			s.close();
		}
	}

	private int countFiles(List<HostFile> files) {
		return files.size() - countDirs(files);
	}

	private int countDirs(List<HostFile> files) {
		int cnt = 0;
		for (HostFile f : files) {
			if (f.isDirectory()) {
				cnt++;
			}
		}
		return cnt;
	}

	private List<HostFile> getDirs(List<HostFile> files) {
		List<HostFile> dirs = new ArrayList<HostFile>();
		for (HostFile f : files) {
			if (f.isDirectory()) {
				dirs.add(f);
			}
		}
		return dirs;
	}

	@Test
	@Ignore
	public void testCopyToSameMachine() {
		HostSession s = HostSessionFactory.getHostSession(host1);
		// HostSession s2 = HostSessionFactory.getHostSession(host2);
		try {
			HostFile h1 = s.getFile("/tmp/input.txt");
			// HostFile h2 = s.getFile("/tmp/output.txt");
			HostFile h2 = s.getFile("/root/loopback/mnt/output.txt");
			HostFileUtils.copy(h1, h2);
		} finally {
			s.close();
			// s2.close();
		}
	}

	@Test
	@Ignore
	public void testCopyToFullDisk() {
		Host h = new Host();
		// h.setAddress("was-51");
		h.setAddress("apache-22");
		h.setUsername("root");
		h.setPassword("centos");
		// h.setTemporaryDirectoryLocation("/root/loopback/mnt");
		h.setAccessMethod(HostAccessMethod.SSH_SCP);
		h.setOperatingSystemFamily(OperatingSystemFamily.UNIX);

		HostSession s = HostSessionFactory.getHostSession(h);
		try {
			try {
				s.copyToTemporaryFile(new ClassPathResource("web/help/settingUpAnEnvironment.mp4"));
				fail("No exception thrown when writing to full disk");
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		} finally {
			s.close();
		}
	}

	@Test
	@Ignore
	public void testTemporaryDirectory() {
		HostSession s = HostSessionFactory.getHostSession(host1);
		try {
			HostFile tmp1 = s.getTempFile("bla.txt");
			HostFileUtils.putStringToHostFile("blargh1", tmp1);
			HostFile tmp2 = s.getTempFile("bla", ".tmp");
			HostFileUtils.putStringToHostFile("blargh2", tmp2);
			HostFile tmp3 = s.getTempFile("bla", ".tmp");
			HostFileUtils.putStringToHostFile("blargh2", tmp3);
		} finally {
			s.close();
		}
	}

	@Test
	public void testUnzipWithReplacement() {
		final Host localHost = Host.getLocalHost();
		final HostSession localHostSession = localHost.getHostSession();
		HostFile destDir = null;
		try {
			final HostFile zipFile = localHostSession.getFile("src/test/resources/testWar/PetClinic-WithReplacement-3.0.war");
			Map<String, String> properties = Maps.newHashMap();
			properties.put("valueA", "benoit");
			properties.put("valueB", "moussaud");
			properties.put("indexPage", "index_fr");
			final HostFile tempFile = localHostSession.getTempFile("PetClinic-3.0", ".war");
			destDir = tempFile.getParentFile().getFile("check");
			destDir.mkdirs();

			HostFileUtils.unzip(zipFile, destDir, new LenientTemplateResolvingArchiveHostFileInputTransformer(properties, PlaceholderFormat.SPRING));

			final String webXmlAsString = HostFileUtils.getHostFileAsString(destDir.getFile("WEB-INF/web.xml"));
			assertTrue(webXmlAsString.contains("<welcome-file>index_fr.html</welcome-file>"));
			assertTrue(webXmlAsString.contains("<welcome-file>index_fr.htm</welcome-file>"));
			assertTrue(webXmlAsString.contains("<welcome-file>index_fr.jsp</welcome-file>"));

			final String myPropertiesAsString = HostFileUtils.getHostFileAsString(destDir.getFile("/WEB-INF/classes/com/xebialabs/petstore/properties/my.properties"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyA=benoit"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyB=moussaud"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyAB=ibenoit-moussaud"));

		}
		finally {
			if (destDir != null)
				destDir.deleteRecursively();

			localHostSession.close();
		}
	}

	@Test
	public void testCopyWarWithReplacement() {
		War warFile = new War();
		warFile.setLocation("src/test/resources/testWar/PetClinic-WithReplacement-3.0.war");
		final Host localHost = Host.getLocalHost();
		HostSession localHostSession = localHost.getHostSession();
		HostFile checkdir = null;
		try {
			final HostFile earHostFile = localHostSession.getFile(warFile.getLocation());
			final HostFile tempFile = localHostSession.getTempFile("PetClinic-3.0", ".war");
			Map<String, String> properties = Maps.newHashMap();
			properties.put("valueA", "benoit");
			properties.put("valueB", "moussaud");
			properties.put("indexPage", "index_fr");

			HostFileUtils.copy(earHostFile, tempFile, new LenientTemplateResolvingArchiveHostFileInputTransformer(properties, PlaceholderFormat.SPRING));

			checkdir = tempFile.getParentFile().getFile("check");
			checkdir.mkdirs();
			HostFileUtils.unzip(tempFile, checkdir);

			final String webXmlAsString = HostFileUtils.getHostFileAsString(checkdir.getFile("WEB-INF/web.xml"));
			assertTrue(webXmlAsString.contains("<welcome-file>index_fr.html</welcome-file>"));
			assertTrue(webXmlAsString.contains("<welcome-file>index_fr.htm</welcome-file>"));
			assertTrue(webXmlAsString.contains("<welcome-file>index_fr.jsp</welcome-file>"));

			final String myPropertiesAsString = HostFileUtils.getHostFileAsString(checkdir.getFile("/WEB-INF/classes/com/xebialabs/petstore/properties/my.properties"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyA=benoit"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyB=moussaud"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyAB=ibenoit-moussaud"));

		} finally {
			if (checkdir != null)
				checkdir.deleteRecursively();
			localHostSession.close();

		}
	}

	@Test
	public void testCopyEarWithReplacement() {
		War warFile = new War();
		warFile.setLocation("src/test/resources/testEar/PetClinic-WithReplacement-3.0.ear");
		final Host localHost = Host.getLocalHost();
		HostSession localHostSession = localHost.getHostSession();
		HostFile checkdir = null;
		try {
			final HostFile earHostFile = localHostSession.getFile(warFile.getLocation());
			final HostFile tempFile = localHostSession.getTempFile("PetClinic-3.0", ".ear");
			Map<String, String> properties = Maps.newHashMap();
			properties.put("valueA", "benoit");
			properties.put("valueB", "moussaud");
			properties.put("indexPage", "index_fr");

			HostFileUtils.copy(earHostFile, tempFile, new LenientTemplateResolvingArchiveHostFileInputTransformer(properties, PlaceholderFormat.SPRING));

			checkdir = tempFile.getParentFile().getFile("check");
			checkdir.mkdirs();
			HostFileUtils.unzip(tempFile, checkdir);
			final HostFile petClinicWarFile = checkdir.getFile("petclinic.war");
			HostFileUtils.unzip(petClinicWarFile, checkdir);


			final String webXmlAsString = HostFileUtils.getHostFileAsString(checkdir.getFile("WEB-INF/web.xml"));
			assertTrue(webXmlAsString, webXmlAsString.contains("<welcome-file>index_fr.html</welcome-file>"));
			assertTrue(webXmlAsString, webXmlAsString.contains("<welcome-file>index_fr.htm</welcome-file>"));
			assertTrue(webXmlAsString, webXmlAsString.contains("<welcome-file>index_fr.jsp</welcome-file>"));

			final String myPropertiesAsString = HostFileUtils.getHostFileAsString(checkdir.getFile("/WEB-INF/classes/com/xebialabs/petstore/properties/my.properties"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyA=benoit"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyB=moussaud"));
			assertTrue(myPropertiesAsString, myPropertiesAsString.contains("keyAB=ibenoit-moussaud"));

		} finally {
			if (checkdir != null)
				checkdir.deleteRecursively();
			localHostSession.close();

		}
	}

	@Test
	public void testCopyJarWithReplacementSpringPlaceHolder() {
		EjbJar ejbjarFile = new EjbJar();
		ejbjarFile.setLocation("src/test/resources/testJar/MyArchiveSpring.jar");
		final Host localHost = Host.getLocalHost();
		HostSession localHostSession = localHost.getHostSession();
		HostFile checkdir = null;
		try {
			final HostFile jarHostFile = localHostSession.getFile(ejbjarFile.getLocation());
			final HostFile tempFile = localHostSession.getTempFile("MyArchiveSpring", ".jar");
			Map<String, String> properties = Maps.newHashMap();
			properties.put("JAVA_HOME", "c:\\java-1.4");
			properties.put("EXEC_DIR", "e:\\work\\exec");
			properties.put("driver", "com.oracle.jdbc.Driver");
			properties.put("user", "scott");
			properties.put("password", "tiger");
			properties.put("serverSMTP", "smtp.xebialabs.com");
			properties.put("File", "e:\\log\\log.txt");
			properties.put("URL_WSS", "http://my.remote.host:999/petservice");

			HostFileUtils.copy(jarHostFile, tempFile, new LenientTemplateResolvingArchiveHostFileInputTransformer(properties, PlaceholderFormat.SPRING));

			checkdir = tempFile.getParentFile().getFile("check");
			checkdir.mkdirs();
			HostFileUtils.unzip(tempFile, checkdir);

			final String AlertDebut = HostFileUtils.getHostFileAsString(checkdir.getFile("RunFCXBAT_AlerteDebutAssolement.bat"));
			assertTrue(AlertDebut, AlertDebut.contains("SET JAVA_HOME=" + properties.get("JAVA_HOME")));
			assertTrue(AlertDebut, AlertDebut.contains("SET EXEC_DIR=" + properties.get("EXEC_DIR")));

			final String AlertFin = HostFileUtils.getHostFileAsString(checkdir.getFile("RunFCXBAT_AlerteFinAssolement.bat"));
			assertTrue(AlertFin, AlertFin.contains("SET JAVA_HOME=" + properties.get("JAVA_HOME")));
			assertTrue(AlertFin, AlertFin.contains("SET EXEC_DIR=" + properties.get("EXEC_DIR")));

			final String frontEnd = HostFileUtils.getHostFileAsString(checkdir.getFile("com/groupama/mrc/fi/config/front-end-config.xml"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"driver-url\" value=\"" + properties.get("driver") + "\">"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"user\" value=\"scott\">"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"serveurSMTP\" value=\"smtp.xebialabs.com\">\n</a>"));

			final String confWSS = HostFileUtils.getHostFileAsString(checkdir.getFile("com/groupama/mrc/fi/config/ConfWss.properties"));
			assertTrue(confWSS, confWSS.contains("C=D\n" +
					"URL_WSS=" + properties.get("URL_WSS") + "\n" +
					"A=B"));

		}
		finally {
			if (checkdir != null)
				checkdir.deleteRecursively();
			localHostSession.close();

		}


	}


	@Test
	public void testCopyJarWithReplacementNonePlaceHolder() {
		EjbJar ejbjarFile = new EjbJar();
		ejbjarFile.setLocation("src/test/resources/testJar/MyArchiveNone.jar");
		final Host localHost = Host.getLocalHost();
		HostSession localHostSession = localHost.getHostSession();
		HostFile checkdir = null;
		try {
			final HostFile jarHostFile = localHostSession.getFile(ejbjarFile.getLocation());
			final HostFile tempFile = localHostSession.getTempFile("MyArchiveNone", ".jar");
			Map<String, String> properties = Maps.newHashMap();
			properties.put("SET JAVA_HOME=", "SET JAVA_HOME=c:\\java-1.4");
			System.out.println(properties);
			properties.put("SET EXEC_DIR=", "SET EXEC_DIR=e:\\work\\exec");
			properties.put("<!-- DATA --><property name=\"driver-url\" value=\"\">", "<property name=\"driver-url\" value=\"com.oracle.jdbc.Driver\">");
			properties.put("<!-- DATA --><property name=\"user\" value=\"\">", "<property name=\"user\" value=\"scott\">");
			properties.put("<!-- PARAM --><property name=\"password\" value=\"\">", "<!-- PARAM --><property name=\"password\" value=\"tiger\">");
			properties.put("<property name=\"serveurSMTP\" value=\"\">", "<property name=\"serveurSMTP\" value=\"smtp.xebialabs.com\">");
			properties.put("File", "e:\\log\\log.txt");
			properties.put("URL_WSS=", "URL_WSS=http://my.remote.host:999/petservice");

			HostFileUtils.copy(jarHostFile, tempFile, new LenientTemplateResolvingArchiveHostFileInputTransformer(properties, PlaceholderFormat.NONE));

			checkdir = tempFile.getParentFile().getFile("check");
			checkdir.mkdirs();
			HostFileUtils.unzip(tempFile, checkdir);

			final String AlertDebut = HostFileUtils.getHostFileAsString(checkdir.getFile("RunFCXBAT_AlerteDebutAssolement.bat"));
			assertTrue(AlertDebut, AlertDebut.contains("SET JAVA_HOME=c:\\java-1.4"));
			assertTrue(AlertDebut, AlertDebut.contains("SET EXEC_DIR=e:\\work\\exec"));

			final String AlertFin = HostFileUtils.getHostFileAsString(checkdir.getFile("RunFCXBAT_AlerteFinAssolement.bat"));
			assertTrue(AlertFin, AlertFin.contains("SET JAVA_HOME=c:\\java-1.4"));
			assertTrue(AlertFin, AlertFin.contains("SET EXEC_DIR=e:\\work\\exec"));

			final String frontEnd = HostFileUtils.getHostFileAsString(checkdir.getFile("com/groupama/mrc/fi/config/front-end-config.xml"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"driver-url\" value=\"com.oracle.jdbc.Driver\">"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"user\" value=\"scott\">"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"serveurSMTP\" value=\"smtp.xebialabs.com\">\n</a>"));

			final String confWSS = HostFileUtils.getHostFileAsString(checkdir.getFile("com/groupama/mrc/fi/config/ConfWss.properties"));
			assertTrue(confWSS, confWSS.contains("C=D\n" +
					"URL_WSS=http://my.remote.host:999/petservice" + "\n" +
					"A=B"));

		}
		finally {
			if (checkdir != null)
				checkdir.deleteRecursively();
			localHostSession.close();

		}


	}

	@Test
	public void testCopyFolderContainingAJarAnImageFileAndAFileWithReplacementSpringPlaceHolder() throws Exception {
		final Libraries lib = new Libraries();
		lib.setLocation("src/test/resources/librariesFolder");

		final HostSession s = Host.getLocalHost().getHostSession();
		final HostFile jarHostFile = s.getFile(lib.getLocation());

		Map<String, String> properties = Maps.newHashMap();
		properties.put("JAVA_HOME", "c:\\java-1.4");
		properties.put("EXEC_DIR", "e:\\work\\exec");
		properties.put("driver", "com.oracle.jdbc.Driver");
		properties.put("user", "scott");
		properties.put("password", "tiger");
		properties.put("serverSMTP", "smtp.xebialabs.com");
		properties.put("File", "e:\\log\\log.txt");
		properties.put("URL_WSS", "http://my.remote.host:999/petservice");


		File workDirFile = File.createTempFile("workdir", null);
		HostFile workDir = s.getFile(workDirFile.getPath());
		String javaTempDirName = workDir.getParent();


		HostFile destDir = s.getFile(new File(javaTempDirName + File.separator + "destdir").getPath());
		HostFile checkdir = s.getFile(new File(javaTempDirName + File.separator + "checkdir").getPath());
		;
		try {
			assertFalse(destDir.exists());
			HostFileUtils.copy(jarHostFile, destDir, new LenientTemplateResolvingArchiveHostFileInputTransformer(properties, PlaceholderFormat.SPRING));

			String configurationFilesAsString = HostFileUtils.getHostFileAsString(destDir.getFile("config.properties"));
			assertTrue(configurationFilesAsString, configurationFilesAsString.contains("driver=com.oracle.jdbc.Driver"));
			assertTrue(configurationFilesAsString, configurationFilesAsString.contains("command=sqlplus scott/tiger@XE"));

			checkdir.mkdirs();

			final HostFile zip = destDir.getFile("MyArchiveSpring.jar");
			HostFileUtils.unzip(zip, checkdir);

			final String AlertDebut = HostFileUtils.getHostFileAsString(checkdir.getFile("RunFCXBAT_AlerteDebutAssolement.bat"));
			assertTrue(AlertDebut, AlertDebut.contains("SET JAVA_HOME=" + properties.get("JAVA_HOME")));
			assertTrue(AlertDebut, AlertDebut.contains("SET EXEC_DIR=" + properties.get("EXEC_DIR")));

			final String AlertFin = HostFileUtils.getHostFileAsString(checkdir.getFile("RunFCXBAT_AlerteFinAssolement.bat"));
			assertTrue(AlertFin, AlertFin.contains("SET JAVA_HOME=" + properties.get("JAVA_HOME")));
			assertTrue(AlertFin, AlertFin.contains("SET EXEC_DIR=" + properties.get("EXEC_DIR")));

			final String frontEnd = HostFileUtils.getHostFileAsString(checkdir.getFile("com/groupama/mrc/fi/config/front-end-config.xml"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"driver-url\" value=\"" + properties.get("driver") + "\">"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"user\" value=\"scott\">"));
			assertTrue(frontEnd, frontEnd.contains("<property name=\"serveurSMTP\" value=\"smtp.xebialabs.com\">\n</a>"));

			final String confWSS = HostFileUtils.getHostFileAsString(checkdir.getFile("com/groupama/mrc/fi/config/ConfWss.properties"));
			assertTrue(confWSS, confWSS.contains("C=D\n" +
					"URL_WSS=" + properties.get("URL_WSS") + "\n" +
					"A=B"));


			final HostFile png = destDir.getFile("arrow_down.png");
			long newSize = png.length();
			long oldSize = jarHostFile.getFile("arrow_down.png").length();
			assertEquals(oldSize + "/" + newSize, oldSize, newSize);

		} finally {
			if (checkdir != null)
				checkdir.deleteRecursively();
			destDir.deleteRecursively();

		}

	}


}
