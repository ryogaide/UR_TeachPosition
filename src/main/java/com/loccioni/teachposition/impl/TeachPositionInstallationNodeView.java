package com.loccioni.teachposition.impl;

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.Arrays;

public class TeachPositionInstallationNodeView implements SwingInstallationNodeView<TeachPositionInstallationNodeContribution> {

	private final Style style;
	private JTextField jTextField = new JTextField();
	private final String[] columns = {"Names", "X[m]", "Y[m]", "Z[m]", "RX[rad]", "RY[rad]", "RZ[rad]"};
	public DefaultTableModel DefTableModel = new DefaultTableModel(columns, 0);
	
	
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
		UrA.setName(installationNode.RobotA);
		UrB.setName(installationNode.RobotB);
		UrA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton radio = (JRadioButton) e.getSource();
				installationNode.onUrSelected(radio);
			}
		});
		UrB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton radio = (JRadioButton) e.getSource();
				installationNode.onUrSelected(radio);
			}
		});
		
		panelSelect.add(UrA);
		panelSelect.add(UrB);
		panelSelect.setLayout(new BoxLayout(panelSelect, BoxLayout.Y_AXIS));
		panelButton.add(panelSelect);
		
		panelButton.add(createHorizontalSpacing(5));
		panelButton.add(JButtonGoTo(installationNode));
		panelButton.add(createHorizontalSpacing(5));
		panelButton.add(JButtonSet(installationNode));
		panelButton.add(createHorizontalSpacing(5));
//		panelButton.add(JFileWriter(installationNode));
//		panelButton.add(createHorizontalSpacing(3));
		panelButton.add(JButtonRef(installationNode));
		panelButton.add(createHorizontalSpacing(5));
//		panelButton.add(test(installationNode));
		panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
		jPanel.add(panelButton);
		
		jPanel.add(createVerticalSpacing(2));
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
	
	private JButton JButtonGoTo(final TeachPositionInstallationNodeContribution installationNode) {
		JButton button = new JButton("Go To");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				installationNode.onGotoButtonPressed();
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
	
	public JButton JButtonSet(final TeachPositionInstallationNodeContribution installationNode) {
		JButton button = new JButton ("Set");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				installationNode.SetPosition();
			}
		});
		return button;
	}
	
	public JButton JButtonRef(final TeachPositionInstallationNodeContribution installationNode) {
		JButton button = new JButton ("REF");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				installationNode.onRefButtonPressed();
			}
		});
		return button;
	}
	
	private JScrollPane varTable() {
		JScrollPane sp = new JScrollPane(table);
		sp.getPreferredSize();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setModel(DefTableModel);
		table.setAutoCreateRowSorter(true);	//To make sorter automatically
		table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
		table.setDefaultEditor(Object.class, null);
		
		/*
		 * To fix sort
		 */
//		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
//		table.setRowSorter(sorter);
//		for(int i=1; i<table.getColumnCount(); i++) {
//			sorter.setSortable(i, false);
//		}
		DefaultTableColumnModel columnModel = (DefaultTableColumnModel)table.getColumnModel();
		TableColumn column = columnModel.getColumn(0);
		column.setPreferredWidth(150);
		return sp;
	}
	
	private Component createHorizontalSpacing(int multiplier) {
		return Box.createRigidArea(new Dimension(style.getHorizontalSpacing() * multiplier, 0));
	}

	private Component createVerticalSpacing(int multiplier) {
		return Box.createRigidArea(new Dimension(0, style.getVerticalSpacing() * multiplier));
	}
	
	public void setPopupText(String t) {
		jTextField.setText(t);
	}
}
