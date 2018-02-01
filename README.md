# GraphML2SBGN-ML - Bidirectional translation between GraphML (yEd Graph Editor) and SBGN-ML

The aim is to provide the most accurate translation for metabolic networks between
[GraphML (yEd)](https://www.yworks.com/products/yed)
and [SBGN-ML](https://sbgn.github.io/sbgn/) file formats.
Translation in both direction is possible. This project should ultimately be integrated into
[SBFC](https://www.ebi.ac.uk/biomodels/tools/converters/).

Compatible formats:
 - SBGN-ML 0.2 (Process Description)
 - yEd 3.17.1

To find more information, please check the [Wiki](https://github.com/ibalaur/GRAPHML2SBGNML/wiki). The javadoc documentation for the source files is available [here](https://github.com/ibalaur/GRAPHML2SBGNML/wiki/Code-documentation) and the full application can be directly donwloaded from here.

Known issues are included in the [issues](https://github.com/ibalaur/GRAPHML2SBGNML/issues)
and the limitations of the tool are listed on the [Limitations](https://github.com/ibalaur/GRAPHML2SBGNML/wiki/Limitations) page from the Wiki.

## Requirements

 - Java 8 (with JavaFX if you want to use the GUI)
 - Maven (tested with Maven 3.5)

## Install

After cloning the repository and getting into its directory:

`mvn clean`

`mvn install`

A small GUI is also provided as the main class of the package. It can be launched by double clicking on the jar or by
directly calling the package with `java -jar`. Be sure to have JavaFX working in your Java distribution.

With the scripts, all log messages will go to System.out. With the GUI, everything will be written in
the selected log file.

## Contributions and issues

If you have any suggestions or want to report a bug, don't hesitate to create an [issue](https://github.com/ibalaur/GRAPHML2SBGNML/issues_.
Pull requests and all forms of contribution will be warmly welcomed.

## Useful links

 - More details about the [SBGN-ML format](https://github.com/sbgn/sbgn/wiki/SBGN_ML)
 - [SBFC doc](http://sbfc.sourceforge.net/mediawiki/index.php/Main_Page)
 - The [yEd Graph Editor](https://www.yworks.com/products/yed) framework
 - A useful tool for translation between [CellDesigner](http://www.celldesigner.org/) to SBGN-ML (bidirectional conversion) is [CD2SBGMML](https://github.com/royludo/cd2sbgnml)
 - More details about the [eTRIKS](https://www.etriks.org/) Project and the [Disease Maps](http://disease-maps.org/) Project 

## Acknowledgements

This work has been initiated at [EISBM](http://www.eisbm.org/) in collaboration with Dr. Alexander Mazein and Ludovic Roy, with Vasundra Toure from [NTNU](https://www.ntnu.edu/about), and with the Disease Maps community.

This work has been supported by the Innovative Medicines Initiative Joint Undertaking under grant agreement no. IMI 115446 (eTRIKS), resources of which are composed of financial contribution from the European Union’s Seventh Framework Programme (FP7/2007-2013) and EFPIA companies.


