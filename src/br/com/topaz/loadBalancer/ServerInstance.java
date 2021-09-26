package br.com.topaz.loadBalancer;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Rafael Costa
 *
 */
public class ServerInstance {

	LinkedList<TickUser> listTickUsers;
	private Long totalUsers = Long.valueOf(0);
	private int ttask = 0;
	private int umax = 0;

	/**
	 * 
	 * Class used to control the server instances.
	 * 
	 * @param tick  - The tick value
	 * @param users - Number of users to be loaded
	 * @param ttask - Number of ticks to perform a task
	 * @param umax  - Number of users that each server supports simultaneously
	 */
	public ServerInstance(final Long tick, final Long users, final int ttask, final int umax) {
		listTickUsers = new LinkedList<TickUser>();
		TickUser tickUser = new TickUser(tick, users);
		listTickUsers.add(tickUser);
		totalUsers = users;
		this.ttask = ttask;
		this.umax = umax;
	}

	public Long getTotalUsers() {
		return totalUsers;
	}

	/**
	 * Returns the total number of users after the updating of a tick.
	 * 
	 * @param tick - The tick value
	 * @return totalUsers - The number of users loaded in the server instance
	 */
	public Long updateListAfterTick(final Long tick) {
		Iterator<TickUser> iterator = listTickUsers.iterator();
		TickUser tickUser = null;
		while (iterator.hasNext()) {
			tickUser = iterator.next();
			if ((tick - ttask) == tickUser.getTick()) {
				totalUsers -= tickUser.getUsers();
				iterator.remove();
			}
		}
		return totalUsers;
	}

	/**
	 * Add _usersToLoad to the ServerInstance, associating them with the specific
	 * tick. Returns the number of user that still need to be added to servers.
	 * 
	 * @param tick         - The tick value
	 * @param _usersToLoad - The number of users to be leaded
	 * @return - (_usersToLoad - usersAccepted)
	 */
	public Long addTickUser(final Long tick, final Long _usersToLoad) {
		if (totalUsers == umax) {
			return _usersToLoad;
		}
		Long usersAccepted = Long.valueOf(0);
		Long usersAvailable = Long.valueOf(umax) - Long.valueOf(totalUsers);
		if (usersAvailable >= _usersToLoad) {
			usersAccepted = _usersToLoad;
		} else {
			usersAccepted = usersAvailable;
		}
		TickUser tickUser = new TickUser(tick, usersAccepted);
		listTickUsers.add(tickUser);
		totalUsers += usersAccepted;
		return _usersToLoad - usersAccepted;
	}

}
