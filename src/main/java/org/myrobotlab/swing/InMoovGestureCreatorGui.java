package org.myrobotlab.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.InMoovGestureCreator;
import org.myrobotlab.service.SwingGui;
import org.myrobotlab.service.model.Frame.RobotSection;
import org.slf4j.Logger;

/**
 * based on _TemplateServiceGUI
 */
/**
 *
 * @author LunDev (github), Ma. Vo. (MyRobotlab)
 */
public class InMoovGestureCreatorGui extends ServiceGui implements ActionListener, ItemListener {

  static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(InMoovGestureCreatorGui.class);

  JTextField control_gestname;
  JTextField control_funcname;

  JButton control_ScriptFolder;
  JButton control_connect;
  JButton control_loadscri;
  JButton control_savescri;
  JButton control_loadgest;
  JButton control_addgest;
  JButton control_updategest;
  JButton control_removegest;
  JButton control_testgest;

  JList control_list;

  JFormattedTextField frameNameTextField;
  JButton frame_add;
  JButton frame_addspeed;
  JFormattedTextField frameSleepTextField;
  JButton frame_addsleep;
  JFormattedTextField frameSpeechTextField;
  JButton frame_addspeech;

  JButton frame_importminresmax;
  JButton frame_remove;
  JButton frame_load;
  JButton frame_update;
  JButton frame_copy;
  JButton frame_up;
  JButton frame_down;
  JButton frame_test;
  JCheckBox frame_moverealtime;

  JList frameList;

	public InMoovGestureCreatorGui(final String boundServiceName, final SwingGui myService) {
		super(boundServiceName, myService);
		LOGGER.info("InMoovGestureCreatorGui constructor [START]");
		try {

			// display:
			// |--------------------|
			// |####################|
			// |########top#########|
			// |####################|
			// |--------------------| <- splitpanetopbottom
			// |######bottom########|
			// |####################|
			// |--------------------|

			// top:
			// |--------------------|
			// |top#| ##top   2#####|
			// |# 1#| ##############|
			// |--------------------|
			// ######/\
			// splitpanetop1top2

			// top1:
			// |----------|
			// |top1top| <- JTextField's: gestname, funcname & JButton: connect
			// |----------|
			// |##########| <- JList: control_list & JButton's: loadscript,
			// savescript, loadgest, addgest, updategest, removegest, testgest
			// |##########|
			// |----------|

			// top2:
			// |----------|
			// |   top2top| <- JButton's & JTextField's: [frame_] add, addspeed,
			// addsleep, addspeech
			// |##########| <- JButton's: [frame_] importminresmax, remove, load,
			// update, copy, up, down, test & JCheckBox: Move Real Time
			// |----------|
			// |##########|
			// |##########| <- JList: framelist
			// |##########|
			// |----------|



			JPanel top = new JPanel();

			JPanel topLeft = new JPanel();
			topLeft.setLayout(new BorderLayout());

			JPanel topLefttop = new JPanel();
			topLefttop.setLayout(new BoxLayout(topLefttop, BoxLayout.X_AXIS));

			control_gestname = new JTextField("Gest. Name");
			topLefttop.add(control_gestname);

			control_funcname = new JTextField("Func. Name");
			topLefttop.add(control_funcname);

			control_connect = new JButton("Connect");
			topLefttop.add(control_connect);
			control_connect.addActionListener(this);

			control_ScriptFolder = new JButton("Scri Fldr");
			topLefttop.add(control_ScriptFolder);
			control_ScriptFolder.addActionListener(this);

			topLeft.add(BorderLayout.NORTH, topLefttop);

			JPanel topLeftright = new JPanel();
			topLeftright.setLayout(new BoxLayout(topLeftright, BoxLayout.Y_AXIS));

			control_loadscri = new JButton("Load Scri");
			topLeftright.add(control_loadscri);
			control_loadscri.addActionListener(this);

			control_savescri = new JButton("Save Scri");
			topLeftright.add(control_savescri);
			control_savescri.addActionListener(this);

			control_loadgest = new JButton("Load Gest");
			topLeftright.add(control_loadgest);
			control_loadgest.addActionListener(this);

			control_addgest = new JButton("Add Gest");
			topLeftright.add(control_addgest);
			control_addgest.addActionListener(this);

			control_updategest = new JButton("Update Gest");
			topLeftright.add(control_updategest);
			control_updategest.addActionListener(this);

			control_removegest = new JButton("Remove Gest");
			topLeftright.add(control_removegest);
			control_removegest.addActionListener(this);

			control_testgest = new JButton("Test Gest");
			topLeftright.add(control_testgest);
			control_testgest.addActionListener(this);

			topLeft.add(BorderLayout.EAST, topLeftright);
			
			String[] te1 = { "Load folder with scripts" };
			control_list = new JList(te1);
			control_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			JScrollPane control_listscroller = new JScrollPane(control_list);
			control_listscroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			control_listscroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			topLeft.add(BorderLayout.CENTER, control_listscroller);

			JPanel topRight = new JPanel();
			topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));

//			JPanel top2top = new JPanel();
//			top2top.setLayout(new BoxLayout(top2top, BoxLayout.Y_AXIS));
//
//
//			top2top.add(top2top1);

