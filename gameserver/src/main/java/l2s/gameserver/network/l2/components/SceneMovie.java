package l2s.gameserver.network.l2.components;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExStartScenePlayer;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author VISTALL
 * @date  12:51/29.12.2010
 */
public enum SceneMovie implements IStaticPacket
{
	LINDVIOR_SPAWN(1, 45500),

	// Echmus
	ECHMUS_OPENING(2, 62000),
	ECHMUS_SUCCESS(3, 18000),
	ECHMUS_FAIL(4, 17000),

	// Tiat
	TIAT_OPENING(5, 54200),
	TIAT_SUCCESS(6, 26100),
	TIAT_FAIL(7, 24800),

	// Seven Signs Quests
	SSQ_SERIES_OF_DOUBT(8, 26000),
	SSQ_DYING_MESSAGE(9, 27000),
	SSQ_MAMMONS_CONTRACT(10, 98000), //good
	SSQ_SECRET_RITUAL_PRIEST(11, 30000),
	SSQ_SEAL_EMPEROR_1(12, 18000),
	SSQ_SEAL_EMPEROR_2(13, 26000),
	SSQ_EMBRYO(14, 28000),

	// Freya
	FREYA_OPENING(15, 53500),
	FREYA_PHASE_CHANGE_A(16, 21100),
	FREYA_PHASE_CHANGE_B(17, 21500),
	KEGOR_INTRUSION(18, 27000),
	FREYA_ENDING_A(19, 16000),
	FREYA_ENDING_B(20, 56000),
	FREYA_FORCED_DEFEAT(21, 21000),
	FREYA_DEFEAT(22, 20500),
	ICE_HEAVY_KNIGHT_SPAWN(23, 7000),

	// High Five Seven Signs Quests
	SSQ2_HOLY_BURIAL_GROUND_OPENING(24, 23000),
	SSQ2_HOLY_BURIAL_GROUND_CLOSING(25, 22000),
	SSQ2_SOLINA_TOMB_OPENING(26, 25000),
	SSQ2_SOLINA_TOMB_CLOSING(27, 15000),
	SSQ2_ELYSS_NARRATION(28, 59000),
	SSQ2_BOSS_OPENING(29, 60000),
	SSQ2_BOSS_CLOSING(30, 60000),

	// Airship
	LANDING_KSERTH_LEFT(1000, 10000),
	LANDING_KSERTH_RIGHT(1001, 10000),
	LANDING_INFINITY(1002, 10000),
	LANDING_DESTRUCTION(1003, 10000),
	LANDING_ANNIHILATION(1004, 15000);

	private final int _id;
	private final int _duration;
	private final L2GameServerPacket _static;

	SceneMovie(int id, int duration)
	{
		_id = id;
		_duration = duration;
		_static = new ExStartScenePlayer(this);
	}

	public int getId()
	{
		return _id;
	}

	public int getDuration()
	{
		return _duration;
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		return _static;
	}
}
