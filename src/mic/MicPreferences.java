/*  $Id$
 *
 *  MicPreferences.java
 *
 *  Mic1MMV microarchitecture compiler/simulator 
 *  Copyright (C) 2005, Prentice-Hall, Inc. 
 * 
 *  This program is free software; you can redistribute it and/or modify 
 *  it under the terms of the GNU General Public License as published by 
 *  the Free Software Foundation; either version 2 of the License, or 
 *  (at your option) any later version. 
 * 
 *  This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 *  Public License for more details. 
 * 
 *  You should have received a copy of the GNU General Public License 
 *  along with this program; if not, write to: 
 * 
 *    Free Software Foundation, Inc. 
 *    59 Temple Place - Suite 330 
 *    Boston, MA 02111-1307, USA. 
 * 
 *  A copy of the GPL is available online the GNU web site: 
 * 
 *    http://www.gnu.org/copyleft/gpl.html
 * 
 */ 

package mic;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.text.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

/**
 * Properties management
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class MicPreferences {
  public final static int LEN1 = 12, LEN2 = 17, 
    DELAY = 0, MBASE = 1, CBASE = 2, CSIZE = 3, SBASE = 4, SSIZE = 5, SP = 6, LV = 7, CPP = 8, MIC = 9,
    MAC = 10, IJVM = 11, TRACK = 12, MHI = 13, SHI = 14, STO = 15, RES = 16, BRK = LEN2;
  public final static String[] options = {"OK", "Cancel", "Restore Defaults"};
  private Mic frame;
  private JPanel mainPanel = new JPanel();
  private JTextField[] prefText = new JTextField[LEN1];
  private JRadioButton[][] choices = {{new JRadioButton("On"), new JRadioButton("Off")},
				      {new JRadioButton("On"), new JRadioButton("Off")},
				      {new JRadioButton("On"), new JRadioButton("Off")},
				      {new JRadioButton("On"), new JRadioButton("Off")},
				      {new JRadioButton("Hi"), new JRadioButton("Low")}};
  private ButtonGroup[] bg = {new ButtonGroup(), new ButtonGroup(), new ButtonGroup(), new ButtonGroup(),
    new ButtonGroup()};
  private String[] titles =
  {"Demo Delay Duration (ms)", "Method Area Base", "Constant Pool Base", "Constant Pool Size", "Stack Area Base",
   "Stack Area Size", 
   "Initial SP (Hex)", "Initial LV (Hex)", "Initial CPP (Hex)", "Default Microprogram", "Default Macroprogram",
   "Default IJVM Configuration",
   "Memory Area Tracking", "Method Highlighting", "CP/Stack Highlighting", 
   "Microstore Highlighting", "Screen Resolution"};
  private JTextArea breakArea = new JTextArea(5, 10);

  public MicPreferences(Mic frame) {
    this.frame = frame;
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    for (int i = 0; i < LEN2; i++) {
      JPanel p = new JPanel();
      p.setBorder(new EmptyBorder(0,0,0,0));
      JLabel lab = new JLabel(titles[i]);
      lab.setPreferredSize(new Dimension(150, 18));
      p.add(lab);
      if (i < LEN1) {
	prefText[i] = new JTextField(15);
	prefText[i].setFont(MicMemory.memFont[0]);
	p.add(prefText[i]);
	mainPanel.add(p);
      } else {
	JPanel q = new JPanel();
	q.setPreferredSize(new Dimension(120, 20));
	q.setBorder(new EmptyBorder(0,0,0,0));
	for (int j = 0; j < choices[i-LEN1].length; j++) {
	  choices[i-LEN1][j].setBorder(new EmptyBorder(0,0,0,0));
	  bg[i-LEN1].add(choices[i-LEN1][j]);
	  q.add(choices[i-LEN1][j]);
	}
	p.add(q);
	mainPanel.add(p);
      }
    }
    JPanel p = new JPanel();
    p.add(new JLabel("Set/Remove Breakpoints"));
    mainPanel.add(p);
    mainPanel.add(new JScrollPane(breakArea));
  }

  public void show(Properties props) {
    prefText[DELAY].setText(Integer.toString(frame.ctlcvs.getDemoDelay()));
    prefText[MBASE].setText(MicTools.formatHex(frame.memory.getStartAddr(0), 8));
    prefText[CBASE].setText(MicTools.formatHex(frame.memory.getStartAddr(1), 8));
    prefText[CSIZE].setText(Integer.toString(frame.memory.getConstValsLength()));
    prefText[SBASE].setText(MicTools.formatHex(frame.memory.getStartAddr(2), 8));
    prefText[SSIZE].setText(Integer.toString(frame.memory.getStackValsLength()));
    prefText[SP].setText(MicTools.formatHex(frame.canvas.getInitSP(), 8));
    prefText[LV].setText(MicTools.formatHex(frame.canvas.getInitLV(), 8));
    prefText[CPP].setText(MicTools.formatHex(frame.canvas.getInitCPP(), 8));
    prefText[MIC].setText(frame.defaultMicFile);
    prefText[MAC].setText(frame.defaultMacFile);
    prefText[IJVM].setText(frame.ijvmFile);
    choices[TRACK-LEN1][0].setSelected(frame.memory.getTracking());
    choices[TRACK-LEN1][1].setSelected(!frame.memory.getTracking());
    choices[MHI-LEN1][0].setSelected(frame.memory.getHilighting(MicMemory.BYTE));
    choices[MHI-LEN1][1].setSelected(!frame.memory.getHilighting(MicMemory.BYTE));
    choices[SHI-LEN1][0].setSelected(frame.memory.getHilighting(MicMemory.STACK));
    choices[SHI-LEN1][1].setSelected(!frame.memory.getHilighting(MicMemory.STACK));
    choices[STO-LEN1][0].setSelected(frame.isMStoHL());
    choices[STO-LEN1][1].setSelected(!frame.isMStoHL());
    choices[RES-LEN1][0].setSelected(frame.hiRes());
    choices[RES-LEN1][1].setSelected(!frame.hiRes());
    breakArea.setText(frame.ctlcvs.getBreakPoints());
    int n = 
      JOptionPane.showOptionDialog(frame, mainPanel, "Edit Preferences",
				   JOptionPane.OK_CANCEL_OPTION,
				   JOptionPane.PLAIN_MESSAGE,
				   null, options, options[0]);
    String[] prefs = new String[LEN2+1];
    switch (n) {
    case -1:
    case 1:
      return;
    case 0:
      for (int i = 0; i < LEN1; i++) prefs[i] = prefText[i].getText();
      for (int i = LEN1; i < LEN2; i++) prefs[i] = which(choices[i-LEN1]);
      prefs[BRK] = breakArea.getText();
      setProps(props, prefs);
      setProperties(props);
      frame.setup();
      frame.repaint();
      break;
    case 2:
      loadDefaults(props);
      frame.setup();
      frame.repaint();
      break;
    }
  }

  private void setProps(Properties props, String[] prefs) {
    props.setProperty("delay", prefs[DELAY]);
    props.setProperty("csize", prefs[CSIZE]);
    props.setProperty("ssize", prefs[SSIZE]);
    props.setProperty("mbase", prefs[MBASE]);
    props.setProperty("cbase", prefs[CBASE]);
    props.setProperty("sbase", prefs[SBASE]);
    props.setProperty("sp", prefs[SP]);
    props.setProperty("lv", prefs[LV]);
    props.setProperty("cpp", prefs[CPP]);
    props.setProperty("mic", prefs[MIC]);
    props.setProperty("mac", prefs[MAC]);
    props.setProperty("ijvm", prefs[IJVM]);
    props.setProperty("track", prefs[TRACK]);
    props.setProperty("mhi", prefs[MHI]);
    props.setProperty("shi", prefs[SHI]);
    props.setProperty("sto", prefs[STO]);
    props.setProperty("res", prefs[RES]);
    props.setProperty("brk", prefs[BRK]);
  }


  public void load(Properties props) {
    File currentFile;
    JFileChooser theFileChooser = new JFileChooser(System.getProperty("user.dir"));
    PrefFilter prefFF = new PrefFilter();
    theFileChooser.addChoosableFileFilter(prefFF);
    theFileChooser.setFileFilter(prefFF);
    theFileChooser.setDialogTitle("Load Preferences");
    if (JFileChooser.APPROVE_OPTION == theFileChooser.showOpenDialog(frame)) 
      currentFile = theFileChooser.getSelectedFile();
    else return;
    try {
      FileInputStream in = new FileInputStream(currentFile);
      props.load(in);
      in.close();
      setProperties(props);
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(frame, "File Not Found:\n"+currentFile, "Error", JOptionPane.ERROR_MESSAGE);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(frame, "IO Error", "Error: "+ex.getMessage(), JOptionPane.ERROR_MESSAGE);
    }
  }

  public void save(Properties props) {
    File currentFile;
    JFileChooser theFileChooser = new JFileChooser(System.getProperty("user.dir"));
    PrefFilter prefFF = new PrefFilter();
    theFileChooser.addChoosableFileFilter(prefFF);
    theFileChooser.setFileFilter(prefFF);
    theFileChooser.setDialogTitle("Save Preferences");
    if (JFileChooser.APPROVE_OPTION == theFileChooser.showSaveDialog(frame)) 
      currentFile = theFileChooser.getSelectedFile();
    else return;
    try {
      OutputStream out = new FileOutputStream(currentFile);
      props.store(out, "-- Mic-1 Properties --");
      out.close();
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(frame, "File Not Found:\n"+currentFile, "Error", JOptionPane.ERROR_MESSAGE);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(frame, "IO Error Loading Preferences\n"+currentFile, "Error",
				    JOptionPane.ERROR_MESSAGE);
    }
  }

  public void loadDefaults(Properties props) {
    InputStream in;
    try {
      File prefFile = new File(System.getProperty("user.dir"), frame.defaultPropFile);
      in = new FileInputStream(prefFile);
    } catch (FileNotFoundException ex) {
      try {
	in = getClass().getResourceAsStream("resources/"+frame.defaultPropFile);
      } catch (Exception ex1) {
	setProperties(props);
	return;
      }
    }
    try {
      props.load(in);
      in.close();
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(frame, "IO Error Loading Preferences\n"+ex.getMessage(), "Error",
				    JOptionPane.ERROR_MESSAGE);
    }
    setProperties(props);
  }

  private void loadIJVM() {
    InputStream in;
    try {
      try {
	in = new FileInputStream(new File(System.getProperty("user.dir"), frame.ijvmFile));
	MicTools.initIJVM(in);
      } catch (FileNotFoundException ex) {
	try {
	  in = getClass().getResourceAsStream("resources/"+frame.defaultIjvmFile);
	  MicTools.initIJVM(in);
	} catch (FileNotFoundException ex1) {
	  JOptionPane.showMessageDialog(frame, "File Error Loading IJVM\n"+ex1.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
	}
      }
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(frame, "IO Error Loading IJVM\n"+ex.getMessage(), "Error",
				    JOptionPane.ERROR_MESSAGE);
    }
  }
  
  private void setProperties(Properties props) {
    int tmp;
    try {
      tmp = Integer.parseInt(props.getProperty("delay"));
      frame.ctlcvs.setDemoDelay(tmp);
    } catch (NumberFormatException e){}
    try {
      tmp = Integer.parseInt(props.getProperty("mbase"), 16);
      frame.memory.setStartAddr(0, tmp);
    } catch (NumberFormatException e){}
    try {
      tmp = Integer.parseInt(props.getProperty("cbase"), 16);
      frame.memory.setStartAddr(1, tmp);
    } catch (NumberFormatException e){}
    try {
      tmp = Integer.parseInt(props.getProperty("csize"));
      frame.memory.setConstValsLength(tmp);
    } catch (NumberFormatException e){}
    try {
      tmp = Integer.parseInt(props.getProperty("sbase"), 16);
      frame.memory.setStartAddr(2, tmp);
    } catch (NumberFormatException e){}
    try {
      tmp = Integer.parseInt(props.getProperty("ssize"));
      frame.memory.setStackValsLength(tmp);
    } catch (NumberFormatException e){}
    try {
      tmp = Integer.parseInt(props.getProperty("sp"), 16);
      frame.canvas.setInitSP(tmp);
    } catch (NumberFormatException e){}
    try {
      tmp = Integer.parseInt(props.getProperty("lv"), 16);
      frame.canvas.setInitLV(tmp);
    } catch (NumberFormatException e){}
    try {
      tmp = Integer.parseInt(props.getProperty("cpp"), 16);
      frame.canvas.setInitCPP(tmp);
    } catch (NumberFormatException e){}
    frame.defaultMicFile = props.getProperty("mic");
    frame.defaultMacFile = props.getProperty("mac");
    frame.ijvmFile = props.getProperty("ijvm");
    frame.memory.setTracking(props.getProperty("track").equals("Y"));
    frame.memory.setHilighting(MicMemory.BYTE, props.getProperty("mhi").equals("Y"));
    frame.memory.setHilighting(MicMemory.STACK, props.getProperty("shi").equals("Y"));
    frame.setMStoHL(props.getProperty("sto").equals("Y"));
    frame.setHiRes(props.getProperty("res").equals("Y"));
    frame.setup();
    frame.ctlcvs.setBreakPoints(props.getProperty("brk"));
    frame.ctlcvs.reset();
    loadIJVM();
  }

  private String which(JRadioButton[] choices) {
    if (choices[0].isSelected()) return "Y";
    return "N";
  }
}
