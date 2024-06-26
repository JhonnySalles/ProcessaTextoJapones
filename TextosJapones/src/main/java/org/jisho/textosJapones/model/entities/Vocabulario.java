package org.jisho.textosJapones.model.entities;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Vocabulario {

	protected UUID id;
	@Expose
	protected String vocabulario;
	protected String formaBasica;
	@Expose
	protected String leitura;
	protected String leituraNovel;
	@Expose
	protected String ingles;
	@Expose
	protected String portugues;
	public String sincronizacao;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}

	public String getFormaBasica() {
		return formaBasica;
	}

	public void setFormaBasica(String formaBasica) {
		this.formaBasica = formaBasica;
	}

	public String getLeitura() {
		return leitura;
	}

	public void setLeitura(String leitura) {
		this.leitura = leitura;
	}

	public String getLeituraNovel() {
		return leituraNovel;
	}

	public void setLeituraNovel(String leituraNovel) {
		this.leituraNovel = leituraNovel;
	}

	public String getIngles() {
		return ingles;
	}

	public void setIngles(String ingles) {
		this.ingles = ingles;
	}

	public String getPortugues() {
		return portugues;
	}

	public void setPortugues(String portugues) {
		this.portugues = portugues;
	}

	public Vocabulario() {
		this.id = null;
		this.vocabulario = "";
		this.formaBasica = "";
		this.leitura = "";
		this.leituraNovel = "";
		this.portugues = "";
		this.ingles = "";
	}

	public Vocabulario(String vocabulario) {
		this.id = null;
		this.vocabulario = vocabulario;
		this.formaBasica = "";
		this.leitura = "";
		this.leituraNovel = "";
		this.portugues = "";
		this.ingles = "";
	}

	public Vocabulario(String vocabulario, String portugues) {
		this.id = null;
		this.vocabulario = vocabulario;
		this.portugues = portugues;
		this.leitura = "";
		this.formaBasica = "";
		this.ingles = "";
	}

	public Vocabulario(UUID id, String vocabulario, String portugues) {
		this.id = id;
		this.vocabulario = vocabulario;
		this.portugues = portugues;
		this.leitura = "";
		this.formaBasica = "";
		this.ingles = "";
	}
	
	public Vocabulario(String vocabulario, String formaBasica, String leitura, String leituraNovel) {
		this.id = null;
		this.vocabulario = vocabulario;
		this.formaBasica = formaBasica;
		this.leitura = leitura;
		this.leituraNovel = leituraNovel;
		this.portugues = "";
		this.ingles = "";
	}

	public Vocabulario(UUID id, String vocabulario, String formaBasica, String leitura, String leituraNovel, String ingles, String portugues) {
		this.id = id;
		this.vocabulario = vocabulario;
		this.formaBasica = formaBasica;
		this.leitura = leitura;
		this.leituraNovel = leituraNovel;
		this.portugues = portugues;
		this.ingles = ingles;
	}

	public Vocabulario(String id, HashMap<String, String> obj) {
		this.id = UUID.fromString(id);
		this.vocabulario = obj.get("vocabulario");
		this.formaBasica = obj.get("formaBasica");
		this.leitura = obj.get("leitura");
		this.leituraNovel = obj.get("leituraNovel");
		this.portugues = obj.get("portugues");
		this.ingles = obj.get("ingles");
	}

	@Override
	public String toString() {
		return vocabulario;
	}

	public void merge(Vocabulario vocab) {
		this.vocabulario = vocab.vocabulario;
		this.formaBasica = vocab.formaBasica;
		this.leitura = vocab.leitura;
		this.leituraNovel = vocab.leituraNovel;
		this.portugues = vocab.portugues;
		this.ingles = vocab.ingles;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vocabulario that = (Vocabulario) o;
		return Objects.equals(id, that.id) && Objects.equals(vocabulario, that.vocabulario);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, vocabulario);
	}
}
