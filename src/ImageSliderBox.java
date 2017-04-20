import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// Creates a VBox with a slider and an OK button
public class ImageSliderBox extends VBox
{
	private Slider slider = new Slider(0, 3, 0);
	private Button okButton = new Button("OK");


	public ImageSliderBox(double spacing)
	{
		super(spacing);
		this.setAlignment(Pos.CENTER);

		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(0);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setSnapToTicks(true);
		slider.setPrefWidth(200);
		slider.setMaxWidth(200);
		slider.setVisible(false);
		slider.setId("image_slider");

		okButton.setVisible(false);

		HBox sliderRow = new HBox(5);
		sliderRow.getChildren().addAll(slider, okButton);
		this.getChildren().addAll(sliderRow);
	}


	public Slider getSlider()
	{
		return this.slider;
	}


	// TODO: OK button text should be the name of the manipulation
	public Button getOkButton()
	{
		return this.okButton;
	}
}