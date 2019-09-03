package com.loccioni.teachposition.impl;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

public class UrPose {
	
	Vector<Double> pose;
	public UrPose(double[] pose) {
		this.pose = new Vector<Double>();
		for (int i = 0; i < pose.length; i++)
			this.pose.add(pose[i]);
		
	}
	
	public UrPose(double[][] xformMat) {
		this.pose = new Vector<Double>();
		this.pose.add(xformMat[0][3]);
		this.pose.add(xformMat[1][3]);
		this.pose.add(xformMat[2][3]);
		UrAxisAngle rot = new UrAxisAngle(new double[][]{
				{xformMat[0][0], xformMat[0][1], xformMat[0][2]},
				{xformMat[1][0], xformMat[1][1], xformMat[1][2]},
				{xformMat[2][0], xformMat[2][1], xformMat[2][2]}
		});
		double[] rv = rot.getRotationVector();
		this.pose.add(rv[0]);
		this.pose.add(rv[1]);
		this.pose.add(rv[2]);
	}
	
	public void setRxRyRz(double[] rot) {
		setRxRyRz(rot[0], rot[1], rot[2]);
	}
	
	public void setRxRyRz(double rx, double ry, double rz) {
		pose.set(3, rx);
		pose.set(4, ry);
		pose.set(5, rz);
	}
	
	public void setXYZ(double[] pos) {
		setXYZ(pos[0], pos[1], pos[2]);
	}
	
	public void setXYZ(double x, double y, double z) {
		pose.set(0, x);
		pose.set(1, y);
		pose.set(2, z);
	}
	
	public double[] getRotationVector() {
		List<Double> sublist = pose.subList(3, 6);
		return convertListToPrimitives(sublist.toArray(new Double[3]));
		}
	
	private double[] convertListToPrimitives(Double[] list) {
		int len = list.length;
		double[] retArr = new double[len];
		for (int i = 0; i < len; i++)
			retArr[i] = list[i];
		
		return retArr;
	}
	
	public double[] getTranslationVector() {
		List<Double> sublist = pose.subList(0,3);
		return convertListToPrimitives(sublist.toArray(new Double[3]));
	}

	public double[] invert() {
		UrMatrix rotMat = new UrMatrix(getTransformMatrix());
		UrMatrix invMat = rotMat.invertTransformation();
		UrAxisAngle angle = new UrAxisAngle(invMat.subMatrix(0, 3, 0, 3));
		double[] rotVec = angle.getRotationVector();
		double[][] transMat = invMat.subMatrix(0, 3, 3, 4);
		
		double[] newPose = new double[]{
				transMat[0][0], transMat[1][0], transMat[2][0],
				rotVec[0], rotVec[1], rotVec[2]
		};
		
		return newPose;
	}
	
	public double[][] getTransformMatrix() {
		double[] trans = getTranslationVector();
		double[] rot = getRotationVector();
		
		UrAxisAngle angle = new UrAxisAngle(rot);
		double[][] rotMat = angle.getRotationMatrix();
		
		double[][] mat = new double[][] {
				{rotMat[0][0], rotMat[0][1], rotMat[0][2], trans[0]},
				{rotMat[1][0], rotMat[1][1], rotMat[1][2], trans[1]},
				{rotMat[2][0], rotMat[2][1], rotMat[2][2], trans[2]},
				{0, 0, 0, 1}
		};
		
		return mat;
	}
	
	/*
	 * Return pose_trans(this, p2)
	 */
	public UrPose pose_trans(UrPose p2) {
		double[][] result = UrMatrix.multiply(getTransformMatrix(), p2.getTransformMatrix());
		return new UrPose(result);
	}
	
	public static double[] pose_trans(double[] p1, double[] p2) {
		UrPose pose1 = new UrPose(p1);
		UrPose pose2 = new UrPose(p2);
		double[][] result = UrMatrix.multiply(pose1.getTransformMatrix(), pose2.getTransformMatrix());
		UrPose xform = new UrPose(result);
		return xform.toDoubleArray();
	}
	
	public double[] toDoubleArray() {
		double[] ret = new double[]{
				pose.get(0), pose.get(1), pose.get(2),
				pose.get(3), pose.get(4), pose.get(5)
		};
		return ret;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(4);
		String str = "< ";
		for (int i = 0; i < pose.size(); i++) {
			str += df.format(epsilon(pose.get(i)));
			if (i < pose.size() - 1) str += ", ";
		}
		str += " >";
		return str;
	}
	
	private double epsilon(double comp) {
		if (Math.abs(comp) < .001) return 0;
		else return comp;
	}

	public static double[] pose_inv(double[] pos) {
		return (new UrPose(pos)).invert();
	}
}
