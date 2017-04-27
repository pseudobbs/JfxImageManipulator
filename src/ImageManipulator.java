import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// TODO: drag and drop images to load
public class ImageManipulator extends Application
{
	private static final Logger LOGGER = Logger.getLogger(ImageManipulator.class.getName());

	// helpful things to keep track of
	private static Image uploadedImage = null;
	private static MyImage copy = null;
	private static String uploadedFileName = null;
	private static Image imageOnScreen = null;

	// for undo/redo
	private static Stack<Image> undoStack = new Stack<>();
	private static Stack<Image> redoStack = new Stack<>();
	private static Button undoButton = new Button("Undo");
	private static Button redoButton = new Button("Redo");

	// builds the window
	private static FlowPane root;
	private static Stage primaryStage;
	private static Scene scene;

	// layout elements in the window
	private static BorderPane border = new BorderPane();
	private static ControlPanel controlPanel = new ControlPanel();
	private static ImagePanel imagePanel = new ImagePanel();
	private static FilePanel filePanel = new FilePanel();
	private static SavePanel savePanel = new SavePanel(undoButton, redoButton);
	private static TitleBar titleBar = new TitleBar();
	private static ImageSliderBox sliderBox = new ImageSliderBox(10);


	public static void main(String[] args)
	{
		launch(args);
	}


