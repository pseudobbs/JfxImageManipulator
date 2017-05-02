import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class FilePanel extends VBox
{
	private static final Logger LOGGER = Logger.getLogger(FilePanel.class.getName());

	Image selectedImage;
	String uploadedFileName;


	public FilePanel()
	{
		this.setPadding(new Insets(10));
		this.setAlignment(Pos.CENTER);

		Button selectFileButton = new Button("Select image...");
		selectFileButton.setOnAction((event) -> fileChooser(event));

		this.getChildren().add(selectFileButton);
	}


	void fileChooser(ActionEvent event)
	{
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.bmp"));

		// open file chooser
		File selectedFile = chooser.showOpenDialog(null);
		selectedImage = null;

		try
		{
			// remove extension from filename and save to variable
			ImageManipulator
					.setUploadedFileName(selectedFile.getName().replaceAll("\\.\\w{3,4}$", ""));
			selectedImage = new Image(new FileInputStream(selectedFile));
			ImageManipulator.processUploadedImage();
		}
		catch (FileNotFoundException | NullPointerException e)
		{
			LOGGER.log(Level.INFO, "User cancelled file selection");
		}
	}


	/**
	 * @return the selectedImage
	 */
	public Image getSelectedImage()
	{
		return selectedImage;
	}


	/**
	 * @param selectedImage
	 *            the selectedImage to set
	 */
	public void setSelectedImage(Image selectedImage)
	{
		this.selectedImage = selectedImage;
	}


	/**
	 * @return the uploadedFileName
	 */
	public String getUploadedFileName()
	{
		return uploadedFileName;
	}


	/**
	 * @param uploadedFileName
	 *            the uploadedFileName to set
	 */
	public void setUploadedFileName(String uploadedFileName)
	{
		this.uploadedFileName = uploadedFileName;
	}
}
