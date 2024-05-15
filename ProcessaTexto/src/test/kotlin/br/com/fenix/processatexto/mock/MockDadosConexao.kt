package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.DadosConexao
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Driver
import br.com.fenix.processatexto.util.configuration.Configuracao
import org.junit.jupiter.api.Assertions.*


class MockDadosConexao : Mock<Long?, DadosConexao> {

    private fun conexaoByID(id: Long?): Conexao {
        return if (id != null && id < 5 && id > 0)
            arrayListOf(Conexao.DECKSUBTITLE, Conexao.TEXTO_INGLES, Conexao.TEXTO_JAPONES, Conexao.MANGA_EXTRACTOR, Conexao.NOVEL_EXTRACTOR)[id.minus(1).toInt()]
        else
            Conexao.PROCESSA_TEXTO
    }

    override fun mockEntity(): DadosConexao = mockEntity(0)

    override fun mockEntityList(): List<DadosConexao> {
        val list: MutableList<DadosConexao> = mutableListOf()
        for (i in 1..3)
            list.add(mockEntity(i.toLong()))
        return list
    }

    override fun updateEntity(input: DadosConexao): DadosConexao {
        return updateEntityById(input.getId())
    }

    override fun updateEntityById(lastId: Long?): DadosConexao {
        return DadosConexao(
            lastId, Conexao.TEXTO_INGLES,
            "jdbc:mysql://" + Configuracao.server + ":" + Configuracao.port + "/" + Configuracao.database + "?useTimezone=true&serverTimezone=UTC",
            "base", "usuario", "senha", Driver.EXTERNO
        )
    }

    override fun mockEntity(id: Long?): DadosConexao {
        return DadosConexao(
            id,
            conexaoByID(id),
            "jdbc:mysql://" + Configuracao.server + ":" + Configuracao.port + "/" + Configuracao.database + "?useTimezone=true&serverTimezone=UTC",
            Configuracao.database,
            Configuracao.user,
            Configuracao.password,
            Driver.MYSQL
        )
    }

    override fun assertsService(input: DadosConexao?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())
        assertNotNull(input.url)

        assertTrue(input.base.isNotBlank())
        assertTrue(input.usuario.isNotEmpty())
        assertTrue(input.senha.isNotEmpty())
        assertNotNull(input.tipo)
        assertNotNull(input.driver)
    }

    override fun assertsService(oldObj: DadosConexao?, newObj: DadosConexao?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.base, newObj!!.base)
        assertEquals(oldObj.tipo, newObj.tipo)
        assertEquals(oldObj.driver, newObj.driver)
        assertEquals(oldObj.senha, newObj.senha)
        assertEquals(oldObj.usuario, newObj.usuario)
    }

}