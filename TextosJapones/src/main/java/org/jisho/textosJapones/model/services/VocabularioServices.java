package org.jisho.textosJapones.model.services;

import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.util.exception.ExcessaoBd;

public class VocabularioServices {

	private VocabularioDao vocabularioDao = DaoFactory.createVocabularioDao();

	public List<Vocabulario> selectAll() throws ExcessaoBd {
		return vocabularioDao.selectAll();
	}

	public void insertOrUpdate(List<Vocabulario> lista) throws ExcessaoBd {
		for (Vocabulario obj : lista) {
			if (vocabularioDao.exist(obj.getVocabulario()))
				vocabularioDao.update(obj);
			else
				vocabularioDao.insert(obj);
		}
	}

	public void insertOrUpdate(Vocabulario obj) throws ExcessaoBd {
		if (vocabularioDao.exist(obj.getVocabulario()))
			vocabularioDao.update(obj);
		else
			vocabularioDao.insert(obj);
	}

	public void insert(Vocabulario obj) throws ExcessaoBd {
		if (!obj.getTraducao().isEmpty())
			vocabularioDao.insert(obj);
	}

	public void insert(List<Vocabulario> lista) throws ExcessaoBd {
		for (Vocabulario obj : lista)
			if (!obj.getTraducao().isEmpty())
				vocabularioDao.insert(obj);
	}

	public VocabularioServices insertExclusao(String palavra) throws ExcessaoBd {
		vocabularioDao.insertExclusao(palavra);
		return this;
	}

	public Set<String> selectExclusao() throws ExcessaoBd {
		return vocabularioDao.selectExclusao();
	}

	public void update(Vocabulario obj) throws ExcessaoBd {
		vocabularioDao.update(obj);
	}

	public void delete(Vocabulario obj) throws ExcessaoBd {
		vocabularioDao.delete(obj);
	}

	public Vocabulario select(String vocabulario, String base) throws ExcessaoBd {
		return vocabularioDao.select(vocabulario, base);
	}

	public Vocabulario select(String vocabulario) throws ExcessaoBd {
		return vocabularioDao.select(vocabulario);
	}

}
