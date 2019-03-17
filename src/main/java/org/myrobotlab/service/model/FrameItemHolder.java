package org.myrobotlab.service.model;

import java.io.Serializable;

public class FrameItemHolder implements Serializable {

	private static final long serialVersionUID = -7038574417962603966L;
	
	public enum FrameType {
	       SPEED, SPEECH, SLEEP, MOVE;
	     };

	private FrameType frameType;  
	     
	private int rightThumbFingerMove, rightIndexFingerMove, rightMajeureFingerMove, rightRingFingerMove, rightPinkyFingerMove, rightWristMove;
	private int rightBicepsMove, rightRotateMove, rightShoulderMove, rightOmoplateMove;
	private int leftThumbFingerMove, leftIndexFingerMove, leftMajeureFingerMove, leftRingFingerMove, leftPinkyFingerMove, leftWristMove;
	private int leftBicepsMove, leftRotateMove, leftShoulderMove, leftOmoplateMove;
	private int neckMove, headRotateMove, eyeXMove, eyeYMove, jawMove;
	private int topStomMove, midStomMove, lowStomMove;
	private double rightThumbFingerSpeed, rightIndexFingerSpeed, rightMajeureFingerSpeed, rightRingFingerSpeed, rightPinkyFingerSpeed, rightWristSpeed;
	private double rightBicepsSpeed, rightRotateSpeed, rightShoulderSpeed, rightOmoplateSpeed;
	private double leftThumbFingerSpeed, leftIndexFingerSpeed, leftMajeureFingerSpeed, leftRingFingerSpeed, leftPinkyFingerSpeed, leftWristSpeed;
	private double leftBicepsSpeed, leftRotateSpeed, leftShoulderSpeed, leftOmoplateSpeed;
	private double neckSpeed, headRotateSpeed, eyeXSpeed, eyeYSpeed, jawSpeed;
	private double topStomSpeed, midStomSpeed, lowStomSpeed;
	private int sleep = -1;
	private String speech;
	private String name;

	private boolean rightHandSpeedSet = false;
	private boolean rightArmSpeedSet = false;
	private boolean leftHandSpeedSet = false;
	private boolean leftArmSpeedSet = false;
	private boolean headSpeedSet = false;
	private boolean torsoSpeedSet = false;
	
	private boolean rightHandMoveSet = true;
	private boolean rightArmMoveSet = true;
	private boolean leftHandMoveSet = true;
	private boolean leftArmMoveSet = true;
	private boolean headMoveSet = true;
	private boolean torsoMoveSet = true;

	private static final String STAR_SYMBOL = "* ";
	private static final String SPACE_SYMBOL = " ";
	private static final String PIPE_SYMBOL = " | ";

