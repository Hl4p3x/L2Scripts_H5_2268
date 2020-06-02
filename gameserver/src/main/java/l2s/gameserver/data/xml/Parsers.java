package l2s.gameserver.data.xml;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.data.string.SkillDescHolder;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.ProductHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.data.xml.parser.AirshipDockParser;
import l2s.gameserver.data.xml.parser.ArmorSetsParser;
import l2s.gameserver.data.xml.parser.ClassDataParser;
import l2s.gameserver.data.xml.parser.CubicParser;
import l2s.gameserver.data.xml.parser.DomainParser;
import l2s.gameserver.data.xml.parser.DoorParser;
import l2s.gameserver.data.xml.parser.EnchantItemParser;
import l2s.gameserver.data.xml.parser.EnchantStoneParser;
import l2s.gameserver.data.xml.parser.EventParser;
import l2s.gameserver.data.xml.parser.FakeItemParser;
import l2s.gameserver.data.xml.parser.FakePlayersParser;
import l2s.gameserver.data.xml.parser.FightClubMapParser;
import l2s.gameserver.data.xml.parser.FishDataParser;
import l2s.gameserver.data.xml.parser.HennaParser;
import l2s.gameserver.data.xml.parser.InstantZoneParser;
import l2s.gameserver.data.xml.parser.ItemParser;
import l2s.gameserver.data.xml.parser.LevelBonusParser;
import l2s.gameserver.data.xml.parser.NpcParser;
import l2s.gameserver.data.xml.parser.OptionDataParser;
import l2s.gameserver.data.xml.parser.PetitionGroupParser;
import l2s.gameserver.data.xml.parser.PlayerTemplateParser;
import l2s.gameserver.data.xml.parser.PremiumAccountParser;
import l2s.gameserver.data.xml.parser.RecipeParser;
import l2s.gameserver.data.xml.parser.ResidenceParser;
import l2s.gameserver.data.xml.parser.RestartPointParser;
import l2s.gameserver.data.xml.parser.SkillAcquireParser;
import l2s.gameserver.data.xml.parser.SoulCrystalParser;
import l2s.gameserver.data.xml.parser.SpawnParser;
import l2s.gameserver.data.xml.parser.StaticObjectParser;
import l2s.gameserver.data.xml.parser.VariationDataParser;
import l2s.gameserver.data.xml.parser.ZoneParser;
import l2s.gameserver.instancemanager.ReflectionManager;

/**
 * @author VISTALL
 * @date  20:55/30.11.2010
 */
public abstract class Parsers
{
	public static void parseAll()
	{
		HtmCache.getInstance().reload();
		StringsHolder.getInstance().load();
		ItemNameHolder.getInstance().load();
		SkillNameHolder.getInstance().load();
		SkillDescHolder.getInstance().load();
		//
		SkillHolder.getInstance().load(); // - SkillParser.getInstance();
		OptionDataParser.getInstance().load();
		VariationDataParser.getInstance().load();
		ItemParser.getInstance().load();
		RecipeParser.getInstance().load();
		//
		LevelBonusParser.getInstance().load();
		PlayerTemplateParser.getInstance().load();
		ClassDataParser.getInstance().load();
		NpcParser.getInstance().load();

		DomainParser.getInstance().load();
		RestartPointParser.getInstance().load();

		StaticObjectParser.getInstance().load();
		DoorParser.getInstance().load();
		ZoneParser.getInstance().load();
		SpawnParser.getInstance().load();
		InstantZoneParser.getInstance().load();

		ReflectionManager.getInstance();
		//
		AirshipDockParser.getInstance().load();
		SkillAcquireParser.getInstance().load();
		//
		ResidenceParser.getInstance().load();
		EventParser.getInstance().load();
		FightClubMapParser.getInstance().load();
		// support(cubic & agathion)
		CubicParser.getInstance().load();
		//
		BuyListHolder.getInstance();
		MultiSellHolder.getInstance();
		ProductHolder.getInstance();
		// AgathionParser.getInstance();
		// item support
		HennaParser.getInstance().load();
		EnchantItemParser.getInstance().load();
		EnchantStoneParser.getInstance().load();
		SoulCrystalParser.getInstance().load();
		ArmorSetsParser.getInstance().load();
		FishDataParser.getInstance().load();

		PremiumAccountParser.getInstance().load();
		
		// etc
		PetitionGroupParser.getInstance().load();

		// Fake players
		FakeItemParser.getInstance().load();
		FakePlayersParser.getInstance().load();
	}
}
