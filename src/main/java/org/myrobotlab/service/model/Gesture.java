package org.myrobotlab.service.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Gesture implements Serializable {

	private static final long serialVersionUID = -7246573963404596295L;
	private static final String NEW_LINE = System.lineSeparator();
	private static final String DOUBLE_SPACE = "  ";
	
	private File gestureFile = null;
	private String gestureName = null;
	private final List<Frame> frames = new ArrayList<Frame>();

	public String toPythonGesture() {
		StringBuffer pythonGestureString = new StringBuffer();

		pythonGestureString.append("def ");

		if (gestureName != null) {
			pythonGestureString.append(this.gestureName);
		} else {
			pythonGestureString.append("unknown");
		}
		pythonGestureString
			.append("():").append(NEW_LINE)
			.append(DOUBLE_SPACE).append("i01.startedGesture()").append(NEW_LINE);
		for (Frame frame : frames) {
			pythonGestureString.append(frame.toPythonFrame());
		}
		pythonGestureString
			.append(DOUBLE_SPACE).append("i01.finishedGesture()").append(NEW_LINE)
			.append(DOUBLE_SPACE).append("relax()");
		return pythonGestureString.toString();
	}
	
	public File getGestureFile() {
		return gestureFile;
	}
	public void setGestureFile(File gestureFile) {
		this.gestureFile = gestureFile;
	}
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