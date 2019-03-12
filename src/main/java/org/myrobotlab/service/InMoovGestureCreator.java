package org.myrobotlab.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.lang.StringUtils;
import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.ServiceType;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.myrobotlab.logging.LoggingFactory;
import org.myrobotlab.service.model.FrameItemHolder;
import org.slf4j.Logger;

import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.io.Files;

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

	public final static Logger log = LoggerFactory.getLogger(InMoovGestureCreator.class);

	transient ServoItemHolder[][] servoitemholder;

	transient ArrayList<FrameItemHolder> frameitemholder;

	transient ArrayList<PythonItemHolder> pythonitemholder;

	boolean[] tabs_main_checkbox_states;

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
    frameitemholder = new ArrayList<FrameItemHolder>();
    pythonitemholder = new ArrayList<PythonItemHolder>();

    tabs_main_checkbox_states = new boolean[6];
  }


  public void control_addgest(JList control_list, JTextField control_gestname, JTextField control_funcname) {
    // Add the current gesture to the script (button bottom-left)
    String defname = ime_funkcije = control_funcname.getText();
    String gestname = ime_gest = control_gestname.getText();

    String code = "";
    for (FrameItemHolder fih : frameitemholder) {
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
        if (tabs_main_checkbox_states[0]) {
          code11 = "    " + pythonname + ".moveHead(" + fih.getNeck() + "," + fih.getRothead() + "," + fih.getEyeX() + "," + fih.getEyeY() + "," + fih.getJaw() + ")\n";
        }
        if (tabs_main_checkbox_states[1]) {
          code12 = "    " + pythonname + ".moveArm(\"left\"," + fih.getLbicep() + "," + fih.getLrotate() + "," + fih.getLshoulder() + "," + fih.getLomoplate() + ")\n";
        }
        if (tabs_main_checkbox_states[2]) {
          code13 = "    " + pythonname + ".moveArm(\"right\"," + fih.getRbicep() + "," + fih.getRrotate() + "," + fih.getRshoulder() + "," + fih.getRomoplate() + ")\n";
        }
        if (tabs_main_checkbox_states[3]) {
          code14 = "    " + pythonname + ".moveHand(\"left\"," + fih.getLthumb() + "," + fih.getLindex() + "," + fih.getLmajeure() + "," + fih.getLringfinger() + "," + fih.getLpinky() + "," + fih.getLwrist()
              + ")\n";
        }
        if (tabs_main_checkbox_states[4]) {
          code15 = "    " + pythonname + ".moveHand(\"right\"," + fih.getRthumb() + "," + fih.getRindex() + "," + fih.getRmajeure() + "," + fih.getRringfinger() + "," + fih.getRpinky() + "," + fih.getRwrist()
              + ")\n";
        }
        if (tabs_main_checkbox_states[5]) {
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
        if (tabs_main_checkbox_states[0]) {
          code11 = "    " + pythonname + ".setHeadSpeed(" + fih.getNeckspeed() + "," + fih.getRotheadspeed() + "," + fih.getEyeXspeed() + "," + fih.getEyeYspeed() + "," + fih.getJawspeed() + ")\n";
        }
        if (tabs_main_checkbox_states[1]) {
          code12 = "    " + pythonname + ".setArmSpeed(\"left\"," + fih.getLbicepspeed() + "," + fih.getLrotatespeed() + "," + fih.getLshoulderspeed() + "," + fih.getLomoplatespeed() + ")\n";
        }
        if (tabs_main_checkbox_states[2]) {
          code13 = "    " + pythonname + ".setArmSpeed(\"right\"," + fih.getRbicepspeed() + "," + fih.getRrotatespeed() + "," + fih.getRshoulderspeed() + "," + fih.getRomoplatespeed() + ")\n";
        }
        if (tabs_main_checkbox_states[3]) {
          code14 = "    " + pythonname + ".setHandSpeed(\"left\"," + fih.getLthumbspeed() + "," + fih.getLindexspeed() + "," + fih.getLmajeurespeed() + "," + fih.getLringfingerspeed() + ","
              + fih.getLpinkyspeed() + "," + fih.getLwristspeed() + ")\n";
        }
        if (tabs_main_checkbox_states[4]) {
          code15 = "    " + pythonname + ".setHandSpeed(\"right\"," + fih.getRthumbspeed() + "," + fih.getRindexspeed() + "," + fih.getRmajeurespeed() + "," + fih.getRringfingerspeed() + ","
              + fih.getRpinkyspeed() + "," + fih.getRwristspeed() + ")\n";
        }
        if (tabs_main_checkbox_states[5]) {
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
        frameitemholder.clear();

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
            fih = new FrameItemHolder();
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
              frameitemholder.add(fih);
              fih = null;
              pos++;
            } else if (line2.startsWith(pythonname)) {
              if (line2.startsWith(pythonname + ".mouth.speak")) {
                fih.setSleep(-1);
                fih.setSpeech(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
                fih.setName(null);
                frameitemholder.add(fih);
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
              frameitemholder.add(fih);
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
              frameitemholder.add(fih);
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
      for (FrameItemHolder fih : frameitemholder) {
        if (fih.getSleep() != -1) {
          sleep(fih.getSleep());
        } else if (fih.getSpeech() != null) {
          try {
            i01.mouth.speakBlocking(fih.getSpeech());
          } catch (Exception e) {
            Logging.logError(e);
          }
        } else if (fih.getName() != null) {
          if (tabs_main_checkbox_states[0]) {
            i01.moveHead(fih.getNeck(), fih.getRothead(), fih.getEyeX(), fih.getEyeY(), fih.getJaw());
          }
          if (tabs_main_checkbox_states[1]) {
            i01.moveArm("left", fih.getLbicep(), fih.getLrotate(), fih.getLshoulder(), fih.getLomoplate());
          }
          if (tabs_main_checkbox_states[2]) {
            i01.moveArm("right", fih.getRbicep(), fih.getRrotate(), fih.getRshoulder(), fih.getRomoplate());
          }
          if (tabs_main_checkbox_states[3]) {
            i01.moveHand("left", fih.getLthumb(), fih.getLindex(), fih.getLmajeure(), fih.getLringfinger(), fih.getLpinky(), (double) fih.getLwrist());
          }
          if (tabs_main_checkbox_states[4]) {
            i01.moveHand("right", fih.getRthumb(), fih.getRindex(), fih.getRmajeure(), fih.getRringfinger(), fih.getRpinky(), (double) fih.getRwrist());
          }
          if (tabs_main_checkbox_states[5]) {
            i01.moveTorso(fih.getTopStom(), fih.getMidStom(), fih.getLowStom());
          }
        } else {
          if (tabs_main_checkbox_states[0]) {
            i01.setHeadSpeed(fih.getNeckspeed(), fih.getRotheadspeed(), fih.getEyeXspeed(), fih.getEyeYspeed(), fih.getJawspeed());
          }
          if (tabs_main_checkbox_states[1]) {
            i01.setArmSpeed("left", fih.getLbicepspeed(), fih.getLrotatespeed(), fih.getLshoulderspeed(), fih.getLomoplatespeed());
          }
          if (tabs_main_checkbox_states[2]) {
            i01.setArmSpeed("right", fih.getRbicepspeed(), fih.getRrotatespeed(), fih.getRshoulderspeed(), fih.getRomoplatespeed());
          }
          if (tabs_main_checkbox_states[3]) {
            i01.setHandSpeed("left", fih.getLthumbspeed(), fih.getLindexspeed(), fih.getLmajeurespeed(), fih.getLringfingerspeed(), fih.getLpinkyspeed(), fih.getLwristspeed());
          }
          if (tabs_main_checkbox_states[4]) {
            i01.setHandSpeed("right", fih.getRthumbspeed(), fih.getRindexspeed(), fih.getRmajeurespeed(), fih.getRringfingerspeed(), fih.getRpinkyspeed(), fih.getRwristspeed());
          }
          if (tabs_main_checkbox_states[5]) {
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
        for (FrameItemHolder fih : frameitemholder) {
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
            if (tabs_main_checkbox_states[0]) {
              code11 = "    " + pythonname + ".moveHead(" + fih.getNeck() + "," + fih.getRothead() + "," + fih.getEyeX() + "," + fih.getEyeY() + "," + fih.getJaw() + ")\n";
            }
            if (tabs_main_checkbox_states[1]) {
              code12 = "    " + pythonname + ".moveArm(\"left\"," + fih.getLbicep() + "," + fih.getLrotate() + "," + fih.getLshoulder() + "," + fih.getLomoplate() + ")\n";
            }
            if (tabs_main_checkbox_states[2]) {
              code13 = "    " + pythonname + ".moveArm(\"right\"," + fih.getRbicep() + "," + fih.getRrotate() + "," + fih.getRshoulder() + "," + fih.getRomoplate() + ")\n";
            }
            if (tabs_main_checkbox_states[3]) {
              code14 = "    " + pythonname + ".moveHand(\"left\"," + fih.getLthumb() + "," + fih.getLindex() + "," + fih.getLmajeure() + "," + fih.getLringfinger() + "," + fih.getLpinky() + ","
                  + fih.getLwrist() + ")\n";
            }
            if (tabs_main_checkbox_states[4]) {
              code15 = "    " + pythonname + ".moveHand(\"right\"," + fih.getRthumb() + "," + fih.getRindex() + "," + fih.getRmajeure() + "," + fih.getRringfinger() + "," + fih.getRpinky() + ","
                  + fih.getRwrist() + ")\n";
            }
            if (tabs_main_checkbox_states[5]) {
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
            if (tabs_main_checkbox_states[0]) {
              code11 = "    " + pythonname + ".setHeadSpeed(" + fih.getNeckspeed() + "," + fih.getRotheadspeed() + "," + fih.getEyeXspeed() + "," + fih.getEyeYspeed() + "," + fih.getJawspeed() + ")\n";
            }
            if (tabs_main_checkbox_states[1]) {
              code12 = "    " + pythonname + ".setArmSpeed(\"left\"," + fih.getLbicepspeed() + "," + fih.getLrotatespeed() + "," + fih.getLshoulderspeed() + "," + fih.getLomoplatespeed() + ")\n";
            }

            if (tabs_main_checkbox_states[2]) {
              code13 = "    " + pythonname + ".setArmSpeed(\"right\"," + fih.getRbicepspeed() + "," + fih.getRrotatespeed() + "," + fih.getRshoulderspeed() + "," + fih.getRomoplatespeed() + ")\n";
            }
            if (tabs_main_checkbox_states[3]) {
              code14 = "    " + pythonname + ".setHandSpeed(\"left\"," + fih.getLthumbspeed() + "," + fih.getLindexspeed() + "," + fih.getLmajeurespeed() + "," + fih.getLringfingerspeed() + ","
                  + fih.getLpinkyspeed() + "," + fih.getLwristspeed() + ")\n";
            }
            if (tabs_main_checkbox_states[4]) {
              code15 = "    " + pythonname + ".setHandSpeed(\"right\"," + fih.getRthumbspeed() + "," + fih.getRindexspeed() + "," + fih.getRmajeurespeed() + "," + fih.getRringfingerspeed() + ","
                  + fih.getRpinkyspeed() + "," + fih.getRwristspeed() + ")\n";
            }
            if (tabs_main_checkbox_states[5]) {
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
    FrameItemHolder fih = new FrameItemHolder();

    fih.setSleep(Integer.parseInt(frame_addsleep_textfield.getText()));
    fih.setSpeech(null);
    fih.setName(null);

    frameitemholder.add(fih);

    framelistact(framelist);
  }

  public void frame_addspeech(JList framelist, JTextField frame_addspeech_textfield) {
    // Add a speech frame to the framelist (button bottom-right)
    FrameItemHolder fih = new FrameItemHolder();

    fih.setSleep(-1);
    fih.setSpeech(frame_addspeech_textfield.getText());
    fih.setName(null);

    frameitemholder.add(fih);

    framelistact(framelist);
  }

  public void frame_addspeed(JList framelist) {
    // Add a speed setting frame to the framelist (button bottom-right)
    FrameItemHolder fih = new FrameItemHolder();

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

    frameitemholder.add(fih);

    framelistact(framelist);
  }

  public void frame_copy(JList framelist) {
    // Copy this frame on the framelist (button bottom-right)
    int pos = framelist.getSelectedIndex();

    if (pos != -1) {
      FrameItemHolder fih = frameitemholder.get(pos);
      frameitemholder.add(fih);

      framelistact(framelist);
    }
  }

  public void frame_down(JList framelist) {
    // Move this frame one down on the framelist (button bottom-right)
    int pos = framelist.getSelectedIndex();

    if (pos != -1) {
      FrameItemHolder fih = frameitemholder.remove(pos);
      frameitemholder.add(pos + 1, fih);

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
      if (frameitemholder.get(pos).getSleep() != -1) {
        frame_addsleep_textfield.setText(frameitemholder.get(pos).getSleep() + "");
      } else if (frameitemholder.get(pos).getSpeech() != null) {
        frame_addspeech_textfield.setText(frameitemholder.get(pos).getSpeech());
      } else if (frameitemholder.get(pos).getName() != null) {
        servoitemholder[0][0].sli.setValue(frameitemholder.get(pos).getRthumb());
        servoitemholder[0][1].sli.setValue(frameitemholder.get(pos).getRindex());
        servoitemholder[0][2].sli.setValue(frameitemholder.get(pos).getRmajeure());
        servoitemholder[0][3].sli.setValue(frameitemholder.get(pos).getRringfinger());
        servoitemholder[0][4].sli.setValue(frameitemholder.get(pos).getRpinky());
        servoitemholder[0][5].sli.setValue(frameitemholder.get(pos).getRwrist());

        servoitemholder[1][0].sli.setValue(frameitemholder.get(pos).getRbicep());
        servoitemholder[1][1].sli.setValue(frameitemholder.get(pos).getRrotate());
        servoitemholder[1][2].sli.setValue(frameitemholder.get(pos).getRshoulder());
        servoitemholder[1][3].sli.setValue(frameitemholder.get(pos).getRomoplate());

        servoitemholder[2][0].sli.setValue(frameitemholder.get(pos).getLthumb());
        servoitemholder[2][1].sli.setValue(frameitemholder.get(pos).getLindex());
        servoitemholder[2][2].sli.setValue(frameitemholder.get(pos).getLmajeure());
        servoitemholder[2][3].sli.setValue(frameitemholder.get(pos).getLringfinger());
        servoitemholder[2][4].sli.setValue(frameitemholder.get(pos).getLpinky());
        servoitemholder[2][5].sli.setValue(frameitemholder.get(pos).getLwrist());

        servoitemholder[3][0].sli.setValue(frameitemholder.get(pos).getLbicep());
        servoitemholder[3][1].sli.setValue(frameitemholder.get(pos).getLrotate());
        servoitemholder[3][2].sli.setValue(frameitemholder.get(pos).getLshoulder());
        servoitemholder[3][3].sli.setValue(frameitemholder.get(pos).getLomoplate());

        servoitemholder[4][0].sli.setValue(frameitemholder.get(pos).getNeck());
        servoitemholder[4][1].sli.setValue(frameitemholder.get(pos).getRothead());
        servoitemholder[4][2].sli.setValue(frameitemholder.get(pos).getEyeX());
        servoitemholder[4][3].sli.setValue(frameitemholder.get(pos).getEyeY());
        servoitemholder[4][4].sli.setValue(frameitemholder.get(pos).getJaw());

        servoitemholder[5][0].sli.setValue(frameitemholder.get(pos).getTopStom());
        servoitemholder[5][1].sli.setValue(frameitemholder.get(pos).getMidStom());
        servoitemholder[5][2].sli.setValue(frameitemholder.get(pos).getLowStom());
        frame_add_textfield.setText(frameitemholder.get(pos).getName());
      } else {
        servoitemholder[0][0].spe.setText(frameitemholder.get(pos).getRthumbspeed() + "");
        servoitemholder[0][1].spe.setText(frameitemholder.get(pos).getRindexspeed() + "");
        servoitemholder[0][2].spe.setText(frameitemholder.get(pos).getRmajeurespeed() + "");
        servoitemholder[0][3].spe.setText(frameitemholder.get(pos).getRringfingerspeed() + "");
        servoitemholder[0][4].spe.setText(frameitemholder.get(pos).getRpinkyspeed() + "");
        servoitemholder[0][5].spe.setText(frameitemholder.get(pos).getRwristspeed() + "");

        servoitemholder[1][0].spe.setText(frameitemholder.get(pos).getRbicepspeed() + "");
        servoitemholder[1][1].spe.setText(frameitemholder.get(pos).getRrotatespeed() + "");
        servoitemholder[1][2].spe.setText(frameitemholder.get(pos).getRshoulderspeed() + "");
        servoitemholder[1][3].spe.setText(frameitemholder.get(pos).getRomoplatespeed() + "");

        servoitemholder[2][0].spe.setText(frameitemholder.get(pos).getLthumbspeed() + "");
        servoitemholder[2][1].spe.setText(frameitemholder.get(pos).getLindexspeed() + "");
        servoitemholder[2][2].spe.setText(frameitemholder.get(pos).getLmajeurespeed() + "");
        servoitemholder[2][3].spe.setText(frameitemholder.get(pos).getLringfingerspeed() + "");
        servoitemholder[2][4].spe.setText(frameitemholder.get(pos).getLpinkyspeed() + "");
        servoitemholder[2][5].spe.setText(frameitemholder.get(pos).getLwristspeed() + "");

        servoitemholder[3][0].spe.setText(frameitemholder.get(pos).getLbicepspeed() + "");
        servoitemholder[3][1].spe.setText(frameitemholder.get(pos).getLrotatespeed() + "");
        servoitemholder[3][2].spe.setText(frameitemholder.get(pos).getLshoulderspeed() + "");
        servoitemholder[3][3].spe.setText(frameitemholder.get(pos).getLomoplatespeed() + "");

        servoitemholder[4][0].spe.setText(frameitemholder.get(pos).getNeckspeed() + "");
        servoitemholder[4][1].spe.setText(frameitemholder.get(pos).getRotheadspeed() + "");
        servoitemholder[4][2].spe.setText(frameitemholder.get(pos).getEyeXspeed() + "");
        servoitemholder[4][3].spe.setText(frameitemholder.get(pos).getEyeYspeed() + "");
        servoitemholder[4][4].spe.setText(frameitemholder.get(pos).getJawspeed() + "");

        servoitemholder[5][0].spe.setText(frameitemholder.get(pos).getTopStomspeed() + "");
        servoitemholder[5][1].spe.setText(frameitemholder.get(pos).getMidStomspeed() + "");
        servoitemholder[5][2].spe.setText(frameitemholder.get(pos).getLowStomspeed() + "");
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
      frameitemholder.remove(pos);

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
        if (tabs_main_checkbox_states[0]) {
          i01.moveHead(fih.getNeck(), fih.getRothead(), fih.getEyeX(), fih.getEyeY(), fih.getJaw());
        }
        if (tabs_main_checkbox_states[1]) {
          i01.moveArm("left", fih.getLbicep(), fih.getLrotate(), fih.getLshoulder(), fih.getLomoplate());
        }
        if (tabs_main_checkbox_states[2]) {
          i01.moveArm("right", fih.getRbicep(), fih.getRrotate(), fih.getRshoulder(), fih.getRomoplate());
        }
        if (tabs_main_checkbox_states[3]) {
          i01.moveHand("left", fih.getLthumb(), fih.getLindex(), fih.getLmajeure(), fih.getLringfinger(), fih.getLpinky(), (double) fih.getLwrist());
        }
        if (tabs_main_checkbox_states[4]) {
          i01.moveHand("right", fih.getRthumb(), fih.getRindex(), fih.getRmajeure(), fih.getRringfinger(), fih.getRpinky(), (double) fih.getRwrist());
        }
        if (tabs_main_checkbox_states[5]) {
          i01.moveTorso(fih.getTopStom(), fih.getMidStom(), fih.getLowStom());
        }
      } else {
        if (tabs_main_checkbox_states[0]) {
          i01.setHeadSpeed(fih.getNeckspeed(), fih.getRotheadspeed(), fih.getEyeXspeed(), fih.getEyeYspeed(), fih.getJawspeed());
        }
        if (tabs_main_checkbox_states[1]) {
          i01.setArmSpeed("left", fih.getLbicepspeed(), fih.getLrotatespeed(), fih.getLshoulderspeed(), fih.getLomoplatespeed());
        }
        if (tabs_main_checkbox_states[2]) {
          i01.setArmSpeed("right", fih.getRbicepspeed(), fih.getRrotatespeed(), fih.getRshoulderspeed(), fih.getRomoplatespeed());
        }
        if (tabs_main_checkbox_states[3]) {
          i01.setHandSpeed("left", fih.getLthumbspeed(), fih.getLindexspeed(), fih.getLmajeurespeed(), fih.getLringfingerspeed(), fih.getLpinkyspeed(), fih.getLwristspeed());
        }
        if (tabs_main_checkbox_states[4]) {
          i01.setHandSpeed("right", fih.getRthumbspeed(), fih.getRindexspeed(), fih.getRmajeurespeed(), fih.getRringfingerspeed(), fih.getRpinkyspeed(), fih.getRwristspeed());
        }
        if (tabs_main_checkbox_states[5]) {
          i01.setTorsoSpeed(fih.getTopStomspeed(), fih.getMidStomspeed(), fih.getLowStomspeed());
        }
      }
    }
  }
*/
  public void frame_test(JList framelist) {
	    // Test this frame (execute)
	    int pos = framelist.getSelectedIndex();
	    log.info("indeks je: " + framelist.getSelectedIndex() + "a i01 = "  + (i01 == null ? "null" : "nije_ null"));
	    if (i01 != null && pos != -1) {
		    for (int i = 0; i < frameitemholder.size(); i++) {
		  	  FrameItemHolder fih = frameitemholder.get(i);
		    //  FrameItemHolder fih = frameitemholder.get(pos);
		      log.info("Trenutno se testira: " + fih.getName() + "\n");
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
		        if (tabs_main_checkbox_states[0]) {
		          i01.moveHead(fih.getNeck(), fih.getRothead(), fih.getEyeX(), fih.getEyeY(), fih.getJaw());
		        }
		        if (tabs_main_checkbox_states[1]) {
		          i01.moveArm("left", fih.getLbicep(), fih.getLrotate(), fih.getLshoulder(), fih.getLomoplate());
		        }
		        if (tabs_main_checkbox_states[2]) {
		          i01.moveArm("right", fih.getRbicep(), fih.getRrotate(), fih.getRshoulder(), fih.getRomoplate());
		        }
		        if (tabs_main_checkbox_states[3]) {
		          i01.moveHand("left", fih.getLthumb(), fih.getLindex(), fih.getLmajeure(), fih.getLringfinger(), fih.getLpinky(), (double) fih.getLwrist());
		        }
		        if (tabs_main_checkbox_states[4]) {
		          i01.moveHand("right", fih.getRthumb(), fih.getRindex(), fih.getRmajeure(), fih.getRringfinger(), fih.getRpinky(), (double) fih.getRwrist());
		        }
		        if (tabs_main_checkbox_states[5]) {
		          i01.moveTorso(fih.getTopStom(), fih.getMidStom(), fih.getLowStom());
		        }
		      } else {
		        if (tabs_main_checkbox_states[0]) {
		          i01.setHeadSpeed(fih.getNeckspeed(), fih.getRotheadspeed(), fih.getEyeXspeed(), fih.getEyeYspeed(), fih.getJawspeed());
		        }
		        if (tabs_main_checkbox_states[1]) {
		          i01.setArmSpeed("left", fih.getLbicepspeed(), fih.getLrotatespeed(), fih.getLshoulderspeed(), fih.getLomoplatespeed());
		        }
		        if (tabs_main_checkbox_states[2]) {
		          i01.setArmSpeed("right", fih.getRbicepspeed(), fih.getRrotatespeed(), fih.getRshoulderspeed(), fih.getRomoplatespeed());
		        }
		        if (tabs_main_checkbox_states[3]) {
		          i01.setHandSpeed("left", fih.getLthumbspeed(), fih.getLindexspeed(), fih.getLmajeurespeed(), fih.getLringfingerspeed(), fih.getLpinkyspeed(), fih.getLwristspeed());
		        }
		        if (tabs_main_checkbox_states[4]) {
		          i01.setHandSpeed("right", fih.getRthumbspeed(), fih.getRindexspeed(), fih.getRmajeurespeed(), fih.getRringfingerspeed(), fih.getRpinkyspeed(), fih.getRwristspeed());
		        }
		        if (tabs_main_checkbox_states[5]) {
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
      FrameItemHolder fih = frameitemholder.remove(pos);
      frameitemholder.add(pos - 1, fih);

      framelistact(framelist);
    }
  }

  public void frame_update(JList framelist, JTextField frame_add_textfield, JTextField frame_addsleep_textfield, JTextField frame_addspeech_textfield) {
    // Update this frame on the framelist (button bottom-right)

    int pos = framelist.getSelectedIndex();

    if (pos != -1) {
      FrameItemHolder fih = new FrameItemHolder();

      // sleep || speech || servo movement || speed setting
      if (frameitemholder.get(pos).getSleep() != -1) {
        fih.setSleep(Integer.parseInt(frame_addsleep_textfield.getText()));
        fih.setSpeech(null);
        fih.setName(null);
      } else if (frameitemholder.get(pos).getSpeech() != null) {
        fih.setSleep(-1);
        fih.setSpeech(frame_addspeech_textfield.getText());
        fih.setName(null);
      } else if (frameitemholder.get(pos).getName() != null) {
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
      frameitemholder.set(pos, fih);

      framelistact(framelist);
    }
  }

  public void framelistact(JList framelist) {
    // Re-Build the framelist
	log.info("Pozvan sam pri incijalizaciji!");
	frameListGlobal = framelist;
    String[] listdata = new String[frameitemholder.size()];

    for (int i = 0; i < frameitemholder.size(); i++) {
      FrameItemHolder fih = frameitemholder.get(i);

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
        if (tabs_main_checkbox_states[0]) {
          displaytext1 = fih.getRthumb() + " " + fih.getRindex() + " " + fih.getRmajeure() + " " + fih.getRringfinger() + " " + fih.getRpinky() + " " + fih.getRwrist();
        }
        if (tabs_main_checkbox_states[1]) {
          displaytext2 = fih.getRbicep() + " " + fih.getRrotate() + " " + fih.getRshoulder() + " " + fih.getRomoplate();
        }
        if (tabs_main_checkbox_states[2]) {
          displaytext3 = fih.getLthumb() + " " + fih.getLindex() + " " + fih.getLmajeure() + " " + fih.getLringfinger() + " " + fih.getLpinky() + " " + fih.getLwrist();
        }
        if (tabs_main_checkbox_states[3]) {
          displaytext4 = fih.getLbicep() + " " + fih.getLrotate() + " " + fih.getLshoulder() + " " + fih.getLomoplate();
        }
        if (tabs_main_checkbox_states[4]) {
          displaytext5 = fih.getNeck() + " " + fih.getRothead() + " " + fih.getEyeX() + " " + fih.getEyeY() + " " + fih.getJaw();
        }
        if (tabs_main_checkbox_states[5]) {
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
        if (tabs_main_checkbox_states[0]) {
          displaytext1 = fih.getRthumbspeed() + " " + fih.getRindexspeed() + " " + fih.getRmajeurespeed() + " " + fih.getRringfingerspeed() + " " + fih.getRpinkyspeed() + " " + fih.getRwristspeed();
        }
        if (tabs_main_checkbox_states[1]) {
          displaytext2 = fih.getRbicepspeed() + " " + fih.getRrotatespeed() + " " + fih.getRshoulderspeed() + " " + fih.getRomoplatespeed();
        }
        if (tabs_main_checkbox_states[2]) {
          displaytext3 = fih.getLthumbspeed() + " " + fih.getLindexspeed() + " " + fih.getLmajeurespeed() + " " + fih.getLringfingerspeed() + " " + fih.getLpinkyspeed() + " " + fih.getLwristspeed();
        }
        if (tabs_main_checkbox_states[3]) {
          displaytext4 = fih.getLbicepspeed() + " " + fih.getLrotatespeed() + " " + fih.getLshoulderspeed() + " " + fih.getLomoplatespeed();
        }
        if (tabs_main_checkbox_states[4]) {
          displaytext5 = fih.getNeckspeed() + " " + fih.getRotheadspeed() + " " + fih.getEyeXspeed() + " " + fih.getEyeYspeed() + " " + fih.getJawspeed();
        }
        if (tabs_main_checkbox_states[5]) {
          displaytext6 = fih.getTopStomspeed() + " " + fih.getMidStomspeed() + " " + fih.getLowStomspeed();
        }
        displaytext = "SPEED   " + displaytext1 + " | " + displaytext2 + " | " + displaytext3 + " | " + displaytext4 + " | " + displaytext5 + " | " + displaytext6;
      }
      listdata[i] = displaytext;
    }

    framelist.setListData(listdata);
  }

  public void control_loadscri(JList control_list) {
	
		List<String> scriptLines = new ArrayList<String>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
//			fileReader = new FileReader("/home/abe/ws-fx/inmoov/InMoov/gestures/" + control_list.getSelectedValue().toString());
			fileReader = new FileReader("/home/abe/balance.py");
			bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				scriptLines.add(line);
			}
			bufferedReader.close();
		} catch (Exception e) {
			log.warn("Exception occurred trying to read /home/abe/balance.py", e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					log.warn("Could not close fileReader", e);
				}
			}
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.warn("Could not close bufferedReader", e);
				}
			}
		}
		
//		pythonscript = stringBuffer.toString();
		//parsescript(control_list);
		try {
			parseScriptToFrame(control_list, scriptLines);
		} catch (Exception e) {
			// TODO
		}		
  }

	private List<FrameItemHolder> parseScriptToFrame(JList list, List<String> scriptLines) throws Exception {
		// TODO add complete file list from folder
		pythonitemholder.clear();
		PythonItemHolder pythonItem = new PythonItemHolder();
		//pythonitemholder.add(pythonItem);
		// parse start
		// step #1: find gesture start
		boolean gestureStartFound = false;
		int counter = 0;
		for(String singleScriptLine : scriptLines) {
			if(singleScriptLine.contains("startedGesture")) {
				// we have a gesture start
				gestureStartFound = true;
				break;
			}
			counter++;
		}
		if(!gestureStartFound) {
			throw new Exception("Gestrue not found. Please provide a startedGesture() as the first command!");
		}
		// trimming lines before startedGesture
		scriptLines = scriptLines.subList(counter, scriptLines.size());
		// at this point the first gesture is starting
		List<String> frameLines = new ArrayList<String>();
		List<FrameItemHolder> fihList = new ArrayList<FrameItemHolder>();
		for(String singleScriptLine : scriptLines) {
			// '  sleep(4)'
			singleScriptLine = singleScriptLine.trim();
			// 'sleep(4)'
			if(!singleScriptLine.contains("setHeadVelocity") 
					&& !singleScriptLine.contains("setArmVelocity") 
					&& !singleScriptLine.contains("setHandVelocity") 
					&& !singleScriptLine.contains("setTorsoVelocity") 
					&& !singleScriptLine.contains("moveHead") 
					&& !singleScriptLine.contains("moveArm") 
					&& !singleScriptLine.contains("moveHand") 
					&& !singleScriptLine.contains("moveTorso") 
					&& !singleScriptLine.contains("sleep") // end frame
					&& !singleScriptLine.contains("finishedGesture")) { // end gesture
				continue;
			}
			/// at this point we have frame command
			if(singleScriptLine.contains("finishedGesture")) {
				// we are finished
				return fihList;
			}
			if(singleScriptLine.contains("speech")) {
				// ignore
				continue;
			}
			if(singleScriptLine.contains("sleep")) {
				// sleep means the end of the frame
				try {
					// parse the frame and add it
					parseScriptFragmentIntoSingleFrame(fihList, frameLines, counter); 
					// finish it with a sleep
					parseScriptSleepToFrameSleep(fihList, singleScriptLine); 
				}catch(Exception e){
					log.error("Exception from function parseScriptFragmentIntoSingleFrame: " + e);
				}
			}
			frameLines.add(singleScriptLine);
		}
		return fihList;
	}

	private void parseScriptFragmentIntoSingleFrame(List<FrameItemHolder> fihList, 
			List<String> frameLines, int frameCounter) throws Exception {
		FrameItemHolder fihSpeed = new FrameItemHolder();
		fihSpeed.setSpeech(null);
		fihSpeed.setName(null);
		FrameItemHolder fihMove = new FrameItemHolder();
		fihMove.setName("Frame#"+frameCounter);
		boolean addSpeed = false;
		boolean addMove = false;

		for(String singleScriptLine : frameLines) {
			// it always starts with 'i01.'
//			i01.setHeadSpeed(0.95,0.95)
			singleScriptLine = singleScriptLine.substring(3, singleScriptLine.length()-1);
//			setHeadSpeed(0.95,0.95
			String[] splitString = singleScriptLine.split("(");
			// splitString[0] setHeadSpeed
			// splitString[1] 0.95,0.95
			String[] valuesString = splitString[1].split(",");
			log.info(singleScriptLine);
			log.info(splitString.toString());
			log.info(valuesString.toString());
			if(splitString[0].contains("Speed")) { 
				addSpeed = true;
				if(splitString[0].contains("Head")) {
//					setHeadSpeed(0.95,0.95)
					fihSpeed.setRotheadspeed(Double.parseDouble(valuesString[0].trim()));
					fihSpeed.setNeckspeed(Double.parseDouble(valuesString[1].trim()));
				} else if(splitString[0].contains("Torso")) { 
//					setTorsoSpeed(0.95,0.85,1.0)
					fihSpeed.setTopStomspeed(Double.parseDouble(valuesString[0].trim()));
					fihSpeed.setMidStomspeed(Double.parseDouble(valuesString[1].trim()));
					fihSpeed.setLowStomspeed(Double.parseDouble(valuesString[2].trim()));
				} else if(splitString[0].contains("Arm")) { 
					if(valuesString[0].contains("left")) {
//						setArmSpeed("left",1.0,0.85,0.95,0.95)
					} else if(valuesString[0].contains("right")) { 
//						setArmSpeed("right",0.65,0.85,0.65,0.85)
					}
				} else if(splitString[0].contains("Hand")) { 
					if(valuesString[0].contains("left")) {
//						setHandSpeed("left",0.85,0.85,0.85,0.85,0.85,0.85)
					} else if(valuesString[0].contains("right")) { 
//						setHandSpeed("right",0.85,0.85,0.85,0.85,0.85,0.85)
					}
				} else { 
					
				}
			} else if(splitString[0].contains("move")) { 
				addMove = true;
				if(splitString[0].contains("Head")) {
//				  moveHead(79,100,82,78,65)
				} else if(splitString[0].contains("Arm")) { 
//				  moveArm("left",5,84,28,15)
//				  moveArm("right",5,82,28,15)
				} else if(splitString[0].contains("Hand")) { 
//				  moveHand("left",92,33,37,71,66,25)
//				  moveHand("right",81,66,82,60,105,113)
				} else if(splitString[0].contains("Torso")) { 
//				  moveTorso(90,90,90)
				} else { 
					
				}
			} else { 
				// we should never get here
			}
		}
		if(addSpeed) {
			fihList.add(fihSpeed);
		}
		if(addMove) {
			fihList.add(fihMove);
		}
	}
	
	private void parseScriptSleepToFrameSleep(List<FrameItemHolder> fihList, String sleepLine) {
		//sleep line:
		//  sleep(3) !!note the 2 spaces at the beginning
		
		int sleepTime = Integer.parseInt(sleepLine.substring(sleepLine.indexOf('('), sleepLine.indexOf(')')));
		FrameItemHolder fih = new FrameItemHolder();
		fih.resetValues();
		
		fih.setName(null); //sleep frame has Name and Speech as null and Sleep as int
		fih.setSpeech(null);
		fih.setSleep(sleepTime);
		
		fihList.add(fih);
	}
	
	private FrameItemHolder parseScriptFragmentIntoSingleFrameAbe(List<String> frameLines) throws Exception{
		
		FrameItemHolder fih = new FrameItemHolder();
		
		for(String singleScriptLine : frameLines) {
			if(singleScriptLine.contains("setHeadVelocity")) { //Head Velocity i01.setHeadVelocity(Double rothead, Double neck)
				if(StringUtils.countMatches(singleScriptLine, ",") == 1) {
					//good parameters
					fih.setRotheadspeed(Double.parseDouble(singleScriptLine.substring(singleScriptLine.indexOf('('),singleScriptLine.indexOf(',')))); // Integer.parseInt(
					fih.setNeckspeed(Double.parseDouble(singleScriptLine.substring(singleScriptLine.indexOf(','),singleScriptLine.indexOf(')'))));
				}
				else {
					throw new Exception("Number of parameters for setHeadVelocity is inapropriate!");
				}
			}
			else if(singleScriptLine.contains("setArmVelocity")) { // setArmVelocity(String which, Double bicep, Double rotate, Double shoulder, Double omoplate)
				if(StringUtils.countMatches(singleScriptLine, ",") == 4) {
					//good parameters
					int lastCommaPosition = 0, commaPosition = singleScriptLine.indexOf(',');
					if(singleScriptLine.substring(singleScriptLine.indexOf('('),commaPosition) == "left") {
						//here we set the attributes for the left arm
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLbicepspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//bicep is the first argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLrotatespeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//rotate is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLshoulderspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//shoulder is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(')',lastCommaPosition); // we need the position of )
						fih.setLomoplatespeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//omoplate is the third argument
					}
					else if(singleScriptLine.substring(singleScriptLine.indexOf('('),singleScriptLine.indexOf(',')) == "right") {
						//here we set the attributes for the right arm
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRbicepspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//bicep is the first argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRrotatespeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//rotate is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRshoulderspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//shoulder is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(')',lastCommaPosition); // we need the position of )
						fih.setRomoplatespeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//omoplate is the third argument
						
					}
					else {
						throw new Exception("Which arm atribute for setArmVelocity is inapropriate!"); // not left or right
					}
				}
				else {
					throw new Exception("Number of parameters for setArmVelocity is inapropriate!");
				}				
			}
			else if(singleScriptLine.contains("setHandVelocity")) { // i01.setHandVelocity(String which, Double thumb, Double index, Double majeure, Double ringFinger, Double pinky, Double wrist) 
				if(StringUtils.countMatches(singleScriptLine, ",") == 6) {
					//good parameters
					int lastCommaPosition = 0, commaPosition = singleScriptLine.indexOf(',');
					if(singleScriptLine.substring(singleScriptLine.indexOf('('),singleScriptLine.indexOf(',')) == "left") {
						//here we set the attributes for the left hand
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLthumbspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//thumb is the first argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLindexspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//index is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLmajeurespeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//majeure is the fourth argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of )
						fih.setLringfingerspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//ring is the fifth argument	
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of )
						fih.setLpinkyspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//pinky is the sixth argument		
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(')',lastCommaPosition); // we need the position of )
						fih.setLwristspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//wrist is the seventh argument	
					}
					else if(singleScriptLine.substring(singleScriptLine.indexOf('('),singleScriptLine.indexOf(',')) == "right") {
						//here we set the attributes for the right hand
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRthumbspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//thumb is the first argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRindexspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//index is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRmajeurespeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//majeure is the fourth argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of )
						fih.setRringfingerspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//ring is the fifth argument	
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of )
						fih.setRpinkyspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//pinky is the sixth argument		
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(')',lastCommaPosition); // we need the position of )
						fih.setRwristspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//wrist is the seventh argument	
					}
					else {
						throw new Exception("Which arm atribute for setHandVelocity is inapropriate!"); // not left or right
					}
				}
				else {
					throw new Exception("Number of parameters for setHandVelocity is inapropriate!");
				}				
			}
			else if(singleScriptLine.contains("setTorsoVelocity")) { // i01.setTorsoVelocity(Double topStom, Double midStom, Double lowStom) 
				int lastCommaPosition = 0, commaPosition = singleScriptLine.indexOf(',');
				if(StringUtils.countMatches(singleScriptLine, ",") == 2) {
					//good parameters
					//here we set the attributes for the torso
					fih.setTopStomspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//topstom is the first argument
					lastCommaPosition = commaPosition;
					commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
					fih.setMidStomspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//midstom is the second argument
					lastCommaPosition = commaPosition;
					commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
					fih.setLowStomspeed(Double.parseDouble(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//lowstom is the fourth argument
				}
				else {
					throw new Exception("Number of parameters for setTorsoVelocity is inapropriate!");
				}				
			}
			else if(singleScriptLine.contains("moveHead")) { //  i01.moveHead(double neck, double rothead, double eyeX, double eyeY, double jaw)
				int lastCommaPosition = 0, commaPosition = singleScriptLine.indexOf(',');
				if(StringUtils.countMatches(singleScriptLine, ",") == 4) {
					//good parameters
					//here we set the attributes for thehead 
					fih.setNeck(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//neck is the first argument
					lastCommaPosition = commaPosition;
					commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
					fih.setRothead(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//rothead is the second argument
					lastCommaPosition = commaPosition;
					commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
					fih.setEyeX(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//eyex is the fourth argument
					lastCommaPosition = commaPosition;
					commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
					fih.setEyeY(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//eyeY is the fifth argument
					lastCommaPosition = commaPosition;
					commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
					fih.setJaw(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//jawa is the sixth
				}
				else {
					throw new Exception("Number of parameters for moveHead is inapropriate!");
				}				
			}
			else if(singleScriptLine.contains("moveArm")) { // i01.moveArm(String which, double bicep, double rotate, double shoulder, double omoplate)
				if(StringUtils.countMatches(singleScriptLine, ",") == 4) {
					//good parameters
					int lastCommaPosition = 0, commaPosition = singleScriptLine.indexOf(',');
					if(singleScriptLine.substring(singleScriptLine.indexOf('('),singleScriptLine.indexOf(',')) == "left") {
						//here we set the attributes for the left arm
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLbicep(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//bicep is the first argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLrotate(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//rotate is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLshoulder(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//shoulder is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(')',lastCommaPosition); // we need the position of )
						fih.setLomoplate(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//omoplate is the third argument
					}
					else if(singleScriptLine.substring(singleScriptLine.indexOf('('),singleScriptLine.indexOf(',')) == "right") {
						//here we set the attributes for the right arm 
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRbicep(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//bicep is the first argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRrotate(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//rotate is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRshoulder(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//shoulder is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(')',lastCommaPosition); // we need the position of )
						fih.setRomoplate(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//omoplate is the third argument
					}
					else {
						throw new Exception("Which arm atribute for moveArm is inapropriate!"); // not left or right
					}
				}
				else {
					throw new Exception("Number of parameters for moveArm is inapropriate!");
				}				
			}
			else if(singleScriptLine.contains("moveHand")) { // i01.moveHand(String which, double thumb, double index, double majeure, double ringFinger, double pinky, Double wrist)
				if(StringUtils.countMatches(singleScriptLine, ",") == 6) {
					//good parameters
					int lastCommaPosition = 0, commaPosition = singleScriptLine.indexOf(',');
					if(singleScriptLine.substring(singleScriptLine.indexOf('('),singleScriptLine.indexOf(',')) == "left") {
						//here we set the attributes for the left hand
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLthumb(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//thumb is the first argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLindex(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//index is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setLmajeure(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//majeure is the fourth argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of )
						fih.setLringfinger(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//ring is the fifth argument	
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of )
						fih.setLpinky(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//pinky is the sixth argument		
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(')',lastCommaPosition); // we need the position of )
						fih.setLwrist(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//wrist is the seventh argument	
					}
					else if(singleScriptLine.substring(singleScriptLine.indexOf('('),singleScriptLine.indexOf(',')) == "right") {
						//here we set the attributes for the right hand
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRthumb(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//thumb is the first argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRindex(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//index is the third argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
						fih.setRmajeure(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//majeure is the fourth argument
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of )
						fih.setRringfinger(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//ring is the fifth argument	
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of )
						fih.setRpinky(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//pinky is the sixth argument		
						lastCommaPosition = commaPosition;
						commaPosition = singleScriptLine.indexOf(')',lastCommaPosition); // we need the position of )
						fih.setRwrist(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
						//wrist is the seventh argument	
					}
					else {
						throw new Exception("Which arm atribute for moveHand is inapropriate!"); // not left or right
					}
				}
				else {
					throw new Exception("Number of parameters for moveHand is inapropriate!");
				}				
			}
			else if(singleScriptLine.contains("moveTorso")) { // i01.moveTorso(double topStom, double midStom, double lowStom)
				int lastCommaPosition = 0, commaPosition = singleScriptLine.indexOf(',');
				if(StringUtils.countMatches(singleScriptLine, ",") == 2) {
					//good parameters
					//here we set the attributes for the torso
					fih.setTopStom(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//topstom is the first argument
					lastCommaPosition = commaPosition;
					commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
					fih.setMidStom(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//midstom is the second argument
					lastCommaPosition = commaPosition;
					commaPosition = singleScriptLine.indexOf(',',lastCommaPosition); // we need the position of the next comma!
					fih.setLowStom(Integer.parseInt(singleScriptLine.substring(lastCommaPosition, commaPosition)));
					//lowstom is the fourth argument
				}
				else {
					throw new Exception("Number of parameters for moveTorso is inapropriate!");
				}				
			}
			//if line has a command that is not covered by these cases, just skip it!
		}
				
//		frameitemholder.add(fih);
//		framelistact(frameListGlobal);
		return fih;
	}
 
  public void parse_frame_to_script() {
	  String code = "def " + /*ime_funkcije*/"test" + "():\n  i01.startedGesture()\n  "; //def + ime plus () + enter i dva spejsa + i01.
	  //treba reci da se prvo dodaje brzina pa onda frame
	  for (int i = 0; i < frameitemholder.size(); i++) {
	      FrameItemHolder fih = frameitemholder.get(i);
	      
	      if(fih.getName() == null && fih.getSleep() == -1) {
	    	  String speeds[] = {"","","","","",""};
	    	  if(tabs_main_checkbox_states[0])	speeds[0] = "i01.setHeadVelocity(" + fih.getRotheadspeed() + "," + fih.getNeckspeed() + ")";
	    	  if(tabs_main_checkbox_states[1])	speeds[1] = "i01.setArmVelocity(\"left\"," + fih.getLbicepspeed() + "," + fih.getLrotatespeed() + "," + fih.getLshoulderspeed() + "," + fih.getLomoplatespeed() + ")";
	    	  if(tabs_main_checkbox_states[2])	speeds[2] = "i01.setArmVelocity(\"right\"," + fih.getRbicepspeed() + "," + fih.getRrotatespeed() + "," + fih.getRshoulderspeed() + "," + fih.getRomoplatespeed() + ")";
	    	  if(tabs_main_checkbox_states[3])	speeds[3] = "i01.setHandVelocity(\"left\"," + fih.getLthumbspeed() + "," + fih.getLpinkyspeed() + "," + fih.getLmajeurespeed() + "," + fih.getLringfingerspeed() + "," + fih.getLpinkyspeed() + "," + fih.getLwristspeed() +")";
	    	  if(tabs_main_checkbox_states[4])	speeds[4] = "i01.setHandVelocity(\"right\"," + fih.getRthumbspeed() + "," + fih.getRpinkyspeed() + "," + fih.getRmajeurespeed() + "," + fih.getRringfingerspeed() + "," + fih.getRpinky() + "," + fih.getRwristspeed() +")";
	    	  if(tabs_main_checkbox_states[5])	speeds[5] = "i01.setTorsoVelocity(" + fih.getTopStomspeed() + "," + fih.getMidStomspeed() + "," + fih.getLowStomspeed() + ")";
	    	  for(int j = 0; j <= 5; j++) {
    			  if(tabs_main_checkbox_states[j]) 
    				  code += (speeds[j] + "\n  ");
	    	  }
	      }else if(fih.getName() == null && fih.getSleep() != -1){
	    	  code += "sleep(" + fih.getSleep() + ")\n  ";
	      }else {
	    	  String movements[] = {"","","","","",""};
	    	  if(tabs_main_checkbox_states[0])	movements[0] = "i01.moveHead(" + fih.getNeck() + "," + fih.getRothead() + "," + fih.getEyeX() + "," + fih.getEyeY() + "," + fih.getJaw() + ")";
	    	  if(tabs_main_checkbox_states[1])	movements[1] = "i01.moveArm(\"left\"," + fih.getLbicep() + "," + fih.getLrotate() + "," + fih.getLshoulder() + "," + fih.getLomoplate() + ")";
	    	  if(tabs_main_checkbox_states[2])	movements[2] = "i01.moveArm(\"right\"," + fih.getRbicep() + "," + fih.getRrotate() + "," + fih.getRshoulder() + "," + fih.getRomoplate() + ")";
	    	  if(tabs_main_checkbox_states[3])	movements[3] = "i01.moveHand(\"left\"," + fih.getLthumb() + "," + fih.getLpinky() + "," + fih.getLmajeure() + "," + fih.getLringfinger() + "," + fih.getLpinky() + "," + fih.getLwrist() +")";
	    	  if(tabs_main_checkbox_states[4])	movements[4] = "i01.moveHand(\"right\"," + fih.getRthumb() + "," + fih.getRpinky() + "," + fih.getRmajeure() + "," + fih.getRringfinger() + "," + fih.getRpinky() + "," + fih.getRwrist() +")";
	    	  if(tabs_main_checkbox_states[5])	movements[5] = "i01.moveTorso(" + fih.getTopStom() + "," + fih.getMidStom() + "," + fih.getLowStom() + ")";
	    	  for(int j = 0; j <= 5; j++) {
    			  if(tabs_main_checkbox_states[j]) 
    				  code += (movements[j] + "\n  ");
	    	  }	    	 
	      }
	  }
	  //code += "i01.finishedGesture()";
	  parsirani_kod = code;
  }
 
  public void frame_add(JList framelist, JTextField frame_add_textfield) {
	    // Add a servo movement frame to the framelist (button bottom-right)
	    FrameItemHolder fih = new FrameItemHolder();

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

	    frameitemholder.add(fih);

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
      FrameItemHolder fih = new FrameItemHolder();

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

      if (tabs_main_checkbox_states[0]) {
        i01.moveHead(fih.getNeck(), fih.getRothead(), fih.getEyeX(), fih.getEyeY(), fih.getJaw());
      }
      if (tabs_main_checkbox_states[1]) {
        i01.moveArm("left", fih.getLbicep(), fih.getLrotate(), fih.getLshoulder(), fih.getLomoplate());
      }
      if (tabs_main_checkbox_states[2]) {
        i01.moveArm("right", fih.getRbicep(), fih.getRrotate(), fih.getRshoulder(), fih.getRomoplate());
      }
      if (tabs_main_checkbox_states[3]) {
        i01.moveHand("left", fih.getLthumb(), fih.getLindex(), fih.getLmajeure(), fih.getLringfinger(), fih.getLpinky(), (double) fih.getLwrist());
      }
      if (tabs_main_checkbox_states[4]) {
        i01.moveHand("right", fih.getRthumb(), fih.getRindex(), fih.getRmajeure(), fih.getRringfinger(), fih.getRpinky(), (double) fih.getRwrist());
      }
      if (tabs_main_checkbox_states[5]) {
        i01.moveTorso(fih.getTopStom(), fih.getMidStom(), fih.getLowStom());
      }
    }
  }

  public void tabs_main_checkbox_states_changed(boolean[] tabs_main_checkbox_states2) {
    // checkbox states (on the main site) (for the services) changed
    tabs_main_checkbox_states = tabs_main_checkbox_states2;
  }

  /**
   * This static method returns all the details of the class without it having
   * to be constructed. It has description, categories, dependencies, and peer
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

}
