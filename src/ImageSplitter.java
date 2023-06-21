import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.image.*;
import java.awt.event.*;

/**
* This class is exclusively used for splitting an image into appropriately sized and masked puzzle pieces.
* @author Noah Brown
*/
public class ImageSplitter {
  /**
   * The unmodified image to use for the puzzle.
   */
   private BufferedImage puzzleImage;
  /**
   * The number of rows the puzzle will have.
   */
   private int rows;
  /**
   * The number of columns the puzzle will have.
   */
   private int cols;
  /**
   * The width, in pixels, of {@link ImageSplitter#puzzleImage}.
   */
   private int imageWidth;
  /**
   * The height, in pixels, of {@link ImageSplitter#puzzleImage}.
   */
   private int imageHeight;
  /**
   * A 2D array of tesselating filepaths to be used for masking.
   */ 
   private String[][] filePaths;
   
  /**
   * Constructor method that reads a given image and, if necessary, converts it to an
   * appropriately sized png file.
   * @param image The image that the puzzle is going to use.
   * @param numRows The number of rows the puzzle will have.
   * @param numCols The number of columns the puzzle will have.
   */
   public ImageSplitter(File image, int numRows, int numCols) {
      try {
         this.puzzleImage = ImageIO.read(image);
      } catch (IOException e) {
         e.printStackTrace();
      }
      this.rows = numRows;
      this.cols = numCols;
      this.imageWidth = puzzleImage.getWidth();
      this.imageHeight = puzzleImage.getHeight();
      this.filePaths = initializePathArray();
      
      if (imageWidth != 700 && imageHeight != 385) {
         try {
            formatImage(700, 385, "png");
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
   
  /**
   * Splits the image held in the {@link ImageSplitter#puzzleImage} field into 220 rectangular images.
   * Images are not adjusted for the masking process and this WILL cause IOExceptions if used in the
   * final product without adjusting other code.
   * Left in for debugging purposes.
   */
   public void splitImageRect() {
      purgePieceImages(new File(System.getProperty("user.dir") + "/src/Resources/"));
      int xpos = 0;
      int ypos = 0;
      
      for (int i = 0; i < rows; i++) {
         xpos = 0;
         for (int j = 0; j < cols; j++) {
            splitImage(xpos, ypos, i, j, 0, 0, 0, 0);
            xpos += 35;
         }
         ypos += 35;
      }
   }
   
  /**
   * Splits the image held in the {@link ImageSplitter#puzzleImage} field into 220 rectangular images,
   * each with appropriate buffer space for the eventual mask step.
   * The file names of the generated subimages reflect what piece they will eventually be assigned to.
   */
   public void splitImageBuffer() {
      purgePieceImages(new File(System.getProperty("user.dir") + "/Resources/"));
      int xpos = 0;
      int ypos = 0;
      int upSpace;
      int rightSpace;
      int downSpace;
      int leftSpace;
      
      for (int i = 0; i < rows; i++) {
         xpos = 0;
         for (int j = 0; j < cols; j++) {
            upSpace = 5;
            rightSpace = 5;
            downSpace = 5;
            leftSpace = 5;
            
            if (i == 0) {
               upSpace = 0;
            } 
            if (i == rows - 1) {
               downSpace = 0;
            } 
            if (j == cols - 1) {
               rightSpace = 0;
            } 
            if (j == 0) {
               leftSpace = 0;
            }
            
            splitImage(xpos, ypos, i, j, upSpace, rightSpace, downSpace, leftSpace);                  
            xpos += 35; 
         }
         
         ypos += 35;
      }
   }
   
  /**
   * Takes a subimage of the main puzzle image and adds buffer space to prepare it for masking.
   * Very prone to IOExceptions. Good luck!
   * @param xpos The base x position to take the subimage from.
   * @param ypos The base y position to take the subimage from.
   * @param row The row to be used in the output filename.
   * @param col The column to be used in the output filename.
   * @param upSpace The amount (in pixels, which is true for all params of this type) of buffer space to be generated upwards of
   * the ypos parameter.
   * @param rightSpace The amount of buffer space to be generated rightwards of the piece's width plus the xpos parameter.
   * @param downSpace The amount of buffer space to be generated downwards of the piece's height plus the ypos parameter.
   * @param leftSpace The amount of buffer space to be generated leftwards of the xpos parameter.
   */
   public void splitImage(int xpos, int ypos, int row, int col, int upSpace, int rightSpace, int downSpace, int leftSpace) {
      try {
         BufferedImage pieceImage = puzzleImage.getSubimage(xpos - leftSpace, ypos - upSpace, 35 + leftSpace + rightSpace, 35 + upSpace + downSpace);
         File outputImage = new File(System.getProperty("user.dir") + "/Resources/piece" + row + "-" + col + ".png");
         ImageIO.write(pieceImage, "png", outputImage);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   
  /**
   * Resizes a given image to the given dimensions.
   * This is necessary because my puzzle algorithm is bad and restrictive.
   * @param newWidth The width, in pixels, to resize the image to.
   * @param newHeight The height, in pixels, to resize the image to.
   * @param filetype The file type to convert the image to. Currently unused, may be implemented later.
   * @throws java.io.IOException Throws an IOException if the image doesn't exist.
   */
   public void formatImage(int newWidth, int newHeight, String filetype) throws IOException {
      if (imageWidth != 700 || imageHeight != 385) {
         Image resizedImage = puzzleImage.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
         puzzleImage.getGraphics().drawImage(resizedImage, 0, 0, null);
      }
   }
   
  /**
   * Removes all files in a given directory.
   * Used to purge the Resources folder of generated piece images, in case the image being used for the
   * puzzle changes between runs. Thank you StackOverflow for this code.
   * @param dir The directory to purge.
   */
   public void purgePieceImages(File dir) {
      for (File file: dir.listFiles()) {
         if (!file.isDirectory()) {
            file.delete();
         }
      }
   }
   
  /**
   * Initializes and fills out {@link ImageSplitter#filePaths}. 
   * The edges and center form a 3x1 or 3x3 tesselation (respectively) that can be used for masking. 
   * @return The value of {@link ImageSplitter#puzzleImage}.
   */ 
   public String[][] initializePathArray() {
      String dir = System.getProperty("user.dir") + "/Resources/Masks/";
      String[][] paths = new String[5][5];
      paths[0][0] = "Corners/corner0-0.png";
      paths[0][4] = "Corners/corner0-1.png";
      paths[4][0] = "Corners/corner1-0.png";
      paths[4][4] = "Corners/corner1-1.png";
      
      for (int i = 1; i < 4; i++) {
         paths[i][0] = "Edges/Vert/edge" + (i - 1) + "-0.png";
         paths[i][4] = "Edges/Vert/edge" + (i - 1) + "-1.png";
         paths[0][i] = "Edges/Horz/edge0-" + (i - 1) + ".png";
         paths[4][i] = "Edges/Horz/edge1-" + (i - 1) + ".png";
      }
      
      for (int i = 1; i < paths.length - 1; i++) {
         for (int j = 1; j < paths[0].length - 1; j++)
            paths[i][j] = "Center/center" + (i - 1) + "-" + (j - 1) + ".png";
      }
      
      for (int i = 0; i < paths.length; i++) {
         for (int j = 0; j < paths[0].length; j++) {
            paths[i][j] = dir + paths[i][j];
         }
      }
      
      return paths;
   }
   
  /**
   * Determines what mask to apply to all 220 puzzle pieces, and calls the appropriate methods
   * to apply that mask. 
   * Which mask is "appropriate" is determined using the piece's position in the global array and the 
   * 5x5 filepath array constructed in {@link ImageSplitter#initializePathArray}. Corner pieces are set manually, 
   * then the tesselation begins. Edge pieces are given the mask filepath at the correct row/column relative to 
   * their edge status, and then 1 (to account for corners in the filepath array) + the current value of the column 
   * / row iterative variable modulo 3. This makes sure that the tesselation loops properly. Center pieces are the 
   * same, but both the column and row variables are iteratives, and they work with the center values of the filepath 
   * array. Once the correct file path has been determined, this calls {@link ImageSplitter#maskPiece} to do the 
   * actual masking. Basically, this is spaghetti code. Have fun!
   * @param pieces The global piece array.
   * @throws java.io.IOException Throws an IOException if the mask or image to apply the mask to doesn't exist.
   */ 
   public void massMaskPieces(Piece[][] pieces) throws IOException {
      purgePieceImages(new File(System.getProperty("user.dir") + "/Resources/Masked/"));
      // Corners
      pieces[0][0].setImage(maskPiece(filePaths[0][0], 0, 0));
      pieces[0][19].setImage(maskPiece(filePaths[0][4], 0, 19));
      pieces[10][0].setImage(maskPiece(filePaths[4][0], 10, 0));
      pieces[10][19].setImage(maskPiece(filePaths[4][4], 10, 19));
      // Top and bottom row
      for (int i = 0; i < 2; i++) {
         for (int cols = 0; cols < pieces[0].length - 2; cols++) {
            pieces[10*i][cols + 1].setImage(maskPiece(filePaths[4 * i][1 + (cols % 3)], 10 * i, cols + 1));
         }
      }
      // Side columns
      for (int i = 0; i < 2; i++) {
         for (int rows = 0; rows < pieces.length - 2; rows++) {
            pieces[rows + 1][19*i].setImage(maskPiece(filePaths[1 + (rows % 3)][4 * i], rows + 1, 19 * i));
         }
      }
      // Center
      for (int rows = 0; rows < pieces.length - 2; rows++) {
         for (int cols = 0; cols < pieces[0].length - 2; cols++) {
            pieces[rows + 1][cols + 1].setImage(maskPiece(filePaths[1 + (rows % 3)][1 + (cols % 3)], rows + 1, cols + 1));
         }
      }
   }
   
  /**
   * Applies a given grayscale mask to an image of the same dimensions.
   * The original author of this method is Michael Myers on StackOverflow.
   * @param maskFilePath The file path of the mask to apply to the image.
   * @param row The row to be used in the output filepath.
   * @param col The column to be used in the output filepath.
   * @return The image with the grayscale mask applied.
   * @throws java.io.IOException Throws an IOException if the mask or image to apply the mask to doesn't exist.
   */ 
   public BufferedImage maskPiece(String maskFilePath, int row, int col) throws IOException {
      BufferedImage[] m_images = new BufferedImage[3];
      m_images[0] = ImageIO.read(new File(System.getProperty("user.dir") + "/Resources/piece" + row + "-" + col + ".png"));
      m_images[1] = ImageIO.read(new File(maskFilePath));
      Image transpImg = transformGrayToTransparency(m_images[1]);
      m_images[2] = applyTransparency(m_images[0], transpImg);
      
      File outputImage = new File(System.getProperty("user.dir") + "/Resources/Masked/piece" + row + "-" + col + ".png");
      ImageIO.write(m_images[2], "png", outputImage);
      return m_images[2];
   }
   
  /**
   * Takes the grayscale mask and converts black (or gray) pixels in it into appropriate alpha values.
   * These values will then be applied to the image to be masked (specified in {@link ImageSplitter#maskPiece})
   * later.
   * The original author of this method is Michael Myers on StackOverflow.
   * @param image The mask image to apply this alpha conversion to.
   * @return The mask image with this conversion applied.
   */
   public BufferedImage transformGrayToTransparency(BufferedImage image) {
      ImageFilter filter = new RGBImageFilter()
      {
          public final int filterRGB(int x, int y, int rgb)
          {
              return (rgb << 8) & 0xFF000000;
          }
      };
   
      ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
      Image maskedAlmostDone = Toolkit.getDefaultToolkit().createImage(ip);
      
      BufferedImage maskedStep1 = new BufferedImage(maskedAlmostDone.getWidth(null), maskedAlmostDone.getHeight(null), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2D = maskedStep1.createGraphics();
      g2D.drawImage(maskedAlmostDone, 0, 0, null);
      g2D.dispose();
      
      return maskedStep1;
   }
   
  /**
   * Applies an alpha mask to a given image of the same dimensions.
   * This works by drawing the alpha mask over the image. Thanks Graphics2D!
   * The original author of this method is Michael Myers on StackOverflow.
   * @param image The image to apply the mask to.
   * @param mask The mask to be applied to the image.
   * @return The image with the mask applied.
   */
   public BufferedImage applyTransparency(BufferedImage image, Image mask) {
      BufferedImage maskedFINAL = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2D = maskedFINAL.createGraphics();
      g2D.drawImage(image, 0, 0, null);
      AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0F);
      g2D.setComposite(ac);
      g2D.drawImage(mask, 0, 0, null);
      g2D.dispose();
      return maskedFINAL;
   }

}