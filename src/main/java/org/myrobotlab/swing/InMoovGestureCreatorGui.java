package org.myrobotlab.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.myrobotlab.service.model.Frame;
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
  public final static Logger log = LoggerFactory.getLogger(InMoovGestureCreatorGui.class);

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

  JTextField frame_add_textfield;
  JButton frame_add;
  JButton frame_addspeed;
  JTextField frame_addsleep_textfield;
  JButton frame_addsleep;
  JTextField frame_addspeech_textfield;
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
		log.info("InMoovGestureCreatorGui constructor [START]");
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


			JPanel bottom = new JPanel();
			Frame frame = new Frame(Frame.FrameType.SLEEP);
			myService.send(boundServiceName, "initializeBottomPaneTabs", bottom, frame);
//			initializeBottomPaneTabs(bottom, myService);

			JPanel top = new JPanel();

			JPanel top1 = new JPanel();
			top1.setLayout(new BorderLayout());

			JPanel top1top = new JPanel();
			top1top.setLayout(new BoxLayout(top1top, BoxLayout.X_AXIS));

			control_gestname = new JTextField("Gest. Name");
			top1top.add(control_gestname);

			control_funcname = new JTextField("Func. Name");
			top1top.add(control_funcname);

			control_connect = new JButton("Connect");
			top1top.add(control_connect);
			control_connect.addActionListener(this);

			control_ScriptFolder = new JButton("Scri Fldr");
			top1top.add(control_ScriptFolder);
			control_ScriptFolder.addActionListener(this);

			top1.add(BorderLayout.NORTH, top1top);

			JPanel top1right = new JPanel();
			top1right.setLayout(new BoxLayout(top1right, BoxLayout.Y_AXIS));

			control_loadscri = new JButton("Load Scri");
			top1right.add(control_loadscri);
			control_loadscri.addActionListener(this);

			control_savescri = new JButton("Save Scri");
			top1right.add(control_savescri);
			control_savescri.addActionListener(this);

			control_loadgest = new JButton("Load Gest");
			top1right.add(control_loadgest);
			control_loadgest.addActionListener(this);

			control_addgest = new JButton("Add Gest");
			top1right.add(control_addgest);
			control_addgest.addActionListener(this);

			control_updategest = new JButton("Update Gest");
			top1right.add(control_updategest);
			control_updategest.addActionListener(this);

			control_removegest = new JButton("Remove Gest");
			top1right.add(control_removegest);
			control_removegest.addActionListener(this);

			control_testgest = new JButton("Test Gest");
			top1right.add(control_testgest);
			control_testgest.addActionListener(this);

			top1.add(BorderLayout.EAST, top1right);
			
			String[] te1 = { "Load folder with scripts" };
			control_list = new JList(te1);
			control_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			JScrollPane control_listscroller = new JScrollPane(control_list);
			control_listscroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			control_listscroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			top1.add(BorderLayout.CENTER, control_listscroller);

			JPanel top2 = new JPanel();
			top2.setLayout(new BoxLayout(top2, BoxLayout.Y_AXIS));

			JPanel top2top = new JPanel();
			top2top.setLayout(new BoxLayout(top2top, BoxLayout.Y_AXIS));

			JPanel top2top1 = new JPanel();
			top2top1.setLayout(new BoxLayout(top2top1, BoxLayout.X_AXIS));

			frame_add_textfield = new JTextField("Frame-Name");
			top2top1.add(frame_add_textfield);

			frame_add = new JButton("Add");
			top2top1.add(frame_add);
			frame_add.addActionListener(this);

			frame_addspeed = new JButton("Add Speed");
			top2top1.add(frame_addspeed);
			frame_addspeed.addActionListener(this);

			frame_addsleep_textfield = new JTextField("Seconds of Sleep");
			top2top1.add(frame_addsleep_textfield);

			frame_addsleep = new JButton("Add Sleep");
			top2top1.add(frame_addsleep);
			frame_addsleep.addActionListener(this);

			frame_addspeech_textfield = new JTextField("Speech");
			top2top1.add(frame_addspeech_textfield);

			frame_addspeech = new JButton("Add Speech");
			top2top1.add(frame_addspeech);
			frame_addspeech.addActionListener(this);

			top2top.add(top2top1);

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

			top2top.add(top2top2);

			top2.add(BorderLayout.NORTH, top2top);

			String[] te2 = {"Load a script to see frames..."}; 

			frameList = new JList(te2);
			frameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			frameList.addListSelectionListener(new ListSelectionListener() {
	            @Override
	            public void valueChanged(ListSelectionEvent arg0) {
	                if (!arg0.getValueIsAdjusting()) {
	                	myService.send(boundServiceName, "frameSelectionChanged", 
	                			bottom, frameList.getSelectedIndex());
	                }
	            }
	        });

			JScrollPane framelistscroller = new JScrollPane(frameList);
			framelistscroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			framelistscroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			top2.add(BorderLayout.CENTER, framelistscroller);

			JSplitPane splitpanetop1top2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, top1, top2);
			splitpanetop1top2.setOneTouchExpandable(true);
			// splitpanebottom1bottom2.setDividerLocation(200);

			top.add(splitpanetop1top2);

			JSplitPane splitpanetopbottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
			splitpanetopbottom.setOneTouchExpandable(true);
			// splitpanetopbottom.setDividerLocation(300);

			display.add(splitpanetopbottom);
		} catch (Exception e) {
			log.warn("Exception occured", e);
		}

		log.info("InMoovGestureCreatorGui constructor [END]");
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
			swingGui.send(boundServiceName, "frame_add", frameList, frame_add_textfield);
		} else if (o == frame_addspeed) {
			swingGui.send(boundServiceName, "frame_addspeed", frameList);
		} else if (o == frame_addsleep) {
			swingGui.send(boundServiceName, "frame_addsleep", frameList, frame_addsleep_textfield);
		} else if (o == frame_addspeech) {
			swingGui.send(boundServiceName, "frame_addspeech", frameList, frame_addspeech_textfield);
		} else if (o == frame_importminresmax) {
			swingGui.send(boundServiceName, "frame_importminresmax");
		} else if (o == frame_remove) {
			swingGui.send(boundServiceName, "frame_remove", frameList);
		} else if (o == frame_load) {
			swingGui.send(boundServiceName, "frame_load", frameList, frame_add_textfield, frame_addsleep_textfield,
					frame_addspeech_textfield);
		} else if (o == frame_update) {
			swingGui.send(boundServiceName, "frame_update", frameList, frame_add_textfield, frame_addsleep_textfield,
					frame_addspeech_textfield);
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