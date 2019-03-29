package org.myrobotlab.service.model;

import java.io.Serializable;
import java.util.Arrays;

import org.myrobotlab.logging.LoggerFactory;
import org.slf4j.Logger;

public class Frame implements Serializable {

	private static final long serialVersionUID = -7038574417962603966L;
	private final static Logger LOGGER = LoggerFactory.getLogger(Frame.class);

//	public enum FrameType {
//		SPEED, SPEECH, SLEEP, MOVE;
//	};
	public enum RobotSection {
		RIGHT_HAND, RIGHT_ARM, LEFT_HAND, LEFT_ARM, HEAD, TORSO;
	};
	public enum HandSection {
		THUMB_FINGER, INDEX_FINGER, MAJEURE_FINGER, RING_FINGER, PINKY_FINGER, WRIST;
	};
	public enum ArmSection {
		BICEPS, ROTATE, SHOULDER, OMOPLATE;
	};
	public enum HeadSection {
		NECK, HEAD, EYE_X, EYE_Y, JAW;
	};
	public enum TorsoSection {
		TOP, MID, LOW;
	};

	private Boolean speechSet = false;
	/**
	 * 0: right hand
	 * 1: right arm
	 * 2: left hand
	 * 3: left arm
	 * 4: head
	 * 5: torso
	 */
	final private Boolean[] moveSets = new Boolean[6];
	final private Boolean[] speedSets = new Boolean[6];
	// moves
	/**
	 * 0: thumb
	 * 1: index
	 * 2: majeure
	 * 3: ring
	 * 4: pinky
	 * 5: wrist
	 */
	final private Integer[] rightHandMoves = new Integer[6];
	/**
	 * 0: biceps
	 * 1: Rotate
	 * 2: shoulder
	 * 3: omoplate
	 */
	final private Integer[] rightArmMoves = new Integer[4];
	final private Integer[] leftHandMoves = new Integer[6];
	final private Integer[] leftArmMoves = new Integer[4];
	/**
	 * 0: neck
	 * 1: head
	 * 2: eyeX
	 * 3: eyeY
	 * 4: jaw
	 */
	final private Integer[] headMoves = new Integer[5];
	/**
	 * 0: top
	 * 1: mid
	 * 2: low
	 */
	final private Integer[] torsoMoves = new Integer[3];
	// speeds
	final private Double[] rightHandSpeeds = new Double[6];
	final private Double[] rightArmSpeeds = new Double[4];
	final private Double[] leftHandSpeeds = new Double[6];
	final private Double[] leftArmSpeeds = new Double[4];
	final private Double[] headSpeeds = new Double[5];
	final private Double[] torsoSpeeds = new Double[3];
	// labels
	final private static String[] SECTION_LABELS = {"Right Hand", "Right Arm", "Left Hand", "Left Arm", "Head", "Torso"};
	final private static String[] HAND_LABELS = {"Thumb", "Index", "Maejure", "Ring", "Pinky", "Wrist"};
	final private static String[] ARM_LABELS = {"Biceps", "Rotate", "Shoulder", "Omoplate"};
	final private static String[] HEAD_LABELS = {"Neck", "Head", "EyeX", "EyeY", "Jaw"};
	final private static String[] TORSO_LABELS = {"Top", "Mid", "Low"};
	
	private Integer sleep = -1;
	private String speech = null;
	private String name = null;

	private static final String STAR_SYMBOL = "* ";
	private static final String SPACE_SYMBOL = " ";
	private static final String PIPE_SYMBOL = " | ";
	private static final String NEW_LINE_HTML = "<br/>";
	private static final String NEW_LINE = System.lineSeparator();
	private static final String DOUBLE_SPACE = "  ";
	private static final String COMMA = ",";

	public Frame() {
		resetValues();
	}
	
