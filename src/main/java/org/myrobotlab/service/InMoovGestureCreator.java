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

//	public static class PythonItemHolder {
//		String code;
//		boolean modifyable;
//		boolean function;
//		boolean notfunction;
//	}
//
//	public static class ServoItemHolder {
//		public JLabel fin;
//		public JLabel min;
//		public JLabel res;
//		public JLabel max;
//		public JSlider sli;
//		public JLabel akt;
//		public JTextField spe;
//	}

	private static final long serialVersionUID = 1L;

	private final static Logger LOGGER = LoggerFactory.getLogger(InMoovGestureCreator.class);

	private static final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
			.getNumberInstance(Locale.getDefault());

	private final Gesture gesture = new Gesture();
	private final List<Frame> frames = gesture.getFrames();

	private final List<File> scriptFiles = new ArrayList<File>();
	
	private InMoov i01;
	private boolean moverealtime = false;
	private String referencename;

//	transient ServoItemHolder[][] servoitemholder;
//
//	transient ArrayList<PythonItemHolder> pythonitemholder;

//	String pythonscript;
//	String pythonname;

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
			JTextField controlGestureName,
			Map<RobotSection, JPanel> robotSectionMovePanels, 
			Map<RobotSection, JPanel> robotSectionSlidersPanels,
			Map<RobotSection, JPanel> robotSectionSpeedPanels,
			Map<RobotSection, JPanel> robotSectionSpeedNumberBoxesPanels) {
		this.gesture.setGestureName(null);
		this.gesture.setGestureFile(null);
		controlGestureName.setText("Gesture name here");
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
				// TODO experiment till it works
				LOGGER.info("i01.enable() start...");
				i01.enable();
				LOGGER.info("i01.enable() end");
//				LOGGER.info("i01.startAll(\"COM3\", \"COM4\") start...");
//				i01.startAll("COM3", "COM4");
//				LOGGER.info("i01.startAll(\"COM3\", \"COM4\") end");
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
		// Copy this frame on the frameList (button bottom-right)
		int pos = frameList.getSelectedIndex();

		if (pos != -1) {
			Frame fih = frames.get(pos);
			frames.add(fih);

			frameListReload(frameList);
		}
	}

	public void frameDown(JList<String> frameList) {
		// Move this frame one down on the frameList (button bottom-right)
		int pos = frameList.getSelectedIndex();

		if (pos != -1) {
			Frame fih = frames.remove(pos);
			frames.add(pos + 1, fih);

			frameListReload(frameList);
		}
	}

	public void frame_moverealtime(JCheckBox frame_moverealtime) {
		moverealtime = frame_moverealtime.isSelected();
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
			LOGGER.info("Running [RELAX] frame ");
			i01.rest();
		} catch (Exception e) {
			LOGGER.warn("Relax error", e);
		}
	}
		
	private void executeFrameOnRobot(Frame fih) {
		LOGGER.info("Executing frame: \"" + fih.getName() + "\"...");
		// speech 
		try {
			LOGGER.info("Running [SPEECH] with text: \"" + fih.getSpeech() + "\"...");
			if (fih.getSpeechSet()) {
				i01.mouth.speak(fih.getSpeech());
			}
		} catch (Exception e) {
			LOGGER.warn("[SPEECH] execution error", e);
		}
		// speed 
		try {
			LOGGER.info("Running [SPEED]...");
			if (fih.getRightHandSpeedSet()) {
				i01.setHandVelocity("right", fih.getRightThumbFingerSpeed(), fih.getRightIndexFingerSpeed(),
						fih.getRightMajeureFingerSpeed(), fih.getRightRingFingerSpeed(),
						fih.getRightPinkyFingerSpeed(), fih.getRightWristSpeed());
			}
			if (fih.getRightArmSpeedSet()) {
				i01.setArmVelocity("right", fih.getRightBicepsSpeed(), fih.getRightRotateSpeed(),
						fih.getRightShoulderSpeed(), fih.getRightOmoplateSpeed());
			}
			if (fih.getLeftHandSpeedSet()) {
				i01.setHandVelocity("left", fih.getLeftThumbFingerSpeed(), fih.getLeftIndexFingerSpeed(),
						fih.getLeftMajeureFingerSpeed(), fih.getLeftRingFingerSpeed(),
						fih.getLeftPinkyFingerSpeed(), fih.getLeftWristSpeed());
			}
			if (fih.getLeftArmSpeedSet()) {
				i01.setArmVelocity("left", fih.getLeftBicepsSpeed(), fih.getLeftRotateSpeed(),
						fih.getLeftShoulderSpeed(), fih.getLeftOmoplateSpeed());
			}
			if (fih.getHeadSpeedSet()) {
				i01.setHeadVelocity(fih.getHeadRotateSpeed(), fih.getNeckSpeed(), fih.getEyeXSpeed(),
						fih.getEyeYSpeed(), fih.getJawSpeed());
			}
			if (fih.getTorsoSpeedSet()) {
				i01.setTorsoVelocity(fih.getTopStomSpeed(), fih.getMidStomSpeed(), fih.getLowStomSpeed());
			}
		} catch (Exception e) {
			LOGGER.warn("[SPEED] execution error", e);
		}
		// move 
		try {
			LOGGER.info("Running [MOVE]...");
			if (fih.getRightHandMoveSet()) {
				i01.moveHand("right", fih.getRightThumbFingerMove(), fih.getRightIndexFingerMove(),
						fih.getRightMajeureFingerMove(), fih.getRightRingFingerMove(),
						fih.getRightPinkyFingerMove(), (double) fih.getRightWristMove());
			}
			if (fih.getRightArmMoveSet()) {
				i01.moveArm("right", fih.getRightBicepsMove(), fih.getRightRotateMove(), fih.getRightShoulderMove(),
						fih.getRightOmoplateMove());
			}
			if (fih.getLeftHandMoveSet()) {
				i01.moveHand("left", fih.getLeftThumbFingerMove(), fih.getLeftIndexFingerMove(),
						fih.getLeftMajeureFingerMove(), fih.getLeftRingFingerMove(), fih.getLeftPinkyFingerMove(),
						(double) fih.getLeftWristMove());
			}
			if (fih.getLeftArmMoveSet()) {
				i01.moveArm("left", fih.getLeftBicepsMove(), fih.getLeftRotateMove(), fih.getLeftShoulderMove(),
						fih.getLeftOmoplateMove());
			}
			if (fih.getHeadMoveSet()) {
				i01.moveHead(fih.getNeckMove(), fih.getHeadRotateMove(), fih.getEyeXMove(), fih.getEyeYMove(),
						fih.getJawMove());
			}
			if (fih.getTorsoMoveSet()) {
				i01.moveTorso(fih.getTopStomMove(), fih.getMidStomMove(), fih.getLowStomMove());
			}
		} catch (Exception e) {
			LOGGER.warn("[MOVE] execution error", e);
		}
		LOGGER.info("Running [SLEEP] for \"" + fih.getSleep() + "\" seconds...");
		// sleep is in mili seconds
		sleep(fih.getSleep()*1000);
		LOGGER.info("Finished frame execution.");
	}

	public void frameExecute(JList<String> frameList) {
		// Test selected frame
		int selectedFrameIndex = frameList.getSelectedIndex();
		if (i01 != null && selectedFrameIndex != -1) {
			Frame fih = frames.get(selectedFrameIndex);
			executeFrameOnRobot(fih);
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

	public void frameUp(JList<String> frameList) {
		// Move this frame one up on the frameList (button bottom-right)
		int pos = frameList.getSelectedIndex();

		if (pos != -1) {
			Frame fih = frames.remove(pos);
			frames.add(pos - 1, fih);

			frameListReload(frameList);
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
			JTextField controlGestureName) {
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
			controlGestureName.setText(gesture.getGestureName());
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
		for (Frame fih : frames) {
			listdata.add(fih.toString());
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
				    if (!slider.getValueIsAdjusting()) {
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
		
					frameMoveRealTime = new JCheckBox("Move Real Time");
					frameMoveRealTime.setSelected(false);
					bottomTop.add(frameMoveRealTime);
					
					JLabel frameNameLabel = new JLabel("Frame Name");
					bottomTop.add(frameNameLabel);
					//TODO
		//			frameMoveRealTime.addItemListener(this);
					
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
		
		//			frame_add = new JButton("Add");
		//			bottomTop.add(frame_add);
		//			frame_add.addActionListener(this);
		
		//			frame_addspeed = new JButton("Add Speed");
		//			top2top1.add(frame_addspeed);
		//			frame_addspeed.addActionListener(this);
		
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
				        }
				    };
				    frameSleepTextField.addPropertyChangeListener("value", frameSleepTextListener);
		
		//			frame_addsleep = new JButton("Add Sleep");
		//			top2top1.add(frame_addsleep);
		//			frame_addsleep.addActionListener(this);
		
					JLabel speechLabel = new JLabel("Speech");
					bottomTop.add(speechLabel);			
					frameSpeechTextField = new JFormattedTextField(frame.getSpeech());
					bottomTop.add(frameSpeechTextField);
		
					PropertyChangeListener frameSpeechTextListener = new PropertyChangeListener() {
				        @Override
				        public void propertyChange(PropertyChangeEvent evt) {
				            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
							frame.setSpeech(text);	
				        }
				    };
				    frameSpeechTextField.addPropertyChangeListener("value", frameSpeechTextListener);
		
		//			frame_addspeech = new JButton("Add Speech");
		//			top2top1.add(frame_addspeech);
		//			frame_addspeech.addActionListener(this);
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

//	public void gestureListAct(JList<String> gestureList) {
//		String[] listdata = new String[pythonitemholder.size()];
//		for (int i = 0; i < pythonitemholder.size(); i++) {
//			PythonItemHolder pih = pythonitemholder.get(i);
//
//			String pre;
//			if (!(pih.modifyable)) {
//				pre = "X    ";
//			} else {
//				pre = "     ";
//			}
//
//			int he = 21;
//			if (pih.code.length() < he) {
//				he = pih.code.length();
//			}
//
//			String des = pih.code.substring(0, he);
//
//			String displaytext = pre + des;
//			listdata[i] = displaytext;
//		}
//		gestureList.setListData(listdata);
//	}
//
//	public void servoitemholder_set_sih1(int i1, ServoItemHolder[] sih1) {
//		// Setting references
//		servoitemholder[i1] = sih1;
//	}
//
//	public void servoitemholder_slider_changed(int t1, int t2) {
//		// One slider were adjusted
//		servoitemholder[t1][t2].akt.setText(servoitemholder[t1][t2].sli.getValue() + "");
//		// Move the Servos in "Real-Time"
//		if (moverealtime && i01 != null) {
//			Frame fih = new Frame();
//
//			fih.setRightThumbFingerMove(servoitemholder[0][0].sli.getValue());
//			fih.setRightIndexFingerMove(servoitemholder[0][1].sli.getValue());
//			fih.setRightMajeureFingerMove(servoitemholder[0][2].sli.getValue());
//			fih.setRightRingFingerMove(servoitemholder[0][3].sli.getValue());
//			fih.setRightPinkyFingerMove(servoitemholder[0][4].sli.getValue());
//			fih.setRightWristMove(servoitemholder[0][5].sli.getValue());
//
//			fih.setRightBicepsMove(servoitemholder[1][0].sli.getValue());
//			fih.setRightRotateMove(servoitemholder[1][1].sli.getValue());
//			fih.setRightShoulderMove(servoitemholder[1][2].sli.getValue());
//			fih.setRightOmoplateMove(servoitemholder[1][3].sli.getValue());
//
//			fih.setLeftThumbFingerMove(servoitemholder[2][0].sli.getValue());
//			fih.setLeftIndexFingerMove(servoitemholder[2][1].sli.getValue());
//			fih.setLeftMajeureFingerMove(servoitemholder[2][2].sli.getValue());
//			fih.setLeftRingFingerMove(servoitemholder[2][3].sli.getValue());
//			fih.setLeftPinkyFingerMove(servoitemholder[2][4].sli.getValue());
//			fih.setLeftWristMove(servoitemholder[2][5].sli.getValue());
//
//			fih.setLeftBicepsMove(servoitemholder[3][0].sli.getValue());
//			fih.setLeftRotateMove(servoitemholder[3][1].sli.getValue());
//			fih.setLeftShoulderMove(servoitemholder[3][2].sli.getValue());
//			fih.setLeftOmoplateMove(servoitemholder[3][3].sli.getValue());
//
//			fih.setNeckMove(servoitemholder[4][0].sli.getValue());
//			fih.setHeadRotateMove(servoitemholder[4][1].sli.getValue());
//			fih.setEyeXMove(servoitemholder[4][2].sli.getValue());
//			fih.setEyeYMove(servoitemholder[4][3].sli.getValue());
//			fih.setJawMove(servoitemholder[4][4].sli.getValue());
//
//			fih.setTopStomMove(servoitemholder[5][0].sli.getValue());
//			fih.setMidStomMove(servoitemholder[5][1].sli.getValue());
//			fih.setLowStomMove(servoitemholder[5][2].sli.getValue());
//
//			if (fih.getRightHandMoveSet()) {
//				i01.moveHead(fih.getNeckMove(), fih.getHeadRotateMove(), fih.getEyeXMove(), fih.getEyeYMove(),
//						fih.getJawMove());
//			}
//			if (fih.getRightArmMoveSet()) {
//				i01.moveArm("left", fih.getLeftBicepsMove(), fih.getLeftRotateMove(), fih.getLeftShoulderMove(),
//						fih.getLeftOmoplateMove());
//			}
//			if (fih.getLeftHandMoveSet()) {
//				i01.moveArm("right", fih.getRightBicepsMove(), fih.getRightRotateMove(), fih.getRightShoulderMove(),
//						fih.getRightOmoplateMove());
//			}
//			if (fih.getLeftArmMoveSet()) {
//				i01.moveHand("left", fih.getLeftThumbFingerMove(), fih.getLeftIndexFingerMove(),
//						fih.getLeftMajeureFingerMove(), fih.getLeftRingFingerMove(), fih.getLeftPinkyFingerMove(),
//						(double) fih.getLeftWristMove());
//			}
//			if (fih.getHeadMoveSet()) {
//				i01.moveHand("right", fih.getRightThumbFingerMove(), fih.getRightIndexFingerMove(),
//						fih.getRightMajeureFingerMove(), fih.getRightRingFingerMove(), fih.getRightPinkyFingerMove(),
//						(double) fih.getRightWristMove());
//			}
//			if (fih.getTorsoMoveSet()) {
//				i01.moveTorso(fih.getTopStomMove(), fih.getMidStomMove(), fih.getLowStomMove());
//			}
//		}
//	}

//	public void frame_add(JList<String> frameList, JTextField frame_add_textfield) {
//		// Add a servo movement frame to the frameList (button bottom-right)
//		Frame fih = new Frame();
//
//		fih.setRightThumbFingerMove(servoitemholder[0][0].sli.getValue());
//		fih.setRightIndexFingerMove(servoitemholder[0][1].sli.getValue());
//		fih.setRightMajeureFingerMove(servoitemholder[0][2].sli.getValue());
//		fih.setRightRingFingerMove(servoitemholder[0][3].sli.getValue());
//		fih.setRightPinkyFingerMove(servoitemholder[0][4].sli.getValue());
//		fih.setRightWristMove(servoitemholder[0][5].sli.getValue());
//
//		fih.setRightBicepsMove(servoitemholder[1][0].sli.getValue());
//		fih.setRightRotateMove(servoitemholder[1][1].sli.getValue());
//		fih.setRightShoulderMove(servoitemholder[1][2].sli.getValue());
//		fih.setRightOmoplateMove(servoitemholder[1][3].sli.getValue());
//
//		fih.setLeftThumbFingerMove(servoitemholder[2][0].sli.getValue());
//		fih.setLeftIndexFingerMove(servoitemholder[2][1].sli.getValue());
//		fih.setLeftMajeureFingerMove(servoitemholder[2][2].sli.getValue());
//		fih.setLeftRingFingerMove(servoitemholder[2][3].sli.getValue());
//		fih.setLeftPinkyFingerMove(servoitemholder[2][4].sli.getValue());
//		fih.setLeftWristMove(servoitemholder[2][5].sli.getValue());
//
//		fih.setLeftBicepsMove(servoitemholder[3][0].sli.getValue());
//		fih.setLeftRotateMove(servoitemholder[3][1].sli.getValue());
//		fih.setLeftShoulderMove(servoitemholder[3][2].sli.getValue());
//		fih.setLeftOmoplateMove(servoitemholder[3][3].sli.getValue());
//
//		fih.setNeckMove(servoitemholder[4][0].sli.getValue());
//		fih.setHeadRotateMove(servoitemholder[4][1].sli.getValue());
//		fih.setEyeXMove(servoitemholder[4][2].sli.getValue());
//		fih.setEyeYMove(servoitemholder[4][3].sli.getValue());
//		fih.setJawMove(servoitemholder[4][4].sli.getValue());
//
//		fih.setTopStomMove(servoitemholder[5][0].sli.getValue());
//		fih.setMidStomMove(servoitemholder[5][1].sli.getValue());
//		fih.setLowStomMove(servoitemholder[5][2].sli.getValue());
//
//		fih.setSleep(-1);
//		fih.setSpeech(null);
//		fih.setName(frame_add_textfield.getText());
//
//		frames.add(fih);
//
//		frameListReload(frameList);
//	}
	
//	public void frame_importminresmax() {
//		// Import the Min- / Res- / Max- settings of your InMoov
//		if (i01 != null) {
//			for (int i1 = 0; i1 < servoitemholder.length; i1++) {
//				for (int i2 = 0; i2 < servoitemholder[i1].length; i2++) {
//					InMoovHand inmhand = null;
//					InMoovArm inmarm = null;
//					InMoovHead inmhead = null;
//					InMoovTorso inmtorso = null;
//
//					if (i1 == 0) {
//						inmhand = i01.rightHand;
//					} else if (i1 == 1) {
//						inmarm = i01.rightArm;
//					} else if (i1 == 2) {
//						inmhand = i01.leftHand;
//					} else if (i1 == 3) {
//						inmarm = i01.rightArm;
//					} else if (i1 == 4) {
//						inmhead = i01.head;
//					} else if (i1 == 5) {
//						inmtorso = i01.torso;
//					}
//
//					Servo servo = null;
//
//					if (i1 == 0 || i1 == 2) {
//						if (i2 == 0) {
//							servo = inmhand.thumb;
//						} else if (i2 == 1) {
//							servo = inmhand.index;
//						} else if (i2 == 2) {
//							servo = inmhand.majeure;
//						} else if (i2 == 3) {
//							servo = inmhand.ringFinger;
//						} else if (i2 == 4) {
//							servo = inmhand.pinky;
//						} else if (i2 == 5) {
//							servo = inmhand.wrist;
//						}
//					} else if (i1 == 1 || i1 == 3) {
//						if (i2 == 0) {
//							servo = inmarm.bicep;
//						} else if (i2 == 1) {
//							servo = inmarm.rotate;
//						} else if (i2 == 2) {
//							servo = inmarm.shoulder;
//						} else if (i2 == 3) {
//							servo = inmarm.omoplate;
//						}
//					} else if (i1 == 4) {
//						if (i2 == 0) {
//							servo = inmhead.neck;
//						} else if (i2 == 1) {
//							servo = inmhead.rothead;
//						} else if (i2 == 2) {
//							servo = inmhead.eyeX;
//						} else if (i2 == 3) {
//							servo = inmhead.eyeY;
//						} else if (i2 == 4) {
//							servo = inmhead.jaw;
//						}
//					} else if (i1 == 5) {
//						if (i2 == 0) {
//							servo = inmtorso.topStom;
//						} else if (i2 == 1) {
//							servo = inmtorso.midStom;
//						} else if (i2 == 2) {
//							servo = inmtorso.lowStom;
//						}
//					}
//
//					Double min = servo.getMin();
//					double res = servo.getRest();
//					Double max = servo.getMax();
//
//					servoitemholder[i1][i2].min.setText(min + "");
//					servoitemholder[i1][i2].res.setText(res + "");
//					servoitemholder[i1][i2].max.setText(max + "");
//					// servoitemholder[i1][i2].sli.setMinimum(min);
//					// servoitemholder[i1][i2].sli.setMaximum(max);
//					// servoitemholder[i1][i2].sli.setValue(res);
//				}
//			}
//		}
//	}

//	public void oldTabs(JPanel bottom, Frame frame, int frameItemHolderIndex) {		
//			
//			JTabbedPane bottomTabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
//
//			// JPanels for the JTabbedPane
//			final JPanel mainpanel = new JPanel();
//			final JPanel c1panel = new JPanel();
//			final JPanel c2panel = new JPanel();
//			final JPanel c3panel = new JPanel();
//
//			// mainpanel (enabling / disabling sections)
//			mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
//			boolean[] tabs_main_checkbox_states = new boolean[6];
//			for (int i = 0; i < 6; i++) {
//				String name = "";
//				if (i == 0) {
//					name = "Right Hand";
//				} else if (i == 1) {
//					name = "Right Arm";
//				} else if (i == 2) {
//					name = "Left Hand";
//				} else if (i == 3) {
//					name = "Left Arm";
//				} else if (i == 4) {
//					name = "Head";
//				} else if (i == 5) {
//					name = "Torso";
//				}
//
//				final int fi = i;
//
//				final JCheckBox checkbox = new JCheckBox(name);
//				checkbox.addItemListener(new ItemListener() {
//					@Override
//					public void itemStateChanged(ItemEvent arg0) {
//						tabs_main_checkbox_states[fi] = checkbox.isSelected();
//						// myService.send(boundServiceName, "tabs_main_checkbox_states_changed",
//						// tabs_main_checkbox_states);
////						tabs_main_checkbox_states_changed(tabs_main_checkbox_states);
//					}
//
//				});
//				checkbox.setSelected(true);
//				mainpanel.add(checkbox);
//			}
//
//			Container c1con = c1panel;
//			Container c2con = c2panel;
//			Container c3con = c3panel;
//			// seting the layout of panels
//			GridBagLayout c1gbl = new GridBagLayout();
//			c1con.setLayout(c1gbl);
//			GridBagLayout c2gbl = new GridBagLayout();
//			c2con.setLayout(c2gbl);
//			GridBagLayout c3gbl = new GridBagLayout();
//			c3con.setLayout(c3gbl);
//
//			// predefined min- / res- / max- positions
//			int[][][] minresmaxpos = {
//					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
//					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
//					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
//					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
//					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
//					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 } } };
//
//			// c1-, c2-, c3-panel
//			for (int i1 = 0; i1 < 6; i1++) {
//
//				Container con = null;
//				GridBagLayout gbl = null;
//
//				if (i1 == 0 || i1 == 1) {
//					con = c1con;
//					gbl = c1gbl;
//				} else if (i1 == 2 || i1 == 3) {
//					con = c2con;
//					gbl = c2gbl;
//				} else if (i1 == 4 || i1 == 5) {
//					con = c3con;
//					gbl = c3gbl;
//				}
//
//				int size = 0;
//
//				if (i1 == 0 || i1 == 2) {
//					size = 6;
//				} else if (i1 == 1 || i1 == 3) {
//					size = 4;
//				} else if (i1 == 4) {
//					size = 5;
//				} else if (i1 == 5) {
//					size = 3;
//				}
//
//				int offset = 0;
//				if (i1 == 1 || i1 == 3) {
//					offset = 6;
//				} else if (i1 == 5) {
//					offset = 5;
//				}
//
//				ServoItemHolder[] sih1 = new ServoItemHolder[size];
//				int value = 0;
//
//				for (int i2 = 0; i2 < size; i2++) {
//					ServoItemHolder sih11 = new ServoItemHolder();
//
//					String servoname = "";
//
//					if (i1 == 0 || i1 == 2) {
//						if (i2 == 0) {
//							if (i1 == 0) {
//								value = frame.getRightThumbFingerMove();
//							} else {
//								value = frame.getLeftThumbFingerMove();
//							}
//							servoname = "thumb";
//						} else if (i2 == 1) {
//							if (i1 == 0) {
//								value = frame.getRightIndexFingerMove();
//							} else {
//								value = frame.getLeftIndexFingerMove();
//							}
//							servoname = "index";
//						} else if (i2 == 2) {
//							if (i1 == 0) {
//								value = frame.getRightMajeureFingerMove();
//							} else {
//								value = frame.getLeftMajeureFingerMove();
//							}
//							servoname = "majeure";
//						} else if (i2 == 3) {
//							if (i1 == 0) {
//								value = frame.getRightRingFingerMove();
//							} else {
//								value = frame.getLeftRingFingerMove();
//							}
//							servoname = "ringfinger";
//						} else if (i2 == 4) {
//							if (i1 == 0) {
//								value = frame.getRightPinkyFingerMove();
//							} else {
//								value = frame.getLeftPinkyFingerMove();
//							}
//							servoname = "pinky";
//						} else if (i2 == 5) {
//							if (i1 == 0) {
//								value = frame.getRightWristMove();
//							} else {
//								value = frame.getLeftWristMove();
//							}
//							servoname = "wrist";
//						}
//					} else if (i1 == 1 || i1 == 3) {
//						if (i2 == 0) {
//							servoname = "bicep";
//						} else if (i2 == 1) {
//							servoname = "rotate";
//						} else if (i2 == 2) {
//							servoname = "shoulder";
//						} else if (i2 == 3) {
//							servoname = "omoplate";
//						}
//					} else if (i1 == 4) {
//						if (i2 == 0) {
//							servoname = "neck";
//						} else if (i2 == 1) {
//							servoname = "rothead";
//						} else if (i2 == 2) {
//							servoname = "eyeX";
//						} else if (i2 == 3) {
//							servoname = "eyeY";
//						} else if (i2 == 4) {
//							servoname = "jaw";
//						}
//					} else if (i1 == 5) {
//						if (i2 == 0) {
//							servoname = "topStom";
//						} else if (i2 == 1) {
//							servoname = "midStom";
//						} else if (i2 == 2) {
//							servoname = "lowStom";
//						}
//					}
//
//					sih11.fin = new JLabel(servoname);
//					sih11.min = new JLabel(minresmaxpos[i1][i2][0] + "");
//					sih11.res = new JLabel(minresmaxpos[i1][i2][1] + "");
//					sih11.max = new JLabel(minresmaxpos[i1][i2][2] + "");
//					sih11.sli = new JSlider();
//					// customizeslider(sih11.sli, i1, i2, minresmaxpos[i1][i2]);
//					sliderSetUp(sih11.sli, i1, i2, minresmaxpos[i1][i2], value);
//					sih11.akt = new JLabel(sih11.sli.getValue() + "");
//					sih11.spe = new JTextField("1.00");
//
//					// x y w h wx wy
//					gridBagLayoutAddComponent(con, gbl, sih11.fin, offset + i2, 0, 1, 1, 1.0, 1.0);
//					gridBagLayoutAddComponent(con, gbl, sih11.min, offset + i2, 1, 1, 1, 1.0, 1.0);
//					gridBagLayoutAddComponent(con, gbl, sih11.res, offset + i2, 2, 1, 1, 1.0, 1.0);
//					gridBagLayoutAddComponent(con, gbl, sih11.max, offset + i2, 3, 1, 1, 1.0, 1.0);
//					gridBagLayoutAddComponent(con, gbl, sih11.sli, offset + i2, 4, 1, 1, 1.0, 1.0);
//					gridBagLayoutAddComponent(con, gbl, sih11.akt, offset + i2, 5, 1, 1, 1.0, 1.0);
//					gridBagLayoutAddComponent(con, gbl, sih11.spe, offset + i2, 6, 1, 1, 1.0, 1.0);
//
//					sih1[i2] = sih11;
//				}
//				// myService.send(boundServiceName, "servoitemholder_set_sih1", i1, sih1);
//				servoitemholder_set_sih1(i1, sih1);
//			}
//
//			bottomTabs.addTab("Main", mainpanel);
//			bottomTabs.addTab("Right Side", c1panel);
//			bottomTabs.addTab("Left Side", c2panel);
//			bottomTabs.addTab("Head + Torso", c3panel);
//			//
////			bottom.add(BorderLayout.CENTER, bottomTabs);
//	}

//	private void gridBagLayoutAddComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int width,
//			int height, double weightx, double weighty) {
//		// function for easier gridbaglayout's
//		GridBagConstraints gbc = new GridBagConstraints();
//		gbc.fill = GridBagConstraints.BOTH;
//		gbc.gridx = x;
//		gbc.gridy = y;
//		gbc.gridwidth = width;
//		gbc.gridheight = height;
//		gbc.weightx = weightx;
//		gbc.weighty = weighty;
//		gbl.setConstraints(c, gbc);
//		cont.add(c);
//	}

//	private void customizeslider(JSlider slider, final int t1, final int t2, int[] minresmaxpos11) {
//		// preset the slider
//		slider.setOrientation(SwingConstants.VERTICAL);
//		slider.setMinimum(minresmaxpos11[0]);
//		slider.setMaximum(minresmaxpos11[2]);
//		slider.setMajorTickSpacing(20);
//		slider.setMinorTickSpacing(1);
//		slider.createStandardLabels(1);
//		slider.setPaintTicks(true);
//		slider.setPaintLabels(true);
//		slider.setValue((minresmaxpos11[0] + minresmaxpos11[2]) / 2);
//
//		slider.addChangeListener(new ChangeListener() {
//
//			@Override
//			public void stateChanged(ChangeEvent ce) {
////				swingGui.send(boundServiceName, "servoitemholder_slider_changed", t1, t2);
//				servoitemholder_slider_changed(t1, t2);
//			}
//		});
//	}

//	private void sliderSetUp(JSlider slider, final int t1, final int t2, int[] minresmaxpos11, int value) {
//		// preset the slider
//		slider.setOrientation(SwingConstants.VERTICAL);
//		slider.setMinimum(minresmaxpos11[0]);
//		slider.setMaximum(minresmaxpos11[2]);
//		slider.setMajorTickSpacing(20);
//		slider.setMinorTickSpacing(1);
//		slider.createStandardLabels(1);
//		slider.setPaintTicks(true);
//		slider.setPaintLabels(true);
//		slider.setValue(value);
//
//		slider.addChangeListener(new ChangeListener() {
//
//			@Override
//			public void stateChanged(ChangeEvent ce) {
////				swingGui.send(boundServiceName, "servoitemholder_slider_changed", t1, t2);
//				servoitemholder_slider_changed(t1, t2);
//			}
//		});
//	}

//	public static void main(String[] args) throws InterruptedException {
//		LoggingFactory.init(Level.INFO);
//		try {
//			Runtime.start("gui", "SwingGui");
//			Runtime.start("inmoovgesturecreator", "InMoovGestureCreator");
//		} catch (Exception e) {
//			Logging.logError(e);
//		}
//	}
	
//	public void control_addgest(JList<String> gestureList, JTextField control_gestname, JTextField control_funcname) {
//		// Add the current gesture to the script (button bottom-left)
//		String defname = ime_funkcije = control_funcname.getText();
//		String gestname = ime_gest = control_gestname.getText();
//
//		String code = "";
//		for (Frame fih : frames) {
//			String code1;
//			if (fih.getSleep() != -1) {
//				code1 = "    sleep(" + fih.getSleep() + ")\n";
//			} else if (fih.getSpeech() != null) {
//				code1 = "    " + pythonname + ".mouth.speakBlocking(\"" + fih.getSpeech() + "\")\n";
//			} else if (fih.getName() != null) {
//				String code11 = "";
//				String code12 = "";
//				String code13 = "";
//				String code14 = "";
//				String code15 = "";
//				String code16 = "";
//				if (fih.getRightHandMoveSet()) {
//					code11 = "    " + pythonname + ".moveHead(" + fih.getNeckMove() + "," + fih.getHeadRotateMove()
//							+ "," + fih.getEyeXMove() + "," + fih.getEyeYMove() + "," + fih.getJawMove() + ")\n";
//				}
//				if (fih.getRightArmMoveSet()) {
//					code12 = "    " + pythonname + ".moveArm(\"left\"," + fih.getLeftBicepsMove() + ","
//							+ fih.getLeftRotateMove() + "," + fih.getLeftShoulderMove() + ","
//							+ fih.getLeftOmoplateMove() + ")\n";
//				}
//				if (fih.getLeftHandMoveSet()) {
//					code13 = "    " + pythonname + ".moveArm(\"right\"," + fih.getRightBicepsMove() + ","
//							+ fih.getRightRotateMove() + "," + fih.getRightShoulderMove() + ","
//							+ fih.getRightOmoplateMove() + ")\n";
//				}
//				if (fih.getLeftArmMoveSet()) {
//					code14 = "    " + pythonname + ".moveHand(\"left\"," + fih.getLeftThumbFingerMove() + ","
//							+ fih.getLeftIndexFingerMove() + "," + fih.getLeftMajeureFingerMove() + ","
//							+ fih.getLeftRingFingerMove() + "," + fih.getLeftPinkyFingerMove() + ","
//							+ fih.getLeftWristMove() + ")\n";
//				}
//				if (fih.getHeadMoveSet()) {
//					code15 = "    " + pythonname + ".moveHand(\"right\"," + fih.getRightThumbFingerMove() + ","
//							+ fih.getRightIndexFingerMove() + "," + fih.getRightMajeureFingerMove() + ","
//							+ fih.getRightRingFingerMove() + "," + fih.getRightPinkyFingerMove() + ","
//							+ fih.getRightWristMove() + ")\n";
//				}
//				if (fih.getTorsoMoveSet()) {
//					code16 = "    " + pythonname + ".moveTorso(" + fih.getTopStomMove() + "," + fih.getMidStomMove()
//							+ "," + fih.getLowStomMove() + ")\n";
//				}
//				code1 = code11 + code12 + code13 + code14 + code15 + code16;
//			} else {
//				String code11 = "";
//				String code12 = "";
//				String code13 = "";
//				String code14 = "";
//				String code15 = "";
//				String code16 = "";
//				if (fih.getRightHandMoveSet()) {
//					code11 = "    " + pythonname + ".setHeadSpeed(" + fih.getNeckSpeed() + ","
//							+ fih.getHeadRotateSpeed() + "," + fih.getEyeXSpeed() + "," + fih.getEyeYSpeed() + ","
//							+ fih.getJawSpeed() + ")\n";
//				}
//				if (fih.getRightArmMoveSet()) {
//					code12 = "    " + pythonname + ".setArmSpeed(\"left\"," + fih.getLeftBicepsSpeed() + ","
//							+ fih.getLeftRotateSpeed() + "," + fih.getLeftShoulderSpeed() + ","
//							+ fih.getLeftOmoplateSpeed() + ")\n";
//				}
//				if (fih.getLeftHandMoveSet()) {
//					code13 = "    " + pythonname + ".setArmSpeed(\"right\"," + fih.getRightBicepsSpeed() + ","
//							+ fih.getRightRotateSpeed() + "," + fih.getRightShoulderSpeed() + ","
//							+ fih.getRightOmoplateSpeed() + ")\n";
//				}
//				if (fih.getLeftArmMoveSet()) {
//					code14 = "    " + pythonname + ".setHandSpeed(\"left\"," + fih.getLeftThumbFingerSpeed() + ","
//							+ fih.getLeftIndexFingerSpeed() + "," + fih.getLeftMajeureFingerSpeed() + ","
//							+ fih.getLeftRingFingerSpeed() + "," + fih.getLeftPinkyFingerSpeed() + ","
//							+ fih.getLeftWristSpeed() + ")\n";
//				}
//				if (fih.getHeadMoveSet()) {
//					code15 = "    " + pythonname + ".setHandSpeed(\"right\"," + fih.getRightThumbFingerSpeed() + ","
//							+ fih.getRightIndexFingerSpeed() + "," + fih.getRightMajeureFingerSpeed() + ","
//							+ fih.getRightRingFingerSpeed() + "," + fih.getRightPinkyFingerSpeed() + ","
//							+ fih.getRightWristSpeed() + ")\n";
//				}
//				if (fih.getTorsoMoveSet()) {
//					code16 = "    " + pythonname + ".setTorsoSpeed(" + fih.getTopStomSpeed() + ","
//							+ fih.getMidStomSpeed() + "," + fih.getLowStomSpeed() + ")\n";
//				}
//				code1 = code11 + code12 + code13 + code14 + code15 + code16;
//			}
//			code = code + code1;
//		}
//		String finalcode = "def " + defname + "():\n" + code;
//
//		String insert = "ear.addCommand(\"" + gestname + "\", \"python\", \"" + defname + "\")";
//		int posear = pythonscript.lastIndexOf("ear.addCommand");
//		int pos = pythonscript.indexOf("\n", posear);
//		pythonscript = pythonscript.substring(0, pos) + "\n" + insert
//				+ pythonscript.substring(pos, pythonscript.length());
//
//		pythonscript = pythonscript + "\n" + finalcode;
//
//		parsescript(gestureList);
//	}

//	public void control_loadgest(JList<String> gestureList, JList<String> frameList, JTextField gestureName,
//			JTextField control_funcname) {
//		// Load the current gesture from the script (button bottom-left)
//		int posl = gestureList.getSelectedIndex();
//
//		if (posl != -1) {
//			if (pythonitemholder.get(posl).modifyable) {
//				frames.clear();
//
//				String defname = null;
//
//				String code = pythonitemholder.get(posl).code;
//				String[] codesplit = code.split("\n");
//				Frame fih = null;
//				boolean ismove = false;
//				boolean isspeed = false;
//				boolean head = false;
//				boolean rhand = false;
//				boolean lhand = false;
//				boolean rarm = false;
//				boolean larm = false;
//				boolean torso = false;
//				boolean keepgoing = true;
//				int pos = 0;
//				while (keepgoing) {
//					if (fih == null) {
//						fih = new Frame();
//					}
//					String line;
//					if (pos < codesplit.length) {
//						line = codesplit[pos];
//					} else {
//						line = "pweicmfh - only one run";
//						keepgoing = false;
//					}
//					String linewithoutspace = line.replace(" ", "");
//					if (linewithoutspace.equals("")) {
//						pos++;
//						continue;
//					}
//					String line2 = line.replace(" ", "");
//					if (!(ismove) && !(isspeed)) {
//						if (line2.startsWith("def")) {
//							String defn = line.substring(line.indexOf(" ") + 1, line.lastIndexOf("():"));
//							defname = defn;
//							pos++;
//						} else if (line2.startsWith("sleep")) {
//							String sleeptime = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
//							fih.setSleep(Integer.parseInt(sleeptime));
//							fih.setSpeech(null);
//							fih.setName(null);
//							frames.add(fih);
//							fih = null;
//							pos++;
//						} else if (line2.startsWith(pythonname)) {
//							if (line2.startsWith(pythonname + ".mouth.speak")) {
//								fih.setSleep(-1);
//								fih.setSpeech(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
//								fih.setName(null);
//								frames.add(fih);
//								fih = null;
//								pos++;
//							} else if (line2.startsWith(pythonname + ".move")) {
//								ismove = true;
//								String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
//								String[] goodsplit = good.split(",");
//								if (line2.startsWith(pythonname + ".moveHead")) {
//									fih.setNeckMove(Integer.parseInt(goodsplit[0]));
//									fih.setHeadRotateMove(Integer.parseInt(goodsplit[1]));
//									if (goodsplit.length > 2) {
//										fih.setEyeXMove(Integer.parseInt(goodsplit[2]));
//										fih.setEyeYMove(Integer.parseInt(goodsplit[3]));
//										fih.setJawMove(Integer.parseInt(goodsplit[4]));
//									} else {
//										fih.setEyeXMove(90);
//										fih.setEyeYMove(90);
//										fih.setJawMove(90);
//									}
//									head = true;
//									pos++;
//								} else if (line2.startsWith(pythonname + ".moveHand")) {
//									String gs = goodsplit[0];
//									String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
//									if (side.equals("right")) {
//										fih.setRightThumbFingerMove(Integer.parseInt(goodsplit[1]));
//										fih.setRightIndexFingerMove(Integer.parseInt(goodsplit[2]));
//										fih.setRightMajeureFingerMove(Integer.parseInt(goodsplit[3]));
//										fih.setRightRingFingerMove(Integer.parseInt(goodsplit[4]));
//										fih.setRightPinkyFingerMove(Integer.parseInt(goodsplit[5]));
//										if (goodsplit.length > 6) {
//											fih.setRightWristMove(Integer.parseInt(goodsplit[6]));
//										} else {
//											fih.setRightWristMove(90);
//										}
//										rhand = true;
//										pos++;
//									} else if (side.equals("left")) {
//										fih.setLeftThumbFingerMove(Integer.parseInt(goodsplit[1]));
//										fih.setLeftIndexFingerMove(Integer.parseInt(goodsplit[2]));
//										fih.setLeftMajeureFingerMove(Integer.parseInt(goodsplit[3]));
//										fih.setLeftRingFingerMove(Integer.parseInt(goodsplit[4]));
//										fih.setLeftPinkyFingerMove(Integer.parseInt(goodsplit[5]));
//										if (goodsplit.length > 6) {
//											fih.setLeftWristMove(Integer.parseInt(goodsplit[6]));
//										} else {
//											fih.setLeftWristMove(90);
//										}
//										lhand = true;
//										pos++;
//									}
//								} else if (line2.startsWith(pythonname + ".moveArm")) {
//									String gs = goodsplit[0];
//									String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
//									if (side.equals("right")) {
//										fih.setRightBicepsMove(Integer.parseInt(goodsplit[1]));
//										fih.setRightRotateMove(Integer.parseInt(goodsplit[2]));
//										fih.setRightShoulderMove(Integer.parseInt(goodsplit[3]));
//										fih.setRightOmoplateMove(Integer.parseInt(goodsplit[4]));
//										rarm = true;
//										pos++;
//									} else if (side.equals("left")) {
//										fih.setLeftBicepsMove(Integer.parseInt(goodsplit[1]));
//										fih.setLeftRotateMove(Integer.parseInt(goodsplit[2]));
//										fih.setLeftShoulderMove(Integer.parseInt(goodsplit[3]));
//										fih.setLeftOmoplateMove(Integer.parseInt(goodsplit[4]));
//										larm = true;
//										pos++;
//									}
//								} else if (line2.startsWith(pythonname + ".moveTorso")) {
//									fih.setTopStomMove(Integer.parseInt(goodsplit[0]));
//									fih.setMidStomMove(Integer.parseInt(goodsplit[1]));
//									fih.setLowStomMove(Integer.parseInt(goodsplit[2]));
//									torso = true;
//									pos++;
//								}
//							} else if (line2.startsWith(pythonname + ".set")) {
//								isspeed = true;
//								String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
//								String[] goodsplit = good.split(",");
//								if (line2.startsWith(pythonname + ".setHeadSpeed")) {
//									fih.setNeckSpeed(Double.parseDouble(goodsplit[0]));
//									fih.setHeadRotateSpeed(Double.parseDouble(goodsplit[1]));
//									if (goodsplit.length > 2) {
//										fih.setEyeXSpeed(Double.parseDouble(goodsplit[2]));
//										fih.setEyeYSpeed(Double.parseDouble(goodsplit[3]));
//										fih.setJawSpeed(Double.parseDouble(goodsplit[4]));
//									} else {
//										fih.setEyeXSpeed(1.0d);
//										fih.setEyeYSpeed(1.0d);
//										fih.setJawSpeed(1.0d);
//									}
//									head = true;
//									pos++;
//								} else if (line2.startsWith(pythonname + ".setHandSpeed")) {
//									String gs = goodsplit[0];
//									String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
//									if (side.equals("right")) {
//										fih.setRightThumbFingerSpeed(Double.parseDouble(goodsplit[1]));
//										fih.setRightIndexFingerSpeed(Double.parseDouble(goodsplit[2]));
//										fih.setRightMajeureFingerSpeed(Double.parseDouble(goodsplit[3]));
//										fih.setRightRingFingerSpeed(Double.parseDouble(goodsplit[4]));
//										fih.setRightPinkyFingerSpeed(Double.parseDouble(goodsplit[5]));
//										if (goodsplit.length > 6) {
//											fih.setRightWristSpeed(Double.parseDouble(goodsplit[6]));
//										} else {
//											fih.setRightWristSpeed(1.0d);
//										}
//										rhand = true;
//										pos++;
//									} else if (side.equals("left")) {
//										fih.setLeftThumbFingerSpeed(Double.parseDouble(goodsplit[1]));
//										fih.setLeftIndexFingerSpeed(Double.parseDouble(goodsplit[2]));
//										fih.setLeftMajeureFingerSpeed(Double.parseDouble(goodsplit[3]));
//										fih.setLeftRingFingerSpeed(Double.parseDouble(goodsplit[4]));
//										fih.setLeftPinkyFingerSpeed(Double.parseDouble(goodsplit[5]));
//										if (goodsplit.length > 6) {
//											fih.setLeftWristSpeed(Double.parseDouble(goodsplit[6]));
//										} else {
//											fih.setLeftWristSpeed(1.0d);
//										}
//										lhand = true;
//										pos++;
//									}
//								} else if (line2.startsWith(pythonname + ".setArmSpeed")) {
//									String gs = goodsplit[0];
//									String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
//									if (side.equals("right")) {
//										fih.setRightBicepsSpeed(Double.parseDouble(goodsplit[1]));
//										fih.setRightRotateSpeed(Double.parseDouble(goodsplit[2]));
//										fih.setRightShoulderSpeed(Double.parseDouble(goodsplit[3]));
//										fih.setRightOmoplateSpeed(Double.parseDouble(goodsplit[4]));
//										rarm = true;
//										pos++;
//									} else if (side.equals("left")) {
//										fih.setLeftBicepsSpeed(Double.parseDouble(goodsplit[1]));
//										fih.setLeftRotateSpeed(Double.parseDouble(goodsplit[2]));
//										fih.setLeftShoulderSpeed(Double.parseDouble(goodsplit[3]));
//										fih.setLeftOmoplateSpeed(Double.parseDouble(goodsplit[4]));
//										larm = true;
//										pos++;
//									}
//								} else if (line2.startsWith(pythonname + ".setTorsoSpeed")) {
//									fih.setTopStomSpeed(Double.parseDouble(goodsplit[0]));
//									fih.setMidStomSpeed(Double.parseDouble(goodsplit[1]));
//									fih.setLowStomSpeed(Double.parseDouble(goodsplit[2]));
//									torso = true;
//									pos++;
//								}
//							}
//						}
//					} else if (ismove && !(isspeed)) {
//						if (line2.startsWith(pythonname + ".move")) {
//							String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
//							String[] goodsplit = good.split(",");
//							if (line2.startsWith(pythonname + ".moveHead")) {
//								fih.setNeckMove(Integer.parseInt(goodsplit[0]));
//								fih.setHeadRotateMove(Integer.parseInt(goodsplit[1]));
//								if (goodsplit.length > 2) {
//									fih.setEyeXMove(Integer.parseInt(goodsplit[2]));
//									fih.setEyeYMove(Integer.parseInt(goodsplit[3]));
//									fih.setJawMove(Integer.parseInt(goodsplit[4]));
//								} else {
//									fih.setEyeXMove(90);
//									fih.setEyeYMove(90);
//									fih.setJawMove(90);
//								}
//								head = true;
//								pos++;
//							} else if (line2.startsWith(pythonname + ".moveHand")) {
//								String gs = goodsplit[0];
//								String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
//								if (side.equals("right")) {
//									fih.setRightThumbFingerMove(Integer.parseInt(goodsplit[1]));
//									fih.setRightIndexFingerMove(Integer.parseInt(goodsplit[2]));
//									fih.setRightMajeureFingerMove(Integer.parseInt(goodsplit[3]));
//									fih.setRightRingFingerMove(Integer.parseInt(goodsplit[4]));
//									fih.setRightPinkyFingerMove(Integer.parseInt(goodsplit[5]));
//									if (goodsplit.length > 6) {
//										fih.setRightWristMove(Integer.parseInt(goodsplit[6]));
//									} else {
//										fih.setRightWristMove(90);
//									}
//									rhand = true;
//									pos++;
//								} else if (side.equals("left")) {
//									fih.setLeftThumbFingerMove(Integer.parseInt(goodsplit[1]));
//									fih.setLeftIndexFingerMove(Integer.parseInt(goodsplit[2]));
//									fih.setLeftMajeureFingerMove(Integer.parseInt(goodsplit[3]));
//									fih.setLeftRingFingerMove(Integer.parseInt(goodsplit[4]));
//									fih.setLeftPinkyFingerMove(Integer.parseInt(goodsplit[5]));
//									if (goodsplit.length > 6) {
//										fih.setLeftWristMove(Integer.parseInt(goodsplit[6]));
//									} else {
//										fih.setLeftWristMove(90);
//									}
//									lhand = true;
//									pos++;
//								}
//							} else if (line2.startsWith(pythonname + ".moveArm")) {
//								String gs = goodsplit[0];
//								String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
//								if (side.equals("right")) {
//									fih.setRightBicepsMove(Integer.parseInt(goodsplit[1]));
//									fih.setRightRotateMove(Integer.parseInt(goodsplit[2]));
//									fih.setRightShoulderMove(Integer.parseInt(goodsplit[3]));
//									fih.setRightOmoplateMove(Integer.parseInt(goodsplit[4]));
//									rarm = true;
//									pos++;
//								} else if (side.equals("left")) {
//									fih.setLeftBicepsMove(Integer.parseInt(goodsplit[1]));
//									fih.setLeftRotateMove(Integer.parseInt(goodsplit[2]));
//									fih.setLeftShoulderMove(Integer.parseInt(goodsplit[3]));
//									fih.setLeftOmoplateMove(Integer.parseInt(goodsplit[4]));
//									larm = true;
//									pos++;
//								}
//							} else if (line2.startsWith(pythonname + ".moveTorso")) {
//								fih.setTopStomMove(Integer.parseInt(goodsplit[0]));
//								fih.setMidStomMove(Integer.parseInt(goodsplit[1]));
//								fih.setLowStomMove(Integer.parseInt(goodsplit[2]));
//								torso = true;
//								pos++;
//							}
//						} else {
//							if (!head) {
//								fih.setNeckMove(90);
//								fih.setHeadRotateMove(90);
//								fih.setEyeXMove(90);
//								fih.setEyeYMove(90);
//								fih.setJawMove(90);
//							}
//							if (!rhand) {
//								fih.setRightThumbFingerMove(90);
//								fih.setRightIndexFingerMove(90);
//								fih.setRightMajeureFingerMove(90);
//								fih.setRightRingFingerMove(90);
//								fih.setRightPinkyFingerMove(90);
//								fih.setRightWristMove(90);
//							}
//							if (!lhand) {
//								fih.setLeftThumbFingerMove(90);
//								fih.setLeftIndexFingerMove(90);
//								fih.setLeftMajeureFingerMove(90);
//								fih.setLeftRingFingerMove(90);
//								fih.setLeftPinkyFingerMove(90);
//								fih.setLeftWristMove(90);
//							}
//							if (!rarm) {
//								fih.setRightBicepsMove(90);
//								fih.setRightRotateMove(90);
//								fih.setRightShoulderMove(90);
//								fih.setRightOmoplateMove(90);
//							}
//							if (!larm) {
//								fih.setLeftBicepsMove(90);
//								fih.setLeftRotateMove(90);
//								fih.setLeftShoulderMove(90);
//								fih.setLeftOmoplateMove(90);
//							}
//							if (!torso) {
//								fih.setTopStomMove(90);
//								fih.setMidStomMove(90);
//								fih.setLowStomMove(90);
//							}
//							fih.setSleep(-1);
//							fih.setSpeech(null);
//							fih.setName("SEQ");
//							frames.add(fih);
//							fih = null;
//							ismove = false;
//							head = false;
//							rhand = false;
//							lhand = false;
//							rarm = false;
//							larm = false;
//							torso = false;
//						}
//					} else if (!(ismove) && isspeed) {
//						if (line2.startsWith(pythonname + ".set")) {
//							String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
//							String[] goodsplit = good.split(",");
//							if (line2.startsWith(pythonname + ".setHeadSpeed")) {
//								fih.setNeckSpeed(Double.parseDouble(goodsplit[0]));
//								fih.setHeadRotateSpeed(Double.parseDouble(goodsplit[1]));
//								if (goodsplit.length > 2) {
//									fih.setEyeXSpeed(Double.parseDouble(goodsplit[2]));
//									fih.setEyeYSpeed(Double.parseDouble(goodsplit[3]));
//									fih.setJawSpeed(Double.parseDouble(goodsplit[4]));
//								} else {
//									fih.setEyeXSpeed(1.0d);
//									fih.setEyeYSpeed(1.0d);
//									fih.setJawSpeed(1.0d);
//								}
//								head = true;
//								pos++;
//							} else if (line2.startsWith(pythonname + ".setHandSpeed")) {
//								String gs = goodsplit[0];
//								String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
//								if (side.equals("right")) {
//									fih.setRightThumbFingerSpeed(Double.parseDouble(goodsplit[1]));
//									fih.setRightIndexFingerSpeed(Double.parseDouble(goodsplit[2]));
//									fih.setRightMajeureFingerSpeed(Double.parseDouble(goodsplit[3]));
//									fih.setRightRingFingerSpeed(Double.parseDouble(goodsplit[4]));
//									fih.setRightPinkyFingerSpeed(Double.parseDouble(goodsplit[5]));
//									if (goodsplit.length > 6) {
//										fih.setRightWristSpeed(Double.parseDouble(goodsplit[6]));
//									} else {
//										fih.setRightWristSpeed(1.0d);
//									}
//									rhand = true;
//									pos++;
//								} else if (side.equals("left")) {
//									fih.setLeftThumbFingerSpeed(Double.parseDouble(goodsplit[1]));
//									fih.setLeftIndexFingerSpeed(Double.parseDouble(goodsplit[2]));
//									fih.setLeftMajeureFingerSpeed(Double.parseDouble(goodsplit[3]));
//									fih.setLeftRingFingerSpeed(Double.parseDouble(goodsplit[4]));
//									fih.setLeftPinkyFingerSpeed(Double.parseDouble(goodsplit[5]));
//									if (goodsplit.length > 6) {
//										fih.setLeftWristSpeed(Double.parseDouble(goodsplit[6]));
//									} else {
//										fih.setLeftWristSpeed(1.0d);
//									}
//									lhand = true;
//									pos++;
//								}
//							} else if (line2.startsWith(pythonname + ".setArmSpeed")) {
//								String gs = goodsplit[0];
//								String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
//								if (side.equals("right")) {
//									fih.setRightBicepsSpeed(Double.parseDouble(goodsplit[1]));
//									fih.setRightRotateSpeed(Double.parseDouble(goodsplit[2]));
//									fih.setRightShoulderSpeed(Double.parseDouble(goodsplit[3]));
//									fih.setRightOmoplateSpeed(Double.parseDouble(goodsplit[4]));
//									rarm = true;
//									pos++;
//								} else if (side.equals("left")) {
//									fih.setLeftBicepsSpeed(Double.parseDouble(goodsplit[1]));
//									fih.setLeftRotateSpeed(Double.parseDouble(goodsplit[2]));
//									fih.setLeftShoulderSpeed(Double.parseDouble(goodsplit[3]));
//									fih.setLeftOmoplateSpeed(Double.parseDouble(goodsplit[4]));
//									larm = true;
//									pos++;
//								}
//							} else if (line2.startsWith(pythonname + ".setTorsoSpeed")) {
//								fih.setTopStomSpeed(Double.parseDouble(goodsplit[0]));
//								fih.setMidStomSpeed(Double.parseDouble(goodsplit[1]));
//								fih.setLowStomSpeed(Double.parseDouble(goodsplit[2]));
//								torso = true;
//								pos++;
//							}
//						} else {
//							if (!head) {
//								fih.setNeckSpeed(1.0d);
//								fih.setHeadRotateSpeed(1.0d);
//								fih.setEyeXSpeed(1.0d);
//								fih.setEyeYSpeed(1.0d);
//								fih.setJawSpeed(1.0d);
//							}
//							if (!rhand) {
//								fih.setRightThumbFingerSpeed(1.0d);
//								fih.setRightIndexFingerSpeed(1.0d);
//								fih.setRightMajeureFingerSpeed(1.0d);
//								fih.setRightRingFingerSpeed(1.0d);
//								fih.setRightPinkyFingerSpeed(1.0d);
//								fih.setRightWristSpeed(1.0d);
//							}
//							if (!lhand) {
//								fih.setLeftThumbFingerSpeed(1.0d);
//								fih.setLeftIndexFingerSpeed(1.0d);
//								fih.setLeftMajeureFingerSpeed(1.0d);
//								fih.setLeftRingFingerSpeed(1.0d);
//								fih.setLeftPinkyFingerSpeed(1.0d);
//								fih.setLeftWristSpeed(1.0d);
//							}
//							if (!rarm) {
//								fih.setRightBicepsSpeed(1.0d);
//								fih.setRightRotateSpeed(1.0d);
//								fih.setRightShoulderSpeed(1.0d);
//								fih.setRightOmoplateSpeed(1.0d);
//							}
//							if (!larm) {
//								fih.setLeftBicepsSpeed(1.0d);
//								fih.setLeftRotateSpeed(1.0d);
//								fih.setLeftShoulderSpeed(1.0d);
//								fih.setLeftOmoplateSpeed(1.0d);
//							}
//							if (!torso) {
//								fih.setTopStomSpeed(1.0d);
//								fih.setMidStomSpeed(1.0d);
//								fih.setLowStomSpeed(1.0d);
//							}
//							fih.setSleep(-1);
//							fih.setSpeech(null);
//							fih.setName(null);
//							frames.add(fih);
//							fih = null;
//							isspeed = false;
//							head = false;
//							rhand = false;
//							lhand = false;
//							rarm = false;
//							larm = false;
//							torso = false;
//						}
//					} else {
//						// this shouldn't be reached
//						// ismove & isspeed true
//						// wrong
//					}
//				}
//
//				frameListReload(frameList);
//
//				int defnamepos = pythonscript.indexOf(defname);
//				int earpos1 = pythonscript.lastIndexOf("\n", defnamepos);
//				int earpos2 = pythonscript.indexOf("\n", defnamepos);
//				String earline = pythonscript.substring(earpos1 + 1, earpos2);
//				if (earline.startsWith("ear.addCommand")) {
//					String good = earline.substring(earline.indexOf("("), earline.lastIndexOf(")"));
//					String[] goodsplit = good.split(",");
//
//					String funcnamedirty = goodsplit[0];
//					String funcname = funcnamedirty.substring(funcnamedirty.indexOf("\"") + 1,
//							funcnamedirty.lastIndexOf("\""));
//
//					gestureName.setText(funcname);
////					control_funcname.setText(defname);
//				}
//			}
//		}
//	}

//	public void control_removegest(JList<String> gestureList) {
//		// Remove the selected gesture from the script (button bottom-left)
//		int posl = gestureList.getSelectedIndex();
//
//		if (posl != -1) {
//
//			if (pythonitemholder.get(posl).function && !pythonitemholder.get(posl).notfunction) {
//
//				String codeold = pythonitemholder.get(posl).code;
//				String defnameold = codeold.substring(codeold.indexOf("def ") + 4, codeold.indexOf("():"));
//
//				int olddefpos = pythonscript.indexOf(defnameold);
//				int pos1 = pythonscript.lastIndexOf("\n", olddefpos);
//				int pos2 = pythonscript.indexOf("\n", olddefpos);
//				pythonscript = pythonscript.substring(0, pos1) + pythonscript.substring(pos2, pythonscript.length());
//
//				int posscript = pythonscript.lastIndexOf(defnameold);
//				int posscriptnextdef = pythonscript.indexOf("def", posscript);
//				if (posscriptnextdef == -1) {
//					posscriptnextdef = pythonscript.length();
//				}
//
//				pythonscript = pythonscript.substring(0, posscript - 4)
//						+ pythonscript.substring(posscriptnextdef - 1, pythonscript.length());
//
//				parsescript(gestureList);
//			}
//		}
//	}

//	public void control_updategest(JList<String> gestureList, JTextField gestureName, JTextField control_funcname) {
//		// Update the current gesture in the script (button bottom-left)
//		int posl = gestureList.getSelectedIndex();
//
//		if (posl != -1) {
//
//			if (pythonitemholder.get(posl).function && !pythonitemholder.get(posl).notfunction) {
//
//				String codeold = pythonitemholder.get(posl).code;
//				String defnameold = codeold.substring(codeold.indexOf("def ") + 4, codeold.indexOf("():"));
//
//				String defname = control_funcname.getText();
//				String gestname = gestureName.getText();
//
//				String code = "";
//				for (Frame fih : frames) {
//					String code1;
//					if (fih.getSleep() != -1) {
//						code1 = "    sleep(" + fih.getSleep() + ")\n";
//					} else if (fih.getSpeech() != null) {
//						code1 = "    " + pythonname + ".mouth.speakBlocking(\"" + fih.getSpeech() + "\")\n";
//					} else if (fih.getName() != null) {
//						String code11 = "";
//						String code12 = "";
//						String code13 = "";
//						String code14 = "";
//						String code15 = "";
//						String code16 = "";
//						if (fih.getRightHandMoveSet()) {
//							code11 = "    " + pythonname + ".moveHead(" + fih.getNeckMove() + ","
//									+ fih.getHeadRotateMove() + "," + fih.getEyeXMove() + "," + fih.getEyeYMove() + ","
//									+ fih.getJawMove() + ")\n";
//						}
//						if (fih.getRightArmMoveSet()) {
//							code12 = "    " + pythonname + ".moveArm(\"left\"," + fih.getLeftBicepsMove() + ","
//									+ fih.getLeftRotateMove() + "," + fih.getLeftShoulderMove() + ","
//									+ fih.getLeftOmoplateMove() + ")\n";
//						}
//						if (fih.getLeftHandMoveSet()) {
//							code13 = "    " + pythonname + ".moveArm(\"right\"," + fih.getRightBicepsMove() + ","
//									+ fih.getRightRotateMove() + "," + fih.getRightShoulderMove() + ","
//									+ fih.getRightOmoplateMove() + ")\n";
//						}
//						if (fih.getLeftArmMoveSet()) {
//							code14 = "    " + pythonname + ".moveHand(\"left\"," + fih.getLeftThumbFingerMove() + ","
//									+ fih.getLeftIndexFingerMove() + "," + fih.getLeftMajeureFingerMove() + ","
//									+ fih.getLeftRingFingerMove() + "," + fih.getLeftPinkyFingerMove() + ","
//									+ fih.getLeftWristMove() + ")\n";
//						}
//						if (fih.getHeadMoveSet()) {
//							code15 = "    " + pythonname + ".moveHand(\"right\"," + fih.getRightThumbFingerMove() + ","
//									+ fih.getRightIndexFingerMove() + "," + fih.getRightMajeureFingerMove() + ","
//									+ fih.getRightRingFingerMove() + "," + fih.getRightPinkyFingerMove() + ","
//									+ fih.getRightWristMove() + ")\n";
//						}
//						if (fih.getTorsoMoveSet()) {
//							code16 = "    " + pythonname + ".moveTorso(" + fih.getTopStomMove() + ","
//									+ fih.getMidStomMove() + "," + fih.getLowStomMove() + ")\n";
//						}
//						code1 = code11 + code12 + code13 + code14 + code15 + code16;
//					} else {
//						String code11 = "";
//						String code12 = "";
//						String code13 = "";
//						String code14 = "";
//						String code15 = "";
//						String code16 = "";
//						if (fih.getRightHandMoveSet()) {
//							code11 = "    " + pythonname + ".setHeadSpeed(" + fih.getNeckSpeed() + ","
//									+ fih.getHeadRotateSpeed() + "," + fih.getEyeXSpeed() + "," + fih.getEyeYSpeed()
//									+ "," + fih.getJawSpeed() + ")\n";
//						}
//						if (fih.getRightArmMoveSet()) {
//							code12 = "    " + pythonname + ".setArmSpeed(\"left\"," + fih.getLeftBicepsSpeed() + ","
//									+ fih.getLeftRotateSpeed() + "," + fih.getLeftShoulderSpeed() + ","
//									+ fih.getLeftOmoplateSpeed() + ")\n";
//						}
//
//						if (fih.getLeftHandMoveSet()) {
//							code13 = "    " + pythonname + ".setArmSpeed(\"right\"," + fih.getRightBicepsSpeed() + ","
//									+ fih.getRightRotateSpeed() + "," + fih.getRightShoulderSpeed() + ","
//									+ fih.getRightOmoplateSpeed() + ")\n";
//						}
//						if (fih.getLeftArmMoveSet()) {
//							code14 = "    " + pythonname + ".setHandSpeed(\"left\"," + fih.getLeftThumbFingerSpeed()
//									+ "," + fih.getLeftIndexFingerSpeed() + "," + fih.getLeftMajeureFingerSpeed() + ","
//									+ fih.getLeftRingFingerSpeed() + "," + fih.getLeftPinkyFingerSpeed() + ","
//									+ fih.getLeftWristSpeed() + ")\n";
//						}
//						if (fih.getHeadMoveSet()) {
//							code15 = "    " + pythonname + ".setHandSpeed(\"right\"," + fih.getRightThumbFingerSpeed()
//									+ "," + fih.getRightIndexFingerSpeed() + "," + fih.getRightMajeureFingerSpeed()
//									+ "," + fih.getRightRingFingerSpeed() + "," + fih.getRightPinkyFingerSpeed() + ","
//									+ fih.getRightWristSpeed() + ")\n";
//						}
//						if (fih.getTorsoMoveSet()) {
//							code16 = "    " + pythonname + ".setTorsoSpeed(" + fih.getTopStomSpeed() + ","
//									+ fih.getMidStomSpeed() + "," + fih.getLowStomSpeed() + ")\n";
//						}
//						code1 = code11 + code12 + code13 + code14 + code15 + code16;
//					}
//					code = code + code1;
//				}
//				String finalcode = "def " + defname + "():\n" + code;
//
//				String insert = "ear.addCommand(\"" + gestname + "\", \"python\", \"" + defname + "\")";
//				int olddefpos = pythonscript.indexOf(defnameold);
//				int pos1 = pythonscript.lastIndexOf("\n", olddefpos);
//				int pos2 = pythonscript.indexOf("\n", olddefpos);
//				pythonscript = pythonscript.substring(0, pos1) + "\n" + insert
//						+ pythonscript.substring(pos2, pythonscript.length());
//
//				int posscript = pythonscript.lastIndexOf(defnameold);
//				int posscriptnextdef = pythonscript.indexOf("def", posscript);
//				if (posscriptnextdef == -1) {
//					posscriptnextdef = pythonscript.length();
//				}
//
//				pythonscript = pythonscript.substring(0, posscript - 4) + "\n" + finalcode
//						+ pythonscript.substring(posscriptnextdef - 1, pythonscript.length());
//
//				parsescript(gestureList);
//			}
//		}
//	}

//	public void frame_load(JList<String> frameList, JTextField frame_add_textfield, JTextField frame_addsleep_textfield,
//			JTextField frame_addspeech_textfield) {
//		// Load this frame from the frameList (button bottom-right)
//		int pos = frameList.getSelectedIndex();
//
//		if (pos != -1) {
//
//			// sleep || speech || servo movement || speed setting
//			if (frames.get(pos).getSleep() != -1) {
//				frame_addsleep_textfield.setText(frames.get(pos).getSleep() + "");
//			} else if (frames.get(pos).getSpeech() != null) {
//				frame_addspeech_textfield.setText(frames.get(pos).getSpeech());
//			} else if (frames.get(pos).getName() != null) {
//				servoitemholder[0][0].sli.setValue(frames.get(pos).getRightThumbFingerMove());
//				servoitemholder[0][1].sli.setValue(frames.get(pos).getRightIndexFingerMove());
//				servoitemholder[0][2].sli.setValue(frames.get(pos).getRightMajeureFingerMove());
//				servoitemholder[0][3].sli.setValue(frames.get(pos).getRightRingFingerMove());
//				servoitemholder[0][4].sli.setValue(frames.get(pos).getRightPinkyFingerMove());
//				servoitemholder[0][5].sli.setValue(frames.get(pos).getRightWristMove());
//
//				servoitemholder[1][0].sli.setValue(frames.get(pos).getRightBicepsMove());
//				servoitemholder[1][1].sli.setValue(frames.get(pos).getRightRotateMove());
//				servoitemholder[1][2].sli.setValue(frames.get(pos).getRightShoulderMove());
//				servoitemholder[1][3].sli.setValue(frames.get(pos).getRightOmoplateMove());
//
//				servoitemholder[2][0].sli.setValue(frames.get(pos).getLeftThumbFingerMove());
//				servoitemholder[2][1].sli.setValue(frames.get(pos).getLeftIndexFingerMove());
//				servoitemholder[2][2].sli.setValue(frames.get(pos).getLeftMajeureFingerMove());
//				servoitemholder[2][3].sli.setValue(frames.get(pos).getLeftRingFingerMove());
//				servoitemholder[2][4].sli.setValue(frames.get(pos).getLeftPinkyFingerMove());
//				servoitemholder[2][5].sli.setValue(frames.get(pos).getLeftWristMove());
//
//				servoitemholder[3][0].sli.setValue(frames.get(pos).getLeftBicepsMove());
//				servoitemholder[3][1].sli.setValue(frames.get(pos).getLeftRotateMove());
//				servoitemholder[3][2].sli.setValue(frames.get(pos).getLeftShoulderMove());
//				servoitemholder[3][3].sli.setValue(frames.get(pos).getLeftOmoplateMove());
//
//				servoitemholder[4][0].sli.setValue(frames.get(pos).getNeckMove());
//				servoitemholder[4][1].sli.setValue(frames.get(pos).getHeadRotateMove());
//				servoitemholder[4][2].sli.setValue(frames.get(pos).getEyeXMove());
//				servoitemholder[4][3].sli.setValue(frames.get(pos).getEyeYMove());
//				servoitemholder[4][4].sli.setValue(frames.get(pos).getJawMove());
//
//				servoitemholder[5][0].sli.setValue(frames.get(pos).getTopStomMove());
//				servoitemholder[5][1].sli.setValue(frames.get(pos).getMidStomMove());
//				servoitemholder[5][2].sli.setValue(frames.get(pos).getLowStomMove());
//				frame_add_textfield.setText(frames.get(pos).getName());
//			} else {
//				servoitemholder[0][0].spe.setText(frames.get(pos).getRightThumbFingerSpeed() + "");
//				servoitemholder[0][1].spe.setText(frames.get(pos).getRightIndexFingerSpeed() + "");
//				servoitemholder[0][2].spe.setText(frames.get(pos).getRightMajeureFingerSpeed() + "");
//				servoitemholder[0][3].spe.setText(frames.get(pos).getRightRingFingerSpeed() + "");
//				servoitemholder[0][4].spe.setText(frames.get(pos).getRightPinkyFingerSpeed() + "");
//				servoitemholder[0][5].spe.setText(frames.get(pos).getRightWristSpeed() + "");
//
//				servoitemholder[1][0].spe.setText(frames.get(pos).getRightBicepsSpeed() + "");
//				servoitemholder[1][1].spe.setText(frames.get(pos).getRightRotateSpeed() + "");
//				servoitemholder[1][2].spe.setText(frames.get(pos).getRightShoulderSpeed() + "");
//				servoitemholder[1][3].spe.setText(frames.get(pos).getRightOmoplateSpeed() + "");
//
//				servoitemholder[2][0].spe.setText(frames.get(pos).getLeftThumbFingerSpeed() + "");
//				servoitemholder[2][1].spe.setText(frames.get(pos).getLeftIndexFingerSpeed() + "");
//				servoitemholder[2][2].spe.setText(frames.get(pos).getLeftMajeureFingerSpeed() + "");
//				servoitemholder[2][3].spe.setText(frames.get(pos).getLeftRingFingerSpeed() + "");
//				servoitemholder[2][4].spe.setText(frames.get(pos).getLeftPinkyFingerSpeed() + "");
//				servoitemholder[2][5].spe.setText(frames.get(pos).getLeftWristSpeed() + "");
//
//				servoitemholder[3][0].spe.setText(frames.get(pos).getLeftBicepsSpeed() + "");
//				servoitemholder[3][1].spe.setText(frames.get(pos).getLeftRotateSpeed() + "");
//				servoitemholder[3][2].spe.setText(frames.get(pos).getLeftShoulderSpeed() + "");
//				servoitemholder[3][3].spe.setText(frames.get(pos).getLeftOmoplateSpeed() + "");
//
//				servoitemholder[4][0].spe.setText(frames.get(pos).getNeckSpeed() + "");
//				servoitemholder[4][1].spe.setText(frames.get(pos).getHeadRotateSpeed() + "");
//				servoitemholder[4][2].spe.setText(frames.get(pos).getEyeXSpeed() + "");
//				servoitemholder[4][3].spe.setText(frames.get(pos).getEyeYSpeed() + "");
//				servoitemholder[4][4].spe.setText(frames.get(pos).getJawSpeed() + "");
//
//				servoitemholder[5][0].spe.setText(frames.get(pos).getTopStomSpeed() + "");
//				servoitemholder[5][1].spe.setText(frames.get(pos).getMidStomSpeed() + "");
//				servoitemholder[5][2].spe.setText(frames.get(pos).getLowStomSpeed() + "");
//			}
//		}
//	}

//	public void frame_update(JList<String> frameList, JTextField frame_add_textfield, JTextField frame_addsleep_textfield,
//			JTextField frame_addspeech_textfield) {
//		// Update this frame on the frameList (button bottom-right)
//
//		int pos = frameList.getSelectedIndex();
//
//		if (pos != -1) {
//			Frame fih = new Frame();
//
//			// sleep || speech || servo movement || speed setting
//			if (frames.get(pos).getSleep() != -1) {
//				fih.setSleep(Integer.parseInt(frame_addsleep_textfield.getText()));
//				fih.setSpeech(null);
//				fih.setName(null);
//			} else if (frames.get(pos).getSpeech() != null) {
//				fih.setSleep(-1);
//				fih.setSpeech(frame_addspeech_textfield.getText());
//				fih.setName(null);
//			} else if (frames.get(pos).getName() != null) {
//				fih.setRightThumbFingerMove(servoitemholder[0][0].sli.getValue());
//				fih.setRightIndexFingerMove(servoitemholder[0][1].sli.getValue());
//				fih.setRightMajeureFingerMove(servoitemholder[0][2].sli.getValue());
//				fih.setRightRingFingerMove(servoitemholder[0][3].sli.getValue());
//				fih.setRightPinkyFingerMove(servoitemholder[0][4].sli.getValue());
//				fih.setRightWristMove(servoitemholder[0][5].sli.getValue());
//
//				fih.setRightBicepsMove(servoitemholder[1][0].sli.getValue());
//				fih.setRightRotateMove(servoitemholder[1][1].sli.getValue());
//				fih.setRightShoulderMove(servoitemholder[1][2].sli.getValue());
//				fih.setRightOmoplateMove(servoitemholder[1][3].sli.getValue());
//
//				fih.setLeftThumbFingerMove(servoitemholder[2][0].sli.getValue());
//				fih.setLeftIndexFingerMove(servoitemholder[2][1].sli.getValue());
//				fih.setLeftMajeureFingerMove(servoitemholder[2][2].sli.getValue());
//				fih.setLeftRingFingerMove(servoitemholder[2][3].sli.getValue());
//				fih.setLeftPinkyFingerMove(servoitemholder[2][4].sli.getValue());
//				fih.setLeftWristMove(servoitemholder[2][5].sli.getValue());
//
//				fih.setLeftBicepsMove(servoitemholder[3][0].sli.getValue());
//				fih.setLeftRotateMove(servoitemholder[3][1].sli.getValue());
//				fih.setLeftShoulderMove(servoitemholder[3][2].sli.getValue());
//				fih.setLeftOmoplateMove(servoitemholder[3][3].sli.getValue());
//
//				fih.setNeckMove(servoitemholder[4][0].sli.getValue());
//				fih.setHeadRotateMove(servoitemholder[4][1].sli.getValue());
//				fih.setEyeXMove(servoitemholder[4][2].sli.getValue());
//				fih.setEyeYMove(servoitemholder[4][3].sli.getValue());
//				fih.setJawMove(servoitemholder[4][4].sli.getValue());
//
//				fih.setTopStomMove(servoitemholder[5][0].sli.getValue());
//				fih.setMidStomMove(servoitemholder[5][1].sli.getValue());
//				fih.setLowStomMove(servoitemholder[5][2].sli.getValue());
//
//				fih.setSleep(-1);
//				fih.setSpeech(null);
//				fih.setName(frame_add_textfield.getText());
//			} else {
//				fih.setRightThumbFingerSpeed(Double.parseDouble(servoitemholder[0][0].spe.getText()));
//				fih.setRightIndexFingerSpeed(Double.parseDouble(servoitemholder[0][1].spe.getText()));
//				fih.setRightMajeureFingerSpeed(Double.parseDouble(servoitemholder[0][2].spe.getText()));
//				fih.setRightRingFingerSpeed(Double.parseDouble(servoitemholder[0][3].spe.getText()));
//				fih.setRightPinkyFingerSpeed(Double.parseDouble(servoitemholder[0][4].spe.getText()));
//				fih.setRightWristSpeed(Double.parseDouble(servoitemholder[0][5].spe.getText()));
//
//				fih.setRightBicepsSpeed(Double.parseDouble(servoitemholder[1][0].spe.getText()));
//				fih.setRightRotateSpeed(Double.parseDouble(servoitemholder[1][1].spe.getText()));
//				fih.setRightShoulderSpeed(Double.parseDouble(servoitemholder[1][2].spe.getText()));
//				fih.setRightOmoplateSpeed(Double.parseDouble(servoitemholder[1][3].spe.getText()));
//
//				fih.setLeftThumbFingerSpeed(Double.parseDouble(servoitemholder[2][0].spe.getText()));
//				fih.setLeftIndexFingerSpeed(Double.parseDouble(servoitemholder[2][1].spe.getText()));
//				fih.setLeftMajeureFingerSpeed(Double.parseDouble(servoitemholder[2][2].spe.getText()));
//				fih.setLeftRingFingerSpeed(Double.parseDouble(servoitemholder[2][3].spe.getText()));
//				fih.setLeftPinkyFingerSpeed(Double.parseDouble(servoitemholder[2][4].spe.getText()));
//				fih.setLeftWristSpeed(Double.parseDouble(servoitemholder[2][5].spe.getText()));
//
//				fih.setLeftBicepsSpeed(Double.parseDouble(servoitemholder[3][0].spe.getText()));
//				fih.setLeftRotateSpeed(Double.parseDouble(servoitemholder[3][1].spe.getText()));
//				fih.setLeftShoulderSpeed(Double.parseDouble(servoitemholder[3][2].spe.getText()));
//				fih.setLeftOmoplateSpeed(Double.parseDouble(servoitemholder[3][3].spe.getText()));
//
//				fih.setNeckSpeed(Double.parseDouble(servoitemholder[4][0].spe.getText()));
//				fih.setHeadRotateSpeed(Double.parseDouble(servoitemholder[4][1].spe.getText()));
//				fih.setEyeXSpeed(Double.parseDouble(servoitemholder[4][2].spe.getText()));
//				fih.setEyeYSpeed(Double.parseDouble(servoitemholder[4][3].spe.getText()));
//				fih.setJawSpeed(Double.parseDouble(servoitemholder[4][4].spe.getText()));
//
//				fih.setTopStomSpeed(Double.parseDouble(servoitemholder[5][0].spe.getText()));
//				fih.setMidStomSpeed(Double.parseDouble(servoitemholder[5][1].spe.getText()));
//				fih.setLowStomSpeed(Double.parseDouble(servoitemholder[5][2].spe.getText()));
//
//				fih.setSleep(-1);
//				fih.setSpeech(null);
//				fih.setName(null);
//			}
//			frames.set(pos, fih);
//
//			frameListReload(frameList);
//		}
//	}	

//	public void frame_addsleep(JList<String> frameList, JTextField frame_addsleep_textfield) {
//		// Add a sleep frame to the frameList (button bottom-right)
//		Frame fih = new Frame();
//
//		fih.setSleep(Integer.parseInt(frame_addsleep_textfield.getText()));
//		fih.setSpeech(null);
//		fih.setName(null);
//
//		frames.add(fih);
//
//		frameListReload(frameList);
//	}
//
//	public void frame_addspeech(JList<String> frameList, JTextField frame_addspeech_textfield) {
//		// Add a speech frame to the frameList (button bottom-right)
//		Frame fih = new Frame();
//
//		fih.setSleep(-1);
//		fih.setSpeech(frame_addspeech_textfield.getText());
//		fih.setName(null);
//
//		frames.add(fih);
//
//		frameListReload(frameList);
//	}
//
//	public void frame_addspeed(JList<String> frameList) {
//		// Add a speed setting frame to the frameList (button bottom-right)
//		Frame fih = new Frame();
//
//		fih.setRightThumbFingerSpeed(Double.parseDouble(servoitemholder[0][0].spe.getText()));
//		fih.setRightIndexFingerSpeed(Double.parseDouble(servoitemholder[0][1].spe.getText()));
//		fih.setRightMajeureFingerSpeed(Double.parseDouble(servoitemholder[0][2].spe.getText()));
//		fih.setRightRingFingerSpeed(Double.parseDouble(servoitemholder[0][3].spe.getText()));
//		fih.setRightPinkyFingerSpeed(Double.parseDouble(servoitemholder[0][4].spe.getText()));
//		fih.setRightWristSpeed(Double.parseDouble(servoitemholder[0][5].spe.getText()));
//
//		fih.setRightBicepsSpeed(Double.parseDouble(servoitemholder[1][0].spe.getText()));
//		fih.setRightRotateSpeed(Double.parseDouble(servoitemholder[1][1].spe.getText()));
//		fih.setRightShoulderSpeed(Double.parseDouble(servoitemholder[1][2].spe.getText()));
//		fih.setRightOmoplateSpeed(Double.parseDouble(servoitemholder[1][3].spe.getText()));
//
//		fih.setLeftThumbFingerSpeed(Double.parseDouble(servoitemholder[2][0].spe.getText()));
//		fih.setLeftIndexFingerSpeed(Double.parseDouble(servoitemholder[2][1].spe.getText()));
//		fih.setLeftMajeureFingerSpeed(Double.parseDouble(servoitemholder[2][2].spe.getText()));
//		fih.setLeftRingFingerSpeed(Double.parseDouble(servoitemholder[2][3].spe.getText()));
//		fih.setLeftPinkyFingerSpeed(Double.parseDouble(servoitemholder[2][4].spe.getText()));
//		fih.setLeftWristSpeed(Double.parseDouble(servoitemholder[2][5].spe.getText()));
//
//		fih.setLeftBicepsSpeed(Double.parseDouble(servoitemholder[3][0].spe.getText()));
//		fih.setLeftRotateSpeed(Double.parseDouble(servoitemholder[3][1].spe.getText()));
//		fih.setLeftShoulderSpeed(Double.parseDouble(servoitemholder[3][2].spe.getText()));
//		fih.setLeftOmoplateSpeed(Double.parseDouble(servoitemholder[3][3].spe.getText()));
//
//		fih.setNeckSpeed(Double.parseDouble(servoitemholder[4][0].spe.getText()));
//		fih.setHeadRotateSpeed(Double.parseDouble(servoitemholder[4][1].spe.getText()));
//		fih.setEyeXSpeed(Double.parseDouble(servoitemholder[4][2].spe.getText()));
//		fih.setEyeYSpeed(Double.parseDouble(servoitemholder[4][3].spe.getText()));
//		fih.setJawSpeed(Double.parseDouble(servoitemholder[4][4].spe.getText()));
//
//		fih.setTopStomSpeed(Double.parseDouble(servoitemholder[5][0].spe.getText()));
//		fih.setMidStomSpeed(Double.parseDouble(servoitemholder[5][1].spe.getText()));
//		fih.setLowStomSpeed(Double.parseDouble(servoitemholder[5][2].spe.getText()));
//
//		fih.setSleep(-1);
//		fih.setSpeech(null);
//		fih.setName(null);
//
//		frames.add(fih);
//
//		frameListReload(frameList);
//	}
	
}