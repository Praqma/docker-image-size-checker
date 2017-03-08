@GrabResolver(name='imaging', root='https://repo.adobe.com/nexus/content/repositories/public/')
@Grapes(
    @Grab(group='org.apache.commons', module='commons-imaging', version='1.0-R1534292')
)

import java.awt.image.BufferedImage
import java.io.File
import javax.activation.MimetypesFileTypeMap
import javax.imageio.ImageIO
import jenkins.*
import jenkins.model.*
import static groovy.io.FileType.FILES

import org.apache.commons.imaging.*

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
    "[ImageChecker] (Warning) (Filesize) File ${associatedPath}. Max image size is ${maxImageSize} kb. Was $sizeRecorded kb"
  }
}

public class UnreadableWarning extends WebsiteWarning {
  def ex, associatedPath

  public UnreadableWarning(def ex, def associatedPath) {
    super(associatedPath)
    this.ex = ex
  }

  public String toString() {
    "[ImageChecker] (Error) Unable to read path $associatedPath message was ${ex.message}"
  }
}

public class DpiWarning extends WebsiteWarning {
  def minDpi, recordedDpi

  public DpiWarning(def minDpi, def recordedDpi, def associatedPath) {
    super(associatedPath)
    this.minDpi = minDpi
    this.recordedDpi = recordedDpi
  }
  
  public String toString() {
    "[ImageChecker] (Warning) (Dpi) File ${associatedPath}. Too low DPI. Is $recordedDpi should be equal or greater than $minDpi"
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

  public String toString() {
    "[ImageChecker] (Warning) (Resolution) File ${associatedPath}. Max width is ${maxy}px was ${y}px. Max height is ${maxx}px was ${x}px"
  }    
} 

class WarningsList extends ArrayList<WebsiteWarning> {

  def imageMaxSize,maxImageWidth,maxImageHeight,minDPI
  def scanBounds = true
  def scanSize = false
  def scanDPI = false

  public WarningsList(def imageMaxSize = 6, def maxImageWidth = 2000, def maxImageHeight = 2000, def minDPI = 100) {
    this.maxImageHeight = maxImageHeight
    this.maxImageWidth = maxImageWidth 
    this.imageMaxSize = imageMaxSize
    this.minDPI = minDPI
  }

  def setScanDPI(def scanDPI = true) {
    this.scanDPI = scanDPI
    return this
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
            if(((int)it.length() / 1000) > imageMaxSize) {
              this.add(new SizeWarning(it, imageMaxSize, (int)(it.length()/1024)))
            }
          }

          if (this.scanDPI) {
            def metadata = Imaging.getImageInfo(it)
            if(metadata != null && metadata.getPhysicalWidthDpi() != -1 && metadata.getPhysicalHeightDpi() != -1) {
              if(minDPI >= metadata.getPhysicalWidthDpi()) {
                this.add(new DpiWarning(minDPI, metadata.getPhysicalWidthDpi(), it))
              }
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

def scanDir = System.getenv("SCAN_DIR") ?: "/home/jenkins/site/"
def scanBounds = System.getenv("SCAN_BOUNDS") ?: false 
def scanSize = System.getenv("SCAN_SIZE") ?: true
def scanDPI = System.getenv("SCAN_DPI") ?: false
def fail = System.getenv("SCAN_FAIL") ?: false

println "Scan bounds: $scanBounds"
println "Scan size: $scanSize"
println "Scan dir: $scanDir"
println "Scan DPI: $scanDPI" 
println "Fail on warnings $fail" 

def warnings = new WarningsList().setScanBounds(scanBounds).setScanSize(scanSize).setScanDPI(scanDPI).scan(scanDir)
println warnings.toString()  

if(fail) {
  //We do NOT want to to use System.exit(...) because it can be used to shut down a VM....for example a Jenkins VM or slave service 
  throw new RuntimeException("Warnings detected. We found ${warnings.size()} warning(s)")
}
