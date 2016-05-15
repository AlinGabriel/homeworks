package buttons;
import javax.swing.JButton;

import main.RefrigeratorDisplay;

public abstract class GUIButton extends JButton {
    private static final long serialVersionUID = 10L;

    public GUIButton(String string) {
        super(string);
    }

    public abstract void inform(RefrigeratorDisplay display);
}