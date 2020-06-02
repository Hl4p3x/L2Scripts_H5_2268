package events.SummerMeleons;

import java.util.concurrent.ScheduledFuture;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.reward.RewardData;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Log;
import npc.model.MeleonInstance;

public class MeleonAI extends Fighter
{
	public class PolimorphTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			MeleonInstance actor = getActor();
			if(actor == null)
				return;
			SimpleSpawner spawn = null;

			try
			{
				spawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(_npcId));
				spawn.setLoc(actor.getLoc());
				NpcInstance npc = spawn.doSpawn(true);
				npc.setAI(new MeleonAI(npc));
				((MeleonInstance) npc).setSpawner(actor.getSpawner());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			_timeToUnspawn = Long.MAX_VALUE;
			actor.deleteMe();
		}
	}

	protected static final RewardData[] _dropList = new RewardData[] {
			new RewardData(1539, 1, 5, 15000), // Greater Healing Potion
			new RewardData(1374, 1, 3, 15000), // Greater Haste Potion

			new RewardData(4411, 1, 1, 5000), // Echo Crystal - Theme of Journey
			new RewardData(4412, 1, 1, 5000), // Echo Crystal - Theme of Battle
			new RewardData(4413, 1, 1, 5000), // Echo Crystal - Theme of Love
			new RewardData(4414, 1, 1, 5000), // Echo Crystal - Theme of Solitude
			new RewardData(4415, 1, 1, 5000), // Echo Crystal - Theme of the Feast
			new RewardData(4416, 1, 1, 5000), // Echo Crystal - Theme of Celebration
			new RewardData(4417, 1, 1, 5000), // Echo Crystal - Theme of Comedy
			new RewardData(5010, 1, 1, 5000), // Echo Crystal - Theme of Victory

			new RewardData(1458, 10, 30, 13846), // Crystal: D-Grade 1.3%
			new RewardData(1459, 10, 30, 3000), // Crystal: C-Grade  0.3%
			new RewardData(1460, 10, 30, 1000), // Crystal: B-Grade  0.1%
			new RewardData(1461, 10, 30, 600), // Crystal: A-Grade   0.06%
			new RewardData(1462, 10, 30, 360), // Crystal: S-Grade   0.036%

			new RewardData(4161, 1, 1, 5000), // Recipe: Blue Wolf Tunic
			new RewardData(4182, 1, 1, 5000), // Recipe: Great Sword
			new RewardData(4174, 1, 1, 5000), // Recipe:  Zubei's Boots
			new RewardData(4166, 1, 1, 5000), // Recipe: Doom Helmet

			new RewardData(8660, 1, 1, 1000), // Demon Horns        0.1%
			new RewardData(8661, 1, 1, 1000), // Mask of Spirits    0.1%
			new RewardData(4393, 1, 1, 300), // Calculator          0.03%
			new RewardData(7836, 1, 1, 200), // Santa's Hat         0.02%
			new RewardData(5590, 1, 1, 200), // Squeaking Shoes     0.02%
			new RewardData(7058, 1, 1, 50), // Chrono Darbuka       0.005%
			new RewardData(8350, 1, 1, 50), // Chrono Maracas       0.005%
			new RewardData(5133, 1, 1, 50), // Chrono Unitus        0.005%
			new RewardData(5817, 1, 1, 50), // Chrono Campana       0.005%
			new RewardData(9140, 1, 1, 30), // Salvation Bow        0.003%

			// Призрачные аксессуары - шанс 0.01%
			new RewardData(9177, 1, 1, 100), // Teddy Bear Hat - Blessed Resurrection Effect
			new RewardData(9178, 1, 1, 100), // Piggy Hat - Blessed Resurrection Effect
			new RewardData(9179, 1, 1, 100), // Jester Hat - Blessed Resurrection Effect
			new RewardData(9180, 1, 1, 100), // Wizard's Hat - Blessed Resurrection Effect
			new RewardData(9181, 1, 1, 100), // Dapper Cap - Blessed Resurrection Effect
			new RewardData(9182, 1, 1, 100), // Romantic Chapeau - Blessed Resurrection Effect
			new RewardData(9183, 1, 1, 100), // Iron Circlet - Blessed Resurrection Effect
			new RewardData(9184, 1, 1, 100), // Teddy Bear Hat - Blessed Escape Effect
			new RewardData(9185, 1, 1, 100), // Piggy Hat - Blessed Escape Effect
			new RewardData(9186, 1, 1, 100), // Jester Hat - Blessed Escape Effect
			new RewardData(9187, 1, 1, 100), // Wizard's Hat - Blessed Escape Effect
			new RewardData(9188, 1, 1, 100), // Dapper Cap - Blessed Escape Effect
			new RewardData(9189, 1, 1, 100), // Romantic Chapeau - Blessed Escape Effect
			new RewardData(9190, 1, 1, 100), // Iron Circlet - Blessed Escape Effect
			new RewardData(9191, 1, 1, 100), // Teddy Bear Hat - Big Head
			new RewardData(9192, 1, 1, 100), // Piggy Hat - Big Head
			new RewardData(9193, 1, 1, 100), // Jester Hat - Big Head
			new RewardData(9194, 1, 1, 100), // Wizard Hat - Big Head
			new RewardData(9195, 1, 1, 100), // Dapper Hat - Big Head
			new RewardData(9196, 1, 1, 100), // Romantic Chapeau - Big Head
			new RewardData(9197, 1, 1, 100), // Iron Circlet - Big Head
			new RewardData(9198, 1, 1, 100), // Teddy Bear Hat - Firework
			new RewardData(9199, 1, 1, 100), // Piggy Hat - Firework
			new RewardData(9200, 1, 1, 100), // Jester Hat - Firework
			new RewardData(9201, 1, 1, 100), // Wizard's Hat - Firework
			new RewardData(9202, 1, 1, 100), // Dapper Hat - Firework
			new RewardData(9203, 1, 1, 100), // Romantic Chapeau - Firework
			new RewardData(9204, 1, 1, 100), // Iron Circlet - Firework

			new RewardData(9146, 1, 3, 5000), // Scroll of Guidance        0.5%
			new RewardData(9147, 1, 3, 5000), // Scroll of Death Whisper   0.5%
			new RewardData(9148, 1, 3, 5000), // Scroll of Focus           0.5%
			new RewardData(9149, 1, 3, 5000), // Scroll of Acumen          0.5%
			new RewardData(9150, 1, 3, 5000), // Scroll of Haste           0.5%
			new RewardData(9151, 1, 3, 5000), // Scroll of Agility         0.5%
			new RewardData(9152, 1, 3, 5000), // Scroll of Empower         0.5%
			new RewardData(9153, 1, 3, 5000), // Scroll of Might           0.5%
			new RewardData(9154, 1, 3, 5000), // Scroll of Wind Walk       0.5%
			new RewardData(9155, 1, 3, 5000), // Scroll of Shield          0.5%
			new RewardData(9156, 1, 3, 2000), // BSoE                      0.2%
			new RewardData(9157, 1, 3, 1000), // BRES                      0.1%

			new RewardData(955, 1, 1, 400), // EWD          0.04%
			new RewardData(956, 1, 1, 2000), // EAD         0.2%
			new RewardData(951, 1, 1, 300), // EWC          0.03%
			new RewardData(952, 1, 1, 1500), // EAC         0.15%
			new RewardData(947, 1, 1, 200), // EWB          0.02%
			new RewardData(948, 1, 1, 1000), // EAB         0.1%
			new RewardData(729, 1, 1, 100), // EWA          0.01%
			new RewardData(730, 1, 1, 500), // EAA          0.05%
			new RewardData(959, 1, 1, 50), // EWS           0.005%
			new RewardData(960, 1, 1, 300) // EAS          0.03%
	};

	public final static int Young_Watermelon = 13271;
	public final static int Rain_Watermelon = 13273;
	public final static int Defective_Watermelon = 13272;
	public final static int Young_Honey_Watermelon = 13275;
	public final static int Rain_Honey_Watermelon = 13277;
	public final static int Defective_Honey_Watermelon = 13276;
	public final static int Large_Rain_Watermelon = 13274;
	public final static int Large_Rain_Honey_Watermelon = 13278;

	public final static int Squash_Level_up = 4513;
	public final static int Squash_Poisoned = 4514;

	private static final String[] textOnSpawn = new String[] {
			"scripts.events.SummerMeleons.MeleonAI.textOnSpawn.0",
			"scripts.events.SummerMeleons.MeleonAI.textOnSpawn.1",
			"scripts.events.SummerMeleons.MeleonAI.textOnSpawn.2" };

	private static final String[] textOnAttack = new String[] {
			"Кто меня кусает? ай! ой! эй ты, сейчас я тебе задам!",
			"Ха-ха-ха! Я вырос всем на зависть, только посмотри!",
			"Ты совсем мазила? Попасть во фрукт не можешь!",
			"Это ты так просчитываешь свои удары? Поищи лучше учителя прицеливания...",
			"Не трать свое время, я бессмертен!",
			"Ха! Правда приятный звук?",
			"Пока ты атакуешь я росту, а вырасту, стану выше тебя в два раза!",
			"Ты бьешь или щекочешь? Не могу разобрать... Жалкие потуги!",
			"Только музыкальное оружие открывает арбуз. Твое затупленное оружие здесь не помощник!" };

	private static final String[] textTooFast = new String[] {
			"Вот это удары! Вот это техника!",
			"Эй ты! Твои навыки плачевны, моя бабушка дерется лучше! Ха-Ха-Ха!!!",
			"Ну-ка еще разок ударь, и снова!",
			"Я твой дом труба шатал!",
			"Слышь, а семки есть? А пять аден? А позвонить? Хахаха!!!",
			"Какие непристойности! Давай без этих шуточек!",
			"Прояви фантазию, подойди сзади, что ты там топчешься!",
			"Разбуди как будешь уходить, ты совсем уныл и скучен..." };

	private static final String[] textSuccess0 = new String[] {
			"Арбуз хорошо растет если его тщательно поить, ты знаешь этот секрет, не так ли?",
			"Вот это я понимаю нектар, а то всегда какие-то помои!",
			"Я вижу! Вижу! Это Китай! О боже, я китайский арбуз!!",
			"Давай наливай еще, между первой и второй перерывчик небольшой!",
			"Дозаправка на лету!" };

	private static final String[] textFail0 = new String[] {
			"Ты оглох? Мне нужен нектар, а не то что ты льешь!",
			"Какой же ты неудачник, а выглядишь вроде бы бодро! Мне нужен нектар, лей качественный, иначе получишь шишь!",
			"И снова fail, сколько же можно? Ты меня насмешить хочешь?" };

	private static final String[] textSuccess1 = new String[] {
			"Сейчас спою! Арбуууууу-э-э-э-э!",
			"Вот так хорошо, так очень хорошо, не останавливайся!",
			"Я росту быстро, успеешь отскочить? Ха!",
			"Да ты мастер своего дела! Продолжай, прошу!" };

	private static final String[] textFail1 = new String[] {
			"Куй железо не отходя от кассы! А иначе никаких тебе коврижек.",
			"Растяпа! Невежда! Олух! Неудачник! Опять ты мне скормил помои!",
			"Давай-ка активней меня окучивай, поливай хорошенько! Что это за жалкие потуги?",
			"Ты хочешь чтобы я так и помер? Давай выращивай правильней!" };

	private static final String[] textSuccess2 = new String[] {
			"Вот! Вот так! Давай же, и скоро я полюблю тебя навсегда!",
			"Такими темпами я стану императором арбузов!",
			"Очень хорошо, ставлю тебе зачет по аграрному хозяйству, ты с умом меня растишь!" };

	private static final String[] textFail2 = new String[] {
			"А ты вообще местный? Ты арбуз в глаза видел? Это провал!",
			"Подарю тебе табличку Лузер Года, только неудачник может так плохо справляться с таким простым делом!",
			"Ну покорми меня, а? Нормально только, а не вот этим сомнительным нектаром...",
			"А ты случаем не террорист? Можешь ты меня голодом моришь? Чего тебе надо?!!" };

	private static final String[] textSuccess3 = new String[] {
			"Жизнь налаживается, лей не жалей!",
			"Тебя этому мама учила? У тебя здорово получается!",
			"А зачем ты ростишь меня? Есть будешь? Я буду очень сочным арбузом!" };

	private static final String[] textFail3 = new String[] {
			"Это что, водичка из канализации? Ты понимаешь что такое нектар?!",
			"Боги, спасите меня от этого неумехи, он же все портит!" };

	private static final String[] textSuccess4 = new String[] {
			"Вот это заряд!! Ты что подмешал в нектар? Там точно градусов 40! Ахахаха! Я пьянею!",
			"Ты рискуешь вырастить не арбуз, а целую ракету! Подливай, давай еще!" };

	private static final String[] textFail4 = new String[] { "Ох как я хочу пить... Нектар, прошу...", "Лей сюда нектар и посмотри что получится!" };

	private int _npcId;
	private int _nectar;
	private int _tryCount;
	private long _lastNectarUse;
	private long _timeToUnspawn;

	private ScheduledFuture<?> _polimorphTask;

	private static int NECTAR_REUSE = 3000;

	public MeleonAI(NpcInstance actor)
	{
		super(actor);
		_npcId = getActor().getNpcId();
		Functions.npcSayCustomMessage(getActor(), textOnSpawn[Rnd.get(textOnSpawn.length)]);
		_timeToUnspawn = System.currentTimeMillis() + 120000;
	}

	@Override
	protected boolean thinkActive()
	{
		if(System.currentTimeMillis() > _timeToUnspawn)
		{
			_timeToUnspawn = Long.MAX_VALUE;
			if(_polimorphTask != null)
			{
				_polimorphTask.cancel(false);
				_polimorphTask = null;
			}
			MeleonInstance actor = getActor();
			actor.deleteMe();
		}

		return false;
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		MeleonInstance actor = getActor();
		if(actor == null || skill.getId() != 2005)
			return;

		if(actor.getNpcId() != Young_Watermelon && actor.getNpcId() != Young_Honey_Watermelon)
			return;

		switch(_tryCount)
		{
			case 0:
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess0[Rnd.get(textSuccess0.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail0[Rnd.get(textFail0.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 1:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess1[Rnd.get(textSuccess1.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail1[Rnd.get(textFail1.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 2:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess2[Rnd.get(textSuccess2.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail2[Rnd.get(textFail2.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 3:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess3[Rnd.get(textSuccess3.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail3[Rnd.get(textFail3.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 4:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess4[Rnd.get(textSuccess4.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail4[Rnd.get(textFail4.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				if(_npcId == Young_Watermelon)
				{
					if(_nectar < 3)
						_npcId = Defective_Watermelon;
					else if(_nectar == 5)
						_npcId = Large_Rain_Watermelon;
					else
						_npcId = Rain_Watermelon;
				}
				else if(_npcId == Young_Honey_Watermelon)
					if(_nectar < 3)
						_npcId = Defective_Honey_Watermelon;
					else if(_nectar == 5)
						_npcId = Large_Rain_Honey_Watermelon;
					else
						_npcId = Rain_Honey_Watermelon;

				_polimorphTask = ThreadPoolManager.getInstance().schedule(new PolimorphTask(), NECTAR_REUSE);
				break;
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		MeleonInstance actor = getActor();
		if(actor != null && Rnd.chance(5))
			Functions.npcSay(actor, textOnAttack[Rnd.get(textOnAttack.length)]);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_tryCount = -1;
		MeleonInstance actor = getActor();
		if(actor == null)
			return;

		double dropMod = 1.5;

		switch(_npcId)
		{
			case Defective_Watermelon:
				dropMod *= 1;
				Functions.npcSay(actor, "Арбуз открывается!");
				Functions.npcSay(actor, "Охо-хо! Да тут жалкие крохи, старайся лучше!");
				break;
			case Rain_Watermelon:
				dropMod *= 2;
				Functions.npcSay(actor, "Арбуз открывается!");
				Functions.npcSay(actor, "Ай-ай-ай! Неплохой улов!");
				break;
			case Large_Rain_Watermelon:
				dropMod *= 4;
				Functions.npcSay(actor, "Арбуз открывается!");
				Functions.npcSay(actor, "Вот это да! Какие сокровища!");
				break;
			case Defective_Honey_Watermelon:
				dropMod *= 12.5;
				Functions.npcSay(actor, "Арбуз открывается!");
				Functions.npcSay(actor, "Потратил много, а выудил мало!");
				break;
			case Rain_Honey_Watermelon:
				dropMod *= 25;
				Functions.npcSay(actor, "Арбуз открывается!");
				Functions.npcSay(actor, "Бум-бум-бах! Улов хорош!");
				break;
			case Large_Rain_Honey_Watermelon:
				dropMod *= 50;
				Functions.npcSay(actor, "Арбуз открывается!");
				Functions.npcSay(actor, "Фанфары! Ты открыл гигантский арбуз! Несметные богатства на земле! Лови их!");
				break;
			default:
				dropMod *= 0;
				Functions.npcSay(actor, "Я ведь ничего тебе не дам, если умру вот так...");
				Functions.npcSay(actor, "Этот позор навеки покроет твое имя...");
				break;
		}

		super.onEvtDead(actor);

		if(dropMod > 0)
		{
			if(_polimorphTask != null)
			{
				_polimorphTask.cancel(false);
				_polimorphTask = null;
				Log.add("SummerMeleons :: Player " + actor.getSpawner().getName() + " tried to use cheat (SquashAI clone): killed " + actor + " after polymorfing started", "illegal-actions");
				return; // при таких вариантах ничего не даем
			}

			for(RewardData d : _dropList)
			{
				RewardItem itd = d.roll(null, dropMod);
				if(itd != null)
					actor.dropItem(actor.getSpawner(), itd.itemId, itd.count);
			}
		}
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	public MeleonInstance getActor()
	{
		return (MeleonInstance) super.getActor();
	}
}