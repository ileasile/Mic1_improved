/* $Id: Mic.java,v 1.3 2005/02/22 05:28:25 RMS Exp $
 *
 *  Mic.java
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
import javax.help.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.filechooser.FileFilter;
import java.text.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;

/**
 * Main frame   
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class Mic extends JFrame {
  public final static int[] WID = {1010, 625};
  public final static int[] HT = {933, 597};
  public final static int[] MID = {395, 240};
  public final static int[] MEMSIZ = {485, 287};
  public final static int[] CTLSIZ = {65, 63};
  public final static int[] MMIOY = {770, 470};
  public final static int[] MMIOHT = {110, 75};
  public final static int[] HELPWID = {1100, 600};
  public final static int[] HELPHT = {600, 350};
  public final static Color bg = Color.cyan;
  public final static String title = "Mic-1 MMV";
  public final static String version = "2.0";
  public final static int ACTIONCOUNT = 13,
    LOADMAC = 0, LOADMIC = 1, LOADJAS = 2, LOADMAL = 3,
    PREF = 4, LOADPREF = 5, SAVEPREF = 6, VIEWMAL = 7,
    IJVM = 8,  MIC = 9, HELP = 10, HELPON = 11,
    ABOUT = 12, FILEMENULEVEL = 4, EDITMENULEVEL = 7, MALMENULEVEL = 8,
    ASSLOADMENULEVEL = 10;
  public static int reslvl = 0;
  public static boolean mStoHL = true;
  public static MicText micText = null;
  public String micName = "", macName = "";
  public String defaultPropFile = "mic1.properties", defaultIjvmFile = "ijvm.conf",
    ijvmFile, helpFile = "help.html";
  public String defaultMicFile = "";
  public String defaultMacFile = "";
  public  MicCanvas canvas = null;
  public MicMemory memory = null;
  public CtlCanvas ctlcvs = null;
  public static ImageIcon titleImage;
  private JMenuItem helpMenuItem;
  private File malFile = null;
  private File jasFile = null;
  private javax.swing.filechooser.FileFilter micff = new Mic1Filter();
  private javax.swing.filechooser.FileFilter malff = new MalFilter();
  private javax.swing.filechooser.FileFilter macff = new IjvmFilter();
  private javax.swing.filechooser.FileFilter jasff = new JasFilter();
  private String[] tipList =
  {"Load IJVM program ...", "Load Micro program ...", "Assemble / Load JAS file ...",
   "Assemble / Load MAL file ...",
   "Edit Preferences ...", "Load Preferences ...", "Save Preferences ...",
   "View Microstore", "Current JAS Assemble / Load", "Current MAL Assemble / Load",
   title+" Help", title+" Help On ...", "About "+title}; 
  private MicAction[] actionList = new MicAction[ACTIONCOUNT];
  private JMenuBar menuBar1 = new JMenuBar();
  private JMenu menuFile = new JMenu("File"), menuEdit = new JMenu("Preferences"),
    menuMal = new JMenu("Microcode Store"),
    menuAssLoad = new JMenu("Assemble/Load"),
    menuAbout = new JMenu("About");
  private MicPreferences micprefs = new MicPreferences(this);
  private JPanel mainPanel = new JPanel(), memDisp = null;
  private MicCtlPanel ctlpanel = null;
  private MMIO mmio;
  private boolean defaultTracking = true, defaultBHigh = true, defaultSHigh = true;
  private int defaultStackSize = 500, defaultConstSize = 500;
  private String internalMicName = "resources/mic1ijvm.mic1";
  private Properties props, defprops = new Properties();
  // help system

  private String helpSetURL = "help/Mic1.hs";
  private HelpSet hs = null;
  private HelpBroker hb = null;

  public Mic(String micFile) {
    super(title);
    setResizable(false);
    titleImage = new ImageIcon(getClass().getResource("resources/mictitle.gif"));
    setIconImage(titleImage.getImage());
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    initDefaultProps();
    props = new Properties(defprops);
    canvas = new MicCanvas(this);
    ctlcvs = new CtlCanvas(this, canvas);
    ctlcvs.setBounds(MID[reslvl], MEMSIZ[reslvl]+CTLSIZ[reslvl]+15, CtlCanvas.WID[reslvl], CtlCanvas.HTH[reslvl]);
    canvas.ctlcvs = ctlcvs;
    canvas.setup();
    validate();
    ctlpanel = new MicCtlPanel(ctlcvs);
    ctlcvs.ctlpanel = ctlpanel;
    ctlpanel.setBounds(MID[reslvl], MEMSIZ[reslvl]+10, CtlCanvas.WID[reslvl], CTLSIZ[reslvl]);
    memory = new MicMemory(defaultConstSize, defaultStackSize, ctlcvs);
    memory.setTracking(defaultTracking);
    memory.setHilighting(MicMemory.BYTE, defaultBHigh);
    memory.setHilighting(MicMemory.STACK, defaultSHigh);
    ctlcvs.setMemory(memory);
    memDisp = memory.display;
    memDisp.setBounds(MID[reslvl], 5, CtlCanvas.WID[reslvl], MEMSIZ[reslvl]);
    memDisp.revalidate();
    mmio = new MMIO();
    mmio.setBounds(5, MMIOY[reslvl], WID[reslvl]-((reslvl == 0) ? 19 : 13), MMIOHT[reslvl]);
    memory.mmio = mmio;
    mainPanel.setLayout(null);
    mainPanel.setBackground(bg);
    mainPanel.add(canvas);
    mainPanel.add(memDisp);
    mainPanel.add(ctlpanel);
    mainPanel.add(ctlcvs);
    mainPanel.add(mmio);
    getContentPane().add(mainPanel);
    for (int i = 0; i < ACTIONCOUNT; i++) {
      actionList[i] = new MicAction(tipList[i]);
      if (i < FILEMENULEVEL) {
	menuFile.add(actionList[i]);
      }
      else if (i < EDITMENULEVEL) menuEdit.add(actionList[i]);
      else if (i < MALMENULEVEL) menuMal.add(actionList[i]);
      else if (i < ASSLOADMENULEVEL) menuAssLoad.add(actionList[i]);
      else menuAbout.add(actionList[i]);
      if (i == 1) menuFile.addSeparator();
      if (i == HELPON) {
	helpMenuItem = menuAbout.getItem(menuAbout.getItemCount()-1);
      }
    }
    menuBar1.add(menuFile);
    menuBar1.add(menuEdit);
    menuBar1.add(menuMal);
    menuBar1.add(menuAssLoad);
    menuBar1.add(menuAbout);
    this.setJMenuBar(menuBar1);
    ctlcvs.reset();
    getDefaultMicFile((micFile == null) ? defaultMicFile : micFile);
    getDefaultMacFile(defaultMacFile);
    setSize(new Dimension(WID[reslvl], HT[reslvl]));
    micprefs.loadDefaults(props);
    initHelp();
    setHelp();
    setTitle();
  }
    
  public void setHelp(Component comp, String ID) {
    CSH.setHelpIDString(comp, ID);
  }

  private void setHelp() {
    helpMenuItem.addActionListener(new CSH.DisplayHelpAfterTracking(hb));
    setHelp(menuBar1, "menus");
    setHelp(canvas, "archview");
    setHelp(memDisp, "memview");
    setHelp(ctlpanel, "commands");
    setHelp(ctlcvs, "insview");
    setHelp(mmio, "ioview");
  }
				   
  private void initDefaultProps() {
    defprops.setProperty("delay", "500");
    defprops.setProperty("csize", "500");
    defprops.setProperty("ssize", "500");
    defprops.setProperty("mbase", "0");
    defprops.setProperty("cbase", "10000");
    defprops.setProperty("sbase", "20000");
    defprops.setProperty("sp", "8010");
    defprops.setProperty("lv", "8000");
    defprops.setProperty("cpp", "4000");
    defprops.setProperty("mic", "mic1ijvm.mic1");
    defprops.setProperty("mac", "");
    defprops.setProperty("ijvm", "ijvm.conf");
    defprops.setProperty("track", "Y");
    defprops.setProperty("mhi", "Y");
    defprops.setProperty("shi", "Y");
    defprops.setProperty("sto", "Y");
    defprops.setProperty("res", "Y");
    defprops.setProperty("brk", "");
  }

  public void setup() {
    memory.buildDisplay();
    memDisp.setBounds(MID[reslvl], 5, CtlCanvas.WID[reslvl], MEMSIZ[reslvl]);
    ctlpanel.setBounds(MID[reslvl], MEMSIZ[reslvl]+10, CtlCanvas.WID[reslvl], CTLSIZ[reslvl]);
    ctlcvs.setBounds(MID[reslvl], MEMSIZ[reslvl]+CTLSIZ[reslvl]+15, CtlCanvas.WID[reslvl], CtlCanvas.HTH[reslvl]);
    mmio.setBounds(5, MMIOY[reslvl], WID[reslvl]-((reslvl == 0) ? 19 : 13), MMIOHT[reslvl]);
    mmio.setup();
    canvas.setup();
    setSize(new Dimension(WID[reslvl], HT[reslvl]));
    ctlpanel.setup();
    ctlcvs.reset();
    validate();
  }

  private void setTitle() {
    StringBuffer sbuf = new StringBuffer(title+" ("+micName+".mic1) - "+macName+
					 ((macName.length() == 0) ? "" : ".ijvm"));
    if (malFile == null && jasFile == null) setTitle(sbuf.toString());
    else {
      sbuf.append("       ");
      if (malFile != null) sbuf.append("MAL: "+malFile+"   ");
      if (jasFile != null) sbuf.append("JAS: "+jasFile);
      setTitle(sbuf.toString());
    }
  }

  public void reset() {
    mStoUnHilite();
  }

  public boolean hiRes() {return reslvl == 0;}
  public void setHiRes(boolean hires) {reslvl = (hires) ? 0 : 1;}

  public boolean isMStoHL() {return mStoHL;}

  public void setMStoHL(boolean mStoHL) {
    this.mStoHL = mStoHL;
    if (!mStoHL) mStoUnHilite();
  }

  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() != WindowEvent.WINDOW_CLOSING) {
      super.processWindowEvent(e);
      return;
    }
    System.exit(0);
  }

  private void central_actionPerformed(ActionEvent e) {
    String name;
    int commandIndex = -1;
    for (int i = 0; i < tipList.length; i++)
      if (e.getActionCommand().equals(tipList[i])) {
	commandIndex = i;
	break;
      }
    switch (commandIndex) {
    case LOADMAC:
      File currentFile = getMacMic("Load IJVM program", macff);
      if (currentFile == null) break;
      loadMac(currentFile);
      break;
    case LOADMIC:
      currentFile = getMacMic("Load MIC program", micff);
      if (currentFile == null) break;
      loadMic(currentFile);
      break;
    case LOADMAL:
      micAndLoad(true);
      break;
    case LOADJAS:
      assAndLoad(true);
      break;
    case PREF:
      micprefs.show(props);
      break;
    case LOADPREF:
      micprefs.load(props);
      break;
    case SAVEPREF:
      micprefs.save(props);
      break;
    case VIEWMAL:
      micText = new MicText(this, ctlcvs);
      setHelp(micText, "msview");
      break;
    case IJVM:
      assAndLoad(false);
      break;
    case MIC:
      micAndLoad(false);
      break;
    case HELP:
      help();
      break;
    case HELPON:
      break;
    case ABOUT:
      helpAbout_actionPerformed(e);
      break;
    }
  }

  public void mStoHilite(int mpc) {
    if (MicText.getCurrent() == null || !isMStoHL()) return;
    MicText.getCurrent().hilite(mpc);
  }

  public void mStoUnHilite() {
    if (MicText.getCurrent() == null) return;
    MicText.getCurrent().unHilite();
  }

  private void help() {
    hb.setDisplayed(true);
  }

  private void initHelp() {
    ClassLoader loader = this.getClass().getClassLoader();
    URL url;
    try {
      url = getClass().getResource(helpSetURL);
      if (url == null) throw new Exception("URL null");
      hs = new HelpSet(loader, url);
    } catch (Exception ee) {
      System.out.println ("Trouble in createHelpSet;");
      ee.printStackTrace();
      return;
    }
    hb = hs.createHelpBroker();
    hb.enableHelpKey(this.getRootPane(), "title_page", hs);
    try {
      hb.setSize(new Dimension(HELPWID[reslvl], HELPHT[reslvl]));
    } catch (javax.help.UnsupportedOperationException ex) {
      System.out.println(ex);
    }
  }
  
  private void helpAbout_actionPerformed(ActionEvent e) {
    MicFrame_AboutBox dlg = new MicFrame_AboutBox(this);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.show();
  }

  private void getDefaultMicFile(String filename) {
    try {
      ctlcvs.readMic(new File(filename));
    } catch (Exception ex) {
      try {
	ctlcvs.readMic(new DataInputStream(getClass().getResourceAsStream(internalMicName)));
      } catch (Exception ex1) {
	JOptionPane.showMessageDialog(this, "Unable to Load Microcode Program", "Error", JOptionPane.ERROR_MESSAGE);
	return;
      }
    }      
    String name = filename;
    if (name.indexOf(".mic1") > 0) name = name.substring(0, name.indexOf(".mic1"));
    micName = name;
  }
    
  private void getDefaultMacFile(String filename) {
    if (filename.length() == 0) return;
    try {
      memory.readMac(new File(filename));
    } catch (BadMagicNumberException ex) {      
      JOptionPane.showMessageDialog(this, "File Error: Invalid File Format:\n" +filename, "Error",
				    JOptionPane.ERROR_MESSAGE);
    } catch (FileNotFoundException e) {
      JOptionPane.showMessageDialog(this, "File Not Found:\n"+filename, "Error", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "IO Error Reading\n"+filename, "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String name = filename;
    if (name.indexOf(".ijvm") > 0) name = name.substring(0, name.indexOf(".ijvm"));
    macName = name;
  }

  private File getMacMic(String dTitle, FileFilter ff){
    File currentFile = null;
    JFileChooser theFileChooser = new JFileChooser(System.getProperty("user.dir"));
    theFileChooser.addChoosableFileFilter(ff);
    theFileChooser.setFileFilter(ff);
    theFileChooser.setDialogTitle(dTitle);
    if (JFileChooser.APPROVE_OPTION == theFileChooser.showOpenDialog(this)) 
      currentFile = theFileChooser.getSelectedFile();
    else return null;
    System.setProperty("user.dir", currentFile.getParent());
    return currentFile;
  }

  private void loadMic(File currentFile){
    try {
      ctlcvs.readMic(currentFile);
    } catch (BadMagicNumberException ex) {
      String s = "Error: Magic Number Not Found\nInput File May Not Contain Mic1 Microcode";
      JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (MicFormatException ex) {
      String s = ex.toString();
      JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
      return;
    } catch(FileNotFoundException e ) {
      JOptionPane.showMessageDialog(this, "File Not Found:\n"+currentFile, "Error", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "IO Error Reading\n"+currentFile, "Error", JOptionPane.ERROR_MESSAGE);
    }
    String name = currentFile.getName();
    if (name.indexOf(".mic1") > 0) name = name.substring(0, name.indexOf(".mic1"));
    micName = name;
    File tmpFile = new File(currentFile.getParent(), micName+".mal");
    if (malFile != null && !tmpFile.equals(malFile)) malFile = null;
    setTitle();
    repaint();
  }

  private boolean loadMac(File currentFile) {
    try {
      memory.readMac(currentFile);
    } catch (BadMagicNumberException ex) {      
      JOptionPane.showMessageDialog(this, "File Error: Invalid File Format:\n" +currentFile, "Error",
				    JOptionPane.ERROR_MESSAGE);
      return false;
    } catch (FileNotFoundException e) {
      JOptionPane.showMessageDialog(this, "File Not Found:\n"+currentFile, "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "IO Error Reading\n"+currentFile, "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    String name = currentFile.getName();
    if (name.endsWith(".ijvm")) name = name.substring(0, name.indexOf(".ijvm"));
    macName = name;
    File tmpFile = new File(currentFile.getParent(), macName+".jas");
    if (jasFile != null && !tmpFile.equals(jasFile)) jasFile = null;
    setTitle();
    repaint();
    return true;
  }

  private void assAndLoad(boolean newFile) {
    File inFile = null, outFile;
    if (jasFile == null || newFile) {
      if ((inFile = getMacMic("Assemble / Load", jasff)) == null) return;
      String name = inFile.getName();
      if (!name.endsWith(".jas")) {
	JOptionPane.showMessageDialog(this, "Source file must be .jas file", "Error", JOptionPane.ERROR_MESSAGE);
	return;
      }
    } else {
      inFile = jasFile;
    }
    outFile = new File(inFile.getParent(), inFile.getName().substring(0, inFile.getName().length()-4) + ".ijvm");
    mic.ontko.IJVMAssembler asm = new mic.ontko.IJVMAssembler();
    jasFile = inFile;
    switch (CompPane.loadAndGo(inFile, outFile, this, asm)) {
    case CompPane.CLOSED_OPTION:
    case CompPane.CANCEL_OPTION:
      break;
    case CompPane.OK_OPTION:
      loadMac(outFile);
      break;
    }
    setTitle();
  }

  private void micAndLoad(boolean newFile) {
    File inFile = null, outFile;
    if (malFile == null || newFile) {
      if ((inFile = getMacMic("Assemble / Load", malff)) == null) return;
      String name = inFile.getName();
      if (!name.endsWith(".mal")) {
	JOptionPane.showMessageDialog(this, "Source file must be .mal file", "Error", JOptionPane.ERROR_MESSAGE);
	return;
      }
    } else {
      inFile = malFile;
    }
    outFile = new File(inFile.getParent(), inFile.getName().substring(0, inFile.getName().length()-4) + ".mic1");
    mic.ontko.mic1asm asm = new mic.ontko.mic1asm();
    malFile = inFile;
    int status = CompPane.loadAndGo(inFile, outFile, this, asm);
    switch (status) {
    case CompPane.CLOSED_OPTION:
    case CompPane.CANCEL_OPTION:
      break;
    case CompPane.OK_OPTION:
      loadMic(outFile);
      break;
    }
    setTitle();
  }

  public static void main(String[] args) {
    String micFile = "mic1ijvm.mic1";
    if (args.length > 0) {
      micFile = args[0];
    }
    try  {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {
    }
    Mic frame = new Mic(micFile);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
    frame.validate();
  }

  class MicAction extends AbstractAction {
    public MicAction(String name) {
      super(name);
    }
    public void setText(String text){
      putValue(Action.NAME, text);
      putValue(Action.ACTION_COMMAND_KEY, text);
    }
    public void setActionCommand(String text) {
      putValue(Action.ACTION_COMMAND_KEY, text);
    }
    public void actionPerformed(ActionEvent e) {
      central_actionPerformed(e);
    }
  }

}
