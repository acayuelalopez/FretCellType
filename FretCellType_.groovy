import ij.CompositeImage
import ij.IJ
import ij.ImagePlus
import ij.ImageStack
import ij.measure.Calibration
import ij.measure.ResultsTable
import ij.process.FloatProcessor
import ij.process.StackStatistics
import loci.plugins.BF
import loci.plugins.in.ImporterOptions
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer
import net.haesleinhuepf.clij2.CLIJ2
import net.haesleinhuepf.clij2.utilities.CLIJUtilities.*

#@File(label = "Input File Directory", style = "directory") inputFilesDir
#@File(label = "Output directory", style = "directory") outputDir
#@Integer(label = "Channel number to use for segmentation", value = 3) segmentChannel
#@Integer(label = "Channel number to use for donor", value = 1) donorChannel
#@Integer(label = "Channel number to use for acceptor (FRET)", value = 2) acceptorChannel
#@Integer(label = "Channel number to use for acceptor", value = 3) acceptorChannel2
#@Integer(label = "Max Intensity", value = 65534) maxIntensity
#@Double(label = "Small DoG sigma", value = 1.2) gaussianSigma
#@Double(label = "Large DoG sigma", value = 6.0) largeDoGSigma
#@Boolean(label = "TopHat background subtraction?", value = true) topHat
#@Double(label = "TopHat sigma", value = 8.0) topHatSigma
#@Boolean(label = "Manually set threshold?", value = false) manualSegment
#@Double(label = "Manual threshold", value = 2000.0) manualThreshold

//def inputFilesDir = new File("/home/anaacayuela/Ana_pruebas_imageJ/irene_cambridge/images/sub_folder");
//def outputDir = new File("/home/anaacayuela/Ana_pruebas_imageJ/irene_cambridge/results")
//def segmentChannel = 3.intValue();
//def donorChannel = 1.intValue();
//def acceptorChannel = 2.intValue();
//def acceptorChannel2 = 3.intValue();
def thresholdMethod = "Otsu"
//def maxIntensity = 65534.intValue()
//def gaussianSigma = (1.2).doubleValue();
//def largeDoGSigma = (6.0).doubleValue();
//def topHat = true;
//def topHatSigma = (8.0).doubleValue();
//def manualSegment = false.booleanValue();
//def manualThreshold = 2000.doubleValue();
def makeNearProj = false
//new ij.ImageJ().setVisible(true);
//def table = null;
def FRETimp2 = null;
def FRETProjImp = null;
def labelImp = null;

