package org.jisho.textosJapones.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.database.mysql.ConexaoMysql;
import org.jisho.textosJapones.util.constraints.Validadores;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.DirectoryChooser;

public class MenuConfiguracaoController implements Initializable {

	@FXML
	public JFXTextField txtUsuario;

	@FXML
	public JFXPasswordField pswSenha;

	@FXML
	public JFXTextField txtServer;

	@FXML
	public JFXTextField txtPorta;

	@FXML
	public JFXTextField txtDataBaseJapones;

	@FXML
	public JFXTextField txtCaminhoMysql;

	@FXML
	public JFXTextField txtCaminhoWinrar;

	@FXML
	public JFXButton btnCaminhoMysql;
	
	@FXML
	public JFXButton btnCaminhoWinrar;

	@FXML
	public JFXTextField txtDataBaseManga;
	
	@FXML
	public JFXTextField txtDataBaseIngles;

	private MenuPrincipalController controller;

	@FXML
	private void onBtnCarregarCaminhoMysql() {
		controller.getPopPup().setDetached(true);
		String caminho = selecionaPasta("Selecione a pasta do mysql", txtCaminhoMysql.getText());
		txtCaminhoMysql.setText(caminho);
	}
	
	@FXML
	private void onBtnCarregarCaminhoWinrar() {
		controller.getPopPup().setDetached(true);
		txtCaminhoWinrar.setText(selecionaPasta("Selecione a pasta do winrar", txtCaminhoWinrar.getText()));
	}

	public void salvar() {
		ConexaoMysql.setDadosConexao(txtServer.getText(), txtPorta.getText(), txtUsuario.getText(), pswSenha.getText(), 
				txtCaminhoMysql.getText(), txtCaminhoWinrar.getText(),	txtDataBaseManga.getText(), 
				txtDataBaseJapones.getText(), txtDataBaseIngles.getText());
		controller.verificaConexao();
	}

	public void carregar() {
		ConexaoMysql.getDadosConexao();
		txtServer.setText(ConexaoMysql.getServer());
		txtPorta.setText(ConexaoMysql.getPort());
		txtDataBaseJapones.setText(ConexaoMysql.getDataBaseJapones());
		txtDataBaseManga.setText(ConexaoMysql.getDataBaseManga());
		txtDataBaseIngles.setText(ConexaoMysql.getDataBaseIngles());
		txtUsuario.setText(ConexaoMysql.getUser());
		pswSenha.setText(ConexaoMysql.getPassword());
		txtCaminhoMysql.setText(ConexaoMysql.getCaminhoMysql());
		txtCaminhoWinrar.setText(ConexaoMysql.getCaminhoWinrar());
	}

	private String selecionaPasta(String titulo, String pasta) {
		DirectoryChooser fileChooser = new DirectoryChooser();
		fileChooser.setTitle(titulo);

		if (pasta != null && !pasta.isEmpty())
			fileChooser.setInitialDirectory(new File(pasta));
		File caminho = fileChooser.showDialog(null);

		if (caminho == null)
			return "";
		else
			return caminho.getAbsolutePath();
	}
	
	public void setControllerPai(MenuPrincipalController cnt) {
		this.controller = cnt;
	}

	public static URL getFxmlLocate() {
		return MenuConfiguracaoController.class.getResource("/view/MenuConfiguracao.fxml");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Validadores.setTextFieldNotEmpty(txtServer);
		Validadores.setTextFieldNotEmpty(txtPorta);
		Validadores.setTextFieldNotEmpty(txtDataBaseJapones);
		Validadores.setTextFieldNotEmpty(txtUsuario);
		Validadores.setTextFieldNotEmpty(pswSenha);
		Validadores.setTextFieldNotEmpty(txtDataBaseManga);
		Validadores.setTextFieldNotEmpty(txtDataBaseIngles);
	}
}
