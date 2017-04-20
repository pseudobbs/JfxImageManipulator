import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

// TODO: try just using ImageManipulator's slider directly in this class
// TODO: canceling should revert to original image (particular problems with reduce colors)
// TODO: implement stacks here so the popup can undo/redo
public class Popup
{
	Stage popupwindow = new Stage();
	ImageSliderBox sliderBox;
	HBox status;
	HBox buttons;
	Label statusText;
	String title;
	double sliderValue;

	Button cancelButton = new Button("Cancel");
	Button acceptButton = new Button("Accept");


	public Popup()
	{
		popupwindow.initModality(Modality.NONE);
	};


	public void display()
	{
		sliderBox = new ImageSliderBox(10);
		sliderBox.getSlider().setVisible(true);
		sliderBox.setAlignment(Pos.CENTER);
		sliderBox.getSlider().valueProperty()
				.addListener((ov, old, newV) -> sliderValue = (double) newV);

		Button previewButton = sliderBox.getOkButton();
		previewButton.setVisible(true);
		previewButton.setText("Preview");
		previewButton.setOnAction(e -> {
			// TODO: this doesn't happen until after the next call. why not?
			statusText.setVisible(true);
			ImageManipulator.setSliderValue(sliderValue);
			// statusText.setText("");
		});

		acceptButton.setAlignment(Pos.CENTER_LEFT);
		acceptButton.setOnAction(e -> {
			ImageManipulator.pressOK();
			popupwindow.close();
		});

		cancelButton.setAlignment(Pos.CENTER_RIGHT);
		cancelButton.setOnAction(e -> {
			cancelButton.setVisible(false);
			popupwindow.close();
		});

		buttons = new HBox(5);
		buttons.getChildren().addAll(acceptButton, cancelButton);

		status = new HBox(5);
		statusText = new Label("Processing...");
		statusText.setVisible(false);
		status.getChildren().add(statusText);

		sliderBox.getChildren().addAll(buttons, status);

		Scene scene1 = new Scene(sliderBox, 300, 250);

		popupwindow.setScene(scene1);
		popupwindow.showAndWait();
	}


	public void setTitle(String title)
	{
		this.title = title;
		popupwindow.setTitle(title);
	}


	public Button getCancelButton()
	{
		return cancelButton;
	}


	public ImageSliderBox getSliderBox()
	{
		return sliderBox;
	}


	public void setStatusText(String text)
	{
		statusText.setText(text);
	}
}