package com.loccioni.teachposition.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.UserInterfaceAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.userinteraction.RobotPositionCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.jointposition.JointPositions;
import com.ur.urcap.api.domain.value.simple.Angle;
import com.ur.urcap.api.domain.value.simple.Length;

public class TeachPositionInstallationNodeContribution implements InstallationNodeContribution {

	private static final String POPUPTITLE_KEY = "popuptitle";
	private static final String DEFAULT_VALUE = "Teach Position";
	
	private final TeachPositionInstallationNodeView view;
	private final KeyboardInputFactory keyboardFactory;
	private DataModel model;
	private final InstallationAPIProvider apiProvider;
	private File file;
	private String line;
	
	public TeachPositionInstallationNodeContribution(InstallationAPIProvider apiProvider, DataModel model, 
			TeachPositionInstallationNodeView view) {
		this.keyboardFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		this.model = model;
		this.view = view;
		
		this.apiProvider = apiProvider;
	}
	
	public void SetPosition() {
		final int selectedRow = view.table.getSelectedRow();
		if(selectedRow  == -1) {
			JOptionPane.showMessageDialog(null, "You have to select one row", "Message", JOptionPane.WARNING_MESSAGE);
			return;
		}
		UserInterfaceAPI uiapi = apiProvider.getUserInterfaceAPI();
		uiapi.getUserInteraction().getUserDefinedRobotPosition(new RobotPositionCallback() {
			@Override
			public void onOk(Pose pose, JointPositions jointPositions) {
				model.set(view.tableModel.getValueAt(selectedRow, 0).toString(), pose);
				double[] data_double = pose.toArray(Length.Unit.M ,Angle.Unit.RAD);
				for(int i=1; i<view.tableModel.getColumnCount(); i++) {
					view.tableModel.setValueAt(data_double[i-1], selectedRow, i);
				}
			}
		});
	}
	
	public void onUrSelected(String RobotName) {
		System.out.println(RobotName);
	}
	
	public void testPrint() {
//		String data = new String();
//		Writer.getResolvedVariableName();
//		var.getDisplayName();
//		model.set(TEST, data);
//		varModel.getAll();
//		System.out.println(var.getDisplayName());
	}
	
	public void fileRead() {
		JFileChooser filechooser = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Position file(.variables)", "variables");
		filechooser.addChoosableFileFilter(filter);
		filechooser.setFileFilter(filter);
		
		int selected = filechooser.showOpenDialog(null);
		view.tableModel.deleteAll();
		if(selected == JFileChooser.APPROVE_OPTION) {
			file = filechooser.getSelectedFile();
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
	}
	
	public void fileWrite() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
		Date date = new Date();
		int index = file.getName().lastIndexOf('.');
		String filename = file.getName().substring(0,index) + "_"+ sdf.format(date) + "." + file.getName().substring(index+1);
		
		int option = JOptionPane.showConfirmDialog(null, "Create new file : " + filename + "?",
				"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(option == JOptionPane.YES_OPTION) {
			File newFile = new File(file.getParentFile() + "//" + filename);
			try {
				newFile.createNewFile();
			}catch(IOException e2) {
				return;
			}
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
				int columNum	= view.tableModel.getColumnCount();
				int RowNum		= view.tableModel.getRowCount();
				outside : while((line = br.readLine()) != null) {
					for(int i=0; i<RowNum; i++) {
						if(line.matches(".*" + view.tableModel.getValueAt(i, 0).toString() + "\\=p\\[.*")) {
							if(line.matches(".*global.*")) {
								bw.write("  global ");
							}
							bw.write(view.tableModel.getValueAt(i, 0).toString() + "=p[");
							for(int j=1; j<columNum - 1; j++) {
								bw.write(view.tableModel.getValueAt(i, j) + ", ");
							}
							bw.write(view.tableModel.getValueAt(i, columNum - 1) + "]");
							bw.newLine();
							continue outside;
						}
					}
					bw.write(line);
					bw.newLine();
				}
				bw.close();
				br.close();
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