	@Override
	public void start(Stage stage)
	{
		primaryStage = stage;
		primaryStage.setTitle("Image Manipulator");

		// set initial content of panels
		border.setTop(titleBar);
		border.setLeft(null);
		border.setRight(null);
		border.setCenter(filePanel);
		border.setBottom(null);

		// add elements to window
		root = new FlowPane();
		scene = new Scene(root, 400, 300);
		// scene.getStylesheets().add(ImageManipulator.class.getResource("im.css").toExternalForm());
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
	 * Allows the user to choose a png, jpg, or bmp image from the file system
	 * and displays it in the image panel
	 * 
	 * @param event
	 *            the button click that fired the upload dialog
	 * 
	 */
	static void processUploadedImage()
	{
		// keep a copy of the original for revert changes function
		uploadedImage = filePanel.getSelectedImage();

		// clears the stack if there is garbage from a previous image in it
		undoStack.clear();
		redoStack.clear();

		// resize window based on image size
		if (imageOnScreen != null)
		{
			double deltaX = uploadedImage.getWidth() - imageOnScreen.getWidth();
			double deltaY = uploadedImage.getHeight() - imageOnScreen.getHeight();

			resize(deltaX, deltaY);
		}
		else
		{
			resize(uploadedImage.getWidth(), uploadedImage.getHeight());
		}

		// TODO: if the processing message ever works, probably won't need this
		if (uploadedImage.getWidth() * uploadedImage.getHeight() >= 1_000_000)
		{
			Alert alert = new Alert(AlertType.WARNING,
					"WARNING: Image exceeds maximum recommended size.  Performance will suffer");
			alert.setHeaderText(null);
			alert.show();
		}

		showImage(uploadedImage);

	}


	/**
	 * Calls the method passed in from the button handlers in the control panel.
	 * If the method requires a parameter, present the user with a slider to
	 * choose the parameter.
	 * 
	 * @param manipulation
	 *            the method to invoke on the image
	 */
	static void manipulateImage(Method manipulation)
	{
		MyImage imageToWrite = new MyImage(imageOnScreen);
		Parameter[] parameters = manipulation.getParameters();

		// if we need a parameter, present a slider for the user to input
		// if there are 2, present a dropdown as well
		if (parameters.length == 2)
		{
			if (manipulation.getName().equals("reduce_colors"))
			{
				sliderBox.setCurrentComboBox(sliderBox.getReduceColorsBox());
				sliderBox.getReduceColorsBox().setVisible(true);
				border.setBottom(sliderBox);
			}
			else if (manipulation.getName().equals("blur"))
			{
				sliderBox.setCurrentComboBox(sliderBox.getBlurBox());
				sliderBox.getBlurBox().setVisible(true);
				border.setBottom(sliderBox);
			}

			sliderBox.getOkButton().setText(capitalize(manipulation.getName()));

			sliderBox.getReduceColorsBox().valueProperty().addListener(
					(ov, old, newV) -> invokeWithValue(sliderBox.getSlider().getValue(), newV,
							manipulation, imageToWrite));

			sliderBox.getBlurBox().valueProperty().addListener((ov, old, newV) -> invokeWithValue(
					sliderBox.getSlider().getValue(), newV, manipulation, imageToWrite));

			sliderBox.getSlider().valueProperty()
					.addListener((ov, old, newV) -> invokeWithValue(newV,
							sliderBox.getReduceColorsBox().getValue(), manipulation, imageToWrite));
		}
		else if (parameters.length != 0)
		{
			sliderBox.getOkButton().setText(capitalize(manipulation.getName()));

			// listen for user inputs
			sliderBox.getSlider().valueProperty().addListener(
					(ov, old, newV) -> invokeWithValue(newV, -1, manipulation, imageToWrite));

			sliderBox.getReduceColorsBox().setVisible(false);

			border.setBottom(sliderBox);
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
	// TODO: slider operations on huge images take way too long
	static void showImage(Image image)
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

		if (useScrollPane)
		{
			sp = new ScrollPane();
			// TODO: this doesn't work
			sp.getStylesheets().add(ImageManipulator.class.getResource("im.css").toExternalForm());
			imagePanel.getChildren().add(sp);
			imagePanel.setAlignment(Pos.CENTER);
			VBox.setVgrow(sp, Priority.ALWAYS);
			sp.setContent(imagePanel);
			sp.setPannable(true);
		}

		// set correct panels for user
		border.setCenter(useScrollPane ? sp : imagePanel);
		border.setLeft(controlPanel);
		border.setRight(savePanel);

		// allow user to undo this manipulation
		undoStack.push(image);

		undoButton.setDisable(undoStack.size() <= 1);
		redoButton.setDisable(redoStack.isEmpty());

		// keep track of what image is currently displaying
		imageOnScreen = image;
	}


	/**
	 * Removes the current edit from the screen and displays the last edit the
	 * user made
	 * 
	 */
	static void undo()
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
	static void redo()
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
	private static void resize(double deltaX, double deltaY)
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
	private static void invokeWithValue(Number sliderValue, Number comboValue, Method manipulation,
			MyImage imageToWrite)
	{
		// prevents excessive calls to manipulation functions
		if (sliderValue.doubleValue() % 1 != 0)
		{
			return;
		}
		try
		{
			// keep a copy of the original to use if the user keeps using the
			// slider. This way we can apply a more intense effect to the
			// original image rather than applying a new effect to an already
			// manipulated image
			copy = new MyImage(imageOnScreen);
			boolean useCopy = imageOnScreen != imageToWrite;

			if (comboValue.intValue() != -1)
			{
				manipulation.invoke(useCopy ? copy : imageToWrite, sliderValue.intValue(),
						comboValue.intValue());
			}
			else
			{
				manipulation.invoke(useCopy ? copy : imageToWrite, sliderValue.intValue());
			}
			redoStack.clear();

			// shows the image after manipulation without calling showImage,
			// which has side effects we don't want until the user clicks "OK"
			imagePanel.getChildren().set(0, new ImageView(useCopy ? copy : imageToWrite));

			sliderBox.getOkButton().setOnAction((e) -> showImage(useCopy ? copy : imageToWrite));

		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			LOGGER.log(Level.SEVERE, "Problem executing method " + manipulation.getName()
					+ "with arguments " + sliderValue + " and " + comboValue, e);
		}
	}


	private static void invokeWithValue(Number sliderValue, String comboValue, Method manipulation,
			MyImage imageToWrite)
	{
		Number val = 0;

		switch (comboValue) {
		case "Box blur":
			val = 1;
			break;
		case "Gaussian blur (3x3)":
			val = 2;
			break;
		case "Gaussian blur (5x5)":
			val = 3;
			break;
		default:
			val = 0;
			break;
		}

		invokeWithValue(sliderValue, val, manipulation, imageToWrite);
	}


	public static String capitalize(String s)
	{
		return (s.substring(0, 1).toUpperCase() + s.substring(1)).replaceAll("_", " ");
	}


	/**
	 * @return the primaryStage
	 */
	public static Stage getPrimaryStage()
	{
		return primaryStage;
	}


	/**
	 * @return the redoStack
	 */
	public static Stack<Image> getRedoStack()
	{
		return redoStack;
	}


	/**
	 * @return the border
	 */
	public static BorderPane getBorder()
	{
		return border;
	}


	/**
	 * @return the uploadedImage
	 */
	public static Image getUploadedImage()
	{
		return uploadedImage;
	}


	/**
	 * @return the filePanel
	 */
	public static FilePanel getFilePanel()
	{
		return filePanel;
	}


	/**
	 * @return the imageOnScreen
	 */
	public static Image getImageOnScreen()
	{
		return imageOnScreen;
	}


	/**
	 * @param imageOnScreen
	 *            the imageOnScreen to set
	 */
	public static void setImageOnScreen(Image imageOnScreen)
	{
		ImageManipulator.imageOnScreen = imageOnScreen;
	}


	/**
	 * @return the uploadedFileName
	 */
	public static String getUploadedFileName()
	{
		return uploadedFileName;
	}
}