#! /bin/bash
echo "hello" 


for((i=$1;i<=$2;i++));
do

dd if=/dev/urandom of=$4/test"$i".txt bs="$3"KB count=1
done




