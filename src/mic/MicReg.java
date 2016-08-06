/*  $Id$
 *
 *  MicReg.java
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
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Registers
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class MicReg extends JPanel {
  public static Font[] dispFont = {new Font("Courier New", Font.BOLD, 18),
			    new Font("Courier New", Font.BOLD, 12)};
  public static Font[] labFont = {new Font("MS Sans Serif", Font.BOLD, 16),
			   new Font("MS Sans Serif", Font.BOLD, 11)};
  public static BasicStroke mbrStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
						 BasicStroke.JOIN_MITER, 1.0f,
						 new float[]{10.0f, 10.0f}, 0.0f);
  public final static int[] WID = {95, 60};
  public final static int[] HT = {35, 18};
  public final static int[] LNAMESPACE = {115, 80}, SNAMESPACE = {60, 45}; 

  public int value = 0;

  private String name;
  private boolean spflg = true, mbr = false;

  public MicReg(String name, int bx, int by) {
    this(name, bx, by, true, false);
  }

  public MicReg(String name, int bx, int by, boolean spflg) {
    this(name, bx, by, spflg, false);
  }

  public MicReg(String name, int bx, int by, boolean spflg, boolean mbr) {
    super();
    setBackground(Color.white);
    setLayout(new BorderLayout());
    //    setLayout(null);
    JLabel lab = new JLabel(((Mic.reslvl == 1 && spflg) ? "   " : "")+name);
    //    lab.setLocation(160, /* MicCanvas.resAdj(20, 60),*/ 30);
    lab.setFont(labFont[Mic.reslvl]);
    //    add(lab);
    add(lab, BorderLayout.WEST);
    this.name = name;
    this.mbr = mbr;
    setFont(dispFont[Mic.reslvl]);
    int spc = (spflg) ? LNAMESPACE[Mic.reslvl] : getFontMetrics(getFont()).stringWidth(name)+2;
    setLocation(bx, by);
    setSize(new Dimension(spc+WID[Mic.reslvl], HT[Mic.reslvl]));
    this.spflg = spflg;
  }

  public void paintComponent(Graphics g)
  {
    FontMetrics fm = g.getFontMetrics();
    int spc = (spflg) ? LNAMESPACE[Mic.reslvl] : SNAMESPACE[Mic.reslvl];
    if (mbr) {
      Graphics2D g1 = (Graphics2D)g;
      Stroke holdStroke = g1.getStroke();
      g1.setStroke(mbrStroke);
      g1.drawRect(spc,0,3*WID[Mic.reslvl]/4-3,HT[Mic.reslvl]-1);
      g1.setStroke(holdStroke);
      g1.drawRect(spc+3*WID[Mic.reslvl]/4-3, 0, WID[Mic.reslvl]/4+3, HT[Mic.reslvl]-1);
    } else 
      g.drawRect(spc,0,WID[Mic.reslvl]-1,HT[Mic.reslvl]-1);
    int top = (mbr) ? 2 : 8;
    String valString = MicTools.formatHex(value, top);
    g.drawString(valString, spc-2+WID[Mic.reslvl]-fm.stringWidth(valString), 3*HT[Mic.reslvl]/4);
  }
}
