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
 * Oct 11, 2011
 */
package pt.up.fe.dceg.neptus.mp.preview;

import pt.up.fe.dceg.neptus.mp.SystemPositionAndAttitude;
import pt.up.fe.dceg.neptus.mp.maneuvers.StationKeeping;
import pt.up.fe.dceg.neptus.types.coord.LocationType;

/**
 * @author zp
 *
 */
public class StationKeepingPreview implements IManeuverPreview<StationKeeping> {

    protected LocationType destination;
    protected double speed;
    protected boolean finished = false;
    protected double sk_time = -0.1;
    protected double maxTime, duration;
    protected boolean arrived = false;
    UnicycleModel model = new UnicycleModel();
    @Override
    public boolean init(String vehicleId, StationKeeping man, SystemPositionAndAttitude state, Object manState) {
        destination = new LocationType(man.getLocation());
        destination.setDepth(0);
        maxTime = man.getMaxTime();
        duration = man.getDuration();
        speed = man.getSpeed();
        model.setMaxSteeringRad(Math.toRadians(9));
        if (man.getSpeedUnits().equals("RPM")) 
            speed = SpeedConversion.convertRpmtoMps(speed);
        else if (man.getSpeedUnits().equals("%")) // convert to RPM and then to m/s
            speed = SpeedConversion.convertPercentageToMps(speed);

        speed = Math.min(speed, SpeedConversion.MAX_SPEED);              
        
        model.setState(state);        
        return true;
    }
    

    @Override
    public SystemPositionAndAttitude step(SystemPositionAndAttitude state, double timestep) {
  
        if (sk_time >= duration)
            finished = true;
        
        if (arrived) {
            sk_time += timestep;
            return model.getState();
        }
        
        if (state.getPosition().getDepth() > 0) {
            model.guide(destination, 0);
            model.advance(timestep);
        }
        else {
            model.getState().getPosition().setDepth(0);
            arrived = model.guide(destination, speed);
            model.advance(timestep);
            sk_time = 0;
        }
        
        return model.getState();
    }
    
    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void reset(SystemPositionAndAttitude state) {
        model.setState(state);
    }
    
    @Override
    public Object getState() {
        return null;
    }
}
