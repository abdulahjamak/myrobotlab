package org.myrobotlab.service.model;

import java.io.Serializable;

public class Frame implements Serializable {

	private static final long serialVersionUID = -7038574417962603966L;
	
	public enum FrameType {
	       SPEED, SPEECH, SLEEP, MOVE;
	     };

	private FrameType frameType;  
	     
	private Integer rightThumbFingerMove, rightIndexFingerMove, rightMajeureFingerMove, rightRingFingerMove, rightPinkyFingerMove, rightWristMove;
	private Integer rightBicepsMove, rightRotateMove, rightShoulderMove, rightOmoplateMove;
	private Integer leftThumbFingerMove, leftIndexFingerMove, leftMajeureFingerMove, leftRingFingerMove, leftPinkyFingerMove, leftWristMove;
	private Integer leftBicepsMove, leftRotateMove, leftShoulderMove, leftOmoplateMove;
	private Integer neckMove, headRotateMove, eyeXMove, eyeYMove, jawMove;
	private Integer topStomMove, midStomMove, lowStomMove;
	private Double rightThumbFingerSpeed, rightIndexFingerSpeed, rightMajeureFingerSpeed, rightRingFingerSpeed, rightPinkyFingerSpeed, rightWristSpeed;
	private Double rightBicepsSpeed, rightRotateSpeed, rightShoulderSpeed, rightOmoplateSpeed;
	private Double leftThumbFingerSpeed, leftIndexFingerSpeed, leftMajeureFingerSpeed, leftRingFingerSpeed, leftPinkyFingerSpeed, leftWristSpeed;
	private Double leftBicepsSpeed, leftRotateSpeed, leftShoulderSpeed, leftOmoplateSpeed;
	private Double neckSpeed, headRotateSpeed, eyeXSpeed, eyeYSpeed, jawSpeed;
	private Double topStomSpeed, midStomSpeed, lowStomSpeed;
	private Integer sleep = -1;
	private String speech;
	private String name;

	private Boolean rightHandSpeedSet = false;
	private Boolean rightArmSpeedSet = false;
	private Boolean leftHandSpeedSet = false;
	private Boolean leftArmSpeedSet = false;
	private Boolean headSpeedSet = false;
	private Boolean torsoSpeedSet = false;
	
