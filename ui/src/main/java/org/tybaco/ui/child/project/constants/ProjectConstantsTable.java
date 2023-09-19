package org.tybaco.ui.child.project.constants;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import org.tybaco.ui.lib.control.Tables;
import org.tybaco.ui.lib.text.Texts;
import org.tybaco.ui.model.Constant;
import org.tybaco.ui.model.Project;

import java.util.List;

@Component
public class ProjectConstantsTable extends TableView<Constant> {

  public ProjectConstantsTable(Project project) {
    super(project.constants);
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    setEditable(true);
    getColumns().addAll(List.of(idColumn(), nameColumn(), factoryColumn(), valueColumn()));
    Tables.initColumnWidths(this, 40, 150, 150, 200);
  }

  private TableColumn<Constant, Number> idColumn() {
    var col = new TableColumn<Constant, Number>("Id");
    col.setEditable(false);
    col.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id));
    return col;
  }

  private TableColumn<Constant, String> nameColumn() {
    var col = new TableColumn<Constant, String>();
    col.textProperty().bind(Texts.text("Name"));
    col.setEditable(true);
    col.setCellValueFactory(c -> c.getValue().name);
    return col;
  }

  private TableColumn<Constant, String> factoryColumn() {
    var col = new TableColumn<Constant, String>();
    col.textProperty().bind(Texts.text("Factory"));
    col.setEditable(false);
    col.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().factory));
    return col;
  }

  private TableColumn<Constant, String> valueColumn() {
    var col = new TableColumn<Constant, String>();
    col.textProperty().bind(Texts.text("Value"));
    col.setEditable(true);
    col.setCellValueFactory(c -> c.getValue().value);
    return col;
  }
}
