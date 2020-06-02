package ai.PaganTemplete;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

/**
 * @author PaInKiLlEr
 *         - AI для монстра Andreas Captain Royal Guard (22175).
 *         - Если увидел игрока в радиусе 500, если его пати состовляет больше 9 мемберов.
 *         - Тогда выбрасывает на рандомные координаты первого увидевшего игрока.
 *         - При атаке когда остаётся ХП меньше 70%, кидают дебаф и умерают.
 *         - AI проверен и работает.
 */
public class AndreasCaptainRoyalGuard extends Fighter
{
	private static int NUMBER_OF_DEATH = 0;
	private boolean _tele = true;
	private boolean _talk = true;

	public static final Location[] locs = {new Location( -16128, -35888, -10726), new Location( -17029, -39617, -10724), new Location( -15729, -42001, -10724)};

	public AndreasCaptainRoyalGuard(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		for(Player player : World.getAroundPlayers(actor, 500, 500))
		{
			if(player == null || !player.isInParty())
				continue;

			if(player.getParty().getMemberCount() >= 9 && _tele)
			{
				_tele = false;
				player.teleToLocation(Rnd.get(locs));
			}
		}

		return true;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();

		if(actor.getCurrentHpPercents() <= 70)
		{
			actor.doCast(SkillHolder.getInstance().getSkill(4612, 9), attacker, true);
			actor.doDie(attacker);
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		NUMBER_OF_DEATH++;
		// Двери на балкон
		// Door door1 = DoorHolder.getInstance().getDoor(19160014);
		// Door door2 = DoorHolder.getInstance().getDoor(19160015);
		// Двери к алтарю
		// Door door3 = DoorHolder.getInstance().getDoor(19160016);
		// Door door4 = DoorHolder.getInstance().getDoor(19160017);
		if(NUMBER_OF_DEATH == 39 && _talk)
		{
			_talk = false;
			// Сбрасываем память
			NUMBER_OF_DEATH = 0;
			// мы убили всех монстров на балконе, закрываем двери на балкон
			// door1.closeMe(actor);
			// door2.closeMe(actor);
			// открываем двери к алтарю
			// door3.openMe(actor, false);
			// door4.openMe(actor, false);
		}
		_tele = true;
		super.onEvtDead(killer);
	}
}