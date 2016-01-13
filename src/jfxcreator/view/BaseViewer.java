/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

/**
 *
 * @author Aniket
 */
import java.io.File;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions.TransitionDirection;
import org.jpedal.examples.viewer.gui.javafx.FXViewerTransitions.TransitionType;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXInputDialog;
import org.jpedal.exception.PdfException;
import org.jpedal.external.PluginHandler;
import org.jpedal.objects.PdfPageData;

public class BaseViewer extends BorderPane {

    private final org.jpedal.PdfDecoderFX pdf = new org.jpedal.PdfDecoderFX();

    PluginHandler customPluginHandle = null;

    public enum FitToPage {

        AUTO, WIDTH, HEIGHT, NONE
    }

    String PDFfile;

    //Variable to hold the current file/directory
    File file;

    //These two variables are todo with PDF encryption & passwords
    private String password; //Holds the password from the JVM or from User input
    private boolean closePasswordPrompt; //boolean controls whether or not we should close the prompt box

    // Layout panes
    private VBox top;
    private HBox bottom;
    private ScrollPane center;
    //Group is a container which holds the decoded PDF content
    private Group group;

    // for the location of the pdf file
    private Text fileLoc;

    private float scale = 1.0f;

    private final float[] scalings = {0.01f, 0.1f, 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 4.0f, 7.5f, 10.0f};

    private int currentScaling = 5;

    private static final float insetX = 25;

    private static final float insetY = 25;

    private int currentPage = 1;

    //Controls size of the stage, in theory setting this to a higher value will
    //increase image quality as there's more pixels due to higher image resolutions
    static final int FXscaling = 1;

    FitToPage zoomMode = FitToPage.AUTO;

    private TransitionType transitionType = TransitionType.None;

    public BaseViewer() {
        setCenter(setupViewer());
    }

    public final BorderPane setupViewer() {

        /* 
         * Setting up layout panes and assigning them to the appropiate locations
         */
        final BorderPane root = new BorderPane();

        top = new VBox();

        root.setTop(top);

        top.getChildren().add(setupToolBar());

        bottom = new HBox();
        bottom.setPadding(new Insets(0, 10, 0, 10));
        root.setBottom(bottom);

        center = new ScrollPane();
        center.setPannable(true);
        center.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        center.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        //needs to be added via group so resizes (see http://pixelduke.wordpress.com/2012/09/16/zooming-inside-a-scrollpane/)
        group = new Group();
        group.getChildren().add(pdf);
        center.setContent(group);
        root.setCenter(center);

        center.viewportBoundsProperty().addListener((final ObservableValue<? extends Bounds> ov, final Bounds ob, final Bounds nb) -> {
            adjustPagePosition(nb);
        });

        /**
         * Sets the text to be displayed at the bottom of the FX Viewer*
         */
        fileLoc = new Text("No PDF Selected");
        fileLoc.setId("file_location");
        bottom.getChildren().add(fileLoc);
        return root;
    }

    public void addListeners() {

        /**
         * auto adjust so dynamically resized as viewer width alters
         */
        widthProperty().addListener((final ObservableValue<? extends Number> observableValue, final Number oldSceneWidth, final Number newSceneWidth) -> {
            fitToX(zoomMode);
        });

        heightProperty().addListener((final ObservableValue<? extends Number> observableValue, final Number oldSceneHeight, final Number newSceneHeight) -> {
            fitToX(zoomMode);
        });

        /**
         * Controls for dragging a PDF into the scene Using the dragboard, which
         * extends the clipboard class, detect a file being dragged onto the
         * scene and if the user drops the file we load it.
         */
        /*
         setOnDragOver(new EventHandler<DragEvent>() {
         @Override
         public void handle(final DragEvent event) {
         final Dragboard db = event.getDragboard();
         if (db.hasFiles()) {
         event.acceptTransferModes(TransferMode.COPY);
         } else {
         event.consume();
         }
         }
         });
        
         setOnDragDropped(new EventHandler<DragEvent>() {        
         @Override
         public void handle(final DragEvent event) {
         final Dragboard db = event.getDragboard();
         boolean success = false;
         if(db.hasFiles()){
         success = true;
         // Only get the first file from the list
         file = db.getFiles().get(0);
         Platform.runLater(new Runnable() {
         @Override
         public void run() {
         loadPDF(file);
         }
         });
         }
         event.setDropCompleted(success);
         event.consume();
         }
         });
         */
    }

