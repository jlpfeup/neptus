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
 * Feb 26, 2013
 */
package pt.up.fe.dceg.neptus.maneuvers;

import java.util.Arrays;

import pt.up.fe.dceg.neptus.imc.DesiredSpeed;
import pt.up.fe.dceg.neptus.imc.DesiredSpeed.SPEED_UNITS;
import pt.up.fe.dceg.neptus.imc.DesiredZ;
import pt.up.fe.dceg.neptus.imc.DesiredZ.Z_UNITS;
import pt.up.fe.dceg.neptus.imc.FollowReference;
import pt.up.fe.dceg.neptus.imc.PlanControl;
import pt.up.fe.dceg.neptus.imc.PlanControl.OP;
import pt.up.fe.dceg.neptus.imc.PlanControl.TYPE;
import pt.up.fe.dceg.neptus.imc.PlanManeuver;
import pt.up.fe.dceg.neptus.imc.PlanSpecification;
import pt.up.fe.dceg.neptus.imc.Reference;
import pt.up.fe.dceg.neptus.imc.net.UDPTransport;
import pt.up.fe.dceg.neptus.util.comm.IMCSendMessageUtils;

/**
 * This program sends a PlanControl message containing a (quick) plan with a FollowReference maneuver
 * @author zp
 * 
 */
public class FollowReferenceTest {

    public static void main(String[] args) throws Exception {
        PlanControl startPlan = new PlanControl();
        startPlan.setType(TYPE.REQUEST);
        startPlan.setOp(OP.START);
        startPlan.setPlanId("follow_ref_test");
        FollowReference man = new FollowReference();
        man.setControlEnt((short)255);
        man.setControlSrc(65535);
        man.setAltitudeInterval(5);
        man.setTimeout(10);
        
        //startPlan.setPlanId("followref_test");
        PlanSpecification spec = new PlanSpecification();
        spec.setPlanId("followref_test");
        spec.setStartManId("1");
        PlanManeuver pm = new PlanManeuver();
        pm.setData(man);
        pm.setManeuverId("1");
        spec.setManeuvers(Arrays.asList(pm));
        startPlan.setArg(spec);
        int reqId = IMCSendMessageUtils.getNextRequestId();
        startPlan.setRequestId(reqId);
        startPlan.setFlags(0);
        UDPTransport t = new UDPTransport(6006, 1);
        //t.sendMessage("127.0.0.1", 6002, startPlan);
        
        while (true) {
            Thread.sleep(1000);
            
            DesiredSpeed dspeed = new DesiredSpeed();
            dspeed.setValue(1);
            dspeed.setSpeedUnits(SPEED_UNITS.METERS_PS);
            DesiredZ z = new DesiredZ();
            z.setValue(0);
            z.setZUnits(Z_UNITS.DEPTH);
            
            Reference ref = new Reference();
            ref.setLat(Math.toRadians(41.184199));
            ref.setLon(Math.toRadians(-8.705643));
            ref.setSpeed(dspeed);
            ref.setZ(z);
            ref.setFlags((short)(Reference.FLAG_SPEED | Reference.FLAG_LOCATION | Reference.FLAG_Z));
            t.sendMessage("127.0.0.1", 6002, ref);
        }     
    }    
}