			JPanel top2top2 = new JPanel();
			top2top2.setLayout(new BoxLayout(top2top2, BoxLayout.X_AXIS));

			frame_importminresmax = new JButton("Import Min Rest Max");
			top2top2.add(frame_importminresmax);
			frame_importminresmax.addActionListener(this);

			frame_remove = new JButton("Remove");
			top2top2.add(frame_remove);
			frame_remove.addActionListener(this);

			frame_load = new JButton("Load");
			top2top2.add(frame_load);
			frame_load.addActionListener(this);

			frame_update = new JButton("Update");
			top2top2.add(frame_update);
			frame_update.addActionListener(this);

			frame_copy = new JButton("Copy");
			top2top2.add(frame_copy);
			frame_copy.addActionListener(this);

			frame_up = new JButton("Up");
			top2top2.add(frame_up);
			frame_up.addActionListener(this);

			frame_down = new JButton("Down");
			top2top2.add(frame_down);
			frame_down.addActionListener(this);

			frame_test = new JButton("Test");
			top2top2.add(frame_test);
			frame_test.addActionListener(this);

			frame_moverealtime = new JCheckBox("Move Real Time");
			frame_moverealtime.setSelected(false);
			top2top2.add(frame_moverealtime);
			frame_moverealtime.addItemListener(this);

//			top2top.add(top2top2);

			topRight.add(BorderLayout.NORTH, top2top2);

			String[] te2 = {"Load a script to see frames..."}; 

			frameList = new JList(te2);
			frameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			JPanel bottom = new JPanel();
			bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
			JPanel bottomTop = new JPanel();
			Dimension maximumSize = new Dimension(Integer.MAX_VALUE, 30);
			bottomTop.setMaximumSize(maximumSize);
			bottomTop.setLayout(new BoxLayout(bottomTop, BoxLayout.X_AXIS));			
			JPanel bottomBottom = new JPanel();
			bottomBottom.setLayout(new BoxLayout(bottomBottom, BoxLayout.X_AXIS));

