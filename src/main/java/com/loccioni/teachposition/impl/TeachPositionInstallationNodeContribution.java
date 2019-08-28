package com.loccioni.teachposition.impl;

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
import com.ur.urcap.api.domain.script.ScriptWriter;

import com.ur.urcap.api.domain.variable.VariableModel;
import com.ur.urcap.api.domain.variable.Variable;

public class TeachPositionInstallationNodeContribution implements InstallationNodeContribution {

	private static final String POPUPTITLE_KEY = "popuptitle";
	private static final String DEFAULT_VALUE = "Teach Position";
	private static final String TEACH_KEY = "Set position";
	private static final String TEST = "test1";
	
	private final TeachPositionInstallationNodeView view;
	private final KeyboardInputFactory keyboardFactory;
	private DataModel model;
	private final InstallationAPIProvider apiProvider;
	
	//debug
	private VariableModel varModel;
	private ScriptWriter Writer;
	private Variable var;

	public TeachPositionInstallationNodeContribution(InstallationAPIProvider apiProvider, DataModel model, 
			TeachPositionInstallationNodeView view) {
		this.keyboardFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		this.model = model;
		this.view = view;
		
		this.apiProvider = apiProvider;
	}
	
	public void SetPosition(final int selectedRow) {
		UserInterfaceAPI uiapi = apiProvider.getUserInterfaceAPI();
		uiapi.getUserInteraction().getUserDefinedRobotPosition(new RobotPositionCallback() {
			@Override
			public void onOk(Pose pose, JointPositions jointPositions) {
				model.set(TEACH_KEY, pose);
				double[] data_double = pose.toArray(Length.Unit.M ,Angle.Unit.RAD);
				for(int i=1; i<view.tableModel.getColumnCount(); i++) {
					view.tableModel.setValueAt(data_double[i-1], selectedRow, i);
				}
			}
		});
	}
	
	public void testPrint() {
//		String data = new String();
//		Writer.getResolvedVariableName();
//		var.getDisplayName();
//		model.set(TEST, data);
//		varModel.getAll();
		System.out.println(var.getDisplayName());
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
