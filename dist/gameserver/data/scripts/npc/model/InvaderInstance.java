package npc.model;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

//By Evil_dnk

public class InvaderInstance extends MonsterInstance
{
	public final static int INVADER = 40002;
	public final static int INVADER_G = 40003;

	private HardReference<Player> _spawnerRef;

	public InvaderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public void reduceCurrentHp(double i, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker.getActiveWeaponInstance() == null)
			return;

		int weaponId = attacker.getActiveWeaponInstance().getItemId();

		if(getNpcId() == INVADER || getNpcId() == INVADER_G)
		{
			// 4202 Chrono Cithara
			// 5133 Chrono Unitus
			// 5817 Chrono Campana
			// 7058 Chrono Darbuka
			// 8350 Chrono Maracas
			if(weaponId != 4202 && weaponId != 5133 && weaponId != 5817 && weaponId != 7058 && weaponId != 8350)
				return;
			i = 1;
		}
		else
			return;

		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	public long getRegenTick()
	{
		return 0L;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}