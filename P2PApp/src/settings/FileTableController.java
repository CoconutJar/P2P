package settings;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.File;


public class FileTableController implements Initializable{
    // From ConnectController
    @FXML private String serverHN, serverPort, userName, userHN, userPort, userSpeed;

    // MyFiles Table (Local files)
    @FXML private TableView<FileObject> myFilesTableView;
    @FXML private TableColumn<FileObject, String> myFilenameColumn;
    @FXML private TableColumn<FileObject, String> myDescColumn;

    // Server Files Table
    @FXML private TableView<FileObject> serverFilesTableView;
    @FXML private TableColumn<FileObject, String> servFilenameColumn;
    @FXML private TableColumn<FileObject, String> servHNColumn;
    @FXML private TableColumn<FileObject, String> servSpeedColumn;

    // General display attributes
    @FXML private Label close;
    @FXML private TextField searchField;
    @FXML private Label connectToLabel;
    @FXML private Label userLabel;
    @FXML private Label ipLabel;
    @FXML private Button disconnectBtn;

    // Description Editor attributes
    @FXML private AnchorPane editDescPane;
    @FXML private Label editDescLabel;
    @FXML private Button updateDescBtn;
    @FXML private TextArea editDescTextArea;


    // Data received from ConnectController
    public void initData(String serverHN, String serverPort, String userName, String userHN, String userPort, String userSpeed){
        this.serverHN = serverHN;
        this.serverPort = serverPort;
        this.userName = userName;
        this.userHN = userHN;
        this.userPort = userPort;
        this.userSpeed = userSpeed;

        // Set connection session attributes banner
        sessionAttributes();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up MyFile Table columns
        myFilenameColumn.setCellValueFactory(new PropertyValueFactory<FileObject, String>("filename"));
        myDescColumn.setCellValueFactory(new PropertyValueFactory<FileObject, String>("description"));

        // Set up Server Files Table columns
        servFilenameColumn.setCellValueFactory(new PropertyValueFactory<FileObject, String>("filename"));
        servHNColumn.setCellValueFactory(new PropertyValueFactory<FileObject, String>("hostname"));
        servSpeedColumn.setCellValueFactory(new PropertyValueFactory<FileObject, String>("speed"));

        // Fill MyFiles Table with files in designated MyFiles folder
        myFilesTableView.setItems(getMyFiles());
        // Load dummy file data for Server Table
        serverFilesTableView.setItems(getServerFiles());

        // Search Server File Table for keywords
        ObservableList<FileObject> serverFiles = getServerFiles();
        FilteredList<FileObject> filteredData = new FilteredList(serverFiles);
        SortedList<FileObject> fileSortedList = new SortedList<>(filteredData);
        serverFilesTableView.setItems(fileSortedList);
        fileSortedList.comparatorProperty().bind(serverFilesTableView.comparatorProperty());
        // Dynamic Server File Table search results based on Description, Filename, Hostname, and connection Speed.
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filteredData.setPredicate(f -> f.getDescription().toLowerCase().contains(searchField.getText().toLowerCase()) ||
                                                f.getFilename().toLowerCase().contains(searchField.getText().toLowerCase()) ||
                                                f.getHostname().contains(searchField.getText()) ||
                                                f.getSpeed().toLowerCase().contains(searchField.getText().toLowerCase()))
        );

        // Right-click Menu on MyFiles Table (Description Editor)
        MenuItem mi1 = new MenuItem("Edit Description");
        mi1.setOnAction((ActionEvent event) -> {
            FileObject item = myFilesTableView.getSelectionModel().getSelectedItem();
            System.out.println("Selected item: " + item.getFilename());
            editDescription(item);
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(mi1);
        myFilesTableView.setContextMenu(menu);

        MenuItem mi12 = new MenuItem("Get File");
        mi12.setOnAction((ActionEvent event) -> {
            FileObject item = serverFilesTableView.getSelectionModel().getSelectedItem();
            System.out.println("Selected item: " + item.getFilename());
            transferFile(item);
        });
        ContextMenu menu2 = new ContextMenu();
        menu2.getItems().add(mi12);
        serverFilesTableView.setContextMenu(menu2);
    }

    // Returns ObservableList of all FileObject objects in MyFiles folder
    private ObservableList<FileObject> getMyFiles(){
        ObservableList<FileObject> files = FXCollections.observableArrayList();

        File folder = new File("./MyFiles");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                files.add(new FileObject(file.getName(), "Add Description"));
            }
        }

        return files;
    }

    // Returns ObservableList of all FileObject objects used to fill Server File Table
    // CURRENTLY -> Fills ObservableList with hardcoded dummy data
    // INTENDED  -> Fill ObservableList with FileObjects gathered from "filelist.xml" received from server
    private ObservableList<FileObject> getServerFiles(){
        ObservableList<FileObject> files = FXCollections.observableArrayList();
        files.add(new FileObject("testfile1.txt", "168.61.111.49", "T1", "Cool stuff"));
        files.add(new FileObject("testfile2.pdf", "148.61.415.49", "Ethernet", "Sweet stuff"));
        files.add(new FileObject("testfile1.docx",  "152.61.112.49", "Fiber Optic", "Awesome stuff"));

        return files;
    }

    // Handles Description edit of FileObject Description attribute
    private void editDescription(FileObject item){
        // Set visible & fill with sought FileObject data
        editDescPane.setVisible(true);
        editDescLabel.setText("Edit Description of " + item.getFilename());
        editDescTextArea.setText(""+item.getDescription());

        // Handles "Update" button
        // Sets FileObject Description, sets Pane to invisible, refreshes (updates) MyFile Table
        updateDescBtn.setOnAction((ActionEvent event) -> {
            System.out.println(editDescTextArea.getText());
            item.setDescription(editDescTextArea.getText());
            editDescPane.setVisible(false);
            myFilesTableView.refresh();
            System.out.println("Close Edit");
        });

    }


    private void transferFile(FileObject item){
        System.out.println("Transfer file: " + item.getFilename());
    }

    private void sessionAttributes(){
        connectToLabel.setText("Connected to " + serverHN + " on " + serverPort);
        userLabel.setText("User: " + userName);
        ipLabel.setText("IP: " + userHN);
    }

    public void disconnectBtnAction(){
        System.out.println("Disconnect");
    }

    // Handles closing of window
    public void closeBtnAction(){
        Stage stage = (Stage) close.getScene().getWindow();
        System.out.println("Application closed.");
        stage.close();
    }
}
