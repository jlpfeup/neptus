/*
 * Copyright (c) 2004-2015 Universidade do Porto - Faculdade de Engenharia
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
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
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
 * Author: jpereira
 * Feb 20, 2015
 */
package pt.lsts.neptus.plugins.pingerLocator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Console;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import oracle.jrockit.jfr.JFRStats;

import org.j3d.renderer.java3d.geom.Box;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;

import com.rabbitmq.client.Command;

import pt.lsts.neptus.comm.manager.imc.ImcSystem;
import pt.lsts.neptus.comm.manager.imc.ImcSystemsHolder;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.PluginDescription.CATEGORY;
import pt.lsts.neptus.plugins.Popup;
import pt.lsts.neptus.plugins.Popup.POSITION;
import pt.lsts.neptus.renderer2d.ILayerPainter;
import pt.lsts.neptus.renderer2d.LayerPriority;
import pt.lsts.neptus.renderer2d.Renderer2DPainter;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.types.vehicle.VehicleType.SystemTypeEnum;
import pt.lsts.neptus.util.GuiUtils;

/**
 * @author jpereira
 *
 */
@PluginDescription(name = "Pinger Locator", author = "jpereira", category=CATEGORY.UNSORTED /*,icon="pt/lsts/neptus/plugins/acoustic/manta.png"*/)
@LayerPriority(priority = 40)
@Popup(name="Pinger Locator", accelerator=KeyEvent.VK_F, width=450, height=250, pos=POSITION.TOP_LEFT/*, icon="pt/lsts/neptus/plugins/acoustic/manta.png"*/)  
public class PingerLocator extends ConsolePanel implements Renderer2DPainter {

    private static final long serialVersionUID = 1L;
    protected boolean initialized = false;
    protected String origin = "Lat/Lon";
    protected JPanel listPanel = new JPanel();
    protected LinkedHashMap<String, JButton> cmdButtons = new LinkedHashMap<String, JButton>();
    private DefaultTableModel lines;
        
    /**
     * @param console
     */
    public PingerLocator(ConsoleLayout console) {
        super(console);
    }

    /* (non-Javadoc)
     * @see pt.lsts.neptus.console.ConsolePanel#cleanSubPanel()
     */
    @Override
    public void cleanSubPanel() {
       

    }

    /* (non-Javadoc)
     * @see pt.lsts.neptus.console.ConsolePanel#initSubPanel()
     */
    @Override
    public void initSubPanel() {
        if (initialized) return;
        initialized = true;
        
        JPanel ctrlPanel = new JPanel();
        ctrlPanel.setLayout(new GridLayout(0, 1, 3, 2));
                
        JButton button = new JButton(I18n.textf("Origin: %origin", origin));
        button.setActionCommand("origin");
        cmdButtons.put("origin", button);
        button.addActionListener(new ActionListener() {
            
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent event) {
                
                Vector<String> options = new Vector<String>();
                options.add(origin);
                options.add(getConsole().getMainSystem());
                
                // fill the options
                Vector<String> systemList = new Vector<String>();
                for (ImcSystem system : ImcSystemsHolder.lookupAllSystems()) {
                    if (!options.contains(system.getName()))
                        systemList.add(system.getName());
                }
                
                Vector<Object> systems = new Vector<>();
                systems.add(I18n.text(origin));
                systems.addAll(Arrays.asList(ImcSystemsHolder.lookupAllSystems()));
                Collections.sort(systemList);
                options.addAll(systemList);
                
                String[] choices = options.toArray(new String[options.size()]);

                Object orig = JOptionPane.showInputDialog(getConsole(), I18n.text("Select System as origin"), 
                        I18n.text("Select a system to use as origin"),
                        JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);

                if (orig != null)
                    origin = ""+orig;
                
                ((JButton) event.getSource()).setText(I18n.textf("Origin: %origin", origin));
                
             //   ((JButton) event.getSource()).setText(I18n.textf("GW: %gateway", gateway));
             //   lblState.setText(buildState());
                // TODO Auto-generated method stub
                
            }
        });
        ctrlPanel.add(button);
        
        button = new JButton(I18n.text("Add Line"));
        ctrlPanel.add(button);
        button.setActionCommand("add");
        cmdButtons.put("add", button);
        
        button = new JButton(I18n.text("Delete Line"));
        ctrlPanel.add(button);
        button.setActionCommand("del");
        cmdButtons.put("del", button);
        
        button = new JButton(I18n.text("Calculate Position"));
        ctrlPanel.add(button);
        button.setActionCommand("calc");
        cmdButtons.put("calc", button);
        
        
        lines = new DefaultTableModel();
        JTable table = new JTable(lines);
        lines.addColumn("#");
        lines.addColumn("Latitude");
        lines.addColumn("Longitude");
        lines.addColumn("Azimuth");
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        
        String [] one = {"1", "41.185419", "-8.707264", "34"};
        lines.addRow(one);
        
        JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, ctrlPanel);
        split1.setDividerLocation(303);
        setLayout(new BorderLayout());
        add(split1, BorderLayout.CENTER);
        //add(scrollPane, BorderLayout.CENTER);
    }

    /* (non-Javadoc)
     * @see pt.lsts.neptus.renderer2d.Renderer2DPainter#paint(java.awt.Graphics2D, pt.lsts.neptus.renderer2d.StateRenderer2D)
     */
    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        // TODO Auto-generated method stub
        
    }

}