def listOfFiles = inputFilesDir.listFiles();
for (def i = 0; i < listOfFiles.length; i++) {
    IJ.log("Analyzing Image (.lif):           " + listOfFiles[i].getName());
    def lifDirect = new File(outputDir.getAbsolutePath() + File.separator + listOfFiles[i].getName());

    if (!lifDirect.exists()) {

        try {
            lifDirect.mkdir();

        } catch (SecurityException se) {
            // handle it
        }

    }

    def options = new ImporterOptions();
    options.setId(inputFilesDir.getAbsolutePath() + File.separator + listOfFiles[i].getName());
    options.setSplitChannels(false);
    options.setSplitTimepoints(false);
    options.setSplitFocalPlanes(false);
    options.setAutoscale(true);
    options.setStackFormat(ImporterOptions.VIEW_HYPERSTACK);
    options.setStackOrder(ImporterOptions.ORDER_XYCZT);
    options.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
    options.setCrop(false);
    options.setOpenAllSeries(true);
    def imps = BF.openImagePlus(options);
    for (int j = 0; j < imps.length; j++) {


        IJ.log("             Serie :           " + imps[j].getTitle());
        def serieDirect = new File(lifDirect.getAbsolutePath() + File.separator + imps[j].getTitle());

        if (!serieDirect.exists()) {

            try {
                serieDirect.mkdir();

            } catch (SecurityException se) {
                // handle it
            }

        }


        ClearCLBuffer gfx1 = null;
        ClearCLBuffer gfx2 = null;
        ClearCLBuffer gfx3 = null;
        ClearCLBuffer gfx4 = null;
        ClearCLBuffer gfx5 = null;
        ClearCLBuffer gfx6 = null;
        ClearCLBuffer gfx7 = null;

        def clij2 = CLIJ2.getInstance()
        clij2.clear()


        //#get the current image
        def imp1 = imps[j];

        //#define inputs (to be put in a dialog if I automate)


//        options = previewDialog(imp1)
//        IJ.log(options)
        ////////////////////////////////////////Preview Dialog
//        def cal = imp1.getCalibration();
//        def pixelAspect = (cal.pixelDepth / cal.pixelWidth).doubleValue()
//
//        def originalTitle = imp1.getTitle()

        ///////////////////////comment

        ////////////////////////end of preview
        def totalFrames = (imp1.getNFrames() + 1).intValue()

        //#table is the final results table
        def table = new ResultsTable()

        clij2 = CLIJ2.getInstance()
        clij2.clear()

        //#get the pixel aspect for use in zscaling kernels for filters
        def cal = imp1.getCalibration()
        def pixelAspect = (cal.pixelDepth / cal.pixelWidth).doubleValue()
        def originalTitle = imp1.getTitle()

        def conThresholdStack = new ImageStack(imp1.width, imp1.height)
        def conFRETImp2Stack = new ImageStack(imp1.width, imp1.height)
        def conFRETProjImpStack = new ImageStack(imp1.width, imp1.height)
        def conlabelImpStack = new ImageStack(imp1.width, imp1.height)
        def conNearZStack = new ImageStack(imp1.width * 2, imp1.height * 2)

        for (def nFrame = 1.intValue(); nFrame < totalFrames; nFrame++) {
            clij2.clear()
            def segmentImp = extractChannel(imp1, segmentChannel, nFrame)
            gfx1 = clij2.push(segmentImp)
            gfx2 = clij2.create(gfx1)
            gfx3 = clij2.create(gfx1)
            gfx4 = clij2.create(gfx1)
            gfx5 = clij2.create(gfx1)
            segment(clij2, gfx1, gfx2, gfx3, gfx4, gfx5, gaussianSigma, thresholdMethod, maxIntensity, largeDoGSigma, pixelAspect, originalTitle, topHat, topHatSigma, manualSegment, manualThreshold)

            def thresholdImp = clij2.pull(gfx3)
            IJ.setMinAndMax(thresholdImp, 0, 1)
            thresholdImp.setCalibration(cal)
            thresholdImp.setTitle("Binary mask of " + originalTitle)

            labelImp = fretCalculations(serieDirect.getAbsolutePath(), clij2, totalFrames, cal, imp1, maxIntensity, nFrame, donorChannel, acceptorChannel, acceptorChannel2, table, gfx5, gfx2, gfx3, gfx4, gfx1, originalTitle);
            if (makeNearProj) {
                def nearZImp = nearestZProject(FRETimp2)
                conNearZStack = concatStacks(conNearZStack, nearZImp)
                nearZImp.close()
            }

            //# add the images to concatenated stacks
            conThresholdStack = concatStacks(conThresholdStack, thresholdImp)
//            conFRETImp2Stack = concatStacks(conFRETImp2Stack, FRETimp2)
//            conFRETProjImpStack = concatStacks(conFRETProjImpStack, FRETProjImp)
            conlabelImpStack = concatStacks(conlabelImpStack, labelImp)

            thresholdImp.close()
            //FRETimp2.close()
            //FRETProjImp.close()
            labelImp.close()

            //#Show the images and make the images pretty... I should have put in a function`

            def conThresholdImp = new ImagePlus("Threshold image for " + originalTitle, conThresholdStack)
            conThresholdImp.setDimensions(1, imp1.getNSlices(), imp1.getNFrames())
            IJ.setMinAndMax(conThresholdImp, 0, 1)
            conThresholdImp.setCalibration(cal)
            conThresholdImp = new CompositeImage(conThresholdImp, CompositeImage.COMPOSITE)
            //conThresholdImp.show()
            IJ.saveAsTiff(conThresholdImp, serieDirect.getAbsolutePath() + File.separator + conThresholdImp.getTitle())


//            def conFRETImp2 = new ImagePlus( "Emission ratios X1000 of "+ originalTitle, conFRETImp2Stack)
//            conFRETImp2.setDimensions(1, imp1.getNSlices(), imp1.getNFrames())
//            conFRETImp2.setCalibration(cal)
//            def stats=new StackStatistics(conFRETImp2)
//            conFRETImp2 = new CompositeImage(conFRETImp2, CompositeImage.COMPOSITE)
//            IJ.setMinAndMax(conFRETImp2, 500, 3000)
//            conFRETImp2.show()
//            IJ.run("16_colors")


//            def conFRETProjImp= new ImagePlus( "Max Z  projection of emission ratios X1000 of "+ originalTitle, conFRETProjImpStack)
//            conFRETProjImp.setDimensions(1, 1, imp1.getNFrames())
//            conFRETProjImp.setCalibration(cal)
//            stats=new StackStatistics(conFRETProjImp)
//            IJ.setMinAndMax(conFRETProjImp, 500, 3000)
//            conFRETProjImp = new CompositeImage(conFRETProjImp, CompositeImage.COMPOSITE)
//            conFRETProjImp.show()
//            IJ.run("16_colors")

            def conlabelImp = new ImagePlus("Label map " + originalTitle, conlabelImpStack)
            conlabelImp.setDimensions(1, imp1.getNSlices(), imp1.getNFrames())
            conlabelImp.setCalibration(cal)
            stats = new StackStatistics(conlabelImp)
            conlabelImp = new CompositeImage(conlabelImp, CompositeImage.COMPOSITE)
            IJ.setMinAndMax(conlabelImp, 0, stats.max)
            //conlabelImp.show()
            //IJ.run(conlabelImp, "glasbey_inverted", "");
            IJ.saveAsTiff(conlabelImp, serieDirect.getAbsolutePath() + File.separator + conlabelImp.getTitle())


        }
    }
    IJ.log("Done!!!  :) mashkinah")

}

