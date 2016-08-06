/* $Id: MMIO.java,v 1.2 2005/02/22 05:28:25 RMS Exp $
 *
 *  MMIO.java
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

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Memory io
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class MMIO extends JPanel implements CaretListener {
  public final static Font[] font = {new Font("Courier New", Font.BOLD, 14),
			      new Font("Courier New", Font.BOLD, 12)};
  public final static Font[] labFont = {new Font("MS Sans Serif", Font.BOLD, 12),
				 new Font("MS Sans Serif", Font.BOLD, 11)};
  private Mic frame;
  private int dot = 0, charPtr = 0, pollCount = 0, oldPollCount = 0;
  private boolean redoMode = false;
  private StringBuffer in = new StringBuffer();
  private StringBuffer out = new StringBuffer();
  private JTextArea inText = new JTextArea(0, 0),
    outText = new JTextArea(0, 0);
  private JPanel inPanel = new JPanel(), outPanel = new JPanel();
  private JLabel inLab, outLab;

  public MMIO() {
    super();
    setBackground(Mic.bg);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.frame = frame;
    inPanel.setLayout(new BorderLayout());
    outPanel.setLayout(new BorderLayout());
    inLab = new JLabel(" Input Console");
    inPanel.add(inLab, BorderLayout.NORTH);
    inPanel.add(new JScrollPane(inText), BorderLayout.CENTER);
    outLab = new JLabel(" Output Console");
    outPanel.add(outLab, BorderLayout.NORTH);
    outPanel.add(new JScrollPane(outText), BorderLayout.CENTER);
    inText.addCaretListener(this);
    outText.setEditable(false);
    inPanel.add(new JScrollPane(inText), BorderLayout.CENTER);
    outPanel.add(new JScrollPane(outText), BorderLayout.CENTER);
    add(inPanel);
    add(Box.createHorizontalStrut(5));
    add(outPanel);
    setup();
  }
  
  public void setup() {
    inText.setFont(font[Mic.reslvl]);
    outText.setFont(font[Mic.reslvl]);
    inLab.setFont(labFont[Mic.reslvl]);
    outLab.setFont(labFont[Mic.reslvl]);
  }

  public void caretUpdate(CaretEvent e) {
    int oldDot = dot;
    dot = e.getDot();
    if (oldDot < dot) in.append(inText.getText().substring(oldDot, dot));
  }

  public void reset() {reset(false);}
  public void reset(boolean soft) {
    charPtr = 0;
    outText.setText("");
    if (soft) {
      oldPollCount = pollCount;
      redoMode = true;
      return;
    }
    oldPollCount = pollCount = dot = 0;
    redoMode = false;
    in = new StringBuffer();
    inText.setText("");
  }

  public byte get() {
    if (redoMode && oldPollCount > 0) {
      oldPollCount--;
      return (byte)0;
    }
    if (charPtr >= in.length()) {
      pollCount++;
      return (byte)0;
    }
    byte x = (byte)in.charAt(charPtr++);
    return x;
  }

  public void put(byte x) {
    if (x == 0) return;
    outText.append(String.valueOf((char)x));
  }
}
