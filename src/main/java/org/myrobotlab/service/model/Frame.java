package org.myrobotlab.service.model;

import java.io.Serializable;
import java.util.Arrays;

public class Frame implements Serializable {

	private static final long serialVersionUID = -7038574417962603966L;
	
	public enum FrameType {
	       SPEED, SPEECH, SLEEP, MOVE;
	     };

	private FrameType frameType;  
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
	final private Integer[] rightArmMoves = new Integer[6];
	final private Integer[] leftHandMoves = new Integer[6];
	final private Integer[] lefttArmMoves = new Integer[6];
	/**
	 * 0: neck
	 * 1: head
	 * 2: eyeX
	 * 3: eyeY
	 * 4: jaw
	 */
	final private Integer[] headMoves = new Integer[6];
	/**
	 * 0: top
	 * 1: mid
	 * 2: low
	 */
	final private Integer[] torsoMoves = new Integer[6];
	// speeds
	final private Double[] rightHandSpeeds = new Double[6];
	final private Double[] rightArmSpeeds = new Double[6];
	final private Double[] leftHandSpeeds = new Double[6];
	final private Double[] leftArmSpeeds = new Double[6];
	final private Double[] headSpeeds = new Double[6];
	final private Double[] torsoSpeeds = new Double[6];
	
	private Integer sleep = -1;
	private String speech;
	private String name;

	private static final String STAR_SYMBOL = "* ";
	private static final String SPACE_SYMBOL = " ";
	private static final String PIPE_SYMBOL = " | ";

	public Frame(FrameType frameType) {
		this.frameType = frameType;
		resetValues();
	}

