package org.jisho.textosJapones.model.enums;

public enum Site {

	NENHUM("Nenhum"), TODOS("Todos"), JAPANESE_TANOSHI("Japanese tanoshi"),
	TANGORIN("Tangorin"), JAPANDICT("JapanDict"), JISHO("Jisho"),
	KANSHUDO("Kanshudo");

	private final String site;

	Site(String site) {
		this.site = site;
	}

	public String getDescricao() {
		return site;
	}

	// Necessário para que a escrita do combo seja Ativo e não ATIVO (nome do enum)
	@Override
	public String toString() {
		return this.site;
	}

}
