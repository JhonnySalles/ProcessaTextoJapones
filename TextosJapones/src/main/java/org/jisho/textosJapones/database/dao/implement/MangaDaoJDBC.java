package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.MangaDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;

import java.sql.*;
import java.util.*;

public class MangaDaoJDBC implements MangaDao {

	private Connection conn;
	private String schema;
	
	final private String CREATE_VOLUMES = "CREATE TABLE %s_volumes (id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL, manga varchar(250) DEFAULT NULL, "
			+ "  volume INT(4) DEFAULT NULL, linguagem varchar(4) DEFAULT NULL, arquivo varchar(250) DEFAULT NULL, vocabulario longtext, "
			+ "  is_processado TINYINT(1) DEFAULT '0', PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	final private String CREATE_CAPITULOS = "CREATE TABLE %s_capitulos (id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL, id_volume VARCHAR(36) DEFAULT NULL, "
			+ "  manga LONGTEXT COLLATE utf8mb4_unicode_ci NOT NULL, volume INT(4) NOT NULL, "
			+ "  capitulo DOUBLE NOT NULL, linguagem VARCHAR(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL, scan VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL, "
			+ "  is_extra TINYINT(1) DEFAULT NULL, is_raw TINYINT(1) DEFAULT NULL, is_processado TINYINT(1) DEFAULT '0', vocabulario LONGTEXT COLLATE utf8mb4_unicode_ci, "
			+ "  PRIMARY KEY (id), KEY %s_volumes_fk (id_volume), "
			+ "  CONSTRAINT %s_volumes_capitulos_fk FOREIGN KEY (id_volume) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE "
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
	final private String CREATE_PAGINAS = "CREATE TABLE %s_paginas (id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL, id_capitulo VARCHAR(36) NOT NULL, "
			+ "  nome VARCHAR(250) DEFAULT NULL, numero VARCHAR(36) DEFAULT NULL, hash_pagina VARCHAR(250) DEFAULT NULL, is_processado TINYINT(1) DEFAULT '0', "
			+ "  vocabulario LONGTEXT, PRIMARY KEY (id), KEY %s_capitulos_fk (id_capitulo), "
			+ "  CONSTRAINT %s_capitulos_paginas_fk FOREIGN KEY (id_capitulo) REFERENCES %s_capitulos (id) ON DELETE CASCADE ON UPDATE CASCADE "
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8";
	final private String CREATE_TEXTO = "CREATE TABLE %s_textos (id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL, id_pagina VARCHAR(36) NOT NULL, "
			+ "  sequencia INT(4) DEFAULT NULL, texto LONGTEXT COLLATE utf8mb4_unicode_ci, posicao_x1 DOUBLE DEFAULT NULL, "
			+ "  posicao_y1 DOUBLE DEFAULT NULL, posicao_x2 DOUBLE DEFAULT NULL, posicao_y2 DOUBLE DEFAULT NULL, versao_app VARCHAR(36) DEFAULT '0', PRIMARY KEY (id), "
			+ "  KEY %s_paginas_fk (id_pagina), "
			+ "  CONSTRAINT %s_paginas_textos_fk FOREIGN KEY (id_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE "
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String CREATE_VOCABULARIO = "CREATE TABLE %s_vocabularios (" + "  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,"
			+ "  id_volume VARCHAR(36) DEFAULT NULL," + "  id_capitulo VARCHAR(36) DEFAULT NULL,"
			+ "  id_pagina VARCHAR(36) DEFAULT NULL," + "  palavra VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
			+ "  portugues LONGTEXT COLLATE utf8mb4_unicode_ci," + "  ingles LONGTEXT COLLATE utf8mb4_unicode_ci,"
			+ "  leitura LONGTEXT COLLATE utf8mb4_unicode_ci," + "  revisado TINYINT(1) DEFAULT 1," + "  PRIMARY KEY (id),"
			+ "  KEY %s_vocab_volume_fk (id_volume)," + "  KEY %s_vocab_capitulo_fk (id_capitulo)," + "  KEY %s_vocab_pagina_fk (id_pagina),"
			+ "  CONSTRAINT %s_vocab_capitulo_fk FOREIGN KEY (id_capitulo) REFERENCES %s_capitulos (id),"
			+ "  CONSTRAINT %s_vocab_pagina_fk FOREIGN KEY (id_pagina) REFERENCES %s_paginas (id),"
			+ "  CONSTRAINT %s_vocab_volume_fk FOREIGN KEY (id_volume) REFERENCES %s_volumes (id)"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String CREATE_TRIGGER_INSERT = "DELIMITER $$" +
			"CREATE TRIGGER tr_%s_insert BEFORE UPDATE ON %s" +
			"  FOR EACH ROW BEGIN" +
			"    IF (NEW.id IS NULL OR NEW.id = '') THEN" +
			"      SET new.id = UUID();" +
			"    END IF;" +
			"  END$$" +
			"DELIMITER ;";

	final private String CREATE_TRIGGER_UPDATE = "DELIMITER $$" +
			"CREATE TRIGGER tr_%s_update BEFORE UPDATE ON %s" +
			"  FOR EACH ROW BEGIN" +
			"    SET new.Atualizacao = NOW();" +
			"  END$$" +
			"DELIMITER ;";

	final private String UPDATE_VOLUMES = "UPDATE %s_volumes SET manga = ?, volume = ?, linguagem = ?, arquivo = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_CAPITULOS = "UPDATE %s_capitulos SET manga = ?, volume = ?, capitulo = ?, linguagem = ?, is_extra = ?, scan = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_CAPITULOS_COM_VOLUME = "UPDATE %s_capitulos SET id_volume = ?, manga = ?, volume = ?, capitulo = ?, linguagem = ?, is_extra = ?, scan = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_PAGINAS = "UPDATE %s_paginas SET nome = ?, numero = ?, hash_pagina = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_TEXTO = "UPDATE %s_textos SET sequencia = ?, texto = ?, posicao_x1 = ?, posicao_y1 = ?, posicao_x2 = ?, posicao_y2 = ? WHERE id = ?";
	final private String UPDATE_PAGINAS_CANCEL = "UPDATE %s_paginas SET is_processado = 0 WHERE id = ?";

	final private String INSERT_VOLUMES = "INSERT INTO %s_volumes (manga, volume, linguagem, arquivo, is_processado) VALUES (?,?,?,?,?)";
	final private String INSERT_CAPITULOS = "INSERT INTO %s_capitulos (id_volume, manga, volume, capitulo, linguagem, scan, is_extra, is_raw, is_processado) VALUES (?,?,?,?,?,?,?,?,?)";
	final private String INSERT_PAGINAS = "INSERT INTO %s_paginas (id_capitulo, nome, numero, hash_pagina, is_processado) VALUES (?,?,?,?,?)";
	final private String INSERT_TEXTO = "INSERT INTO %s_textos (id_pagina, sequencia, texto, posicao_x1, posicao_y1, posicao_x2, posicao_y2) VALUES (?,?,?,?,?,?,?)";

	final private String DELETE_VOLUMES = "DELETE v FROM %s_volumes AS v %s";
	final private String DELETE_CAPITULOS = "DELETE c FROM %s_capitulos AS c INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s";
	final private String DELETE_PAGINAS = "DELETE p FROM %s_paginas p "
			+ "INNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s";
	final private String DELETE_TEXTOS = "DELETE t FROM %s_textos AS t INNER JOIN %s_paginas AS p ON p.id = t.id_pagina "
			+ "INNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s";

	final private String SELECT_VOLUMES = "SELECT VOL.id, VOL.manga, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL %s WHERE %s GROUP BY VOL.id ORDER BY VOL.manga, VOL.linguagem, VOL.volume";
	final private String SELECT_CAPITULOS = "SELECT CAP.id, CAP.manga, CAP.volume, CAP.capitulo, CAP.linguagem, CAP.scan, CAP.is_extra, CAP.is_raw, CAP.is_processado "
			+ "FROM %s_capitulos CAP %s WHERE id_volume = ? AND %s GROUP BY CAP.id ORDER BY CAP.linguagem, CAP.volume, CAP.is_extra, CAP.capitulo";
	final private String SELECT_PAGINAS = "SELECT id, nome, numero, hash_pagina, is_processado FROM %s_paginas WHERE id_capitulo = ? AND %s ";
	final private String SELECT_TEXTOS = "SELECT id, sequencia, texto, posicao_x1, posicao_y1, posicao_x2, posicao_y2 FROM %s_textos WHERE id_pagina = ? ";

	final private String FIND_VOLUME = "SELECT VOL.id, VOL.manga, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL WHERE manga = ? AND volume = ? AND linguagem = ? LIMIT 1";
	final private String SELECT_VOLUME = "SELECT VOL.id, VOL.manga, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL WHERE id = ?";
	final private String SELECT_CAPITULO = "SELECT CAP.id, CAP.manga, CAP.volume, CAP.capitulo, CAP.linguagem, CAP.scan, CAP.is_extra, CAP.is_raw, CAP.is_processado "
			+ "FROM %s_capitulos CAP WHERE id = ?";
	final private String SELECT_PAGINA = "SELECT id, nome, numero, hash_pagina, is_processado FROM %s_paginas WHERE id = ?";

	final private String INNER_CAPITULOS = "INNER JOIN %s_paginas PAG ON CAP.id = PAG.id_capitulo AND PAG.is_processado = 0";
	final private String INNER_VOLUMES = "INNER JOIN %s_capitulos CAP ON VOL.id = CAP.id_volume "
			+ "INNER JOIN %s_paginas PAG ON CAP.id = PAG.id_capitulo AND PAG.is_processado = 0";

	final private String SELECT_TABELAS = "SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
			+ "FROM information_schema.tables WHERE table_schema = '%s' AND Table_Name NOT LIKE '%%exemplo%%' "
			+ "AND Table_Name LIKE '%%_volumes' AND %s GROUP BY Tabela ";

	final private String SELECT_LISTA_TABELAS = "SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
			+ " FROM information_schema.tables WHERE table_schema = '%s' "
			+ " AND Table_Name LIKE '%%_volumes%%' GROUP BY Tabela ";

	final private String EXIST_TABELA_VOCABULARIO = "SELECT Table_Name AS Tabela "
			+ " FROM information_schema.tables WHERE table_schema = '%s' "
			+ " AND Table_Name LIKE '%%_vocabularios%%' AND Table_Name LIKE '%%%s%%' GROUP BY Tabela ";

	final private String DELETE_VOCABULARIO = "DELETE FROM %s_vocabularios WHERE %s;";
	final private String INSERT_VOCABULARIO = "INSERT INTO %s_vocabularios (%s, palavra, portugues, ingles, leitura, revisado) "
			+ " VALUES (?,?,?,?,?,?);";
	final private String SELECT_VOCABUALARIO = "SELECT id, palavra, portugues, ingles, leitura, revisado FROM %s_vocabularios WHERE %s ";
	final private String UPDATE_PROCESSADO = "UPDATE %s_%s SET is_processado = 1 WHERE id = ?";

	public MangaDaoJDBC(Connection conn, String base) {
		this.conn = conn;
		this.schema = base;
	}
	
	private List<Language> getLinguagem(Language... linguagem) {
		List<Language> list = new ArrayList<Language>();
		for (Language lang : linguagem)
			if (lang != null)
				list.add(lang);
		
		return list;
	}

	@Override
	public void updateVolume(String base, MangaVolume obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_VOLUMES, base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getManga());
			st.setInt(2, obj.getVolume());
			st.setString(3, obj.getLingua().getSigla());
			st.setString(4, obj.getArquivo());
			st.setBoolean(5, obj.getProcessado());
			st.setString(6, obj.getId().toString());

			insertVocabulario(base, obj.getId(), null, null, obj.getVocabularios());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				System.out.println("Nenhum registro atualizado.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}

	}

	@Override
	public void updateCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_CAPITULOS, base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getManga());
			st.setInt(2, obj.getVolume());
			st.setFloat(3, obj.getCapitulo());
			st.setString(4, obj.getLingua().getSigla());
			st.setBoolean(5, obj.isExtra());
			st.setString(6, obj.getScan());
			st.setBoolean(7, obj.getProcessado());
			st.setString(8, obj.getId().toString());

