package com.loccioni.teachposition.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.feature.Feature;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.userinteraction.RobotPositionCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.userinteraction.robot.movement.MovementCompleteEvent;
import com.ur.urcap.api.domain.userinteraction.robot.movement.RobotMovementCallback;
import com.ur.urcap.api.domain.util.Filter;
import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.PoseFactory;
import com.ur.urcap.api.domain.value.jointposition.JointPosition;
import com.ur.urcap.api.domain.value.jointposition.JointPositionFactory;
import com.ur.urcap.api.domain.value.jointposition.JointPositions;
import com.ur.urcap.api.domain.value.simple.Angle;
import com.ur.urcap.api.domain.value.simple.Length;


public class TeachPositionInstallationNodeContribution implements InstallationNodeContribution {

	private static final String POPUPTITLE_KEY = "popuptitle";
	private static final String DEFAULT_VALUE = "Teach Position";
	private String selectedRobot = "";	//To save which Robot we chose
	
	public final String RobotA = "A";
	public final String RobotB = "B";
	public static final String RefValueA = "A_REF_VAR";
	public static final String RefValueB = "A_REF_VAR";
	
	private final TeachPositionInstallationNodeView view;
	private final KeyboardInputFactory keyboardFactory;
	private DataModel model;
	private final InstallationAPIProvider apiProvider;
	private String line;
	private final PoseFactory poseFactory;
	private final JointPositionFactory jPosFactory;
	private final Pose PoseDefaultValue;
	private final JointPositions jPosDefaultValue;
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
		
	}
	
	java.net.URL fileURL = getClass().getResource("code.txt");
	
	public void SetPosition() {
//		final int selectedRow = view.table.getSelectedRow();
		final int selectedRow = view.table.convertRowIndexToModel(view.table.getSelectedRow());
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
		final int selectedRow = view.table.convertRowIndexToModel(view.table.getSelectedRow());
//		final int selectedRow = view.table.getSelectedRow();
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
	
	public void onUrSelected(JRadioButton radio) {
		selectedRobot = radio.getName();
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
				System.out.println("debug point 1");
//				view.table.removeAll();
//				view.tableModel.deleteAll();
				System.out.println("debug point 2");
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
						System.out.println("debug point 3");
						view.tableModel.addRow(name, valueDouble);
						System.out.println("debug point 4");
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
//						System.out.println(keyName.substring(1) + "=" + jPos.toString());
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
	
	public void onRefButtonPressed() {
//		UrPose pose = new UrPose(new double[] {0.2, 0.5, 0.1, 1.57, 0, 3.14});
//		UrPose poseInv = new UrPose(UrPose.pose_inv(pose.toDoubleArray()));
//		System.out.println("pose = " + pose.toString());
//		System.out.println("poseInv = " + poseInv.toString());
//		UrPose pose1 = new UrPose(new double[] {0.2, 0.5, 0.1, 1.57, 0, 0});
//		UrPose pose2 = new UrPose(new double[] {0.2, 0.5, 0.6, 1.57, 0, 0});
//		UrPose poseTrans = new UrPose(UrPose.pose_trans(pose1.toDoubleArray(), pose2.toDoubleArray()));
//		System.out.println("pose1 = " + pose1.toString());
//		System.out.println("pose2 = " + pose2.toString());
//		System.out.println("poseTrans = " + poseTrans.toString());
		
		//System.out.println("REF button pressed!");
		
		Collection<Feature> features =  apiProvider.getInstallationAPI().getFeatureModel().getGeomFeatures(new Filter<Feature>() {
			
			@Override
			public boolean accept(Feature element) {
				if (selectedRobot == RobotA) {
					return element.getName().contentEquals("A_REF");
				}
				if (selectedRobot == RobotB) {
					return element.getName().contentEquals("B_REF");
				}
				return false;
			}
		});
		Pose nullPose = PoseDefaultValue;
		Pose newRef = nullPose;
		if (features.size() == 1) {
			newRef = features.iterator().next().getPose();
		}
		Pose oldRef = nullPose;
		if (selectedRobot == RobotA) {
			oldRef = model.get(RefValueA, PoseDefaultValue);
		}
		if (selectedRobot == RobotB) {
			oldRef = model.get(RefValueB, PoseDefaultValue);
		}
		if (oldRef.epsilonEquals(nullPose, 0.01, Length.Unit.MM, 0.01, Angle.Unit.DEG)) {
			System.out.println("OLD reference pose NOT defined!");
			return;
		} else {
			System.out.println("OLD REF = " + oldRef.toString());
		}
		if (newRef.epsilonEquals(nullPose, 0.01, Length.Unit.MM, 0.01, Angle.Unit.DEG)) {
			System.out.println("NEW reference pose NOT defined!");
			return;
		} else {
			System.out.println("NEW REF = " + newRef.toString());
		}
		if (newRef.epsilonEquals(oldRef, 0.01, Length.Unit.MM, 0.01, Angle.Unit.DEG)) {
			System.out.println("Same reference: no need to update!");
			return;
		}
		System.out.println("Reference was changed!");
		Set<String> keySet = model.getKeys();
		ArrayList<String> keyList = new ArrayList<String>(keySet);
		Collections.sort(keyList);
		Iterator<String> keysI = keyList.iterator();
		while (keysI.hasNext()) {
			String key = keysI.next();
			if (!key.startsWith("*")) { 	// pose
				double[] oldPose = model.get(key, PoseDefaultValue).toArray(Length.Unit.M, Angle.Unit.RAD);
				double[] newPose = UrPose.pose_trans(newRef.toArray(Length.Unit.M, Angle.Unit.RAD), UrPose.pose_trans(UrPose.pose_inv(oldRef.toArray(Length.Unit.M, Angle.Unit.RAD)), oldPose));
				model.set(key, poseFactory.createPose(newPose[0], newPose[1], newPose[2], newPose[3], newPose[4], newPose[5], Length.Unit.M, Angle.Unit.RAD));
			}
		}
		double[] ref = newRef.toArray(Length.Unit.M, Angle.Unit.RAD);
		if (selectedRobot.contentEquals("A")) {
			model.set(RefValueA, poseFactory.createPose(ref[0], ref[1], ref[2], ref[3], ref[4], ref[5], Length.Unit.M, Angle.Unit.RAD));
		}
		if (selectedRobot.contentEquals("B")) {
			model.set(RefValueB, poseFactory.createPose(ref[0], ref[1], ref[2], ref[3], ref[4], ref[5], Length.Unit.M, Angle.Unit.RAD));
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
