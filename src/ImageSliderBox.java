import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// Creates a VBox with a slider and an OK button
public class ImageSliderBox extends VBox
{
	private Slider slider = new Slider(1, 4, 1);
	private Button okButton = new Button("OK");
	private ComboBox<Integer> comboBox;


	// TODO: add labels to controls
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
		slider.setId("image_slider");

		ObservableList<Integer> options = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7);
		comboBox = new ComboBox<Integer>(options);
		comboBox.setVisible(false);
		comboBox.valueProperty().set(1);

		HBox sliderRow = new HBox(5);
		sliderRow.setAlignment(Pos.CENTER);

		sliderRow.getChildren().addAll(slider, comboBox, okButton);
		this.getChildren().addAll(sliderRow);
	}


	public Slider getSlider()
	{
		return this.slider;
	}


	public Button getOkButton()
	{
		return this.okButton;
	}


	public ComboBox<Integer> getComboBox()
	{
		return this.comboBox;
	}
}