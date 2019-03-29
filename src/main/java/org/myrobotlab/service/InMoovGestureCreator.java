package org.myrobotlab.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
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


	private final Gesture gesture = new Gesture();
	private final List<Frame> frames = gesture.getFrames();

	private final List<File> scriptFiles = new ArrayList<File>();
	
	private InMoov i01;
	private Boolean moveRealTime = false;
	private String referencename;

	public InMoovGestureCreator(String n) {
		super(n);
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
		
	public void controlConnect(final JButton controlConnect) {
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

	public void gestureExecute(JList<String> frameList) {
		// test the gesture
		if (i01 == null) {
			// this should go into some kind of message to the user
			LOGGER.info("Testing of gesture is not possible! Because robot is not initialized!");
			return;
		} else if (frames.size() > 0) {
			LOGGER.info("Running gesture \"" + gesture.getGestureName() + "\"");
			int frameIndex = 0;
			for (Frame frame : frames) {
				frameList.setSelectedIndex(frameIndex);
				executeFrameOnRobot(frame);
				frameIndex++;
			}
			 robotRelax();
		} else {
			LOGGER.info("No frames to execute.");
		}
	}

	public void frameExecute(final JList<String> frameList) {
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

	public void frameCopy(final JList<String> frameList, final DefaultListModel<String> frameListModel) {
		LOGGER.info("frameCopy frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int selectedFrameIndex = frameList.getSelectedIndex();
		if (selectedFrameIndex >= 0 && selectedFrameIndex < frames.size()) {
			Frame frame = frames.get(selectedFrameIndex);
			frames.add(SerializationUtils.clone(frame));
			frameListReload(frameListModel);
		} else {
			JOptionPane.showMessageDialog(null, 
					"No frame selected to remove", 
					"Info", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void frameDown(final JList<String> frameList, DefaultListModel<String> frameListModel) {
		LOGGER.info("frameDown frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int selectedFrameIndex = frameList.getSelectedIndex();
		if (selectedFrameIndex >= 0 && selectedFrameIndex < frames.size()-1) {
			Frame frame = frames.remove(selectedFrameIndex);
			frames.add(selectedFrameIndex + 1, frame);
			frameListReload(frameListModel);
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

	public void frameUp(final JList<String> frameList, DefaultListModel<String> frameListModel) {
		LOGGER.info("frameUps frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int selectedFrameIndex = frameList.getSelectedIndex();
		if (selectedFrameIndex > 0 && selectedFrameIndex < frames.size()) {
			Frame frame = frames.remove(selectedFrameIndex);
			frames.add(selectedFrameIndex - 1, frame);
			frameListReload(frameListModel);
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

	public void frameNew(final JList<String> frameList, DefaultListModel<String> frameListModel) {
		LOGGER.info("frameNew frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int frameIndex = frameList.getSelectedIndex();
		if(frameIndex >= 0) {
			frames.add(frameIndex, new Frame());
		} else {
			frames.add(new Frame());
		}
		frameListReload(frameListModel);
		frameList.setSelectedIndex(frameIndex);
	}
	
	public void frameRemove(final JList<String> frameList, DefaultListModel<String> frameListModel) {
		LOGGER.info("frameRemove frameList.getSelectedIndex(): [{}]", frameList.getSelectedIndex());
		int frameIndex = frameList.getSelectedIndex();
		if (frameIndex >= 0) {
			frames.remove(frameIndex);
			frameListReload(frameListModel);
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
		
	private void executeFrameOnRobot(final Frame frame) {
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
			DefaultListModel<String> frameListModel,
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
			frameListReload(frameListModel);
			LOGGER.trace("Reload GUI finished");
		} catch (Exception e) {
			LOGGER.warn("Loading parsed frames", e);
		}
	}

	private void frameListItemReload(DefaultListModel<String> frameListModel, Frame frame, int listSelection) {
		LOGGER.trace("frameListItemReload listSelection: [{}] frameListModel.size(): [{}]", listSelection, frameListModel.size());
		try {
			if(frameListModel.size() > 0 && listSelection >= 0 && listSelection < frameListModel.size()) {
				LOGGER.trace("frameListItemReload changing frame: [{}]", frame.toString());
				frameListModel.setElementAt(frame.toString(), listSelection);
			}
		} catch (Exception e) {
			LOGGER.warn("frameListItemReload error: ", e);
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void frameListReload(DefaultListModel frameListModel) {
		LOGGER.info("frameListReload frameListModel: [{}]", frameListModel);
		frameListModel.clear();
		try {
			for (Frame frame : frames) {
				frameListModel.addElement(frame.toString());
			}
		} catch (Exception e) {
			LOGGER.warn("frameListReload error: ", e);
		}
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
	
	public void updateGestureName(String newName) {
		LOGGER.info("updateGestureName newName: [{}]", newName);
		this.gesture.setGestureName(newName);
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
	
	public void clearGestureAndSelectedFrame(final DefaultListModel<String> frameListModel,
			final JFormattedTextField frameNameTextField,
			final JFormattedTextField frameSleepTextField,
			final JFormattedTextField frameSpeechTextField,
			final Map<RobotSection, JCheckBox> robotSectionMoveSetCheckboxes,
			final Map<RobotSection, JCheckBox> robotSectionSpeedSetCheckboxes,
			final Map<RobotSection, List<JSlider>> robotSectionMoveSliders,
			final Map<RobotSection, List<JFormattedTextField>> robotSectionSpeedTextBoxes) {
		LOGGER.trace("clearGestureAndSelectedFrame [START]");
		try {
			// clear gesture first
			this.gesture.setGestureName(null);
			this.gesture.setGestureFile(null);
			this.frames.clear();
			frameListModel.removeAllElements();
			// then panel
			clearFramePanel(
					frameNameTextField,
					frameSleepTextField,
					frameSpeechTextField,
					robotSectionMoveSetCheckboxes,
					robotSectionSpeedSetCheckboxes,
					robotSectionMoveSliders,
					robotSectionSpeedTextBoxes);
		} catch (Exception e) {
			LOGGER.warn("clearGestureAndSelectedFrame error: ", e);
		}
		LOGGER.trace("clearGestureAndSelectedFrame [END]");
	}
	
	public void clearFramePanel(
			final JFormattedTextField frameNameTextField,
			final JFormattedTextField frameSleepTextField,
			final JFormattedTextField frameSpeechTextField,
			final Map<RobotSection, JCheckBox> robotSectionMoveSetCheckboxes,
			final Map<RobotSection, JCheckBox> robotSectionSpeedSetCheckboxes,
			final Map<RobotSection, List<JSlider>> robotSectionMoveSliders,
			final Map<RobotSection, List<JFormattedTextField>> robotSectionSpeedTextBoxes) {
		LOGGER.trace("clearFramePanel [START]");
		try {
			frameNameTextField.setText("");
			frameSleepTextField.setText("");
			frameSpeechTextField.setText("");
			for (RobotSection robotSection : RobotSection.values()) {
				// move
				JCheckBox moveCheckBox = robotSectionMoveSetCheckboxes.get(robotSection);
				moveCheckBox.setEnabled(false);
				List<JSlider> sliders = robotSectionMoveSliders.get(robotSection);
				for (JSlider slider : sliders) {
					slider.setEnabled(false);
				}
				// speed
				JCheckBox speedCheckBox = robotSectionSpeedSetCheckboxes.get(robotSection);
				speedCheckBox.setEnabled(false);
				List<JFormattedTextField> speedTextBoxes = robotSectionSpeedTextBoxes.get(robotSection);
				for (JFormattedTextField speedTextBox : speedTextBoxes) {
					speedTextBox.setEnabled(false);
				}
			}
		} catch (Exception e) {
			LOGGER.warn("clearFramePanel error: ", e);
		}
		LOGGER.trace("clearFramePanel [END]");
	}

	public void frameSelectionChanged(final JList<String> frameList,
			final DefaultListModel<String> frameListModel,
			final JFormattedTextField frameNameTextField,
			final JFormattedTextField frameSleepTextField,
			final JFormattedTextField frameSpeechTextField,
			final Map<RobotSection, JCheckBox> robotSectionMoveSetCheckboxes,
			final Map<RobotSection, JCheckBox> robotSectionSpeedSetCheckboxes,
			final Map<RobotSection, List<JSlider>> robotSectionMoveSliders,
			final Map<RobotSection, List<JFormattedTextField>> robotSectionSpeedTextBoxes) {
		LOGGER.trace("frameSelectionChanged [START]");
		try {
			int frameIndex = frameList.getSelectedIndex();
			if(frameIndex >= 0 && frameIndex < frameListModel.size()) {
				// frame is selected
				Frame frame = frames.get(frameIndex);
				frameNameTextField.setText(frame.getName());
				frameSleepTextField.setText(""+frame.getSleep());
				frameSpeechTextField.setText(frame.getSpeech());
				for (RobotSection robotSection : RobotSection.values()) {
					// move
					boolean moveSectionEnabled = frame.getMoveSet(robotSection);
					JCheckBox moveCheckBox = robotSectionMoveSetCheckboxes.get(robotSection);
					moveCheckBox.setSelected(moveSectionEnabled);
					List<JSlider> sliders = robotSectionMoveSliders.get(robotSection);
					int sectionIndex = 0;
					for (JSlider slider : sliders) {
						if(moveSectionEnabled) {
							slider.setValue(frame.getMoveValue(robotSection, sectionIndex));
							slider.setEnabled(true);
						} else {
							slider.setEnabled(false);
						}
						sectionIndex++;
					}
					// speed
					boolean speedSectionEnabled = frame.getSpeedSet(robotSection);
					JCheckBox speedCheckBox = robotSectionSpeedSetCheckboxes.get(robotSection);
					speedCheckBox.setSelected(speedSectionEnabled);
					List<JFormattedTextField> speedTextBoxes = robotSectionSpeedTextBoxes.get(robotSection);
					sectionIndex = 0;
					for (JFormattedTextField speedTextBox : speedTextBoxes) {
						if(speedSectionEnabled) {
							speedTextBox.setEnabled(true);
							speedTextBox.setValue(frame.getSpeedValue(robotSection, sectionIndex));
						} else {
							speedTextBox.setEnabled(false);
						}
						sectionIndex++;
					}
				}
			} else {
				// no FRAME selected, disable all elements
				clearFramePanel(
						frameNameTextField,
						frameSleepTextField,
						frameSpeechTextField,
						robotSectionMoveSetCheckboxes,
						robotSectionSpeedSetCheckboxes,
						robotSectionMoveSliders,
						robotSectionSpeedTextBoxes);
			}
		} catch (Exception e) {
			LOGGER.warn("frameSelectionChanged error: ", e);
		}
		LOGGER.trace("frameSelectionChanged [END]");
	}

	public void updateFrameSliders(final JList<String> frameList, DefaultListModel<String> frameListModel,
			final RobotSection robotSection, final Integer sectionIndex, 
			final Integer sliderValue, final Boolean sliderIsAdjusting) {
		LOGGER.info("updateFrameSliders sectionIndex: [{}]", sectionIndex);
		try {
			int frameIndex = frameList.getSelectedIndex();
			if(frameIndex >= 0) {
				Frame frame = frames.get(frameIndex);	
				frame.setMoveValue(robotSection, sectionIndex, sliderValue);
				if (moveRealTime) {
					moveRobotAsSliderChangesRealTime(robotSection, frame);
				}
			    if (!sliderIsAdjusting) {
			    	// sliding stopped
			    	frameListItemReload(frameListModel, frame, frameIndex);
			    }
			}
		} catch (Exception e) {
			LOGGER.warn("updateFrameSpeech error: ", e);
		}
		LOGGER.trace("updateFrameSpeech [END]");
	}
	public void updateFrameBooleans(final JList<String> frameList, DefaultListModel<String> frameListModel, 
			final RobotSection robotSection, 
			final Boolean value, final boolean move) {
		LOGGER.info("updateFrameBooleans robotSection: [{}]", robotSection);
		try {
			int frameIndex = frameList.getSelectedIndex();
			if(frameIndex >= 0) {
				Frame frame = frames.get(frameIndex);
				if (move) {
					frame.setMoveSet(robotSection, value);
				} else {
					frame.setSpeedSet(robotSection, value);
				}
		    	frameListItemReload(frameListModel, frame, frameIndex);
			}
		} catch (Exception e) {
			LOGGER.warn("updateFrameSpeech error: ", e);
		}
		LOGGER.trace("updateFrameSpeech [END]");
	}
	public void updateFrameSpeed(final JList<String> frameList, DefaultListModel<String> frameListModel, 
			final RobotSection robotSection, 
			final Integer sectionIndex, final Double speed) {
		LOGGER.info("updateFrameSpeed sectionIndex: [{}]", sectionIndex);
		try {
			int frameIndex = frameList.getSelectedIndex();
			if(frameIndex >= 0) {
				Frame frame = frames.get(frameIndex);
				frame.setSpeedValue(robotSection, sectionIndex, speed);	
		    	frameListItemReload(frameListModel, frame, frameIndex);
			}
		} catch (Exception e) {
			LOGGER.warn("updateFrameSpeech error: ", e);
		}
		LOGGER.trace("updateFrameSpeech [END]");
	}
	public void updateFrameSpeech(final JList<String> frameList, DefaultListModel<String> frameListModel, 
			final String frameSpeech) {
		LOGGER.info("updateFrameSpeech frameSpeech: [{}]", frameSpeech);
		try {
			int frameIndex = frameList.getSelectedIndex();
			if(frameIndex >= 0) {
				Frame frame = frames.get(frameIndex);
				frame.setSpeech(frameSpeech);	
		    	frameListItemReload(frameListModel, frame, frameIndex);
			}
		} catch (Exception e) {
			LOGGER.warn("updateFrameSpeech error: ", e);
		}
		LOGGER.trace("updateFrameSpeech [END]");
	}
	public void updateFrameName(final JList<String> frameList, final String frameName) {
		LOGGER.info("updateFrameName frameName: [{}]", frameName);
		try {
			int frameIndex = frameList.getSelectedIndex();
			if(frameIndex >= 0) {
				Frame frame = frames.get(frameIndex);
				frame.setName(frameName);
			}
		} catch (Exception e) {
			LOGGER.warn("updateFrameName error: ", e);
		}
		LOGGER.trace("updateFrameName [END]");
	}
	public void updateFrameSleep(final JList<String> frameList, DefaultListModel<String> frameListModel, 
			final Integer frameSleep) {
		LOGGER.info("updateFrameSleep frameSleep: [{}]", frameSleep);
		try {
			int frameIndex = frameList.getSelectedIndex();
			if(frameIndex >= 0) {
				Frame frame = frames.get(frameIndex);
				frame.setSleep(frameSleep);	
		    	frameListItemReload(frameListModel, frame, frameIndex);
			}
		} catch (Exception e) {
			LOGGER.warn("updateFrameSleep error: ", e);
		}
		LOGGER.trace("updateFrameSleep [END]");
	}
	public void updateMoveRealTime(final Boolean selected) {
		this.moveRealTime = selected;
	}
}