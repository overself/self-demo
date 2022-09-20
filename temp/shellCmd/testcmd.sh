for filename in ./001/002/*test*.csv
do
  #PATH_NAME=${filename}
  PATH_NAME=${filename##*/}
  CODE_TMP=`echo ${PATH_NAME} | cut -d '-' -f 2`
  TEMP_FILE="./001/${CODE_TMP}_CODE.tmp"
  touch $TEMP_FILE
  echo "${filename##*/}"
  if [ $? -ne 0 ]; then
      echo ${TEMP_FILE}_OK
  fi
  for datfilename in ./001/002/*test*.csv; #*test*.csv
  do
  	echo "${datfilename}"
    while read line
    do
      # OUTPUT_FLG=`echo ${line} | awk -F "," '{print $3}'`
      # echo $RESULT_CDE - $OUTPUT_FLG
      # echo "${line}"
      # awk -F "," '{print $0}' ./001/002/$PATH_NAME
      awk -F "," '{print $1,$2}' > /home/cmd/001/CODE_${datfilename##*/}.tmp
    done < $datfilename
  done
done

