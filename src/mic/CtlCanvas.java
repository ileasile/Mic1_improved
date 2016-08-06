/*  $Id: CtlCanvas.java,v 1.2 2005/02/22 05:28:24 RMS Exp $
 *
 *  CtlCanvas.java
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
 * Control panel and program control
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
class CtlCanvas extends JPanel {
  public final static int[] WID = {600, 375};
  public final static int[] HTH = {200, 100};
  public final static int[] MIRX = {20, 10};
  public final static int[] MIRY = {60, 30};
  public final static int[] MIRHT = {30, 15};
  public final static int[] MIRWID = {WID[0]-2*MIRX[0], WID[1]-2*MIRX[1]};
  public final static int MIRLEN = 16;
  public final static int PHASEMAX = 3;
  public final static int PHASEMODE = 0;
  public final static int MICMODE = 1;
  public final static int MACMODE = 2;
  public final static int PROGMODE = 3;
  public final static int ADDR = 0, JMPC = 1, JAMN = 2, JAMZ = 3, SLL8 = 4, SRA1 = 5, F = 6, ENA = 7,
    ENB = 8, INVA = 9, INC =10, C = 11, WR = 12, RD = 13, FT = 14, B = 15;
  
  public byte oldAlu = 0, oldSLL8 = 0, oldSRA1 = 0;
  public   int mpc = 0, phasectr = 0, pgmsize = 0, micphasectr = 0, marHold = 0, ftchHold = 0,
    clatch = 0, macidx = 0, N = 0, Z = 0, procmode = MICMODE, micctr = 0, macctr = 0;
  public   Mic frame = null;
  public   MicCanvas canvas = null;
  public   MicCtlPanel ctlpanel;

  private int mir[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
  private int delay = 100, demoDelay = 500;
  private String cmnt = "";
  private   File micIn = null, macIn = null;
  private   Mic1Instruction[] cmdlist = new Mic1Instruction[0];
  private   boolean rding = false, wrting = false, ftching = false, macflg = false, resetFlag = false, stopFlag = false,
    running = false, delayFlag = true, prevFlag = false, breakflg = false;
  private   MicMemory memory = null;
  private   Hashtable breakpoints = new Hashtable();
  private   String mirtitle0[] = {"A","J","J","J","S","S","F","E","E","I","I","C","W","R","F","B"};
  private   String mirtitle1[] = {"D","M","A","A","L","R"," ","N","N","N","N"," ","R","D","E"," "};
  private   String mirtitle2[] = {"D","P","M","M","L","A"," ","A","B","V","C"," "," "," ","T"," "};
  private   String mirtitle3[] = {"R","C","N","Z","8","1"," "," "," ","A"," "," "," "," "," "," "};
  private   String mirtitle4[] = {"(9)"," "," "," "," "," ","(2) "," "," "," "," ","(9)"," "," "," ","(4)"};

  private   Thread rThread = null;

  public CtlCanvas(Mic frame, MicCanvas canvas)
  {
    super();
    setBackground(Color.white);
    this.canvas = canvas;
    this.frame = frame;
    setFont(canvas.font[Mic.reslvl]);
    setSize(new Dimension(WID[frame.reslvl], HTH[frame.reslvl]));
  }
  
  public Mic1Instruction getCmd(int i) {return (i < cmdlist.length) ? cmdlist[i] : null;}
  public int cmdLength() {return cmdlist.length;}

  public void setDemoDelay(int demoDelay) {this.demoDelay = demoDelay;}
  public int getDemoDelay() {return demoDelay;}
  
  public void setMemory(MicMemory memory) {this.memory = memory;}
  public void setDelayFlag(boolean delayFlag) {this.delayFlag = delayFlag;}
  public void setProcmode(int procmode) {
    this.procmode = procmode;
    if (procmode != PROGMODE) breakflg = false;
    frame.reset();
    canvas.init();
    repaint();
  }
  public int getProcmode() {return procmode;}
  public void stop() {setStopFlag(true);}

  public void setResetFlag(boolean flag) {resetFlag = flag;}
  public boolean getResetFlag() {return resetFlag;}
  public void setStopFlag(boolean flag) {stopFlag = flag;}
  public boolean getStopFlag() {return stopFlag;}

  public boolean demomode() {return (procmode == PHASEMODE) || ((procmode <= MACMODE) && delayFlag);}
  private boolean progmode() {return procmode == PROGMODE;}
  private boolean demoMicMode() {return delayFlag && procmode == MICMODE;}
  private boolean demoMacMode() {return delayFlag && procmode == MACMODE;}
  private boolean demoProgMode() {return delayFlag && procmode == PROGMODE;}
  private boolean fastProgMode() {return !delayFlag && procmode == PROGMODE;}
  private boolean animateMode() {return delayFlag && procmode != PROGMODE;}
  private boolean storeMode() {return procmode < MACMODE;}
  private boolean stepMode() {return procmode <= MACMODE;}
  private boolean goMode() {return procmode <= PROGMODE;}

  public void clearBreakPoints() {
    breakpoints = new Hashtable();
  }

  public boolean isBreakPoint(int n){
    return (breakpoints.get(new Integer(n)) != null);
  }

  public String getBreakPoints() {
    StringBuffer ans = new StringBuffer();
    for (Enumeration en = breakpoints.elements(); en.hasMoreElements();) {
      Integer x = (Integer)en.nextElement();
      ans.append(MicTools.formatHex(x.intValue(), 4)+"\n");
    }
    return ans.toString();
  }

  public void setBreakPoints(String s){
    if (s == null) return;
    StringTokenizer tok = new StringTokenizer(s);
    breakpoints = new Hashtable();
    while (tok.hasMoreTokens()) {
      String t = tok.nextToken();
      try {
	Integer x = new Integer(Integer.parseInt(t, 16));
	breakpoints.put(x, x);
      } catch (NumberFormatException ex) {}
    }
  }

  public void init() {
    init(false);
  }
  public void init(boolean soft)
  {
    clearOldAlu();
    mpc = phasectr = N = Z = 0;
    macidx = -1;
    breakflg = ftching = rding = false;
    if (cmdlist.length > 0) parseMic(cmdlist[mpc]);
    String s = MicTools.formatHex(mpc, 4);
    cmnt = "0x"+s+":   " + cmnt;
    if (soft) return;
    micphasectr = micctr = macctr = 0;
  }

  public void readMic(File f)
    throws IOException, BadMagicNumberException,
	   MicFormatException {
    FileInputStream instr = new FileInputStream(f) ;
    DataInputStream in = new DataInputStream(instr) ;
    readMic(in);
  }
    
  public void readMic(DataInputStream in)
    throws IOException, BadMagicNumberException, 
	   MicFormatException
  {
    int magic = 0;
    magic = in.read( );
    for (int i = 0; i < 3; i++) {
      int c = in.read( );
      magic = magic * 256 + c;
    }
    if (magic != 0x12345678) {
      throw new BadMagicNumberException();
    }
    Vector cmdvec = new Vector();
    Mic1Instruction mi = new Mic1Instruction() ;
    for(int i = 0 ; mi.read(in) != - 1; i++) {
      cmdvec.add(mi);
      mi = new Mic1Instruction() ;
    }
    if (cmdvec.size() == 0) throw new MicFormatException("Empty file");
    cmdlist = new Mic1Instruction[0];
    cmdlist = (Mic1Instruction[])cmdvec.toArray(cmdlist);
    pgmsize = cmdlist.length;
    phasectr = PHASEMAX;
    in.close();
    reset();
    MicText.setMicrocode(this);
  }

  public static int resAdj(int x, int y) {
    return (Mic.reslvl == 0) ? x : y;
  }

  public void paintComponent(Graphics g) {
    setFont(canvas.font[Mic.reslvl]);
    int reslvl = frame.reslvl;
    super.paintComponent(g);
    g.drawRect(0,0,getWidth()-1, getHeight()-1);
    FontMetrics fm = getFontMetrics(MicReg.dispFont[reslvl]);
    g.drawString("MPC:   "+cmnt, MIRX[reslvl], MIRY[reslvl]-resAdj(35,17));
    if (storeMode()) frame.mStoHilite(mpc);
    if (breakflg) {
      g.setColor(Color.red);
      g.drawString("BREAK", MIRX[reslvl]+resAdj(400,260), MIRY[reslvl]-resAdj(35,17));
      g.setColor(Color.black);
    }
    if (demomode())
      g.drawString("Subcycle "+Integer.toString(phasectr), MIRX[reslvl]+resAdj(100,50), MIRY[reslvl]-resAdj(10,5));
    g.drawString("MIR", MIRX[reslvl], MIRY[reslvl]-resAdj(10,5));
    g.drawRect(MIRX[reslvl], MIRY[reslvl], MIRWID[reslvl], MIRHT[reslvl]);
    int mirx = resAdj(30,15);
    Font holdFont = g.getFont();
    for (int i = 0; i < MIRLEN; i++) {
      String valstr = Integer.toHexString(mir[i]);
      g.setFont(MicReg.dispFont[reslvl]);
      g.drawString(valstr, MIRX[reslvl]+resAdj(6,3)+mirx-fm.stringWidth(valstr)/2,
		   MIRY[reslvl]+MIRHT[reslvl]-resAdj(8,3));
      g.setFont(this.getFont());
      //      g.setFont(holdFont);
      g.drawString(mirtitle0[i], MIRX[reslvl]+mirx, MIRY[reslvl]+2*MIRHT[reslvl]-resAdj(10,5));
      g.drawString(mirtitle1[i], MIRX[reslvl]+mirx, MIRY[reslvl]+2*MIRHT[reslvl]+resAdj(5,4));
      g.drawString(mirtitle2[i], MIRX[reslvl]+mirx, MIRY[reslvl]+2*MIRHT[reslvl]+resAdj(20,13));
      g.drawString(mirtitle3[i], MIRX[reslvl]+mirx, MIRY[reslvl]+2*MIRHT[reslvl]+resAdj(35,22));
      g.drawString(mirtitle4[i], MIRX[reslvl]+mirx-3, MIRY[reslvl]+2*MIRHT[reslvl]+resAdj(65,34));
      mirx += resAdj(20, 15);
      if (i == 0) mirx += resAdj(30,15);
      else if (i == 3 || i == 3 || i == 5 || i == 6 || i == 14) mirx += resAdj(20,10);
      else if (i == 10 || i == 11) mirx += resAdj(30,15);
    }
    String s = (macidx >= 0 && macidx < memory.byteVals.length) ? memory.cmts[macidx] : "";
    if (s != null) g.drawString(MicTools.formatHex(macidx, 4)+": "+s, MIRX[reslvl]+resAdj(400,230), MIRY[reslvl]-resAdj(10,5));
  }

  public void quit() {
    quit(false);
  }

  public void quit(boolean done) {
    running = false;
    ctlpanel.reset(done);
    setStopFlag(false);
    if (getResetFlag()) doReset();
    setResetFlag(false);
  }

  public void reset() {
    if (running) {
      setResetFlag(true);
    }
    else doReset();
  }

  public void doReset() {
    doReset(false);
  }
  public void doReset(boolean soft) {
    init(soft);
    canvas.init();
    frame.reset();
    memory.reset();
    memory.mmio.reset(soft);
    setResetFlag(false);
    frame.repaint();
  }

  private void setOldAlu() {
    int b = 0;
    b = b | (mir[F] << 4) | (mir[ENA] << 3) | ( mir[ENB] << 2) | (mir[INVA] << 1) | mir[INC];
    oldAlu = (byte)b;
    oldSLL8 = (byte)mir[SLL8];
    oldSRA1 = (byte)mir[SRA1];
  }

  private void clearOldAlu() {
    oldSLL8 = oldSRA1 = oldAlu = 0;
  }

  private void macProg() {
    if (breakflg) {
      breakflg = false;
      repaint();
    }
    do {
      macCycle();
    } while (!getResetFlag() && !getStopFlag() && canvas.pc().value != 0xff00 && mpc != 0xff &&
	     !isBreakPoint(macidx));
    breakflg = isBreakPoint(macidx);
    memory.reTrack();
    quit(mpc == 0xff);
    frame.repaint();
  }

  private void macCycle() {
    macctr++;
    do {
      micCycle();
      if (macflg == true) break;
    } while (!getResetFlag() && !getStopFlag());
    if (demoMacMode()) quit();
  }

  private void micCycle() {
    micctr++;
    for (int i = 0; i <= PHASEMAX; i++) {
      micPhase();
      if (animateMode()) {
	frame.repaint();
	try {
	  Thread.sleep(demoDelay);
	} catch (Exception ex) {}
      }
    }
    if (demoMicMode()) quit();
    if (!fastProgMode() && !prevFlag) frame.repaint();
  }

  private void micPhase() {
    micphasectr++;
    if (phasectr < PHASEMAX) {
      if (phasectr == 0) clearOldAlu();
      if (phasectr == 1) setOldAlu();
      process(++phasectr);
      if (procmode == MICMODE) frame.repaint();
      return;
    }
    if (demomode() || demoProgMode()) {
      canvas.clearInHilite();
      if (!wrting) canvas.mdrBusHilite(false);
      canvas.mbrBusHilite(false);
    } else if (!wrting) {
      canvas.marBusHilite(false);
    }
    canvas.cBusHilite(false);
    if (mpc == pgmsize) return;
    if (macflg == true) macidx = canvas.pc().value;
    phasectr = 0;
    parseMic(cmdlist[mpc]);
    String s = MicTools.formatHex(mpc, 4);
    cmnt = "0x"+s+":   " + cmnt;
  }

  private void processTo(int steps) {
    init();
    canvas.init();
    switch (procmode) {
    case PHASEMODE:
      for (int i = 0; i < steps; i++) micPhase();
      break;
    case MICMODE:
      for (int i = 0; i < steps; i++) micCycle();
      break;
    case MACMODE:
      for (int i = 0; i < steps; i++) macCycle();
      break;
    case PROGMODE:
      reset();
      break;
    }
    frame.repaint();
  }

  public void nextCmd() {
    if (running) {
      setStopFlag(true);
    }
    rThread = null;
    switch (procmode) {
    case PHASEMODE:
      micPhase();
      break;
    case MICMODE:
      if (demomode()) {
	running = true;
	rThread = new Thread(new MicThread());
      } else micCycle();
      break;
    case MACMODE:
      if (demomode()) {
	running = true;
	rThread = new Thread(new MacThread());
      } else  macCycle();
      break;
    case PROGMODE:
      rThread = new Thread(new ProgThread());
      break;
    }
    if (rThread != null) rThread.start();
    frame.repaint();  
  }

  public void prevCmd() {
    int temp = 0;
    switch (procmode) {
    case PHASEMODE:
      temp = Math.max(micphasectr-1, 0);
      break;
    case MICMODE:
      temp = Math.max(micctr-1, 0);
      break;
    case MACMODE:
      temp = Math.max(macctr-1, 0);
      break;
    }
    boolean oldDelay = delayFlag;
    boolean oldTrack = memory.getTracking();
    delayFlag = false;
    prevFlag = true;
    memory.setTracking(false);
    doReset(true);
    processTo(temp);
    delayFlag = oldDelay;
    memory.setTracking(oldTrack);
    if (oldTrack) memory.reTrack();
    prevFlag = false;
    frame.repaint();
  }

  public void parseMic(Mic1Instruction mi) {
    if (mi == null) return;
    mir[ADDR] = mi.NEXT_ADDRESS;
    mir[JMPC] = (mi.JMPC) ? 1 : 0;
    mir[JAMN] = (mi.JAMN) ? 1 : 0;
    mir[JAMZ] = (mi.JAMZ) ? 1 : 0;
    mir[SLL8] = (mi.SLL8) ? 1 : 0;
    mir[SRA1] = (mi.SRA1) ? 1 : 0;
    mir[F]    = (mi.F0 && mi.F1) ? 3 : (mi.F0 && !mi.F1) ? 2 : (!mi.F0 & mi.F1) ? 1 : 0;
    mir[ENA]  = (mi.ENA) ? 1 : 0;
    mir[ENB]  = (mi.ENB) ? 1 : 0;
    mir[INVA] = (mi.INVA) ? 1 : 0;
    mir[INC]  = (mi.INC) ? 1 : 0;
    mir[C]    = computeC(mi);
    mir[WR]   = (mi.WRITE) ? 1 : 0;
    mir[RD]   = (mi.READ) ? 1 : 0;
    mir[FT]   = (mi.FETCH) ? 1 : 0;
    mir[B]    = mi.B;
    cmnt = mi.toString();;
  }

  private int computeC(Mic1Instruction mi) {
    int ans = 0;
    if (mi.H) ans = ans | 0x100;
    if (mi.OPC) ans = ans | 0x80;
    if (mi.TOS) ans = ans | 0x40;
    if (mi.CPP) ans = ans | 0x20;
    if (mi.LV) ans = ans | 0x10;
    if (mi.SP) ans = ans | 0x8;
    if (mi.PC) ans = ans | 0x4;
    if (mi.MDR) ans = ans | 0x2;
    if (mi.MAR) ans = ans | 0x1;
    return ans;
  }

  private void feedC(int val, int mask) {
    if ((mask & 0x100) != 0) {
      canvas.regList[MicCanvas.H].value = val;
      canvas.inHilite(8);
    }
    if ((mask & 0x80) != 0) {
      canvas.regList[MicCanvas.OPC].value = val;
      canvas.inHilite(7);
    }
    if ((mask & 0x40) != 0) {
      canvas.regList[MicCanvas.TOS].value = val;
      canvas.inHilite(6);
    }
    if ((mask & 0x20) != 0) {
      canvas.regList[MicCanvas.CPP].value = val;
      canvas.inHilite(5);
    }
    if ((mask & 0x10) != 0) {
      canvas.regList[MicCanvas.LV].value = val;
      canvas.inHilite(4);
    }
    if ((mask & 0x8) != 0) {
      canvas.regList[MicCanvas.SP].value = val;
      canvas.inHilite(3);
    }
    if ((mask & 0x4) != 0) {
      canvas.regList[MicCanvas.PC].value = val;
      canvas.inHilite(2);
    }
    if ((mask & 0x2) != 0) {
      canvas.regList[MicCanvas.MDR].value = val;
      canvas.inHilite(1);
    }
    if ((mask & 0x1) != 0) {
      canvas.regList[MicCanvas.MAR].value = val;
      canvas.inHilite(0);
    }
  }

  private int getB(int idx) {
    if (idx > 3) return canvas.regList[idx].value;
    if (idx < 2) return canvas.regList[idx+1].value;
    int tmp = canvas.mbr().value & 0xff;
    if (idx == 3) return tmp;
    if ((tmp & 0x80) != 0) return tmp | 0xffffff00;
    return tmp;
  }

  private void process(int phasectr) {
    macflg = false;
    switch (phasectr) {
      
    case 1:
      canvas.aLatch = 0;
      canvas.bLatch = 0;
      if (mir[ENA] != 0) {
	canvas.aLatch = canvas.regList[MicCanvas.H].value;
	canvas.aBusHilite(true);
      } else canvas.aBusHilite(false);
      if (mir[INVA] != 0) canvas.aLatch = ~canvas.aLatch;
      if (mir[ENB] != 0) {
	canvas.bLatch = getB(mir[B]);
	canvas.outHilite(mir[B]);
	if (demomode()) canvas.bBusHilite(true);
      } else {
	canvas.outHilite(-1);
      }
      break;
    case 2:
      if (demomode()) {
	canvas.shiftBusHilite(true);
      }
      clatch = 
	(mir[F] == 0) ? canvas.aLatch & canvas.bLatch :
	  (mir[F] == 1) ? canvas.aLatch | canvas.bLatch :
	    (mir[F] == 2) ? ~canvas.bLatch : canvas.aLatch + canvas.bLatch;
      if (mir[INC] != 0) clatch++;
      N = (clatch & 0x80000000) >>> 31;
      Z = (clatch == 0) ? 1 : 0;
      canvas.shift.value = (mir[SLL8] != 0) ? (clatch << 8) : clatch;
      if (mir[SRA1] != 0) canvas.shift.value = (canvas.shift.value >> 1);
      break;
    case 3:
      canvas.bBusHilite(false);
      canvas.shiftBusHilite(false);
      canvas.clearInHilite();
      if (demomode()) {
	canvas.clearInHilite();
	canvas.aBusHilite(false);
	canvas.outHilite(-1);
	canvas.cBusHilite(true);
      }
      feedC(canvas.shift.value, mir[C]);
      mpc = mir[ADDR] & 0x1ff;
      boolean hiBit = (mir[JAMZ] == 1 && Z == 1) || (mir[JAMN] == 1 && N == 1);
      if (hiBit) mpc = mpc | 0x100;
      if (mir[JMPC] == 1) mpc = (canvas.mbr().value & 0xff) | mpc;
      if (mir[ADDR] == 2 || mir[ADDR] == 0xff || ((canvas.mbr().value & 0xff) == MicTools.wideOp && mir[ADDR] == 0)) {   // wide fix
	macflg = true;
      }
      if (procmode == MICMODE) {
	canvas.mdrBusHilite(false);
	canvas.mbrBusHilite(false);
      }
      if (rding) {
	canvas.mdr().value = memory.read(marHold << 2);
	if (goMode()) {
	  canvas.marBusHilite(false);
	  canvas.mdrBusHilite(true);
	  canvas.mdrBusHiliteDir(false);
	} 
      }
      if (wrting) {
	if (goMode()) {
	  canvas.marBusHilite(false);
	  canvas.mdrBusHilite(false);
	} 
      }
      if (ftching) {
	canvas.mbr().value = memory.fetch(ftchHold, !fastProgMode());
	if (goMode()) {
	  canvas.pcBusHilite(false);
	  canvas.mbrBusHilite(true);
	}
      }
      if (demoProgMode()) 
	try {
	  Thread.sleep(delay);
	} catch (Exception e) {}
      rding = false;
      ftching = false;
      wrting = false;
      if (mir[RD] != 0) {
	rding = true;
	marHold = canvas.mar().value;
	if (goMode()) {
	  canvas.marBusHilite(true);
	}
      }
      if (mir[WR] != 0) {
	memory.write(canvas.mar().value << 2, canvas.mdr().value, !fastProgMode());
	wrting = true;
	if (goMode()) {
	  canvas.marBusHilite(true);
	  canvas.mdrBusHilite(true);
	  canvas.mdrBusHiliteDir(true);
	}
      }
      if (mir[FT] != 0) {
	ftching = true;
	ftchHold = canvas.pc().value;
	if (goMode()) canvas.pcBusHilite(true);
      }
      break;
    }
  }

  class MicThread implements Runnable {
    public void run() {
      running = true;
      micCycle();
    }
  }

  class MacThread implements Runnable {
    public void run() {
      running = true;
      macCycle();
    }
  }

  class ProgThread implements Runnable {
    public void run() {
      running = true;
      macProg();
    }
  }

}
