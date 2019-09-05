package com.loccioni.teachposition.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;

public class TeachPositionProgramNodeView implements SwingProgramNodeView<TeachPositionProgramNodeContribution>{

//	private final ViewAPIProvider apiProvider;
	
	public TeachPositionProgramNodeView(ViewAPIProvider apiProvider) {
//		this.apiProvider = apiProvider;
	}
	
	private final ButtonGroup group = new ButtonGroup();
	private final JRadioButton UrA = new JRadioButton("UR A (ext)");
	private final JRadioButton UrB = new JRadioButton("UR B (int)");

	@Override
	public void buildUI(JPanel panel, final ContributionProvider<TeachPositionProgramNodeContribution> provider) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		group.add(UrA);
		group.add(UrB);
		
		panel.add(createDescription("Select the robot DEV"));
		panel.add(createSpacer(0, 20));
		
		UrA.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				provider.get().onUrASelected();
			}
		});	
		panel.add(UrA);
		
		UrB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				provider.get().onUrBSelected();
			}
		});
		panel.add(UrB);
	}

	private Box createDescription(String desc) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel label = new JLabel(desc);
		
		box.add(label);
		
		return box;
	}
	
	private Component createSpacer(int width, int height) {
		return Box.createRigidArea(new Dimension(width, height));
	}
	
	public void urASelected() {
//		System.out.println("UR A selected");
		UrA.setSelected(true);
	}
	
	public void urBSelected() {
//		System.out.println("UR B selected");
		UrB.setSelected(true);
	}
}
