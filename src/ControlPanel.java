import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Uses reflection to find the public methods of the MyImage class, then Creates
 * a button for each method so the user can access it.
 * 
 * @author Shawn
 *
 */
public class ControlPanel extends VBox
{
	public ControlPanel()
	{
		this.setPadding(new Insets(10));
		this.setSpacing(8);

		// get methods of image class
		Method[] manipulations = MyImage.class.getDeclaredMethods();

		// create buttons for each manipulation method
		for (Method method : manipulations)
		{
			// don't need buttons for private methods
			if (!Modifier.isPublic(method.getModifiers()))
			{
				continue;
			}

			// create button for each of MyImage's manipulation methods
			Button button = new Button(capitalize(method.getName()));
			button.setPrefWidth(125);
			button.setId(method.getName());
			button.setOnAction((event) -> ImageManipulator.manipulateImage(method));
			this.getChildren().add(button);
		}

		this.setAlignment(Pos.CENTER);
	}


	/**
	 * Capitalizes the first letter of a string and replaces underscores with
	 * spaces
	 * 
	 * @param s
	 *            the string to capitalize
	 * 
	 * @return the capitalized string
	 */
	private String capitalize(String s)
	{
		return (s.substring(0, 1).toUpperCase() + s.substring(1)).replaceAll("_", " ");
	}
}