ImagePlus extractChannel(ImagePlus imp, int nChannel, int nFrame) {
    """extract a channel from the image, at a given frame returning a new imagePlus labelled with the channel name"""

    def stack = imp.getImageStack()
    def ch = new ImageStack(imp.width, imp.height)
    for (def i = 0.intValue(); i < imp.getNSlices(); i++) {
        def index = imp.getStackIndex(nChannel, i, nFrame)
        ch.addSlice(i.toString(), stack.getProcessor(index))
    }
    def imp3 = new ImagePlus("Channel " + nChannel.toString(), ch).duplicate()
    def stats = new StackStatistics(imp3)
    IJ.setMinAndMax(imp3, stats.min, stats.max)
    return imp3
}

void segment(CLIJ2 clij2, ClearCLBuffer gfx1, ClearCLBuffer gfx2, ClearCLBuffer gfx3, ClearCLBuffer gfx4, ClearCLBuffer gfx5, double gaussianSigma, String thresholdMethod, int maxIntensity, double largeDoGSigma, double pixelAspect, String originalTitle, boolean topHat, double topHatSigma, boolean manualSegment, double manualThreshold) {


//# DoG filter for background normalised segmentation. NB. Kernel is Z-normalised to pixel aspect ratio
    if (topHat) {
        clij2.topHatBox(gfx1, gfx3, topHatSigma, topHatSigma, 2)
        clij2.differenceOfGaussian3D(gfx3, gfx2, gaussianSigma, gaussianSigma, 1 + (gaussianSigma - 1) / pixelAspect, largeDoGSigma, largeDoGSigma, largeDoGSigma / pixelAspect)

    } else {
        clij2.differenceOfGaussian3D(gfx1, gfx2, gaussianSigma, gaussianSigma, 1 + (gaussianSigma - 1) / pixelAspect, largeDoGSigma, largeDoGSigma, largeDoGSigma / pixelAspect)
    }
    if (manualSegment) {
        clij2.threshold(gfx2, gfx3, manualThreshold)

    } else {
        // # auto threshold and watershed to seed the object splitting
        clij2.automaticThreshold(gfx2, gfx3, thresholdMethod)

    }
    clij2.watershed(gfx3, gfx2)


//    # add watershed to original threshold , and then use this to generate a binary image of any ROI lost in
//    watershed process
    clij2.addImages(gfx2, gfx3, gfx5)
    clij2.floodFillDiamond(gfx5, gfx4, 1, 2)
    clij2.replaceIntensity(gfx4, gfx5, 2, 0)

    //# label watershed image
    clij2.connectedComponentsLabelingDiamond(gfx2, gfx4)

//    # dilate all the labeled watershed ROI out(only onto zero labeled pixels) , then multiply
//    this by original binary map , to get labeled ROI
    for (def i = 0.intValue(); i < 30; i++) {
        //# I 'm not sure why it needs the second argument image (gfx3) here... It doesn' t seem to affect the results ...
        clij2.onlyzeroOverwriteMaximumBox(gfx4, gfx3, gfx2)
        clij2.onlyzeroOverwriteMaximumDiamond(gfx2, gfx3, gfx4)
    }

    clij2.multiplyImages(gfx4, gfx3, gfx2)

    //# label the missed ROI then add on the largest value from the other labelled image(so they can be combined)
    def watershedLabelMax = clij2.getMaximumOfAllPixels(gfx2)
    clij2.connectedComponentsLabelingDiamond(gfx5, gfx4)
    clij2.addImageAndScalar(gfx4, gfx5, (1 + watershedLabelMax))
    //# delete the background and combine the two images
    clij2.replaceIntensity(gfx5, gfx4, (1 + watershedLabelMax), 0)
    clij2.maximumImages(gfx4, gfx2, gfx5)


    //# gfx3 = threshold channel, gfx5 = label image, gfx1 = original image, gfx2 & gfx4 & gfx5 are junk
}

