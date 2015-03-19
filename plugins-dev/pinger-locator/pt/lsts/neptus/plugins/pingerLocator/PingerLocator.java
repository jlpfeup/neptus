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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.io.Console;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;



import org.j3d.renderer.java3d.geom.Box;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;

import com.google.common.collect.Table;
import com.rabbitmq.client.Command;


import pt.lsts.neptus.comm.manager.imc.ImcSystem;
import pt.lsts.neptus.comm.manager.imc.ImcSystemsHolder;
import pt.lsts.neptus.console.ConsoleEvents;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.gui.SelectAllFocusListener;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.PluginDescription.CATEGORY;
import pt.lsts.neptus.plugins.Popup;
import pt.lsts.neptus.plugins.Popup.POSITION;
import pt.lsts.neptus.plugins.acoustic.MantaOperations;
import pt.lsts.neptus.plugins.gauges.GaugeDisplay;
import pt.lsts.neptus.plugins.map.edit.RemoveObjectEdit;
import pt.lsts.neptus.plugins.vtk.events.MouseEvent;
import pt.lsts.neptus.renderer2d.ILayerPainter;
import pt.lsts.neptus.renderer2d.LayerPriority;
import pt.lsts.neptus.renderer2d.Renderer2DPainter;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.systems.external.ExternalSystem;
import pt.lsts.neptus.systems.external.ExternalSystemsHolder;
import pt.lsts.neptus.types.coord.CoordinateUtil;
import pt.lsts.neptus.types.coord.LocationType;
import pt.lsts.neptus.types.map.AbstractElement;
import pt.lsts.neptus.types.map.LineSegmentElement;
import pt.lsts.neptus.types.map.MapGroup;
import pt.lsts.neptus.types.map.MapType;
import pt.lsts.neptus.types.vehicle.VehicleType.SystemTypeEnum;
import pt.lsts.neptus.util.ConsoleParse;
import pt.lsts.neptus.util.GuiUtils;
import pt.lsts.neptus.util.ImageUtils;
import pt.lsts.neptus.util.MathMiscUtils;

/**
 * @author jpereira
 * Many thanks to Antonio Sérgio and José Pinto
 */
@PluginDescription(name = "Pinger Locator", author = "jpereira", category=CATEGORY.UNSORTED /*,icon="pt/lsts/neptus/plugins/acoustic/manta.png"*/)
@LayerPriority(priority = 40)
@Popup(name="Pinger Locator", accelerator=KeyEvent.VK_F, width=480, height=250, pos=POSITION.TOP_LEFT/*, icon="pt/lsts/neptus/plugins/acoustic/manta.png"*/)  
public class PingerLocator extends ConsolePanel implements Renderer2DPainter, KeyListener, MouseListener {

    private static final long serialVersionUID = 1L;
    public static final short DECIMAL_DEGREES_DISPLAY = 0;
    public static final short DM_DISPLAY = 1;
    public static final short DMS_DISPLAY = 2;
    protected boolean initialized = false;
    protected String origin = "Lat/Lon";
    protected JPanel listPanel = new JPanel();
    protected LinkedHashMap<String, JButton> cmdButtons = new LinkedHashMap<String, JButton>();
    private DefaultTableModel lines = new DefaultTableModel();
    private NumberFormat df = GuiUtils.getNeptusDecimalFormat();
    private NumberFormat lg = GuiUtils.getNeptusIntegerFormat();
    private int degrees = 0;
   // private int llength = 4000; 
    private JButton okBtn = null;
    private JButton cancelBtn = null;
    private JFormattedTextField latDeg = null;
    private JFormattedTextField latMin = null;
    private JFormattedTextField latSec = null;
    private JFormattedTextField lonDeg = null;
    private JFormattedTextField lonMin = null;
    private JFormattedTextField lonSec = null;
    private JFormattedTextField azimuth = null;
    private JFormattedTextField length = null;
    private JRadioButton ddegreesRadioButton = null;
    private JRadioButton dmRadioButton = null;
    private JRadioButton dmsRadioButton = null;
    private JPanel convPanel = null;
    private LocationType location = null;
    
    private CardLayout cl = new CardLayout();
    private JPanel cardsPanel = new JPanel(new CardLayout());
    
