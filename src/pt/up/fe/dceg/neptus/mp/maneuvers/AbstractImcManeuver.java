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
 * Author: zp
 * Nov 15, 2011
 */
package pt.up.fe.dceg.neptus.mp.maneuvers;

import java.util.Arrays;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import pt.up.fe.dceg.neptus.NeptusLog;
import pt.up.fe.dceg.neptus.gui.PropertiesProvider;
import pt.up.fe.dceg.neptus.imc.IMCDefinition;
import pt.up.fe.dceg.neptus.imc.IMCMessage;
import pt.up.fe.dceg.neptus.mp.ManeuverLocation;
import pt.up.fe.dceg.neptus.plugins.PluginProperty;
import pt.up.fe.dceg.neptus.plugins.PluginUtils;
import pt.up.fe.dceg.neptus.util.comm.IMCUtils;
import pt.up.fe.dceg.neptus.util.conf.ConfigFetch;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;

/**
 * @author zp
 *
 */
public abstract class AbstractImcManeuver<T extends IMCMessage> extends DefaultManeuver implements LocatedManeuver, PropertiesProvider, IMCSerialization {

    protected T message = null;
    

    @SuppressWarnings("unchecked")
    public AbstractImcManeuver() {
        try {
            this.message = (T)(message.getClass().newInstance());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        setMessage(message);
    }
    
    @SuppressWarnings("unchecked")
    public AbstractImcManeuver(T message) {
        try {
            this.message = (T)(message.getClass().newInstance());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        setMessage(message);
    }
    
    public void setMessage(IMCMessage message) {
        try {
            message.setMessage(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    public void parseIMCMessage(IMCMessage message) {
        setMessage(message);
    }
    
    @Override
    public IMCMessage serializeToIMC() {
        return message;
    }
    
    @Override
    public String getType() {
        if (message != null)
            return message.getClass().getSimpleName();
        return getClass().getSimpleName();
    }
    
    @Override
    public Object clone() {
        try {
            @SuppressWarnings("unchecked")
            AbstractImcManeuver<T> other = (AbstractImcManeuver<T>)getClass().newInstance();
            clone(other);
            other.setMessage(message.cloneMessage());
            other.setManeuverLocation(getManeuverLocation());
            return other;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void loadFromXML(String XML) {
        
        if (message == null) {
            NeptusLog.pub().error("Invalid maneuver: "+message.getClass());
            return;
        }
        
        try {
            Document doc = DocumentHelper.parseText(XML);
            
            PluginProperty[] props = IMCUtils.getProperties(message).toArray(new PluginProperty[0]);
            
            for (int i = 0; i < props.length; i++) {
                
                Node node = doc.selectSingleNode("//"+props[i].getName().replaceAll(" ", ""));
                if (node != null) {
                    props[i].unserialize(node.getText());
                }              
            }
            setProperties(props);
        }
        catch (Exception e) {

            NeptusLog.pub().error(this, e);
            return;
        }
    }

    public Document getManeuverAsDocument(String rootElementName) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement( rootElementName );

        IMCMessage header = IMCDefinition.getInstance().createHeader();
        
        for (DefaultProperty prop : IMCUtils.getProperties(message)) {
            
            // add only non-header fields
            if (header.getTypeOf(prop.getName()) == null && prop instanceof PluginProperty) {
                root.addElement(prop.getName().replaceAll(" ", "")).setText(((PluginProperty)prop).serialize());
            }
        }
                
        for (PluginProperty prop : PluginUtils.getProperties(this, false).values()) {
            root.addElement(prop.getName().replaceAll(" ", "")).setText(prop.serialize());
        }
        
        return document;
    }

    @Override
    public DefaultProperty[] getProperties() {
        Vector<DefaultProperty> props = new Vector<DefaultProperty>();
        props.addAll(Arrays.asList(super.getProperties()));    
        props.addAll(IMCUtils.getProperties(message, true));
        props.addAll(PluginUtils.getProperties(this, true).values());
        return props.toArray(new DefaultProperty[0]);
    }

    @Override
    public void setProperties(Property[] properties) {
        super.setProperties(properties);
        IMCUtils.setProperties(properties, message);
        PluginUtils.setPluginProperties(this, properties);
    }

    @Override
    public ManeuverLocation getStartLocation() {
        return getManeuverLocation();
    }

    @Override
    public ManeuverLocation getEndLocation() {
        return getManeuverLocation();
    }

    @Override
    public ManeuverLocation getManeuverLocation() {
        ManeuverLocation loc = new ManeuverLocation();
        try {
            loc.setLatitude(Math.toDegrees(message.getDouble("lat")));
            loc.setLongitude(Math.toDegrees(message.getDouble("lon")));
            String mode = message.getString("zunits");
            if (mode == null || mode.equals("NONE") || mode.equals("DEPTH"))
                loc.setDepth(message.getDouble("z"));
            else if (mode.equals("ALTITUDE") || mode.equals("HEIGHT"))
                loc.setHeight(message.getDouble("z"));
            
            return loc;
        }
        catch (Exception e) {
            return null;
        }                
    }

    @Override
    public void setManeuverLocation(ManeuverLocation location) {
        ManeuverLocation loc = location.clone();
        loc.convertToAbsoluteLatLonDepth();

        message.setValue("lat", Math.toRadians(loc.getLatitudeAsDoubleValue()));
        message.setValue("lon", Math.toRadians(loc.getLongitudeAsDoubleValue()));
        message.setValue("z", loc.getAllZ());
        message.setValue("zunits", "NONE");
    }

    @Override
    public void translate(double offsetNorth, double offsetEast, double offsetDown) {
        getManeuverLocation().translatePosition(offsetNorth, offsetEast, offsetDown);        
    }
    
    public static void main(String[] args) {
        ConfigFetch.initialize();
        CoverArea ca = new CoverArea();
        String xml = ca.asXML();
        System.out.println(xml);
        
        
    }
}
