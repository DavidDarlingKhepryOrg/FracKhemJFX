/*******************************************************************************
 * Copyright 2013 Khepry Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.khepry.utilities;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;

public class GenericUtilities {
	
    public static void displayLogFile(String logFilePath, long logSleepMillis, String xsltFilePath, String xsldFilePath) {
        Logger.getLogger("").getHandlers()[0].close();
        // only display the log file
        // if the logSleepMillis property
        // is greater than zero milliseconds
        if (logSleepMillis > 0) {
            try {
                Thread.sleep(logSleepMillis);
                File logFile = new File(logFilePath);
                File xsltFile = new File(xsltFilePath);
                File xsldFile = new File(xsldFilePath);
                File tmpFile = File.createTempFile("tmpLogFile", ".xhtml", logFile.getParentFile());
                if (logFile.exists()) {
                	if (xsltFile.exists() && xsldFile.exists()) {
                		String xslFilePath;
                		String xslFileName;
                		String dtdFilePath;
                		String dtdFileName;
                		try {
                			xslFileName = new File(logFilePath).getName().replace(".xhtml",".xsl");
                			xslFilePath = logFile.getParentFile().toString().concat("/").concat(xslFileName);
                			FileUtils.copyFile(new File(xsltFilePath), new File(xslFilePath));
                			dtdFileName = new File(logFilePath).getName().replace(".xhtml",".dtd");
                			dtdFilePath = logFile.getParentFile().toString().concat("/").concat(dtdFileName);
                			FileUtils.copyFile(new File(xsldFilePath), new File(dtdFilePath));
                		} catch (IOException ex) {
                			String message = Level.SEVERE.toString().concat(": ").concat(ex.getLocalizedMessage());
                			Logger.getLogger(GenericUtilities.class.getName()).log(Level.SEVERE, message, ex);
                			GenericUtilities.outputToSystemErr(message, logSleepMillis > 0);
                			return;
                		}
                		BufferedWriter bw = Files.newBufferedWriter(Paths.get(tmpFile.getAbsolutePath()), Charset.defaultCharset(), StandardOpenOption.CREATE);
                		List<String> logLines = Files.readAllLines(Paths.get(logFilePath), Charset.defaultCharset());
                		for (String line : logLines) {
                			if (line.startsWith("<!DOCTYPE log SYSTEM \"logger.dtd\">")) {
                				bw.write("<!DOCTYPE log SYSTEM \"" + dtdFileName + "\">\n");
                				bw.write("<?xml-stylesheet type=\"text/xsl\" href=\"" + xslFileName + "\"?>\n");
                			}
                			else {
                				bw.write(line.concat("\n"));
                			}
                		}
                		bw.write("</log>\n");
                		bw.close();
                	}
                	// the following statement is commented out because it's not quite ready for prime-time yet
                	// Files.write(Paths.get(logFilePath), transformLogViaXSLT(logFilePath, xsltFilePath).getBytes(), StandardOpenOption.CREATE);
                    Desktop.getDesktop().open(tmpFile);
                } else {
                    Logger.getLogger(GenericUtilities.class.getName()).log(Level.SEVERE, logFilePath, new FileNotFoundException());
                }
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(GenericUtilities.class.getName()).log(Level.SEVERE, null, ex);
			}
        }
    }

    
    public static void displayOutFile(
            String outFilePath,
            Handler handler) {
    	if (handler != null) {
    		Logger.getLogger("").addHandler(handler);
    	}
        File outFile = new File(outFilePath);
        if (outFile.exists()) {
            try {
				Desktop.getDesktop().open(outFile);
			} catch (IOException ex) {
                Logger.getLogger(GenericUtilities.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
        }
        else {
            Logger.getLogger(GenericUtilities.class.getName()).log(Level.SEVERE, outFilePath, new FileNotFoundException());
        }
    }
    

    public static void generateOutFile(
            String outFilePath,
            String outputText,
            Handler handler) {
        Logger.getLogger("").addHandler(handler);
        File outFile = new File(outFilePath);
        if (outFile.getParentFile().exists()) {
        	try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(outFilePath));
				bw.write(outputText);
				bw.close();
			} catch (IOException ex) {
	            Logger.getLogger(GenericUtilities.class.getName()).log(Level.SEVERE, null, ex);
			}
        }
        else {
            Logger.getLogger(GenericUtilities.class.getName()).log(Level.SEVERE, "Output path: " + outFile.getParent() + " does not exist!", new FileNotFoundException());
        }
    }

    
	public static Map<Integer, String> getAllJdbcTypeNames() {
	    Map<Integer, String> result = new HashMap<Integer, String>();
	    try {
		    for (Field field : Types.class.getFields()) {
		        result.put((Integer)field.get(null), field.getName());
		    }
		} catch (IllegalArgumentException | IllegalAccessException ex) {
	        Logger.getLogger(GenericUtilities.class.getName()).log(Level.SEVERE, null, ex);
		}
	    return result;
	}

    
	public static boolean isNumeric(String value) {
		boolean isNumeric = true;
		char[] chars = value.toLowerCase().toCharArray();
		for (char c : chars) {
			if (c < '0' || c > '9') {
				isNumeric = false;
				break;
			}
		}
		return isNumeric;
	}


    public static String joinString(Collection<String> values, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(delimiter);
        }
        if (sb.length() >= delimiter.length()) {
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }


    public static String joinString(List<String> values, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(delimiter);
        }
        if (sb.length() >= delimiter.length()) {
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    
    public static String joinString(String[] values, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(delimiter);
        }
        if (sb.length() >= delimiter.length()) {
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }
    

    public static String replaceTildePrefixWithUserHome(String path, String prefix) {
    	String result = "";
    	// if the prefix is non-null and not blank and starts with a tilde (~)
    	if (prefix != null && !prefix.equals("") && prefix.startsWith("~")) {
    		// replace the tilde with the user's home folder
    		prefix = prefix.replace("~", System.getProperty("user.home"));
    	}
    	// if the prefix is blank or the path starts with a tilde (~)
    	if (prefix == null || prefix.equals("") || path.startsWith("~")) {
    		// if the path starts with a tilde (~), replace it with the user's home folder
    		// otherwise, simply use the path from the incoming path parameter
    		result = path.startsWith("~") ? path.replace("~", System.getProperty("user.home")) : path; 
    	}
    	// otherwise,
    	else {
    		// if the path starts with a slash or backslash or contains a colon, use the path as provided
    		// otherwise, pre-pend any non-null prefix to the path from the incoming path parameter
    		result = (path.startsWith("/") || path.startsWith("\\") || path.indexOf(":") > -1) ? path : prefix != null ? prefix.concat(path) : path;
    	}
    	// System.out.println("replaceTildePrefixWithUserHome:: Path: " + path + ", Prefix: " + prefix + ", Result: " + result);
        return result;
    }

    
    public static void outputToSystemErr(String message, Boolean systemErrorsDesired) {
        if (systemErrorsDesired) {
        	System.err.println(message);
        }
    }
    
    public static void outputToSystemOut(String message, Boolean systemOutputDesired) {
        if (systemOutputDesired) {
        	System.out.println(message);
        }
    }
	
    
	public static String startsWithPattern(String value, String matchingPatterns) {
		return startsWithPattern(value, matchingPatterns, ",");
	}
	
	public static String startsWithPattern(String value, String matchingPatterns, String separator) {
		String patternMatched = null;
		// only check for pattern matches
		// if there are patterns to check
		if (!matchingPatterns.equals("")) {
			String[] patterns = matchingPatterns.split(separator);
			for (String pattern : patterns) {
				if (value.toLowerCase().startsWith(pattern)) {
					patternMatched = pattern;
					break;
				}
			}
		}
		return patternMatched;
	}
    
	
    public static String transformLogViaXSLT(String logFilePath, String xslFilePath)
    		throws TransformerConfigurationException, TransformerException, IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	File logFile = new File(logFilePath);
    	if (logFile.exists()) {
	    	File xslFile = new File(xslFilePath);
	    	if (xslFile.exists()) {
		        TransformerFactory factory = TransformerFactory.newInstance();
		        Source xslt = new StreamSource(new File(xslFilePath));
		        Transformer transformer = factory.newTransformer(xslt);
		        Source logXmlText = new StreamSource(new File(logFilePath));
		        transformer.transform(logXmlText, new StreamResult(baos));
		        return baos.toString();
	    	}
	    	else {
		        return new String(Files.readAllBytes(Paths.get(logFilePath)));
	    	}
    	}
    	else {
    		return baos.toString();
    	}
    }
}
