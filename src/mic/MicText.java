/*  $Id$
 *
 *  MicText.java
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Dialog to display current microprogram
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class MicText extends JDialog implements ActionListener {
  private static MicText current = null;
  public final static int[] WID = {350, 217}, HT = {350, 217};
  private final JTextArea pane = new JTextArea() {
      public void addMouseListener(MouseListener ml) {}
      public void addMouseMotionListener(MouseMotionListener ml) {}
    };
  private JButton close = new JButton("close");

  public MicText(Mic frame, CtlCanvas ctlcvs) {
    super(frame, "Microstore", false);
    if (current != null) return;
    pane.setFont(MicMemory.memFont[Mic.reslvl]);
    pane.setEditable(false);
    pane.setSelectedTextColor(Color.red);
    pane.setSelectionColor(Color.white);
    addMicrocode(pane, ctlcvs);
    JScrollPane spane = new JScrollPane(pane);
    getContentPane().add(spane, BorderLayout.CENTER);
    close.addActionListener(this);
    JPanel south = new JPanel();
    south.add(close);
    getContentPane().add(south, BorderLayout.PAGE_END);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setSize(new Dimension(WID[Mic.reslvl], HT[Mic.reslvl]));
    current = this;
    setLocationRelativeTo(frame);
    setVisible(true);
  }

  public static MicText getCurrent() {return current;}

  public void unHilite() {
    try {
      pane.setCaretPosition(pane.getSelectionStart());
    } catch (Exception ex) {}
  }

  public void hilite(int lineno) {
    try {
      pane.setCaretPosition(pane.getLineEndOffset(lineno));
      pane.moveCaretPosition(pane.getLineStartOffset(lineno));
    } catch (javax.swing.text.BadLocationException ex) {}
  }

  public static void setMicrocode(CtlCanvas ctl) {
    if (current == null) return;
    current.addMicrocode(current.pane, ctl);
  }

  private void addMicrocode(JTextArea text, CtlCanvas ctl) {
    text.setText("");
    for (int i = 0; i < ctl.cmdLength(); i++) {
      Mic1Instruction mi = ctl.getCmd(i);
      String s = (mi == null) ? "" : mi.toString();
      text.append(MicTools.formatHex(i, 4)+": "+mi+"\n");
    }
    text.setCaretPosition(0);
  }

  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSED) 
      current = null;
    super.processWindowEvent(e);
  }

  public void actionPerformed(ActionEvent e) {
    setVisible(false);
    dispose();
    current = null;
  }


}








