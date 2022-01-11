package pdl.backend;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ch.qos.logback.core.joran.conditional.ElseAction;
import io.scif.FormatException;
import io.scif.img.SCIFIOImgPlus;
import net.imglib2.algorithm.math.Else;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;


@RestController
/**
 * Cette classe gere les requetes des clients sur le coté serveur, notamment en manipulant la base de donnée
 */
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  /**
   * Récupère une image depuis la HashMap 'images'
   * 
   * @param id Id utilisé afin de récupérer l'image depuis la HashMap 'images' de
   *           la class @see ImageDao
   * @return Une réponse HTTP contenant les données de l'image dans le body ainsi
   *         qu'un status code à 200 (correspondant à OK) si' l'image est trouvée.
   *         Sinon renvois seulement une réponse HTTP contenant le status code 404
   *         (correspondant à 'NOT_FOUND').
   */
  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> getImage(@PathVariable("id") long id, @PathVariable("path") String path)
  {

    if(imageDao.retrieve(id).isPresent())
      return new ResponseEntity<>(imageDao.retrieve(id).get().getData(), HttpStatus.OK); 
    else
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Supprime une image contenu dans la HashMap 'images' (bdd du serveur)
   * 
   * @param id Id utilisé afin de supprimer l'image depuis la HashMap 'images' de
   *           la class @see ImageDao
   * @return Une réponse HTTP renvoyant un status code à 200 (correspondant à OK)
   *         si' l'image à pu être supprimée . Sinon renvois seulement une réponse
   *         HTTP contenant le status code 404 (correspondant à 'NOT_FOUND').
   * 
   *         Pour tester cette requete depuis un terminal : curl -X DELETE
   *         "http://localhost:8080/images/<id>"
   */
  @RequestMapping(value = "/{path}/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id, @PathVariable("path") String path) {

    if(!path.equals("images"))
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    else if(imageDao.retrieve(id).isPresent()){ //si l'image qu'on souhaite supprimer est bien présente
      imageDao.delete(imageDao.retrieve(id).get()); //on supprime
      return new ResponseEntity<>(HttpStatus.OK);
    } 
    else if(!imageDao.retrieve(id).isPresent())
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    else
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

 
  /**
   * Méthode réalisée à l'aide du lien suivant : https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
   * @param width
   * @param height
   * @param image
   * @return true si l'image est en RGB, false sinon
   */
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
   * Ajoute une image au format jpeg dans la HashMap 'images' (sur la bdd du serveur)
   * 
   * @param file               Image à ajouter à la HashMap 'images'
   * @param redirectAttributes
   * @return Une réponse HTTP renvoyant un status code à 200 (correspondant à
   *         'OK') indiquant que la requete à créé un nouvelle ressource et
   *         donc que l'image à bien été créée. Si le type de fichier envoyé n'est
   *         pas une image jpeg, renvois un status code 415 (correspondant à
   *         UNSUPPORTED_MEDIA_TYPE), s'il est vide renvois un status code 400
   *         (correspondant à BAD_REQUEST).
   * @throws IOException
   * 
   *                     Pour tester cette requete depuis un terminal : curl -X
   *                     POST -F "file=@<imagepath>"
   *                     "http://localhost:8080/images"
   */
  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file,
      RedirectAttributes redirectAttributes) throws IOException{

      String formatFichier = file.getContentType(); //on récupère le format de fichier

      if (formatFichier == null){ //on ne souhaite que du jpeg
        System.out.println("Mauvais format de fichier");
        return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
      }
      if (!formatFichier.equals("image/jpeg") && !formatFichier.equals("image/tiff")){
        System.out.println("Mauvais format de fichier");
        return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
      }
      byte[] picture = file.getBytes(); // on récupére le tableau de bytes de l'image
      InputStream in = new ByteArrayInputStream(picture);
      BufferedImage buf = ImageIO.read(in); // on créer un reader
      // on récupère ainsi height et width
      if (buf != null){

        int height = buf.getHeight(); 
        int width = buf.getWidth();
  
        int[] dimensions = new int[3];
        dimensions[0] = width;
        dimensions[1] = height;
        if (isRGB(width, height, buf)) //si c'est une image couleur, il y a 3 canaux
          dimensions[2] = 3;
        else
          dimensions[2] = 1; //sinon c'est une image en niveaux de gris et donc il n'y a qu'un seul canal
        
        Image nouvelle = new Image(file.getOriginalFilename(), file.getBytes(),dimensions); //on créer l'image avec son vrai nom (situé dans le système de fichier de l'utilisateur)
        imageDao.create(nouvelle);

      }
     
      return new ResponseEntity<>(HttpStatus.CREATED);
  }
  /**
   * Listes les images presentes sur le serveur au format JSON, sous forme d'arbre
   * 
   * @return La liste des images presentes sur le serveur au format JSON
   * 
   *         cmd : curl -X GET "http://localhost:8080/images"
   */
  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ArrayNode getImageList() {
    ArrayNode nodes = mapper.createArrayNode(); //on crée la liste de noeuds
    
    imageDao.retrieveAll().forEach(img -> { //pour chaque image présente dans la hashmap on ajoute un noeud et on nomme l'image avec son double identifiant (nom + id)
      ObjectNode imgNode = mapper.createObjectNode();
     
        imgNode.put("id",img.getId());
        imgNode.put("name",img.getName());
        imgNode.put("size", img.getWidth() + "*" + img.getHeight() + "*" + img.isRGB_Or_Grey());
        nodes.add(imgNode);
      });

      return nodes;
  }

    
  /**
   * Les parametres de cette fonction sont envoyés en methode GET
   * @param id identifiant de l'image
   * @param algorithm algorithme selectionné par l'utilisateur
   * @param arg1 argument 1 de l'algorithme (optionnel)
   * @param arg2 argument 1 de l'algorithme (optionnel)
   * @return Renvoie l'image filtrée par l'algorithme
   * @throws FormatException
   * @throws IOException
   */
  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  @ResponseBody
  public ResponseEntity<?> executeAlgo(@PathVariable("id") long id,@RequestParam(defaultValue  = "empty") String algorithm,@RequestParam(required = false) Integer arg1,@RequestParam(required = false) Integer arg2 ) throws FormatException, IOException{
    
    if (imageDao.retrieve(id).isPresent()){
      byte imageToFilter[] = imageDao.retrieve(id).get().getData(); //tableau de bytes correspondant à l'image à filtrer
      if (imageToFilter == null)
        return new ResponseEntity<>( HttpStatus.NOT_FOUND);
    
    
    switch (algorithm) {

      case "empty" : 
        if (imageDao.retrieve(id).isPresent())
          return new ResponseEntity<>(imageToFilter, HttpStatus.OK);
        break;

      case "ColorToGray" : 
          if (imageDao.retrieve(id).isPresent()){
          Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
          Color.ColorToGray(img);
          return new ResponseEntity<>(ImageConverter.imageToJPEGBytes((SCIFIOImgPlus<UnsignedByteType>) img), HttpStatus.OK);
        }
          else 
            return new ResponseEntity<>( HttpStatus.NOT_FOUND);

    case "HueFilter" : 
        if (imageDao.retrieve(id).isPresent()){
          Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
          Color.HueFilter(img, arg1);
          return new ResponseEntity<>(ImageConverter.imageToJPEGBytes((SCIFIOImgPlus<UnsignedByteType>) img), HttpStatus.OK);
        }
        else 
          return new ResponseEntity<>( HttpStatus.NOT_FOUND);
     
    
        case "contrast" : 
          if (imageDao.retrieve(id).isPresent()){
            Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
            Color.contrast(img, arg1, arg2);
            return new ResponseEntity<>(ImageConverter.imageToJPEGBytes((SCIFIOImgPlus<UnsignedByteType>) img), HttpStatus.OK);
        }
        else 
          return new ResponseEntity<>( HttpStatus.NOT_FOUND);
      

        case "Ehistogramme" :
          if (imageDao.retrieve(id).isPresent()){
            Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
            Color.Ehistogramme(img);
            return new ResponseEntity<>(ImageConverter.imageToJPEGBytes((SCIFIOImgPlus<UnsignedByteType>) img), HttpStatus.OK);
          }
          else 
            return new ResponseEntity<>( HttpStatus.NOT_FOUND);

        case "brighness" :
          if (imageDao.retrieve(id).isPresent()){
            Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
            Color.brightness(img, arg1);
            return new ResponseEntity<>(ImageConverter.imageToJPEGBytes((SCIFIOImgPlus<UnsignedByteType>) img), HttpStatus.OK);
         }
         else 
          return new ResponseEntity<>( HttpStatus.NOT_FOUND);

        case "edges" :
          if (imageDao.retrieve(id).isPresent()){
            Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
            Img<UnsignedByteType> output= ImageConverter.imageFromJPEGBytes(imageToFilter);
           Color.Sobel( img, output,1);
            SCIFIOImgPlus<UnsignedByteType> finaloutput=(SCIFIOImgPlus<UnsignedByteType>) output;
            return new ResponseEntity<>(ImageConverter.imageToJPEGBytes(finaloutput), HttpStatus.OK);
          }
          else 
            return new ResponseEntity<>( HttpStatus.NOT_FOUND);
        
        case "sharpen" :
            if (imageDao.retrieve(id).isPresent()){
              Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
              Img<UnsignedByteType> output= ImageConverter.imageFromJPEGBytes(imageToFilter);
             Color.Sobel( img, output, 2);
              SCIFIOImgPlus<UnsignedByteType> finaloutput=(SCIFIOImgPlus<UnsignedByteType>) output;
              return new ResponseEntity<>(ImageConverter.imageToJPEGBytes(finaloutput), HttpStatus.OK);
            }
            else 
              return new ResponseEntity<>( HttpStatus.NOT_FOUND);

        case "gauss" : 
          if (imageDao.retrieve(id).isPresent()){
            Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
            Img<UnsignedByteType> output= ImageConverter.imageFromJPEGBytes(imageToFilter);
            Color.convolution(img, output,arg1);

          SCIFIOImgPlus<UnsignedByteType> finaloutput=(SCIFIOImgPlus<UnsignedByteType>) output;
          return new ResponseEntity<>(ImageConverter.imageToJPEGBytes(finaloutput), HttpStatus.OK);
        }
        else 
          return new ResponseEntity<>( HttpStatus.NOT_FOUND);

        case "negative" : 
          if (imageDao.retrieve(id).isPresent()){
            Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
            Color.negative(img);

          SCIFIOImgPlus<UnsignedByteType> finaloutput=(SCIFIOImgPlus<UnsignedByteType>) img;
          return new ResponseEntity<>(ImageConverter.imageToJPEGBytes(finaloutput), HttpStatus.OK);
        }
        else 
          return new ResponseEntity<>( HttpStatus.NOT_FOUND);

        case "reverse" : 
          if (imageDao.retrieve(id).isPresent()){
            Img<UnsignedByteType> img= ImageConverter.imageFromJPEGBytes(imageToFilter);
            Color.reverse(img);

          SCIFIOImgPlus<UnsignedByteType> finaloutput=(SCIFIOImgPlus<UnsignedByteType>) img;
          return new ResponseEntity<>(ImageConverter.imageToJPEGBytes(finaloutput), HttpStatus.OK);
        }
        else 
          return new ResponseEntity<>( HttpStatus.NOT_FOUND);
        
        default: break;
      }
       
  }
  return new ResponseEntity<>( HttpStatus.NOT_FOUND);   
}
}