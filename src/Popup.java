import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

// TODO: try just using ImageManipulator's slider directly in this class
// TODO: canceling should revert to original image (particular problems with reduce colors)
public class Popup
{
	static Stage popupwindow = new Stage();
	static String title = "";
	static double sliderValue = 0;


	public static void display()
	{
		// TODO: multiple calls to open the popup throw exception setting
		// modality more than once
		popupwindow.initModality(Modality.NONE);

		ImageSliderBox sliderBox = new ImageSliderBox(10);
		sliderBox.setAlignment(Pos.CENTER);

		Button previewButton = sliderBox.getOkButton();
		sliderBox.getSlider().setVisible(true);
		previewButton.setVisible(true);
		previewButton.setText("Preview");
		previewButton.setOnAction(e -> ImageManipulator.setSliderValue(sliderValue));
		sliderBox.getSlider().valueProperty()
				.addListener((ov, old, newV) -> sliderValue = (double) newV);

		Button acceptButton = new Button("Accept");
		acceptButton.setAlignment(Pos.CENTER_LEFT);
		acceptButton.setOnAction(e -> {
			ImageManipulator.pressOK();
			popupwindow.close();
		});

		Button closeButton = new Button("Cancel");
		closeButton.setAlignment(Pos.CENTER_RIGHT);
		closeButton.setOnAction(e -> {
			sliderBox.getSlider().valueProperty().set(0);
			previewButton.fireEvent(new ActionEvent());
			popupwindow.close();
		});

		sliderBox.getChildren().addAll(acceptButton, closeButton);

		Scene scene1 = new Scene(sliderBox, 300, 250);

		popupwindow.setScene(scene1);
		popupwindow.showAndWait();
	}


	public static void setTitle(String title)
	{
		Popup.title = title;
		popupwindow.setTitle(title);
	}
}