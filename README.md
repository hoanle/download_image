# Movie Image Display

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)

The project is to allow user to see a list of images from a movie 
  - User can see the byte count when the image is being downloaded
  - User can see image displayed after download is done
  - User can tap on each image to go to the next one. When there is no more item, tapping on image will go to the first item
# Tech details
  - Overall, It's not easy to write clean and elegant code because only Android default libraries could be used, which makes it more difficult 
  than usual to write. Besides the one mentioned in the instruction (RxJava, Coroutine, Apache HttpClient), the "default" made it unclear which to use. 
  I decided to go with whatever libraries are provided in the gradle file when the project is created. 
  - The app handle the below cases:
  + Image is not in local, start to download image, show progress on the screen 
  + Image is in local, first display, don't download image, use local file, decode and display. Big file can take time to decode
  + Image is displayed, next time it should appear immediately 
  + Big file is re-sampled to avoid overflow. The resampling can be done **right after download and save as resampled file**, 
  but this will lose original information so i decided to resample **when the app uses it**, so decoding can be slow. 
  - Two images folder are used to make sure files are downloaded successfully. First download use temp folder, it copies downloaded file to
  per folder later, and delete the file in temp folder. 
  - RxJava is used to handle thread for heavy task (downloading, decoding...) and some reporting events
  
# Testing
  - **Junit4**(4.12) is used. 
  - **Espresso** is used to test some UI coponent. 
  - Only simple test of views are done due to the lack of tools and utilities (mock, rule)

### Installation

The project should be installed directly on emulator or Android devices without issues

### Todos
 - Improve UIs
 - Add Night Mode
 - Optimise code
 - More test cases
License
----

MIT
