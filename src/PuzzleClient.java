import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
* Client class that sets up the window and adds the puzzle board to it.
*/
public class PuzzleClient {
   
   public static void main(String[] args) {
      JFrame frame = new JFrame("Jigsaw Puzzle");
      
      frame.setLayout(null);
      
      JigsawPuzzle puzzleFrame = new JigsawPuzzle(frame, 100);
      frame.add(puzzleFrame);
      
      GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Rectangle bounds = environment.getMaximumWindowBounds(); 
      frame.setSize((int)bounds.getWidth(), (int)bounds.getHeight());
      
      frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
      frame.setUndecorated(false);
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.setVisible(true);
   }
}