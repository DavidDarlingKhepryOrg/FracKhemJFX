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
			    	String message;
			    	// index toxicities
			    	message = "Start Indexing Toxicities via Lucene...";
//			    	System.out.println(message);
					progressMessageQueue.send(new MessageInput(message));
					String txtPath = "data/scorecard-goodguide-toxicities-grouped.txt";
					String colSeparator = "\t";
					String indexPath = "lucene/toxicities";
					String indexFields = "toxCasEdfId,toxChemicalName,toxRecognized,toxSuspected";
					Integer progressInterval = 100;
					LuceneIndex luceneIndex = new LuceneIndex(progressMessageQueue);
					luceneIndex.load(txtPath, colSeparator, indexPath, indexFields, progressInterval, txtAreaMessages);
					message = "Ended Indexing Toxicities via Lucene!";
					System.out.println(message);
					txtAreaMessages.appendText(System.lineSeparator() + message);
					// index chemicals
			    	message = "Start Indexing Blendeds via Lucene...";
			    	System.out.println(message);
					txtAreaMessages.setText(message);
					txtPath = "data/2013_FracKhem_Blendeds.txt";
					colSeparator = "\t";
					indexPath = "lucene/blendeds";
					indexFields = "rptPdfSeqId,rptAPI,rptState,rptCounty,rptOperator,rptWellName,chmCasEdfId,chmTradeName,chmSupplier,chmPurpose,chmIngredients,chmComments,rptFractureDate,toxChemicalName,toxRecognized,toxSuspected";
					String taxonomyPath = "lucene/taxonomies";
					String casEdfIdFieldName =  "chmCasEdfId";
					progressInterval = 10000;
					List<org.apache.lucene.document.Document> documents = luceneIndex.query("lucene/toxicities", "text", "*", maxDocs, Boolean.TRUE);
					Map<String,Toxicity> toxicities = new Toxicities(documents).getToxicitiesMap();
					luceneIndex.load(txtPath, colSeparator, indexPath, indexFields, progressInterval, txtAreaMessages, toxicities);
					message = "Ended Indexing Blendeds via Lucene!";
//					System.out.println(message);
					progressMessageQueue.send(new MessageInput(message));
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
			    	String message;
			    	// index toxicities
			    	message = "Start Indexing Toxicities via Lucene...";
//			    	System.out.println(message);
					progressMessageQueue.send(new MessageInput(message));
					String txtPath = "data/scorecard-goodguide-toxicities-grouped.txt";
					String colSeparator = "\t";
					String indexPath = "lucene/toxicities";
					String indexFields = "toxCasEdfId,toxChemicalName,toxRecognized,toxSuspected";
					Integer progressInterval = 100;
					LuceneIndex luceneIndex = new LuceneIndex(progressMessageQueue);
					luceneIndex.load(txtPath, colSeparator, indexPath, indexFields, progressInterval, txtAreaMessages);
					message = "Ended Indexing Toxicities via Lucene!";
					System.out.println(message);
					txtAreaMessages.appendText(System.lineSeparator() + message);
					// index chemicals
					message = "Start Indexing Chemicals via Lucene...";
			    	System.out.println(message);
					txtAreaMessages.setText(message);
					txtPath = "data/2013_FracKhem_Blendeds.txt";
					colSeparator = "\t";
					indexPath = "lucene/chemicals";
					indexFields = "rptPdfSeqId,rptAPI,chmCasEdfId,chmTradeName,chmSupplier,chmPurpose,chmIngredients,chmComments,rptFractureDate,toxChemicalName,toxRecognized,toxSuspected";
					String taxonomyPath = "lucene/taxonomies";
					String casEdfIdFieldName =  "chmCasEdfId";
					progressInterval = 10000;
					List<org.apache.lucene.document.Document> documents = luceneIndex.query("lucene/toxicities", "text", "*", maxDocs, Boolean.TRUE);
					Map<String,Toxicity> toxicities = new Toxicities(documents).getToxicitiesMap();
					luceneIndex.load(txtPath, colSeparator, indexPath, indexFields, progressInterval, txtAreaMessages, toxicities);
					message = "Ended Indexing Chemicals via Lucene!";
//					System.out.println(message);
					progressMessageQueue.send(new MessageInput(message));
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
			    	String message;
			    	// index toxicities
			    	message = "Start Indexing Toxicities via Lucene...";
