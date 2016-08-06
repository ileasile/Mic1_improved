/* $Id: HtmlHelp.java,v 1.2 2005/02/22 05:28:24 RMS Exp $
 *
 *  HtmlHelp.java
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
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.swing.border.*;

/**
 * Html help frame
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
*/
class HtmlHelp extends JDialog implements ActionListener {
  public final static int WID = 680, HT = 550;
  public static HtmlHelp htmlFrame = null;
  private final JEditorPane pane;
  private JButton close = new JButton("Close"), back = new JButton("Back"),
    forward = new JButton("Forward");
  private WebHistory whist;

  public HtmlHelp(Mic frame, URL url) {
    super(frame, Mic.title+" Help", false);
    if (htmlFrame != null) {
      return;
    }
    try {
      pane = new JEditorPane(url);
    } catch (IOException ex) {
      ex.printStackTrace();
      return;
    }
    pane.setEditable(false);
    pane.setBorder(new EmptyBorder(15, 15, 15, 15));
    pane.addHyperlinkListener(new HyperlinkListener() {
	public void hyperlinkUpdate(HyperlinkEvent e) {
	  if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	    JEditorPane pane = (JEditorPane) e.getSource();
	    if (e instanceof HTMLFrameHyperlinkEvent) {
	      HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
	      HTMLDocument doc = (HTMLDocument)pane.getDocument();
	      doc.processHTMLFrameHyperlinkEvent(evt);
	    } else {
	      try {
		pane.setPage(e.getURL());
		whist.add(e.getURL());
	      } catch (Throwable t) {
		t.printStackTrace();
	      }
	    }
	  }}});
    JScrollPane spane = new JScrollPane(pane);
    getContentPane().add(spane, BorderLayout.CENTER);
    close.addActionListener(this);
    JPanel south = new JPanel();
    JPanel north = new JPanel();
    JPanel tmp = new JPanel();
    forward.setEnabled(false);
    back.setEnabled(false);
    tmp.setLayout(new GridLayout(1,2,2,0));
    tmp.add(back);
    tmp.add(forward);
    north.add(tmp);
    getContentPane().add(north, BorderLayout.NORTH);
    south.add(close);
    getContentPane().add(south, BorderLayout.SOUTH);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setSize(WID, HT);
    setLocationRelativeTo(frame);
    whist = new WebHistory(forward, back, pane, url);
    htmlFrame = this;
    setVisible(true);
  }

  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSED)
      htmlFrame = null;
    super.processWindowEvent(e);
  }

  public void actionPerformed(ActionEvent e) {
    hide();
    dispose();
  }
}

class WebHistory implements ActionListener {
  Vector v = new Vector();
  int idx = 0;
  JButton forward, back;
  JEditorPane pane;

  public WebHistory(JButton forward, JButton back, JEditorPane pane, URL url) {
    this.forward = forward;
    this.back = back;
    this.pane = pane;
    forward.addActionListener(this);
    back.addActionListener(this);
    v.add(url);
  }
    
  public void add(URL url) {
    forward.setEnabled(false);
    for (int j = v.size()-1; j > idx; j--) v.removeElementAt(j);
    v.add(url);
    idx++;
    back.setEnabled(true);
  }

  public void back() {
    try {
      pane.setPage((URL)v.elementAt(--idx));
      forward.setEnabled(true);
      if (idx == 0) back.setEnabled(false);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public void forward() {
    try {
      pane.setPage((URL)v.elementAt(++idx));
      back.setEnabled(true);
      if (idx == v.size()-1) forward.setEnabled(false);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == forward) forward();
    else back();
  }
}
