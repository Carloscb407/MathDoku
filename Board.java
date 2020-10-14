import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.Math;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Board extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    Button undo = new Button("undo");
    Button redo = new Button("redo");
    Button clear_board = new Button("clear board");
    ToggleButton mistakes = new ToggleButton("mistakes");
    ChoiceBox font_size = new ChoiceBox(FXCollections.observableArrayList("Font Size","Small Font","Medium Font","Large Font"));



    public Board_Grid getBoard_grid() {
        return board_grid;
    }
    public void setBoard_grid(Board_Grid board_grid) {
        this.board_grid = board_grid;
    }
    Board_Grid board_grid;

    public Cell[][] getCells() {
        return cells;
    }

    public void setCells(Cell[][] cells) {
        this.cells = cells;
    }

    Cell[][] cells;
    Cell last_cell_pressed;
    Cell undo_redo_cell;
    Cell last_undo_cell;
    int nr_cells_stack=0;
    Stack<String> undo_stack = new Stack<>();
    Stack<String> redo_stack = new Stack<>();
    ArrayList<Stack<String>> stackArrayList = new ArrayList<>();

    boolean show_mistakes = false;

    public Cell getLast_undo_cell() {
        return last_undo_cell;
    }

    public void setLast_undo_cell(Cell last_undo_cell) {
        this.last_undo_cell = last_undo_cell;
    }

    public Stack<String> getUndo_stack() {
        return undo_stack;
    }

    public void setUndo_stack(Stack<String> undo_stack) {
        this.undo_stack = undo_stack;
    }

    public Stack<String> getRedo_stack() {
        return redo_stack;
    }

    public void setRedo_stack(Stack<String> redo_stack) {
        this.redo_stack = redo_stack;
    }

    public Cell getUndo_redo_cell() {
        return undo_redo_cell;
    }

    public void setUndo_redo_cell(Cell undo_redo_cell) {
        this.undo_redo_cell = undo_redo_cell;
    }

    public Cell getLast_cell_pressed() {
        return last_cell_pressed;
    }

    public void setLast_cell_pressed(Cell cell) {
        last_cell_pressed = cell;
    }


    public class Cell extends BorderPane {
        int row_position;
        int column_position;
        Label target_value = new Label();
        TextField textField = new TextField();
        boolean cell_pressed = false;
        Board_Grid board_grid;
        Cage cage;
        Stack<String> cell_undo_stack = new Stack<>();
        Stack<String> cell_redo_stack = new Stack<>();

        public Stack<String> getCell_undo_stack() {
            return cell_undo_stack;
        }

        public void setCell_undo_stack(Stack<String> cell_undo_stack) {
            this.cell_undo_stack = cell_undo_stack;
        }

        public Stack<String> getCell_redo_stack() {
            return cell_redo_stack;
        }

        public void setCell_redo_stack(Stack<String> cell_redo_stack) {
            this.cell_redo_stack = cell_redo_stack;
        }

        public boolean isCell_pressed() {
            return cell_pressed;
        }

        public TextField getTextField() {
            return textField;
        }

        public void setTextField(TextField textField) {
            this.textField = textField;
        }

        public Board_Grid getBoard_grid() {
            return board_grid;
        }

        public void setBoard_grid(Board_Grid board_grid) {
            this.board_grid = board_grid;
        }

        public void setCell_pressed(Cell cell) {
            this.cell_pressed = cell_pressed;
        }

        public int getRow_position() {
            return row_position;
        }

        public void setRow_position(int row_position) {
            this.row_position = row_position;
        }

        public int getColumn_position() {
            return column_position;
        }

        public void setColumn_position(int column_position) {
            this.column_position = column_position;
        }

        public String getTarget_value(){
            return target_value.getText();
        }

        public void setTarget_value(String target_value) {
            this.target_value = new Label(target_value);
            this.setLeft(this.target_value);
        }

        public Cage getCage(){
            return cage;
        }

        public void setCage(Cage cage){
            this.cage = cage;
        }

        public Label getLabel(){
            return target_value;
        }

        public void change_font(int size){
            getTextField().setStyle("-fx-background-color: transparent; -fx-border-color: transparent;-fx-font-size:" + size + ";");
            getLabel().setStyle("-fx-font-size:" + size + ";");
        }

        public Cell(){
            this.setStyle("-fx-border-color: black;");
            setPrefHeight(70);
            setPrefWidth(70);
            this.setLeft(target_value);
            this.setCenter(textField);
            textField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 20;");

            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    if(!newValue.equals("")) {
                        if (!newValue.matches("[1-" + getBoard_grid().getBoard_size() + "]")) {
                            textField.setText("");
                        }
                        else{
                            if(show_mistakes) {
                                if(!cage_Solved(getCage())){
                                    color_cage(getCage());
                                }
                                else{
                                    color_cage_green(getCage());
                                }
                                if(is_duplicate_column(getRow_position(),getColumn_position())){
                                    color_column(getColumn_position());
                                }
                                else {
                                    uncolor_column(getColumn_position());
                                    if (is_duplicate_row(getRow_position(), getColumn_position())) {
                                        color_row(getRow_position());
                                    } else {
                                        uncolor_row(getRow_position());
                                    }
                                    for(Cage cage : board_grid.getCage_ArrayList()) {
                                        if (!cage_Solved(getCage())) {
                                            color_cage(cage);
                                        } else {
                                            color_cage_green(cage);
                                        }
                                    }
                                    for(int i=0;i<board_grid.getBoard_size();i++){
                                        for(int j=0;j<board_grid.getBoard_size();j++){
                                            if(is_duplicate_column(cells[i][j].getRow_position(),cells[i][j].getColumn_position())){
                                                color_column(cells[i][j].getColumn_position());
                                            }
                                            if(is_duplicate_row(cells[i][j].getRow_position(),cells[i][j].getColumn_position())){
                                                color_row(cells[i][j].getRow_position());
                                            }
                                        }
                                    }
                                }
                                if (is_duplicate_row(getRow_position(), getColumn_position())) {
                                    color_row(getRow_position());
                                } else {
                                    uncolor_column(getColumn_position());
                                    uncolor_row(getRow_position());
                                    for(Cage cage : board_grid.getCage_ArrayList()) {
                                        if (!cage_Solved(getCage())) {
                                            color_cage(cage);
                                        } else {
                                            color_cage_green(cage);
                                        }
                                    }
                                    for(int i=0;i<board_grid.getBoard_size();i++){
                                        for(int j=0;j<board_grid.getBoard_size();j++){
                                            if(is_duplicate_column(cells[i][j].getRow_position(),cells[i][j].getColumn_position())){
                                                color_column(cells[i][j].getColumn_position());
                                            }
                                            if(is_duplicate_row(cells[i][j].getRow_position(),cells[i][j].getColumn_position())){
                                                color_row(cells[i][j].getRow_position());
                                            }
                                        }
                                    }
                                }

                            }
                            else{
                                uncolor_row_column();
                            }
                        }
                    }
                }
            });

            textField.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    setLast_cell_pressed(cells[getRow_position()][getColumn_position()]);

                    getTextField().setOnKeyTyped(new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent keyEvent) {
                            for(int i=0;i<board_grid.getBoard_size();i++){
                                for(int j=0;j<board_grid.getBoard_size();j++){
                                    cells[i][j].getCell_undo_stack().push(cells[i][j].getTextField().getText());
                                }
                            }
                            if(getCell_undo_stack().size() <= 1){
                                undo.setDisable(true);
                            } else {
                                undo.setDisable(false);
                            }
                            if(getCell_redo_stack().size() <1){
                                redo.setDisable(true);
                            } else{
                                redo.setDisable(false);
                            }
                        }
                    });

                }
            });
        }
    }

    public class Cage extends GridPane{
        Board_Grid board_grid;
        String label;
        String cells_positions;
        ArrayList<Cell> cage_cells = new ArrayList<>();

        public String getCells_positions() {
            return cells_positions;
        }

        public void setCells_positions(String cells_positions) {
            this.cells_positions = cells_positions;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public ArrayList<Cell> getCage_cells() {
            return cage_cells;
        }

        public void setCage_cells(ArrayList<Cell> cage_cells) {
            this.cage_cells = cage_cells;
        }

        public Board_Grid getBoard_grid() {
            return board_grid;
        }

        public void setBoard_grid(Board_Grid board_grid) {
            this.board_grid = board_grid;
        }

        public Cage(String label,String cells_positions){
            String right_border_width = "4";
            String top_border_width = "4";
            String bottom_border_width = "4";
            String left_border_width = "4";
            this.label=label;
            this.cells_positions = cells_positions;
            String[] cell_position = cells_positions.split(",");

            for(int i = 0 ; i < cell_position.length ; i++){
                int str = Integer.parseInt(cell_position[i]) - 1;
                cell_position[i] = String.valueOf(str);
            }


            for(String position : cell_position){
                cage_cells.add(cells[Integer.parseInt(position)/cells.length][Integer.parseInt(position)%cells.length]);
            }

            /**
             * checks if it s the first cell in the cage
             * checks if it has neighbours]
             * makes border thicker where there are no neighbours
             */
            for(Cell cell : cage_cells){
                cell.setCage(this);

                if(cage_cells.get(0)== cell){
                    cell.setTarget_value(label);
                }

                for(Cell neighbour : cage_cells) {
                    if(neighbour != cell) {
                        if (neighbour.getColumn_position() == cell.getColumn_position() - 1 && neighbour.getRow_position() == cell.getRow_position()) {
                            left_border_width = "1";
                        }
                        if (neighbour.getColumn_position() == cell.getColumn_position() + 1 && neighbour.getRow_position() == cell.getRow_position()) {
                            right_border_width = "1";
                        }
                        if (neighbour.getRow_position() == cell.getRow_position() - 1 && neighbour.getColumn_position() == cell.getColumn_position()) {
                            top_border_width = "1";
                        }
                        if (neighbour.getRow_position() == cell.getRow_position() + 1 && neighbour.getColumn_position() == cell.getColumn_position()) {
                            bottom_border_width = "1";
                        }
                        cell.setStyle("-fx-border-width: " + top_border_width + " " + right_border_width + " " + bottom_border_width + " " + left_border_width + " ; -fx-border-color: black; -fx-background-color: white");
                    }

                }
                right_border_width = "4";
                top_border_width = "4";
                bottom_border_width = "4";
                left_border_width = "4";
                System.out.println();
            }
        }
    }


    public boolean is_numerical(String string){
        if(string.matches("[0-9]")){
            return true;
        }
        return false;
    }

    public boolean cage_Solved(Cage cage){
        String target = cage.getCage_cells().get(0).getTarget_value();
        int mult=1,add=0;
        ArrayList<Integer> div_subs_values = new ArrayList<>() ;
        for(Cell cell : cage.getCage_cells()){
            if (target.endsWith("x")) {
                mult *= Integer.parseInt(cell.getTextField().getText());
            }
            else if (target.endsWith("+")) {
                add += Integer.parseInt(cell.getTextField().getText());
            }
            else if (target.endsWith("-")) {
                div_subs_values.add(Integer.parseInt(cell.getTextField().getText()));
            }
            else if (target.endsWith("÷") || target.endsWith("�")) {
                div_subs_values.add(Integer.parseInt(cell.getTextField().getText()));
            }
            else if(target.equals(cell.getTextField().getText())){
                return true;
            }
        }
        int value = Integer.parseInt(target.substring(0,target.length()-1));
        int[] values = new int[div_subs_values.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = div_subs_values.get(i);
        }
        Arrays.sort(values);
        if(target.endsWith("÷") || target.endsWith("�")) {
            int div = values[values.length - 1];
            for (int j = values.length - 2; j >= 0; j--) {
                div = div/values[j];
            }
            if((target.endsWith("÷") || target.endsWith("�")) && value == div){
                return true;
            }
        }
        if(target.endsWith("-")) {
            int subs = values[values.length - 1];
            for (int j = values.length - 2; j >= 0; j--) {
                subs -= values[j];
            }
            if(target.endsWith("-") && value == subs){
                return true;
            }
        }

        if(target.endsWith("x") && value == mult){
            return true;
        }
        if(target.endsWith("+") && value == add){
            return true;
        }


        return false;
    }

    public void color_cage(Cage cage){
        for(Cell cell : cage.getCage_cells()){
            cell.setBackground(new Background(new BackgroundFill(Color.LIGHTSALMON, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public void color_cage_green(Cage cage){
        for(Cell cell : cage.getCage_cells()){
            cell.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public void uncolor_row_column(){
        for(int i=0;i<cells.length;i++){
            for(int j=0;j<cells.length;j++){
                cells[i][j].setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    public void randomColor_row_column(){
        for(int i=0;i<cells.length;i++){
            for(int j=0;j<cells.length;j++){
                cells[i][j].setBackground(new Background(new BackgroundFill(Color.color(Math.random(),Math.random(),Math.random()), CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    public void purple_row_column(){
        for(int i=0;i<cells.length;i++){
            for(int j=0;j<cells.length;j++){
                cells[i][j].setBackground(new Background(new BackgroundFill(Color.PURPLE, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    public void uncolor_row(int row){
        for(int i=0;i<cells.length;i++){
            cells[row][i].setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public void color_row(int row){
        for(int i=0;i<cells.length;i++){
            cells[row][i].setBackground(new Background(new BackgroundFill(Color.LIGHTSALMON, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public void uncolor_column(int column){
        for(int i=0;i<cells.length;i++){
            cells[i][column].setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public void color_column(int column){
        for(int i=0;i<cells.length;i++){
            cells[i][column].setBackground(new Background(new BackgroundFill(Color.LIGHTSALMON, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public boolean is_duplicate_row(int row,int column){
        for(int i=0;i<cells.length;i++){
            if(i != column) {
                if(!cells[row][i].getTextField().getText().equals("")) {
                    if (Integer.parseInt(cells[row][i].getTextField().getText()) == Integer.parseInt(cells[row][column].getTextField().getText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean is_duplicate_column(int row,int column){
        for(int i=0;i<cells.length;i++){
            if(i != row) {
                if(!cells[row][i].getTextField().getText().equals("")) {
                    if (Integer.parseInt(cells[i][column].getTextField().getText()) == Integer.parseInt(cells[row][column].getTextField().getText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasDuplicatesInRows()
    {
        for (int row = 0; row < cells.length; row++)
        {
            for (int col = 0; col < cells.length; col++)
            {
                int num = Integer.parseInt(cells[row][col].getTextField().getText());
                for (int otherCol = col + 1; otherCol < cells.length; otherCol++)
                {
                    if (num == Integer.parseInt(cells[row][otherCol].getTextField().getText()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean hasDuplicatesInColumn()
    {
        for (int col = 0; col < cells.length; col++)
        {
            for (int row = 0; row < cells.length; row++)
            {
                int num = Integer.parseInt(cells[row][col].getTextField().getText());
                for (int otherRow = row + 1; otherRow < cells.length; otherRow++)
                {
                    if (num == Integer.parseInt(cells[otherRow][col].getTextField().getText()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean allCages_solved(Board_Grid board_grid){
        for(Cage cage : board_grid.getCage_ArrayList()){
            if(!cage_Solved(cage)){
                return false;
            }
        }
        return true;
    }

    public boolean board_solved(Board_Grid board_grid){
        if(allCages_solved(board_grid) && !hasDuplicatesInColumn() && !hasDuplicatesInRows()){
            return true;
        }
        return false;
    }

    public void game_won(Board_Grid board_grid){
        if(board_solved(board_grid)){

            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(this::randomColor_row_column, 0, 1, TimeUnit.SECONDS);

            RotateTransition rotateTransition = new RotateTransition();
            rotateTransition.setDuration(Duration.millis(2000));
            rotateTransition.setNode(board_grid);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(4);
            rotateTransition.setAutoReverse(false);

            TranslateTransition translate = new TranslateTransition();
            translate.setByX(400);
            translate.setDuration(Duration.millis(2000));
            translate.setCycleCount(4);
            translate.setAutoReverse(true);
            translate.setNode(board_grid);

            ParallelTransition parallelTransition = new ParallelTransition(rotateTransition,translate);
            parallelTransition.play();

            Alert win = new Alert(Alert.AlertType.INFORMATION);
            win.setHeaderText("You Win!");
            win.showAndWait();
            executorService.shutdownNow();
        }
    }

    public class Board_Grid extends GridPane{
        ArrayList<Cage> cage_ArrayList = new ArrayList<>();
        int board_size;
        Button numpad_button;
        ArrayList<Button> numpad = new ArrayList<>();
        GridPane num_gridpane = new GridPane();

        public ArrayList<Cage> getCage_ArrayList() {
            return cage_ArrayList;
        }

        public void setCage_ArrayList(ArrayList<Cage> cage_ArrayList) {
            this.cage_ArrayList = cage_ArrayList;
        }

        public GridPane getNum_gridpane() {
            return num_gridpane;
        }

        public int getBoard_size() {
            return board_size;
        }

        public void setBoard_size(int board_size) {
            this.board_size = board_size;
        }


        public Cell getCell(int position) {
            return cells[position/getBoard_size()][position%getBoard_size()];
        }

        public void create_board(int board_size){
            for(int i = 0 ; i < board_size; i++){
                for(int j = 0 ;j < board_size ; j++){
                    this.add(cells[i][j],j,i);
                    cells[i][j].setRow_position(i);
                    cells[i][j].setColumn_position(j);
                    cells[i][j].setBoard_grid(this);
                    setHgrow(cells[i][j],Priority.ALWAYS);
                    setVgrow(cells[i][j],Priority.ALWAYS);
                    setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
                }
            }

            for(Cage cage : this.getCage_ArrayList()) {
                String right_border_width = "4";
                String top_border_width = "4";
                String bottom_border_width = "4";
                String left_border_width = "4";
                for (Cell cell : cage.getCage_cells()) {
                    if (cage.getCage_cells().get(0) == cell) {
                        cell.setTarget_value(cage.getLabel());
                    }

                    for (Cell neighbour : cage.getCage_cells()) {
                        if (neighbour != cell) {
                            if (neighbour.getColumn_position() == cell.getColumn_position() - 1 && neighbour.getRow_position() == cell.getRow_position()) {
                                left_border_width = "1";
                            }
                            if (neighbour.getColumn_position() == cell.getColumn_position() + 1 && neighbour.getRow_position() == cell.getRow_position()) {
                                right_border_width = "1";
                            }
                            if (neighbour.getRow_position() == cell.getRow_position() - 1 && neighbour.getColumn_position() == cell.getColumn_position()) {
                                top_border_width = "1";
                            }
                            if (neighbour.getRow_position() == cell.getRow_position() + 1 && neighbour.getColumn_position() == cell.getColumn_position()) {
                                bottom_border_width = "1";
                            }
                            cell.setStyle("-fx-border-width: " + top_border_width + " " + right_border_width + " " + bottom_border_width + " " + left_border_width + " ; -fx-border-color: black; -fx-background-color: white");
                        }

                    }
                    right_border_width = "4";
                    top_border_width = "4";
                    bottom_border_width = "4";
                    left_border_width = "4";
                    System.out.println();
                }
            }
        }
        public Board_Grid(int board_size, ArrayList<Cage> cages) {
            this.cage_ArrayList = cages;
            this.board_size = board_size;
            setPadding(new Insets(5,40,40,40));
        }
    }

    public GridPane make_numpad(){
        GridPane num_gridpane = new GridPane();
        ArrayList<Button> numpad = new ArrayList<>();
        int i;
        for(i = 0 ; i < board_grid.getBoard_size();i++){
            Button numpad_button = new Button(String.valueOf(i+1));
            numpad_button.getStyleClass().add("num-button");
            numpad.add(numpad_button);
            GridPane.setVgrow(numpad_button,Priority.ALWAYS);
            GridPane.setHgrow(numpad_button,Priority.ALWAYS);
            numpad_button.setMaxWidth(Control.USE_PREF_SIZE);
            num_gridpane.add(numpad_button,i % 3 , (int) Math.ceil(i / 3));
        }
        Button delete = new Button("<");
        numpad.add(delete);
        num_gridpane.add(delete,i % 3 , (int) Math.ceil(i / 3));

        for(Button button : numpad){
            button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if(button.getText().equals("<")){
                        getLast_cell_pressed().getTextField().setText("");
                    }
                    else {
                        getLast_cell_pressed().getTextField().setText(button.getText());
                        for(int i=0;i<board_grid.getBoard_size();i++){
                            for(int j=0;j<board_grid.getBoard_size();j++){
                                cells[i][j].getCell_undo_stack().push(cells[i][j].getTextField().getText());
                            }
                        }
                        if(cells[0][0].getCell_undo_stack().size() <= 1){
                            undo.setDisable(true);
                        } else {
                            undo.setDisable(false);
                        }
                        if(cells[0][0].getCell_redo_stack().size() <1){
                            redo.setDisable(true);
                        } else{
                            redo.setDisable(false);
                        }
                    }
                }
            });
        }
        return num_gridpane;
    }

    private int nr_of_fileCells(ArrayList<Cage> cages){
        int nr_of_fileCells = 0;
        for(Cage cage : cages){
            String[] cageCells =cage.getCells_positions().split(",");
            nr_of_fileCells += cageCells.length;
        }
        return nr_of_fileCells;
    }


    public Board_Grid make_boardGrid(TextArea area){
        int size = 0;
        ArrayList<Cage> cageArrayList = new ArrayList<>();

        for(String line : area.getText().split("\\n")){
            String[] cage_info = line.split("\\s+");
            String cage_value = cage_info[0];
            String cell_positions = cage_info[1];
            String[] nrofCellsInCage = cell_positions.split(",");
            size += nrofCellsInCage.length;
        }
        size = (int) Math.sqrt(size);
        cells = new Cell[size][size];


        for(String line : area.getText().split("\\n")){
            String[] cage_info = line.split("\\s+");
            String cage_value = cage_info[0];
            String cell_positions = cage_info[1];
            String[] nrofCellsInCage = cell_positions.split(",");
            for(String string : nrofCellsInCage){
                int i = (Integer.parseInt(string) -1)/cells.length;
                int j = (Integer.parseInt(string) -1)%cells.length;
                cells[i][j] = new Cell();
            }
            Cage cage = new Cage(cage_value,cell_positions);
            for(String string : nrofCellsInCage){
                int i = (Integer.parseInt(string) -1)/cells.length;
                int j = (Integer.parseInt(string) -1)%cells.length;
                cells[i][j].setCage(cage);
            }
            cageArrayList.add(cage);
        }
        return new Board_Grid(size,cageArrayList);
    }


    public void clear_textArea(TextArea textArea){
        textArea.setText("");
    }

    @Override
    public void start(Stage primaryStage) {
        font_size.setValue("Font Size");

        HBox buttons_hbox = new HBox();
        buttons_hbox.getChildren().addAll(undo,redo,clear_board,font_size,mistakes);

        buttons_hbox.setPadding(new Insets(30,20,10,20));
        buttons_hbox.setSpacing(20);
        buttons_hbox.setFillHeight(true);


        undo.setMaxWidth(Control.USE_PREF_SIZE);
        redo.setMaxWidth(Control.USE_PREF_SIZE);
        clear_board.setMaxWidth(Control.USE_PREF_SIZE);
        font_size.setMaxWidth(Control.USE_PREF_SIZE);
        mistakes.setMaxWidth(Control.USE_PREF_SIZE);


        HBox.setHgrow(undo, Priority.ALWAYS);
        HBox.setHgrow(redo, Priority.ALWAYS);
        HBox.setHgrow(clear_board, Priority.ALWAYS);
        HBox.setHgrow(font_size, Priority.ALWAYS);
        HBox.setHgrow(mistakes, Priority.ALWAYS);


        BorderPane border = new BorderPane();
        border.setPadding(new Insets(10));

        Label menu_label = new Label("Choose or write your game format!");
        TextArea area = new TextArea();
        border.setCenter(area);

        Button choose_file = new Button("Choose File");
        Button play_game = new Button("Play game!");
        Button clear_text_area = new Button("Clear Text Area");

        HBox menu_butttons = new HBox(choose_file,clear_text_area,play_game);
        menu_butttons.setPadding(new Insets(10,10,10,10));
        menu_butttons.setSpacing(10);
        choose_file.setMaxWidth(Control.USE_PREF_SIZE);
        play_game.setMaxWidth(Control.USE_PREF_SIZE);
        clear_text_area.setMaxWidth(Control.USE_PREF_SIZE);
        HBox.setHgrow(clear_text_area, Priority.ALWAYS);
        HBox.setHgrow(choose_file, Priority.ALWAYS);
        HBox.setHgrow(play_game, Priority.ALWAYS);

        border.setTop(menu_label);
        border.setBottom(menu_butttons);
        BorderPane.setAlignment(menu_butttons, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(menu_butttons, new Insets(10, 0, 0, 0));
        Scene intro_menu = new Scene(border,500,500);

        choose_file.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //area.setText("");
                clear_textArea(area);
                FileChooser fileChooser = new FileChooser();

                fileChooser.setTitle("Open File to Load");
                FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text files",
                        "*.txt");
                fileChooser.getExtensionFilters().add(txtFilter);

                File file = fileChooser.showOpenDialog(primaryStage);

                if (file != null && file.exists() && file.canRead()) {
                    try {
                        BufferedReader buffered = new BufferedReader(
                                new FileReader(file));
                        String line;
                        while ((line = buffered.readLine()) != null) {
                            area.appendText(line + "\n");
                        }
                        buffered.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

        });

        play_game.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Alert play_alert = new Alert(Alert.AlertType.INFORMATION);
                play_alert.setHeaderText("Are you sure you want to play the game in this format?");
                ButtonType cancel_button = new ButtonType("Cancel");
                play_alert.getButtonTypes().addAll(cancel_button);
                Optional<ButtonType> result = play_alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    setBoard_grid(make_boardGrid(area));
                    board_grid.create_board(board_grid.getBoard_size());
                    undo.setDisable(cells[0][0].getCell_undo_stack().size() <= 1);
                    redo.setDisable(cells[0][0].getCell_redo_stack().size() < 1);
                    buttons_hbox.getChildren().add(make_numpad());
                    VBox vbox = new VBox();
                    vbox.getChildren().addAll(buttons_hbox,board_grid);
                    board_grid.setAlignment(Pos.CENTER);
                    buttons_hbox.setAlignment(Pos.TOP_CENTER);
                    vbox.setFillWidth(true);
                    VBox.setVgrow(board_grid,Priority.ALWAYS);
                    vbox.maxHeightProperty().bind( vbox.heightProperty());
                    vbox.maxWidthProperty().bind( vbox.widthProperty());

                    Scene game_scene = new Scene(vbox,700,700);
                    primaryStage.setScene(game_scene);
                } else if (result.get() == cancel_button){
                    play_alert.close();
                }
            }
        });

        clear_text_area.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                clear_textArea(area);
            }
        });

        font_size.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(font_size.getValue().equals("Small Font")){
                    for(int i = 0;i< board_grid.getBoard_size();i++){
                        for(int j=0;j< board_grid.getBoard_size();j++){
                            cells[i][j].change_font(10);
                        }
                    }
                }
                if(font_size.getValue().equals("Medium Font")){
                    for(int i = 0;i< board_grid.getBoard_size();i++){
                        for(int j=0;j< board_grid.getBoard_size();j++){
                            cells[i][j].change_font(20);
                        }
                    }
                }
                if(font_size.getValue().equals("Large Font")){
                    for(int i = 0;i< board_grid.getBoard_size();i++){
                        for(int j=0;j< board_grid.getBoard_size();j++){
                            cells[i][j].change_font(30);
                        }
                    }
                }
            }
        });

        redo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for(int i=0;i<board_grid.getBoard_size();i++){
                    for(int j=0;j<board_grid.getBoard_size();j++){
                        cells[i][j].getTextField().setText(cells[i][j].getCell_redo_stack().peek());
                        cells[i][j].getCell_undo_stack().push(cells[i][j].getCell_redo_stack().pop());

                    }
                }
                if(cells[0][0].getCell_undo_stack().size() <=1){
                    undo.setDisable(true);
                } else{
                    undo.setDisable(false);
                }
                if(cells[0][0].getCell_redo_stack().size() <1){
                    redo.setDisable(true);
                } else{
                    redo.setDisable(false);
                }
            }
        });

        undo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for(int i=0;i<board_grid.getBoard_size();i++){
                    for(int j=0;j<board_grid.getBoard_size();j++){
                        cells[i][j].getCell_redo_stack().push(cells[i][j].getCell_undo_stack().pop());
                        cells[i][j].getTextField().setText(cells[i][j].getCell_undo_stack().peek());
                    }
                }
                if(cells[0][0].getCell_undo_stack().size() <=1){
                    undo.setDisable(true);
                } else{
                    undo.setDisable(false);
                }
                if(cells[0][0].getCell_redo_stack().size() <1){
                    redo.setDisable(true);
                } else{
                    redo.setDisable(false);
                }
            }
        });

        mistakes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(mistakes.isSelected()){
                    show_mistakes = true;
                    mistakes.setStyle("-fx-base: red;");

                    for(Cage cage : board_grid.getCage_ArrayList()){
                        if(cage_Solved(cage)){
                            color_cage_green(cage);
                        }
                        else {
                            color_cage(cage);
                        }

                    }
                    for(int i=0;i<board_grid.getBoard_size();i++){
                        for(int j=0;j<board_grid.getBoard_size();j++){
                            if(is_duplicate_column(cells[i][j].getRow_position(),cells[i][j].getColumn_position())){
                                color_column(cells[i][j].getColumn_position());
                            }
                            if(is_duplicate_row(cells[i][j].getRow_position(),cells[i][j].getColumn_position())){
                                color_row(cells[i][j].getRow_position());
                            }
                        }
                    }
                    if(allCages_solved(board_grid)){
                        game_won(board_grid);
                    }
                }
                else{
                    uncolor_row_column();
                    show_mistakes = false;
                    mistakes.setStyle("-fx-base: ghostwhite;");
                }
            }
        });

        clear_board.setOnAction(e -> { Alert a = new Alert(Alert.AlertType.WARNING);
            a.setHeaderText("Are you sure you want to clear the board?");
            Optional<ButtonType> result = a.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                for(int i=0;i<board_grid.getBoard_size();i++){
                    for(int j=0;j<board_grid.getBoard_size();j++){
                        cells[i][j].getTextField().clear();
                    }
                }
                uncolor_row_column();
            }});


        primaryStage.setScene(intro_menu);
        primaryStage.setTitle("Mathdoku");
        primaryStage.show();

    }
}