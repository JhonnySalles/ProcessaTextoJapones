package org.jisho.textosJapones.database.dao.implement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jisho.textosJapones.database.dao.RevisarDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;

public class RevisarJaponesDaoJDBC implements RevisarDao {

	private Connection conn;

	final private String INSERT = "INSERT IGNORE INTO revisar (vocabulario, formaBasica, leitura, traducao, ingles, revisado, isAnime, isManga) VALUES (?,?,?,?,?,?,?,?);";
	final private String UPDATE = "UPDATE revisar SET formaBasica = ?, leitura = ?, traducao = ?, ingles = ?, revisado = ?, isAnime = ?, isManga = ? WHERE vocabulario = ?;";
	final private String DELETE = "DELETE FROM revisar WHERE vocabulario = ?;";
	final private String SELECT = "SELECT vocabulario, formaBasica, leitura, traducao, ingles, revisado, isAnime, isManga FROM revisar ";
	final private String SELECT_FORMA = SELECT + "WHERE vocabulario = ? OR formaBasica = ?;";
	final private String SELECT_PALAVRA = SELECT + "WHERE vocabulario = ?;";
	final private String EXIST = "SELECT vocabulario FROM revisar WHERE vocabulario = ?;";
	final private String SELECT_ALL = SELECT + "WHERE 1 > 0;";
	final private String SELECT_TRADUZIR = SELECT + "WHERE revisado = false";
	final private String SELECT_QUANTIDADE_RESTANTE = "SELECT COUNT(*) AS Quantidade FROM revisar";
	final private String SELECT_REVISAR = SELECT + "WHERE %s ORDER BY aparece DESC LIMIT 1";
	final private String SELECT_REVISAR_PESQUISA = SELECT + "WHERE vocabulario = ? or formaBasica = ? LIMIT 1";
	final private String SELECT_SIMILAR = SELECT + "WHERE vocabulario <> ? AND ingles <> '' AND ingles = ?";
	final private String INCREMENTA_VEZES_APARECE = "UPDATE revisar SET aparece = (aparece + 1) WHERE vocabulario = ?;";
	final private String SET_ISMANGA = "UPDATE revisar SET isManga = ? WHERE vocabulario = ?;";

	public RevisarJaponesDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Revisar obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setString(2, obj.getFormaBasica());
			st.setString(3, obj.getLeitura());
			st.setString(4, obj.getTraducao());
			st.setString(5, obj.getIngles());
			st.setBoolean(6, obj.getRevisado().isSelected());
			st.setBoolean(7, obj.isAnime());
			st.setBoolean(8, obj.isManga());

			st.executeUpdate();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Revisar obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getFormaBasica());
			st.setString(2, obj.getLeitura());
			st.setString(3, obj.getTraducao());
			st.setString(4, obj.getIngles());
			st.setBoolean(5, obj.getRevisado().isSelected());
			st.setBoolean(6, obj.isAnime());
			st.setBoolean(7, obj.isManga());
			st.setString(8, obj.getVocabulario());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void delete(Revisar obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(DELETE);

			st.setString(1, obj.getVocabulario());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void delete(String vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(DELETE);

			st.setString(1, vocabulario);

			st.executeUpdate();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Revisar select(String vocabulario, String base) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT_FORMA);
			st.setString(1, vocabulario);
			st.setString(2, base);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado"),
						rs.getBoolean("isAnime"), rs.getBoolean("isManga"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	@Override
	public Revisar select(String vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT_PALAVRA);
			st.setString(1, vocabulario);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado"),
						rs.getBoolean("isAnime"), rs.getBoolean("isManga"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return new Revisar(vocabulario);
	}

	@Override
	public List<Revisar> selectAll() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_ALL);
			rs = st.executeQuery();

			List<Revisar> list = new ArrayList<>();

			while (rs.next()) {
				list.add(new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado"),
						rs.getBoolean("isAnime"), rs.getBoolean("isManga")));
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<Revisar> selectTraduzir(Integer quantidadeRegistros) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_TRADUZIR + (quantidadeRegistros > 0 ? " LIMIT " + quantidadeRegistros.toString() : " LIMIT 1000"));
			rs = st.executeQuery();

			List<Revisar> list = new ArrayList<>();

			while (rs.next())
				list.add(new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado"),
						rs.getBoolean("isAnime"), rs.getBoolean("isManga")));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public boolean exist(String vocabulario) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(EXIST);
			st.setString(1, vocabulario);
			rs = st.executeQuery();

			if (rs.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return false;
	}

	@Override
	public List<String> selectFrases(String select) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(select);
			rs = st.executeQuery();

			List<String> list = new ArrayList<>();

			while (rs.next())
				list.add(rs.getString(1));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public String selectQuantidadeRestante() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT_QUANTIDADE_RESTANTE);
			rs = st.executeQuery();

			if (rs.next())
				return rs.getString("Quantidade");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return "0";
	}

	@Override
	public Revisar selectRevisar(String pesquisar, Boolean isAnime, Boolean isManga) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			if (!pesquisar.trim().isEmpty()) {
				st = conn.prepareStatement(SELECT_REVISAR_PESQUISA);
				st.setString(1, pesquisar);
				st.setString(2, pesquisar);
			} else {
				String parametro = "1>0";

				if (isAnime && isManga)
					parametro = "isAnime = true AND isManga = true";
				else {
					if (isAnime)
						parametro = "isAnime = true";
					else if (isManga)
						parametro = "isManga = true";
				}

				st = conn.prepareStatement(String.format(SELECT_REVISAR, parametro));
			}

			rs = st.executeQuery();

			if (rs.next())
				return new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado"),
						rs.getBoolean("isAnime"), rs.getBoolean("isManga"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	@Override
	public List<Revisar> selectSimilar(String vocabulario, String ingles) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_SIMILAR);
			st.setString(1, vocabulario);
			st.setString(2, ingles);
			rs = st.executeQuery();

			List<Revisar> list = new ArrayList<>();

			while (rs.next())
				list.add(new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado"),
						rs.getBoolean("isAnime"), rs.getBoolean("isManga")));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public void incrementaVezesAparece(String vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(INCREMENTA_VEZES_APARECE);
			st.setString(1, vocabulario);
			st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void setIsManga(Revisar obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(SET_ISMANGA);
			st.setBoolean(1, obj.isManga());
			st.setString(2, obj.getVocabulario());
			st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.closeStatement(st);
		}
	}

}