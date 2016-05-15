package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import buttons.DoorCloseButton;
import buttons.DoorOpenButton;
import buttons.GUIButton;
import buttons.TempSetButton;

public class Project3 extends RefrigeratorDisplay implements ActionListener {
	private static SimpleDisplay frame;

	protected Project3(ArrayList<Integer> configs) {
		frame = new SimpleDisplay();
		initialize(configs);
	}

	private class SimpleDisplay extends JFrame {
		private static final long serialVersionUID = 10L;

		private List<String> names = new ArrayList<>();
		private List<GUIButton> doorCloser = new ArrayList<>();
		private List<GUIButton> doorOpener = new ArrayList<>();

		private List<JLabel> light = new ArrayList<>();
		private List<JLabel> temp = new ArrayList<>();
		private List<JLabel> status = new ArrayList<>();

		private List<JTextField> tempInput = new ArrayList<>();
		private List<GUIButton> tempSet = new ArrayList<>();
		private List<JLabel> tempError = new ArrayList<>();

		private JTextField roomTemp = new JTextField("20");
		private GUIButton roomTempSet = new TempSetButton("Set Room Temp", RefrigeratorContext.ROOM);
		
		private JLabel roomTempError = new JLabel("");

		private SimpleDisplay() {
			super("Refrigerator");
			setDefaultCloseOperation(EXIT_ON_CLOSE);

			names.add(RefrigeratorContext.FRIDGE, "Fridge");
			names.add(RefrigeratorContext.FREEZER, "Freezer");

			for (int i = 0; i < names.size(); i++) {
				GUIButton closeButton = new DoorCloseButton("Close " + names.get(i) + " Door", i);
				closeButton.addActionListener(Project3.this);
				doorCloser.add(i, closeButton);
				GUIButton openButton = new DoorOpenButton("Open " + names.get(i) + " Door", i);
				openButton.addActionListener(Project3.this);
				doorOpener.add(i, openButton);
				light.add(i, new JLabel("Light Off"));
				temp.add(i, new JLabel("20"));
				status.add(i, new JLabel("idle"));
				tempInput.add(i, new JTextField("20"));
				GUIButton tempButton = new TempSetButton("Set " + names.get(i) + " Temp", i);
				tempButton.setBackground(Color.red);
				tempButton.setForeground(Color.white);
				tempButton.addActionListener(Project3.this);
				tempSet.add(i, tempButton);
				JLabel tempErrorLabel = new JLabel("");
				tempErrorLabel.setForeground(Color.RED);
				tempError.add(i, tempErrorLabel);
			}
			
			roomTempSet.setBackground(Color.red);
			roomTempSet.setForeground(Color.white);
			roomTempSet.addActionListener(Project3.this);

			JPanel pnlControlFields = new JPanel(new GridLayout(3, 4, 5, 5));
			for (int i = 0; i < names.size(); i++) {
				pnlControlFields.add(new JLabel("Desired " + names.get(i) + " temp"));
				pnlControlFields.add(tempError.get(i));
				pnlControlFields.add(tempInput.get(i));
				pnlControlFields.add(tempSet.get(i));
			}
			pnlControlFields.add(new JLabel("Room Temp"));
			pnlControlFields.add(roomTempError);
			pnlControlFields.add(roomTemp);
			pnlControlFields.add(roomTempSet);
			roomTempError.setForeground(Color.red);
			
			JPanel pnlControlButtons = new JPanel(new GridLayout(2, 2, 5, 5));
			for (int i = 0; i < names.size(); i++) {
				doorOpener.get(i).setBackground(Color.blue);
				doorOpener.get(i).setForeground(Color.white);
				pnlControlButtons.add(doorOpener.get(i));
				doorCloser.get(i).setBackground(Color.blue);
				doorCloser.get(i).setForeground(Color.white);
				pnlControlButtons.add(doorCloser.get(i));
				doorOpener.get(i).addActionListener(Project3.this);
				doorCloser.get(i).addActionListener(Project3.this);
			}
			
			JPanel pnlControlStatus = new JPanel(new GridLayout(3, 3, 5, 5));
			pnlControlStatus.add(new JLabel());
			JLabel lbl = new JLabel("Status");
			lbl.setForeground(Color.magenta);
			pnlControlStatus.add(lbl);
			pnlControlStatus.add(new JLabel());

			for (int i = 0; i < names.size(); i++) {
				JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
				lbl = new JLabel(names.get(i) + " light : ");
				lbl.setForeground(Color.magenta);
				panel.add(lbl);
				light.get(i).setForeground(Color.magenta);
				panel.add(light.get(i));
				lbl = new JLabel(names.get(i) + " temp : ");
				lbl.setForeground(Color.magenta);
				panel.add(lbl);
				temp.get(i).setForeground(Color.magenta);
				panel.add(temp.get(i));
				lbl = new JLabel(names.get(i) + " : ");
				lbl.setForeground(Color.magenta);
				panel.add(lbl);
				status.get(i).setForeground(Color.magenta);
				panel.add(status.get(i));
				pnlControlStatus.add(panel);
				pnlControlStatus.add(new JLabel());
			}
			
			setLayout(new BorderLayout(5, 5));
			add(pnlControlFields, BorderLayout.NORTH);
			add(pnlControlButtons, BorderLayout.CENTER);
			add(pnlControlStatus, BorderLayout.SOUTH);
			pack();
			setVisible(true);
		}
	}

	@Override
	public void turnLightOn(int type) {
		frame.light.get(type).setText("on");
	}

	@Override
	public void turnLightOff(int type) {
		frame.light.get(type).setText("off");
	}

	public void setCurrentTemp(int type) {
		frame.temp.get(type).setText(String.valueOf(RefrigeratorContext.instances().get(type).getCurrentTemp()));
	}

	public String getDesiredTemp(int type) {
		return frame.tempInput.get(type).getText();
	}

	@Override
	public void setDesiredTemp(int type) {
		frame.tempInput.get(type).setText(String.valueOf(RefrigeratorContext.instances().get(type).getSelectedTemp()));
	}

	public void setRoomDesiredTemp() {
		frame.roomTemp.setText(String.valueOf(RefrigeratorContext.getRoomTemp()));
	}

	public String getRoomDesiredTemp() {
		return frame.roomTemp.getText();
	}
	
	@Override
	public void startCooling(int type) {
		frame.status.get(type).setText("cooling");
	}

	@Override
	public void stopCooling(int type) {
		frame.status.get(type).setText("idle");
	}

	public void showError(int type) {
		frame.tempError.get(type).setText("must be " + RefrigeratorContext.instances().get(type).getLowTemp()
				+ " <=> " + RefrigeratorContext.instances().get(type).getHighTemp());
	}

	public void showRoomTempError() {
		frame.roomTempError.setText("must be " + RefrigeratorContext.getLowRoomTemp() + " <=> "
				+ RefrigeratorContext.getHighRoomTemp());
	}

	public void hideError(int type) {
		frame.tempError.get(type).setText("");
	}
	
	public void hideRoomTempError() {
		frame.roomTempError.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		((GUIButton) event.getSource()).inform(instance);
	}
	
	public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            ArrayList<Integer> configs = new ArrayList<>();
            String s;
            while ((s = br.readLine()) != null) {
                configs.add(Integer.parseInt(s));
            }
            br.close();
            new Project3(configs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}