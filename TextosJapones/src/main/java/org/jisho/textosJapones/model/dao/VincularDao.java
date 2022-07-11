package org.jisho.textosJapones.model.dao;

import java.util.List;

import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface VincularDao {

	void update(String base, Vinculo obj) throws ExcessaoBd;

	Vinculo select(String base, Integer volume, String mangaOriginal, Language original, String arquivoOriginal,
			String mangaVinculado, Language vinculado, String arquivoVinculado) throws ExcessaoBd;

	Vinculo select(String base, Integer volume, String mangaOriginal, String arquivoOriginal,
			 String mangaVinculado, String arquivoVinculado) throws ExcessaoBd;

	Vinculo select(String base, Integer volume, String mangaOriginal, Language original,
			String mangaVinculado, Language vinculado) throws ExcessaoBd;

	void delete(String base, Vinculo obj) throws ExcessaoBd;

	Long insert(String base, Vinculo obj) throws ExcessaoBd;

	Boolean createTabelas(String nome) throws ExcessaoBd;

	List<String> getMangas(String base, Language linguagem) throws ExcessaoBd;

	List<String> getTabelas() throws ExcessaoBd;

}
