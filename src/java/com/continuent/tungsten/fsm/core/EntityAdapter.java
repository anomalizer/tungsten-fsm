/**
 * Tungsten Finite State Machine Library (FSM)
 * Copyright (C) 2007-2009 Continuent Inc.
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
 * Initial developer(s): Robert Hodges
 * Contributor(s):
 */

package com.continuent.tungsten.fsm.core;

/**
 * Provides an adapter that permits ordinary objects to be handled as entities
 * without implementing the Entity interface directly.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class EntityAdapter<EntityType> implements Entity
{
    EntityType entity;

    /**
     * Creates a new instance
     * 
     * @param entity An entity that this adapter should hold
     */
    public EntityAdapter(EntityType entity)
    {
        this.entity = entity;
    }

    /**
     * Returns the entity stored in this adapter.
     */
    public EntityType getEntity()
    {
        return entity;
    }

    /**
     * Set the entity instance in the adapter.
     */
    public void setEntity(EntityType entity)
    {
        this.entity = entity;
    }
}