package l2s.gameserver.mods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l2s.commons.util.Rnd;

public class ChatReplacer
{
	//letters massive
	public static Set<String> letters = new HashSet<String>();
	public static List<String> replace = new ArrayList<String>();
	//en literal
	public String a = "a";
	public String b = "b";
	public String c = "c";
	public String d = "d";
	public String e = "e";
	public String f = "f";
	public String g = "g";
	public String h = "h";
	public String i = "i";
	public String j = "j";
	public String k = "k";
	public String l = "l";
	public String m = "m";
	public String n = "n";
	public String o = "o";
	public String p = "p";
	public String q = "q";
	public String r = "r";
	public String s = "s";
	public String t = "t";
	public String u = "u";
	public String v = "v";
	public String w = "w";
	public String x = "x";
	public String y = "y";
	public String z = "z";
	
	//ru literal
	public String ii = "й";
	public String cc = "ц";
	public String yy = "у";
	public String kk = "к";
	public String ee = "е";
	public String yo = "ё";
	public String nn = "н";
	public String gg = "г";
	public String sh = "ш";
	public String ww = "щ";
	public String zz = "з";
	public String xx = "х";
	public String ik = "ъ";
	public String je = "э";
	public String zh = "ж";
	public String dd = "д";
	public String ll = "л";
	public String oo = "о";
	public String rr = "р";
	public String pp = "п";
	public String as = "а";
	public String vv = "в";
	public String yyu = "ы";
	public String ff = "ф";
	public String ja = "я";
	public String ch = "ч";
	public String ca = "с";
	public String mm = "м";
	public String ai = "и";
	public String tt = "т";
	public String aik = "ь";
	public String bb = "б";
	public String yu = "ю";
	
	//greek (for encryption)
	public String qa = "ς";
	public String qb = "ε";
	public String qc = "ρ";
	public String qd = "τ";
	public String qe = "υ";
	public String qf = "θ";
	public String qg = "ι";
	public String qh = "ο";
	public String qi = "π";
	public String qj = "λ";
	public String qk = "κ";
	public String ql = "ξ";
	public String qm = "η";
	public String qn = "γ";
	public String qo = "φ";
	public String qp = "δ";
	public String qq = "σ";
	public String qr = "α";
	public String qs = "ζ";
	public String qt = "χ";
	public String qu = "ψ";
	public String qv = "ω";
	public String qw = "β";
	public String qx = "ν";
	public String qy = "μ";
	
	//put all the words in one massive
	public void init()
	{
		letters.add(a);
		letters.add(b);
		letters.add(c);
		letters.add(d);
		letters.add(e);
		letters.add(f);
		letters.add(g);
		letters.add(h);
		letters.add(i);
		letters.add(j);
		letters.add(k);
		letters.add(l);
		letters.add(m);
		letters.add(n);
		letters.add(o);
		letters.add(p);
		letters.add(q);
		letters.add(r);
		letters.add(s);
		letters.add(t);
		letters.add(u);
		letters.add(v);
		letters.add(w);
		letters.add(x);
		letters.add(y);
		letters.add(z);
		letters.add(ii);
		letters.add(cc);
		letters.add(yy);
		letters.add(kk);
		letters.add(ee);
		letters.add(yo);
		letters.add(nn);
		letters.add(gg);
		letters.add(sh);
		letters.add(ww);
		letters.add(zz);
		letters.add(xx);
		letters.add(ik);
		letters.add(je);
		letters.add(zh);
		letters.add(dd);
		letters.add(ll);
		letters.add(oo);
		letters.add(rr);
		letters.add(pp);
		letters.add(as);
		letters.add(vv);
		letters.add(yyu);
		letters.add(ff);
		letters.add(ja);
		letters.add(ch);
		letters.add(ca);
		letters.add(mm);
		letters.add(ai);
		letters.add(tt);
		letters.add(aik);
		letters.add(bb);
		letters.add(yu);
		
		replace.add(qa);
		replace.add(qb);
		replace.add(qc);
		replace.add(qd);
		replace.add(qe);
		replace.add(qf);
		replace.add(qg);
		replace.add(qh);
		replace.add(qi);
		replace.add(qj);
		replace.add(qk);
		replace.add(ql);
		replace.add(qm);
		replace.add(qn);
		replace.add(qo);
		replace.add(qp);
		replace.add(qq);
		replace.add(qr);
		replace.add(qs);
		replace.add(qt);
		replace.add(qu);
		replace.add(qv);
		replace.add(qw);
		replace.add(qx);
		replace.add(qy);
						
		
	}
	//replace text with new symbols
	public static String textReplace(String text)
	{
		text.toLowerCase();
		
		for(String t : letters)
			text.replace(t, replace.get(Rnd.get(replace.size() - 1)));
		return text;	
	}
}