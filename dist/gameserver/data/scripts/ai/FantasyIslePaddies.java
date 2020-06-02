package ai;

import l2s.gameserver.GameTimeController;
import l2s.gameserver.ai.NpcAI;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.game.OnDayNightChangeListener;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.FantasiIsleParadEvent;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author PaInKiLlEr
 *         - AI для Fantasy Isle Paddies (32378).
 *         - Запускает Парад НПЦ.
 *         - AI проверен и работает.
 */
public class FantasyIslePaddies extends NpcAI
{
	public FantasyIslePaddies(NpcInstance actor)
	{
		super(actor);
		GameTimeController.getInstance().addListener(new StartEvent());
	}

	private class StartEvent implements OnDayNightChangeListener
	{
		private StartEvent()
		{
			if(GameTimeController.getInstance().isNowNight())
				onNight();
			else
				onDay();
		}

		/**
		 * Вызывается, когда на сервере наступает ночь
		 */
		@Override
		public void onNight()
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				FantasiIsleParadEvent n_event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 10031);
				FantasiIsleParadEvent d_event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 10032);
				n_event.registerActions();
				d_event.stopEvent(false);
			}
		}

		/**
		 * Вызывается, когда на сервере наступает день
		 */
		@Override
		public void onDay()
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				FantasiIsleParadEvent n_event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 10031);
				FantasiIsleParadEvent d_event = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 10032);
				n_event.stopEvent(false);
				d_event.registerActions();
			}
		}
	}
}