package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Class relative à toute opération liée à la base de donnée (representée par une hashmap)
 */
public class ImageDao implements Dao<Image> {

  //besoin côté client (lors de l'upload d'image) et côté serveur (lors de la récupération des images situées sur le serveur)
  public boolean isRGB(int width, int height, BufferedImage image){

    for (int i = 0 ; i < width ; i++) {
      for (int j = 0 ; j < height ; j++){
        int pixel = image.getRGB(i, j);
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        //check if R=G=B
        if (red != green || green != blue ) {
            return true;
      }
    }
  }
  return false;

}

   /**
   * Reprensente une "base de donnée" sous forme de HashMap, contenant les
   * 'images' par 'id'
   */
  private final Map<Long, Image> images = new HashMap<>();

  public ImageDao() throws IOException{
    
    File dir = new File("src/main/resources/");
        
    if (dir.isDirectory())
    {
      // Parcours dossier
      for (File f : dir.listFiles())
      {
        int[] dimension = new int[3];
        if(f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".tiff") )
        {
          
          ClassPathResource imgFile = new ClassPathResource(f.getName());
          BufferedImage bimg = ImageIO.read(new File("src/main/resources/"+f.getName()));
          int width          = bimg.getWidth();
          int height         = bimg.getHeight();
          dimension[0] = width;
          dimension[1] = height;
          if (isRGB(width, height, bimg)) //si c'est une image couleur, il y a 3 canaux
            dimension[2] = 3;
          else
            dimension[2] = 1; //sinon c'est une image en niveaux de gris et donc il n'y a qu'un seul canal

          byte[] fileContent;
          try {
            fileContent = Files.readAllBytes(imgFile.getFile().toPath());
            Image img = new Image(f.getName(), fileContent, dimension);
            images.put(img.getId(), img);
           
          } catch (final IOException e) {
            e.printStackTrace();
           }
         }
      }
      // Parcours sousdossier
      for (final File findSDir : dir.listFiles())
      {
        if(findSDir.isDirectory())
        {
          File subdir = new File(findSDir.getAbsolutePath());
          for (final File f : subdir.listFiles())
          {
            int[] dimension = new int[3];
            if(f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") )
            {
              ClassPathResource imgFile = new ClassPathResource(subdir.getName() + "/" + f.getName());
              BufferedImage bimg = ImageIO.read(new File("src/main/resources/"+subdir.getName() + "/" + f.getName()));
              int width          = bimg.getWidth();
              int height         = bimg.getHeight();
              dimension[0] = width;
              dimension[1] = height;
              if (isRGB(width, height, bimg)) //si c'est une image couleur, il y a 3 canaux
                dimension[2] = 3;
              else
                dimension[2] = 1; //sinon c'est une image en niveaux de gris et donc il n'y a qu'un seul canal

              byte[] fileContent;
              try {
                fileContent = Files.readAllBytes(imgFile.getFile().toPath());
                Image img = new Image(f.getName(), fileContent, dimension);
                images.put(img.getId(), img);
              } catch (final IOException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
    }
  }

  /**
   * Récupère une image
   * 
   * @param id Id utilisé afin de récupéré l'image depuis la HashMap 'images'
   * @return L'image possendant l'id si elle existe, sinon empty
   */
  @Override
  public Optional<Image> retrieve(final long id) {
    if (images.get(id) != null) // Si l'image est contenu dans la HashMap
      return Optional.of(images.get(id)); // On la retourne
    else
      return Optional.empty(); // Sinon on retourne un empty
  }

  /**
   * Parcours la HashMap 'images' par leur id. Pour chaque image contenu dans la
   * HashMap 'images', on l'ajoute à une liste.
   * 
   * @return La liste des images contenu dans la HashMap.
   */
  @Override
  public List<Image> retrieveAll() {
    final ArrayList<Image> Listimages = new ArrayList<Image>();
    for (final Long i : images.keySet()) { // Parcours la HashMap 'images' par 'id'
      Listimages.add(images.get(i)); // Ajout dans la Liste
    }
    return Listimages;
  }

  /**
   * Ajoute l'image donnée en parametre dans la Hashmap
   * 
   * @param img Image à ajouter dans la HashMap 'images'
   */
  @Override
  public void create(final Image img) {
    images.put(img.getId(), img); // L'image ajoutée aura le dernier 'id' de la HashMap
  }

  @Override
  public void update(final Image img, final String[] params) {
    // Not used
  }

  /**
   * Supprime l'image donnée en parametre
   * 
   * @param img Image à supprimer de la HashMap 'images'
   */
  @Override
  public void delete(final Image img) {
    images.remove(img.getId(), img);
  }
}
