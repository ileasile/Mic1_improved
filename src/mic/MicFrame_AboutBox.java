/*  $Id$
 *
 *  MicFrame_AboutBox.java
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
import javax.swing.*;
import javax.swing.border.*;

/**
 * "About" panel
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class MicFrame_AboutBox extends JDialog implements ActionListener {

  private JPanel panel1 = new JPanel();
  private JPanel panel2 = new JPanel();
  private JPanel insetsPanel1 = new JPanel();
  private JPanel insetsPanel2 = new JPanel();
  private JPanel insetsPanel3 = new JPanel();
  private JButton button1 = new JButton();
  private JLabel imageControl1 = new JLabel();
  private ImageIcon imageIcon;
  private JLabel label1 = new JLabel();
  private JLabel label2 = new JLabel();
  private JLabel label3 = new JLabel();
  private JLabel label4 = new JLabel();
  private JLabel label5 = new JLabel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private FlowLayout flowLayout1 = new FlowLayout();
  private FlowLayout flowLayout2 = new FlowLayout();
  private GridLayout gridLayout1 = new GridLayout();
  private String product = Mic.title+" Simulator";
  private String version = "Version "+Mic.version;
  private String copyright = "Copyright (c) 2002-2005";
  private String comments1 = "Richard M. Salter";
  private String comments2 = "rms@cs.oberlin.edu";
  public MicFrame_AboutBox(Frame parent) {
    super(parent);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try  {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    imageControl1.setIcon(imageIcon);
    pack();
  }

  private void jbInit() throws Exception  {
    imageIcon = new ImageIcon(getClass().getResource("resources/mic.gif"));
    this.setTitle("About");
    setResizable(false);
    panel1.setLayout(borderLayout1);
    panel2.setLayout(borderLayout2);
    insetsPanel1.setLayout(flowLayout1);
    insetsPanel2.setLayout(flowLayout1);
    insetsPanel2.setBorder(new EmptyBorder(10, 10, 10, 10));
    gridLayout1.setRows(5);
    gridLayout1.setColumns(1);
    label1.setText(product);
    label2.setText(version);
    label3.setText(copyright);
    label4.setText(comments1);
    label5.setText(comments2);
    insetsPanel3.setLayout(gridLayout1);
    insetsPanel3.setBorder(new EmptyBorder(10, 60, 10, 10));
    button1.setText("OK");
    button1.addActionListener(this);
    insetsPanel2.add(imageControl1, null);
    panel2.add(insetsPanel2, BorderLayout.WEST);
    this.getContentPane().add(panel1, null);
    insetsPanel3.add(label1, null);
    insetsPanel3.add(label2, null);
    insetsPanel3.add(label3, null);
    insetsPanel3.add(label4, null);
    insetsPanel3.add(label5, null);
    panel2.add(insetsPanel3, BorderLayout.CENTER);
    insetsPanel1.add(button1, null);
    panel1.add(insetsPanel1, BorderLayout.SOUTH);
    panel1.add(panel2, BorderLayout.NORTH);
  }

  protected void processWindowEvent(WindowEvent e) {
    if(e.getID() == WindowEvent.WINDOW_CLOSING) {
      cancel();
    }
    super.processWindowEvent(e);
  }

  void cancel() {
    dispose();
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == button1) {
      cancel();
    }
  }
} 