//			    	System.out.println(message);
					progressMessageQueue.send(new MessageInput(message));
					String txtPath = "data/scorecard-goodguide-toxicities-grouped.txt";
					String colSeparator = "\t";
					String indexPath = "lucene/toxicities";
					String indexFields = "toxCasEdfId,toxChemicalName,toxRecognized,toxSuspected";
					Integer progressInterval = 100;
					LuceneIndex luceneIndex = new LuceneIndex(progressMessageQueue);
					luceneIndex.load(txtPath, colSeparator, indexPath, indexFields, progressInterval, txtAreaMessages);
					message = "Ended Indexing Toxicities via Lucene!";
					System.out.println(message);
					txtAreaMessages.appendText(System.lineSeparator() + message);
					// index reports
			    	message = "Start Indexing Reports via Lucene...";
			    	System.out.println(message);
					txtAreaMessages.setText(message);
					txtPath = "data/2013_FracKhem_Blendeds.txt";
					colSeparator = "\t";
					indexPath = "lucene/reports";
					indexFields = "rptPdfSeqId,rptAPI,rptState,rptCounty,rptOperator,rptWellName,rptFractureDate,toxRecognized,toxSuspected";
					String levelFields = "rptAPI,rptCounty,rptDatum,rptFractureDate,rptLatLng,rptLatitude,rptLongitude,rptOperator,rptProdType,rptPdfSeqId,rptPublishedDate,rptSeqId,rptState,rptTWV,rptTVD,rptWellName";
					String taxonomyPath = "lucene/taxonomies";
					String casEdfIdFieldName =  "chmCasEdfId";
					progressInterval = 10000;
					List<org.apache.lucene.document.Document> documents = luceneIndex.query("lucene/toxicities", "text", "*", maxDocs, Boolean.TRUE);
					Map<String,Toxicity> toxicities = new Toxicities(documents).getToxicitiesMap();
					luceneIndex.load(txtPath, colSeparator, indexPath, indexFields, casEdfIdFieldName, taxonomyPath, progressInterval, txtAreaMessages, toxicities, levelFields, "toxRecognized", "toxSuspected");
					message = "Ended Indexing Reports via Lucene!";
//					System.out.println(message);
					progressMessageQueue.send(new MessageInput(message));
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
			    	String message;
			    	message = "Start Indexing Toxicities via Lucene...";
//			    	System.out.println(message);
					progressMessageQueue.send(new MessageInput(message));
					String txtPath = "data/scorecard-goodguide-toxicities-grouped.txt";
					String colSeparator = "\t";
					String indexPath = "lucene/toxicities";
					String indexFields = "toxCasEdfId,toxChemicalName,toxRecognized,toxSuspected";
					Integer progressInterval = 100;
					LuceneIndex luceneIndex = new LuceneIndex(progressMessageQueue);
					luceneIndex.load(txtPath, colSeparator, indexPath, indexFields, progressInterval, txtAreaMessages);
					message = "Ended Indexing Toxicities via Lucene!";
