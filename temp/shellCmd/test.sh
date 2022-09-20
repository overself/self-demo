for filename in test*1.csv
	do
	  PATH_NAME=${filename##*/}
	  echo ${PATH_NAME} 
	done