			// main 6 panels for robot sections
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
			// borders
			rightHandPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
			rightArmPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
			leftHandPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
			leftArmPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
			headPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
			torsoPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
			//
//			bottom.removeAll();
			bottomBottom.add(rightHandPanel);
			bottomBottom.add(rightArmPanel);
			bottomBottom.add(leftHandPanel);
			bottomBottom.add(leftArmPanel);
			bottomBottom.add(headPanel);
			bottomBottom.add(torsoPanel);
//			bottom.revalidate();
//			bottom.repaint();
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
			final Map<RobotSection, JPanel> robotSectionMovePanels = new HashMap<RobotSection, JPanel>();
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
			final Map<RobotSection, JPanel> robotSectionSlidersPanels = new HashMap<RobotSection, JPanel>();
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
			torsoSpeedPanel.setLayout(new BoxLayout(torsoSpeedPanel, BoxLayout.Y_AXIS));
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
			// add to map
			final Map<RobotSection, JPanel> robotSectionSpeedPanels = new HashMap<RobotSection, JPanel>();
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
			final Map<RobotSection, JPanel> robotSectionSpeedNumberBoxesPanels = new HashMap<RobotSection, JPanel>();
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.RIGHT_HAND, rightHandSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.RIGHT_ARM, rightArmSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.LEFT_HAND, leftHandSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.LEFT_ARM, leftArmSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.HEAD, headSpeedNumberBoxesPanel);
			robotSectionSpeedNumberBoxesPanels.put(RobotSection.TORSO, torsoSpeedNumberBoxesPanel);
			
			JScrollPane frameListScroller = new JScrollPane(frameList);
			frameListScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			frameListScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			topRight.add(BorderLayout.CENTER, frameListScroller);
			
			frameList.addListSelectionListener(new ListSelectionListener() {
	            @Override
	            public void valueChanged(ListSelectionEvent arg0) {
	                if (!arg0.getValueIsAdjusting()) {
	                	myService.send(boundServiceName, "addBottomTopPane", 
	                			bottomTop,
	                			frameNameTextField,
	                			frameSleepTextField, 
	                			frameSpeechTextField, 
	                			frameList.getSelectedIndex());			
	                	myService.send(boundServiceName, "frameSelectionChanged", 
	                			top,
	                			robotSectionMovePanels, 
	                			robotSectionSlidersPanels,
	                			robotSectionSpeedPanels,
	                			robotSectionSpeedNumberBoxesPanels, 
	                			frameList.getSelectedIndex());
	                }
	            }
	        });
			bottom.add(bottomTop);
			bottom.add(bottomBottom);

			JSplitPane splitPaneTopLeftTopRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topLeft, topRight);
			splitPaneTopLeftTopRight.setOneTouchExpandable(true);
			// splitpanebottom1bottom2.setDividerLocation(200);

			top.add(splitPaneTopLeftTopRight);

			JSplitPane splitPaneTopBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
			splitPaneTopBottom.setOneTouchExpandable(true);
			// splitpanetopbottom.setDividerLocation(300);

			display.add(splitPaneTopBottom);
		} catch (Exception e) {
			LOGGER.warn("Exception occured", e);
		}

		LOGGER.info("InMoovGestureCreatorGui constructor [END]");
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();

		// Button - Events
		if (o == control_connect) {
			swingGui.send(boundServiceName, "control_connect", control_connect);
		} else if (o == control_ScriptFolder) {
			swingGui.send(boundServiceName, "control_ScriptFolder", control_list);
		} else if (o == control_loadscri) {
			swingGui.send(boundServiceName, "control_loadscri", control_list, frameList);
		} else if (o == control_savescri) {
			swingGui.send(boundServiceName, "control_savescri");
		} else if (o == control_loadgest) {
			swingGui.send(boundServiceName, "control_loadgest", control_list, frameList, control_gestname,
					control_funcname);
		} else if (o == control_addgest) {
			swingGui.send(boundServiceName, "control_addgest", control_list, control_gestname, control_funcname);
		} else if (o == control_updategest) {
			swingGui.send(boundServiceName, "control_updategest", control_list, control_gestname, control_funcname);
		} else if (o == control_removegest) {
			swingGui.send(boundServiceName, "control_removegest", control_list);
		} else if (o == control_testgest) {
			swingGui.send(boundServiceName, "control_testgest");
		} else if (o == frame_add) {
			swingGui.send(boundServiceName, "frame_add", frameList, frameNameTextField);
		} else if (o == frame_addspeed) {
			swingGui.send(boundServiceName, "frame_addspeed", frameList);
		} else if (o == frame_addsleep) {
			swingGui.send(boundServiceName, "frame_addsleep", frameList, frameSleepTextField);
		} else if (o == frame_addspeech) {
			swingGui.send(boundServiceName, "frame_addspeech", frameList, frameSpeechTextField);
		} else if (o == frame_importminresmax) {
			swingGui.send(boundServiceName, "frame_importminresmax");
		} else if (o == frame_remove) {
			swingGui.send(boundServiceName, "frame_remove", frameList);
		} else if (o == frame_load) {
			swingGui.send(boundServiceName, "frame_load", frameList, frameNameTextField, frameSleepTextField,
					frameSpeechTextField);
		} else if (o == frame_update) {
			swingGui.send(boundServiceName, "frame_update", frameList, frameNameTextField, frameSleepTextField,
					frameSpeechTextField);
		} else if (o == frame_copy) {
			swingGui.send(boundServiceName, "frame_copy", frameList);
		} else if (o == frame_up) {
			swingGui.send(boundServiceName, "frame_up", frameList);
		} else if (o == frame_down) {
			swingGui.send(boundServiceName, "frame_down", frameList);
		} else if (o == frame_test) {
			swingGui.send(boundServiceName, "frame_test", frameList);
		}
		swingGui.send(boundServiceName, "publishState");
	}

	@Override
	public void subscribeGui() {
		// commented out subscription due to this class being used for
		// un-defined gui's

		// subscribe("publishState", "onState", _TemplateService.class);
		// send("publishState");
	}

	@Override
	public void unsubscribeGui() {
		// commented out subscription due to this class being used for
		// un-defined gui's

		// unsubscribe("publishState", "onState", _TemplateService.class);
	}

	public void onState(final InMoovGestureCreator inMoovGestureCreator) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

			}
		});
	}

	@Override
	public void itemStateChanged(ItemEvent ie) {
		Object o = ie.getSource();
		// CheckBox - Events
		if (o == frame_moverealtime) {
			swingGui.send(boundServiceName, "frame_moverealtime", frame_moverealtime);
		}
	}
}