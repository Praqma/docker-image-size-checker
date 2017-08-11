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
    "[ImageChecker] WARNING"
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
    def label = "Size"
    def result = "${sizeRecorded}kB"
    def spacing = "".padLeft(columnPadding)
    return "${super.toString()} $label ${result} $associatedPath" //Max image size is ${maxImageSize} kb. Was $sizeRecorded kb"
  }
}

public class UnreadableWarning extends WebsiteWarning {
  def ex, associatedPath

  public UnreadableWarning(def ex, def associatedPath) {
    super(associatedPath)
    this.ex = ex
  }

  public String toString() {
    "[ImageChecker] ERROR Unable to read path $associatedPath message was ${ex.message}"
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

  public DimensionWarning(def x, def y, def maxx, def maxy, def associatedPath) {
    super(associatedPath)
    this.y = y
    this.x = x
    this.maxy = maxy
    this.maxx = maxx
  }

  @Override
  public String toString() {
    def label = "Resolution"
    def result = "${x}x${y.toString()}"
    def spacing = "".padLeft(columnPadding)
    return "${super.toString()} $label ${result} $associatedPath" //Max image size is ${maxImageSize} kb. Was $sizeRecorded kb"
  }
}

class WarningsList extends ArrayList<WebsiteWarning> {

  def imageMaxSize
  def maxImageWidth 
  def maxImageHeight
  def minDPI
  def scanSize
  def imagecfg

  def checkDimensions(file, maxWidth, maxHeight) {
    def img = ImageIO.read(file);
    if(img == null) {
      return null
    }
    if (img.getWidth() > maxWidth || img.getHeight() > maxHeight) {
      def warn = new DimensionWarning(img.getWidth(), img.getHeight(), maxImageWidth, maxImageHeight, file);
      return warn
    }   
  }

  def checkExactDimensions(file, width, height) {
    def img = ImageIO.read(file);
    if(img == null) {
      return null
    }
    if (img.getWidth() != width || (height != "none" && img.getHeight() != (int)height)) {
      def warn = new DimensionWarning(img.getWidth(), img.getHeight(), maxImageWidth, maxImageHeight, file);
      return warn
    }   
  }

  def scanResolution(file, config) {
    if(config || maxImageWidth.toString().isInteger() || maxImageHeight.toString().isInteger() ) {
      if (config) { //If the dynamic resolution rule is defined we check exact matches
        config.each { k,v ->
          def matcher = (file =~ v.regex)
          if(matcher.getCount() > 0) {
            //if the match included the Scale modifier
            //I.e 4x2 In this case the height must be 2/4 ~1/2 of the width
            def scaledHeight = v.width   
            if(matcher[0][2]) {
              def op1 = matcher[0][2].split("x")[0].toInteger()
              def op2 = matcher[0][2].split("x")[1].toInteger()
              //Remove everything after the decimal
              scaledHeight = (v.width.toInteger() / op1 * op2).toInteger() 
            }
            def warn = checkExactDimensions(file, v.width.toInteger(), scaledHeight)
            if(warn) {
              this.add(warn)
              return warn
            }
          }  
        } 
      } else {
        def warn = checkDimensions(file, maxImageWidth.toInteger(), maxImageHeight.toInteger())
        if(warn){
          this.add(warn)
          return warn
        }
      }
    }
    return null
  }

  def scanDPI(file) {
    if (minDPI) {
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
    if(imageMaxSize) {
      if(((int)file.length() / 1000) > (imageMaxSize as int)) {
        def warn = new SizeWarning(file, imageMaxSize as int, (int)(file.length()/1024))
        this.add(warn)
        return warn
      }
    }
    return null
  }

  def scan(def rootPath) {
    println imagecfg
    def config
    if(imagecfg) {
      config = new groovy.json.JsonSlurper().parse(new File(imagecfg)) 
    }
    new File(rootPath).eachFileRecurse(FILES) {
      if( it.name ==~ /([^\s]+(\.(?i)(jpg|png|gif|bmp))$)/ ) {
        try {
          def res = scanResolution(it, config)
          def dpi = scanDPI(it)
          def size = scanSize(it)
          if (res == null && dpi == null && size == null) {
            println "[ImageChecker] OK $it"
          }
        } catch (Exception ex) {

          this.add(new UnreadableWarning(ex, it))
          throw ex
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
cli.h(longOpt: 'help', 'print help message')
cli.t(longOpt: 'target', args:1, argName: 'targetFolder', 'Root folder to look for images')
cli.s(longOpt: 'filesize', args:1, argName: 'maxsize', 'Check file size of images in kb. Default: 0')
cli.d(longOpt: 'dpi', args:1, argName: 'mindpi', 'Check dpi of images. Default 0')
cli.x(longOpt: 'maxwidth', args:1, argName: 'maxwidth', 'Max image width, for example: 1920 (always in pixels)')
cli.y(longOpt: 'maxheight', args:1, argName: 'maxheight', 'Max image width, for example: 1080 (always in pixels)')
cli.c(longOpt: 'imageconfig', args:1, argName: 'config', 'Use a defined set of rules based on image name to have dynamic per-picture max limits')
cli.f(longOpt: 'fail', 'fail on warnings')
def options = cli.parse(args)

if(!options) {
  cli.usage()
} else if(options.h) {
  cli.usage()
} else {
  try {

    scanDir = options.target ?: "/home/jenkins/site/"

    println "Max file size     : ${options.s ? options.s : 0} kb"
    println "Image resolution  : ${options.c ? "Specified by ${options.c}" : "${options.x}x${options.y}"}"
    println "Min DPI:          : ${options.dpi}" 
    println "Fail on warning   : ${options.f}"

    warnings = new WarningsList(
      imagecfg: options.c, 
      imageMaxSize: options.s, 
      maxImageWidth: options.x, 
      maxImageHeight: options.y,
      minDPI: options.d
    )
    warnings.scan(scanDir)
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

