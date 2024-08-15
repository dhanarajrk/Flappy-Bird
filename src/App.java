import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
       int boardWidth = 360;
       int boardHeight = 640; //Main window dimensions

       JFrame frame = new JFrame("Fappy Bird");
       //frame.setVisible(true);
       frame.setSize(boardWidth, boardHeight); //we chose this size because the background img file has 360px X 640px dimension
       frame.setLocationRelativeTo(null); 
       frame.setResizable(false);
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       FlappyBird flappyBird = new FlappyBird();  //FlappyBird's class constructor will run automatically
       frame.add(flappyBird); //initialize/add the panel to Main frame
       frame.pack();   //Apply the panel excluding title bar area.
       flappyBird.requestFocus(); //To redirect KeyEvent focus on this component -already knowing that FlappyBird class/component is setFocusable(true) to allow focus on it.
       frame.setVisible(true);
    }
}
