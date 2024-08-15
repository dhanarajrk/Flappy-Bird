import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList; //to store pipes
import java.util.Random;    //to appear pipes at random position

public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 360;
    int boardHeight = 640;

    //Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //Bird position and size
    int birdx = boardWidth/8;
    int birdy = boardHeight/2;
    int birdwidth = 34;
    int birdheight = 24;

    //Create a Bird 
    Bird bird;

    //bird velocity
    //int velocityY = -6; //bird moving upward means -y value.
    int velocityY = 0;
    
    //bird gravity (to have downfall effect)
    //int gravity = 2; //this means it will add +2 to the velocity which will eventually make velocity turns positive value and starts moving downwards. eg -6+2=-4, -4+2=-2, -2+2=0, 0+2=2... moving down Y axis
    int gravity = 1;

    //Pipe positio and size
    int pipex = 360;
    int pipey = 0;       //((360,0) corordinates to start from top right
    int pipewidth = 64;
    int pipeheight = 512;
    //pipe velocity
    int velocityX = -4; //pipes moving left mean -x

    //Timers
    Timer gameLoop;
    Timer pipeTimer;

    //Score
    double score =0;

    //Declare ArrayList reference to store new ArrayList<pipe> objects
    ArrayList<Pipe> pipelist;
    //We need Ramdon to display Pipe positions at random
    Random random = new Random();

    boolean gameover = false;

    class Bird {
        int birdX = birdx;
        int birdY = birdy;
        int birdWidth = birdwidth;
        int birdHeight = birdheight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }

    //Pipes full structure
    class Pipe {
        int pipeX = pipex;
        int pipeY = pipey;
        int pipeWidth = pipewidth;
        int pipeHeight = pipeheight;
        Image img;
        boolean passed = false; //to check if the bird has passed a pipe or not 

        Pipe(Image img){  //when creating a new Pipe object,this constructor will be called with topPipeImg or bottomPipeImg according to what type of Pipe obj we want to create
            this.img = img;
        }
    }

    FlappyBird(){  //This constructor makes overlay panel (inside main frame)
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        //setBackground(Color.blue);
        setFocusable(true); //When a component is focusable, it can receive keyboard input. (which means KeyEvent will work only when the component is focusable)
        addKeyListener(this); //When you want only the current class to handle the Key Events

        //Load Images              
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();  //To convert ImageIcon to Image, use .getImage() at the end;
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        //create a bird
        bird = new Bird(birdImg);

        //Another Gamer timer to repeat Pipes every 1.5 secs
        pipelist = new ArrayList<Pipe>(); //You cannot add anything to ArrayList<Pipe> pipelist; if pipelist = new ArrayList<Pipe>(); is not created. Without initializing pipelist, it does not reference any ArrayList object, and attempting to use it will result in a NullPointerException. OR we can initialize in the global like  ArrayList<Integer> pipelist = new ArrayList<Pipe>();
        pipeTimer = new Timer(1500, new ActionListener(){ //we create a new ActionListener for pipeTimer since the other ActionListener is made for gameloop Timer
            public void actionPerformed(ActionEvent e){
                placePipes();
            }
        }); 
        pipeTimer.start();

        //Game timer (To repeat frames in 1 seconds)
        gameLoop = new Timer(1000/60, this);  //this refers to ActionListener of this class. The ActionListner will execute repaint(); So as soon as the timer starts, it will execute repaint 60 times per second
        gameLoop.start();                      //start timer it will start executing paint 60 times per second. (60 frames in 1000 millisec)
    }

    public void paintComponent(Graphics g){     //paintComponent(Grapgics g) is a function of JPanel. *Note: The method name is fixed since its a method from JPanel
        super.paintComponent(g);  //we need to invoke this component to super of JPanel to let the super(parent class) do this task. This is useful when we want to eg. draw a hollow circle over a component ensuring that circle doesn't cover the entire component making the component's background visible in hollow area of the circle.
        draw(g);
    }

    public void draw(Graphics g){
        //drawing background image 
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        //drawing bird
        g.drawImage(bird.img, bird.birdX, bird.birdY, bird.birdWidth, bird.birdHeight, null);
        //drawing pipes
        for(int i=0; i<pipelist.size(); i++){
            Pipe pipe = pipelist.get(i);  //Store each index(or each pipe) from pipelist in a variable called pipe and draw the "pipe.img" for every iteration
            g.drawImage(pipe.img, pipe.pipeX, pipe.pipeY, pipe.pipeWidth, pipe.pipeHeight, null );
        }
        //draw score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if(gameover){
            g.drawString("Game Over: "+ String.valueOf((int)score), 80, 300);
            g.drawString("Spacebar to Restart", 35, 340);
        }
        else{
            g.drawString(String.valueOf((int)score), 10, 35 );
        }
    }

    public void move(){
        //bird velocity and gravity                     
        velocityY += gravity; 
        bird.birdY += velocityY;   //from the original bird's position, it will add -6 to its Y so that birdY value will reduce and eventually will position upwards
                              //eg. Suppose bird is at y=320, then to move its position upward we need to subtract from 320. (Like 320-6=314)
        bird.birdY = Math.max(bird.birdY, 0); //This will limit the value of birdY at 0 so it it stopes moving out of the screen.    
        
        //pipe velocity
        for(int i=0; i<pipelist.size(); i++){
            Pipe pipe = pipelist.get(i);  //first get i index Pipe and store in pipe variable
            pipe.pipeX += velocityX;  //add velocityX=-4 to each pipe.pipeX to move the pipe left side 

            //score counting
            if(!pipe.passed && bird.birdX > pipe.pipeX + pipewidth){ //if pipe.passed is false and birdX exceeds pipeX+pipeWidth, then set pipe.passed=true and increase the score by 0.5
                pipe.passed = true;
                score += 0.5;       //0.5 because there are 2 pipes(up and bottom opposite to each other). So, 0.5*2=1 for each set of pipes
            }

            //call collision detection:
            if(collision(bird, pipe)){
                gameover = true;
            }
        }

        //game over case
        if(bird.birdY > boardHeight){  //bird exceeds boardheight ie falls down outside frame
            gameover = true;      //if gameover=true,  then stop timer will be executed in actionPerformed(ActionEvent e)
        }
    }       
    
    public void placePipes(){
        
        int ramdomPipeY = (int) (pipey - pipeheight/4 - Math.random()*(pipeheight/2)); //First: pipey - pipeheight/4 will shift up a quarter of pipey position and -Math,random() ranges from (0,1) and it will multiply with pipeheight/2=256 ie(range from 0-256)
                                                                                       //Eg. 0 - 128 - (0-256)--> will give random number if Math.random multiply value is 1
                                                                                       // If Math.random() value is zero, that means it leaves pipeheight/4 iteself --> 0 - 128 - 0 = -128px
        Pipe topPipe = new Pipe(topPipeImg); //create a new topPipe object of type class Pipe with topPipeImg passed in the constructor
        topPipe.pipeY = ramdomPipeY;  //reassgined pipeY of each topPipe with randomPipeY  
        pipelist.add(topPipe);              //then add the created topPipe in the ArrayList
        
        int openSpace = boardHeight/4; //(ie 160px) - space between two pipes to pass the bird.

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.pipeY = topPipe.pipeY + topPipe.pipeHeight + openSpace;  //eg. -128 + 512 + 160 = 544y position for bottom pipe
        pipelist.add(bottomPipe);
    }

    public boolean collision(Bird b, Pipe p){
        return b.birdX < p.pipeX + p.pipeWidth && //b's top left corner doesn't reach p's top right corner
               b.birdX + b.birdWidth > p.pipeX && //b's top right corner doesn't pass p's top left corner
               b.birdY < p.pipeY + p.pipeHeight && //b's top left corner doesn't reach p's bottom left corner
               b.birdY + b.birdHeight > p.pipeY; //b's bottom left corner doesn't reach b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();  //repaint(); will call paintComponent

        if(gameover){
            pipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { //when we use to read Characters from keyboard (will not be used in Flappy Bird)
       
    }

    @Override
    public void keyPressed(KeyEvent e) { //when we use to read any Keys from keyboard 
        if(e.getKeyCode() == KeyEvent.VK_SPACE){      //e.getKeyCode() will read the input KeyCode being pressed on keyboard and it will check if it is same as that of KeyEvent int constants that represents spacebar key (ie.32=VK_SPACE)
            velocityY += -9;  //when we press Spacebard, the bird must move up. Up means -y So it will move up 9px up(not actually 9px up because of gravity +2 will cancel some of the velocityY value)

            //if you want to restart the game by pressing Spacebard, then you have to reset all the values and start the timer again when VK_SPACE is pressed
            if(gameover){
                bird.birdY = birdy;
                velocityY = 0;
                pipelist.clear();
                score = 0;
                gameover = false;
                gameLoop.start();
                pipeTimer.start();
            }
        }
     
    }

    @Override
    public void keyReleased(KeyEvent e) { //when a key is being released (will not be used in Flappy Bird)
        
    }
}
