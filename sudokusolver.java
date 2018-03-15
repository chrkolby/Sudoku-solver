import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;



class Sudokusolver {

    public static void main(String[]args){

	File file = null;

	JFileChooser chooser = new JFileChooser();
	FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt");
	chooser.setFileFilter(filter);
	int returnVal = chooser.showOpenDialog(null);
	
	if(returnVal == JFileChooser.APPROVE_OPTION){
	    
	    file = chooser.getSelectedFile();
	}

	else{System.out.println("No file selected");}

	while(!file.getName().endsWith(".txt")){

	    System.out.println("Please select a text file");
	
	    returnVal = chooser.showOpenDialog(null);
	
	    if(returnVal == JFileChooser.APPROVE_OPTION){
		
		file = chooser.getSelectedFile();
	    }
	}
	
	try{
	    Scanner sc = new Scanner(file);
  
	    int rows = sc.nextInt();
	    int columns = sc.nextInt();
  
	    Board b = new Board(rows, columns, sc);
	}

	catch(IOException e){
	    System.out.println(e);
	}
    }
}

class SudokuContainer {

    private ArrayList<char[]> solutions = new ArrayList<char[]>();

    private int count;
   
    public SudokuContainer(){

	count = 0;

    }

    public void insert(Square[][] squares, int size){

	char[] temp = new char[size*size];

	int x = 0;
	for(int i = 0; i < size; i++){
	    for(int j = 0; j < size; j++){
		temp[x] = squares[i][j].myValue;
		x++;
	    }
	}
	solutions.add(temp);
	count++;
    }

    public char[] get(int nr){

	try {
	    return solutions.get(nr);
	}
	catch(IndexOutOfBoundsException e) {
	    System.out.println("Not enough solutions");
	}
	return solutions.get(nr);
    }

    public int getSolutionCount(){
	return count;
    }
}

abstract class Square {

    protected Row myRow;
    protected Box myBox;
    protected Column myColumn;
    protected char myValue;
    protected Board myBoard;

    protected Square next = null;

    public Square(char value){
	myValue = value;
    }
    public abstract void fillBoard();
}


class FilledSquare extends Square{
    public FilledSquare(char value){
	super(value);
    }
    public void fillBoard(){

	if(next != null){
	    next.fillBoard();
	}
	else if(next == null){
	    myBoard.addSolution();
	}
    }
}

class EmptySquare extends Square{

    public EmptySquare(char value){
	super(value);   
    }

    public void fillBoard(){
	for (int i = 1; i <= myRow.size(); i++){
	    if(myBoard.getSolutionCount() >= 750) break;

	    String temp = Integer.toString(i);
	    char value = temp.charAt(0);

	    if(i>9){
		i = i+55;
		value = (char)i;
		i = i-55;
	    }

	    if(validValue(value)){
		myValue = value;
		if(next != null){
		    next.fillBoard();
		}
		else if(next == null){
		    myBoard.addSolution();
		}
		myValue = '.'; 
	    }
	}
    }
   
    public boolean validValue(char value){
	boolean valid = false;
	if(myRow.writeList(value)){
	    if(myColumn.writeList(value)){
		if(myBox.writeList(value)){
		    valid = true;
		}
	    }
	}
	return valid;
    }
}

class Board {

    private SudokuContainer solutions = new SudokuContainer();

    private Square[][] squares;
    private Box[] boxes;
    private Column[] columns;
    private Row[] rows;
    private int size;
       
    public Board(int i, int j, Scanner sc){
	int temp = i*j;
	size = temp;
	squares = new Square[temp][temp];
	rows = new Row[temp];
	columns = new Column[temp];
	boxes = new Box[temp];
	for(int x = 0; x < temp; x++){
	    boxes[x] = new Box();
	}
	createSquares(sc, temp);
	createRows(temp);
	createColumns(temp);
	createBoxes(i,j);
	solve();
	new Window(i, j, solutions);
    } 


    public void solve(){
	squares[0][0].fillBoard();
    }

    public int getSolutionCount(){
	return solutions.getSolutionCount();
    }

    public void createSquares(Scanner sc, int size){
	Square[] temp = new Square[size*size];
	int i = 0;
	while(sc.hasNext()){
	    String line = sc.nextLine();
	    for(int j = 0; j < line.length(); j++){
		char value = line.charAt(j);
		if(value != ' '){
		    System.out.println(line + "     " + value);
		    if(value == '.'){
			temp[i] = new EmptySquare(value);
		    }
		    else{
			temp[i] = new FilledSquare(value);
		    }
		    System.out.println(i);
		    i++;
		}
	    }
	}
	for(int ii = 0; ii < (temp.length-1); ii++){
    
	    temp[ii].next = temp[ii+1];
   
	}
	int j = 0;
	for(int x = 0; x < size; x++){
	    for(int y = 0; y < size; y++){
		squares[x][y] = temp[j];
		squares[x][y].myBoard = this;
		j++;
	    }
	}
    }

    public void addSolution(){
	solutions.insert(squares, size);
    }

    public void createRows(int i){
	for(int x = 0; x < i; x++){
	    rows[x] = new Row();
	    for(int y = 0; y < i; y++){
		rows[x].addList(squares[x][y]);
		squares[x][y].myRow = rows[x];
	    }
	}
    }


