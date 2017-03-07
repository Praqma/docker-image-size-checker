import java.awt.image.BufferedImage;
import java.io.File;
import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import jenkins.*;
import jenkins.model.*;
import static groovy.io.FileType.FILES;
import groovy.transform.CompileStatic

def workspace = "/home/jenkins/site/";

public class WebsiteWarning {
  def associatedPath
  public WebsiteWarning(def associatedPath) {
    this.associatedPath = associatedPath
  }
}

public class SizeWarning extends WebsiteWarning {

  def maxImageSize,sizeRecorded

  public SizeWarning(def associatedPath, def maxImageSize, def sizeRecorded) { 
    super(associatedPath)
    this.maxImageSize = maxImageSize
    this.sizeRecorded = sizeRecorded
  }

  @Override
  public String toString() {
    "[ImageChecker] (Warning) File ${associatedPath}. Max image size is ${maxImageSize} bytes. Was $sizeRecorded bytes"
  }
}

public class UnreadableWarning extends WebsiteWarning {
  def ex, unreadablePath
  public UnreadableWarning(def ex, def unreadablePath) {
    super(unreadablePath)
    this.ex = ex
  }

  @Override
  public String toString() {
    "[ImageChecker] (Warning) Unable to read path $path message was ${ex.message}"
  }
}

class DimensionWarning extends WebsiteWarning {
  def y,x,maxy,maxx

  public DimensionWarning(def y, def x, def maxy, def maxx, def associatedPath) {
    super(associatedPath)
    this.y = y
    this.x = x
    this.maxy = maxy
    this.maxx = maxx
  }

  @Override
  public String toString() {
    "[ImageChecker] (Warning) File ${associatedPath}. Max width is ${maxy}px was ${y}px. Max height is ${maxx}px was ${x}px"
  }    
} 

class WarningsList extends ArrayList<WebsiteWarning> {
  
  def imageMaxSize,maxImageWidth,maxImageHeight
  def scanBounds = true
  def scanSize = false

  public WarningsList(def imageMaxSize = 1_000_000, def maxImageWidth = 2000, def maxImageHeight = 2000) {
    this.maxImageHeight = maxImageHeight
    this.maxImageWidth = maxImageWidth 
    this.imageMaxSize = imageMaxSize
  }

  def setScanSize(def scanSize = true) {
    this.scanSize = scanSize
    return this
  }

  def setScanBounds(def scanBounds = true) {
    this.scanBounds = scanBounds
    return this
  }

  def scan(def rootPath) {
    new File(rootPath).eachFileRecurse(FILES) {
      if( it.name ==~ /([^\s]+(\.(?i)(jpg|png|gif|bmp))$)/ ) {
        try {
          if (this.scanBounds) {
            def img = ImageIO.read(it);
            if (img == null) {
              prinln "[ImageChecker] (Warning) Unable to check $it.name"
            } else if ( img.getWidth() > maxImageWidth || img.getHeight() > maxImageHeight ){
              this.add(new DimensionWarning(img.getWidth(), img.getHeight(), maxImageWidth, maxImageHeight, it))
            }
          }
          if (this.scanSize) {
            if(it.length() > imageMaxSize) {
              this.add(new SizeWarning(it, imageMaxSize, it.length()))
            }
          }
          
        } catch (Exception ex) {
          this.add(new UnreadableWarning(ex, it)) 
        } finally {
          println "[ImageChecker] Checked $it"
        }
      }      
    }
    return this
  }

  @Override
  public String toString() {
    return this.join("\n")
  }
}

//IMPROVE-ME: This should be part of some CLI method as args
def scanDir = System.getenv("SCAN_DIR") ?: "/home/jenkins/site/"
def scanBounds = System.getenv("SCAN_BOUNDS") ?: false 
def scanSize = System.getenv("SCAN_SIZE") ?: false

println "Scan bounds: $scanBounds"
println "Scan size: $scanSize"
println "Scan dir: $scanDir"

println new WarningsList().setScanBounds(scanBounds).setScanSize(scanSize).scan(scanDir).toString()

//TODO: Implement CLI? We can create a seperate JAR for this if need be and include it in our image. (If we want to have a totally seperate image for this) 