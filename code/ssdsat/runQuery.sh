Query=$1

for i in `seq 0 139`; do
    python driver.py -t RW -q /home/gabriela/GUN/GQR/data/data_generated/chain_100qX140v_20PredSpace_8PredBody_4var_10Dtill80v_3Dtill140v_5repMax/data/run_${Query}/views_for_q_${Query}/queryHD_${Query}.txt -v /home/gabriela/GUN/GQR/data/data_generated/chain_100qX140v_20PredSpace_8PredBody_4var_10Dtill80v_3Dtill140v_5repMax/data/run_${Query}/views_for_q_${Query}/view_${i}.txt > salida_chain_${Query}_${i} 2> error_chain_${Query}_${i}
done

