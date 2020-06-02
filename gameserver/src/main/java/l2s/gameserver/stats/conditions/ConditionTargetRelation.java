package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.stats.Env;

public class ConditionTargetRelation extends Condition
{
	private final Relation _state;

	public static enum Relation
	{
		Neutral,
		Friend,
		Enemy;
	}

	public ConditionTargetRelation(Relation state)
	{
		_state = state;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return getRelation(env.character, env.target) == _state;
	}

	public static Relation getRelation(Creature activeChar, Creature aimingTarget)
	{
		if(activeChar.isPlayable() && activeChar.getPlayer() != null)
		{
			if(aimingTarget.isMonster())
				return Relation.Enemy;
			Player player = activeChar.getPlayer();

			if(aimingTarget.isPlayable() && aimingTarget.getPlayer() != null)
			{
				Player target = aimingTarget.getPlayer();

				if(player == target && player.getParty() != null && player.getParty() == target.getParty())
					return Relation.Friend;
				if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == target.getOlympiadSide())
					return Relation.Friend;
				if(player.getTeam() != TeamType.NONE && target.getTeam() != TeamType.NONE && player.getTeam() == target.getTeam())
					return Relation.Friend;
				if(activeChar.isInZoneBattle())
					return Relation.Enemy;
				if(activeChar.isInZonePvP())
					return Relation.Enemy;					
				if(player.getClanId() != 0 && player.getClanId() == target.getClanId())
				{
					return Relation.Friend;
				}
				if(player.getAllyId() != 0 && player.getAllyId() == target.getAllyId())
				{
					return Relation.Friend;
				}
				if(player == target && player.getParty() != null && player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == target.getParty().getCommandChannel())
					return Relation.Friend;
				if(activeChar.isInZonePeace())
					return Relation.Neutral;
				if(player.atMutualWarWith(target))
					return Relation.Enemy;
				if(target.getKarma() > 0)
					return Relation.Enemy;
			}
		}
		return Relation.Neutral;
	}
}