	public String toPythonFrame() {
		StringBuffer pythonFrameString = new StringBuffer();
		/* SPEECH */
		if (speechSet) {
			pythonFrameString
			.append(DOUBLE_SPACE).append("i01.mouth.speak(\"")
				.append(speech).append("\")").append(NEW_LINE);
		}
		/* SPEED */
		// right hand speed
		if (speedSets[0]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.setHandSpeed(\"right\",");
			for(Double speed : rightHandSpeeds) {
				pythonFrameString.append(speed).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// right arm
		if (speedSets[1]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.setArmSpeed(\"right\",");
			for(Double speed : rightArmSpeeds) {
				pythonFrameString.append(speed).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// left hand
		if (speedSets[2]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.setHandSpeed(\"left\",");
			for(Double speed : leftHandSpeeds) {
				pythonFrameString.append(speed).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// left arm
		if (speedSets[3]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.setArmSpeed(\"left\",");
			for(Double speed : leftArmSpeeds) {
				pythonFrameString.append(speed).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// head
		if (speedSets[4]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.setHeadSpeed(");
			for(Double speed : headSpeeds) {
				pythonFrameString.append(speed).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// torso
		if (speedSets[5]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.setTorsoSpeed(");
			for(Double speed : torsoSpeeds) {
				pythonFrameString.append(speed).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		/* MOVE */
		// right hand
		if (moveSets[0]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.moveHand(\"right\",");
			for(Integer move : rightHandMoves) {
				pythonFrameString.append(move).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// right arm
		if (moveSets[1]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.moveArm(\"right\",");
			for(Integer move : rightArmMoves) {
				pythonFrameString.append(move).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// left hand
		if (moveSets[2]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.moveHand(\"left\",");
			for(Integer move : leftHandMoves) {
				pythonFrameString.append(move).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// left arm
		if (moveSets[3]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.moveArm(\"left\",");
			for(Integer move : leftArmMoves) {
				pythonFrameString.append(move).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// head
		if (moveSets[4]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.moveHead(");
			for(Integer move : headMoves) {
				pythonFrameString.append(move).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		// torso
		if (moveSets[5]) {
			pythonFrameString.append(DOUBLE_SPACE).append("i01.moveTorso(");
			for(Integer move : torsoMoves) {
				pythonFrameString.append(move).append(COMMA);
			}
			// remove the last comma
			pythonFrameString.setLength(pythonFrameString.length() - 1);
			pythonFrameString.append(")").append(NEW_LINE);
		}
		/* SLEEP */
		pythonFrameString.append(DOUBLE_SPACE).append("sleep(").append(this.sleep).append(")").append(NEW_LINE);
		return pythonFrameString.toString();
	}

	@Override
	public String toString() {
		StringBuffer frameString = new StringBuffer();
		// speech frame
		frameString.append("<html>FRAME: ").append(this.name).append(NEW_LINE_HTML);
		frameString.append("- SPEECH ");
		if (speechSet) {
			frameString.append(this.speech);
		} else {
			frameString.append(STAR_SYMBOL);
		}
		frameString.append(NEW_LINE_HTML);
		// speed frame
		frameString.append("- SPEED ");
		// right hand
		if (speedSets[0]) {
			frameString.append(this.rightHandSpeeds[0]).append(SPACE_SYMBOL).append(this.rightHandSpeeds[1])
					.append(SPACE_SYMBOL).append(this.rightHandSpeeds[2]).append(SPACE_SYMBOL)
					.append(this.rightHandSpeeds[3]).append(SPACE_SYMBOL).append(this.rightHandSpeeds[4])
					.append(SPACE_SYMBOL).append(this.rightHandSpeeds[5]).append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
					.append(STAR_SYMBOL).append(PIPE_SYMBOL);
		}
		// right arm
		if (speedSets[1]) {
			frameString.append(this.rightArmSpeeds[0]).append(SPACE_SYMBOL).append(this.rightArmSpeeds[1])
					.append(SPACE_SYMBOL).append(this.rightArmSpeeds[2]).append(SPACE_SYMBOL)
					.append(this.rightArmSpeeds[3]).append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
		}
		// left hand
		if (speedSets[2]) {
			frameString.append(this.leftHandSpeeds[0]).append(SPACE_SYMBOL).append(this.leftHandSpeeds[1])
					.append(SPACE_SYMBOL).append(this.leftHandSpeeds[2]).append(SPACE_SYMBOL)
					.append(this.leftHandSpeeds[3]).append(SPACE_SYMBOL).append(this.leftHandSpeeds[4])
					.append(SPACE_SYMBOL).append(this.leftHandSpeeds[5]).append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
					.append(STAR_SYMBOL).append(PIPE_SYMBOL);
		}
		// left arm
		if (speedSets[3]) {
			frameString.append(this.leftArmSpeeds[0]).append(SPACE_SYMBOL).append(this.leftArmSpeeds[1]).append(SPACE_SYMBOL)
					.append(this.leftArmSpeeds[2]).append(SPACE_SYMBOL).append(this.leftArmSpeeds[3])
					.append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
		}
		// head
		if (speedSets[4]) {
			frameString.append(this.headSpeeds[0]).append(SPACE_SYMBOL).append(this.headSpeeds[1]).append(SPACE_SYMBOL)
					.append(this.headSpeeds[2]).append(SPACE_SYMBOL).append(this.headSpeeds[3]).append(SPACE_SYMBOL)
					.append(this.headSpeeds[4]).append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
					.append(PIPE_SYMBOL);
		}
		// torso
		if (speedSets[5]) {
			frameString.append(this.torsoSpeeds[0]).append(SPACE_SYMBOL).append(this.torsoSpeeds[1]).append(SPACE_SYMBOL)
					.append(this.torsoSpeeds[2]);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL);
		}
		frameString.append(NEW_LINE_HTML);
		// move frame
		frameString.append("- MOVE ");
		// right hand
		if (moveSets[0]) {
			frameString.append(this.rightHandMoves[0]).append(SPACE_SYMBOL).append(this.rightHandMoves[1])
					.append(SPACE_SYMBOL).append(this.rightHandMoves[2]).append(SPACE_SYMBOL)
					.append(this.rightHandMoves[3]).append(SPACE_SYMBOL).append(this.rightHandMoves[4])
					.append(SPACE_SYMBOL).append(this.rightHandMoves[5]).append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
					.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
		}
		// right arm
		if (moveSets[1]) {
			frameString.append(this.rightArmMoves[0]).append(SPACE_SYMBOL).append(this.rightArmMoves[1])
					.append(SPACE_SYMBOL).append(this.rightArmMoves[2]).append(SPACE_SYMBOL)
					.append(this.rightArmMoves[3]).append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
					.append(PIPE_SYMBOL);
		}
		// left hand
		if (moveSets[2]) {
			frameString.append(this.leftHandMoves[0]).append(SPACE_SYMBOL).append(this.leftHandMoves[1])
					.append(SPACE_SYMBOL).append(this.leftHandMoves[2]).append(SPACE_SYMBOL)
					.append(this.leftHandMoves[3]).append(SPACE_SYMBOL).append(this.leftHandMoves[4])
					.append(SPACE_SYMBOL).append(this.leftHandMoves[5]).append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
					.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
		}
		// left arm
		if (moveSets[3]) {
			frameString.append(this.leftArmMoves[0]).append(SPACE_SYMBOL).append(this.leftArmMoves[1])
					.append(SPACE_SYMBOL).append(this.leftArmMoves[2]).append(SPACE_SYMBOL).append(this.leftArmMoves[3])
					.append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
					.append(PIPE_SYMBOL);
		}
		// head
		if (moveSets[4]) {
			frameString.append(this.headMoves[0]).append(SPACE_SYMBOL).append(this.headMoves[1]).append(SPACE_SYMBOL)
					.append(this.headMoves[2]).append(SPACE_SYMBOL).append(this.headMoves[3]).append(SPACE_SYMBOL)
					.append(this.headMoves[4]).append(PIPE_SYMBOL);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
					.append(STAR_SYMBOL).append(PIPE_SYMBOL);
		}
		// torso
		if (moveSets[5]) {
			frameString.append(this.torsoMoves[0]).append(SPACE_SYMBOL).append(this.torsoMoves[1]).append(SPACE_SYMBOL)
					.append(this.torsoMoves[2]);
		} else {
			frameString.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL);
		}
		frameString.append(NEW_LINE_HTML);
		// sleep frame
		frameString.append("- SLEEP ").append(this.sleep).append("</html>");
		//
		return frameString.toString();
	}

	public void resetValues() {	
		Arrays.fill(moveSets, false);
		Arrays.fill(speedSets, false);

		Arrays.fill(rightHandMoves, 90);
		Arrays.fill(rightArmMoves, 90);
		Arrays.fill(leftHandMoves, 90);
		Arrays.fill(leftArmMoves, 90);
		Arrays.fill(headMoves, 90);
		Arrays.fill(torsoMoves, 90);
		
		Arrays.fill(rightHandSpeeds, 0d);
		Arrays.fill(rightArmSpeeds, 0d);
		Arrays.fill(leftHandSpeeds, 0d);
		Arrays.fill(leftArmSpeeds, 0d);
		Arrays.fill(headSpeeds, 0d);
		Arrays.fill(torsoSpeeds, 0d);
		
		this.sleep = -1;
		this.speech = null;
		this.name = null;
	}

	public static int getSubSectionSize(RobotSection robotSection) {
		switch (robotSection) {
		case RIGHT_HAND:
			return 6;
		case RIGHT_ARM:
			return 4;
		case LEFT_HAND:
			return 6;
		case LEFT_ARM:
			return 4;
		case HEAD:
			return 5;
		case TORSO:
			return 3;
		default:
			return 0;
		}
	}

	public static String getSectionLabel(RobotSection robotSection, int sectionIndex) {
		LOGGER.debug("getSectionLabel robotSection: [{}] sectionIndex: [{}]", robotSection, sectionIndex);
		switch (robotSection) {
		case RIGHT_HAND:
		case LEFT_HAND:
			return HAND_LABELS[sectionIndex];
		case RIGHT_ARM:
		case LEFT_ARM:
			return ARM_LABELS[sectionIndex];
		case HEAD:
			return HEAD_LABELS[sectionIndex];
		case TORSO:
			return TORSO_LABELS[sectionIndex];
		default:
			return null;
		}
	}

	public static String getSectionLabel(RobotSection robotSection) {
		LOGGER.debug("getMoveValue robotSection: [{}]", robotSection);
		switch (robotSection) {
		case RIGHT_HAND:
			return SECTION_LABELS[0];
		case RIGHT_ARM:
			return SECTION_LABELS[1];
		case LEFT_HAND:
			return SECTION_LABELS[2];
		case LEFT_ARM:
			return SECTION_LABELS[3];
		case HEAD:
			return SECTION_LABELS[4];
		case TORSO:
			return SECTION_LABELS[5];
		default:
			return null;
		}
	}

	public Integer getMoveValue(RobotSection robotSection, int sectionIndex) {
		LOGGER.debug("getMoveValue robotSection: [{}] sectionIndex: [{}]", robotSection, sectionIndex);
		switch (robotSection) {
		case RIGHT_HAND:
			return rightHandMoves[sectionIndex];
		case RIGHT_ARM:
			return rightArmMoves[sectionIndex];
		case LEFT_HAND:
			return leftHandMoves[sectionIndex];
		case LEFT_ARM:
			return leftArmMoves[sectionIndex];
		case HEAD:
			return headMoves[sectionIndex];
		case TORSO:
			return torsoMoves[sectionIndex];
		default:
			return null;
		}
	}

	public void setMoveValue(RobotSection robotSection, int sectionIndex, Integer moveValue) {
		LOGGER.trace("setMoveValue robotSection: [{}] sectionIndex: [{}] moveValue: [{}]", 
				robotSection, sectionIndex, moveValue);
		switch (robotSection) {
		case RIGHT_HAND:
			this.rightHandMoves[sectionIndex] = moveValue;
			return;
		case RIGHT_ARM:
			this.rightArmMoves[sectionIndex] = moveValue;
			return;
		case LEFT_HAND:
			this.leftHandMoves[sectionIndex] = moveValue;
			return;
		case LEFT_ARM:
			this.leftArmMoves[sectionIndex] = moveValue;
			return;
		case HEAD:
			this.headMoves[sectionIndex] = moveValue;
			return;
		case TORSO:
			this.torsoMoves[sectionIndex] = moveValue;
			return;
		default:
		}
	}

	public Double getSpeedValue(RobotSection robotSection, int sectionIndex) {
		LOGGER.debug("getSpeedValue robotSection: [{}] and sectionIndex: [{}]", robotSection, sectionIndex);
		switch (robotSection) {
		case RIGHT_HAND:
			return rightHandSpeeds[sectionIndex];
		case RIGHT_ARM:
			return rightArmSpeeds[sectionIndex];
		case LEFT_HAND:
			return leftHandSpeeds[sectionIndex];
		case LEFT_ARM:
			return leftArmSpeeds[sectionIndex];
		case HEAD:
			return headSpeeds[sectionIndex];
		case TORSO:
			return torsoSpeeds[sectionIndex];
		default:
			return null;
		}
	}

	public void setSpeedValue(RobotSection robotSection, int sectionIndex, Double speedValue) {
		LOGGER.debug("setSpeedValue robotSection: [{}] sectionIndex: [{}] speedValue: [{}]", 
				robotSection, sectionIndex, speedValue);
		switch (robotSection) {
		case RIGHT_HAND:
			this.rightHandSpeeds[sectionIndex] = speedValue;
			return;
		case RIGHT_ARM:
			this.rightArmSpeeds[sectionIndex] = speedValue;
			return;
		case LEFT_HAND:
			this.leftHandSpeeds[sectionIndex] = speedValue;
			return;
		case LEFT_ARM:
			this.leftArmSpeeds[sectionIndex] = speedValue;
			return;
		case HEAD:
			this.headSpeeds[sectionIndex] = speedValue;
			return;
		case TORSO:
			this.torsoSpeeds[sectionIndex] = speedValue;
			return;
		default:
		}
	}

	public Boolean getMoveSet(RobotSection robotSection) {
		LOGGER.debug("getMoveSet robotSection: [{}]", robotSection);
		switch (robotSection) {
		case RIGHT_HAND:
			return moveSets[0];
		case RIGHT_ARM:
			return moveSets[1];
		case LEFT_HAND:
			return moveSets[2];
		case LEFT_ARM:
			return moveSets[3];
		case HEAD:
			return moveSets[4];
		case TORSO:
			return moveSets[5];
		default:
			return null;
		}
	}

	public void setMoveSet(RobotSection robotSection, Boolean moveSet) {
		LOGGER.debug("setMoveSet robotSection: [{}] moveSet: [{}]", robotSection, moveSet);
		switch (robotSection) {
		case RIGHT_HAND:
			this.moveSets[0] = moveSet;
			return;
		case RIGHT_ARM:
			this.moveSets[1] = moveSet;
			return;
		case LEFT_HAND:
			this.moveSets[2] = moveSet;
			return;
		case LEFT_ARM:
			this.moveSets[3] = moveSet;
			return;
		case HEAD:
			this.moveSets[4] = moveSet;
			return;
		case TORSO:
			this.moveSets[5] = moveSet;
			return;
		default:
		}
	}

	public Boolean getSpeedSet(RobotSection robotSection) {
		LOGGER.debug("getSpeedSet robotSection: [{}]", robotSection);
		switch (robotSection) {
		case RIGHT_HAND:
			return speedSets[0];
		case RIGHT_ARM:
			return speedSets[1];
		case LEFT_HAND:
			return speedSets[2];
		case LEFT_ARM:
			return speedSets[3];
		case HEAD:
			return speedSets[4];
		case TORSO:
			return speedSets[5];
		default:
			return null;
		}
	}

	public void setSpeedSet(RobotSection robotSection, Boolean speedSet) {
		LOGGER.debug("setSpeedSet robotSection: [{}] speedSet: [{}]", robotSection, speedSet);
		switch (robotSection) {
		case RIGHT_HAND:
			this.speedSets[0] = speedSet;
			return;
		case RIGHT_ARM:
			this.speedSets[1] = speedSet;
			return;
		case LEFT_HAND:
			this.speedSets[2] = speedSet;
			return;
		case LEFT_ARM:
			this.speedSets[3] = speedSet;
			return;
		case HEAD:
			this.speedSets[4] = speedSet;
			return;
		case TORSO:
			this.speedSets[5] = speedSet;
			return;
		default:
		}
	}

	public Boolean getRightHandSpeedSet() {
		return speedSets[0];
	}

	public void setRightHandSpeedSet(Boolean rightHandSpeedSet) {
		this.speedSets[0] = rightHandSpeedSet;
	}

	public Boolean getRightArmSpeedSet() {
		return speedSets[1];
	}

	public void setRightArmSpeedSet(Boolean rightArmSpeedSet) {
		this.speedSets[1] = rightArmSpeedSet;
	}

	public Boolean getLeftHandSpeedSet() {
		return speedSets[2];
	}

	public void setLeftHandSpeedSet(Boolean leftHandSpeedSet) {
		this.speedSets[2] = leftHandSpeedSet;
	}

	public Boolean getLeftArmSpeedSet() {
		return speedSets[3];
	}

	public void setLeftArmSpeedSet(Boolean leftArmSpeedSet) {
		this.speedSets[3] = leftArmSpeedSet;
	}

	public Boolean getHeadSpeedSet() {
		return speedSets[4];
	}

	public void setHeadSpeedSet(Boolean headSpeedSet) {
		this.speedSets[4] = headSpeedSet;
	}

	public Boolean getTorsoSpeedSet() {
		return speedSets[5];
	}

	public void setTorsoSpeedSet(Boolean torsoSpeedSet) {
		this.speedSets[5] = torsoSpeedSet;
	}

	public Boolean getRightHandMoveSet() {
		return moveSets[0];
	}

	public void setRightHandMoveSet(Boolean rightHandMoveSet) {
		this.moveSets[0] = rightHandMoveSet;
	}

	public Boolean getRightArmMoveSet() {
		return moveSets[1];
	}

	public void setRightArmMoveSet(Boolean rightArmMoveSet) {
		this.moveSets[1] = rightArmMoveSet;
	}

	public Boolean getLeftHandMoveSet() {
		return moveSets[2];
	}

	public void setLeftHandMoveSet(Boolean leftHandMoveSet) {
		this.moveSets[2] = leftHandMoveSet;
	}

	public Boolean getLeftArmMoveSet() {
		return moveSets[3];
	}

	public void setLeftArmMoveSet(Boolean leftArmMoveSet) {
		this.moveSets[3] = leftArmMoveSet;
	}

	public Boolean getHeadMoveSet() {
		return moveSets[4];
	}

	public void setHeadMoveSet(Boolean headMoveSet) {
		this.moveSets[4] = headMoveSet;
	}

	public Boolean getTorsoMoveSet() {
		return moveSets[5];
	}

	public void setTorsoMoveSet(Boolean torsoMoveSet) {
		this.moveSets[5] = torsoMoveSet;
	}
	public Integer getRightThumbFingerMove() {
		return rightHandMoves[0];
	}
	public void setRightThumbFingerMove(Integer rthumb) {
		this.rightHandMoves[0] = rthumb;
	}
	public Integer getRightIndexFingerMove() {
		return rightHandMoves[1];
	}
	public void setRightIndexFingerMove(Integer rindex) {
		this.rightHandMoves[1] = rindex;
	}
	public Integer getRightMajeureFingerMove() {
		return rightHandMoves[2];
	}
	public void setRightMajeureFingerMove(Integer rmajeure) {
		this.rightHandMoves[2] = rmajeure;
	}
	public Integer getRightRingFingerMove() {
		return rightHandMoves[3];
	}
	public void setRightRingFingerMove(Integer rringfinger) {
		this.rightHandMoves[3] = rringfinger;
	}
	public Integer getRightPinkyFingerMove() {
		return rightHandMoves[4];
	}
	public void setRightPinkyFingerMove(Integer rpinky) {
		this.rightHandMoves[4] = rpinky;
	}
	public Integer getRightWristMove() {
		return rightHandMoves[5];
	}
	public void setRightWristMove(Integer rwrist) {
		this.rightHandMoves[5] = rwrist;
	}
	public Integer getRightBicepsMove() {
		return rightArmMoves[0];
	}
	public void setRightBicepsMove(Integer rbicep) {
		this.rightArmMoves[0] = rbicep;
	}
	public Integer getRightRotateMove() {
		return rightArmMoves[1];
	}
	public void setRightRotateMove(Integer rrotate) {
		this.rightArmMoves[1] = rrotate;
	}
	public Integer getRightShoulderMove() {
		return rightArmMoves[2];
	}
	public void setRightShoulderMove(Integer rshoulder) {
		this.rightArmMoves[2] = rshoulder;
	}
	public Integer getRightOmoplateMove() {
		return rightArmMoves[3];
	}
	public void setRightOmoplateMove(Integer romoplate) {
		this.rightArmMoves[3] = romoplate;
	}
	public Integer getLeftThumbFingerMove() {
		return leftHandMoves[0];
	}
	public void setLeftThumbFingerMove(Integer lthumb) {
		this.leftHandMoves[0] = lthumb;
	}
	public Integer getLeftIndexFingerMove() {
		return leftHandMoves[1];
	}
	public void setLeftIndexFingerMove(Integer lindex) {
		this.leftHandMoves[1] = lindex;
	}
	public Integer getLeftMajeureFingerMove() {
		return leftHandMoves[2];
	}
	public void setLeftMajeureFingerMove(Integer lmajeure) {
		this.leftHandMoves[2] = lmajeure;
	}
	public Integer getLeftRingFingerMove() {
		return leftHandMoves[3];
	}
	public void setLeftRingFingerMove(Integer lringfinger) {
		this.leftHandMoves[3] = lringfinger;
	}
	public Integer getLeftPinkyFingerMove() {
		return leftHandMoves[4];
	}
	public void setLeftPinkyFingerMove(Integer lpinky) {
		this.leftHandMoves[4] = lpinky;
	}
	public Integer getLeftWristMove() {
		return leftHandMoves[5];
	}
	public void setLeftWristMove(Integer lwrist) {
		this.leftHandMoves[5] = lwrist;
	}
	public Integer getLeftBicepsMove() {
		return leftArmMoves[0];
	}
	public void setLeftBicepsMove(Integer lbicep) {
		this.leftArmMoves[0] = lbicep;
	}
	public Integer getLeftRotateMove() {
		return leftArmMoves[1];
	}
	public void setLeftRotateMove(Integer lrotate) {
		this.leftArmMoves[1] = lrotate;
	}
	public Integer getLeftShoulderMove() {
		return leftArmMoves[2];
	}
	public void setLeftShoulderMove(Integer lshoulder) {
		this.leftArmMoves[2] = lshoulder;
	}
	public Integer getLeftOmoplateMove() {
		return leftArmMoves[3];
	}
	public void setLeftOmoplateMove(Integer lomoplate) {
		this.leftArmMoves[3] = lomoplate;
	}
	public Integer getNeckMove() {
		return headMoves[0];
	}
	public void setNeckMove(Integer neck) {
		this.headMoves[0] = neck;
	}
	public Integer getHeadRotateMove() {
		return headMoves[1];
	}
	public void setHeadRotateMove(Integer rothead) {
		this.headMoves[1] = rothead;
	}
	public Integer getEyeXMove() {
		return headMoves[2];
	}
	public void setEyeXMove(Integer eyeX) {
		this.headMoves[2] = eyeX;
	}
	public Integer getEyeYMove() {
		return headMoves[3];
	}
	public void setEyeYMove(Integer eyeY) {
		this.headMoves[3] = eyeY;
	}
	public Integer getJawMove() {
		return headMoves[4];
	}
	public void setJawMove(Integer jaw) {
		this.headMoves[4] = jaw;
	}
	public Integer getTopStomMove() {
		return torsoMoves[0];
	}
	public void setTopStomMove(Integer topStom) {
		this.torsoMoves[0] = topStom;
	}
	public Integer getMidStomMove() {
		return torsoMoves[1];
	}
	public void setMidStomMove(Integer midStom) {
		this.torsoMoves[1] = midStom;
	}
	public Integer getLowStomMove() {
		return torsoMoves[2];
	}

	public void setLowStomMove(Integer lowStom) {
		this.torsoMoves[2] = lowStom;
	}

	public Double getRightThumbFingerSpeed() {
		return rightHandSpeeds[0];
	}

	public void setRightThumbFingerSpeed(Double rthumbspeed) {
		this.rightHandSpeeds[0] = rthumbspeed;
	}

	public Double getRightIndexFingerSpeed() {
		return rightHandSpeeds[1];
	}

	public void setRightIndexFingerSpeed(Double rindexspeed) {
		this.rightHandSpeeds[1] = rindexspeed;
	}

	public Double getRightMajeureFingerSpeed() {
		return rightHandSpeeds[2];
	}

	public void setRightMajeureFingerSpeed(Double rmajeurespeed) {
		this.rightHandSpeeds[2] = rmajeurespeed;
	}

	public Double getRightRingFingerSpeed() {
		return rightHandSpeeds[3];
	}

	public void setRightRingFingerSpeed(Double rringfingerspeed) {
		this.rightHandSpeeds[3] = rringfingerspeed;
	}

	public Double getRightPinkyFingerSpeed() {
		return rightHandSpeeds[4];
	}

	public void setRightPinkyFingerSpeed(Double rpinkyspeed) {
		this.rightHandSpeeds[4] = rpinkyspeed;
	}

	public Double getRightWristSpeed() {
		return rightHandSpeeds[5];
	}

	public void setRightWristSpeed(Double rwristspeed) {
		this.rightHandSpeeds[5] = rwristspeed;
	}

	public Double getRightBicepsSpeed() {
		return rightArmSpeeds[0];
	}

	public void setRightBicepsSpeed(Double rbicepspeed) {
		this.rightArmSpeeds[0] = rbicepspeed;
	}

	public Double getRightRotateSpeed() {
		return rightArmSpeeds[1];
	}

	public void setRightRotateSpeed(Double rrotatespeed) {
		this.rightArmSpeeds[1] = rrotatespeed;
	}

	public Double getRightShoulderSpeed() {
		return rightArmSpeeds[2];
	}

	public void setRightShoulderSpeed(Double rshoulderspeed) {
		this.rightArmSpeeds[2] = rshoulderspeed;
	}

	public Double getRightOmoplateSpeed() {
		return rightArmSpeeds[3];
	}

	public void setRightOmoplateSpeed(Double romoplatespeed) {
		this.rightArmSpeeds[3] = romoplatespeed;
	}

	public Double getLeftThumbFingerSpeed() {
		return leftHandSpeeds[0];
	}

	public void setLeftThumbFingerSpeed(Double lthumbspeed) {
		this.leftHandSpeeds[0] = lthumbspeed;
	}

	public Double getLeftIndexFingerSpeed() {
		return leftHandSpeeds[1];
	}

	public void setLeftIndexFingerSpeed(Double lindexspeed) {
		this.leftHandSpeeds[1] = lindexspeed;
	}

	public Double getLeftMajeureFingerSpeed() {
		return leftHandSpeeds[2];
	}

	public void setLeftMajeureFingerSpeed(Double lmajeurespeed) {
		this.leftHandSpeeds[2] = lmajeurespeed;
	}

	public Double getLeftRingFingerSpeed() {
		return leftHandSpeeds[3];
	}

	public void setLeftRingFingerSpeed(Double lringfingerspeed) {
		this.leftHandSpeeds[3] = lringfingerspeed;
	}

	public Double getLeftPinkyFingerSpeed() {
		return leftHandSpeeds[4];
	}

	public void setLeftPinkyFingerSpeed(Double lpinkyspeed) {
		this.leftHandSpeeds[4] = lpinkyspeed;
	}

	public Double getLeftWristSpeed() {
		return leftHandSpeeds[5];
	}

	public void setLeftWristSpeed(Double lwristspeed) {
		this.leftHandSpeeds[5] = lwristspeed;
	}

	public Double getLeftBicepsSpeed() {
		return leftArmSpeeds[0];
	}

	public void setLeftBicepsSpeed(Double lbicepspeed) {
		this.leftArmSpeeds[0] = lbicepspeed;
	}

	public Double getLeftRotateSpeed() {
		return leftArmSpeeds[1];
	}

	public void setLeftRotateSpeed(Double lrotatespeed) {
		this.leftArmSpeeds[1] = lrotatespeed;
	}

	public Double getLeftShoulderSpeed() {
		return leftArmSpeeds[2];
	}

	public void setLeftShoulderSpeed(Double lshoulderspeed) {
		this.leftArmSpeeds[2] = lshoulderspeed;
	}

	public Double getLeftOmoplateSpeed() {
		return leftArmSpeeds[3];
	}

	public void setLeftOmoplateSpeed(Double lomoplatespeed) {
		this.leftArmSpeeds[3] = lomoplatespeed;
	}

	public Double getNeckSpeed() {
		return headSpeeds[0];
	}

	public void setNeckSpeed(Double neckspeed) {
		this.headSpeeds[0] = neckspeed;
	}

	public Double getHeadRotateSpeed() {
		return headSpeeds[1];
	}

	public void setHeadRotateSpeed(Double rotheadspeed) {
		this.headSpeeds[1] = rotheadspeed;
	}

	public Double getEyeXSpeed() {
		return headSpeeds[2];
	}

	public void setEyeXSpeed(Double eyeXspeed) {
		this.headSpeeds[2] = eyeXspeed;
	}

	public Double getEyeYSpeed() {
		return headSpeeds[3];
	}

	public void setEyeYSpeed(Double eyeYspeed) {
		this.headSpeeds[3] = eyeYspeed;
	}

	public Double getJawSpeed() {
		return headSpeeds[4];
	}

	public void setJawSpeed(Double jawspeed) {
		this.headSpeeds[4] = jawspeed;
	}

	public Double getTopStomSpeed() {
		return torsoSpeeds[0];
	}

	public void setTopStomSpeed(Double topStomspeed) {
		this.torsoSpeeds[0] = topStomspeed;
	}
	public Double getMidStomSpeed() {
		return torsoSpeeds[1];
	}
	public void setMidStomSpeed(Double midStomspeed) {
		this.torsoSpeeds[1] = midStomspeed;
	}
	public Double getLowStomSpeed() {
		return torsoSpeeds[2];
	}
	public void setLowStomSpeed(Double lowStomspeed) {
		this.torsoSpeeds[2] = lowStomspeed;
	}
	public Integer getSleep() {
		return sleep;
	}
	public void setSleep(Integer sleep) {
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
	public Boolean getSpeechSet() {
		return speechSet;
	}
	public void setSpeechSet(Boolean speechSet) {
		this.speechSet = speechSet;
	}
}