    /**
     * Sets up a MenuBar to be used at the top of the window.
     *
     * It contains one Menu - navMenu - which allows the user to open and
     * navigate pdf files
     *
     * @return
     */
    private ToolBar setupToolBar() {

        final ToolBar toolbar = new ToolBar();

        final Button back = new Button("Back");
        final ComboBox<String> pages = new ComboBox<>();
        final Label pageCount = new Label();
        final Button forward = new Button("Forward");
        final Button zoomIn = new Button("Zoom in");
        final Button zoomOut = new Button("Zoom out");
        final Button fitWidth = new Button("Fit to Width");
        final Button fitHeight = new Button("Fit to Height");
        final Button fitPage = new Button("Fit to Page");

        ComboBox<String> transitionList = new ComboBox<>();

        back.setId("back");
        pageCount.setId("pgCount");
        pages.setId("pages");
        forward.setId("forward");
        zoomIn.setId("zoomIn");
        zoomOut.setId("zoomOut");
        fitWidth.setId("fitWidth");
        fitHeight.setId("fitHeight");
        fitPage.setId("fitPage");

        /**
         * Open the PDF File
         */
        pages.getSelectionModel().selectedIndexProperty().addListener((final ObservableValue<? extends Number> ov, final Number oldVal, final Number newVal) -> {
            if (newVal.intValue() != -1 && newVal.intValue() + 1 != currentPage) {
                final int newPage = newVal.intValue() + 1;
                goToPage(newPage);
            }
        });

        // Navigate backward
        back.setOnAction((final ActionEvent t) -> {
            if (currentPage > 1) {
                goToPage(currentPage - 1);
            }
        });

        // Navigate forward
        forward.setOnAction((final ActionEvent t) -> {
            if (currentPage < pdf.getPageCount()) {
                goToPage(currentPage + 1);
            }
        });

        // Zoom in
        zoomIn.setOnAction((final ActionEvent t) -> {
            zoomMode = FitToPage.NONE;

            if (currentScaling < scalings.length - 1) {

                currentScaling = findClosestIndex(scale, scalings);

                if (scale >= scalings[findClosestIndex(scale, scalings)]) {

                    currentScaling++;

                }

                scale = scalings[currentScaling];

            }

            pdf.setPageParameters(scale, currentPage);
            adjustPagePosition(center.getViewportBounds());
        });

        // Zoom out
        zoomOut.setOnAction((final ActionEvent t) -> {
            zoomMode = FitToPage.NONE;

            if (currentScaling > 0) {

                currentScaling = findClosestIndex(scale, scalings);

                if (scale <= scalings[findClosestIndex(scale, scalings)]) {

                    currentScaling--;

                }

                scale = scalings[currentScaling];

            }

            pdf.setPageParameters(scale, currentPage);
            adjustPagePosition(center.getViewportBounds());
        });

        // Fit to width
        fitWidth.setOnAction((final ActionEvent t) -> {
            zoomMode = FitToPage.WIDTH;
            fitToX(FitToPage.WIDTH);
        });

        // Fit to height
        fitHeight.setOnAction((final ActionEvent t) -> {
            zoomMode = FitToPage.HEIGHT;
            fitToX(FitToPage.HEIGHT);
        });

        // Fit to Page
        fitPage.setOnAction((final ActionEvent t) -> {
            zoomMode = FitToPage.AUTO;
            fitToX(FitToPage.AUTO);
        });

        final Region spacerLeft = new Region();
        final Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        // Set up the ComboBox for transitions
        final ObservableList<String> options = FXCollections.observableArrayList();

        for (final TransitionType transition : TransitionType.values()) {
            options.add(transition.name());
        }

        if (!options.isEmpty()) {
            transitionList = new ComboBox<>(options);
            // Put before setValue so that setValue triggers the event
            transitionList.valueProperty().addListener((final ObservableValue<? extends String> ov, final String oldVal, final String newVal) -> {
                transitionType = TransitionType.valueOf(newVal);
            });

            transitionList.setValue(options.get(transitionType.ordinal()));
        }

        toolbar.getItems().addAll(spacerLeft, back, pages, pageCount, forward, zoomIn, zoomOut, spacerRight, transitionList);

        return toolbar;
    }

    /**
     * take a File handle to PDF file on local filesystem and displays in PDF
     * viewer
     *
     * @param input The PDF file to load in the viewer
     */
    public void loadPDF(final File input) {

        if (input == null) {
            return;
        }

        scale = 1; //reset to default for new page

        PDFfile = input.getAbsolutePath();
        fileLoc.setText(PDFfile);

        openFile(input, null, false);

    }

    /**
     * take a File handle to PDF file on local filesystem and displays in PDF
     * viewer
     *
     * @param input The PDF file to load in the viewer
     */
    public void loadPDF(final String input) {

        if (input == null) {
            return;
        }

        scale = 1; //reset to default for new page
        PDFfile = input;
        fileLoc.setText(PDFfile);

        if (input.startsWith("http")) {
            openFile(null, input, true);
        } else {
            openFile(new File(input), null, false);
        }

    }

