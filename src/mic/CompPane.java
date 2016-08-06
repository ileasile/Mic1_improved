/* $Id: CompPane.java,v 1.2 2005/02/22 05:28:24 RMS Exp $
 *
 *  CompPane.java
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
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

/**
 * IJVM and Mic Compiler panels
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class CompPane extends JPanel{
  private final static int[] WID = {400, 200}, HT = {200, 100};
  private final static String[] title = {"IJVM Assembler", "Microcode Assembler"};
  public final static int CLOSED_OPTION = JOptionPane.CLOSED_OPTION,
    CANCEL_OPTION = JOptionPane.CANCEL_OPTION,
    OK_OPTION = JOptionPane.OK_OPTION;
  private static Mic frame;
  private static JButton done = new JButton("Load"), 
    cancel = new JButton("Cancel");
  private static Object[] options = new Object[]{done, cancel};
  private static JOptionPane pane;
  private static JDialog dialog;
  private static JTextArea text = new JTextArea();
  private static JLabel label = new JLabel();
  private static MicTranslator comp;
  private static ActionListener bl = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	pane.setValue(e.getSource());
	dialog.hide();
      }
    };

  static {
    cancel.addActionListener(bl);
    done.addActionListener(bl);
    text.setEditable(false);
  }
			     
  private InputStream in;
  private OutputStream out;
  private boolean flag = true, errflg = false;

  public CompPane(String filename, InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
    label.setText("Assembling "+filename+":");
    text.setFont(MicMemory.memFont[Mic.reslvl]);
    text.setText("");
    setCancelText("Cancel");
    JScrollPane scrollPane = new JScrollPane(text);
    scrollPane.setPreferredSize(new Dimension(WID[frame.reslvl], HT[frame.reslvl]));
    this.setLayout(new BorderLayout());
    this.add(label, BorderLayout.NORTH);
    this.add(scrollPane, BorderLayout.CENTER);
  }

  public void setDoneEnabled(boolean b) {
    done.setEnabled(b);
  }

  public void setCancelEnabled(boolean b) {
    cancel.setEnabled(b);
  }

  public void setCancelText(String s) {
    cancel.setText(s);
    setCancelEnabled(true);
  }

  public void setStatus(boolean status) {
    errflg = !status;
  }

  public boolean isErr() {
    return errflg;
  }

  public void appendErr(String s) {
    append(s+"\n");
    errflg = true;
  }

  public void append(final String s) {
    SwingFix.doitLater(new Runnable() {
	public void run() {
	  text.append(s);
	}});
  }

  public void exec() {
    new Thread(new Runnable() {
	public void run() {
	  CompPane cpane = CompPane.this;
	  try {
    	    Thread.sleep(500);
	  } catch (InterruptedException ex) {}
	  try {
	    comp.run();
	    if (cpane.isErr()) {
	      cpane.setCancelText("Close");
	    } else {
	      cpane.append("assembly complete\n");
	      cpane.setDoneEnabled(true);
	    }
	  } catch (Exception ex) {
	    cpane.append("assembly errors\n");
	    cpane.setCancelText("Continue");
	  }
	}}).start();
  }
  
  public static Mic getFrame() {return frame;}

  public static int loadAndGo(File inFile, File outFile, Mic frm, MicTranslator cmp) {
    InputStream in = null;
    OutputStream out = null;
    CompPane cpane;
    int ctype = (cmp instanceof mic.ontko.IJVMAssembler) ? 0 : 1;
    frame = frm;
    comp = cmp;
    try {
      in = new BufferedInputStream(new FileInputStream(inFile));
    } catch (IOException e) {
      errorMsg("Error opening input file " + inFile.getName());
    }
    try {
      if (ctype == 0)
	out = new FileOutputStream(outFile);
      else out = new DataOutputStream(new FileOutputStream (outFile));
    } catch (IOException e) {
      errorMsg("Error opening output file " + outFile.getName());
    }
    cpane = new CompPane(inFile.getName(), in, out);
    pane = new JOptionPane(cpane, JOptionPane.PLAIN_MESSAGE,
				       JOptionPane.DEFAULT_OPTION, null,
				       options, options[1]);
    dialog = pane.createDialog(frame, "Assembling "+inFile.getName()+" ...");
    cpane.setDoneEnabled(false);
    cpane.append(title[ctype]+"...\n");
    comp.setCPane(cpane);
    comp.setInputStream(in);
    comp.setOutputStream(out);
    frm.setHelp(dialog, "assemview");
    cpane.exec();
    dialog.show();

    Object selectedValue = pane.getValue();
    if(selectedValue == null) return CLOSED_OPTION;
    return (selectedValue.equals(done)) ? OK_OPTION :
      (selectedValue.equals(cancel)) ? CANCEL_OPTION
      : CLOSED_OPTION;
  }

  private static void errorMsg(String msg) {
    JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }
}

