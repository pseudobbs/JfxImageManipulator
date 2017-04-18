import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

// TODO: try just using ImageManipulator's slider directly in this class
public class Popup
{
	static Stage popupwindow = new Stage();
	static String title = "";
	static Number sliderValue = 0;


	public static void display()
	{
		// TODO: multiple calls to open the popup throw exception setting
		// modality more than once
		popupwindow.initModality(Modality.NONE);
		popupwindow.setTitle("This is a pop up window");

		Slider slider = new Slider(0, 3, 0);
		// slider.setBlockIncrement(1); why is this commented out?
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(0);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setSnapToTicks(true);
		slider.setPrefWidth(200);
		slider.setMaxWidth(200);
		slider.setVisible(true);
		// slider.setPrefSize(slider.getWidth(), 1);
		slider.setId("image_slider");
		slider.valueProperty().addListener((ov, old, newV) -> sliderValue = (double) newV);

		Button okbutton = new Button("OK");
		okbutton.setVisible(true);
		okbutton.setOnAction(e -> reportSliderValue(slider.getValue()));

		Button okBtn = new Button("Close");
		okBtn.setAlignment(Pos.CENTER);
		okBtn.setOnAction(e -> popupwindow.close());

		VBox layout = new VBox(10);
		layout.getChildren().addAll(slider, okBtn, okbutton);
		layout.setAlignment(Pos.CENTER);

		Scene scene1 = new Scene(layout, 300, 250);

		popupwindow.setScene(scene1);
		popupwindow.showAndWait();
	}


	static void reportSliderValue(double value)
	{
		ImageManipulator.setSliderValue(value);
	}


	public static void setTitle(String title)
	{
		Popup.title = title;
		popupwindow.setTitle(title);
	}
}