#!/bin/bash

RESULT_FILE=result
TIME="/usr/bin/time -p -o time_file"
OUT=./out
OUT_REF=./out-ref-tema2
EXEC_NAME=../mapreduce.jar
PRINT_LEN=55
COUNT=0
PASS_COUNT=0
FAILED_TEST=0

rm -rf ./out
mkdir ./out
> $RESULT_FILE
echo >> $RESULT_FILE
echo >> $RESULT_FILE
echo >> $RESULT_FILE

function get_test_no {
	printf "%03d. " $COUNT
	COUNT=$(($COUNT + 1))
}

function print_points {
	NUM=$1
	for i in $(seq $NUM)
		do echo -n .
	done
}

function check_test {
	TEST=$1
	VAL=$2
	
	get_test_no
	len=$(($PRINT_LEN - ${#TEST}))
	if [ -n "$VAL" ]
	then
		#echo $2 >> $RESULT_FILE
		echo "$TEST$(print_points $len)FAILED"
		FAILED_TEST=1
	else
		PASS_COUNT=$(($PASS_COUNT + 2))
		echo "$TEST$(print_points $len)PASSED"
	fi
}

function check_consistency {
	TEST=$1
	VAL1=$2
	VAL2=$3

	get_test_no
	len=$(($PRINT_LEN - ${#TEST}))
	if [ "$VAL1" == "$VAL2" ];
	then
		PASS_COUNT=$(($PASS_COUNT + 2))
		echo "$TEST$(print_points $len)PASSED"
	else
		echo "$TEST$(print_points $len)FAILED"
		FAILED_TEST=1
	fi
}

function check_test_scalability {
	TEST=$1
	TIME1=$2
	TIME2=$3
	TIME4=$4
	TIME8=$5

	get_test_no
	len=$(($PRINT_LEN - ${#TEST}))
	t12=$(echo $TIME1'<='$TIME2 | bc -l)
	t24=$(echo $TIME2'<='$TIME4 | bc -l)
	t48=$(echo $TIME4'<='$TIME8 | bc -l)
	sum=$(($t12+$t24+$t48))
	if test $sum -gt 0;
	then
		echo "$TEST$(print_points $len)FAILED"
	else
		PASS_COUNT=$(($PASS_COUNT + 2))
		echo "$TEST$(print_points $len)PASSED"
	fi
}

serial_time[1]=1
serial_time[2]=1
serial_time[3]=1
serial_time[4]=10
serial_time[5]=10

if [ ! -f "./mapreduce.jar" ]; then
	echo "Fisierul mapreduce.jar nu a fost gasit probabil au existat probleme la compilare!" >> $RESULT_FILE
	echo >> $RESULT_FILE
	echo >> $RESULT_FILE
	echo >> $RESULT_FILE
	echo "                                      Total  =  [ 0/100.000 ]" >> $RESULT_FILE
	exit 0
fi

for test_file in 1 2 3 4 5
do
	f_in=./test$test_file.txt
	file=`basename "$f_in" `
	outfile=output$test_file.txt
	f_out=../$OUT/$outfile
	f_out_ref=../$OUT_REF/$outfile
	FAILED_TEST=0
	for num_proc in 1 2 4 8
	do
		cd in_tema2/;$TIME java -jar $EXEC_NAME $num_proc $f_in $f_out 2>&1  1>/dev/null
		cd ..
		res_time[$num_proc]=$(cat in_tema2/time_file | grep real |cut -d " " -f2)
		res_time[$num_proc]=`echo ${res_time[$num_proc]} | sed 's/^0*//'`
		output[$num_proc]=$(cat $OUT/$outfile)
		cat $OUT/$outfile | cut -f 1-2 -d\; > output
		cat $OUT_REF/$outfile | cut -f 1-2 -d\; > output_ref
		res_diff="$( diff output output_ref 2>&1 )"
		check_test "Test $file with $num_proc threads" "$res_diff" >> $RESULT_FILE

		rm -rf $f_out time_file
	done
	check_consistency "Consistency test ($file) 1 vs 2 threads" "${output[1]}" "${output[2]}" >> $RESULT_FILE
	check_consistency "Consistency test ($file) 1 vs 4 threads" "${output[1]}" "${output[4]}" >> $RESULT_FILE
	check_consistency "Consistency test ($file) 1 vs 8 threads" "${output[1]}" "${output[8]}" >> $RESULT_FILE
	check_consistency "Consistency test ($file) 4 vs 8 threads" "${output[4]}" "${output[8]}" >> $RESULT_FILE

	check_test_scalability "Test $file T1=${res_time[1]} T2=${res_time[2]} T4=${res_time[4]} T8=${res_time[8]}" ${res_time[1]} ${res_time[2]} ${res_time[4]} ${res_time[8]} >> $RESULT_FILE
done

echo >> $RESULT_FILE
echo >> $RESULT_FILE
echo >> $RESULT_FILE
echo "                                      Total  =  [ $(echo "scale=3; $PASS_COUNT" | bc)/100.000 ]" >> $RESULT_FILE
echo >> $RESULT_FILE
echo >> $RESULT_FILE

