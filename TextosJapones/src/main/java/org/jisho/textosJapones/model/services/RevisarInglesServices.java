package org.jisho.textosJapones.model.services;

import java.util.List;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.RevisarDao;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class RevisarInglesServices {

	private RevisarDao revisarDao = DaoFactory.createRevisarInglesDao();

	public List<Revisar> selectAll() throws ExcessaoBd {
		return revisarDao.selectAll();
	}

	public List<Revisar> selectTraduzir(Integer quantidadeRegistros) throws ExcessaoBd {
		return revisarDao.selectTraduzir(quantidadeRegistros);
	}

	public RevisarInglesServices insertOrUpdate(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista) {
			if (revisarDao.exist(obj.getVocabulario()))
				revisarDao.update(obj);
			else
				revisarDao.insert(obj);
		}

		return this;
	}

	public RevisarInglesServices insertOrUpdate(Revisar obj) throws ExcessaoBd {
		if (revisarDao.exist(obj.getVocabulario()))
			revisarDao.update(obj);
		else
			revisarDao.insert(obj);

		return this;
	}

	public RevisarInglesServices insert(Revisar obj) throws ExcessaoBd {
		revisarDao.insert(obj);
		return this;
	}

	public RevisarInglesServices insert(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista)
			revisarDao.insert(obj);

		return this;
	}

	public void update(Revisar obj) throws ExcessaoBd {
		revisarDao.update(obj);
	}

	public void delete(Revisar obj) throws ExcessaoBd {
		revisarDao.delete(obj);
	}

	public void delete(String vocabulario) throws ExcessaoBd {
		revisarDao.delete(vocabulario);
	}

	public void delete(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista)
			delete(obj);
	}

	public Revisar select(String vocabulario) throws ExcessaoBd {
		return revisarDao.select(vocabulario);
	}

	public boolean existe(String palavra) throws ExcessaoBd {
		return revisarDao.exist(palavra);
	}

	public List<String> selectFrases(String select) throws ExcessaoBd {
		return revisarDao.selectFrases(select);
	}

	public String selectQuantidadeRestante() throws ExcessaoBd {
		return revisarDao.selectQuantidadeRestante();
	}

	public Revisar selectRevisar(String pesquisar, Boolean isAnime, Boolean isManga) throws ExcessaoBd {
		return revisarDao.selectRevisar(pesquisar, isAnime, isManga);
	}

	public void incrementaVezesAparece(String vocabulario) throws ExcessaoBd {
		revisarDao.incrementaVezesAparece(vocabulario);
	}

	public void setIsManga(Revisar obj) throws ExcessaoBd {
		revisarDao.setIsManga(obj);
	}
}