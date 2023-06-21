import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.image.*;

/**
* This class manages the puzzle board and the generation of all the puzzle pieces.
* It also checks for completion of the puzzle every time a piece is fused to another.
* @author Noah Brown
*/
public class JigsawPuzzle extends JPanel {
  /**
   * The global piece array.
   */
   private Piece[][] pieces;
  /**
   * The current row the piece generator is on.
   */
   private int genRow;
  /**
   * The current column the piece generator is on.
   */
   private int genCol;

  /**
   * Constructs the puzzle itself. Adds all the pieces to the board and generally manages everything.
   * @param frame the JFrame object initialized in the client class
   * @param pieceCount how many pieces the puzzle should have. Currently set at 220.
   */
   public JigsawPuzzle(JFrame frame, int pieceCount) {
      GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Rectangle bounds = environment.getMaximumWindowBounds(); 
      frame.getContentPane().setSize((int)bounds.getWidth(), (int)bounds.getHeight());
      frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
      genRow = 0;
      genCol = 0;
      
      pieces = new Piece[11][20];
      File puzzleImage = new File(System.getProperty("user.dir") + "/Resources/puzzleImages/TheWhitePeople.png");
      ImageSplitter imageSplitter = new ImageSplitter(puzzleImage, 11, 20);
      imageSplitter.purgePieceImages(new File(System.getProperty("user.dir") + "/Resources/"));
      imageSplitter.splitImageBuffer();
      generatePieces(frame, pieceCount);
      setPieceImages();
     
      try {
         imageSplitter.massMaskPieces(pieces);
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      scatterPieces(pieceCount);
      
   }
   
  /**
   * Calls each method to generate puzzle pieces around the edge of the board.
   * @param frame the JFrame object initialized in the client class
   * @param pieceCount how many pieces the puzzle should have. Currently set at 220.
   */
   public void generatePieces(JFrame frame, int pieceCount) {
      int frameWidth = frame.getContentPane().getWidth();
      int frameHeight = frame.getContentPane().getHeight();
      int id = generateHorizontal(frame, frameWidth, frameHeight, true, 0);
      id = generateHorizontal(frame, frameWidth, frameHeight, false, id);
      id = generateVertical(frame, frameWidth, frameHeight, true, id);
      id = generateVertical(frame, frameWidth, frameHeight, false, id);
      
      for (int i = 0; i < pieces.length; i++) {
         for (int j = 0; j < pieces[0].length; j++) {
            getAdjacentPieces(pieces[i][j]);
         }
      }
   }
   
  /**
   * Once every piece has had its image generated, pass every piece in the global array
   * the image in the image folder that matches its row and column in the global array.
   */
   public void setPieceImages() {
      for (int i = 0; i < pieces.length; i++) {
         for (int j = 0; j < pieces[0].length; j++) {
            BufferedImage pieceImage = null;
            try {
               File pieceImageFile = new File(System.getProperty("user.dir") + "/Resources/piece" + i + "-" + j + ".png");
               pieceImage = ImageIO.read(pieceImageFile);
            } catch (IOException e) {
               e.printStackTrace();
            }
            pieces[i][j].setImage(pieceImage);
         }
      }      
   }
   
  /**
   * Generates pieces along the top and bottom of the screen.
   * @param frame the window itself
   * @param frameWidth the width of the application window
   * @param frameHeight the height of the application window
   * @param top whether to generate pieces at the top or bottom of the application window
   * @param pieceID the number of pieces that have been generated at the time of the method call
   * @return the number of pieces that have been generated at the time the method call finishes running.
   * This is used as the starting ID for the next time a piece generation method is called.
   */
   public int generateHorizontal(JFrame frame, int frameWidth, int frameHeight, boolean top, int pieceID) {
      int ypos = 0;
      
      for (int rows = 0; rows < 3; rows++) {
         if (top) {
            ypos = (rows*35 + (15*(rows + 1)));
         } else {
            ypos = frameHeight - 20 - ((rows+1)*50);
         }
         
         int col = 0; 
         while ((col*35 + 10*col) < frameWidth - 40) {
            Piece puzzlePiece = new Piece(this, (col*35 + ((col+1)*10)), ypos, 35, 35, pieceID);
            frame.add(puzzlePiece);
            setArrayLocation(puzzlePiece);
            col++;
            pieceID++;
         }
      }
      return pieceID;
   }
   
  /**
   * Generates pieces along the left and right of the screen, filling the gaps created by generateHorizontal.
   * @param frame the window itself
   * @param frameWidth the width of the application window
   * @param frameHeight the height of the application window
   * @param left whether to generate pieces at the left or right of the application window
   * @param pieceID the number of pieces that have been generated at the time of the method call
   * @return the number of pieces that have been generated at the time the method call finishes running.
   * This is used as the starting ID for the next time a piece generation method is called.
   */
   public int generateVertical(JFrame frame, int frameWidth, int frameHeight, boolean left, int pieceID) {
      int ypos = 160;
      int iterations = 0;
      int startingX = 0;
      int rightOffset = 0;
      
      if (!left) {
         startingX = 27;
         rightOffset = 1;
      }
      
      while (ypos < frameHeight - 170) {
         for (int i = startingX; i < startingX + 2 + rightOffset; i++) {
            Piece puzzlePiece = new Piece(this, (i*35 + ((i+1)*10)), ypos, 35, 35, pieceID);
            frame.add(puzzlePiece);
            setArrayLocation(puzzlePiece);
            pieceID++;
         }
         ypos += 50;
         iterations++;
      }
      
      return pieceID;
   }
   
  /**
   * Adds a piece to the general pieces array at the appropriate location. 
   * This array is used for pieces to check adjacency.
   * @param piece the piece being added to the array
   */
   public void setArrayLocation(Piece piece) {
      if (this.genCol > 19) {
         genRow++;
         genCol = 0;
      }
      
      pieces[genRow][genCol] = piece;
      piece.setArrayPosition(genRow, genCol);
      genCol++;
   }
   
  /**
   * Assigns every piece in the global array its adjacent pieces. 
   * Not every piece is adjacent to 4 others - in these cases, the slot in the adjacent pieces array is 
   * left as null. Positions in the generated adjacent pieces array go clockwise. This means that 
   * index 0 = the adjacent piece above, index 1 = the adjacent piece to the right, and so forth.
   * @param currentPiece the piece to generate this array of adjacent pieces for
   */ 
   public void getAdjacentPieces(Piece currentPiece) {
      Piece[] adjacencies = new Piece[4]; 
      if (currentPiece.getFinalRow() == 0) {
         adjacencies[0] = null;
      } else {
         adjacencies[0] = pieces[currentPiece.getFinalRow() - 1][currentPiece.getFinalCol()];
      }
      
      if (currentPiece.getFinalCol() == pieces[0].length - 1) {
         adjacencies[1] = null;
      } else {
         adjacencies[1] = pieces[currentPiece.getFinalRow()][currentPiece.getFinalCol() + 1];
      }
      
      if (currentPiece.getFinalRow() == pieces.length - 1) {
         adjacencies[2] = null;
      } else {
         adjacencies[2] = pieces[currentPiece.getFinalRow() + 1][currentPiece.getFinalCol()];
      }
      
      if (currentPiece.getFinalCol() == 0) {
         adjacencies[3] = null;
      } else {
         adjacencies[3] = pieces[currentPiece.getFinalRow()][currentPiece.getFinalCol() - 1];
      }
      
      currentPiece.setAdjacencies(adjacencies);
   }
   
  /**
   * Randomizes the positions of the piece around the board, so that the nice grid layout doesn't change but adjacent
   * pieces no longer are created right next to each other. 
   * The first loop adds the position of every piece to the list, the second loop swaps all of them around.
   * @param pieceCount the amount of pieces in the puzzle
   */
   public void scatterPieces(int pieceCount) {
      Random rand = new Random();
      ArrayList<Integer> xPositions = new ArrayList<Integer>();
      ArrayList<Integer> yPositions = new ArrayList<Integer>();
      
      for (int i = 0; i < pieces.length; i++) {
         for (int j = 0; j < pieces[0].length; j++) {
            xPositions.add(pieces[i][j].getPieceX());
            yPositions.add(pieces[i][j].getPieceY());   
         }
      }
      
      for (int i = 0; i < pieces.length; i++) {
         for (int j = 0; j < pieces[0].length; j++) {
            Piece currentPiece = pieces[i][j];
            int swapIndex = rand.nextInt(xPositions.size());
            int newXPos = xPositions.get(swapIndex);
            int newYPos = yPositions.get(swapIndex);
            currentPiece.setPieceLocation(newXPos, newYPos);
            xPositions.remove(swapIndex);
            yPositions.remove(swapIndex);
         }
      }
   }
   
  /**
   * Returns the global piece array.
   * @return {@link JigsawPuzzle#pieces}
   */
   public Piece[][] getPieceArray() {
      return pieces;
   }
}
