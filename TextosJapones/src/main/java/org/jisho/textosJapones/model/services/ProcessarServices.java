package org.jisho.textosJapones.model.services;

import java.util.List;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.ProcessarDao;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class ProcessarServices {

	private ProcessarDao processarDao = DaoFactory.createProcessarDao();

	public void update(String update, List<Processar> lista) throws ExcessaoBd {
		for (Processar obj : lista)
			processarDao.update(update, obj);
	}

	public void update(String update, Processar obj) throws ExcessaoBd {
		processarDao.update(update, obj);
	}

	public List<Processar> select(String select) throws ExcessaoBd {
		return processarDao.select(select);
	}

}
