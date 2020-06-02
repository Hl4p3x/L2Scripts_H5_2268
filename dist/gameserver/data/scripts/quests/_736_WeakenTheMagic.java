package quests;

import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.network.l2.components.NpcString;

/**
 * @author VISTALL
 * @date 16:18/12.04.2011
 */
public class _736_WeakenTheMagic extends Dominion_KillSpecialUnitQuest
{
	public _736_WeakenTheMagic()
	{
		super();
	}

	@Override
	protected NpcString startNpcString()
	{
		return NpcString.DEFEAT_S1_WIZARDS_AND_SUMMONERS;
	}

	@Override
	protected NpcString progressNpcString()
	{
		return NpcString.YOU_HAVE_DEFEATED_S2_OF_S1_ENEMIES;
	}

	@Override
	protected NpcString doneNpcString()
	{
		return NpcString.YOU_WEAKENED_THE_ENEMYS_MAGIC;
	}

	@Override
	protected int getRandomMin()
	{
		return 10;
	}

	@Override
	protected int getRandomMax()
	{
		return 15;
	}

	@Override
	protected ClassId[] getTargetClassIds()
	{
		return new ClassId[]{
				ClassId.SORCERER,
				ClassId.WARLOCK,
				ClassId.SPELLSINGER,
				ClassId.ELEMENTAL_SUMMONER,
				ClassId.SPELLHOWLER,
				ClassId.PHANTOM_SUMMONER,
				ClassId.ARCHMAGE,
				ClassId.ARCANA_LORD,
				ClassId.MYSTIC_MUSE,
				ClassId.ELEMENTAL_MASTER,
				ClassId.STORM_SCREAMER,
				ClassId.SPECTRAL_MASTER
		};
	}
}
