package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.database.dao.implement.*;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.enums.Conexao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DaoFactory {

	public static VocabularioDao createVocabularioJaponesDao() {
		return new VocabularioJaponesDaoJDBC(DB.getLocalConnection());
	}
	
	public static VocabularioDao createVocabularioInglesDao() {
		return new VocabularioInglesDaoJDBC(DB.getConnection(Conexao.TEXTOINGLES));
	}

	public static EstatisticaDao createEstatisticaDao() {
		return new EstatisticaDaoJDBC(DB.getLocalConnection());
	}

	public static RevisarDao createRevisarJaponesDao() {
		return new RevisarJaponesDaoJDBC(DB.getLocalConnection());
	}
	
	public static RevisarDao createRevisarInglesDao() {
		return new RevisarInglesDaoJDBC(DB.getConnection(Conexao.TEXTOINGLES));
	}

	public static LegendasDao createLegendasDao() {
		Connection conn = DB.getConnection(Conexao.DECKSUBTITLE);
		return conn != null ? new LegendasDaoJDBC(DB.getLocalConnection(), DB.getConnection(Conexao.DECKSUBTITLE), DB.getDados(Conexao.DECKSUBTITLE).getBase()) : null;
	}

	public static MangaDao createMangaDao() {
		Connection conn = DB.getConnection(Conexao.MANGAEXTRACTOR);
		return conn != null ? new MangaDaoJDBC(conn, DB.getDados(Conexao.MANGAEXTRACTOR).getBase()) : null;
	}

	public static NovelDao createNovelDao() {
		Connection conn = DB.getConnection(Conexao.NOVELEXTRACTOR);
		return conn != null ? new NovelDaoJDBC(conn, DB.getDados(Conexao.NOVELEXTRACTOR).getBase()) : null;
	}
	
	public static VincularDao createVincularDao() {
		Connection conn = DB.getConnection(Conexao.NOVELEXTRACTOR);
		return conn != null ? new VincularDaoJDBC(DB.getConnection(Conexao.MANGAEXTRACTOR)) : null;
	}

	public static KanjiDao createKanjiDao() {
		return new KanjiDaoJDBC(DB.getLocalConnection());
	}

	public static ComicInfoDao createComicInfoDao() {
		return new ComicInfoJDBC(DB.getLocalConnection());
	}

	public static SincronizacaoDao createSincronizacaoDao() {
		return new SincronizacaoDaoJDBC(DB.getLocalConnection());
	}

	public static List<VocabularioDao> getVocabularioExternos() {
		List<VocabularioDao> externos = new ArrayList<>();

		Connection extractor = DB.getConnection(Conexao.MANGAEXTRACTOR);
		if (extractor != null)
			externos.add(new VocabularioExternoDaoJDBC(extractor));

		Connection novel = DB.getConnection(Conexao.NOVELEXTRACTOR);
		if (novel != null)
			externos.add(new VocabularioExternoDaoJDBC(novel));

		return externos;
	}

}
