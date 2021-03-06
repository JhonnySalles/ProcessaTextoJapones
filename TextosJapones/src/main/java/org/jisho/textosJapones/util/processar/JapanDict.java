package org.jisho.textosJapones.util.processar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JapanDict {

	final private static String LINK = "https://www.japandict.com/{kanji}?lang=eng";

	public static String processa(String kanji) {
		try {
			String url = LINK.replace("{kanji}", kanji);
			Document pagina = null;

			try {
				pagina = Jsoup.parse(new URL(url).openStream(), "ISO-8859-1", url);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return "";
			}

			Elements grupos = pagina.getElementsByClass("list-group-item");

			if (grupos == null || grupos.size() == 0)
				return "";

			for (Element elemento : grupos) {

				Elements traducoes = elemento.getElementsByAttributeValue("lang", "en");

				if (traducoes != null && traducoes.size() > 0) {
					String resultado = "";
					for (Element traducao : traducoes)
						if (!traducao.text().isEmpty())
							resultado += traducao.text() + "\n";

					return resultado;
				}
			}

			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}
