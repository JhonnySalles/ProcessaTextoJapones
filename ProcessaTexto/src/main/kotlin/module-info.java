module br.com.fenix.processatexto {

    requires java.naming;
    requires transitive java.sql;
    requires transitive com.jfoenix;
    requires javafx.base;
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires kuromoji.core;
    requires kuromoji.ipadic;
    requires transitive sudachi;
    requires AnimateFX;
    requires transitive org.controlsfx.controls;
    requires org.jsoup;
    requires com.google.common;
    requires nativejavafx.taskbar;
    requires javafx.web;
    requires com.google.gson;
    requires org.json;
    requires java.desktop;
    requires java.base;
    requires junrar;
    requires org.apache.commons.compress;
    requires java.xml.bind;
    requires Mal4J;
    requires java.net.http;
    requires org.slf4j;
    requires logback.classic;
    requires logback.core;
    requires mp3agic;
    requires firebase.admin;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    requires google.cloud.core;
    requires google.cloud.firestore;
    requires com.google.api.apicommon;
    requires kotlin.stdlib;
    requires org.hibernate.orm.core;
    requires org.flywaydb.core;
    requires org.jooq;
    requires annotations;
    requires jakarta.persistence;
    requires jakarta.transaction;

    exports br.com.fenix.processatexto;
    exports br.com.fenix.processatexto.logback;
    exports br.com.fenix.processatexto.model.entities;

    opens br.com.fenix.processatexto to javafx.fxml;
    opens br.com.fenix.processatexto.model.entities to org.hibernate.orm.core;
    opens br.com.fenix.processatexto.model.entities.comicinfo to org.hibernate.orm.core;
    opens br.com.fenix.processatexto.model.entities.mangaextractor to org.hibernate.orm.core;
    opens br.com.fenix.processatexto.model.entities.novelextractor to org.hibernate.orm.core;
    opens br.com.fenix.processatexto.model.entities.processatexto to org.hibernate.orm.core;
    opens br.com.fenix.processatexto.model.entities.subtitle to org.hibernate.orm.core;
}