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

// TODO: package access instead of getter/setters in this class only
// TODO: clean up getters/setters, some might be unused
// TODO: undo button goes too far
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
		border.setCenter(getFilePanel());
		border.setBottom(null);

		// add elements to window
		root = new FlowPane();
		scene = new Scene(root, 400, 300);
		root.getChildren().add(getBorder());

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
		uploadedImage = getFilePanel().getSelectedImage();

		// clears the stack if there is garbage from a previous image in it
		undoStack.clear();
		getRedoStack().clear();

		// resize window based on image size
		if (getImageOnScreen() != null)
		{
			double deltaX = uploadedImage.getWidth() - getImageOnScreen().getWidth();
			double deltaY = uploadedImage.getHeight() - getImageOnScreen().getHeight();

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
		MyImage imageToWrite = new MyImage(getImageOnScreen());
		Parameter[] parameters = manipulation.getParameters();

		// if we need a parameter, present a slider for the user to input
		if (parameters.length != 0)
		{
			// listen for user input
			sliderBox.getSlider().valueProperty().addListener(
					(ov, old, newV) -> invokeWithValue(newV, manipulation, imageToWrite));

			border.setBottom(sliderBox);
		}
		else
		{
			try
			{
				manipulation.invoke(imageToWrite);
				getRedoStack().clear();
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
		setImageOnScreen(image);
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
	private static void invokeWithValue(Number newValue, Method manipulation, MyImage imageToWrite)
	{
		if (newValue.doubleValue() % 1 != 0)
		{
			return;
		}
		try
		{
			// keep a copy of the original to use if the user keeps using the
			// slider. This way we can apply a more intense effect to the
			// original image rather than applying a new effect to an already
			// manipulated image
			copy = new MyImage(getImageOnScreen());
			boolean useCopy = getImageOnScreen() != imageToWrite;

			manipulation.invoke(useCopy ? copy : imageToWrite, newValue.intValue());
			getRedoStack().clear();

			// shows the image after manipulation without calling showImage,
			// which has side effects we don't want until the user clicks "OK"
			imagePanel.getChildren().set(0, new ImageView(useCopy ? copy : imageToWrite));

			sliderBox.getOkButton().setVisible(true);
			sliderBox.getOkButton().setOnAction((e) -> showImage(useCopy ? copy : imageToWrite));

		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			LOGGER.log(Level.SEVERE, "Problem executing method " + manipulation.getName()
					+ "with argument " + newValue, e);
		}
	}


	/**
	 * @return the primaryStage
	 */
	public static Stage getPrimaryStage()
	{
		return primaryStage;
	}


	/**
	 * @param primaryStage
	 *            the primaryStage to set
	 */
	public static void setPrimaryStage(Stage primaryStage)
	{
		ImageManipulator.primaryStage = primaryStage;
	}


	/**
	 * @return the redoStack
	 */
	public static Stack<Image> getRedoStack()
	{
		return redoStack;
	}


	/**
	 * @param redoStack
	 *            the redoStack to set
	 */
	public static void setRedoStack(Stack<Image> redoStack)
	{
		ImageManipulator.redoStack = redoStack;
	}


	/**
	 * @return the border
	 */
	public static BorderPane getBorder()
	{
		return border;
	}


	/**
	 * @param border
	 *            the border to set
	 */
	public static void setBorder(BorderPane border)
	{
		ImageManipulator.border = border;
	}


	/**
	 * @return the uploadedImage
	 */
	public static Image getUploadedImage()
	{
		return uploadedImage;
	}


	/**
	 * @param uploadedImage
	 *            the uploadedImage to set
	 */
	public static void setUploadedImage(Image uploadedImage)
	{
		ImageManipulator.uploadedImage = uploadedImage;
	}


	/**
	 * @return the filePanel
	 */
	public static FilePanel getFilePanel()
	{
		return filePanel;
	}


	/**
	 * @param filePanel
	 *            the filePanel to set
	 */
	public static void setFilePanel(FilePanel filePanel)
	{
		ImageManipulator.filePanel = filePanel;
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


	/**
	 * @param uploadedFileName
	 *            the uploadedFileName to set
	 */
	public static void setUploadedFileName(String uploadedFileName)
	{
		ImageManipulator.uploadedFileName = uploadedFileName;
	}
}