			insertVocabulario(base, null, obj.getId(), null, obj.getVocabularios());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				System.out.println("Nenhum registro atualizado.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}

	}

	@Override
	public void updateCapitulo(String base, UUID IdVolume, MangaCapitulo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_CAPITULOS_COM_VOLUME, base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, IdVolume.toString());
			st.setString(2, obj.getManga());
			st.setInt(3, obj.getVolume());
			st.setFloat(4, obj.getCapitulo());
			st.setString(5, obj.getLingua().getSigla());
			st.setBoolean(6, obj.isExtra());
			st.setString(7, obj.getScan());
			st.setBoolean(8, obj.getProcessado());
			st.setString(9, obj.getId().toString());

			insertVocabulario(base, null, obj.getId(), null, obj.getVocabularios());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				System.out.println("Nenhum registro atualizado.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void insertVocabulario(String base, UUID idVolume, UUID idCapitulo, UUID idPagina,
			Set<MangaVocabulario> vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			if (idVolume == null && idCapitulo == null && idPagina == null)
				return;

			String where = idVolume != null ? ("id_volume = " + idVolume)
					: idCapitulo != null ? ("id_capitulo = " + idCapitulo) : ("id_pagina = " + idPagina);
			clearVocabulario(base, where);

			String campo = idVolume != null ? "id_volume" : idCapitulo != null ? "id_capitulo" : "id_pagina";
			UUID id = idVolume != null ? idVolume : idCapitulo != null ? idCapitulo : idPagina;

			for (MangaVocabulario vocab : vocabulario) {
				st = conn.prepareStatement(String.format(INSERT_VOCABULARIO, base, campo),
						Statement.RETURN_GENERATED_KEYS);

				st.setString(1, id.toString());
				st.setString(2, vocab.getPalavra());
				st.setString(3, vocab.getPortugues());
				st.setString(4, vocab.getIngles());
				st.setString(5, vocab.getLeitura());
				st.setBoolean(6, vocab.getRevisado());

				st.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	private void clearVocabulario(String base, String where) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(DELETE_VOCABULARIO, base, where),
					Statement.RETURN_GENERATED_KEYS);

			st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
	}

