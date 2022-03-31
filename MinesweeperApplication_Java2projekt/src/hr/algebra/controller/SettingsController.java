/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * FXML Controller class
 *
 * @author Korisnik
 */
public class SettingsController implements Initializable {

    public final static String SETTINGS_FILE = "settings.xml";
    public static boolean breakScoreboard;
    public static boolean useTheme;

    @FXML
    private CheckBox cb_breakTheScoreboard;
    @FXML
    private CheckBox cb_useColorfulTheme;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSettings();
        cb_breakTheScoreboard.setSelected(breakScoreboard);
        cb_useColorfulTheme.setSelected(useTheme);

    }

    @FXML
    private void confirm(ActionEvent event) {
        try {
            saveSettings();
        } catch (ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        ((Stage) cb_breakTheScoreboard.getScene().getWindow()).close();
    }

    private void saveSettings() throws ParserConfigurationException, TransformerException {

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document xmlDocument = documentBuilder.newDocument();

        Element rootElement = xmlDocument.createElement("Settings");
        xmlDocument.appendChild(rootElement);

        //Add Scoreboard
        Element scoreboard = xmlDocument.createElement("BrokenScoreboard");
        Node scoreboardTextNode = xmlDocument.createTextNode(cb_breakTheScoreboard.isSelected() ? "true" : "false");
        scoreboard.appendChild(scoreboardTextNode);
        rootElement.appendChild(scoreboard);

        //Add Theme
        Element theme = xmlDocument.createElement("Theme");
        Node themeTextNode = xmlDocument.createTextNode(cb_useColorfulTheme.isSelected() ? "true" : "false");
        theme.appendChild(themeTextNode);
        rootElement.appendChild(theme);

        //Save XML to file
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source xmlSource = new DOMSource(xmlDocument);
        Result xmlResult = new StreamResult(new File(SETTINGS_FILE));
        transformer.transform(xmlSource, xmlResult);

    }

    public static void loadSettings() {
        DocumentBuilder parser;
        Document xmlDocument;
        if (!Files.exists(Paths.get(SETTINGS_FILE))) {
            return;
        }
        File file = new File(SETTINGS_FILE);

        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xmlDocument = parser.parse(file);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        breakScoreboard = xmlDocument.getDocumentElement().getChildNodes().item(0).getTextContent().equals("true");
        useTheme = xmlDocument.getDocumentElement().getChildNodes().item(1).getTextContent().equals("true");
    }

}
