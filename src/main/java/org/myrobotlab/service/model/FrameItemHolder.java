package org.myrobotlab.service.model;

import java.io.Serializable;
import java.util.Arrays;

public class FrameItemHolder implements Serializable {

	private static final long serialVersionUID = -7038574417962603966L;
	
	private int rthumb, rindex, rmajeure, rringfinger, rpinky, rwrist;
	private int rbicep, rrotate, rshoulder, romoplate;
	private int lthumb, lindex, lmajeure, lringfinger, lpinky, lwrist;
	private int lbicep, lrotate, lshoulder, lomoplate;
	private int neck, rothead, eyeX, eyeY, jaw;
	private int topStom, midStom, lowStom;
	private double rthumbspeed, rindexspeed, rmajeurespeed, rringfingerspeed, rpinkyspeed, rwristspeed;
	private double rbicepspeed, rrotatespeed, rshoulderspeed, romoplatespeed;
	private double lthumbspeed, lindexspeed, lmajeurespeed, lringfingerspeed, lpinkyspeed, lwristspeed;
	private double lbicepspeed, lrotatespeed, lshoulderspeed, lomoplatespeed;
	private double neckspeed, rotheadspeed, eyeXspeed, eyeYspeed, jawspeed;
	private double topStomspeed, midStomspeed, lowStomspeed;
	private int sleep = -1;
	private String speech;
	private String name;

	private final boolean[] tabsMainCheckboxStates = new boolean[6];

	public FrameItemHolder() {
		resetValues();
	}

	@Override
	public String toString() {
		if (this.sleep != -1) {
			// sleep frame
			return "SLEEP "+this.sleep;
		} else if (this.speech != null) {
			// speech frame
			return "SPEECH "+this.speech;
		} else if (this.name != null) {
			// move frame
			// TODO
			return this.name + ": "+this.lshoulder;
		} else {
			// speed frame
			// TODO
			return "SPEED "+this.eyeXspeed;
		}
	}

	public boolean[] getTabsMainCheckboxStates() {
		return tabsMainCheckboxStates;
	}

	public void resetValues() {
		Arrays.fill(tabsMainCheckboxStates, true);
		
		this.sleep = -1;

		this.rthumb = 90;
		this.rindex = 90;
		this.rmajeure = 90;
		this.rringfinger = 90;
		this.rpinky = 90;
		this.rwrist = 90;

		this.rbicep = 90;
		this.rrotate = 90;
		this.rshoulder = 90;
		this.romoplate = 90;

		this.lthumb = 90;
		this.lindex = 90;
		this.lmajeure = 90;
		this.lringfinger = 90;
		this.lpinky = 90;
		this.lwrist = 90;

		this.lbicep = 90;
		this.lrotate = 90;
		this.lshoulder = 90;
		this.lomoplate = 90;

		this.neck = 90;
		this.rothead = 90;
		this.eyeX = 90;
		this.eyeY = 90;
		this.jaw = 90;

		this.topStom = 90;
		this.midStom = 90;
		this.lowStom = 90;
	}

	public int getRthumb() {
		return rthumb;
	}

	public void setRthumb(int rthumb) {
		this.rthumb = rthumb;
	}

	public int getRindex() {
		return rindex;
	}

	public void setRindex(int rindex) {
		this.rindex = rindex;
	}

	public int getRmajeure() {
		return rmajeure;
	}

	public void setRmajeure(int rmajeure) {
		this.rmajeure = rmajeure;
	}

	public int getRringfinger() {
		return rringfinger;
	}

	public void setRringfinger(int rringfinger) {
		this.rringfinger = rringfinger;
	}

	public int getRpinky() {
		return rpinky;
	}

	public void setRpinky(int rpinky) {
		this.rpinky = rpinky;
	}

	public int getRwrist() {
		return rwrist;
	}

	public void setRwrist(int rwrist) {
		this.rwrist = rwrist;
	}

	public int getRbicep() {
		return rbicep;
	}

	public void setRbicep(int rbicep) {
		this.rbicep = rbicep;
	}

	public int getRrotate() {
		return rrotate;
	}

