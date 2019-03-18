package org.myrobotlab.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Gesture implements Serializable {

	private static final long serialVersionUID = -7246573963404596295L;
	
	private String gestureName;
	private final List<Frame> frames = new ArrayList<Frame>();
	
	public String getGestureName() {
		return gestureName;
	}
	public void setGestureName(String gestureName) {
		this.gestureName = gestureName;
	}
	public List<Frame> getFrames() {
		return frames;
	}
}