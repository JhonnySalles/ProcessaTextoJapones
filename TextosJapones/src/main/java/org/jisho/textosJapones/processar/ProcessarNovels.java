package org.jisho.textosJapones.processar;

import com.google.common.io.Files;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.util.Callback;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.BaseController;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.controller.novels.NovelsImportarController;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.entities.novelextractor.*;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Site;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.*;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.jisho.textosJapones.tokenizers.SudachiTokenizer;
import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessarNovels {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessarNovels.class);

    private final VocabularioJaponesServices vocabularioJaponesService = new VocabularioJaponesServices();
    private final VocabularioInglesServices vocabularioInglesService = new VocabularioInglesServices();
    private final RevisarJaponesServices serviceJaponesRevisar = new RevisarJaponesServices();
    private final RevisarInglesServices serviceInglesRevisar = new RevisarInglesServices();
    private final BaseController controller;
    private final NovelServices serviceNovel = new NovelServices();
    private final ProcessarPalavra desmembra = new ProcessarPalavra();
    private Site siteDicionario;
    private Boolean desativar = false;
    private Integer traducoes = 0;
    private final Set<VocabularioExterno> vocabHistorico = new HashSet<>();
    private Set<String> validaHistorico = new HashSet<>();

    public ProcessarNovels(BaseController controller) {
        this.controller = controller;
    }

    public void setDesativar(Boolean desativar) {
        this.desativar = desativar;
    }

    private Integer V, Progress, Size;
    private String mensagem;
    private Set<VocabularioExterno> vocabVolume = new HashSet<>();
    private Set<VocabularioExterno> vocabCapitulo = new HashSet<>();
    private Set<String> vocabValida = new HashSet<>();

    private final HashMap<String, Integer> vocabErros = new HashMap<>();

    private final DoubleProperty propTexto = new SimpleDoubleProperty(.0);

    private Boolean error;

    private String toAlfabeto(String texto) {
        String tabela = "temp";
        switch (texto) {
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                tabela = "0";
                break;

            case "あ":
            case "ア":
            case "A":
            case "a":
                tabela = "a";
                break;

            case "え":
            case "エ":
            case "E":
            case "e":
                tabela = "e";
                break;

            case "い":
            case "イ":
            case "I":
            case "i":
                tabela = "i";
                break;

            case "お":
            case "オ":
            case "O":
            case "o":
                tabela = "o";
                break;

            case "う":
            case "ウ":
            case "U":
            case "u":
                tabela = "u";
                break;

            case "ば":
            case "べ":
            case "び":
            case "ぼ":

            case "バ":
            case "ベ":
            case "ビ":
            case "ボ":

            case "B":
            case "b":
                tabela = "b";
                break;

            case "ち":
            case "チ":
            case "C":
            case "c":
                tabela = "c";
                break;

            case "だ":
            case "で":
            case "ど":

            case "ダ":
            case "デ":
            case "ド":

            case "D":
            case "d":
                tabela = "d";
                break;

            case "ふ":
            case "フ":
            case "ぶ":
            case "ブ":
            case "F":
            case "f":
                tabela = "f";
                break;

            case "G":
            case "g":
                tabela = "g";
                break;

            case "は":
            case "へ":
            case "ひ":
            case "ほ":

            case "ハ":
            case "ヘ":
            case "ヒ":
            case "ホ":

            case "H":
            case "h":
                tabela = "h";
                break;

            case "じ":
            case "ジ":

            case "J":
            case "j":
                tabela = "j";
                break;

            case "か":
            case "け":
            case "き":
            case "こ":
            case "く":

            case "が":
            case "げ":
            case "ぎ":
            case "ご":
            case "ぐ":

            case "カ":
            case "ケ":
            case "キ":
            case "コ":
            case "ク":

            case "ガ":
            case "ゲ":
            case "ギ":
            case "ゴ":
            case "グ":

            case "K":
            case "k":
                tabela = "k";
                break;

            case "L":
            case "l":
                tabela = "l";
                break;

            case "ま":
            case "め":
            case "み":
            case "も":
            case "む":

            case "マ":
            case "メ":
            case "ミ":
            case "モ":
            case "ム":

            case "M":
            case "m":
                tabela = "m";
                break;

            case "な":
            case "ね":
            case "に":
            case "の":
            case "ぬ":
            case "ん":

            case "ナ":
            case "ネ":
            case "ニ":
            case "ノ":
            case "ヌ":
            case "ン":

            case "N":
            case "n":
                tabela = "n";
                break;

            case "ぱ":
            case "ぺ":
            case "ぴ":
            case "ぽ":
            case "ぷ":

            case "パ":
            case "ペ":
            case "ピ":
            case "ポ":
            case "プ":

            case "P":
            case "p":
                tabela = "p";
                break;

            case "Q":
            case "q":
                tabela = "q";
                break;

            case "ら":
            case "れ":
            case "り":
            case "ろ":
            case "る":

            case "ラ":
            case "レ":
            case "リ":
            case "ロ":
            case "ル":

            case "R":
            case "r":
                tabela = "r";
                break;

            case "さ":
            case "せ":
            case "し":
            case "そ":
            case "す":

            case "サ":
            case "セ":
            case "シ":
            case "ソ":
            case "ス":

            case "S":
            case "s":
                tabela = "s";
                break;

            case "た":
            case "て":
            case "と":
            case "つ":

            case "づ":

            case "タ":
            case "テ":
            case "ト":
            case "ツ":

            case "ヅ":

            case "T":
            case "t":
                tabela = "t";
                break;

            case "V":
            case "v":
                tabela = "v";
                break;

            case "や":
            case "よ":
            case "ゆ":

            case "ヤ":
            case "ヨ":
            case "ユ":

            case "Y":
            case "y":
                tabela = "y";
                break;


            case "わ":
            case "を":

            case "ワ":
            case "ヲ":

            case "W":
            case "w":
                tabela = "w";
                break;

            case "X":
            case "x":
                tabela = "x";
                break;

            case "ざ":
            case "ぜ":
            case "ぞ":
            case "ず":

            case "ザ":
            case "ゼ":
            case "ゾ":
            case "ズ":

            case "Z":
            case "z":
                tabela = "z";
                break;
        }

        return tabela;
    }

    private Integer getBase(String texto, Integer inicio) {
        Integer base = 1;
        for (int n = inicio; n < texto.length(); n++) {
            switch (texto.substring(n, n + 1)) {
                case "十":
                    if (base < 10)
                        base = 10;
                    break;

                case "百":
                    if (base < 100)
                        base = 100;
                    break;

                case "千":
                    if (base < 1000)
                        base = 1000;
                    break;

                case "万":
                    base = base * 10000;
                    break;

                case "億":
                    base = base * 100000000;
                    break;
            }
        }

        return base;
    }

    private Float toNumero(String texto, Float original) {
        Float numero = original;

        if (!texto.isEmpty()) {
            if (texto.matches("([０-９])*"))
                numero = Float.valueOf(texto.replaceAll("\uFF10", "0").replaceAll("\uFF11", "1").replaceAll("\uFF12", "2").replaceAll("\uFF13", "3")
                        .replaceAll("\uFF14", "4").replaceAll("\uFF15", "5").replaceAll("\uFF16", "6").replaceAll("\uFF17", "7")
                        .replaceAll("\uFF18", "8").replaceAll("\uFF19", "9"));
            else {
                Float num = 0f;
                Integer base = 1;
                for (int n = texto.length() -1; n >= 0; n--) {
                    switch (texto.substring(n, n + 1)) {
                        case "零":
                            num = (0f * base) + num;
                            break;

                        case "一":
                            num = (1f * base) + num;
                            break;

                        case "二":
                            num = (2f * base) + num;
                            break;

                        case "三":
                            num = (3f * base) + num;
                            break;

                        case "四":
                            num = (4f * base) + num;
                            break;

                        case "五":
                            num = (5f * base) + num;
                            break;

                        case "六":
                            num = (6f * base) + num;
                            break;

                        case "七":
                            num = (7f * base) + num;
                            break;

                        case "八":
                            num = (8f * base) + num;
                            break;

                        case "九":
                            num = (9f * base) + num;
                            break;

                        case "十":
                        case "百":
                        case "千":
                        case "万":
                        case "億":
                            base = getBase(texto, n);
                            break;
                    }
                }

                if (num > numero)
                    numero = num;
            }
        }
        return numero;
    }

    private String getBase(Language linguagem, String texto) {
        String tabela;
        String nome = "";
        if (linguagem.compareTo(Language.JAPANESE) == 0) {
            Matcher matcher = Pattern.compile("^[a-zA-Z0-9]").matcher(texto);
            if (matcher.find() && !matcher.group(0).isEmpty())
                tabela = toAlfabeto(matcher.group(0).substring(0, 1));
            else {
                matcher = Pattern.compile("([\u3041-\u9FAF]+)").matcher(texto);
                if (matcher.find() && !matcher.group(0).isEmpty()) {
                    String item = matcher.group(0);
                    if (item.trim().substring(0, 1).matches("[ぁ-んァ-ンa-zA-Z0-9]"))
                        tabela = toAlfabeto(item.trim().substring(0, 1));
                    else {
                        List<Morpheme> m = tokenizer.tokenize(SplitMode.A, item);
                        if (!m.isEmpty()) {
                            if (!m.get(0).readingForm().isEmpty())
                                nome = m.get(0).readingForm().substring(0, 1);
                            else if (!m.get(0).surface().isEmpty())
                                nome = m.get(0).surface().substring(0, 1);
                        }

                        tabela = toAlfabeto(nome);
                    }
                } else
                    tabela = texto.substring(0, 1);
            }
        } else
            tabela = texto.substring(0, 1);

        return tabela;
    }

    private NovelVolume getVolume(File arquivo, Language linguagem, Boolean favorito) throws IOException {
        File jpg = new File(arquivo.getPath().substring(0, arquivo.getPath().lastIndexOf(".")) + ".jpg");
        File opf = new File(arquivo.getPath().substring(0, arquivo.getPath().lastIndexOf(".")) + ".opf");
        String arq = arquivo.getName().substring(0, arquivo.getName().lastIndexOf("."));
        String nome;
        if (arq.toLowerCase().contains("volume"))
            nome = arq.substring(0, arq.toLowerCase().lastIndexOf("volume")).trim();
        else if (arq.toLowerCase().contains("vol."))
            nome = arq.substring(0, arq.toLowerCase().lastIndexOf("vol.")).trim();
        else
            nome = arq;

        String titulo = "";
        if (nome.matches("[a-zA-Z\\d]"))
            titulo = nome;

        Float volume = 0F;

        Matcher matcher = Pattern.compile("((volume |vol. |vol )?([\\d]+.)?[\\d]+)").matcher(arq.toLowerCase());
        if (matcher.find() && !matcher.group(0).isEmpty()) {
            String aux = matcher.group(0).toLowerCase().replace("volume", "").replace("vol.", "").replace("vol","").trim();
            if (aux.matches("[\\d.]+"))
                volume = Float.valueOf(aux);
        }

        if (volume <= 0f) {
            matcher = Pattern.compile("(([0-9]+.)?[0-9]+$)").matcher(arq.trim());
            if (matcher.find() && !matcher.group(0).isEmpty()) {
                if (matcher.group(0).contains(" "))
                    volume = Float.valueOf(matcher.group(0).substring(matcher.group(0).lastIndexOf(" ")).trim());
                else
                    volume = Float.valueOf(matcher.group(0));

                if (nome.matches("[a-zA-Z\\d]") && nome.contains(matcher.group(0)))
                    titulo = nome.substring(0, nome.lastIndexOf(matcher.group(0)));
            } else {
                matcher = Pattern.compile("(([0-9]+.)?[0-9]+)").matcher(arq.trim());
                if (matcher.find() && !matcher.group(0).isEmpty() && !matcher.group(0).contains("-"))
                    volume = Float.valueOf(matcher.group(0));
                else {
                    matcher = Pattern.compile("(([\uFF10-\uFF19]+.)?[\uFF10-\uFF19]+$)").matcher(arq.trim());
                    if (matcher.find() && !matcher.group(0).isEmpty())
                        volume = Float.valueOf(matcher.group(0).replaceAll("\uFF10", "0").replaceAll("\uFF11", "1").replaceAll("\uFF12", "2").replaceAll("\uFF13", "3")
                                .replaceAll("\uFF14", "4").replaceAll("\uFF15", "5").replaceAll("\uFF16", "6").replaceAll("\uFF17", "7")
                                .replaceAll("\uFF18", "8").replaceAll("\uFF19", "9"));
                    else {
                        matcher = Pattern.compile("(([\uFF10-\uFF19]+.)?[\uFF10-\uFF19]+)").matcher(arq.trim());
                        if (matcher.find() && !matcher.group(0).isEmpty())
                            volume = Float.valueOf(matcher.group(0).replaceAll("\uFF10", "0").replaceAll("\uFF11", "1").replaceAll("\uFF12", "2").replaceAll("\uFF13", "3")
                                    .replaceAll("\uFF14", "4").replaceAll("\uFF15", "5").replaceAll("\uFF16", "6").replaceAll("\uFF17", "7")
                                    .replaceAll("\uFF18", "8").replaceAll("\uFF19", "9").replaceAll("\uFF0E", "."));
                    }
                }
            }
        }

        NovelVolume novel = new NovelVolume(UUID.randomUUID(), nome, titulo, "", "", "", arquivo.getName(), "", "", volume, linguagem, favorito, false);

        if (jpg.exists()) {
            BufferedImage imagem = ImageIO.read(jpg);
            novel.setCapa(new NovelCapa(UUID.randomUUID(), novel.getNovel(), novel.getVolume(), novel.getLingua(), jpg.getName(), "jpg", imagem));
        }

        if (opf.exists()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

                // parse XML file
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(opf);

                NodeList title = doc.getElementsByTagName("dc:title");
                if (title != null && title.getLength() > 0)
                    novel.setTitulo(title.item(0).getTextContent());

                String autor = "";
                NodeList creator = doc.getElementsByTagName("dc:creator");
                if (creator != null && creator.getLength() > 0) {
                    for (int i = 0; i < creator.getLength(); i++)
                        autor += creator.item(i).getTextContent() + "; ";

                    if (creator.item(0).hasAttributes())
                        autor += " (" + creator.item(0).getAttributes().getNamedItem("opf:file-as").getTextContent() + "); ";

                    autor = autor.substring(0, autor.lastIndexOf("; "));
                }

                novel.setAutor(autor);

                NodeList publisher = doc.getElementsByTagName("dc:publisher");
                if (publisher != null && publisher.getLength() > 0)
                    novel.setEditora(publisher.item(0).getTextContent());

                NodeList sort = doc.getElementsByTagName("calibre:title_sort");
                if (sort != null && sort.getLength() > 0)
                    novel.setTituloAlternativo(sort.item(0).getTextContent());

                NodeList description = doc.getElementsByTagName("dc:description");
                if (description != null && description.getLength() > 0)
                    novel.setDescricao(description.item(0).getTextContent());

                NodeList series = doc.getElementsByTagName("calibre:serie");
                if (series != null && series.getLength() > 0)
                    novel.setSerie(series.item(0).getTextContent());

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return novel;
    }

    private NovelTabela getTabela(String tabela, File arquivo, Language linguagem, Boolean favorito) throws IOException {
        ArrayList<NovelVolume> list = new ArrayList<>();
        NovelVolume vol = getVolume(arquivo, linguagem, favorito);
        list.add(vol);

        if (tabela != null && !tabela.isEmpty())
            return new NovelTabela(tabela, list);
        else
            return new NovelTabela(getBase(linguagem, vol.getNovel()), list);
    }

    private void getIndices(NovelVolume volume, ArrayList<NovelTexto> textos, Language linguagem) {
        HashMap<Integer, String> indices = new HashMap<>();
        for (NovelTexto texto : textos) {
            if (texto.getTexto().contains("*"))
                indices.put(texto.getSequencia(), texto.getTexto().replaceAll("\\* ", "").trim());
            else if (texto.getTexto().toLowerCase().contains("índice:"))
                continue;
            else
                break;
        }

        if (!indices.isEmpty()) {
            Float lastCap = 0f;

            for (Integer k : indices.keySet()) {
                String indice = indices.get(k);
                Float cap = lastCap;

                if (linguagem.compareTo(Language.JAPANESE) == 0) {
                    Matcher matcher = Pattern.compile("((第)?([\\d]|[０-９]|零|一|二|三|四|五|六|七|八|九|十|千|万|百|億|兆)*(話|譜|章))").matcher(indice);
                    if (matcher.find() && !matcher.group(0).isEmpty()) {
                        String aux = matcher.group(0).replaceAll("(第|話|譜|章)","");
                        if (aux.matches("([０-９]|零|一|二|三|四|五|六|七|八|九|十|千|万|百|億|兆)*"))
                            cap = toNumero(aux, cap);
                        else if (aux.matches("([\\d])*"))
                            cap = Float.valueOf(aux);
                    }
                } else if (linguagem.compareTo(Language.ENGLISH) == 0) {
                    Matcher matcher = Pattern.compile("((capítulo |capitulo |cap. |cap )?([\\d.]+)?[\\d]+)").matcher(indice.toLowerCase());
                    if (matcher.find() && !matcher.group(0).isEmpty()) {
                        String aux = matcher.group(0).toLowerCase()
                                .replace("capítulo", "")
                                .replace("capitulo", "")
                                .replace("cap.", "")
                                .replace("cap","").trim();
                        matcher = Pattern.compile("(\\d*\\.?\\d+)").matcher(aux);
                        if (matcher.find() && !matcher.group(0).isEmpty())
                            cap = Float.valueOf(matcher.group(0));
                    }
                }

                if (cap == lastCap) {
                    Matcher matcher = Pattern.compile("^(([\\d.]+)?[\\d]+)").matcher(indice.toLowerCase());
                    if (matcher.find() && !matcher.group(0).isEmpty())
                        cap = Float.valueOf(matcher.group(0));
                }

                NovelCapitulo capitulo = new NovelCapitulo(UUID.randomUUID(), volume.getNovel(), volume.getVolume(), cap, indice, k, volume.getLingua());
                int pi = 0, pos = -1;
                for (int i = pi; i < textos.size(); i++) {
                    pi = i;
                    if (textos.get(i).getTexto().trim().compareToIgnoreCase(indice) == 0) {
                        pi = i + 1;
                        pos = i;
                        break;
                    }
                }

                for (int i = pi; i < textos.size(); i++) {
                    if (textos.get(i).getTexto().trim().compareToIgnoreCase(indice) == 0) {
                        pos = i;
                        break;
                    }
                }

                if (pos >= 0) {
                    capitulo.setSequencia(textos.get(0).getSequencia());
                    for (int i = 0; i <= pos; i++)
                        capitulo.addTexto(textos.remove(0));
                }

                lastCap = cap;
                volume.addCapitulos(capitulo);
            }

            if (!textos.isEmpty()) {
                NovelCapitulo capitulo = volume.getCapitulos().get(volume.getCapitulos().size() - 1);
                for (int i = 0; i < textos.size(); i++)
                    capitulo.addTexto(textos.remove(i));
            }
        } else {
            NovelCapitulo capitulo = new NovelCapitulo(UUID.randomUUID(), volume.getNovel(), volume.getVolume(), 0f, "", 0, volume.getLingua());
            capitulo.setTextos(textos);
            volume.addCapitulos(capitulo);
        }
    }

    private void addLog(String texto) {
        if (controller != null && controller instanceof NovelsImportarController)
            Platform.runLater(() -> ((NovelsImportarController) controller).addLog(texto));

        if (LOG != null && LOG.exists())
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG, true))) {
                writer.append(texto);
                writer.newLine();
                writer.flush();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
    }

    private static String LOGFILE = "log.txt";
    private File LOG;
    public void processarArquivos(File caminho, String tabela, Language linguagem, Boolean favorito) {
        error = false;
        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo().setText("Novels - Processar arquivos");

        Task<Void> processarArquivos = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (caminho.isDirectory())
                    LOG = new File(caminho + "\\" + LOGFILE);
                else
                    LOG = new File(caminho.getPath().substring(0, caminho.getPath().lastIndexOf("\\")) + "\\" + LOGFILE);

                File concluido = new File(caminho + "\\concluido\\");
                if (!concluido.exists())
                    concluido.mkdirs();

                try {
                    try (Dictionary dict = new DictionaryFactory().create("",
                            SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(MenuPrincipalController.getController().getDicionario()))))) {
                        tokenizer = dict.create();
                        mode = SudachiTokenizer.getModo(MenuPrincipalController.getController().getModo());
                        siteDicionario = MenuPrincipalController.getController().getSite();

                        validaHistorico = new HashSet<>();
                        propTexto.set(.0);

                        addLog("Preparando arquivos...");

                        HashMap<String, File> arquivos = new HashMap<>();
                        List<NovelTabela> novels = new ArrayList<>();

                        if (caminho.isDirectory()) {
                            for (File arquivo : caminho.listFiles())
                                if (!arquivo.getName().equalsIgnoreCase(LOGFILE) && arquivo.getName().substring(arquivo.getName().lastIndexOf('.') + 1).equalsIgnoreCase("txt")) {
                                    addLog("Preparando " + arquivo.getName());
                                    NovelTabela obj = getTabela(tabela, arquivo, linguagem, favorito);
                                    Optional<NovelTabela> tab = novels.stream().filter(i -> i.getBase().equalsIgnoreCase(obj.getBase())).findFirst();
                                    if (tab.isPresent())
                                        tab.get().getVolumes().addAll(obj.getVolumes());
                                    else
                                        novels.add(obj);

                                    arquivos.put(arquivo.getName(), arquivo);
                                }
                        } else {
                            novels.add(getTabela(tabela, caminho, linguagem, favorito));
                            arquivos.put(caminho.getName(), caminho);
                        }

                        Progress = 0;
                        Size = 0;

                        novels.forEach(t -> Size += t.getVolumes().size());
                        Size = Size * 3;

                        desativar = false;
                        for (NovelTabela novel : novels) {
                            for (NovelVolume volume : novel.getVolumes()) {
                                propTexto.set(.0);
                                updateMessage("Importando texto do arquivo " + volume.getArquivo() + "...");
                                updateProgress(++Progress, Size);

                                addLog("Importando texto do arquivo " + volume.getArquivo() + "...");
                                addLog("Inicio do processo: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

                                ArrayList<NovelTexto> textos = new ArrayList<>();
                                FileReader fr = new FileReader(arquivos.get(volume.getArquivo()));
                                try (BufferedReader br = new BufferedReader(fr)) {
                                    Integer seq = 0;
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        if (line.trim().isEmpty())
                                            continue;
                                        seq++;
                                        textos.add(new NovelTexto(UUID.randomUUID(), line, seq));
                                    }
                                }

                                getIndices(volume, textos, linguagem);

                                if (desativar)
                                    break;

                                addLog("Processando textos... ");
                                updateMessage("Processando textos... ");
                                updateProgress(++Progress, Size);

                                Callback<Integer[], Boolean> callback = param -> {
                                    Platform.runLater(() -> {
                                        updateMessage("Processando itens...." + param[0] + '/' + param[1]);
                                        propTexto.set((double) param[0] / param[1]);
                                    });
                                    return true;
                                };

                                switch (linguagem) {
                                    case JAPANESE -> processarJapones(volume, callback);
                                    case ENGLISH -> processarIngles(volume, callback);
                                }

                                if (desativar)
                                    break;

                                updateMessage("Salvando textos...");
                                updateProgress(++Progress, Size);
                                addLog("Salvando textos...");

                                serviceNovel.salvarVolume(novel.getBase(), volume);

                                addLog("Concluído processamento do arquivo " + volume.getArquivo() + ".");
                                addLog("-".repeat(30));
                                addLog("");

                                File arq = arquivos.get(volume.getArquivo());
                                File jpg = new File(arq.getPath().substring(0, arq.getPath().lastIndexOf(".")) + ".jpg");
                                File opf = new File(arq.getPath().substring(0, arq.getPath().lastIndexOf(".")) + ".opf");

                                Files.move(arq, new File(concluido, volume.getArquivo()));
                                if (jpg.exists())
                                    Files.move(jpg, new File(concluido, jpg.getName()));
                                if (opf.exists())
                                    Files.move(opf, new File(concluido, opf.getName()));

                                Platform.runLater(() -> {
                                    if (TaskbarProgressbar.isSupported())
                                        TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.NORMAL);
                                });
                            }

                            if (desativar)
                                break;
                        }

                        Platform.runLater(() -> {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
                        });

                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                        error = true;

                        addLog("Erro ao processar o arquivo.");
                        addLog(e.getMessage());

                        Platform.runLater(() -> {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.ERROR);
                        });
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    error = true;

                    addLog("Erro ao processar o arquivo.");
                    addLog(e.getMessage());

                    Platform.runLater(() -> {
                        if (TaskbarProgressbar.isSupported())
                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.ERROR);
                    });
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.failed();
                if (error)
                    AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(), controller.getRoot(), null, "Erro", "Erro ao processar as novels.");
                else if (!desativar)
                    AlertasPopup.AvisoModal(controller.getControllerPai().getStackPane(), controller.getRoot(), null, "Aviso", "Novels processadas com sucesso.");

                if (error)
                    addLog("Erro ao processar as novels.");
                else if (!desativar)
                    addLog("Novels processadas com sucesso.");

                progress.getBarraProgresso().progressProperty().unbind();
                controller.getBarraProgresso().progressProperty().unbind();
                if (controller instanceof NovelsImportarController)
                    ((NovelsImportarController) controller).getBarraProgressoTextos().progressProperty().unbind();
                progress.getLog().textProperty().unbind();
                controller.habilitar();

                MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
            }

            @Override
            protected void failed() {
                super.failed();
                LOGGER.warn("Erro na thread de processamento da novel: " + super.getMessage());
                addLog("Erro na thread de processamento da novel: " + super.getMessage());
            }
        };
        progress.getBarraProgresso().progressProperty().bind(processarArquivos.progressProperty());
        if (controller instanceof NovelsImportarController)
            ((NovelsImportarController) controller).getBarraProgressoTextos().progressProperty().bind(propTexto);
        controller.getBarraProgresso().progressProperty().bind(processarArquivos.progressProperty());
        progress.getLog().textProperty().bind(processarArquivos.messageProperty());
        Thread t = new Thread(processarArquivos);
        t.start();
    }

    public void processarTabelas(List<NovelTabela> tabelas) {
        error = false;
        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo().setText("Novels - Processar tabelas");
        Task<Void> processar = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    try (Dictionary dict = new DictionaryFactory().create("",
                            SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(MenuPrincipalController.getController().getDicionario()))))) {
                        tokenizer = dict.create();
                        mode = SudachiTokenizer.getModo(MenuPrincipalController.getController().getModo());
                        siteDicionario = MenuPrincipalController.getController().getSite();

                        validaHistorico = new HashSet<>();

                        Progress = 0;
                        Size = 0;
                        mensagem = "";
                        tabelas.stream().filter(t -> t.isProcessar()).collect(Collectors.toList())
                                .forEach(tabela -> tabela.getVolumes().stream().filter(v -> v.isProcessar())
                                        .collect(Collectors.toList())
                                        .forEach(volume -> Size++));

                        desativar = false;
                        for (NovelTabela novel : tabelas) {
                            if (!novel.isProcessar())
                                continue;

                            V = 0;
                            for (NovelVolume volume : novel.getVolumes()) {
                                if (!volume.isProcessar())
                                    continue;

                                V++;
                                updateMessage("Processando Vocabulários... " + volume.getNovel());
                                updateProgress(++Progress, Size);

                                mensagem = "Processando " + V + " de " + novel.getVolumes().size() + " volumes." + " Novel: " + volume.getNovel();
                                updateMessage(mensagem);

                                Callback<Integer[], Boolean> callback = param -> {
                                    Platform.runLater(() -> {
                                        updateMessage(mensagem + " - Processando itens...." + param[0] + '/' + param[1]);
                                    });
                                    return true;
                                };

                                switch (volume.getLingua()) {
                                    case JAPANESE -> processarJapones(volume, callback);
                                    case ENGLISH -> processarIngles(volume, callback);
                                }

                                if (desativar)
                                    break;

                                updateMessage("Salvando vocabulário...");
                                updateProgress(++Progress, Size);

                                serviceNovel.updateVocabularioVolume(novel.getBase(), volume);

                                if (desativar) {
                                    updateMessage("Revertendo a ultima alteração da Novel: " + volume.getNovel() + " - Volume: " + volume.getVolume().toString());
                                    serviceNovel.updateCancel(novel.getBase(), volume);
                                    break;
                                }

                                Platform.runLater(() -> {
                                    if (TaskbarProgressbar.isSupported())
                                        TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.NORMAL);
                                });
                            }

                            if (desativar)
                                break;
                        }

                        Platform.runLater(() -> {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
                        });

                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                        error = true;

                        addLog("Erro ao processar as novels.");
                        addLog(e.getMessage());

                        Platform.runLater(() -> {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.ERROR);
                        });
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    error = true;

                    addLog("Erro ao processar as novels.");
                    addLog(e.getMessage());

                    Platform.runLater(() -> {
                        if (TaskbarProgressbar.isSupported())
                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.ERROR);
                    });
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.failed();
                if (error)
                    AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(), controller.getRoot(), null, "Erro", "Erro ao processar as novels.");
                else if (!desativar)
                    AlertasPopup.AvisoModal(controller.getControllerPai().getStackPane(), controller.getRoot(), null, "Aviso", "Novels processadas com sucesso.");

                if (error)
                    addLog("Erro ao processar as novels.");
                else if (!desativar)
                    addLog("Novels processadas com sucesso.");

                progress.getBarraProgresso().progressProperty().unbind();
                controller.getBarraProgresso().progressProperty().unbind();
                progress.getLog().textProperty().unbind();
                controller.habilitar();

                MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
            }

            @Override
            protected void failed() {
                super.failed();
                LOGGER.warn("Erro na thread de processamento da novel: " + super.getMessage());
                addLog("Erro na thread de processamento da novel: " + super.getMessage());
            }
        };
        progress.getBarraProgresso().progressProperty().bind(processar.progressProperty());
        controller.getBarraProgresso().progressProperty().bind(processar.progressProperty());
        progress.getLog().textProperty().bind(processar.messageProperty());
        Thread t = new Thread(processar);
        t.start();
    }

    private void processarJapones(NovelVolume volume, Callback<Integer[], Boolean> callback) throws ExcessaoBd {
        try (Dictionary dict = new DictionaryFactory().create("",
                SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(MenuPrincipalController.getController().getDicionario()))))) {
            tokenizer = dict.create();
            mode = SudachiTokenizer.getModo(MenuPrincipalController.getController().getModo());
            siteDicionario = MenuPrincipalController.getController().getSite();

            validaHistorico = new HashSet<>();
            desativar = false;

            Integer[] size = new Integer[2];

            vocabVolume = new HashSet<>();
            size[0] = 0;
            size[1] = 0;
            volume.getCapitulos().forEach(c -> size[1] += c.getTextos().size());

            for (NovelCapitulo capitulo : volume.getCapitulos()) {
                vocabCapitulo = new HashSet<>();
                vocabValida = new HashSet<>();
                for (NovelTexto texto : capitulo.getTextos()) {
                    size[0]++;
                    callback.call(size);
                    gerarVocabulario(texto.getTexto());

                    if (desativar)
                        break;
                }

                capitulo.setVocabularios(vocabCapitulo);

                if (desativar)
                    break;
            }
            volume.setVocabularios(vocabVolume);
            volume.setProcessado(true);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            error = false;
        }
    }

    private String getSignificado(String kanji) {
        if (kanji.trim().isEmpty())
            return "";

        addLog(kanji + " : Obtendo significado.");

        String resultado = "";
        switch (siteDicionario) {
            case TODOS:
                resultado = TanoshiJapanese.processa(kanji);

                if (resultado.isEmpty())
                    resultado = JapanDict.processa(kanji);

                if (resultado.isEmpty())
                    resultado = Jisho.processa(kanji);
                break;
            case JAPANESE_TANOSHI:
                resultado = TanoshiJapanese.processa(kanji);
                break;
            case JAPANDICT:
                resultado = JapanDict.processa(kanji);
                break;
            case JISHO:
                resultado = Jisho.processa(kanji);
                break;
            default:
        }

        return resultado;
    }

    private String getDesmembrado(String palavra) {
        String resultado = "";
        Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(palavra + " : Desmembrando a palavra."));
        resultado = processaPalavras(palavra, desmembra.processarDesmembrar(palavra, MenuPrincipalController.getController().getDicionario(), Modo.B), Modo.B);

        if (resultado.isEmpty())
            resultado = processaPalavras(palavra, desmembra.processarDesmembrar(palavra, MenuPrincipalController.getController().getDicionario(), Modo.A), Modo.A);

        return resultado;
    }

    private String processaPalavras(String original, List<String> palavras, Modo modo) {
        String desmembrado = "";
        try {
            for (String palavra : palavras) {
                if (original.equalsIgnoreCase(palavra))
                    continue;

                String resultado = getSignificado(palavra);

                if (!resultado.trim().isEmpty())
                    desmembrado += palavra + " - " + resultado + "; ";
                else if (modo.equals(Modo.B)) {
                    resultado = processaPalavras(original, desmembra.processarDesmembrar(palavra,MenuPrincipalController.getController().getDicionario(), Modo.A), Modo.A);
                    if (!resultado.trim().isEmpty())
                        desmembrado += resultado;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            desmembrado = "";
        }

        return desmembrado;
    }

    final private String pattern = ".*[\u4E00-\u9FAF].*";
    final private String japanese = "[\u3041-\u9FAF]";
    private Tokenizer tokenizer;
    private SplitMode mode;

    private void gerarVocabulario(String frase) throws ExcessaoBd {
        String texto = frase;

        HashMap<String, String> furigana = new HashMap<>();
        if (texto.toLowerCase().contains("[ruby]")) {
            Matcher matcher = Pattern.compile("(\\[ruby\\][^\\ruby]*\\[\\\\ruby\\])").matcher(texto);
            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    String palavra = matcher.group(i).replaceAll("\\[\\\\?ruby\\]", "");
                    String kanji = palavra.replaceAll("\\[rt\\][ぁ-龯]*\\[\\\\rt\\]", "");
                    String furi = palavra.replaceAll("[ぁ-龯]*\\[rt\\]", "").replaceAll("\\[\\\\rt\\]", "");
                    if (!furigana.containsKey(kanji))
                        furigana.put(kanji, furi);
                }
            }
            texto = texto.replaceAll("\\[rt\\][ぁ-龯]*\\[\\\\rt\\]", "").replaceAll("\\[\\\\?ruby\\]", "");
        }

        for (Morpheme m : tokenizer.tokenize(mode, texto)) {
            if (m.surface().matches(pattern)) {
                if (validaHistorico.contains(m.dictionaryForm())) {
                    VocabularioExterno vocabulario = vocabHistorico.stream()
                            .filter(vocab -> m.dictionaryForm().equalsIgnoreCase(vocab.getPalavra())).findFirst()
                            .orElse(null);
                    if (vocabulario != null) {
                        vocabCapitulo.add(vocabulario);
                        vocabVolume.add(vocabulario);
                        continue;
                    }
                }

                if (vocabErros.containsKey(m.dictionaryForm()) && vocabErros.get(m.dictionaryForm()) > 3)
                    continue;

                if (!vocabValida.contains(m.dictionaryForm())) {
                    Vocabulario palavra = vocabularioJaponesService.select(m.surface(), m.dictionaryForm());

                    String kanji = texto.substring(m.begin(), m.end());
                    String leitura = "";
                    if (furigana.containsKey(kanji))
                        leitura = furigana.get(kanji);

                    if (palavra != null) {
                        VocabularioExterno vocabulario = new VocabularioExterno(palavra.getId(), palavra.getVocabulario(), palavra.getPortugues(), palavra.getIngles(), palavra.getLeitura(), true);

                        // Usado apenas para correção em formas em branco.
                        if (palavra.getFormaBasica().isEmpty()) {
                            palavra.setFormaBasica(m.dictionaryForm());
                            palavra.setLeitura(m.readingForm());
                            vocabularioJaponesService.update(palavra);
                        }

                        if (!leitura.isEmpty() && (palavra.getLeituraNovel() == null || !palavra.getLeituraNovel().equalsIgnoreCase(leitura))) {
                            palavra.setLeituraNovel(leitura);
                            vocabularioJaponesService.update(palavra);
                        }

                        validaHistorico.add(m.dictionaryForm());
                        vocabHistorico.add(vocabulario);

                        vocabValida.add(m.dictionaryForm());
                        vocabCapitulo.add(vocabulario);
                        vocabVolume.add(vocabulario);
                    } else {
                        Revisar revisar = serviceJaponesRevisar.select(m.surface(), m.dictionaryForm());
                        if (revisar == null) {
                            revisar = new Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), leitura, false, false, false, true);
                            Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(m.surface() + " : Vocabulário novo."));
                            serviceJaponesRevisar.insert(revisar);

                            revisar.setIngles(getSignificado(revisar.getVocabulario()));

                            if (revisar.getIngles().isEmpty() && !revisar.getFormaBasica().equalsIgnoreCase(revisar.getVocabulario()))
                                revisar.setIngles(getSignificado(revisar.getFormaBasica()));

                            if (revisar.getIngles().isEmpty())
                                revisar.setIngles(getDesmembrado(revisar.getVocabulario()));

                            if (!revisar.getIngles().isEmpty()) {
                                try {
                                    traducoes++;

                                    if (traducoes > 3000) {
                                        traducoes = 0;
                                        MenuPrincipalController.getController().setContaGoogle(Util.next(MenuPrincipalController.getController().getContaGoogle()));
                                    }

                                    Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(m.surface() + " : Obtendo tradução."));
                                    revisar.setPortugues(Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(),
                                                    Language.PORTUGUESE.getSigla(), revisar.getIngles(), MenuPrincipalController.getController().getContaGoogle())));
                                } catch (IOException e) {
                                    LOGGER.error(e.getMessage(), e);

                                    if (vocabErros.containsKey(m.dictionaryForm()))
                                        vocabErros.put(m.dictionaryForm(), vocabErros.get(m.dictionaryForm()) +1);
                                    else
                                        vocabErros.put(m.dictionaryForm(), 1);
                                }
                            } else {
                                if (vocabErros.containsKey(m.dictionaryForm()))
                                    vocabErros.put(m.dictionaryForm(), vocabErros.get(m.dictionaryForm()) +1);
                                else
                                    vocabErros.put(m.dictionaryForm(), 1);
                            }

                            serviceJaponesRevisar.update(revisar);
                            Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(""));
                        } else {
                            if (!revisar.isNovel()) {
                                revisar.setNovel(true);
                                serviceJaponesRevisar.setIsNovel(revisar);
                            }

                            if (!leitura.isEmpty() && (revisar.getLeituraNovel() == null || !revisar.getLeituraNovel().equalsIgnoreCase(leitura))) {
                                revisar.setLeituraNovel(leitura);
                                serviceJaponesRevisar.update(revisar);
                            }

                            serviceJaponesRevisar.incrementaVezesAparece(revisar.getVocabulario());
                        }

                        VocabularioExterno vocabulario = new VocabularioExterno(revisar.getId(), revisar.getVocabulario(), revisar.getPortugues(), revisar.getIngles(), m.readingForm(), false);

                        validaHistorico.add(m.dictionaryForm());
                        vocabHistorico.add(vocabulario);

                        vocabValida.add(m.dictionaryForm());
                        vocabCapitulo.add(vocabulario);
                        vocabVolume.add(vocabulario);
                    }
                }
            }
        }
    }

    private Set<String> palavraValida = new HashSet<>();

    public void processarIngles(NovelVolume volume, Callback<Integer[], Boolean> callback) throws ExcessaoBd {
        validaHistorico = new HashSet<>();
        vocabVolume = new HashSet<>();
        palavraValida = new HashSet<>();

        Pattern ignore = Pattern.compile("[\\d]|[^a-zA-Z0-9_'çãáàéèíìúù]");

        Integer[] size = new Integer[2];
        size[0] = 0;
        size[1] = 0;
        volume.getCapitulos().forEach(c -> size[1] += c.getTextos().size());
        for (NovelCapitulo capitulo : volume.getCapitulos()) {
            vocabCapitulo = new HashSet<>();
            vocabValida = new HashSet<>();
            for (NovelTexto texto : capitulo.getTextos()) {
                size[0]++;
                callback.call(size);

                if (texto.getTexto() != null && !texto.getTexto().isEmpty()) {
                    Set<String> palavras = Stream.of(texto.getTexto().toLowerCase().split(" "))
                            .filter(txt -> !txt.trim().contains(" ") && !txt.isEmpty())
                            .collect(Collectors.toSet());

                    for (String palavra : palavras) {
                        if (ignore.matcher(palavra).find())
                            continue;

                        if (validaHistorico.contains(palavra)) {
                            VocabularioExterno vocabulario = vocabHistorico.stream().filter(vocab -> palavra.equalsIgnoreCase(vocab.getPalavra())).findFirst().orElse(null);
                            if (vocabulario != null) {
                                vocabCapitulo.add(vocabulario);
                                vocabVolume.add(vocabulario);
                                continue;
                            }
                        }

                        if (!vocabValida.contains(palavra)) {
                            Vocabulario salvo = vocabularioInglesService.select(palavra);

                            if (salvo != null) {
                                VocabularioExterno vocabulario = new VocabularioExterno(salvo.getId(), palavra, salvo.getPortugues(), true);

                                validaHistorico.add(palavra);
                                vocabHistorico.add(vocabulario);

                                vocabValida.add(palavra);
                                vocabCapitulo.add(vocabulario);
                                vocabVolume.add(vocabulario);
                            } else {

                                if (!palavraValida.contains(palavra.toLowerCase())) {
                                    String valido = serviceInglesRevisar.isValido(palavra);

                                    if (valido == null)
                                        continue;

                                    palavraValida.add(valido);
                                }

                                Revisar revisar = serviceInglesRevisar.select(palavra);
                                if (revisar == null) {
                                    revisar = new Revisar(palavra, false, false, false, true);
                                    Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(palavra + " : Vocabulário novo."));
                                    addLog(palavra + " : Vocabulário novo.");

                                    if (!revisar.getVocabulario().isEmpty()) {
                                        try {
                                            traducoes++;

                                            if (traducoes > 3000) {
                                                traducoes = 0;
                                                MenuPrincipalController.getController()
                                                        .setContaGoogle(Util.next(MenuPrincipalController.getController().getContaGoogle()));
                                            }

                                            Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(palavra + " : Obtendo tradução."));
                                            addLog(palavra + " : Obtendo tradução.");
                                            revisar.setPortugues(Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(),
                                                            revisar.getVocabulario(), MenuPrincipalController.getController().getContaGoogle())));
                                        } catch (IOException e) {
                                            LOGGER.error(e.getMessage(), e);
                                        }
                                    }

                                    serviceInglesRevisar.insert(revisar);
                                    Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(""));
                                } else {
                                    if (!revisar.isNovel()) {
                                        revisar.setNovel(true);
                                        serviceInglesRevisar.setIsNovel(revisar);
                                    }

                                    serviceInglesRevisar.incrementaVezesAparece(revisar.getVocabulario());
                                }

                                VocabularioExterno vocabulario = new VocabularioExterno(revisar.getId(), palavra, revisar.getPortugues(), false);

                                validaHistorico.add(palavra);
                                vocabHistorico.add(vocabulario);

                                vocabValida.add(palavra);
                                vocabCapitulo.add(vocabulario);
                                vocabVolume.add(vocabulario);
                            }
                        }
                    }
                }

                if (desativar)
                    break;
            }

            capitulo.setVocabularios(vocabCapitulo);
            if (desativar)
                break;
        }
        volume.setVocabularios(vocabVolume);
        volume.setProcessado(true);
    }


    public void corrige() {
        // Função de correção ou movimentação de novels para outra tabela. Basta adicionar a condição abaixo na função de select
        //condicao += " AND Table_Name LIKE 'temp_volumes' ";
        try {
            try (Dictionary dict = new DictionaryFactory().create("",
                    SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(Dicionario.FULL))))) {
                tokenizer = dict.create();
                mode = SplitMode.A;

                LOGGER.info("Consultando a correção...");
                List<NovelTabela> lista = serviceNovel.selectTabelas(true, false, null, Language.JAPANESE, null);

                LOGGER.info("Iniciando a correção...");
                for (NovelTabela novel : lista) {
                    String oldBase = novel.getBase();
                    LOGGER.info("Corrigindo a base " + oldBase);
                    for (NovelVolume volume : novel.getVolumes()) {
                        String newBase = getBase(Language.JAPANESE, volume.getTitulo());
                        if (oldBase.equalsIgnoreCase(newBase))
                            continue;

                        LOGGER.info("Corrigindo a novel " + volume.getTitulo());
                        serviceNovel.salvarVolume(newBase, volume);
                        serviceNovel.delete(oldBase, volume);
                        LOGGER.info("Concluido a correção da novel " + volume.getTitulo());
                    }
                }
                LOGGER.info("Concluido a correção das novels.");
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                error = false;
            }
        } catch (Exception ex) {
            LOGGER.error("Erro ao corrigir as listas", ex);
        }
    }

}
