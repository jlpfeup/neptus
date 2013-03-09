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
 * Author: 
 * 20??/??/??
 */
package pt.up.fe.dceg.neptus.console.plugins;

import java.util.Vector;

import pt.up.fe.dceg.neptus.console.ConsoleLayout;
import pt.up.fe.dceg.neptus.console.ConsoleSystem;
import pt.up.fe.dceg.neptus.gui.PropertiesEditor;
import pt.up.fe.dceg.neptus.gui.PropertiesProvider;
import pt.up.fe.dceg.neptus.types.comm.CommMean;
import pt.up.fe.dceg.neptus.util.comm.CommUtil;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;

public class CommunicationsControlsProvider implements PropertiesProvider {

    ConsoleLayout console;

    public CommunicationsControlsProvider(ConsoleLayout cons) {
        console = cons;
    }

    public DefaultProperty[] getProperties() {

        Vector<DefaultProperty> props = new Vector<DefaultProperty>();

        for (ConsoleSystem j : console.getConsoleSystems().values()) {
            if (CommUtil.isProtocolSupported(j.getVehicleId(), CommMean.IMC))
                props.add(PropertiesEditor.getPropertyInstance(j.getVehicleId(), "IMC Communications", Boolean.class,
                        j.isNeptusCommunications(), true));
        }

        return props.toArray(new DefaultProperty[] {});
    }

    public void setProperties(Property[] properties) {

        for (Property p : properties) {
            for (ConsoleSystem j : console.getConsoleSystems().values()) {
                if (j.getVehicleId().equals(p.getName())) {

                    if (p.getCategory().equals("IMC Communications")) {
                        boolean imc =  (Boolean) p.getValue();
                        if(imc)
                            j.enableIMC();
                        else
                            j.disableIMC();
                    }
                }
            }
        }
    }

    public String getPropertiesDialogTitle() {
        return "Communications Panel";
    }

    public String[] getPropertiesErrors(Property[] properties) {
        return null;
    }
}
