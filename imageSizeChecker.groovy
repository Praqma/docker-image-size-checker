import java.awt.image.BufferedImage;
import java.io.File;
import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import jenkins.*;
import jenkins.model.*;
import static groovy.io.FileType.FILES;

int IMG_MAX_SIZE = 2000;

def workspace = "/home/jenkins/site/";

new File(workspace).eachFileRecurse(FILES) {
  
  if( it.name ==~ /([^\s]+(\.(?i)(jpg|png|gif|bmp))$)/ ) {
    println "Checking $it";
    def img = ImageIO.read(it);
    if (img == null) {
      println "Error: Could not process image $it"
    } else if ( img.getWidth()>IMG_MAX_SIZE || img.getHeight()>IMG_MAX_SIZE ){
      println "Error: image ${it} size ${ img.getWidth() }x${ img.getWidth() } exceeds limit ${ IMG_MAX_SIZE }x${ IMG_MAX_SIZE }.";
    } else {
      println "Image is within bounds"
  }
}
