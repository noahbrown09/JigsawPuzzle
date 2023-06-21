import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class ImageTester {
   private static BufferedImage image;
   private static BufferedImage mask;
   
   public static void main(String[] args) throws IOException {
      BufferedImage[] m_images = new BufferedImage[3];
      m_images[0] = ImageIO.read(new File("C:/Users/noahb/Downloads/tedTest.png"));
      m_images[1] = ImageIO.read(new File("C:/Users/noahb/Downloads/tedTestMask2.png"));
      Image transpImg = TransformGrayToTransparency(m_images[1]);
      m_images[2] = ApplyTransparency(m_images[0], transpImg);
      
      File outputImage = new File("C:/Users/noahb/Downloads/tedProcessed.png");
      ImageIO.write(m_images[2], "png", outputImage);
      quit();
   }
   
   private static Image TransformGrayToTransparency(BufferedImage image) {
      ImageFilter filter = new RGBImageFilter()
      {
          public final int filterRGB(int x, int y, int rgb)
          {
              return (rgb << 8) & 0xFF000000;
          }
      };
   
      ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
      return Toolkit.getDefaultToolkit().createImage(ip);
   }  

   private static BufferedImage ApplyTransparency(BufferedImage image, Image mask) {
      BufferedImage dest = new BufferedImage(
      image.getWidth(), image.getHeight(),
      BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = dest.createGraphics();
      g2.drawImage(image, 0, 0, null);
      AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0F);
      g2.setComposite(ac);
      g2.drawImage(mask, 0, 0, null);
      g2.dispose();
      return dest;
   }
}