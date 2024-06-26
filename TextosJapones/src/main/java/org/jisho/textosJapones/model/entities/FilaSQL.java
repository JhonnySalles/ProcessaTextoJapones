package org.jisho.textosJapones.model.entities;

import org.jisho.textosJapones.model.enums.Language;

import java.util.UUID;

public class FilaSQL {

    private final UUID id;
    private final Long sequencial;
    private final String select;
    private final String update;
    private final String delete;
    private String vocabulario;
    private Language linguagem;
    private Boolean isExporta;
    private Boolean isLimpeza;

    public UUID getId() {
        return id;
    }

    public Long getSequencial() {
        return sequencial;
    }

    public String getSelect() {
        return select;
    }

    public String getUpdate() {
        return update;
    }

    public String getDelete() {
        return delete;
    }

    public String getVocabulario() {
        return vocabulario;
    }

    public Language getLinguagem() {
        return linguagem;
    }

    public Boolean isExporta() {
        return isExporta;
    }

    public void setExporta(Boolean exporta) {
        isExporta = exporta;
    }

    public Boolean isLimpeza() {
        return isLimpeza;
    }

    public void setLimpeza(Boolean limpeza) {
        isLimpeza = limpeza;
    }

    public void setVocabulario(String vocabulario) {
        this.vocabulario = vocabulario;
    }

    public FilaSQL(String select, String update, String delete, Language linguagem, Boolean isExporta, Boolean isLimpeza) {
        this.id = null;
        this.sequencial = 0L;
        this.select = select;
        this.update = update;
        this.delete = delete;
        this.linguagem = linguagem;
        this.isExporta = isExporta;
        this.isLimpeza = isLimpeza;
        this.vocabulario = "";
    }

    public FilaSQL(UUID id, Long sequencial, String select, String update, String delete, String vocabulario, Language linguagem, Boolean isExporta, Boolean isLimpeza) {
        this.id = id;
        this.sequencial = sequencial;
        this.select = select;
        this.update = update;
        this.delete = delete;
        this.vocabulario = vocabulario;
        this.linguagem = linguagem;
        this.isExporta = isExporta;
        this.isLimpeza = isLimpeza;
    }

    @Override
    public String toString() {
        return "FilaSQL [id=" + id + ", select=" + select + ", update=" + update + ", delete=" + delete + ", vocabulario=" + vocabulario + "]";
    }

}
