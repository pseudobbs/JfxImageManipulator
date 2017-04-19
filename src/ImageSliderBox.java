import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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

		okButton.setVisible(false);

		this.getChildren().addAll(slider, okButton);
	}


	public Slider getSlider()
	{
		return this.slider;
	}


	public Button getOkButton()
	{
		return this.okButton;
	}
}