import java.awt.Graphics;

/**
 * Rectangle implements the functionality needed to draw filled rectangles with
 * a given color
 *
 */
@SuppressWarnings("serial")
public class Rectangle extends Figure {

	public Rectangle(int x, int y, int newx, int newy) {
		this.coordinateX = x;
		this.coordinateY = y;
		this.newCoordinateX = newx;
		this.newCoordinatey = newy;
		this.width = Math.abs(newCoordinateX - coordinateX);
		this.height = Math.abs(newCoordinatey - coordinateY);
	}

	/**
	 * Draws the rectangle using the given Graphics parameter
	 * @param graphics the Graphics object for drawing the rectangle
	 */
	@Override
	public void draw(Graphics graphics) {
		graphics.setColor(color);
		graphics.fillRect(coordinateX, coordinateY, width, height);
	}

	/**
	 * Returns the String representation of a rectangle
	 */
	@Override
	public String toString() {
		return "Rectangle {x=" + coordinateX + ", y=" + coordinateY + ", width=" + width + ", height=" + height
				+ ", color=" + color.toString() + "}\n";
	}
}