package l2s.gameserver.ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;

public class Keltirs extends Fighter
{
	// Радиус на который будут отбегать келтиры.
	private static final int range = 600;
	// Время в мс. через которое будет повторяться Rnd фраза.
	private static final int voicetime = 8000;
	private long _lastAction;
	private static final String[] _retreatText = {"Не трогай меня, я вымирающий вид!", "Почему именно я ? ...", "Если еще раз меня ударишь - у тебя будут неприятности!", "Богиня Кельтир я иду к тебе!", "Мы принимаем Бооой! (чешется)", "Каждый сам за себя..."};

	private static final String[] _fightText = {"А мы уйдем на север и переждем.", "Рррррррр!", "А мы уйдем на север!"};

	public Keltirs(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean createNewTask()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return false;
		}

		if(Rnd.chance(60))
		{
			// clearTasks();
			Creature target;
			if((target = prepareTarget()) == null)
			{
				return false;
			}

			// Добавить новое задание
			addTaskAttack(target);

			if(System.currentTimeMillis() - _lastAction > voicetime)
			{
				Functions.npcSay(actor, _fightText[Rnd.get(_fightText.length)]);
				_lastAction = System.currentTimeMillis();
			}
			return true;
		}

		Location sloc = actor.getSpawnedLoc();
		int spawnX = sloc.x;
		int spawnY = sloc.y;
		int spawnZ = sloc.z;

		int x = spawnX + Rnd.get(2 * range) - range;
		int y = spawnY + Rnd.get(2 * range) - range;
		int z = GeoEngine.getLowerHeight(x, y, spawnZ, actor.getReflection().getGeoIndex());

		actor.setRunning();

		actor.moveToLocation(x, y, z, 0, true);

		addTaskMove(spawnX, spawnY, spawnZ, false, false);
		if(System.currentTimeMillis() - _lastAction > voicetime)
		{
			Functions.npcSay(actor, _retreatText[Rnd.get(_retreatText.length)]);
			_lastAction = System.currentTimeMillis();
		}
		return true;
	}
}