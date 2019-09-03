package com.loccioni.teachposition.impl;

public class UrAxisAngle {

	double[][] rotMat;
	double[] rotVec;
	double _angle;
	double _x;
	double _y;
	double _z;

	public UrAxisAngle(double[] rotVec) {
		this.rotVec = rotVec;
		computeComponentsFromRotVec();
	}

	public UrAxisAngle(double[][] rotMat) {
		this.rotMat = rotMat;
		computeComponentsFromRotMat();
	}
	
	public UrAxisAngle(double x, double y, double z, double theta) {
		_x = x;
		_y = y;
		_z = z;
		_angle = theta;
	}

	public double getAngle() {
		return _angle;
	}

	public double getX() {
		return _x;
	}

	public double getY() {
		return _y;
	}

	public double getZ() {
		return _z;
	}

	public double[] getRotationVector() {
		if (rotVec != null) 
			return rotVec;
		
		rotVec = new double[]{_angle*_x, _angle*_y, _angle*_z};
		return rotVec;
	}

	public double[][] getRotationMatrix() {
		if (rotMat != null)
			return rotMat;
		if (_angle == 0)
			return new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };

		double c = Math.cos(_angle);
		double s = Math.sin(_angle);
		double t = 1.0 - c;

		double m00 = c + _x * _x * t;
		double m11 = c + _y * _y * t;
		double m22 = c + _z * _z * t;

		double tmp1 = _x * _y * t;
		double tmp2 = _z * s;
		double m10 = tmp1 + tmp2;
		double m01 = tmp1 - tmp2;

		tmp1 = _x * _z * t;
		tmp2 = _y * s;
		double m20 = tmp1 - tmp2;
		double m02 = tmp1 + tmp2;

		tmp1 = _y * _z * t;
		tmp2 = _x * s;
		double m21 = tmp1 + tmp2;
		double m12 = tmp1 - tmp2;

		return new double[][] { { m00, m01, m02 }, { m10, m11, m12 },
				{ m20, m21, m22 } };
	}
	
	public void printRotationMatrix() {
		double[][] matrix = getRotationMatrix();
		for (int i = 0; i < matrix.length; i++) {
		    for (int j = 0; j < matrix[i].length; j++) {
		        System.out.print(epsilon(matrix[i][j]) + " ");
		    }
		    System.out.println();
		}
	}
	
	private double epsilon(double comp) {
		if (Math.abs(comp) < .001)
			return 0;
		else return comp;
	}

	private void computeComponentsFromRotVec() {
		rotMat = null; // we do this to reset the lazy calculation from getRotationMatrix
		
		_angle = Math.sqrt(Math.pow(rotVec[0], 2) + Math.pow(rotVec[1], 2)
				+ Math.pow(rotVec[2], 2));
		if (_angle == 0) {
			_y = _z = 0;
			_x = 1;
		} else {
			_x = rotVec[0] / _angle;
			_y = rotVec[1] / _angle;
			_z = rotVec[2] / _angle;
		}
	}

	private void computeComponentsFromRotMat() {
		rotVec = null;  // we do this to reset the lazy calculation from getRotVec
		double angle, x, y, z; // variables for result
		double epsilon = 0.01; // margin to allow for rounding errors
		double epsilon2 = 0.1; // margin to distinguish between 0 and 180
								// degrees
		// optional check that input is pure rotation, 'isRotationMatrix' is
		// defined at:
		// https://www.euclideanspace.com/maths/algebra/matrix/orthogonal/rotation/
		if ((Math.abs(rotMat[0][1] - rotMat[1][0]) < epsilon)
				&& (Math.abs(rotMat[0][2] - rotMat[2][0]) < epsilon)
				&& (Math.abs(rotMat[1][2] - rotMat[2][1]) < epsilon)) {
			// singularity found
			// first check for identity matrix which must have +1 for all terms
			// in leading diagonaland zero in other terms
			if ((Math.abs(rotMat[0][1] + rotMat[1][0]) < epsilon2)
					&& (Math.abs(rotMat[0][2] + rotMat[2][0]) < epsilon2)
					&& (Math.abs(rotMat[1][2] + rotMat[2][1]) < epsilon2)
					&& (Math.abs(rotMat[0][0] + rotMat[1][1] + rotMat[2][2] - 3) < epsilon2)) {
				// this singularity is identity matrix so angle = 0
				_angle = _y = _z = 0;
				_x = 1;
				return;
			}
			// otherwise this singularity is angle = 180
			angle = Math.PI;
			double xx = (rotMat[0][0] + 1) / 2;
			double yy = (rotMat[1][1] + 1) / 2;
			double zz = (rotMat[2][2] + 1) / 2;
			double xy = (rotMat[0][1] + rotMat[1][0]) / 4;
			double xz = (rotMat[0][2] + rotMat[2][0]) / 4;
			double yz = (rotMat[1][2] + rotMat[2][1]) / 4;
			if ((xx > yy) && (xx > zz)) { // m[0][0] is the largest diagonal
											// term
				if (xx < epsilon) {
					x = 0;
					y = 0.7071;
					z = 0.7071;
				} else {
					x = Math.sqrt(xx);
					y = xy / x;
					z = xz / x;
				}
			} else if (yy > zz) { // m[1][1] is the largest diagonal term
				if (yy < epsilon) {
					x = 0.7071;
					y = 0;
					z = 0.7071;
				} else {
					y = Math.sqrt(yy);
					x = xy / y;
					z = yz / y;
				}
			} else { // m[2][2] is the largest diagonal term so base result on
						// this
				if (zz < epsilon) {
					x = 0.7071;
					y = 0.7071;
					z = 0;
				} else {
					z = Math.sqrt(zz);
					x = xz / z;
					y = yz / z;
				}
			}
			_x = x;
			_y = y;
			_z = z;
			_angle = angle;
			return; // return 180 deg rotation
		}
		// as we have reached here there are no singularities so we can handle
		// normally
		double s = Math.sqrt((rotMat[2][1] - rotMat[1][2]) * (rotMat[2][1] - rotMat[1][2])
				+ (rotMat[0][2] - rotMat[2][0]) * (rotMat[0][2] - rotMat[2][0])
				+ (rotMat[1][0] - rotMat[0][1]) * (rotMat[1][0] - rotMat[0][1])); // used to
																// normalise
		if (Math.abs(s) < 0.001)
			s = 1;
		// prevent divide by zero, should not happen if matrix is orthogonal and
		// should be
		// caught by singularity test above, but I've left it in just in case
		angle = Math.acos((rotMat[0][0] + rotMat[1][1] + rotMat[2][2] - 1) / 2);
		x = (rotMat[2][1] - rotMat[1][2]) / s;
		y = (rotMat[0][2] - rotMat[2][0]) / s;
		z = (rotMat[1][0] - rotMat[0][1]) / s;
		_x = x;
		_y = y;
		_z = z;
		_angle = angle;
	}

}
