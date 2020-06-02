package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.Config;
import l2s.gameserver.model.DeathPenalty;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;

public class SubClass
{
	public static final int CERTIFICATION_65 = 1 << 0;
	public static final int CERTIFICATION_70 = 1 << 1;
	public static final int CERTIFICATION_75 = 1 << 2;
	public static final int CERTIFICATION_80 = 1 << 3;

	public static final int DUALCERTIFICATION_85 = 1 << 0;
	public static final int DUALCERTIFICATION_90 = 1 << 1;
	public static final int DUALCERTIFICATION_95 = 1 << 2;
	public static final int DUALCERTIFICATION_99 = 1 << 3;

	private final Player _owner;

	private int _classId = 0;
	private int _index = 1;

	private boolean _active = false;
	private boolean _isBase = true;

	private int _level = 1;
	private long _exp = 0;
	private long _sp = 0;

	private int _maxLvl = Experience.getMaxLevel();
	private long _minExp = 0;
	private long _maxExp = Experience.getExpForLevel(_maxLvl + 1) - 1;

	private DeathPenalty _deathPenalty;
	private int _certification;

	private double _hp = 1;
	private double _mp = 1;
	private double _cp = 1;

	public SubClass(Player owner)
	{
		_owner = owner;
	}

	public int getClassId()
	{
		return _classId;
	}

	public long getExp()
	{
		return _exp;
	}

	public long getMaxExp()
	{
		return _maxExp;
	}

	public void addExp(long val)
	{
		setExp(_exp + val);
	}

	public long getSp()
	{
		return _sp;
	}

	public void addSp(long val)
	{
		setSp(_sp + val);
	}

	public int getLevel()
	{
		return _level;
	}

	public void setClassId(int id)
	{
		if(_classId == id)
			return;

		_classId = id;
	}

	public void setExp(long val)
	{
		_exp = val;

		_exp = Math.min(_exp, _maxExp);
		_exp = Math.max(_minExp, _exp);
		_level = Experience.getLevel(_exp);
	}

	public void setSp(long spValue)
	{
		_sp = Math.min(Math.max(0L, spValue), Integer.MAX_VALUE);
	}

	public void setHp(double hpValue)
	{
		_hp = Math.max(0., hpValue);
	}

	public double getHp()
	{
		return _hp;
	}

	public void setMp(final double mpValue)
	{
		_mp = Math.max(0., mpValue);
	}

	public double getMp()
	{
		return _mp;
	}

	public void setCp(final double cpValue)
	{
		_cp = Math.max(0., cpValue);
	}

	public double getCp()
	{
		return _cp;
	}

	public void setActive(final boolean active)
	{
		_active = active;
	}

	public boolean isActive()
	{
		return _active;
	}

	public void setIsBase(final boolean isBase)
	{
		if(_isBase == isBase)
			return;

		_isBase = isBase;

		if(!_isBase)
		{
			_maxLvl = Experience.getMaxSubLevel();
			_minExp = Experience.getExpForLevel(Config.ALT_GAME_START_LEVEL_TO_SUBCLASS);
			_level = Math.min(Math.max(Config.ALT_GAME_START_LEVEL_TO_SUBCLASS, _level), _maxLvl);
		}
		else
		{
			_maxLvl = Experience.getMaxLevel();
			_minExp = 0;
			_level = Math.min(Math.max(1, _level), _maxLvl);
		}
		_minExp = Math.max(0, _minExp);
		_maxExp = Experience.getExpForLevel(_maxLvl + 1) - 1;
		_exp = Math.min(Math.max(Experience.getExpForLevel(_level), _exp), _maxExp);
	}

	public boolean isBase()
	{
		return _isBase;
	}

	public DeathPenalty getDeathPenalty()
	{
		return _deathPenalty;
	}

	public void setDeathPenalty(int deathPenalty)
	{
		_deathPenalty = new DeathPenalty(_owner, deathPenalty);
	}

	public int getCertification()
	{
		return _certification;
	}

	public void setCertification(int certification)
	{
		_certification = certification;
	}

	public void addCertification(int c)
	{
		_certification |= c;
	}

	public boolean isCertificationGet(int v)
	{
		return (_certification & v) == v;
	}

	@Override
	public String toString()
	{
		return ClassId.VALUES[_classId].toString() + " " + _level;
	}

	public int getMaxLevel()
	{
		return _maxLvl;
	}

	public void setIndex(int i)
	{
		_index = i;
	}

	public int getIndex()
	{
		return _index;
	}
}