ImagePlus fretCalculations(String serieDirect, CLIJ2 clij2, int totalFrames, Calibration cal, ImagePlus imp1, int maxIntensity, int nFrame, int donorChannel, int acceptorChannel, int acceptorChannel2, ResultsTable table, ClearCLBuffer gfx1, ClearCLBuffer gfx2, ClearCLBuffer gfx3, ClearCLBuffer gfx4, ClearCLBuffer gfx5, String originalTitle) {
    def donorImp = extractChannel(imp1, donorChannel, nFrame)
    def acceptorImp = extractChannel(imp1, acceptorChannel, nFrame)
    def acceptorImp2 = extractChannel(imp1, acceptorChannel2, nFrame)

    //# push donor and acceptor channels to gpu and threshold them both to remove saturated pixels

    gfx4 = clij2.push(donorImp)
    gfx5 = clij2.push(acceptorImp)
    def gfx6 = clij2.create(gfx5)

    clij2.threshold(gfx4, gfx2, maxIntensity)
    clij2.binarySubtract(gfx3, gfx2, gfx6)

    clij2.threshold(gfx5, gfx2, maxIntensity)
    clij2.binarySubtract(gfx6, gfx2, gfx3)

    clij2.threshold(gfx3, gfx6, 0.5)
    clij2.multiplyImages(gfx6, gfx4, gfx2)
    clij2.multiplyImages(gfx6, gfx5, gfx4)


    gfx6 = clij2.push(acceptorImp2)

    //# donor is gfx2 , acceptor FRET is gfx4, segment channel(acceptor normal) is gfx6

    def results = new ResultsTable()
    clij2.statisticsOfBackgroundAndLabelledPixels(gfx2, gfx1, results)

    def donorChIntensity = results.getColumn(13)
    def results2 = new ResultsTable()
    clij2.statisticsOfBackgroundAndLabelledPixels(gfx4, gfx1, results2)
    def acceptorChIntensity = results2.getColumn(13)

    def results3 = new ResultsTable()
    clij2.statisticsOfBackgroundAndLabelledPixels(gfx6, gfx1, results3)

    //# calculate the fret ratios, removing any ROI with intensity of zero
    def vascular1 = 0.doubleValue();
    def stomata = 0.doubleValue(); ;
    def vascular2 = 0.doubleValue(); ;
    def spongyM = 0.doubleValue(); ;
    def palisadeM = 0.doubleValue();
    def counter = 0;
    List<Double> FRET = new ArrayList<Double>();
    for (def i = 0.intValue(); i < acceptorChIntensity.length; i++) {
        if ((acceptorChIntensity[i] > 0) && (donorChIntensity[i] > 0)) {
            //# don 't write in the zeros to the results
            FRET.add((1000 * acceptorChIntensity[i] / donorChIntensity[i]))
            //counter++;
            table.incrementCounter()
            table.addValue("Frame (Time)", nFrame)
            table.addValue("Label", i)
            table.addValue("Emission ratio", acceptorChIntensity[i] / donorChIntensity[i])

            table.addValue("Mean donor emission", results.getValue("MEAN_INTENSITY", i))
            table.addValue("Mean acceptor emission (FRET)", results2.getValue("MEAN_INTENSITY", i))
            table.addValue("Mean acceptor emission", results3.getValue("MEAN_INTENSITY", i))


            table.addValue("Sum donor emission", donorChIntensity[i])
            table.addValue("Sum acceptor emission (FRET)", acceptorChIntensity[i])
            table.addValue("Sum acceptor emission", results3.getValue("SUM_INTENSITY", i))


            table.addValue("Volume", cal.pixelWidth * cal.pixelHeight * cal.pixelDepth * results.getValue("PIXEL_COUNT", i))
            table.addValue("Pixel count", results.getValue("PIXEL_COUNT", i))
            table.addValue("x", cal.pixelWidth * results.getValue("CENTROID_X", i))
            table.addValue("y", cal.pixelHeight * results.getValue("CENTROID_Y", i))
            table.addValue("z", cal.pixelDepth * results.getValue("CENTROID_Z", i))

            if (results.getValue("PIXEL_COUNT", i) >= 0
                    && results.getValue("PIXEL_COUNT", i) <= 20) {
                table.addValue("LABEL", "Vascular_Bundle");
                table.addValue("FINAL_PIXEL_COUNT", results.getValue("PIXEL_COUNT", i));
                vascular1++;
            }
            if (results.getValue("PIXEL_COUNT", i) >= 21
                    && results.getValue("PIXEL_COUNT", i) <= 80) {
                table.addValue("LABEL", "Stomata");
                table.addValue("FINAL_PIXEL_COUNT",
                        results.getValue("PIXEL_COUNT", i) / 2.0);
                stomata++;
            }
            if (results.getValue("PIXEL_COUNT", i) >= 81
                    && results.getValue("PIXEL_COUNT", i) <= 91) {
                table.addValue("LABEL", "Blundle_Sheat");
                table.addValue("FINAL_PIXEL_COUNT", results.getValue("PIXEL_COUNT", i));
                vascular2++;
            }
            if (results.getValue("PIXEL_COUNT", i) >= 92
                    && results.getValue("PIXEL_COUNT", i) <= 400) {
                table.addValue("LABEL", "Spongy Mesophyl");
                table.addValue("FINAL_PIXEL_COUNT", results.getValue("PIXEL_COUNT", i));
                spongyM++;
            }
            if (results.getValue("PIXEL_COUNT", i) >= 400) {
                table.addValue("LABEL", "Palisade Mesophyl");
                table.addValue("FINAL_PIXEL_COUNT", results.getValue("PIXEL_COUNT", i));
                palisadeM++;
            }

            table.addValue("TYPE_PROB", (100 * nFrame) / (totalFrames));
            double prob = (100 * nFrame) / (totalFrames);
            if (prob <= 30.0)
                table.addValue("TYPE_PROB_LABEL", "Stomata");
            if (prob >= 30.0 && prob <= 70.0)
                table.addValue("TYPE_PROB_LABEL", "Palisade/Spongy Mesophyl");
            if (prob >= 70.0)
                table.addValue("TYPE_PROB_LABEL", "1/2 Vascular");

            table.addValue("File name", originalTitle)
        } else {
            FRET.add(Double.valueOf(0.0));
        }

    }

    //table.show("Results of " + originalTitle)
    table.saveAs(serieDirect + File.separator + "Results_of_" + originalTitle + ".csv");
    IJ.log("                      " + "Results_of_" + originalTitle + ".csv  saved in "
            + serieDirect.toString());

    def sumTable = new ResultsTable();
    sumTable.setValue("Vascular_Bundle", 0, vascular1);
    sumTable.setValue("Stomata", 0, stomata);
    sumTable.setValue("Bundle_Sheat", 0, vascular2);
    sumTable.setValue("Spongy Mesophyl", 0, spongyM);
    sumTable.setValue("Palisade Mesophyl", 0, palisadeM);

    sumTable.saveAs(serieDirect + File.separator + "Summary_"
            + originalTitle + ".csv");

    IJ.log("                      Summary_" + originalTitle + ".csv   saved in "
            + serieDirect);
    FRET.set(0, (double) 0);
    float[] FRETarray = new float[FRET.size()];
    for (int p = 0; p < FRETarray.length; p++)
        FRETarray[p] = FRET.get(p).floatValue();
    def fp = new FloatProcessor(FRET.size(), 1, FRETarray);
    def FRETImp = new ImagePlus("FRETImp", fp);
    gfx4 = clij2.push(FRETImp);
    clij2.replaceIntensities(gfx1, gfx4, gfx5);
    def maxProj = clij2.create(gfx5.getWidth(), gfx5.getHeight(), 1);
    clij2.maximumZProjection(gfx5, maxProj);

//
//
//    # pull the images
//
    FRETimp2 = clij2.pull(gfx5)
    FRETProjImp = clij2.pull(maxProj)
    labelImp = clij2.pull(gfx1)
    //labelImp.show();
    clij2.clear()
    donorImp.close()
    acceptorImp.close()
    acceptorImp2.close()
    return labelImp;
}

