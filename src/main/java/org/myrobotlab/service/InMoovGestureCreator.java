package org.myrobotlab.service;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang3.SerializationUtils;
import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.ServiceType;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.model.Frame;
import org.myrobotlab.service.model.Frame.RobotSection;
import org.myrobotlab.service.model.Gesture;
import org.slf4j.Logger;

/**
 * InMoovGestureCreator - This is a helper service to create gestures for the
 * InMoov It has a swing based gui that allows you to set servo angles on the
 * InMoov to create new gestures.
 *
 * @author LunDev (github), Ma. Vo. (MyRobotlab)
 */
public class InMoovGestureCreator extends Service {

	private static final long serialVersionUID = -188229444384862246L;

	private final static Logger LOGGER = LoggerFactory.getLogger(InMoovGestureCreator.class);

	private static final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
			.getNumberInstance(Locale.getDefault());

	private final Gesture gesture = new Gesture();
	private final List<Frame> frames = gesture.getFrames();

	private final List<File> scriptFiles = new ArrayList<File>();
	
	private InMoov i01;
	private boolean moveRealTime = false;
	private String referencename;

	public InMoovGestureCreator(String n) {
		super(n);
		// intializing variables
//		servoitemholder = new ServoItemHolder[6][];
//		pythonitemholder = new ArrayList<PythonItemHolder>();
		decimalFormat.setGroupingUsed(false);
	}

	/**
	 * This static method returns all the details of the class without it having to
	 * be constructed. It has description, categories, dependencies, and peer
	 * definitions.
	 * 
	 * @return ServiceType - returns all the data
	 * 
	 */
	static public ServiceType getMetaData() {
		ServiceType meta = new ServiceType(InMoovGestureCreator.class.getCanonicalName());
		meta.addDescription("an easier way to create gestures for InMoov");
		meta.addCategory("robot");
		return meta;
	}
	
	public void clearGestureAndSelectedFrame(JList<String> frameList, 
			JPanel bottomTop, 
			JPanel top, 
			JTextField gestureName,
			Map<RobotSection, JPanel> robotSectionMovePanels, 
			Map<RobotSection, JPanel> robotSectionSlidersPanels,
			Map<RobotSection, JPanel> robotSectionSpeedPanels,
			Map<RobotSection, JPanel> robotSectionSpeedNumberBoxesPanels) {
		this.gesture.setGestureName(null);
		this.gesture.setGestureFile(null);
		gestureName.setText("Gesture name here");
		this.frames.clear();
		frameListReload(frameList);
		clearSelectedFrame(bottomTop, top, robotSectionMovePanels, 
				robotSectionSlidersPanels, robotSectionSpeedPanels, 
				robotSectionSpeedNumberBoxesPanels);
	}

	public void clearSelectedFrame(
			JPanel bottomTop, 
			JPanel top, 
			Map<RobotSection, JPanel> robotSectionMovePanels, 
			Map<RobotSection, JPanel> robotSectionSlidersPanels,
			Map<RobotSection, JPanel> robotSectionSpeedPanels,
			Map<RobotSection, JPanel> robotSectionSpeedNumberBoxesPanels) {
		addBottomTopPane(bottomTop, null, null, null, null, null);
		frameSelectionChanged(top, robotSectionMovePanels, 
				robotSectionSlidersPanels, robotSectionSpeedPanels, 
				robotSectionSpeedNumberBoxesPanels, null);
	}