    private JTable table = new JTable();
    private JPanel coordPanel = new JPanel();
    
    private String idsTable [];
    private int lengthVal = 800;
    
    //private LocationType lineLocation = new LocationType();
    
    protected MapType pivot;
    protected LineSegmentElement element = null;
    protected MapGroup mg = MapGroup.getMapGroupInstance(getConsole().getMission());

/*    protected MapType getPivot() {
        return mg.getPivotMap();
    }*/
                
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
        
        
        //GuiUtils.reac
       // JPanel ctrlPanel = new JPanel();
       // final JPanel cardsPanel = new JPanel(new CardLayout());
        //JPanel card1 = new JPanel();
        //card1.setSize(400, 200);
        JPanel ctrlPanel = new JPanel();
        ctrlPanel.setLayout(new GridLayout(0, 1, 3, 2));
        
        //ctrlPanel.setSize(200, 200);
        
       // card1.setSize(177, 250);
                
        JButton button = new JButton(I18n.textf("Origin: %origin", origin));
        button.setActionCommand("origin");
        cmdButtons.put("origin", button);
        button.addActionListener(new ActionListener() {
            
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent event) {
                
                Vector<String> options = new Vector<String>();
                
                // fill the options
                if (!options.contains(origin))
                    options.add(origin);
                
                if (!options.contains("Lat/Lon"))
                options.add("Lat/Lon");
                
                if (!options.contains(getConsole().getMainSystem()));
                    options.add(getConsole().getMainSystem());
                
                Vector<String> systemList = new Vector<String>();    
                for (ImcSystem system : ImcSystemsHolder.lookupAllSystems()) {
                    if ((!options.contains(system.getName())) && (!origin.equals(system.getName())))
                        systemList.add(system.getName());
                }
                
                Collections.sort(systemList);
                options.addAll(systemList);
                
                String[] choices = options.toArray(new String[options.size()]);

                Object orig = JOptionPane.showInputDialog(getConsole(), I18n.text("Select System as origin"), 
                        I18n.text("Select a system to use as origin"),
                        JOptionPane.QUESTION_MESSAGE, null, choices, origin);

                if (orig != null)
                    origin = ""+orig;
                
                ((JButton) event.getSource()).setText(I18n.textf("Origin: %origin", origin));
                
            }
        });
        ctrlPanel.add(button);
        
        button = new JButton(I18n.text("Add Line"));
        button.setActionCommand("add");
        cmdButtons.put("add", button);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(origin.equals("Lat/Lon")){
                    location = null;
                }
                else{
                    ImcSystem sys = ImcSystemsHolder.lookupSystemByName(origin);
                    if (sys != null) {
                        location = sys.getLocation();
                        System.out.println("location: " + location);
                    }
                    else {
                        ExternalSystem extsys = ExternalSystemsHolder.lookupSystem(origin);
                        if (extsys != null) 
                            location = extsys.getLocation();
                    }
                }
                changeCardLayout("Coordinates");
                setCoord();
                             
            }
        });
        
        ctrlPanel.add(button);
        
        
        button = new JButton(I18n.text("Delete Line"));
        ctrlPanel.add(button);
        button.setActionCommand("del");
        cmdButtons.put("del", button);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                try{
                    //lines.removeRow(table.getSelectedRow());
                    
                    
                    /*LineSegmentElement element = new LineSegmentElement(mg.getPivotMap().getMapGroup(), mg.getPivotMap());
                    element.setId();
                    // element.setColor(Color.RED);
                    element.setColor(Color.RED);*/
                    
                  //  AbstractElement elem = mg.getMapObjectsByID("line1")[0];

                  // elem.invertColor(Color.BLUE);
                    
                   // element.setColor(Color.RED);
                  //  mg.getPivotMap().remove("line1");
                    //(table.getSelectedRow()+1)
                    
                    int row = table.getSelectedRow();
                    String index = (String) lines.getValueAt(row, 0);
                    AbstractElement elem = mg.getMapObjectsByID("line"+index)[0];
                    
                    System.out.println("deleted id" + index);
                   // element.setColor(Color.RED);
                    lines.removeRow(row);
                    RemoveObjectEdit edit = new RemoveObjectEdit(elem);
                    edit.redo();

                    
                }
                catch (Exception e2) {
                    JOptionPane.showMessageDialog(getConsole(), I18n.text("No selected Line"),
                    I18n.text("Removal Rejected"), JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        button = new JButton(I18n.text("Calculate Position"));
        ctrlPanel.add(button);
        
        
        button.setActionCommand("calc");
        cmdButtons.put("calc", button);
        
        
        
        table.setModel(lines);
        lines.addColumn("#");
        lines.addColumn("Latitude");
        lines.addColumn("Longitude");
        lines.addColumn("Azimuth");
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
       // table.removeColumn(table.getColumnModel().getColumn(0));
        //table.setColumnSelectionAllowed(false);
        //table.setRowSelectionAllowed(false);
       
        table.addMouseListener(this);
        
//        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        table.setCellSelectionEnabled(true);
//        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
//                // TODO Auto-generated method stub
//              for (int selected :  table.getSelectedRows()){
//                  AbstractElement element = mg.getMapObject("line" + (selected + 1));
//                 // element.setColor(Color.RED);
//                  element.invertColor(Color.BLUE);
//              }
//           }
//        });
        
        
        
      //  table.addMouseListener(new PingerLocator(getMainpanel().getConsole()));
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        
       /* String [] one = {"1", "41.185419", "-8.707264", "34"};
        lines.addRow(one);
        */
        JSplitPane card1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, ctrlPanel);
        card1.setMaximumSize(new Dimension(480,250));
        card1.setDividerLocation(303);
        
        
        //JPanel coordPanel = new JPanel();
        //coordPanel.setLayout(new BorderLayout());
        coordPanel.setBorder(BorderFactory.createTitledBorder(null, null, TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        coordPanel.setLayout(null);
        JLabel jlabel = new JLabel();
        JLabel jlabel1 = new JLabel();
        JLabel jlabel2 = new JLabel();
        JLabel jlabel3 = new JLabel();
        JLabel jlabel4 = new JLabel();
        JLabel jlabel5 = new JLabel();
        JLabel jlabel6 = new JLabel();
        JLabel jlabel7 = new JLabel();
        JLabel jlabel8 = new JLabel();
        JLabel jlabel9 = new JLabel();
        JLabel jlabel10 = new JLabel();
        JLabel jlabel11 = new JLabel();
        jlabel.setBounds(10, 24, 59, 20);
        jlabel.setText(I18n.text("Latitude:"));
        jlabel1.setBounds(10, 84, 90, 20);
        jlabel1.setText(I18n.text("Longitude:"));
        jlabel2.setBounds(167, 109, 10, 20);
        jlabel2.setText("'");
        jlabel2.setHorizontalTextPosition(SwingConstants.CENTER);
        jlabel2.setHorizontalAlignment(SwingConstants.CENTER);
        jlabel3.setBounds(252, 109, 10, 20);
        jlabel3.setText("''");
        jlabel3.setHorizontalTextPosition(SwingConstants.CENTER);
        jlabel3.setHorizontalAlignment(SwingConstants.CENTER);
        jlabel4.setBounds(81, 109, 10, 20);
        jlabel4.setText(""+CoordinateUtil.CHAR_DEGREE);
        jlabel4.setHorizontalTextPosition(SwingConstants.CENTER);
        jlabel4.setHorizontalAlignment(SwingConstants.CENTER);
        jlabel4.setFont(new Font("Dialog", Font.BOLD, 14));
        jlabel5.setBounds(81, 49, 10, 20);
        jlabel5.setText(""+CoordinateUtil.CHAR_DEGREE);
        jlabel5.setHorizontalTextPosition(SwingConstants.CENTER);
        jlabel5.setHorizontalAlignment(SwingConstants.CENTER);
        jlabel5.setFont(new Font("Dialog", Font.BOLD, 14));
        jlabel6.setBounds(167, 49, 10, 20);
        jlabel6.setText("'");
        jlabel6.setHorizontalTextPosition(SwingConstants.CENTER);
        jlabel6.setHorizontalAlignment(SwingConstants.CENTER);
        jlabel7.setBounds(252, 49, 10, 20);
        jlabel7.setText("''");
        jlabel7.setHorizontalTextPosition(SwingConstants.CENTER);
        jlabel7.setHorizontalAlignment(SwingConstants.CENTER);
        jlabel8.setText(I18n.text("Azimuth:"));
        jlabel8.setBounds(10, 144, 59, 20);
        jlabel9.setBounds(40, 169, 10, 20);
        jlabel9.setText(""+CoordinateUtil.CHAR_DEGREE);
        jlabel9.setHorizontalTextPosition(SwingConstants.CENTER);
        jlabel9.setHorizontalAlignment(SwingConstants.CENTER);
        jlabel9.setFont(new Font("Dialog", Font.BOLD, 14));
        jlabel10.setText(I18n.text("Length:"));
        jlabel10.setBounds(100, 144, 59, 20);
        jlabel11.setBounds(140, 164, 20, 30);
        jlabel11.setText("m");
        jlabel11.setHorizontalTextPosition(SwingConstants.CENTER);
        jlabel11.setHorizontalAlignment(SwingConstants.CENTER);
        //jlabel11.setFont(new Font("Dialog", Font.BOLD, 14));
        
        
        
        coordPanel.add(jlabel);
        coordPanel.add(jlabel1);
        coordPanel.add(jlabel2);
        coordPanel.add(jlabel3);
        coordPanel.add(jlabel4);
        coordPanel.add(jlabel5);
        coordPanel.add(jlabel6);
        coordPanel.add(jlabel7);
        coordPanel.add(jlabel8);
        coordPanel.add(jlabel9);
        coordPanel.add(jlabel10);
        coordPanel.add(jlabel11);
        
        coordPanel.add(getOkBtn());
        coordPanel.add(getCancelBtn());
        coordPanel.add(getLatDeg());
        
        coordPanel.add(getLatDeg(), null);
        coordPanel.add(getLatMin(), null);
        coordPanel.add(getLatSec(), null);
        coordPanel.add(getLonDeg(), null);
        coordPanel.add(getLonMin(), null);
        coordPanel.add(getLonSec(), null);
        coordPanel.add(getAzimuth(), null);
        coordPanel.add(getConvPanel(), null);
        coordPanel.add(getLength(), null);
        
        
        //setLayout(new BorderLayout());
        //card1.setSize(480, 250);
        //card1.add(split1, BorderLayout.CENTER);
        //maisUmPanel.setSize(ctrlPanel.size());
        //maisUmPanel.setLayout(new BorderLayout());
        cardsPanel.setSize(480, 250);
        cardsPanel.add(card1, "Control");
        cardsPanel.add(coordPanel, "Coordinates");
       // ctrlPanel.add(cards, BorderLayout.CENTER);
        //ctrlPanel.setSize(480, 250);
        setLayout(new BorderLayout());
       // add(ctrlPanel, BorderLayout.CENTER);
        add(cardsPanel, BorderLayout.CENTER);
        //add(split1, BorderLayout.CENTER);
        //cardsPanel.setSize(480, 250);
        
         
         
       /* 
        CardLayout cl = (CardLayout)(cardsPanel.getLayout());
        cl.show(cardsPanel, "Lines");
    */
         GuiUtils.reactEnterKeyPress(okBtn);
         GuiUtils.reactEscapeKeyPress(cancelBtn);
    }

    /* (non-Javadoc)
     * @see pt.lsts.neptus.renderer2d.Renderer2DPainter#paint(java.awt.Graphics2D, pt.lsts.neptus.renderer2d.StateRenderer2D)
     */
    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
                
    }
    
    
    private JButton getOkBtn() {
        if (okBtn == null) {
            okBtn = new JButton();
            okBtn.setBounds(257, 180, 60, 20);
            okBtn.setText(I18n.text("OK"));
            okBtn.setActionCommand("ok");
            okBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("btn ok");
                    if(!azimuthAcceptable())
                    {
                        JOptionPane.showMessageDialog(getConsole(), I18n.text("Please insert an Azimuth value between 0 and 359"),
                                I18n.text("Adition Rejected"), JOptionPane.WARNING_MESSAGE);
                        getAzimuth().setText("0");
                    }else if (!lengthAcceptable()){
                            JOptionPane.showMessageDialog(getConsole(), I18n.text("Please insert a Length value between 0 and 4000"),
                                    I18n.text("Adition Rejected"), JOptionPane.WARNING_MESSAGE);
                            getLength().setText(""+lengthVal);
                          }else if(addLineToTable())
                                {   
                                    if(!(lengthVal==Integer.parseInt(getLength().getText())))
                                    {
                                        lengthVal=Integer.parseInt(getLength().getText());
                                        int reply = JOptionPane.showConfirmDialog(getConsole(), I18n.text("Do you want to set all the lines with this lenght?"),
                                                I18n.text("Lenght is diferent from previous lines"), JOptionPane.YES_NO_OPTION);
                                        if (reply == JOptionPane.YES_OPTION)
                                            updateLinesLength();
                                    }
                                    lengthVal=Integer.parseInt(getLength().getText());
                                    addLineToMap();
                                }
                        
                    //if(azimuthAcceptable())
                    //{
                    //    if(addLineToTable())
                    //        addLineToMap();
                    //}else
                    //    {
                            
                    //    }
                }

               
            });
        }
        return okBtn;
    }
    
    private JButton getCancelBtn() {
        if (cancelBtn == null) {
            cancelBtn = new JButton();
            cancelBtn.setBounds(357, 180, 60, 20);
            cancelBtn.setText(I18n.text("Cancel"));
            cancelBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    changeCardLayout("Control");
                }
            });
        }
        return cancelBtn;
    }
    
    private JFormattedTextField getLatDeg() {
        if (latDeg == null) {
            latDeg = new JFormattedTextField(df);
            latDeg.setBounds(10, 49, 72, 20);
            latDeg.setText("0.0");
            latDeg.addKeyListener(this);
            latDeg.addFocusListener(new SelectAllFocusListener());
            
        }
        return latDeg;
    }
    
    private JFormattedTextField getLatMin() {
        if (latMin == null) {
            latMin = new JFormattedTextField(df);
            latMin.setBounds(96, 49, 72, 20);
            latMin.setText("0.0");
            latMin.setDisabledTextColor(Color.GRAY);
            latMin.addKeyListener(this);
            latMin.addFocusListener(new SelectAllFocusListener());
        }
        return latMin;
    }
    
    private JFormattedTextField getLatSec() {
        if (latSec == null) {
            latSec = new JFormattedTextField(df);
            latSec.setBounds(181, 49, 72, 20);
            latSec.setText("0.0");
            latSec.setDisabledTextColor(Color.GRAY);
            latSec.addKeyListener(this);
            latSec.addFocusListener(new SelectAllFocusListener());
        }
        return latSec;
    }
    
    private JFormattedTextField getLonDeg() {
        if (lonDeg == null) {
            lonDeg = new JFormattedTextField(df);
            lonDeg.setBounds(10, 109, 72, 20);
            lonDeg.setText("0.0");
            lonDeg.addKeyListener(this);
            lonDeg.addFocusListener(new SelectAllFocusListener());
        }
        return lonDeg;
    }
    
    private JFormattedTextField getLonMin() {
        if (lonMin == null) {
            lonMin = new JFormattedTextField(df);
            lonMin.setBounds(96, 109, 72, 20);
            lonMin.setText("0.0");
            lonMin.setDisabledTextColor(Color.GRAY);
            lonMin.addKeyListener(this);
            lonMin.addFocusListener(new SelectAllFocusListener());
        }
        return lonMin;
    }
    
    private JFormattedTextField getLonSec() {
        if (lonSec == null) {
            lonSec = new JFormattedTextField(df);
            lonSec.setBounds(181, 109, 72, 20);
            lonSec.setText("0.0");
            lonSec.setDisabledTextColor(Color.GRAY);
            lonSec.addKeyListener(this);
            lonSec.addFocusListener(new SelectAllFocusListener());
        }
        return lonSec;
    }
    
    private JFormattedTextField getAzimuth() {
        if(azimuth == null) {
           azimuth = new JFormattedTextField(df);
           azimuth.setBounds(10, 169, 30, 20);
           azimuth.setText("0");
           azimuth.addKeyListener(this);
           azimuth.addFocusListener(new SelectAllFocusListener());
        }
        return azimuth;
    }
    
    private JFormattedTextField getLength() {
        if(length == null) {
           length = new JFormattedTextField(lg);
           length.setBounds(100, 169, 40, 20);
           length.setText("800");
           length.setHorizontalAlignment(SwingConstants.RIGHT);
           length.addKeyListener(this);
           length.addFocusListener(new SelectAllFocusListener());
        }
        return length;
    }
    
    private JPanel getConvPanel() {
        if (convPanel == null) {
            convPanel = new JPanel();
            convPanel.setLayout(new BoxLayout(getConvPanel(), BoxLayout.Y_AXIS));
            convPanel.setBorder(BorderFactory.createTitledBorder(null, I18n.text("Lat/Lon Display"),
                    TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
                    new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
            convPanel.setBounds(new Rectangle(270, 30, 200, 98));
            convPanel.add(getDdegreesRadioButton(), null);
            convPanel.add(getDmRadioButton(), null);
            convPanel.add(getDmsRadioButton(), null);
        }
        return convPanel;
    }
    
    private JRadioButton getDdegreesRadioButton() {
        if (ddegreesRadioButton == null) {
            ddegreesRadioButton = new JRadioButton();
            ddegreesRadioButton.setText(I18n.text("Decimal Degrees"));
            ddegreesRadioButton.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        dmRadioButton.setSelected(false);
                        dmsRadioButton.setSelected(false);
                        convertLatLonTo(DECIMAL_DEGREES_DISPLAY);
                        getLatMin().setEnabled(false);
                        //getLatMin().setText("0");
                        getLatSec().setEnabled(false);
                        //getLatSec().setText("0");
                        getLonMin().setEnabled(false);
                        //getLonMin().setText("0");
                        getLonSec().setEnabled(false);
                        //getLonSec().setText("0");
                        
                    }
                }
            });
        }
        return ddegreesRadioButton;
    }

    /**
     * This method initializes dmRadioButton    
     *  
     * @return javax.swing.JRadioButton 
     */
    private JRadioButton getDmRadioButton() {
        if (dmRadioButton == null) {
            dmRadioButton = new JRadioButton();
            dmRadioButton.setText(I18n.text("Degrees, Minutes"));
            dmRadioButton.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        ddegreesRadioButton.setSelected(false);
                        dmsRadioButton.setSelected(false);
                        convertLatLonTo(DM_DISPLAY);
                        getLatMin().setEnabled(true);
                        //getLatMin().setText("0.0");
                        getLatSec().setEnabled(false);
                        //getLatSec().setText("0");
                        getLonMin().setEnabled(true);
                        //getLonMin().setText("0.0");
                        getLonSec().setEnabled(false);
                        //getLonSec().setText("0");
                        
                    }
                }
            });
        }
        return dmRadioButton;
    }

    /**
     * This method initializes dmsRadioButton   
     *  
     * @return javax.swing.JRadioButton 
     */
    private JRadioButton getDmsRadioButton() {
        if (dmsRadioButton == null) {
            dmsRadioButton = new JRadioButton();
            dmsRadioButton.setHorizontalAlignment(SwingConstants.LEADING);
            /// DMS = Degrees, Minutes, Seconds
            dmsRadioButton.setText(I18n.text("Degrees, Minutes, Seconds"));
            dmsRadioButton.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        ddegreesRadioButton.setSelected(false);
                        dmRadioButton.setSelected(false);
                        convertLatLonTo(DMS_DISPLAY);
                        getLatMin().setEnabled(true);
                        //getLatMin().setText("0");
                        getLatSec().setEnabled(true);
                        //getLatSec().setText("0");
                        getLonMin().setEnabled(true);
                        //getLonMin().setText("0");
                        getLonSec().setEnabled(true);
                        //getLonSec().setText("0");
                        
                        
                    }
                }
            });
        }
        return dmsRadioButton;
    }
    
    protected LocationType getLatLon(){
        LocationType loc = new LocationType();
        loc.setLatitudeStr(getLatitude());
        loc.setLongitudeStr(getLongitude());
        return loc;
    }
    
    protected boolean convertLatLonTo(short type) {
        LocationType loc = new LocationType();
        loc.setLatitudeStr(getLatitude());
        loc.setLongitudeStr(getLongitude());
        switch (type) {
        case DECIMAL_DEGREES_DISPLAY:
            this.setLatitude(new double[] {
                    MathMiscUtils.round(loc.getLatitudeDegs(), 6), 0,
                    0 });
            this.setLongitude(new double[] {
                    MathMiscUtils.round(loc.getLongitudeDegs(), 6), 0,
                    0 });
            break;

        case DM_DISPLAY:
            double[] dmLat = CoordinateUtil.decimalDegreesToDM(loc
                    .getLatitudeDegs());
            double[] dmLon = CoordinateUtil.decimalDegreesToDM(loc
                    .getLongitudeDegs());
            this.setLatitude(new double[] { dmLat[0],
                    MathMiscUtils.round(dmLat[1], 4), 0 });
            this.setLongitude(new double[] { dmLon[0],
                    MathMiscUtils.round(dmLon[1], 4), 0 });
            break;

        case DMS_DISPLAY:
            double[] dmsLat = CoordinateUtil.decimalDegreesToDMS(loc
                    .getLatitudeDegs());
            double[] dmsLon = CoordinateUtil.decimalDegreesToDMS(loc
                    .getLongitudeDegs());
            this.setLatitude(new double[] { dmsLat[0], dmsLat[1],
                    MathMiscUtils.round(dmsLat[2], 2) });
            this.setLongitude(new double[] { dmsLon[0], dmsLon[1],
                    MathMiscUtils.round(dmsLon[2], 2) });
            break;

        default:
            break;
        }
        return true;
    }
    
    
    
    public String getLatitude() {
        String latString =  CoordinateUtil.dmsToLatString(
            Double.parseDouble(getLatDeg().getText()),
            Double.parseDouble(getLatMin().getText()),
            Double.parseDouble(getLatSec().getText())
        );

        return latString;
    }
    
    public String getLongitude() {
        return CoordinateUtil.dmsToLonString(
                Double.parseDouble(getLonDeg().getText()),
                Double.parseDouble(getLonMin().getText()),
                Double.parseDouble(getLonSec().getText()));
    }
    
    public double getLatitudeValue() {
        try {
            return CoordinateUtil.parseLatitudeCoordToDoubleValue(getLatitude());//Double.parseDouble(getLatDecDegrees().getText());
        }
        catch (Exception e) {
            return 0;
        }
    }

    public double getLongitudeValue() {
        try {
            return CoordinateUtil.parseLongitudeCoordToDoubleValue(getLongitude());//Double.parseDouble(getLonDecDegrees().getText());
        }
        catch (Exception e) {
            return 0;
        }
    }
    
    public void setLatitude(double dms[]) {
        getLatDeg().setText(String.valueOf(dms[0]));
        getLatMin().setText(String.valueOf(dms[1]));
        getLatSec().setText(String.valueOf(dms[2]));
        latMin.setEnabled(true);
        latSec.setEnabled(true);
        latMin.setBackground(Color.WHITE);
        latSec.setBackground(Color.WHITE);
    }

    public void setLongitude(double dms[]) {
        getLonDeg().setText(String.valueOf(dms[0]));
        getLonMin().setText(String.valueOf(dms[1]));
        getLonSec().setText(String.valueOf(dms[2]));
        lonMin.setEnabled(true);
        lonSec.setEnabled(true);
        lonMin.setBackground(Color.WHITE);
        lonSec.setBackground(Color.WHITE);
    }
    

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    public void changeCardLayout( String layout) {
        cl = (CardLayout)(cardsPanel.getLayout());
        cl.show(cardsPanel, layout);
    }
    
    public void setCoord (){
        if(location == null){
            latDeg.setEditable(true);
            latDeg.setText("0.0");
            lonDeg.setEditable(true);
            lonDeg.setText("0.0");
            ddegreesRadioButton.setEnabled(true);
            dmRadioButton.setEnabled(true);
            dmsRadioButton.setEnabled(true);
            
        }else{
            setLatitude(new double[] {
                    MathMiscUtils.round(location.getLatitudeDegs(), 6), 0, 0 });
            setLongitude(new double[] {
                    MathMiscUtils.round(location.getLongitudeDegs(), 6), 0, 0 });
            latDeg.setEditable(false);
            lonDeg.setEditable(false);
            ddegreesRadioButton.setEnabled(false);
            dmRadioButton.setEnabled(false);
            dmsRadioButton.setEnabled(false);
            convertLatLonTo(DECIMAL_DEGREES_DISPLAY);
        }
        ddegreesRadioButton.setSelected(true);
        getLatMin().setEnabled(false);
        //getLatMin().setText("0");
        getLatSec().setEnabled(false);
        //getLatSec().setText("0");
        getLonMin().setEnabled(false);
        //getLonMin().setText("0");
        getLonSec().setEnabled(false);
        //getLonSec().setText("0");
        getAzimuth().setText("0");
    }
    
    public boolean addLineToTable(){
       String [] candidate = {""+ (highestVal() + 1) , getLatitude() , getLongitude(), getAzimuth().getText()+""+CoordinateUtil.CHAR_DEGREE};
       // String [] candidate = {getLatitude() , getLongitude(), getAzimuth().getText()+""+CoordinateUtil.CHAR_DEGREE};
        boolean addLine=true;
        
        System.out.println("highest value + " + highestVal());
        for(int r=0; r<lines.getRowCount(); r++){
            boolean duplicatedLine=true;
            for(int c=1; c<lines.getColumnCount(); c++){
                String str = (String) lines.getValueAt(r, c);
                if(str.equals(candidate[c]))
                    duplicatedLine=duplicatedLine && true;
                else
                    duplicatedLine=duplicatedLine && false;
            }
            if(duplicatedLine){
                addLine=false;
                break;
            }
        }
        if(addLine){
            lines.addRow(candidate);
            changeCardLayout("Control");
            return true;
        }else{
            JOptionPane.showMessageDialog(this, I18n.text("Duplicated Line"),
            I18n.text("Adition Rejected"), JOptionPane.WARNING_MESSAGE);
            //JOptionPane.showMessageDialog(getConsole(), I18n.text("Duplicated Line"));
            return false;
            
        }
    }
    

    public int highestVal(){
        int highest=0;
        for(int r=0 ; r<lines.getRowCount(); r++){
            if(Integer.parseInt((String)lines.getValueAt(r, 0)) > 0)
                highest=Integer.parseInt((String)lines.getValueAt(r, 0));
        }
        return highest;
    }
    
    public void addLineToMap(){
        element = new LineSegmentElement(mg.getPivotMap().getMapGroup(), mg.getPivotMap());
        if(location != null)
            element.setCenterLocation(location);
        else
            element.setCenterLocation(getLatLon());
        element.setLength(lengthVal);
        element.setYawDeg(Double.parseDouble(getAzimuth().getText()));
        element.setId("line"+ (highestVal()));
        System.out.println("added element id " + element.getId());
        mg.getPivotMap().addObject(element);
       //mg.getPivotMap().remove("line1");
        
        
    }
    
    
    public boolean azimuthAcceptable(){
        if((Integer.parseInt(getAzimuth().getText())>(-1)) && (Integer.parseInt(getAzimuth().getText())<360))
            return true;
        else return false;
    }
    
    public boolean lengthAcceptable(){
        try{
            if((Integer.parseInt(getLength().getText())>(-1)) && (Integer.parseInt(getLength().getText())<4001))
                return true;
            else return false;
        }catch (Exception e) {
            return false;
        }
        
    }
    
    public void updateLinesLength() {
        for(int i=0; i<(table.getRowCount()-1); i++ ){
            int index = Integer.parseInt((String) lines.getValueAt(i, 0));
            ((LineSegmentElement) mg.getMapObjectsByID("line"+index)[0]).setLength(lengthVal);
            System.out.println("updated line"+index);
            
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub
        if(e.getButton() == java.awt.event.MouseEvent.BUTTON1){
            for(int i=0; i<table.getRowCount(); i++ ){
                int index = Integer.parseInt((String) lines.getValueAt(i, 0));
                //elem.invertColor(Color.BLUE);
                           
                if(i == table.getSelectedRow())
                    ((LineSegmentElement) mg.getMapObjectsByID("line"+index)[0]).setColor(Color.RED);
                else
                    ((LineSegmentElement) mg.getMapObjectsByID("line"+index)[0]).setColor(Color.BLUE);
            }
        }
           // System.out.println("selected row " +table.getSelectedRow());
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    
    
   
    
}
