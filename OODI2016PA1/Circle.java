import java.awt.Graphics;

/**
 * Circle implements the functionality needed to draw filled circles with a
 * given color
 *
 */
@SuppressWarnings("serial")
public class Circle extends Figure {
	protected int radius;

	public Circle(int x, int y, int newx, int newy) {
		this.coordinateX = x;
		this.coordinateY = y;
		this.newCoordinateX = newx;
		this.newCoordinatey = newy;
		this.width = Math.abs(newCoordinateX - coordinateX);
		this.height = Math.abs(newCoordinatey - coordinateY);
		this.radius = (int) Math.sqrt(width * width + height * height);
	}

	/**
	 * Draws the circle using the given Graphics parameter
	 * @param graphics the Graphics object for drawing the circle
	 */
	@Override
	public void draw(Graphics graphics) {
		graphics.setColor(color);
		graphics.fillOval(coordinateX - radius, coordinateY - radius, radius * 2, radius * 2);
	}

	/**
	 * Returns the String representation of a circle
	 */
	@Override
	public String toString() {
		return "Circle {x=" + coordinateX + ", y=" + coordinateY + ", radius=" + radius + ", color=" + color.toString()
				+ "}\n";
	}
}