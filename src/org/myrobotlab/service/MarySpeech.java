package org.myrobotlab.service;

import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.SynthesisException;
import marytts.tools.install.ComponentDescription;
import marytts.tools.install.InstallFileParser;
import marytts.tools.install.LanguageComponentDescription;
import marytts.tools.install.LicensePanel;
import marytts.tools.install.LicenseRegistry;
import marytts.tools.install.ProgressPanel;
import marytts.tools.install.VoiceComponentDescription;
import marytts.util.MaryUtils;
import marytts.util.data.audio.AudioPlayer;
import org.myrobotlab.framework.Service;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.myrobotlab.logging.LoggingFactory;
import org.myrobotlab.service.interfaces.SpeechSynthesis;
import org.myrobotlab.service.interfaces.TextListener;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

public class MarySpeech extends Service implements TextListener, SpeechSynthesis {

    public final static Logger log = LoggerFactory.getLogger(MarySpeech.class);
    private static final long serialVersionUID = 1L;

    transient MaryInterface marytts = null;

    private final static String INSTALLFILEURL = "https://raw.github.com/marytts/marytts/master/download/marytts-components.xml";
    private List<LanguageComponentDescription> possibleLanguages;
    private List<VoiceComponentDescription> possibleVoices;


	public boolean speakBlocking(String toSpeak) {
		return speakInternal(toSpeak, true);
	}
	
	public boolean speakInternal(String toSpeak, boolean blocking) {
		AudioInputStream audio;
		try {
			log.info("speakInternal {}", toSpeak);
			if (toSpeak == null || toSpeak.length() == 0){
				log.info("speech null or empty");
				return false;
			}
			audio = marytts.generateAudio(toSpeak);
			AudioPlayer player = new AudioPlayer(audio);
			player.start();
			// To make this blocking you can join the player thread.
			if (blocking) {
				player.join();
			}
			return true;
		} catch (SynthesisException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Speech synth failed", e);
			return false;
		}
	}
	
    public MarySpeech(String reservedKey) {
        super(reservedKey);
        
        System.setProperty("mary.base", "mary");

        try {
            updateFromComponentUrl();

            marytts = new LocalMaryInterface();
        } catch (Exception e) {
            Logging.logError(e);
        }

        // Grab the first voice that's available.  :-/  
        Set<String> voices = marytts.getAvailableVoices();
        marytts.setVoice(voices.iterator().next());

    }

    @Override
    public void onText(String text) {
        // TODO Auto-generated method stub
        speak(text);
    }

    public int speak(String toSpeak) {
        // TODO: handle the isSpeaking logic/state
        speakInternal(toSpeak, false);
        return -1;
    }

 

    @Override
    public String[] getCategories() {
        return new String[]{"speech", "sound"};
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return "Speech synthesis based on MaryTTS";
    }

    // TODO IMPLEMENT !!!! --------- WHEEEEE !
    @Override
    public Boolean isSpeaking(Boolean b) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getVoices() {
        List<String> list = new ArrayList<String>(marytts.getAvailableVoices());
        return list;
    }

    @Override
    public boolean setVoice(String voice) {
        // TODO Auto-generated method stub
        marytts.setVoice(voice);
        return false;
    }

    @Override
    public String saying(String t) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLanguage(String l) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getLanguage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setVolume(float volume) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getVolume() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void interrupt() {
        // TODO Auto-generated method stub

    }

    @Override
    public String publishStartSpeaking(String utterance) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String publishEndSpeaking(String utterance) {
        // TODO Auto-generated method stub
        return null;
    }

    public static void main(String[] args) {
        LoggingFactory.getInstance().configure();
        LoggingFactory.getInstance().setLevel(Level.DEBUG);

        Runtime.start("webgui", "WebGui");
        MarySpeech mary = (MarySpeech) Runtime.start("mary", "MarySpeech");
//        mary.speakBlocking("Hello world");
//        mary.speakBlocking("I am Mary TTS and I am open source");
//        mary.speakBlocking("and I will evolve quicker than any closed source application if not in a short window of time");
//        mary.speakBlocking("then in the long term evolution of software");
//        mary.speak("Hello world");
    }

