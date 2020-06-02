package quests;

import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.network.l2.components.NpcString;

/**
 * @author VISTALL
 * @date 16:14/12.04.2011
 */
public class _735_MakeSpearsDull extends Dominion_KillSpecialUnitQuest
{
	public _735_MakeSpearsDull()
	{
		super();
	}

	@Override
	protected NpcString startNpcString()
	{
		return NpcString.DEFEAT_S1_WARRIORS_AND_ROGUES;
	}

	@Override
	protected NpcString progressNpcString()
	{
		return NpcString.YOU_HAVE_DEFEATED_S2_OF_S1_WARRIORS_AND_ROGUES;
	}

	@Override
	protected NpcString doneNpcString()
	{
		return NpcString.YOU_WEAKENED_THE_ENEMYS_ATTACK;
	}

	@Override
	protected int getRandomMin()
	{
		return 15;
	}

	@Override
	protected int getRandomMax()
	{
		return 20;
	}

	@Override
	protected ClassId[] getTargetClassIds()
	{
		return new ClassId[]{
				ClassId.GLADIATOR,
				ClassId.WARLORD,
				ClassId.TREASURE_HUNTER,
				ClassId.HAWKEYE,
				ClassId.PLAIN_WALKER,
				ClassId.SILVER_RANGER,
				ClassId.ABYSS_WALKER,
				ClassId.PHANTOM_RANGER,
				ClassId.DESTROYER,
				ClassId.TYRANT,
				ClassId.BOUNTY_HUNTER,
				ClassId.DUELIST,
				ClassId.DREADNOUGHT,
				ClassId.SAGITTARIUS,
				ClassId.ADVENTURER,
				ClassId.WIND_RIDER,
				ClassId.MOONLIGHT_SENTINEL,
				ClassId.GHOST_HUNTER,
				ClassId.GHOST_SENTINEL,
				ClassId.TITAN,
				ClassId.GRAND_KHAVATARI,
				ClassId.FORTUNE_SEEKER,
				ClassId.BERSERKER,
				ClassId.M_SOUL_BREAKER,
				ClassId.F_SOUL_BREAKER,
				ClassId.ARBALESTER,
				ClassId.DOOMBRINGER,
				ClassId.M_SOUL_HOUND,
				ClassId.F_SOUL_HOUND,
				ClassId.TRICKSTER
		};
	}
}
