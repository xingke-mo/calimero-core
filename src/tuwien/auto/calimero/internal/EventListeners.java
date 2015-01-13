/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2006, 2014 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Linking this library statically or dynamically with other modules is
    making a combined work based on this library. Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.

    As a special exception, the copyright holders of this library give you
    permission to link this library with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under terms
    of your choice, provided that you also meet, for each linked independent
    module, the terms and conditions of the license of that module. An
    independent module is a module which is not derived from or based on
    this library. If you modify this library, you may extend this exception
    to your version of the library, but you are not obligated to do so. If
    you do not wish to do so, delete this exception statement from your
    version.
*/

package tuwien.auto.calimero.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

/**
 * Container for keeping event listeners.
 * <p>
 * The assumption for implementation of this class is that iterating over event listeners
 * is the predominant operation, adding and removing listeners not.
 *
 * @author B. Malinowsky
 */
public class EventListeners<T extends EventListener>
{
	private final List<T> listeners = new ArrayList<>();
	private T[] listenersCopy;
	private final Class<T> type;
	private final Logger logger;

	/**
	 * Creates a new event listeners container object.
	 *
	 * @param listenerType common class type of listeners EventListeners is parameterized for
	 */
	public EventListeners(final Class<T> listenerType)
	{
		this(listenerType, null);
	}

	/**
	 * Creates a new event listeners container object.
	 *
	 * @param listenerType common class type of listeners EventListeners is parameterized for
	 * @param logger optional logger for log output
	 */
	public EventListeners(final Class<T> listenerType, final Logger logger)
	{
		type = listenerType;
		this.logger = logger;
		createCopy();
	}

	/**
	 * Adds the specified event listener <code>l</code> to this container.
	 * <p>
	 * If <code>l</code> is
	 * <code>null</code> or was already added as listener, no action is performed.
	 *
	 * @param l the listener to add
	 */
	public void add(final T l)
	{
		if (l == null)
			return;
		synchronized (listeners) {
			if (!listeners.contains(l)) {
				listeners.add(l);
				createCopy();
			}
			else if (logger != null)
				logger.warn("event listener already registered");
		}
	}

	/**
	 * Removes the specified event listener <code>l</code> from this container.
	 * <p>
	 * If <code>l</code> was not added in the first place, no action is performed.
	 *
	 * @param l the listener to remove
	 */
	public void remove(final T l)
	{
		synchronized (listeners) {
			if (listeners.remove(l))
				createCopy();
		}
	}

	/**
	 * Removes all event listeners from this container.
	 */
	public void removeAll()
	{
		synchronized (listeners) {
			listeners.clear();
			createCopy();
		}
	}

	/**
	 * Returns an array with all event listeners.
	 * <p>
	 * While modifying the returned array will have no impact on the event listeners kept
	 * by this class, the array might be reused for subsequent callers, who will be
	 * affected.
	 *
	 * @return array with all event listeners in this container, with array size equal to
	 *         the number of contained listeners
	 */
	public T[] listeners()
	{
		return listenersCopy;
	}

	/**
	 * Returns an iterator for the contained event listeners.
	 * <p>
	 *
	 * @return the iterator for the listeners
	 */
	public Iterator<T> iterator()
	{
		return Arrays.asList(listenersCopy).iterator();
	}

	private void createCopy()
	{
		@SuppressWarnings("unchecked")
		final T[] t = (T[]) Array.newInstance(type, listeners.size());
		listenersCopy = listeners.toArray(t);
	}

	// not for general use, quite slow due to reflection mechanism
	/*
	void fire(final Object event, final Method method)
	{
		final Object[] objs = new Object[] { event };
		for (final Iterator i = iterator(); i.hasNext();) {
			final EventListener l = (EventListener) i.next();
			try {
				method.invoke(l, objs);
			}
			catch (final RuntimeException rte) {
				remove(l);
				logger.error("removed event listener", rte);
			}
			catch (final IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (final InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	*/
}