    @Override
    public String getVoice() {
        return marytts.getVoice();
    }

    public void updateFromComponentUrl() throws IOException, SAXException {
        InstallFileParser p = new InstallFileParser(new URL(INSTALLFILEURL));
        possibleLanguages = p.getLanguageDescriptions();
        possibleVoices = p.getVoiceDescriptions();
    }

    public void installSelectedLanguagesAndVoices(String[] toInstall_) {
        //TODO - remove last remaining parts of swing !!! (transform them)
        //a lot of code is copied and modified from marytts.tools.install.InstallerGUI
        System.out.println("toInstall" + Arrays.toString(toInstall_));

        for (VoiceComponentDescription voice : possibleVoices) {
            for (String toInstallVoice : toInstall_) {
                if (toInstallVoice.equals(voice.getName())) {
                    voice.setSelected(true);
                    break;
                }
            }
        }

        long downloadSize = 0;
        List<ComponentDescription> toInstall = new ArrayList<>();
        for (VoiceComponentDescription voice : possibleVoices) {

            if (voice.isSelected() && (voice.getStatus() != ComponentDescription.Status.INSTALLED || voice.isUpdateAvailable())) {
//                for (String toInstallVoice : toInstall_) {
//                    if (toInstallVoice.equals(voice.getName())) {
                toInstall.add(voice);
//                        break;
//                    }
//                }
            }
        }
        if (toInstall.isEmpty()) {
            //move to WebGui
            JOptionPane.showMessageDialog(null, "You have not selected any installable components");
            return;
        }

//        // Verify if all dependencies are met
//        // There are the following ways of meeting a dependency:
//        // - the component with the right name and version number is already installed;
//        // - the component with the right name and version number is selected for installation;
//        // - an update of the component with the right version number is selected for installation.
//        Map<String, String> unmetDependencies = new TreeMap<String, String>(); // map name to problem description
//        for (ComponentDescription cd : toInstall) {
//            if (cd instanceof VoiceComponentDescription) {
//                // Currently have dependencies only for voice components
//                VoiceComponentDescription vcd = (VoiceComponentDescription) cd;
//                String depLang = vcd.getDependsLanguage();
//                String depVersion = vcd.getDependsVersion();
//                // Two options for fulfilling the dependency: either it is already installed, or it is in toInstall
//                LanguageComponentDescription lcd = languages.get(depLang);
//                if (lcd == null) {
//                    unmetDependencies.put(depLang, "-- no such language component");
//                } else if (lcd.getStatus() == ComponentDescription.Status.INSTALLED) {
//                    if (ComponentDescription.isVersionNewerThan(depVersion, lcd.getVersion())) {
//                        ComponentDescription update = lcd.getAvailableUpdate();
//                        if (update == null) {
//                            unmetDependencies.put(depLang, "version " + depVersion + " is required by " + vcd.getName()
//                                    + ",\nbut older version " + lcd.getVersion() + " is installed and no update is available");
//                        } else if (ComponentDescription.isVersionNewerThan(depVersion, update.getVersion())) {
//                            unmetDependencies.put(depLang, "version " + depVersion + " is required by " + vcd.getName()
//                                    + ",\nbut only version " + update.getVersion() + " is available as an update");
//                        } else if (!toInstall.contains(lcd)) {
//                            unmetDependencies.put(depLang, "version " + depVersion + " is required by " + vcd.getName()
//                                    + ",\nbut older version " + lcd.getVersion() + " is installed\nand update to version "
//                                    + update.getVersion() + " is not selected for installation");
//                        }
//                    }
//                } else if (!toInstall.contains(lcd)) {
//                    if (ComponentDescription.isVersionNewerThan(depVersion, lcd.getVersion())) {
//                        unmetDependencies.put(depLang, "version " + depVersion + " is required by " + vcd.getName()
//                                + ",\nbut only older version " + lcd.getVersion() + " is available");
//                    } else {
//                        unmetDependencies.put(depLang, "is required  by " + vcd.getName()
//                                + "\nbut is not selected for installation");
//                    }
//                }
//            }
//        }
//        // Any unmet dependencies?
//        if (unmetDependencies.size() > 0) {
//            StringBuilder buf = new StringBuilder();
//            for (String compName : unmetDependencies.keySet()) {
//                buf.append("Component ").append(compName).append(" ").append(unmetDependencies.get(compName)).append("\n");
//            }
//            JOptionPane.showMessageDialog(this, buf.toString(), "Dependency problem", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
        for (ComponentDescription cd : toInstall) {
            if (cd.getStatus() == ComponentDescription.Status.AVAILABLE) {
                downloadSize += cd.getPackageSize();
            } else if (cd.getStatus() == ComponentDescription.Status.INSTALLED && cd.isUpdateAvailable()) {
                if (cd.getAvailableUpdate().getStatus() == ComponentDescription.Status.AVAILABLE) {
                    downloadSize += cd.getAvailableUpdate().getPackageSize();
                }
            }
        }
        int returnValue = JOptionPane
                .showConfirmDialog(null,
                        "Install " + toInstall.size() + " components?\n(" + MaryUtils.toHumanReadableSize(downloadSize)
                        + " to download)", "Proceed with installation?", JOptionPane.YES_NO_OPTION);
        if (returnValue != JOptionPane.YES_OPTION) {
            System.err.println("Aborting installation.");
            return;
        }
        System.out.println("Check license(s)");
        boolean accepted = showLicenses(toInstall);
        if (accepted) {
            System.out.println("Starting installation");
            showProgressPanel(toInstall, true);
        }
    }

