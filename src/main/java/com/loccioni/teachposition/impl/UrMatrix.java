package com.loccioni.popup.impl;

import java.util.Arrays;

public class UrMatrix {
	
	double[][] matrix;
	
	public UrMatrix(double[][] data) {
		matrix = data;
	}
	
	public void setData(double[][] data) {
		matrix = data;
	}
	
	public double[][] getData() {
		return matrix;
	}

	public double determinant() {
		if (matrix.length != matrix[0].length)
			throw new IllegalStateException("invalid dimensions");

		if (matrix.length == 2)
			return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

		double det = 0;
		
		for (int i = 0; i < matrix[0].length; i++) {
			UrMatrix detMat = new UrMatrix(minor(0, i));
			det += Math.pow(-1, i) * matrix[0][i]
					* detMat.determinant();
		}
		return det;
	}
	
	/*
	 * The formula looks like this: 
	 * |   R   | T |    |  R^-1 | -(R^-1)*T |
	 * |-------+---| -> |-------+-----------|
	 * | 0 0 0 | 1 |    | 0 0 0 |     1     |
	 */
	public UrMatrix invertTransformation() {
		assert(matrix[0].length == matrix.length &&
				matrix.length == 4);
		double[][] rmatrix = subMatrix(0, 3, 0, 3);
		double[][] tmatrix = subMatrix(0, 3, 3, 4);
		
		// calculate inverse of the rotation matrix
		double[][] rinv = UrMatrix.invert(rmatrix);
		// caluclate -(R^-1) * T
		double[][] tvector = UrMatrix.negate(UrMatrix.multiply(rinv, tmatrix));
		
		// being explicit to make sure nothing is messed up
		UrMatrix inversion = new UrMatrix(new double[][]{
			{rinv[0][0], rinv[0][1], rinv[0][2], tvector[0][0]},
			{rinv[1][0], rinv[1][1], rinv[1][2], tvector[1][0]},
			{rinv[2][0], rinv[2][1], rinv[2][2], tvector[2][0]},
			{0, 0, 0, 1}
		});
		
		return inversion;
	}
	
	public String toString()
	{

	    String str = "";

	    for (int i = 0 ; i < matrix.length ; i ++ ){
	        for (int j = 0 ; j < matrix[0].length ; j++){
	            str += epsilon(matrix[i][j])+"\t";
	        }
	        str += "\n";
	    }
	    return str;
	}
	
	private double epsilon(double comp) {
		if (Math.abs(comp) < .001) return 0;
		else return comp;
	}
	
	/*
	 * Negate each value of the matrix
	 */
	public static double[][] negate(double[][] mat) {
		double[][] newmat = new double[mat.length][mat[0].length];
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat[i].length; j++) {
				newmat[i][j] = -mat[i][j];
			}
		}
		return newmat;
	}
	
	/*
	 * Start and end indexes are inclusive
	 */
	public double[][] subMatrix(int rstart, int rend, int colstart, int colend) {
		int rowlen = rend - rstart;
		int collen = colend - colstart;
		double[][] sub = new double[rowlen][collen];
		
		int count = 0;
		for (int i = rstart; i <  rend; i++) {
			sub[count++] = Arrays.copyOfRange(matrix[i], colstart, colend);
		}
		return sub;
	}
	
	public static double[][] invert(double[][] matrix) {
		return new UrMatrix(matrix).inverse();
	}

	public double[][] inverse() {
		double[][] inverse = new double[matrix.length][matrix.length];

		// minors and cofactors
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				inverse[i][j] = Math.pow(-1, i + j)
						* new UrMatrix(minor(i, j)).determinant();

		// adjugate and determinant
		double det = 1.0 / determinant();
		for (int i = 0; i < inverse.length; i++) {
			for (int j = 0; j <= i; j++) {
				double temp = inverse[i][j];
				inverse[i][j] = inverse[j][i] * det;
				inverse[j][i] = temp * det;
			}
		}

		return inverse;
	}

	public double[][] minor(int row, int column) {
		double[][] minor = new double[matrix.length - 1][matrix.length - 1];

		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; i != row && j < matrix[i].length; j++)
				if (j != column)
					minor[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
		return minor;
	}
	
	public static double[][] multiply(double[][] a, double[][] b) {
		if (a[0].length != b.length)
			throw new IllegalStateException("invalid dimensions");

		double[][] newmatrix = new double[a.length][b[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				double sum = 0;
				for (int k = 0; k < a[i].length; k++)
					sum += a[i][k] * b[k][j];
				newmatrix[i][j] = sum;
			}
		}

		return newmatrix;
	}
	
	public double[][] multiply(double[][] b) {
		if (matrix[0].length != b.length)
			throw new IllegalStateException("invalid dimensions");

		double[][] newmatrix = new double[matrix.length][b[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				double sum = 0;
				for (int k = 0; k < matrix[i].length; k++)
					sum += matrix[i][k] * b[k][j];
				newmatrix[i][j] = sum;
			}
		}

		return newmatrix;
	}

	public double[][] rref() {
		double[][] rref = new double[matrix.length][];
		for (int i = 0; i < matrix.length; i++)
			rref[i] = Arrays.copyOf(matrix[i], matrix[i].length);

		int r = 0;
		for (int c = 0; c < rref[0].length && r < rref.length; c++) {
			int j = r;
			for (int i = r + 1; i < rref.length; i++)
				if (Math.abs(rref[i][c]) > Math.abs(rref[j][c]))
					j = i;
			if (Math.abs(rref[j][c]) < 0.00001)
				continue;

			double[] temp = rref[j];
			rref[j] = rref[r];
			rref[r] = temp;

			double s = 1.0 / rref[r][c];
			for (j = 0; j < rref[0].length; j++)
				rref[r][j] *= s;
			for (int i = 0; i < rref.length; i++) {
				if (i != r) {
					double t = rref[i][c];
					for (j = 0; j < rref[0].length; j++)
						rref[i][j] -= t * rref[r][j];
				}
			}
			r++;
		}

		return rref;
	}

	public double[][] transpose() {
		double[][] transpose = new double[matrix[0].length][matrix.length];

		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				transpose[j][i] = matrix[i][j];
		return transpose;
	}
	
}
