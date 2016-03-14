import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/*
 * Author: Arhip Alin Gabriel
 * Artificial Intelligence Master Program, first year.
 *
 * Simple Java Drawing Application using AWT and Swing.
 * It can draw rectangles and circles of
 * three colors: red, green and blue. It also shows the
 * current date.
 *
 * When the exit button is clicked, the GUI
 * serializes the figures and content of the text area
 * to disk in a file named "figures",
 * basically saving the current state of the drawings.
 *
 * When the GUI is started, it will check for the existence
 * of the "figures" file and if exists, opens it and populates
 * the figures and the corresponding content in the text area,
 * deserializing any saved figures and redrawing them.
 *
 * The "X" Close Button was created intentionally to not
 * save any contents. For this purpose exists the
 * "Exit" button.
 */
@SuppressWarnings("serial")
public class FigureGUI extends JFrame implements ActionListener {
	private static enum Action {
		RECTANGLE, CIRCLE
	};

	private Color colors[] = { Color.red, Color.green, Color.blue };
	private JButton redButton = new JButton("Red");
	private JButton greenButton = new JButton("Green");
	private JButton blueButton = new JButton("Blue");
	private JButton rectangleButton = new JButton("Rectangle");
	private JButton circleButton = new JButton("Circle");
	private JButton exitButton = new JButton("Exit");
	private JTextArea listArea = new JTextArea(10, 10);
	private FiguresPanel figuresPanel = new FiguresPanel();
	private GregorianCalendar currentDate;
	private Action action = Action.RECTANGLE;
	private Color color = Color.RED;
	private int index = 0;
	private ArrayList<Figure> figures = new ArrayList<Figure>();

	/* Internal class that represents the drawing area of the GUI */
	public class FiguresPanel extends JPanel implements MouseListener {

		private int coordinateX, coordinateY, newCoordinateX, newCoordinateY;
		private int clicksCounter = 1, offset = 10;
		private String formattedDate;

		@Override
		public void paintComponent(Graphics graphics) {
			/* draw all figures */
			for (Figure figure : figures)
				figure.draw(graphics);
			/* put the date */
			graphics.setColor(Color.black);
			graphics.drawString(formattedDate, offset, this.getHeight() - offset);
		}

		/**
		 * Waits for the mouse click and creates the appropriate figures.
		 */
		@Override
		public void mouseClicked(MouseEvent event) {
			Figure figure;

			if (clicksCounter == 1) {
				/* set the first click X and Y coordinates */
				clicksCounter = 0;
				coordinateX = event.getX();
				coordinateY = event.getY();
			} else {
				/* set the second click's X and Y coordinates */
				clicksCounter = 1;
				newCoordinateX = event.getX();
				newCoordinateY = event.getY();

				/*
				 * Create the desired figure: a circle or a rectangle. The
				 * default action is to draw a rectangle
				 */
				switch (action) {
				case CIRCLE:
					figure = new Circle(coordinateX, coordinateY, newCoordinateX, newCoordinateY);
					break;
				default:
					figure = new Rectangle(coordinateX, coordinateY, newCoordinateX, newCoordinateY);
					break;
				}

				/* set color, set text and paint figure */
				figure.color = color;
				listArea.append(figure.toString());
				figures.add(figure);
				this.paintComponent(getGraphics());
			}
		}

		/**
		 * Not used
		 */
		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		/**
		 * Not used
		 */
		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		/**
		 * Not used
		 */
		@Override
		public void mousePressed(MouseEvent arg0) {
		}

		/**
		 * Not used
		 */
		@Override
		public void mouseReleased(MouseEvent arg0) {
		}
	}

	/**
	 * Sets up the entire interface.
	 */
	@SuppressWarnings("unchecked")
	public FigureGUI() {
		super("Figures GUI");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		/* set up the panels */
		JPanel buttonPanel = new JPanel(new GridLayout(2, 3));
		JPanel listPanel = new JPanel(new GridLayout(1, 1));
		JPanel mainPanel = new JPanel(new GridLayout(1, 4));
		/* set up the buttonPanels and add Listeners to them */
		buttonPanel.add(redButton, 0, 0);
		redButton.addActionListener(this);
		buttonPanel.add(greenButton, 0, 1);
		greenButton.addActionListener(this);
		buttonPanel.add(blueButton, 0, 2);
		blueButton.addActionListener(this);
		buttonPanel.add(rectangleButton, 0, 3);
		rectangleButton.addActionListener(this);
		buttonPanel.add(circleButton, 0, 4);
		circleButton.addActionListener(this);
		buttonPanel.add(exitButton, 0, 5);
		exitButton.addActionListener(this);

		listPanel.add(new JScrollPane(listArea));
		mainPanel.add(figuresPanel);
		figuresPanel.addMouseListener(figuresPanel);

		/* try to deserialize all figures and their contents, if the
		 * "figures" file exists */
		try {
			File file = new File("figures");
			if (file.exists()) {
				FileInputStream fileIn = new FileInputStream(file);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				ArrayList<Figure> figuresOnDisk = (ArrayList<Figure>) in.readObject();
				figures.addAll(figuresOnDisk);
				listArea.setText((String) in.readObject());
				in.close();
				fileIn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* set up the date */
		currentDate = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		figuresPanel.formattedDate = dateFormat.format(currentDate.getTime());

		/* add all panels to the GUI and make it visible */
		setLayout(new GridLayout());
		add(mainPanel);
		add(buttonPanel);
		add(listPanel);
		pack();
		setVisible(true);
	}

	/**
	 * Listener for all buttons.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();

		if (command.equals(redButton.getText()))
			color = colors[index];
		else if (command.equals(greenButton.getText()))
			color = colors[1 - index];
		else if (command.equals(blueButton.getText()))
			color = colors[2 - index];
		else if (command.equals(rectangleButton.getText()))
			action = Action.RECTANGLE;
		else if (command.equals(circleButton.getText()))
			action = Action.CIRCLE;
		else if (command.equals(exitButton.getText())) {
			/* try to serialize all figures and text, before exiting the GUI */
			try {
				FileOutputStream fileOut = new FileOutputStream("figures");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(figures);
				out.writeObject(listArea.getText());
				out.close();
				fileOut.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

	/**
	 * The method creates an FigureGUI object
	 */
	public static void main(String[] args) {
		new FigureGUI();
	}
}