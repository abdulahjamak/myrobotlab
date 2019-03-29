package org.myrobotlab.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.InMoovGestureCreator;
import org.myrobotlab.service.SwingGui;
import org.myrobotlab.service.model.Frame;
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

	private final JFormattedTextField gestureName = new JFormattedTextField("Gesture Name");

	private final JButton controlConnect = new JButton("Connect");
	private final JButton loadScriptFolder = new JButton("Open Folder");
	private final JButton loadGestureScript = new JButton("Load");
	private final JButton controlSaveScript = new JButton("Save");
	private final JButton controlNewGesture = new JButton("New");
	private final JButton controlExecuteGesture = new JButton("Execute");
	private JFormattedTextField frameNameTextField;
	private JFormattedTextField frameSleepTextField;
	private JFormattedTextField frameSpeechTextField;

	private final JButton frameNew = new JButton("New");
	private final JButton frameRemove = new JButton("Remove");
	private final JButton frameCopy = new JButton("Copy");
	private final JButton frameUp = new JButton("Up");
	private final JButton frameDown = new JButton("Down");
	private final JButton frameExecute = new JButton("Execute");
	private final JCheckBox frameMoveRealTime = new JCheckBox("Move Real Time");

	private static final String[] GESTURE_LIST_PLACEHOLDER = { "Load folder with scripts" };
	private static final String[] FRAME_LIST_PLACEHOLDER = {"Load a script to see frames..."}; 

	private final JList<String> gestureList = new JList<String>(GESTURE_LIST_PLACEHOLDER);
	private final JList<String> frameList = new JList<String>(FRAME_LIST_PLACEHOLDER);
	
	private final JPanel bottomTop = new JPanel();
	private final JPanel top = new JPanel();
  
  	private final Map<RobotSection, JPanel> robotSectionMovePanels = new HashMap<RobotSection, JPanel>();
  	private final Map<RobotSection, JPanel> robotSectionSlidersPanels = new HashMap<RobotSection, JPanel>();
  	private final Map<RobotSection, JPanel> robotSectionSpeedPanels = new HashMap<RobotSection, JPanel>();
  	private final Map<RobotSection, JPanel> robotSectionSpeedNumberBoxesPanels = new HashMap<RobotSection, JPanel>();

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
			// |##########| <- JList: gestureList & JButton's: loadscript,
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


			JPanel topLeft = new JPanel();
			topLeft.setLayout(new BorderLayout());

			JPanel topLeftTop = new JPanel();
			topLeftTop.setLayout(new BoxLayout(topLeftTop, BoxLayout.X_AXIS));

			JLabel gesturePanelLabel = new JLabel("Gestures");
			topLeftTop.add(gesturePanelLabel);
			
			topLeftTop.add(controlConnect);
			controlConnect.addActionListener(this);

			topLeft.add(BorderLayout.NORTH, topLeftTop);

			JPanel topLeftRight = new JPanel();
			topLeftRight.setLayout(new BoxLayout(topLeftRight, BoxLayout.Y_AXIS));

			topLeftRight.add(loadScriptFolder);
			loadScriptFolder.addActionListener(this);

			topLeftRight.add(loadGestureScript);
			loadGestureScript.addActionListener(this);

			topLeftRight.add(controlSaveScript);
			controlSaveScript.addActionListener(this);

			topLeftRight.add(controlNewGesture);
			controlNewGesture.addActionListener(this);

			topLeftRight.add(controlExecuteGesture);
			controlExecuteGesture.addActionListener(this);

			topLeft.add(BorderLayout.EAST, topLeftRight);
			
			gestureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			JScrollPane gestureListScroller = new JScrollPane(gestureList);
			gestureListScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			gestureListScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			topLeft.add(BorderLayout.CENTER, gestureListScroller);

			JPanel topRight = new JPanel();
			topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));

			JPanel topRightTop = new JPanel();
			topRightTop.setLayout(new BoxLayout(topRightTop, BoxLayout.X_AXIS));

			JLabel gestureNameLabel = new JLabel("Gesture name");
			topRightTop.add(gestureNameLabel);
			
			PropertyChangeListener gestureNameTextListener = new PropertyChangeListener() {
		        @Override
		        public void propertyChange(PropertyChangeEvent evt) {
		            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
                	myService.send(boundServiceName, "updateGestureName", text);	
		        }
		    };
		    gestureName.addPropertyChangeListener("value", gestureNameTextListener);
			topRightTop.add(gestureName);

			topRightTop.add(frameNew);
			frameNew.addActionListener(this);
			
			topRightTop.add(frameRemove);
			frameRemove.addActionListener(this);

			topRightTop.add(frameCopy);
			frameCopy.addActionListener(this);

			topRightTop.add(frameUp);
			frameUp.addActionListener(this);

			topRightTop.add(frameDown);
			frameDown.addActionListener(this);

			topRightTop.add(frameExecute);
			frameExecute.addActionListener(this);

			topRight.add(BorderLayout.NORTH, topRightTop);

			frameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
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
	                			frameMoveRealTime,
	                			frameList);			
	                	myService.send(boundServiceName, "frameSelectionChanged", 
	                			top,
	                			robotSectionMovePanels, 
	                			robotSectionSlidersPanels,
	                			robotSectionSpeedPanels,
	                			robotSectionSpeedNumberBoxesPanels, 
	                			frameList);
	                }
	            }
	        });

			JPanel bottom = new JPanel();
			bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
			
			Dimension maximumSize = new Dimension(Integer.MAX_VALUE, 50);
			bottomTop.setMaximumSize(maximumSize);
			bottomTop.setMinimumSize(maximumSize);
			bottomTop.setLayout(new BoxLayout(bottomTop, BoxLayout.X_AXIS));	
			bottomTop.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 

			robotSectionMovePanels.clear();
			robotSectionSpeedPanels.clear();
			robotSectionSlidersPanels.clear();
			robotSectionSpeedNumberBoxesPanels.clear();
			
			JPanel bottomBottom = new JPanel();
			bottomBottom.setLayout(new BoxLayout(bottomBottom, BoxLayout.X_AXIS));
			
			for (RobotSection robotSection : RobotSection.values()) {
				final JPanel sectionPanel = new JPanel();
				sectionPanel.setLayout(new BorderLayout());
				sectionPanel.add(BorderLayout.NORTH, new JLabel(Frame.getSectionLabel(robotSection)));
				sectionPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
				bottomBottom.add(sectionPanel);
				final JPanel sectionMovePanel = new JPanel();
				sectionMovePanel.setLayout(new BoxLayout(sectionMovePanel, BoxLayout.Y_AXIS));
				robotSectionMovePanels.put(robotSection, sectionMovePanel);
				final JPanel sectionSlidersPanel = new JPanel();
				sectionSlidersPanel.setLayout(new BoxLayout(sectionSlidersPanel, BoxLayout.X_AXIS));
				robotSectionSlidersPanels.put(robotSection, sectionSlidersPanel);
				final JPanel sectionSpeedPanel = new JPanel();
				sectionSpeedPanel.setLayout(new BoxLayout(sectionSpeedPanel, BoxLayout.Y_AXIS));
				sectionPanel.add(BorderLayout.CENTER, sectionMovePanel);
				sectionPanel.add(BorderLayout.SOUTH, sectionSpeedPanel);
				robotSectionSpeedPanels.put(robotSection, sectionSpeedPanel);
				final JPanel sectionSpeedNumberBoxesPanel = new JPanel();
				sectionSpeedNumberBoxesPanel.setLayout(new BoxLayout(sectionSpeedNumberBoxesPanel, BoxLayout.X_AXIS));
				robotSectionSpeedNumberBoxesPanels.put(robotSection, sectionSpeedNumberBoxesPanel);
			}
			
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
		if (o == controlConnect) {
			swingGui.send(boundServiceName, "controlConnect", controlConnect);
		} else if (o == loadScriptFolder) {
			swingGui.send(boundServiceName, "loadScriptFolder", gestureList);
		} else if (o == loadGestureScript) {
			swingGui.send(boundServiceName, "loadGestureScript", gestureList, frameList, gestureName);
		} else if (o == controlSaveScript) {
			swingGui.send(boundServiceName, "controlSaveScript");
		} else if (o == controlNewGesture) {
			swingGui.send(boundServiceName, "clearGestureAndSelectedFrame", 
					frameList, bottomTop, top,
					gestureName,
        			robotSectionMovePanels, 
        			robotSectionSlidersPanels,
        			robotSectionSpeedPanels,
        			robotSectionSpeedNumberBoxesPanels);
		} else if (o == controlExecuteGesture) {
			swingGui.send(boundServiceName, "controlExecuteGesture");
		} else if (o == frameNew) {
			swingGui.send(boundServiceName, "frameNew", frameList);
		} else if (o == frameRemove) {
			swingGui.send(boundServiceName, "frameRemove", frameList);
		} else if (o == frameCopy) {
			swingGui.send(boundServiceName, "frameCopy", frameList);
		} else if (o == frameUp) {
			swingGui.send(boundServiceName, "frameUp", frameList);
		} else if (o == frameDown) {
			swingGui.send(boundServiceName, "frameDown", frameList);
		} else if (o == frameExecute) {
			swingGui.send(boundServiceName, "frameExecute", frameList);
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
//		Object o = ie.getSource();
		// CheckBox - Events
//		if (o == frameMoveRealTime) {
//			swingGui.send(boundServiceName, "frameMoveRealTime", frameMoveRealTime);
//		}
	}
}