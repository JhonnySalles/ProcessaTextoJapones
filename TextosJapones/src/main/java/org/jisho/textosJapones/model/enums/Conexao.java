package org.jisho.textosJapones.model.enums;

public enum Conexao {
    MANGAEXTRACTOR("MANGA_EXTRACTOR"),
    TEXTOINGLES("TEXTO_INGLES"),
    DECKSUBTITLE("DECKSUBTITLE"),
    TEXTOJAPONES("TEXTO_JAPONES");

    private final String value;

    Conexao(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
