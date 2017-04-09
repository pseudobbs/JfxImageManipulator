import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImageManipulator extends Application
{
	private static final Logger LOGGER = Logger.getLogger(ImageManipulator.class.getName());

	// helpful things to keep track of
	private Image uploadedImage = null;
	private String uploadedFileName;
	private Image imageOnScreen = null;
	private Text actionStatus = new Text();

	// builds the window
	private FlowPane root;
	private Stage primaryStage;
	private Scene scene;

	// layout elements in the window
	private BorderPane border;
	private VBox controlPanel;
	private VBox imagePanel;
	private VBox filePanel;
	private VBox savePanel;
	private HBox titleBar;
	private Slider slider;

	// for undo/redo
	private Stack<Image> undoStack = new Stack<>();
	private Stack<Image> redoStack = new Stack<>();
	private Button undoButton;
	private Button redoButton;


	public static void main(String[] args)
	{
		launch(args);
	}


	@Override
	public void start(Stage stage)
	{
		primaryStage = stage;

		primaryStage.setTitle("Image Manipulator");

		// set up all the panels that will be used ahead of time
		border = new BorderPane();
		controlPanel = controlPanel();
		imagePanel = imagePanel();
		titleBar = titleBar();
		filePanel = filePanel();
		savePanel = savePanel();

		// set initial content of panels
		border.setTop(titleBar);
		border.setLeft(null);
		border.setRight(null);
		border.setCenter(filePanel);

		// add elements to window
		root = new FlowPane();
		scene = new Scene(root, 400, 300);
		root.getChildren().add(border);

		// force BorderPane to fit window size
		border.prefHeightProperty().bind(scene.heightProperty());
		border.prefWidthProperty().bind(scene.widthProperty());

		// create window in top left of screen
		primaryStage.setScene(scene);
		primaryStage.setX(0);
		primaryStage.setY(0);
		primaryStage.show();
	}


	/**
	 * Generates a title bar to span the top of the window
	 * 
	 * @return the title bar
	 */
	private HBox titleBar()
	{
		// horizontal box for a title
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(15, 12, 15, 12));
		hbox.setAlignment(Pos.CENTER);
		hbox.setStyle("-fx-background-color: #369;");

		// title bar
		Label label = new Label("JavaFX Image Manipulator");
		label.setFont(new Font("Stencil", 24));
		label.setTextFill(Color.WHITE);
		label.setTextAlignment(TextAlignment.CENTER);
		label.setAlignment(Pos.CENTER);

		// add title bar to the box
		hbox.getChildren().add(label);

		return hbox;
	}


	/**
	 * Uses reflection to find the public methods of the MyImage class, then
	 * creates a button for each method so the user can access it.
	 * 
	 * @return a control panel with buttons for each manipulation method in
	 *         class MyImage
	 */
	private VBox controlPanel()
	{
		// create a vertical box
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(8);

		// get methods of image class
		Method[] manipulations = MyImage.class.getDeclaredMethods();

		// create buttons for each manipulation method
		for (Method method : manipulations)
		{
			// don't need buttons for private methods
			if (!Modifier.isPublic(method.getModifiers()))
			{
				continue;
			}

			// create button for each of MyImage's manipulation methods
			Button button = new Button(capitalize(method.getName()));
			button.setPrefWidth(125);
			button.setId(method.getName());
			button.setOnAction((event) -> manipulateImage(method));
			vbox.getChildren().add(button);
		}

		vbox.setAlignment(Pos.CENTER);

		return vbox;
	}


	/**
	 * Generates the file upload panel
	 * 
	 * @return the file upload panel
	 */
	private VBox filePanel()
	{
		VBox vbox = new VBox(5);
		vbox.setPadding(new Insets(10));
		vbox.setAlignment(Pos.CENTER);

		Button selectFileButton = new Button("Select image...");
		selectFileButton.setOnAction((event) -> fileChooser(event));

		vbox.getChildren().addAll(selectFileButton, actionStatus);

		return vbox;
	}


	/**
	 * Generates an initially empty image panel
	 * 
	 * @return the empty image panel
	 */
	private VBox imagePanel()
	{
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);

		return vbox;
	}


	/**
	 * Generates the right-hand panel
	 * 
	 * @return the save panel
	 * 
	 */
	private VBox savePanel()
	{
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(15));
		vbox.setSpacing(15);
		vbox.setAlignment(Pos.CENTER);

		// so undo/redo can be side by side
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(15));
		hbox.setSpacing(15);
		hbox.setAlignment(Pos.CENTER);

		undoButton = new Button("Undo");
		undoButton.setOnAction((event) -> undo());

		redoButton = new Button("Redo");
		redoButton.setOnAction((event) -> redo());

		Button saveButton = new Button("Save image");
		saveButton.setOnAction((event) -> saveImage());

		Button discardButton = new Button("Discard image");
		discardButton.setOnAction((event) -> reset());

		Button revertButton = new Button("Revert changes");
		revertButton.setOnAction((event) -> revert());

		hbox.getChildren().addAll(undoButton, redoButton);
		vbox.getChildren().addAll(hbox, saveButton, discardButton, revertButton);

		return vbox;
	}


	/**
	 * Allows the user to choose a png, jpg, or bmp image from the file system
	 * and displays it in the image panel
	 * 
	 * @param event
	 *            the button click that fired the upload dialog
	 * 
	 */
	private void fileChooser(ActionEvent event)
	{
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.bmp"));

		// open file chooser
		File selectedFile = chooser.showOpenDialog(null);
		Image selectedImage = null;

		try
		{
			// remove extension from filename and save to variable
			uploadedFileName = selectedFile.getName().replaceAll("\\.\\w{3,4}$", "");
			selectedImage = new Image(new FileInputStream(selectedFile));
		}
		catch (FileNotFoundException | NullPointerException e)
		{
			actionStatus.setText("File selection cancelled");
			LOGGER.log(Level.INFO, "User cancelled file selection");
		}

		// keep a copy of the original for revert changes function
		uploadedImage = selectedImage;

		// clears the stack if there is garbage from a previous image in it
		undoStack.clear();
		redoStack.clear();

		// resize window based on image size
		if (imageOnScreen != null)
		{
			double deltaX = selectedImage.getWidth() - imageOnScreen.getWidth();
			double deltaY = selectedImage.getHeight() - imageOnScreen.getHeight();

			resize(deltaX, deltaY);
		}
		else
		{
			resize(selectedImage.getWidth(), selectedImage.getHeight());
		}

		if (selectedImage.getWidth() * selectedImage.getHeight() >= 1_000_000)
		{
			Alert alert = new Alert(AlertType.WARNING,
					"WARNING: Image exceeds maximum recommended size.  Performance will suffer");
			alert.setHeaderText(null);
			alert.show();
		}

		showImage(selectedImage);

	}


	/**
	 * Calls the method passed in from the button handlers in the control panel.
	 * If the method requires a parameter, present the user with a slider to
	 * choose the parameter.
	 * 
	 * @param manipulation
	 *            the method to invoke on the image
	 */
	private void manipulateImage(Method manipulation)
	{
		MyImage imageToWrite = new MyImage(imageOnScreen);
		Parameter[] parameters = manipulation.getParameters();

		// if we need a parameter, present a slider for the user to input
		if (parameters.length != 0)
		{
			slider.setPrefHeight(new Slider().getHeight());
			slider.setVisible(true);

			// listen for user input
			slider.valueProperty().addListener(
					(ov, old, newV) -> invokeWithValue(newV, manipulation, imageToWrite));
		}
		else
		{
			try
			{
				manipulation.invoke(imageToWrite);
				redoStack.clear();
				showImage(imageToWrite);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				LOGGER.log(Level.SEVERE, "Problem executing method " + manipulation.getName(), e);
			}
		}

	}


	/**
	 * Displays an image in the center panel
	 * 
	 * @param image
	 *            the image to display
	 */
	// TODO: make scroll bars fatter/more noticeable
	// TODO: slider operations on huge images take way too long (parallelizing
	// w/streams?)
	// TODO: on huge images, slider should appear in a box or somewhere besides
	// centered below the image
	private void showImage(Image image)
	{
		// if image is very large, use scroll bars
		boolean useScrollPane = image.getHeight() * image.getWidth() >= 640_000;
		ScrollPane sp = null;
		ImageView imageView = new ImageView(image);

		// fit height of 0 means set the dimensions to match the image contained
		imageView.setFitHeight(0);
		imageView.setFitWidth(0);

		// clear out previous image in the image panel and add a new one
		imagePanel.getChildren().clear();
		imagePanel.getChildren().add(imageView);
		imagePanel.getChildren().add(makeCustomSlider());

		if (useScrollPane)
		{
			sp = new ScrollPane();
			imagePanel.getChildren().add(sp);
			imagePanel.setAlignment(Pos.CENTER);
			VBox.setVgrow(sp, Priority.ALWAYS);
			sp.setContent(imagePanel);
			sp.setPannable(true);
			// TODO: this doesn't work
			sp.setStyle(".scroll-bar { -fx-pref-width: 24px;}");
		}

		// set correct panels for user
		border.setCenter(useScrollPane ? sp : imagePanel);
		border.setLeft(controlPanel);
		border.setRight(savePanel);

		// allow user to undo this manipulation
		undoStack.push(image);

		undoButton.setDisable(undoStack.size() == 1);
		redoButton.setDisable(redoStack.isEmpty());

		// keep track of what image is currently displaying
		imageOnScreen = image;
	}


	/**
	 * Allows the user to save the image on screen to a location of their choice
	 * 
	 */
	private void saveImage()
	{
		BufferedImage outputImage = SwingFXUtils.fromFXImage(imageOnScreen, null);
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.bmp"));
		chooser.setTitle("Save file");
		chooser.setInitialFileName(uploadedFileName + "_manip");
		File savedFile = chooser.showSaveDialog(primaryStage);

		try
		{
			ImageIO.write(outputImage, "png", savedFile);
		}
		catch (IOException | IllegalArgumentException e)
		{
			Alert alert = new Alert(AlertType.WARNING, "Image not saved.");
			alert.setHeaderText(null);
			alert.show();

			LOGGER.log(Level.WARNING, "Image not saved");
		}
	}


	/**
	 * Removes the current edit from the screen and displays the last edit the
	 * user made
	 * 
	 */
	private void undo()
	{
		// don't undo if all that's there is the original image
		if (undoStack.size() > 0)
		{
			redoStack.push(undoStack.pop());
			showImage(undoStack.pop());
		}
	}


	/**
	 * Redisplays the last thing undone by the user
	 * 
	 */
	private void redo()
	{
		if (!redoStack.isEmpty())
		{
			showImage(redoStack.pop());
		}
	}


	/**
	 * Listener function to dynamically resize the window to fit the content
	 * 
	 * @param deltaX
	 *            the horizontal change in window size
	 * 
	 * @param deltaY
	 *            the vertical change in window size
	 */
	private void resize(double deltaX, double deltaY)
	{
		if (deltaY > 600)
		{
			primaryStage.setHeight(600);
			primaryStage.setWidth(900);
		}
		else
		{
			primaryStage.setWidth(primaryStage.getWidth() + deltaX);
			primaryStage.setHeight(primaryStage.getHeight() + deltaY);
		}
	}


	/**
	 * Replaces the modified image with a stored copy of the original
	 */
	private void revert()
	{
		redoStack.clear();
		showImage(uploadedImage);
	}


	/**
	 * Resets the window to its initial on-load state
	 * 
	 */
	private void reset()
	{
		imageOnScreen = null;
		border.setLeft(null);
		border.setCenter(filePanel);
		border.setRight(null);
		primaryStage.setWidth(400);
		primaryStage.setHeight(300);
	}


	/**
	 * Capitalizes the first letter of a string and replaces underscores with
	 * spaces
	 * 
	 * @param s
	 *            the string to capitalize
	 * 
	 * @return the capitalized string
	 */
	private String capitalize(String s)
	{
		return (s.substring(0, 1).toUpperCase() + s.substring(1)).replaceAll("_", " ");
	}


	/**
	 * Creates a standardized intensity slider for image effects.
	 * 
	 * @return the slider node
	 */
	private Node makeCustomSlider()
	{
		VBox sliderbox = new VBox();
		sliderbox.setAlignment(Pos.CENTER);

		slider = new Slider(0, 3, 0);
		// slider.setBlockIncrement(1); why is this commented out?
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(0);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setSnapToTicks(true);
		slider.setPrefWidth(200);
		slider.setMaxWidth(200);
		slider.setVisible(false);
		// slider.setPrefSize(slider.getWidth(), 1);
		slider.setId("image_slider");

		Button okbutton = new Button("OK");
		okbutton.setVisible(false);

		sliderbox.getChildren().addAll(slider, okbutton);

		return sliderbox;
	}


	/**
	 * Called when a method from MyImage requiring a parameter is selected. The
	 * calling method presents the user with a slider, and this method is called
	 * once the user chooses a value with the slider. The passed method is then
	 * called with the parameter chosen by the user (via the slider).
	 * 
	 * @param newValue
	 *            the value the user set the slider to
	 * @param manipulation
	 *            the method to invoke on the image
	 * @param imageToWrite
	 *            the image to invoke the method on
	 */
	private void invokeWithValue(Number newValue, Method manipulation, MyImage imageToWrite)
	{
		try
		{
			// keep a copy of the original to use if the user keeps using the
			// slider. This way we can apply a more intense effect to the
			// original image rather than applying a new effect to an already
			// manipulated image
			MyImage copy = new MyImage(imageOnScreen);
			boolean useCopy = imageOnScreen != imageToWrite;

			manipulation.invoke(useCopy ? copy : imageToWrite, newValue.intValue());
			redoStack.clear();

			// shows the image after manipulation without calling showImage,
			// which has side effects we don't want until the user clicks "OK"
			imagePanel.getChildren().set(0, new ImageView(useCopy ? copy : imageToWrite));

			// OK button for user to confirm input
			VBox sliderbox = (VBox) imagePanel.getChildren().get(1);
			Button okbutton = (Button) sliderbox.getChildren().get(1);
			okbutton.setVisible(true);
			okbutton.setOnAction((e) -> showImage(useCopy ? copy : imageToWrite));

		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			LOGGER.log(Level.SEVERE, "Problem executing method " + manipulation.getName()
					+ "with argument " + newValue, e);
		}
	}
}