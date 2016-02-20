
## Welcome
Welcome to the Video Motion Tracker from [joelbecker.net](http://joelbecker.net)! I hope you find this software useful, fun, interesting, or all of those.

This is free software, under the terms of the GNU General Public License. (See the accompanying LICENSE.TXT.)

## What does it do exactly?
Tracks a virtually unlimited number of moving objects simultaneously, in a live webcam or prerecorded video, giving each object's current bounding rectangle coordinates, pixel count, etc., for each moment in time (each video frame). Possible applications include surveillance, pranks, interactive games, monitoring, traffic analysis. Start out by [watching the demo video](https://www.youtube.com/watch?v=2aFDTZlR7DM)!

You can play with the example Swing application, or use the libraries in your own application. JMF is used for video input.

The algorithm processes each frame of video through multiple stages of image processing to determine what is background and what is foreground (moving objects). Objects are given their bounding rectangles, and a "label" (numeric ID) that can be tracked throughout multiple frames.

The libraries and application are Eclipse Plug-In projects, but if there is enough demand for these to be regular Java or maven projects, I can convert them. So if you want/need them converted just let me know.

## Running
This project requires you to have installed:

 1. A **32-bit** [Java Runtime Environment (JRE) or JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html). A 64-bit JRE will not work because the Java Media Framework (see next step) does not run on a 64-bit JRE.
 1. To be able to run the .bat files in the bin folder on Windows, the 32-bit JRE must be your default JRE (the one in your PATH variable). If you haven't installed another JRE don't worry about that. 
 1. [The Java Media Framework (JMF)](http://www.oracle.com/technetwork/java/javase/setup-138642.html)

On Windows you can run the example application by going to the "bin" folder and running the .bat file of your choice there.

The executable jar is located in the bin folder. Two command-line scripts provide templates for running the motion tracking program with both a video file input, and web-cam input. (Both are specified as a URL on the command line.)


## Source Code
The source code is organized into Eclipse Java projects, under the eclipse_projects folder. Within each project is a "src" folder where the actual Java code resides. Using the Eclipse Java IDE is recommended (http://eclipse.org).


## Installation
This project is organized into a set of Eclipse projects. To use them:


### Creating an Eclipse Workspace
* If you don't already have Eclipse installed, download and install Eclipse Java IDE (http://eclipse.org)
* Right click in the "Package Explorer" view to the left, and select "Import..." from the pop-up menu.
* In the dialog, expand General, and select "Existing projects into workspace". Click "Next"
* Next to "Select root directory:" click the "Browse" button and select the eclipse_projects folder of this project.
* Make sure all of the projects are selected, then click "Finish".

Now you have an Eclipse workspace containing the projects. 
