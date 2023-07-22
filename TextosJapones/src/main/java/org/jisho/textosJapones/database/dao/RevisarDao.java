package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;

public interface RevisarDao {

	void insert(Revisar obj) throws ExcessaoBd;

	void update(Revisar obj) throws ExcessaoBd;

	void delete(Revisar obj) throws ExcessaoBd;

	void delete(String vocabulario) throws ExcessaoBd;

	boolean exist(String vocabulario);
	
	String isValido(String vocabulario);

	Revisar select(String vocabulario, String base) throws ExcessaoBd;

	Revisar select(String vocabulario) throws ExcessaoBd;

	List<Revisar> selectAll() throws ExcessaoBd;

	List<String> selectFrases(String select) throws ExcessaoBd;

	List<Revisar> selectTraduzir(Integer quantidadeRegistros) throws ExcessaoBd;

	String selectQuantidadeRestante() throws ExcessaoBd;

	Revisar selectRevisar(String pesquisar, Boolean isAnime, Boolean isManga) throws ExcessaoBd;

	List<Revisar> selectSimilar(String vocabulario, String ingles) throws ExcessaoBd;

	void incrementaVezesAparece(String vocabulario) throws ExcessaoBd;

	void setIsManga(Revisar obj) throws ExcessaoBd;
}
