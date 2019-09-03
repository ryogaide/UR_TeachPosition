package com.loccioni.teachposition.impl;

import java.util.ArrayList;
import java.math.BigDecimal;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.math.NumberUtils;

public class TableModel extends AbstractTableModel{
	private final JTable table;
	
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	private String[] columns = {"Names", "X[m]", "Y[m]", "Z[m]", "RX[rad]", "RY[rad]", "RZ[rad]"};
	
	public TableModel(JTable table) {
		this.table = table;
	}
	
	@Override
	public String getColumnName(int column) {
		return columns[column];	  
	}

	@Override
	public int getRowCount() {
		return data.size();
	}
	
	@Override
	public int getColumnCount() {
		return columns.length;
	}
	
	public void addRowFromFile(String[] dataFromFile) {
		Object name = new Object();
		Object[] dataPosition = new Object[6];
		for(int i=0; i<dataFromFile.length; i++) {
			if(NumberUtils.isNumber(dataFromFile[i]) == true) {
				name = dataFromFile[i-1];
				for(int j=0; j<6; j++) {
					dataPosition[j] = dataFromFile[i];
					i++;
				}
				break;
			}
		}
		data.add(new Object[] {name,dataPosition[0], dataPosition[1],
				dataPosition[2],dataPosition[3],dataPosition[4],dataPosition[5],});
		fireTableRowsInserted(0, data.size()-1);
	}
	
	public void addRow(String name, Double[] val) {
		for(int i=0; i<6; i++) {
			val[i] = BigDecimal.valueOf(val[i]).setScale(4, BigDecimal.ROUND_DOWN).doubleValue();
		}
		data.add(new Object[] {name,val[0], val[1], val[2],val[3],val[4],val[5]});
		fireTableRowsInserted(0, data.size()-1);
	}
	
	public void setValueAt(Double val, int rowIndex, int columnIndex) {
		data.get(rowIndex)[columnIndex] = BigDecimal.valueOf(val).setScale(4, BigDecimal.ROUND_DOWN).doubleValue();
		fireTableCellUpdated(rowIndex, columnIndex);
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex)[columnIndex];
	}
	
	public void deleteAll() {
		data.clear();
	}
}