	private Set<MangaVocabulario> selectVocabulario(String base, String where, Boolean inverterTexto)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String order = "";
			if (inverterTexto)
				order = " ORDER BY id DESC";

			st = conn.prepareStatement(String.format(SELECT_VOCABUALARIO, base, where + order));
			rs = st.executeQuery();

			Set<MangaVocabulario> list = new HashSet<MangaVocabulario>();

			while (rs.next()) {
				// Ignora os kanji solto.
				if (rs.getString("palavra").length() <= 1)
					continue;
				list.add(new MangaVocabulario(UUID.fromString(rs.getString("id")), rs.getString("palavra"), rs.getString("portugues"),
						rs.getString("ingles"), rs.getString("leitura"), rs.getBoolean("revisado")));
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public void updatePagina(String base, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_PAGINAS, base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getNomePagina());
			st.setInt(2, obj.getNumero());
			st.setString(3, obj.getHash());
			st.setBoolean(4, obj.getProcessado());
			st.setString(5, obj.getId().toString());

			insertVocabulario(base, null, null, obj.getId(), obj.getVocabularios());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void updateTexto(String base, MangaTexto obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_TEXTO, base), Statement.RETURN_GENERATED_KEYS);

			st.setInt(1, obj.getSequencia());
			st.setString(2, obj.getTexto());
			st.setInt(3, obj.getX1());
			st.setInt(4, obj.getY1());
			st.setInt(5, obj.getX2());
			st.setInt(6, obj.getY2());
			st.setString(7, obj.getId().toString());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void updateCancel(String base, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_PAGINAS_CANCEL, base));
			st.setString(1, obj.getId().toString());
			st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE_CANCEL);
		} finally {
			DB.closeStatement(st);
		}
	}

	public List<MangaVolume> selectVolumesTransferir(String baseOrigem) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_VOLUMES, baseOrigem, "", "1>0"));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")), rs.getString("arquivo"),
						selectCapitulosTransferir(baseOrigem, UUID.fromString(rs.getString("id")))));
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaVolume> selectVolumes(String base, Boolean todos, Boolean apenasJapones) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_VOLUMES, base, base);

			st = conn.prepareStatement(
					String.format(SELECT_VOLUMES, base, inner, "VOL.linguagem = 'ja' AND 1>0"));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")), rs.getString("arquivo"),
						selectVocabulario(base, "id_volume = " + UUID.fromString(rs.getString("id")), false),
						selectCapitulos(base, todos, UUID.fromString(rs.getString("id")), apenasJapones)));
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
	
	public List<MangaVolume> selectVolumes(String base, Boolean todos, String manga, Integer volume, Float capitulo,
										   List<Language> linguagem, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_VOLUMES, base, base);

			String condicao = " 1>0 ";
			
			if (linguagem != null && !linguagem.isEmpty()) {
				String lang = "";
				for (Language lg: linguagem)
					lang += " VOL.linguagem = '" + lg.getSigla() + "' OR ";
				
				condicao += " AND (" + lang.substring(0, lang.lastIndexOf(" OR ")) + ")";
			}

			if (manga != null && !manga.trim().isEmpty())
				condicao += " AND VOL.manga LIKE " + '"' + manga.trim() + '"';

			if (volume != null && volume > 0)
				condicao += " AND VOL.volume = " + String.valueOf(volume);

			st = conn.prepareStatement(String.format(SELECT_VOLUMES, base, inner, condicao));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")), rs.getString("arquivo"),
						selectVocabulario(base, "id_volume = " + UUID.fromString(rs.getString("id")), false),
						selectCapitulos(base, todos, UUID.fromString(rs.getString("id")), capitulo, linguagem, inverterTexto)));
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaCapitulo> selectCapitulosTransferir(String baseOrigem, UUID idVolume) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_CAPITULOS, baseOrigem, "", "1>0"));
			st.setString(1, idVolume.toString());
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getBoolean("is_extra"), rs.getBoolean("is_raw"), rs.getBoolean("is_processado"),
						selectPaginasTransferir(baseOrigem, UUID.fromString(rs.getString("id")))));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaCapitulo> selectCapitulos(String base, Boolean todos, UUID idVolume, Boolean apenasJapones)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			String where = "1>0";
			if (!todos)
				inner = String.format(INNER_CAPITULOS, base);

			if (apenasJapones)
				where = "CAP.linguagem = 'ja'";

			st = conn.prepareStatement(String.format(SELECT_CAPITULOS, base, inner, where));
			st.setString(1, idVolume.toString());
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getBoolean("is_extra"), rs.getBoolean("is_raw"), rs.getBoolean("is_processado"),
						selectVocabulario(base, "id_capitulo = " + UUID.fromString(rs.getString("id")), false),
						selectPaginas(base, todos, UUID.fromString(rs.getString("id")), false, false)));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
	

	public List<MangaCapitulo> selectCapitulos(String base, Boolean todos, UUID idVolume, Float capitulo,
											   List<Language> linguagem, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_CAPITULOS, base);

			String condicao = " 1>0 ";

			if (linguagem != null && !linguagem.isEmpty()) {
				String lang = "";
				for (Language lg: linguagem)
					lang += " CAP.linguagem = '" + lg.getSigla() + "' OR ";
				
				condicao += " AND (" + lang.substring(0, lang.lastIndexOf(" OR ")) + ")";
			}

			if (capitulo != null && capitulo > 0)
				condicao += " AND CAP.capitulo = " + String.valueOf(capitulo);

			st = conn.prepareStatement(String.format(SELECT_CAPITULOS, base, inner, condicao));
			st.setString(1, idVolume.toString());
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getBoolean("is_extra"), rs.getBoolean("is_raw"), rs.getBoolean("is_processado"),
						selectVocabulario(base, "id_capitulo = " + UUID.fromString(rs.getString("id")), false),
						selectPaginas(base, todos, UUID.fromString(rs.getString("id")), inverterTexto, true)));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaPagina> selectPaginasTransferir(String baseOrigem, UUID idCapitulo) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(String.format(SELECT_PAGINAS, baseOrigem, "1>0"));
			st.setString(1, idCapitulo.toString());
			rs = st.executeQuery();

			List<MangaPagina> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaPagina(UUID.fromString(rs.getString("id")), rs.getString("nome"), rs.getInt("numero"),
						rs.getString("hash_pagina"), rs.getBoolean("is_processado"),
						selectTextosTransferir(baseOrigem, UUID.fromString(rs.getString("id")))));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaPagina> selectPaginas(String base, Boolean todos, UUID idCapitulo, Boolean inverterTexto,
										   Boolean selectVocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(
					String.format(SELECT_PAGINAS, base, (todos ? "1>0" : "is_Processado = 0")));
			st.setString(1, idCapitulo.toString());
			rs = st.executeQuery();

			List<MangaPagina> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaPagina(UUID.fromString(rs.getString("id")), rs.getString("nome"), rs.getInt("numero"),
						rs.getString("hash_pagina"), rs.getBoolean("is_processado"),
						selectTextos(base, UUID.fromString(rs.getString("id")), inverterTexto),
						(selectVocabulario ? selectVocabulario(base, "id_pagina = " + UUID.fromString(rs.getString("id")), inverterTexto)
								: new HashSet<MangaVocabulario>())));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaTexto> selectTextosTransferir(String baseOrigem, UUID idPagina) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(String.format(SELECT_TEXTOS, baseOrigem));
			st.setString(1, idPagina.toString());
			rs = st.executeQuery();

			List<MangaTexto> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaTexto(UUID.fromString(rs.getString("id")), rs.getString("texto"), rs.getInt("sequencia"),
						rs.getInt("posicao_x1"), rs.getInt("posicao_y1"), rs.getInt("posicao_x2"),
						rs.getInt("posicao_y2")));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaTexto> selectTextos(String base, UUID idPagina, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			String order = "";
			if (inverterTexto)
				order = " ORDER BY sequencia DESC";

			st = conn.prepareStatement(String.format(SELECT_TEXTOS, base) + order);
			st.setString(1, idPagina.toString());
			rs = st.executeQuery();

			List<MangaTexto> list = new ArrayList<>();

			int sequencia = 1;

			while (rs.next())
				list.add(new MangaTexto(UUID.fromString(rs.getString("id")), rs.getString("texto"), sequencia++, rs.getInt("posicao_x1"),
						rs.getInt("posicao_y1"), rs.getInt("posicao_x2"), rs.getInt("posicao_y2")));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public MangaVolume selectVolume(String base, String manga, Integer volume, Language linguagem) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(FIND_VOLUME, base));
			st.setString(1, manga);
			st.setInt(2, volume);
			st.setString(3, linguagem.getSigla());
			rs = st.executeQuery();

			if (rs.next())
				return new MangaVolume(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")), rs.getString("arquivo"),
						selectVocabulario(base, "id_volume = " + UUID.fromString(rs.getString("id")), false),
						selectCapitulos(base, true, UUID.fromString(rs.getString("id")), false));
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public MangaVolume selectVolume(String base, UUID id) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_VOLUME, base));
			st.setString(1, id.toString());
			rs = st.executeQuery();

			if (rs.next())
				return new MangaVolume(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")), rs.getString("arquivo"),
						selectVocabulario(base, "id_volume = " + UUID.fromString(rs.getString("id")), false),
						selectCapitulos(base, true, UUID.fromString(rs.getString("id")), false));
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public MangaCapitulo selectCapitulo(String base, UUID id) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_CAPITULO, base));
			st.setString(1, id.toString());
			rs = st.executeQuery();

			if (rs.next())
				return new MangaCapitulo(UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getBoolean("is_extra"), rs.getBoolean("is_raw"), rs.getBoolean("is_processado"),
						selectVocabulario(base, "id_capitulo = " + UUID.fromString(rs.getString("id")), false),
						selectPaginas(base, true, UUID.fromString(rs.getString("id")), false, false));

			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public MangaPagina selectPagina(String base, UUID id) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_PAGINA, base));
			st.setString(1, id.toString());
			rs = st.executeQuery();

			if (rs.next())
				return new MangaPagina(UUID.fromString(rs.getString("id")), rs.getString("nome"), rs.getInt("numero"),
						rs.getString("hash_pagina"), rs.getBoolean("is_processado"),
						selectTextos(base, UUID.fromString(rs.getString("id")), false), new HashSet<MangaVocabulario>());

			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<MangaTabela> selectAll(String base) throws ExcessaoBd {
		return selectAll(base, "", 0, 0F, null);
	}

	@Override
	public List<MangaTabela> selectAll(String base, String manga, Integer volume, Float capitulo, Language linguagem)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String condicao = "1>0 ";

			if (base != null && !base.trim().isEmpty())
				condicao += " AND Table_Name LIKE '%" + base.trim() + "%'";

			st = conn.prepareStatement(
					String.format(SELECT_TABELAS, schema, condicao));
			rs = st.executeQuery();

			List<MangaTabela> list = new ArrayList<>();

			while (rs.next()) {
				List<MangaVolume> volumes = selectVolumes(rs.getString("Tabela"), true, manga, volume, capitulo,
						getLinguagem(linguagem), false);
				if (volumes.size() > 0)
					list.add(new MangaTabela(rs.getString("Tabela"), volumes));
			}

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					String.format(SELECT_TABELAS, schema, "1>0"));
			rs = st.executeQuery();

			List<MangaTabela> list = new ArrayList<>();

			while (rs.next()) {
				List<MangaVolume> volumes = selectVolumes(rs.getString("Tabela"), todos, true);
				if (volumes.size() > 0)
					list.add(new MangaTabela(rs.getString("Tabela"), volumes));
			}

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga) throws ExcessaoBd {
		return selectTabelas(todos, isLike, base, linguagem, manga, 0, 0F);
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga, Integer volume) throws ExcessaoBd {
		return selectTabelas(todos, isLike, base, linguagem, manga, volume, 0F);
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga, Integer volume, Float capitulo)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String condicao = "1>0 ";

			if (base != null && !base.trim().isEmpty()) {
				if (isLike)
					condicao += " AND Table_Name LIKE '%" + base.trim() + "%'";
				else
					condicao += " AND Table_Name LIKE '" + base.trim() + "_volumes'";
			}

			st = conn.prepareStatement(
					String.format(SELECT_TABELAS, schema, condicao));
			rs = st.executeQuery();

			List<MangaTabela> list = new ArrayList<>();

			while (rs.next()) {
				createBaseVocabulario(rs.getString("Tabela"));
				List<MangaVolume> volumes = selectVolumes(rs.getString("Tabela"), todos, manga, volume, capitulo,
						getLinguagem(linguagem), false);
				if (volumes.size() > 0)
					list.add(new MangaTabela(rs.getString("Tabela"), volumes));
			}

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public void deletarVocabulario(String base) throws ExcessaoBd {
		PreparedStatement stVolume = null;
		PreparedStatement stCapitulo = null;
		PreparedStatement stPagina = null;
		PreparedStatement stVocabulario = null;
		try {
			stVocabulario = conn.prepareStatement(String.format("DELETE FROM %s_vocabulario", base));
			stPagina = conn
					.prepareStatement(String.format("UPDATE %s_paginas SET is_processado = 0", base));
			stCapitulo = conn
					.prepareStatement(String.format("UPDATE %s_capitulos SET is_processado = 0", base));
			stVolume = conn
					.prepareStatement(String.format("UPDATE %s_volumes SET is_processado = 0", base));

			conn.setAutoCommit(false);
			conn.beginRequest();
			stVocabulario.executeUpdate();
			stPagina.executeUpdate();
			stCapitulo.executeUpdate();
			stVolume.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.out.println(stVocabulario.toString());
			System.out.println(stPagina.toString());
			System.out.println(stCapitulo.toString());
			System.out.println(stVolume.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DB.closeStatement(stVocabulario);
			DB.closeStatement(stPagina);
			DB.closeStatement(stCapitulo);
			DB.closeStatement(stVolume);
		}
	}

	@Override
	public void deleteVolume(String base, MangaVolume obj) throws ExcessaoBd {
		PreparedStatement stVolume = null;
		PreparedStatement stCapitulo = null;
		PreparedStatement stPagina = null;
		PreparedStatement stTexto = null;
		try {
			String where = "WHERE ";
			if (obj.getId() != null)
				where += " v.id = " + obj.getId().toString();
			else
				where += " v.manga = '" + obj.getManga().toString() + "' AND v.volume = " + obj.getVolume().toString()
						+ " AND v.linguagem = '" + obj.getLingua().getSigla() + "'";

			String caminhoBase = base;

			stTexto = conn.prepareStatement(
					String.format(DELETE_TEXTOS, caminhoBase, caminhoBase, caminhoBase, caminhoBase, where));
			stPagina = conn
					.prepareStatement(String.format(DELETE_PAGINAS, caminhoBase, caminhoBase, caminhoBase, where));
			stCapitulo = conn.prepareStatement(String.format(DELETE_CAPITULOS, caminhoBase, caminhoBase, where));
			stVolume = conn.prepareStatement(String.format(DELETE_VOLUMES, caminhoBase, where));

			conn.setAutoCommit(false);
			conn.beginRequest();
			stTexto.executeUpdate();
			stPagina.executeUpdate();
			stCapitulo.executeUpdate();
			stVolume.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.out.println(stTexto.toString());
			System.out.println(stPagina.toString());
			System.out.println(stCapitulo.toString());
			System.out.println(stVolume.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DB.closeStatement(stTexto);
			DB.closeStatement(stPagina);
			DB.closeStatement(stCapitulo);
			DB.closeStatement(stVolume);
		}
	}

	@Override
	public void deleteCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd {
		PreparedStatement stCapitulo = null;
		PreparedStatement stPagina = null;
		PreparedStatement stTexto = null;
		try {
			String where = "WHERE c.id = " + obj.getId().toString();
			String caminhoBase = base;

			stTexto = conn.prepareStatement(
					String.format(DELETE_TEXTOS, caminhoBase, caminhoBase, caminhoBase, caminhoBase, where));
			stPagina = conn
					.prepareStatement(String.format(DELETE_PAGINAS, caminhoBase, caminhoBase, caminhoBase, where));
			stCapitulo = conn.prepareStatement(String.format(DELETE_CAPITULOS, caminhoBase, caminhoBase, where));

			conn.setAutoCommit(false);
			conn.beginRequest();
			stTexto.executeUpdate();
			stPagina.executeUpdate();
			stCapitulo.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.out.println(stTexto.toString());
			System.out.println(stPagina.toString());
			System.out.println(stCapitulo.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DB.closeStatement(stTexto);
			DB.closeStatement(stPagina);
			DB.closeStatement(stCapitulo);
		}

	}

	@Override
	public void deletePagina(String base, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement stPagina = null;
		PreparedStatement stTexto = null;
		try {
			String where = "WHERE p.id = " + obj.getId().toString();
			String caminhoBase = base;

			stTexto = conn.prepareStatement(
					String.format(DELETE_TEXTOS, caminhoBase, caminhoBase, caminhoBase, caminhoBase, where));
			stPagina = conn
					.prepareStatement(String.format(DELETE_PAGINAS, caminhoBase, caminhoBase, caminhoBase, where));

			conn.setAutoCommit(false);
			conn.beginRequest();
			stTexto.executeUpdate();
			stPagina.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.out.println(stTexto.toString());
			System.out.println(stPagina.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DB.closeStatement(stTexto);
			DB.closeStatement(stPagina);
		}

	}

	@Override
	public void deleteTexto(String base, MangaTexto obj) throws ExcessaoBd {
		PreparedStatement stTexto = null;
		try {
			String where = "WHERE t.id = " + obj.getId().toString();
			String caminhoBase = base;

			stTexto = conn.prepareStatement(
					String.format(DELETE_TEXTOS, caminhoBase, caminhoBase, caminhoBase, caminhoBase, where));
			conn.setAutoCommit(false);
			conn.beginRequest();
			stTexto.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.out.println(stTexto.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DB.closeStatement(stTexto);
		}

	}

	@Override
	public UUID insertVolume(String base, MangaVolume obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT_VOLUMES, base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getManga());
			st.setInt(2, obj.getVolume());
			st.setString(3, obj.getLingua().getSigla());
			st.setString(4, obj.getArquivo());
			st.setBoolean(5, obj.getProcessado());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				UUID id = null;
				if (rs.next())
					id = UUID.fromString(rs.getString(1));

				insertVocabulario(base, id, null, null, obj.getVocabularios());

				return id;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public UUID insertCapitulo(String base, UUID idVolume, MangaCapitulo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT_CAPITULOS, base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, idVolume.toString());
			st.setString(2, obj.getManga());
			st.setInt(3, obj.getVolume());
			st.setFloat(4, obj.getCapitulo());
			st.setString(5, obj.getLingua().getSigla());
			st.setString(6, obj.getScan());
			st.setBoolean(7, obj.isExtra());
			st.setBoolean(8, obj.isRaw());
			st.setBoolean(9, obj.getProcessado());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				UUID id = null;
				if (rs.next())
					id = UUID.fromString(rs.getString(1));

				insertVocabulario(base, null, id, null, obj.getVocabularios());

				return id;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public UUID insertPagina(String base, UUID idCapitulo, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT_PAGINAS, base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, idCapitulo.toString());
			st.setString(2, obj.getNomePagina());
			st.setInt(3, obj.getNumero());
			st.setString(4, obj.getHash());
			st.setBoolean(5, obj.getProcessado());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				UUID id = null;
				if (rs.next())
					id = UUID.fromString(rs.getString(1));

				insertVocabulario(base, null, null, id, obj.getVocabularios());
				return id;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public UUID insertTexto(String base, UUID idPagina, MangaTexto obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT_TEXTO, base), Statement.RETURN_GENERATED_KEYS);

			st.setString(1, idPagina.toString());
			st.setInt(2, obj.getSequencia());
			st.setString(3, obj.getTexto());
			st.setInt(4, obj.getX1());
			st.setInt(5, obj.getY1());
			st.setInt(6, obj.getX2());
			st.setInt(7, obj.getY2());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					return UUID.fromString(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
		return null;
	}

	@Override
	public List<MangaVolume> selectTransferir(String baseOrigem) throws ExcessaoBd {
		return selectVolumesTransferir(baseOrigem);
	}

	private void createTriggers(String base, String nome) throws ExcessaoBd {
		PreparedStatement st = null;

		try {
			st = conn.prepareStatement(String.format(CREATE_TRIGGER_INSERT, nome,  base + nome));
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		try {
			st = conn.prepareStatement(String.format(CREATE_TRIGGER_UPDATE, nome,  base + nome));
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void createDatabase(String baseDestino) throws ExcessaoBd {
		String base = schema;
		String nome = baseDestino.trim();
		if (nome.contains(".")) {
			nome = baseDestino.substring(baseDestino.indexOf(".")).replace(".", "");
			base = baseDestino.substring(0, baseDestino.indexOf(".")) + ".";
		}

		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(CREATE_VOLUMES, base + nome));
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		createTriggers(base, nome + "_volumes");

		try {
			st = conn.prepareStatement(String.format(CREATE_CAPITULOS, base + nome, nome, nome, base + nome));
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		createTriggers(base, nome + "_capitulos");

		try {
			st = conn.prepareStatement(String.format(CREATE_PAGINAS, base + nome, nome, nome, base + nome));
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		createTriggers(base, nome + "_paginas");

		try {
			st = conn.prepareStatement(String.format(CREATE_TEXTO, base + nome, nome, nome, base + nome));
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		createTriggers(base, nome + "_textos");

		try {
			st = conn.prepareStatement(String.format(CREATE_VOCABULARIO, base + nome, nome, nome, nome, nome,
					base + nome, nome, base + nome, nome, base + nome));
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		createTriggers(base, nome + "_vocabularios");
	}

	@Override
	public void createBaseVocabulario(String nome) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					String.format(EXIST_TABELA_VOCABULARIO, schema, nome));
			rs = st.executeQuery();

			if (rs.next())
				return;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		st = null;
		try {
			st = conn.prepareStatement(String.format(CREATE_VOCABULARIO, nome, nome, nome, nome, nome,
					nome, nome, nome, nome, nome));
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void updateProcessado(String base, String tabela, UUID id) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_PROCESSADO, base, tabela),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, id.toString());
			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo,
											   Language linguagem, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String condicao = "1>0 ";

			if (base != null && !base.trim().isEmpty())
				condicao += " AND Table_Name LIKE '%" + base.trim() + "%'";

			st = conn.prepareStatement(
					String.format(SELECT_TABELAS, schema, condicao));
			rs = st.executeQuery();

			List<MangaTabela> list = new ArrayList<>();

			while (rs.next()) {
				createBaseVocabulario(rs.getString("Tabela"));
				List<MangaVolume> volumes = selectVolumes(rs.getString("Tabela"), true, manga, volume, capitulo,
						getLinguagem(linguagem), inverterTexto);
				if (volumes.size() > 0)
					list.add(new MangaTabela(rs.getString("Tabela"), volumes));
			}

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<String> getTabelas() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					String.format(SELECT_LISTA_TABELAS, schema));
			rs = st.executeQuery();

			List<String> list = new ArrayList<>();

			while (rs.next())
				list.add(rs.getString("Tabela"));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

}
