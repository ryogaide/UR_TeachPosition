package com.loccioni.teachposition.impl;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

public class TeachPositionInstallationNodeView implements SwingInstallationNodeView<TeachPositionInstallationNodeContribution> {

	private final Style style;
	private JTextField jTextField = new JTextField();
	public TableModel tableModel = new TableModel();
	public JTable table = new JTable();
	
	public TeachPositionInstallationNodeView(Style style) {
		this.style = style;
	}

	@Override
	public void buildUI(JPanel jPanel, final TeachPositionInstallationNodeContribution installationNode) {
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
		
		JPanel panelButton = new JPanel();
		panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
		
		JPanel panelSelect = new JPanel();
		ButtonGroup group = new ButtonGroup();
		JRadioButton UrA = new JRadioButton("UR A (ext)");
		JRadioButton UrB = new JRadioButton("UR B (int)");
		group.add(UrA);
		group.add(UrB);
		UrA.setName("UrA");
		UrB.setName("UrB");
		UrA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				installationNode.onUrSelected(button.getName());
			}
		});
		UrB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				installationNode.onUrSelected(button.getName());
			}
		});
		
		UrB.addActionListener(new RobotSelectListener());
		panelSelect.add(UrA);
		panelSelect.add(UrB);
		panelSelect.setLayout(new BoxLayout(panelSelect, BoxLayout.Y_AXIS));
		panelButton.add(panelSelect);
		
		panelButton.add(createHorizontalSpacing());
		panelButton.add(JFileChooser(installationNode));
		panelButton.add(createHorizontalSpacing());
		panelButton.add(JRegister(installationNode));
		panelButton.add(createHorizontalSpacing());
		panelButton.add(JFileWriter(installationNode));
		panelButton.add(createHorizontalSpacing());
		panelButton.add(test(installationNode));
		panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
		jPanel.add(panelButton);
		
		jPanel.add(createVerticalSpacing());
		jPanel.add(varTable());
	}
	
	public JButton test(final TeachPositionInstallationNodeContribution installationNode) {
		JButton button = new JButton("test");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				installationNode.testPrint();
			}
		});
		return button;
	}
	
	class RobotSelectListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton button = (JRadioButton) e.getSource();
			
		}
	}
	
	private JButton JFileChooser(final TeachPositionInstallationNodeContribution installationNode) {
		JButton button = new JButton("Open");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				installationNode.fileRead();
			}
		});
		return button;
	}
	
	private JButton JFileWriter(final TeachPositionInstallationNodeContribution installationNode) {
		JButton button = new JButton("Save");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				installationNode.fileWrite();
			}
		});
		return button;
	}
	
	public JButton JRegister(final TeachPositionInstallationNodeContribution installationNode) {
		JButton button = new JButton ("Register");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				installationNode.SetPosition();
			}
		});
		return button;
	}
	
	private JScrollPane varTable() {
		table.setModel(tableModel);
		JScrollPane sp = new JScrollPane(table);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		return sp;
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
	
	private Component createHorizontalSpacing() {
		return Box.createRigidArea(new Dimension(style.getHorizontalSpacing(), 0));
	}

	private Component createVerticalSpacing() {
		return Box.createRigidArea(new Dimension(0, style.getVerticalSpacing()));
	}

	public void setPopupText(String t) {
		jTextField.setText(t);
	}
}
