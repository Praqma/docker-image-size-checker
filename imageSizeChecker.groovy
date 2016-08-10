import java.awt.image.BufferedImage;
import java.io.File;
import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import jenkins.*;
import jenkins.model.*;
import static groovy.io.FileType.FILES;

int IMG_MAX_SIZE = 2000;
//boolean isError = false;
def workspace = "/home/jenkins/site/";
//assert imgExtentions.any { it == ".png" }
//println workspace.dump();
new File(workspace).eachFileRecurse(FILES) {
  //imgExtentions
  if( it.name ==~ /([^\s]+(\.(?i)(jpg|png|gif|bmp))$)/  ){
    println it;
    def img = ImageIO.read(it);
    if( img.getWidth()>IMG_MAX_SIZE || img.getHeight()>IMG_MAX_SIZE ){
      println "Error: image ${it} size ${ img.getWidth() }x${ img.getWidth() } exceeds limit ${ IMG_MAX_SIZE }x${ IMG_MAX_SIZE }.";
//      isError = true;
    }
  }
}
//if (isError == true){
//  System.exit(1);
//}
//System.exit(0);
