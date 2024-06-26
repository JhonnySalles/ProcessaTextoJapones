package org.jisho.textosJapones.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.components.notification.Notificacoes;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VocabularioJaponesServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CorrecaoController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrecaoController.class);

    final private static String STYLE_SHEET = AlertasPopup.class.getResource("/css/Dark_Theme.css").toExternalForm();

    @FXML
    public JFXTextField txtVocabulario;

    @FXML
    public JFXTextField txtTraducao;

    public static JFXButton btnVoltar;
    public static JFXButton btnCancelar;
    public static JFXButton btnConfirmar;

    private static JFXDialog dialog;

    private VocabularioJaponesServices vocabServ;
    private Vocabulario vocabulario;
    private final Robot robot = new Robot();

    private void onBtnCancelar() {
        limpar();
    }

    private void onBtnConfirmar() {
        salvar();
    }

    private CorrecaoController servico() {
        vocabServ = new VocabularioJaponesServices();
        return this;
    }

    private CorrecaoController procurar() {
        if (!txtVocabulario.getText().trim().isEmpty()) {
            if (vocabServ == null)
                servico();

            try {
                if (vocabServ.existe(txtVocabulario.getText())) {
                    vocabulario = vocabServ.select(txtVocabulario.getText().trim());
                    carregar();
                } else {
                    txtVocabulario.setUnFocusColor(Color.RED);
                    Notificacoes.notificacao(Notificacao.ERRO, "Vocabulário informado não encontrado.",
                            txtVocabulario.getText());
                }

            } catch (ExcessaoBd e) {
                
                LOGGER.error(e.getMessage(), e);
                Notificacoes.notificacao(Notificacao.ERRO, "Erro ao carregar vocabulário.", txtVocabulario.getText());
                txtVocabulario.setUnFocusColor(Color.RED);
            }
        }
        return this;
    }

    private CorrecaoController salvar() {
        if (!txtTraducao.getText().trim().isEmpty()) {
            if (vocabServ == null)
                servico();

            try {
                atualiza();
                vocabServ.insertOrUpdate(vocabulario);
                Notificacoes.notificacao(Notificacao.SUCESSO, "Vocabulário salvo com sucesso.",
                        vocabulario.getPortugues());
                limpar();
                txtVocabulario.requestFocus();
            } catch (ExcessaoBd e) {
                
                LOGGER.error(e.getMessage(), e);
                Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar tradução.", txtTraducao.getText());
            }
        } else
            txtVocabulario.setUnFocusColor(Color.RED);

        return this;
    }

    private CorrecaoController carregar() {
        txtVocabulario.setText(vocabulario.getVocabulario());
        txtTraducao.setText(vocabulario.getPortugues());
        return this;
    }

    private CorrecaoController atualiza() {
        vocabulario.setPortugues(txtTraducao.getText().trim());
        return this;
    }

    private CorrecaoController limpar() {
        txtVocabulario.setText("");
        txtTraducao.setText("");
        vocabulario = null;
        return this;
    }

    public static void abreTelaCorrecao(StackPane rootStackPane, Node nodeBlur) {
        try {
            BoxBlur blur = new BoxBlur(3, 3, 3);
            JFXDialogLayout dialogLayout = new JFXDialogLayout();
            dialog = new JFXDialog(rootStackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getFxmlLocate());
            Parent newAnchorPane = loader.load();
            CorrecaoController cnt = loader.getController();

            Label titulo = new Label("Tela de correção");
            titulo.setFont(Font.font(20));
            titulo.setTextFill(Color.web("#ffffff", 0.8));

            List<JFXButton> botoes = new ArrayList<JFXButton>();

            btnConfirmar = new JFXButton("Confirmar");
            btnConfirmar.setOnAction(AE -> cnt.onBtnConfirmar());
            btnConfirmar.getStyleClass().add("background-Green2");
            botoes.add(btnConfirmar);

            btnCancelar = new JFXButton("Cancelar");
            btnCancelar.setOnAction(AC -> cnt.onBtnCancelar());
            btnCancelar.getStyleClass().add("background-Red2");
            botoes.add(btnCancelar);

            btnVoltar = new JFXButton("Voltar");
            btnVoltar.setOnAction(AV -> dialog.close());
            btnVoltar.getStyleClass().add("background-White1");
            botoes.add(btnVoltar);

            dialogLayout.setHeading(titulo);
            dialogLayout.setBody(newAnchorPane);
            dialogLayout.setActions(botoes);

            dialog.getStylesheets().add(STYLE_SHEET);
            dialog.setPadding(new Insets(0, 0, 0, 0));
            dialog.setOnDialogClosed((JFXDialogEvent event1) -> {
                nodeBlur.setEffect(null);
            });

            nodeBlur.setEffect(blur);
            dialog.show();
        } catch (IOException e) {

            
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void configuraListenert() {
        txtVocabulario.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (oldVal) {
                txtVocabulario.setUnFocusColor(Color.web("#106ebe"));
                procurar();
            }
        });

        txtTraducao.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (oldVal)
                txtTraducao.setUnFocusColor(Color.web("#106ebe"));
        });

        txtVocabulario.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        txtTraducao.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configuraListenert();
        servico();
    }

    public static URL getFxmlLocate() {
        return CorrecaoController.class.getResource("/view/Correcao.fxml");
    }
}
