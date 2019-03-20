package org.myrobotlab.service;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.ServiceType;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.myrobotlab.logging.LoggingFactory;
import org.myrobotlab.service.model.Frame;
import org.myrobotlab.service.model.Frame.FrameType;
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

	public static class PythonItemHolder {
		String code;
		boolean modifyable;
		boolean function;
		boolean notfunction;
	}

	public static class ServoItemHolder {
		public JLabel fin;
		public JLabel min;
		public JLabel res;
		public JLabel max;
		public JSlider sli;
		public JLabel akt;
		public JTextField spe;
	}

	private static final long serialVersionUID = 1L;

	private final static Logger LOGGER = LoggerFactory.getLogger(InMoovGestureCreator.class);

	private static final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
			.getNumberInstance(Locale.getDefault());

	private final Gesture gesture = new Gesture();
	private final List<Frame> frames = gesture.getFrames();

	transient ServoItemHolder[][] servoitemholder;

	private final List<File> pythonFiles = new ArrayList<File>();

	transient ArrayList<PythonItemHolder> pythonitemholder;

	boolean moverealtime = false;
	InMoov i01;

	String pythonscript;

	String pythonname;

	String referencename;

	String parsirani_kod;

	String ime_funkcije = null;

	String ime_gest = null;

	private JList frameListGlobal;

	public JList getFrameListGlobal() {
		return frameListGlobal;
	}

	public void setFrameListGlobal(JList frameListGlobal) {
		this.frameListGlobal = frameListGlobal;
	}

	public static void main(String[] args) throws InterruptedException {

		LoggingFactory.init(Level.INFO);
		try {

			Runtime.start("gui", "SwingGui");
			Runtime.start("inmoovgesturecreator", "InMoovGestureCreator");

		} catch (Exception e) {
			Logging.logError(e);
		}

	}

	public InMoovGestureCreator(String n) {
		super(n);
		// intializing variables
		servoitemholder = new ServoItemHolder[6][];
		pythonitemholder = new ArrayList<PythonItemHolder>();
		decimalFormat.setGroupingUsed(false);
	}

	public void control_addgest(JList control_list, JTextField control_gestname, JTextField control_funcname) {
		// Add the current gesture to the script (button bottom-left)
		String defname = ime_funkcije = control_funcname.getText();
		String gestname = ime_gest = control_gestname.getText();

		String code = "";
		for (Frame fih : frames) {
			String code1;
			if (fih.getSleep() != -1) {
				code1 = "    sleep(" + fih.getSleep() + ")\n";
			} else if (fih.getSpeech() != null) {
				code1 = "    " + pythonname + ".mouth.speakBlocking(\"" + fih.getSpeech() + "\")\n";
			} else if (fih.getName() != null) {
				String code11 = "";
				String code12 = "";
				String code13 = "";
				String code14 = "";
				String code15 = "";
				String code16 = "";
				if (fih.getRightHandMoveSet()) {
					code11 = "    " + pythonname + ".moveHead(" + fih.getNeckMove() + "," + fih.getHeadRotateMove()
							+ "," + fih.getEyeXMove() + "," + fih.getEyeYMove() + "," + fih.getJawMove() + ")\n";
				}
				if (fih.getRightArmMoveSet()) {
					code12 = "    " + pythonname + ".moveArm(\"left\"," + fih.getLeftBicepsMove() + ","
							+ fih.getLeftRotateMove() + "," + fih.getLeftShoulderMove() + ","
							+ fih.getLeftOmoplateMove() + ")\n";
				}
				if (fih.getLeftHandMoveSet()) {
					code13 = "    " + pythonname + ".moveArm(\"right\"," + fih.getRightBicepsMove() + ","
							+ fih.getRightRotateMove() + "," + fih.getRightShoulderMove() + ","
							+ fih.getRightOmoplateMove() + ")\n";
				}
				if (fih.getLeftArmMoveSet()) {
					code14 = "    " + pythonname + ".moveHand(\"left\"," + fih.getLeftThumbFingerMove() + ","
							+ fih.getLeftIndexFingerMove() + "," + fih.getLeftMajeureFingerMove() + ","
							+ fih.getLeftRingFingerMove() + "," + fih.getLeftPinkyFingerMove() + ","
							+ fih.getLeftWristMove() + ")\n";
				}
				if (fih.getHeadMoveSet()) {
					code15 = "    " + pythonname + ".moveHand(\"right\"," + fih.getRightThumbFingerMove() + ","
							+ fih.getRightIndexFingerMove() + "," + fih.getRightMajeureFingerMove() + ","
							+ fih.getRightRingFingerMove() + "," + fih.getRightPinkyFingerMove() + ","
							+ fih.getRightWristMove() + ")\n";
				}
				if (fih.getTorsoMoveSet()) {
					code16 = "    " + pythonname + ".moveTorso(" + fih.getTopStomMove() + "," + fih.getMidStomMove()
							+ "," + fih.getLowStomMove() + ")\n";
				}
				code1 = code11 + code12 + code13 + code14 + code15 + code16;
			} else {
				String code11 = "";
				String code12 = "";
				String code13 = "";
				String code14 = "";
				String code15 = "";
				String code16 = "";
				if (fih.getRightHandMoveSet()) {
					code11 = "    " + pythonname + ".setHeadSpeed(" + fih.getNeckSpeed() + ","
							+ fih.getHeadRotateSpeed() + "," + fih.getEyeXSpeed() + "," + fih.getEyeYSpeed() + ","
							+ fih.getJawSpeed() + ")\n";
				}
				if (fih.getRightArmMoveSet()) {
					code12 = "    " + pythonname + ".setArmSpeed(\"left\"," + fih.getLeftBicepsSpeed() + ","
							+ fih.getLeftRotateSpeed() + "," + fih.getLeftShoulderSpeed() + ","
							+ fih.getLeftOmoplateSpeed() + ")\n";
				}
				if (fih.getLeftHandMoveSet()) {
					code13 = "    " + pythonname + ".setArmSpeed(\"right\"," + fih.getRightBicepsSpeed() + ","
							+ fih.getRightRotateSpeed() + "," + fih.getRightShoulderSpeed() + ","
							+ fih.getRightOmoplateSpeed() + ")\n";
				}
				if (fih.getLeftArmMoveSet()) {
					code14 = "    " + pythonname + ".setHandSpeed(\"left\"," + fih.getLeftThumbFingerSpeed() + ","
							+ fih.getLeftIndexFingerSpeed() + "," + fih.getLeftMajeureFingerSpeed() + ","
							+ fih.getLeftRingFingerSpeed() + "," + fih.getLeftPinkyFingerSpeed() + ","
							+ fih.getLeftWristSpeed() + ")\n";
				}
				if (fih.getHeadMoveSet()) {
					code15 = "    " + pythonname + ".setHandSpeed(\"right\"," + fih.getRightThumbFingerSpeed() + ","
							+ fih.getRightIndexFingerSpeed() + "," + fih.getRightMajeureFingerSpeed() + ","
							+ fih.getRightRingFingerSpeed() + "," + fih.getRightPinkyFingerSpeed() + ","
							+ fih.getRightWristSpeed() + ")\n";
				}
				if (fih.getTorsoMoveSet()) {
					code16 = "    " + pythonname + ".setTorsoSpeed(" + fih.getTopStomSpeed() + ","
							+ fih.getMidStomSpeed() + "," + fih.getLowStomSpeed() + ")\n";
				}
				code1 = code11 + code12 + code13 + code14 + code15 + code16;
			}
			code = code + code1;
		}
		String finalcode = "def " + defname + "():\n" + code;

		String insert = "ear.addCommand(\"" + gestname + "\", \"python\", \"" + defname + "\")";
		int posear = pythonscript.lastIndexOf("ear.addCommand");
		int pos = pythonscript.indexOf("\n", posear);
		pythonscript = pythonscript.substring(0, pos) + "\n" + insert
				+ pythonscript.substring(pos, pythonscript.length());

		pythonscript = pythonscript + "\n" + finalcode;

		parsescript(control_list);
	}

	public void control_connect(JButton controlConnect) {
		// Connect / Disconnect to / from the InMoov service (button top-left)
		if (controlConnect.getText().equals("Connect")) {
			if (referencename == null) {
				referencename = "i01";
			}
			i01 = (InMoov) Runtime.getService(referencename);
			i01.enable();
			if (i01 != null) {
				controlConnect.setText("Disconnect");
			} else {
				LOGGER.info("Failed to connect!"); // should be a message to the user
			}
		} else {
			i01 = null;
			controlConnect.setText("Connect");
		}
	}

	public void control_loadgest(JList control_list, JList framelist, JTextField control_gestname,
			JTextField control_funcname) {
		// Load the current gesture from the script (button bottom-left)
		int posl = control_list.getSelectedIndex();

		if (posl != -1) {
			if (pythonitemholder.get(posl).modifyable) {
				frames.clear();

				String defname = null;

				String code = pythonitemholder.get(posl).code;
				String[] codesplit = code.split("\n");
				Frame fih = null;
				boolean ismove = false;
				boolean isspeed = false;
				boolean head = false;
				boolean rhand = false;
				boolean lhand = false;
				boolean rarm = false;
				boolean larm = false;
				boolean torso = false;
				boolean keepgoing = true;
				int pos = 0;
				while (keepgoing) {
					if (fih == null) {
						fih = new Frame(Frame.FrameType.SLEEP);
					}
					String line;
					if (pos < codesplit.length) {
						line = codesplit[pos];
					} else {
						line = "pweicmfh - only one run";
						keepgoing = false;
					}
					String linewithoutspace = line.replace(" ", "");
					if (linewithoutspace.equals("")) {
						pos++;
						continue;
					}
					String line2 = line.replace(" ", "");
					if (!(ismove) && !(isspeed)) {
						if (line2.startsWith("def")) {
							String defn = line.substring(line.indexOf(" ") + 1, line.lastIndexOf("():"));
							defname = defn;
							pos++;
						} else if (line2.startsWith("sleep")) {
							String sleeptime = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
							fih.setSleep(Integer.parseInt(sleeptime));
							fih.setSpeech(null);
							fih.setName(null);
							frames.add(fih);
							fih = null;
							pos++;
						} else if (line2.startsWith(pythonname)) {
							if (line2.startsWith(pythonname + ".mouth.speak")) {
								fih.setSleep(-1);
								fih.setSpeech(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
								fih.setName(null);
								frames.add(fih);
								fih = null;
								pos++;
							} else if (line2.startsWith(pythonname + ".move")) {
								ismove = true;
								String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
								String[] goodsplit = good.split(",");
								if (line2.startsWith(pythonname + ".moveHead")) {
									fih.setNeckMove(Integer.parseInt(goodsplit[0]));
									fih.setHeadRotateMove(Integer.parseInt(goodsplit[1]));
									if (goodsplit.length > 2) {
										fih.setEyeXMove(Integer.parseInt(goodsplit[2]));
										fih.setEyeYMove(Integer.parseInt(goodsplit[3]));
										fih.setJawMove(Integer.parseInt(goodsplit[4]));
									} else {
										fih.setEyeXMove(90);
										fih.setEyeYMove(90);
										fih.setJawMove(90);
									}
									head = true;
									pos++;
								} else if (line2.startsWith(pythonname + ".moveHand")) {
									String gs = goodsplit[0];
									String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
									if (side.equals("right")) {
										fih.setRightThumbFingerMove(Integer.parseInt(goodsplit[1]));
										fih.setRightIndexFingerMove(Integer.parseInt(goodsplit[2]));
										fih.setRightMajeureFingerMove(Integer.parseInt(goodsplit[3]));
										fih.setRightRingFingerMove(Integer.parseInt(goodsplit[4]));
										fih.setRightPinkyFingerMove(Integer.parseInt(goodsplit[5]));
										if (goodsplit.length > 6) {
											fih.setRightWristMove(Integer.parseInt(goodsplit[6]));
										} else {
											fih.setRightWristMove(90);
										}
										rhand = true;
										pos++;
									} else if (side.equals("left")) {
										fih.setLeftThumbFingerMove(Integer.parseInt(goodsplit[1]));
										fih.setLeftIndexFingerMove(Integer.parseInt(goodsplit[2]));
										fih.setLeftMajeureFingerMove(Integer.parseInt(goodsplit[3]));
										fih.setLeftRingFingerMove(Integer.parseInt(goodsplit[4]));
										fih.setLeftPinkyFingerMove(Integer.parseInt(goodsplit[5]));
										if (goodsplit.length > 6) {
											fih.setLeftWristMove(Integer.parseInt(goodsplit[6]));
										} else {
											fih.setLeftWristMove(90);
										}
										lhand = true;
										pos++;
									}
								} else if (line2.startsWith(pythonname + ".moveArm")) {
									String gs = goodsplit[0];
									String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
									if (side.equals("right")) {
										fih.setRightBicepsMove(Integer.parseInt(goodsplit[1]));
										fih.setRightRotateMove(Integer.parseInt(goodsplit[2]));
										fih.setRightShoulderMove(Integer.parseInt(goodsplit[3]));
										fih.setRightOmoplateMove(Integer.parseInt(goodsplit[4]));
										rarm = true;
										pos++;
									} else if (side.equals("left")) {
										fih.setLeftBicepsMove(Integer.parseInt(goodsplit[1]));
										fih.setLeftRotateMove(Integer.parseInt(goodsplit[2]));
										fih.setLeftShoulderMove(Integer.parseInt(goodsplit[3]));
										fih.setLeftOmoplateMove(Integer.parseInt(goodsplit[4]));
										larm = true;
										pos++;
									}
								} else if (line2.startsWith(pythonname + ".moveTorso")) {
									fih.setTopStomMove(Integer.parseInt(goodsplit[0]));
									fih.setMidStomMove(Integer.parseInt(goodsplit[1]));
									fih.setLowStomMove(Integer.parseInt(goodsplit[2]));
									torso = true;
									pos++;
								}
							} else if (line2.startsWith(pythonname + ".set")) {
								isspeed = true;
								String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
								String[] goodsplit = good.split(",");
								if (line2.startsWith(pythonname + ".setHeadSpeed")) {
									fih.setNeckSpeed(Double.parseDouble(goodsplit[0]));
									fih.setHeadRotateSpeed(Double.parseDouble(goodsplit[1]));
									if (goodsplit.length > 2) {
										fih.setEyeXSpeed(Double.parseDouble(goodsplit[2]));
										fih.setEyeYSpeed(Double.parseDouble(goodsplit[3]));
										fih.setJawSpeed(Double.parseDouble(goodsplit[4]));
									} else {
										fih.setEyeXSpeed(1.0d);
										fih.setEyeYSpeed(1.0d);
										fih.setJawSpeed(1.0d);
									}
									head = true;
									pos++;
								} else if (line2.startsWith(pythonname + ".setHandSpeed")) {
									String gs = goodsplit[0];
									String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
									if (side.equals("right")) {
										fih.setRightThumbFingerSpeed(Double.parseDouble(goodsplit[1]));
										fih.setRightIndexFingerSpeed(Double.parseDouble(goodsplit[2]));
										fih.setRightMajeureFingerSpeed(Double.parseDouble(goodsplit[3]));
										fih.setRightRingFingerSpeed(Double.parseDouble(goodsplit[4]));
										fih.setRightPinkyFingerSpeed(Double.parseDouble(goodsplit[5]));
										if (goodsplit.length > 6) {
											fih.setRightWristSpeed(Double.parseDouble(goodsplit[6]));
										} else {
											fih.setRightWristSpeed(1.0d);
										}
										rhand = true;
										pos++;
									} else if (side.equals("left")) {
										fih.setLeftThumbFingerSpeed(Double.parseDouble(goodsplit[1]));
										fih.setLeftIndexFingerSpeed(Double.parseDouble(goodsplit[2]));
										fih.setLeftMajeureFingerSpeed(Double.parseDouble(goodsplit[3]));
										fih.setLeftRingFingerSpeed(Double.parseDouble(goodsplit[4]));
										fih.setLeftPinkyFingerSpeed(Double.parseDouble(goodsplit[5]));
										if (goodsplit.length > 6) {
											fih.setLeftWristSpeed(Double.parseDouble(goodsplit[6]));
										} else {
											fih.setLeftWristSpeed(1.0d);
										}
										lhand = true;
										pos++;
									}
								} else if (line2.startsWith(pythonname + ".setArmSpeed")) {
									String gs = goodsplit[0];
									String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
									if (side.equals("right")) {
										fih.setRightBicepsSpeed(Double.parseDouble(goodsplit[1]));
										fih.setRightRotateSpeed(Double.parseDouble(goodsplit[2]));
										fih.setRightShoulderSpeed(Double.parseDouble(goodsplit[3]));
										fih.setRightOmoplateSpeed(Double.parseDouble(goodsplit[4]));
										rarm = true;
										pos++;
									} else if (side.equals("left")) {
										fih.setLeftBicepsSpeed(Double.parseDouble(goodsplit[1]));
										fih.setLeftRotateSpeed(Double.parseDouble(goodsplit[2]));
										fih.setLeftShoulderSpeed(Double.parseDouble(goodsplit[3]));
										fih.setLeftOmoplateSpeed(Double.parseDouble(goodsplit[4]));
										larm = true;
										pos++;
									}
								} else if (line2.startsWith(pythonname + ".setTorsoSpeed")) {
									fih.setTopStomSpeed(Double.parseDouble(goodsplit[0]));
									fih.setMidStomSpeed(Double.parseDouble(goodsplit[1]));
									fih.setLowStomSpeed(Double.parseDouble(goodsplit[2]));
									torso = true;
									pos++;
								}
							}
						}
					} else if (ismove && !(isspeed)) {
						if (line2.startsWith(pythonname + ".move")) {
							String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
							String[] goodsplit = good.split(",");
							if (line2.startsWith(pythonname + ".moveHead")) {
								fih.setNeckMove(Integer.parseInt(goodsplit[0]));
								fih.setHeadRotateMove(Integer.parseInt(goodsplit[1]));
								if (goodsplit.length > 2) {
									fih.setEyeXMove(Integer.parseInt(goodsplit[2]));
									fih.setEyeYMove(Integer.parseInt(goodsplit[3]));
									fih.setJawMove(Integer.parseInt(goodsplit[4]));
								} else {
									fih.setEyeXMove(90);
									fih.setEyeYMove(90);
									fih.setJawMove(90);
								}
								head = true;
								pos++;
							} else if (line2.startsWith(pythonname + ".moveHand")) {
								String gs = goodsplit[0];
								String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
								if (side.equals("right")) {
									fih.setRightThumbFingerMove(Integer.parseInt(goodsplit[1]));
									fih.setRightIndexFingerMove(Integer.parseInt(goodsplit[2]));
									fih.setRightMajeureFingerMove(Integer.parseInt(goodsplit[3]));
									fih.setRightRingFingerMove(Integer.parseInt(goodsplit[4]));
									fih.setRightPinkyFingerMove(Integer.parseInt(goodsplit[5]));
									if (goodsplit.length > 6) {
										fih.setRightWristMove(Integer.parseInt(goodsplit[6]));
									} else {
										fih.setRightWristMove(90);
									}
									rhand = true;
									pos++;
								} else if (side.equals("left")) {
									fih.setLeftThumbFingerMove(Integer.parseInt(goodsplit[1]));
									fih.setLeftIndexFingerMove(Integer.parseInt(goodsplit[2]));
									fih.setLeftMajeureFingerMove(Integer.parseInt(goodsplit[3]));
									fih.setLeftRingFingerMove(Integer.parseInt(goodsplit[4]));
									fih.setLeftPinkyFingerMove(Integer.parseInt(goodsplit[5]));
									if (goodsplit.length > 6) {
										fih.setLeftWristMove(Integer.parseInt(goodsplit[6]));
									} else {
										fih.setLeftWristMove(90);
									}
									lhand = true;
									pos++;
								}
							} else if (line2.startsWith(pythonname + ".moveArm")) {
								String gs = goodsplit[0];
								String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
								if (side.equals("right")) {
									fih.setRightBicepsMove(Integer.parseInt(goodsplit[1]));
									fih.setRightRotateMove(Integer.parseInt(goodsplit[2]));
									fih.setRightShoulderMove(Integer.parseInt(goodsplit[3]));
									fih.setRightOmoplateMove(Integer.parseInt(goodsplit[4]));
									rarm = true;
									pos++;
								} else if (side.equals("left")) {
									fih.setLeftBicepsMove(Integer.parseInt(goodsplit[1]));
									fih.setLeftRotateMove(Integer.parseInt(goodsplit[2]));
									fih.setLeftShoulderMove(Integer.parseInt(goodsplit[3]));
									fih.setLeftOmoplateMove(Integer.parseInt(goodsplit[4]));
									larm = true;
									pos++;
								}
							} else if (line2.startsWith(pythonname + ".moveTorso")) {
								fih.setTopStomMove(Integer.parseInt(goodsplit[0]));
								fih.setMidStomMove(Integer.parseInt(goodsplit[1]));
								fih.setLowStomMove(Integer.parseInt(goodsplit[2]));
								torso = true;
								pos++;
							}
						} else {
							if (!head) {
								fih.setNeckMove(90);
								fih.setHeadRotateMove(90);
								fih.setEyeXMove(90);
								fih.setEyeYMove(90);
								fih.setJawMove(90);
							}
							if (!rhand) {
								fih.setRightThumbFingerMove(90);
								fih.setRightIndexFingerMove(90);
								fih.setRightMajeureFingerMove(90);
								fih.setRightRingFingerMove(90);
								fih.setRightPinkyFingerMove(90);
								fih.setRightWristMove(90);
							}
							if (!lhand) {
								fih.setLeftThumbFingerMove(90);
								fih.setLeftIndexFingerMove(90);
								fih.setLeftMajeureFingerMove(90);
								fih.setLeftRingFingerMove(90);
								fih.setLeftPinkyFingerMove(90);
								fih.setLeftWristMove(90);
							}
							if (!rarm) {
								fih.setRightBicepsMove(90);
								fih.setRightRotateMove(90);
								fih.setRightShoulderMove(90);
								fih.setRightOmoplateMove(90);
							}
							if (!larm) {
								fih.setLeftBicepsMove(90);
								fih.setLeftRotateMove(90);
								fih.setLeftShoulderMove(90);
								fih.setLeftOmoplateMove(90);
							}
							if (!torso) {
								fih.setTopStomMove(90);
								fih.setMidStomMove(90);
								fih.setLowStomMove(90);
							}
							fih.setSleep(-1);
							fih.setSpeech(null);
							fih.setName("SEQ");
							frames.add(fih);
							fih = null;
							ismove = false;
							head = false;
							rhand = false;
							lhand = false;
							rarm = false;
							larm = false;
							torso = false;
						}
					} else if (!(ismove) && isspeed) {
						if (line2.startsWith(pythonname + ".set")) {
							String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
							String[] goodsplit = good.split(",");
							if (line2.startsWith(pythonname + ".setHeadSpeed")) {
								fih.setNeckSpeed(Double.parseDouble(goodsplit[0]));
								fih.setHeadRotateSpeed(Double.parseDouble(goodsplit[1]));
								if (goodsplit.length > 2) {
									fih.setEyeXSpeed(Double.parseDouble(goodsplit[2]));
									fih.setEyeYSpeed(Double.parseDouble(goodsplit[3]));
									fih.setJawSpeed(Double.parseDouble(goodsplit[4]));
								} else {
									fih.setEyeXSpeed(1.0d);
									fih.setEyeYSpeed(1.0d);
									fih.setJawSpeed(1.0d);
								}
								head = true;
								pos++;
							} else if (line2.startsWith(pythonname + ".setHandSpeed")) {
								String gs = goodsplit[0];
								String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
								if (side.equals("right")) {
									fih.setRightThumbFingerSpeed(Double.parseDouble(goodsplit[1]));
									fih.setRightIndexFingerSpeed(Double.parseDouble(goodsplit[2]));
									fih.setRightMajeureFingerSpeed(Double.parseDouble(goodsplit[3]));
									fih.setRightRingFingerSpeed(Double.parseDouble(goodsplit[4]));
									fih.setRightPinkyFingerSpeed(Double.parseDouble(goodsplit[5]));
									if (goodsplit.length > 6) {
										fih.setRightWristSpeed(Double.parseDouble(goodsplit[6]));
									} else {
										fih.setRightWristSpeed(1.0d);
									}
									rhand = true;
									pos++;
								} else if (side.equals("left")) {
									fih.setLeftThumbFingerSpeed(Double.parseDouble(goodsplit[1]));
									fih.setLeftIndexFingerSpeed(Double.parseDouble(goodsplit[2]));
									fih.setLeftMajeureFingerSpeed(Double.parseDouble(goodsplit[3]));
									fih.setLeftRingFingerSpeed(Double.parseDouble(goodsplit[4]));
									fih.setLeftPinkyFingerSpeed(Double.parseDouble(goodsplit[5]));
									if (goodsplit.length > 6) {
										fih.setLeftWristSpeed(Double.parseDouble(goodsplit[6]));
									} else {
										fih.setLeftWristSpeed(1.0d);
									}
									lhand = true;
									pos++;
								}
							} else if (line2.startsWith(pythonname + ".setArmSpeed")) {
								String gs = goodsplit[0];
								String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
								if (side.equals("right")) {
									fih.setRightBicepsSpeed(Double.parseDouble(goodsplit[1]));
									fih.setRightRotateSpeed(Double.parseDouble(goodsplit[2]));
									fih.setRightShoulderSpeed(Double.parseDouble(goodsplit[3]));
									fih.setRightOmoplateSpeed(Double.parseDouble(goodsplit[4]));
									rarm = true;
									pos++;
								} else if (side.equals("left")) {
									fih.setLeftBicepsSpeed(Double.parseDouble(goodsplit[1]));
									fih.setLeftRotateSpeed(Double.parseDouble(goodsplit[2]));
									fih.setLeftShoulderSpeed(Double.parseDouble(goodsplit[3]));
									fih.setLeftOmoplateSpeed(Double.parseDouble(goodsplit[4]));
									larm = true;
									pos++;
								}
							} else if (line2.startsWith(pythonname + ".setTorsoSpeed")) {
								fih.setTopStomSpeed(Double.parseDouble(goodsplit[0]));
								fih.setMidStomSpeed(Double.parseDouble(goodsplit[1]));
								fih.setLowStomSpeed(Double.parseDouble(goodsplit[2]));
								torso = true;
								pos++;
							}
						} else {
							if (!head) {
								fih.setNeckSpeed(1.0d);
								fih.setHeadRotateSpeed(1.0d);
								fih.setEyeXSpeed(1.0d);
								fih.setEyeYSpeed(1.0d);
								fih.setJawSpeed(1.0d);
							}
							if (!rhand) {
								fih.setRightThumbFingerSpeed(1.0d);
								fih.setRightIndexFingerSpeed(1.0d);
								fih.setRightMajeureFingerSpeed(1.0d);
								fih.setRightRingFingerSpeed(1.0d);
								fih.setRightPinkyFingerSpeed(1.0d);
								fih.setRightWristSpeed(1.0d);
							}
							if (!lhand) {
								fih.setLeftThumbFingerSpeed(1.0d);
								fih.setLeftIndexFingerSpeed(1.0d);
								fih.setLeftMajeureFingerSpeed(1.0d);
								fih.setLeftRingFingerSpeed(1.0d);
								fih.setLeftPinkyFingerSpeed(1.0d);
								fih.setLeftWristSpeed(1.0d);
							}
							if (!rarm) {
								fih.setRightBicepsSpeed(1.0d);
								fih.setRightRotateSpeed(1.0d);
								fih.setRightShoulderSpeed(1.0d);
								fih.setRightOmoplateSpeed(1.0d);
							}
							if (!larm) {
								fih.setLeftBicepsSpeed(1.0d);
								fih.setLeftRotateSpeed(1.0d);
								fih.setLeftShoulderSpeed(1.0d);
								fih.setLeftOmoplateSpeed(1.0d);
							}
							if (!torso) {
								fih.setTopStomSpeed(1.0d);
								fih.setMidStomSpeed(1.0d);
								fih.setLowStomSpeed(1.0d);
							}
							fih.setSleep(-1);
							fih.setSpeech(null);
							fih.setName(null);
							frames.add(fih);
							fih = null;
							isspeed = false;
							head = false;
							rhand = false;
							lhand = false;
							rarm = false;
							larm = false;
							torso = false;
						}
					} else {
						// this shouldn't be reached
						// ismove & isspeed true
						// wrong
					}
				}

				framelistact(framelist);

				int defnamepos = pythonscript.indexOf(defname);
				int earpos1 = pythonscript.lastIndexOf("\n", defnamepos);
				int earpos2 = pythonscript.indexOf("\n", defnamepos);
				String earline = pythonscript.substring(earpos1 + 1, earpos2);
				if (earline.startsWith("ear.addCommand")) {
					String good = earline.substring(earline.indexOf("("), earline.lastIndexOf(")"));
					String[] goodsplit = good.split(",");

					String funcnamedirty = goodsplit[0];
					String funcname = funcnamedirty.substring(funcnamedirty.indexOf("\"") + 1,
							funcnamedirty.lastIndexOf("\""));

					control_gestname.setText(funcname);
					control_funcname.setText(defname);
				}
			}
		}
	}

	public void control_removegest(JList control_list) {
		// Remove the selected gesture from the script (button bottom-left)
		int posl = control_list.getSelectedIndex();

		if (posl != -1) {

			if (pythonitemholder.get(posl).function && !pythonitemholder.get(posl).notfunction) {

				String codeold = pythonitemholder.get(posl).code;
				String defnameold = codeold.substring(codeold.indexOf("def ") + 4, codeold.indexOf("():"));

				int olddefpos = pythonscript.indexOf(defnameold);
				int pos1 = pythonscript.lastIndexOf("\n", olddefpos);
				int pos2 = pythonscript.indexOf("\n", olddefpos);
				pythonscript = pythonscript.substring(0, pos1) + pythonscript.substring(pos2, pythonscript.length());

				int posscript = pythonscript.lastIndexOf(defnameold);
				int posscriptnextdef = pythonscript.indexOf("def", posscript);
				if (posscriptnextdef == -1) {
					posscriptnextdef = pythonscript.length();
				}

				pythonscript = pythonscript.substring(0, posscript - 4)
						+ pythonscript.substring(posscriptnextdef - 1, pythonscript.length());

				parsescript(control_list);
			}
		}
	}

	public void control_savescri() {
		// Save the Python-Script (in Python-Service) (button bottom-left)
		JFrame frame = new JFrame();
		JTextArea textarea = new JTextArea();
		parse_frame_to_script();
		textarea.setText(parsirani_kod);
		textarea.setEditable(false);
		textarea.setLineWrap(true);
		JScrollPane scrollpane = new JScrollPane(textarea);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		frame.add(scrollpane);
		frame.pack();
		frame.setVisible(true);

		FileWriter fileWriter;
		try {
			/* "/home/abe/ws-fx/inmoov/InMoov/gestures/abetova.py" */
			fileWriter = new FileWriter("/home/abe/ws-fx/inmoov/InMoov/gestures/"
					+ parsirani_kod.substring(4, parsirani_kod.indexOf('(')) + "_abetovo" + ".py");
			fileWriter.write(parsirani_kod);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void control_testgest() {
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
		} else {
			LOGGER.info("No frames to execute.");
		}
	}

	public void control_updategest(JList control_list, JTextField control_gestname, JTextField control_funcname) {
		// Update the current gesture in the script (button bottom-left)
		int posl = control_list.getSelectedIndex();

		if (posl != -1) {

			if (pythonitemholder.get(posl).function && !pythonitemholder.get(posl).notfunction) {

				String codeold = pythonitemholder.get(posl).code;
				String defnameold = codeold.substring(codeold.indexOf("def ") + 4, codeold.indexOf("():"));

				String defname = control_funcname.getText();
				String gestname = control_gestname.getText();

				String code = "";
				for (Frame fih : frames) {
					String code1;
					if (fih.getSleep() != -1) {
						code1 = "    sleep(" + fih.getSleep() + ")\n";
					} else if (fih.getSpeech() != null) {
						code1 = "    " + pythonname + ".mouth.speakBlocking(\"" + fih.getSpeech() + "\")\n";
					} else if (fih.getName() != null) {
						String code11 = "";
						String code12 = "";
						String code13 = "";
						String code14 = "";
						String code15 = "";
						String code16 = "";
						if (fih.getRightHandMoveSet()) {
							code11 = "    " + pythonname + ".moveHead(" + fih.getNeckMove() + ","
									+ fih.getHeadRotateMove() + "," + fih.getEyeXMove() + "," + fih.getEyeYMove() + ","
									+ fih.getJawMove() + ")\n";
						}
						if (fih.getRightArmMoveSet()) {
							code12 = "    " + pythonname + ".moveArm(\"left\"," + fih.getLeftBicepsMove() + ","
									+ fih.getLeftRotateMove() + "," + fih.getLeftShoulderMove() + ","
									+ fih.getLeftOmoplateMove() + ")\n";
						}
						if (fih.getLeftHandMoveSet()) {
							code13 = "    " + pythonname + ".moveArm(\"right\"," + fih.getRightBicepsMove() + ","
									+ fih.getRightRotateMove() + "," + fih.getRightShoulderMove() + ","
									+ fih.getRightOmoplateMove() + ")\n";
						}
						if (fih.getLeftArmMoveSet()) {
							code14 = "    " + pythonname + ".moveHand(\"left\"," + fih.getLeftThumbFingerMove() + ","
									+ fih.getLeftIndexFingerMove() + "," + fih.getLeftMajeureFingerMove() + ","
									+ fih.getLeftRingFingerMove() + "," + fih.getLeftPinkyFingerMove() + ","
									+ fih.getLeftWristMove() + ")\n";
						}
						if (fih.getHeadMoveSet()) {
							code15 = "    " + pythonname + ".moveHand(\"right\"," + fih.getRightThumbFingerMove() + ","
									+ fih.getRightIndexFingerMove() + "," + fih.getRightMajeureFingerMove() + ","
									+ fih.getRightRingFingerMove() + "," + fih.getRightPinkyFingerMove() + ","
									+ fih.getRightWristMove() + ")\n";
						}
						if (fih.getTorsoMoveSet()) {
							code16 = "    " + pythonname + ".moveTorso(" + fih.getTopStomMove() + ","
									+ fih.getMidStomMove() + "," + fih.getLowStomMove() + ")\n";
						}
						code1 = code11 + code12 + code13 + code14 + code15 + code16;
					} else {
						String code11 = "";
						String code12 = "";
						String code13 = "";
						String code14 = "";
						String code15 = "";
						String code16 = "";
						if (fih.getRightHandMoveSet()) {
							code11 = "    " + pythonname + ".setHeadSpeed(" + fih.getNeckSpeed() + ","
									+ fih.getHeadRotateSpeed() + "," + fih.getEyeXSpeed() + "," + fih.getEyeYSpeed()
									+ "," + fih.getJawSpeed() + ")\n";
						}
						if (fih.getRightArmMoveSet()) {
							code12 = "    " + pythonname + ".setArmSpeed(\"left\"," + fih.getLeftBicepsSpeed() + ","
									+ fih.getLeftRotateSpeed() + "," + fih.getLeftShoulderSpeed() + ","
									+ fih.getLeftOmoplateSpeed() + ")\n";
						}

						if (fih.getLeftHandMoveSet()) {
							code13 = "    " + pythonname + ".setArmSpeed(\"right\"," + fih.getRightBicepsSpeed() + ","
									+ fih.getRightRotateSpeed() + "," + fih.getRightShoulderSpeed() + ","
									+ fih.getRightOmoplateSpeed() + ")\n";
						}
						if (fih.getLeftArmMoveSet()) {
							code14 = "    " + pythonname + ".setHandSpeed(\"left\"," + fih.getLeftThumbFingerSpeed()
									+ "," + fih.getLeftIndexFingerSpeed() + "," + fih.getLeftMajeureFingerSpeed() + ","
									+ fih.getLeftRingFingerSpeed() + "," + fih.getLeftPinkyFingerSpeed() + ","
									+ fih.getLeftWristSpeed() + ")\n";
						}
						if (fih.getHeadMoveSet()) {
							code15 = "    " + pythonname + ".setHandSpeed(\"right\"," + fih.getRightThumbFingerSpeed()
									+ "," + fih.getRightIndexFingerSpeed() + "," + fih.getRightMajeureFingerSpeed()
									+ "," + fih.getRightRingFingerSpeed() + "," + fih.getRightPinkyFingerSpeed() + ","
									+ fih.getRightWristSpeed() + ")\n";
						}
						if (fih.getTorsoMoveSet()) {
							code16 = "    " + pythonname + ".setTorsoSpeed(" + fih.getTopStomSpeed() + ","
									+ fih.getMidStomSpeed() + "," + fih.getLowStomSpeed() + ")\n";
						}
						code1 = code11 + code12 + code13 + code14 + code15 + code16;
					}
					code = code + code1;
				}
				String finalcode = "def " + defname + "():\n" + code;

				String insert = "ear.addCommand(\"" + gestname + "\", \"python\", \"" + defname + "\")";
				int olddefpos = pythonscript.indexOf(defnameold);
				int pos1 = pythonscript.lastIndexOf("\n", olddefpos);
				int pos2 = pythonscript.indexOf("\n", olddefpos);
				pythonscript = pythonscript.substring(0, pos1) + "\n" + insert
						+ pythonscript.substring(pos2, pythonscript.length());

				int posscript = pythonscript.lastIndexOf(defnameold);
				int posscriptnextdef = pythonscript.indexOf("def", posscript);
				if (posscriptnextdef == -1) {
					posscriptnextdef = pythonscript.length();
				}

				pythonscript = pythonscript.substring(0, posscript - 4) + "\n" + finalcode
						+ pythonscript.substring(posscriptnextdef - 1, pythonscript.length());

				parsescript(control_list);
			}
		}
	}

	public void frame_addsleep(JList framelist, JTextField frame_addsleep_textfield) {
		// Add a sleep frame to the framelist (button bottom-right)
		Frame fih = new Frame(Frame.FrameType.SLEEP);

		fih.setSleep(Integer.parseInt(frame_addsleep_textfield.getText()));
		fih.setSpeech(null);
		fih.setName(null);

		frames.add(fih);

		framelistact(framelist);
	}

	public void frame_addspeech(JList framelist, JTextField frame_addspeech_textfield) {
		// Add a speech frame to the framelist (button bottom-right)
		Frame fih = new Frame(Frame.FrameType.SPEECH);

		fih.setSleep(-1);
		fih.setSpeech(frame_addspeech_textfield.getText());
		fih.setName(null);

		frames.add(fih);

		framelistact(framelist);
	}

	public void frame_addspeed(JList framelist) {
		// Add a speed setting frame to the framelist (button bottom-right)
		Frame fih = new Frame(Frame.FrameType.SPEED);

		fih.setRightThumbFingerSpeed(Double.parseDouble(servoitemholder[0][0].spe.getText()));
		fih.setRightIndexFingerSpeed(Double.parseDouble(servoitemholder[0][1].spe.getText()));
		fih.setRightMajeureFingerSpeed(Double.parseDouble(servoitemholder[0][2].spe.getText()));
		fih.setRightRingFingerSpeed(Double.parseDouble(servoitemholder[0][3].spe.getText()));
		fih.setRightPinkyFingerSpeed(Double.parseDouble(servoitemholder[0][4].spe.getText()));
		fih.setRightWristSpeed(Double.parseDouble(servoitemholder[0][5].spe.getText()));

		fih.setRightBicepsSpeed(Double.parseDouble(servoitemholder[1][0].spe.getText()));
		fih.setRightRotateSpeed(Double.parseDouble(servoitemholder[1][1].spe.getText()));
		fih.setRightShoulderSpeed(Double.parseDouble(servoitemholder[1][2].spe.getText()));
		fih.setRightOmoplateSpeed(Double.parseDouble(servoitemholder[1][3].spe.getText()));

		fih.setLeftThumbFingerSpeed(Double.parseDouble(servoitemholder[2][0].spe.getText()));
		fih.setLeftIndexFingerSpeed(Double.parseDouble(servoitemholder[2][1].spe.getText()));
		fih.setLeftMajeureFingerSpeed(Double.parseDouble(servoitemholder[2][2].spe.getText()));
		fih.setLeftRingFingerSpeed(Double.parseDouble(servoitemholder[2][3].spe.getText()));
		fih.setLeftPinkyFingerSpeed(Double.parseDouble(servoitemholder[2][4].spe.getText()));
		fih.setLeftWristSpeed(Double.parseDouble(servoitemholder[2][5].spe.getText()));

		fih.setLeftBicepsSpeed(Double.parseDouble(servoitemholder[3][0].spe.getText()));
		fih.setLeftRotateSpeed(Double.parseDouble(servoitemholder[3][1].spe.getText()));
		fih.setLeftShoulderSpeed(Double.parseDouble(servoitemholder[3][2].spe.getText()));
		fih.setLeftOmoplateSpeed(Double.parseDouble(servoitemholder[3][3].spe.getText()));

		fih.setNeckSpeed(Double.parseDouble(servoitemholder[4][0].spe.getText()));
		fih.setHeadRotateSpeed(Double.parseDouble(servoitemholder[4][1].spe.getText()));
		fih.setEyeXSpeed(Double.parseDouble(servoitemholder[4][2].spe.getText()));
		fih.setEyeYSpeed(Double.parseDouble(servoitemholder[4][3].spe.getText()));
		fih.setJawSpeed(Double.parseDouble(servoitemholder[4][4].spe.getText()));

		fih.setTopStomSpeed(Double.parseDouble(servoitemholder[5][0].spe.getText()));
		fih.setMidStomSpeed(Double.parseDouble(servoitemholder[5][1].spe.getText()));
		fih.setLowStomSpeed(Double.parseDouble(servoitemholder[5][2].spe.getText()));

		fih.setSleep(-1);
		fih.setSpeech(null);
		fih.setName(null);

		frames.add(fih);

		framelistact(framelist);
	}

	public void frame_copy(JList framelist) {
		// Copy this frame on the framelist (button bottom-right)
		int pos = framelist.getSelectedIndex();

		if (pos != -1) {
			Frame fih = frames.get(pos);
			frames.add(fih);

			framelistact(framelist);
		}
	}

	public void frame_down(JList framelist) {
		// Move this frame one down on the framelist (button bottom-right)
		int pos = framelist.getSelectedIndex();

		if (pos != -1) {
			Frame fih = frames.remove(pos);
			frames.add(pos + 1, fih);

			framelistact(framelist);
		}
	}

	public void frame_importminresmax() {
		// Import the Min- / Res- / Max- settings of your InMoov
		if (i01 != null) {
			for (int i1 = 0; i1 < servoitemholder.length; i1++) {
				for (int i2 = 0; i2 < servoitemholder[i1].length; i2++) {
					InMoovHand inmhand = null;
					InMoovArm inmarm = null;
					InMoovHead inmhead = null;
					InMoovTorso inmtorso = null;

					if (i1 == 0) {
						inmhand = i01.rightHand;
					} else if (i1 == 1) {
						inmarm = i01.rightArm;
					} else if (i1 == 2) {
						inmhand = i01.leftHand;
					} else if (i1 == 3) {
						inmarm = i01.rightArm;
					} else if (i1 == 4) {
						inmhead = i01.head;
					} else if (i1 == 5) {
						inmtorso = i01.torso;
					}

					Servo servo = null;

					if (i1 == 0 || i1 == 2) {
						if (i2 == 0) {
							servo = inmhand.thumb;
						} else if (i2 == 1) {
							servo = inmhand.index;
						} else if (i2 == 2) {
							servo = inmhand.majeure;
						} else if (i2 == 3) {
							servo = inmhand.ringFinger;
						} else if (i2 == 4) {
							servo = inmhand.pinky;
						} else if (i2 == 5) {
							servo = inmhand.wrist;
						}
					} else if (i1 == 1 || i1 == 3) {
						if (i2 == 0) {
							servo = inmarm.bicep;
						} else if (i2 == 1) {
							servo = inmarm.rotate;
						} else if (i2 == 2) {
							servo = inmarm.shoulder;
						} else if (i2 == 3) {
							servo = inmarm.omoplate;
						}
					} else if (i1 == 4) {
						if (i2 == 0) {
							servo = inmhead.neck;
						} else if (i2 == 1) {
							servo = inmhead.rothead;
						} else if (i2 == 2) {
							servo = inmhead.eyeX;
						} else if (i2 == 3) {
							servo = inmhead.eyeY;
						} else if (i2 == 4) {
							servo = inmhead.jaw;
						}
					} else if (i1 == 5) {
						if (i2 == 0) {
							servo = inmtorso.topStom;
						} else if (i2 == 1) {
							servo = inmtorso.midStom;
						} else if (i2 == 2) {
							servo = inmtorso.lowStom;
						}
					}

					Double min = servo.getMin();
					double res = servo.getRest();
					Double max = servo.getMax();

					servoitemholder[i1][i2].min.setText(min + "");
					servoitemholder[i1][i2].res.setText(res + "");
					servoitemholder[i1][i2].max.setText(max + "");
					// servoitemholder[i1][i2].sli.setMinimum(min);
					// servoitemholder[i1][i2].sli.setMaximum(max);
					// servoitemholder[i1][i2].sli.setValue(res);
				}
			}
		}
	}

	public void frame_load(JList framelist, JTextField frame_add_textfield, JTextField frame_addsleep_textfield,
			JTextField frame_addspeech_textfield) {
		// Load this frame from the framelist (button bottom-right)
		int pos = framelist.getSelectedIndex();

		if (pos != -1) {

			// sleep || speech || servo movement || speed setting
			if (frames.get(pos).getSleep() != -1) {
				frame_addsleep_textfield.setText(frames.get(pos).getSleep() + "");
			} else if (frames.get(pos).getSpeech() != null) {
				frame_addspeech_textfield.setText(frames.get(pos).getSpeech());
			} else if (frames.get(pos).getName() != null) {
				servoitemholder[0][0].sli.setValue(frames.get(pos).getRightThumbFingerMove());
				servoitemholder[0][1].sli.setValue(frames.get(pos).getRightIndexFingerMove());
				servoitemholder[0][2].sli.setValue(frames.get(pos).getRightMajeureFingerMove());
				servoitemholder[0][3].sli.setValue(frames.get(pos).getRightRingFingerMove());
				servoitemholder[0][4].sli.setValue(frames.get(pos).getRightPinkyFingerMove());
				servoitemholder[0][5].sli.setValue(frames.get(pos).getRightWristMove());

				servoitemholder[1][0].sli.setValue(frames.get(pos).getRightBicepsMove());
				servoitemholder[1][1].sli.setValue(frames.get(pos).getRightRotateMove());
				servoitemholder[1][2].sli.setValue(frames.get(pos).getRightShoulderMove());
				servoitemholder[1][3].sli.setValue(frames.get(pos).getRightOmoplateMove());

				servoitemholder[2][0].sli.setValue(frames.get(pos).getLeftThumbFingerMove());
				servoitemholder[2][1].sli.setValue(frames.get(pos).getLeftIndexFingerMove());
				servoitemholder[2][2].sli.setValue(frames.get(pos).getLeftMajeureFingerMove());
				servoitemholder[2][3].sli.setValue(frames.get(pos).getLeftRingFingerMove());
				servoitemholder[2][4].sli.setValue(frames.get(pos).getLeftPinkyFingerMove());
				servoitemholder[2][5].sli.setValue(frames.get(pos).getLeftWristMove());

				servoitemholder[3][0].sli.setValue(frames.get(pos).getLeftBicepsMove());
				servoitemholder[3][1].sli.setValue(frames.get(pos).getLeftRotateMove());
				servoitemholder[3][2].sli.setValue(frames.get(pos).getLeftShoulderMove());
				servoitemholder[3][3].sli.setValue(frames.get(pos).getLeftOmoplateMove());

				servoitemholder[4][0].sli.setValue(frames.get(pos).getNeckMove());
				servoitemholder[4][1].sli.setValue(frames.get(pos).getHeadRotateMove());
				servoitemholder[4][2].sli.setValue(frames.get(pos).getEyeXMove());
				servoitemholder[4][3].sli.setValue(frames.get(pos).getEyeYMove());
				servoitemholder[4][4].sli.setValue(frames.get(pos).getJawMove());

				servoitemholder[5][0].sli.setValue(frames.get(pos).getTopStomMove());
				servoitemholder[5][1].sli.setValue(frames.get(pos).getMidStomMove());
				servoitemholder[5][2].sli.setValue(frames.get(pos).getLowStomMove());
				frame_add_textfield.setText(frames.get(pos).getName());
			} else {
				servoitemholder[0][0].spe.setText(frames.get(pos).getRightThumbFingerSpeed() + "");
				servoitemholder[0][1].spe.setText(frames.get(pos).getRightIndexFingerSpeed() + "");
				servoitemholder[0][2].spe.setText(frames.get(pos).getRightMajeureFingerSpeed() + "");
				servoitemholder[0][3].spe.setText(frames.get(pos).getRightRingFingerSpeed() + "");
				servoitemholder[0][4].spe.setText(frames.get(pos).getRightPinkyFingerSpeed() + "");
				servoitemholder[0][5].spe.setText(frames.get(pos).getRightWristSpeed() + "");

				servoitemholder[1][0].spe.setText(frames.get(pos).getRightBicepsSpeed() + "");
				servoitemholder[1][1].spe.setText(frames.get(pos).getRightRotateSpeed() + "");
				servoitemholder[1][2].spe.setText(frames.get(pos).getRightShoulderSpeed() + "");
				servoitemholder[1][3].spe.setText(frames.get(pos).getRightOmoplateSpeed() + "");

				servoitemholder[2][0].spe.setText(frames.get(pos).getLeftThumbFingerSpeed() + "");
				servoitemholder[2][1].spe.setText(frames.get(pos).getLeftIndexFingerSpeed() + "");
				servoitemholder[2][2].spe.setText(frames.get(pos).getLeftMajeureFingerSpeed() + "");
				servoitemholder[2][3].spe.setText(frames.get(pos).getLeftRingFingerSpeed() + "");
				servoitemholder[2][4].spe.setText(frames.get(pos).getLeftPinkyFingerSpeed() + "");
				servoitemholder[2][5].spe.setText(frames.get(pos).getLeftWristSpeed() + "");

				servoitemholder[3][0].spe.setText(frames.get(pos).getLeftBicepsSpeed() + "");
				servoitemholder[3][1].spe.setText(frames.get(pos).getLeftRotateSpeed() + "");
				servoitemholder[3][2].spe.setText(frames.get(pos).getLeftShoulderSpeed() + "");
				servoitemholder[3][3].spe.setText(frames.get(pos).getLeftOmoplateSpeed() + "");

				servoitemholder[4][0].spe.setText(frames.get(pos).getNeckSpeed() + "");
				servoitemholder[4][1].spe.setText(frames.get(pos).getHeadRotateSpeed() + "");
				servoitemholder[4][2].spe.setText(frames.get(pos).getEyeXSpeed() + "");
				servoitemholder[4][3].spe.setText(frames.get(pos).getEyeYSpeed() + "");
				servoitemholder[4][4].spe.setText(frames.get(pos).getJawSpeed() + "");

				servoitemholder[5][0].spe.setText(frames.get(pos).getTopStomSpeed() + "");
				servoitemholder[5][1].spe.setText(frames.get(pos).getMidStomSpeed() + "");
				servoitemholder[5][2].spe.setText(frames.get(pos).getLowStomSpeed() + "");
			}
		}
	}

	public void frame_moverealtime(JCheckBox frame_moverealtime) {
		moverealtime = frame_moverealtime.isSelected();
	}

	public void frame_remove(JList framelist) {
		// Remove this frame from the framelist (button bottom-right)
		int pos = framelist.getSelectedIndex();
		if (pos != -1) {
			frames.remove(pos);

			framelistact(framelist);
		}
	}

	private void executeFrameOnRobot(Frame fih) {
		if (fih.getFrameType() == FrameType.SLEEP) {
			LOGGER.info("Running [SLEEP] frame for \"" + fih.getSleep() + "\" seconds...");
			// sleep frame
			sleep(fih.getSleep());
		} else if (fih.getFrameType() == FrameType.SPEECH) {
			// speech frame
			try {
				LOGGER.info("Running [SPEECH] frame with text: \"" + fih.getSpeech() + "\"...");
				i01.mouth.speakBlocking(fih.getSpeech());
			} catch (Exception e) {
				LOGGER.warn("Speech frame test error", e);
			}
		} else if (fih.getFrameType() == FrameType.MOVE) {
			// move frame
			LOGGER.info("Running [MOVE] frame \"" + fih.getName() + "\"...");
			try {
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
				LOGGER.warn("MOVE frame test error", e);
			}
		} else if (fih.getFrameType() == FrameType.SPEED) {
			// speed frame
			LOGGER.info("Running [SPEED] frame...");
			try {
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
				LOGGER.warn("SPEED frame test error", e);
			}
		}
		LOGGER.info("Finished frame execution.");
	}

	public void frame_test(JList framelist) {
		// Test selected frame
		int selectedFrameIndex = framelist.getSelectedIndex();
		if (i01 != null && selectedFrameIndex != -1) {
			Frame fih = frames.get(selectedFrameIndex);
			executeFrameOnRobot(fih);
		} else {
			if (selectedFrameIndex == -1) {
				// this should go into some kind of message to the user
				LOGGER.info("Please select a frame!");
			} else {
				// this should go into some kind of message to the user
				LOGGER.info("Testing of frame is not possible!");
				LOGGER.info("Robot is not initialised!");
			}
		}
	}

	public void frame_up(JList framelist) {
		// Move this frame one up on the framelist (button bottom-right)
		int pos = framelist.getSelectedIndex();

		if (pos != -1) {
			Frame fih = frames.remove(pos);
			frames.add(pos - 1, fih);

			framelistact(framelist);
		}
	}

	public void frame_update(JList framelist, JTextField frame_add_textfield, JTextField frame_addsleep_textfield,
			JTextField frame_addspeech_textfield) {
		// Update this frame on the framelist (button bottom-right)

		int pos = framelist.getSelectedIndex();

		if (pos != -1) {
			Frame fih = new Frame(Frame.FrameType.SLEEP);

			// sleep || speech || servo movement || speed setting
			if (frames.get(pos).getSleep() != -1) {
				fih.setFrameType(Frame.FrameType.SLEEP);
				fih.setSleep(Integer.parseInt(frame_addsleep_textfield.getText()));
				fih.setSpeech(null);
				fih.setName(null);
			} else if (frames.get(pos).getSpeech() != null) {
				fih.setFrameType(Frame.FrameType.SPEECH);
				fih.setSleep(-1);
				fih.setSpeech(frame_addspeech_textfield.getText());
				fih.setName(null);
			} else if (frames.get(pos).getName() != null) {
				fih.setFrameType(Frame.FrameType.MOVE);
				fih.setRightThumbFingerMove(servoitemholder[0][0].sli.getValue());
				fih.setRightIndexFingerMove(servoitemholder[0][1].sli.getValue());
				fih.setRightMajeureFingerMove(servoitemholder[0][2].sli.getValue());
				fih.setRightRingFingerMove(servoitemholder[0][3].sli.getValue());
				fih.setRightPinkyFingerMove(servoitemholder[0][4].sli.getValue());
				fih.setRightWristMove(servoitemholder[0][5].sli.getValue());

				fih.setRightBicepsMove(servoitemholder[1][0].sli.getValue());
				fih.setRightRotateMove(servoitemholder[1][1].sli.getValue());
				fih.setRightShoulderMove(servoitemholder[1][2].sli.getValue());
				fih.setRightOmoplateMove(servoitemholder[1][3].sli.getValue());

				fih.setLeftThumbFingerMove(servoitemholder[2][0].sli.getValue());
				fih.setLeftIndexFingerMove(servoitemholder[2][1].sli.getValue());
				fih.setLeftMajeureFingerMove(servoitemholder[2][2].sli.getValue());
				fih.setLeftRingFingerMove(servoitemholder[2][3].sli.getValue());
				fih.setLeftPinkyFingerMove(servoitemholder[2][4].sli.getValue());
				fih.setLeftWristMove(servoitemholder[2][5].sli.getValue());

				fih.setLeftBicepsMove(servoitemholder[3][0].sli.getValue());
				fih.setLeftRotateMove(servoitemholder[3][1].sli.getValue());
				fih.setLeftShoulderMove(servoitemholder[3][2].sli.getValue());
				fih.setLeftOmoplateMove(servoitemholder[3][3].sli.getValue());

				fih.setNeckMove(servoitemholder[4][0].sli.getValue());
				fih.setHeadRotateMove(servoitemholder[4][1].sli.getValue());
				fih.setEyeXMove(servoitemholder[4][2].sli.getValue());
				fih.setEyeYMove(servoitemholder[4][3].sli.getValue());
				fih.setJawMove(servoitemholder[4][4].sli.getValue());

				fih.setTopStomMove(servoitemholder[5][0].sli.getValue());
				fih.setMidStomMove(servoitemholder[5][1].sli.getValue());
				fih.setLowStomMove(servoitemholder[5][2].sli.getValue());

				fih.setSleep(-1);
				fih.setSpeech(null);
				fih.setName(frame_add_textfield.getText());
			} else {
				fih.setFrameType(Frame.FrameType.SPEED);
				fih.setRightThumbFingerSpeed(Double.parseDouble(servoitemholder[0][0].spe.getText()));
				fih.setRightIndexFingerSpeed(Double.parseDouble(servoitemholder[0][1].spe.getText()));
				fih.setRightMajeureFingerSpeed(Double.parseDouble(servoitemholder[0][2].spe.getText()));
				fih.setRightRingFingerSpeed(Double.parseDouble(servoitemholder[0][3].spe.getText()));
				fih.setRightPinkyFingerSpeed(Double.parseDouble(servoitemholder[0][4].spe.getText()));
				fih.setRightWristSpeed(Double.parseDouble(servoitemholder[0][5].spe.getText()));

				fih.setRightBicepsSpeed(Double.parseDouble(servoitemholder[1][0].spe.getText()));
				fih.setRightRotateSpeed(Double.parseDouble(servoitemholder[1][1].spe.getText()));
				fih.setRightShoulderSpeed(Double.parseDouble(servoitemholder[1][2].spe.getText()));
				fih.setRightOmoplateSpeed(Double.parseDouble(servoitemholder[1][3].spe.getText()));

				fih.setLeftThumbFingerSpeed(Double.parseDouble(servoitemholder[2][0].spe.getText()));
				fih.setLeftIndexFingerSpeed(Double.parseDouble(servoitemholder[2][1].spe.getText()));
				fih.setLeftMajeureFingerSpeed(Double.parseDouble(servoitemholder[2][2].spe.getText()));
				fih.setLeftRingFingerSpeed(Double.parseDouble(servoitemholder[2][3].spe.getText()));
				fih.setLeftPinkyFingerSpeed(Double.parseDouble(servoitemholder[2][4].spe.getText()));
				fih.setLeftWristSpeed(Double.parseDouble(servoitemholder[2][5].spe.getText()));

				fih.setLeftBicepsSpeed(Double.parseDouble(servoitemholder[3][0].spe.getText()));
				fih.setLeftRotateSpeed(Double.parseDouble(servoitemholder[3][1].spe.getText()));
				fih.setLeftShoulderSpeed(Double.parseDouble(servoitemholder[3][2].spe.getText()));
				fih.setLeftOmoplateSpeed(Double.parseDouble(servoitemholder[3][3].spe.getText()));

				fih.setNeckSpeed(Double.parseDouble(servoitemholder[4][0].spe.getText()));
				fih.setHeadRotateSpeed(Double.parseDouble(servoitemholder[4][1].spe.getText()));
				fih.setEyeXSpeed(Double.parseDouble(servoitemholder[4][2].spe.getText()));
				fih.setEyeYSpeed(Double.parseDouble(servoitemholder[4][3].spe.getText()));
				fih.setJawSpeed(Double.parseDouble(servoitemholder[4][4].spe.getText()));

				fih.setTopStomSpeed(Double.parseDouble(servoitemholder[5][0].spe.getText()));
				fih.setMidStomSpeed(Double.parseDouble(servoitemholder[5][1].spe.getText()));
				fih.setLowStomSpeed(Double.parseDouble(servoitemholder[5][2].spe.getText()));

				fih.setSleep(-1);
				fih.setSpeech(null);
				fih.setName(null);
			}
			frames.set(pos, fih);

			framelistact(framelist);
		}
	}

	public void framelistact(JList framelist) {
		// Re-Build the framelist
		frameListGlobal = framelist;
		String[] listdata = new String[frames.size()];

		for (int i = 0; i < frames.size(); i++) {
			Frame fih = frames.get(i);

			String displaytext = "";

			// servo movement || sleep || speech || speed setting
			if (fih.getSleep() != -1) {
				displaytext = "SLEEP   " + fih.getSleep();
			} else if (fih.getSpeech() != null) {
				displaytext = "SPEECH   " + fih.getSpeech();
			} else if (fih.getName() != null) {
				String displaytext1 = "";
				String displaytext2 = "";
				String displaytext3 = "";
				String displaytext4 = "";
				String displaytext5 = "";
				String displaytext6 = "";
				if (fih.getRightHandMoveSet()) {
					displaytext1 = fih.getRightThumbFingerMove() + " " + fih.getRightIndexFingerMove() + " "
							+ fih.getRightMajeureFingerMove() + " " + fih.getRightRingFingerMove() + " "
							+ fih.getRightPinkyFingerMove() + " " + fih.getRightWristMove();
				}
				if (fih.getRightArmMoveSet()) {
					displaytext2 = fih.getRightBicepsMove() + " " + fih.getRightRotateMove() + " "
							+ fih.getRightShoulderMove() + " " + fih.getRightOmoplateMove();
				}
				if (fih.getLeftHandMoveSet()) {
					displaytext3 = fih.getLeftThumbFingerMove() + " " + fih.getLeftIndexFingerMove() + " "
							+ fih.getLeftMajeureFingerMove() + " " + fih.getLeftRingFingerMove() + " "
							+ fih.getLeftPinkyFingerMove() + " " + fih.getLeftWristMove();
				}
				if (fih.getLeftArmMoveSet()) {
					displaytext4 = fih.getLeftBicepsMove() + " " + fih.getLeftRotateMove() + " "
							+ fih.getLeftShoulderMove() + " " + fih.getLeftOmoplateMove();
				}
				if (fih.getHeadMoveSet()) {
					displaytext5 = fih.getNeckMove() + " " + fih.getHeadRotateMove() + " " + fih.getEyeXMove() + " "
							+ fih.getEyeYMove() + " " + fih.getJawMove();
				}
				if (fih.getTorsoMoveSet()) {
					displaytext6 = fih.getTopStomMove() + " " + fih.getMidStomMove() + " " + fih.getLowStomMove();
				}
				displaytext = fih.getName() + ": " + displaytext1 + " | " + displaytext2 + " | " + displaytext3 + " | "
						+ displaytext4 + " | " + displaytext5 + " | " + displaytext6;
			} else {
				String displaytext1 = "";
				String displaytext2 = "";
				String displaytext3 = "";
				String displaytext4 = "";
				String displaytext5 = "";
				String displaytext6 = "";
				if (fih.getRightHandMoveSet()) {
					displaytext1 = fih.getRightThumbFingerSpeed() + " " + fih.getRightIndexFingerSpeed() + " "
							+ fih.getRightMajeureFingerSpeed() + " " + fih.getRightRingFingerSpeed() + " "
							+ fih.getRightPinkyFingerSpeed() + " " + fih.getRightWristSpeed();
				}
				if (fih.getRightArmMoveSet()) {
					displaytext2 = fih.getRightBicepsSpeed() + " " + fih.getRightRotateSpeed() + " "
							+ fih.getRightShoulderSpeed() + " " + fih.getRightOmoplateSpeed();
				}
				if (fih.getLeftHandMoveSet()) {
					displaytext3 = fih.getLeftThumbFingerSpeed() + " " + fih.getLeftIndexFingerSpeed() + " "
							+ fih.getLeftMajeureFingerSpeed() + " " + fih.getLeftRingFingerSpeed() + " "
							+ fih.getLeftPinkyFingerSpeed() + " " + fih.getLeftWristSpeed();
				}
				if (fih.getLeftArmMoveSet()) {
					displaytext4 = fih.getLeftBicepsSpeed() + " " + fih.getLeftRotateSpeed() + " "
							+ fih.getLeftShoulderSpeed() + " " + fih.getLeftOmoplateSpeed();
				}
				if (fih.getHeadMoveSet()) {
					displaytext5 = fih.getNeckSpeed() + " " + fih.getHeadRotateSpeed() + " " + fih.getEyeXSpeed() + " "
							+ fih.getEyeYSpeed() + " " + fih.getJawSpeed();
				}
				if (fih.getTorsoMoveSet()) {
					displaytext6 = fih.getTopStomSpeed() + " " + fih.getMidStomSpeed() + " " + fih.getLowStomSpeed();
				}
				displaytext = "SPEED   " + displaytext1 + " | " + displaytext2 + " | " + displaytext3 + " | "
						+ displaytext4 + " | " + displaytext5 + " | " + displaytext6;
			}
			listdata[i] = displaytext;
		}

		framelist.setListData(listdata);
	}

	public void control_ScriptFolder(JList control_list) {
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		jfc.setDialogTitle("Choose a directory with Gesture scripts in Python");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnValue = jfc.showSaveDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			if (jfc.getSelectedFile().isDirectory()) {
				LOGGER.info("Selected script directory: " + jfc.getSelectedFile());

				File[] listOfFiles = jfc.getSelectedFile().listFiles();
				pythonFiles.clear();
				List<String> pythonFileNames = new ArrayList<String>();
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".py")) {
						// list only ".py" files in the folder
						pythonFileNames.add(listOfFiles[i].getName());
						pythonFiles.add(listOfFiles[i]);
					}
				}
				if (pythonFileNames != null && pythonFileNames.size() > 0) {
					control_list.setListData(pythonFileNames.toArray());
				}
			}
		}
	}

	public void control_loadscri(JList control_list, JList frameListGui) {
		List<String> scriptLines = new ArrayList<String>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			File selectedFile = pythonFiles.get(control_list.getSelectedIndex());
			LOGGER.info("Loading script \"" + selectedFile.getAbsolutePath() + "\"...");
			fileReader = new FileReader(selectedFile);
//			fileReader = new FileReader("/home/abe/ws-fx/inmoov/InMoov/gestures/" + control_list.getSelectedValue().toString());
//			fileReader = new FileReader("/d:/balance.py");
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
			parseScriptToGesture(scriptLines);
			LOGGER.info("Parsed \"" + gesture.getGestureName() + "\" GESTURE with FRAME count \"" + frames.size() + "\"");
			// loading parsed frames into GUI list
			controlListReload(frameListGui);
			LOGGER.trace("Reload GUI finished");
		} catch (Exception e) {
			LOGGER.warn("Loading parsed frames", e);
		}
	}

	public void controlListReload(JList framelist) {
		List<String> listdata = new ArrayList<String>();
		for (Frame fih : frames) {
			listdata.add(fih.toString());
		}
		framelist.setListData(listdata.toArray());
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
			List<String> frameLines = new ArrayList<String>();
			for (String singleScriptLine : scriptLines) {
				LOGGER.trace("frameList.size() \"" + frames.size() + "\"");
				// ' sleep(4)'
				singleScriptLine = singleScriptLine.trim();
				LOGGER.trace("singleScriptLine \"" + singleScriptLine + "\"");
				// 'sleep(4)'
				if (!singleScriptLine.contains("setHeadVelocity") && !singleScriptLine.contains("setArmVelocity")
						&& !singleScriptLine.contains("setHandVelocity")
						&& !singleScriptLine.contains("setTorsoVelocity") && !singleScriptLine.contains("setHeadSpeed")
						&& !singleScriptLine.contains("setArmSpeed") && !singleScriptLine.contains("setHandSpeed")
						&& !singleScriptLine.contains("setTorsoSpeed") && !singleScriptLine.contains("moveHead")
						&& !singleScriptLine.contains("moveArm") && !singleScriptLine.contains("moveHand")
						&& !singleScriptLine.contains("moveTorso") && !singleScriptLine.contains("sleep") // end frame
						&& !singleScriptLine.contains("finishedGesture")) { // end gesture
					continue;
				}
				/// at this point we have frame command
				if (singleScriptLine.contains("finishedGesture")) {
					// we are finished
					return;
				} else if (singleScriptLine.contains("speech")) {
					// ignore
					continue;
				} else if (singleScriptLine.contains("sleep")) {
					// sleep means the end of the frame
					try {
						// parse the frame and add it
						parseScriptFragmentIntoSingleFrame(frameLines, counter);
						// finish it with a sleep
						parseScriptSleepToFrameSleep(singleScriptLine);
						// reset framelines
						frameLines.clear();
					} catch (Exception e) {
						LOGGER.error("Exception from function parseScriptFragmentIntoSingleFrame: " + e);
					} finally {
						counter++;
					}
				} else {
					frameLines.add(singleScriptLine);
				}
			}
		} catch (Exception e) {
			LOGGER.warn("parseScriptToFrame error", e);
		}
	}

	private void parseScriptFragmentIntoSingleFrame(List<String> frameLines, int frameCounter)
			throws Exception {
		try {
			boolean addSpeed = false;
			boolean addMove = false;
			Frame fihSpeed = new Frame(Frame.FrameType.SPEED);
			fihSpeed.setSpeech(null);
			fihSpeed.setName(null);
			fihSpeed.setSleep(-1);
			Frame fihMove = new Frame(Frame.FrameType.MOVE);
			fihMove.setName("Frame#" + frameCounter);

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
						addSpeed = true;
						if (splitString[0].contains("Head")) {
							// setHeadSpeed(0.95,0.95)
							// it has to have 2 arguments
							if (valuesString.length > 0) {
								fihSpeed.setHeadSpeedSet(true);
								fihSpeed.setHeadRotateSpeed(Double.parseDouble(valuesString[0].trim()));
							}
							if (valuesString.length > 1) {
								fihSpeed.setNeckSpeed(Double.parseDouble(valuesString[1].trim()));
							}
							if (valuesString.length > 2) {
								fihSpeed.setEyeXSpeed(Double.parseDouble(valuesString[2].trim()));
							}
							if (valuesString.length > 3) {
								fihSpeed.setEyeYSpeed(Double.parseDouble(valuesString[3].trim()));
							}
							if (valuesString.length > 4) {
								fihSpeed.setJawSpeed(Double.parseDouble(valuesString[4].trim()));
							}
						} else if (splitString[0].contains("Torso")) {
							// setTorsoSpeed(0.95,0.85,1.0)
							if (valuesString.length > 0) {
								fihSpeed.setTorsoSpeedSet(true);
								fihSpeed.setTopStomSpeed(Double.parseDouble(valuesString[0].trim()));
							}
							if (valuesString.length > 1) {
								fihSpeed.setMidStomSpeed(Double.parseDouble(valuesString[1].trim()));
							}
							if (valuesString.length > 2) {
								fihSpeed.setLowStomSpeed(Double.parseDouble(valuesString[2].trim()));
							}
						} else if (splitString[0].contains("Arm")) {
							if (valuesString.length > 0) {
								if (valuesString[0].contains("left")) {
									// setArmSpeed("left",1.0,0.85,0.95,0.95)
									fihSpeed.setLeftArmSpeedSet(true);
									if (valuesString.length > 1) {
										fihSpeed.setLeftBicepsSpeed(Double.parseDouble(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										fihSpeed.setLeftRotateSpeed(Double.parseDouble(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										fihSpeed.setLeftShoulderSpeed(Double.parseDouble(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										fihSpeed.setLeftOmoplateSpeed(Double.parseDouble(valuesString[4].trim()));
									}
								} else if (valuesString[0].contains("right")) {
									// setArmSpeed("right",0.65,0.85,0.65,0.85)
									fihSpeed.setRightArmSpeedSet(true);
									if (valuesString.length > 1) {
										fihSpeed.setRightBicepsSpeed(Double.parseDouble(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										fihSpeed.setRightRotateSpeed(Double.parseDouble(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										fihSpeed.setRightShoulderSpeed(Double.parseDouble(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										fihSpeed.setRightOmoplateSpeed(Double.parseDouble(valuesString[4].trim()));
									}
								}
							}
						} else if (splitString[0].contains("Hand")) {
							if (valuesString.length > 0) {
								if (valuesString[0].contains("left")) {
									// setHandSpeed("left",0.85,0.85,0.85,0.85,0.85,0.85)
									fihSpeed.setLeftHandSpeedSet(true);
									if (valuesString.length > 1) {
										fihSpeed.setLeftThumbFingerSpeed(Double.parseDouble(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										fihSpeed.setLeftIndexFingerSpeed(Double.parseDouble(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										fihSpeed.setLeftMajeureFingerSpeed(Double.parseDouble(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										fihSpeed.setLeftRingFingerSpeed(Double.parseDouble(valuesString[4].trim()));
									}
									if (valuesString.length > 5) {
										fihSpeed.setLeftPinkyFingerSpeed(Double.parseDouble(valuesString[5].trim()));
									}
									if (valuesString.length > 6) {
										fihSpeed.setLeftWristSpeed(Double.parseDouble(valuesString[6].trim()));
									}
								} else if (valuesString[0].contains("right")) {
									fihSpeed.setRightHandSpeedSet(true);
									// setHandSpeed("right",0.85,0.85,0.85,0.85,0.85,0.85)
									if (valuesString.length > 1) {
										fihSpeed.setRightThumbFingerSpeed(Double.parseDouble(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										fihSpeed.setRightIndexFingerSpeed(Double.parseDouble(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										fihSpeed.setRightMajeureFingerSpeed(Double.parseDouble(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										fihSpeed.setRightRingFingerSpeed(Double.parseDouble(valuesString[4].trim()));
									}
									if (valuesString.length > 5) {
										fihSpeed.setRightPinkyFingerSpeed(Double.parseDouble(valuesString[5].trim()));
									}
									if (valuesString.length > 6) {
										fihSpeed.setRightWristSpeed(Double.parseDouble(valuesString[6].trim()));
									}
								}
							}
						}
					} else if (splitString[0].contains("move")) {
						addMove = true;
						if (splitString[0].contains("Head")) {
							// moveHead(79,100,82,78,65)
							if (valuesString.length > 0) {
								fihMove.setNeckMove(Integer.parseInt(valuesString[0].trim()));
							}
							if (valuesString.length > 1) {
								fihMove.setHeadRotateMove(Integer.parseInt(valuesString[1].trim()));
							}
							if (valuesString.length > 2) {
								fihMove.setEyeXMove(Integer.parseInt(valuesString[2].trim()));
							}
							if (valuesString.length > 3) {
								fihMove.setEyeYMove(Integer.parseInt(valuesString[3].trim()));
							}
							if (valuesString.length > 4) {
								fihMove.setJawMove(Integer.parseInt(valuesString[4].trim()));
							}
						} else if (splitString[0].contains("Arm")) {
							if (valuesString.length > 0) {
								if (valuesString[0].contains("left")) {
									// moveArm("left",5,84,28,15)
									if (valuesString.length > 1) {
										fihMove.setLeftBicepsMove(Integer.parseInt(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										fihMove.setLeftRotateMove(Integer.parseInt(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										fihMove.setLeftShoulderMove(Integer.parseInt(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										fihMove.setLeftOmoplateMove(Integer.parseInt(valuesString[4].trim()));
									}
								} else if (valuesString[0].contains("right")) {
									// moveArm("right",5,82,28,15)
									if (valuesString.length > 1) {
										fihMove.setRightBicepsMove(Integer.parseInt(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										fihMove.setRightRotateMove(Integer.parseInt(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										fihMove.setRightShoulderMove(Integer.parseInt(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										fihMove.setRightOmoplateMove(Integer.parseInt(valuesString[4].trim()));
									}
								}
							}
						} else if (splitString[0].contains("Hand")) {
							if (valuesString.length > 0) {
								if (valuesString[0].contains("left")) {
									// moveHand("left",92,33,37,71,66,25)
									if (valuesString.length > 1) {
										fihMove.setLeftThumbFingerMove(Integer.parseInt(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										fihMove.setLeftIndexFingerMove(Integer.parseInt(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										fihMove.setLeftMajeureFingerMove(Integer.parseInt(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										fihMove.setLeftRingFingerMove(Integer.parseInt(valuesString[4].trim()));
									}
									if (valuesString.length > 5) {
										fihMove.setLeftPinkyFingerMove(Integer.parseInt(valuesString[5].trim()));
									}
									if (valuesString.length > 6) {
										fihMove.setLeftWristMove(Integer.parseInt(valuesString[6].trim()));
									}
								} else if (valuesString[0].contains("right")) {
									// moveHand("right",81,66,82,60,105,113)
									if (valuesString.length > 1) {
										fihMove.setRightThumbFingerMove(Integer.parseInt(valuesString[1].trim()));
									}
									if (valuesString.length > 2) {
										fihMove.setRightIndexFingerMove(Integer.parseInt(valuesString[2].trim()));
									}
									if (valuesString.length > 3) {
										fihMove.setRightMajeureFingerMove(Integer.parseInt(valuesString[3].trim()));
									}
									if (valuesString.length > 4) {
										fihMove.setRightRingFingerMove(Integer.parseInt(valuesString[4].trim()));
									}
									if (valuesString.length > 5) {
										fihMove.setRightPinkyFingerMove(Integer.parseInt(valuesString[5].trim()));
									}
									if (valuesString.length > 6) {
										fihMove.setRightWristMove(Integer.parseInt(valuesString[6].trim()));
									}
								}
							}
						} else if (splitString[0].contains("Torso")) {
							// moveTorso(90,90,90)
							if (valuesString.length > 0) {
								fihMove.setTopStomMove(Integer.parseInt(valuesString[0].trim()));
							}
							if (valuesString.length > 1) {
								fihMove.setMidStomMove(Integer.parseInt(valuesString[1].trim()));
							}
							if (valuesString.length > 2) {
								fihMove.setLowStomMove(Integer.parseInt(valuesString[2].trim()));
							}
						}
					}
				} catch (Exception e) {
					LOGGER.warn("Frame line parsing error on frame: " + frameCounter + "! ", e);
				}
			}
			if (addSpeed) {
				frames.add(fihSpeed);
			}
			if (addMove) {
				frames.add(fihMove);
			}
		} catch (Exception e) {
			LOGGER.warn("Frame line parsing error", e);
		}
	}

	private void parseScriptSleepToFrameSleep(String sleepLine) {
		try {
			// sleep line: sleep(3)
			sleepLine = sleepLine.substring(sleepLine.indexOf('(') + 1, sleepLine.indexOf(')'));
			Double sleepTime = Double.parseDouble(sleepLine);
			Frame fihSleep = new Frame(Frame.FrameType.SLEEP);
			fihSleep.resetValues();

			fihSleep.setName(null); // sleep frame has Name and Speech as null and Sleep as int
			fihSleep.setSpeech(null);
			fihSleep.setSleep(sleepTime.intValue());

			frames.add(fihSleep);
		} catch (Exception e) {
			LOGGER.warn("Sleep line parsing error", e);
		}
	}

	public void parse_frame_to_script() {
		String code = "def " + /* ime_funkcije */"test" + "():\n  i01.startedGesture()\n  "; // def + ime plus () +
																								// enter i dva spejsa +
																								// i01.
		for (int i = 0; i < frames.size(); i++) {
			Frame fih = frames.get(i);

			if (fih.getName() == null && fih.getSleep() == -1) {
				String speeds[] = { "", "", "", "", "", "" };
				if (fih.getRightHandMoveSet())
					speeds[0] = "i01.setHeadVelocity(" + fih.getHeadRotateSpeed() + "," + fih.getNeckSpeed() + ")";
				if (fih.getRightArmMoveSet())
					speeds[1] = "i01.setArmVelocity(\"left\"," + fih.getLeftBicepsSpeed() + ","
							+ fih.getLeftRotateSpeed() + "," + fih.getLeftShoulderSpeed() + ","
							+ fih.getLeftOmoplateSpeed() + ")";
				if (fih.getLeftHandMoveSet())
					speeds[2] = "i01.setArmVelocity(\"right\"," + fih.getRightBicepsSpeed() + ","
							+ fih.getRightRotateSpeed() + "," + fih.getRightShoulderSpeed() + ","
							+ fih.getRightOmoplateSpeed() + ")";
				if (fih.getLeftArmMoveSet())
					speeds[3] = "i01.setHandVelocity(\"left\"," + fih.getLeftThumbFingerSpeed() + ","
							+ fih.getLeftPinkyFingerSpeed() + "," + fih.getLeftMajeureFingerSpeed() + ","
							+ fih.getLeftRingFingerSpeed() + "," + fih.getLeftPinkyFingerSpeed() + ","
							+ fih.getLeftWristSpeed() + ")";
				if (fih.getHeadMoveSet())
					speeds[4] = "i01.setHandVelocity(\"right\"," + fih.getRightThumbFingerSpeed() + ","
							+ fih.getRightPinkyFingerSpeed() + "," + fih.getRightMajeureFingerSpeed() + ","
							+ fih.getRightRingFingerSpeed() + "," + fih.getRightPinkyFingerMove() + ","
							+ fih.getRightWristSpeed() + ")";
				if (fih.getTorsoMoveSet())
					speeds[5] = "i01.setTorsoVelocity(" + fih.getTopStomSpeed() + "," + fih.getMidStomSpeed() + ","
							+ fih.getLowStomSpeed() + ")";
				for (int j = 0; j <= 5; j++) {
					// TODO
//    			  if(fih.getMoveSet()[j]) 
					code += (speeds[j] + "\n  ");
				}
			} else if (fih.getName() == null && fih.getSleep() != -1) {
				code += "sleep(" + fih.getSleep() + ")\n  ";
			} else {
				String movements[] = { "", "", "", "", "", "" };
				if (fih.getRightHandMoveSet())
					movements[0] = "i01.moveHead(" + fih.getNeckMove() + "," + fih.getHeadRotateMove() + ","
							+ fih.getEyeXMove() + "," + fih.getEyeYMove() + "," + fih.getJawMove() + ")";
				if (fih.getRightArmMoveSet())
					movements[1] = "i01.moveArm(\"left\"," + fih.getLeftBicepsMove() + "," + fih.getLeftRotateMove()
							+ "," + fih.getLeftShoulderMove() + "," + fih.getLeftOmoplateMove() + ")";
				if (fih.getLeftHandMoveSet())
					movements[2] = "i01.moveArm(\"right\"," + fih.getRightBicepsMove() + "," + fih.getRightRotateMove()
							+ "," + fih.getRightShoulderMove() + "," + fih.getRightOmoplateMove() + ")";
				if (fih.getLeftArmMoveSet())
					movements[3] = "i01.moveHand(\"left\"," + fih.getLeftThumbFingerMove() + ","
							+ fih.getLeftPinkyFingerMove() + "," + fih.getLeftMajeureFingerMove() + ","
							+ fih.getLeftRingFingerMove() + "," + fih.getLeftPinkyFingerMove() + ","
							+ fih.getLeftWristMove() + ")";
				if (fih.getHeadMoveSet())
					movements[4] = "i01.moveHand(\"right\"," + fih.getRightThumbFingerMove() + ","
							+ fih.getRightPinkyFingerMove() + "," + fih.getRightMajeureFingerMove() + ","
							+ fih.getRightRingFingerMove() + "," + fih.getRightPinkyFingerMove() + ","
							+ fih.getRightWristMove() + ")";
				if (fih.getTorsoMoveSet())
					movements[5] = "i01.moveTorso(" + fih.getTopStomMove() + "," + fih.getMidStomMove() + ","
							+ fih.getLowStomMove() + ")";
				for (int j = 0; j <= 5; j++) {
					// TODO
//    			  if(fih.getMoveSet()[j]) 
					code += (movements[j] + "\n  ");
				}
			}
		}
		// code += "i01.finishedGesture()";
		parsirani_kod = code;
	}

	public void frame_add(JList framelist, JTextField frame_add_textfield) {
		// Add a servo movement frame to the framelist (button bottom-right)
		Frame fih = new Frame(Frame.FrameType.MOVE);

		fih.setRightThumbFingerMove(servoitemholder[0][0].sli.getValue());
		fih.setRightIndexFingerMove(servoitemholder[0][1].sli.getValue());
		fih.setRightMajeureFingerMove(servoitemholder[0][2].sli.getValue());
		fih.setRightRingFingerMove(servoitemholder[0][3].sli.getValue());
		fih.setRightPinkyFingerMove(servoitemholder[0][4].sli.getValue());
		fih.setRightWristMove(servoitemholder[0][5].sli.getValue());

		fih.setRightBicepsMove(servoitemholder[1][0].sli.getValue());
		fih.setRightRotateMove(servoitemholder[1][1].sli.getValue());
		fih.setRightShoulderMove(servoitemholder[1][2].sli.getValue());
		fih.setRightOmoplateMove(servoitemholder[1][3].sli.getValue());

		fih.setLeftThumbFingerMove(servoitemholder[2][0].sli.getValue());
		fih.setLeftIndexFingerMove(servoitemholder[2][1].sli.getValue());
		fih.setLeftMajeureFingerMove(servoitemholder[2][2].sli.getValue());
		fih.setLeftRingFingerMove(servoitemholder[2][3].sli.getValue());
		fih.setLeftPinkyFingerMove(servoitemholder[2][4].sli.getValue());
		fih.setLeftWristMove(servoitemholder[2][5].sli.getValue());

		fih.setLeftBicepsMove(servoitemholder[3][0].sli.getValue());
		fih.setLeftRotateMove(servoitemholder[3][1].sli.getValue());
		fih.setLeftShoulderMove(servoitemholder[3][2].sli.getValue());
		fih.setLeftOmoplateMove(servoitemholder[3][3].sli.getValue());

		fih.setNeckMove(servoitemholder[4][0].sli.getValue());
		fih.setHeadRotateMove(servoitemholder[4][1].sli.getValue());
		fih.setEyeXMove(servoitemholder[4][2].sli.getValue());
		fih.setEyeYMove(servoitemholder[4][3].sli.getValue());
		fih.setJawMove(servoitemholder[4][4].sli.getValue());

		fih.setTopStomMove(servoitemholder[5][0].sli.getValue());
		fih.setMidStomMove(servoitemholder[5][1].sli.getValue());
		fih.setLowStomMove(servoitemholder[5][2].sli.getValue());

		fih.setSleep(-1);
		fih.setSpeech(null);
		fih.setName(frame_add_textfield.getText());

		frames.add(fih);

		framelistact(framelist);
	}

	public void parsescript(JList control_list) {
		pythonitemholder.clear();

		if (true) { // wut?
			String pscript = pythonscript;
			String[] pscriptsplit = pscript.split("\n");

			// get the name of the InMoov-reference
			for (String line : pscriptsplit) {
				if (line.contains(" = Runtime.createAndStart(") || line.contains("Runtime.start(")) {
					if (line.contains(", \"InMoov\")")) {
						pythonname = line.substring(0, line.indexOf(" = "));
						referencename = line.substring(line.indexOf("(\"") + 2, line.indexOf("\", \"InMoov\")"));
					}
				}
			}

			PythonItemHolder pih = null;
			boolean keepgoing = true;
			int pos = 0;
			while (keepgoing) {
				if (pih == null) {
					pih = new PythonItemHolder();
				}
				if (pos >= pscriptsplit.length) {
					keepgoing = false;
					break;
				}
				String line = pscriptsplit[pos];
				String linewithoutspace = line.replace(" ", "");
				if (linewithoutspace.equals("")) {
					pos++;
					continue;
				}
				if (linewithoutspace.startsWith("#")) {
					pih.code = pih.code + "\n" + line;
					pos++;
					continue;
				}
				line = line.replace("  ", "    "); // 2 -> 4
				line = line.replace("   ", "    "); // 3 -> 4
				line = line.replace("     ", "    "); // 5 -> 4
				line = line.replace("      ", "    "); // 6 -> 4

				if (!(pih.function) && !(pih.notfunction)) {
					if (line.startsWith("def")) {
						pih.function = true;
						pih.notfunction = false;
						pih.modifyable = false;
						pih.code = line;
						pos++;
					} else {
						pih.notfunction = true;
						pih.function = false;
						pih.modifyable = false;
						pih.code = line;
						pos++;
					}
				} else if (pih.function && !(pih.notfunction)) {
					if (line.startsWith("    ")) {
						pih.code = pih.code + "\n" + line;
						pos++;
					} else {
						pythonitemholder.add(pih);
						pih = null;
					}
				} else if (!(pih.function) && pih.notfunction) {
					if (!(line.startsWith("def"))) {
						pih.code = pih.code + "\n" + line;
						pos++;
					} else {
						pythonitemholder.add(pih);
						pih = null;
					}
				} else {
					// it should never end here ...
					// .function & .notfunction true ...
					// would be wrong ...
				}
			}
			pythonitemholder.add(pih);
		}

		if (true) {
			ArrayList<PythonItemHolder> pythonitemholder1 = pythonitemholder;
			pythonitemholder = new ArrayList<PythonItemHolder>();
			for (PythonItemHolder pih : pythonitemholder1) {
				if (pih.function && !(pih.notfunction)) {
					String code = pih.code;
					String[] codesplit = code.split("\n");
					String code2 = "";
					for (String line : codesplit) {
						line = line.replace(" ", "");
						if (line.startsWith("def")) {
							line = "";
						} else if (line.startsWith("sleep")) {
							line = "";
						} else if (line.startsWith(pythonname)) {
							if (line.startsWith(pythonname + ".move")) {
								if (line.startsWith(pythonname + ".moveHead")) {
									line = "";
								} else if (line.startsWith(pythonname + ".moveHand")) {
									line = "";
								} else if (line.startsWith(pythonname + ".moveArm")) {
									line = "";
								} else if (line.startsWith(pythonname + ".moveTorso")) {
									line = "";
								}
							} else if (line.startsWith(pythonname + ".set")) {
								if (line.startsWith(pythonname + ".setHeadSpeed")) {
									line = "";
								} else if (line.startsWith(pythonname + ".setHandSpeed")) {
									line = "";
								} else if (line.startsWith(pythonname + ".setArmSpeed")) {
									line = "";
								} else if (line.startsWith(pythonname + ".setTorsoSpeed")) {
									line = "";
								}
							} else if (line.startsWith(pythonname + ".mouth.speak")) {
								line = "";
							}
						}
						code2 = code2 + line;
					}
					if (code2.length() > 0) {
						pih.modifyable = false;
					} else {
						pih.modifyable = true;
					}
				} else if (!(pih.function) && pih.notfunction) {
					pih.modifyable = false;
				} else {
					// shouldn't get here
					// both true or both false
					// wrong
				}
				pythonitemholder.add(pih);
			}
		}
		controllistact(control_list);
	}

	public void controllistact(JList control_list) {
		String[] listdata = new String[pythonitemholder.size()];
		for (int i = 0; i < pythonitemholder.size(); i++) {
			PythonItemHolder pih = pythonitemholder.get(i);

			String pre;
			if (!(pih.modifyable)) {
				pre = "X    ";
			} else {
				pre = "     ";
			}

			int he = 21;
			if (pih.code.length() < he) {
				he = pih.code.length();
			}

			String des = pih.code.substring(0, he);

			String displaytext = pre + des;
			listdata[i] = displaytext;
		}
		control_list.setListData(listdata);
	}

	public void servoitemholder_set_sih1(int i1, ServoItemHolder[] sih1) {
		// Setting references
		servoitemholder[i1] = sih1;
	}

	public void servoitemholder_slider_changed(int t1, int t2) {
		// One slider were adjusted
		servoitemholder[t1][t2].akt.setText(servoitemholder[t1][t2].sli.getValue() + "");
		// Move the Servos in "Real-Time"
		if (moverealtime && i01 != null) {
			Frame fih = new Frame(Frame.FrameType.MOVE);

			fih.setRightThumbFingerMove(servoitemholder[0][0].sli.getValue());
			fih.setRightIndexFingerMove(servoitemholder[0][1].sli.getValue());
			fih.setRightMajeureFingerMove(servoitemholder[0][2].sli.getValue());
			fih.setRightRingFingerMove(servoitemholder[0][3].sli.getValue());
			fih.setRightPinkyFingerMove(servoitemholder[0][4].sli.getValue());
			fih.setRightWristMove(servoitemholder[0][5].sli.getValue());

			fih.setRightBicepsMove(servoitemholder[1][0].sli.getValue());
			fih.setRightRotateMove(servoitemholder[1][1].sli.getValue());
			fih.setRightShoulderMove(servoitemholder[1][2].sli.getValue());
			fih.setRightOmoplateMove(servoitemholder[1][3].sli.getValue());

			fih.setLeftThumbFingerMove(servoitemholder[2][0].sli.getValue());
			fih.setLeftIndexFingerMove(servoitemholder[2][1].sli.getValue());
			fih.setLeftMajeureFingerMove(servoitemholder[2][2].sli.getValue());
			fih.setLeftRingFingerMove(servoitemholder[2][3].sli.getValue());
			fih.setLeftPinkyFingerMove(servoitemholder[2][4].sli.getValue());
			fih.setLeftWristMove(servoitemholder[2][5].sli.getValue());

			fih.setLeftBicepsMove(servoitemholder[3][0].sli.getValue());
			fih.setLeftRotateMove(servoitemholder[3][1].sli.getValue());
			fih.setLeftShoulderMove(servoitemholder[3][2].sli.getValue());
			fih.setLeftOmoplateMove(servoitemholder[3][3].sli.getValue());

			fih.setNeckMove(servoitemholder[4][0].sli.getValue());
			fih.setHeadRotateMove(servoitemholder[4][1].sli.getValue());
			fih.setEyeXMove(servoitemholder[4][2].sli.getValue());
			fih.setEyeYMove(servoitemholder[4][3].sli.getValue());
			fih.setJawMove(servoitemholder[4][4].sli.getValue());

			fih.setTopStomMove(servoitemholder[5][0].sli.getValue());
			fih.setMidStomMove(servoitemholder[5][1].sli.getValue());
			fih.setLowStomMove(servoitemholder[5][2].sli.getValue());

			if (fih.getRightHandMoveSet()) {
				i01.moveHead(fih.getNeckMove(), fih.getHeadRotateMove(), fih.getEyeXMove(), fih.getEyeYMove(),
						fih.getJawMove());
			}
			if (fih.getRightArmMoveSet()) {
				i01.moveArm("left", fih.getLeftBicepsMove(), fih.getLeftRotateMove(), fih.getLeftShoulderMove(),
						fih.getLeftOmoplateMove());
			}
			if (fih.getLeftHandMoveSet()) {
				i01.moveArm("right", fih.getRightBicepsMove(), fih.getRightRotateMove(), fih.getRightShoulderMove(),
						fih.getRightOmoplateMove());
			}
			if (fih.getLeftArmMoveSet()) {
				i01.moveHand("left", fih.getLeftThumbFingerMove(), fih.getLeftIndexFingerMove(),
						fih.getLeftMajeureFingerMove(), fih.getLeftRingFingerMove(), fih.getLeftPinkyFingerMove(),
						(double) fih.getLeftWristMove());
			}
			if (fih.getHeadMoveSet()) {
				i01.moveHand("right", fih.getRightThumbFingerMove(), fih.getRightIndexFingerMove(),
						fih.getRightMajeureFingerMove(), fih.getRightRingFingerMove(), fih.getRightPinkyFingerMove(),
						(double) fih.getRightWristMove());
			}
			if (fih.getTorsoMoveSet()) {
				i01.moveTorso(fih.getTopStomMove(), fih.getMidStomMove(), fih.getLowStomMove());
			}
		}
	}

	public void tabs_main_checkbox_states_changed(boolean[] tabs_main_checkbox_states2) {
		// checkbox states (on the main site) (for the services) changed
		// TODO this needs to be updated based on FRAME selection
//    tabs_main_checkbox_states = tabs_main_checkbox_states2;
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

	public void frameSelectionChanged(JPanel bottom, int frameItemHolderIndex) {
		LOGGER.trace("frameSelectionChanged [START]");
		LOGGER.info("frame#" + frameItemHolderIndex + ": \"" + frames.get(frameItemHolderIndex) + "\"");
		initializeBottomPaneTabs(bottom, frames.get(frameItemHolderIndex));
		LOGGER.trace("frameSelectionChanged [END]");
	}

	private void addSpeedTextToSectionPane(JPanel panel, Frame frame, RobotSection robotSection) {
		LOGGER.trace("addSpeedTextToSectionPane subSectionSize: \"" + frame.getSubSectionSize(robotSection) + "\"");
		for(int i = 0; i < frame.getSubSectionSize(robotSection); i++) {
			JFormattedTextField speed = new JFormattedTextField(decimalFormat);
			speed.setColumns(4);
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
			LOGGER.warn("Creating bottom tabbed frame", e);
		}
	}

	public void initializeBottomPaneTabs(JPanel bottom, Frame frame) {
		LOGGER.trace("initializeBottomPaneTabs [START]");
		try {
			// main 6 panels for robor sections
			final JPanel rightHandPanel = new JPanel();
			rightHandPanel.setLayout(new BorderLayout());
			final JPanel rightArmPanel = new JPanel();
			rightArmPanel.setLayout(new BorderLayout());
			final JPanel leftHandPanel = new JPanel();
			leftHandPanel.setLayout(new BorderLayout());
			final JPanel leftArmPanel = new JPanel();
			leftArmPanel.setLayout(new BorderLayout());
			final JPanel headPanel = new JPanel();
			headPanel.setLayout(new BorderLayout());
			final JPanel torsoPanel = new JPanel();
			torsoPanel.setLayout(new BorderLayout());
			// Titles
			rightHandPanel.add(BorderLayout.NORTH, new JLabel("Right Hand"));
			rightArmPanel.add(BorderLayout.NORTH, new JLabel("Right Arm"));
			leftHandPanel.add(BorderLayout.NORTH, new JLabel("Left Hand"));
			leftArmPanel.add(BorderLayout.NORTH, new JLabel("Left Arm"));
			headPanel.add(BorderLayout.NORTH, new JLabel("Head"));
			torsoPanel.add(BorderLayout.NORTH, new JLabel("Torso"));
			// panels for MOVE controls
			final JPanel rightHandMovePanel = new JPanel();
			rightHandMovePanel.setLayout(new BoxLayout(rightHandMovePanel, BoxLayout.Y_AXIS));
			final JPanel rightArmMovePanel = new JPanel();
			rightArmMovePanel.setLayout(new BoxLayout(rightArmMovePanel, BoxLayout.Y_AXIS));
			final JPanel leftHandMovePanel = new JPanel();
			leftHandMovePanel.setLayout(new BoxLayout(leftHandMovePanel, BoxLayout.Y_AXIS));
			final JPanel leftArmMovePanel = new JPanel();
			leftArmMovePanel.setLayout(new BoxLayout(leftArmMovePanel, BoxLayout.Y_AXIS));
			final JPanel headMovePanel = new JPanel();
			headMovePanel.setLayout(new BoxLayout(headMovePanel, BoxLayout.Y_AXIS));
			final JPanel torsoMovePanel = new JPanel();
			torsoMovePanel.setLayout(new BoxLayout(torsoMovePanel, BoxLayout.Y_AXIS));
			// add to map
			Map<RobotSection, JPanel> robotSectionMovePanels = new HashMap<RobotSection, JPanel>();
			robotSectionMovePanels.put(RobotSection.RIGHT_HAND, rightHandMovePanel);
			robotSectionMovePanels.put(RobotSection.RIGHT_ARM, rightArmMovePanel);
			robotSectionMovePanels.put(RobotSection.LEFT_HAND, leftHandMovePanel);
			robotSectionMovePanels.put(RobotSection.LEFT_ARM, leftArmMovePanel);
			robotSectionMovePanels.put(RobotSection.HEAD, headMovePanel);
			robotSectionMovePanels.put(RobotSection.TORSO, torsoMovePanel);
			// add panels for sliders
			final JPanel rightHandSlidersPanel = new JPanel();
			rightHandSlidersPanel.setLayout(new BoxLayout(rightHandSlidersPanel, BoxLayout.X_AXIS));
			final JPanel rightArmSlidersPanel = new JPanel();
			rightArmSlidersPanel.setLayout(new BoxLayout(rightArmSlidersPanel, BoxLayout.X_AXIS));
			final JPanel leftHandSlidersPanel = new JPanel();
			leftHandSlidersPanel.setLayout(new BoxLayout(leftHandSlidersPanel, BoxLayout.X_AXIS));
			final JPanel leftArmSlidersPanel = new JPanel();
			leftArmSlidersPanel.setLayout(new BoxLayout(leftArmSlidersPanel, BoxLayout.X_AXIS));
			final JPanel headSlidersPanel = new JPanel();
			headSlidersPanel.setLayout(new BoxLayout(headSlidersPanel, BoxLayout.X_AXIS));
			final JPanel torsoSlidersPanel = new JPanel();
			torsoSlidersPanel.setLayout(new BoxLayout(torsoSlidersPanel, BoxLayout.X_AXIS));
			// add to map
			Map<RobotSection, JPanel> robotSectionSlidersPanels = new HashMap<RobotSection, JPanel>();
			robotSectionSlidersPanels.put(RobotSection.RIGHT_HAND, rightHandSlidersPanel);
			robotSectionSlidersPanels.put(RobotSection.RIGHT_ARM, rightArmSlidersPanel);
			robotSectionSlidersPanels.put(RobotSection.LEFT_HAND, leftHandSlidersPanel);
			robotSectionSlidersPanels.put(RobotSection.LEFT_ARM, leftArmSlidersPanel);
			robotSectionSlidersPanels.put(RobotSection.HEAD, headSlidersPanel);
			robotSectionSlidersPanels.put(RobotSection.TORSO, torsoSlidersPanel);
			// panels for SPEED controls
			final JPanel rightHandSpeedPanel = new JPanel();
			rightHandSpeedPanel.setLayout(new BoxLayout(rightHandSpeedPanel, BoxLayout.Y_AXIS));
			final JPanel rightArmSpeedPanel = new JPanel();
			rightArmSpeedPanel.setLayout(new BoxLayout(rightArmSpeedPanel, BoxLayout.Y_AXIS));
			final JPanel leftHandSpeedPanel = new JPanel();
			leftHandSpeedPanel.setLayout(new BoxLayout(leftHandSpeedPanel, BoxLayout.Y_AXIS));
			final JPanel leftArmSpeedPanel = new JPanel();
			leftArmSpeedPanel.setLayout(new BoxLayout(leftArmSpeedPanel, BoxLayout.Y_AXIS));
			final JPanel headSpeedPanel = new JPanel();
			headSpeedPanel.setLayout(new BoxLayout(headSpeedPanel, BoxLayout.Y_AXIS));
			final JPanel torsoSpeedPanel = new JPanel();
			headSpeedPanel.setLayout(new BoxLayout(headSpeedPanel, BoxLayout.Y_AXIS));
			// add to map
			Map<RobotSection, JPanel> robotSectionSpeedPanels = new HashMap<RobotSection, JPanel>();
			robotSectionSpeedPanels.put(RobotSection.RIGHT_HAND, rightHandSpeedPanel);
			robotSectionSpeedPanels.put(RobotSection.RIGHT_ARM, rightArmSpeedPanel);
			robotSectionSpeedPanels.put(RobotSection.LEFT_HAND, leftHandSpeedPanel);
			robotSectionSpeedPanels.put(RobotSection.LEFT_ARM, leftArmSpeedPanel);
			robotSectionSpeedPanels.put(RobotSection.HEAD, headSpeedPanel);
			robotSectionSpeedPanels.put(RobotSection.TORSO, torsoSpeedPanel);
			// panels for SPEED text boxes
			final JPanel rightHandSpeedNumberBoxesPanel = new JPanel();
			rightHandSpeedNumberBoxesPanel.setLayout(new BoxLayout(rightHandSpeedNumberBoxesPanel, BoxLayout.X_AXIS));
			final JPanel rightArmSpeedNumberBoxesPanel = new JPanel();
			rightArmSpeedNumberBoxesPanel.setLayout(new BoxLayout(rightArmSpeedNumberBoxesPanel, BoxLayout.X_AXIS));
			final JPanel leftHandSpeedNumberBoxesPanel = new JPanel();
			leftHandSpeedNumberBoxesPanel.setLayout(new BoxLayout(leftHandSpeedNumberBoxesPanel, BoxLayout.X_AXIS));
			final JPanel leftArmSpeedNumberBoxesPanel = new JPanel();
			leftArmSpeedNumberBoxesPanel.setLayout(new BoxLayout(leftArmSpeedNumberBoxesPanel, BoxLayout.X_AXIS));
			final JPanel headSpeedNumberBoxesPanel = new JPanel();
			headSpeedNumberBoxesPanel.setLayout(new BoxLayout(headSpeedNumberBoxesPanel, BoxLayout.X_AXIS));
			final JPanel torsoSpeedNumberBoxesPanel = new JPanel();
			torsoSpeedNumberBoxesPanel.setLayout(new BoxLayout(torsoSpeedNumberBoxesPanel, BoxLayout.X_AXIS));
			// add to map
			Map<RobotSection, JPanel> robotSectionSpeedNumberBoxesPanels = new HashMap<RobotSection, JPanel>();
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.RIGHT_HAND, rightHandSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.RIGHT_ARM, rightArmSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.LEFT_HAND, leftHandSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.LEFT_ARM, leftArmSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.HEAD, headSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.TORSO, torsoSpeedNumberBoxesPanel);
			// add elements and listeners, and make panel hierarchy
			for (RobotSection robotSection : RobotSection.values()) {
				LOGGER.info("robotSection: \"" + robotSection + "\"");
				// adding MOVE elements
				JPanel robotSectionMovePanel = robotSectionMovePanels.get(robotSection);
				addEnableCheckBoxesToSectionPane(robotSectionMovePanel, frame, "Move?", robotSection, true);
				robotSectionMovePanel.add(robotSectionSlidersPanels.get(robotSection));
				// adding SPEED elements
				JPanel robotSectionSpeedPanel = robotSectionSpeedPanels.get(robotSection);
				addEnableCheckBoxesToSectionPane(robotSectionSpeedPanel, frame, "Set Speed?", robotSection, false);
				addSpeedTextToSectionPane(robotSectionSpeedPanel, frame, robotSection);
				robotSectionSpeedPanel.add(robotSectionSpeedNumberBoxesPanels.get(robotSection));
			}
//			for (Map.Entry<RobotSection, JPanel> robotSectionPanel : robotSectionMovePanels.entrySet()) {
//				addEnableCheckBoxesToSectionPane(robotSectionPanel.getValue(), frame, "Move?",
//						robotSectionPanel.getKey(), true);
//			}
//			for (Map.Entry<RobotSection, JPanel> robotSectionSpeedPanel : robotSectionSpeedPanels.entrySet()) {
//				addEnableCheckBoxesToSectionPane(robotSectionSpeedPanel.getValue(), frame, "Set Speed?",
//						robotSectionSpeedPanel.getKey(), false);
//			}
			//
//			rightHandSpeedPanel.add(rightHandSpeedNumberBoxesPanel);
//			rightArmSpeedPanel.add(rightArmSpeedNumberBoxesPanel);
//			leftHandSpeedPanel.add(leftHandSpeedNumberBoxesPanel);
//			leftArmSpeedPanel.add(leftArmSpeedNumberBoxesPanel);
//			headSpeedPanel.add(headSpeedNumberBoxesPanel);
//			torsoSpeedPanel.add(torsoSpeedNumberBoxesPanel);
			// ENABLE / DISABLE logic
			if (frame.getFrameType() == Frame.FrameType.MOVE) {
				// disable SPEED panels
				setPanelEnabled(rightHandSpeedPanel, false);
				setPanelEnabled(rightArmSpeedPanel, false);
				setPanelEnabled(leftHandSpeedPanel, false);
				setPanelEnabled(leftArmSpeedPanel, false);
				setPanelEnabled(headSpeedPanel, false);
				setPanelEnabled(torsoSpeedPanel, false);
				// enable MOVE panels
				setPanelEnabled(rightHandMovePanel, true);
				setPanelEnabled(rightArmMovePanel, true);
				setPanelEnabled(leftHandMovePanel, true);
				setPanelEnabled(leftArmMovePanel, true);
				setPanelEnabled(headMovePanel, true);
				setPanelEnabled(torsoMovePanel, true);
			} else if (frame.getFrameType() == Frame.FrameType.SPEED) {
				// enable SPEED panels
				setPanelEnabled(rightHandSpeedPanel, true);
				setPanelEnabled(rightArmSpeedPanel, true);
				setPanelEnabled(leftHandSpeedPanel, true);
				setPanelEnabled(leftArmSpeedPanel, true);
				setPanelEnabled(headSpeedPanel, true);
				setPanelEnabled(torsoSpeedPanel, true);
				// disable MOVE panels
				setPanelEnabled(rightHandMovePanel, false);
				setPanelEnabled(rightArmMovePanel, false);
				setPanelEnabled(leftHandMovePanel, false);
				setPanelEnabled(leftArmMovePanel, false);
				setPanelEnabled(headMovePanel, false);
				setPanelEnabled(torsoMovePanel, false);
			} else {
				// disable SPEED panels
				setPanelEnabled(rightHandSpeedPanel, false);
				setPanelEnabled(rightArmSpeedPanel, false);
				setPanelEnabled(leftHandSpeedPanel, false);
				setPanelEnabled(leftArmSpeedPanel, false);
				setPanelEnabled(headSpeedPanel, false);
				setPanelEnabled(torsoSpeedPanel, false);
				// disable MOVE panels
				setPanelEnabled(rightHandMovePanel, false);
				setPanelEnabled(rightArmMovePanel, false);
				setPanelEnabled(leftHandMovePanel, false);
				setPanelEnabled(leftArmMovePanel, false);
				setPanelEnabled(headMovePanel, false);
				setPanelEnabled(torsoMovePanel, false);
			}

			
			
			JTabbedPane bottomTabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);

			// JPanels for the JTabbedPane
			final JPanel mainpanel = new JPanel();
			final JPanel c1panel = new JPanel();
			final JPanel c2panel = new JPanel();
			final JPanel c3panel = new JPanel();

			// mainpanel (enabling / disabling sections)
			mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
			boolean[] tabs_main_checkbox_states = new boolean[6];
			for (int i = 0; i < 6; i++) {
				String name = "";
				if (i == 0) {
					name = "Right Hand";
				} else if (i == 1) {
					name = "Right Arm";
				} else if (i == 2) {
					name = "Left Hand";
				} else if (i == 3) {
					name = "Left Arm";
				} else if (i == 4) {
					name = "Head";
				} else if (i == 5) {
					name = "Torso";
				}

				final int fi = i;

				final JCheckBox checkbox = new JCheckBox(name);
				checkbox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent arg0) {
						tabs_main_checkbox_states[fi] = checkbox.isSelected();
						// myService.send(boundServiceName, "tabs_main_checkbox_states_changed",
						// tabs_main_checkbox_states);
						tabs_main_checkbox_states_changed(tabs_main_checkbox_states);
					}

				});
				checkbox.setSelected(true);
				mainpanel.add(checkbox);
			}

			Container c1con = c1panel;
			Container c2con = c2panel;
			Container c3con = c3panel;
			// seting the layout of panels
			GridBagLayout c1gbl = new GridBagLayout();
			c1con.setLayout(c1gbl);
			GridBagLayout c2gbl = new GridBagLayout();
			c2con.setLayout(c2gbl);
			GridBagLayout c3gbl = new GridBagLayout();
			c3con.setLayout(c3gbl);

			// predefined min- / res- / max- positions
			int[][][] minresmaxpos = {
					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 60, 180 }, { 0, 90, 180 }, { 0, 90, 180 } } };

			// c1-, c2-, c3-panel
			for (int i1 = 0; i1 < 6; i1++) {

				Container con = null;
				GridBagLayout gbl = null;

				if (i1 == 0 || i1 == 1) {
					con = c1con;
					gbl = c1gbl;
				} else if (i1 == 2 || i1 == 3) {
					con = c2con;
					gbl = c2gbl;
				} else if (i1 == 4 || i1 == 5) {
					con = c3con;
					gbl = c3gbl;
				}

				int size = 0;

				if (i1 == 0 || i1 == 2) {
					size = 6;
				} else if (i1 == 1 || i1 == 3) {
					size = 4;
				} else if (i1 == 4) {
					size = 5;
				} else if (i1 == 5) {
					size = 3;
				}

				int offset = 0;
				if (i1 == 1 || i1 == 3) {
					offset = 6;
				} else if (i1 == 5) {
					offset = 5;
				}

				ServoItemHolder[] sih1 = new ServoItemHolder[size];
				int value = 0;

				for (int i2 = 0; i2 < size; i2++) {
					ServoItemHolder sih11 = new ServoItemHolder();

					String servoname = "";

					if (i1 == 0 || i1 == 2) {
						if (i2 == 0) {
							if (i1 == 0) {
								value = frame.getRightThumbFingerMove();
							} else {
								value = frame.getLeftThumbFingerMove();
							}
							servoname = "thumb";
						} else if (i2 == 1) {
							if (i1 == 0) {
								value = frame.getRightIndexFingerMove();
							} else {
								value = frame.getLeftIndexFingerMove();
							}
							servoname = "index";
						} else if (i2 == 2) {
							if (i1 == 0) {
								value = frame.getRightMajeureFingerMove();
							} else {
								value = frame.getLeftMajeureFingerMove();
							}
							servoname = "majeure";
						} else if (i2 == 3) {
							if (i1 == 0) {
								value = frame.getRightRingFingerMove();
							} else {
								value = frame.getLeftRingFingerMove();
							}
							servoname = "ringfinger";
						} else if (i2 == 4) {
							if (i1 == 0) {
								value = frame.getRightPinkyFingerMove();
							} else {
								value = frame.getLeftPinkyFingerMove();
							}
							servoname = "pinky";
						} else if (i2 == 5) {
							if (i1 == 0) {
								value = frame.getRightWristMove();
							} else {
								value = frame.getLeftWristMove();
							}
							servoname = "wrist";
						}
					} else if (i1 == 1 || i1 == 3) {
						if (i2 == 0) {
							servoname = "bicep";
						} else if (i2 == 1) {
							servoname = "rotate";
						} else if (i2 == 2) {
							servoname = "shoulder";
						} else if (i2 == 3) {
							servoname = "omoplate";
						}
					} else if (i1 == 4) {
						if (i2 == 0) {
							servoname = "neck";
						} else if (i2 == 1) {
							servoname = "rothead";
						} else if (i2 == 2) {
							servoname = "eyeX";
						} else if (i2 == 3) {
							servoname = "eyeY";
						} else if (i2 == 4) {
							servoname = "jaw";
						}
					} else if (i1 == 5) {
						if (i2 == 0) {
							servoname = "topStom";
						} else if (i2 == 1) {
							servoname = "midStom";
						} else if (i2 == 2) {
							servoname = "lowStom";
						}
					}

					sih11.fin = new JLabel(servoname);
					sih11.min = new JLabel(minresmaxpos[i1][i2][0] + "");
					sih11.res = new JLabel(minresmaxpos[i1][i2][1] + "");
					sih11.max = new JLabel(minresmaxpos[i1][i2][2] + "");
					sih11.sli = new JSlider();
					// customizeslider(sih11.sli, i1, i2, minresmaxpos[i1][i2]);
					sliderSetUp(sih11.sli, i1, i2, minresmaxpos[i1][i2], value);
					sih11.akt = new JLabel(sih11.sli.getValue() + "");
					sih11.spe = new JTextField("1.00");

					// x y w h wx wy
					gridbaglayout_addComponent(con, gbl, sih11.fin, offset + i2, 0, 1, 1, 1.0, 1.0);
					gridbaglayout_addComponent(con, gbl, sih11.min, offset + i2, 1, 1, 1, 1.0, 1.0);
					gridbaglayout_addComponent(con, gbl, sih11.res, offset + i2, 2, 1, 1, 1.0, 1.0);
					gridbaglayout_addComponent(con, gbl, sih11.max, offset + i2, 3, 1, 1, 1.0, 1.0);
					gridbaglayout_addComponent(con, gbl, sih11.sli, offset + i2, 4, 1, 1, 1.0, 1.0);
					gridbaglayout_addComponent(con, gbl, sih11.akt, offset + i2, 5, 1, 1, 1.0, 1.0);
					gridbaglayout_addComponent(con, gbl, sih11.spe, offset + i2, 6, 1, 1, 1.0, 1.0);

					sih1[i2] = sih11;
				}
				// myService.send(boundServiceName, "servoitemholder_set_sih1", i1, sih1);
				servoitemholder_set_sih1(i1, sih1);
			}

			bottomTabs.addTab("Main", mainpanel);
			bottomTabs.addTab("Right Side", c1panel);
			bottomTabs.addTab("Left Side", c2panel);
			bottomTabs.addTab("Head + Torso", c3panel);
			//
			// layouts
			rightHandPanel.add(BorderLayout.CENTER, rightHandMovePanel);
			rightHandPanel.add(BorderLayout.SOUTH, rightHandSpeedPanel);
			rightArmPanel.add(BorderLayout.CENTER, rightArmMovePanel);
			rightArmPanel.add(BorderLayout.SOUTH, rightArmSpeedPanel);
			leftHandPanel.add(BorderLayout.CENTER, leftHandMovePanel);
			leftHandPanel.add(BorderLayout.SOUTH, leftHandSpeedPanel);
			leftArmPanel.add(BorderLayout.CENTER, leftArmMovePanel);
			leftArmPanel.add(BorderLayout.SOUTH, leftArmSpeedPanel);
			headPanel.add(BorderLayout.CENTER, headMovePanel);
			headPanel.add(BorderLayout.SOUTH, headSpeedPanel);
			torsoPanel.add(BorderLayout.CENTER, torsoMovePanel);
			torsoPanel.add(BorderLayout.SOUTH, torsoSpeedPanel);
			//
			bottom.removeAll();
			bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
//			bottom.add(BorderLayout.CENTER, bottomTabs);
			bottom.add(rightHandPanel);
			bottom.add(rightArmPanel);
			bottom.add(leftHandPanel);
			bottom.add(leftArmPanel);
			bottom.add(headPanel);
			bottom.add(torsoPanel);
			bottom.revalidate();
			bottom.repaint();

			LOGGER.trace("initializeBottomPaneTabs [END]");
		} catch (Exception e) {
			LOGGER.warn("Creating bottom tabbed frame", e);
		}
	}

	private void gridbaglayout_addComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int width,
			int height, double weightx, double weighty) {
		// function for easier gridbaglayout's
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}

	private void customizeslider(JSlider slider, final int t1, final int t2, int[] minresmaxpos11) {
		// preset the slider
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setMinimum(minresmaxpos11[0]);
		slider.setMaximum(minresmaxpos11[2]);
		slider.setMajorTickSpacing(20);
		slider.setMinorTickSpacing(1);
		slider.createStandardLabels(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setValue((minresmaxpos11[0] + minresmaxpos11[2]) / 2);

		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent ce) {
//				swingGui.send(boundServiceName, "servoitemholder_slider_changed", t1, t2);
				servoitemholder_slider_changed(t1, t2);
			}
		});
	}

	private void sliderSetUp(JSlider slider, final int t1, final int t2, int[] minresmaxpos11, int value) {
		// preset the slider
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setMinimum(minresmaxpos11[0]);
		slider.setMaximum(minresmaxpos11[2]);
		slider.setMajorTickSpacing(20);
		slider.setMinorTickSpacing(1);
		slider.createStandardLabels(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setValue(value);

		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent ce) {
//				swingGui.send(boundServiceName, "servoitemholder_slider_changed", t1, t2);
				servoitemholder_slider_changed(t1, t2);
			}
		});
	}
}