    public void createBoxes(int i, int j){
	int temp = i*j;
	for(int x = 0; x < temp; x++){
	    for(int y = 0; y < temp; y++){
		int boxindex = calcboxnumber(x,y,i,j);
		boxes[boxindex].addList(squares[x][y]);
		squares[x][y].myBox = boxes[boxindex];
	    }
	}
    }


    public void createColumns(int i){
	for(int x = 0; x < i; x++){
	    columns[x] = new Column();
	    for(int y = 0; y < i; y++){
		Square s = rows[y].findElement(x);
		columns[x].addList(s);
		s.myColumn = columns[x];
	    }
	}
    }
    public int calcboxnumber( int x, int y, int height, int width) {
	int boxindex = (y/width)+(height*(x/height));
	return boxindex;
    }
}

abstract class BCR{

    private ArrayList<Square> list;

    public BCR(){
 
	list = new ArrayList<Square>();
 
    }

    public int size(){
	return list.size();
    }

    public void addList(Square s){
	list.add(s);
    }

    public Square findElement(int i){
	Square s = list.get(i);
	return s;
    }

    public boolean writeList(char c){
	boolean valid = true;
	Iterator<Square> it = list.iterator();
	while(it.hasNext()){
	    Square s = it.next();
	    if(s.myValue == c){
		valid = false;
	    }
	}
	return valid;
    }
}

class Box extends BCR{
}

class Column extends BCR{
}

class Row extends BCR{
}
   
class Window extends JFrame{

    private JTextField[] squares;
    private int size;
    private int dim;
    private int rows;
    private int columns;
    private SudokuContainer sc;
    private int solutions;
    private int currentSolution = 1;
    private Font font = new Font("SansSerif", Font.BOLD, 15);
    private Font font2 = new Font("SansSerif", Font.BOLD, 10);
    private JTextField show = new JTextField();

    public Window(int i, int j, SudokuContainer s){
	super("Sudoku solver");
	setSize(800,850);

	sc = s;
	dim = i*j;
	size = dim*dim;
	rows = i;
	columns = j;

	solutions = sc.getSolutionCount();

	squares = new JTextField[size];

	Container lerret = getContentPane();
	lerret.setLayout(new BorderLayout());
	JTextField counter = new JTextField();
	String count = Integer.toString(sc.getSolutionCount());
	show.setEditable(false);
	show.setBorder(BorderFactory.createEmptyBorder());
	show.setFont(font2);
	show.setPreferredSize(new Dimension(250,30));
	counter.setText("Number of solutions: " + count);
	counter.setEditable(false);
	counter.setBorder(BorderFactory.createEmptyBorder());
	counter.setFont(font2);

	JPanel frame = addSquares();

	JPanel buttons = addButtons();

	buttons.add(counter);
	buttons.add(show);

	output(currentSolution);

	lerret.add(buttons,BorderLayout.NORTH);
	lerret.add(frame,BorderLayout.CENTER);
	setVisible(true);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public JPanel addButtons(){
	JPanel buttons = new JPanel();
	buttons.setLayout(new FlowLayout());
	JButton next = new JButton("Next");
	JButton prev = new JButton("Previous");
	next.addActionListener(new Button1());
	prev.addActionListener(new Button2());
	buttons.add(prev);
	buttons.add(next);
	return buttons;
    }

    public JPanel addSquares(){
	JPanel frame = new JPanel();

	frame.setLayout(new GridLayout(dim,dim));
	int temp = 0;

	for(int i = 0; i < dim; i++){
	    int top = (i % rows == 0 && i != 0) ? 4 : 1;
	    for(int j = 0; j < dim; j++){
		int left = (j % columns == 0 && j != 0) ? 4 : 1;
		JTextField field = new JTextField();
		field.setText("A");
		field.setEditable(false);
		field.setBorder(BorderFactory.createMatteBorder(top,left,1,1, Color.black));
		field.setHorizontalAlignment(SwingConstants.CENTER);
		field.setPreferredSize(new Dimension(5,5));
		field.setFont(font);
		squares[temp] = field;
		temp++;
		frame.add(field);
	    }
	}

	return frame;
    }

    public void output(int j){	
	if(j == -1){
	    show.setText("No more solutions");
	}
	if(j == -2){
	    show.setText("Already showing first solution");
	}
	else if(j>0){	
	    int temp = j-1;
	    char[] values = sc.get(temp);
	    show.setText("Showing solution number: " + j);
	    for(int i = 0; i < size; i++){
	    String s = Character.toString(values[i]);
	    squares[i].setText(s);
	    }
	}
    }

    class Button1 implements ActionListener{

	public void actionPerformed(ActionEvent ae){

	    if (currentSolution < solutions){
		currentSolution++;
		output(currentSolution);
	    }
	    else if(currentSolution == solutions){
		output(-1);
	    }
	}
    }

    class Button2 implements ActionListener{

	public void actionPerformed(ActionEvent ae){
	    
	    if (currentSolution > 1){
		currentSolution--;
		output(currentSolution);
	    }
	    else if(currentSolution == 1){
		output(-2);
	    }
	}
    }
}