/*  $Id$
 *
 *  MicCanvas.java
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
 * Simulator diagram
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
class MicCanvas extends JPanel {
  public final static Color OUTCOL = Color.lightGray;
  public final static Color INCOL = Color.darkGray;  
  public final static Color OUTHCOL = Color.red;
  public final static Color INHCOL = Color.blue;
  public final static Color MEMCOL = Color.white;
  public final static Color MEMHCOL = Color.green;
  public final static int REGLEN = 10;
  public final static int[] REGCOL = {35, 15};
  public final static int[] REGROW = {50, 25};
  public final static int[] ABBUSHT = {525, 262};
  public final static int[] REGECOL = {REGCOL[0]+150, REGCOL[1]+90};
  public final static int[] ABUSCOL = {REGECOL[0]-5, REGECOL[1]+10};
  public final static int[] BBUSCOL = {REGECOL[0]+90, REGECOL[1]+60};
  public final static int[] CBUSCOL = {REGCOL[0]+50, REGCOL[1]+40};
  public final static int[] CBUSHT = {ABBUSHT[0]+165, ABBUSHT[1]+83};
  public final static int[] BBUSWID = {45, 22}, CBUSWID = {40, 20}, MBUSWID = {75, 38};
  public final static int[] TOP = {50, 70};
  public final static int[] WD = {380, 233}, HT = {760, 461};
  public final static Font[] numFont = {MicReg.dispFont[0].deriveFont(16.0f),
				 MicReg.dispFont[0].deriveFont(10.0f)};
  public final static Font[] font = {new Font("MS Sans Serif", Font.BOLD, 16),
			      new Font("MS Sans Serif", Font.BOLD, 11)};
  public final static String[] REG_NAMES =
  {"MAR", "MDR", "PC", "MBR", "SP", "LV", "CPP", "TOS", "OPC", "H"};
  public final static int MAR = 0,  MDR = 1,  PC = 2, MBR = 3, SP = 4, LV = 5,
    CPP = 6, TOS = 7, OPC = 8, H = 9;

  public MicReg regList[] = new MicReg[REGLEN], shift;
  public Bus[] cbuses = new Bus[REGLEN-1], bbuses = new Bus[REGLEN-1];
  public Bus abus, bbus, cbus, obus0, obus1, obus2, marBus, mdrBus, pcBus, mbrBus;
  public Mic frame;
  public int aLatch, bLatch, initSP = 0x8010, initLV = 0x8000, initCPP = 0x4000, initPC = 0xffffffff;
  public CtlCanvas ctlcvs;

  public MicCanvas(Mic frame) {
    super();
    setLayout(null);
    this.frame = frame;
    setup();
  }

  public void setup() {
    removeAll();
    int reslvl = frame.reslvl;
    setBounds(5, 5, WD[reslvl], HT[reslvl]);
    setBackground(Color.white);
    JLabel lab = new JLabel("C Bus");
    lab.setFont(font[reslvl]);
    lab.setBounds(CBUSCOL[reslvl], TOP[reslvl]-30, 50, 20);
    add(lab);
    lab = new JLabel("B Bus");
    lab.setFont(font[reslvl]);
    lab.setBounds(BBUSCOL[reslvl], TOP[reslvl]-30, 50, 20);
    add(lab);
    lab = new JLabel("A Bus");
    lab.setFont(font[reslvl]);
    lab.setBounds(ABUSCOL[reslvl]-resAdj(50, 45), ABBUSHT[reslvl]+resAdj(20, 60), 50, 20);
    add(lab);
    setFont(font[reslvl]);
    abus = new Bus(ABUSCOL[reslvl], ABBUSHT[reslvl]+resAdj(10, 50),
		   (reslvl == 0) ? 41 : 20, Bus.DOWN, OUTCOL, OUTHCOL);
    bbus = new Bus(BBUSCOL[reslvl], TOP[reslvl], ABBUSHT[reslvl], Bus.DOWN, OUTCOL, OUTHCOL);
    cbus = new Bus(CBUSCOL[reslvl], TOP[reslvl], CBUSHT[reslvl], Bus.VPLAIN, INCOL, INHCOL);
    obus0 = new Bus((ABUSCOL[reslvl]+BBUSCOL[reslvl]+resAdj(10, -2))/2,
		    TOP[reslvl]+ABBUSHT[reslvl]+resAdj(60, 30),
		    resAdj(30, 15), Bus.DOWN, OUTCOL, OUTHCOL);
    obus1 = new Bus((ABUSCOL[reslvl]+BBUSCOL[reslvl]+resAdj(10, -2))/2,
		    TOP[reslvl]+ABBUSHT[reslvl]+resAdj(124, 62),
		    resAdj(34, 19), Bus.VPLAIN, INCOL, INHCOL);
    obus2 = new Bus(CBUSCOL[reslvl]+resAdj(10, 2),
		    TOP[reslvl]+CBUSHT[reslvl]-resAdj(13, 5), 
		    resAdj(149, 88), Bus.HPLAIN,INCOL, INHCOL);
    shift = new MicReg("Shifter", ((ABUSCOL[reslvl]+BBUSCOL[reslvl])/2)-resAdj(93, 75),
		       TOP[reslvl]+ABBUSHT[reslvl]+resAdj(90, 45), false);
    Alu alu = new Alu(ABUSCOL[reslvl]-6, TOP[reslvl]+ABBUSHT[reslvl],
		      BBUSCOL[reslvl]+resAdj(35, 18)-ABUSCOL[reslvl], resAdj(60, 30));
    add(abus);
    add(bbus);
    add(cbus);
    add(alu);
    add(shift);
    add(obus0);
    add(obus1);
    add(obus2);
    for (int i = 0; i < REGLEN; i++) {
      regList[i] = new MicReg(REG_NAMES[i], REGCOL[reslvl]-10, TOP[reslvl] + i*REGROW[reslvl], true, (i == 3));
      add(regList[i]);
      if (i < 9) {
	if (i == 3) 
	  bbuses[i] = new Bus(BBUSCOL[reslvl]+Bus.BREADTH[reslvl]/4-BBUSWID[reslvl],
			      TOP[reslvl]+i*REGROW[reslvl]+resAdj(18, 9),
			      BBUSWID[reslvl], Bus.RIGHT, OUTCOL, OUTHCOL);
	else if (i < 3) 
	  bbuses[i] = new Bus(BBUSCOL[reslvl]+Bus.BREADTH[reslvl]/4-BBUSWID[reslvl], TOP[reslvl]+(i+1)*REGROW[reslvl],
			      BBUSWID[reslvl], Bus.RIGHT, OUTCOL, OUTHCOL);
	else bbuses[i] = new Bus(BBUSCOL[reslvl]+Bus.BREADTH[reslvl]/4-BBUSWID[reslvl], TOP[reslvl]+i*REGROW[reslvl],
				 BBUSWID[reslvl], Bus.RIGHT, OUTCOL, OUTHCOL);
	add(bbuses[i]);
      }
      if (i < 9) {
	if (i < 3)
	  cbuses[i] = new Bus(CBUSCOL[reslvl]+3*Bus.BREADTH[reslvl]/4, TOP[reslvl]+i*REGROW[reslvl],
			      CBUSWID[reslvl], Bus.RIGHT, INCOL, INHCOL);
	else
	  cbuses[i] = new Bus(CBUSCOL[reslvl]+3*Bus.BREADTH[reslvl]/4, TOP[reslvl]+(i+1)*REGROW[reslvl],
			      CBUSWID[reslvl], Bus.RIGHT, INCOL, INHCOL);
	add(cbuses[i]);
      }
      switch (i) {
      case 0:
	marBus = new Bus(CBUSCOL[reslvl]+3*Bus.BREADTH[reslvl]/4-resAdj(31, 15),
			 TOP[reslvl]+i*REGROW[reslvl]+resAdj(18, 9),
			 MBUSWID[reslvl], Bus.LEFT, MEMCOL, MEMHCOL);
	add(marBus);
	break;
      case 1:
	mdrBus = new Bus(CBUSCOL[reslvl]+3*Bus.BREADTH[reslvl]/4-resAdj(31, 15),
			 TOP[reslvl]+i*REGROW[reslvl]+resAdj(18, 9),
			 MBUSWID[reslvl], Bus.HDOUBLE, MEMCOL, MEMHCOL);
	add(mdrBus);
	break;
      case 2:
	pcBus = new Bus(CBUSCOL[reslvl]+3*Bus.BREADTH[reslvl]/4-resAdj(31, 15),
			TOP[reslvl]+i*REGROW[reslvl]+ resAdj(18, 9),
			MBUSWID[reslvl], Bus.LEFT, MEMCOL, MEMHCOL);
	add(pcBus);
	break;
      case 3:
	mbrBus = new Bus(CBUSCOL[reslvl]+3*Bus.BREADTH[reslvl]/4-resAdj(31, 15),
			 TOP[reslvl]+i*REGROW[reslvl]+resAdj(18, 9),
			 MBUSWID[reslvl], Bus.RIGHT, MEMCOL, MEMHCOL);
	add(mbrBus);
	break;
      }
    }
    init();
  }

  public void init() {
    for (int i = 0; i < REGLEN; i++) regList[i].value = 0;
    pc().value = initPC;
    sp().value = initSP;
    lv().value = initLV;
    regList[CPP].value = initCPP;
    shift.value = 0;
    clearInHilite();
    outHilite(-1);
    aBusHilite(false);
    bBusHilite(false);
    cBusHilite(false);
    shiftBusHilite(false);
    marBusHilite(false);
    mdrBusHilite(false);
    pcBusHilite(false);
    mbrBusHilite(false);
  }

  public int getInitSP() {return initSP;}
  public int getInitLV() {return initLV;}
  public int getInitCPP() {return initCPP;}
  public void setInitSP(int val) {initSP = val;}
  public void setInitLV(int val) {initLV = val;}
  public void setInitCPP(int val) {initCPP = val;}


  public MicReg mar() {return regList[MAR];}
  public MicReg mdr() {return regList[MDR];}
  public MicReg mbr() {return regList[MBR];}
  public MicReg pc() {return regList[PC];}
  public MicReg sp() {return regList[SP];}
  public MicReg lv() {return regList[LV];}
  public MicReg cpp() {return regList[CPP];}

  public void aBusHilite(boolean on) {
    abus.hiliteFlag = on;
  }
  public void bBusHilite(boolean on) {
    bbus.hiliteFlag = on;
  }
  public void shiftBusHilite(boolean on) {
    obus0.hiliteFlag = on;
  }
  public void cBusHilite(boolean on) {
   obus1.hiliteFlag = on;
   obus2.hiliteFlag = on;
   cbus.hiliteFlag = on;
  }
  public void marBusHilite(boolean on) {
    marBus.hiliteFlag = on;
  }
  public void mdrBusHilite(boolean on) {
    mdrBus.hiliteFlag = on;
  }
  public void mdrBusHiliteDir(boolean left) {
    mdrBus.leftFlag = left;
  }
  public void pcBusHilite(boolean on) {
    pcBus.hiliteFlag = on;
  }
  public void mbrBusHilite(boolean on) {
    mbrBus.hiliteFlag = on;
  }
  public void outHilite(int idx) {
    for (int i = 0; i < bbuses.length; i++)
      bbuses[i].hiliteFlag = false;
    if (idx >= 0) bbuses[idx].hiliteFlag = true;
  }
  public void clearInHilite() {
    for (int i = 0; i < cbuses.length; i++)
      cbuses[i].hiliteFlag = false;
  }
  public void inHilite(int idx) {
    if (idx >= 0) cbuses[idx].hiliteFlag = true;
  }

  public static int resAdj(int x, int y) {
    return (Mic.reslvl == 0) ? x : y;
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawRect(0,0,getWidth()-1,getHeight()-1);
    g.drawString("SLL8: "+Integer.toString(ctlcvs.oldSLL8), shift.getX()+resAdj(160, 110),
		 shift.getY()+resAdj(10, 5));
    g.drawString("SRA1: "+Integer.toString(ctlcvs.oldSRA1), shift.getX()+resAdj(160, 110),
		 shift.getY()+resAdj(30, 18));
  }

  class Alu extends JPanel {
    private int x, y, WD, HT;
    public Alu(int x, int y, int WD, int HT) {
      this.x = x;
      this.y = y;
      this.WD = WD;
      this.HT = HT;
      setBounds(x, y, WD+45, HT+1);
    }

    public void paintComponent(Graphics g) {
      g.setFont(font[Mic.reslvl]);
      String s = MicTools.aluInfo(ctlcvs.oldAlu);
      g.drawString(s, WD/2-getFontMetrics(getFont()).stringWidth(s)/2-resAdj(3,0), resAdj(30, 27));
      s = MicTools.formatHex(ctlcvs.clatch, 8);
      g.setFont(numFont[Mic.reslvl]);
      g.drawString(s, WD/2-resAdj(39, 23), resAdj(50, 16));
      int[] xpoints = new int[9];
      int[] ypoints = new int[9];
      xpoints[0]=0;   xpoints[1]=resAdj(35,18);   xpoints[2]=resAdj(40,20);    xpoints[3]=WD-resAdj(40,20);
      ypoints[0]=0;     ypoints[1]=0;  ypoints[2]=resAdj(10,5);    ypoints[3]=resAdj(10,5);
      xpoints[4]=WD-resAdj(35,18); xpoints[5]=WD; xpoints[6]=WD-resAdj(25, 13);
      xpoints[7]=resAdj(25, 13);
      ypoints[4]=0;     ypoints[5]=0;  ypoints[6]=HT;    ypoints[7]=HT;
      xpoints[8]=xpoints[0];
      ypoints[8]=ypoints[0];
      g.drawPolygon(xpoints, ypoints, 9);    
      g.setFont(font[Mic.reslvl]);
      g.drawString("N: "+Integer.toString(ctlcvs.N), WD+resAdj(10,5), resAdj(15, 7));
      g.drawString("Z: "+Integer.toString(ctlcvs.Z), WD+resAdj(10,5), resAdj(40, 20));
    }
  }
}

class Bus extends JPanel {  
  public final static int LEFT = 0, RIGHT = 1, UP = 2, DOWN = 3, HPLAIN = 4, HDOUBLE = 6,
    VPLAIN = 5;
  public final static int[] BREADTH = {16, 8};
  private int dir;
  public boolean hiliteFlag = false;
  public boolean leftFlag = false;
  public Color bg, hilite;
    
  public Bus(int left, int top, int len, int dir) {
    this(left, top, len, dir, Color.black, MicCanvas.OUTHCOL);
  }

  public Bus(int left, int top, int len, int dir, Color bg, Color hilite) {
    this.dir = dir;
    this.bg = bg;
    this.hilite = hilite;
    switch (dir) {
    case LEFT:
    case RIGHT:
    case HPLAIN:
    case HDOUBLE:
      setBounds(left, top, len, BREADTH[Mic.reslvl]);
      break;
    case UP:
    case DOWN:
    case VPLAIN:
      setBounds(left, top, BREADTH[Mic.reslvl], len);
      break;
    }
  }

  public void paintComponent(Graphics g) {
    int reslvl = Mic.reslvl;
    int[] xpoints = new int[5];
    int[] ypoints = new int[5];
    g.setColor((hiliteFlag) ? hilite : bg);
    switch (dir) {
    case LEFT:
      int x1 = BREADTH[reslvl]/2;
      g.fillRect(x1, BREADTH[reslvl]/4, getWidth()-x1, BREADTH[reslvl]/2);
      xpoints[0] = x1;         xpoints[1] = x1; xpoints[2] = 0;          xpoints[3] = x1;
      ypoints[0] = BREADTH[reslvl]/4;  ypoints[1] = 0;  ypoints[2] = BREADTH[reslvl]/2;  ypoints[3] = BREADTH[reslvl];
      xpoints[4] = x1;
      ypoints[4] = 3*BREADTH[reslvl]/4;
      g.fillPolygon(xpoints, ypoints, 4);
      g.setColor((hiliteFlag) ? hilite : Color.black);
      g.drawLine(x1, BREADTH[reslvl]/4, getWidth(), BREADTH[reslvl]/4);
      g.drawLine(getWidth()-1, BREADTH[reslvl]/4, getWidth()-1, 3*BREADTH[reslvl]/4);
      g.drawLine(x1, 3*BREADTH[reslvl]/4, getWidth()-1, 3*BREADTH[reslvl]/4);
      g.drawPolyline(xpoints, ypoints, 5);
      break;
    case RIGHT:
      x1 = getWidth()-(BREADTH[reslvl]/2);
      g.fillRect(0, BREADTH[reslvl]/4+1, x1, BREADTH[reslvl]/2);
      xpoints[0] = x1;        xpoints[1] = x1;  xpoints[2] = getWidth(); xpoints[3] = x1;
      ypoints[0] = BREADTH[reslvl]/4; ypoints[1] = 0;   ypoints[2] = BREADTH[reslvl]/2;  ypoints[3] = BREADTH[reslvl];
      xpoints[4] = x1;
      ypoints[4] = 3*BREADTH[reslvl]/4;
      g.fillPolygon(xpoints, ypoints, 4);
      g.setColor((hiliteFlag) ? hilite : Color.black);
      g.drawLine(0, BREADTH[reslvl]/4, x1, BREADTH[reslvl]/4);
      g.drawLine(0, BREADTH[reslvl]/4, 0, 3*BREADTH[reslvl]/4);
      g.drawLine(0, 3*BREADTH[reslvl]/4, x1, 3*BREADTH[reslvl]/4);
      g.drawPolyline(xpoints, ypoints, 5);
      break;
    case HDOUBLE:
      x1 = BREADTH[reslvl]/2;
      g.fillRect(x1, BREADTH[reslvl]/4, getWidth()-2*x1, BREADTH[reslvl]/2);
      if (!hiliteFlag || (hiliteFlag && leftFlag)) {
	xpoints[0] = x1;         xpoints[1] = x1; xpoints[2] = 0;          xpoints[3] = x1;
	ypoints[0] = BREADTH[reslvl]/4;  ypoints[1] = 0;  ypoints[2] = BREADTH[reslvl]/2;  ypoints[3] = BREADTH[reslvl];
	xpoints[4] = x1;
	ypoints[4] = 3*BREADTH[reslvl]/4;
	g.fillPolygon(xpoints, ypoints, 4);
	g.setColor((hiliteFlag) ? hilite : Color.black);
	g.drawLine(x1, BREADTH[reslvl]/4, getWidth()-x1, BREADTH[reslvl]/4);
	g.drawLine(x1, 3*BREADTH[reslvl]/4, getWidth()-x1, 3*BREADTH[reslvl]/4);
	g.drawPolyline(xpoints, ypoints, 5);
      }
      g.setColor((hiliteFlag) ? hilite : bg);
      if (!hiliteFlag || (hiliteFlag && !leftFlag)) {
	x1 = getWidth()-(BREADTH[reslvl]/2);
	xpoints[0] = x1;        xpoints[1] = x1;  xpoints[2] = getWidth(); xpoints[3] = x1;
	ypoints[0] = BREADTH[reslvl]/4; ypoints[1] = 0;   ypoints[2] = BREADTH[reslvl]/2;  ypoints[3] = BREADTH[reslvl];
	xpoints[4] = x1;       	  
	ypoints[4] = 3*BREADTH[reslvl]/4; 
	g.fillPolygon(xpoints, ypoints, 5);
	g.setColor((hiliteFlag) ? hilite : Color.black);
	g.drawLine(BREADTH[reslvl]/2, BREADTH[reslvl]/4, x1, BREADTH[reslvl]/4);
	g.drawLine(BREADTH[reslvl]/2, 3*BREADTH[reslvl]/4, x1, 3*BREADTH[reslvl]/4);
	g.drawPolyline(xpoints, ypoints, 5);
      }
      break;
    case UP:
      int y1 = BREADTH[reslvl]/2;
      g.fillRect(BREADTH[reslvl]/4, y1, BREADTH[reslvl]/2, getHeight()-y1);
      xpoints[0] = BREADTH[reslvl]/4; xpoints[1] = 0;   xpoints[2] = BREADTH[reslvl]/2; xpoints[3] = BREADTH[reslvl];
      ypoints[0] = y1;        ypoints[1] = y1;  ypoints[2] = 0;         ypoints[3] = y1;
      xpoints[4] = 3*BREADTH[reslvl]/4;
      ypoints[4] = y1;
      g.fillPolygon(xpoints, ypoints, 4);
      g.setColor((hiliteFlag) ? hilite : Color.black);
      g.drawLine(BREADTH[reslvl]/4, y1, BREADTH[reslvl]/4, getHeight());
      g.drawLine(BREADTH[reslvl]/4, getHeight()-1, 3*BREADTH[reslvl]/4, getHeight()-1);
      g.drawLine(3*BREADTH[reslvl]/4, y1, 3*BREADTH[reslvl]/4, getHeight()-1);
      g.drawPolyline(xpoints, ypoints, 5);
      break;
    case DOWN:
      y1 = getHeight()-(BREADTH[reslvl]/2);
      g.fillRect(BREADTH[reslvl]/4+1, 0, BREADTH[reslvl]/2, y1);
      xpoints[0] = BREADTH[reslvl]/4; xpoints[1] = 0;  xpoints[2] = BREADTH[reslvl]/2;   xpoints[3] = BREADTH[reslvl];
      ypoints[0] = y1;        ypoints[1] = y1; ypoints[2] = getHeight(); ypoints[3] = y1;
      xpoints[4] = 3*BREADTH[reslvl]/4;
      ypoints[4] = y1;
      g.fillPolygon(xpoints, ypoints, 4);
      g.setColor((hiliteFlag) ? hilite : Color.black);
      g.drawLine(BREADTH[reslvl]/4, 0, BREADTH[reslvl]/4, y1);
      g.drawLine(BREADTH[reslvl]/4, 0, 3*BREADTH[reslvl]/4, 0);
      g.drawLine(3*BREADTH[reslvl]/4, 0, 3*BREADTH[reslvl]/4, y1);
      g.drawPolyline(xpoints, ypoints, 5);
      break;
    case HPLAIN:
      g.fillRect(0, BREADTH[reslvl]/4+1, getWidth(), BREADTH[reslvl]/2);
      g.setColor((hiliteFlag) ? hilite : Color.black);
      g.drawRect(0, BREADTH[reslvl]/4+1, getWidth()-1, BREADTH[reslvl]/2-1);
      break;
    case VPLAIN:
      g.fillRect(BREADTH[reslvl]/4, 0, BREADTH[reslvl]/2, getHeight());
      g.setColor((hiliteFlag) ? hilite : Color.black);
      g.drawRect(BREADTH[reslvl]/4, 0, BREADTH[reslvl]/2-1, getHeight()-1);
      break;
    }	
  }
}

    