	public void setRrotate(int rrotate) {
		this.rrotate = rrotate;
	}

	public int getRshoulder() {
		return rshoulder;
	}

	public void setRshoulder(int rshoulder) {
		this.rshoulder = rshoulder;
	}

	public int getRomoplate() {
		return romoplate;
	}

	public void setRomoplate(int romoplate) {
		this.romoplate = romoplate;
	}

	public int getLthumb() {
		return lthumb;
	}

	public void setLthumb(int lthumb) {
		this.lthumb = lthumb;
	}

	public int getLindex() {
		return lindex;
	}

	public void setLindex(int lindex) {
		this.lindex = lindex;
	}

	public int getLmajeure() {
		return lmajeure;
	}

	public void setLmajeure(int lmajeure) {
		this.lmajeure = lmajeure;
	}

	public int getLringfinger() {
		return lringfinger;
	}

	public void setLringfinger(int lringfinger) {
		this.lringfinger = lringfinger;
	}

	public int getLpinky() {
		return lpinky;
	}

	public void setLpinky(int lpinky) {
		this.lpinky = lpinky;
	}

	public int getLwrist() {
		return lwrist;
	}

	public void setLwrist(int lwrist) {
		this.lwrist = lwrist;
	}

	public int getLbicep() {
		return lbicep;
	}

	public void setLbicep(int lbicep) {
		this.lbicep = lbicep;
	}

	public int getLrotate() {
		return lrotate;
	}

	public void setLrotate(int lrotate) {
		this.lrotate = lrotate;
	}

	public int getLshoulder() {
		return lshoulder;
	}

	public void setLshoulder(int lshoulder) {
		this.lshoulder = lshoulder;
	}

	public int getLomoplate() {
		return lomoplate;
	}

	public void setLomoplate(int lomoplate) {
		this.lomoplate = lomoplate;
	}

	public int getNeck() {
		return neck;
	}

	public void setNeck(int neck) {
		this.neck = neck;
	}

	public int getRothead() {
		return rothead;
	}

	public void setRothead(int rothead) {
		this.rothead = rothead;
	}

	public int getEyeX() {
		return eyeX;
	}

	public void setEyeX(int eyeX) {
		this.eyeX = eyeX;
	}

	public int getEyeY() {
		return eyeY;
	}

	public void setEyeY(int eyeY) {
		this.eyeY = eyeY;
	}

	public int getJaw() {
		return jaw;
	}

	public void setJaw(int jaw) {
		this.jaw = jaw;
	}

	public int getTopStom() {
		return topStom;
	}

	public void setTopStom(int topStom) {
		this.topStom = topStom;
	}

	public int getMidStom() {
		return midStom;
	}

	public void setMidStom(int midStom) {
		this.midStom = midStom;
	}

	public int getLowStom() {
		return lowStom;
	}

	public void setLowStom(int lowStom) {
		this.lowStom = lowStom;
	}

	public double getRthumbspeed() {
		return rthumbspeed;
	}

	public void setRthumbspeed(double rthumbspeed) {
		this.rthumbspeed = rthumbspeed;
	}

	public double getRindexspeed() {
		return rindexspeed;
	}

	public void setRindexspeed(double rindexspeed) {
		this.rindexspeed = rindexspeed;
	}

	public double getRmajeurespeed() {
		return rmajeurespeed;
	}

	public void setRmajeurespeed(double rmajeurespeed) {
		this.rmajeurespeed = rmajeurespeed;
	}

	public double getRringfingerspeed() {
		return rringfingerspeed;
	}

	public void setRringfingerspeed(double rringfingerspeed) {
		this.rringfingerspeed = rringfingerspeed;
	}

	public double getRpinkyspeed() {
		return rpinkyspeed;
	}

	public void setRpinkyspeed(double rpinkyspeed) {
		this.rpinkyspeed = rpinkyspeed;
	}

	public double getRwristspeed() {
		return rwristspeed;
	}

	public void setRwristspeed(double rwristspeed) {
		this.rwristspeed = rwristspeed;
	}

	public double getRbicepspeed() {
		return rbicepspeed;
	}

