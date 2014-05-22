/**
 * Tungsten Finite State Machine Library (FSM)
 * Copyright (C) 2008-2011 Continuent Inc.
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
 * Initial developer(s):  Robert Hodges
 * Contributor(s):  Teemu Ollakka
 */

package com.continuent.tungsten.fsm.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.StateMachine;

/**
 * Submits an event to the state machine with logging and sets the status.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class EventProcessor implements Runnable
{
    private static Logger                 logger = LoggerFactory.getLogger(EventProcessor.class);
    private final StateMachine            sm;
    private final EventRequest<?>         request;
    private final EventCompletionListener listener;

    /**
     * Instantiates a new processor for a particular event and state machine.
     */
    EventProcessor(StateMachine stateMachine, EventRequest<?> request,
            EventCompletionListener listener)
    {
        this.sm = stateMachine;
        this.request = request;
        this.listener = listener;
    }

    /**
     * Submits the event and computes status.
     */
    public void run()
    {
        Event<?> event = request.getEvent();
        if (logger.isDebugEnabled())
            logger.debug("Processing event: "
                    + event.getClass().getSimpleName());

        EventStatus status = null;
        try
        {
            if (request.isCancelRequested())
            {
                // If the event is cancelled note it.
                status = new EventStatus(false, true, null);
                if (logger.isDebugEnabled())
                    logger.debug("Skipped cancelled event: "
                            + event.getClass().getSimpleName());
            }
            else
            {
                // Mark the request as started and submit to state machine.
                request.started();
                sm.applyEvent(event);
                status = new EventStatus(true, false, null);
                if (logger.isDebugEnabled())
                    logger.debug("Applied event: "
                            + event.getClass().getSimpleName());
            }
        }
        catch (InterruptedException e)
        {
            // Handle an interruption, which could happen if we are cancelled
            // while executing.
            status = new EventStatus(false, true, e);
            logger.debug(String.format("Failed to apply event %s, reason=%s",
                    event, e.getLocalizedMessage()));
        }
        catch (Throwable e)
        {
            // Handle a failure.
            status = new EventStatus(false, false, e);
            logger.debug(String.format("Failed to apply event %s, reason=%s",
                    event, e.getLocalizedMessage()));
        }
        finally
        {
            // We need to store the status and call the completion
            // listener, if any. This must happen regardless of any
            // exception that occurs.
            try
            {
                if (listener != null)
                    request.setAnnotation(listener.onCompletion(event, status));
            }
            catch (InterruptedException e)
            {
                // Do nothing; this is the end of the road for this task.
            }
            catch (Throwable e)
            {
                logger.error("Unexpected failure while calling listener", e);
            }
            finally
            {
                // Make sure we record the request state no matter what to
                // prevent hangs.
                request.setStatus(status);
            }
        }
    }
}
