package l2s.gameserver.utils;

import java.util.Collection;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassType2;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.Functions;


/**
 * @author VISTALL
 * @date  17:49/08.12.2010
 */
public class CertificationFunctions
{
	public static final String PATH = "villagemaster/certification/";

	public static void showCertificationList(NpcInstance npc, Player player)
	{
		if (!checkConditions(65, npc, player, true))
		{
			return;
		}

		Functions.show(PATH + "certificatelist.htm", player, npc);
	}

	public static void getCertification65(NpcInstance npc, Player player)
	{
		if (!checkConditions(65, npc, player, Config.ALT_GAME_SUB_BOOK))
		{
			return;
		}

		SubClass clzz = player.getActiveSubClass();
		if (clzz.isCertificationGet(SubClass.CERTIFICATION_65))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		ItemFunctions.addItem(player, 10280, 1, "Get 65 certification");
		clzz.addCertification(SubClass.CERTIFICATION_65);
		player.store(true);
	}

	public static void getCertification70(NpcInstance npc, Player player)
	{
		if (!checkConditions(70, npc, player, Config.ALT_GAME_SUB_BOOK))
		{
			return;
		}

		SubClass clzz = player.getActiveSubClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		ItemFunctions.addItem(player, 10280, 1, "Get 70 certification");
		clzz.addCertification(SubClass.CERTIFICATION_70);
		player.store(true);
	}

	public static void getCertification75List(NpcInstance npc, Player player)
	{
		if (!checkConditions(75, npc, player, Config.ALT_GAME_SUB_BOOK))
		{
			return;
		}

		SubClass clzz = player.getActiveSubClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		Functions.show(PATH + "certificate-choose.htm", player, npc);
	}

	public static void getCertification75(NpcInstance npc, Player player, boolean classCertifi)
	{
		if (!checkConditions(75, npc, player, Config.ALT_GAME_SUB_BOOK))
		{
			return;
		}

		SubClass clzz = player.getActiveSubClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		if (classCertifi)
		{
			ClassId cl = ClassId.VALUES[clzz.getClassId()];
			if(cl.getType2() == null)
				return;


			ItemFunctions.addItem(player, cl.getType2().getCertificateId(), 1, "Get 75 certification");
		}
		else
		{
			ItemFunctions.addItem(player, 10612, 1, "Get 75 Master Ability"); // master ability
		}

		clzz.addCertification(SubClass.CERTIFICATION_75);
		player.store(true);
	}

	public static void getCertification80(NpcInstance npc, Player player)
	{
		if (!checkConditions(80, npc, player, Config.ALT_GAME_SUB_BOOK))
		{
			return;
		}

		SubClass clzz = player.getActiveSubClass();

		// если не взят(ы) преведущий сертификат(ы)
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70) || !clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_80))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		ClassId cl = ClassId.VALUES[clzz.getClassId()];
		if(cl.getType2() == null)
			return;

		ItemFunctions.addItem(player, cl.getType2().getTransformationId(), 1, "Get 80 certification");
		clzz.addCertification(SubClass.CERTIFICATION_80);
		player.store(true);
	}

	public static void cancelCertification(NpcInstance npc, Player player)
	{
		if(player.getInventory().getAdena() < 10000000)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(!player.isBaseClassActive())
			return;

		player.getInventory().reduceAdena(10000000);

		for (ClassType2 classType2 : ClassType2.VALUES)
		{
			player.getInventory().destroyItemByItemId(classType2.getCertificateId(), player.getInventory().getCountOf(classType2.getCertificateId()));
			player.getInventory().destroyItemByItemId(classType2.getTransformationId(), player.getInventory().getCountOf(classType2.getTransformationId()));
		}

		Collection<SkillLearn> skillLearnList = SkillAcquireHolder.getInstance().getAvailableSkills(null, AcquireType.CERTIFICATION);
		for(SkillLearn learn : skillLearnList)
		{
			Skill skill = player.getKnownSkill(learn.getId());
			if(skill != null)
				player.removeSkill(skill, true);
		}

		for(SubClass subClass : player.getSubClassList())
		{
			if(!subClass.isBase())
				subClass.setCertification(0);
		}

		player.sendSkillList();
		Functions.show(new CustomMessage("scripts.services.SubclassSkills.SkillsDeleted", player), player);
	}

	public static boolean checkConditions(int level, NpcInstance npc, Player player, boolean first)
	{
		if (player.getLevel() < level)
		{
			Functions.show(PATH + "certificate-nolevel.htm", player, npc, "%level%", level);
			return false;
		}

		if (player.isBaseClassActive())
		{
			Functions.show(PATH + "certificate-nosub.htm", player, npc);
			return false;
		}

		if (first)
		{
			return true;
		}

		if(!Config.CAN_RECEIVE_CERTIFICATION_WITHOUT_LEARN_PREVIOUS_SKILL) {
			for (ClassType2 type : ClassType2.VALUES) {
				if (player.getInventory().getCountOf(type.getCertificateId()) > 0 || player.getInventory().getCountOf(type.getTransformationId()) > 0) {
					Functions.show(PATH + "certificate-already.htm", player, npc);
					return false;
				}
			}
		}
		return true;
	}
}