//					System.out.println(message);
					progressMessageQueue.send(new MessageInput(message));
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
	            stage.getScene().setCursor(Cursor.DEFAULT);
			}
		}).start();
    }

    
    @FXML
    private void onMnuClearBlendedsLuceneIndex(ActionEvent event) {
		String indexPath = "lucene/blendeds";
		LuceneIndex luceneIndex = new LuceneIndex();
		File indexFile = new File(indexPath);
		if (indexFile.exists()) {
			luceneIndex.deleteLuceneIndex(indexFile);
		}
    }

    
    @FXML
    private void onMnuClearChemicalsLuceneIndex(ActionEvent event) {
		String indexPath = "lucene/chemicals";
		LuceneIndex luceneIndex = new LuceneIndex();
		File indexFile = new File(indexPath);
		if (indexFile.exists()) {
			luceneIndex.deleteLuceneIndex(indexFile);
		}
    }

    
    @FXML
    private void onMnuClearReportsLuceneIndex(ActionEvent event) {
		String indexPath = "lucene/reports";
		LuceneIndex luceneIndex = new LuceneIndex();
		File indexFile = new File(indexPath);
		if (indexFile.exists()) {
			luceneIndex.deleteLuceneIndex(indexFile);
		}
    }

    
    @FXML
    private void onMnuClearToxicitiesLuceneIndex(ActionEvent event) {
		String indexPath = "lucene/toxicities";
		LuceneIndex luceneIndex = new LuceneIndex();
		File indexFile = new File(indexPath);
		if (indexFile.exists()) {
			luceneIndex.deleteLuceneIndex(indexFile);
		}
    }

    
    @FXML
    private void txtFieldQueryText0_onAction(ActionEvent event) {
		String indexPath = "lucene/blendeds";
		String queryField = "text";
		String queryText = txtFieldQueryText0.getText().trim();
		stage.getScene().setCursor(Cursor.WAIT);
		try {
		    LuceneIndex luceneIndex = new LuceneIndex();
			List<org.apache.lucene.document.Document> documents;
			Long bgnMillis = System.currentTimeMillis();
			documents = luceneIndex.query(indexPath, queryField, queryText, maxDocs, Boolean.FALSE, "rptPdfSeqId", "chmRow:Integer");
			Long endMillis = System.currentTimeMillis();
			Long elapsedMillis = endMillis - bgnMillis;
			String statText = documents.size() + " disclosed reports+chemicals retrieved in " + (elapsedMillis * 1.0 / 1000) + " seconds (" + df.format((documents.size() * 1000.0) / elapsedMillis) + " records/second).";
			txtFieldQueryStat0.setText(statText);
			// populate the ObservableList for
			// later usage in the TableView control
			Blendeds<Blended> blendeds = new Blendeds<>(documents);
			// build the table column headers
			List<TableColumn<Map,String>> dataColumns = new ArrayList<>();
			for (org.apache.lucene.document.Document document : documents) {
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
		String indexPath = "lucene/chemicals";
		String queryField = "text";
		String queryText = txtFieldQueryText1.getText().trim();
		stage.getScene().setCursor(Cursor.WAIT);
		try {
		    LuceneIndex luceneIndex = new LuceneIndex();
			List<org.apache.lucene.document.Document> documents;
			Long bgnMillis = System.currentTimeMillis();
			documents = luceneIndex.query(indexPath, queryField, queryText, maxDocs, Boolean.FALSE, "rptPdfSeqId", "chmRow:Integer");
			Long endMillis = System.currentTimeMillis();
			Long elapsedMillis = endMillis - bgnMillis;
			String statText = documents.size() + " disclosed chemicals retrieved in " + (elapsedMillis * 1.0 / 1000) + " seconds (" + df.format((documents.size() * 1000.0) / elapsedMillis) + " records/second).";
			txtFieldQueryStat1.setText(statText);
			// populate the ObservableList for
			// later usage in the TableView control
			Chemicals<Chemical> chemicals = new Chemicals<>(documents);
			// build the table column headers
			List<TableColumn<Map,String>> dataColumns = new ArrayList<>();
			for (org.apache.lucene.document.Document document : documents) {
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
		String indexPath = "lucene/reports";
		String queryField = "text";
		String queryText = txtFieldQueryText2.getText().trim();
		stage.getScene().setCursor(Cursor.WAIT);
		try {
		    LuceneIndex luceneIndex = new LuceneIndex();
			List<org.apache.lucene.document.Document> documents;
			Long bgnMillis = System.currentTimeMillis();
			documents = luceneIndex.query(indexPath, queryField, queryText, maxDocs, Boolean.FALSE, "rptPdfSeqId");
			Long endMillis = System.currentTimeMillis();
			Long elapsedMillis = endMillis - bgnMillis;
			String statText = documents.size() + " disclosed reports retrieved in " + (elapsedMillis * 1.0 / 1000) + " seconds (" + df.format((documents.size() * 1000.0) / elapsedMillis) + " records/second).";
			txtFieldQueryStat2.setText(statText);
			// populate the ObservableList for
			// later usage in the TableView control
			Reports<Report> reports = new Reports<>(documents);
			// build the table column headers
			List<TableColumn<Map,String>> dataColumns = new ArrayList<>();
			for (org.apache.lucene.document.Document document : documents) {
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
		String indexPath = "lucene/toxicities";
		String queryField = "text";
		String queryText = txtFieldQueryText3.getText().trim();
		stage.getScene().setCursor(Cursor.WAIT);
		try {
		    LuceneIndex luceneIndex = new LuceneIndex();
			List<org.apache.lucene.document.Document> documents;
			Long bgnMillis = System.currentTimeMillis();
			documents = luceneIndex.query(indexPath, queryField, queryText, maxDocs, Boolean.FALSE, "toxCasEdfId");
			Long endMillis = System.currentTimeMillis();
			Long elapsedMillis = endMillis - bgnMillis;
			String statText = documents.size() + " toxic chemicals retrieved in " + (elapsedMillis * 1.0 / 1000) + " seconds (" + df.format((documents.size() * 1000.0) / elapsedMillis) + " records/second).";
			txtFieldQueryStat3.setText(statText);
			// populate the ObservableList for
			// later usage in the TableView control
			Toxicities<Toxicity> toxicities = new Toxicities<>(documents);
			// build the table column headers
			List<TableColumn<Map,String>> dataColumns = new ArrayList<>();
			for (org.apache.lucene.document.Document document : documents) {
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
	
}
