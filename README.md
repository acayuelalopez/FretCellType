# FretCellType
This is a Groovy script version of 'FRETENATOR_(Rowe et al 2022)' containing cell-type based classification analysis.
## Installation Prerequisites
As already happen by installing **'FRETENATOR_(Rowe et al 2022)'**, it is needed to have previously installed the **CLIJ** and **CLIJ2** libraries by activating their corresponding update site through FIJI. 

## How to download 
1. Go to the ``GitHub`` repository
2. Click on ``<Code>``>``Download ZIP``
3. The repo will be found at ``Downloads`` directory.
4. Unzip in the desired location.
5. Extract the ``.groovy`` script file

## Running FretCellType in headless mode through ImageJ/Fiji Windows terminal
``ImageJ-win64.exe --ij2 --headless --run "/absolute_path/to/groovyscript/FretCellType_.groovy" "headless=true, inputFilesDir='/absolute_path/to/inputFiles/images',outputDir='/absolute_path/to/outputDirectory/results',segmentChannel=3,donorChannel=1,acceptorChannel=2,acceptorChannel2=3,maxIntensity=65534,gaussianSigma=1.2,largeDoGSigma=6.0,topHap=true,topHatSigma=8.0,manualSegment=false,manualThreshold=2000.0"``

### Parameters Explanation:
- ``headless`` : true. 
- ``inputFilesDir`` : Directory in which the images (lif,tiff, jpeg... files) to be analyzed are located. ``'/home/anaacayuela/Ana_pruebas_imageJ/margarita/images'``.
- ``outputDir`` : Directory in which the outputs are saved. ``'/home/anaacayuela/Ana_pruebas_imageJ/margarita/results'``
- ``segmentChannel`` : Channel number to do the segmentation (number) by Otsu method to generate a binary map.
- ``donorChannel`` : Donor channel postion (number).
- ``acceptorChannel`` : Acceptor channel postion (number).
- ``acceptorChannel2`` : Acceptor channel 2 postion (number).
- ``maxIntensity`` : max intensity value (number).
-  ``gaussianSigma`` : Channel number to do the segmentation.
-  ``largeDoGSigma`` : DoG filter.
-  ``topHat`` : TopHat background removal filter: ``true`` or ``false``.
-  ``topHatSigma`` : this is a number.
-  ``manualSegment`` : Channel number for manual segmentation: ``true`` or ``false``.
-  ``manualThreshold`` : Threshold value to do the manual segmentation by thresholding (number).
  
-  ## Running through ImageJ/Fiji 
1. Navigate to reach Script Editor tool:
   - By writing ``true`` on the search tool or by ``File``>``New``>``Script...``
     <p align="center">
    <img width="650" height="350" src="https://github.com/cnio-cmu-BioimageAnalysis/cellQuantification_code/assets/83207172/0ad85b7b-d214-41a1-83a3-ac4c9395231b">
    </p>

2. Browse to find the directory in which the corresponding the groovy script is stored: ``FretCellType_.groovy``
    <p align="center">
    <img width="500" height="350" src="https://github.com/cnio-cmu-BioimageAnalysis/cellQuantification_code/assets/83207172/5b34dde0-2f35-4908-85f2-ffc4f89341d5">
    </p>
 
3. Press ``Run`` button to compile the script.
    <p align="center">
    <img width="500" height="350" src="https://github.com/cnio-cmu-BioimageAnalysis/cellQuantification_code/assets/83207172/1886af45-c01a-44d3-804b-30e289a2aa38">
    </p>

4. Then a dialog will be displayed in order to set the input directory path in which the images to be analyzed are stored and the output directory path to save the outputs, along with the processing parameters.
   <p align="center">
    <img width="350" height="400" src="https://github.com/acayuelalopez/FretCellType/assets/83207172/22610bf3-b796-4d28-8462-93ee41081d01">
    </p>


5. A log window will appear to update about the processing status.
  <p align="center">
    <img width="350" height="150" src="https://github.com/cnio-cmu-BioimageAnalysis/cellQuantification_code/assets/83207172/ae08ebc2-a720-451c-8a50-542a708972fa">
    </p>
 
6. Finally, you will be enabled to check the outputs (``CSV table`` and ``ZIP RoiSets`` corresponding for each area of interest) in the output directory previously selected.
  <p align="center">
    <img width="600" height="80" src="https://github.com/cnio-cmu-BioimageAnalysis/cellQuantification_code/assets/83207172/bcd520f9-fed0-44f6-aade-757450d05539">
    </p>

- [x] To check ``ZIP RoiSets`` through ImageJ/Fij, **NOTE THAT** you do not have to unzip these files. Go to ``RoiManager``>``More``>``Open``>``imageTitle_RoiSet``
<p align="center">
    <img width="120" height="150" src="https://github.com/cnio-cmu-BioimageAnalysis/cellQuantification_code/assets/83207172/d6d9a16c-14c8-4867-b925-cfe7031cb12e">
    </p>
