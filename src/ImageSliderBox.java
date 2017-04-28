import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// Creates a VBox with a slider and an OK button
public class ImageSliderBox extends VBox
{
	private HBox sliderRow;
	private VBox controlBox;
	private Slider intensitySlider;
	private Slider rotationSlider = new Slider(0, 360, 0);
	private Button okButton = new Button("OK");
	private ComboBox<Integer> reduceColorsBox;
	private ComboBox<String> blurBox;


	public ImageSliderBox(double spacing)
	{
		super(spacing);
		this.setAlignment(Pos.CENTER);

		intensitySlider = makeIntensitySlider();
		rotationSlider = makeRotationSlider();

		ObservableList<Integer> options = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7);
		reduceColorsBox = new ComboBox<Integer>(options);
		reduceColorsBox.setVisible(false);
		reduceColorsBox.valueProperty().set(1);

		ObservableList<String> blurOptions = FXCollections.observableArrayList("Box blur",
				"Gaussian Blur (3x3)", "Gaussian Blur (5x5)");
		blurBox = new ComboBox<String>(blurOptions);
		blurBox.setVisible(false);
		blurBox.valueProperty().set("Box blur");

		Label intensityLabel = new Label("Intensity");
		intensityLabel.setAlignment(Pos.CENTER_RIGHT);

		VBox sliderCol = new VBox(5);
		sliderCol.getChildren().addAll(intensitySlider, intensityLabel);

		sliderRow = new HBox(5);
		sliderRow.setAlignment(Pos.CENTER);
		sliderRow.getChildren().add(sliderCol);

		controlBox = new VBox(5);
		controlBox.setAlignment(Pos.CENTER);
		controlBox.getChildren().addAll(sliderRow, okButton);

		this.getChildren().add(controlBox);
	}


	private Slider makeIntensitySlider()
	{
		this.intensitySlider = new Slider(1, 4, 1);

		this.intensitySlider.setMajorTickUnit(1);
		this.intensitySlider.setMinorTickCount(0);
		this.intensitySlider.setShowTickLabels(true);
		this.intensitySlider.setShowTickMarks(true);
		this.intensitySlider.setSnapToTicks(true);
		this.intensitySlider.setPrefWidth(200);
		this.intensitySlider.setMaxWidth(200);
		this.intensitySlider.setId("image_slider");

		return intensitySlider;
	}


	private Slider makeRotationSlider()
	{
		this.rotationSlider = new Slider(0, 360, 0);

		this.rotationSlider.setMajorTickUnit(30);
		this.rotationSlider.setMinorTickCount(0);
		this.rotationSlider.setShowTickLabels(true);
		this.rotationSlider.setShowTickMarks(true);
		this.rotationSlider.setSnapToTicks(true);
		this.rotationSlider.setPrefWidth(200);
		this.rotationSlider.setMaxWidth(200);
		this.rotationSlider.setId("rotation_slider");

		return rotationSlider;
	}


	public Slider getIntensitySlider()
	{
		return this.intensitySlider;
	}


	public Slider getRotationSlider()
	{
		return this.rotationSlider;
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


	// TODO: could check if current slider is the one passed in
	public void setCurrentSlider(Slider slider)
	{
		this.sliderRow.getChildren().remove(0);
		this.sliderRow.getChildren().add(0, slider);
	}
}