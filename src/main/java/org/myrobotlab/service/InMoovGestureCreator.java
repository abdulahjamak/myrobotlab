package org.myrobotlab.service;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
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
import org.myrobotlab.service.model.FrameItemHolder;
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

	public final static Logger LOGGER = LoggerFactory.getLogger(InMoovGestureCreator.class);

	transient ServoItemHolder[][] servoitemholder;

	transient ArrayList<FrameItemHolder> frames;
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
    frames = new ArrayList<FrameItemHolder>();
    pythonitemholder = new ArrayList<PythonItemHolder>();
  }


  public void control_addgest(JList control_list, JTextField control_gestname, JTextField control_funcname) {
    // Add the current gesture to the script (button bottom-left)
    String defname = ime_funkcije = control_funcname.getText();
    String gestname = ime_gest = control_gestname.getText();

    String code = "";
    for (FrameItemHolder fih : frames) {
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
          code11 = "    " + pythonname + ".moveHead(" + fih.getNeck() + "," + fih.getRothead() + "," + fih.getEyeX() + "," + fih.getEyeY() + "," + fih.getJaw() + ")\n";
        }
        if (fih.getRightArmMoveSet()) {
          code12 = "    " + pythonname + ".moveArm(\"left\"," + fih.getLbicep() + "," + fih.getLrotate() + "," + fih.getLshoulder() + "," + fih.getLomoplate() + ")\n";
        }
        if (fih.getLeftHandMoveSet()) {
          code13 = "    " + pythonname + ".moveArm(\"right\"," + fih.getRbicep() + "," + fih.getRrotate() + "," + fih.getRshoulder() + "," + fih.getRomoplate() + ")\n";
        }
        if (fih.getLeftArmMoveSet()) {
          code14 = "    " + pythonname + ".moveHand(\"left\"," + fih.getLthumb() + "," + fih.getLindex() + "," + fih.getLmajeure() + "," + fih.getLringfinger() + "," + fih.getLpinky() + "," + fih.getLwrist()
              + ")\n";
        }
        if (fih.getHeadMoveSet()) {
          code15 = "    " + pythonname + ".moveHand(\"right\"," + fih.getRthumb() + "," + fih.getRindex() + "," + fih.getRmajeure() + "," + fih.getRringfinger() + "," + fih.getRpinky() + "," + fih.getRwrist()
              + ")\n";
        }
        if (fih.getTorsoMoveSet()) {
          code16 = "    " + pythonname + ".moveTorso(" + fih.getTopStom() + "," + fih.getMidStom() + "," + fih.getLowStom() + ")\n";
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
          code11 = "    " + pythonname + ".setHeadSpeed(" + fih.getNeckspeed() + "," + fih.getRotheadspeed() + "," + fih.getEyeXspeed() + "," + fih.getEyeYspeed() + "," + fih.getJawspeed() + ")\n";
        }
        if (fih.getRightArmMoveSet()) {
          code12 = "    " + pythonname + ".setArmSpeed(\"left\"," + fih.getLbicepspeed() + "," + fih.getLrotatespeed() + "," + fih.getLshoulderspeed() + "," + fih.getLomoplatespeed() + ")\n";
        }
        if (fih.getLeftHandMoveSet()) {
          code13 = "    " + pythonname + ".setArmSpeed(\"right\"," + fih.getRbicepspeed() + "," + fih.getRrotatespeed() + "," + fih.getRshoulderspeed() + "," + fih.getRomoplatespeed() + ")\n";
        }
        if (fih.getLeftArmMoveSet()) {
          code14 = "    " + pythonname + ".setHandSpeed(\"left\"," + fih.getLthumbspeed() + "," + fih.getLindexspeed() + "," + fih.getLmajeurespeed() + "," + fih.getLringfingerspeed() + ","
              + fih.getLpinkyspeed() + "," + fih.getLwristspeed() + ")\n";
        }
        if (fih.getHeadMoveSet()) {
          code15 = "    " + pythonname + ".setHandSpeed(\"right\"," + fih.getRthumbspeed() + "," + fih.getRindexspeed() + "," + fih.getRmajeurespeed() + "," + fih.getRringfingerspeed() + ","
              + fih.getRpinkyspeed() + "," + fih.getRwristspeed() + ")\n";
        }
        if (fih.getTorsoMoveSet()) {
          code16 = "    " + pythonname + ".setTorsoSpeed(" + fih.getTopStomspeed() + "," + fih.getMidStomspeed() + "," + fih.getLowStomspeed() + ")\n";
        }
        code1 = code11 + code12 + code13 + code14 + code15 + code16;
      }
      code = code + code1;
    }
    String finalcode = "def " + defname + "():\n" + code;

    String insert = "ear.addCommand(\"" + gestname + "\", \"python\", \"" + defname + "\")";
    int posear = pythonscript.lastIndexOf("ear.addCommand");
    int pos = pythonscript.indexOf("\n", posear);
    pythonscript = pythonscript.substring(0, pos) + "\n" + insert + pythonscript.substring(pos, pythonscript.length());

    pythonscript = pythonscript + "\n" + finalcode;

    parsescript(control_list);
  }

  public void control_connect(JButton control_connect) {
    // Connect / Disconnect to / from the InMoov service (button
    // bottom-left)
    if (control_connect.getText().equals("Connect")) {
      if (referencename == null) {
        referencename = "i01";
      }
      i01 = (InMoov) Runtime.getService(referencename);
      control_connect.setText("Disconnect");
    } else {
      i01 = null;
      control_connect.setText("Connect");
    }
  }

  public void control_loadgest(JList control_list, JList framelist, JTextField control_gestname, JTextField control_funcname){
    // Load the current gesture from the script (button bottom-left)
    int posl = control_list.getSelectedIndex();

    if (posl != -1) {
      if (pythonitemholder.get(posl).modifyable) {
        frames.clear();

        String defname = null;

        String code = pythonitemholder.get(posl).code;
        String[] codesplit = code.split("\n");
        FrameItemHolder fih = null;
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
            fih = new FrameItemHolder(FrameItemHolder.FrameType.SLEEP);
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
                  fih.setNeck(Integer.parseInt(goodsplit[0]));
                  fih.setRothead(Integer.parseInt(goodsplit[1]));
                  if (goodsplit.length > 2) {
                    fih.setEyeX(Integer.parseInt(goodsplit[2]));
                    fih.setEyeY(Integer.parseInt(goodsplit[3]));
                    fih.setJaw(Integer.parseInt(goodsplit[4]));
                  } else {
                    fih.setEyeX(90);
                    fih.setEyeY(90);
                    fih.setJaw(90);
                  }
                  head = true;
                  pos++;
                } else if (line2.startsWith(pythonname + ".moveHand")) {
                  String gs = goodsplit[0];
                  String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
                  if (side.equals("right")) {
                    fih.setRthumb(Integer.parseInt(goodsplit[1]));
                    fih.setRindex(Integer.parseInt(goodsplit[2]));
                    fih.setRmajeure(Integer.parseInt(goodsplit[3]));
                    fih.setRringfinger(Integer.parseInt(goodsplit[4]));
                    fih.setRpinky(Integer.parseInt(goodsplit[5]));
                    if (goodsplit.length > 6) {
                      fih.setRwrist(Integer.parseInt(goodsplit[6]));
                    } else {
                      fih.setRwrist(90);
                    }
                    rhand = true;
                    pos++;
                  } else if (side.equals("left")) {
                    fih.setLthumb(Integer.parseInt(goodsplit[1]));
                    fih.setLindex(Integer.parseInt(goodsplit[2]));
                    fih.setLmajeure(Integer.parseInt(goodsplit[3]));
                    fih.setLringfinger(Integer.parseInt(goodsplit[4]));
                    fih.setLpinky(Integer.parseInt(goodsplit[5]));
                    if (goodsplit.length > 6) {
                      fih.setLwrist(Integer.parseInt(goodsplit[6]));
                    } else {
                      fih.setLwrist(90);
                    }
                    lhand = true;
                    pos++;
                  }
                } else if (line2.startsWith(pythonname + ".moveArm")) {
                  String gs = goodsplit[0];
                  String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
                  if (side.equals("right")) {
                    fih.setRbicep(Integer.parseInt(goodsplit[1]));
                    fih.setRrotate(Integer.parseInt(goodsplit[2]));
                    fih.setRshoulder(Integer.parseInt(goodsplit[3]));
                    fih.setRomoplate(Integer.parseInt(goodsplit[4]));
                    rarm = true;
                    pos++;
                  } else if (side.equals("left")) {
                    fih.setLbicep(Integer.parseInt(goodsplit[1]));
                    fih.setLrotate(Integer.parseInt(goodsplit[2]));
                    fih.setLshoulder(Integer.parseInt(goodsplit[3]));
                    fih.setLomoplate(Integer.parseInt(goodsplit[4]));
                    larm = true;
                    pos++;
                  }
                } else if (line2.startsWith(pythonname + ".moveTorso")) {
                  fih.setTopStom(Integer.parseInt(goodsplit[0]));
                  fih.setMidStom(Integer.parseInt(goodsplit[1]));
                  fih.setLowStom(Integer.parseInt(goodsplit[2]));
                  torso = true;
                  pos++;
                }
              } else if (line2.startsWith(pythonname + ".set")) {
                isspeed = true;
                String good = line2.substring(line2.indexOf("(") + 1, line2.lastIndexOf(")"));
                String[] goodsplit = good.split(",");
                if (line2.startsWith(pythonname + ".setHeadSpeed")) {
                  fih.setNeckspeed(Float.parseFloat(goodsplit[0]));
                  fih.setRotheadspeed(Float.parseFloat(goodsplit[1]));
                  if (goodsplit.length > 2) {
                    fih.setEyeXspeed(Float.parseFloat(goodsplit[2]));
                    fih.setEyeYspeed(Float.parseFloat(goodsplit[3]));
                    fih.setJawspeed(Float.parseFloat(goodsplit[4]));
                  } else {
                    fih.setEyeXspeed(1.0f);
                    fih.setEyeYspeed(1.0f);
                    fih.setJawspeed(1.0f);
                  }
                  head = true;
                  pos++;
                } else if (line2.startsWith(pythonname + ".setHandSpeed")) {
                  String gs = goodsplit[0];
                  String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
                  if (side.equals("right")) {
                    fih.setRthumbspeed(Float.parseFloat(goodsplit[1]));
                    fih.setRindexspeed(Float.parseFloat(goodsplit[2]));
                    fih.setRmajeurespeed(Float.parseFloat(goodsplit[3]));
                    fih.setRringfingerspeed(Float.parseFloat(goodsplit[4]));
                    fih.setRpinkyspeed(Float.parseFloat(goodsplit[5]));
                    if (goodsplit.length > 6) {
                      fih.setRwristspeed(Float.parseFloat(goodsplit[6]));
                    } else {
                      fih.setRwristspeed(1.0f);
                    }
                    rhand = true;
                    pos++;
                  } else if (side.equals("left")) {
                    fih.setLthumbspeed(Float.parseFloat(goodsplit[1]));
                    fih.setLindexspeed(Float.parseFloat(goodsplit[2]));
                    fih.setLmajeurespeed(Float.parseFloat(goodsplit[3]));
                    fih.setLringfingerspeed(Float.parseFloat(goodsplit[4]));
                    fih.setLpinkyspeed(Float.parseFloat(goodsplit[5]));
                    if (goodsplit.length > 6) {
                      fih.setLwristspeed(Float.parseFloat(goodsplit[6]));
                    } else {
                      fih.setLwristspeed(1.0f);
                    }
                    lhand = true;
                    pos++;
                  }
                } else if (line2.startsWith(pythonname + ".setArmSpeed")) {
                  String gs = goodsplit[0];
                  String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
                  if (side.equals("right")) {
                    fih.setRbicepspeed(Float.parseFloat(goodsplit[1]));
                    fih.setRrotatespeed(Float.parseFloat(goodsplit[2]));
                    fih.setRshoulderspeed(Float.parseFloat(goodsplit[3]));
                    fih.setRomoplatespeed(Float.parseFloat(goodsplit[4]));
                    rarm = true;
                    pos++;
                  } else if (side.equals("left")) {
                    fih.setLbicepspeed(Float.parseFloat(goodsplit[1]));
                    fih.setLrotatespeed(Float.parseFloat(goodsplit[2]));
                    fih.setLshoulderspeed(Float.parseFloat(goodsplit[3]));
                    fih.setLomoplatespeed(Float.parseFloat(goodsplit[4]));
                    larm = true;
                    pos++;
                  }
                } else if (line2.startsWith(pythonname + ".setTorsoSpeed")) {
                  fih.setTopStomspeed(Float.parseFloat(goodsplit[0]));
                  fih.setMidStomspeed(Float.parseFloat(goodsplit[1]));
                  fih.setLowStomspeed(Float.parseFloat(goodsplit[2]));
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
                fih.setNeck(Integer.parseInt(goodsplit[0]));
                fih.setRothead(Integer.parseInt(goodsplit[1]));
                if (goodsplit.length > 2) {
                  fih.setEyeX(Integer.parseInt(goodsplit[2]));
                  fih.setEyeY(Integer.parseInt(goodsplit[3]));
                  fih.setJaw(Integer.parseInt(goodsplit[4]));
                } else {
                  fih.setEyeX(90);
                  fih.setEyeY(90);
                  fih.setJaw(90);
                }
                head = true;
                pos++;
              } else if (line2.startsWith(pythonname + ".moveHand")) {
                String gs = goodsplit[0];
                String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
                if (side.equals("right")) {
                  fih.setRthumb(Integer.parseInt(goodsplit[1]));
                  fih.setRindex(Integer.parseInt(goodsplit[2]));
                  fih.setRmajeure(Integer.parseInt(goodsplit[3]));
                  fih.setRringfinger(Integer.parseInt(goodsplit[4]));
                  fih.setRpinky(Integer.parseInt(goodsplit[5]));
                  if (goodsplit.length > 6) {
                    fih.setRwrist(Integer.parseInt(goodsplit[6]));
                  } else {
                    fih.setRwrist(90);
                  }
                  rhand = true;
                  pos++;
                } else if (side.equals("left")) {
                  fih.setLthumb(Integer.parseInt(goodsplit[1]));
                  fih.setLindex(Integer.parseInt(goodsplit[2]));
                  fih.setLmajeure(Integer.parseInt(goodsplit[3]));
                  fih.setLringfinger(Integer.parseInt(goodsplit[4]));
                  fih.setLpinky(Integer.parseInt(goodsplit[5]));
                  if (goodsplit.length > 6) {
                    fih.setLwrist(Integer.parseInt(goodsplit[6]));
                  } else {
                    fih.setLwrist(90);
                  }
                  lhand = true;
                  pos++;
                }
              } else if (line2.startsWith(pythonname + ".moveArm")) {
                String gs = goodsplit[0];
                String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
                if (side.equals("right")) {
                  fih.setRbicep(Integer.parseInt(goodsplit[1]));
                  fih.setRrotate(Integer.parseInt(goodsplit[2]));
                  fih.setRshoulder(Integer.parseInt(goodsplit[3]));
                  fih.setRomoplate(Integer.parseInt(goodsplit[4]));
                  rarm = true;
                  pos++;
                } else if (side.equals("left")) {
                  fih.setLbicep(Integer.parseInt(goodsplit[1]));
                  fih.setLrotate(Integer.parseInt(goodsplit[2]));
                  fih.setLshoulder(Integer.parseInt(goodsplit[3]));
                  fih.setLomoplate(Integer.parseInt(goodsplit[4]));
                  larm = true;
                  pos++;
                }
              } else if (line2.startsWith(pythonname + ".moveTorso")) {
                fih.setTopStom(Integer.parseInt(goodsplit[0]));
                fih.setMidStom(Integer.parseInt(goodsplit[1]));
                fih.setLowStom(Integer.parseInt(goodsplit[2]));
                torso = true;
                pos++;
              }
            } else {
              if (!head) {
                fih.setNeck(90);
                fih.setRothead(90);
                fih.setEyeX(90);
                fih.setEyeY(90);
                fih.setJaw(90);
              }
              if (!rhand) {
                fih.setRthumb(90);
                fih.setRindex(90);
                fih.setRmajeure(90);
                fih.setRringfinger(90);
                fih.setRpinky(90);
                fih.setRwrist(90);
              }
              if (!lhand) {
                fih.setLthumb(90);
                fih.setLindex(90);
                fih.setLmajeure(90);
                fih.setLringfinger(90);
                fih.setLpinky(90);
                fih.setLwrist(90);
              }
              if (!rarm) {
                fih.setRbicep(90);
                fih.setRrotate(90);
                fih.setRshoulder(90);
                fih.setRomoplate(90);
              }
              if (!larm) {
                fih.setLbicep(90);
                fih.setLrotate(90);
                fih.setLshoulder(90);
                fih.setLomoplate(90);
              }
              if (!torso) {
                fih.setTopStom(90);
                fih.setMidStom(90);
                fih.setLowStom(90);
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
                fih.setNeckspeed(Float.parseFloat(goodsplit[0]));
                fih.setRotheadspeed(Float.parseFloat(goodsplit[1]));
                if (goodsplit.length > 2) {
                  fih.setEyeXspeed(Float.parseFloat(goodsplit[2]));
                  fih.setEyeYspeed(Float.parseFloat(goodsplit[3]));
                  fih.setJawspeed(Float.parseFloat(goodsplit[4]));
                } else {
                  fih.setEyeXspeed(1.0f);
                  fih.setEyeYspeed(1.0f);
                  fih.setJawspeed(1.0f);
                }
                head = true;
                pos++;
              } else if (line2.startsWith(pythonname + ".setHandSpeed")) {
                String gs = goodsplit[0];
                String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
                if (side.equals("right")) {
                  fih.setRthumbspeed(Float.parseFloat(goodsplit[1]));
                  fih.setRindexspeed(Float.parseFloat(goodsplit[2]));
                  fih.setRmajeurespeed(Float.parseFloat(goodsplit[3]));
                  fih.setRringfingerspeed(Float.parseFloat(goodsplit[4]));
                  fih.setRpinkyspeed(Float.parseFloat(goodsplit[5]));
                  if (goodsplit.length > 6) {
                    fih.setRwristspeed(Float.parseFloat(goodsplit[6]));
                  } else {
                    fih.setRwristspeed(1.0f);
                  }
                  rhand = true;
                  pos++;
                } else if (side.equals("left")) {
                  fih.setLthumbspeed(Float.parseFloat(goodsplit[1]));
                  fih.setLindexspeed(Float.parseFloat(goodsplit[2]));
                  fih.setLmajeurespeed(Float.parseFloat(goodsplit[3]));
                  fih.setLringfingerspeed(Float.parseFloat(goodsplit[4]));
                  fih.setLpinkyspeed(Float.parseFloat(goodsplit[5]));
                  if (goodsplit.length > 6) {
                    fih.setLwristspeed(Float.parseFloat(goodsplit[6]));
                  } else {
                    fih.setLwristspeed(1.0f);
                  }
                  lhand = true;
                  pos++;
                }
              } else if (line2.startsWith(pythonname + ".setArmSpeed")) {
                String gs = goodsplit[0];
                String side = gs.substring(gs.indexOf("\"") + 1, gs.lastIndexOf("\""));
                if (side.equals("right")) {
                  fih.setRbicepspeed(Float.parseFloat(goodsplit[1]));
                  fih.setRrotatespeed(Float.parseFloat(goodsplit[2]));
                  fih.setRshoulderspeed(Float.parseFloat(goodsplit[3]));
                  fih.setRomoplatespeed(Float.parseFloat(goodsplit[4]));
                  rarm = true;
                  pos++;
                } else if (side.equals("left")) {
                  fih.setLbicepspeed(Float.parseFloat(goodsplit[1]));
                  fih.setLrotatespeed(Float.parseFloat(goodsplit[2]));
                  fih.setLshoulderspeed(Float.parseFloat(goodsplit[3]));
                  fih.setLomoplatespeed(Float.parseFloat(goodsplit[4]));
                  larm = true;
                  pos++;
                }
              } else if (line2.startsWith(pythonname + ".setTorsoSpeed")) {
                fih.setTopStomspeed(Float.parseFloat(goodsplit[0]));
                fih.setMidStomspeed(Float.parseFloat(goodsplit[1]));
                fih.setLowStomspeed(Float.parseFloat(goodsplit[2]));
                torso = true;
                pos++;
              }
            } else {
              if (!head) {
                fih.setNeckspeed(1.0f);
                fih.setRotheadspeed(1.0f);
                fih.setEyeXspeed(1.0f);
                fih.setEyeYspeed(1.0f);
                fih.setJawspeed(1.0f);
              }
              if (!rhand) {
                fih.setRthumbspeed(1.0f);
                fih.setRindexspeed(1.0f);
                fih.setRmajeurespeed(1.0f);
                fih.setRringfingerspeed(1.0f);
                fih.setRpinkyspeed(1.0f);
                fih.setRwristspeed(1.0f);
              }
              if (!lhand) {
                fih.setLthumbspeed(1.0f);
                fih.setLindexspeed(1.0f);
                fih.setLmajeurespeed(1.0f);
                fih.setLringfingerspeed(1.0f);
                fih.setLpinkyspeed(1.0f);
                fih.setLwristspeed(1.0f);
              }
              if (!rarm) {
                fih.setRbicepspeed(1.0f);
                fih.setRrotatespeed(1.0f);
                fih.setRshoulderspeed(1.0f);
                fih.setRomoplatespeed(1.0f);
              }
              if (!larm) {
                fih.setLbicepspeed(1.0f);
                fih.setLrotatespeed(1.0f);
                fih.setLshoulderspeed(1.0f);
                fih.setLomoplatespeed(1.0f);
              }
              if (!torso) {
                fih.setTopStomspeed(1.0f);
                fih.setMidStomspeed(1.0f);
                fih.setLowStomspeed(1.0f);
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
          String funcname = funcnamedirty.substring(funcnamedirty.indexOf("\"") + 1, funcnamedirty.lastIndexOf("\""));

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

        pythonscript = pythonscript.substring(0, posscript - 4) + pythonscript.substring(posscriptnextdef - 1, pythonscript.length());

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
		/*"/home/abe/ws-fx/inmoov/InMoov/gestures/abetova.py"*/
		fileWriter = new FileWriter("/home/abe/ws-fx/inmoov/InMoov/gestures/" + parsirani_kod.substring(4,parsirani_kod.indexOf('(')) + "_abetovo" + ".py");
	    fileWriter.write(parsirani_kod);
	    fileWriter.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
  }

  public void control_testgest() {
    // test (execute) the created gesture (button bottom-left)
    if (i01 != null) {
      for (FrameItemHolder fih : frames) {
        if (fih.getSleep() != -1) {
          sleep(fih.getSleep());
        } else if (fih.getSpeech() != null) {
          try {
            i01.mouth.speakBlocking(fih.getSpeech());
          } catch (Exception e) {
            Logging.logError(e);
          }
        } else if (fih.getName() != null) {
          if (fih.getRightHandMoveSet()) {
            i01.moveHead(fih.getNeck(), fih.getRothead(), fih.getEyeX(), fih.getEyeY(), fih.getJaw());
          }
          if (fih.getRightArmMoveSet()) {
            i01.moveArm("left", fih.getLbicep(), fih.getLrotate(), fih.getLshoulder(), fih.getLomoplate());
          }
          if (fih.getLeftHandMoveSet()) {
            i01.moveArm("right", fih.getRbicep(), fih.getRrotate(), fih.getRshoulder(), fih.getRomoplate());
          }
          if (fih.getLeftArmMoveSet()) {
            i01.moveHand("left", fih.getLthumb(), fih.getLindex(), fih.getLmajeure(), fih.getLringfinger(), fih.getLpinky(), (double) fih.getLwrist());
          }
          if (fih.getHeadMoveSet()) {
            i01.moveHand("right", fih.getRthumb(), fih.getRindex(), fih.getRmajeure(), fih.getRringfinger(), fih.getRpinky(), (double) fih.getRwrist());
          }
          if (fih.getTorsoMoveSet()) {
            i01.moveTorso(fih.getTopStom(), fih.getMidStom(), fih.getLowStom());
          }
        } else {
          if (fih.getRightHandMoveSet()) {
            i01.setHeadSpeed(fih.getNeckspeed(), fih.getRotheadspeed(), fih.getEyeXspeed(), fih.getEyeYspeed(), fih.getJawspeed());
          }
          if (fih.getRightArmMoveSet()) {
            i01.setArmSpeed("left", fih.getLbicepspeed(), fih.getLrotatespeed(), fih.getLshoulderspeed(), fih.getLomoplatespeed());
          }
          if (fih.getLeftHandMoveSet()) {
            i01.setArmSpeed("right", fih.getRbicepspeed(), fih.getRrotatespeed(), fih.getRshoulderspeed(), fih.getRomoplatespeed());
          }
          if (fih.getLeftArmMoveSet()) {
            i01.setHandSpeed("left", fih.getLthumbspeed(), fih.getLindexspeed(), fih.getLmajeurespeed(), fih.getLringfingerspeed(), fih.getLpinkyspeed(), fih.getLwristspeed());
          }
          if (fih.getHeadMoveSet()) {
            i01.setHandSpeed("right", fih.getRthumbspeed(), fih.getRindexspeed(), fih.getRmajeurespeed(), fih.getRringfingerspeed(), fih.getRpinkyspeed(), fih.getRwristspeed());
          }
          if (fih.getTorsoMoveSet()) {
            i01.setTorsoSpeed(fih.getTopStomspeed(), fih.getMidStomspeed(), fih.getLowStomspeed());
          }
        }
      }
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
        for (FrameItemHolder fih : frames) {
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
              code11 = "    " + pythonname + ".moveHead(" + fih.getNeck() + "," + fih.getRothead() + "," + fih.getEyeX() + "," + fih.getEyeY() + "," + fih.getJaw() + ")\n";
            }
            if (fih.getRightArmMoveSet()) {
              code12 = "    " + pythonname + ".moveArm(\"left\"," + fih.getLbicep() + "," + fih.getLrotate() + "," + fih.getLshoulder() + "," + fih.getLomoplate() + ")\n";
            }
            if (fih.getLeftHandMoveSet()) {
              code13 = "    " + pythonname + ".moveArm(\"right\"," + fih.getRbicep() + "," + fih.getRrotate() + "," + fih.getRshoulder() + "," + fih.getRomoplate() + ")\n";
            }
            if (fih.getLeftArmMoveSet()) {
              code14 = "    " + pythonname + ".moveHand(\"left\"," + fih.getLthumb() + "," + fih.getLindex() + "," + fih.getLmajeure() + "," + fih.getLringfinger() + "," + fih.getLpinky() + ","
                  + fih.getLwrist() + ")\n";
            }
            if (fih.getHeadMoveSet()) {
              code15 = "    " + pythonname + ".moveHand(\"right\"," + fih.getRthumb() + "," + fih.getRindex() + "," + fih.getRmajeure() + "," + fih.getRringfinger() + "," + fih.getRpinky() + ","
                  + fih.getRwrist() + ")\n";
            }
            if (fih.getTorsoMoveSet()) {
              code16 = "    " + pythonname + ".moveTorso(" + fih.getTopStom() + "," + fih.getMidStom() + "," + fih.getLowStom() + ")\n";
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
              code11 = "    " + pythonname + ".setHeadSpeed(" + fih.getNeckspeed() + "," + fih.getRotheadspeed() + "," + fih.getEyeXspeed() + "," + fih.getEyeYspeed() + "," + fih.getJawspeed() + ")\n";
            }
            if (fih.getRightArmMoveSet()) {
              code12 = "    " + pythonname + ".setArmSpeed(\"left\"," + fih.getLbicepspeed() + "," + fih.getLrotatespeed() + "," + fih.getLshoulderspeed() + "," + fih.getLomoplatespeed() + ")\n";
            }

            if (fih.getLeftHandMoveSet()) {
              code13 = "    " + pythonname + ".setArmSpeed(\"right\"," + fih.getRbicepspeed() + "," + fih.getRrotatespeed() + "," + fih.getRshoulderspeed() + "," + fih.getRomoplatespeed() + ")\n";
            }
            if (fih.getLeftArmMoveSet()) {
              code14 = "    " + pythonname + ".setHandSpeed(\"left\"," + fih.getLthumbspeed() + "," + fih.getLindexspeed() + "," + fih.getLmajeurespeed() + "," + fih.getLringfingerspeed() + ","
                  + fih.getLpinkyspeed() + "," + fih.getLwristspeed() + ")\n";
            }
            if (fih.getHeadMoveSet()) {
              code15 = "    " + pythonname + ".setHandSpeed(\"right\"," + fih.getRthumbspeed() + "," + fih.getRindexspeed() + "," + fih.getRmajeurespeed() + "," + fih.getRringfingerspeed() + ","
                  + fih.getRpinkyspeed() + "," + fih.getRwristspeed() + ")\n";
            }
            if (fih.getTorsoMoveSet()) {
              code16 = "    " + pythonname + ".setTorsoSpeed(" + fih.getTopStomspeed() + "," + fih.getMidStomspeed() + "," + fih.getLowStomspeed() + ")\n";
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
        pythonscript = pythonscript.substring(0, pos1) + "\n" + insert + pythonscript.substring(pos2, pythonscript.length());

        int posscript = pythonscript.lastIndexOf(defnameold);
        int posscriptnextdef = pythonscript.indexOf("def", posscript);
        if (posscriptnextdef == -1) {
          posscriptnextdef = pythonscript.length();
        }

        pythonscript = pythonscript.substring(0, posscript - 4) + "\n" + finalcode + pythonscript.substring(posscriptnextdef - 1, pythonscript.length());

        parsescript(control_list);
      }
    }
  }

  public void frame_addsleep(JList framelist, JTextField frame_addsleep_textfield) {
    // Add a sleep frame to the framelist (button bottom-right)
    FrameItemHolder fih = new FrameItemHolder(FrameItemHolder.FrameType.SLEEP);

    fih.setSleep(Integer.parseInt(frame_addsleep_textfield.getText()));
    fih.setSpeech(null);
    fih.setName(null);

    frames.add(fih);

    framelistact(framelist);
  }

  public void frame_addspeech(JList framelist, JTextField frame_addspeech_textfield) {
    // Add a speech frame to the framelist (button bottom-right)
    FrameItemHolder fih = new FrameItemHolder(FrameItemHolder.FrameType.SPEECH);

    fih.setSleep(-1);
    fih.setSpeech(frame_addspeech_textfield.getText());
    fih.setName(null);

    frames.add(fih);

    framelistact(framelist);
  }

  public void frame_addspeed(JList framelist) {
    // Add a speed setting frame to the framelist (button bottom-right)
    FrameItemHolder fih = new FrameItemHolder(FrameItemHolder.FrameType.SPEED);

    fih.setRthumbspeed(Float.parseFloat(servoitemholder[0][0].spe.getText()));
    fih.setRindexspeed(Float.parseFloat(servoitemholder[0][1].spe.getText()));
    fih.setRmajeurespeed(Float.parseFloat(servoitemholder[0][2].spe.getText()));
    fih.setRringfingerspeed(Float.parseFloat(servoitemholder[0][3].spe.getText()));
    fih.setRpinkyspeed(Float.parseFloat(servoitemholder[0][4].spe.getText()));
    fih.setRwristspeed(Float.parseFloat(servoitemholder[0][5].spe.getText()));

    fih.setRbicepspeed(Float.parseFloat(servoitemholder[1][0].spe.getText()));
    fih.setRrotatespeed(Float.parseFloat(servoitemholder[1][1].spe.getText()));
    fih.setRshoulderspeed(Float.parseFloat(servoitemholder[1][2].spe.getText()));
    fih.setRomoplatespeed(Float.parseFloat(servoitemholder[1][3].spe.getText()));

    fih.setLthumbspeed(Float.parseFloat(servoitemholder[2][0].spe.getText()));
    fih.setLindexspeed(Float.parseFloat(servoitemholder[2][1].spe.getText()));
    fih.setLmajeurespeed(Float.parseFloat(servoitemholder[2][2].spe.getText()));
    fih.setLringfingerspeed(Float.parseFloat(servoitemholder[2][3].spe.getText()));
    fih.setLpinkyspeed(Float.parseFloat(servoitemholder[2][4].spe.getText()));
    fih.setLwristspeed(Float.parseFloat(servoitemholder[2][5].spe.getText()));

    fih.setLbicepspeed(Float.parseFloat(servoitemholder[3][0].spe.getText()));
    fih.setLrotatespeed(Float.parseFloat(servoitemholder[3][1].spe.getText()));
    fih.setLshoulderspeed(Float.parseFloat(servoitemholder[3][2].spe.getText()));
    fih.setLomoplatespeed(Float.parseFloat(servoitemholder[3][3].spe.getText()));

    fih.setNeckspeed(Float.parseFloat(servoitemholder[4][0].spe.getText()));
    fih.setRotheadspeed(Float.parseFloat(servoitemholder[4][1].spe.getText()));
    fih.setEyeXspeed(Float.parseFloat(servoitemholder[4][2].spe.getText()));
    fih.setEyeYspeed(Float.parseFloat(servoitemholder[4][3].spe.getText()));
    fih.setJawspeed(Float.parseFloat(servoitemholder[4][4].spe.getText()));

    fih.setTopStomspeed(Float.parseFloat(servoitemholder[5][0].spe.getText()));
    fih.setMidStomspeed(Float.parseFloat(servoitemholder[5][1].spe.getText()));
    fih.setLowStomspeed(Float.parseFloat(servoitemholder[5][2].spe.getText()));

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
      FrameItemHolder fih = frames.get(pos);
      frames.add(fih);

      framelistact(framelist);
    }
  }

  public void frame_down(JList framelist) {
    // Move this frame one down on the framelist (button bottom-right)
    int pos = framelist.getSelectedIndex();

    if (pos != -1) {
      FrameItemHolder fih = frames.remove(pos);
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

  public void frame_load(JList framelist, JTextField frame_add_textfield, JTextField frame_addsleep_textfield, JTextField frame_addspeech_textfield) {
    // Load this frame from the framelist (button bottom-right)
    int pos = framelist.getSelectedIndex();

    if (pos != -1) {

      // sleep || speech || servo movement || speed setting
      if (frames.get(pos).getSleep() != -1) {
        frame_addsleep_textfield.setText(frames.get(pos).getSleep() + "");
      } else if (frames.get(pos).getSpeech() != null) {
        frame_addspeech_textfield.setText(frames.get(pos).getSpeech());
      } else if (frames.get(pos).getName() != null) {
        servoitemholder[0][0].sli.setValue(frames.get(pos).getRthumb());
        servoitemholder[0][1].sli.setValue(frames.get(pos).getRindex());
        servoitemholder[0][2].sli.setValue(frames.get(pos).getRmajeure());
        servoitemholder[0][3].sli.setValue(frames.get(pos).getRringfinger());
        servoitemholder[0][4].sli.setValue(frames.get(pos).getRpinky());
        servoitemholder[0][5].sli.setValue(frames.get(pos).getRwrist());

        servoitemholder[1][0].sli.setValue(frames.get(pos).getRbicep());
        servoitemholder[1][1].sli.setValue(frames.get(pos).getRrotate());
        servoitemholder[1][2].sli.setValue(frames.get(pos).getRshoulder());
        servoitemholder[1][3].sli.setValue(frames.get(pos).getRomoplate());

        servoitemholder[2][0].sli.setValue(frames.get(pos).getLthumb());
        servoitemholder[2][1].sli.setValue(frames.get(pos).getLindex());
        servoitemholder[2][2].sli.setValue(frames.get(pos).getLmajeure());
        servoitemholder[2][3].sli.setValue(frames.get(pos).getLringfinger());
        servoitemholder[2][4].sli.setValue(frames.get(pos).getLpinky());
        servoitemholder[2][5].sli.setValue(frames.get(pos).getLwrist());

        servoitemholder[3][0].sli.setValue(frames.get(pos).getLbicep());
        servoitemholder[3][1].sli.setValue(frames.get(pos).getLrotate());
        servoitemholder[3][2].sli.setValue(frames.get(pos).getLshoulder());
        servoitemholder[3][3].sli.setValue(frames.get(pos).getLomoplate());

        servoitemholder[4][0].sli.setValue(frames.get(pos).getNeck());
        servoitemholder[4][1].sli.setValue(frames.get(pos).getRothead());
        servoitemholder[4][2].sli.setValue(frames.get(pos).getEyeX());
        servoitemholder[4][3].sli.setValue(frames.get(pos).getEyeY());
        servoitemholder[4][4].sli.setValue(frames.get(pos).getJaw());

        servoitemholder[5][0].sli.setValue(frames.get(pos).getTopStom());
        servoitemholder[5][1].sli.setValue(frames.get(pos).getMidStom());
        servoitemholder[5][2].sli.setValue(frames.get(pos).getLowStom());
        frame_add_textfield.setText(frames.get(pos).getName());
      } else {
        servoitemholder[0][0].spe.setText(frames.get(pos).getRthumbspeed() + "");
        servoitemholder[0][1].spe.setText(frames.get(pos).getRindexspeed() + "");
        servoitemholder[0][2].spe.setText(frames.get(pos).getRmajeurespeed() + "");
        servoitemholder[0][3].spe.setText(frames.get(pos).getRringfingerspeed() + "");
        servoitemholder[0][4].spe.setText(frames.get(pos).getRpinkyspeed() + "");
        servoitemholder[0][5].spe.setText(frames.get(pos).getRwristspeed() + "");

        servoitemholder[1][0].spe.setText(frames.get(pos).getRbicepspeed() + "");
        servoitemholder[1][1].spe.setText(frames.get(pos).getRrotatespeed() + "");
        servoitemholder[1][2].spe.setText(frames.get(pos).getRshoulderspeed() + "");
        servoitemholder[1][3].spe.setText(frames.get(pos).getRomoplatespeed() + "");

        servoitemholder[2][0].spe.setText(frames.get(pos).getLthumbspeed() + "");
        servoitemholder[2][1].spe.setText(frames.get(pos).getLindexspeed() + "");
        servoitemholder[2][2].spe.setText(frames.get(pos).getLmajeurespeed() + "");
        servoitemholder[2][3].spe.setText(frames.get(pos).getLringfingerspeed() + "");
        servoitemholder[2][4].spe.setText(frames.get(pos).getLpinkyspeed() + "");
        servoitemholder[2][5].spe.setText(frames.get(pos).getLwristspeed() + "");

        servoitemholder[3][0].spe.setText(frames.get(pos).getLbicepspeed() + "");
        servoitemholder[3][1].spe.setText(frames.get(pos).getLrotatespeed() + "");
        servoitemholder[3][2].spe.setText(frames.get(pos).getLshoulderspeed() + "");
        servoitemholder[3][3].spe.setText(frames.get(pos).getLomoplatespeed() + "");

        servoitemholder[4][0].spe.setText(frames.get(pos).getNeckspeed() + "");
        servoitemholder[4][1].spe.setText(frames.get(pos).getRotheadspeed() + "");
        servoitemholder[4][2].spe.setText(frames.get(pos).getEyeXspeed() + "");
        servoitemholder[4][3].spe.setText(frames.get(pos).getEyeYspeed() + "");
        servoitemholder[4][4].spe.setText(frames.get(pos).getJawspeed() + "");

        servoitemholder[5][0].spe.setText(frames.get(pos).getTopStomspeed() + "");
        servoitemholder[5][1].spe.setText(frames.get(pos).getMidStomspeed() + "");
        servoitemholder[5][2].spe.setText(frames.get(pos).getLowStomspeed() + "");
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

 /* public void frame_test(JList framelist) {
    // Test this frame (execute)
    int pos = framelist.getSelectedIndex();
    if (i01 != null && pos != -1) {
      FrameItemHolder fih = frameitemholder.get(pos);
      
      // sleep || speech || servo movement || speed setting
      if (fih.getSleep() != -1) {
        sleep(fih.getSleep());
      } else if (fih.getSpeech() != null) {
        try {
          i01.mouth.speakBlocking(fih.getSpeech());
        } catch (Exception e) {
          Logging.logError(e);
        }
      } else if (fih.getName() != null) {
        if (fih.getTabsMainCheckboxStates()[0]) {
          i01.moveHead(fih.getNeck(), fih.getRothead(), fih.getEyeX(), fih.getEyeY(), fih.getJaw());
        }
        if (fih.getTabsMainCheckboxStates()[1]) {
          i01.moveArm("left", fih.getLbicep(), fih.getLrotate(), fih.getLshoulder(), fih.getLomoplate());
        }
        if (fih.getTabsMainCheckboxStates()[2]) {
          i01.moveArm("right", fih.getRbicep(), fih.getRrotate(), fih.getRshoulder(), fih.getRomoplate());
        }
        if (fih.getTabsMainCheckboxStates()[3]) {
          i01.moveHand("left", fih.getLthumb(), fih.getLindex(), fih.getLmajeure(), fih.getLringfinger(), fih.getLpinky(), (double) fih.getLwrist());
        }
        if (fih.getTabsMainCheckboxStates()[4]) {
          i01.moveHand("right", fih.getRthumb(), fih.getRindex(), fih.getRmajeure(), fih.getRringfinger(), fih.getRpinky(), (double) fih.getRwrist());
        }
        if (fih.getTabsMainCheckboxStates()[5]) {
          i01.moveTorso(fih.getTopStom(), fih.getMidStom(), fih.getLowStom());
        }
      } else {
        if (fih.getTabsMainCheckboxStates()[0]) {
          i01.setHeadSpeed(fih.getNeckspeed(), fih.getRotheadspeed(), fih.getEyeXspeed(), fih.getEyeYspeed(), fih.getJawspeed());
        }
        if (fih.getTabsMainCheckboxStates()[1]) {
          i01.setArmSpeed("left", fih.getLbicepspeed(), fih.getLrotatespeed(), fih.getLshoulderspeed(), fih.getLomoplatespeed());
        }
        if (fih.getTabsMainCheckboxStates()[2]) {
          i01.setArmSpeed("right", fih.getRbicepspeed(), fih.getRrotatespeed(), fih.getRshoulderspeed(), fih.getRomoplatespeed());
        }
        if (fih.getTabsMainCheckboxStates()[3]) {
          i01.setHandSpeed("left", fih.getLthumbspeed(), fih.getLindexspeed(), fih.getLmajeurespeed(), fih.getLringfingerspeed(), fih.getLpinkyspeed(), fih.getLwristspeed());
        }
        if (fih.getTabsMainCheckboxStates()[4]) {
          i01.setHandSpeed("right", fih.getRthumbspeed(), fih.getRindexspeed(), fih.getRmajeurespeed(), fih.getRringfingerspeed(), fih.getRpinkyspeed(), fih.getRwristspeed());
        }
        if (fih.getTabsMainCheckboxStates()[5]) {
          i01.setTorsoSpeed(fih.getTopStomspeed(), fih.getMidStomspeed(), fih.getLowStomspeed());
        }
      }
    }
  }
*/
  public void frame_test(JList framelist) {
	    // Test this frame (execute)
	    int pos = framelist.getSelectedIndex();
	    LOGGER.info("indeks je: " + framelist.getSelectedIndex() + "a i01 = "  + (i01 == null ? "null" : "nije_ null"));
	    if (i01 != null && pos != -1) {
		    for (int i = 0; i < frames.size(); i++) {
		  	  FrameItemHolder fih = frames.get(i);
		    //  FrameItemHolder fih = frameitemholder.get(pos);
		      LOGGER.info("Trenutno se testira: " + fih.getName() + "\n");
		      // sleep || speech || servo movement || speed setting
		      if (fih.getSleep() != -1) {
		        sleep(fih.getSleep());
		      } else if (fih.getSpeech() != null) {
		        try {
		          i01.mouth.speakBlocking(fih.getSpeech());
		        } catch (Exception e) {
		          Logging.logError(e);
		        }
		      } else if (fih.getName() != null) {
		        if (fih.getRightHandMoveSet()) {
		          i01.moveHead(fih.getNeck(), fih.getRothead(), fih.getEyeX(), fih.getEyeY(), fih.getJaw());
		        }
		        if (fih.getRightArmMoveSet()) {
		          i01.moveArm("left", fih.getLbicep(), fih.getLrotate(), fih.getLshoulder(), fih.getLomoplate());
		        }
		        if (fih.getLeftHandMoveSet()) {
		          i01.moveArm("right", fih.getRbicep(), fih.getRrotate(), fih.getRshoulder(), fih.getRomoplate());
		        }
		        if (fih.getLeftArmMoveSet()) {
		          i01.moveHand("left", fih.getLthumb(), fih.getLindex(), fih.getLmajeure(), fih.getLringfinger(), fih.getLpinky(), (double) fih.getLwrist());
		        }
		        if (fih.getHeadMoveSet()) {
		          i01.moveHand("right", fih.getRthumb(), fih.getRindex(), fih.getRmajeure(), fih.getRringfinger(), fih.getRpinky(), (double) fih.getRwrist());
		        }
		        if (fih.getTorsoMoveSet()) {
		          i01.moveTorso(fih.getTopStom(), fih.getMidStom(), fih.getLowStom());
		        }
		      } else {
		        if (fih.getRightHandMoveSet()) {
		          i01.setHeadSpeed(fih.getNeckspeed(), fih.getRotheadspeed(), fih.getEyeXspeed(), fih.getEyeYspeed(), fih.getJawspeed());
		        }
		        if (fih.getRightArmMoveSet()) {
		          i01.setArmSpeed("left", fih.getLbicepspeed(), fih.getLrotatespeed(), fih.getLshoulderspeed(), fih.getLomoplatespeed());
		        }
		        if (fih.getLeftHandMoveSet()) {
		          i01.setArmSpeed("right", fih.getRbicepspeed(), fih.getRrotatespeed(), fih.getRshoulderspeed(), fih.getRomoplatespeed());
		        }
		        if (fih.getLeftArmMoveSet()) {
		          i01.setHandSpeed("left", fih.getLthumbspeed(), fih.getLindexspeed(), fih.getLmajeurespeed(), fih.getLringfingerspeed(), fih.getLpinkyspeed(), fih.getLwristspeed());
		        }
		        if (fih.getHeadMoveSet()) {
		          i01.setHandSpeed("right", fih.getRthumbspeed(), fih.getRindexspeed(), fih.getRmajeurespeed(), fih.getRringfingerspeed(), fih.getRpinkyspeed(), fih.getRwristspeed());
		        }
		        if (fih.getTorsoMoveSet()) {
		          i01.setTorsoSpeed(fih.getTopStomspeed(), fih.getMidStomspeed(), fih.getLowStomspeed());
		        }
		      }
		    }
	    }
	  } 
  public void frame_up(JList framelist) {
    // Move this frame one up on the framelist (button bottom-right)
    int pos = framelist.getSelectedIndex();

    if (pos != -1) {
      FrameItemHolder fih = frames.remove(pos);
      frames.add(pos - 1, fih);

      framelistact(framelist);
    }
  }

  public void frame_update(JList framelist, JTextField frame_add_textfield, JTextField frame_addsleep_textfield, JTextField frame_addspeech_textfield) {
    // Update this frame on the framelist (button bottom-right)

    int pos = framelist.getSelectedIndex();

    if (pos != -1) {
      FrameItemHolder fih = new FrameItemHolder(FrameItemHolder.FrameType.SLEEP);

      // sleep || speech || servo movement || speed setting
      if (frames.get(pos).getSleep() != -1) {
          fih.setFrameType(FrameItemHolder.FrameType.SLEEP);
        fih.setSleep(Integer.parseInt(frame_addsleep_textfield.getText()));
        fih.setSpeech(null);
        fih.setName(null);
      } else if (frames.get(pos).getSpeech() != null) {
          fih.setFrameType(FrameItemHolder.FrameType.SPEECH);
        fih.setSleep(-1);
        fih.setSpeech(frame_addspeech_textfield.getText());
        fih.setName(null);
      } else if (frames.get(pos).getName() != null) {
        fih.setFrameType(FrameItemHolder.FrameType.MOVE);
        fih.setRthumb(servoitemholder[0][0].sli.getValue());
        fih.setRindex(servoitemholder[0][1].sli.getValue());
        fih.setRmajeure(servoitemholder[0][2].sli.getValue());
        fih.setRringfinger(servoitemholder[0][3].sli.getValue());
        fih.setRpinky(servoitemholder[0][4].sli.getValue());
        fih.setRwrist(servoitemholder[0][5].sli.getValue());

        fih.setRbicep(servoitemholder[1][0].sli.getValue());
        fih.setRrotate(servoitemholder[1][1].sli.getValue());
        fih.setRshoulder(servoitemholder[1][2].sli.getValue());
        fih.setRomoplate(servoitemholder[1][3].sli.getValue());

        fih.setLthumb(servoitemholder[2][0].sli.getValue());
        fih.setLindex(servoitemholder[2][1].sli.getValue());
        fih.setLmajeure(servoitemholder[2][2].sli.getValue());
        fih.setLringfinger(servoitemholder[2][3].sli.getValue());
        fih.setLpinky(servoitemholder[2][4].sli.getValue());
        fih.setLwrist(servoitemholder[2][5].sli.getValue());

        fih.setLbicep(servoitemholder[3][0].sli.getValue());
        fih.setLrotate(servoitemholder[3][1].sli.getValue());
        fih.setLshoulder(servoitemholder[3][2].sli.getValue());
        fih.setLomoplate(servoitemholder[3][3].sli.getValue());

        fih.setNeck(servoitemholder[4][0].sli.getValue());
        fih.setRothead(servoitemholder[4][1].sli.getValue());
        fih.setEyeX(servoitemholder[4][2].sli.getValue());
        fih.setEyeY(servoitemholder[4][3].sli.getValue());
        fih.setJaw(servoitemholder[4][4].sli.getValue());

        fih.setTopStom(servoitemholder[5][0].sli.getValue());
        fih.setMidStom(servoitemholder[5][1].sli.getValue());
        fih.setLowStom(servoitemholder[5][2].sli.getValue());

        fih.setSleep(-1);
        fih.setSpeech(null);
        fih.setName(frame_add_textfield.getText());
      } else {
        fih.setFrameType(FrameItemHolder.FrameType.SPEED);
        fih.setRthumbspeed(Float.parseFloat(servoitemholder[0][0].spe.getText()));
        fih.setRindexspeed(Float.parseFloat(servoitemholder[0][1].spe.getText()));
        fih.setRmajeurespeed(Float.parseFloat(servoitemholder[0][2].spe.getText()));
        fih.setRringfingerspeed(Float.parseFloat(servoitemholder[0][3].spe.getText()));
        fih.setRpinkyspeed(Float.parseFloat(servoitemholder[0][4].spe.getText()));
        fih.setRwristspeed(Float.parseFloat(servoitemholder[0][5].spe.getText()));

        fih.setRbicepspeed(Float.parseFloat(servoitemholder[1][0].spe.getText()));
        fih.setRrotatespeed(Float.parseFloat(servoitemholder[1][1].spe.getText()));
        fih.setRshoulderspeed(Float.parseFloat(servoitemholder[1][2].spe.getText()));
        fih.setRomoplatespeed(Float.parseFloat(servoitemholder[1][3].spe.getText()));

        fih.setLthumbspeed(Float.parseFloat(servoitemholder[2][0].spe.getText()));
        fih.setLindexspeed(Float.parseFloat(servoitemholder[2][1].spe.getText()));
        fih.setLmajeurespeed(Float.parseFloat(servoitemholder[2][2].spe.getText()));
        fih.setLringfingerspeed(Float.parseFloat(servoitemholder[2][3].spe.getText()));
        fih.setLpinkyspeed(Float.parseFloat(servoitemholder[2][4].spe.getText()));
        fih.setLwristspeed(Float.parseFloat(servoitemholder[2][5].spe.getText()));

        fih.setLbicepspeed(Float.parseFloat(servoitemholder[3][0].spe.getText()));
        fih.setLrotatespeed(Float.parseFloat(servoitemholder[3][1].spe.getText()));
        fih.setLshoulderspeed(Float.parseFloat(servoitemholder[3][2].spe.getText()));
        fih.setLomoplatespeed(Float.parseFloat(servoitemholder[3][3].spe.getText()));

        fih.setNeckspeed(Float.parseFloat(servoitemholder[4][0].spe.getText()));
        fih.setRotheadspeed(Float.parseFloat(servoitemholder[4][1].spe.getText()));
        fih.setEyeXspeed(Float.parseFloat(servoitemholder[4][2].spe.getText()));
        fih.setEyeYspeed(Float.parseFloat(servoitemholder[4][3].spe.getText()));
        fih.setJawspeed(Float.parseFloat(servoitemholder[4][4].spe.getText()));

        fih.setTopStomspeed(Float.parseFloat(servoitemholder[5][0].spe.getText()));
        fih.setMidStomspeed(Float.parseFloat(servoitemholder[5][1].spe.getText()));
        fih.setLowStomspeed(Float.parseFloat(servoitemholder[5][2].spe.getText()));

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
      FrameItemHolder fih = frames.get(i);

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
          displaytext1 = fih.getRthumb() + " " + fih.getRindex() + " " + fih.getRmajeure() + " " + fih.getRringfinger() + " " + fih.getRpinky() + " " + fih.getRwrist();
        }
        if (fih.getRightArmMoveSet()) {
          displaytext2 = fih.getRbicep() + " " + fih.getRrotate() + " " + fih.getRshoulder() + " " + fih.getRomoplate();
        }
        if (fih.getLeftHandMoveSet()) {
          displaytext3 = fih.getLthumb() + " " + fih.getLindex() + " " + fih.getLmajeure() + " " + fih.getLringfinger() + " " + fih.getLpinky() + " " + fih.getLwrist();
        }
        if (fih.getLeftArmMoveSet()) {
          displaytext4 = fih.getLbicep() + " " + fih.getLrotate() + " " + fih.getLshoulder() + " " + fih.getLomoplate();
        }
        if (fih.getHeadMoveSet()) {
          displaytext5 = fih.getNeck() + " " + fih.getRothead() + " " + fih.getEyeX() + " " + fih.getEyeY() + " " + fih.getJaw();
        }
        if (fih.getTorsoMoveSet()) {
          displaytext6 = fih.getTopStom() + " " + fih.getMidStom() + " " + fih.getLowStom();
        }
        displaytext = fih.getName() + ": " + displaytext1 + " | " + displaytext2 + " | " + displaytext3 + " | " + displaytext4 + " | " + displaytext5 + " | " + displaytext6;
      } else {
        String displaytext1 = "";
        String displaytext2 = "";
        String displaytext3 = "";
        String displaytext4 = "";
        String displaytext5 = "";
        String displaytext6 = "";
        if (fih.getRightHandMoveSet()) {
          displaytext1 = fih.getRthumbspeed() + " " + fih.getRindexspeed() + " " + fih.getRmajeurespeed() + " " + fih.getRringfingerspeed() + " " + fih.getRpinkyspeed() + " " + fih.getRwristspeed();
        }
        if (fih.getRightArmMoveSet()) {
          displaytext2 = fih.getRbicepspeed() + " " + fih.getRrotatespeed() + " " + fih.getRshoulderspeed() + " " + fih.getRomoplatespeed();
        }
        if (fih.getLeftHandMoveSet()) {
          displaytext3 = fih.getLthumbspeed() + " " + fih.getLindexspeed() + " " + fih.getLmajeurespeed() + " " + fih.getLringfingerspeed() + " " + fih.getLpinkyspeed() + " " + fih.getLwristspeed();
        }
        if (fih.getLeftArmMoveSet()) {
          displaytext4 = fih.getLbicepspeed() + " " + fih.getLrotatespeed() + " " + fih.getLshoulderspeed() + " " + fih.getLomoplatespeed();
        }
        if (fih.getHeadMoveSet()) {
          displaytext5 = fih.getNeckspeed() + " " + fih.getRotheadspeed() + " " + fih.getEyeXspeed() + " " + fih.getEyeYspeed() + " " + fih.getJawspeed();
        }
        if (fih.getTorsoMoveSet()) {
          displaytext6 = fih.getTopStomspeed() + " " + fih.getMidStomspeed() + " " + fih.getLowStomspeed();
        }
        displaytext = "SPEED   " + displaytext1 + " | " + displaytext2 + " | " + displaytext3 + " | " + displaytext4 + " | " + displaytext5 + " | " + displaytext6;
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
				if(pythonFileNames != null && pythonFileNames.size() > 0) {
					control_list.setListData(pythonFileNames.toArray());
				}
			}
		}
	}

	public void control_loadscri(JList control_list, JList framelist) {
		List<String> scriptLines = new ArrayList<String>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			File selectedFile = pythonFiles.get(control_list.getSelectedIndex());
			fileReader = new FileReader(selectedFile);
//			fileReader = new FileReader("/home/abe/ws-fx/inmoov/InMoov/gestures/" + control_list.getSelectedValue().toString());
//			fileReader = new FileReader("/d:/balance.py");
			bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				scriptLines.add(line);
			}
			bufferedReader.close();
		} catch (Exception e) {
			LOGGER.warn("Exception occurred trying to read /home/abe/balance.py", e);
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
			List<FrameItemHolder> fihList = parseScriptToFrame(scriptLines);
			if (fihList != null) {
				// loading parsed frames into GUI list
				LOGGER.trace("Existing FRAME count \"" + fihList.size() + "\"");
				// reload GUI now
				frames.clear();
				frames.addAll(fihList);
				controlListReload(framelist, fihList);
				LOGGER.trace("Reload GUI finished");
			}
		} catch (Exception e) {
			LOGGER.warn("Loading parsed frames", e);
		}
	}

	public void controlListReload(JList framelist, List<FrameItemHolder> fihList) {
		List<String> listdata = new ArrayList<String>();
		for (FrameItemHolder fih : fihList) {
			listdata.add(fih.toString());
		}
		framelist.setListData(listdata.toArray());
	}

	private List<FrameItemHolder> parseScriptToFrame(List<String> scriptLines) throws Exception {
		// TODO add complete file list from folder
		List<FrameItemHolder> fihList = new ArrayList<FrameItemHolder>();
		try {
			// parse start
			// step #1: find gesture start
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
				LOGGER.trace("fihList.size() \"" + fihList.size() + "\"");
				// ' sleep(4)'
				singleScriptLine = singleScriptLine.trim();
				LOGGER.trace("singleScriptLine \"" + singleScriptLine + "\"");
				// 'sleep(4)'
				if (!singleScriptLine.contains("setHeadVelocity") 
						&& !singleScriptLine.contains("setArmVelocity")
						&& !singleScriptLine.contains("setHandVelocity")
						&& !singleScriptLine.contains("setTorsoVelocity") 
						&& !singleScriptLine.contains("setHeadSpeed") 
						&& !singleScriptLine.contains("setArmSpeed")
						&& !singleScriptLine.contains("setHandSpeed")
						&& !singleScriptLine.contains("setTorsoSpeed") 
						&& !singleScriptLine.contains("moveHead")
						&& !singleScriptLine.contains("moveArm") 
						&& !singleScriptLine.contains("moveHand")
						&& !singleScriptLine.contains("moveTorso") 
						&& !singleScriptLine.contains("sleep") // end frame
						&& !singleScriptLine.contains("finishedGesture")) { // end gesture
					continue;
				}
				/// at this point we have frame command
				if (singleScriptLine.contains("finishedGesture")) {
					// we are finished
					LOGGER.info("Parsed FRAME count \"" + fihList.size() + "\"");
					return fihList;
				} else if (singleScriptLine.contains("speech")) {
					// ignore
					continue;
				} else if (singleScriptLine.contains("sleep")) {
					// sleep means the end of the frame
					try {
						// parse the frame and add it
						parseScriptFragmentIntoSingleFrame(fihList, frameLines, counter);
						// finish it with a sleep
						parseScriptSleepToFrameSleep(fihList, singleScriptLine);
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
		return fihList;
	}

	private void parseScriptFragmentIntoSingleFrame(List<FrameItemHolder> fihList, 
			List<String> frameLines, int frameCounter) throws Exception {
		try {
			boolean addSpeed = false;
			boolean addMove = false;
			FrameItemHolder fihSpeed = new FrameItemHolder(FrameItemHolder.FrameType.SPEED);
			fihSpeed.setSpeech(null);
			fihSpeed.setName(null);
			fihSpeed.setSleep(-1);
			FrameItemHolder fihMove = new FrameItemHolder(FrameItemHolder.FrameType.MOVE);
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
					LOGGER.trace("splitString[1] expected: 0.95,0.95 \"" + splitString[1] + "\"");;
					// splitString[0] setHeadSpeed
					// splitString[1] 0.95,0.95
					//LOGGER.trace("Testing if split function does as predicted. splitString[0] = " + splitString[0] + " and splitString[1] = " + splitString[1]);
					String[] valuesString = splitString[1].split(",");
					LOGGER.trace("valuesString[0] \"" + valuesString[0] + "\"");
					LOGGER.trace("valuesString.length \"" + valuesString.length + "\"");
					if (splitString[0].contains("Speed") || splitString[0].contains("Velocity")) {
						addSpeed = true;
						if (splitString[0].contains("Head")) {
							// setHeadSpeed(0.95,0.95)
							fihSpeed.setHeadSpeedSet(true);
							fihSpeed.setRotheadspeed(Double.parseDouble(valuesString[0].trim()));
							fihSpeed.setNeckspeed(Double.parseDouble(valuesString[1].trim()));
						} else if (splitString[0].contains("Torso")) {
							// setTorsoSpeed(0.95,0.85,1.0)
							fihSpeed.setTorsoSpeedSet(true);
							fihSpeed.setTopStomspeed(Double.parseDouble(valuesString[0].trim()));
							fihSpeed.setMidStomspeed(Double.parseDouble(valuesString[1].trim()));
							fihSpeed.setLowStomspeed(Double.parseDouble(valuesString[2].trim()));
						} else if (splitString[0].contains("Arm")) {
							if (valuesString[0].contains("left")) {
								// setArmSpeed("left",1.0,0.85,0.95,0.95)
								fihSpeed.setLeftArmSpeedSet(true);
								fihSpeed.setLbicepspeed(Double.parseDouble(valuesString[1].trim()));
								fihSpeed.setLrotatespeed(Double.parseDouble(valuesString[2].trim()));
								fihSpeed.setLshoulderspeed(Double.parseDouble(valuesString[3].trim()));
								fihSpeed.setLomoplatespeed(Double.parseDouble(valuesString[4].trim()));
							} else if (valuesString[0].contains("right")) {
								// setArmSpeed("right",0.65,0.85,0.65,0.85)
								fihSpeed.setRightArmSpeedSet(true);
								fihSpeed.setRbicepspeed(Double.parseDouble(valuesString[1].trim()));
								fihSpeed.setRrotatespeed(Double.parseDouble(valuesString[2].trim()));
								fihSpeed.setRshoulderspeed(Double.parseDouble(valuesString[3].trim()));
								fihSpeed.setRomoplatespeed(Double.parseDouble(valuesString[4].trim()));
							}
						} else if (splitString[0].contains("Hand")) {
							if (valuesString[0].contains("left")) {
								// setHandSpeed("left",0.85,0.85,0.85,0.85,0.85,0.85)
								fihSpeed.setLeftHandSpeedSet(true);
								fihSpeed.setLthumbspeed(Double.parseDouble(valuesString[1].trim()));
								fihSpeed.setLindexspeed(Double.parseDouble(valuesString[2].trim()));
								fihSpeed.setLmajeurespeed(Double.parseDouble(valuesString[3].trim()));
								fihSpeed.setLringfingerspeed(Double.parseDouble(valuesString[4].trim()));
								fihSpeed.setLpinkyspeed(Double.parseDouble(valuesString[5].trim()));
								fihSpeed.setLwristspeed(Double.parseDouble(valuesString[6].trim()));
							} else if (valuesString[0].contains("right")) {
								fihSpeed.setRightHandSpeedSet(true);
								// setHandSpeed("right",0.85,0.85,0.85,0.85,0.85,0.85)
								fihSpeed.setRthumbspeed(Double.parseDouble(valuesString[1].trim()));
								fihSpeed.setRindexspeed(Double.parseDouble(valuesString[2].trim()));
								fihSpeed.setRmajeurespeed(Double.parseDouble(valuesString[3].trim()));
								fihSpeed.setRringfingerspeed(Double.parseDouble(valuesString[4].trim()));
								fihSpeed.setRpinkyspeed(Double.parseDouble(valuesString[5].trim()));
								fihSpeed.setRwristspeed(Double.parseDouble(valuesString[6].trim()));
							}
						} else {

						}
					} else if (splitString[0].contains("move")) {
						addMove = true;
						if (splitString[0].contains("Head")) {
							// moveHead(79,100,82,78,65)
							fihMove.setNeck(Integer.parseInt(valuesString[0].trim()));
							fihMove.setRothead(Integer.parseInt(valuesString[1].trim()));
							fihMove.setEyeX(Integer.parseInt(valuesString[2].trim()));
							fihMove.setEyeY(Integer.parseInt(valuesString[3].trim()));
							fihMove.setJaw(Integer.parseInt(valuesString[4].trim()));
						} else if (splitString[0].contains("Arm")) {
							if (valuesString[0].contains("left")) {
								// moveArm("left",5,84,28,15)
								fihMove.setLbicep(Integer.parseInt(valuesString[1].trim()));
								fihMove.setLrotate(Integer.parseInt(valuesString[2].trim()));
								fihMove.setLshoulder(Integer.parseInt(valuesString[3].trim()));
								fihMove.setLomoplate(Integer.parseInt(valuesString[4].trim()));
							} else if (valuesString[0].contains("right")) {
								// moveArm("right",5,82,28,15)
								fihMove.setRbicep(Integer.parseInt(valuesString[1].trim()));
								fihMove.setRrotate(Integer.parseInt(valuesString[2].trim()));
								fihMove.setRshoulder(Integer.parseInt(valuesString[3].trim()));
								fihMove.setRomoplate(Integer.parseInt(valuesString[4].trim()));
							}
						} else if (splitString[0].contains("Hand")) {
							if (valuesString[0].contains("left")) {
								// moveHand("left",92,33,37,71,66,25)
								fihMove.setLthumb(Integer.parseInt(valuesString[1].trim()));
								fihMove.setLindex(Integer.parseInt(valuesString[2].trim()));
								fihMove.setLmajeure(Integer.parseInt(valuesString[3].trim()));
								fihMove.setLringfinger(Integer.parseInt(valuesString[4].trim()));
								fihMove.setLpinky(Integer.parseInt(valuesString[5].trim()));
								fihMove.setLwrist(Integer.parseInt(valuesString[6].trim()));
							} else if (valuesString[0].contains("right")) {
								// moveHand("right",81,66,82,60,105,113)
								fihMove.setRthumb(Integer.parseInt(valuesString[1].trim()));
								fihMove.setRindex(Integer.parseInt(valuesString[2].trim()));
								fihMove.setRmajeure(Integer.parseInt(valuesString[3].trim()));
								fihMove.setRringfinger(Integer.parseInt(valuesString[4].trim()));
								fihMove.setRpinky(Integer.parseInt(valuesString[5].trim()));
								fihMove.setRwrist(Integer.parseInt(valuesString[6].trim()));
							}
						} else if (splitString[0].contains("Torso")) {
							// moveTorso(90,90,90)
							fihMove.setTopStom(Integer.parseInt(valuesString[0].trim()));
							fihMove.setMidStom(Integer.parseInt(valuesString[1].trim()));
							fihMove.setLowStom(Integer.parseInt(valuesString[2].trim()));
						} else {

						}
					} else {
						// we should never get here
					}
				} catch (Exception e) {
					LOGGER.warn("Frame line parsing error on frame: " + frameCounter + "! ", e);
				}
			}
			if (addSpeed) {
				fihList.add(fihSpeed);
			}
			if (addMove) {
				fihList.add(fihMove);
			}
		} catch (Exception e) {
			LOGGER.warn("Frame line parsing error", e);
		}
	}
	
	private void parseScriptSleepToFrameSleep(List<FrameItemHolder> fihList, String sleepLine) {
		try {
			// sleep line: sleep(3)
			sleepLine = sleepLine.substring(sleepLine.indexOf('(')+1, sleepLine.indexOf(')'));
			Double sleepTime = Double.parseDouble(sleepLine);
			FrameItemHolder fihSleep = new FrameItemHolder(FrameItemHolder.FrameType.SLEEP);
			fihSleep.resetValues();

			fihSleep.setName(null); // sleep frame has Name and Speech as null and Sleep as int
			fihSleep.setSpeech(null);
			fihSleep.setSleep(sleepTime.intValue());

			fihList.add(fihSleep);
		} catch (Exception e) {
			LOGGER.warn("Sleep line parsing error", e);
		}
	}
 
  public void parse_frame_to_script() {
	  String code = "def " + /*ime_funkcije*/"test" + "():\n  i01.startedGesture()\n  "; //def + ime plus () + enter i dva spejsa + i01.
	  for (int i = 0; i < frames.size(); i++) {
	      FrameItemHolder fih = frames.get(i);
	      
	      if(fih.getName() == null && fih.getSleep() == -1) {
	    	  String speeds[] = {"","","","","",""};
	    	  if(fih.getRightHandMoveSet())	speeds[0] = "i01.setHeadVelocity(" + fih.getRotheadspeed() + "," + fih.getNeckspeed() + ")";
	    	  if(fih.getRightArmMoveSet())	speeds[1] = "i01.setArmVelocity(\"left\"," + fih.getLbicepspeed() + "," + fih.getLrotatespeed() + "," + fih.getLshoulderspeed() + "," + fih.getLomoplatespeed() + ")";
	    	  if(fih.getLeftHandMoveSet())	speeds[2] = "i01.setArmVelocity(\"right\"," + fih.getRbicepspeed() + "," + fih.getRrotatespeed() + "," + fih.getRshoulderspeed() + "," + fih.getRomoplatespeed() + ")";
	    	  if(fih.getLeftArmMoveSet())	speeds[3] = "i01.setHandVelocity(\"left\"," + fih.getLthumbspeed() + "," + fih.getLpinkyspeed() + "," + fih.getLmajeurespeed() + "," + fih.getLringfingerspeed() + "," + fih.getLpinkyspeed() + "," + fih.getLwristspeed() +")";
	    	  if(fih.getHeadMoveSet())	speeds[4] = "i01.setHandVelocity(\"right\"," + fih.getRthumbspeed() + "," + fih.getRpinkyspeed() + "," + fih.getRmajeurespeed() + "," + fih.getRringfingerspeed() + "," + fih.getRpinky() + "," + fih.getRwristspeed() +")";
	    	  if(fih.getTorsoMoveSet())	speeds[5] = "i01.setTorsoVelocity(" + fih.getTopStomspeed() + "," + fih.getMidStomspeed() + "," + fih.getLowStomspeed() + ")";
	    	  for(int j = 0; j <= 5; j++) {
	    		  // TODO
//    			  if(fih.getMoveSet()[j]) 
    				  code += (speeds[j] + "\n  ");
	    	  }
	      }else if(fih.getName() == null && fih.getSleep() != -1){
	    	  code += "sleep(" + fih.getSleep() + ")\n  ";
	      }else {
	    	  String movements[] = {"","","","","",""};
	    	  if(fih.getRightHandMoveSet())	movements[0] = "i01.moveHead(" + fih.getNeck() + "," + fih.getRothead() + "," + fih.getEyeX() + "," + fih.getEyeY() + "," + fih.getJaw() + ")";
	    	  if(fih.getRightArmMoveSet())	movements[1] = "i01.moveArm(\"left\"," + fih.getLbicep() + "," + fih.getLrotate() + "," + fih.getLshoulder() + "," + fih.getLomoplate() + ")";
	    	  if(fih.getLeftHandMoveSet())	movements[2] = "i01.moveArm(\"right\"," + fih.getRbicep() + "," + fih.getRrotate() + "," + fih.getRshoulder() + "," + fih.getRomoplate() + ")";
	    	  if(fih.getLeftArmMoveSet())	movements[3] = "i01.moveHand(\"left\"," + fih.getLthumb() + "," + fih.getLpinky() + "," + fih.getLmajeure() + "," + fih.getLringfinger() + "," + fih.getLpinky() + "," + fih.getLwrist() +")";
	    	  if(fih.getHeadMoveSet())	movements[4] = "i01.moveHand(\"right\"," + fih.getRthumb() + "," + fih.getRpinky() + "," + fih.getRmajeure() + "," + fih.getRringfinger() + "," + fih.getRpinky() + "," + fih.getRwrist() +")";
	    	  if(fih.getTorsoMoveSet())	movements[5] = "i01.moveTorso(" + fih.getTopStom() + "," + fih.getMidStom() + "," + fih.getLowStom() + ")";
	    	  for(int j = 0; j <= 5; j++) {
	    		  // TODO
//    			  if(fih.getMoveSet()[j]) 
    				  code += (movements[j] + "\n  ");
	    	  }	    	 
	      }
	  }
	  //code += "i01.finishedGesture()";
	  parsirani_kod = code;
  }
 
  public void frame_add(JList framelist, JTextField frame_add_textfield) {
	    // Add a servo movement frame to the framelist (button bottom-right)
	    FrameItemHolder fih = new FrameItemHolder(FrameItemHolder.FrameType.MOVE);

	    fih.setRthumb(servoitemholder[0][0].sli.getValue());
	    fih.setRindex(servoitemholder[0][1].sli.getValue());
	    fih.setRmajeure(servoitemholder[0][2].sli.getValue());
	    fih.setRringfinger(servoitemholder[0][3].sli.getValue());
	    fih.setRpinky(servoitemholder[0][4].sli.getValue());
	    fih.setRwrist(servoitemholder[0][5].sli.getValue());

	    fih.setRbicep(servoitemholder[1][0].sli.getValue());
	    fih.setRrotate(servoitemholder[1][1].sli.getValue());
	    fih.setRshoulder(servoitemholder[1][2].sli.getValue());
	    fih.setRomoplate(servoitemholder[1][3].sli.getValue());

	    fih.setLthumb(servoitemholder[2][0].sli.getValue());
	    fih.setLindex(servoitemholder[2][1].sli.getValue());
	    fih.setLmajeure(servoitemholder[2][2].sli.getValue());
	    fih.setLringfinger(servoitemholder[2][3].sli.getValue());
	    fih.setLpinky(servoitemholder[2][4].sli.getValue());
	    fih.setLwrist(servoitemholder[2][5].sli.getValue());

	    fih.setLbicep(servoitemholder[3][0].sli.getValue());
	    fih.setLrotate(servoitemholder[3][1].sli.getValue());
	    fih.setLshoulder(servoitemholder[3][2].sli.getValue());
	    fih.setLomoplate(servoitemholder[3][3].sli.getValue());

	    fih.setNeck(servoitemholder[4][0].sli.getValue());
	    fih.setRothead(servoitemholder[4][1].sli.getValue());
	    fih.setEyeX(servoitemholder[4][2].sli.getValue());
	    fih.setEyeY(servoitemholder[4][3].sli.getValue());
	    fih.setJaw(servoitemholder[4][4].sli.getValue());

	    fih.setTopStom(servoitemholder[5][0].sli.getValue());
	    fih.setMidStom(servoitemholder[5][1].sli.getValue());
	    fih.setLowStom(servoitemholder[5][2].sli.getValue());

	    fih.setSleep(-1);
	    fih.setSpeech(null);
	    fih.setName(frame_add_textfield.getText());

	    frames.add(fih);

	    framelistact(framelist);
  }
  
  public void parsescript(JList control_list) {
    pythonitemholder.clear();

    if (true) { //wut?
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
      FrameItemHolder fih = new FrameItemHolder(FrameItemHolder.FrameType.MOVE);

      fih.setRthumb(servoitemholder[0][0].sli.getValue());
      fih.setRindex(servoitemholder[0][1].sli.getValue());
      fih.setRmajeure(servoitemholder[0][2].sli.getValue());
      fih.setRringfinger(servoitemholder[0][3].sli.getValue());
      fih.setRpinky(servoitemholder[0][4].sli.getValue());
      fih.setRwrist(servoitemholder[0][5].sli.getValue());

      fih.setRbicep(servoitemholder[1][0].sli.getValue());
      fih.setRrotate(servoitemholder[1][1].sli.getValue());
      fih.setRshoulder(servoitemholder[1][2].sli.getValue());
      fih.setRomoplate(servoitemholder[1][3].sli.getValue());

      fih.setLthumb(servoitemholder[2][0].sli.getValue());
      fih.setLindex(servoitemholder[2][1].sli.getValue());
      fih.setLmajeure(servoitemholder[2][2].sli.getValue());
      fih.setLringfinger(servoitemholder[2][3].sli.getValue());
      fih.setLpinky(servoitemholder[2][4].sli.getValue());
      fih.setLwrist(servoitemholder[2][5].sli.getValue());

      fih.setLbicep(servoitemholder[3][0].sli.getValue());
      fih.setLrotate(servoitemholder[3][1].sli.getValue());
      fih.setLshoulder(servoitemholder[3][2].sli.getValue());
      fih.setLomoplate(servoitemholder[3][3].sli.getValue());

      fih.setNeck(servoitemholder[4][0].sli.getValue());
      fih.setRothead(servoitemholder[4][1].sli.getValue());
      fih.setEyeX(servoitemholder[4][2].sli.getValue());
      fih.setEyeY(servoitemholder[4][3].sli.getValue());
      fih.setJaw(servoitemholder[4][4].sli.getValue());

      fih.setTopStom(servoitemholder[5][0].sli.getValue());
      fih.setMidStom(servoitemholder[5][1].sli.getValue());
      fih.setLowStom(servoitemholder[5][2].sli.getValue());

      if (fih.getRightHandMoveSet()) {
        i01.moveHead(fih.getNeck(), fih.getRothead(), fih.getEyeX(), fih.getEyeY(), fih.getJaw());
      }
      if (fih.getRightArmMoveSet()) {
        i01.moveArm("left", fih.getLbicep(), fih.getLrotate(), fih.getLshoulder(), fih.getLomoplate());
      }
      if (fih.getLeftHandMoveSet()) {
        i01.moveArm("right", fih.getRbicep(), fih.getRrotate(), fih.getRshoulder(), fih.getRomoplate());
      }
      if (fih.getLeftArmMoveSet()) {
        i01.moveHand("left", fih.getLthumb(), fih.getLindex(), fih.getLmajeure(), fih.getLringfinger(), fih.getLpinky(), (double) fih.getLwrist());
      }
      if (fih.getHeadMoveSet()) {
        i01.moveHand("right", fih.getRthumb(), fih.getRindex(), fih.getRmajeure(), fih.getRringfinger(), fih.getRpinky(), (double) fih.getRwrist());
      }
      if (fih.getTorsoMoveSet()) {
        i01.moveTorso(fih.getTopStom(), fih.getMidStom(), fih.getLowStom());
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
		LOGGER.info("frameSelectionChanged [START]");
		LOGGER.info("frameItemHolderIndex \"" + frameItemHolderIndex + "\"");
		LOGGER.info("frames.get(frameItemHolderIndex) \"" + frames.get(frameItemHolderIndex) + "\"");
		initializeBottomPaneTabs(bottom, frames.get(frameItemHolderIndex));
		LOGGER.info("frameSelectionChanged [END]");
	}
		
	public void initializeBottomPaneTabs(JPanel bottom, FrameItemHolder frameItemHolder) {
		LOGGER.info("initializeBottomPaneTabs [START]");
		try {
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
	//					myService.send(boundServiceName, "tabs_main_checkbox_states_changed",
	//							tabs_main_checkbox_states);
						tabs_main_checkbox_states_changed(tabs_main_checkbox_states);
					}
	
				});
				checkbox.setSelected(true);
				mainpanel.add(checkbox);
			}
	
			Container c1con = c1panel;
			Container c2con = c2panel;
			Container c3con = c3panel;
	
			GridBagLayout c1gbl = new GridBagLayout();
			c1con.setLayout(c1gbl);
			GridBagLayout c2gbl = new GridBagLayout();
			c2con.setLayout(c2gbl);
			GridBagLayout c3gbl = new GridBagLayout();
			c3con.setLayout(c3gbl);
	
			// predefined min- / res- / max- positions
			int[][][] minresmaxpos = {
					{ { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } },
					{ { 0, 90, 180 }, { 0, 90, 180 }, { 0, 90, 180 } } };
			
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
	
				for (int i2 = 0; i2 < size; i2++) {
					ServoItemHolder sih11 = new ServoItemHolder();
	
					String servoname = "";
	
					if (i1 == 0 || i1 == 2) {
						if (i2 == 0) {
							servoname = "thumb";
						} else if (i2 == 1) {
							servoname = "index";
						} else if (i2 == 2) {
							servoname = "majeure";
						} else if (i2 == 3) {
							servoname = "ringfinger";
						} else if (i2 == 4) {
							servoname = "pinky";
						} else if (i2 == 5) {
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
					customizeslider(sih11.sli, i1, i2, minresmaxpos[i1][i2]);
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
	//			myService.send(boundServiceName, "servoitemholder_set_sih1", i1, sih1);
				servoitemholder_set_sih1(i1, sih1);
			}
	
			bottomTabs.addTab("Main", mainpanel);
			bottomTabs.addTab("Right Side", c1panel);
			bottomTabs.addTab("Left Side", c2panel);
			bottomTabs.addTab("Head + Torso", c3panel);
			bottom.removeAll();
			bottom.add(BorderLayout.CENTER, bottomTabs);
			
			LOGGER.info("initializeBottomPaneTabs [END]");
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
}