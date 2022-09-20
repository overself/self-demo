#サービス提供事業者コード毎にマージ ソート
for filename in ${BM_BYT_SHK_DAT_WORK}/IVC_PRINT_C_New_TMP_${DETAIL_TYPE}_*PC_${MULTI_ID}_TMP.csv
  do
    PATH_NAME=${filename##*/}
    SERVICE_PROVIDER_CDE_TMP=`echo ${PATH_NAME} | cut-d '_'-f 7`
    #前回ファイル削除
    rm-f${BM_BYT_SHK_DAT_WORK}/IVC_PRINT_C_New_TMP_${DETAIL_TYPE}_${SERVICE_PROVIDER_CDE_TMP}_AC_${MULTI_ID}.tmp
    tm-f${BM_BYT_SHK_DAT_WORK}/IVC_PRINT_C_New_TMP_${DETAIL_TYPE}_${SERVICE_PROVIDER_CDE_TMP}_AC_SORT_${MULTI_ID}.tmp
    #再発行明細書中間ファイル（A臨時ファイル）を作成
    touch ${BM_BYT_SHK_DAT_WORK}/IVC_PRINT_C_New_TMP_${DETAIL_TYPE}_${SERVICE_PROVIDER_CDE_TMP}_AC_${MULTI_ID}.tmp
    if [ $? -ne 0];then
      abnormal_end ${JOB_ID} ${step_name}
    fi
    for datfilename in ${BM_BYT_SHK_DAT_WORK}/ivc_bpr_DETAILS_REQUEST_*.dat
    do
      while read line
      do
        RESULT_CDE=`echo ${line} | awk '{print $1}'`
        OUTPUT_FLG=`echo ${line} | awk '{print $7}'`
        if ["${RESULT_CDE}"=="0000" ] && ["${OUTPUT_FLG}"=="A" -o "${OUTPUT_FLG}" =="C"];then
	        BILL_MONTH=`echo ${line} | awk'{print $5}'`
	        BILL_CYCLE_ID=`echo${line} | awk'{print $6}'`
	        BILL_GROUP_ID=`echo ${line} | awk'{print $4}'`
	        INPUT_SYSTEM_MGM_NBR=`echo ${line} | awk'{print $3}'`
	        if ["${OUTPUT_FLG}"=="C"];then
	          SERVICE_ID=`echo ${line} |-awk'{print $9}'`
	          #入カファイルの「請求年月、請求群、B＃、S＃」が同じ場合、再発行明細書中間ファイル（C臨時ファイル）を出力
	          awk -F"," -v billMonth=${BILL_MONTH} -v billCycleycleld=${=${BILL_CYCLE_ID} -v billGroupId=${BILL_GROUP_ID} -v serviceId=${SERVICE_ID} -v inputNbr=${INPUT_SYST}
	          if ($3==billGroupId && $4==billMonth && $5==billCycleId &$6 ==serviceld) {$NF =inputNbr;print $0}}' 
	          ${BM_BYT_SHK_DAT_WORK}/IVC_PRINT_C_New_236}'
	          	if [ $?-ne 0];then
	              abnormal_end ${JOB_ID} ${step_name}
	            fi
	          fi
	        else
	          #入力ファイルの「請求年月、請求群、B＃」が同じ場合、再発行明細書中間ファイル（A臨時ファイル）を出力
	          awk-F","-v billMonth=${BILL_MONTH}-v billCycllCycleId=${B${BILL_CYCLE_ID}-v billGroupId=${BILL_GROUP_ID} -v inputNbr=${INPUT_SYSTEM_MGM_NBR}'{{OFS=","}
	          if($3==billGroupId &&$4 $4==bill=billMonth &&&&$5==bbillCycleId)-{$NF = inputNbr;print $0}}
	          '${BM_BYT_SHK_DAT_WORK}/IVC_PRINT_C_New_TMP_S{DETAIL_TYPE
	          if [ $? -ne 0 ];then
	            abnormal_end ${JOB_ID} ${step_name}
	          fi
          fi
        fi
      done < ${datfilename}
    done
  done