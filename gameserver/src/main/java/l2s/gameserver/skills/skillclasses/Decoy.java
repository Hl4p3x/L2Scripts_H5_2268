package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;


/**
 * 
 * @author n0nam3
 * @date 23/07/2010 19:14
 */
public class Decoy extends Skill
{
	private final int _npcId;
	private final int _lifeTime;

	public Decoy(StatsSet set)
	{
		super(set);

		_npcId = set.getInteger("npcId", 0);
		_lifeTime = set.getInteger("lifeTime", 1200) * 1000;
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(activeChar.isAlikeDead() || !activeChar.isPlayer() || activeChar != target) // only TARGET_SELF
			return false;

		if(_npcId <= 0)
			return false;

		if(activeChar.isInObserverMode())
			return false;

		/* need correct
		if(activeChar.getServitor() != null || activeChar.getPlayer().isMounted())
		{
			activeChar.getPlayer().sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
			return false;
		}
		 */
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		Player activeChar = caster.getPlayer();

		NpcTemplate DecoyTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
		DecoyInstance decoy = new DecoyInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, activeChar, _lifeTime);

		decoy.setCurrentHp(decoy.getMaxHp(), false);
		decoy.setCurrentMp(decoy.getMaxMp());
		decoy.setHeading(activeChar.getHeading());
		decoy.setReflection(activeChar.getReflection());

		activeChar.setDecoy(decoy);

		decoy.spawnMe(caster.getLoc());
	}
}