#!/usr/bin/groovy

@GrabResolver(name='imaging', root='https://repo.adobe.com/nexus/content/repositories/public/')
@Grapes(
    @Grab(group='org.apache.commons', module='commons-imaging', version='1.0-R1534292')
)

import javax.imageio.ImageIO
import static groovy.io.FileType.FILES
import org.apache.commons.imaging.*

public class WebsiteWarning {
  def associatedPath
  def columnPadding = 16

  public WebsiteWarning(def associatedPath) {
    this.associatedPath = associatedPath
  }

  public String toString() {
    "[ImageChecker] WARNING "
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
    def label = "Size".padRight(columnPadding);
    def result = "${sizeRecorded} kB".padLeft(columnPadding)
    def max = "max(${maxImageSize} kB)".padLeft(columnPadding)
    def spacing = "".padLeft(columnPadding)
    return "${super.toString()} $label ${result} $max$spacing $associatedPath" //Max image size is ${maxImageSize} kb. Was $sizeRecorded kb"
  }
}

public class UnreadableWarning extends WebsiteWarning {
  def ex, associatedPath

  public UnreadableWarning(def ex, def associatedPath) {
    super(associatedPath)
    this.ex = ex
  }

  public String toString() {
    "[ImageChecker] Error Unable to read path $associatedPath message was ${ex.message}"
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
    def label = "DPI".padRight(columnPadding);
    def result = "${recordedDpi}".padLeft(columnPadding)
    def max = "min(${minDpi})".padLeft(columnPadding)
    def spacing = "".padLeft(columnPadding)
    return "${super.toString()} $label ${result} $max$spacing $associatedPath" //Max image size is ${maxImageSize} kb. Was $sizeRecorded kb"
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
    def label = "Resolution".padRight(columnPadding);
    def result = "${x}x${y.toString()}".padLeft(columnPadding)
    def max = "max(${maxx}x${maxy})".padLeft(columnPadding)
    def spacing = "".padLeft(columnPadding)
    return "${super.toString()} $label ${result} $max$spacing $associatedPath" //Max image size is ${maxImageSize} kb. Was $sizeRecorded kb"
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

  def scanResolution(file) {
    if (this.scanBounds) {
      def img = ImageIO.read(file);
      if (img == null) {
        println "[ImageChecker] Warning Unable to check $file.name"
      } else if ( img.getWidth() > maxImageWidth || img.getHeight() > maxImageHeight ){
        def warn = new DimensionWarning(img.getWidth(), img.getHeight(), maxImageWidth, maxImageHeight, file);
        this.add(warn)
        return warn
      }
    }
    return null
  }

  def scanDPI(file) {
    if (this.scanDPI) {
      def metadata = Imaging.getImageInfo(file)
      if(metadata != null && metadata.getPhysicalWidthDpi() != -1 && metadata.getPhysicalHeightDpi() != -1) {
        if(minDPI > metadata.getPhysicalWidthDpi()) {
          def warn = new DpiWarning(minDPI, metadata.getPhysicalWidthDpi(), file);
          this.add(warn)
          return warn
        }
      }
    }
    return null
  }

  def scanSize(file) {
    if (this.scanSize) {
      if(((int)file.length() / 1000) > imageMaxSize) {
        def warn = new SizeWarning(file, imageMaxSize, (int)(file.length()/1024))
        this.add(warn)
        return warn
      }
    }
    return null
  }

  def scan(def rootPath) {
    new File(rootPath).eachFileRecurse(FILES) {
      if( it.name ==~ /([^\s]+(\.(?i)(jpg|png|gif|bmp))$)/ ) {
        try {
          def res = scanResolution(it)
          def dpi = scanDPI(it)
          def size = scanSize(it)
          if (res == null && dpi == null && size == null) {
            println "[ImageChecker] OK $it"
          }
        } catch (Exception ex) {
          this.add(new UnreadableWarning(ex, it))
        }
      }      
    }
    return this
  }

  @Override
  public String toString() {
    return this.join("\n")
  }

  def prettyPrint(biggestPath) {
    this.each {
      def padding = this.associatedPath.getAbsolutePath().length() - biggestPath

    }
  }
}

def cli = new CliBuilder(usage: 'imageSizeChecker [options]', header:'Options:')
cli.help('print help message')
cli.target(args:1, argName: 'targetFolder', 'Root folder to look for images')
cli.size(args:1, argName: 'maxsize', 'Check file size of images in kb. Default: 500')
cli.dpi(args:1, argName: 'mindpi', 'Check dpi of images. Default 0')
cli.res(args:2, valueSeparator:'x', argName: 'maxres', 'Check dimensions of image "widthxheight" for example 1920x1080. Default: 2000x2000')
cli.fail('fail on warnings')
def options = cli.parse(args)

if(!options) {
  cli.usage()
} else if(options.help) {
  cli.usage()
} else {
  def scanDir
  def scanBounds
  def scanSize
  def scanDPI
  def warnings

  try {

    scanDir = options.target ?: "/home/jenkins/site/"
    scanBoundsEnv = System.getenv("SCAN_BOUNDS") ?: "2000x2000"

    assert scanBoundsEnv.split("x").size() == 2, "Range bound must be Width x Height. For example '-dim 1920x1080'"
    scanBounds = options.ress ?: scanBoundsEnv.split("x")

    assert scanBounds.size() == 2, "Range value must be Width x Height. For example '-dim 1920x1080'"
    assert scanBounds[0].isInteger(), "Width must be an integer"
    assert scanBounds[1].isInteger(), "Height must be an integer"

    scanSize = options.size ?: System.getenv("SCAN_SIZE") ?: "500"
    assert scanSize.isInteger(), "File size must be a valid integer"

    scanDPI =  options.dpi ?: System.getenv("SCAN_DPI") ?: "0"
    assert scanDPI.isInteger(), "DPI specification must be an integer"

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
      println warnings.sort{ it.class }.toString()
    } else {
      println "[ImageChecker] No warnings found"
    }
  }
}

