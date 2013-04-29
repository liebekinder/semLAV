for i in `grep -l "%27" $1/*`
do
	sed -i "s/%27/\'/" $i
done