	public void setRbicepspeed(double rbicepspeed) {
		this.rbicepspeed = rbicepspeed;
	}

	public double getRrotatespeed() {
		return rrotatespeed;
	}

	public void setRrotatespeed(double rrotatespeed) {
		this.rrotatespeed = rrotatespeed;
	}

	public double getRshoulderspeed() {
		return rshoulderspeed;
	}

	public void setRshoulderspeed(double rshoulderspeed) {
		this.rshoulderspeed = rshoulderspeed;
	}

	public double getRomoplatespeed() {
		return romoplatespeed;
	}

	public void setRomoplatespeed(double romoplatespeed) {
		this.romoplatespeed = romoplatespeed;
	}

	public double getLthumbspeed() {
		return lthumbspeed;
	}

	public void setLthumbspeed(double lthumbspeed) {
		this.lthumbspeed = lthumbspeed;
	}

	public double getLindexspeed() {
		return lindexspeed;
	}

	public void setLindexspeed(double lindexspeed) {
		this.lindexspeed = lindexspeed;
	}

	public double getLmajeurespeed() {
		return lmajeurespeed;
	}

	public void setLmajeurespeed(double lmajeurespeed) {
		this.lmajeurespeed = lmajeurespeed;
	}

	public double getLringfingerspeed() {
		return lringfingerspeed;
	}

	public void setLringfingerspeed(double lringfingerspeed) {
		this.lringfingerspeed = lringfingerspeed;
	}

	public double getLpinkyspeed() {
		return lpinkyspeed;
	}

	public void setLpinkyspeed(double lpinkyspeed) {
		this.lpinkyspeed = lpinkyspeed;
	}

	public double getLwristspeed() {
		return lwristspeed;
	}

	public void setLwristspeed(double lwristspeed) {
		this.lwristspeed = lwristspeed;
	}

	public double getLbicepspeed() {
		return lbicepspeed;
	}

	public void setLbicepspeed(double lbicepspeed) {
		this.lbicepspeed = lbicepspeed;
	}

	public double getLrotatespeed() {
		return lrotatespeed;
	}

	public void setLrotatespeed(double lrotatespeed) {
		this.lrotatespeed = lrotatespeed;
	}

	public double getLshoulderspeed() {
		return lshoulderspeed;
	}

	public void setLshoulderspeed(double lshoulderspeed) {
		this.lshoulderspeed = lshoulderspeed;
	}

	public double getLomoplatespeed() {
		return lomoplatespeed;
	}

	public void setLomoplatespeed(double lomoplatespeed) {
		this.lomoplatespeed = lomoplatespeed;
	}

	public double getNeckspeed() {
		return neckspeed;
	}

	public void setNeckspeed(double neckspeed) {
		this.neckspeed = neckspeed;
	}

	public double getRotheadspeed() {
		return rotheadspeed;
	}

	public void setRotheadspeed(double rotheadspeed) {
		this.rotheadspeed = rotheadspeed;
	}

	public double getEyeXspeed() {
		return eyeXspeed;
	}

	public void setEyeXspeed(double eyeXspeed) {
		this.eyeXspeed = eyeXspeed;
	}

	public double getEyeYspeed() {
		return eyeYspeed;
	}

	public void setEyeYspeed(double eyeYspeed) {
		this.eyeYspeed = eyeYspeed;
	}

	public double getJawspeed() {
		return jawspeed;
	}

	public void setJawspeed(double jawspeed) {
		this.jawspeed = jawspeed;
	}

	public double getTopStomspeed() {
		return topStomspeed;
	}

	public void setTopStomspeed(double topStomspeed) {
		this.topStomspeed = topStomspeed;
	}

	public double getMidStomspeed() {
		return midStomspeed;
	}

	public void setMidStomspeed(double midStomspeed) {
		this.midStomspeed = midStomspeed;
	}

	public double getLowStomspeed() {
		return lowStomspeed;
	}

	public void setLowStomspeed(double lowStomspeed) {
		this.lowStomspeed = lowStomspeed;
	}

	public int getSleep() {
		return sleep;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}

	public String getSpeech() {
		return speech;
	}

	public void setSpeech(String speech) {
		this.speech = speech;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
