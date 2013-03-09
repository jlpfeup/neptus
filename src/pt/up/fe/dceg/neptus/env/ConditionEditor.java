/*
 * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: ZP 
 * 2005/08/03
 */
package pt.up.fe.dceg.neptus.env;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import pt.up.fe.dceg.neptus.util.GuiUtils;
/**
 * @author zp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConditionEditor extends JPanel {

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JButton okBtn = null;
	private JButton cancelBtn = null;
	private JTextPane conditionText = null;
	private JLabel jLabel = null;
	private JComboBox<?> varCombo = null;
	private JLabel jLabel1 = null;
	private Environment env = null;
	
	public ConditionEditor(Condition condition) {
		this(condition.getEnv(), condition.getConditionText());
	}
	
	/**
	 * This is the default constructor
	 */
	public ConditionEditor(Environment env, String initialCondition) {
		super();
		this.env = env;
		getConditionText().setText(initialCondition);
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(300,200);
		this.add(getJPanel(), java.awt.BorderLayout.SOUTH);
		this.add(getJPanel1(), java.awt.BorderLayout.CENTER);
		this.add(getJPanel2(), java.awt.BorderLayout.NORTH);
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			FlowLayout flowLayout2 = new FlowLayout();
			jPanel = new JPanel();
			jPanel.setLayout(flowLayout2);
			flowLayout2.setHgap(10);
			flowLayout2.setAlignment(java.awt.FlowLayout.RIGHT);
			jPanel.add(getOkBtn(), null);
			jPanel.add(getCancelBtn(), null);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jLabel = new JLabel();
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));
			jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
			jLabel.setText("Condition:");
			jPanel1.add(jLabel, null);
			jPanel1.add(getConditionText(), null);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jLabel1 = new JLabel();
			FlowLayout flowLayout3 = new FlowLayout();
			jPanel2 = new JPanel();
			jPanel2.setLayout(flowLayout3);
			jLabel1.setText("Variables:");
			flowLayout3.setAlignment(java.awt.FlowLayout.LEFT);
			flowLayout3.setHgap(10);
			jPanel2.add(jLabel1, null);
			jPanel2.add(getVarCombo(), null);
		}
		return jPanel2;
	}
	/**
	 * This method initializes okBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getOkBtn() {
		if (okBtn == null) {
			okBtn = new JButton();
			okBtn.setText("OK");
			okBtn.setPreferredSize(new java.awt.Dimension(75,25));
			okBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Scriptable scope = env.getScope();
					Context cx = Context.enter();
					try {
						Object result = cx.evaluateString(scope, conditionText.getText(), "<condition>", 0, null);
						GuiUtils.errorMessage(SwingUtilities.getRoot((Component)e.getSource()), "Result of condition", "Condition returned "+result);
					}
					catch (Exception exception) {
						GuiUtils.errorMessage(SwingUtilities.getRoot((Component)e.getSource()), "Error parsing condition", exception.getMessage());
					}
				}
			});
		}
		return okBtn;
	}
	/**
	 * This method initializes cancelBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getCancelBtn() {
		if (cancelBtn == null) {
			cancelBtn = new JButton();
			cancelBtn.setText("Cancel");
			cancelBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFrame frame = (JFrame)SwingUtilities.getRoot((Component) e.getSource());
					frame.setVisible(false);
					frame.dispose();
				}
			});

		}
		return cancelBtn;
	}
	/**
	 * This method initializes conditionText	
	 * 	
	 * @return javax.swing.JTextPane	
	 */    
	private JTextPane getConditionText() {
		if (conditionText == null) {
			conditionText = new JTextPane();
			conditionText.setPreferredSize(new java.awt.Dimension(250,40));
		}
		return conditionText;
	}
	/**
	 * This method initializes varCombo	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox<?> getVarCombo() {
		if (varCombo == null) {
			varCombo = new JComboBox<Object>(env.getVariableNames());
			varCombo.setPreferredSize(new java.awt.Dimension(110,21));
			varCombo.setEditable(false);
			varCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getConditionText().setText(getConditionText().getText()+ " "+varCombo.getSelectedItem());
				}
			});
		}
		return varCombo;
	}
	
	
	
	
	public static void main(String args[]) {
		Environment env = new Environment();
		//env.putEnv("x", new Double(0.3));
		//env.putEnv("y", new Double(-12.7));
		//env.putEnv("VinteTres", new Integer(23));
		//env.putEnv("Verdadeiro", new Boolean(true));
		
		GuiUtils.testFrame(new ConditionEditor(env, "vintetres"), "Teste");
	}
}
