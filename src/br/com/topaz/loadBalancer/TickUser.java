package br.com.topaz.loadBalancer;

/**
 * @author Rafael Costa
 *
 */
public class TickUser {
	private Long tick;
	private Long users;

	/**
	 * Class used to associate the number users to a tick.
	 * 
	 * @param tick
	 * @param users
	 */
	public TickUser(final Long tick, final Long users) {
		this.tick = tick;
		this.users = users;
	}

	public Long getTick() {
		return tick;
	}

	public void setTick(final Long tick) {
		this.tick = tick;
	}

	public Long getUsers() {
		return users;
	}

	public void setUsers(final Long users) {
		this.users = users;
	}

}