	public FrameItemHolder(FrameType frameType) {
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

	public boolean getRightHandSpeedSet() {
		return rightHandSpeedSet;
	}

	public void setRightHandSpeedSet(boolean rightHandSpeedSet) {
		this.rightHandSpeedSet = rightHandSpeedSet;
	}

	public boolean getRightArmSpeedSet() {
		return rightArmSpeedSet;
	}

	public void setRightArmSpeedSet(boolean rightArmSpeedSet) {
		this.rightArmSpeedSet = rightArmSpeedSet;
	}

	public boolean getLeftHandSpeedSet() {
		return leftHandSpeedSet;
	}

	public void setLeftHandSpeedSet(boolean leftHandSpeedSet) {
		this.leftHandSpeedSet = leftHandSpeedSet;
	}

	public boolean getLeftArmSpeedSet() {
		return leftArmSpeedSet;
	}

	public void setLeftArmSpeedSet(boolean leftArmSpeedSet) {
		this.leftArmSpeedSet = leftArmSpeedSet;
	}

	public boolean getHeadSpeedSet() {
		return headSpeedSet;
	}

	public void setHeadSpeedSet(boolean headSpeedSet) {
		this.headSpeedSet = headSpeedSet;
	}

	public boolean getTorsoSpeedSet() {
		return torsoSpeedSet;
	}

	public void setTorsoSpeedSet(boolean torsoSpeedSet) {
		this.torsoSpeedSet = torsoSpeedSet;
	}

	public boolean getRightHandMoveSet() {
		return rightHandMoveSet;
	}

	public void setRightHandMoveSet(boolean rightHandMoveSet) {
		this.rightHandMoveSet = rightHandMoveSet;
	}

	public boolean getRightArmMoveSet() {
		return rightArmMoveSet;
	}

	public void setRightArmMoveSet(boolean rightArmMoveSet) {
		this.rightArmMoveSet = rightArmMoveSet;
	}

	public boolean getLeftHandMoveSet() {
		return leftHandMoveSet;
	}

	public void setLeftHandMoveSet(boolean leftHandMoveSet) {
		this.leftHandMoveSet = leftHandMoveSet;
	}

	public boolean getLeftArmMoveSet() {
		return leftArmMoveSet;
	}

	public void setLeftArmMoveSet(boolean leftArmMoveSet) {
		this.leftArmMoveSet = leftArmMoveSet;
	}

	public boolean getHeadMoveSet() {
		return headMoveSet;
	}

	public void setHeadMoveSet(boolean headMoveSet) {
		this.headMoveSet = headMoveSet;
	}

	public boolean getTorsoMoveSet() {
		return torsoMoveSet;
	}

	public void setTorsoMoveSet(boolean torsoMoveSet) {
		this.torsoMoveSet = torsoMoveSet;
	}

	public FrameType getFrameType() {
		return frameType;
	}

	public void setFrameType(FrameType frameType) {
		this.frameType = frameType;
	}

//	public boolean[] getMovesSet() {
//		return movesSet;
//	}

	public int getRthumb() {
		return rightThumbFingerMove;
	}

	public void setRthumb(int rthumb) {
		this.rightThumbFingerMove = rthumb;
	}

	public int getRindex() {
		return rightIndexFingerMove;
	}

	public void setRindex(int rindex) {
		this.rightIndexFingerMove = rindex;
	}

	public int getRmajeure() {
		return rightMajeureFingerMove;
	}

	public void setRmajeure(int rmajeure) {
		this.rightMajeureFingerMove = rmajeure;
	}

	public int getRringfinger() {
		return rightRingFingerMove;
	}

	public void setRringfinger(int rringfinger) {
		this.rightRingFingerMove = rringfinger;
	}

	public int getRpinky() {
		return rightPinkyFingerMove;
	}

	public void setRpinky(int rpinky) {
		this.rightPinkyFingerMove = rpinky;
	}

	public int getRwrist() {
		return rightWristMove;
	}

	public void setRwrist(int rwrist) {
		this.rightWristMove = rwrist;
	}

	public int getRbicep() {
		return rightBicepsMove;
	}

	public void setRbicep(int rbicep) {
		this.rightBicepsMove = rbicep;
	}

	public int getRrotate() {
		return rightRotateMove;
	}

	public void setRrotate(int rrotate) {
		this.rightRotateMove = rrotate;
	}

	public int getRshoulder() {
		return rightShoulderMove;
	}

	public void setRshoulder(int rshoulder) {
		this.rightShoulderMove = rshoulder;
	}

	public int getRomoplate() {
		return rightOmoplateMove;
	}

	public void setRomoplate(int romoplate) {
		this.rightOmoplateMove = romoplate;
	}

	public int getLthumb() {
		return leftThumbFingerMove;
	}

	public void setLthumb(int lthumb) {
		this.leftThumbFingerMove = lthumb;
	}

	public int getLindex() {
		return leftIndexFingerMove;
	}

	public void setLindex(int lindex) {
		this.leftIndexFingerMove = lindex;
	}

	public int getLmajeure() {
		return leftMajeureFingerMove;
	}

	public void setLmajeure(int lmajeure) {
		this.leftMajeureFingerMove = lmajeure;
	}

	public int getLringfinger() {
		return leftRingFingerMove;
	}

	public void setLringfinger(int lringfinger) {
		this.leftRingFingerMove = lringfinger;
	}

	public int getLpinky() {
		return leftPinkyFingerMove;
	}

	public void setLpinky(int lpinky) {
		this.leftPinkyFingerMove = lpinky;
	}

	public int getLwrist() {
		return leftWristMove;
	}

	public void setLwrist(int lwrist) {
		this.leftWristMove = lwrist;
	}

	public int getLbicep() {
		return leftBicepsMove;
	}

	public void setLbicep(int lbicep) {
		this.leftBicepsMove = lbicep;
	}

	public int getLrotate() {
		return leftRotateMove;
	}

	public void setLrotate(int lrotate) {
		this.leftRotateMove = lrotate;
	}

	public int getLshoulder() {
		return leftShoulderMove;
	}

	public void setLshoulder(int lshoulder) {
		this.leftShoulderMove = lshoulder;
	}

	public int getLomoplate() {
		return leftOmoplateMove;
	}

	public void setLomoplate(int lomoplate) {
		this.leftOmoplateMove = lomoplate;
	}

	public int getNeck() {
		return neckMove;
	}

	public void setNeck(int neck) {
		this.neckMove = neck;
	}

	public int getRothead() {
		return headRotateMove;
	}

	public void setRothead(int rothead) {
		this.headRotateMove = rothead;
	}

	public int getEyeX() {
		return eyeXMove;
	}

	public void setEyeX(int eyeX) {
		this.eyeXMove = eyeX;
	}

	public int getEyeY() {
		return eyeYMove;
	}

	public void setEyeY(int eyeY) {
		this.eyeYMove = eyeY;
	}

	public int getJaw() {
		return jawMove;
	}

	public void setJaw(int jaw) {
		this.jawMove = jaw;
	}

	public int getTopStom() {
		return topStomMove;
	}

	public void setTopStom(int topStom) {
		this.topStomMove = topStom;
	}

	public int getMidStom() {
		return midStomMove;
	}

	public void setMidStom(int midStom) {
		this.midStomMove = midStom;
	}

	public int getLowStom() {
		return lowStomMove;
	}

	public void setLowStom(int lowStom) {
		this.lowStomMove = lowStom;
	}

	public double getRthumbspeed() {
		return rightThumbFingerSpeed;
	}

	public void setRthumbspeed(double rthumbspeed) {
		this.rightThumbFingerSpeed = rthumbspeed;
	}

	public double getRindexspeed() {
		return rightIndexFingerSpeed;
	}

	public void setRindexspeed(double rindexspeed) {
		this.rightIndexFingerSpeed = rindexspeed;
	}

	public double getRmajeurespeed() {
		return rightMajeureFingerSpeed;
	}

	public void setRmajeurespeed(double rmajeurespeed) {
		this.rightMajeureFingerSpeed = rmajeurespeed;
	}

	public double getRringfingerspeed() {
		return rightRingFingerSpeed;
	}

	public void setRringfingerspeed(double rringfingerspeed) {
		this.rightRingFingerSpeed = rringfingerspeed;
	}

	public double getRpinkyspeed() {
		return rightPinkyFingerSpeed;
	}

	public void setRpinkyspeed(double rpinkyspeed) {
		this.rightPinkyFingerSpeed = rpinkyspeed;
	}

	public double getRwristspeed() {
		return rightWristSpeed;
	}

	public void setRwristspeed(double rwristspeed) {
		this.rightWristSpeed = rwristspeed;
	}

	public double getRbicepspeed() {
		return rightBicepsSpeed;
	}

	public void setRbicepspeed(double rbicepspeed) {
		this.rightBicepsSpeed = rbicepspeed;
	}

	public double getRrotatespeed() {
		return rightRotateSpeed;
	}

	public void setRrotatespeed(double rrotatespeed) {
		this.rightRotateSpeed = rrotatespeed;
	}

	public double getRshoulderspeed() {
		return rightShoulderSpeed;
	}

	public void setRshoulderspeed(double rshoulderspeed) {
		this.rightShoulderSpeed = rshoulderspeed;
	}

	public double getRomoplatespeed() {
		return rightOmoplateSpeed;
	}

	public void setRomoplatespeed(double romoplatespeed) {
		this.rightOmoplateSpeed = romoplatespeed;
	}

	public double getLthumbspeed() {
		return leftThumbFingerSpeed;
	}

	public void setLthumbspeed(double lthumbspeed) {
		this.leftThumbFingerSpeed = lthumbspeed;
	}

	public double getLindexspeed() {
		return leftIndexFingerSpeed;
	}

	public void setLindexspeed(double lindexspeed) {
		this.leftIndexFingerSpeed = lindexspeed;
	}

	public double getLmajeurespeed() {
		return leftMajeureFingerSpeed;
	}

	public void setLmajeurespeed(double lmajeurespeed) {
		this.leftMajeureFingerSpeed = lmajeurespeed;
	}

	public double getLringfingerspeed() {
		return leftRingFingerSpeed;
	}

	public void setLringfingerspeed(double lringfingerspeed) {
		this.leftRingFingerSpeed = lringfingerspeed;
	}

	public double getLpinkyspeed() {
		return leftPinkyFingerSpeed;
	}

	public void setLpinkyspeed(double lpinkyspeed) {
		this.leftPinkyFingerSpeed = lpinkyspeed;
	}

	public double getLwristspeed() {
		return leftWristSpeed;
	}

	public void setLwristspeed(double lwristspeed) {
		this.leftWristSpeed = lwristspeed;
	}

	public double getLbicepspeed() {
		return leftBicepsSpeed;
	}

	public void setLbicepspeed(double lbicepspeed) {
		this.leftBicepsSpeed = lbicepspeed;
	}

	public double getLrotatespeed() {
		return leftRotateSpeed;
	}

	public void setLrotatespeed(double lrotatespeed) {
		this.leftRotateSpeed = lrotatespeed;
	}

	public double getLshoulderspeed() {
		return leftShoulderSpeed;
	}

	public void setLshoulderspeed(double lshoulderspeed) {
		this.leftShoulderSpeed = lshoulderspeed;
	}

	public double getLomoplatespeed() {
		return leftOmoplateSpeed;
	}

	public void setLomoplatespeed(double lomoplatespeed) {
		this.leftOmoplateSpeed = lomoplatespeed;
	}

	public double getNeckspeed() {
		return neckSpeed;
	}

	public void setNeckspeed(double neckspeed) {
		this.neckSpeed = neckspeed;
	}

	public double getRotheadspeed() {
		return headRotateSpeed;
	}

	public void setRotheadspeed(double rotheadspeed) {
		this.headRotateSpeed = rotheadspeed;
	}

	public double getEyeXspeed() {
		return eyeXSpeed;
	}

	public void setEyeXspeed(double eyeXspeed) {
		this.eyeXSpeed = eyeXspeed;
	}

	public double getEyeYspeed() {
		return eyeYSpeed;
	}

	public void setEyeYspeed(double eyeYspeed) {
		this.eyeYSpeed = eyeYspeed;
	}

	public double getJawspeed() {
		return jawSpeed;
	}

	public void setJawspeed(double jawspeed) {
		this.jawSpeed = jawspeed;
	}

	public double getTopStomspeed() {
		return topStomSpeed;
	}

	public void setTopStomspeed(double topStomspeed) {
		this.topStomSpeed = topStomspeed;
	}

	public double getMidStomspeed() {
		return midStomSpeed;
	}

	public void setMidStomspeed(double midStomspeed) {
		this.midStomSpeed = midStomspeed;
	}

	public double getLowStomspeed() {
		return lowStomSpeed;
	}

	public void setLowStomspeed(double lowStomspeed) {
		this.lowStomSpeed = lowStomspeed;
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
