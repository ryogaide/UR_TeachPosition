package com.loccioni.teachposition.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.userinteraction.RobotPositionCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.userinteraction.robot.movement.MovementCompleteEvent;
import com.ur.urcap.api.domain.userinteraction.robot.movement.RobotMovementCallback;
import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.PoseFactory;
import com.ur.urcap.api.domain.value.jointposition.JointPosition;
import com.ur.urcap.api.domain.value.jointposition.JointPositionFactory;
import com.ur.urcap.api.domain.value.jointposition.JointPositions;
import com.ur.urcap.api.domain.value.simple.Angle;
import com.ur.urcap.api.domain.value.simple.Length;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.api.domain.variable.VariableModel;

public class TeachPositionInstallationNodeContribution implements InstallationNodeContribution {

	private static final String POPUPTITLE_KEY = "popuptitle";
	private static final String DEFAULT_VALUE = "Teach Position";
	
	private final TeachPositionInstallationNodeView view;
	private final KeyboardInputFactory keyboardFactory;
	private DataModel model;
	private final InstallationAPIProvider apiProvider;
	private String line;
	private final PoseFactory poseFactory;
	private final JointPositionFactory jPosFactory;
	private final Pose PoseDefaultValue;
	private final JointPositions jPosDefaultValue;
	private final VariableModel varModel;
	private File fileGlobal;
	
	public TeachPositionInstallationNodeContribution(InstallationAPIProvider apiProvider, DataModel model, 
			TeachPositionInstallationNodeView view) {
		this.keyboardFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		this.model = model;
		this.view = view;
		this.poseFactory = apiProvider.getInstallationAPI().getValueFactoryProvider().getPoseFactory();
		this.jPosFactory = apiProvider.getInstallationAPI().getValueFactoryProvider().getJointPositionFactory();
		this.apiProvider = apiProvider;
		this.PoseDefaultValue = poseFactory.createPose(0, 0, 0, 0, 0, 0, Length.Unit.M, Angle.Unit.RAD);
		this.jPosDefaultValue = jPosFactory.createJointPositions(0, 0, 0, 0, 0, 0, Angle.Unit.RAD);
		this.varModel = apiProvider.getInstallationAPI().getVariableModel();
		
	}
	
	java.net.URL fileURL = getClass().getResource("code.txt");
	
