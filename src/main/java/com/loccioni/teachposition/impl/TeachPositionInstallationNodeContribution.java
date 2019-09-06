package com.loccioni.teachposition.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
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
	private String selectedRobot = null;	//To save which Robot we chose
	
	public final String RobotA = "A";
	public final String RobotB = "B";
	public static final String RefValueA = "A_REF_VAR";
	public static final String RefValueB = "B_REF_VAR";
//	private static final String PositionFileA = "/programs/NAG3M_UrA.positions";
//	private static final String PositionFileB = "/programs/NAG3M_UrB.positions";
	private static final String FilePath = "/programs/";
	
	private final TeachPositionInstallationNodeView view;
	private final KeyboardInputFactory keyboardFactory;
	private DataModel model;
	private final InstallationAPIProvider apiProvider;
	private String line;
	private final PoseFactory poseFactory;
	private final JointPositionFactory jPosFactory;
	private final Pose PoseDefaultValue;
	private final JointPositions jPosDefaultValue;
	private File fileGlobal = null;
	DecimalFormat df = new DecimalFormat("0.0000");
	
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
	
	public void SetPosition() {
		if(view.table.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(null, "You have to select one row", "Message", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final int selectedRow = view.table.convertRowIndexToModel(view.table.getSelectedRow());
		final String nameKey = view.DefTableModel.getValueAt(selectedRow, 0).toString();
		apiProvider.getUserInterfaceAPI().getUserInteraction().getUserDefinedRobotPosition(new RobotPositionCallback() {
			@Override
			public void onOk(Pose pose, JointPositions jointPositions) {
				if(model.isSet(nameKey) == true) {
					model.set(nameKey, pose);
				}else if(model.isSet(nameKey) == false) {
					model.set("*" + nameKey, jointPositions);
				}
				double[] data_double = pose.toArray(Length.Unit.M ,Angle.Unit.RAD);
				for(int i=1; i<view.DefTableModel.getColumnCount(); i++) {
					view.DefTableModel.setValueAt(df.format(data_double[i-1]), selectedRow, i);
				}
			}
		});
	}
	
	public void onGotoButtonPressed() {
		if(view.table.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(null, "You have to select one row", "Message", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final int selectedRow = view.table.convertRowIndexToModel(view.table.getSelectedRow());
		String nameKey = view.DefTableModel.getValueAt(selectedRow, 0).toString();
		if(model.isSet(nameKey) == true) {
			Pose pose = model.get(nameKey, PoseDefaultValue);
			apiProvider.getUserInterfaceAPI().getUserInteraction().getRobotMovement().requestUserToMoveRobot(pose, new RobotMovementCallback() {
				@Override
				public void onComplete(MovementCompleteEvent event) {
				}
			});
			
		}else if(model.isSet(nameKey) == false){
			JointPositions jPos = model.get("*" + nameKey, jPosDefaultValue);
			apiProvider.getUserInterfaceAPI().getUserInteraction().getRobotMovement().requestUserToMoveRobot(jPos, new RobotMovementCallback() {
				@Override
				public void onComplete(MovementCompleteEvent event) {
				}
			});
		}
	}
	
	public void onUrSelectedAuto() {
		fileGlobal = fileSearch();

		if(checkBeforeReadFile(fileGlobal) != true){
			JOptionPane.showMessageDialog(null, "Uncorrect file name or path", "Message", JOptionPane.INFORMATION_MESSAGE);
		};
		try {
			removeAllKeys();
			view.DefTableModel.setRowCount(0);
			BufferedReader br = new BufferedReader(new FileReader(fileGlobal));
			while((line = br.readLine()) != null) {
				if(line.contains("p[")) {
					String[] temp = line.split("=");
					String name = temp[0];
					String valueString = temp[1].substring(2, temp[1].length() - 1);
					temp = valueString.split(",");
					double[] val = new double[6];
					for(int i=0; i<6; i++) {
						val[i] = Double.valueOf(temp[i].trim());
					}
					model.set(name, poseFactory.createPose(val[0], val[1], val[2], val[3], val[4], val[5], Length.Unit.M, Angle.Unit.RAD));
					Data2Table(name, val);
				}else if(line.contains("[")) {
					String[] temp = line.split("=");
			    	String name = temp[0];
			    	String valueString = temp[1].substring(1, temp[1].length()-1);
			    	temp = valueString.split(",");
			    	double[] val = new double[6];
			    	for (int i=0; i<6; i++) {
			    		val[i] = Double.valueOf(temp[i].trim());
			    	}
			    	model.set("*"+name, jPosFactory.createJointPositions(val[0], val[1],val[2], val[3], val[4], val[5], Angle.Unit.RAD));
			    	Data2Table(name, val);
				}
			}
			br.close();
		} catch (FileNotFoundException err) {
			System.out.println(err);
		} catch (IOException err) {
			System.out.println(err);
		}
	}
	
	public void onUrSelected(JRadioButton radio) {

//		JFileChooser filechooser = new JFileChooser();
//		FileFilter filter = new FileNameExtensionFilter("Position file(.variables)", "variables");
//		filechooser.addChoosableFileFilter(filter);
//		filechooser.setFileFilter(filter);
//		
//		int selected = filechooser.showOpenDialog(null);
////		if(selected == JFileChooser.APPROVE_OPTION) {
//		File fileDebug =  filechooser.getSelectedFile();
//		System.out.println("name: " + fileDebug.getName());
//		System.out.println("path: " + fileDebug.getPath());
//		selectedRobot = radio.getName();
//		if(selectedRobot == RobotA) {
//			fileGlobal = new File(PositionFileA);
//		}else if(selectedRobot == RobotB) {
//			fileGlobal = new File(PositionFileB);
//		}else {
//			JOptionPane.showMessageDialog(null,"You have to select robot" , "Message",JOptionPane.INFORMATION_MESSAGE);
//		}
		if(checkBeforeReadFile(fileGlobal) != true){
			JOptionPane.showMessageDialog(null, "Uncorrect file name or path", "Message", JOptionPane.INFORMATION_MESSAGE);
		};
		try {
			removeAllKeys();
			view.DefTableModel.setRowCount(0);
			BufferedReader br = new BufferedReader(new FileReader(fileGlobal));
			while((line = br.readLine()) != null) {
				if(line.contains("p[")) {
					String[] temp = line.split("=");
					String name = temp[0];
					String valueString = temp[1].substring(2, temp[1].length() - 1);
					temp = valueString.split(",");
					double[] val = new double[6];
					for(int i=0; i<6; i++) {
						val[i] = Double.valueOf(temp[i].trim());
					}
					model.set(name, poseFactory.createPose(val[0], val[1], val[2], val[3], val[4], val[5], Length.Unit.M, Angle.Unit.RAD));
					Data2Table(name, val);
				}else if(line.contains("[")) {
					String[] temp = line.split("=");
			    	String name = temp[0];
			    	String valueString = temp[1].substring(1, temp[1].length()-1);
			    	temp = valueString.split(",");
			    	double[] val = new double[6];
			    	for (int i=0; i<6; i++) {
			    		val[i] = Double.valueOf(temp[i].trim());
			    	}
			    	model.set("*"+name, jPosFactory.createJointPositions(val[0], val[1],val[2], val[3], val[4], val[5], Angle.Unit.RAD));
			    	Data2Table(name, val);
				}
			}
			br.close();
		} catch (FileNotFoundException err) {
			System.out.println(err);
		} catch (IOException err) {
			System.out.println(err);
		}
//		}else if(selected == JFileChooser.CANCEL_OPTION) {
//			System.out.println("CANCEL");
//		}else if(selected == JFileChooser.ERROR_OPTION) {
//			System.out.println("ERROR");
//		}
	}
	
	private void removeAllKeys() {
		Set<String> keySet = model.getKeys();
		Iterator<String> keysI = keySet.iterator();
		while (keysI.hasNext()) {
			model.remove(keysI.next());
		}
	}
	
	public void testPrint() {
		fileGlobal = fileSearch();
		System.out.println(fileGlobal);
	}
	
	public File fileChoose() {
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
	
	public File fileSearch() {
		File dir = new File(FilePath);
		final String extension = ".positions";
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File file, String name) {
				int index = name.lastIndexOf(".");
				String ext = name.substring(index).toLowerCase();
				if(ext.equals(extension)) {
					return true;
				}else {
					return false;
				}
			}
		};
		File[] files = dir.listFiles(filter);
		if(files.length == 0) {
			JOptionPane.showMessageDialog(null, "There is no .positions file", "Message", JOptionPane.INFORMATION_MESSAGE);
		}else if(files.length == 1) {
			return files[0];
		}else {
			JOptionPane.showMessageDialog(null, "There are more than 1 .positions file", "Message", JOptionPane.INFORMATION_MESSAGE);
		}
		return null;
	}
	
	public void fileWrite() {
//		int option = JOptionPane.showConfirmDialog(null, "Overwrite the file : " + fileGlobal.getName() + "?",
//				"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//		if(option == JOptionPane.YES_OPTION) {
		if(fileGlobal == null || selectedRobot == null) {
			return;
		}
		
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
//			JOptionPane.showMessageDialog(null, "Succeeded", "Message", JOptionPane.INFORMATION_MESSAGE);
//		}else if(option == JOptionPane.NO_OPTION) {
////			JOptionPane.showMessageDialog(null, "Failed", "Message", JOptionPane.INFORMATION_MESSAGE);
//			return;
//		}
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
		
		int option = JOptionPane.showConfirmDialog(null, "New Referece is set\n change all positions?",
				"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(option == JOptionPane.YES_OPTION) {
			view.DefTableModel.setRowCount(0);	
			System.out.println("Reference was changed!");
			Set<String> keySet = model.getKeys();
			ArrayList<String> keyList = new ArrayList<String>(keySet);
			Collections.sort(keyList);
			Iterator<String> keysI = keyList.iterator();
			while (keysI.hasNext()) {
				String key = keysI.next();
				if (!key.startsWith("*")) { 	// pose
					double[] oldPose = model.get(key, PoseDefaultValue).toArray(Length.Unit.M, Angle.Unit.RAD);
					double[] newPose = UrPose.pose_trans(newRef.toArray(Length.Unit.M, Angle.Unit.RAD), 
							UrPose.pose_trans(UrPose.pose_inv(oldRef.toArray(Length.Unit.M, Angle.Unit.RAD)), oldPose));
					model.set(key, poseFactory.createPose(newPose[0], newPose[1], newPose[2], newPose[3], newPose[4], newPose[5], Length.Unit.M, Angle.Unit.RAD));
					Data2Table(key, newPose);
				}else {
					System.out.println(key);
					JointPositions jPos = model.get(key, jPosDefaultValue);
					double[] jPosDouble = jPos2Double(jPos);
					Data2Table(key.substring(1), jPosDouble);
 				}
			}
		}else {
			return;
		}
	}
	
	private double[] jPos2Double(JointPositions jPos) {
		double[] val = new double[6];
		JointPosition[] jP = jPos.getAllJointPositions();
		for(int i=0; i<6; i++) {
			val[i] = jP[i].getPosition(Angle.Unit.RAD);
		}
		return val;
	}
	
	private void Data2Table(String name, double val[]) {
		view.DefTableModel.addRow(new Object[] {name,df.format(val[0]), df.format(val[1]), df.format(val[2]), 
				df.format(val[3]), df.format(val[4]), df.format(val[5])});
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
		return "["+jP[0].getPosition(Angle.Unit.RAD)+","+jP[1].getPosition(Angle.Unit.RAD)+","+jP[2].getPosition(Angle.Unit.RAD)+","+
				jP[3].getPosition(Angle.Unit.RAD)+","+jP[4].getPosition(Angle.Unit.RAD)+","+jP[5].getPosition(Angle.Unit.RAD)+"]";
	}
	
	public String getFilename() {
		if(fileGlobal == null) {
			return null;
		}else {
			return fileGlobal.getName();
		}
	}

	@Override
	public void openView() {
		view.setPopupText(getPopupTitle());
		onUrSelectedAuto();
	}

	@Override
	public void closeView() {
		fileWrite();
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
