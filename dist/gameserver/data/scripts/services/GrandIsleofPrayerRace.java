package services;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;

/**
 * Сервис для гонок на Isle of Prayer, см. также ai.PrisonGuard
 * @author SYS
 */
public class GrandIsleofPrayerRace extends Functions
{
	private static final int RACE_STAMP = 10013;
	private static final int SECRET_KEY = 9694;

	public void startRace()
	{
		Skill skill = SkillHolder.getInstance().getSkill(Skill.SKILL_EVENT_TIMER, 1);
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(skill == null || player == null || npc == null)
			return;

		getNpc().altUseSkill(skill, player);
		ItemFunctions.deleteItem(player, RACE_STAMP, ItemFunctions.getItemCount(player, RACE_STAMP));
		show("default/32349-2.htm", player, npc);
	}

	public String DialogAppend_32349(Integer val)
	{
		Player player = getSelf();
		if(player == null)
			return "";

		// Нет бафа с таймером
		if(player.getEffectList().getEffectsBySkillId(Skill.SKILL_EVENT_TIMER) == null)
			return "<br>[scripts_services.GrandIsleofPrayerRace:startRace|Start the Race.]";

		// Есть бафф с таймером
		long raceStampsCount = ItemFunctions.getItemCount(player, RACE_STAMP);
		if(raceStampsCount < 4)
			return "<br>*Race in progress, hurry!*";
		ItemFunctions.deleteItem(player, RACE_STAMP, raceStampsCount);
		ItemFunctions.addItem(player, SECRET_KEY, 3, "Give Secret Key on Isle of Prayer");
		player.getEffectList().stopEffect(Skill.SKILL_EVENT_TIMER);
		return "<br>Good job, here is your keys.";
	}
}