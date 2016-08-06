/*  $Id$
 *
 *  MicMemory.java
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
 * Simulator memory
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
public class MicMemory {
  public final static int BYTE = 0, CONST = 1, STACK = 2;
  public final static int TOP = 15, WID[] = CtlCanvas.WID;
  public final static int[][] MAXROW = {{12, 4, 8}, {8, 2, 4}};
  public final static int MEMX = 30;
  public final static int[] MEMSKIP = {15, 10};
  public final static int MMIOADDR = 0xfffffff4;
  public final static Color THILITE = Color.red;
  public final static Color BHILITE = Color.blue;
  public final static Color SHILITE = Color.green.darker();
  public final static String[] disText = {" Method Area", " Constant Pool", " Stack Area"};
  public final static int[][] panelHt = {{0, 100, 160}, {0, 70, 90}};
  public final static Font[] memFont =  {new Font("Courier New", Font.BOLD, 14),
				  new Font("Courier New", Font.BOLD, 9)},
    cmtFnt = {new Font("Helvetica", Font.BOLD, 14),
	      new Font("Helvetica", Font.BOLD, 9)};
  public byte[] byteVals;
  public String[] cmts;
  public MMIO mmio;
  public JPanel display = new JPanel(), subDisplay = new JPanel();
  private int[] startAddr = {0, 0x10000, 0x20000};
  private int[] sz = new int[3];
  private boolean tracking = true, hilighting[] = {true, true, true};
  private CtlCanvas ctlcvs;
  private int[] constVals, constHold, stackVals;
  private Vector progLst;
  private FontMetrics memFm;
  private JScrollPane[] scrollPane = new JScrollPane[3];
  private JPanel[] displayPanel = {new JPanel(), new JPanel(), new JPanel()};

  public MicMemory(int cz, int sz, CtlCanvas ctlcvs) {
    this.ctlcvs = ctlcvs;
    byteVals = new byte[cz];
    constVals = new int[cz];
    constHold = new int[cz];
    stackVals = new int[sz];
    cmts = new String[cz];
    display.setLayout(new BorderLayout());
    buildDisplay();
    memFm = displayPanel[0].getFontMetrics(displayPanel[0].getFont());
  }

  public void setStartAddr(int idx, int val) {
    if (val == startAddr[idx]) return;
    startAddr[idx] = val;
    buildDisplay(idx, (idx == 0) ? byteVals.length : (idx == 1) ? constVals.length : stackVals.length);
    ctlcvs.frame.repaint();
  }
  public int getStartAddr(int idx) {return startAddr[idx];}
  public void setTracking(boolean tracking) {this.tracking = tracking;}
  public boolean getTracking(){return tracking;}
  public void setHilighting(int idx, boolean val) {
    this.hilighting[idx] = val;
    if (idx == 2) hilighting[1] = val;
  }
  public boolean getHilighting(int idx){return hilighting[idx];}
  public void setConstValsLength(int len) {
    if (len == constVals.length) return;
    int[] tmp = new int[len];
    constHold = new int[len];
    for (int i = 0; i < Math.min(tmp.length, constVals.length); i++) 
      constHold[i] = tmp[i] = constVals[i];
    constVals = tmp;
    buildDisplay(CONST, len);
  }
  public int getConstValsLength() {return constVals.length;}
  public void setStackValsLength(int len) {
    if (len == stackVals.length) return;
    int[] tmp = new int[len];
    for (int i = 0; i < Math.min(tmp.length, stackVals.length); i++) tmp[i] = stackVals[i];
    stackVals = tmp;
    buildDisplay(STACK, len);
  }
  public int getStackValsLength() {return stackVals.length;}

  public void reset() {
    for (int i = 0; i < stackVals.length; i++) stackVals[i] = 0;
    for (int i = 0; i < constVals.length; i++) constVals[i] = constHold[i];
    for (int i = 0; i < 3; i++) setTrack(i, 0);
  }

  private byte fetch(int addr) {
    return fetch(addr, false);
  }

  public byte fetch(int addr, boolean track) {
    if (addr < byteVals.length && addr >= 0) {
      if (track) setTrack(0, addr);
      return byteVals[addr];
    }
    return (byte)-1;
  }

  public int read(int addr) {
    if (addr == MMIOADDR) return mmio.get() & 0xff;
    if (addr < startAddr[1]) {
      if (addr < byteVals.length-3 && addr >= 0) {
	int ans = byteVals[addr+3] & 0xff;
	ans = ans | ((byteVals[addr+2] << 8) & 0xff00);
	ans = ans | ((byteVals[addr+1] << 16) & 0xff0000);
	ans = ans | ((byteVals[addr] << 24) & 0xff000000);
	//	setTrack(0, addr);
	return ans;
      }
      return 0;
    }
    if (addr < startAddr[2]) {
      int idx = (addr - startAddr[1]) >>> 2;
      if (idx >= constVals.length) return 0;
      //      setTrack(1, idx);
      return constVals[idx];
    }
    int idx = (addr - startAddr[2]) >>> 2;
    if (idx >= stackVals.length) return 0;
    //    setTrack(2, idx);
    return stackVals[idx];
  }

  public void write(int addr, int val) {
    write(addr, val, false, true);
  }

  public void write(int addr, int val, boolean track) {
    write(addr, val, track, false);
  }

  public void write(int addr, int val, boolean track, boolean fast) {
    if (addr == MMIOADDR) mmio.put((byte)val);
    if (addr < startAddr[1]) {
      if (addr < byteVals.length-3 && addr >= 0) {
	byteVals[addr+3] = (byte)(val & 0xff);
	byteVals[addr+2] = (byte)((val & 0xff00) >> 8);
	byteVals[addr+1] = (byte)((val & 0xff0000) >> 16);
	byteVals[addr] = (byte)((val & 0xff000000) >> 24);
      }
      if (track) setTrack(0, addr);
      if (!fast) cmts = MicTools.disAssemble(byteVals);
      return;
    }
    if (addr < startAddr[2]) {
      int idx = (addr - startAddr[1]) >>> 2;
      if (idx < constVals.length) {
	if (track) setTrack(1, idx);
	constVals[idx] = val;
      }
      return;
    }
    int idx = (addr - startAddr[2]) >>> 2;
    if (idx < stackVals.length) {
      if (track) setTrack(2, idx);
      stackVals[idx] = val;
    }
  }

  public void reTrack() {
    setTrack(0, ctlcvs.frame.canvas.pc().value);
    setTrack(2, (ctlcvs.frame.canvas.sp().value * 4 - startAddr[2]) >>> 2);
  }

  public void setTrack(int idx, int addr) {
    if (!tracking) return;
    JScrollBar scroll = scrollPane[idx].getHorizontalScrollBar();
    if (scroll == null) return;
    int myPage = (addr/(3*MAXROW[Mic.reslvl][idx]));
    MicMemoryDisplay mmd = (MicMemoryDisplay)scrollPane[idx].getViewport().getView();
    if (mmd.page == myPage) return;
    mmd.page = myPage;
    scroll.setValue(myPage * WID[Mic.reslvl]);
  }

  public void readMac(File f) throws BadMagicNumberException, IOException {
    ctlcvs.reset();
    ctlcvs.clearBreakPoints();
    IJVMLoader loader = new IJVMLoader(f.getPath());
    int top = 0;
    byte[] prog = loader.getProgram();
    int progsz = prog.length;
    for (int i = 0; i < progsz; i++) {
      if (i < startAddr[1]) {
	if (top == 0  && i > 2 && prog[i] == 0 && prog[i-1] == 0 &&
	    prog[i-2] == 0) top = i-2;
      } else {
	int ans = prog[i+3] & 0xff;
	ans = ans | ((prog[i+2] << 8) & 0xff00);
	ans = ans | ((prog[i+1] << 16) & 0xff0000);
	ans = ans | ((prog[i] << 24) & 0xff000000);
	write(i, ans);
	if (i >= startAddr[1] && i < startAddr[2]) {
	  int idx = (i - startAddr[1]) >>> 2;
	  if (idx < constHold.length) constHold[idx] = ans;
	}
	i += 3;
      }
    }
    byteVals = new byte[top];
    for (int i = 0; i < top; i++) byteVals[i] = prog[i];
    cmts = MicTools.disAssemble(byteVals);
    buildDisplay(BYTE, top);
  }

  public void buildDisplay() {
    int[] memshow = {byteVals.length, constVals.length, stackVals.length};
    for (int i = 0; i < 3; i++) buildDisplay(i, memshow[i]);
  }

  public void buildDisplay(int idx, int top) {
    display.removeAll();
    displayPanel[idx] = new JPanel();
    displayPanel[idx].setLayout(new BorderLayout());
    MicMemoryDisplay mmd =
      new MicMemoryDisplay(idx, MAXROW[Mic.reslvl][idx], idx==0, startAddr[idx], top);
    mmd.setBackground(Color.white);
    scrollPane[idx] = new JScrollPane(mmd);
    displayPanel[idx].add(scrollPane[idx], BorderLayout.CENTER);
    JLabel lab = new JLabel(disText[idx]);
    lab.setFont(MMIO.labFont[Mic.reslvl]);
    displayPanel[idx].setPreferredSize(new Dimension(5*CtlCanvas.WID[Mic.reslvl], panelHt[Mic.reslvl][idx]));      
    displayPanel[idx].add(lab, BorderLayout.NORTH);
    subDisplay.removeAll();
    display.add(displayPanel[0], BorderLayout.CENTER);
    subDisplay.setLayout(new BorderLayout());
    subDisplay.add(displayPanel[1], BorderLayout.NORTH);
    subDisplay.add(displayPanel[2], BorderLayout.CENTER);
    display.add(subDisplay, BorderLayout.SOUTH);
    display.validate();
  }

  class MicMemoryDisplay extends JPanel {
    int idx, maxrow, startAddr, memshow, page = 0;
    boolean byteLevel;
    
    public MicMemoryDisplay(int idx, int maxrow, boolean byteLevel, int startAddr, int memshow) {
      this.idx = idx;
      this.maxrow = maxrow;
      this.byteLevel = byteLevel;
      this.startAddr = startAddr;
      this.memshow = memshow;
      setPreferredSize(new Dimension((memshow/(3*maxrow)+1)*CtlCanvas.WID[Mic.reslvl], panelHt[Mic.reslvl][idx]-40));      
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      int colptr = 0;
      Color hold = g.getColor();
      for (int i = 0; i < memshow; i++) {
	int col = (i/maxrow) * WID[Mic.reslvl]/3;
	int row = i - (i/maxrow) * maxrow;
	if (hilighting[idx]) {
	  if (byteLevel && ctlcvs.macidx == i || ctlcvs.canvas.sp().value == (i+startAddr/4)) 
	    g.setColor(THILITE);
	  else if (idx != 0 &&
		   ((idx == 2 && ctlcvs.canvas.lv().value == (i+startAddr/4)) ||
		    (idx == 1 && ctlcvs.canvas.cpp().value == (i+startAddr/4)))) 
	    g.setColor(BHILITE);
	  else if (idx == 2 && ctlcvs.canvas.lv().value < (i+startAddr/4) &&
				ctlcvs.canvas.sp().value > (i+startAddr/4))
	    g.setColor(SHILITE);
	  else if (byteLevel && ctlcvs.isBreakPoint(i+startAddr))
	    g.setColor(BHILITE);
	}
	g.setFont(memFont[Mic.reslvl]);
	String s;
	if (byteLevel) {
	  int addr = startAddr + i;
	  byte val = fetch(addr);
	  String sval = MicTools.formatHex(val, 2);
	  s = MicTools.formatHex(addr, 4)+": "+sval+" "+
	    ((cmts[i] != null) ? cmts[i] : "");
	} else {
	  int addr = startAddr + i * 4;
	  int val = read(addr);
	  String sval = MicTools.formatHex(val, 8);
	  s = MicTools.formatHex(addr, 8)+": "+sval;
	}
	g.drawString(s, col+5, TOP+row*MEMSKIP[Mic.reslvl]);
	g.setColor(hold);
      }
    }
  }
}
