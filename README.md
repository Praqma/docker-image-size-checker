---
Maintainer:
- MadsNielsen
---

# image-size-checker

Groovy script to check various attributes of images, all of which determine the `quality` of the image. We can check the following:
* File size
* Min DPI for the image (useful if it is for print)
* Resolution of image


## Running the script

Showing help.

	groovy imageSizeChecker.groovy -help


Checking no image greater than 100kb and no greater resolution than 1920x1080 (Full HD) in folder `/tmp/site`.

	groovy imageSizeChecker.groovy -res 1920x1080 -target /tmp/site -size 100

Fail if warnings exists. 
	
	groovy imageSizeChecker.groovy -res 1920x1080 -target /tmp/site -size 100 -fail

## Example output

	[ImageChecker] WARNING  Resolution              1772x1535   max(1080x1929)                 /tmp/site/images/customers/philips.jpg
	[ImageChecker] WARNING  Resolution               1242x800   max(1080x1929)                 /tmp/site/images/people/jovirt.png
	[ImageChecker] WARNING  Resolution               1360x901   max(1080x1929)                 /tmp/site/images/people/monaslarsen.png
	[ImageChecker] WARNING  Resolution              2322x4128   max(1080x1929)                 /tmp/site/images/stories/karen-shandler-gitmerge.jpg
	[ImageChecker] WARNING  Resolution              1500x2000   max(1080x1929)                 /tmp/site/images/stories/busy-lego-building.jpg
	[ImageChecker] WARNING  Resolution              1286x2000   max(1080x1929)                 /tmp/site/images/stories/hands-on-lego.jpg
	[ImageChecker] WARNING  Resolution              1504x2000   max(1080x1929)                 /tmp/site/images/stories/happy-academy.jpg
	[ImageChecker] WARNING  Resolution              1418x2000   max(1080x1929)                 /tmp/site/images/stories/codeacademycphday1.jpg
	[ImageChecker] WARNING  Resolution              1197x2000   max(1080x1929)                 /tmp/site/images/stories/group-code-academy.jpg
	[ImageChecker] WARNING  Resolution              1248x1582   max(1080x1929)                 /tmp/site/images/stories/github-issue.png
	[ImageChecker] WARNING  Resolution              2322x4128   max(1080x1929)                 /tmp/site/images/stories/jkrag-randomsort.jpg
	[ImageChecker] WARNING  Resolution              1333x2000   max(1080x1929)                 /tmp/site/images/stories/group-of-academy-students.jpg
	[ImageChecker] WARNING  Resolution               806x1934   max(1080x1929)                 /tmp/site/images/stories/bonnie/jules-bkb.jpg
	[ImageChecker] WARNING  Resolution              1094x1459   max(1080x1929)                 /tmp/site/images/stories/bonnie/wolf-with-mug.png
	[ImageChecker] WARNING  Resolution              1990x2778   max(1080x1929)                 /tmp/site/images/stories/fosdem/cauldron_praqma.png
	[ImageChecker] WARNING  Resolution              1857x3200   max(1080x1929)                 /tmp/site/images/stories/fosdem/crazy_schedule.png
	[ImageChecker] WARNING  Resolution              1787x1329   max(1080x1929)                 /tmp/site/images/stories/fosdem/dgsh-committer-plot.png
	[ImageChecker] WARNING  Resolution              1080x2000   max(1080x1929)                 /tmp/site/images/stories/waffle-wip.png
	[ImageChecker] WARNING  Resolution              2000x1315   max(1080x1929)                 /tmp/site/images/stories/sapiens.jpg
	[ImageChecker] WARNING  Resolution               174x1974   max(1080x1929)                 /tmp/site/images/stories/staci_ill_2.png
	[ImageChecker] WARNING  Resolution              2322x4128   max(1080x1929)                 /tmp/site/images/stories/send-more-money-gitmerge.jpg
	[ImageChecker] WARNING  Resolution              2988x4482   max(1080x1929)                 /tmp/site/images/stories/codeconfsthlmpanel.jpg
	[ImageChecker] WARNING  Resolution              1466x2458   max(1080x1929)                 /tmp/site/images/stories/guillaumespeaks.png
	[ImageChecker] WARNING  Resolution              1333x2000   max(1080x1929)                 /tmp/site/images/stories/plan-lego.jpg
	[ImageChecker] WARNING  Resolution              2322x4128   max(1080x1929)                 /tmp/site/images/stories/git-summit.jpg
	[ImageChecker] WARNING  Resolution              2322x4128   max(1080x1929)                 /tmp/site/images/stories/warm-welcome-breakfast.jpg
	[ImageChecker] WARNING  Resolution              1116x1980   max(1080x1929)                 /tmp/site/images/stories/academy-trd/code-academy-trd.png
	[ImageChecker] WARNING  Resolution              1500x2000   max(1080x1929)                 /tmp/site/images/stories/academy-trd/performing.jpg
	[ImageChecker] WARNING  Resolution              2448x3264   max(1080x1929)                 /tmp/site/images/stories/gitmerge/git-manifold.jpg
	[ImageChecker] WARNING  Resolution              2448x3264   max(1080x1929)                 /tmp/site/images/stories/gitmerge/gitless-stats.jpg
	[ImageChecker] WARNING  Resolution              2448x3264   max(1080x1929)                 /tmp/site/images/stories/gitmerge/gitless-vs-git.jpg
	[ImageChecker] WARNING  Resolution              1068x2000   max(1080x1929)                 /tmp/site/images/stories/waffle-done.png
	[ImageChecker] WARNING  Resolution              2448x3264   max(1080x1929)                 /tmp/site/images/stories/gitmerge-microsoft.jpg
	[ImageChecker] WARNING  Resolution              1333x2000   max(1080x1929)                 /tmp/site/images/stories/lots-of-lego.jpg
	[ImageChecker] WARNING  Resolution              1141x1241   max(1080x1929)                 /tmp/site/images/services/WorkshopAgendaHi.png
	[ImageChecker] WARNING  Resolution              1280x1920   max(1080x1929)                 /tmp/site/images/services/assessment.JPG
	[ImageChecker] WARNING  Resolution               1218x999   max(1080x1929)                 /tmp/site/images/services/tools/vagrant.png
	[ImageChecker] WARNING  Size                       216 kB      max(100 kB)                 /tmp/site/images/customers/bkultrasound.png
	[ImageChecker] WARNING  Size                       152 kB      max(100 kB)                 /tmp/site/images/customers/landrover.png
	[ImageChecker] WARNING  Size                       205 kB      max(100 kB)                 /tmp/site/images/customers/philips.jpg
	[ImageChecker] WARNING  Size                       351 kB      max(100 kB)                 /tmp/site/images/people/naesheim.png
	[ImageChecker] WARNING  Size                       265 kB      max(100 kB)                 /tmp/site/images/people/madsnielsen.png
	[ImageChecker] WARNING  Size                       374 kB      max(100 kB)                 /tmp/site/images/people/randomsort.png
	[ImageChecker] WARNING  Size                      1263 kB      max(100 kB)                 /tmp/site/images/people/jovirt.png
	[ImageChecker] WARNING  Size                       306 kB      max(100 kB)                 /tmp/site/images/people/hoeghh.png
	[ImageChecker] WARNING  Size                       452 kB      max(100 kB)                 /tmp/site/images/people/praqma-thi.png
	[ImageChecker] WARNING  Size                       441 kB      max(100 kB)                 /tmp/site/images/people/thakangbaby.png
	[ImageChecker] WARNING  Size                       317 kB      max(100 kB)                 /tmp/site/images/people/johnmj.png
	[ImageChecker] WARNING  Size                       520 kB      max(100 kB)                 /tmp/site/images/people/andrey9kin.png
	[ImageChecker] WARNING  Size                       364 kB      max(100 kB)                 /tmp/site/images/people/drbosse.png
	[ImageChecker] WARNING  Size                       409 kB      max(100 kB)                 /tmp/site/images/people/martinmosegaard.png
	[ImageChecker] WARNING  Size                       359 kB      max(100 kB)                 /tmp/site/images/people/les-praqma.png
	[ImageChecker] WARNING  Size                       446 kB      max(100 kB)                 /tmp/site/images/people/smadarmhansen.png
	[ImageChecker] WARNING  Size                       378 kB      max(100 kB)                 /tmp/site/images/people/sofusalbertsen.png
	[ImageChecker] WARNING  Size                       395 kB      max(100 kB)                 /tmp/site/images/people/jkrag.png
	[ImageChecker] WARNING  Size                      1109 kB      max(100 kB)                 /tmp/site/images/people/adamhenriques.png
	[ImageChecker] WARNING  Size                      1733 kB      max(100 kB)                 /tmp/site/images/people/carmosin.png
	[ImageChecker] WARNING  Size                      1046 kB      max(100 kB)                 /tmp/site/images/people/ewelinawilkosz2.png

When you run with the `-fail` switch

	[ImageChecker] WARNING  Size                       434 kB      max(100 kB)                 /tmp/site/images/main-banner-mastering-git.png
	Caught: java.lang.RuntimeException: Warnings detected. We found 222 warning(s)
	java.lang.RuntimeException: Warnings detected. We found 222 warning(s)
		at imageSizeChecker.run(imageSizeChecker.groovy:240)

Do not fret that this is listed as an exeception, it's the safest way to exit nonzero with groovy, since using System.exit(...) can be unsafe inside a running VM.


## The Dockerfile 

We've included a dockerfile from which you can create your own image with `docker build -t <tag> .` from this source. The image can then be used by mounting your folder as volume and running the same commands with the `imagecheck` command. 

As an example, we can do the following if we built the image `praqma/image-file-check`

	docker run --rm -v /home/myuser/mysite:/tmp/site praqma/image-file-check imagecheck --resolution=1929x1080 --target=/tmp/site -size 100 --fail

	

