package com.noahkurrack.IFCommissionsLauncher;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

    private static final String urlTrunk = "http://ifcommissions.noahkurrack.com";
    private static final String versionsUrl = urlTrunk + "/versions.xml";

    private static String newest_versionNumber;
    private static String newest_releaseType;
    private static String newest_filePath;
    private static String newestFileName;

    private static File dir;

    public static void main(String[] args) {
        System.out.println("IFCommissions Launcher initializing...");

	    //check for directory
            //if doesnt exist, create directory
        System.out.println("Establishing local directory...");
        dir = new File("IFCommissions_local");

        if (!dir.isDirectory()) {
            System.out.println("mkdir");
            dir.mkdir();
        }
        System.out.println("Done.");
        //////

        //check server for current version number (version.xml)
        System.out.println("Checking server for newest version...");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(versionsUrl).openStream());
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        if (doc != null) {
            NodeList studentNodes = doc.getElementsByTagName("version");
            int max = Integer.MIN_VALUE;
            int i_id = -1;
            for(int i=0; i<studentNodes.getLength(); i++)
            {
                Node studentNode = studentNodes.item(i);
                if(studentNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element studentElement = (Element) studentNode;
                    int id = Integer.valueOf(studentElement.getElementsByTagName("id").item(0).getTextContent());
                    String versionNumber = studentElement.getElementsByTagName("versionNumber").item(0).getTextContent();
                    String releaseType = studentElement.getElementsByTagName("releaseType").item(0).getTextContent();
                    String filePath = studentElement.getElementsByTagName("filePath").item(0).getTextContent();
                    //System.out.println("ID = " + id);
                    //System.out.println("Version Number = " + versionNumber);
                    //System.out.println("Release Type = " + releaseType);
                    //System.out.println("File Path = " + filePath);

                    if (id>max) {
                        max = id;
                        i_id = i;
                    }
                }
            }

            Element newestVersion = (Element) studentNodes.item(i_id);
            newest_versionNumber = newestVersion.getElementsByTagName("versionNumber").item(0).getTextContent();
            newest_releaseType = newestVersion.getElementsByTagName("releaseType").item(0).getTextContent();
            newest_filePath = newestVersion.getElementsByTagName("filePath").item(0).getTextContent();
            System.out.println("Newest Version Number = " + newest_versionNumber);
            System.out.println("Newest Release Type = " + newest_releaseType);
            System.out.println("Newest File Path = " + newest_filePath);

            //System.out.println("Newest version is "+newest_versionNumber+".");

        } else {
            //could not load versions.xml
            System.out.println("Could not load server version data...launching current installed version.");
        }

        ///////
        //check if jar present
        //if not, download current version
        //if present, compare to current version
        //if current, continue
        //if not current, delete jar, download current
        System.out.println("Checking current install...");
        String[] tmp = newest_filePath.split("/");
        newestFileName = tmp[tmp.length-1];
        //System.out.println(newestFileName);

        File[] existingFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (!pathname.isFile()) return false;

                return pathname.toString().contains(".jar");
            }
        });

        File currentJar;
        String finalJarName = null;

        //System.out.println(Arrays.toString(existingFiles));

        if (existingFiles != null && existingFiles.length > 0) {
            //System.out.println("a file exists");
            if (existingFiles.length > 1) {
                //delete existing
                for (File file : existingFiles) {
                    file.delete();
                }
                System.out.println("Too many files in local directory...reinstalling current version.");
                if (downloadNewJar()) {
                    finalJarName = newestFileName;
                    System.out.println("Done.");
                } else {
                    System.out.println("Download failed...manual installation needed.");
                }
            } else {
                currentJar = existingFiles[0];
                String currentVersion = currentJar.toString().split("-")[1];
                System.out.println("current version: "+currentVersion);
                //System.out.println("newest version: "+newest_versionNumber);
                if (compareVersions(newest_versionNumber, currentVersion)) {
                    System.out.println("Current version ("+currentVersion+") being replaced with newest version ("+newest_versionNumber+")....");
                    if (downloadNewJar()) {
                        currentJar.delete();
                        finalJarName = newestFileName;
                        System.out.println("Done.");
                    } else {
                        System.out.println("Download failed...reverting to installed version.");
                        finalJarName = currentJar.getName();
                    }
                } else {
                    System.out.println("Currently installed version up to date...launching...");
                    finalJarName = currentJar.getName();
                }
            }
        } else {
            System.out.println("IFCommissions not installed...downloading and installing latest version.");
            if (downloadNewJar()) {
                finalJarName = newestFileName;
                System.out.println("Done.");
            } else {
                System.out.println("Download failed...manual installation needed.");
            }
        }

        //launch jar
        try {
            if (finalJarName == null) {
                System.out.println("No version of IFCommissions installed...skipping launch sequence.");
                return;
            }
            System.out.println("Launching version ("+finalJarName+") of IFCommissions Calculator...");
            File finalJar = new File(dir.getCanonicalPath(), finalJarName);
            String[] commandArray = {"java", "-jar", finalJar.getCanonicalPath()};

            Runtime runtime = Runtime.getRuntime();
            Process javap = runtime.exec(commandArray);
            writeProcessOutput(javap);
        } catch (Exception e) {
            System.out.println("Could not launch IFCommissions Calculator...");
            e.printStackTrace();
        }

        System.out.println("Launcher exiting...");
    }

    private static void writeProcessOutput(Process process) throws Exception{
        InputStreamReader tempReader = new InputStreamReader(
                new BufferedInputStream(process.getInputStream()));
        BufferedReader reader = new BufferedReader(tempReader);
        while (true){
            String line = reader.readLine();
            if (line == null)
                break;
            System.out.println(line);
        }
    }

    //returns true if v1 greater than v2
    private static boolean compareVersions(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");

        //System.out.println(Arrays.toString(v1));
        //System.out.println(Arrays.toString(v2));

        if (Integer.valueOf(v1[0])>Integer.valueOf(v2[0])) return true;
        if (Integer.valueOf(v1[1])>Integer.valueOf(v2[1])) return true;
        return Integer.valueOf(v1[2]) > Integer.valueOf(v2[2]);

    }

    private static boolean downloadNewJar() {
        try {
            System.out.println("Downloading...");
            File out = new File(dir.getCanonicalPath(),newestFileName);
            try (InputStream in = URI.create(urlTrunk+newest_filePath).toURL().openStream()) {
                Files.copy(in, Paths.get(out.getCanonicalPath()));
            }
            return true;
        } catch (Exception e) {
            System.out.println("Could not download newest version...");
            e.printStackTrace();
            return false;
        }
    }

}