	private Boolean rightHandMoveSet = true;
	private Boolean rightArmMoveSet = true;
	private Boolean leftHandMoveSet = true;
	private Boolean leftArmMoveSet = true;
	private Boolean headMoveSet = true;
	private Boolean torsoMoveSet = true;

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
				if(rightHandMoveSet) {
					movements.append(this.rightThumbFingerMove).append(SPACE_SYMBOL)
						.append(this.rightIndexFingerMove).append(SPACE_SYMBOL)
						.append(this.rightMajeureFingerMove).append(SPACE_SYMBOL)
						.append(this.rightRingFingerMove).append(SPACE_SYMBOL)
						.append(this.rightPinkyFingerMove).append(SPACE_SYMBOL)
						.append(this.rightWristMove).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// right arm
				if(rightArmMoveSet) {
					movements.append(this.rightBicepsMove).append(SPACE_SYMBOL)
						.append(this.rightRotateMove).append(SPACE_SYMBOL)
						.append(this.rightShoulderMove).append(SPACE_SYMBOL)
						.append(this.rightOmoplateMove).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// left hand
				if(leftHandMoveSet) {
					movements.append(this.leftThumbFingerMove).append(SPACE_SYMBOL)
						.append(this.leftIndexFingerMove).append(SPACE_SYMBOL)
						.append(this.leftMajeureFingerMove).append(SPACE_SYMBOL)
						.append(this.leftRingFingerMove).append(SPACE_SYMBOL)
						.append(this.leftPinkyFingerMove).append(SPACE_SYMBOL)
						.append(this.leftWristMove).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// left arm
				if(leftArmMoveSet) {
					movements.append(this.leftBicepsMove).append(SPACE_SYMBOL)
						.append(this.leftRotateMove).append(SPACE_SYMBOL)
						.append(this.leftShoulderMove).append(SPACE_SYMBOL)
						.append(this.leftOmoplateMove).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL)
						.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// head
				if(headMoveSet) {
					movements.append(this.neckMove).append(SPACE_SYMBOL)
						.append(this.headRotateMove).append(SPACE_SYMBOL)
						.append(this.eyeXMove).append(SPACE_SYMBOL)
						.append(this.eyeYMove).append(SPACE_SYMBOL)
						.append(this.jawMove).append(PIPE_SYMBOL);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// torso
				if(torsoMoveSet) {
					movements.append(this.topStomMove).append(SPACE_SYMBOL)
						.append(this.midStomMove).append(SPACE_SYMBOL)
						.append(this.lowStomMove);
				} else {
					movements.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL);
				}
			return movements.toString();
		} else {
			// speed frame
			StringBuffer speeds = new StringBuffer();
			speeds.append("SPEED").append(" ");
				// right hand
				if(rightHandSpeedSet) {
					speeds.append(this.rightThumbFingerSpeed).append(SPACE_SYMBOL)
						.append(this.rightIndexFingerSpeed).append(SPACE_SYMBOL)
						.append(this.rightMajeureFingerSpeed).append(SPACE_SYMBOL)
						.append(this.rightRingFingerSpeed).append(SPACE_SYMBOL)
						.append(this.rightPinkyFingerSpeed).append(SPACE_SYMBOL)
						.append(this.rightWristSpeed).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// right arm
				if(rightArmSpeedSet) {
					speeds.append(this.rightBicepsSpeed).append(SPACE_SYMBOL)
						.append(this.rightRotateSpeed).append(SPACE_SYMBOL)
						.append(this.rightShoulderSpeed).append(SPACE_SYMBOL)
						.append(this.rightOmoplateSpeed).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// left hand
				if(leftHandSpeedSet) {
					speeds.append(this.leftThumbFingerSpeed).append(SPACE_SYMBOL)
						.append(this.leftIndexFingerSpeed).append(SPACE_SYMBOL)
						.append(this.leftMajeureFingerSpeed).append(SPACE_SYMBOL)
						.append(this.leftRingFingerSpeed).append(SPACE_SYMBOL)
						.append(this.leftPinkyFingerSpeed).append(SPACE_SYMBOL)
						.append(this.leftWristSpeed).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// left arm
				if(leftArmSpeedSet) {
					speeds.append(this.leftBicepsSpeed).append(SPACE_SYMBOL)
						.append(this.leftRotateSpeed).append(SPACE_SYMBOL)
						.append(this.leftShoulderSpeed).append(SPACE_SYMBOL)
						.append(this.leftOmoplateSpeed).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL)
						.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// head
				if(headSpeedSet) {
					speeds.append(this.neckSpeed).append(SPACE_SYMBOL)
						.append(this.headRotateSpeed).append(SPACE_SYMBOL)
						.append(this.eyeXSpeed).append(SPACE_SYMBOL)
						.append(this.eyeYSpeed).append(SPACE_SYMBOL)
						.append(this.jawSpeed).append(PIPE_SYMBOL);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL)
							.append(STAR_SYMBOL).append(STAR_SYMBOL).append(PIPE_SYMBOL);
				}
				// torso
				if(torsoSpeedSet) {
					speeds.append(this.topStomSpeed).append(SPACE_SYMBOL)
						.append(this.midStomSpeed).append(SPACE_SYMBOL)
						.append(this.lowStomSpeed);
				} else {
					speeds.append(STAR_SYMBOL).append(STAR_SYMBOL).append(STAR_SYMBOL);
				}
			return speeds.toString();
		} 
	}

	public void resetValues() {		
		rightHandSpeedSet = false;
		rightArmSpeedSet = false;
		leftHandSpeedSet = false;
		leftArmSpeedSet = false;
		headSpeedSet = false;
		torsoSpeedSet = false;
		
		rightHandMoveSet = true;
		rightArmMoveSet = true;
		leftHandMoveSet = true;
		leftArmMoveSet = true;
		headMoveSet = true;
		torsoMoveSet = true;
		
		this.sleep = -1;

		this.rightThumbFingerMove = 90;
		this.rightIndexFingerMove = 90;
		this.rightMajeureFingerMove = 90;
		this.rightRingFingerMove = 90;
		this.rightPinkyFingerMove = 90;
		this.rightWristMove = 90;

		this.rightBicepsMove = 90;
		this.rightRotateMove = 90;
		this.rightShoulderMove = 90;
		this.rightOmoplateMove = 90;

		this.leftThumbFingerMove = 90;
		this.leftIndexFingerMove = 90;
		this.leftMajeureFingerMove = 90;
		this.leftRingFingerMove = 90;
		this.leftPinkyFingerMove = 90;
		this.leftWristMove = 90;

		this.leftBicepsMove = 90;
		this.leftRotateMove = 90;
		this.leftShoulderMove = 90;
		this.leftOmoplateMove = 90;

		this.neckMove = 90;
		this.headRotateMove = 90;
		this.eyeXMove = 90;
		this.eyeYMove = 90;
		this.jawMove = 90;

		this.topStomMove = 90;
		this.midStomMove = 90;
		this.lowStomMove = 90;
	}

	public Boolean getRightHandSpeedSet() {
		return rightHandSpeedSet;
	}

	public void setRightHandSpeedSet(Boolean rightHandSpeedSet) {
		this.rightHandSpeedSet = rightHandSpeedSet;
	}

	public Boolean getRightArmSpeedSet() {
		return rightArmSpeedSet;
	}

	public void setRightArmSpeedSet(Boolean rightArmSpeedSet) {
		this.rightArmSpeedSet = rightArmSpeedSet;
	}

	public Boolean getLeftHandSpeedSet() {
		return leftHandSpeedSet;
	}

	public void setLeftHandSpeedSet(Boolean leftHandSpeedSet) {
		this.leftHandSpeedSet = leftHandSpeedSet;
	}

	public Boolean getLeftArmSpeedSet() {
		return leftArmSpeedSet;
	}

	public void setLeftArmSpeedSet(Boolean leftArmSpeedSet) {
		this.leftArmSpeedSet = leftArmSpeedSet;
	}

	public Boolean getHeadSpeedSet() {
		return headSpeedSet;
	}

	public void setHeadSpeedSet(Boolean headSpeedSet) {
		this.headSpeedSet = headSpeedSet;
	}

	public Boolean getTorsoSpeedSet() {
		return torsoSpeedSet;
	}

	public void setTorsoSpeedSet(Boolean torsoSpeedSet) {
		this.torsoSpeedSet = torsoSpeedSet;
	}

	public Boolean getRightHandMoveSet() {
		return rightHandMoveSet;
	}

	public void setRightHandMoveSet(Boolean rightHandMoveSet) {
		this.rightHandMoveSet = rightHandMoveSet;
	}

	public Boolean getRightArmMoveSet() {
		return rightArmMoveSet;
	}

	public void setRightArmMoveSet(Boolean rightArmMoveSet) {
		this.rightArmMoveSet = rightArmMoveSet;
	}

	public Boolean getLeftHandMoveSet() {
		return leftHandMoveSet;
	}

	public void setLeftHandMoveSet(Boolean leftHandMoveSet) {
		this.leftHandMoveSet = leftHandMoveSet;
	}

	public Boolean getLeftArmMoveSet() {
		return leftArmMoveSet;
	}

	public void setLeftArmMoveSet(Boolean leftArmMoveSet) {
		this.leftArmMoveSet = leftArmMoveSet;
	}

	public Boolean getHeadMoveSet() {
		return headMoveSet;
	}

	public void setHeadMoveSet(Boolean headMoveSet) {
		this.headMoveSet = headMoveSet;
	}

	public Boolean getTorsoMoveSet() {
		return torsoMoveSet;
	}

	public void setTorsoMoveSet(Boolean torsoMoveSet) {
		this.torsoMoveSet = torsoMoveSet;
	}

	public FrameType getFrameType() {
		return frameType;
	}

	public void setFrameType(FrameType frameType) {
		this.frameType = frameType;
	}

	public Integer getRightThumbFingerMove() {
		return rightThumbFingerMove;
	}

	public void setRightThumbFingerMove(Integer rthumb) {
		this.rightThumbFingerMove = rthumb;
	}

	public Integer getRightIndexFingerMove() {
		return rightIndexFingerMove;
	}

	public void setRightIndexFingerMove(Integer rindex) {
		this.rightIndexFingerMove = rindex;
	}

	public Integer getRightMajeureFingerMove() {
		return rightMajeureFingerMove;
	}

	public void setRightMajeureFingerMove(Integer rmajeure) {
		this.rightMajeureFingerMove = rmajeure;
	}

	public Integer getRightRingFingerMove() {
		return rightRingFingerMove;
	}

	public void setRightRingFingerMove(Integer rringfinger) {
		this.rightRingFingerMove = rringfinger;
	}

	public Integer getRightPinkyFingerMove() {
		return rightPinkyFingerMove;
	}

	public void setRightPinkyFingerMove(Integer rpinky) {
		this.rightPinkyFingerMove = rpinky;
	}

	public Integer getRightWristMove() {
		return rightWristMove;
	}

	public void setRightWristMove(Integer rwrist) {
		this.rightWristMove = rwrist;
	}

	public Integer getRightBicepsMove() {
		return rightBicepsMove;
	}

	public void setRightBicepsMove(Integer rbicep) {
		this.rightBicepsMove = rbicep;
	}

	public Integer getRightRotateMove() {
		return rightRotateMove;
	}

	public void setRightRotateMove(Integer rrotate) {
		this.rightRotateMove = rrotate;
	}

	public Integer getRightShoulderMove() {
		return rightShoulderMove;
	}

	public void setRightShoulderMove(Integer rshoulder) {
		this.rightShoulderMove = rshoulder;
	}

	public Integer getRightOmoplateMove() {
		return rightOmoplateMove;
	}

	public void setRightOmoplateMove(Integer romoplate) {
		this.rightOmoplateMove = romoplate;
	}

	public Integer getLeftThumbFingerMove() {
		return leftThumbFingerMove;
	}

	public void setLeftThumbFingerMove(Integer lthumb) {
		this.leftThumbFingerMove = lthumb;
	}

	public Integer getLeftIndexFingerMove() {
		return leftIndexFingerMove;
	}

	public void setLeftIndexFingerMove(Integer lindex) {
		this.leftIndexFingerMove = lindex;
	}

	public Integer getLeftMajeureFingerMove() {
		return leftMajeureFingerMove;
	}

	public void setLeftMajeureFingerMove(Integer lmajeure) {
		this.leftMajeureFingerMove = lmajeure;
	}

	public Integer getLeftRingFingerMove() {
		return leftRingFingerMove;
	}

	public void setLeftRingFingerMove(Integer lringfinger) {
		this.leftRingFingerMove = lringfinger;
	}

	public Integer getLeftPinkyFingerMove() {
		return leftPinkyFingerMove;
	}

	public void setLeftPinkyFingerMove(Integer lpinky) {
		this.leftPinkyFingerMove = lpinky;
	}

	public Integer getLeftWristMove() {
		return leftWristMove;
	}

	public void setLeftWristMove(Integer lwrist) {
		this.leftWristMove = lwrist;
	}

	public Integer getLeftBicepsMove() {
		return leftBicepsMove;
	}

	public void setLeftBicepsMove(Integer lbicep) {
		this.leftBicepsMove = lbicep;
	}

	public Integer getLeftRotateMove() {
		return leftRotateMove;
	}

	public void setLeftRotateMove(Integer lrotate) {
		this.leftRotateMove = lrotate;
	}

	public Integer getLeftShoulderMove() {
		return leftShoulderMove;
	}

	public void setLeftShoulderMove(Integer lshoulder) {
		this.leftShoulderMove = lshoulder;
	}

	public Integer getLeftOmoplateMove() {
		return leftOmoplateMove;
	}

	public void setLeftOmoplateMove(Integer lomoplate) {
		this.leftOmoplateMove = lomoplate;
	}

	public Integer getNeckMove() {
		return neckMove;
	}

	public void setNeckMove(Integer neck) {
		this.neckMove = neck;
	}

	public Integer getHeadRotateMove() {
		return headRotateMove;
	}

	public void setHeadRotateMove(Integer rothead) {
		this.headRotateMove = rothead;
	}

	public Integer getEyeXMove() {
		return eyeXMove;
	}

	public void setEyeXMove(Integer eyeX) {
		this.eyeXMove = eyeX;
	}

	public Integer getEyeYMove() {
		return eyeYMove;
	}

	public void setEyeYMove(Integer eyeY) {
		this.eyeYMove = eyeY;
	}

	public Integer getJawMove() {
		return jawMove;
	}

	public void setJawMove(Integer jaw) {
		this.jawMove = jaw;
	}

	public Integer getTopStomMove() {
		return topStomMove;
	}

	public void setTopStomMove(Integer topStom) {
		this.topStomMove = topStom;
	}

	public Integer getMidStomMove() {
		return midStomMove;
	}

	public void setMidStomMove(Integer midStom) {
		this.midStomMove = midStom;
	}

	public Integer getLowStomMove() {
		return lowStomMove;
	}

	public void setLowStomMove(Integer lowStom) {
		this.lowStomMove = lowStom;
	}

	public Double getRightThumbFingerSpeed() {
		return rightThumbFingerSpeed;
	}

	public void setRightThumbFingerSpeed(Double rthumbspeed) {
		this.rightThumbFingerSpeed = rthumbspeed;
	}

	public Double getRightIndexFingerSpeed() {
		return rightIndexFingerSpeed;
	}

	public void setRightIndexFingerSpeed(Double rindexspeed) {
		this.rightIndexFingerSpeed = rindexspeed;
	}

	public Double getRightMajeureFingerSpeed() {
		return rightMajeureFingerSpeed;
	}

	public void setRightMajeureFingerSpeed(Double rmajeurespeed) {
		this.rightMajeureFingerSpeed = rmajeurespeed;
	}

	public Double getRightRingFingerSpeed() {
		return rightRingFingerSpeed;
	}

	public void setRightRingFingerSpeed(Double rringfingerspeed) {
		this.rightRingFingerSpeed = rringfingerspeed;
	}

	public Double getRightPinkyFingerSpeed() {
		return rightPinkyFingerSpeed;
	}

	public void setRightPinkyFingerSpeed(Double rpinkyspeed) {
		this.rightPinkyFingerSpeed = rpinkyspeed;
	}

	public Double getRightWristSpeed() {
		return rightWristSpeed;
	}

	public void setRightWristSpeed(Double rwristspeed) {
		this.rightWristSpeed = rwristspeed;
	}

	public Double getRightBicepsSpeed() {
		return rightBicepsSpeed;
	}

	public void setRightBicepsSpeed(Double rbicepspeed) {
		this.rightBicepsSpeed = rbicepspeed;
	}

	public Double getRightRotateSpeed() {
		return rightRotateSpeed;
	}

	public void setRightRotateSpeed(Double rrotatespeed) {
		this.rightRotateSpeed = rrotatespeed;
	}

	public Double getRightShoulderSpeed() {
		return rightShoulderSpeed;
	}

	public void setRightShoulderSpeed(Double rshoulderspeed) {
		this.rightShoulderSpeed = rshoulderspeed;
	}

	public Double getRightOmoplateSpeed() {
		return rightOmoplateSpeed;
	}

	public void setRightOmoplateSpeed(Double romoplatespeed) {
		this.rightOmoplateSpeed = romoplatespeed;
	}

	public Double getLeftThumbFingerSpeed() {
		return leftThumbFingerSpeed;
	}

	public void setLeftThumbFingerSpeed(Double lthumbspeed) {
		this.leftThumbFingerSpeed = lthumbspeed;
	}

	public Double getLeftIndexFingerSpeed() {
		return leftIndexFingerSpeed;
	}

	public void setLeftIndexFingerSpeed(Double lindexspeed) {
		this.leftIndexFingerSpeed = lindexspeed;
	}

	public Double getLeftMajeureFingerSpeed() {
		return leftMajeureFingerSpeed;
	}

	public void setLeftMajeureFingerSpeed(Double lmajeurespeed) {
		this.leftMajeureFingerSpeed = lmajeurespeed;
	}

	public Double getLeftRingFingerSpeed() {
		return leftRingFingerSpeed;
	}

	public void setLeftRingFingerSpeed(Double lringfingerspeed) {
		this.leftRingFingerSpeed = lringfingerspeed;
	}

	public Double getLeftPinkyFingerSpeed() {
		return leftPinkyFingerSpeed;
	}

	public void setLeftPinkyFingerSpeed(Double lpinkyspeed) {
		this.leftPinkyFingerSpeed = lpinkyspeed;
	}

	public Double getLeftWristSpeed() {
		return leftWristSpeed;
	}

	public void setLeftWristSpeed(Double lwristspeed) {
		this.leftWristSpeed = lwristspeed;
	}

	public Double getLeftBicepsSpeed() {
		return leftBicepsSpeed;
	}

	public void setLeftBicepsSpeed(Double lbicepspeed) {
		this.leftBicepsSpeed = lbicepspeed;
	}

	public Double getLeftRotateSpeed() {
		return leftRotateSpeed;
	}

	public void setLeftRotateSpeed(Double lrotatespeed) {
		this.leftRotateSpeed = lrotatespeed;
	}

	public Double getLeftShoulderSpeed() {
		return leftShoulderSpeed;
	}

	public void setLeftShoulderSpeed(Double lshoulderspeed) {
		this.leftShoulderSpeed = lshoulderspeed;
	}

	public Double getLeftOmoplateSpeed() {
		return leftOmoplateSpeed;
	}

	public void setLeftOmoplateSpeed(Double lomoplatespeed) {
		this.leftOmoplateSpeed = lomoplatespeed;
	}

	public Double getNeckSpeed() {
		return neckSpeed;
	}

	public void setNeckSpeed(Double neckspeed) {
		this.neckSpeed = neckspeed;
	}

	public Double getHeadRotateSpeed() {
		return headRotateSpeed;
	}

	public void setHeadRotateSpeed(Double rotheadspeed) {
		this.headRotateSpeed = rotheadspeed;
	}

	public Double getEyeXSpeed() {
		return eyeXSpeed;
	}

	public void setEyeXSpeed(Double eyeXspeed) {
		this.eyeXSpeed = eyeXspeed;
	}

	public Double getEyeYSpeed() {
		return eyeYSpeed;
	}

	public void setEyeYSpeed(Double eyeYspeed) {
		this.eyeYSpeed = eyeYspeed;
	}

	public Double getJawSpeed() {
		return jawSpeed;
	}

	public void setJawSpeed(Double jawspeed) {
		this.jawSpeed = jawspeed;
	}

	public Double getTopStomSpeed() {
		return topStomSpeed;
	}

	public void setTopStomSpeed(Double topStomspeed) {
		this.topStomSpeed = topStomspeed;
	}
	public Double getMidStomSpeed() {
		return midStomSpeed;
	}
	public void setMidStomSpeed(Double midStomspeed) {
		this.midStomSpeed = midStomspeed;
	}
	public Double getLowStomSpeed() {
		return lowStomSpeed;
	}
	public void setLowStomSpeed(Double lowStomspeed) {
		this.lowStomSpeed = lowStomspeed;
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