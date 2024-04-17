package org.jisho.textosJapones.model.services;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.enums.Database;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VocabularioJaponesServices {

	private final VocabularioDao vocabularioDao = DaoFactory.createVocabularioJaponesDao();

	private final List<VocabularioDao> externos = DaoFactory.getVocabularioExternos();

	private void updateExterno(Vocabulario vocab) throws ExcessaoBd {
		SincronizacaoServices.enviar(Database.JAPONES, vocab);

		VocabularioExterno vocabulario = new VocabularioExterno(vocab.getId(), vocab.getVocabulario(), vocab.getPortugues(), vocab.getIngles(), vocab.getLeitura(), vocab.getLeituraNovel(), true);
		for (VocabularioDao dao : externos)
			dao.update(vocabulario);
	}

	public List<Vocabulario> selectAll() throws ExcessaoBd {
		return vocabularioDao.selectAll();
	}

	public VocabularioJaponesServices insertOrUpdate(List<Vocabulario> lista) throws ExcessaoBd {
		for (Vocabulario obj : lista)
			insertOrUpdate(obj);

		return this;
	}

	public VocabularioJaponesServices insertOrUpdate(Vocabulario obj) throws ExcessaoBd {
		if (vocabularioDao.exist(obj.getVocabulario()))
			vocabularioDao.update(obj);
		else
			save(obj);

		updateExterno(obj);

		return this;
	}

	private void save(Vocabulario obj) throws ExcessaoBd {
		if (obj.getId() == null)
			obj.setId(UUID.randomUUID());
		vocabularioDao.insert(obj);
	}

	public VocabularioJaponesServices insert(Vocabulario obj) throws ExcessaoBd {
		if (!obj.getPortugues().isEmpty()) {
			if (obj.getId() == null)
				obj.setId(UUID.randomUUID());
			vocabularioDao.insert(obj);
		}

		return this;
	}

	public VocabularioJaponesServices insert(List<Vocabulario> lista) throws ExcessaoBd {
		for (Vocabulario obj : lista)
			insert(obj);

		return this;
	}
	
	public void insertExclusao(List<String> exclusoes) throws ExcessaoBd {
		for (String exclusao : exclusoes)
			insertExclusao(exclusao);
	}

	public VocabularioJaponesServices insertExclusao(String palavra) throws ExcessaoBd {
		vocabularioDao.insertExclusao(palavra.trim());
		return this;
	}

	public boolean existeExclusao(String palavra) throws ExcessaoBd {
		return vocabularioDao.existeExclusao(palavra, palavra);
	}
	
	public boolean existeExclusao(String palavra, String basico) throws ExcessaoBd {
		return vocabularioDao.existeExclusao(palavra, basico);
	}

	public Set<String> selectExclusao() throws ExcessaoBd {
		return vocabularioDao.selectExclusao();
	}

	public void update(Vocabulario obj) throws ExcessaoBd {
		vocabularioDao.update(obj);
		updateExterno(obj);
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
	
	public boolean existe(String palavra) throws ExcessaoBd {
		return vocabularioDao.exist(palavra);
	}

}
