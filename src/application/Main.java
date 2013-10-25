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
package application;
	

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
//import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.khepry.frackhem.fxml.FracKhemGUIController;
import com.khepry.utilities.GenericUtilities;

public class Main extends Application {

    private boolean firstTime;
    private TrayIcon trayIcon;
    private String applicationTitle = "FracKhemJFX";
    private String iconLocation = "/com/khepry/images/ScarabBeetle.jpg";
    private String minimizeMessageText = "Right-click on the icon below to show a menu, or click on this 'balloon' to restore the interface to full size.";
	
	private String fxmlPath = "/com/khepry/frackhem/fxml";
	private String fxmlXmlFileName = "FracKhemGUI.fxml";
	private String fxmlCssFileName = "application.css";
	
	private Boolean systemErrorsDesired = Boolean.TRUE;
	private Boolean systemOutputDesired = Boolean.TRUE;
	
	FracKhemGUIController controller;
	
	@Override
	public void start(Stage primaryStage) throws InterruptedException {

		System.out.println(applicationTitle + " initialized!");

		// define working variables
		String text = "";
		Boolean terminateExecution = Boolean.FALSE;
		
		// does the specified FXML XML file path exist?
		String fxmlXmlFileFullPath = fxmlPath.equals("") ? fxmlXmlFileName : fxmlPath + "/" + fxmlXmlFileName;
		URL urlXml = Main.class.getResource(fxmlXmlFileFullPath);
		if (urlXml == null) {
			terminateExecution = Boolean.TRUE;
			text = Main.class.getName() + ": FXML XML File '" + fxmlXmlFileFullPath + "' not found in resource path!";
			GenericUtilities.outputToSystemErr(text, systemErrorsDesired);
		}

		// does the specified FXML CSS file path exist?
		String fxmlCssFileFullPath = fxmlPath.equals("") ? fxmlCssFileName : fxmlPath + "/" + fxmlCssFileName;
		URL urlCss = Main.class.getResource(fxmlCssFileFullPath);
		if (urlCss == null) {
			terminateExecution = Boolean.TRUE;
			text = Main.class.getName() + ": FXML CSS File '" + fxmlCssFileFullPath + "' not found in resource path!";
			GenericUtilities.outputToSystemErr(text, systemErrorsDesired);
		}
		
		if (terminateExecution) {
			text = Main.class.getName() + ": Execution terminated due to errors!";
			GenericUtilities.outputToSystemErr(text, systemErrorsDesired);
			GenericUtilities.outputToSystemOut(text, systemOutputDesired);
			return;
		}
		
    	// initialize and display the primary stage
		try {
	        // load the FXML file and instantiate the "root" object
			FXMLLoader fxmlLoader = new FXMLLoader(urlXml);
			Parent root = (Parent)fxmlLoader.load();
			
			controller = (FracKhemGUIController)fxmlLoader.getController();
			controller.setStage(primaryStage);
			controller.setInitStageTitle(applicationTitle);
	        
	    	// initialize and display the stage
	    	createTrayIcon(primaryStage);
	        firstTime = Boolean.TRUE;
	        Platform.setImplicitExit(Boolean.FALSE);
	        
	        Scene scene = new Scene(root);
			scene.getStylesheets().add(urlCss.toExternalForm());
	        primaryStage.setScene(scene);
	        primaryStage.setTitle(applicationTitle + " - " + controller.getPropFileCurrFileVal());
	        primaryStage.show();
		} catch(Exception e) {
			text = ExceptionUtils.getStackTrace(e);
			GenericUtilities.outputToSystemErr(text, systemErrorsDesired);
			text = Main.class.getName() + ": Execution terminated due to errors!";
			GenericUtilities.outputToSystemErr(text, systemErrorsDesired);
			GenericUtilities.outputToSystemOut(text, systemOutputDesired);
		}

	}
	
	public static void main(String[] args) {
		launch(args);
	}

    
    public void createTrayIcon(final Stage stage) {
    	// if the operating system
    	// supports the system tray
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();
            // load an image
            java.awt.Image image = null;
            try {
//                File file = new File(iconLocation);
//                image = ImageIO.read(file);
                URL urlIcon = Main.class.getResource(iconLocation);
                image = ImageIO.read(urlIcon);
            } catch (IOException ex) {
                System.out.println(ex);
            }


            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    hide(stage);
                }
            });
            
            // create an action listener to listen for default action executed on the tray icon
            final ActionListener closeListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.close();
                            controller.terminate();
//                        	// fileWatcher.setTerminateWatching(Boolean.TRUE);
                        	System.out.println(applicationTitle + " terminated!");
                            Platform.exit();
                            System.exit(0);
                        }
                    });
                }
            };

            ActionListener showListener = new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.show();
                        }
                    });
                }
            };
            
            // create a pop-up menu
            PopupMenu popupMenu = new PopupMenu();
            
            MenuItem nameItem = new MenuItem(applicationTitle);
            nameItem.addActionListener(showListener);
            popupMenu.add(nameItem);
            
            popupMenu.addSeparator();
            
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showListener);
            popupMenu.add(showItem);

            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(closeListener);
            popupMenu.add(closeItem);
            
            /// ... add other menu items
            
            // construct a TrayIcon, scaling the image to 16x16 (the default dimensions of a tray icon)
            trayIcon = new TrayIcon(image.getScaledInstance(24, 24, Image.SCALE_DEFAULT), applicationTitle, popupMenu);
            // set the TrayIcon properties
            trayIcon.addActionListener(showListener);

            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        }
    }

    public void showProgramIsMinimizedMsg() {
        if (firstTime) {
            trayIcon.displayMessage(applicationTitle, minimizeMessageText, TrayIcon.MessageType.INFO);
            firstTime = Boolean.FALSE;
        }
    }

    private void hide(final Stage stage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	// if the operating system
            	// supports the system tray
                if (SystemTray.isSupported()) {
                    stage.hide();
                    showProgramIsMinimizedMsg();
                } else {
                    System.exit(0);
                }
            }
        });
    }
}
