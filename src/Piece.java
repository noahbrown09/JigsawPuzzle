import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * The individual puzzle piece component. Can fuse with other puzzle pieces
 * (if appropriate) and move an image around the board when dragged with the
 * mouse.
 * @author Noah Brown
 */
public class Piece extends JPanel {
  /**
   * The board as a whole. Used to access the global piece array.
   */
   private JigsawPuzzle fullPuzzle;
  /**
   * The image displayed over the piece.
   */
   private BufferedImage pieceImage;
  /**
   * The x-position of the mouse.
   */
   private int mouseX = 0;
  /**
   * The y-position of the mouse.
   */
   private int mouseY = 0;
  /**
   * The x-position of the piece.
   */
   private int pieceX;
  /**
   * The y-position of the piece.
   */
   private int pieceY;
  /**
   * The row the piece occupies in the global piece array.
   */
   private int puzzleRow;
  /**
   * The column the piece occupies in the global piece array.
   */
   private int puzzleCol;
  /**
   * The unique ID of the piece.
   */
   private int pieceID;
  /**
   * An array of all pieces that an instance of a piece object can connect to.
   * Determined in {@link JigsawPuzzle#getAdjacentPieces}.
   */
   private Piece[] adjacentPieces;
  /**
   * A list of all pieces the piece is currently connected to.
   * When the piece moves, all pieces in this list need to be updated by the same change in position.
   */
   private ArrayList<Piece> boundPieces;
   
  /**
   * Constructor that places the pieces around the board instead of just at 0, 0.
   * @param puzzle the puzzle board as a whole 
   * @param xpos the x-position to initialize the piece at
   * @param ypos the y-position to initialize the piece at
   * @param width the width of the piece being initialized
   * @param height the height of the piece being initialized
   * @param id the unique ID of the piece. Used for checking if two pieces are the same.
   */
   public Piece(JigsawPuzzle puzzle, int xpos, int ypos, int width, int height, int id) {
      this.pieceX = xpos;
      this.pieceY = ypos;
      this.pieceID = id;
      this.fullPuzzle = puzzle;
      this.boundPieces = new ArrayList<Piece>();
      Piece[][] puzzleBoard = fullPuzzle.getPieceArray();
      
      
      drawPiece(width, height);
      setupControls();
   }
  
  /* GENERALLY USEFUL METHODS: LINES 85 TO 156 */