	public void controlSaveScript() {
		FileWriter fileWriter = null;
		try {
			if(gesture.getGestureFile() != null) {
				// overwrite the existing file
				int dialogResult = JOptionPane.showConfirmDialog (null, 
						"Do you want to overwrite the existing file \""+gesture.getGestureFile()+"\"?",
						"Warning",JOptionPane.YES_NO_CANCEL_OPTION);
				if(dialogResult == JOptionPane.YES_OPTION) {
					// overwrite
					fileWriter = new FileWriter(gesture.getGestureFile());
					fileWriter.write(gesture.toPythonGesture());
					JOptionPane.showMessageDialog(null, 
							"File \""+gesture.getGestureFile().getAbsolutePath()+"\" succesfully saved!", 
							"Info", JOptionPane.INFORMATION_MESSAGE);
				} else if(dialogResult == JOptionPane.NO_OPTION) {
					// open file dialog
					JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
					jfc.setDialogTitle("Gesture file name");
					int returnValue = jfc.showSaveDialog(null);

					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File selectedFile = jfc.getSelectedFile();
						gesture.setGestureFile(selectedFile);
						LOGGER.info("Saving to \""+selectedFile.getAbsolutePath()+"\"");
						fileWriter = new FileWriter(gesture.getGestureFile());
						fileWriter.write(gesture.toPythonGesture());
						JOptionPane.showMessageDialog(null, 
								"File \""+selectedFile.getAbsolutePath()+"\" succesfully saved!", 
								"Info", JOptionPane.INFORMATION_MESSAGE);
					} else {
						// user did not choose a file to save
						JOptionPane.showMessageDialog(null, 
								"No file selected,nothing saved!", "Info", 
								JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					// cancel option
					return;
				}
			} else {
				// open file dialog
				JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				jfc.setDialogTitle("Gesture file name");
				int returnValue = jfc.showSaveDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					gesture.setGestureFile(selectedFile);
					LOGGER.info("Saving to \""+selectedFile.getAbsolutePath()+"\"");
					fileWriter = new FileWriter(gesture.getGestureFile());
					fileWriter.write(gesture.toPythonGesture());
					JOptionPane.showMessageDialog(null, 
							"File \""+selectedFile.getAbsolutePath()+"\" succesfully saved!", 
							"Info", JOptionPane.INFORMATION_MESSAGE);
				} else {
					// user did not choose a file to save
					JOptionPane.showMessageDialog(null, "No file selected,nothing saved!", "Info", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Could not save script.", e);
		} finally {
			if(fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
				}
			}
		}
	}
		
	public void controlConnect(JButton controlConnect) {
		// Connect / Disconnect to / from the InMoov service (button top-left)
		LOGGER.info("controlConnect start...");
		try {
			if (controlConnect.getText().equals("Connect")) {
				if (referencename == null) {
					referencename = "i01";
				}
				i01 = (InMoov) Runtime.getService(referencename);
				LOGGER.info("i01.enable() start...");
				i01.enable();
				LOGGER.info("i01.enable() end");
				if (i01 != null) {
					controlConnect.setText("Disconnect");
				} else {
					LOGGER.info("Failed to connect!");
				}
			} else {
				i01 = null;
				controlConnect.setText("Connect");
			}
		} catch (Exception e) {
			LOGGER.warn("Exception occured", e);
		}
		LOGGER.info("controlConnect end");
	}

	public void controlExecuteGesture() {
		// test the gesture
		if (i01 == null) {
			// this should go into some kind of message to the user
			LOGGER.info("Testing of gesture is not possible! Because robot is not initialized!");
			return;
		} else if (frames.size() > 0) {
			LOGGER.info("Running gesture \"" + gesture.getGestureName() + "\"");
			for (Frame frame : frames) {
				executeFrameOnRobot(frame);
			}
			 robotRelax();
		} else {
			LOGGER.info("No frames to execute.");
		}
	}

	public void frameCopy(JList<String> frameList) {
		LOGGER.info("frameCopy frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int selectedFrameIndex = frameList.getSelectedIndex();
		if (selectedFrameIndex >= 0 && selectedFrameIndex < frames.size()) {
			Frame frame = frames.get(selectedFrameIndex);
			frames.add(SerializationUtils.clone(frame));
			frameListReload(frameList);
		} else {
			JOptionPane.showMessageDialog(null, 
					"No frame selected to remove", 
					"Info", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void frameDown(JList<String> frameList) {
		LOGGER.info("frameDown frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int selectedFrameIndex = frameList.getSelectedIndex();
		if (selectedFrameIndex >= 0 && selectedFrameIndex < frames.size()-1) {
			Frame frame = frames.remove(selectedFrameIndex);
			frames.add(selectedFrameIndex + 1, frame);
			frameListReload(frameList);
		} else if (selectedFrameIndex == frames.size()-1) {
			JOptionPane.showMessageDialog(null, 
					"Alreay at the bottom", 
					"Info", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, 
					"No frame selected to remove", 
					"Info", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void frameUp(JList<String> frameList) {
		LOGGER.info("frameUps frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int selectedFrameIndex = frameList.getSelectedIndex();
		if (selectedFrameIndex > 0 && selectedFrameIndex < frames.size()) {
			Frame frame = frames.remove(selectedFrameIndex);
			frames.add(selectedFrameIndex - 1, frame);
			frameListReload(frameList);
		} else if (selectedFrameIndex == 0) {
			JOptionPane.showMessageDialog(null, 
					"Alreay at the top", 
					"Info", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, 
					"No frame selected to remove", 
					"Info", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void frameNew(JList<String> frameList) {
		LOGGER.info("frameNew frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int frameIndex = frameList.getSelectedIndex();
		if(frameIndex >= 0) {
			frames.add(frameIndex, new Frame());
		} else {
			frames.add(new Frame());
		}
		frameListReload(frameList);
		frameList.setSelectedIndex(frameIndex);
	}
	
	public void frameRemove(JList<String> frameList) {
		LOGGER.info("frameRemove frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int frameIndex = frameList.getSelectedIndex();
		if (frameIndex >= 0) {
			frames.remove(frameIndex);
			frameListReload(frameList);
		} else {
			JOptionPane.showMessageDialog(null, 
					"No frame selected to remove", 
					"Info", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void robotRelax() {
		try {
			LOGGER.info("Running [RELAX] line...");
			i01.rest();
		} catch (Exception e) {
			LOGGER.warn("Relax error", e);
		}
	}
		
	private void executeFrameOnRobot(Frame frame) {
		LOGGER.info("Executing frame: \"" + frame.getName() + "\"...");
		// speech 
		try {
			LOGGER.info("Running [SPEECH] with text: \"" + frame.getSpeech() + "\"...");
			if (frame.getSpeechSet()) {
				i01.mouth.speak(frame.getSpeech());
			}
		} catch (Exception e) {
			LOGGER.warn("[SPEECH] execution error", e);
		}
		// speed 
		try {
			LOGGER.info("Running [SPEED]...");
			if (frame.getRightHandSpeedSet()) {
				i01.setHandVelocity("right", frame.getRightThumbFingerSpeed(), frame.getRightIndexFingerSpeed(),
						frame.getRightMajeureFingerSpeed(), frame.getRightRingFingerSpeed(),
						frame.getRightPinkyFingerSpeed(), frame.getRightWristSpeed());
			}
			if (frame.getRightArmSpeedSet()) {
				i01.setArmVelocity("right", frame.getRightBicepsSpeed(), frame.getRightRotateSpeed(),
						frame.getRightShoulderSpeed(), frame.getRightOmoplateSpeed());
			}
			if (frame.getLeftHandSpeedSet()) {
				i01.setHandVelocity("left", frame.getLeftThumbFingerSpeed(), frame.getLeftIndexFingerSpeed(),
						frame.getLeftMajeureFingerSpeed(), frame.getLeftRingFingerSpeed(),
						frame.getLeftPinkyFingerSpeed(), frame.getLeftWristSpeed());
			}
			if (frame.getLeftArmSpeedSet()) {
				i01.setArmVelocity("left", frame.getLeftBicepsSpeed(), frame.getLeftRotateSpeed(),
						frame.getLeftShoulderSpeed(), frame.getLeftOmoplateSpeed());
			}
			if (frame.getHeadSpeedSet()) {
				i01.setHeadVelocity(frame.getHeadRotateSpeed(), frame.getNeckSpeed(), frame.getEyeXSpeed(),
						frame.getEyeYSpeed(), frame.getJawSpeed());
			}
			if (frame.getTorsoSpeedSet()) {
				i01.setTorsoVelocity(frame.getTopStomSpeed(), frame.getMidStomSpeed(), frame.getLowStomSpeed());
			}
		} catch (Exception e) {
			LOGGER.warn("[SPEED] execution error", e);
		}
		// move 
		try {
			LOGGER.info("Running [MOVE]...");
			if (frame.getRightHandMoveSet()) {
				i01.moveHand("right", frame.getRightThumbFingerMove(), frame.getRightIndexFingerMove(),
						frame.getRightMajeureFingerMove(), frame.getRightRingFingerMove(),
						frame.getRightPinkyFingerMove(), (double) frame.getRightWristMove());
			}
			if (frame.getRightArmMoveSet()) {
				i01.moveArm("right", frame.getRightBicepsMove(), frame.getRightRotateMove(), frame.getRightShoulderMove(),
						frame.getRightOmoplateMove());
			}
			if (frame.getLeftHandMoveSet()) {
				i01.moveHand("left", frame.getLeftThumbFingerMove(), frame.getLeftIndexFingerMove(),
						frame.getLeftMajeureFingerMove(), frame.getLeftRingFingerMove(), frame.getLeftPinkyFingerMove(),
						(double) frame.getLeftWristMove());
			}
			if (frame.getLeftArmMoveSet()) {
				i01.moveArm("left", frame.getLeftBicepsMove(), frame.getLeftRotateMove(), frame.getLeftShoulderMove(),
						frame.getLeftOmoplateMove());
			}
			if (frame.getHeadMoveSet()) {
				i01.moveHead(frame.getNeckMove(), frame.getHeadRotateMove(), frame.getEyeXMove(), frame.getEyeYMove(),
						frame.getJawMove());
			}
			if (frame.getTorsoMoveSet()) {
				i01.moveTorso(frame.getTopStomMove(), frame.getMidStomMove(), frame.getLowStomMove());
			}
		} catch (Exception e) {
			LOGGER.warn("[MOVE] execution error", e);
		}
		LOGGER.info("Running [SLEEP] for \"" + frame.getSleep() + "\" seconds...");
		// sleep is in mili seconds
		sleep(frame.getSleep()*1000);
		LOGGER.info("Finished frame execution.");
	}

	public void frameExecute(JList<String> frameList) {
		// Test selected frame
		int selectedFrameIndex = frameList.getSelectedIndex();
		if (i01 != null && selectedFrameIndex != -1) {
			Frame frame = frames.get(selectedFrameIndex);
			executeFrameOnRobot(frame);
		} else {
			if (selectedFrameIndex == -1) {
				// this should go into some kind of message to the user
				LOGGER.info("Please select a frame!");
			} else {
				// this should go into some kind of message to the user
				LOGGER.info("Testing of frame is not possible! Robot is not initialised!");
			}
		}
	}

	public void loadScriptFolder(JList gestureList) {
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		jfc.setDialogTitle("Choose a directory with Gesture scripts in Python");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnValue = jfc.showSaveDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			if (jfc.getSelectedFile().isDirectory()) {
				LOGGER.info("Selected script directory: " + jfc.getSelectedFile());

				File[] listOfFiles = jfc.getSelectedFile().listFiles();
				scriptFiles.clear();
				List<String> scriptFileNames = new ArrayList<String>();
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".py")) {
						// list only ".py" files in the folder
						scriptFileNames.add(listOfFiles[i].getName());
						scriptFiles.add(listOfFiles[i]);
					}
				}
				if (scriptFileNames != null && scriptFileNames.size() > 0) {
					gestureList.setListData(scriptFileNames.toArray());
				}
			}
		}
	}

	public void loadGestureScript(
			JList<String> gestureList, 
			JList<String> frameListGui,
			JTextField gestureName) {
		List<String> scriptLines = new ArrayList<String>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		File selectedFile = null;
		try {
			selectedFile = scriptFiles.get(gestureList.getSelectedIndex());
			LOGGER.info("Loading script \"" + selectedFile.getAbsolutePath() + "\"...");
			fileReader = new FileReader(selectedFile);
			bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				scriptLines.add(line);
			}
		} catch (Exception e) {
			LOGGER.warn("Exception occurred trying to read script", e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					LOGGER.warn("Could not close fileReader", e);
				}
			}
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					LOGGER.warn("Could not close bufferedReader", e);
				}
			}
		}
		try {
			frames.clear();
			gesture.setGestureFile(selectedFile);
			parseScriptToGesture(scriptLines);
			gestureName.setText(gesture.getGestureName());
			LOGGER.info("Parsed \"" + gesture.getGestureName() + "\" GESTURE with FRAME count \"" + frames.size() + "\"");
			// loading parsed frames into GUI list
			frameListReload(frameListGui);
			LOGGER.trace("Reload GUI finished");
		} catch (Exception e) {
			LOGGER.warn("Loading parsed frames", e);
		}
	}

	private void frameListReload(JList frameList) {
		List<String> listdata = new ArrayList<String>();
		for (Frame frame : frames) {
			listdata.add(frame.toString());
		}
		frameList.setListData(listdata.toArray());
	}

	private void parseScriptToGesture(List<String> scriptLines) throws Exception {
		// parse start
		try {
			// step #1: find gesture name
			for (String singleScriptLine : scriptLines) {
				if (singleScriptLine.trim().startsWith("def ")) {
					// we have a gesture name
					gesture.setGestureName(singleScriptLine.trim().substring(4, singleScriptLine.indexOf('(')));
					break;
				}
			}
			LOGGER.trace("gesture.getGestureName() \"" + gesture.getGestureName() + "\"");
			// step #2: find gesture start
			boolean gestureStartFound = false;
			int counter = 0;
			for (String singleScriptLine : scriptLines) {
				if (singleScriptLine.contains("startedGesture")) {
					// we have a gesture start
					gestureStartFound = true;
					break;
				}
				counter++;
			}
			if (!gestureStartFound) {
				throw new Exception("Gesture not found. Please provide a startedGesture() as the first command!");
			}
			// trimming lines before startedGesture
			scriptLines = scriptLines.subList(counter, scriptLines.size());
			// at this point the first gesture is starting
			final List<String> frameLines = new ArrayList<String>();
			String speechLine = null;
			for (String singleScriptLine : scriptLines) {
				singleScriptLine = singleScriptLine.trim();
				if (!singleScriptLine.contains("setHeadVelocity") && !singleScriptLine.contains("setArmVelocity")
						&& !singleScriptLine.contains("setHandVelocity")
						&& !singleScriptLine.contains("setTorsoVelocity") && !singleScriptLine.contains("setHeadSpeed")
						&& !singleScriptLine.contains("setArmSpeed") && !singleScriptLine.contains("setHandSpeed")
						&& !singleScriptLine.contains("setTorsoSpeed") && !singleScriptLine.contains("moveHead")
						&& !singleScriptLine.contains("moveArm") && !singleScriptLine.contains("moveHand")
						&& !singleScriptLine.contains("moveTorso")  && !singleScriptLine.contains("speak")
						&& !singleScriptLine.contains("sleep") // end frame
						&& !singleScriptLine.contains("finishedGesture")) { // end gesture
					continue;
				}
				/// at this point we have frame command
				if (singleScriptLine.contains("finishedGesture")) {
					// we are finished
					return;
				} else if (singleScriptLine.contains("mouth.speak(")) {
					// ignore
					speechLine = singleScriptLine;
					continue;
				} else if (singleScriptLine.contains("sleep")) {
					// sleep means the end of the frame
					try {
						final Frame frame = new Frame();
						frame.setName("Frame#" + counter);
						// parse the frame and add it
						parseScriptFragmentIntoSingleFrame(frameLines, frame);
						// speech
						parseScriptSpeechLineToFrame(speechLine, frame);
						// finish it with a sleep
						parseScriptSleepLineToFrame(singleScriptLine, frame);
						frames.add(frame);
					} catch (Exception e) {
						LOGGER.error("Exception from function parseScriptFragmentIntoSingleFrame: " + e);
					} finally {
						speechLine = null;
						counter++;
						// reset framelines
						frameLines.clear();
					}
				} else {
					frameLines.add(singleScriptLine);
				}
			}
		} catch (Exception e) {
			LOGGER.warn("parseScriptToFrame error", e);
		}
	}

	private void parseScriptFragmentIntoSingleFrame(List<String> frameLines, Frame frame)
			throws Exception {
		try {
			for (String singleScriptLine : frameLines) {
				try {
					// it always starts with 'i01.'
					// i01.setHeadSpeed(0.95,0.95)
					LOGGER.trace("expected: i01.setHeadSpeed(0.95,0.95) \"" + singleScriptLine + "\"");
					singleScriptLine = singleScriptLine.substring(4, singleScriptLine.length() - 1);
					// setHeadSpeed(0.95,0.95
					LOGGER.trace("expected: setHeadSpeed(0.95,0.95 \"" + singleScriptLine + "\"");
					String[] splitString = singleScriptLine.split("\\(");
					LOGGER.trace("splitString[0] expected: setHeadSpeed \"" + splitString[0] + "\"");
					LOGGER.trace("splitString[1] expected: 0.95,0.95 \"" + splitString[1] + "\"");
					// splitString[0] setHeadSpeed
					// splitString[1] 0.95,0.95
					// LOGGER.trace("Testing if split function does as predicted. splitString[0] = "
					// + splitString[0] + " and splitString[1] = " + splitString[1]);
					String[] valuesString = splitString[1].split(",");
					LOGGER.trace("valuesString[0] \"" + valuesString[0] + "\"");
					LOGGER.trace("valuesString.length \"" + valuesString.length + "\"");
					if (splitString[0].contains("Speed") || splitString[0].contains("Velocity")) {
						if (splitString[0].contains("Head")) {
							// setHeadSpeed(0.95,0.95)
							// it has to have 2 arguments
							if (valuesString.length > 0) {
								frame.setHeadSpeedSet(true);
								frame.setHeadRotateSpeed(Double.parseDouble(valuesString[0].trim()));
							}
							if (valuesString.length > 1) {
								frame.setNeckSpeed(Double.parseDouble(valuesString[1].trim()));
							}
							if (valuesString.length > 2) {
								frame.setEyeXSpeed(Double.parseDouble(valuesString[2].trim()));
							}
							if (valuesString.length > 3) {
								frame.setEyeYSpeed(Double.parseDouble(valuesString[3].trim()));
							}
							if (valuesString.length > 4) {
								frame.setJawSpeed(Double.parseDouble(valuesString[4].trim()));
							}
						} else if (splitString[0].contains("Torso")) {
							// setTorsoSpeed(0.95,0.85,1.0)
							if (valuesString.length > 0) {
								frame.setTorsoSpeedSet(true);
								frame.setTopStomSpeed(Double.parseDouble(valuesString[0].trim()));
							}
							if (valuesString.length > 1) {
								frame.setMidStomSpeed(Double.parseDouble(valuesString[1].trim()));
							}
							if (valuesString.length > 2) {
								frame.setLowStomSpeed(Double.parseDouble(valuesString[2].trim()));
							}
						} else if (splitString[0].contains("Arm")) {
							if (valuesString.length > 0) {
								if (valuesString[0].contains("left")) {
									// setArmSpeed("left",1.0,0.85,0.95,0.95)
									frame.setLeftArmSpeedSet(true);
									if (valuesString.length > 1) {
										frame.setLeftBicepsSpeed(Double.parseDouble(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										frame.setLeftRotateSpeed(Double.parseDouble(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										frame.setLeftShoulderSpeed(Double.parseDouble(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										frame.setLeftOmoplateSpeed(Double.parseDouble(valuesString[4].trim()));
									}
								} else if (valuesString[0].contains("right")) {
									// setArmSpeed("right",0.65,0.85,0.65,0.85)
									frame.setRightArmSpeedSet(true);
									if (valuesString.length > 1) {
										frame.setRightBicepsSpeed(Double.parseDouble(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										frame.setRightRotateSpeed(Double.parseDouble(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										frame.setRightShoulderSpeed(Double.parseDouble(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										frame.setRightOmoplateSpeed(Double.parseDouble(valuesString[4].trim()));
									}
								}
							}
						} else if (splitString[0].contains("Hand")) {
							if (valuesString.length > 0) {
								if (valuesString[0].contains("left")) {
									// setHandSpeed("left",0.85,0.85,0.85,0.85,0.85,0.85)
									frame.setLeftHandSpeedSet(true);
									if (valuesString.length > 1) {
										frame.setLeftThumbFingerSpeed(Double.parseDouble(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										frame.setLeftIndexFingerSpeed(Double.parseDouble(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										frame.setLeftMajeureFingerSpeed(Double.parseDouble(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										frame.setLeftRingFingerSpeed(Double.parseDouble(valuesString[4].trim()));
									}
									if (valuesString.length > 5) {
										frame.setLeftPinkyFingerSpeed(Double.parseDouble(valuesString[5].trim()));
									}
									if (valuesString.length > 6) {
										frame.setLeftWristSpeed(Double.parseDouble(valuesString[6].trim()));
									}
								} else if (valuesString[0].contains("right")) {
									frame.setRightHandSpeedSet(true);
									// setHandSpeed("right",0.85,0.85,0.85,0.85,0.85,0.85)
									if (valuesString.length > 1) {
										frame.setRightThumbFingerSpeed(Double.parseDouble(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										frame.setRightIndexFingerSpeed(Double.parseDouble(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										frame.setRightMajeureFingerSpeed(Double.parseDouble(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										frame.setRightRingFingerSpeed(Double.parseDouble(valuesString[4].trim()));
									}
									if (valuesString.length > 5) {
										frame.setRightPinkyFingerSpeed(Double.parseDouble(valuesString[5].trim()));
									}
									if (valuesString.length > 6) {
										frame.setRightWristSpeed(Double.parseDouble(valuesString[6].trim()));
									}
								}
							}
						}
					} else if (splitString[0].contains("move")) {
						if (splitString[0].contains("Head")) {
							// moveHead(79,100,82,78,65)
							if (valuesString.length > 0) {
								frame.setHeadMoveSet(true);
								frame.setNeckMove(Integer.parseInt(valuesString[0].trim()));
							}
							if (valuesString.length > 1) {
								frame.setHeadRotateMove(Integer.parseInt(valuesString[1].trim()));
							}
							if (valuesString.length > 2) {
								frame.setEyeXMove(Integer.parseInt(valuesString[2].trim()));
							}
							if (valuesString.length > 3) {
								frame.setEyeYMove(Integer.parseInt(valuesString[3].trim()));
							}
							if (valuesString.length > 4) {
								frame.setJawMove(Integer.parseInt(valuesString[4].trim()));
							}
						} else if (splitString[0].contains("Arm")) {
							if (valuesString.length > 0) {
								if (valuesString[0].contains("left")) {
									// moveArm("left",5,84,28,15)
									if (valuesString.length > 1) {
										frame.setLeftArmMoveSet(true);
										frame.setLeftBicepsMove(Integer.parseInt(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										frame.setLeftRotateMove(Integer.parseInt(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										frame.setLeftShoulderMove(Integer.parseInt(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										frame.setLeftOmoplateMove(Integer.parseInt(valuesString[4].trim()));
									}
								} else if (valuesString[0].contains("right")) {
									// moveArm("right",5,82,28,15)
									if (valuesString.length > 1) {
										frame.setRightArmMoveSet(true);
										frame.setRightBicepsMove(Integer.parseInt(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										frame.setRightRotateMove(Integer.parseInt(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										frame.setRightShoulderMove(Integer.parseInt(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										frame.setRightOmoplateMove(Integer.parseInt(valuesString[4].trim()));
									}
								}
							}
						} else if (splitString[0].contains("Hand")) {
							if (valuesString.length > 0) {
								if (valuesString[0].contains("left")) {
									// moveHand("left",92,33,37,71,66,25)
									if (valuesString.length > 1) {
										frame.setLeftHandMoveSet(true);
										frame.setLeftThumbFingerMove(Integer.parseInt(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										frame.setLeftIndexFingerMove(Integer.parseInt(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										frame.setLeftMajeureFingerMove(Integer.parseInt(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										frame.setLeftRingFingerMove(Integer.parseInt(valuesString[4].trim()));
									}
									if (valuesString.length > 5) {
										frame.setLeftPinkyFingerMove(Integer.parseInt(valuesString[5].trim()));
									}
									if (valuesString.length > 6) {
										frame.setLeftWristMove(Integer.parseInt(valuesString[6].trim()));
									}
								} else if (valuesString[0].contains("right")) {
									// moveHand("right",81,66,82,60,105,113)
									if (valuesString.length > 1) {
										frame.setRightHandMoveSet(true);
										frame.setRightThumbFingerMove(Integer.parseInt(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										frame.setRightIndexFingerMove(Integer.parseInt(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										frame.setRightMajeureFingerMove(Integer.parseInt(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										frame.setRightRingFingerMove(Integer.parseInt(valuesString[4].trim()));
									}
									if (valuesString.length > 5) {
										frame.setRightPinkyFingerMove(Integer.parseInt(valuesString[5].trim()));
									}
									if (valuesString.length > 6) {
										frame.setRightWristMove(Integer.parseInt(valuesString[6].trim()));
									}
								}
							}
						} else if (splitString[0].contains("Torso")) {
							// moveTorso(90,90,90)
							if (valuesString.length > 0) {
								frame.setTorsoMoveSet(true);
								frame.setTopStomMove(Integer.parseInt(valuesString[0].trim()));
							}
							if (valuesString.length > 1) {
								frame.setMidStomMove(Integer.parseInt(valuesString[1].trim()));
							}
							if (valuesString.length > 2) {
								frame.setLowStomMove(Integer.parseInt(valuesString[2].trim()));
							}
						}
					}
				} catch (Exception e) {
					LOGGER.warn("Frame line parsing error on frame: \"" + singleScriptLine + "\"", e);
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Frame line parsing error", e);
		}
	}

	private void parseScriptSpeechLineToFrame(String speechLine, Frame frame) {
	    // i01.mouth.speak("Some speech")
		try {
			LOGGER.trace("speechLine: \"" + speechLine + "\"");
			if (speechLine != null) {
				String[] splitLine = speechLine.split("\"");
				LOGGER.trace("splitLine[1]: \"" + splitLine[1] + "\"");
				frame.setSpeech(splitLine[1]);
				frame.setSpeechSet(true);
			}
		} catch (Exception e) {
			LOGGER.warn("Speak line parsing error", e);
		}
	}
	
	private void parseScriptSleepLineToFrame(String sleepLine, Frame frame) {
		try {
			// sleep line: sleep(3)
			sleepLine = sleepLine.substring(sleepLine.indexOf('(') + 1, sleepLine.indexOf(')'));
			Double sleepTime = Double.parseDouble(sleepLine);
			frame.setSleep(sleepTime.intValue());
		} catch (Exception e) {
			LOGGER.warn("Sleep line parsing error", e);
		}
	}

	private void addMoveSlidersToSectionPane(JPanel panel, 
			final Frame frame, 
			RobotSection robotSection,
			JList<String> frameList) {
		LOGGER.trace("addMoveSlidersToSectionPane for \"" + robotSection +
					"\" subSectionSize: \"" + frame.getSubSectionSize(robotSection) + "\"");
		for(int i = 0; i < frame.getSubSectionSize(robotSection); i++) {
			// preset the slider			
			final int sectionIndex = i;
			JLabel sliderLabel = new JLabel(Frame.getSectionLabel(robotSection, sectionIndex));
			JSlider slider = new JSlider();
			slider.setOrientation(SwingConstants.VERTICAL);
			slider.setMinimum(0);
			slider.setMaximum(180);
			slider.setMajorTickSpacing(20);
			slider.setMinorTickSpacing(1);
			slider.createStandardLabels(1);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setValue(frame.getMoveValue(robotSection, i));

			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
					frame.setMoveValue(robotSection, sectionIndex, slider.getValue());
					if (moveRealTime) {
						moveRobotAsSliderChangesRealTime(robotSection, frame);
					}
				    if (!slider.getValueIsAdjusting()) {
				    	// sliding stopped
				    	frameListReload(frameList);
				    }
				}
			});
			final JPanel sliderLabelContainer = new JPanel();
			sliderLabelContainer.setLayout(new BoxLayout(sliderLabelContainer, BoxLayout.Y_AXIS));
			sliderLabelContainer.add(sliderLabel);
			sliderLabelContainer.add(slider);
			panel.add(sliderLabelContainer);
		}
	}
	
	private void addSpeedTextToSectionPane(JPanel panel, Frame frame, RobotSection robotSection) {
		LOGGER.trace("addSpeedTextToSectionPane for \"" + robotSection + 
				"\" subSectionSize: \"" + frame.getSubSectionSize(robotSection) + "\"");
		for(int i = 0; i < frame.getSubSectionSize(robotSection); i++) {
			JFormattedTextField speed = new JFormattedTextField(decimalFormat);
			speed.setColumns(3);
			final int sectionIndex = i;
			PropertyChangeListener l = new PropertyChangeListener() {
		        @Override
		        public void propertyChange(PropertyChangeEvent evt) {
		            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
					frame.setSpeedValue(robotSection, sectionIndex, Double.valueOf(text));	
		        }
		    };
			speed.setValue(frame.getSpeedValue(robotSection, i));
		    speed.addPropertyChangeListener("value", l);
			panel.add(speed);
		}
	}

	private void addEnableCheckBoxesToSectionPane(JPanel panel, Frame frame, String title, 
			RobotSection robotSection, boolean move) {
		final JCheckBox checkbox = new JCheckBox(title);
		if (move) {
			checkbox.setSelected(frame.getMoveSet(robotSection));
		} else {
			checkbox.setSelected(frame.getSpeedSet(robotSection));
		}
		checkbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (move) {
					frame.setMoveSet(robotSection, checkbox.isSelected());
				} else {
					frame.setSpeedSet(robotSection, checkbox.isSelected());
				}
			}
		});
		panel.add(checkbox);
	}

	private void setPanelEnabled(JPanel panel, Boolean isEnabled) {
		try {
			panel.setEnabled(isEnabled);
			Component[] components = panel.getComponents();
			for (Component component : components) {
				if (component instanceof JPanel) {
					setPanelEnabled((JPanel) component, isEnabled);
				}
				component.setEnabled(isEnabled);
			}
		} catch (Exception e) {
			LOGGER.warn("setPanelEnabled", e);
		}
	}
	
	public void updateGestureName(String newName) {
		LOGGER.info("updateGestureName newName: [{}]", newName);
		this.gesture.setGestureName(newName);
	}

	public void addBottomTopPane(
			JPanel bottomTop, 
			JFormattedTextField frameNameTextField, 
			JFormattedTextField frameSleepTextField, 
			JFormattedTextField frameSpeechTextField, 
			JCheckBox frameMoveRealTime,
			JList<String> frameList) {
		LOGGER.trace("addBottomTopPane [START]");
		try {
			bottomTop.removeAll();
			if(frameList != null) {
				int frameIndex = frameList.getSelectedIndex();
				if(frameIndex >= 0) {
					Frame frame = frames.get(frameIndex);
		
					frameMoveRealTime.setSelected(false);
					bottomTop.add(frameMoveRealTime);
					
					JLabel frameNameLabel = new JLabel("Frame Name");
					bottomTop.add(frameNameLabel);
					frameMoveRealTime.addChangeListener(new ChangeListener() {
			            @Override
			            public void stateChanged(ChangeEvent e) {
			        		moveRealTime = frameMoveRealTime.isSelected();
			            }
			        });
					
					frameNameTextField = new JFormattedTextField(frame.getName());
					bottomTop.add(frameNameTextField);
		
					PropertyChangeListener frameNameTextListener = new PropertyChangeListener() {
				        @Override
				        public void propertyChange(PropertyChangeEvent evt) {
				            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
							frame.setName(text);
				        }
				    };
				    frameNameTextField.addPropertyChangeListener("value", frameNameTextListener);
		
					JLabel sleepLabel = new JLabel("Sleep (s)");
					bottomTop.add(sleepLabel);
					frameSleepTextField = new JFormattedTextField(decimalFormat);
					frameSleepTextField.setColumns(3);
					frameSleepTextField.setValue(frame.getSleep());
					bottomTop.add(frameSleepTextField);
		
					PropertyChangeListener frameSleepTextListener = new PropertyChangeListener() {
				        @Override
				        public void propertyChange(PropertyChangeEvent evt) {
				            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
							frame.setSleep(Integer.valueOf(text));	
					    	frameListReload(frameList);
				        }
				    };
				    frameSleepTextField.addPropertyChangeListener("value", frameSleepTextListener);
		
					JLabel speechLabel = new JLabel("Speech");
					bottomTop.add(speechLabel);			
					frameSpeechTextField = new JFormattedTextField(frame.getSpeech());
					bottomTop.add(frameSpeechTextField);
		
					PropertyChangeListener frameSpeechTextListener = new PropertyChangeListener() {
				        @Override
				        public void propertyChange(PropertyChangeEvent evt) {
				            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
							frame.setSpeech(text);	
					    	frameListReload(frameList);
				        }
				    };
				    frameSpeechTextField.addPropertyChangeListener("value", frameSpeechTextListener);
				}
			}
		} catch (Exception e) {
			LOGGER.warn("addBottomTopPane error: ", e);
		}
		LOGGER.trace("addBottomTopPane [END]");
	}

	public void frameSelectionChanged(JPanel top, 
			Map<RobotSection, JPanel> robotSectionMovePanels, 
			Map<RobotSection, JPanel> robotSectionSlidersPanels,
			Map<RobotSection, JPanel> robotSectionSpeedPanels,
			Map<RobotSection, JPanel> robotSectionSpeedNumberBoxesPanels,
			JList<String> frameList) {
		LOGGER.trace("frameSelectionChanged [START]");
		try {
			if(frameList != null) {
				int frameIndex = frameList.getSelectedIndex();
				if(frameIndex >= 0) {
					Frame frame = frames.get(frameIndex);
					// add elements and listeners, and make panel hierarchy
					for (RobotSection robotSection : RobotSection.values()) {
						LOGGER.trace("robotSection: \"" + robotSection + "\"");
						// cleanup
						JPanel robotSectionMovePanel = robotSectionMovePanels.get(robotSection);
						JPanel robotSectionSlidersPanel = robotSectionSlidersPanels.get(robotSection);
						robotSectionMovePanel.removeAll();
						robotSectionSlidersPanel.removeAll();
						// adding MOVE elements
						addEnableCheckBoxesToSectionPane(robotSectionMovePanel, frame, "Move?", robotSection, true);
						addMoveSlidersToSectionPane(robotSectionSlidersPanel, frame, robotSection, frameList);
						robotSectionMovePanel.add(robotSectionSlidersPanel);
						setPanelEnabled(robotSectionSlidersPanel, frame.getMoveSet(robotSection));
						// cleanup
						JPanel robotSectionSpeedPanel = robotSectionSpeedPanels.get(robotSection);
						JPanel robotSectionSpeedNumberBoxesPanel = robotSectionSpeedNumberBoxesPanels.get(robotSection);
						robotSectionSpeedPanel.removeAll();
						robotSectionSpeedNumberBoxesPanel.removeAll();
						// adding SPEED elements
						addEnableCheckBoxesToSectionPane(robotSectionSpeedPanel, frame, "Set Speed?", robotSection, false);
						addSpeedTextToSectionPane(robotSectionSpeedNumberBoxesPanel, frame, robotSection);
						robotSectionSpeedPanel.add(robotSectionSpeedNumberBoxesPanel);
						setPanelEnabled(robotSectionSpeedNumberBoxesPanel, frame.getSpeedSet(robotSection));
					}
				}
			} else {
				for (RobotSection robotSection : RobotSection.values()) {
					LOGGER.trace("robotSection: \"" + robotSection + "\"");
					JPanel robotSectionMovePanel = robotSectionMovePanels.get(robotSection);
					JPanel robotSectionSlidersPanel = robotSectionSlidersPanels.get(robotSection);
					robotSectionMovePanel.removeAll();
					robotSectionSlidersPanel.removeAll();
					JPanel robotSectionSpeedPanel = robotSectionSpeedPanels.get(robotSection);
					JPanel robotSectionSpeedNumberBoxesPanel = robotSectionSpeedNumberBoxesPanels.get(robotSection);
					robotSectionSpeedPanel.removeAll();
					robotSectionSpeedNumberBoxesPanel.removeAll();
				}
			}
			top.revalidate();
			top.repaint();

		} catch (Exception e) {
			LOGGER.warn("frameSelectionChanged error: ", e);
		}
		LOGGER.trace("frameSelectionChanged [END]");
	}
	
	public void moveRobotAsSliderChangesRealTime(RobotSection robotSection, Frame frame) {
		// slider move in progress
		if (moveRealTime && i01 != null) {
			// Move the Servos in "Real-Time"
			if (robotSection == RobotSection.RIGHT_HAND && frame.getRightHandMoveSet()) {
				i01.moveHand("right", frame.getRightThumbFingerMove(), frame.getRightIndexFingerMove(),
						frame.getRightMajeureFingerMove(), frame.getRightRingFingerMove(), frame.getRightPinkyFingerMove(),
						(double) frame.getRightWristMove());
			}
			if (robotSection == RobotSection.RIGHT_ARM && frame.getRightArmMoveSet()) {
				i01.moveArm("right", frame.getRightBicepsMove(), frame.getRightRotateMove(), frame.getRightShoulderMove(),
						frame.getRightOmoplateMove());
			}
			if (robotSection == RobotSection.LEFT_HAND && frame.getLeftHandMoveSet()) {
				i01.moveHand("left", frame.getLeftThumbFingerMove(), frame.getLeftIndexFingerMove(),
						frame.getLeftMajeureFingerMove(), frame.getLeftRingFingerMove(), frame.getLeftPinkyFingerMove(),
						(double) frame.getLeftWristMove());
			}
			if (robotSection == RobotSection.LEFT_ARM && frame.getLeftArmMoveSet()) {
				i01.moveArm("left", frame.getLeftBicepsMove(), frame.getLeftRotateMove(), frame.getLeftShoulderMove(),
						frame.getLeftOmoplateMove());
			}
			if (robotSection == RobotSection.HEAD && frame.getHeadMoveSet()) {
				i01.moveHead(frame.getNeckMove(), frame.getHeadRotateMove(), frame.getEyeXMove(), frame.getEyeYMove(),
						frame.getJawMove());
			}
			if (robotSection == RobotSection.TORSO && frame.getTorsoMoveSet()) {
				i01.moveTorso(frame.getTopStomMove(), frame.getMidStomMove(), frame.getLowStomMove());
			}
		}
	}
}