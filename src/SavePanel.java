import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class SavePanel extends VBox
{
	private static final Logger LOGGER = Logger.getLogger(SavePanel.class.getName());


	public SavePanel(Button undoButton, Button redoButton)
	{
		this.setPadding(new Insets(15));
		this.setSpacing(15);
		this.setAlignment(Pos.CENTER);

		// so undo/redo can be side by side
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(15));
		hbox.setSpacing(15);
		hbox.setAlignment(Pos.CENTER);

		undoButton.setOnAction((event) -> ImageManipulator.undo());
		redoButton.setOnAction((event) -> ImageManipulator.redo());

		Button saveButton = new Button("Save image");
		saveButton.setOnAction((event) -> saveImage());

		Button discardButton = new Button("Discard image");
		discardButton.setOnAction((event) -> discard());

		Button revertButton = new Button("Revert changes");
		revertButton.setOnAction((event) -> revert());

		hbox.getChildren().addAll(undoButton, redoButton);
		this.getChildren().addAll(hbox, saveButton, discardButton, revertButton);
	}


	/**
	 * Allows the user to save the image on screen to a location of their choice
	 * 
	 */
	private void saveImage()
	{
		BufferedImage outputImage = SwingFXUtils.fromFXImage(ImageManipulator.getImageOnScreen(),
				null);
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.bmp"));
		chooser.setTitle("Save file");
		chooser.setInitialFileName(ImageManipulator.getUploadedFileName() + "_manip");
		File savedFile = chooser.showSaveDialog(ImageManipulator.getPrimaryStage());

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


	private void revert()
	{
		ImageManipulator.getRedoStack().clear();
		ImageManipulator.getBorder().setBottom(null);
		ImageManipulator.showImage(ImageManipulator.getUploadedImage());
	}


	/**
	 * Resets the window to its initial on-load state
	 * 
	 */
	private void discard()
	{
		ImageManipulator.setImageOnScreen(null);
		ImageManipulator.setUploadedImage(null);
		ImageManipulator.setUploadedFileName(null);
		ImageManipulator.getBorder().setLeft(null);
		ImageManipulator.getBorder().setCenter(ImageManipulator.getFilePanel());
		ImageManipulator.getBorder().setRight(null);
		ImageManipulator.getBorder().setBottom(null);
		ImageManipulator.getPrimaryStage().setWidth(400);
		ImageManipulator.getPrimaryStage().setHeight(300);
	}
}
