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

package com.continuent.tungsten.commons.patterns.fsm;

/**
 * Call-back interface to allow state machine client code to listen for 
 * state changes easily.  
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public interface StateChangeListener
{
    /**
     * Called to indicate a state change.  Implementation should be 
     * aware that this method call implicitly synchronizes on the state 
     * machine instance.  This means that listeners must finish quickly to 
     * release the monitor on the state machine instance.  Also, if the 
     * listener is in another thread it may not make calls to the state
     * machine that synchronize on the monitor or a deadlock will result. 
     * 
     * @param entity Entity whose state changed
     * @param oldState Old state of entity
     * @param newState New state of entity
     */
    public void stateChanged(Entity entity, State oldState, State newState);
}