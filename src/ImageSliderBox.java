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
	private HBox sliderRow;
	private VBox controlBox;
	private Slider slider = new Slider(1, 4, 1);
	private Button okButton = new Button("OK");
	private ComboBox<Integer> reduceColorsBox;
	private ComboBox<String> blurBox;


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
		reduceColorsBox = new ComboBox<Integer>(options);
		reduceColorsBox.setVisible(false);
		reduceColorsBox.valueProperty().set(1);

		ObservableList<String> blurOptions = FXCollections.observableArrayList("Box blur",
				"Gaussian Blur (3x3)", "Gaussian Blur (5x5)");
		blurBox = new ComboBox<String>(blurOptions);
		blurBox.setVisible(false);
		blurBox.valueProperty().set("Box blur");

		sliderRow = new HBox(5);
		sliderRow.setAlignment(Pos.CENTER);
		sliderRow.getChildren().add(slider);

		controlBox = new VBox(5);
		controlBox.setAlignment(Pos.CENTER);
		controlBox.getChildren().addAll(sliderRow, okButton);

		this.getChildren().add(controlBox);
	}


	public Slider getSlider()
	{
		return this.slider;
	}


	public Button getOkButton()
	{
		return this.okButton;
	}


	public ComboBox<Integer> getReduceColorsBox()
	{
		return this.reduceColorsBox;
	}


	public ComboBox<String> getBlurBox()
	{
		return this.blurBox;
	}


	public HBox getSliderRow()
	{
		return this.sliderRow;
	}


	public ComboBox<?> getCurrentComboBox()
	{
		if (sliderRow.getChildren().size() > 1)
		{
			return (ComboBox<?>) sliderRow.getChildren().get(1);
		}

		return null;
	}


	public void setCurrentComboBox(ComboBox<?> comboBox)
	{
		if (this.sliderRow.getChildren().size() > 1)
		{
			this.sliderRow.getChildren().remove(1);
		}

		this.sliderRow.getChildren().add(comboBox);
	}
}