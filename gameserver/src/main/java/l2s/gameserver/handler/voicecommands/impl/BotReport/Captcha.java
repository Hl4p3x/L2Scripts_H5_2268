package l2s.gameserver.handler.voicecommands.impl.BotReport;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.formats.dds.DDSConverter;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.IStaticPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.PledgeCrestPacket;

public class Captcha
{
	private static final Logger LOG = LoggerFactory.getLogger(Captcha.class);
	private static final char[] CAPTCHA_TEXT_POSSIBILITIES = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'K', 'L', 'M', 'P', 'R', 'S', 'T', 'U', 'W', 'X', 'Y', 'Z' };
	private static final int CAPTCHA_WORD_LENGTH = 5;
	private static final int CAPTCHA_MIN_ID = 1900000000;
	private static final int CAPTCHA_MAX_ID = 2000000000;

	public static String sendCaptcha(Player target)
	{
		int captchaId = generateRandomCaptchaId();
		char[] captchaText = generateCaptchaText();
    
		BufferedImage image = generateCaptcha(captchaText);
		ByteBuffer buffer = DDSConverter.convertToDxt1NoTransparency(image);
		byte[] array = buffer.array();
		IStaticPacket packet = new PledgeCrestPacket(captchaId, array);
		target.sendPacket(packet);
    
		sendCaptchaWindow(target, captchaId);
    
		return String.valueOf(captchaText);
	}
	
	private static void sendCaptchaWindow(Player target, int captchaId)
	{
		String text = HtmCache.getInstance().getHtml("captcha.htm", target);
		text = text.replace("%captchaId%", String.valueOf(captchaId));
		text = text.replace("%time%", String.valueOf(Config.CAPTCHA_ANSWER_SECONDS));
		NpcHtmlMessagePacket msg = new NpcHtmlMessagePacket(0);
		msg.setHtml(text);
		target.sendPacket(msg);
	}

	private static char[] generateCaptchaText()
	{
		char[] text = new char[5];
		for(int i = 0; i < 5; i++)
			text[i] = CAPTCHA_TEXT_POSSIBILITIES[Rnd.get(CAPTCHA_TEXT_POSSIBILITIES.length)];
		return text;
	}
  
	private static int generateRandomCaptchaId()
	{
		return Rnd.get(1900000000, 2000000000);
	}
  
	private static BufferedImage generateCaptcha(char[] text)
	{
		Color textColor = new Color(38, 213, 30);
		Color circleColor = new Color(73, 100, 151);
		Font textFont = new Font("comic sans ms", 1, 24);
		int charsToPrint = 5;
		int width = 256;
		int height = 64;
		int circlesToDraw = 8;
		float horizMargin = 20.0F;
		double rotationRange = 0.7D;
		BufferedImage bufferedImage = new BufferedImage(width, height, 1);  
		Graphics2D g = (Graphics2D)bufferedImage.getGraphics();
		g.setColor(new Color(30, 31, 31));
		g.fillRect(0, 0, width, height); 
		g.setColor(circleColor);
		for (int i = 0; i < circlesToDraw; i++)
		{
			int circleRadius = (int)(Math.random() * height / 2.0D);
			int circleX = (int)(Math.random() * width - circleRadius);
			int circleY = (int)(Math.random() * height - circleRadius);
			g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
		}
    
		g.setColor(textColor);
		g.setFont(textFont);
    
		FontMetrics fontMetrics = g.getFontMetrics();
		int maxAdvance = fontMetrics.getMaxAdvance();
		int fontHeight = fontMetrics.getHeight();
    
		float spaceForLetters = -horizMargin * 2.0F + width;
		float spacePerChar = spaceForLetters / (charsToPrint - 1.0F);
    
		for (int i = 0; i < charsToPrint; i++)
		{
			char characterToShow = text[i];
			int charWidth = fontMetrics.charWidth(characterToShow);
			int charDim = Math.max(maxAdvance, fontHeight);
			int halfCharDim = charDim / 2;
      
			BufferedImage charImage = new BufferedImage(charDim, charDim, 2);
			Graphics2D charGraphics = charImage.createGraphics();
			charGraphics.translate(halfCharDim, halfCharDim);
			double angle = (Math.random() - 0.5D) * rotationRange;
			charGraphics.transform(AffineTransform.getRotateInstance(angle));
			charGraphics.translate(-halfCharDim, -halfCharDim);
			charGraphics.setColor(textColor);
			charGraphics.setFont(textFont);
			int charX = (int)(0.5D * charDim - 0.5D * charWidth);
			charGraphics.drawString(String.valueOf(characterToShow), charX, (charDim - fontMetrics.getAscent()) / 2 + fontMetrics.getAscent());
			float x = horizMargin + spacePerChar * i - charDim / 2.0F;
			int y = (height - charDim) / 2;
			g.drawImage(charImage, (int)x, y, charDim, charDim, null, null);
			charGraphics.dispose();
		}
		g.dispose();
		return bufferedImage;
	}
}
