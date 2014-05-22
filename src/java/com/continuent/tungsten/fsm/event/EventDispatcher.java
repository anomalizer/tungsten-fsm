/**
 * Tungsten Finite State Machine Library (FSM)
 * Copyright (C) 2007-2011 Continuent Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 * Initial developer(s): Teemu Ollakka
 * Contributor(s): Robert Hodges
 */

package com.continuent.tungsten.fsm.event;

import com.continuent.tungsten.fsm.core.Event;

/**
 * Denotes a class that can dispatch events to a state machine. It handles
 * normal events, out-of-band events, and event cancellation.
 * 
 * @author <a href="mailto:teemu.ollakka@continuent.com">Teemu Ollakka</a>
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public interface EventDispatcher
{
    /**
     * Set a listenener for event completion.
     */
    public void setListener(EventCompletionListener listener);

    /**
     * Puts an event in the queue for normal processing. This method returns a
     * Future that callers can call to obtain the event status.
     */
    public <EventType> EventRequest<EventType> put(Event<EventType> event) throws InterruptedException;

    /**
     * Cancel all pending events and put a new event in the queue for immediate
     * processing.
     */
    public <EventType> EventRequest<EventType> putOutOfBand(Event<EventType> event) throws InterruptedException;

    /**
     * Cancel a currently running request.
     * 
     * @param request Request to cancel
     * @param mayInterruptIfRunning If true we can cancel running as opposed to
     *            enqueued request
     */
    public boolean cancelActive(EventRequest<?> request,
            boolean mayInterruptIfRunning) throws InterruptedException;
}