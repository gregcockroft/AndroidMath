
# Information on the implementation 

Adding fonts
----------------------------------

The library needs Math OpenType Format fonts for display.
The fonts files are stored in mathdisplaylib/src/main/assets/fonts.

The [MTFontManager](https://rawgit.com/gregcockroft/AndroidMath/master/mathdisplaylib/doc/com.agog.mathdisplay/-m-t-font-manager/index.html)
class is used to change fonts.
There are convenience functions to load the 3 included fonts but any new font could be selected using fontWithName() after adding to the assets folder.

Test Case Images
----------------------------------
The drawinstrumented and viewinstrumented test cases generate png files for visual inspection.
These get stored in the download directory on the device or emulator.

For example the test case testSimpleVariable creates a file testSimpleVariable.png.
The file can viewed by retrieving to a host computer and opening in an image viewer.
Example on Mac

```
rm -r /tmp/img
mkdir /tmp/img
cd /tmp/img
for image in testSimpleVariable.png testMultipleVariables.png 
do
    adb pull /storage/emulated/0/Download/$image $image
done

open /tmp/img

```


Missing TeX Commands 
----------------------------------
These aren't parsed yet.<BR>
[atopwithdelims](https://www.tutorialspoint.com/tex_commands/atopwithdelims.htm)<br>
[mkern](https://www.tutorialspoint.com/tex_commands/mkern.htm)


