/**
 * Tungsten Scale-Out Stack
 * Copyright (C) 2007-2009 Continuent Inc.
 * Contact: tungsten@continuent.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 * Initial developer(s): Robert Hodges
 * Contributor(s):
 */
package com.continuent.tungsten.fsm.core.test;

import com.continuent.tungsten.fsm.core.Entity;
import com.continuent.tungsten.fsm.core.State;
import com.continuent.tungsten.fsm.core.StateChangeListener;

public class SampleListener implements StateChangeListener
{
    int changes = 0;

    public void stateChanged(Entity entity, State oldState, State newState)
    {
        changes++;
    }
    
    public int getChanges()
    {
        return changes;
    }
}