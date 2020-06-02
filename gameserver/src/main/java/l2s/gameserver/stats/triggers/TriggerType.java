package l2s.gameserver.stats.triggers;

/**
* @author VISTALL
* @date 15:05/22.01.2011
*/
public enum TriggerType
{
	ADD, // скилл срабатывает при добавлении в лист
	ATTACK,
	RECEIVE_DAMAGE,
	CRIT,
	OFFENSIVE_SKILL_USE,
	OFFENSIVE_PHYSICAL_SKILL_USE,
	OFFENSIVE_MAGICAL_SKILL_USE,
	SUPPORT_SKILL_USE,
	SUPPORT_PHYSICAL_SKILL_USE,
	SUPPORT_MAGICAL_SKILL_USE,
	UNDER_MISSED_ATTACK,
	DIE,
	ON_START_EFFECT, // Срабатывает при старте эффекта.
	ON_EXIT_EFFECT, // Срабатывает по завершению эффекта (любым способом: время вышло, принудительно и т.д.).
	ON_FINISH_EFFECT, // Срабатывает по завершению времени действия эффекта.
}
