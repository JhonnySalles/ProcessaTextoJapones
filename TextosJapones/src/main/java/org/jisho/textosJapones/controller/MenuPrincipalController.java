package org.jisho.textosJapones.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.jisho.textosJapones.components.animation.Animacao;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.components.notification.Notificacoes;
import org.jisho.textosJapones.database.mysql.Backup;
import org.jisho.textosJapones.database.mysql.ConexaoMysql;
import org.jisho.textosJapones.model.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class MenuPrincipalController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MenuPrincipalController.class);

    private static MenuPrincipalController CONTROLLER;

    final static Image imgAnimaBanco = new Image(Animacao.class.getResourceAsStream("/images/bd/icoDataBase_48.png"));
    final static Image imgAnimaBancoEspera = new Image(
            Animacao.class.getResourceAsStream("/images/bd/icoDataEspera_48.png"));
    final static Image imgAnimaBancoErro = new Image(
            Animacao.class.getResourceAsStream("/images/bd/icoDataSemConexao_48.png"));
    final static Image imgAnimaBancoConectado = new Image(
            Animacao.class.getResourceAsStream("/images/bd/icoDataConectado_48.png"));

    final static Image imgAnimaBackup = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_48.png"));

    final static Image imgAnimaExporta = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Exportando_48.png"));
    final static Image imgAnimaExportaEspera = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Exportando_Espera_48.png"));
    final static Image imgAnimaExportaErro = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Exportando_Erro_48.png"));
    final static Image imgAnimaExportaConcluido = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Exportando_Concluido_48.png"));

    final static Image imgAnimaImporta = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Importando_48.png"));
    final static Image imgAnimaImportaEspera = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Importando_Espera_48.png"));
    final static Image imgAnimaImportaErro = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Importando_Erro_48.png"));
    final static Image imgAnimaImportaConcluido = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Importando_Concluido_48.png"));

    final static Image imgAnimaCompartilha = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoCompartilhamentoDataBase_48.png"));
    final static Image imgAnimaCompartilhaEspera = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoCompartilhamentoDataBaseEspera_48.png"));
    final static Image imgAnimaCompartilhaErro = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoCompartilhamentoDataBaseErro_48.png"));
    final static Image imgAnimaCompartilhaEnvio = new Image(
            Animacao.class.getResourceAsStream("/images/export/icoCompartilhamentoDataBaseEnvio_48.png"));

    @FXML
    private AnchorPane apGlobal;

    @FXML
    private StackPane rootStackPane;

    @FXML
    protected AnchorPane root;

    @FXML
    private JFXButton btnBanco;

    @FXML
    private ImageView imgConexaoBase;

    @FXML
    private JFXButton btnBackup;

    @FXML
    private ImageView imgBackup;

    @FXML
    private JFXButton btnCompartilhamento;

    @FXML
    private ImageView imgCompartilhamento;

    @FXML
    private JFXComboBox<Site> cbSite;

    @FXML
    private JFXComboBox<Modo> cbModo;

    @FXML
    private JFXComboBox<Dicionario> cbDicionario;

    @FXML
    private JFXComboBox<Api> cbContaGoolge;

    @FXML
    private HBox hbContainerLog;

    @FXML
    private Label lblLog;

    @FXML
    private ScrollPane scpBarraProgress;

    @FXML
    private VBox vbBarraProgress;
    private final Map<GrupoBarraProgressoController, Node> progressBar = new HashMap<>();

    private PopOver pop;
    private Timeline tmlImagemBackup;
    private final Animacao animacaoConexao = new Animacao();
    private final Animacao animacaoCompartilhaDataBase = new Animacao();

    @FXML
    private void onBtnVerificaConexao() {
        verificaConexao();
    }

    @FXML
    private void onBtnCompartilhamentoDatabase() {
        compartilhaDataBase();
    }

    @FXML
    private void onBtnConexaoOnMouseClicked(MouseEvent mouseClick) {
        if (mouseClick.getButton() == MouseButton.SECONDARY) {
            if (!pop.isShowing())
                mostrarConfiguracao();
        }
    }

    public void setImagemBancoErro(String erro) {
        animacaoConexao.tmLineImageBanco.stop();
        imgConexaoBase.setImage(imgAnimaBancoErro);
        Notificacoes.notificacao(Notificacao.ERRO, "Erro.", erro);
    }

    public MenuPrincipalController mostrarConfiguracao() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = btnBanco.getScene();
        Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
        Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
        Point2D nodeCoord = btnBanco.localToScene(0.0, 0.0);
        double cordenadaX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());

        if (cordenadaX < screenBounds.getWidth() / 2)
            pop.arrowLocationProperty().set(ArrowLocation.LEFT_TOP);
        else
            pop.arrowLocationProperty().set(ArrowLocation.RIGHT_TOP);

        pop.show(btnBanco, 30);
        return this;
    }

    public void cancelaBackup() {
        animacaoConexao.tmLineImageBackup.stop();
        imgBackup.setImage(imgAnimaBackup);
    }

    public void importaBackup() {
        animacaoConexao.animaImageBackup(imgBackup, imgAnimaImporta, imgAnimaImportaEspera);
        animacaoConexao.tmLineImageBackup.play();
        Backup.importarBackup(this);
    }

    public void importaConcluido(boolean erro) {
        animacaoConexao.tmLineImageBackup.stop();
        if (erro)
            imgBackup.setImage(imgAnimaImportaErro);
        else {
            imgBackup.setImage(imgAnimaImportaConcluido);
            verificaConexao();
        }

        tmlImagemBackup.play();
    }

    public void exportaBackup() {
        animacaoConexao.animaImageBackup(imgBackup, imgAnimaExporta, imgAnimaExportaEspera);
        animacaoConexao.tmLineImageBackup.play();
        Backup.exportarBackup(this);
    }

    public void exportaConcluido(boolean erro) {
        animacaoConexao.tmLineImageBackup.stop();
        if (erro)
            imgBackup.setImage(imgAnimaExportaErro);
        else
            imgBackup.setImage(imgAnimaExportaConcluido);

        tmlImagemBackup.play();
    }

    public void setAviso(String aviso) {
        Notificacoes.notificacao(Notificacao.AVISO, "Aviso.", aviso);
    }

    public PopOver getPopPup() {
        return pop;
    }

    public static MenuPrincipalController getController() {
        return CONTROLLER;
    }

    public Modo getModo() {
        return cbModo.getSelectionModel().getSelectedItem();
    }

    public Dicionario getDicionario() {
        return cbDicionario.getSelectionModel().getSelectedItem();
    }

    public Api getContaGoogle() {
        return cbContaGoolge.getSelectionModel().getSelectedItem();
    }

    public void setContaGoogle(Api contaGoogle) {
        cbContaGoolge.getSelectionModel().select(contaGoogle);
    }

    public Site getSite() {
        return cbSite.getSelectionModel().getSelectedItem();
    }

    public Label getLblLog() {
        return lblLog;
    }

    private void progressBarVisible(Boolean visible) {
        scpBarraProgress.setVisible(visible);
    }

    public GrupoBarraProgressoController criaBarraProgresso() {
        try {
            progressBarVisible(true);
            lblLog.setText("");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(GrupoBarraProgressoController.getFxmlLocate());
            Node barra = loader.load(); // Necessário primeiro iniciar o loader para pegar o controller.
            GrupoBarraProgressoController cnt = loader.getController();
            vbBarraProgress.getChildren().add(barra);
            progressBar.put(cnt, barra);
            return cnt;
        } catch (IOException e) {
            System.out.println("Erro ao criar barra de progresso.");
            
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public void destroiBarraProgresso(GrupoBarraProgressoController barra, String texto) {
        if (progressBar.containsKey(barra)) {
            Node item = progressBar.get(barra);
            vbBarraProgress.getChildren().remove(item);
            progressBar.remove(barra);
            progressBarVisible(!progressBar.isEmpty());
        }
        lblLog.setText(texto);
    }

    public void verificaConexao() {
        animacaoConexao.tmLineImageBanco.play();

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        Task<String> verificaConexao = new Task<String>() {

            @Override
            protected String call() throws Exception {
                TimeUnit.SECONDS.sleep(1);
                return ConexaoMysql.testaConexaoMySQL();
            }

            @Override
            protected void succeeded() {
                animacaoConexao.tmLineImageBanco.stop();
                String conectado = getValue();

                if (!conectado.isEmpty())
                    imgConexaoBase.setImage(imgAnimaBancoConectado);
                else
                    imgConexaoBase.setImage(imgAnimaBancoErro);
            }
        };

        Thread t = new Thread(verificaConexao);
        t.start();
    }

    public void compartilhaDataBase() {
        animacaoCompartilhaDataBase.tmLineImageBanco.play();
    }

    private void criaMenuBackup() {
        ContextMenu menuBackup = new ContextMenu();

        MenuItem miBackup = new MenuItem("Backup");
        ImageView imgExporta = new ImageView(imgAnimaExporta);
        imgExporta.setFitHeight(20);
        imgExporta.setFitWidth(20);
        miBackup.setGraphic(imgExporta);
        miBackup.setOnAction(event -> exportaBackup());

        MenuItem miRestaurar = new MenuItem("Restaurar");
        ImageView imgImporta = new ImageView(imgAnimaImporta);
        imgImporta.setFitHeight(20);
        imgImporta.setFitWidth(20);
        miRestaurar.setGraphic(imgImporta);
        miRestaurar.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                importaBackup();
            }
        });

        menuBackup.getItems().addAll(miBackup, miRestaurar);

        btnBackup.setOnMouseClicked(event -> {
            Scene scene = btnBanco.getScene();
            Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
            Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
            Point2D nodeCoord = btnBanco.localToScene(0.0, 0.0);
            double cordenadaX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());
            double cordenadaY = Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY());
            menuBackup.show(btnBackup, cordenadaX + 95, cordenadaY + 30);
        });

        tmlImagemBackup = new Timeline(new KeyFrame(Duration.millis(5000), ae -> cancelaBackup()));
    }

    private MenuPrincipalController criaConfiguracao() {
        pop = new PopOver();
        URL url = MenuConfiguracaoController.getFxmlLocate();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(url);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(3));// 10px padding todos os lados
        try {
            vbox.getChildren().add(loader.load());

            MenuConfiguracaoController cntConfiguracao = loader.getController();
            cntConfiguracao.setControllerPai(this);
            pop.setTitle("Configuração banco de dados");
            pop.setContentNode(vbox);
            pop.setCornerRadius(5);
            pop.setHideOnEscape(true);
            pop.setAutoFix(true);
            pop.setAutoHide(true);
            pop.setOnHidden(e -> cntConfiguracao.salvar());
            pop.setOnShowing(e -> cntConfiguracao.carregar());
            pop.getRoot().getStylesheets().add(MenuPrincipalController.class.getResource("/css/Dark_PopOver.css").toExternalForm());

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return this;
    }

    public void initialize(URL arg0, ResourceBundle arg1) {
        CONTROLLER = this;
        scpBarraProgress.managedProperty().bind(scpBarraProgress.visibleProperty());
        progressBarVisible(false);
        animacaoConexao.animaImageBanco(imgConexaoBase, imgAnimaBanco, imgAnimaBancoEspera);
        criaConfiguracao();
        criaMenuBackup();

        animacaoCompartilhaDataBase.animaImageBanco(imgCompartilhamento, imgAnimaCompartilha, imgAnimaCompartilhaEspera);

        cbSite.getItems().addAll(Site.values());
        cbSite.getSelectionModel().select(Site.TODOS);

        cbModo.getItems().addAll(Modo.values());
        cbModo.getSelectionModel().select(Modo.C);

        cbDicionario.getItems().addAll(Dicionario.values());
        cbDicionario.getSelectionModel().select(Dicionario.FULL);

        cbContaGoolge.getItems().addAll(Api.values());
        cbContaGoolge.getSelectionModel().selectFirst();

        /* Setando as variáveis para o alerta padrão. */
        AlertasPopup.setRootStackPane(rootStackPane);
        AlertasPopup.setNodeBlur(root);
        Notificacoes.setRootAnchorPane(apGlobal);

        verificaConexao();
    }

    public static URL getFxmlLocate() {
        return MenuPrincipalController.class.getResource("/view/MenuPrincipal.fxml");
    }

    public static String getIconLocate() {
        return "/images/icoTextoJapones_128.png";
    }

}
