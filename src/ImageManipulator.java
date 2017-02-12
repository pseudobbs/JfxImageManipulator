import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImageManipulator extends Application
{
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
		hbox.setStyle("-fx-background-color: #336699;");

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


	// Rotate Enlarge Shrink Grayscale Exit
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

			//
			Button button = new Button(capitalize(method.getName()));
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

		Button saveButton = new Button("Save image");
		saveButton.setOnAction((event) -> saveImage());

		Button discardButton = new Button("Discard image");
		discardButton.setOnAction((event) -> reset(event));

		Button revertButton = new Button("Revert changes");
		revertButton.setOnAction((event) -> revert(event));

		vbox.getChildren().addAll(saveButton, discardButton, revertButton);

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
			uploadedFileName = selectedFile.getName().replaceAll("\\.\\w{3}$", "");
			selectedImage = new Image(new FileInputStream(selectedFile));
		}
		catch (FileNotFoundException | NullPointerException e)
		{
			actionStatus.setText("File selection cancelled");
			Logger.getGlobal().log(Level.INFO, "User cancelled file selection");
		}

		// keep a copy of the original for revert changes function
		uploadedImage = selectedImage;

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

		showImage(selectedImage);

	}


	private void manipulateImage(Method manipulation)
	{
		MyImage imageToWrite = new MyImage(imageOnScreen);

		try
		{
			int w = 2;

			Parameter[] parameters = manipulation.getParameters();

			if (parameters.length == 0)
			{
				manipulation.invoke(imageToWrite);
			}
			else
			{
				manipulation.invoke(imageToWrite, w);
			}
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Logger.getLogger(ImageManipulator.class.getName()).log(Level.SEVERE,
					"Problem executing method " + manipulation.getName(), e);
		}

		showImage(imageToWrite);
	}


	/**
	 * Displays an image in the center panel
	 * 
	 * @param image
	 *            the image to display
	 */
	private void showImage(Image image)
	{
		ImageView imageView = new ImageView(image);

		// fit height of 0 means set the dimensions to match the image contained
		imageView.setFitHeight(0);
		imageView.setFitWidth(0);

		// clear out previous image in the image panel and add a new one
		imagePanel.getChildren().clear();
		imagePanel.getChildren().add(imageView);

		// set correct panels for user
		border.setCenter(imagePanel);
		border.setLeft(controlPanel);
		border.setRight(savePanel);

		// keep track of what image is currently displaying
		imageOnScreen = image;
	}


	/**
	 * Allows the user to save the image on screen to a location of their choice
	 * 
	 * TODO: inform user image was not saved (popup?)
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
			Logger.getGlobal().log(Level.WARNING, "Image not saved");
		}
	}


	/**
	 * Listener function to dynamically resize the window to fit the content
	 * 
	 * 
	 * TODO: "o" may not be needed since we don't actually use it. come back and
	 * double check after you get to where we can load a different image and
	 * resize smaller (toggle basically)
	 * 
	 * TODO: handle if the image is bigger than the screen (scrollbars)
	 */
	private void resize(double deltaX, double deltaY)
	{
		primaryStage.setWidth(primaryStage.getWidth() + deltaX);
		primaryStage.setHeight(primaryStage.getHeight() + deltaY);
	}


	private void revert(ActionEvent event)
	{
		showImage(uploadedImage);
	}


	private void reset(ActionEvent event)
	{
		border.setLeft(null);
		border.setCenter(filePanel);
		border.setRight(null);
	}


	/**
	 * capitalizes the first letter of a string
	 * 
	 * @param s
	 *            the string to capitalize
	 * 
	 * @return the capitalized string
	 */
	private String capitalize(String s)
	{
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
}