    private void openFile(final File input, String url, boolean isURL) {
        try {
            //Open the pdf file so we can check for encryption
            if (isURL) {
                pdf.openPdfFileFromURL(url, false);
            } else {
                pdf.openPdfFile(input.getAbsolutePath());
            }

            if (customPluginHandle != null) {
                if (isURL) {
                    customPluginHandle.setFileName(url);
                } else {
                    customPluginHandle.setFileName(input.getAbsolutePath());
                }
            }

            if (System.getProperty("org.jpedal.page") != null && !System.getProperty("org.jpedal.page").isEmpty()) {
                currentPage = currentPage < 1 ? 1 : currentPage;
                currentPage = currentPage > pdf.getPageCount() ? pdf.getPageCount() : currentPage;
            } else {
                currentPage = 1;
            }
            /**
             * This code block deals with user input and JVM passwords in
             * Encrypted PDF documents.
             */
            if (pdf.isEncrypted()) {

                int passwordCount = 0;        //Monitors how many attempts there have been to the password
                closePasswordPrompt = false;  //Do not close the prompt box

                //While the PDF content is not viewable, repeat until the correct password is found
                while (!pdf.isFileViewable() && !closePasswordPrompt) {

                    /**
                     * See if there's a JVM flag for the password & Use it if
                     * there is Otherwise prompt the user to enter a password
                     */
                    if (System.getProperty("org.jpedal.password") != null) {
                        password = System.getProperty("org.jpedal.password");
                    } else if (!closePasswordPrompt) {
                        showPasswordPrompt(passwordCount);
                    }

                    //If we have a password, try and open the PdfFile again with the password
                    if (password != null) {

                        if (isURL) {
                            pdf.openPdfFileFromURL(url, false, password);
                        } else {
                            pdf.openPdfFile(input.getAbsolutePath());
                        }
                        //pdf.setEncryptionPassword(password);

                    }
                    passwordCount += 1; //Increment he password attempt

                }

            }

            // Set up top bar values
            ((Label) top.lookup("#pgCount")).setText("/" + pdf.getPageCount());
            final ComboBox<String> pages = ((ComboBox<String>) top.lookup("#pages"));
            pages.getItems().clear();
            for (int i = 1; i <= pdf.getPageCount(); i++) {
                pages.getItems().add(String.valueOf(i));
            }
            // Goes to the first page and starts the decoding process
            goToPage(currentPage);

        } catch (final PdfException ex) {

        }

    }

    /**
     * This method will show a popup box and request for a password.
     *
     * If the user does not enter the correct password it will ask them to try
     * again. If the user presses the Cross button, the password prompt will
     * close.
     *
     * @param passwordCount is an int which represents the current input attempt
     */
    private void showPasswordPrompt(final int passwordCount) {

        //Setup password prompt content
        final Text titleText = new Text("Password Request");
        final TextField inputPasswordField = new TextField("Please Enter Password");

        //If the user has attempted to enter the password more than once, change the text
        if (passwordCount >= 1) {
            titleText.setText("Incorrect Password");
            inputPasswordField.setText("Please Try Again");
        }

        final FXInputDialog passwordInput = new FXInputDialog((Stage) getScene().getWindow(), titleText.getText()) {
            @Override
            protected void positiveClose() {
                super.positiveClose();
                closePasswordPrompt = true;
            }
        };

        password = passwordInput.showInputDialog();

    }

    private void fitToX(final FitToPage fitToPage) {

        if (fitToPage == FitToPage.NONE) {
            return;
        }

        final float pageW = pdf.getPdfPageData().getCropBoxWidth2D(currentPage);
        final float pageH = pdf.getPdfPageData().getCropBoxHeight2D(currentPage);
        final int rotation = pdf.getPdfPageData().getRotation(currentPage);

        //Handle how we auto fit the content to the page
        if (fitToPage == FitToPage.AUTO && (pageW < pageH)) {
            if (pdf.getPDFWidth() < pdf.getPDFHeight()) {
                fitToX(FitToPage.HEIGHT);
            } else {
                fitToX(FitToPage.WIDTH);
            }
        }

        //Handle how we fit the content to the page width or height
        if (fitToPage == FitToPage.WIDTH) {
            final float width = (float) (getWidth());
            if (rotation == 90 || rotation == 270) {
                scale = (width - insetX - insetX) / pageH;
            } else {
                scale = (width - insetX - insetX) / pageW;
            }
        } else if (fitToPage == FitToPage.HEIGHT) {
            final float height = (float) (getHeight() - top.getBoundsInLocal().getHeight() - bottom.getHeight());

            if (rotation == 90 || rotation == 270) {
                scale = (height - insetY - insetY) / pageW;
            } else {
                scale = (height - insetY - insetY) / pageH;
            }
        }

        pdf.setPageParameters(scale, currentPage);
    }

