package org.jisho.textosJapones.model.entities.mangaextractor;

import com.google.gson.annotations.Expose;
import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.enums.Language;

import java.util.*;

public class MangaVolume extends Manga {

	private UUID id;
	@Expose
	private String manga;
	@Expose
	private Integer volume;
	@Expose
	private Language lingua;
	@Expose
	private List<MangaCapitulo> capitulos;
	@Expose
	private Set<MangaVocabulario> vocabularios;
	@Expose
	private String arquivo;
	private Boolean processado;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getManga() {
		return manga;
	}

	public void setManga(String manga) {
		this.manga = manga;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public Language getLingua() {
		return lingua;
	}

	public void setLingua(Language lingua) {
		this.lingua = lingua;
	}

	public String getArquivo() {
		return arquivo;
	}

	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
	}

	public List<MangaCapitulo> getCapitulos() {
		return capitulos;
	}

	public void setCapitulos(List<MangaCapitulo> capitulos) {
		this.capitulos = capitulos;
	}

	public void addCapitulos(MangaCapitulo capitulo) {
		this.capitulos.add(capitulo);
	}

	public Set<MangaVocabulario> getVocabularios() {
		return vocabularios;
	}

	public void setVocabularios(Set<MangaVocabulario> vocabularios) {
		this.vocabularios = vocabularios;
	}

	public void addVocabulario(MangaVocabulario vocabulario) {
		this.vocabularios.add(vocabulario);
	}

	public Boolean getProcessado() {
		return processado;
	}

	public void setProcessado(Boolean processado) {
		this.processado = processado;
	}

	public MangaVolume() {
		super();
		this.id = null;
		this.manga = "";
		this.volume = 0;
		this.lingua = Language.PORTUGUESE;
		this.vocabularios = new HashSet<MangaVocabulario>();
		this.processado = false;
		this.processar = true;
		this.capitulos = new ArrayList<MangaCapitulo>();
		this.arquivo = "";
	}

	public MangaVolume(UUID id, String manga, Integer volume, Language lingua, String arquivo) {
		super(manga, volume, null);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.arquivo = arquivo == null ? "" : arquivo;
		this.capitulos = new ArrayList<MangaCapitulo>();
		this.vocabularios = new HashSet<MangaVocabulario>();
		this.processado = false;
		this.processar = true;
	}

	public MangaVolume(UUID id, String manga, Integer volume, Language lingua, String arquivo,
			List<MangaCapitulo> capitulos) {
		super(manga, volume, null);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.arquivo = arquivo == null ? "" : arquivo;
		this.capitulos = capitulos;
		this.vocabularios = new HashSet<MangaVocabulario>();
		this.processado = false;
		this.processar = true;
	}

	public MangaVolume(UUID id, String manga, Integer volume, Language lingua, String arquivo,
			Set<MangaVocabulario> vocabularios) {
		super(manga, volume, null);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.vocabularios = vocabularios;
		this.arquivo = arquivo == null ? "" : arquivo;
		this.processado = false;
		this.processar = true;
		this.capitulos = new ArrayList<MangaCapitulo>();
	}

	public MangaVolume(UUID id, String manga, Integer volume, Language lingua, String arquivo,
			Set<MangaVocabulario> vocabularios, List<MangaCapitulo> capitulos) {
		super(manga, volume, null);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.capitulos = capitulos;
		this.vocabularios = vocabularios;
		this.arquivo = arquivo == null ? "" : arquivo;
		this.processado = false;
		this.processar = true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		MangaVolume that = (MangaVolume) o;
		return Objects.equals(id, that.id) && Objects.equals(manga, that.manga) && Objects.equals(volume, that.volume) && lingua == that.lingua;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, manga, volume, lingua);
	}

	@Override
	public String toString() {
		return "MangaVolume [id=" + id + ", manga=" + manga + ", volume=" + volume + ", lingua=" + lingua
				+ ", vocabularios=" + vocabularios + ", arquivo=" + arquivo + "]";
	}


}