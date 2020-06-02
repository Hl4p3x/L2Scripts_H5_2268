package l2s.gameserver.templates.npc;

/**
 * This class defines the spawn data of a Minion type
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 *
 * <B><U> Data</U> :</B><BR><BR>
 * <li>_minionId : The Identifier of the L2Minion to spawn </li>
 * <li>_minionAmount :  The number of this Minion Type to spawn </li><BR><BR>
 */
public class MinionData
{

	/** The Identifier of the L2Minion */
	private final int _minionId;

	/** The number of this Minion Type to spawn */
	private final int _minionAmount;

	public MinionData(int minionId, int minionAmount)
	{
		_minionId = minionId;
		_minionAmount = minionAmount;
	}

	/**
	 * Return the Identifier of the Minion to spawn.<BR><BR>
	 */
	public int getMinionId()
	{
		return _minionId;
	}

	/**
	 * Return the amount of this Minion type to spawn.<BR><BR>
	 */
	public int getAmount()
	{
		return _minionAmount;
	}

	@Override 
	public boolean equals(Object o)
	{
		if(o == this)
			return true;
		if(o == null)
			return false;
		if(o.getClass() != this.getClass())
			return false;
		return ((MinionData)o).getMinionId() == this.getMinionId();
	}

	@Override     
	public int hashCode()
	{
		return 7 * getMinionId() + 17840;
	}
}