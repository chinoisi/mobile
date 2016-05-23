
## retrieve files
#wget -O index.xml http://packs.shtooka.net/cmn-caen-tan/mp3/index.xml

## create csv files without translation
#sed "s#.*DOCTYPE.*##gi" -i index.xml
#xsltproc transform.xslt index.xml | sed "s#	##gi" > all.csv
#grep "HSK niveau I$" all.csv | cut -d, -f1-4 > hsk1.csv
#grep "HSK niveau II$" all.csv | cut -d, -f1-4 > hsk2.csv
#grep "HSK niveau III$" all.csv | cut -d, -f1-4 > hsk3.csv
#grep "HSK niveau IV$" all.csv | cut -d, -f1-4 > hsk4.csv

## append translation
function doTranslation() {
file=$1
echo "Do translation for file : $file"
cat $file| while read line; 
do 
zh=$(echo $line | cut -d, -f1)
pin=$(echo $line | cut -d, -f2)
trans=$(echo $line | cut -d, -f3)
audio=$(echo $line | cut -d, -f4)

if [ "$trans" == "TO TRANSLATE" ]
then
   transAndSyn=`./trans  :fr $zh -show-translation Y -show-translation-phonetics N -show-prompt-message N -show-languages N -show-original-dictionary N  -show-original N -show-alternatives Y -no-ansi`
   trans=`echo $transAndSyn | sed "s# ${zh}.*##gi"`
   syn=`echo $transAndSyn |  sed "s#.*${zh}##gi"` 
fi
echo $zh,$pin,$trans,$audio,\"$syn\">>  $file.tmp
echo $zh,$pin,$trans,$audio,\"$syn\"
sleep 2
done
mv $file.tmp $file
}

#doTranslation hsk1.csv
#doTranslation hsk2.csv
#doTranslation hsk3.csv
doTranslation hsk4.csv