  /**
   * Whenever the piece is painted, this method draws the image given by {@link Piece#pieceImage} over it.
   * @param g the graphics object used for... graphics, probably
   */
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (pieceImage != null) {
         g.drawImage(pieceImage, 0, 0, this);
      }
   }
   
   
  /**
   * Sets the location of the piece on the board.
   * @param x the top-left x coordinate of the piece
   * @param y the top-left y coordinate of the piece
   */ 
   public void setPieceLocation(int x, int y) {
      super.setLocation(x, y);
      pieceX = getX();
      pieceY = getY();
   }
   
  /**
   * Adds a mouse listener so the piece can be draggable.
   * While this method is only called in initial setup, the mouse listener is constantly functioning.
   */
   public void setupControls() {
      Piece[][] puzzleBoard = fullPuzzle.getPieceArray();
      addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            mouseX = e.getXOnScreen();
            mouseY = e.getYOnScreen();
            pieceX = getX();
            pieceY = getY();
         }
         
         public void mouseReleased(MouseEvent e) {
            Rectangle bounds = returnBounds();
            for (int i = 0; i < puzzleBoard.length; i++) {
               for (int j = 0; j < puzzleBoard[0].length; j++) {
                  if (bounds.intersects(puzzleBoard[i][j].getBounds())) {
                     checkAdjacence(puzzleBoard[i][j]);
                  }
               }
            }
         }
      });
         
      addMouseMotionListener(new MouseMotionAdapter() {
         public void mouseDragged(MouseEvent e) {
            int deltaX = e.getXOnScreen() - mouseX;
            int deltaY = e.getYOnScreen() - mouseY;
            
            int oldPieceX = getX();
            int oldPieceY = getY();
            setLocation(pieceX + deltaX, pieceY + deltaY);
            
            int dx = getX() - oldPieceX;
            int dy = getY() - oldPieceY;
            
            if (boundPieces.size() > 0) {
               updateBoundPieces(dx, dy);
            }
         }
      });
   }
  
  /* END OF GENERALLY USEFUL METHODS 
   * ADJACENCY AND BINDING METHODS: LINES 157 - 282 */ 
  
  /**
   * Checks two intersecting pieces for adjacency. If the pieces are indeed adjacent in the global array,
   * it adds the intersecting piece to the original's ArrayList and vice versa.
   * @param intersecting the piece to check adjacence against
   */ 
   public void checkAdjacence(Piece intersecting) {
      for (int i = 0; i < adjacentPieces.length; i++) {
         if (adjacentPieces[i] != null) {
            if (intersecting.getPieceID() == adjacentPieces[i].getPieceID()) {
               
               int oldX = getX();
               int oldY = getY();
               if (i == 0) { // Piece is above
                  setPieceLocation(intersecting.getPieceX(), intersecting.getPieceY() + intersecting.getPieceImage().getHeight() - 10); 
               } else if (i == 1) { // Piece is right
                  setPieceLocation(intersecting.getPieceX() - pieceImage.getWidth() + 10, intersecting.getPieceY());
               } else if (i == 2) { // Piece is below
                  setPieceLocation(intersecting.getPieceX(), intersecting.getPieceY() - pieceImage.getHeight() + 10);
               } else { // Piece is left
                  setPieceLocation(intersecting.getPieceX() + intersecting.getPieceImage().getWidth() - 10, intersecting.getPieceY());
               }
               
               if (boundPieces.size() > 0) {
                  updateBoundPieces(getX() - oldX, getY() - oldY);
               }

               addBindingsNew(intersecting);
               //checkBindDesyncs();
            }
         }     
      }
   }
   
  /**
   * Functions by first adding the intersecting piece to the method caller's list of connected pieces, then adding every 
   * piece the intersecting piece connects to to the method caller's list. Duplicates and the caller itself are removed
   * from the caller's list. 
   * Every piece in the caller's list is then sent the caller itself, and every item in the caller's list. All received
   * objects are added to the receiver's list. Duplicates and the receiver itself are removed from the receiver's list.
   * @param bindTo the intersecting piece as referred to above
   */
   public void addBindingsNew(Piece bindTo) {
      boundPieces.add(bindTo);
      if (bindTo.getBoundPieces().size() > 0) {
         for (int i = 0; i < bindTo.getBoundPieces().size(); i++) {
            boundPieces.add(bindTo.getBoundPieces().get(i));
         }
      }
      boundPieces = sanitizeList(boundPieces);
      
      for (int i = 0; i < boundPieces.size(); i++) {
         boundPieces.get(i).addBindingUnsafe(this);
         for (int j = 0; j < boundPieces.size(); j++) {
            boundPieces.get(i).addBindingUnsafe(boundPieces.get(j));
         }
         boundPieces.get(i).sanitizeList(boundPieces.get(i).getBoundPieces());
      }
   }
   
  /**
   * Adds a given piece to this piece's binding list without sanitizing the list.
   * This method is marked as unsafe because it does not sanitize the list after adding the piece.
   * This can cause two problems: a duplicate in the list, or the piece itself existing as an item in its own list.
   * Remember to call {@link Piece#sanitizeList} after running this to prevent these problems.
   * @param bindTo the piece to add to the list of connected pieces
   */
   public void addBindingUnsafe(Piece bindTo) {
      boundPieces.add(bindTo);
   }
   
  /**
   * Removes all duplicates and the method caller itself from a given list of objects. This is referred
   * to as "sanitization."
   * @param list the arrayList to sanitize
   * @return the sanitized list
   */
   public ArrayList<Piece> sanitizeList(ArrayList<Piece> list) {
      Set<Piece> tempSet = new HashSet<>(list);
      list.clear();
      list.addAll(tempSet);
      
      for (int i = list.size() - 1; i >= 0; i--) {
         if (list.get(i).getPieceID() == pieceID) {
            list.remove(i);
         }
      }
      
      return list;
   }
   
  /**
   * If a bug arises where some pieces aren't being correctly added to other's lists by {@link Piece#addBindingsNew}, 
   * this method serves as a nuclear option to reset the problematic piece's list and start over. 
   * The piece binding algorithm seems to work perfectly, but this method has been left in the code for if the need arises.
   */
   public void checkBindDesyncs() {
      for (int i = 0; i < boundPieces.size(); i++) {
         if (boundPieces.get(i).getBoundPieces().size() < boundPieces.size()) {
            // oh god
            // sound the alarms
            Piece problemPiece = boundPieces.get(i);
            problemPiece.addBindingUnsafe(this);
            for (int j = 0; j < boundPieces.size(); j++) {
               problemPiece.addBindingUnsafe(boundPieces.get(i));
            }
            problemPiece.sanitizeList(problemPiece.getBoundPieces());
         }
      }
   }
   
  /**
   * Updates the position of all pieces connected to the one that called the method.
   * Uses the piece's ArrayList of bound pieces to determine which ones are connected.
   * @param deltaX the amount of pixels to move all bound pieces left or right by
   * @param deltaY the amount of pixels to move all bound pieces up or down by
   */
   public void updateBoundPieces(int deltaX, int deltaY) {
      for (int i = 0; i < boundPieces.size(); i++) {
         Piece currentBoundPiece = boundPieces.get(i);
         currentBoundPiece.setPieceLocation(currentBoundPiece.getPieceX() + deltaX, currentBoundPiece.getPieceY() + deltaY);
      }
   }
  
  /* END OF ADJACENCY AND BINDING METHODS
   * ACCESSOR METHODS: LINES 284 TO 350 */
  
  /**
   * Returns the current x-coordinate of the piece.
   * @return {@link Piece#pieceX}
   */ 
   public int getPieceX() {
      return getX();
   }
   
  /**
   * Returns the current x-coordinate of the piece.
   * @return {@link Piece#pieceY}
   */ 
   public int getPieceY() {
      return getY();
   }
  
  /**
   * Returns the correct final row for the piece.
   * @return {@link Piece#puzzleRow}
   */  
   public int getFinalRow() {
      return this.puzzleRow;
   }
  
  /**
   * Returns the correct final column for the piece.
   * @return {@link Piece#puzzleCol}
   */
   public int getFinalCol() {
      return this.puzzleCol;
   }
  
  /**
   * Returns the list of currently bound pieces.
   * @return {@link Piece#boundPieces}
   */
   public ArrayList<Piece> getBoundPieces() {
      return boundPieces;
   }   
  
  /**
   * Returns the unique ID of the piece.
   * @return {@link Piece#pieceID}
   */
   public int getPieceID() {
      return pieceID;
   }
   
  /**
   * Returns the current bounds of the piece as a Rectangle object.
   * @return {@link Piece#getBounds}
   */
   public Rectangle returnBounds() {
      return getBounds();
   }
  
  /**
   * Returns the current image used for the piece.
   * @return {@link Piece#pieceImage}
   */
   public BufferedImage getPieceImage() {
      return pieceImage;
   }   
   
  /* END OF ACCESSOR METHODS
   * INITIAL SETUP METHODS: LINES 351 TO 397 */
   
  /**
   * Sets {@link Piece#pieceImage} to the given BufferedImage.
   * Only called in initial setup.
   * @param image the image to set {@link Piece#pieceImage} to
   */
   public void setImage(BufferedImage image) {
      this.pieceImage = image;
      drawPiece(image.getWidth(), image.getHeight());
      repaint();
   }
   
  /**
   * Adds all adjacent pieces to {@link Piece#adjacentPieces} at the appropriate indices.
   * Only called in initial setup.
   * @param adjacencies an array of pieces adjacent to this one in the global array to be copied into 
   * the piece's array
   */
   public void setAdjacencies(Piece[] adjacencies) {
      adjacentPieces = adjacencies;
   }
   
  /**
   * Adds the piece to the global array so later adjacency can be checked.
   * Only called in initial setup.
   * @param row the row of the global array to add the piece to
   * @param col the column of the global array to add the piece to
   */
   public void setArrayPosition(int row, int col) {
      this.puzzleRow = row;
      this.puzzleCol = col;
   }
   
  /** 
   * Sets the bounds of the piece and makes it non-opaque. Actual visuals are done via {@link Piece#setImage}.
   * Only called in initial setup.
   * @param width the width of the piece
   * @param height the height of the piece
   */
   public void drawPiece(int width, int height) {
      setBackground(Color.WHITE);
      setBounds(pieceX, pieceY, width, height);
      setOpaque(false);
   }
   
  /* END OF INITIAL SETUP METHODS
     END OF FILE */
}