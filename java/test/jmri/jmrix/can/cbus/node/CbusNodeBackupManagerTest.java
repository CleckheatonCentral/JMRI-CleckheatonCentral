package jmri.jmrix.can.cbus.node;

import java.io.File;
import java.util.ArrayList;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupManagerTest {
    
    @Test
    public void testCTor() {
        CbusNode node = new CbusNode(null,256);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        Assert.assertNotNull("exists",t);
        node.dispose();
    }
    
    @Test
    public void testNotOnNetwork() {
        
        CbusNode node = new CbusNode(null,257);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        
        Assert.assertNull(t.getFirstBackupTime());
        Assert.assertNull(t.getLastBackupTime());
        
        Assert.assertTrue("node backup 0 entry exists",t.getBackups().isEmpty());
        Assert.assertTrue(t.getNumCompleteBackups()==0);
        
        t.nodeNotOnNetwork();
        Assert.assertTrue("node backup 0 entry exists",1 == t.getBackups().size());
        Assert.assertTrue(t.getNumCompleteBackups()==0);
        Assert.assertEquals("Last backup not on network",
            CbusNodeConstants.BackupType.NOTONNETWORK, t.getBackups().get(0).getBackupResult());
            
        node.dispose();
    }
    
    @Test
    public void testNodeInSlim() {
        
        CbusNode node = new CbusNode(null,258);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        
        Assert.assertTrue("node backup 0 entry exists",t.getBackups().isEmpty());
        Assert.assertTrue(t.getNumCompleteBackups()==0);
        
        t.nodeInSLiM();
        Assert.assertTrue("node backup 0 entry exists",1 == t.getBackups().size());
        Assert.assertTrue(t.getNumCompleteBackups()==0);
        Assert.assertEquals("Last backup slim",
            CbusNodeConstants.BackupType.SLIM, t.getBackups().get(0).getBackupResult());
            
        node.dispose();
    }
    
    @Test
    public void testNodeBackupError() {
        
        CbusNode node = new CbusNode(null,259);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        
        Assert.assertTrue("node backup 0 entry exists",t.getBackups().isEmpty());
        Assert.assertTrue(t.getNumCompleteBackups()==0);
        
        t.doStore(true, false); // create new, no existing errors logged
        Assert.assertTrue("node backup 0 entry exists",1 == t.getBackups().size());
        Assert.assertTrue(t.getNumCompleteBackups()==0);
        Assert.assertEquals("Last backup complete with errors",
            CbusNodeConstants.BackupType.COMPLETEDWITHERROR, t.getBackups().get(0).getBackupResult());
            
        node.dispose();
    }
    
    @Test
    public void testOpenxistingFile() throws java.text.ParseException, java.io.IOException {
        
        CbusNode node = new CbusNode(null,41375);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        
       // jmri.util.FileUtil.createDirectory("cbus" + java.io.File.separator + "node");
        
        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/node/");
        java.io.File systemFile = new java.io.File(dir, "41375.xml");

        // note that 41375.xml deliberately has the backup in the wrong order 
        // to ensure that the array order is corrected

        java.nio.file.Files.copy(systemFile.toPath(), t.getFileLocation().toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        Assert.assertTrue("node backup 0 entry exists",t.getBackups().isEmpty());
        Assert.assertTrue(t.getNumCompleteBackups()==0);
        
        t.doLoad();
        
        Assert.assertEquals("File loaded to ArrayList",6, t.getBackups().size());
        Assert.assertTrue(t.getNumCompleteBackups()==2);
        
        Assert.assertEquals("Node Username from xml","My Node Name for Node 41375", node.getUserName());
        Assert.assertEquals("Node UserComment from xml","My Node 41375 Freetext", node.getUserComment());
        Assert.assertEquals("Node custom Module Type Name from xml","CATFLAP", node.getNodeNameFromName());
        
        Assert.assertEquals("First Backup Time",t.xmlDateStyle.parse("2019-06-24 19:51:15"), t.getFirstBackupTime());
        Assert.assertEquals("Last Backup Time",t.xmlDateStyle.parse("2019-08-30 14:51:15"), t.getLastBackupTime());
        
        Assert.assertEquals("backup 0 complete with errors",
            CbusNodeConstants.BackupType.COMPLETEDWITHERROR, t.getBackups().get(0).getBackupResult());
        Assert.assertEquals("backup 1 complete",
            CbusNodeConstants.BackupType.COMPLETE, t.getBackups().get(1).getBackupResult());
        Assert.assertEquals("backup 2 slim",
            CbusNodeConstants.BackupType.SLIM, t.getBackups().get(2).getBackupResult());
        Assert.assertEquals("backup 3 not on network",
            CbusNodeConstants.BackupType.NOTONNETWORK, t.getBackups().get(3).getBackupResult());
        Assert.assertEquals("backup 4 complete",
            CbusNodeConstants.BackupType.COMPLETE, t.getBackups().get(4).getBackupResult());
        Assert.assertEquals("backup 5 incomplete",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(5).getBackupResult());
        
        Assert.assertEquals("backup 4 comment",
            "First Backup Completed Comment", t.getBackups().get(4).getBackupComment());
        Assert.assertTrue("backup 2 comment", t.getBackups().get(2).getBackupComment().isEmpty());
        
        Assert.assertEquals("backup node parameters",
            "A5591D800D01010D0D0100080000000000000101", t.getBackups().get(4).getNodeParamManager().getParameterHexString());
        Assert.assertEquals("backup node NVs","01", t.getBackups().get(4).getNodeNvManager().getNvHexString());
        
        
        ArrayList<CbusNodeEvent> tempList = t.getBackups().get(4).getNodeEventManager().getEventArray();
        Assert.assertNotNull("Ev Array Not Null", tempList);
        
        
        Assert.assertEquals("backup node events",
            "[NN:127 EN:100 , NN:299 EN:203 , NN:127 EN:207 ]",
            tempList.toString());
        
        CbusNodeEvent tmpEv = t.getBackups().get(4).getNodeEventManager().getNodeEvent(299,203);
        Assert.assertNotNull("Ev Not Null", tmpEv);
        
        Assert.assertEquals("backup node event 203 nn 299 ev vars",
            "0102030405060708090A0B0CFF",
            tmpEv.getHexEvVarString());
        
        node.dispose();
    }
    
    @Test
    public void testSaveFile() throws java.text.ParseException, java.io.IOException {
        
        CbusNode node = new CbusNode(null,41375);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        
        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/node/");
        java.io.File systemFile = new java.io.File(dir, "41375.xml");

        // note that 41375.xml deliberately has the backup in the wrong order 
        // to ensure that the array order is corrected

        java.nio.file.Files.copy(systemFile.toPath(), t.getFileLocation().toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Assert.assertEquals("File should not be loaded",0, t.getBackups().size());
        t.doLoad();
        Assert.assertEquals("File loaded to ArrayList",6, t.getBackups().size());
        
        t.nodeInSLiM();
        
        // we can't do a direct comparison of the xml as the new entry will have a date stamp
        // but we can reload it and check a few things have saved ok
        node.getNodeBackupManager().setBackupStarted(false);
        t.resetBupArray();
        t.doLoad();
        
        JUnitUtil.waitFor(()->{ return(t.getBackups().size()>6); }, "Not all backups loaded");
                
        Assert.assertEquals("File loaded to ArrayList",7, t.getBackups().size());
        Assert.assertTrue(t.getNumCompleteBackups()==2);
        
        Assert.assertEquals("Node Username from xml","My Node Name for Node 41375", node.getUserName());
        Assert.assertEquals("Node UserComment from xml","My Node 41375 Freetext", node.getUserComment());
        Assert.assertEquals("Node custom Module Type Name from xml","CATFLAP", node.getNodeNameFromName());
        
        Assert.assertEquals("First Backup Time",t.xmlDateStyle.parse("2019-06-24 19:51:15"), t.getFirstBackupTime());
        Assert.assertEquals("Last Backup Time",t.xmlDateStyle.parse("2019-08-30 14:51:15"), t.getLastBackupTime());
        
        Assert.assertEquals("backup 0 slim",
            CbusNodeConstants.BackupType.SLIM, t.getBackups().get(0).getBackupResult());
        Assert.assertEquals("backup 1 complete with errors",
            CbusNodeConstants.BackupType.COMPLETEDWITHERROR, t.getBackups().get(1).getBackupResult());
        Assert.assertEquals("backup 2 complete",
            CbusNodeConstants.BackupType.COMPLETE, t.getBackups().get(2).getBackupResult());
        Assert.assertEquals("backup 3 slim",
            CbusNodeConstants.BackupType.SLIM, t.getBackups().get(3).getBackupResult());
        Assert.assertEquals("backup 4 not on network",
            CbusNodeConstants.BackupType.NOTONNETWORK, t.getBackups().get(4).getBackupResult());
        Assert.assertEquals("backup 5 complete",
            CbusNodeConstants.BackupType.COMPLETE, t.getBackups().get(5).getBackupResult());
        Assert.assertEquals("backup 6 incomplete",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(6).getBackupResult());
        
        Assert.assertEquals("backup 5 comment",
            "First Backup Completed Comment", t.getBackups().get(5).getBackupComment());
        Assert.assertTrue("backup 3 comment", t.getBackups().get(3).getBackupComment().isEmpty());
        
        Assert.assertEquals("backup node parameters",
            "A5591D800D01010D0D0100080000000000000101", t.getBackups().get(5).getNodeParamManager().getParameterHexString());
        Assert.assertEquals("backup node NVs","01", t.getBackups().get(5).getNodeNvManager().getNvHexString());
        
        ArrayList<CbusNodeEvent> tempList = t.getBackups().get(5).getNodeEventManager().getEventArray();
        Assert.assertNotNull("Ev Array Not Null", tempList);
        
        Assert.assertEquals("backup node events",
            "[NN:127 EN:100 , NN:299 EN:203 , NN:127 EN:207 ]",
            tempList.toString());
        
        CbusNodeEvent tmpEv = t.getBackups().get(5).getNodeEventManager().getNodeEvent(299,203);
        Assert.assertNotNull("Ev Not Null", tmpEv);
        
        Assert.assertEquals("backup node event 203 nn 299 ev vars",
            "0102030405060708090A0B0CFF",
            tmpEv.getHexEvVarString());
        
        node.dispose();
        
    }
    
    @Test
    public void testTrimBackups() throws java.text.ParseException, java.io.IOException {
        
        Assertions.assertNotNull(memo);
        jmri.jmrix.can.cbus.CbusPreferences pref = memo.get( jmri.jmrix.can.cbus.CbusPreferences.class);
        
        CbusNode node = new CbusNode(memo,41376);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        
        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/node/");
        java.io.File systemFile = new java.io.File(dir, "41376.xml");

        java.nio.file.Files.copy(systemFile.toPath(), t.getFileLocation().toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Assert.assertEquals("File should not be loaded",0, t.getBackups().size());
        
        t.doLoad();
        Assert.assertEquals("File loaded to ArrayList",14, t.getBackups().size());
        
        Assert.assertEquals(12,t.getNumCompleteBackups());
        
        Assert.assertEquals("First Backup Time",t.xmlDateStyle.parse("2019-01-24 19:51:15"), t.getFirstBackupTime());
        Assert.assertEquals("Last Backup Time",t.xmlDateStyle.parse("2019-12-24 19:51:15"), t.getLastBackupTime());
        
        pref.setMinimumNumBackupsToKeep(10);
        t.doStore(false,false);
        
        // reload it
        node.getNodeBackupManager().setBackupStarted(false);
        t.resetBupArray();
        t.doLoad();
        
        JUnitUtil.waitFor(()->{ return(t.getBackups().size()>10); }, "Not all backups loaded");
        
        Assert.assertEquals("File loaded to ArrayList",11, t.getBackups().size());
        Assert.assertEquals("Complete backups after save and trim",10,t.getNumCompleteBackups());
        
        Assert.assertEquals("First Backup Time",t.xmlDateStyle.parse("2019-01-24 19:51:15"), t.getFirstBackupTime());
        Assert.assertEquals("Last Backup Time",t.xmlDateStyle.parse("2019-12-24 19:51:15"), t.getLastBackupTime());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-12-24 19:51:15"), t.getBackups().get(0).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-11-24 19:51:15"), t.getBackups().get(1).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-10-24 19:51:15"), t.getBackups().get(2).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-09-24 19:51:15"), t.getBackups().get(3).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-08-27 19:51:15"), t.getBackups().get(4).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-08-24 19:51:15"), t.getBackups().get(5).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-07-24 19:51:15"), t.getBackups().get(6).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-06-24 19:51:15"), t.getBackups().get(7).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-05-24 19:51:15"), t.getBackups().get(8).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-04-24 19:51:15"), t.getBackups().get(9).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-01-24 19:51:15"), t.getBackups().get(10).getBackupTimeStamp());
        
        
        pref.setMinimumNumBackupsToKeep(5);
        t.doStore(false,false);
        
        // reload it
        node.getNodeBackupManager().setBackupStarted(false);
        t.resetBupArray();
        t.doLoad();
        
        Assert.assertEquals("File loaded to ArrayList",6, t.getBackups().size());
        Assert.assertEquals("Complete backups after save and trim",5,t.getNumCompleteBackups());

        Assert.assertEquals("First Backup Time",t.xmlDateStyle.parse("2019-01-24 19:51:15"), t.getFirstBackupTime());
        Assert.assertEquals("Last Backup Time",t.xmlDateStyle.parse("2019-12-24 19:51:15"), t.getLastBackupTime());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-12-24 19:51:15"), t.getBackups().get(0).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-11-24 19:51:15"), t.getBackups().get(1).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-10-24 19:51:15"), t.getBackups().get(2).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-09-24 19:51:15"), t.getBackups().get(3).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-08-27 19:51:15"), t.getBackups().get(4).getBackupTimeStamp());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-01-24 19:51:15"), t.getBackups().get(5).getBackupTimeStamp());
        
        t.getBackups().get(1).setBackupComment("Test Comment to avoid being deleted");
        
        pref.setMinimumNumBackupsToKeep(0);
        t.doStore(false,false);
        
        // reload it 
        node.getNodeBackupManager().setBackupStarted(false);
        t.resetBupArray();
        t.doLoad();

        Assert.assertEquals("File loaded to ArrayList",2, t.getBackups().size());
        Assert.assertEquals("Complete backups after save and trim",2,t.getNumCompleteBackups());
        
        Assert.assertEquals("First Backup Time",t.xmlDateStyle.parse("2019-01-24 19:51:15"), t.getFirstBackupTime());
        Assert.assertEquals("Last Backup Time",t.xmlDateStyle.parse("2019-11-24 19:51:15"), t.getLastBackupTime());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-11-24 19:51:15"), t.getBackups().get(0).getBackupTimeStamp());
        
        node.dispose();
    }
    
    @Test
    public void testFailedBackups() throws java.text.ParseException, java.io.IOException {
        
        CbusNode node = new CbusNode(null,41377);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        
        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/node/");
        java.io.File systemFile = new java.io.File(dir, "41377.xml");

        java.nio.file.Files.copy(systemFile.toPath(), t.getFileLocation().toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Assert.assertEquals("File should not be loaded",0, t.getBackups().size());
        
        t.doLoad();
        
        JUnitAppender.assertErrorMessageStartsWith("Incorrect Event Variable Length in Backup");
        JUnitAppender.assertErrorMessageStartsWith("NO datetimestamp in a backup log entry");
        JUnitAppender.assertErrorMessageStartsWith("NO result in a backup log entry");
        JUnitAppender.assertErrorMessageStartsWith("Unable to parse date NOT A DATE");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect Event Variable Length in Backup");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect Node / Event Number in Backup");
        JUnitAppender.assertErrorMessageStartsWith("Node / Event Number Missing in Backup");
        JUnitAppender.assertErrorMessageStartsWith("Node / Event Number Missing in Backup");
        JUnitAppender.assertErrorMessageStartsWith("NO result in a backup log entry");
        
        Assert.assertEquals("File loaded to ArrayList",8, t.getBackups().size());
        Assert.assertEquals("backup 0 INCOMPLETE",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(0).getBackupResult());
        Assert.assertEquals("backup 1 INCOMPLETE",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(1).getBackupResult());
        Assert.assertEquals("backup 2 INCOMPLETE",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(2).getBackupResult());
        Assert.assertEquals("backup 3 INCOMPLETE",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(3).getBackupResult());
        Assert.assertEquals("backup 4 INCOMPLETE",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(4).getBackupResult());
        Assert.assertEquals("backup 5 INCOMPLETE",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(5).getBackupResult());
        Assert.assertEquals("backup 6 INCOMPLETE",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(6).getBackupResult());
        Assert.assertEquals("backup 7 INCOMPLETE",
            CbusNodeConstants.BackupType.INCOMPLETE, t.getBackups().get(7).getBackupResult());
        
        node.dispose();
        
    }
    
    @Test
    public void testMalfordmedFile() throws java.text.ParseException, java.io.IOException {
        
        CbusNode node = new CbusNode(null,41378);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        
        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/node/");
        java.io.File systemFile = new java.io.File(dir, "41378.xml");

        java.nio.file.Files.copy(systemFile.toPath(), t.getFileLocation().toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        t.doLoad();
        JUnitAppender.assertErrorMessageStartsWith("File invalid: org.jdom2.input.JDOMParseException: Error on line 6:");
        
    }

    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp(@TempDir File folder) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(ConfigurationManager.MERGCBUS);
        memo.configureManagers();
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeXmlTest.class);

}
