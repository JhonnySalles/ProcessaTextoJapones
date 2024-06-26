package org.jisho.textosJapones.controller.legendas;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class LegendasController implements Initializable {

	@FXML
	private AnchorPane apRoot;

	@FXML
	private StackPane stackPane;

	@FXML
	protected AnchorPane apConteinerRoot;

	@FXML
	private LegendasImportarController importarController;

	@FXML
	private LegendasVocabularioController processarController;

	@FXML
	private LegendasMarcasController marcasController;

	public AnchorPane getRoot() {
		return apConteinerRoot;
	}

	public StackPane getStackPane() {
		return stackPane;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		importarController.setControllerPai(this);
		processarController.setControllerPai(this);
		marcasController.setControllerPai(this);
	}

	public static URL getFxmlLocate() {
		return LegendasController.class.getResource("/view/legendas/Legendas.fxml");
	}

}