    private boolean showLicenses(List<ComponentDescription> toInstall) {
        Map<URL, SortedSet<ComponentDescription>> licenseGroups = new HashMap<URL, SortedSet<ComponentDescription>>();
        // Group components by their license:
        for (ComponentDescription cd : toInstall) {
            URL licenseURL = cd.getLicenseURL(); // may be null
            // null is an acceptable key for HashMaps, so it's OK.
            SortedSet<ComponentDescription> compsUnderLicense = licenseGroups.get(licenseURL);
            if (compsUnderLicense == null) {
                compsUnderLicense = new TreeSet<ComponentDescription>();
                licenseGroups.put(licenseURL, compsUnderLicense);
            }
            assert compsUnderLicense != null;
            compsUnderLicense.add(cd);
        }
        // Now show license for each group
        for (URL licenseURL : licenseGroups.keySet()) {
            if (licenseURL == null) {
                continue;
            }
            URL localURL = LicenseRegistry.getLicense(licenseURL);
            SortedSet<ComponentDescription> comps = licenseGroups.get(licenseURL);
            System.out.println("Showing license " + licenseURL + " for " + comps.size() + " components");
            LicensePanel licensePanel = new LicensePanel(localURL, comps);
            final JOptionPane optionPane = new JOptionPane(licensePanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION,
                    null, new String[]{"Reject", "Accept"}, "Reject");
            optionPane.setPreferredSize(new Dimension(800, 600));
            final JDialog dialog = new JDialog((Frame) null, "Do you accept the following license?", true);
            dialog.setContentPane(optionPane);
            optionPane.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    String prop = e.getPropertyName();

                    if (dialog.isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        dialog.setVisible(false);
                    }
                }
            });
            dialog.pack();
            dialog.setVisible(true);

            if (!"Accept".equals(optionPane.getValue())) {
                System.out.println("License not accepted. Installation of component cannot proceed.");
                return false;
            }
            System.out.println("License accepted.");
        }
        return true;
    }

    private void showProgressPanel(List<ComponentDescription> comps, boolean install) {
        final ProgressPanel pp = new ProgressPanel(comps, install);
        final JOptionPane optionPane = new JOptionPane(pp, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                new String[]{"Abort"}, "Abort");
        // optionPane.setPreferredSize(new Dimension(640,480));
        final JDialog dialog = new JDialog((Frame) null, "Progress", false);
        dialog.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (dialog.isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                    pp.requestExit();
                    dialog.setVisible(false);
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);
        new Thread(pp).start();
    }
}
