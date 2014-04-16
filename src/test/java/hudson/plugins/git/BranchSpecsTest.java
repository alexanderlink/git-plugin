package hudson.plugins.git;

import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.plugins.git.util.BuildData;
import hudson.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class BranchSpecsTest extends AbstractGitTestCase
{

    private List<UserRemoteConfig> REMOTE; 
    private Properties COMMITS; 
                
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(REMOTE == null) {
            final File tempRemoteDir = this.createTmpDir();
            extract(new ZipFile("src/test/resources/specialBranchRepo.zip"), tempRemoteDir);
            COMMITS = parseLsRemote(new File("src/test/resources/specialBranchRepo.ls-remote"));
            List<UserRemoteConfig> list = new ArrayList<UserRemoteConfig>();
            list.add(new UserRemoteConfig(tempRemoteDir.getAbsolutePath(), "origin", "", null));
            REMOTE = Collections.unmodifiableList(list);
        }
    }
    
    @Test
    public void testMaster() throws Exception {
        check("master", COMMITS.getProperty("refs/heads/master"));
    }

    @Test
    public void testOriginMaster() throws Exception {
        check("origin/master", COMMITS.getProperty("refs/heads/master"));
    }

    @Test
    public void testRefsHeadsMaster() throws Exception {
        check("refs/heads/master", COMMITS.getProperty("refs/heads/master"));
    }

    @Test
    public void testRefsRemotesOriginMaster() throws Exception {
        check("refs/remotes/origin/master", COMMITS.getProperty("refs/heads/master"));
    }

    @Test
    public void testRemotesOriginMaster() throws Exception {
        check("remotes/origin/master", COMMITS.getProperty("refs/heads/master"));
    }

    @Test
    public void testRefsHeadsRefsHeadsMaster() throws Exception {
        check("refs/heads/refs/heads/master", COMMITS.getProperty("refs/heads/refs/heads/master"));
    }

    @Test
    public void testRefsTagsMaster() throws Exception {
        check("refs/tags/master", COMMITS.getProperty("refs/tags/master^{}"));
    }

    @Test
    public void testRefsHeadsRemotesOriginMaster() throws Exception {
        check("refs/heads/remotes/origin/master", COMMITS.getProperty("refs/heads/remotes/origin/master"));
    }

    private void check(String configuredBranchSpec, String expectedRevision) throws Exception
    {
        FreeStyleProject project = setupProject(REMOTE, branchSpec(configuredBranchSpec), 
                    false, null, null, null, null, false, null, null);
        FreeStyleBuild build = build(project, Result.SUCCESS);
        BuildData buildData = ((GitSCM)project.getScm()).getBuildData(build);
        Revision lastBuiltRevision = buildData.getLastBuiltRevision();
        assertEquals("Checked out revision unexpected", expectedRevision, lastBuiltRevision.getSha1().getName());
    }

    private List<BranchSpec> branchSpec(String branchSpecString)
    {
        ArrayList<BranchSpec> list = new ArrayList<BranchSpec>();
        list.add(new BranchSpec(branchSpecString));
        return Collections.unmodifiableList(list);
    }

    private void extract(ZipFile zipFile, File outputDir) throws IOException
    {
        listener.getLogger().println(String.format("Extracting %s to %s", zipFile.getName(), outputDir.getAbsolutePath()));
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File entryDestination = new File(outputDir,  entry.getName());
            entryDestination.getParentFile().mkdirs();
            if (entry.isDirectory())
                entryDestination.mkdirs();
            else {
                InputStream in = zipFile.getInputStream(entry);
                OutputStream out = new FileOutputStream(entryDestination);
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    private Properties parseLsRemote(File file) throws IOException
    {
        Properties properties = new Properties();
        Pattern pattern = Pattern.compile("([a-f0-9]{40})\\s*(.*)");
        for(Object lineO : FileUtils.readLines(file)) {
            String line = ((String)lineO).trim();
            Matcher matcher = pattern.matcher(line);
            if(matcher.matches()) {
                properties.setProperty(matcher.group(2), matcher.group(1));
            } else {
                System.err.println("ls-remote pattern does not match '" + line + "'");
            }
        }
        return properties;
    }
    
}