    /**
     * Locate scaling value closest to current scaling setting
     *
     * @param scale
     * @param scalings
     * @return int
     */
    private static int findClosestIndex(final float scale, final float[] scalings) {
        float currentMinDiff = Float.MAX_VALUE;
        int closest = 0;

        for (int i = 0; i < scalings.length - 1; i++) {

            final float diff = Math.abs(scalings[i] - scale);

            if (diff < currentMinDiff) {
                currentMinDiff = diff;
                closest = i;
            }

        }
        return closest;
    }

    private void decodePage() {

        try {
            final PdfPageData pageData = pdf.getPdfPageData();
            final int rotation = pageData.getRotation(currentPage); //rotation angle of current page

            //Only call this when the page is displayed vertically, otherwise
            //it will mess up the document cropping on side-ways documents.
            if (rotation == 0 || rotation == 180) {
                pdf.setPageParameters(scale, currentPage);
            }

            pdf.decodePage(currentPage);
            //wait to ensure decoded
            pdf.waitForDecodingToFinish();
        } catch (final Exception e) {
        }
        fitToX(FitToPage.AUTO);
        updateNavButtons();
        setBorder();
        adjustPagePosition(center.getViewportBounds());
    }

    private void updateNavButtons() {
        if (currentPage > 1) {
            top.lookup("#back").setDisable(false);
        } else {
            top.lookup("#back").setDisable(true);
        }

        if (currentPage < pdf.getPageCount()) {
            top.lookup("#forward").setDisable(false);
        } else {
            top.lookup("#forward").setDisable(true);
        }

        ((ComboBox) top.lookup("#pages")).getSelectionModel().select(currentPage - 1);
    }

    private void goToPage(final int newPage) {

        final TransitionDirection direction;

        // For sliding Transitions
        if (transitionType != TransitionType.Fade || transitionType != TransitionType.None) {
            direction = newPage > currentPage ? TransitionDirection.LEFT : TransitionDirection.RIGHT;
        } else {
            direction = TransitionDirection.NONE;
        }

        switch (transitionType) {

            case Fade:
                startTransition(newPage, direction);
                break;

            case Scale:
                startTransition(newPage, direction);
                break;

            case Rotate:
                startTransition(newPage, direction);
                break;

            case CardStack:
                startTransition(newPage, direction);
                break;

            default: //no transition

                currentPage = newPage;
                decodePage();
                break;
        }

    }

    private void startTransition(final int newPage, final TransitionDirection direction) {
        final Transition exitTransition = FXViewerTransitions.exitTransition(pdf, transitionType, direction);
        if (exitTransition != null) {
            exitTransition.setOnFinished((final ActionEvent t) -> {
                currentPage = newPage;

                Platform.runLater(() -> {
                    decodePage();
                });

                TransitionDirection entryDirection = direction;
                if (direction != TransitionDirection.NONE) {
                    entryDirection = direction == TransitionDirection.LEFT ? TransitionDirection.RIGHT : TransitionDirection.LEFT;
                }

                final Transition entryTransition = FXViewerTransitions.entryTransition(pdf, transitionType, entryDirection);
                entryTransition.play();
            });
            exitTransition.play();
        }
    }

    /**
     * @return the case sensitive full path and name of the PDF file
     */
    public String getPDFfilename() {
        return PDFfile;
    }

    private void adjustPagePosition(final Bounds nb) {
        // (new scrollbar width / 2) - (page width / 2)
        double adjustment = ((nb.getWidth() / 2) - (group.getBoundsInLocal().getWidth() / 2));
        // Keep the group within the viewport of the scrollpane
        if (adjustment < 0) {
            adjustment = 0;
        }
        group.setTranslateX(adjustment);
    }

    // Set a space between the top toolbar and the page
    private void setBorder() {
        // Why it's easier to use a dropshadow for this is beyond me, but here it is...
        final int rotation = pdf.getPdfPageData().getRotation(currentPage);
        final double x = (rotation == 90 || rotation == 270) ? 40 : 0;
        final double y = (rotation == 90 || rotation == 270) ? 0 : 40;
        final DropShadow pdfBorder = new DropShadow(0, x, y, Color.TRANSPARENT);
        pdf.setEffect(pdfBorder);
    }

    public void addExternalHandler(PluginHandler customPluginHandle) {
        this.customPluginHandle = customPluginHandle;
    }
}
