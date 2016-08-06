/*  $Id$
 *
 *  MicCtlPanel.java
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
import java.awt.*;
import java.io.*;
import java.awt.event.*;

/**
 * Control panel
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class MicCtlPanel extends JPanel implements ActionListener {
  final static Color bg = Color.cyan;
  final static Font[] font = {new Font("Helvetica", Font.BOLD, 12),
			      new Font("Helvetica", Font.BOLD, 9)};
  JRadioButton[] delay = {new JRadioButton("Off"), new JRadioButton("On")},
    speed = {new JRadioButton("SubClock"), new JRadioButton("Clock"),
	     new JRadioButton("IJVM"), new JRadioButton("Prog")};
  ButtonGroup delayGroup = new ButtonGroup(),
    speedGroup = new ButtonGroup();
  int[] initChoice = {0, 1};
  JButton reset = new JButton(new ImageIcon(getClass().getResource("resources/reset.gif"))),
    left = new JButton(new ImageIcon(getClass().getResource("resources/larrow.gif"))),
    right = new JButton(new ImageIcon(getClass().getResource("resources/rarrow.gif"))),
    stop = new JButton(new ImageIcon(getClass().getResource("resources/stop.gif")));
  Box choiceBox = Box.createHorizontalBox();
  JPanel btnPanel = new JPanel();
  CtlCanvas ctlcvs;

  public MicCtlPanel(CtlCanvas ctlcvs) {
    super();
    this.ctlcvs = ctlcvs;
    setBackground(bg);
    init();
  }

  public void init() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(new EmptyBorder(2,7,2,3));
    choiceBox.setBackground(bg);
    for (int i = 0; i < 2; i++) {
      delay[i].setBackground(bg);
      delay[i].addActionListener(this);
      delayGroup.add(delay[i]);
    }
    for (int i = 0; i < 4; i++) {
      speed[i].setBackground(bg);
      speed[i].addActionListener(this);
      speedGroup.add(speed[i]);
    }
    btnPanel.setBackground(bg);
    btnPanel.add(reset);
    btnPanel.add(Box.createHorizontalStrut(5));
    btnPanel.add(left);
    btnPanel.add(Box.createHorizontalStrut(5));
    btnPanel.add(right);
    btnPanel.add(Box.createHorizontalStrut(5));
    btnPanel.add(stop);
    reset.addActionListener(this);
    left.addActionListener(this);
    right.addActionListener(this);
    stop.addActionListener(this);
    stop.setEnabled(false);
    setup();
    delay[initChoice[0]].doClick();
    speed[initChoice[1]].doClick();
  }
   
  public void setup() {
    removeAll();
    choiceBox.removeAll();
    setFont(font[Mic.reslvl]);
    JLabel lab = new JLabel("Delay:");
    lab.setFont(font[Mic.reslvl]);
    choiceBox.add(lab);
    choiceBox.add(Box.createHorizontalStrut(MicCanvas.resAdj(10,2)));
    for (int i = 0; i < 2; i++) {
      delay[i].setFont(font[Mic.reslvl]);
      choiceBox.add(delay[i]);
    }
    choiceBox.add(Box.createHorizontalStrut(MicCanvas.resAdj(20,10)));
    lab = new JLabel("Speed:");
    lab.setFont(font[Mic.reslvl]);
    choiceBox.add(lab);
    choiceBox.add(Box.createHorizontalStrut(MicCanvas.resAdj(10,2)));
    for (int i = 0; i < 4; i++) {
      speed[i].setFont(font[Mic.reslvl]);
      choiceBox.add(speed[i]);
    }
    add(choiceBox);
    add(btnPanel);
    add(Box.createVerticalStrut(2));
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == speed[0]) {
      ctlcvs.setProcmode(CtlCanvas.PHASEMODE);
    } else if (e.getSource() == speed[1]) {
      ctlcvs.setProcmode(CtlCanvas.MICMODE);
    } else if (e.getSource() == speed[2]) {
      ctlcvs.setProcmode(CtlCanvas.MACMODE);
    } else if (e.getSource() == speed[3]) {
      ctlcvs.setProcmode(CtlCanvas.PROGMODE);
    } else if (e.getSource() == right) {
      if (ctlcvs.getProcmode() == CtlCanvas.PROGMODE ||
	  (ctlcvs.demomode() && (ctlcvs.getProcmode() != CtlCanvas.PHASEMODE))) {
	left.setEnabled(false);
	right.setEnabled(false);
	stop.setEnabled(true);
      }
      ctlcvs.nextCmd();
    } else if (e.getSource() == left)
      ctlcvs.prevCmd();
      else if (e.getSource() == reset) {
      ctlcvs.reset();
      reset();
      stop.setEnabled(false);
    } else if (e.getSource() == stop) {
      stop.setEnabled(false);
      ctlcvs.stop();
    } else if (e.getSource() == delay[0])
      ctlcvs.setDelayFlag(false);
    else if (e.getSource() == delay[1])
      ctlcvs.setDelayFlag(true);
  }


  public void reset() {
    reset(false);
  }

  public void reset(boolean done) {
    left.setEnabled(!done);
    right.setEnabled(!done);
    stop.setEnabled(false);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawRect(0,0,getWidth()-1,getHeight()-1);
  }
}

