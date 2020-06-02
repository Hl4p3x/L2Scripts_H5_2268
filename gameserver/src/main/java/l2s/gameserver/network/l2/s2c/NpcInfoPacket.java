package l2s.gameserver.network.l2.s2c;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Location;

/**
 * @reworked VISTALL
 */
public class NpcInfoPacket extends AbstractMaskPacket<NpcInfoType>
{
	public static class SummonInfoPacket extends NpcInfoPacket
	{
		public SummonInfoPacket(SummonInstance summon, Creature attacker)
		{
			super(summon, attacker);
		}
	}

	public static class PetInfoPacket extends NpcInfoPacket
	{
		public PetInfoPacket(PetInstance summon, Creature attacker)
		{
			super(summon, attacker);
		}
	}

	// Flags
	private static final int IS_IN_COMBAT = 1 << 0;
	private static final int IS_ALIKE_DEAD = 1 << 1;
	private static final int IS_TARGETABLE = 1 << 2;
	private static final int IS_SHOW_NAME = 1 << 3;

	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x0C,
		(byte) 0x0C,
		(byte) 0x00,
		(byte) 0x00
	};

	private boolean _canWrite = false;
	private final Creature _creature;
	private int _npcObjId, _npcId, incombat, _showSpawnAnimation;
	private int _runSpd, _walkSpd, _mAtkSpd, _pAtkSpd, _rHand, _lHand, _enchantEffect;
	private int _karma, _pvpFlag, _abnormalEffect, _abnormalEffect2, _clanId, _clanCrestId, _allyId, _allyCrestId, _formId, _titleColor;
	private double colHeight, colRadius, currentColHeight, currentColRadius;
	private boolean _alive, _isAttackable, _isNameAbove, _running, _flying, _inWater;
	private Location _loc;
	private String _name = StringUtils.EMPTY;
	private String _title = StringUtils.EMPTY;
	private boolean _showName;
	private int _state;
	private NpcString _nameNpcString = NpcString.NONE;
	private NpcString _titleNpcString = NpcString.NONE;
	private TeamType _team;
	private int _currentHP, _currentMP;
	private int _maxHP, _maxMP;
	private int _transformId;
	private AbnormalEffect[] _abnormalEffects;
	private double _atkSpdMul, _runSpdMul;

	private int _initSize = 0;
	private int _blockSize = 0;

	private int _statusMask = 0;

	public NpcInfoPacket(NpcInstance npc, Creature attacker)
	{
		_creature = npc;
		if(Config.SERVER_SIDE_NPC_NAME || npc.getTemplate().displayId != 0 || !npc.getName().equals(npc.getTemplate().name))
			_name = npc.getName();
		if(Config.SERVER_SIDE_NPC_TITLE || npc.getTemplate().displayId != 0 || !npc.getTitle().equals(npc.getTemplate().title))
			_title = npc.getTitle();
		_npcId = npc.getDisplayId() != 0 ? npc.getDisplayId() : npc.getTemplate().npcId;
		_isAttackable = attacker != null && npc.isAutoAttackable(attacker);
		_rHand = npc.getRightHandItem();
		_lHand = npc.getLeftHandItem();
		_showSpawnAnimation = npc.getSpawnAnimation();
		_state = npc.getNpcState();

		NpcString nameNpcString = npc.getNameNpcString();
		_nameNpcString = nameNpcString != null ? nameNpcString : NpcString.NONE;

		NpcString titleNpcString = npc.getTitleNpcString();
		_titleNpcString = titleNpcString != null ? titleNpcString : NpcString.NONE;

		_showName = npc.isShowName();

		//if(npc.isTargetable(attacker))
			_statusMask |= IS_TARGETABLE;

		if(_showName)
			_statusMask |= IS_SHOW_NAME;

		common(npc);
	}

	public NpcInfoPacket(Servitor servitor, Creature attacker)
	{
		_creature = servitor;

		if(servitor.getPlayer() != null && servitor.getPlayer().isInvisible(attacker))
			return;

		_npcId = servitor.getTemplate().npcId;
		_isAttackable = servitor.isAutoAttackable(attacker);
		_rHand = 0;
		_lHand = 0;
		_showName = true;
		if(!servitor.getName().equals(servitor.getTemplate().name))
			_name = servitor.getName();
		else
			_name = StringUtils.EMPTY;
		_title = servitor.getTitle();
		_showSpawnAnimation = servitor.getSpawnAnimation();

		//if(servitor.isTargetable(attacker))
			_statusMask |= IS_TARGETABLE;

		if(_showName)
			_statusMask |= IS_SHOW_NAME;

		common(servitor);
	}

	private void common(Creature cha)
	{
		colHeight = cha.getCollisionHeight();
		colRadius = cha.getCollisionRadius();
		currentColHeight = cha.getCurrentCollisionHeight();
		currentColRadius = cha.getCurrentCollisionRadius();
		_npcObjId = cha.getObjectId();
		_loc = cha.getLoc();
		_pAtkSpd = cha.getPAtkSpd();
		_mAtkSpd = cha.getMAtkSpd();
		_atkSpdMul = cha.getAttackSpeedMultiplier();
		_runSpdMul = cha.getMovementSpeedMultiplier();
		//
		Clan clan = cha.getClan();
		Alliance alliance = clan == null ? null : clan.getAlliance();
		//
		_clanId = clan == null ? 0 : clan.getClanId();
		_clanCrestId = clan == null ? 0 : clan.getCrestId();
		//
		_allyId = alliance == null ? 0 : alliance.getAllyId();
		_allyCrestId = alliance == null ? 0 : alliance.getAllyCrestId();

		_runSpd = cha.getRunSpeed();
		_walkSpd = cha.getWalkSpeed();
		_karma = cha.getKarma();
		_pvpFlag = cha.getPvpFlag();
		_running = cha.isRunning();
		incombat = cha.isInCombat() ? 1 : 0;
		_alive = !cha.isAlikeDead();
		_abnormalEffect = cha.getAbnormalEffectMask();
		_abnormalEffect2 = cha.getAbnormalEffectMask2();
		_flying = cha.isFlying();
		_inWater = cha.isInWater();
		_team = cha.getTeam();
		_formId = cha.getFormId();
		_isNameAbove = cha.isNameAbove();
		_titleColor = cha.isServitor() ? 1 : 0;
		_currentHP = (int) cha.getCurrentHp();
		_currentMP = (int) cha.getCurrentMp();
		_maxHP = cha.getMaxHp();
		_maxMP = cha.getMaxMp();
		_transformId = 0;
		_abnormalEffects = cha.getAbnormalEffectsArray();
		_enchantEffect = 0;

		_canWrite = true;
	}

	@Override
	protected boolean canWrite()
	{
		return _canWrite;
	}

	public NpcInfoPacket init()
	{
		addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);

		if(_name != StringUtils.EMPTY)
		{
			//_masks[2] |= 0x10;
			addComponentType(NpcInfoType.NAME);
		}

		if(_title != StringUtils.EMPTY)
			addComponentType(NpcInfoType.TITLE);

		if(_loc.h > 0)
			addComponentType(NpcInfoType.HEADING);

		if(_pAtkSpd > 0 || _mAtkSpd > 0)
			addComponentType(NpcInfoType.ATK_CAST_SPEED);

		if(_running && _creature.getRunSpeed() > 0 || !_running && _creature.getWalkSpeed() > 0)
			addComponentType(NpcInfoType.SPEED_MULTIPLIER);

		if(_rHand > 0 || _lHand > 0)
			addComponentType(NpcInfoType.EQUIPPED);

		if(_team != TeamType.NONE)
			addComponentType(NpcInfoType.TEAM);

		if(_state > 0)
			addComponentType(NpcInfoType.DISPLAY_EFFECT);

		if(_inWater || _flying)
			addComponentType(NpcInfoType.SWIM_OR_FLY);

		if(_flying)
			addComponentType(NpcInfoType.FLYING);

		if(_maxHP > 0)
			addComponentType(NpcInfoType.MAX_HP);

		if(_maxMP > 0)
			addComponentType(NpcInfoType.MAX_MP);

		if(_currentHP <= _maxHP)
			addComponentType(NpcInfoType.CURRENT_HP);

		if(_currentMP <= _maxMP)
			addComponentType(NpcInfoType.CURRENT_MP);

		if(_abnormalEffects.length > 0)
			addComponentType(NpcInfoType.ABNORMALS);

		if(_enchantEffect > 0)
			addComponentType(NpcInfoType.ENCHANT);

		if(_transformId > 0)
			addComponentType(NpcInfoType.TRANSFORMATION);

		if(_clanId > 0)
			addComponentType(NpcInfoType.CLAN);

		addComponentType(NpcInfoType.UNKNOWN8);

		if(_creature.getPvpFlag() > 0)
			addComponentType(NpcInfoType.PVP_FLAG);
		
		if(_creature.getKarma() != 0)
			addComponentType(NpcInfoType.REPUTATION);

		if(_creature.isInCombat())
			_statusMask |= IS_IN_COMBAT;

		if(_creature.isAlikeDead())
			_statusMask |= IS_ALIKE_DEAD;

		if(_statusMask != 0)
			addComponentType(NpcInfoType.VISUAL_STATE);

		if(_nameNpcString != NpcString.NONE)
			addComponentType(NpcInfoType.NAME_NPCSTRINGID);

		if(_titleNpcString != NpcString.NONE)
			addComponentType(NpcInfoType.TITLE_NPCSTRINGID);

		return this;
	}

	public NpcInfoPacket update(IUpdateTypeComponent... components)
	{
		_showSpawnAnimation = 1;

		//TODO: Проверить какие на оффе посылаются при обновлении в обазательном порядке.
		addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);

		for(IUpdateTypeComponent component : components)
		{
			if(component instanceof NpcInfoType)
				addComponentType((NpcInfoType) component);
		}
		return this;
	}

	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}

	@Override
	protected void onNewMaskAdded(NpcInfoType component)
	{
		switch(component)
		{
			case ATTACKABLE:
			case UNKNOWN1:
			{
				_initSize += component.getBlockLength();
				break;
			}
			case TITLE:
			{
				_initSize += component.getBlockLength() + (_title.length() * 2);
				break;
			}
			case NAME:
			{
				_blockSize += component.getBlockLength() + (_name.length() * 2);
				break;
			}
			default:
			{
				_blockSize += component.getBlockLength();
				break;
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_npcObjId);
		writeC(_showSpawnAnimation); // // 0=teleported 1=default 2=summoned
		writeH(37); // mask_bits_37
		writeB(_masks);
		
		// Block 1
		writeC(_initSize);
		
		if(containsMask(NpcInfoType.ATTACKABLE))
			writeC(_isAttackable);

		if(containsMask(NpcInfoType.UNKNOWN1))
			writeD(0x00); // unknown

		if(containsMask(NpcInfoType.TITLE))
			writeS(_title);
		
		// Block 2
		writeH(_blockSize);

		if(containsMask(NpcInfoType.ID))
			writeD(_npcId + 1000000);

		if(containsMask(NpcInfoType.POSITION))
		{
			writeD(_loc.x);
			writeD(_loc.y);
			writeD(_loc.z);
		}

		if(containsMask(NpcInfoType.HEADING))
			writeD(_loc.h);

		if(containsMask(NpcInfoType.UNKNOWN2))
			writeD(0x00); // Unknown

		if(containsMask(NpcInfoType.ATK_CAST_SPEED))
		{
			writeD(_pAtkSpd);
			writeD(_mAtkSpd);
		}

		if(containsMask(NpcInfoType.SPEED_MULTIPLIER))
		{
			writeCutF(_runSpdMul);
			writeCutF(_atkSpdMul);
		}

		if(containsMask(NpcInfoType.EQUIPPED))
		{
			writeD(_rHand);
			writeD(0x00); // Armor id?
			writeD(_lHand);
		}

		if(containsMask(NpcInfoType.ALIVE))
			writeC(_alive);

		if(containsMask(NpcInfoType.RUNNING))
			writeC(_running);

		if(containsMask(NpcInfoType.SWIM_OR_FLY))
			writeC(_inWater ? 1 : _flying ? 2 : 0);

		if(containsMask(NpcInfoType.TEAM))
			writeC(_team.ordinal());

		if(containsMask(NpcInfoType.ENCHANT))
			writeD(_enchantEffect);

		if(containsMask(NpcInfoType.FLYING))
			writeD(_flying);

		if(containsMask(NpcInfoType.CLONE))
			writeD(0x00); // Player ObjectId with Decoy

		if(containsMask(NpcInfoType.UNKNOWN8))
		{
			// No visual effect
			writeD(0x00); // Unknown
		}

		if(containsMask(NpcInfoType.DISPLAY_EFFECT))
			writeD(_state);

		if(containsMask(NpcInfoType.TRANSFORMATION))
			writeD(_transformId);

		if(containsMask(NpcInfoType.CURRENT_HP))
			writeD(_currentHP);

		if(containsMask(NpcInfoType.CURRENT_MP))
			writeD(_currentMP);

		if(containsMask(NpcInfoType.MAX_HP))
			writeD(_maxHP);

		if(containsMask(NpcInfoType.MAX_MP))
			writeD(_maxMP);

		if(containsMask(NpcInfoType.SUMMONED))
			writeC(0x00);	// тип клона 1 == приманка, 2 = клон у ножа

		if(containsMask(NpcInfoType.UNKNOWN12))
		{
			writeD(0x00);
			writeD(0x00);
		}

		if(containsMask(NpcInfoType.NAME))
			writeS(_name);

		if(containsMask(NpcInfoType.NAME_NPCSTRINGID))
			writeD(_nameNpcString.getId()); // NPCStringId for name

		if(containsMask(NpcInfoType.TITLE_NPCSTRINGID))
			writeD(_titleNpcString.getId()); // NPCStringId for title

		if(containsMask(NpcInfoType.PVP_FLAG))
			writeC(_pvpFlag); // PVP flag

		if(containsMask(NpcInfoType.REPUTATION))
			writeD(_karma); // Karma

		if(containsMask(NpcInfoType.CLAN))
		{
			writeD(_clanId);
			writeD(_clanCrestId);
			writeD(0x00); // Large Crest ID
			writeD(_allyId);
			writeD(_allyCrestId);
		}
		
		if(containsMask(NpcInfoType.VISUAL_STATE))
			writeC(_statusMask);
		
		if(containsMask(NpcInfoType.ABNORMALS))
		{
			writeH(_abnormalEffects.length);
			for(AbnormalEffect abnormal : _abnormalEffects)
				writeH(abnormal.getId());
		}
	}

	@Override
	protected final void writeImplHF()
	{
		//ddddddddddddddddddffffdddcccccSSddddddddccffddddccd
		writeD(_npcObjId);
		writeD(_npcId + 1000000); // npctype id c4
		writeD(_isAttackable ? 1 : 0);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd /*_swimRunSpd*//*0x32*/); // swimspeed
		writeD(_walkSpd/*_swimWalkSpd*//*0x32*/); // swimspeed
		writeD(_runSpd/*_flRunSpd*/);
		writeD(_walkSpd/*_flWalkSpd*/);
		writeD(_runSpd/*_flyRunSpd*/);
		writeD(_walkSpd/*_flyWalkSpd*/);
		writeF(_runSpdMul); // взято из клиента
		writeF(_atkSpdMul);
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rHand); // right hand weapon
		writeD(0); //TODO chest
		writeD(_lHand); // left hand weapon
		writeC(_isNameAbove ? 1 : 0); // 2.2: name above char 1=true ... ??; 2.3: 1 - normal, 2 - dead
		writeC(_running);
		writeC(incombat);
		writeC(!_alive);
		writeC(_showSpawnAnimation); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeD(_nameNpcString.getId());
		writeS(_name);
		writeD(_titleNpcString.getId());
		writeS(_title);
		writeD(_titleColor); // 0- светло зеленый титул(моб), 1 - светло синий(пет)/отображение текущего МП
		writeD(_pvpFlag);
		writeD(_karma); // hmm karma ??
		writeD(_abnormalEffect); // C2
		writeD(_clanId);
		writeD(_clanCrestId);
		writeD(_allyId);
		writeD(_allyCrestId);
		writeC(_flying ? 2 : 0); // C2
		writeC(_team.ordinal()); // team aura 1-blue, 2-red
		writeF(currentColRadius); // тут что-то связанное с colRadius
		writeF(currentColHeight); // тут что-то связанное с colHeight
		writeD(_enchantEffect); // C4
		writeD(0x00); // writeD(_npc.isFlying() ? 1 : 0); // C6
		writeD(0x00);
		writeD(_formId);// great wolf type
		writeC(_showName ? 0x01 : 0x00); // show name
		writeC(_showName ? 0x01 : 0x00); // show title
		writeD(_abnormalEffect2);
		writeD(_state);
	}
}