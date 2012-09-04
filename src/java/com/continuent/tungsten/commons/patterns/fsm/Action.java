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
 * This interfaces denotes a procedure that may be executed as part of
 * processing a state transition. Three types of actions are possible:
 * <ul>
 * <li>EXIT_ACTION - Action taken on leaving a state</li>
 * <li>TRANSITION_ACTION - Action taken on traversing a transition</li>
 * <li>ENTRY_ACTION - Action taken on entering a state</li>
 * </ul>
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public interface Action<ET extends Entity>
{
    /** An action executed on leaving a state. */
    public static final int EXIT_ACTION       = 1;
    
    /** An action executed by the transition. */
    public static final int TRANSITION_ACTION = 2;
    
    /** An action executed on entering a state. */
    public static final int ENTER_ACTION      = 3;

    /**
     * Perform an action as part of a transition.  TransitionRollbackException
     * provides a mechanism for Action implementations to force a transition 
     * to roll back.  Unhandled exceptions are passed back up the stack; state
     * machine behavior in this case is undefined.  
     * 
     * @param message Event that triggered the transition
     * @param entity Entity whose state is changing
     * @param transition Transition we are executing
     * @param actionType Type of action
     * @throws TransitionRollbackException Thrown if the state transition has failed
     *         and may be safely rolled back. 
     * @throws TransitionFailureException Thrown if the state transition failed and
     *         state machine should move to default error state. 
     */
    public void doAction(Event message, ET entity, Transition<ET> transition,
            int actionType) throws TransitionRollbackException, TransitionFailureException;
}