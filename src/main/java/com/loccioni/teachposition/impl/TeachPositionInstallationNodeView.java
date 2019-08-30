package com.loccioni.teachposition.impl;

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.*;
import javax.swing.JTable;
import javax.swing.JScrollPane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.text.SimpleDateFormat;

public class TeachPositionInstallationNodeView implements SwingInstallationNodeView<TeachPositionInstallationNodeContribution> {

	private final Style style;
	private JTextField jTextField = new JTextField();
	private String line;
	public TableModel tableModel = new TableModel();
	private JTable table = new JTable();
	private File file;
	
	public TeachPositionInstallationNodeView(Style style) {
		this.style = style;
	}
	//test for git

	@Override
	public void buildUI(JPanel jPanel, final TeachPositionInstallationNodeContribution installationNode) {
		JPanel panelButton = new JPanel();
		panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
		panelButton.add(JFileChooser());
		panelButton.add(createHorizontalSpacing());
		panelButton.add(JRegister(installationNode));
		panelButton.add(createHorizontalSpacing());
		panelButton.add(JFileWriter());
		panelButton.add(createHorizontalSpacing());
		panelButton.add(test(installationNode));
		
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
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
	
	private JButton JFileChooser() {
		JButton button = new JButton("Open");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				FileFilter filter = new FileNameExtensionFilter("Position file(.variables)", "variables");
				filechooser.addChoosableFileFilter(filter);
				filechooser.setFileFilter(filter);
				
				int selected = filechooser.showOpenDialog(null);
				tableModel.deleteAll();
				if(selected == JFileChooser.APPROVE_OPTION) {
					file = filechooser.getSelectedFile();
					if(checkBeforeReadFile(file) == false) {
						JOptionPane.showMessageDialog(null, "File is broken or can't be opend");
					}else {
						fileRead(file);
					}
				}
			}
		});
		return button;
	}
	
	private JButton JFileWriter() {
		JButton button = new JButton("Save");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
				Date date = new Date();
				int index = file.getName().lastIndexOf('.');
				String filename = file.getName().substring(0,index) + "_"+ sdf.format(date) + "." + file.getName().substring(index+1);
				
				int option = JOptionPane.showConfirmDialog(null, "Create new file : " + filename + "?",
						"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(option == JOptionPane.YES_OPTION) {
					fileWrite(filename);
					JOptionPane.showMessageDialog(null, "Succeeded", "Message", JOptionPane.INFORMATION_MESSAGE);
				}else if(option == JOptionPane.NO_OPTION) {
					JOptionPane.showMessageDialog(null, "Failed", "Message", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
		});
		return button;
	}
	
	public JButton JRegister(final TeachPositionInstallationNodeContribution installationNode) {
		JButton button = new JButton ("Register");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Add function to choose position from Robot
				int selectedRow = table.getSelectedRow();
				if(selectedRow == -1) {
					JOptionPane.showMessageDialog(null, "You have to select one row", "Message", JOptionPane.WARNING_MESSAGE);
					return;
				}
				installationNode.SetPosition(selectedRow);
				
//				for(int i=1; i<tableModel.getColumnCount(); i++) {
//					tableModel.setValueAt(i, selectedRow, i);
//				}
			}
		});
		return button;
	}
	
	private void fileWrite(String filename) {
		File newFile = new File(file.getParentFile() + "//" + filename);
		try {
			newFile.createNewFile();
		}catch(IOException e2) {
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
			int columNum	= tableModel.getColumnCount();
			int RowNum		= tableModel.getRowCount();
			outside : while((line = br.readLine()) != null) {
				for(int i=0; i<RowNum; i++) {
					if(line.matches(".*" + tableModel.getValueAt(i, 0).toString() + "\\=p\\[.*")) {
						if(line.matches(".*global.*")) {
							bw.write("  global ");
						}
						bw.write(tableModel.getValueAt(i, 0).toString() + "=p[");
						for(int j=1; j<columNum - 1; j++) {
							bw.write(tableModel.getValueAt(i, j) + ", ");
						}
						bw.write(tableModel.getValueAt(i, columNum - 1) + "]");
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
	}
	
	private void fileRead(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while((line = br.readLine()) != null) {
				Pattern p = Pattern.compile("=p");	//find the data of position
				Matcher m = p.matcher(line);
				if(m.find() != true) continue;
				String[] dataFromFile = line.split(",\\s*|\\s+|\\=p\\[|\\]");
				tableModel.addRowFromFile(dataFromFile);
			}
			br.close();
		}catch(FileNotFoundException err) {
			System.out.println(err);
		}catch(IOException err) {
			System.out.println(err);
		}
	}
	
	private static boolean checkBeforeReadFile(File file) {
		if(file.exists()) {		//file is exist or not
			if(file.isFile() && file.canRead()) return true;	//file is readable or not
		}
		return false;
	}
	
	private JScrollPane varTable() {
		table.setModel(tableModel);
		JScrollPane sp = new JScrollPane(table);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		return sp;
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