	public void SetPosition() {
		final int selectedRow = view.table.getSelectedRow();
		if(selectedRow  == -1) {
			JOptionPane.showMessageDialog(null, "You have to select one row", "Message", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final String nameKey = view.tableModel.getValueAt(selectedRow, 0).toString();
		apiProvider.getUserInterfaceAPI().getUserInteraction().getUserDefinedRobotPosition(new RobotPositionCallback() {
			@Override
			public void onOk(Pose pose, JointPositions jointPositions) {
				if(model.isSet(nameKey) == true) {
					model.set(nameKey, pose);
				}else if(model.isSet(nameKey) == false) {
					model.set("*" + nameKey, jointPositions);
				}
				
				double[] data_double = pose.toArray(Length.Unit.M ,Angle.Unit.RAD);
				for(int i=1; i<view.tableModel.getColumnCount(); i++) {
					view.tableModel.setValueAt(data_double[i-1], selectedRow, i);
				}
			}
		});
	}
	
	public void onGotoButtonPressed() {
		final int selectedRow = view.table.getSelectedRow();
		if(selectedRow  == -1) {
			JOptionPane.showMessageDialog(null, "You have to select one row", "Message", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String nameKey = view.tableModel.getValueAt(selectedRow, 0).toString();
		if(model.isSet(nameKey) == true) {
			Pose pose = model.get(nameKey, PoseDefaultValue);
			apiProvider.getUserInterfaceAPI().getUserInteraction().getRobotMovement()
			.requestUserToMoveRobot(pose, new RobotMovementCallback() {
				@Override
				public void onComplete(MovementCompleteEvent event) {
				}
			});
		}else if(model.isSet(nameKey) == false){
			JointPositions jPos = model.get("*" + nameKey, jPosDefaultValue);
			apiProvider.getUserInterfaceAPI().getUserInteraction().getRobotMovement()
			.requestUserToMoveRobot(jPos, new RobotMovementCallback() {
				@Override
				public void onComplete(MovementCompleteEvent event) {
				}
			});
		}
	}
	
	public void onUrSelected(String RobotName) {
		JFileChooser filechooser = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Position file(.variables)", "variables");
		filechooser.addChoosableFileFilter(filter);
		filechooser.setFileFilter(filter);
		
		int selected = filechooser.showOpenDialog(null);
		if(selected == JFileChooser.APPROVE_OPTION) {
			fileGlobal =  filechooser.getSelectedFile();
			if(checkBeforeReadFile(fileGlobal) != true) return;
			try {
				removeAllKeys();
				view.tableModel.deleteAll();
				BufferedReader br = new BufferedReader(new FileReader(fileGlobal));
				while((line = br.readLine()) != null) {
					if(line.contains("p[")) {
						String[] temp = line.split("=");
						String name = temp[0];
						String valueString = temp[1].substring(2, temp[1].length() - 1);
						temp = valueString.split(",");
						Double[] valueDouble = new Double[6];
						for(int i=0; i<6; i++) {
							valueDouble[i] = Double.valueOf(temp[i].trim());
						}
						model.set(name, poseFactory.createPose(valueDouble[0], valueDouble[1], valueDouble[2], 
								valueDouble[3], valueDouble[4], valueDouble[5], Length.Unit.M, Angle.Unit.RAD));
						view.tableModel.addRow(name, valueDouble);
					}else if(line.contains("[")) {
						String[] temp = line.split("=");
				    	String name = temp[0];
				    	String valueString = temp[1].substring(1, temp[1].length()-1);
				    	temp = valueString.split(",");
				    	Double[] valueDouble = new Double[6];
				    	for (int i=0; i<6; i++) {
				    		valueDouble[i] = Double.valueOf(temp[i].trim());
				    	}
				    	model.set("*"+name, jPosFactory.createJointPositions(valueDouble[0], valueDouble[1],
				    			valueDouble[2], valueDouble[3], valueDouble[4], valueDouble[5], Angle.Unit.RAD));
				    	view.tableModel.addRow(name, valueDouble);
					}
				}
				br.close();
			} catch (FileNotFoundException err) {
				System.out.println(err);
			} catch (IOException err) {
				System.out.println(err);
			}
		}else if(selected == JFileChooser.CANCEL_OPTION) {
			System.out.println("CANCEL");
		}else if(selected == JFileChooser.ERROR_OPTION) {
			System.out.println("ERROR");
		}
	}
	
	private void removeAllKeys() {
		Set<String> keySet = model.getKeys();
		Iterator<String> keysI = keySet.iterator();
		while (keysI.hasNext()) {
			model.remove(keysI.next());
		}
	}
	
	public void testPrint() {
//		Collection<Variable> variables = varModel.getAll();
	}
	
	public File fileRead() {
		JFileChooser filechooser = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Position file(.variables)", "variables");
		filechooser.addChoosableFileFilter(filter);
		filechooser.setFileFilter(filter);
		
		int selected = filechooser.showOpenDialog(null);
		if(selected == JFileChooser.APPROVE_OPTION) {
			return filechooser.getSelectedFile();
		}else if(selected == JFileChooser.CANCEL_OPTION) {
			System.out.println("CANCEL");
		}else if(selected == JFileChooser.ERROR_OPTION) {
			System.out.println("ERROR");
		}
		return null;
	}
	
	public void SetValueFromFile(File file) {
		if(checkBeforeReadFile(file)) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((line = br.readLine()) != null) {
					Pattern p = Pattern.compile("=p");	//find the data of position
					Matcher m = p.matcher(line);
					if(m.find() != true) continue;
					String[] dataFromFile = line.split(",\\s*|\\s+|\\=p\\[|\\]");
					view.tableModel.addRowFromFile(dataFromFile);
				}
				br.close();
			}catch(FileNotFoundException err) {
				System.out.println(err);
			}catch(IOException err) {
				System.out.println(err);
			}
		}
	}
	
	public void fileWrite() {
		int option = JOptionPane.showConfirmDialog(null, "Overwrite the file : " + fileGlobal.getName() + "?",
				"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(option == JOptionPane.YES_OPTION) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(fileGlobal));
				Set<String> keySet = model.getKeys();
				ArrayList<String> keyList = new ArrayList<String>(keySet);
				Collections.sort(keyList);	//need or no?
				Iterator<String> keysI = keyList.iterator();
				
				while(keysI.hasNext()) {
					String keyName = keysI.next();
					if (keyName.startsWith("*")) { 	// joint positions
						JointPositions jPos = model.get(keyName, jPosDefaultValue);
						bw.write(keyName.substring(1) + "=" + jointPositionsToString(jPos));
						System.out.println(keyName.substring(1) + "=" + jPos.toString());
					} else { 					// pose
						Pose pose = model.get(keyName, PoseDefaultValue);
						bw.append(keyName + "=" + pose.toString());
					}
					bw.newLine();
				}
				bw.close();
			}catch(IOException e3) {
				return;
			}
			JOptionPane.showMessageDialog(null, "Succeeded", "Message", JOptionPane.INFORMATION_MESSAGE);
		}else if(option == JOptionPane.NO_OPTION) {
//			JOptionPane.showMessageDialog(null, "Failed", "Message", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
	}
	
	private boolean checkBeforeReadFile(File file){
		if(file.exists()) {
			if(file.isFile() && file.canRead()) {
				return true;
			}
		}
		return false;
	}
	
	private String jointPositionsToString(JointPositions jPos) {
		JointPosition[] jP = jPos.getAllJointPositions();
		return "["+jP[0].getPosition(Angle.Unit.RAD)+","+jP[1].getPosition(Angle.Unit.RAD)+","+jP[2].getPosition(Angle.Unit.RAD)+","+jP[3].getPosition(Angle.Unit.RAD)+","+jP[4].getPosition(Angle.Unit.RAD)+","+jP[5].getPosition(Angle.Unit.RAD)+"]";
	}

	@Override
	public void openView() {
		view.setPopupText(getPopupTitle());
	}

	@Override
	public void closeView() {

	}

	public boolean isDefined() {
		return !getPopupTitle().isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Store the popup title in a global variable so it is globally available to all Hello World Swing program nodes.
		writer.assign("teach_position_popup_title", "\"" + getPopupTitle() + "\"");
	}

	public String getPopupTitle() {
		return model.get(POPUPTITLE_KEY, DEFAULT_VALUE);
	}

	public void setPopupTitle(String message) {
		if ("".equals(message)) {
			resetToDefaultValue();
		} else {
			model.set(POPUPTITLE_KEY, message);
		}
	}

	private void resetToDefaultValue() {
		view.setPopupText(DEFAULT_VALUE);
		model.set(POPUPTITLE_KEY, DEFAULT_VALUE);
	}

	public KeyboardTextInput getInputForTextField() {
		KeyboardTextInput keyboardInput = keyboardFactory.createStringKeyboardInput();
		keyboardInput.setInitialValue(getPopupTitle());
		return keyboardInput;
	}

	public KeyboardInputCallback<String> getCallbackForTextField() {
		return new KeyboardInputCallback<String>() {
			@Override
			public void onOk(String value) {
				setPopupTitle(value);
				view.setPopupText(value);
			}
		};
	}
}
