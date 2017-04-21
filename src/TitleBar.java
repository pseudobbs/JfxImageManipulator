import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class TitleBar extends HBox
{
	public TitleBar()
	{
		this.setPadding(new Insets(15, 12, 15, 12));
		this.setAlignment(Pos.CENTER);
		this.setStyle("-fx-background-color: #369;");

		// title bar
		Label label = new Label("JavaFX Image Manipulator");
		label.setFont(new Font("Stencil", 24));
		label.setTextFill(Color.WHITE);
		label.setTextAlignment(TextAlignment.CENTER);
		label.setAlignment(Pos.CENTER);

		// add title bar to the box
		this.getChildren().add(label);
	}
}