	@Override
	public String toString() {
		if (this.frameType == FrameType.SLEEP) {
			// sleep frame
			return "SLEEP "+this.sleep;
		} else if (this.frameType == FrameType.SPEECH) {
			// speech frame
			return "SPEECH "+this.speech;
		} else if (this.frameType == FrameType.MOVE) {
			// move frame
			StringBuffer movements = new StringBuffer();
			movements.append(this.name).append(": ");
				// right hand
				if(moveSets[0]) {
					movements.append(this.rightHandMoves[0]).append(SPACE_SYMBOL)
						.append(this.rightHandMoves[1]).append(SPACE_SYMBOL)
						.append(this.rightHandMoves[2]).append(SPACE_SYMBOL)
						.append(this.rightHandMoves[3]).append(SPACE_SYMBOL)
						.append(this.rightHandMoves[4]).append(SPACE_SYMBOL)
						.append(this.rightHandMoves[5]).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// right arm
				if(moveSets[1]) {
					movements.append(this.rightArmMoves[0]).append(SPACE_SYMBOL)
						.append(this.rightArmMoves[1]).append(SPACE_SYMBOL)
						.append(this.rightArmMoves[2]).append(SPACE_SYMBOL)
						.append(this.rightArmMoves[3]).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// left hand
				if(moveSets[2]) {
					movements.append(this.leftHandMoves[0]).append(SPACE_SYMBOL)
						.append(this.leftHandMoves[1]).append(SPACE_SYMBOL)
						.append(this.leftHandMoves[2]).append(SPACE_SYMBOL)
						.append(this.leftHandMoves[3]).append(SPACE_SYMBOL)
						.append(this.leftHandMoves[4]).append(SPACE_SYMBOL)
						.append(this.leftHandMoves[5]).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// left arm
				if(moveSets[3]) {
					movements.append(this.lefttArmMoves[0]).append(SPACE_SYMBOL)
						.append(this.lefttArmMoves[1]).append(SPACE_SYMBOL)
						.append(this.lefttArmMoves[2]).append(SPACE_SYMBOL)
						.append(this.lefttArmMoves[3]).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL)
						.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// head
				if(moveSets[4]) {
					movements.append(this.headMoves[0]).append(SPACE_SYMBOL)
						.append(this.headMoves[1]).append(SPACE_SYMBOL)
						.append(this.headMoves[2]).append(SPACE_SYMBOL)
						.append(this.headMoves[3]).append(SPACE_SYMBOL)
						.append(this.headMoves[4]).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// torso
				if(moveSets[5]) {
					movements.append(this.torsoMoves[0]).append(SPACE_SYMBOL)
						.append(this.torsoMoves[1]).append(SPACE_SYMBOL)
						.append(this.torsoMoves[2]);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL);
				}
			return movements.toString();
		} else {
			// speed frame
			StringBuffer speeds = new StringBuffer();
			speeds.append("SPEED").append(" ");
				// right hand
				if(speedSets[0]) {
					speeds.append(this.rightHandSpeeds[0]).append(SPACE_SYMBOL)
						.append(this.rightHandSpeeds[1]).append(SPACE_SYMBOL)
						.append(this.rightHandSpeeds[2]).append(SPACE_SYMBOL)
						.append(this.rightHandSpeeds[3]).append(SPACE_SYMBOL)
						.append(this.rightHandSpeeds[4]).append(SPACE_SYMBOL)
						.append(this.rightHandSpeeds[5]).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// right arm
				if(speedSets[1]) {
					speeds.append(this.rightArmSpeeds[0]).append(SPACE_SYMBOL)
						.append(this.rightArmSpeeds[1]).append(SPACE_SYMBOL)
						.append(this.rightArmSpeeds[2]).append(SPACE_SYMBOL)
						.append(this.rightArmSpeeds[3]).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// left hand
				if(speedSets[2]) {
					speeds.append(this.leftHandSpeeds[0]).append(SPACE_SYMBOL)
						.append(this.leftHandSpeeds[1]).append(SPACE_SYMBOL)
						.append(this.leftHandSpeeds[2]).append(SPACE_SYMBOL)
						.append(this.leftHandSpeeds[3]).append(SPACE_SYMBOL)
						.append(this.leftHandSpeeds[4]).append(SPACE_SYMBOL)
						.append(this.leftHandSpeeds[5]).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// left arm
				if(speedSets[3]) {
					speeds.append(this.leftArmSpeeds[0]).append(SPACE_SYMBOL)
						.append(this.leftArmSpeeds[1]).append(SPACE_SYMBOL)
						.append(this.leftArmSpeeds[2]).append(SPACE_SYMBOL)
						.append(this.leftArmSpeeds[3]).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL)
						.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// head
				if(speedSets[4]) {
					speeds.append(this.headSpeeds[0]).append(SPACE_SYMBOL)
						.append(this.headSpeeds[1]).append(SPACE_SYMBOL)
						.append(this.headSpeeds[2]).append(SPACE_SYMBOL)
						.append(this.headSpeeds[3]).append(SPACE_SYMBOL)
						.append(this.headSpeeds[4]).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// torso
				if(speedSets[5]) {
					speeds.append(this.torsoSpeeds[0]).append(SPACE_SYMBOL)
						.append(this.torsoSpeeds[1]).append(SPACE_SYMBOL)
						.append(this.torsoSpeeds[2]);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL);
				}
			return speeds.toString();
		} 
	}

	public void resetValues() {	
		Arrays.fill(moveSets, true);
		Arrays.fill(speedSets, false);

		Arrays.fill(rightHandMoves, 90);
		Arrays.fill(rightArmMoves, 90);
		Arrays.fill(leftHandMoves, 90);
		Arrays.fill(lefttArmMoves, 90);
		Arrays.fill(headMoves, 90);
		Arrays.fill(torsoMoves, 90);
		
		Arrays.fill(rightHandSpeeds, 0d);
		Arrays.fill(rightArmSpeeds, 0d);
		Arrays.fill(leftHandSpeeds, 0d);
		Arrays.fill(leftArmSpeeds, 0d);
		Arrays.fill(headSpeeds, 0d);
		Arrays.fill(torsoSpeeds, 0d);
		
		this.sleep = -1;
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

	public FrameType getFrameType() {
		return frameType;
	}

	public void setFrameType(FrameType frameType) {
		this.frameType = frameType;
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
		return lefttArmMoves[0];
	}

	public void setLeftBicepsMove(Integer lbicep) {
		this.lefttArmMoves[0] = lbicep;
	}

	public Integer getLeftRotateMove() {
		return lefttArmMoves[1];
	}

	public void setLeftRotateMove(Integer lrotate) {
		this.lefttArmMoves[1] = lrotate;
	}

	public Integer getLeftShoulderMove() {
		return lefttArmMoves[2];
	}

	public void setLeftShoulderMove(Integer lshoulder) {
		this.lefttArmMoves[2] = lshoulder;
	}

	public Integer getLeftOmoplateMove() {
		return lefttArmMoves[3];
	}

	public void setLeftOmoplateMove(Integer lomoplate) {
		this.lefttArmMoves[3] = lomoplate;
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
}