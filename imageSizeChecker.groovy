@GrabResolver(name='imaging', root='https://repo.adobe.com/nexus/content/repositories/public/')
@Grapes(
    @Grab(group='org.apache.commons', module='commons-imaging', version='1.0-R1534292')
)

import groovy.util.CliBuilder
import java.io.File
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
    "[ImageChecker] (Warning) File ${associatedPath}. Max image size is ${maxImageSize} kb. Was $sizeRecorded kb"
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

  @Override
  public String toString() {
    "[ImageChecker] (Warning) File ${associatedPath}. Too low DPI. Is $recordedDpi should be equal or greater than $minDpi"
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
    "[ImageChecker] (Warning) File ${associatedPath}. Max width is ${maxy}px was ${y}px. Max height is ${maxx}px was ${x}px"
  }    
} 

class WarningsList extends ArrayList<WebsiteWarning> {

  int imageMaxSize
  int maxImageWidth
  int maxImageHeight
  int minDPI

  boolean scanBounds = true
  boolean scanSize = false
  boolean scanDPI = false

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
              if(minDPI > metadata.getPhysicalWidthDpi()) {
                this.add(new DpiWarning(minDPI, metadata.getPhysicalWidthDpi(), it))
              }
            }
          }
          
        } catch (Exception ex) {
          ex.printStackTrace(System.out)
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

def cli = new CliBuilder(usage: 'imageSizeChecker [options]', header:'Options:')
cli.help('print help message')
cli.target(args:1, argName: 'targetFolder', 'Root folder to look for images')
cli.size(args:1, argName: 'maxsize', 'Check file size of images in kb. Default: 500')
cli.dpi(args:1, argName: 'mindpi', 'Check dpi of images. Default 0')
cli.dim(args:2, valueSeparator:'x', argName: 'maxdim', 'Check dimensions of image "widthxheight" for example 1920x1080. Default: 2000x2000')
cli.fail('fail on warnings')
def options = cli.parse(args)

if(options.help) {
  cli.usage()
} else {
  def scanDir
  def scanBounds
  def scanSize
  def scanDPI
  def warnings

  try {

    scanDir = options.target ?: "/home/jenkins/site/"

    scanBounds = options.dims ?: (System.getenv("SCAN_BOUNDS").split("x") ?: [2000,2000])
    scanSize = options.size ?: System.getenv("SCAN_SIZE") ?: 500
    scanDPI =  options.dpi ?: System.getenv("SCAN_DPI") ?: 0

    println "===Options==="
    println "Scan bounds: $scanBounds"
    println "Scan size: $scanSize"
    println "Scan dir: $scanDir"
    println "Scan DPI: $scanDPI"
    println "Fail on warnings ${options.fail}"
    println "=============\n"

    assert scanBounds != null && scanBounds[0] as boolean && scanBounds[1] as boolean : "Image dimension must be specified with the -dim option"

    warnings = new WarningsList(imageMaxSize: scanSize as int, maxImageWidth: scanBounds[0] as int, maxImageHeight: scanBounds[1] as int, minDPI: scanDPI as int)
    warnings.
            setScanBounds(scanBounds as boolean).
            setScanSize(options.size as boolean).
            setScanDPI(options.dpi as boolean).scan(scanDir)

    if(options.fail) {
      //We do NOT want to to use System.exit(...) because it can be used to shut down a VM....for example a Jenkins VM or slave service
      throw new RuntimeException("Warnings detected. We found ${warnings.size()} warning(s)")
    }
  } catch (FileNotFoundException ex) {
    println "The file $scanDir cannot be found"
  } finally {
    if(warnings) {
      println warnings.toString()
    }
  }
}