ImagePlus nearestZProject(ImagePlus imp1) {
    def relicedImp = new ij.plugin.Slicer().reslice(imp1)
    def relicedStack = relicedImp.getStack()
    def width = imp1.getWidth()
    def height = imp1.getHeight()
    def depth = imp1.getNSlices()
    float[] topPixels = new float[width * height];
    Arrays.fill(topPixels 0.0.floatValue());
    def stack2 = new ImageStack(width, height)
    for (def i = 1.intValue(); i < relicedImp.getNSlices(); i++) {
        def pixels = (float[]) relicedStack.getPixels(i)
        for (def x = 0.intValue(); x < width; x++) {
            for (def pixel = x.intValue(); pixel < x + width * (depth - 1); pixel += width) {

                //# after finding the first pixel above the threshold value , add the value to the list
                if (pixels[pixel] != 0)
                    topPixels[i * width + x] = pixels[pixel]

                break
            }
        }
    }
    def ip2 = new FloatProcessor(width, height, topPixels)
    def imp2 = new ImagePlus("Nearest point proj", ip2)
    def imp3 = imp2.resize(imp2.getWidth() * 2, imp2.getHeight() * 2, 'none')
    return imp3
}

ImageStack concatStacks(ImageStack masterStack, ImagePlus impToAdd) {
//#takes an IMP and adds it to a stack, returning the concatenated stack
    def impToAddStack = impToAdd.getImageStack()
    for (def i = 1.intValue(); i < impToAdd.getNSlices() + 1; i++)
        masterStack.addSlice(impToAddStack.getProcessor(i))

    return masterStack
}
