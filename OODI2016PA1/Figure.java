import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

/**
 * This abstract class represents a figure that can be drawn on the GUI.
 *
 */
@SuppressWarnings("serial")
public abstract class Figure implements Serializable {
	protected int coordinateX, coordinateY, newCoordinateX, newCoordinatey;
	protected int width, height;
	protected Color color;

	/**
	 * Draws the figure using the given Graphics parameter
	 *
	 * @param graphics
	 *            the Graphics object for drawing the figure
	 */
	public void draw(Graphics graphics) {
		graphics.setColor(color);
	}
}