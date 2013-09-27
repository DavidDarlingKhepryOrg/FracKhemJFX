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
package com.khepry.frackhem.fxml;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;

import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.markdown4j.Markdown4jProcessor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.khepry.dialogs.Dialog;
import com.khepry.frackhem.entities.Blended;
import com.khepry.frackhem.entities.Blendeds;
import com.khepry.frackhem.entities.Chemical;
import com.khepry.frackhem.entities.Chemicals;
import com.khepry.frackhem.entities.QueryResult;
import com.khepry.frackhem.entities.Report;
import com.khepry.frackhem.entities.Reports;
import com.khepry.frackhem.entities.Toxicities;
import com.khepry.frackhem.entities.Toxicity;
import com.khepry.frackhem.etl.lucene.LuceneIndex;
import com.khepry.handlers.queue.MessageQueueMonitor;
import com.khepry.utilities.GenericUtilities;
import com.npstrandberg.simplemq.MessageInput;
import com.npstrandberg.simplemq.MessageQueue;
import com.npstrandberg.simplemq.MessageQueueService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Accordion;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class FracKhemGUIController {
	
	private Preferences preferences;
	private Properties properties = new Properties();
	
	private String initStageTitle = "FracKhemJFX";
	
	private String propFileCurrPathVal = "";
	private String propFileCurrFileVal = "FracKhemJFX.properties";

	private String propFilePrevPathKey = "propFilePrevPath";
	private String propFilePrevPathVal = "";
	
	private String propFilePrevFileKey = "propFilePrevFile";
	private String propFilePrevFileVal = "FracKhemJFX.properties";
	
	private String recentPropFilesKey = "recentPropFiles";

	private Integer recentPropFilesMaxSize = 10;
    private List<String> recentPropFilesLst = new ArrayList<>();
    
	private Integer maxToxicities = 10000;


	private Stage stage;
	
	@FXML
	private Menu mnuFileRecent; 

	@FXML
	private Accordion accordionOperators;

	@FXML
	private ScrollPane scrollPaneOperators;

	@FXML
	private Accordion accordionToxicities;

	@FXML
	private ScrollPane scrollPaneToxicities;

	@FXML
	private TextField txtFieldQueryStat0;

	@FXML
	private TextField txtFieldQueryText0;

	@FXML
	private TableView tblViewQueryResults0;

	@FXML
	private TextField txtFieldQueryStat1;

	@FXML
	private TextField txtFieldQueryText1;

	@FXML
	private TableView tblViewQueryResults1;

	@FXML
	private TextField txtFieldQueryStat2;

	@FXML
	private TextField txtFieldQueryText2;

	@FXML
	private TableView tblViewQueryResults2;

	@FXML
	private TextField txtFieldQueryStat3;

	@FXML
	private TextField txtFieldQueryText3;

	@FXML
	private TableView tblViewQueryResults3;

	@FXML
	private TextArea txtAreaMessages;

	@FXML
	private TextArea titledPaneTags;

	@FXML
	private VBox vboxTags;

	@FXML
	private TabPane tabPaneCenter;

	@FXML
	private Tab tabBlendeds;
	@FXML
	private Tab tabChemicals;
	@FXML
	private Tab tabReportss;
	@FXML
	private Tab tabToxicitiess;
	@FXML
	private Tab tabMessages;
	
	private Integer maxDocs = 50000;
	
    private String applicationTitle = "FracKhemJFX";
    private String iconLocation = "images/ScarabBeetle.jpg";
    private String minimizeMessageText = "Right-click on the icon below to show a menu, or click on this 'balloon' to restore the interface to full size.";
    
    // this path will be pre-pended to any path that doesn't have a leading slash.
    // NOTE: if a path is specified, please remember to append a trailing slash
    private String globalPathPrefix = "/home/distributions/FracKhemJFX/";
    
    private Boolean outputToSystem = Boolean.TRUE;
    private Boolean outputDebugInfo = Boolean.FALSE;
    
    private Long sleepMillis = 2000L;

    private Markdown4jProcessor markdown4jProcessor = new Markdown4jProcessor();
	
    private String htmlHeader = "<!DOCTYPE HTML5>";
    private String prevPathHtmlKey = "prevPathHTML";
    private String prevPathHtmlVal = "";
    private String prevPathPdfKey = "prevPathPDF";
    private String prevPathPdfVal = "";
    private String cssFileFullPath = "markdown-github.css";
    
    private	String recentFileName = "";
    
    private DecimalFormat df = new DecimalFormat("###,###,##0");
	
	private String progressMessageQueueName = "Progress Queue";
	private MessageQueue progressMessageQueue;
	
	private List<Thread> messageQueueMonitorThreads = new ArrayList<>();
	private Map<String, MessageQueueMonitor> messageQueueMonitors = new HashMap<>();
	
	/*
	 * NOTE: FXMLLoader will now automatically call any suitably
	 * annotated no-argument initialize() method defined by the controller.
	 * It is recommended that the injection approach be used whenever possible.
	 */
	@FXML
	public void initialize() {
		// obtain this user's preferences for this class
		String className = this.getClass().getName();
		preferences = Preferences.userRoot().node(className);
		// obtain the last properties path used
		propFileCurrPathVal = preferences.get(propFilePrevPathKey , "");
		propFileCurrPathVal = propFileCurrPathVal.equals("") ? System.getProperty("user.dir") : propFileCurrPathVal; 
		System.out.println(propFilePrevPathKey + ": " + propFileCurrPathVal);
		// obtain the last properties file used
		propFileCurrFileVal = preferences.get(propFilePrevFileKey , propFileCurrFileVal);
		System.out.println(propFilePrevFileKey + ": " + propFilePrevFileVal);
		// obtain the last HTML output path used
        prevPathHtmlVal = preferences.get(prevPathHtmlKey, "");
		prevPathHtmlVal = prevPathHtmlVal.equals("") ? System.getProperty("user.dir") : prevPathHtmlVal; 
		System.out.println(prevPathHtmlKey + ": " + prevPathHtmlVal);
		// obtain the last PDF output path used
        prevPathPdfVal = preferences.get(prevPathPdfKey, "");
		prevPathPdfVal = prevPathPdfVal.equals("") ? System.getProperty("user.dir") : prevPathPdfVal; 
		System.out.println(prevPathPdfKey + ": " + prevPathPdfVal);
		// obtain the list of recently used properties files
		String recentPropFilesVal = preferences.get(recentPropFilesKey, "");
		if (!recentPropFilesVal.equals("")) {
			String[] recentPropFilesAry = recentPropFilesVal.split(",");
			for (String recentPropFilesStr : recentPropFilesAry) {
				if (!recentPropFilesLst.contains(recentPropFilesStr)) {
					recentPropFilesLst.add(recentPropFilesStr);
				}
			}
		}
		System.out.println(recentPropFilesKey + ": " + recentPropFilesLst.toString());
		final EventHandler<ActionEvent> cancelButtonHandler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
            	event.consume();
			}
		};

		final EventHandler<ActionEvent> loadPropertiesFileHandler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (!recentFileName.equals("")) {
					openPropertiesFile(recentFileName);
				}
            	event.consume();
			}
		};

		
		// define an ActionListener to handle clicking on a recent file menu item
        final EventHandler<ActionEvent> recentFileEventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	recentFileName = ((MenuItem)event.getSource()).getText();
            	File recentFile = new File(recentFileName);
            	if (recentFile.exists()) {
	            	Dialog.buildConfirmation("Confirm loading of properties file...", recentFileName)
	                .addYesButton(loadPropertiesFileHandler)
	                .addCancelButton(cancelButtonHandler)
	                    .build()
	                        .show();
            	}
	        	else {
	        		System.out.println("Properties file: " + recentFileName + " does not exist!");
	                Dialog.showWarning("Properties file not found", recentFileName);
	        	}
            }
        };

		// load the "Recent" menu with a menu item for each recent file in the list
		for (String fileName : recentPropFilesLst) {
			MenuItem menuItem = new MenuItem(fileName);
			menuItem.setOnAction(recentFileEventHandler);
			mnuFileRecent.getItems().add(menuItem);
		}
		
		// set the stage title
        if (this.stage != null) {
            this.stage.setTitle(initStageTitle.concat(" - ").concat(propFileCurrFileVal));
        }
        File file = new File(propFileCurrFileVal);
        if (file.exists()) {
        	String text;
			try {
				text = loadFileAsText(file);
				if (text != null) {
					if (txtAreaMessages != null) {
						txtAreaMessages.setText(text);
					}
				}
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
        }
        
		// instantiate the progress message queue
		progressMessageQueue = MessageQueueService.getMessageQueue(progressMessageQueueName);
		System.out.println(progressMessageQueueName + " was initialized!");

		// instantiate a message queue monitor for the progress message queue
		MessageQueueMonitor progressQueueMonitor = new MessageQueueMonitor(progressMessageQueue, 100L, txtAreaMessages);
		Thread thread = new Thread(progressQueueMonitor, progressMessageQueueName);
		messageQueueMonitors.put(thread.getName(), progressQueueMonitor);
		messageQueueMonitorThreads.add(thread);
		thread.start();
}

	
	public void terminate() {
		onMnuFileClose(null);
	}

	
	@FXML
	private void onMnuFileClose(ActionEvent event) {
		// save the user's preferences
		savePreferences();
		// stop the message queue monitors
		System.out.println(initStageTitle + " terminating progress monitors.");
		for (Thread thread : messageQueueMonitorThreads) {
			messageQueueMonitors.get(thread.getName()).setTerminateMonitoring(true);;
		}
		// delete the progress message queue
		MessageQueueService.deleteMessageQueue(progressMessageQueueName);
		System.out.println(initStageTitle + " progress monitors terminated.");
		// signal that processing has ended
		System.out.println(initStageTitle + " terminated!");
		// exit the GUI
		Platform.exit();
		// exit the system
		System.exit(0);
	}
	
	@FXML
	private void onMnuFileNew(ActionEvent event) {
		if (txtAreaMessages != null) {
			txtAreaMessages.setText("");
		}
		propFileCurrFileVal = "";
	}
	
	@FXML
	private void onMnuFileOpen(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open a properties file...");
		ExtensionFilter extensionFilter = new ExtensionFilter("Properties files (*.properties)", "*.properties");
		fileChooser.getExtensionFilters().add(extensionFilter);
		if (!propFileCurrPathVal.equals("") || !propFilePrevPathVal.equals("")) {
			fileChooser.setInitialDirectory(new File(propFileCurrPathVal.equals("") ? propFilePrevPathVal : propFileCurrPathVal));
		}
		File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			openPropertiesFile(file.getAbsolutePath());
		}
	}
	
	@FXML
	private void onMnuFileSave(ActionEvent event) {
		if (propFileCurrFileVal.equals("")) {
			onMnuFileSaveAs(event);
		}
		else {
			try {
				File file = new File(propFileCurrFileVal);
				if (txtAreaMessages != null) {
					saveTextAsFile(txtAreaMessages.getText().trim(), file);
				}
				System.out.println("Properties file: " + file.getCanonicalPath() + " was saved.");
				savePreferences();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
	@FXML
	private void onMnuFileSaveAs(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save properties file as...");
		ExtensionFilter extensionFilter = new ExtensionFilter("Properties files (*.properties)", "*.properties");
		fileChooser.getExtensionFilters().add(extensionFilter);
		if (!propFileCurrPathVal.equals("") || !propFilePrevPathVal.equals("")) {
			fileChooser.setInitialDirectory(new File(propFileCurrPathVal.equals("") ? propFilePrevPathVal : propFileCurrPathVal));
		}
		File file = fileChooser.showSaveDialog(null);
		if (file != null) {
			try {
				try {
					if (txtAreaMessages != null) {
						saveTextAsFile(txtAreaMessages.getText().trim(), file);
					}
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
				if (stage != null) {
					stage.setTitle(initStageTitle.concat(" - ").concat(file.getCanonicalPath()));
				}
				propFileCurrFileVal = file.getCanonicalPath();
				propFileCurrPathVal = file.getParent() + "";
				System.out.println("Properties file: " + file.getCanonicalPath() + " was saved.");
				savePreferences();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}

    @FXML
    private void onMnuFileExportAsHTML(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML Files (*.html)", "*.html");
        fileChooser.getExtensionFilters().add(extFilter);
        // Set the initial folder
        if (!prevPathHtmlVal.equals("")) {
            fileChooser.setInitialDirectory(new File(prevPathHtmlVal));
        }
        //Show save file dialog
        fileChooser.setTitle("Export HTML As");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".html");
            }
            prevPathHtmlVal = file.getParent();
            stage.getScene().setCursor(Cursor.WAIT);
            try {
            	if (txtAreaMessages != null) {
            		saveTextAsHTML(txtAreaMessages.getText(), file);
            	}
			} catch (DocumentException | IOException | InterruptedException e) {
				e.printStackTrace(System.err);
			}
            stage.getScene().setCursor(Cursor.DEFAULT);
            savePreferences();
        }
    }

    @FXML
    private void onMnuFileExportAsPDF(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Adobe PDF Files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        // Set the initial folder
        if (!prevPathPdfVal.equals("")) {
            fileChooser.setInitialDirectory(new File(prevPathPdfVal));
        }
        //Show save file dialog
        fileChooser.setTitle("Export PDF As");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            prevPathPdfVal = file.getParent();
            stage.getScene().setCursor(Cursor.WAIT);
            try {
            	if (txtAreaMessages != null) {
            		saveTextAsPDF(txtAreaMessages.getText(), file);
            	}
			} catch (DocumentException | IOException | InterruptedException e) {
				e.printStackTrace(System.err);
			}
            stage.getScene().setCursor(Cursor.DEFAULT);
            savePreferences();
        }
    }

    
    @FXML
    private void onMnuIndexBlendeds(ActionEvent event) {
    	tabPaneCenter.getSelectionModel().select(tabMessages);
    	txtAreaMessages.clear();
		new Thread(new Runnable() {
			@Override
			public void run() {
	            stage.getScene().setCursor(Cursor.WAIT);
				try {
					// index and obtain toxicities
					// for later use indexing reports
		    		Toxicities toxicities = new Toxicities();
		    		toxicities.setIndexFolderPath("indexes/toxicities");
		    		toxicities.setIndexFields("toxCasEdfId,toxChemicalName,toxRecognized,toxSuspected");
		    		toxicities.setTaxonomyFolderPath("taxonomies/toxicities");
		    		toxicities.setProgressInterval(100);
		    		toxicities.setOutputDebugInfo(false);
		    		toxicities.setOutputToSystemErr(true);
		    		toxicities.setOutputToSystemOut(true);
		    		toxicities.setProgressMessageQueue(progressMessageQueue);
		    		toxicities.setOutputToMsgQueue(true);
		    		String textFilePath = "data/scorecard-goodguide-toxicities-grouped.txt";
		    		String textColSeparator = "\t";
					toxicities.indexViaLucene(textFilePath, textColSeparator);
					toxicities.queryViaLucene("toxCasEdfId", "*", maxToxicities, "toxCasEdfId", Boolean.TRUE);
					// index blendeds
					String casEdfIdFieldName =  "chmCasEdfId";
					Blendeds blendeds = new Blendeds();
					blendeds.setIndexFolderPath("indexes/blendeds");
					blendeds.setIndexFields("rptPdfSeqId,rptAPI,rptState,rptCounty,rptOperator,rptWellName,chmCasEdfId,chmTradeName,chmSupplier,chmPurpose,chmIngredients,chmComments,rptFractureDate,toxChemicalName,toxRecognized,toxSuspected");
					blendeds.setTaxonomyFolderPath("taxonomies/blendeds");
					blendeds.setProgressInterval(10000);
					blendeds.setOutputDebugInfo(false);
					blendeds.setOutputToSystemErr(true);
					blendeds.setOutputToSystemOut(true);
		    		blendeds.setProgressMessageQueue(progressMessageQueue);
		    		blendeds.setOutputToMsgQueue(true);
					textFilePath = "data/2013_FracKhem_Blendeds.txt";
					textColSeparator = "\t";
					blendeds.indexViaLucene(textFilePath, textColSeparator, casEdfIdFieldName, toxicities.getToxicitiesMap());
				} catch (IOException e) {
					e.printStackTrace(System.err);
				} catch (ParseException e) {
					e.printStackTrace(System.err);
				}
	            stage.getScene().setCursor(Cursor.DEFAULT);
			}
		}).start();
    }

    
    @FXML
    private void onMnuIndexChemicals(ActionEvent event) {
    	tabPaneCenter.getSelectionModel().select(tabMessages);
    	txtAreaMessages.clear();
		new Thread(new Runnable() {

			@Override
			public void run() {
	            stage.getScene().setCursor(Cursor.WAIT);
				try {
					// index and obtain toxicities
					// for later use indexing reports
		    		Toxicities toxicities = new Toxicities();
		    		toxicities.setIndexFolderPath("indexes/toxicities");
		    		toxicities.setIndexFields("toxCasEdfId,toxChemicalName,toxRecognized,toxSuspected");
		    		toxicities.setTaxonomyFolderPath("taxonomies/toxicities");
		    		toxicities.setProgressInterval(100);
		    		toxicities.setOutputDebugInfo(false);
		    		toxicities.setOutputToSystemErr(true);
		    		toxicities.setOutputToSystemOut(true);
		    		toxicities.setProgressMessageQueue(progressMessageQueue);
		    		toxicities.setOutputToMsgQueue(true);
		    		String textFilePath = "data/scorecard-goodguide-toxicities-grouped.txt";
		    		String textColSeparator = "\t";
					toxicities.indexViaLucene(textFilePath, textColSeparator);
					toxicities.queryViaLucene("toxCasEdfId", "*", maxToxicities, "toxCasEdfId", Boolean.TRUE);
					// index chemicals
					String casEdfIdFieldName =  "chmCasEdfId";
					Chemicals chemicals = new Chemicals();
					chemicals.setIndexFolderPath("indexes/chemicals");
					chemicals.setIndexFields("rptPdfSeqId,rptAPI,chmCasEdfId,chmTradeName,chmSupplier,chmPurpose,chmIngredients,chmComments,rptFractureDate,toxChemicalName,toxRecognized,toxSuspected");
					chemicals.setTaxonomyFolderPath("taxonomies/chemicals");
					chemicals.setProgressInterval(10000);
					chemicals.setOutputDebugInfo(false);
					chemicals.setOutputToSystemErr(true);
					chemicals.setOutputToSystemOut(true);
					chemicals.setProgressMessageQueue(progressMessageQueue);
					chemicals.setOutputToMsgQueue(true);
					textFilePath = "data/2013_FracKhem_Blendeds.txt";
					textColSeparator = "\t";
					chemicals.indexViaLucene(textFilePath, textColSeparator, casEdfIdFieldName, toxicities.getToxicitiesMap());
				} catch (IOException e) {
					e.printStackTrace(System.err);
				} catch (ParseException e) {
					e.printStackTrace(System.err);
				}
	            stage.getScene().setCursor(Cursor.DEFAULT);
			}
		}).start();
    }

    
    @FXML
    private void onMnuIndexReports(ActionEvent event) {
    	tabPaneCenter.getSelectionModel().select(tabMessages);
    	txtAreaMessages.clear();
		new Thread(new Runnable() {
			@Override
			public void run() {
	            stage.getScene().setCursor(Cursor.WAIT);
				try {
					// index and obtain toxicities
					// for later use indexing reports
		    		Toxicities toxicities = new Toxicities();
		    		toxicities.setIndexFolderPath("indexes/toxicities");
		    		toxicities.setIndexFields("toxCasEdfId,toxChemicalName,toxRecognized,toxSuspected");
		    		toxicities.setTaxonomyFolderPath("taxonomies/toxicities");
		    		toxicities.setProgressInterval(100);
		    		toxicities.setOutputDebugInfo(false);
		    		toxicities.setOutputToSystemErr(true);
		    		toxicities.setOutputToSystemOut(true);
		    		toxicities.setProgressMessageQueue(progressMessageQueue);
		    		toxicities.setOutputToMsgQueue(true);
		    		String textFilePath = "data/scorecard-goodguide-toxicities-grouped.txt";
		    		String textColSeparator = "\t";
					toxicities.indexViaLucene(textFilePath, textColSeparator);
					toxicities.queryViaLucene("toxCasEdfId", "*", maxToxicities, "toxCasEdfId", Boolean.TRUE);
					// index reports
					String casEdfIdFieldName =  "chmCasEdfId";
					Reports reports = new Reports();
					reports.setIndexFolderPath("indexes/reports");
					reports.setIndexFields("rptPdfSeqId,rptAPI,rptState,rptCounty,rptOperator,rptWellName,rptFractureDate,toxRecognized,toxSuspected");
					reports.setLevelFields("rptAPI,rptCounty,rptDatum,rptFractureDate,rptLatLng,rptLatitude,rptLongitude,rptOperator,rptProdType,rptPdfSeqId,rptPublishedDate,rptSeqId,rptState,rptTWV,rptTVD,rptWellName");
					reports.setCasEdfIdFieldName(casEdfIdFieldName);
					reports.setTaxonomyFolderPath("taxonomies/reports");
					reports.setProgressInterval(10000);
					reports.setOutputDebugInfo(false);
					reports.setOutputToSystemErr(true);
					reports.setOutputToSystemOut(true);
					reports.setProgressMessageQueue(progressMessageQueue);
					reports.setOutputToMsgQueue(true);
					textFilePath = "data/2013_FracKhem_Blendeds.txt";
					textColSeparator = "\t";
					reports.indexViaLucene(textFilePath, textColSeparator, toxicities.getToxicitiesMap(), "toxRecognized", "toxSuspected");
				} catch (IOException e) {
					e.printStackTrace(System.err);
				} catch (ParseException e) {
					e.printStackTrace(System.err);
				}
	            stage.getScene().setCursor(Cursor.DEFAULT);
			}
		}).start();
    }

    
    @FXML
    private void onMnuIndexToxicities(ActionEvent event) {
    	tabPaneCenter.getSelectionModel().select(tabMessages);
    	txtAreaMessages.clear();
		new Thread(new Runnable() {
			@Override
			public void run() {
	            stage.getScene().setCursor(Cursor.WAIT);
	            try {
		    		Toxicities toxicities = new Toxicities();
		    		toxicities.setIndexFolderPath("indexes/toxicities");
		    		toxicities.setIndexFields("toxCasEdfId,toxChemicalName,toxRecognized,toxSuspected");
		    		toxicities.setTaxonomyFolderPath("taxonomies/toxicities");
		    		toxicities.setProgressInterval(100);
		    		toxicities.setOutputDebugInfo(false);
		    		toxicities.setOutputToSystemErr(true);
		    		toxicities.setOutputToSystemOut(true);
		    		toxicities.setProgressMessageQueue(progressMessageQueue);
		    		toxicities.setOutputToMsgQueue(true);
		    		String textFilePath = "data/scorecard-goodguide-toxicities-grouped.txt";
		    		String textColSeparator = "\t";
					toxicities.indexViaLucene(textFilePath, textColSeparator);
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
	            stage.getScene().setCursor(Cursor.DEFAULT);
			}
		}).start();
    }

    
    @FXML
    private void onMnuClearBlendedsLuceneIndex(ActionEvent event) {
		String indexFolderPath = "indexes/blendeds";
		File indexFile = new File(indexFolderPath);
		if (indexFile.exists()) {
			GenericUtilities.deleteLuceneIndex(indexFile);
		}
		String taxonomyFolderPath = "taxonomies/blendeds";
		File taxonomyFile = new File(taxonomyFolderPath);
		if (taxonomyFile.exists()) {
			GenericUtilities.deleteLuceneIndex(taxonomyFile);
		}
    }

    
    @FXML
    private void onMnuClearChemicalsLuceneIndex(ActionEvent event) {
		String indexFolderPath = "indexes/chemicals";
		File indexFile = new File(indexFolderPath);
		if (indexFile.exists()) {
			GenericUtilities.deleteLuceneIndex(indexFile);
		}
		String taxonomyFolderPath = "taxonomies/chemicals";
		File taxonomyFile = new File(taxonomyFolderPath);
		if (taxonomyFile.exists()) {
			GenericUtilities.deleteLuceneIndex(taxonomyFile);
		}
    }

    
    @FXML
    private void onMnuClearReportsLuceneIndex(ActionEvent event) {
		String indexFolderPath = "indexes/reports";
		File indexFile = new File(indexFolderPath);
		if (indexFile.exists()) {
			GenericUtilities.deleteLuceneIndex(indexFile);
		}
		String taxonomyFolderPath = "taxonomies/reports";
		File taxonomyFile = new File(taxonomyFolderPath);
		if (taxonomyFile.exists()) {
			GenericUtilities.deleteLuceneIndex(taxonomyFile);
		}
    }

    
    @FXML
    private void onMnuClearToxicitiesLuceneIndex(ActionEvent event) {
		String indexFolderPath = "indexes/toxicities";
		File indexFile = new File(indexFolderPath);
		if (indexFile.exists()) {
			GenericUtilities.deleteLuceneIndex(indexFile);
		}
		String taxonomyFolderPath = "taxonomies/toxicities";
		File taxonomyFile = new File(taxonomyFolderPath);
		if (taxonomyFile.exists()) {
			GenericUtilities.deleteLuceneIndex(taxonomyFile);
		}
    }

    
    @FXML
    private void txtFieldQueryText0_onAction(ActionEvent event) {
		String indexFolderPath = "indexes/blendeds";
		String taxonomyFolderPath = "taxonomies/blendeds";
		String queryField = "text";
		String queryValue = txtFieldQueryText0.getText().trim();
		stage.getScene().setCursor(Cursor.WAIT);
		try {
			Boolean allowLeadingWildcard = Boolean.FALSE;
			String sortOrder = "rptPdfSeqId,chmRow:Integer";
			Integer maxDocs = 50000;
			Blendeds<Blended> blendeds = new Blendeds<>();
			blendeds.setIndexFolderPath(indexFolderPath);
			blendeds.setTaxonomyFolderPath(taxonomyFolderPath);
			QueryResult queryResult = blendeds.queryViaLucene(queryField, queryValue, maxDocs, sortOrder, allowLeadingWildcard);
			txtFieldQueryStat0.setText(queryResult.getCommentary());
			// build the table column headers
			List<TableColumn<Map,String>> dataColumns = new ArrayList<>();
			for (org.apache.lucene.document.Document document : queryResult.getDocuments()) {
				for (IndexableField field : document.getFields()) {
					TableColumn<Map,String> dataColumn = new TableColumn<>(field.name());
					dataColumn.setCellValueFactory(new MapValueFactory(field.name()));
					dataColumn.setSortable(true);
					dataColumns.add(dataColumn);
				}
				break;
			}
			tblViewQueryResults0.getItems().clear();
			// populate the table with data
			tblViewQueryResults0.setItems(blendeds);
			// add the table column headers
			tblViewQueryResults0.getColumns().setAll(blendeds.getTableColumns());
		} catch (IOException | ParseException e) {
			e.printStackTrace(System.err);
		}
		stage.getScene().setCursor(Cursor.DEFAULT);
    }

    
    @FXML
    private void txtFieldQueryText1_onAction(ActionEvent event) {
		String indexFolderPath = "indexes/chemicals";
		String taxonomyFolderPath = "taxonomies/chemicals";
		String queryField = "text";
		String queryValue = txtFieldQueryText1.getText().trim();
		stage.getScene().setCursor(Cursor.WAIT);
		try {
			Boolean allowLeadingWildcard = Boolean.FALSE;
			String sortOrder = "rptPdfSeqId,chmRow:Integer";
			Integer maxDocs = 50000;
			Chemicals<Chemical> chemicals = new Chemicals<>();
			chemicals.setIndexFolderPath(indexFolderPath);
			chemicals.setTaxonomyFolderPath(taxonomyFolderPath);
			QueryResult queryResult = chemicals.queryViaLucene(queryField, queryValue, maxDocs, sortOrder, allowLeadingWildcard);
			txtFieldQueryStat1.setText(queryResult.getCommentary());
			// build the table column headers
			List<TableColumn<Map,String>> dataColumns = new ArrayList<>();
			for (org.apache.lucene.document.Document document : queryResult.getDocuments()) {
				for (IndexableField field : document.getFields()) {
					TableColumn<Map,String> dataColumn = new TableColumn<>(field.name());
					dataColumn.setCellValueFactory(new MapValueFactory(field.name()));
					dataColumn.setSortable(true);
					dataColumns.add(dataColumn);
				}
				break;
			}
			tblViewQueryResults1.getItems().clear();
			// populate the table with data
			tblViewQueryResults1.setItems(chemicals);
			// add the table column headers
			tblViewQueryResults1.getColumns().setAll(chemicals.getTableColumns());
		} catch (IOException | ParseException e) {
			e.printStackTrace(System.err);
		}
		stage.getScene().setCursor(Cursor.DEFAULT);
    }
    
    
    @FXML
    private void txtFieldQueryText2_onAction(ActionEvent event) {
		String indexFolderPath = "indexes/reports";
		String taxonomyFolderPath = "taxonomies/reports";
		String queryField = "text";
		String queryValue = txtFieldQueryText2.getText().trim();
		stage.getScene().setCursor(Cursor.WAIT);
		try {
			Boolean allowLeadingWildcard = Boolean.FALSE;
			String sortOrder = "rptPdfSeqId";
			Integer maxDocs = 50000;
			Reports<Report> reports = new Reports<>();
			reports.setIndexFolderPath(indexFolderPath);
			reports.setTaxonomyFolderPath(taxonomyFolderPath);
			QueryResult queryResult = reports.queryViaLucene(queryField, queryValue, maxDocs, sortOrder, allowLeadingWildcard);
			txtFieldQueryStat2.setText(queryResult.getCommentary());
			// build the table column headers
			List<TableColumn<Map,String>> dataColumns = new ArrayList<>();
			for (org.apache.lucene.document.Document document : queryResult.getDocuments()) {
				for (IndexableField field : document.getFields()) {
					TableColumn<Map,String> dataColumn = new TableColumn<>(field.name());
					dataColumn.setCellValueFactory(new MapValueFactory(field.name()));
					dataColumn.setSortable(true);
					dataColumns.add(dataColumn);
				}
				break;
			}
			tblViewQueryResults2.getItems().clear();
			// populate the table with data
			tblViewQueryResults2.setItems(reports);
			// add the table column headers
			tblViewQueryResults2.getColumns().setAll(reports.getTableColumns());
		} catch (IOException | ParseException e) {
			e.printStackTrace(System.err);
		}
		stage.getScene().setCursor(Cursor.DEFAULT);
    }
    
    
    @FXML
    private void txtFieldQueryText3_onAction(ActionEvent event) {
		String indexFolderPath = "indexes/toxicities";
		String taxonomyFolderPath = "taxonomies/toxicities";
		String queryField = "text";
		String queryValue = txtFieldQueryText3.getText().trim();
		stage.getScene().setCursor(Cursor.WAIT);
		try {

			Boolean allowLeadingWildcard = Boolean.FALSE;
			String sortOrder = "toxCasEdfId";
			Integer maxDocs = 10000;
			Toxicities<Toxicity> toxicities = new Toxicities<>();
			toxicities.setIndexFolderPath(indexFolderPath);
			toxicities.setTaxonomyFolderPath(taxonomyFolderPath);
			QueryResult queryResult = toxicities.queryViaLucene(queryField, queryValue, maxDocs, sortOrder, allowLeadingWildcard);
			txtFieldQueryStat3.setText(queryResult.getCommentary());
			// build the table column headers
			List<TableColumn<Map,String>> dataColumns = new ArrayList<>();
			for (org.apache.lucene.document.Document document : queryResult.getDocuments()) {
				for (IndexableField field : document.getFields()) {
					TableColumn<Map,String> dataColumn = new TableColumn<>(field.name());
					dataColumn.setCellValueFactory(new MapValueFactory(field.name()));
					dataColumn.setSortable(true);
					dataColumns.add(dataColumn);
					if (outputDebugInfo) {
						System.out.println("Toxicity column name: " + field.name());
					}
				}
				break;
			}
			tblViewQueryResults3.getItems().clear();
			// populate the table with data
			tblViewQueryResults3.setItems(toxicities);
			// add the table column headers
			List<TableColumn> tableColumns = toxicities.getTableColumns();
			if (outputDebugInfo) {
				for (TableColumn tableColumn : tableColumns) {
					System.out.println("Property text: " + tableColumn.textProperty().get());
				}
			}
			tblViewQueryResults3.getColumns().setAll(tableColumns);
		} catch (IOException | ParseException e) {
			e.printStackTrace(System.err);
		}
		stage.getScene().setCursor(Cursor.DEFAULT);
    }

    
    private String loadFileAsText(File file) throws IOException {
        String text = "";
        if (file.exists()) {
        	if (file.length() > 0L) {
	            try {
	            	Scanner scanner = new Scanner(file);
	            	scanner.useDelimiter("\\Z");
	                text = scanner.next();
	                scanner.close();
	            } catch (FileNotFoundException ex) {
	                text = ex.getLocalizedMessage();
	                
	            }
        	}
        } else {
            text = file.getCanonicalPath().concat(" does not exist!");
        }
        return text;
    }

    
    private void openPropertiesFile(String fileName) {
		File file = new File(fileName);
		if (file != null && file.exists()) {
			try {
				properties.load(new FileReader(file));
				if (stage != null) {
					stage.setTitle(initStageTitle.concat(" = ").concat(file.getCanonicalPath()));
				}
				propFileCurrFileVal = file.getCanonicalPath();
				propFileCurrPathVal = file.getParent() + "";
				System.out.println("Properties file: " + file.getCanonicalPath() + " was loaded.");
				String text = loadFileAsText(file);
				if (txtAreaMessages != null) {
					txtAreaMessages.setText(text);
				}
				savePreferences();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
		else {
			System.out.println("Properties file: " + fileName + " does not exist!");
        	Dialog.showWarning("Properties file not found", recentFileName);
		}
    	
    }

    
    private void saveTextAsFile(String content, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        }
    }
    
    private void saveTextAsHTML(String content, File file) throws FileNotFoundException, DocumentException, IOException, InterruptedException {
        String html = getHTMLFromText(content);
        saveTextAsFile(html, file);
        displayFile(file.getAbsolutePath(), sleepMillis);
    }
    
    private void saveTextAsPDF(String content, File file) throws FileNotFoundException, DocumentException, IOException, InterruptedException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        document.addAuthor("Author of the Doc");
        document.addCreator("Creator of the Doc");
        document.addSubject("Subject of the Doc");
        document.addCreationDate();
        document.addTitle(file.getName());
        String html = htmlHeader.concat(markdown4jProcessor.process(content.trim()));
//        XMLWorkerHelper.getInstance().parseXHtml(pdfWriter, document, new ByteArrayInputStream(html.getBytes("UTF-8")), this.getClass().getResourceAsStream(cssFileFullPath));
        XMLWorkerHelper.getInstance().parseXHtml(pdfWriter, document, new ByteArrayInputStream(html.getBytes("UTF-8")));
        document.close();
        pdfWriter.close();
        displayFile(file.getAbsolutePath(), sleepMillis);
    }
    
    private String getHTMLFromText(String content) throws IOException {
        String styleText = convertStreamToString(this.getClass().getResourceAsStream(cssFileFullPath));
        String htmlPrefix = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">";
        String html = htmlPrefix.concat("\n<head>\n<style type='text/css'>\n").concat(styleText).concat("\n</style>\n</head>\n<body>\n");
        html = htmlHeader.concat(html).concat(markdown4jProcessor.process(content.trim())).concat("\n</body>\n</html>");
        return html;
    }
    
    private String convertStreamToString(InputStream is) {
    	Scanner scanner = new Scanner(is);
    	scanner.useDelimiter("\\A");
        String result = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return result;
    }
    
    private void displayFile(String filePath, long sleepMillis) throws IOException, InterruptedException {
        // only display the file
        // if the sleepMillis property
        // is greater than zero milliseconds
        if (sleepMillis > 0) {
            Thread.sleep(sleepMillis);
            File file = new File(filePath);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }
            else {
                new FileNotFoundException(filePath).printStackTrace(System.err);
            }
        }
    }
    
    private void savePreferences() {
		preferences.put(propFilePrevPathKey, propFileCurrPathVal.replace("\\", "/"));
		System.out.println(propFilePrevPathKey + ": " + propFileCurrPathVal.replace("\\", "/"));
		preferences.put(propFilePrevFileKey, propFileCurrFileVal.replace("\\", "/"));
		System.out.println(propFilePrevFileKey + ": " + propFileCurrFileVal.replace("\\", "/"));
		preferences.put(prevPathHtmlKey, prevPathHtmlVal.replace("\\", "/"));
		System.out.println(prevPathHtmlKey + ": " + prevPathHtmlVal.replace("\\", "/"));
		preferences.put(prevPathPdfKey, prevPathPdfVal.replace("\\", "/"));
		System.out.println(prevPathPdfKey + ": " + prevPathPdfVal.replace("\\", "/"));
		// save the recent files
		if (!recentPropFilesLst.contains(propFileCurrFileVal.replace("\\", "/"))) {
			while (recentPropFilesLst.size() >= recentPropFilesMaxSize) {
				recentPropFilesLst.remove(0);
			}
			recentPropFilesLst.add(propFileCurrFileVal.replace("\\", "/"));
			preferences.put(recentPropFilesKey, GenericUtilities.joinString(recentPropFilesLst, ","));
		}
    }

    public void setInitStageTitle(String initStageTitle) {
        this.initStageTitle = initStageTitle;
        if (this.stage != null) {
            this.stage.setTitle(initStageTitle.concat(" - ").concat(propFileCurrFileVal));
        }
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
        if (this.stage != null) {
            this.stage.setTitle(initStageTitle.concat(" - ").concat(propFileCurrFileVal));
        }
    }
	
	public String getPropFileCurrFileVal() {
		return propFileCurrFileVal;
	}


	public void setPropFileCurrFileVal(String propFileCurrFileVal) {
		this.propFileCurrFileVal = propFileCurrFileVal;
	}
	
	public Integer getMaxToxicities() {
		return maxToxicities;
	}


	public void setMaxToxicities(Integer maxToxicities) {
		this.maxToxicities = maxToxicities;
	}
	
}
