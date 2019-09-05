package com.loccioni.teachposition.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
//import com.ur.urcap.api.domain.program.nodes.ProgramNodeFactory;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.PoseFactory;
import com.ur.urcap.api.domain.value.jointposition.JointPosition;
import com.ur.urcap.api.domain.value.jointposition.JointPositionFactory;
import com.ur.urcap.api.domain.value.jointposition.JointPositions;
import com.ur.urcap.api.domain.value.simple.Angle;
import com.ur.urcap.api.domain.value.simple.Length;

public class TeachPositionProgramNodeContribution implements ProgramNodeContribution{

//	private final ProgramAPIProvider apiProvider;
	private final TeachPositionProgramNodeView view;
	private final DataModel model;
	private final UndoRedoManager undoRedoManager;
//	private final ProgramNodeFactory progFactory;
	private final PoseFactory poseFactory;
	private final JointPositionFactory jPosFactory;
	
	private HashMap<String, Pose> poseMap = new HashMap<String, Pose>();
	private HashMap<String, JointPositions> jointPosMap = new HashMap<String, JointPositions>();
	
	private static final String ROBOT_KEY = "UR";
	private static final String ROBOT_A = "UR A";
	private static final String ROBOT_B = "UR B";
	
	public TeachPositionProgramNodeContribution(ProgramAPIProvider apiProvider, TeachPositionProgramNodeView view, DataModel model) {
//		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.undoRedoManager = apiProvider.getProgramAPI().getUndoRedoManager();
//		this.progFactory = apiProvider.getProgramAPI().getProgramModel().getProgramNodeFactory();
		this.poseFactory = apiProvider.getProgramAPI().getValueFactoryProvider().getPoseFactory();
		this.jPosFactory = apiProvider.getProgramAPI().getValueFactoryProvider().getJointPositionFactory();
	}
	
	public void onUrASelected() {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(ROBOT_KEY, ROBOT_A);				
			}
		});
	}
	
	public void onUrBSelected() {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(ROBOT_KEY, ROBOT_B);				
			}
		});
	}
	
	@Override
	public void openView() {
		System.out.println("*** OPEN VIEW ***");
		String robot = model.get(ROBOT_KEY, "");
		if (robot.contentEquals(ROBOT_A)) {
			System.out.println("*** UR A ***");
			view.urASelected();
		}
		if (robot.contentEquals(ROBOT_B)) {
			System.out.println("*** UR B ***");
			view.urBSelected();
		}
	}

	@Override
	public void closeView() {

	}

	@Override
	public String getTitle() {
		return "Teach Positions " + model.get(ROBOT_KEY, "");
	}

	@Override
	public boolean isDefined() {
		return model.isSet(ROBOT_KEY);
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		System.out.println("*** GENERATING SCRIPT ***");
		readPositionsFile();
		
		Set<String> pKeys = poseMap.keySet();
		Iterator<String> pKeysI = pKeys.iterator();
		while (pKeysI.hasNext()) {
			String key = pKeysI.next();
			Pose pose = poseMap.get(key);
			writer.assign(key, pose.toString());
		}
		
		Set<String> jKeys = jointPosMap.keySet();
		Iterator<String> jKeysI = jKeys.iterator();
		while (jKeysI.hasNext()) {
			String key = jKeysI.next();
			JointPositions jPos = jointPosMap.get(key);
			writer.assign(key, jointPositionsToString(jPos));
		}
	}
	
	private String jointPositionsToString(JointPositions jPos) {
		JointPosition[] jP = jPos.getAllJointPositions();
		return "["+jP[0].getPosition(Angle.Unit.RAD)+","+jP[1].getPosition(Angle.Unit.RAD)+","+jP[2].getPosition(Angle.Unit.RAD)+","+jP[3].getPosition(Angle.Unit.RAD)+","+jP[4].getPosition(Angle.Unit.RAD)+","+jP[5].getPosition(Angle.Unit.RAD)+"]";
	}
	
	private String getFileName() {
		String robot = "";
		if (model.isSet(ROBOT_KEY)) robot = model.get(ROBOT_KEY, "");
		if (robot.contentEquals(ROBOT_A)) return "/programs/NAG3M_UrA.positions";
		if (robot.contentEquals(ROBOT_B)) return "/programs/NAG3M_UrB.positions";
		return "";
	}
	
	private void readPositionsFile() {
		//System.out.println("*** READING POSITIONS FILE ***");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(getFileName())));
			while (true) {
			    String line = reader.readLine();
			    if (line == null) break;
			    if (line.contains("p[")) {
			    	String[] temp = line.split("=");
			    	String name = temp[0];
			    	String valueString = temp[1].substring(2, temp[1].length()-1);
			    	temp = valueString.split(",");
			    	Double[] value = new Double[6];
			    	for (int i=0; i<temp.length; i++) {
			    		value[i] = Double.valueOf(temp[i].trim());
			    	}
			    	poseMap.put(name, poseFactory.createPose(value[0], value[1], value[2], value[3], value[4], value[5], Length.Unit.M, Angle.Unit.RAD));
			    } else {
			    	if (line.contains("[")) {
			    		String[] temp = line.split("=");
				    	String name = temp[0];
				    	String valueString = temp[1].substring(1, temp[1].length()-1);
				    	temp = valueString.split(",");
				    	Double[] value = new Double[6];
				    	for (int i=0; i<temp.length; i++) {
				    		value[i] = Double.valueOf(temp[i].trim());
				    	}
				    	jointPosMap.put(name, jPosFactory.createJointPositions(value[0], value[1], value[2], value[3], value[4], value[5], Angle.Unit.RAD));
			    	}
			    }
			    	
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
